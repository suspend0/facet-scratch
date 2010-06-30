package ca.hullabaloo.facets;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

public final class Facet {
    public static Facet create(String type, String value) {
        Preconditions.checkArgument(type.indexOf(':') == -1);
        Preconditions.checkArgument(value.indexOf(':') == -1);
        return new Facet(type + ":" + value);
    }

    // parses a colon-delimited facet
    public static Facet create(String facet) {
        int splitAt = facet.indexOf(':');
        String type = facet.substring(0, splitAt);
        String value = facet.substring(splitAt + 1);
        return create(type, value);
    }

    private final String str;

    private Facet(String facetString) {
        this.str = facetString;
    }

    public String toString() {
        return str;
    }

    public int hashCode() {
        return str.hashCode();
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (this.getClass() != o.getClass()) return false;

        Facet that = (Facet) o;
        return Objects.equal(this.str, that.str);
    }
}
