/* UIMTaskTest.java - created on Feb 16, 2011, Copyright (c) 2011 The European Library, all rights reserved */
package eu.europeana.uim;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.junit.Test;

import eu.europeana.uim.api.ExecutionContext;
import eu.europeana.uim.api.IngestionPlugin;
import eu.europeana.uim.api.IngestionPluginFailedException;
import eu.europeana.uim.api.StorageEngine;
import eu.europeana.uim.api.StorageEngineAdapter;
import eu.europeana.uim.common.TKey;
import eu.europeana.uim.store.MetaDataRecord;
import eu.europeana.uim.workflow.Task;
import eu.europeana.uim.workflow.TaskStatus;

/**
 * Tests task management (setup and workflow).
 * 
 * @author Andreas Juffinger (andreas.juffinger@kb.nl)
 * @since Feb 16, 2011
 */
public class UIMTaskTest {
    /**
     * test method to test setter/getter pairs and default task setup.
     */
    @Test
    public void testTaskSetup() {
        StorageEngine<?> engine = new StorageEngineAdapter() {
        };

        Task task = new Task(null, engine, null);

        assertNull(task.getStep());
        assertNull(task.getMetaDataRecord());
        assertNull(task.getOnFailure());
        assertNull(task.getOnSuccess());
        assertNull(task.getThrowable());

        assertEquals(TaskStatus.NEW, task.getStatus());

        task.setOnFailure(new LinkedList<Task>());
        task.setOnSuccess(new LinkedList<Task>());
        task.setThrowable(new Exception());

        assertNotNull(task.getOnFailure());
        assertNotNull(task.getOnSuccess());
        assertNotNull(task.getThrowable());
    }

    /**
     * test method to test simple workflow handling.
     */
    @Test
    public void testTaskWorkflow() {
        Task task = new Task(null, null, null);

        assertEquals(TaskStatus.NEW, task.getStatus());

        task.setStep(new IngestionPlugin() {
            @Override
            public boolean processRecord(MetaDataRecord<?> mdr, ExecutionContext context) {
                throw new UnsupportedOperationException("Sorry, not implemented.");
            }

            @Override
            public String getIdentifier() {
                return getClass().getSimpleName();
            }
            
            @Override
            public String getName() {
                return "Anonymous Plugin";
            }

            @Override
            public String getDescription() {
                return "Anonymous implementation of IngestionPlugin!";
            }

            @Override
            public TKey<?, ?>[] getInputFields() {
                return new TKey[0];
            }

            @Override
            public TKey<?, ?>[] getOptionalFields() {
                return new TKey[0];
            }

            @Override
            public TKey<?, ?>[] getOutputFields() {
                return new TKey[0];
            }

            @Override
            public int getPreferredThreadCount() {
                return 1;
            }

            @Override
            public int getMaximumThreadCount() {
                return 1;
            }

            @SuppressWarnings("unchecked")
            @Override
            public List<String> getParameters() {
                return Collections.EMPTY_LIST;
            }

            @Override
            public void initialize(ExecutionContext context) throws IngestionPluginFailedException {
                // nothing to do
            }

            @Override
            public void completed(ExecutionContext context) throws IngestionPluginFailedException {
                // nothing to do
            }

            @Override
            public void initialize() {
                // nothing to do
            }

            @Override
            public void shutdown() {
                // nothing to do
            }
        }, false);

        try {
            task.run();
            fail("There is an exception in the process method - task must fail");
        } catch (UnsupportedOperationException t) {
        }
    }
}
