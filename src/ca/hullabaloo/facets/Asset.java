package ca.hullabaloo.facets;

public class Asset {
    private final String name;

    public Asset(String name) {
        this.name = name;
    }

    public String toString() {
        return name;
    }

    public int hashCode() {
        return name.hashCode();
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (getClass() != o.getClass()) return false;

        Asset that = (Asset) o;
        return this.name == that.name;
    }
}
