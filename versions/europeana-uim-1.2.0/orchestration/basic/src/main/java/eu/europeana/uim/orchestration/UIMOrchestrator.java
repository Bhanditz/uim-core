package eu.europeana.uim.orchestration;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import eu.europeana.uim.api.ActiveExecution;
import eu.europeana.uim.api.IngestionPlugin;
import eu.europeana.uim.api.LoggingEngine;
import eu.europeana.uim.api.Orchestrator;
import eu.europeana.uim.api.Registry;
import eu.europeana.uim.api.ResourceEngine;
import eu.europeana.uim.api.StorageEngineException;
import eu.europeana.uim.common.RevisableProgressMonitor;
import eu.europeana.uim.orchestration.processing.TaskExecutorRegistry;
import eu.europeana.uim.store.Collection;
import eu.europeana.uim.store.Execution;
import eu.europeana.uim.store.MetaDataRecord;
import eu.europeana.uim.store.Provider;
import eu.europeana.uim.store.Request;
import eu.europeana.uim.store.UimDataSet;
import eu.europeana.uim.workflow.Workflow;
import eu.europeana.uim.workflow.WorkflowStart;

/**
 * Orchestrates the ingestion job execution. The orchestrator keeps a map of WorkflowProcessors, one
 * for each different workflow. When a new request for workflow execution comes in, the
 * WorkflowProcessor for the Workflow is retrieved, or created if it does not exist.
 * 
 * @author Markus Muhr (markus.muhr@kb.nl)
 * @since Mar 22, 2011
 */
public class UIMOrchestrator implements Orchestrator {
    private static Logger              log = Logger.getLogger(UIMOrchestrator.class.getName());

    private final Registry             registry;
    private final UIMWorkflowProcessor processor;

    /**
     * Creates a new instance of this class.
     * 
     * @param registry
     * @param processor
     */
    public UIMOrchestrator(Registry registry, UIMWorkflowProcessor processor) {
        this.registry = registry;
        this.processor = processor;

        processor.startup();
    }

    @Override
    public String getIdentifier() {
        return UIMOrchestrator.class.getSimpleName();
    }

    /**
     * Executes a given workflow. A new Execution is created and a WorkflowProcessor created if none
     * exists for this workflow. Note, this method queries the registered resource engine for
     * properties known to the plugins of the workflow.
     * 
     * @param w
     *            the workflow to execute
     * @param dataset
     *            the data set on which this Execution runs
     * @return a new ActiveExecution for this execution request
     */
    @Override
    public ActiveExecution<?> executeWorkflow(Workflow w, UimDataSet<?> dataset) {
        return executeWorkflow(w, dataset, new Properties());
    }

    /**
     * Executes a given workflow. A new Execution is created and a WorkflowProcessor created if none
     * exists for this workflow. Note, this method queries the registered resource engine for
     * properties known to the plugins of the workflow for all not given properties, but necessary
     * for the plugins.
     * 
     * @param w
     *            the workflow to execute
     * @param dataset
     *            the data set on which this Execution runs
     * @return a new ActiveExecution for this execution request
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public ActiveExecution<?> executeWorkflow(Workflow w, UimDataSet dataset, Properties properties) {
        setupProperties(w, dataset, properties);

        RevisableProgressMonitor monitor = new RevisableProgressMonitor();
        monitor.beginTask(w.getName(), 1);

        try {
            Execution e = registry.getStorageEngine().createExecution(dataset, w.getIdentifier());
            e.setActive(true);
            e.setStartTime(new Date());
            registry.getStorageEngine().updateExecution(e);

            try {
                UIMActiveExecution activeExecution = new UIMActiveExecution(e, w,
                        registry.getStorageEngine(), registry.getLoggingEngine(),
                        registry.getResourceEngine(), properties, monitor);
                processor.schedule(activeExecution);

                if (registry.getLoggingEngine() != null)
                    registry.getLoggingEngine().log("UIMOrchestrator", e, "start", LoggingEngine.Level.INFO, "Started:" + activeExecution.getName());
                return activeExecution;
            } catch (Throwable t) {
                log.log(Level.SEVERE, "Could not update execution details: " + t.getMessage(), t);
            }
        } catch (StorageEngineException e1) {
            log.log(Level.SEVERE, "Could not update execution details: " + e1.getMessage(), e1);
            e1.printStackTrace();
        }
        return null;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private void setupProperties(Workflow w, UimDataSet<?> dataset, Properties properties) {
        ResourceEngine<?> resourceEngine = this.registry.getResourceEngine();
        if (resourceEngine != null) {
            Collection collection = null;
            Provider provider = null;
            if (dataset instanceof MetaDataRecord<?>){
                collection = ((MetaDataRecord<?>)dataset).getCollection();
                provider = collection.getProvider();
            } else if (dataset instanceof Request<?>) {
                collection = ((Request<?>)dataset).getCollection();
                provider = collection.getProvider();
                
            } else if (dataset instanceof Collection<?>){
                collection = ((Collection<?>)dataset);
                provider = collection.getProvider();
            }
            
            List<String> params = new ArrayList<String>();
            WorkflowStart start = w.getStart();
            params.addAll(start.getParameters());
            for (IngestionPlugin i : w.getSteps()) {
                params.addAll(i.getParameters());
            }

            LinkedHashMap<String, List<String>> globalResources = resourceEngine.getGlobalResources(params);

            
            
            if (collection != null) {
                LinkedHashMap<String, List<String>> collectionResources = resourceEngine.getCollectionResources(
                        collection, params);
                if (collectionResources != null && collectionResources.size() > 0) {
                    for (Entry<String, List<String>> entry : collectionResources.entrySet()) {
                        if (entry.getValue() != null) {
                            globalResources.put(entry.getKey(), entry.getValue());
                        }
                    }
                }
            }
            if (provider != null) {
                LinkedHashMap<String, List<String>> providerResources = resourceEngine.getProviderResources(provider, params);
                if (providerResources != null && providerResources.size() > 0) {
                    for (Entry<String, List<String>> entry : providerResources.entrySet()) {
                        if (entry.getValue() != null) {
                            globalResources.put(entry.getKey(), entry.getValue());
                        }
                    }
                }
            }

            for (Entry<String, List<String>> entry : globalResources.entrySet()) {
                if (!properties.containsKey(entry.getKey())) {
                    if (entry.getValue() != null) {
                        if (entry.getValue().size() > 1) {
                            StringBuilder b = new StringBuilder();
                            for (String val : entry.getValue()) {
                                b.append(val);
                                b.append(",");
                            }
                            b.deleteCharAt(b.length() - 1);
                            properties.put(entry.getKey(), b.toString());
                        } else if (entry.getValue().size() == 1) {
                            properties.put(entry.getKey(), entry.getValue().get(0));
                        }
                    }
                }
            }
        }
    }

    @Override
    public List<ActiveExecution<?>> getActiveExecutions() {
        return processor.getExecutions();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <I> ActiveExecution<I> getActiveExecution(I id) {
        for (ActiveExecution<?> ae : processor.getExecutions()) {
            if (ae.getId().equals(id)) { return (ActiveExecution<I>)ae; }
        }
        return null;
    }

    @Override
    public void pause(ActiveExecution<?> execution) {
        execution.setPaused(true);
    }

    @Override
    public void resume(ActiveExecution<?> execution) {
        execution.setPaused(false);
    }

    @Override
    public void cancel(ActiveExecution<?> execution) {
        execution.getMonitor().setCancelled(true);
    }

    @Override
    public void shutdown() {
        processor.shutdown();
        TaskExecutorRegistry.getInstance().shutdown();
    }
}
