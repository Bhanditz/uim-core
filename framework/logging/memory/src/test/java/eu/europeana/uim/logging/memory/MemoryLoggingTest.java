package eu.europeana.uim.logging.memory;

import eu.europeana.uim.logging.AbstractLoggingEngineTest;
import eu.europeana.uim.logging.LoggingEngine;

/**
 * Tests {@link MemoryLoggingEngine} implementations used for it.
 * 
 * @author Markus Muhr (markus.muhr@kb.nl)
 * @since Apr 4, 2011
 */
public class MemoryLoggingTest extends AbstractLoggingEngineTest {
    @Override
    protected LoggingEngine<Long> getLoggingEngine() {
        return new MemoryLoggingEngine<Long>();
    }
}
