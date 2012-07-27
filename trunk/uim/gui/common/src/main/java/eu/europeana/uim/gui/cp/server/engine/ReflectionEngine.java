package eu.europeana.uim.gui.cp.server.engine;

import eu.europeana.uim.api.IngestionPlugin;
import eu.europeana.uim.api.LoggingEngine;
import eu.europeana.uim.api.Orchestrator;
import eu.europeana.uim.api.Registry;
import eu.europeana.uim.api.ResourceEngine;
import eu.europeana.uim.api.StorageEngine;
import eu.europeana.uim.repox.RepoxService;
import eu.europeana.uim.sugarcrm.SugarService;
import eu.europeana.uim.util.SampleProperties;
import eu.europeana.uim.workflow.Workflow;

/**
 * This class map's a reflection based setup of the UIM framework. Everything is reflection because,
 * otherwise we would need to have the dependencies in the bundle - which we do not want.
 * 
 * This class is effectively used to mock the framework during development of the GWT application. A
 * developer can "add" dependencies in the IDE without changing dependencies of the bundle - so that
 * the SOA is not invalidated.
 * 
 * @author Andreas Juffinger (andreas.juffinger@kb.nl)
 * @since May 11, 2011
 */
@SuppressWarnings("rawtypes")
public class ReflectionEngine extends ExternalServiceEngine {
    private Registry        registry;
    private Orchestrator    ochestrator;
    private RepoxService    repoxService;
    private SugarService    sugarService;

    private static String   configuredStorageEngine = "MemoryStorageEngine";

    private static String[] storage                 = new String[] { "eu.europeana.uim.store.memory.MemoryStorageEngine" };

    private static String[] resource                = new String[] { "eu.europeana.uim.store.memory.MemoryResourceEngine" };

    private static String[] logging                 = new String[] { "eu.europeana.uim.logging.memory.MemoryLoggingEngine" };

    private static String[] plugins                 = new String[] { "eu.europeana.uim.workflows.SysoutPlugin" };

    private static String[] workflows               = new String[] { "eu.europeana.uim.workflows.SysoutWorkflow" };

    /**
     * Creates a new instance of this class.
     */
    public ReflectionEngine() {
        try {
            Class<?> registryClazz = Class.forName("eu.europeana.uim.UIMRegistry");
            registry = (Registry)registryClazz.newInstance();

            Class<?> orchestratorClazz = Class.forName("eu.europeana.uim.orchestration.UIMOrchestrator");
            Class<?> workflowClazz = Class.forName("eu.europeana.uim.orchestration.UIMWorkflowProcessor");
            Object workflowObject = workflowClazz.getConstructor(Registry.class).newInstance(
                    registry);
            ochestrator = (Orchestrator)orchestratorClazz.getConstructor(Registry.class,
                    workflowObject.getClass()).newInstance(registry, workflowObject);

            registry.setOrchestrator(ochestrator);

            Class<?> repoxServiceClazz = Class.forName("eu.europeana.uim.repox.rest.RepoxServiceImpl");
            repoxService = (RepoxService)repoxServiceClazz.newInstance();

            Class<?> sugarServiceClazz = Class.forName("eu.europeana.uim.Sugar.SugarServiceImpl");
            sugarService = (SugarService)sugarServiceClazz.newInstance();
        } catch (Throwable e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        setupStorage();
        setupResource();
        setupLogging();
        setupPlugins();
        setupWorkflows();

        setupSampleData();
// try {
// StorageEngine<?> storageEngine = registry.getStorageEngine();
// List<?> colls = storageEngine.getAllCollections();
//
// MetaDataRecord<?> mdr = storageEngine.createMetaDataRecord((Collection)colls.get(0), "test1");
// storageEngine.updateMetaDataRecord((MetaDataRecord)mdr);
// mdr = storageEngine.createMetaDataRecord((Collection)colls.get(0), "test2");
// storageEngine.updateMetaDataRecord((MetaDataRecord)mdr);
// } catch (Exception e) {
//
// }
    }

    private void setupSampleData() {
        try {
            SampleProperties sample = new SampleProperties();
            sample.loadSampleData(registry.getStorageEngine());

// Collection coll = registry.getStorage().getAllCollections().iterator().next();
// System.out.println(coll.getName());
// Request req = registry.getStorage().createRequest(coll, new Date());
// for (int i = 0; i < 10000; i++) {
// MetaDataRecord mdr = registry.getStorage().createMetaDataRecord(req, "Record " + i);
// registry.getStorage().updateMetaDataRecord(mdr);
// }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private void setupWorkflows() {
        for (String name : workflows) {
            try {
                Class<?> clazz = Class.forName(name);
                Workflow workflow = (Workflow)clazz.getConstructor(Registry.class).newInstance(
                        registry);
                registry.addWorkflow(workflow);
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }

    private void setupPlugins() {
        for (String name : plugins) {
            try {
                Class<?> clazz = Class.forName(name);
                try {
                    IngestionPlugin plugin = (IngestionPlugin)clazz.getConstructor(Registry.class).newInstance(
                            registry);
                    registry.addPlugin(plugin);
                } catch (NoSuchMethodException e) {

                    IngestionPlugin plugin = (IngestionPlugin)clazz.newInstance();
                    registry.addPlugin(plugin);
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }

    private void setupLogging() {
        for (String name : logging) {
            try {
                Class<?> clazz = Class.forName(name);
                LoggingEngine<?> logging = (LoggingEngine<?>)clazz.newInstance();
                registry.addLoggingEngine(logging);
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }

    private void setupStorage() {
        for (String name : storage) {
            try {
                Class<?> clazz = Class.forName(name);
                StorageEngine storage = (StorageEngine)clazz.newInstance();
                registry.addStorageEngine(storage);
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
        registry.setConfiguredStorageEngine(configuredStorageEngine);
    }

    private void setupResource() {
        for (String name : resource) {
            try {
                Class<?> clazz = Class.forName(name);
                ResourceEngine resource = (ResourceEngine)clazz.newInstance();
                registry.addResourceEngine(resource);
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
        registry.setConfiguredStorageEngine(configuredStorageEngine);
    }

    @Override
    public Registry getRegistry() {
        return registry;
    }

    @Override
    public RepoxService getRepoxService() {
        return repoxService;
    }

    @Override
    public SugarService getSugarService() {
        return sugarService;
    }
}
