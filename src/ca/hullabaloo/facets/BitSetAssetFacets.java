package ca.hullabaloo.facets;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.MapMaker;

import java.util.BitSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

public class BitSetAssetFacets implements AssetFacets {
    private int nextAssetId = 0;
    private final BiMap<Integer, Asset> assets = HashBiMap.create();
    private final ConcurrentMap<String, BitSet> facets = new MapMaker().makeComputingMap(new Function<String, BitSet>() {
        public BitSet apply(String facet) {
            return new BitSet();
        }
    });
    private final Evaluator evaluator = new Evaluator();

    public BitSetAssetFacets() {
        checkInvariants();
    }

    public void add(Asset asset, Facet facet) {
        Integer nn = assets.inverse().get(asset);
        if (nn == null) assets.put(nn = nextAssetId++, asset);
        facets.get(facet.toString()).set(nn);
        checkInvariants();
    }

    public Set<Asset> find(String facetExpression) {
        FExpr expr = FExpr.compile(facetExpression);
        return find(expr);
    }

    public Set<Asset> find(FExpr expr) {
        BitSet bs = expr.evaluate(evaluator);
        return toNodes(bs);
    }

    public Set<Asset> findAny(final Iterable<Facet> finding) {
        return find(Joiner.on(" || ").join(finding));
    }

    public Set<Asset> findAll(final Iterable<Facet> finding) {
        return find(Joiner.on(" && ").join(finding));
    }

    private Set<Asset> toNodes(BitSet found) {
        ImmutableSet.Builder<Asset> results = ImmutableSet.builder();
        for (int i = found.nextSetBit(0); i >= 0; i = found.nextSetBit(i + 1)) {
            results.add(assets.get(i));
        }
        return results.build();
    }

    public void remove(Asset asset, Facet facet) {
        Integer nn = assets.inverse().get(asset);
        if (nn == null) assets.put(nn = nextAssetId++, asset);
        facets.get(facet.toString()).clear(nn);
    }

    public boolean has(Asset asset, Facet facet) {
        return findAny(ImmutableSet.of(facet)).contains(asset);
    }

    private void checkInvariants() {
    }

    private class Evaluator implements FExprEval<BitSet> {
        public BitSet and(Iterable<FExpr.Node> nodes) {
            Iterator<FExpr.Node> iter = nodes.iterator();
            BitSet bs = iter.next().evaluate(this);
            while (iter.hasNext())
                bs.and(iter.next().evaluate(this));
            return bs;
        }

        public BitSet or(Iterable<FExpr.Node> nodes) {
            Iterator<FExpr.Node> iter = nodes.iterator();
            BitSet bs = iter.next().evaluate(this);
            while (iter.hasNext())
                bs.or(iter.next().evaluate(this));
            return bs;
        }

        public BitSet not(FExpr.Node node) {
            BitSet bs = node.evaluate(this);
            bs.flip(0, assets.size());
            return bs;
        }

        public BitSet value(String facet) {
            // careful to clone to allow other methods to modify (also, this might escape)
            return (BitSet) facets.get(facet).clone();
        }
    }
}
