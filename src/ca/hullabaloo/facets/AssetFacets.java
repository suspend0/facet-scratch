package ca.hullabaloo.facets;

import java.util.Set;

public interface AssetFacets {
    /** Associate a facet with an asset */
    void add(Asset asset, Facet facet);

    /** Remove a facet from an asset */
    void remove(Asset asset, Facet facet);

    /** Inspect if an asset has a facet applied */
    boolean has(Asset asset, Facet facet);

    /**
     * Finds assets matching the facet expression
     * @see ca.hullabaloo.facets.FExpr#compile(String)
     */
    Set<Asset> find(String facetExpression);

    /**
     * Finds assets matching the facet expression
     * @see ca.hullabaloo.facets.FExpr#compile(String)
     */
    Set<Asset> find(FExpr expr);

    /** Finds assets which have all of the passed facets */
    Set<Asset> findAny(Iterable<Facet> finding);

    /** Finds assets which have all of the passed facets */
    Set<Asset> findAll(Iterable<Facet> finding);
}
