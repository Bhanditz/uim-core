package eu.europeana.uim.orchestration;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.spy;

import java.util.Date;

import org.junit.Before;

import eu.europeana.uim.UIMRegistry;
import eu.europeana.uim.api.ActiveExecution;
import eu.europeana.uim.api.Registry;
import eu.europeana.uim.api.ResourceEngine;
import eu.europeana.uim.api.StorageEngine;
import eu.europeana.uim.api.StorageEngineException;
import eu.europeana.uim.store.Collection;
import eu.europeana.uim.store.Execution;
import eu.europeana.uim.store.MetaDataRecord;
import eu.europeana.uim.store.Provider;
import eu.europeana.uim.store.Request;
import eu.europeana.uim.store.memory.MemoryResourceEngine;
import eu.europeana.uim.store.memory.MemoryStorageEngine;
import eu.europeana.uim.workflow.Workflow;

/**
 * Abstract class for workflow tests. Setting up UIM framework and provides callback method to fill
 * records.
 * 
 * @author Markus Muhr (markus.muhr@kb.nl)
 * @since Mar 22, 2011
 */
public abstract class AbstractWorkflowTest {
    /**
     * registry
     */
    protected Registry              registry;
    /**
     * storage
     */
    protected StorageEngine<Long>   engine;
    /**
     * resource
     */
    protected ResourceEngine        resource;
    /**
     * orchestrator
     */
    protected UIMOrchestrator<Long> orchestrator;

    /**
     * Sets up the orchistrator with a registry and a in memory storage engine.
     * 
     * @throws Exception
     */
    @Before
    public void setUp() throws Exception {
        if (orchestrator == null) {
            registry = new UIMRegistry();

            engine = spy(new MemoryStorageEngine());
            registry.addStorageEngine(engine);
            registry.setConfiguredStorageEngine(MemoryStorageEngine.class.getSimpleName());

            resource = new MemoryResourceEngine();
            registry.addResourceEngine(resource);
            registry.setConfiguredResourceEngine(MemoryResourceEngine.class.getSimpleName());

            UIMWorkflowProcessor<Long> processor = new UIMWorkflowProcessor<Long>(registry);
            orchestrator = new UIMOrchestrator<Long>(registry, processor);
        }
    }

    /**
     * @param count
     * @return request holding test data
     * @throws StorageEngineException
     */
    protected Request<Long> createTestData(int count) throws StorageEngineException {
        Provider<Long> provider0 = engine.createProvider();
        provider0.setMnemonic("TEL");
        provider0.setName("The European Library");
        engine.updateProvider(provider0);

        Collection<Long> collection0 = engine.createCollection(provider0);
        collection0.setMnemonic("a0001");
        collection0.setName("TEL's collection 001");
        engine.updateCollection(collection0);

        Request<Long> request0 = engine.createRequest(collection0, new Date(0));
        engine.updateRequest(request0);

        for (int i = 0; i < count; i++) {
            MetaDataRecord<Long> record = engine.createMetaDataRecord(collection0, "TEL-" + i);
            fillRecord(record, i);
            engine.updateMetaDataRecord(record);
            engine.addRequestRecord(request0, record);
        }

        return request0;
    }

    /**
     * Executes the given workflow while creating a given amount of records and a boolean flag to
     * signal if the records are stored again.
     * 
     * @param w
     * @param count
     * @return execution used for workflow processing
     * @throws InterruptedException
     * @throws StorageEngineException
     */
    protected Execution<Long> executeWorkflow(Workflow w, int count) throws InterruptedException,
            StorageEngineException {
        assertEquals(0, orchestrator.getActiveExecutions().size());

        Request<Long> request = createTestData(count);

        ActiveExecution<Long> execution = orchestrator.executeWorkflow(w, request);
        execution.waitUntilFinished();

        assertEquals(count, execution.getCompletedSize());
        assertEquals(0, execution.getFailureSize());
        assertEquals(count, execution.getScheduledSize());

        return execution.getExecution();
    }

    /**
     * Test class can fill the records as they wish.
     * 
     * @param record
     *            empty record to be filled
     * @param count
     *            running variable to keep track of the created records
     */
    protected abstract void fillRecord(MetaDataRecord<Long> record, int count);
}