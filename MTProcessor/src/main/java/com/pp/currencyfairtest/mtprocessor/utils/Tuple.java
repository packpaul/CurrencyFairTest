package com.pp.currencyfairtest.mtprocessor.utils;

public final class Tuple<T1, T2> {
    private final T1 first;
    private final T2 second;

    private Tuple(T1 first, T2 second) {
        this.first = first;
        this.second = second;
    }

    public static <T1, T2> Tuple<T1, T2> create(T1 first, T2 second) {
        return new Tuple(first, second);
    }

    public T1 getFirst() {
        return first;
    }
    
    public T1 f() {
        return first;
    }

    public T2 getSecond() {
        return second;
    }
    
    public T2 s() {
        return second;
    }    
    
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if ((obj == null) || (obj.getClass() != Tuple.class)) {
            return false;
        }
        Tuple<?, ?> other = (Tuple<?, ?>) obj;

        return (((first != null) ? first.equals(other.first) : (other.first == null)) &&
                ((second != null) ? second.equals(other.second) : (other.second == null)));
    }
    
    @Override
    public int hashCode() {
        return 31 * ((first != null) ? first.hashCode() : 0) +
                    ((second != null) ? second.hashCode() : 0);
    }

    @Override
    public String toString() {
        return "[" + first + "," + second + "]";
    }

}
