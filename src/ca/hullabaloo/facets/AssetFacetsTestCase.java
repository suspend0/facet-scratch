package ca.hullabaloo.facets;

import com.google.common.collect.ImmutableSet;
import junit.framework.TestCase;

@SuppressWarnings({"UnusedDeclaration"})
public abstract class AssetFacetsTestCase extends TestCase {
    private final Asset n1 = new Asset("x");
    private final Asset n2 = new Asset("y");
    private final Asset n3 = new Asset("z");
    private final Facet f1 = Facet.create("a", "b");
    private final Facet f2 = Facet.create("c", "d");
    private final AssetFacets nf;

    public AssetFacetsTestCase(AssetFacets nf) {
        this.nf = nf;
    }

    public void setUp() {
        if (nf != null) {
            nf.add(n1, f1);
            nf.add(n2, f1);
        }
    }

    public void testAdd() throws Exception {
        nf.add(n2, f1);
        assertTrue(nf.has(n1, f1));
        assertFalse(nf.has(n1, f2));
        assertTrue(nf.has(n2, f1));
        assertFalse(nf.has(n2, f2));
    }

    public void testRemove() throws Exception {
        nf.remove(n2, f1);
        assertTrue(nf.has(n1, f1));
        assertFalse(nf.has(n1, f2));
        assertFalse(nf.has(n2, f1));
        assertFalse(nf.has(n2, f2));
    }

    public void testMethodFindAny() throws Exception {
        nf.add(n2, f2);
        assertEquals(setOf(n1, n2), nf.findAny(setOf(f1)));
        assertEquals(setOf(n2), nf.findAny(setOf(f2)));
        assertEquals(setOf(n1, n2), nf.findAny(setOf(f1, f2)));
    }

    public void testMethodFindAll() throws Exception {
        nf.add(n2, f2);
        assertEquals(setOf(n1, n2), nf.findAll(setOf(f1)));
        assertEquals(setOf(n2), nf.findAll(setOf(f2)));
        assertEquals(setOf(n2), nf.findAll(setOf(f1, f2)));
    }

    public void testFindExprSimple() {
        assertEquals(setOf(n1,n2), nf.find(f1.toString()));
    }

    public void testFindExprAnd() {
        nf.add(n2,f2);
        assertEquals(setOf(n2), nf.find(f1 + "&&" + f2));
    }

    public void testFindExprOr() {
        nf.add(n2,f2);
        assertEquals(setOf(n1,n2), nf.find(f1 + "||" + f2));
    }

    public void testFindExprNot() {
        nf.add(n3,f2);
        assertEquals(setOf(n3), nf.find("!" + f1));
    }

    private static <E> ImmutableSet<E> setOf(E... elements) {
        return ImmutableSet.copyOf(elements);
    }
}
