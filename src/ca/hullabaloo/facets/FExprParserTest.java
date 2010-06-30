package ca.hullabaloo.facets;

import junit.framework.TestCase;

import java.io.IOException;

public class FExprParserTest extends TestCase {
    private final FExprParser p = new FExprParser();

    public void testSimple() throws IOException {
        String actual = p.parse("CS:foo").toString();
        String expected = "CS:foo";
        assertEquals(expected, actual);
    }

    public void testParen() throws IOException {
        String actual = p.parse("(CS:foo)").toString();
        String expected = "CS:foo";
        assertEquals(expected, actual);
    }

    public void testAnd() throws IOException {
        String actual = p.parse("CS:foo && bar").toString();
        String expected = "CS:foo && bar";
        assertEquals(expected, actual);
    }

    public void testParenAnd() throws IOException {
        String actual = p.parse("(CS:foo && bar)").toString();
        String expected = "CS:foo && bar";
        assertEquals(expected, actual);
    }

    public void testOr() throws IOException {
        String actual = p.parse("CS:foo||bar").toString();
        String expected = "CS:foo || bar";
        assertEquals(expected, actual);
    }

    public void testParenOr() throws IOException {
        String actual = p.parse("(CS:foo|| bar)").toString();
        String expected = "CS:foo || bar";
        assertEquals(expected, actual);
    }

    public void testMultiple() throws IOException {
        String actual = p.parse("CS:foo && bar && baz").toString();
        String expected = "CS:foo && bar && baz";
        assertEquals(expected, actual);
    }

    public void testMultipleAndOr() throws IOException {
        String actual = p.parse("CS:foo && bar || baz").toString();
        String expected = "(CS:foo && bar) || baz";
        assertEquals(expected, actual);
    }

    public void testMultipleOrAnd() throws IOException {
        String actual = p.parse("CS:foo || bar && baz").toString();
        String expected = "(CS:foo || bar) && baz";
        assertEquals(expected, actual);
    }

    public void testNot() throws IOException {
        String actual = p.parse("!CS:foo").toString();
        String expected = "!CS:foo";
        assertEquals(expected, actual);
    }

    public void testNotParens() throws IOException {
        String actual = p.parse("!(CS:foo||bar)").toString();
        String expected = "!(CS:foo || bar)";
        assertEquals(expected, actual);
    }
}
