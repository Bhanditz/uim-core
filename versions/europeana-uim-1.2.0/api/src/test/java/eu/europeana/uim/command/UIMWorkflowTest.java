/* UIMWorkflowTest.java - created on Jun 19, 2011, Copyright (c) 2011 The European Library, all rights reserved */
package eu.europeana.uim.command;

import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.*;

import org.apache.felix.service.command.CommandSession;
import org.junit.Test;

import eu.europeana.uim.LegalIngestionWorkflow;
import eu.europeana.uim.UIMRegistry;

/**
 * 
 * 
 * @author Andreas Juffinger (andreas.juffinger@kb.nl)
 * @since Jun 19, 2011
 */
public class UIMWorkflowTest {

    
    @Test
    public void testListWorkflows() throws Exception {
        UIMRegistry registry = new UIMRegistry();
        
        LegalIngestionWorkflow workflow = new LegalIngestionWorkflow();
        registry.addWorkflow(workflow);
        assertFalse(registry.getWorkflows().isEmpty());

        
        CommandSession session = mock(CommandSession.class);
        
        UIMWorkflow command = new UIMWorkflow(registry);
        command.operation = UIMWorkflow.Operation.listWorkflows;
        command.execute(session);
        
    }
}
