/* SpatialObjectTest.java - created on Sep 30, 2011, Copyright (c) 2011 The European Library, all rights reserved */
package org.theeuropeanlibrary.model.common.spatial;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import junit.framework.Assert;

import org.junit.Test;
import org.theeuropeanlibrary.model.common.Identifier;
import org.theeuropeanlibrary.model.common.qualifier.SpatialIdentifierType;

/**
 * Unit tests for spatial objects.
 * 
 * @author Andreas Juffinger (andreas.juffinger@kb.nl)
 * @since Sep 30, 2011
 */
public class SpatialObjectTest {
    /**
     * Tests the conversion of NamedPlace
     * 
     * @throws IOException
     */
    @Test
    public void testNamedPlace() throws IOException {
        NamedPlace enc = new NamedPlace("revised");
        Assert.assertEquals("revised", enc.getPlaceName());

        assertEquals(new NamedPlace("revised"), enc);
        assertEquals(new NamedPlace("revised").hashCode(), enc.hashCode());
    }

    /**
     * Tests the conversion of GeoReferencedPlace
     * 
     * @throws IOException
     */
    @Test
    public void testGeoReferencedPlace() throws IOException {
        GeoReferencedPlace enc = new GeoReferencedPlace("revised", 10.0, 20.0);
        Assert.assertEquals("revised", enc.getPlaceName());
        Assert.assertEquals(10.0, enc.getLatitude());
        Assert.assertEquals(20.0, enc.getLongitude());

        assertEquals(new GeoReferencedPlace("revised", 10.0, 20.0), enc);
        assertEquals(new GeoReferencedPlace("revised", 10.0, 20.0).hashCode(), enc.hashCode());
    }

    /**
     * Tests the conversion of BoxReferencedPlace
     * 
     * @throws IOException
     */
    @Test
    public void testBoundingBoxReferencedPlace() throws IOException {
        BoundingBoxReferencedPlace enc = new BoundingBoxReferencedPlace("revised", 1.0, 2.0, 3.0,
                4.0, null, null);
        Assert.assertEquals("revised", enc.getPlaceName());
        Assert.assertEquals(1.0, enc.getNorthLimit());
        Assert.assertEquals(2.0, enc.getSouthLimit());
        Assert.assertEquals(3.0, enc.getEastLimit());
        Assert.assertEquals(4.0, enc.getWestLimit());
        Assert.assertNull(enc.getUpLimit());
        Assert.assertNull(enc.getDownLimit());

        assertEquals(new BoundingBoxReferencedPlace("revised", 1.0, 2.0, 3.0, 4.0, null, null), enc);
        assertEquals(
                new BoundingBoxReferencedPlace("revised", 1.0, 2.0, 3.0, 4.0, null, null).hashCode(),
                enc.hashCode());
    }

    /**
     * Tests the conversion of SpacialEntity
     * 
     * @throws IOException
     */
    @Test
    public void testSpatialEntity() throws IOException {
        SpatialEntity enc = new SpatialEntity();
        for (int i = 0; i < SpatialIdentifierType.values().length; i++) {
            enc.getIdentifiers().add(
                    new Identifier("" + i, SpatialIdentifierType.values()[i].toString()));
        }

        Assert.assertEquals(SpatialIdentifierType.values().length, enc.getIdentifiers().size());
        for (int i = 0; i < enc.getIdentifiers().size(); i++) {
            Assert.assertEquals("" + i, enc.getIdentifiers().get(i).getIdentifier());
            Assert.assertEquals(SpatialIdentifierType.values()[i],
                    SpatialIdentifierType.valueOf(enc.getIdentifiers().get(i).getScope()));
        }

    }

    /**
     * Tests display of coordinates!
     * 
     * @throws IOException
     */
    @Test
    public void testCoordinatesDisplay() throws IOException {
        BoundingBoxReferencedPlace b = new BoundingBoxReferencedPlace("Test place", 87.728055, 83d,
                -10.60, -5.9880, 300d, 0d);
        b.setProjection("projection...");
        org.junit.Assert.assertTrue(b.getDisplay().contains(
                "W 5º59'17\" - W 10º35'60\" / N 87º43'41\" - N 83º"));

        GeoReferencedPlace p = new GeoReferencedPlace(null, 38.809722, -9.231111111111112);
        org.junit.Assert.assertEquals("N 38º48'35\", W 9º13'52\"", p.getDisplay());
    }
}
