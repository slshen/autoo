package autoo.util;

import java.io.Serializable;

/**
 * A pair of values.
 */
public class Pair<T,U> implements Serializable {
    private static final long serialVersionUID = 1L;
    private T first;
    private U second;
    
    /**
     * Create a pair, inferring the types.  This should be generally used instead of
     * and explicit call to <code>new Pair()</code>.
     */
    public static <F,S> Pair<F,S> create(F first, S second) {
        return new Pair<F,S>(first, second);
    }

    public Pair() {
    }
    
    public Pair(T first, U second) {
        this.first = first;
        this.second = second;
    }
    public T getFirst() {
        return first;
    }
    public U getSecond() {
        return second;
    }
    @Override
    public boolean equals(Object o) {
        if (o instanceof Pair) {
            Pair<?,?> p = (Pair<?,?>) o;
            if (first == p.first && second == p.second) {
                return true;
            }
            return first != null && first.equals(p.first)
                && second != null && second.equals(p.second);
        }
        return false;
    }
    @Override
    public int hashCode() {
        return (first != null ? first.hashCode() : 0) 
            ^ (second != null ? second.hashCode() : 0);
    }
    @Override
    public String toString() {
        return "(" + first + ", " + second + ")";
    }
}
