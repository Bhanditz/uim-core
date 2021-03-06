/**
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

import eu.europeana.uim.sugarcrm.model.SugarCrmField;

/**
 * The interface daclaration of a SugarCrmQuery
 * 
 * @author Georgios Markakis
 * @since Jan 19, 2012
 */
public interface SugarCrmQuery {
    /**
     * @param orderBy
     *            the field to sort by
     */
    void setOrderBy(SugarCrmField orderBy);

    /**
     * @return the field to sort by
     */
    SugarCrmField getOrderBy();

    /**
     * @param maxResults
     *            the maximum number of results to return
     */
    void setMaxResults(int maxResults);

    /**
     * @return the maximum number of results to return
     */
    int getMaxResults();

    /**
     * @param offset
     *            the index of result record to start from
     */
    void setOffset(int offset);

    /**
     * @return the index of result record to start from
     */
    int getOffset();
}
