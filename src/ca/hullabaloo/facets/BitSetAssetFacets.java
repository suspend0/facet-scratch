package ca.hullabaloo.facets;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.*;

import java.util.*;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

public class BitSetAssetFacets implements AssetFacets, FExprEval<BitSet> {
    private int nextAssetId = 0;
    private final BiMap<Asset, Integer> assets = HashBiMap.create();
    private final ConcurrentMap<Facet, BitSet> facets = new MapMaker().makeComputingMap(new Function<Facet, BitSet>() {
        public BitSet apply(Facet facet) {
            return new BitSet();
        }
    });

    public BitSetAssetFacets() {
        checkInvariants();
    }

    public void add(Asset asset, Facet facet) {
        Integer nn = assets.get(asset);
        if (nn == null) assets.put(asset, nn = nextAssetId++);
        facets.get(facet).set(nn);
        checkInvariants();
    }

    public Set<Asset> find(String facetExpression) {
        FExpr expr = FExpr.compile(facetExpression);
        return find(expr);
    }

    public Set<Asset> find(FExpr expr) {
        BitSet bs = expr.evaluate(this);
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
            results.add(assets.inverse().get(i));
        }
        return results.build();
    }

    public void remove(Asset asset, Facet facet) {
        Integer nn = assets.get(asset);
        if (nn == null) assets.put(asset, nn = nextAssetId++);
        facets.get(facet).clear(nn);
    }

    public boolean has(Asset asset, Facet facet) {
        return findAny(ImmutableSet.of(facet)).contains(asset);
    }

    private void checkInvariants() {
    }

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
        return (BitSet) this.facets.get(Facet.create(facet)).clone();
    }
}
