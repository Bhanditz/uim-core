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
package eu.europeana.uim.repox;

/**
 * Exception thrown in case of a harvesting operation error (starting or scheduling an ingestion)
 * 
 * @author Georgios Markakis
 * @since Jan 20, 2012
 */
public class HarvestingOperationException extends RepoxException {
    private static final long serialVersionUID = 1L;

    /**
     * This constructor takes as an argument a String
     * 
     * @param message
     *            the error message
     */
    public HarvestingOperationException(String message) {
        super(message);
    }
}
