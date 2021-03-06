package eu.europeana.uim.orchestration;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Properties;

import junit.framework.Assert;

import org.junit.Test;

import eu.europeana.uim.api.ActiveExecution;
import eu.europeana.uim.api.StorageEngine;
import eu.europeana.uim.api.StorageEngineException;
import eu.europeana.uim.common.MDRFieldRegistry;
import eu.europeana.uim.store.Collection;
import eu.europeana.uim.store.MetaDataRecord;
import eu.europeana.uim.store.Provider;
import eu.europeana.uim.store.Request;
import eu.europeana.uim.workflow.Workflow;
import eu.europeana.uim.workflows.MixedWorkflow;
import eu.europeana.uim.workflows.SyserrWorkflow;
import eu.europeana.uim.workflows.SysoutPlugin;
import eu.europeana.uim.workflows.SysoutWorkflow;

/**
 * Tests UIM orchestration.
 * 
 * @author Markus Muhr (markus.muhr@kb.nl)
 * @since Mar 22, 2011
 */
@SuppressWarnings("unchecked")
public class UIMOrchestratorTest extends AbstractBatchWorkflowTest {
    /**
     * Tests success of basic setup of orchestrator.o
     * 
     * @throws InterruptedException
     * @throws StorageEngineException
     */
    @Test
    public void testSimpleSuccessSetup() throws InterruptedException, StorageEngineException {
        assertEquals(0, orchestrator.getActiveExecutions().size());

        Request<Long> request = createTestData(engine, 1);

        // creating the data calles 20 times the update method.
        verify(engine, times(1)).updateMetaDataRecord(any(MetaDataRecord.class));

        Workflow w = new SysoutWorkflow();

        ActiveExecution<Long> execution0 = orchestrator.executeWorkflow(w, request);
        execution0.waitUntilFinished();

        // each delivered metadata record is saved
        verify(engine, times(2)).updateMetaDataRecord(any(MetaDataRecord.class));

        assertEquals(1, execution0.getCompletedSize());
        assertEquals(0, execution0.getFailureSize());
        assertEquals(1, execution0.getScheduledSize());
    }

    /**
     * Tests automatically pausing option for too many executions.
     * 
     * @throws InterruptedException
     * @throws StorageEngineException
     */
    @Test
    public void testExecutionQueuing() throws InterruptedException, StorageEngineException {
        Assert.assertEquals(0, orchestrator.getActiveExecutions().size());

        Request<Long> request = createTestData(engine, 1);

        Workflow w = new SysoutWorkflow();

        Properties properties = new Properties();
        properties.setProperty(SysoutPlugin.RANDOM_SLEEP, "true");
        properties.setProperty(SysoutPlugin.SLEEP_RANGES, "500-1000");

        ActiveExecution<Long> execution0 = orchestrator.executeWorkflow(w, request, properties);
        ActiveExecution<Long> execution1 = orchestrator.executeWorkflow(w, request, properties);
        ActiveExecution<Long> execution2 = orchestrator.executeWorkflow(w, request, properties);
        ActiveExecution<Long> execution3 = orchestrator.executeWorkflow(w, request, properties);
        ActiveExecution<Long> execution4 = orchestrator.executeWorkflow(w, request, properties);
        ActiveExecution<Long> execution5 = orchestrator.executeWorkflow(w, request, properties);

        Assert.assertEquals(6, orchestrator.getActiveExecutions().size());
        int countPaused = 0;
        for (ActiveExecution<Long> activeExecution : orchestrator.getActiveExecutions()) {
            if (activeExecution.isPaused()) {
                countPaused++;
            }
        }
        Assert.assertEquals(2, countPaused);

        execution0.waitUntilFinished();
        execution1.waitUntilFinished();
        execution2.waitUntilFinished();
        execution3.waitUntilFinished();
        execution4.waitUntilFinished();
        execution5.waitUntilFinished();
        
        Assert.assertEquals(0, orchestrator.getActiveExecutions().size());
    }

    /**
     * Tests failed setup.
     * 
     * @throws InterruptedException
     * @throws StorageEngineException
     */
    @Test
    public void testSimpleFailedSetup() throws InterruptedException, StorageEngineException {
        assertEquals(0, orchestrator.getActiveExecutions().size());

        Request<Long> request = createTestData((StorageEngine<Long>)registry.getStorageEngine(), 21);
        // creating the data calles 21 times the update method.
        verify(engine, times(21)).updateMetaDataRecord(any(MetaDataRecord.class));

        Workflow w = new SyserrWorkflow(7, true);

        ActiveExecution<Long> execution0 = orchestrator.executeWorkflow(w, request);
        execution0.waitUntilFinished();

        // each failed metadata record is saved once 21 + original count of 21
        verify(engine, times(42)).updateMetaDataRecord(any(MetaDataRecord.class));

        assertEquals(21, execution0.getCompletedSize() + execution0.getFailureSize());
        assertEquals(21, execution0.getScheduledSize());
    }

    /**
     * Tests partial failures.
     * 
     * @throws InterruptedException
     * @throws StorageEngineException
     */
    @Test
    public void testSimplePartlyFailedSetup() throws InterruptedException, StorageEngineException {
        assertEquals(0, orchestrator.getActiveExecutions().size());

        Request<Long> request = createTestData((StorageEngine<Long>)registry.getStorageEngine(), 30);
        // creating the data calles 30 times the update method.
        verify(engine, times(30)).updateMetaDataRecord(any(MetaDataRecord.class));

        Workflow w = new MixedWorkflow(7, true);

        ActiveExecution<Long> execution0 = orchestrator.executeWorkflow(w, request);
        execution0.waitUntilFinished();

        // each failed metadata record is saved once 30 + original count of 30
        verify(engine, times(60)).updateMetaDataRecord(any(MetaDataRecord.class));

        assertEquals(20, execution0.getCompletedSize());
        assertEquals(10, execution0.getFailureSize());
        assertEquals(30, execution0.getScheduledSize());
    }

    /**
     * Tests partial failures.
     * 
     * @throws InterruptedException
     * @throws StorageEngineException
     */
    @Test
    public void testSimplePluginFailedSetup() throws InterruptedException, StorageEngineException {
        assertEquals(0, orchestrator.getActiveExecutions().size());

        Request<Long> request = createTestData((StorageEngine<Long>)registry.getStorageEngine(), 30);
        // creating the data calles 30 times the update method.
        verify(engine, times(30)).updateMetaDataRecord(any(MetaDataRecord.class));

        Workflow w = new MixedWorkflow(7, true);

        Properties properties = new Properties();
        properties.setProperty("syserr.fullfailure", "true");

        ActiveExecution<Long> execution0 = orchestrator.executeWorkflow(w, request, properties);
        execution0.waitUntilFinished();

        // each failed metadata record is saved once 30 + original count of 30
        // cannot sy this - depends on when the shutdown happens.
        // verify(engine, times(32)).updateMetaDataRecord(any(MetaDataRecord.class));

        assertEquals(0, execution0.getCompletedSize());
        // assertEquals(2, execution0.getFailureSize());
        assertEquals(30, execution0.getScheduledSize());
    }

    /**
     * Tests partial failures.
     * 
     * @throws InterruptedException
     * @throws StorageEngineException
     */
    @Test
    public void testResourcesSetup() throws InterruptedException, StorageEngineException {
        assertEquals(0, orchestrator.getActiveExecutions().size());

        Request<Long> request = createTestData((StorageEngine<Long>)registry.getStorageEngine(), 30);
        // creating the data calles 30 times the update method.
        verify(engine, times(30)).updateMetaDataRecord(any(MetaDataRecord.class));

        Workflow w = new SysoutWorkflow();

        LinkedHashMap<String, List<String>> resources = new LinkedHashMap<String, List<String>>();
        ArrayList<String> list = new ArrayList<String>();
        list.add("true");
        resources.put("sysout.random.sleep", list);
        resource.setGlobalResources(resources);

        ActiveExecution<Long> execution0 = orchestrator.executeWorkflow(w, request);
        execution0.waitUntilFinished();
        Assert.assertEquals("true", execution0.getProperties().get("sysout.random.sleep"));

        list.set(0, "false");
        resource.setWorkflowResources(w, resources);

        execution0 = orchestrator.executeWorkflow(w, request);
        execution0.waitUntilFinished();
        Assert.assertEquals("false", execution0.getProperties().get("sysout.random.sleep"));

        list.set(0, "true");
        resource.setProviderResources(request.getCollection().getProvider(), resources);

        execution0 = orchestrator.executeWorkflow(w, request);
        execution0.waitUntilFinished();
        Assert.assertEquals("true", execution0.getProperties().get("sysout.random.sleep"));

        list.set(0, "false");
        resource.setCollectionResources(request.getCollection(), resources);

        execution0 = orchestrator.executeWorkflow(w, request);
        execution0.waitUntilFinished();
        Assert.assertEquals("false", execution0.getProperties().get("sysout.random.sleep"));

    }

    /**
     * Test save point.
     * 
     * @throws InterruptedException
     * @throws StorageEngineException
     */
    @Test
    public void testSavepointSuccess() throws InterruptedException, StorageEngineException {
        assertEquals(0, orchestrator.getActiveExecutions().size());

        Request<Long> request = createTestData(engine, 20);

        // creating the data calles 20 times the update method.
        verify(engine, times(20)).updateMetaDataRecord(any(MetaDataRecord.class));

        Workflow w = new SysoutWorkflow();

        ActiveExecution<Long> execution0 = orchestrator.executeWorkflow(w, request);
        execution0.waitUntilFinished();

        // each delivered metadata record is saved once per plugin (only one plugin in the
        // workflow) 20 plus the initial 20
        verify(engine, times(40)).updateMetaDataRecord(any(MetaDataRecord.class));

        assertEquals(20, execution0.getCompletedSize());
        assertEquals(0, execution0.getFailureSize());
        assertEquals(20, execution0.getScheduledSize());
    }

    private Request<Long> createTestData(StorageEngine<Long> engine, int count)
            throws StorageEngineException {
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
            MetaDataRecord<Long> record0 = engine.createMetaDataRecord(collection0, "abcd" + i);
            record0.addValue(MDRFieldRegistry.rawrecord, "title " + i);
            engine.updateMetaDataRecord(record0);
            engine.addRequestRecord(request0, record0);
        }

        return request0;
    }

    @Override
    protected void fillRecord(MetaDataRecord<Long> record, int count) {
        record.addValue(MDRFieldRegistry.rawrecord, "title " + count);
    }
}
