package com.proto.core.event;

/**
 * A event key simply backed by a string
 *
 * @author wfrancis
 */
public class StringEventKey implements EventKey {

    private final String s;

    public StringEventKey(String s) {
        if(s == null) {
            throw new IllegalArgumentException("Not supporting null value in constructor");
        }
        this.s = s;
    }

    public StringEventKey(String s1, String s2) {
        if(s1 == null || s2 == null) {
            throw new IllegalArgumentException("Not supporting null value in constructor");
        }
        this.s = s1+s2;
    }

    public boolean matches(EventKey other) {
        if (s.equals("*"))
            return true;
        if (other.toString().equals(("*")))
            return true;
        return s.equals(other.toString());
    }

    public String getNamespace() {
        return "";  //TODO: Implement namespace.
    }

    @Override
    public String toString() {
        return s;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final StringEventKey that = (StringEventKey) o;

        return !(s != null ? !s.equals(that.s) : that.s != null);
    }

    @Override
    public int hashCode() {
        return s != null ? s.hashCode() : 0;
    }
}
