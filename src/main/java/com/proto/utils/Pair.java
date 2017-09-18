package com.proto.utils;

import org.apache.commons.lang3.ObjectUtils;

/**
 * A pair of objects.
 *
 * @param <A>  the type of the first value in the pair.
 * @param <B>  the type of the second value in the pair.
 * 
 * @author wfrancis
 */
public class Pair<A, B> {
    /**
     * The pair values.
     */
    private A a;
    private B b;
    
    /**
     * Create a new, empty {@link Pair}.
     */
    public Pair() {
        //
    }

    /**
     * Create a new {@link Pair}.
     * 
     * @param a  the first value.
     * @param b  the second value.
     */
    public Pair(A a, B b) {
        this.a = a;
        this.b = b;
    }
    
    /**
     * Static "constructor" to save typing.
     * 
     * @param <A>  the type of the first value in the pair.
     * @param <B>  the type of the second value in the pair.
     * @param a    the first value.
     * @param b    the second value.
     * 
     * @return the new {@link Pair}.
     */
    public static <A, B> Pair<A, B> create(A a, B b) {
        return new Pair<A, B>(a, b);        
    }

    /**
     * Returns a pair containing the current elements reversed.
     *
     * @return new reversed pair
     */
    public Pair<B, A> reverse() {
        return create(b, a);
    }
    
    /**
     * @return the first value in the pair.
     */
    public A getA() {
        return a;
    }
    
    /**
     * Set the first value in the pair.
     * 
     * @param a  the new value.
     */
    public void setA(A a) {
        this.a = a;
    }
    
    /**
     * @return the second value in the pair.
     */
    public B getB() {
        return b;
    }
    
    /**
     * Set the second value in the pair.
     * 
     * @param b  the new value.
     */
    public void setB(B b) {
        this.b = b;
    }
    
    @Override
    public boolean equals(Object o) {
        /*
         * Simple cases.
         */
        if (o == null) return false;
        if (o == this) return true;
        
        /*
         * Check the values.
         */
        if (o instanceof Pair<?, ?>) {
            Pair<?, ?> p = (Pair<?, ?>)o;
            return 
                ObjectUtils.equals(a, p.a) &&
                ObjectUtils.equals(b, p.b)
            ;
        } else {
            return false;
        }
    }
    
    @Override
    public int hashCode() {
        int h = HashCodeUtils.SEED;
            h = HashCodeUtils.getHashCode(h, a);
            h = HashCodeUtils.getHashCode(h, b);
        return h;
    }
    
    @Override
    public String toString() {
        return a + "," + b;
    }
}
