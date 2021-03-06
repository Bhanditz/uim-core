/*
 * Copyright 2007 EDL FOUNDATION
 *
 * Licensed under the EUPL, Version 1.1 or - as soon they
 * will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 * you may not use this work except in compliance with the
 * Licence.
 * You may obtain a copy of the Licence at:
 *
 * http://ec.europa.eu/idabc/eupl
 *
 * Unless required by applicable law or agreed to in
 * writing, software distributed under the Licence is
 * distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied.
 * See the Licence for the specific language governing
 * permissions and limitations under the Licence.
 */

package eu.europeana.uim.sugarcrm;

import java.util.List;

/**
 * Interface declaration of a Polling listener. It implements the Command Pattern in PollingBean
 * class. Any Class implementing this interface can be added as a PollingBean listener.
 * 
 * @author Georgios Markakis
 * @since Jan 19, 2012
 */
public interface PollingListener {
    /**
     * Returns a specific query that will act as a trigger for the designated action (in case that
     * any results are returned) .
     * 
     * @return the SugarCrmQuery to be executed
     */
    SugarCrmQuery getTrigger();

    /**
     * Defines the contents of a specific action
     * 
     * @param pluginReference
     *            a reference to the SugarCRM OSGI plugin
     * @param retrievedRecords
     *            the input to the specified action
     * @throws GenericSugarCrmException
     */
    void performAction(SugarCrmService pluginReference, List<SugarCrmRecord> retrievedRecords)
            throws GenericSugarCrmException;
}
