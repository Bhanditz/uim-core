package eu.europeana.uim.logging.database;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/test-context.xml", "/test-beans.xml" })
public class DatabaseLogEntryTest {
    @Autowired
    private TStringDatabaseLogEntryHome questionaireHome;

    @Autowired
    private TObjectDatabaseLogEntryHome questionHome;

    @Autowired
    private TDurationDatabaseEntryHome  partnerHome;

    @Before
    public void setup() {
        questionaireHome.truncate();
        questionHome.truncate();
        partnerHome.truncate();
    }

// private static Semaphore lock = new Semaphore(1);
//
// @Before
// public void start() throws InterruptedException {
// lock.acquire();
// }
//
// @After
// public void clean() {
// entryHome.truncate();
//
// lock.release();
// }
//
    @Test
    public void testEntryLifeCycle() {
// TStringDatabaseLogEntry entry = new TStringDatabaseLogEntry();
// entry.setLevel(Level.WARNING);
//
// assertEquals("Plain setter/getter testing", entry.getLevel(), Level.WARNING);
//
// entryHome.insert(entry);
//
// assertNotNull(entry.getOid());
//
// TStringDatabaseLogEntry entry2 = entryHome.findByOid(entry.getOid());
// assertNotSame("Persistence instance", entry, entry2);
//
// assertEquals(entry.getLevel(), entry2.getLevel());
    }
}
