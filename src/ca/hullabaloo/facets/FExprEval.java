package ca.hullabaloo.facets;

public interface FExprEval<T> {
    T and(Iterable<FExpr.Node> nodes);

    T or(Iterable<FExpr.Node> nodes);

    T not(FExpr.Node node);

    T value(String facet);
}
