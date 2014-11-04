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
package eu.europeana.uim.deprecated;

import eu.europeana.uim.external.ExternalServiceException;

/**
 * Basic exception thrown of methods in {@link RepoxService}.
 * 
 * @author Georgios Markakis
 * @since Jan 20, 2012
 */
public class RepoxException extends ExternalServiceException {
    /**
     * For inheritance reasons, pipes through to the super constructor.
     * 
     * @param message
     *            description of the error
     */
    public RepoxException(String message) {
        super(message);
    }

    /**
     * For inheritance reasons, pipes through to the super constructor.
     * 
     * @param message
     *            description of the error
     * @param cause
     *            root cause of the error
     */
    public RepoxException(String message, Throwable cause) {
        super(message, cause);
    }
}