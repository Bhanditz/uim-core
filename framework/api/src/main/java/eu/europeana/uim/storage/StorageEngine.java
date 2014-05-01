package eu.europeana.uim.storage;

import java.util.Map;

import eu.europeana.uim.EngineStatus;
import eu.europeana.uim.storage.modules.CollectionStorageEngine;
import eu.europeana.uim.storage.modules.ExecutionStorageEngine;
import eu.europeana.uim.storage.modules.MetaDataRecordStorageEngine;
import eu.europeana.uim.storage.modules.ProviderStorageEngine;
import eu.europeana.uim.storage.modules.RequestStorageEngine;

/**
 * Base class for storage engine typed with a ID class.
 * 
 * @param <I>
 *            generic ID
 * 
 * @author Markus Muhr (markus.muhr@kb.nl)
 * @since Mar 21, 2011
 */
public interface StorageEngine<I> extends CollectionStorageEngine<I>, ExecutionStorageEngine<I>,
        MetaDataRecordStorageEngine<I>, ProviderStorageEngine<I>, RequestStorageEngine<I> {
    /**
     * @return identifier of the storage engine
     */
    String getIdentifier();

    /**
     * @param config
     *            arbitrary key - value map
     */
    void setConfiguration(Map<String, String> config);

    /**
     * @return configuration as arbitrary key - value map
     */
    Map<String, String> getConfiguration();

    /**
     * Initializes engine by for example opening database connection.
     */
    void initialize();

    /**
     * Shutdown the engine and its connected components like connection to database.
     */
    void shutdown();

    /**
     * Mark a checkpoint.
     */
    void checkpoint();

    /**
     * @param command
     *            arbitrary command interpreted by the engine implementation
     */
    void command(String command);

    /**
     * @return status of the engine (starting, ...)
     */
    EngineStatus getStatus();
}
