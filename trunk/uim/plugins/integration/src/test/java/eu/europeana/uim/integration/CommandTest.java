package eu.europeana.uim.integration;

import static org.junit.Assert.assertEquals;
import static org.ops4j.pax.exam.CoreOptions.felix;
import static org.ops4j.pax.exam.CoreOptions.maven;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.CoreOptions.systemProperty;
import static org.ops4j.pax.exam.CoreOptions.waitForFrameworkStartup;
import static org.ops4j.pax.exam.OptionUtils.combine;
import static org.ops4j.pax.exam.container.def.PaxRunnerOptions.scanFeatures;

import org.apache.karaf.testing.Helper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.container.def.PaxRunnerOptions;
import org.ops4j.pax.exam.junit.Configuration;
import org.ops4j.pax.exam.junit.JUnit4TestRunner;
import org.osgi.framework.Constants;

import eu.europeana.uim.api.Registry;
import eu.europeana.uim.api.StorageEngine;
import eu.europeana.uim.store.Collection;
import eu.europeana.uim.store.Provider;

/**
 * Integration test for UIM commands<br/>
 * Warning: /!\ if you do not want to be driven insane, do check -- twice -- if you do NOT have a
 * running Karaf instance somewhere on your system<br/>
 * 
 * @author Manuel Bernhardt
 */
@RunWith(JUnit4TestRunner.class)
public class CommandTest extends AbstractUIMIntegrationTest {

    @Configuration
    public static Option[] configuration() throws Exception {
        return combine(
                Helper.getDefaultOptions(
                        systemProperty("karaf.name").value("junit"),
                        systemProperty("org.ops4j.pax.logging.DefaultServiceLog.level").value(
                                "INFO")),

                // PaxRunnerOptions.vmOption( "-Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=5006" ),


                mavenBundle().groupId("org.apache.karaf.shell").artifactId(
                        "org.apache.karaf.shell.console").versionAsInProject(),

                mavenBundle().groupId("eu.europeana").artifactId("europeana-uim-common").versionAsInProject(),

                mavenBundle().groupId("eu.europeana").artifactId("europeana-uim-api").versionAsInProject(),
                mavenBundle().groupId("eu.europeana").artifactId("europeana-uim-storage-memory").versionAsInProject(),

                mavenBundle().groupId("eu.europeana").artifactId("europeana-uim-plugin-basic").versionAsInProject(),
// mavenBundle().groupId("eu.europeana").artifactId("europeana-uim-plugin-fileimp").versionAsInProject(),

                felix(),

                waitForFrameworkStartup());
    }

    @Test
    public void testUIInfo() throws Exception {
        Registry registry = getOsgiService(Registry.class);

        StorageEngine storage = null;
        while (storage == null) {
            storage = registry.getStorage();
            Thread.sleep(500);
        }

        Provider provider = storage.createProvider();

        Collection collection = storage.createCollection(provider);
        collection.setMnemonic("CCCC");
        storage.updateCollection(collection);

        assertEquals("MemoryStorageEngine", registry.getStorage().getIdentifier());

        String property = bundleContext.getProperty(Constants.FRAMEWORK_VERSION);
        assertEquals("1.5", property);

        // load the provider data
        String result = getCommandResult("uim:exec -o start SysoutWorkflow CCCC prop.a=/data/&prop.b=aaa&language=eng,spa");

        Thread.sleep(1000);

        // assertEquals("", result);
    }

}
