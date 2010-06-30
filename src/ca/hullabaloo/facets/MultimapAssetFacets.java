package ca.hullabaloo.facets;

import com.google.common.base.Joiner;
import com.google.common.collect.*;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class MultimapAssetFacets implements AssetFacets, FExprEval<Set<Asset>> {
    private SetMultimap<Facet, Asset> nodeFacets = HashMultimap.create();

    public void add(Asset asset, Facet facet) {
        nodeFacets.put(facet,asset);
    }

    public Set<Asset> find(String facetExpression) {
        FExpr expr = FExpr.compile(facetExpression);
        return find(expr);
    }

    public Set<Asset> find(FExpr expr) {
        return expr.evaluate(this);
    }

    public Set<Asset> findAny(final Iterable<Facet> finding) {
        return find(Joiner.on(" || ").join(finding));
    }

    public Set<Asset> findAll(final Iterable<Facet> finding) {
        return find(Joiner.on(" && ").join(finding));
    }

    public void remove(Asset asset, Facet facet) {
        nodeFacets.remove(facet, asset);
    }

    public boolean has(Asset asset, Facet facet) {
        return nodeFacets.containsEntry(facet, asset);
    }

    public Set<Asset> and(Iterable<FExpr.Node> nodes) {
        Iterator<FExpr.Node> iter = nodes.iterator();
        Set<Asset> result = iter.next().evaluate(this);
        while(iter.hasNext())
            result.retainAll(iter.next().evaluate(this));
        return result;
    }

    public Set<Asset> or(Iterable<FExpr.Node> nodes) {
        Set<Asset> result = Sets.newHashSet();
        for(FExpr.Node node : nodes)
            result.addAll(node.evaluate(this));
        return result;
    }

    public Set<Asset> not(FExpr.Node node) {
        Set<Asset> val = node.evaluate(this);
        Set<Asset> results = Sets.newHashSet(nodeFacets.values());
        results.removeAll(val);
        return results;
    }

    public Set<Asset> value(String facet) {
        return nodeFacets.get(Facet.create(facet));
    }
}
