package ca.hullabaloo.facets;

import com.google.common.base.Joiner;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableSet;
import it.uniroma3.mat.extendedset.ConciseSet;
import org.ardverk.collection.PatriciaTrie;
import org.ardverk.collection.StringKeyAnalyzer;

import java.util.Iterator;
import java.util.Set;

/**
 * Compresses the facet strings using a patricia trie and the node bitmaps by using a "Concise Set"
 */
public class CompressedAssetFacets implements AssetFacets {
    private int nextAssetId = 0;
    private final BiMap<Asset, Integer> assets = HashBiMap.create();
    PatriciaTrie<String, ConciseSet> facets = new PatriciaTrie<String, ConciseSet>(StringKeyAnalyzer.INSTANCE);
    private final Evaluator evaluator = new Evaluator();

    public CompressedAssetFacets() {
        checkInvariants();
    }

    public void add(Asset asset, Facet facet) {
        Integer nn = assets.get(asset);
        if (nn == null) assets.put(asset, nn = nextAssetId++);
        get(facet).add(nn);
        checkInvariants();
    }

    public void remove(Asset asset, Facet facet) {
        Integer nn = assets.get(asset);
        if (nn != null)
            get(facet).remove(nn);
    }

    public boolean has(Asset asset, Facet facet) {
        return find(facet.toString()).contains(asset);
    }

    public Set<Asset> find(String facetExpression) {
        FExpr expr = FExpr.compile(facetExpression);
        return find(expr);
    }

    public Set<Asset> find(FExpr expr) {
        ConciseSet bs = expr.evaluate(evaluator);
        return toNodes(bs);
    }

    /**
     * returns assets matching any of the provided facets
     */
    public Set<Asset> findAny(final Iterable<Facet> finding) {
        return find(Joiner.on(" || ").join(finding));
    }

    /**
     * returns assets matching all of the provided facets
     */
    public Set<Asset> findAll(final Iterable<Facet> finding) {
        return find(Joiner.on(" && ").join(finding));
    }

    private ConciseSet get(Facet facet) {
        return get(facet.toString());
    }

    private ConciseSet get(String facet) {
        ConciseSet s = facets.get(facet);
        if (s == null)
            facets.put(facet, s = new ConciseSet());
        return s;
    }

    private Set<Asset> toNodes(ConciseSet found) {
        ImmutableSet.Builder<Asset> results = ImmutableSet.builder();
        for (int i : found) {
            results.add(assets.inverse().get(i));
        }
        return results.build();
    }

    private void checkInvariants() {
    }

    private class Evaluator implements FExprEval<ConciseSet> {
        public ConciseSet and(Iterable<FExpr.Node> nodes) {
            Iterator<FExpr.Node> iter = nodes.iterator();
            ConciseSet bs = iter.next().evaluate(this);
            while (iter.hasNext())
                bs.retainAll(iter.next().evaluate(this));
            return bs;
        }

        public ConciseSet or(Iterable<FExpr.Node> nodes) {
            Iterator<FExpr.Node> iter = nodes.iterator();
            ConciseSet bs = iter.next().evaluate(this);
            while (iter.hasNext())
                bs.addAll(iter.next().evaluate(this));
            return bs;
        }

        public ConciseSet not(FExpr.Node node) {
            ConciseSet bs = node.evaluate(this);
            int len = bs.size();
            bs.complement();
            if (len < assets.size())
                bs.fill(len, assets.size() - 1);
            return bs;
        }

        public ConciseSet value(String facet) {
            // careful to clone b/c this might escape (and callers may modify)
            return get(facet).clone();
        }
    }
}