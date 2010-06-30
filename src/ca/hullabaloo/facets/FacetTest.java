package ca.hullabaloo.facets;

import junit.framework.TestCase;

public class FacetTest extends TestCase {
    public void testCreate() throws Exception {
        Facet one = Facet.create("a", "b");
        Facet two = Facet.create("a:b");
        assertEquals(one, two);
        assertEquals("a:b", one.toString());
    }
}
