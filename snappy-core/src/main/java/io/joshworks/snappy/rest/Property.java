package io.joshworks.snappy.rest;

/**
 * Created by Josh Gontijo on 3/17/17.
 */
public class Property {

    private final String value;

    public Property(String value) {
        this.value = value;
    }

    public String orElse(String value) {
        return isPresent() ? this.value : value;
    }

    public Property orElseVal(String value) {
        String v = isPresent() ? this.value : value;
        return new Property(v);
    }

    public int asInt() {
        return Integer.parseInt(value);
    }

    public double asDouble() {
        return Double.parseDouble(value);
    }

    public float asFloat() {
        return Float.parseFloat(value);
    }

    public boolean asBoolean() {
        return Boolean.parseBoolean(value);
    }

    public long asLong() {
        return Long.parseLong(value);
    }

    public String asString() {
        return value;
    }

    public boolean isPresent() {
        return value != null && !value.isEmpty();
    }
}
