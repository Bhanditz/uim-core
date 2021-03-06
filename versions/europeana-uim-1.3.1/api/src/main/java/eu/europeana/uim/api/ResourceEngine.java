/* ResourceEngine.java - created on May 3, 2011, Copyright (c) 2011 The European Library, all rights reserved */
package eu.europeana.uim.api;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import eu.europeana.uim.store.Collection;
import eu.europeana.uim.store.Provider;
import eu.europeana.uim.workflow.Workflow;

/**
 * Interface for resource storage engine type to an identifier
 * 
 * @author Markus Muhr (markus.muhr@kb.nl)
 * @author Rene Wiermer (rene.wiermer@kb.nl)
 * @since May 3, 2011
 */
public interface ResourceEngine {
    /**
     * @return the identifier for this resource engine. Usually the simpel class name.
     */
    public String getIdentifier();

    /**
     * @param config
     */
    public void setConfiguration(Map<String, String> config);

    /**
     * @return configuration
     */
    public Map<String, String> getConfiguration();

    /**
     * Initializes engine by for example opening database connection.
     */
    public void initialize();

    /**
     * Shutdown the engine and its connected components like connection to database.
     */
    public void shutdown();

    /**
     * Saves all entries
     */
    public void checkpoint();

    /**
     * @return the current running status of the engine
     */
    public EngineStatus getStatus();

    /**
     * Sets the global resources. Parameters are only overwritten, if they have a non-null List
     * associated with them. Parameters are deleted, if the List is null.
     * 
     * @param resources
     *            key/resource map
     */
    public void setGlobalResources(LinkedHashMap<String, List<String>> resources);

    /**
     * Gets a hash map with every key that is requested and a List of resources, if there exist, or
     * null if not.
     * 
     * @param keys
     *            the keys to be in the HashMap
     * @return key/resource map
     */
    public LinkedHashMap<String, List<String>> getGlobalResources(List<String> keys);

    /**
     * Sets the workflow specific resources. If resources are null, then all workflow specific
     * resources are removed. Parameters are only overwritten, if they have a non-null List
     * associated with them. Parameters are deleted, if the List is null.
     * 
     * @param workflow
     *            the relevant workflow
     * @param resources
     *            key/resource map
     */
    public void setWorkflowResources(Workflow workflow,
            LinkedHashMap<String, List<String>> resources);

    /**
     * Gets a hash map of the workflow specific resources with every key that is requested and a
     * list of resources, if they exist, or null if not.
     * 
     * @param workflow
     *            the relevant workflow
     * @param keys
     *            the keys to be in the HashMap
     * @return key/resource map
     */

    public LinkedHashMap<String, List<String>> getWorkflowResources(Workflow workflow,
            List<String> keys);

    /**
     * Sets the provider specific resources. if resources are null, then all provider specific
     * resources are removed. Parameters are only overwritten, if they have a non-null List
     * associated with them. Parameters are deleted, if the List is null.
     * 
     * @param provider
     *            the relevant provider
     * @param resources
     *            key/resource map
     */
    public void setProviderResources(Provider<?> provider,
            LinkedHashMap<String, List<String>> resources);

    /**
     * Gets a hash map of the provider specific resources with every key that is requested and a
     * List of resources, if there exist, or null if not.
     * 
     * @param provider
     *            the relevant provider
     * @param keys
     *            the keys to be in the HashMap
     * @return key/resource map
     */

    public LinkedHashMap<String, List<String>> getProviderResources(Provider<?> provider,
            List<String> keys);

    /**
     * Sets the collection specific resources. if resources are null, then all collection specific
     * resources are removed. Parameters are only overwritten, if they have a non-null List
     * associated with them. Parameters are deleted, if the List is null.
     * 
     * @param collection
     *            the relevant collection
     * @param resources
     *            key/resource map
     */

    public void setCollectionResources(Collection<?> collection,
            LinkedHashMap<String, List<String>> resources);

    /**
     * Gets a hash map of the collection specific resources with every key that is requested and a
     * List of resources, if there exist, or null if not.
     * 
     * @param collection
     *            the relevant collection
     * @param keys
     *            the keys to be in the HashMap
     * @return key/resource map
     */
    public LinkedHashMap<String, List<String>> getCollectionResources(Collection<?> collection,
            List<String> keys);

    /**
     * @return the root directory to store resource files in.
     */
    public File getResourceDirectory();

    /**
     * @return the root directory for creating work directories for persisted output files from
     *         executions.
     */
    public File getWorkingDirectory();

    /**
     * @return the root directory for creating temporary directories for executions.
     */
    public File getTemporaryDirectory();
}
