package org.theeuropeanlibrary.uim.gui.gwt.server;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import org.theeuropeanlibrary.uim.gui.gwt.client.OrchestrationService;
import org.theeuropeanlibrary.uim.gui.gwt.shared.CollectionDTO;
import org.theeuropeanlibrary.uim.gui.gwt.shared.ExecutionDTO;
import org.theeuropeanlibrary.uim.gui.gwt.shared.ParameterDTO;
import org.theeuropeanlibrary.uim.gui.gwt.shared.ProgressDTO;
import org.theeuropeanlibrary.uim.gui.gwt.shared.ProviderDTO;
import org.theeuropeanlibrary.uim.gui.gwt.shared.StepStatusDTO;
import org.theeuropeanlibrary.uim.gui.gwt.shared.WorkflowDTO;

import eu.europeana.uim.api.ActiveExecution;
import eu.europeana.uim.api.IngestionPlugin;
import eu.europeana.uim.api.LoggingEngine;
import eu.europeana.uim.api.ResourceEngine;
import eu.europeana.uim.api.StorageEngine;
import eu.europeana.uim.api.StorageEngineException;
import eu.europeana.uim.store.Collection;
import eu.europeana.uim.store.DataSet;
import eu.europeana.uim.store.Execution;
import eu.europeana.uim.store.Provider;
import eu.europeana.uim.store.bean.CollectionBean;
import eu.europeana.uim.store.bean.ProviderBean;
import eu.europeana.uim.workflow.Workflow;
import eu.europeana.uim.workflow.WorkflowStart;

/**
 * Orchestration service implementation.
 * 
 * @author Manuel Bernhardt <bernhardt.manuel@gmail.com>
 * @author Markus Muhr (markus.muhr@kb.nl)
 * @since May 11, 2011
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class OrchestrationServiceImpl extends AbstractOSGIRemoteServiceServlet implements
        OrchestrationService {

// private static Logger log = Logger.getLogger(OrchestrationServiceImpl.class.getName());

    /**
     * Creates a new instance of this class.
     */
    public OrchestrationServiceImpl() {
        super();
    }

    private Map<Long, ProviderDTO>  wrappedProviderDTOs  = new HashMap<Long, ProviderDTO>();

    private Map<Long, ExecutionDTO> wrappedExecutionDTOs = new HashMap<Long, ExecutionDTO>();

    @Override
    public List<WorkflowDTO> getWorkflows() {
        List<WorkflowDTO> res = new ArrayList<WorkflowDTO>();
        List<eu.europeana.uim.workflow.Workflow> workflows = getEngine().getRegistry().getWorkflows();
        if (workflows != null) {
            for (eu.europeana.uim.workflow.Workflow w : workflows) {
                WorkflowDTO wd = new WorkflowDTO(w.getName(), w.getDescription());
                res.add(wd);
            }
        }
        return res;
    }

    @Override
    public List<ParameterDTO> getParameters(Long provider, Long collection, String workflow) {
        List<ParameterDTO> res = new ArrayList<ParameterDTO>();
        if (workflow != null) {
            Workflow w = getWorkflow(workflow);
            WorkflowStart start = w.getStart();

            List<String> params = new ArrayList<String>();
            params.addAll(start.getParameters());
            for (IngestionPlugin i : w.getSteps()) {
                params.addAll(i.getParameters());
            }

            ResourceEngine<Long> resource = (ResourceEngine<Long>)getEngine().getRegistry().getResourceEngine();
            LinkedHashMap<String, List<String>> globalResources = resource.getGlobalResources(params);
            for (String param : params) {
                if (!globalResources.containsKey(param)) {
                    globalResources.put(param, new ArrayList<String>());
                }
            }

            if (collection != null) {
                LinkedHashMap<String, List<String>> collectionResources = resource.getCollectionResources(
                        new CollectionBean<Long>(collection, new ProviderBean<Long>(provider)),
                        params);
                if (collectionResources != null && collectionResources.size() > 0) {
                    for (String key : collectionResources.keySet()) {
                        globalResources.remove(key);
                    }
                    globalResources.putAll(collectionResources);
                }
            }
            if (provider != null) {
                LinkedHashMap<String, List<String>> providerResources = resource.getProviderResources(
                        new ProviderBean<Long>(provider), params);
                if (providerResources != null && providerResources.size() > 0) {
                    for (String key : providerResources.keySet()) {
                        globalResources.remove(key);
                    }
                    globalResources.putAll(providerResources);
                }
            }

            for (Entry<String, List<String>> entry : globalResources.entrySet()) {
                res.add(new ParameterDTO(entry.getKey(), entry.getValue().toArray(
                        new String[entry.getValue().size()])));
            }
        }
        return res;
    }

    @Override
    public Boolean setParameters(ParameterDTO parameter, Long provider, Long collection,
            String workflow) {
        Boolean res = true;

        LinkedHashMap<String, List<String>> values = new LinkedHashMap<String, List<String>>();
        values.put(parameter.getKey(), Arrays.asList(parameter.getValues()));
        ResourceEngine<Long> resource = (ResourceEngine<Long>)getEngine().getRegistry().getResourceEngine();

        if (provider == null && collection == null && workflow != null) {
            resource.setGlobalResources(values);
        } else if (provider != null && collection != null) {
            resource.setCollectionResources(new CollectionBean<Long>(collection,
                    new ProviderBean<Long>(provider)), values);
        } else if (provider != null && collection == null) {
            resource.setProviderResources(new ProviderBean<Long>(provider), values);
        } else {
            res = false;
        }

        return res;
    }

    @Override
    public List<ProviderDTO> getProviders() {
        List<ProviderDTO> res = new ArrayList<ProviderDTO>();
        try {
            StorageEngine<Long> storage = (StorageEngine<Long>)getEngine().getRegistry().getStorage();
            List<Provider<Long>> providers = storage.getAllProviders();
            if (providers != null) {
                for (Provider<Long> p : providers) {
                    ProviderDTO provider = getWrappedProviderDTO(p.getId());
                    res.add(provider);
                }
            }
        } catch (StorageEngineException e) {
            res.add(new ProviderDTO(0l, "Failed to load provider. Notify system administrator.",
                    null));
            throw new RuntimeException(e);
        }
        return res;
    }

    @Override
    public List<CollectionDTO> getCollections(Long provider) {
        List<CollectionDTO> res = new ArrayList<CollectionDTO>();
        try {
            StorageEngine<Long> storage = (StorageEngine<Long>)getEngine().getRegistry().getStorage();
            Provider<Long> p = storage.getProvider(provider);
            List<Collection<Long>> cols = storage.getCollections(p);
            for (Collection<Long> col : cols) {
                res.add(new CollectionDTO(col.getId(), col.getName(), col.getMnemonic(),
                        getWrappedProviderDTO(provider), storage.getTotalByCollection(col)));
            }
        } catch (StorageEngineException e) {
            e.printStackTrace();
        }
        return res;
    }

    @Override
    public List<CollectionDTO> getAllCollections() {
        List<CollectionDTO> res = new ArrayList<CollectionDTO>();
        try {
            StorageEngine<Long> storage = (StorageEngine<Long>)getEngine().getRegistry().getStorage();
            List<Collection<Long>> cols = storage.getAllCollections();
            for (Collection<Long> col : cols) {
                res.add(new CollectionDTO(col.getId(), col.getName(), col.getMnemonic(),
                        getWrappedProviderDTO(col.getProvider().getId()),
                        storage.getTotalByCollection(col)));
            }
        } catch (StorageEngineException e) {
            e.printStackTrace();
        }
        return res;
    }

    @Override
    public List<ExecutionDTO> getActiveExecutions() {
        List<ExecutionDTO> r = new ArrayList<ExecutionDTO>();
        java.util.Collection<ActiveExecution<?>> activeExecutions = getEngine().getRegistry().getOrchestrator().getActiveExecutions();
        for (ActiveExecution<?> execution : activeExecutions) {
            ExecutionDTO exec = getWrappedExecutionDTO((Long)execution.getId(), execution);
            r.add(exec);
        }
        return r;
    }

    @Override
    public List<ExecutionDTO> getPastExecutions() {
        List<ExecutionDTO> r = new ArrayList<ExecutionDTO>();
        try {
            StorageEngine<Long> storage = (StorageEngine<Long>)getEngine().getRegistry().getStorage();
            for (Execution<Long> execution : storage.getAllExecutions()) {
                if (!execution.isActive()) {
                    r.add(getWrappedExecutionDTO(execution.getId(), execution));
                }
            }
        } catch (StorageEngineException e) {
            e.printStackTrace();
        }
        return r;
    }

    @Override
    public ExecutionDTO startCollection(String workflow, Long collection, String executionName,
            Set<ParameterDTO> parameters) {
        try {
            StorageEngine<Long> storage = (StorageEngine<Long>)getEngine().getRegistry().getStorage();
            Collection<Long> c = storage.getCollection(collection);
            if (c == null) { throw new RuntimeException("Error: cannot find collection " +
                                                        collection); }
            eu.europeana.uim.workflow.Workflow w = getWorkflow(workflow);
            ExecutionDTO execution = new ExecutionDTO();

            GWTProgressMonitor monitor = new GWTProgressMonitor(execution);

            ActiveExecution<Long> ae;
            if (parameters != null) {
                Properties properties = prepareProperties(parameters);
                ae = (ActiveExecution<Long>)getEngine().getRegistry().getOrchestrator().executeWorkflow(w, c,
                        properties);
            } else {
                ae = (ActiveExecution<Long>)getEngine().getRegistry().getOrchestrator().executeWorkflow(w, c);
            }
            ae.getMonitor().addListener(monitor);
            populateWrappedExecutionDTO(execution, ae, w, c, executionName);

            return execution;
        } catch (StorageEngineException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    private Properties prepareProperties(Set<ParameterDTO> parameters) {
        Properties properties = new Properties();
        for (ParameterDTO parameter : parameters) {
            if (parameter.getValues() != null) {
                if (parameter.getValues().length > 1) {
                    StringBuilder b = new StringBuilder();
                    for (String val : parameter.getValues()) {
                        b.append(val);
                        b.append(",");
                    }
                    b.deleteCharAt(b.length() - 1);
                    properties.put(parameter.getKey(), b.toString());
                } else if (parameter.getValues().length == 1) {
                    properties.put(parameter.getKey(), parameter.getValues()[0]);
                }
            }
        }
        return properties;
    }

    @Override
    public ExecutionDTO startProvider(String workflow, Long provider, String executionName,
            Set<ParameterDTO> parameters) {
        try {
            StorageEngine<Long> storage = (StorageEngine<Long>)getEngine().getRegistry().getStorage();
            Provider<Long> p = storage.getProvider(provider);
            if (p == null) { throw new RuntimeException("Error: cannot find provider " + provider); }
            eu.europeana.uim.workflow.Workflow w = getWorkflow(workflow);
            ExecutionDTO execution = new ExecutionDTO();
            GWTProgressMonitor monitor = new GWTProgressMonitor(execution);

            ActiveExecution<Long> ae;
            if (parameters != null) {
                Properties properties = prepareProperties(parameters);
                ae = (ActiveExecution<Long>)getEngine().getRegistry().getOrchestrator().executeWorkflow(w, p,
                        properties);
            } else {
                ae = (ActiveExecution<Long>)getEngine().getRegistry().getOrchestrator().executeWorkflow(w, p);
            }
            ae.setName(executionName);

            ae.getMonitor().addListener(monitor);

            populateWrappedExecutionDTO(execution, ae, w, p, executionName);
            return execution;
        } catch (StorageEngineException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void populateWrappedExecutionDTO(ExecutionDTO execution, ActiveExecution<Long> ae,
            eu.europeana.uim.workflow.Workflow w, DataSet<Long> dataset, String executionName) {
        execution.setId(ae.getId());
        execution.setName(executionName);
        execution.setWorkflow(w.getName());
        execution.setCompleted(ae.getCompletedSize());
        execution.setFailure(ae.getFailureSize());
        execution.setScheduled(ae.getScheduledSize());
        execution.setCanceled(ae.isCanceled());
        execution.setStartTime(ae.getStartTime());
        execution.setDataSet(dataset.toString());

        ProgressDTO progress = new ProgressDTO();
        progress.setWork(ae.getTotalSize());
        progress.setWorked(ae.getFailureSize() + ae.getCompletedSize());
        progress.setTask(ae.getMonitor().getTask());
        progress.setSubtask(ae.getMonitor().getSubtask());

        execution.setProgress(progress);

        wrappedExecutionDTOs.put(ae.getId(), execution);
    }

    @Override
    public ExecutionDTO getExecution(Long id) {
        ExecutionDTO exec = null;
        ActiveExecution<?> ae = getEngine().getRegistry().getOrchestrator().getActiveExecution(id);
        if (ae != null) {
            exec = getWrappedExecutionDTO((Long)ae.getId(), ae);
        } else {
            StorageEngine<Long> storage = (StorageEngine<Long>)getEngine().getRegistry().getStorage();
            Execution<Long> execution;
            try {
                execution = storage.getExecution(id);
                exec = getWrappedExecutionDTO(execution.getId(), execution);
            } catch (StorageEngineException e) {
                e.printStackTrace();
            }
        }
        return exec;
    }

    private ProviderDTO getWrappedProviderDTO(Long provider) {
        StorageEngine<Long> storage = (StorageEngine<Long>)getEngine().getRegistry().getStorage();
        ProviderDTO wrapped = wrappedProviderDTOs.get(provider);
        if (wrapped == null) {
            try {
                Provider<Long> p = storage.getProvider(provider);
                wrapped = new ProviderDTO(p.getId(), p.getName(), p.getMnemonic());
                wrappedProviderDTOs.put(provider, wrapped);
            } catch (StorageEngineException e) {
                e.printStackTrace();
            }
        }
        return wrapped;
    }

    private ExecutionDTO getWrappedExecutionDTO(Long execution, Execution e) {
        ExecutionDTO wrapped = wrappedExecutionDTOs.get(execution);
        if (wrapped == null) {
            wrapped = new ExecutionDTO();
            wrapped.setId(execution);
            wrapped.setStartTime(e.getStartTime());
            wrapped.setDataSet(e.getDataSet().toString());
            wrapped.setName(e.getName());
            wrapped.setWorkflow(e.getWorkflowName());
            wrapped.setProgress(new ProgressDTO());
            wrappedExecutionDTOs.put(execution, wrapped);
        }
        // update what may have changed
        wrapped.setActive(e.isActive());
        if (e.getEndTime() != null) {
            wrapped.setEndTime(e.getEndTime());
        }
        wrapped.setCanceled(e.isCanceled());
        if (e.isActive() && e instanceof ActiveExecution) {
            ActiveExecution ae = (ActiveExecution)e;
            wrapped.setScheduled(ae.getScheduledSize());
            wrapped.setCompleted(ae.getCompletedSize());
            wrapped.setFailure(ae.getFailureSize());
            wrapped.setPaused(ae.isPaused());

            ProgressDTO progress = wrapped.getProgress();
            progress.setWork(ae.getTotalSize());
            progress.setWorked(ae.getFailureSize() + ae.getCompletedSize());
            progress.setTask(ae.getMonitor().getTask());
            progress.setSubtask(ae.getMonitor().getSubtask());
            progress.setDone(!e.isActive());
        } else if (!e.isActive()) {
            wrapped.setScheduled(e.getProcessedCount());
            wrapped.setCompleted(e.getSuccessCount());
            wrapped.setFailure(e.getErrorCount());
        }
        return wrapped;
    }

    private eu.europeana.uim.workflow.Workflow getWorkflow(String name) {
        eu.europeana.uim.workflow.Workflow workflow = getEngine().getRegistry().getWorkflow(name);
        if (workflow == null) { throw new RuntimeException("Error: cannot find workflow " +
                                                           workflow); }
        return workflow;
    }

    @Override
    public Integer getCollectionTotal(Long collection) {
        try {
            StorageEngine<Long> storage = (StorageEngine<Long>)getEngine().getRegistry().getStorage();
            return storage.getTotalByCollection(storage.getCollection(collection));
        } catch (StorageEngineException e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public List<StepStatusDTO> getStatus(String workflow) {
        List<StepStatusDTO> res = new ArrayList<StepStatusDTO>();
        /*
         * FIXME re-implement this List<WorkflowStepStatusDTO> runtimeStatus =
         * getEngine().getOrchestrator().getRuntimeStatus(getWorkflow(workflow)); for
         * (WorkflowStepStatusDTO wss : runtimeStatus) { StepStatusDTO ss = new
         * StepStatusDTO(wss.getStep().getIdentifier(), (wss.getParent() != null ?
         * wss.getParent().getIdentifier() : null), wss.queueSize(), wss.successes(),
         * wss.failures()); res.add(ss); }
         */
        return res;
    }

    @Override
    public List<String> getResourceFileNames() {
        List<String> fileNames = new ArrayList<String>();
        ResourceEngine<Long> resource = (ResourceEngine<Long>)getEngine().getRegistry().getResourceEngine();
        File rootDirectory = resource.getRootDirectory();
        if (rootDirectory != null && rootDirectory.exists() && rootDirectory.isDirectory()) {
            for (File file : rootDirectory.listFiles()) {
                fileNames.add(file.getName());
            }
        }
        return fileNames;
    }

    @Override
    public Boolean pauseExecution(Long execution) {
        ActiveExecution<Long> ae = getEngine().getRegistry().getOrchestrator().getActiveExecution(execution);
        if (ae != null) {
            getEngine().getRegistry().getOrchestrator().pause(ae);
            return ae.isPaused();
        } else {
            return false;
        }
    }

    @Override
    public Boolean resumeExecution(Long execution) {
        ActiveExecution<Long> ae = getEngine().getRegistry().getOrchestrator().getActiveExecution(execution);
        if (ae != null) {
            getEngine().getRegistry().getOrchestrator().resume(ae);
            return !ae.isPaused();
        } else {
            return false;
        }
    }

    @Override
    public Boolean cancelExecution(Long execution) {
        ActiveExecution<Long> ae = getEngine().getRegistry().getOrchestrator().getActiveExecution(execution);
        if (ae != null) {
            getEngine().getRegistry().getOrchestrator().cancel(ae);
            return ae.isCanceled();
        } else {
            return false;
        }
    }
}
