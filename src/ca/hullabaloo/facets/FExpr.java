package ca.hullabaloo.facets;

/** An expression for matching facets */
public class FExpr {
    private final Node root;

    /**
     * expression syntax supports operators "&&", "||" and "!", as well as parentheses.  Facets are
     * Classification:Value; spaces must be enclosed in quotes
     *
     * Sample: ( "Location:San Jose" || Location:Portland ) && !OS:WinXP
     */
    public static FExpr compile(String expression) {
        return new FExprParser().parse(expression);
    }

    public FExpr(Node root) {
        this.root = root;
    }

    public <T> T evaluate(FExprEval<T> evaluator) {
        return root.evaluate(evaluator);
    }

    public String toString() {
        return removeOuterParens(root.toString());
    }

    private String removeOuterParens(String expr) {
        return expr.charAt(0) == '('
                ? expr.substring(1, expr.length() - 1)
                : expr;
    }

    public interface Node {
        <T> T evaluate(FExprEval<T> evaluator);
    }
}
