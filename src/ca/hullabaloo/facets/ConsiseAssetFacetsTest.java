package ca.hullabaloo.facets;

import it.uniroma3.mat.extendedset.ConciseSet;

public class ConsiseAssetFacetsTest extends AssetFacetsTestCase {
    public ConsiseAssetFacetsTest() {
        super(new CompressedAssetFacets());
    }

    public void testComplement() {
        ConciseSet a = new ConciseSet();
        a.add(2);
        a.add(4);
        a.complement();
        assertTrue(a.contains(0));
        assertTrue(a.contains(3));
        assertTrue(a.contains(1));
    }
}
