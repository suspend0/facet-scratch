package ca.hullabaloo.facets;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import java.io.IOException;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.util.List;

class FExprParser {
    // can be any value, as long as it's less than zero and doesn't conflict with any of StreamTokenizer.TT_*
    private static final int ONE_TOKEN = -6;

    /** @see ca.hullabaloo.facets.FExpr#compile(String) */
    FExpr parse(String expression) {
        try {
            StreamTokenizer s = new StreamTokenizer(new StringReader(expression));
            s.resetSyntax();
            s.wordChars('a', 'z');
            s.wordChars('A', 'Z');
            s.wordChars('0', '9');
            s.wordChars(':',':');
            s.wordChars('_','_');
            s.wordChars('/','/');
            s.wordChars(128 + 32, 255);
            s.whitespaceChars(0, ' ');
            s.quoteChar('"');
            s.quoteChar('\'');
            s.ordinaryChar('|');
            s.ordinaryChar('&');
            s.ordinaryChar('(');
            s.ordinaryChar(')');
            s.eolIsSignificant(false);
            s.slashStarComments(true);

            FExpr.Node root = parseGroup(s, StreamTokenizer.TT_EOF);
            return new FExpr(root);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(expression,e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private FExpr.Node parseGroup(StreamTokenizer s, final int end) throws IOException {
        char op = '\0';

        List<FExpr.Node> tokens = Lists.newArrayListWithCapacity(2);
        while (s.nextToken() != end) {
            switch (s.ttype) {
                case StreamTokenizer.TT_WORD:
                    tokens.add(create(s.sval));
                    break;
                case '&':
                case '|':
                    // detects when operators change in the same group 'a && b || c'
                    // by manufacturing a group like such: '(a && b) || c'
                    if (op != '\0' && op != s.ttype) {
                        tokens.add(create(op, tokens));
                    }
                    op = (char) s.ttype;
                    break;
                case '(':
                    tokens.add(parseGroup(s, ')'));
                    break;
                case '!':
                    tokens.add(new Not(parseGroup(s, ONE_TOKEN)));
                    break;
                case StreamTokenizer.TT_EOF:
                    throw new IllegalArgumentException("EOF looking for " + typeToString(s.ttype));
                default:
                    throw new IllegalArgumentException("unrecognized: " + typeToString(s.ttype));
            }
            if (end == ONE_TOKEN) {
                break;
            }
        }
        if (op == '\0' && tokens.size() == 1) return tokens.get(0);
        return create(op, tokens);
    }

    private FExpr.Node create(String word) {
        return new Value(word);
    }

    private FExpr.Node create(char op, List<FExpr.Node> children) {
        ImmutableList<FExpr.Node> c = ImmutableList.copyOf(children);
        children.clear();
        switch (op) {
            case '&':
                return new And(c);
            case '|':
                return new Or(c);
            default:
                throw new AssertionError(op);
        }
    }

    private String typeToString(int ttype) {
        switch (ttype) {
            case ONE_TOKEN:
                return "any token";
            case StreamTokenizer.TT_EOF:
                return "EOF";
            case StreamTokenizer.TT_EOL:
                return "end of line";
            case StreamTokenizer.TT_NUMBER:
                return "any number";
            case StreamTokenizer.TT_WORD:
                return "any word";
        }
        return (ttype > 0)
                ? Character.valueOf((char) ttype).toString()
                : "char#" + Integer.valueOf(ttype).toString();
    }

    private static abstract class OpNode implements FExpr.Node {
        private final String op;
        protected final ImmutableList<FExpr.Node> children;

        public OpNode(String op, ImmutableList<FExpr.Node> children) {
            this.op = op;
            this.children = children;
        }

        public final String toString() {
            return Joiner.on(op).appendTo(new StringBuilder().append('('), children).append(')').toString();
        }
    }

    private static final class And extends OpNode {
        public And(ImmutableList<FExpr.Node> children) {
            super(" && ", children);
        }

        public <T> T evaluate(FExprEval<T> evaluator) {
            return evaluator.and(children);
        }
    }

    private static final class Or extends OpNode {
        public Or(ImmutableList<FExpr.Node> children) {
            super(" || ", children);
        }

        public <T> T evaluate(FExprEval<T> evaluator) {
            return evaluator.or(children);
        }
    }

    private final class Not implements FExpr.Node {
        private final FExpr.Node invert;

        public Not(FExpr.Node invert) {
            this.invert = invert;
        }

        public String toString() {
            return "!" + invert.toString();
        }

        public <T> T evaluate(FExprEval<T> evaluator) {
            return evaluator.not(invert);
        }
    }

    private final class Value implements FExpr.Node {
        private final String val;

        public Value(String val) {
            this.val = val;
        }

        public String toString() {
            return val;
        }

        public <T> T evaluate(FExprEval<T> evaluator) {
            return evaluator.value(val);
        }
    }
}
