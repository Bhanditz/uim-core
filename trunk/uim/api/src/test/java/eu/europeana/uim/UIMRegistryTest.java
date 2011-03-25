package eu.europeana.uim;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import eu.europeana.uim.api.LoggingEngine;
import eu.europeana.uim.api.LoggingEngineAdapter;
import eu.europeana.uim.api.StorageEngine;
import eu.europeana.uim.api.StorageEngineAdapter;

/**
 * Tests registration of storage and logging engine.
 * 
 * @author Andreas Juffinger (andreas.juffinger@kb.nl)
 * @since Feb 16, 2011
 */
public class UIMRegistryTest {
    private UIMRegistry registry = new UIMRegistry();

    /**
     * Tests registration of a {@link StorageEngine}.
     */
    @Test
    public void testStorageEngine() {
        registry.setConfiguredStorageEngine(StorageEngineAdapter.class.getSimpleName());
        registry.addStorage(new StorageEngineAdapter() {
        });
        assertNotNull(registry.getStorage());

        registry.setConfiguredStorageEngine("a");
        assertNull(registry.getActiveStorage());
        assertNotNull(registry.getStorage());
        assertNotNull(registry.getStorage(StorageEngineAdapter.class.getSimpleName()));

        assertEquals(1, registry.getStorages().size());
    }

    /**
     * Tests registration of a {@link LoggingEngine}.
     */
    @SuppressWarnings("rawtypes")
    @Test
    public void testLoggingEngine() {
        registry.setConfiguredLoggingEngine(LoggingEngineAdapter.class.getSimpleName());
        registry.addLoggingEngine(new LoggingEngineAdapter() {
        });
        assertNotNull(registry.getLoggingEngine());

        registry.setConfiguredLoggingEngine("a");
        assertNull(registry.getActiveLoggingEngine());
        assertNotNull(registry.getLoggingEngine());
        assertNotNull(registry.getLoggingEngine(LoggingEngineAdapter.class.getSimpleName()));

        assertEquals(1, registry.getLoggingEngines().size());
    }
}
