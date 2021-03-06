/* XmlConversionTest.java - created on Jan 25, 2012, Copyright (c) 2011 The European Library, all rights reserved */
package eu.europeana.uim.repox.rest.client.xml;

import java.io.InputStream;

import junit.framework.Assert;

import org.junit.Test;

import eu.europeana.uim.repox.rest.utils.RepoxXmlUtils;

/**
 * Tests marshalling/unmarshalling of a XML document to Java objects.
 * 
 * @author Markus Muhr (markus.muhr@kb.nl)
 * @since Jan 25, 2012
 */
public class XmlConversionTest {
    /**
     * Tests converting of response.xml in test resources
     * 
     * @throws Exception
     */
    @Test
    public void convertResponse() throws Exception {
        InputStream in = XmlConversionTest.class.getResourceAsStream("/repox/response.xml");

        StringBuffer out = new StringBuffer();
        byte[] b = new byte[4096];
        for (int n; (n = in.read(b)) != -1;) {
            out.append(new String(b, 0, n));
        }
        String xml = out.toString();

        Object xmlObject = RepoxXmlUtils.unmarshall(xml);
        Assert.assertTrue(xmlObject instanceof Response);

        Response response = (Response)xmlObject;
        Assert.assertEquals(2, response.getAggregators().getAggregator().size());
        Assert.assertEquals(3, response.getDataProviders().getProvider().size());
        Assert.assertEquals(8, response.getDataSources().getSource().size());

        String mXmlObject = RepoxXmlUtils.marshall(response);
        Assert.assertTrue(mXmlObject.contains("TimestampHarvester"));
    }
}
