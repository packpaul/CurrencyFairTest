package com.pp.currencyfairtest.mtprocessor.utils;

public final class Triple<T1, T2, T3> {
    private final T1 first;
    private final T2 second;
    private final T3 third;


    private Triple(T1 first, T2 second, T3 third) {
        this.first = first;
        this.second = second;
        this.third = third;
    }

    public static <T1, T2, T3> Triple<T1, T2, T3> create(T1 first, T2 second, T3 third) {
        return new Triple(first, second, third);
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
    
    public T3 getThird() {
        return third;
    }
    
    public T3 t() {
        return third;
    }  
    
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if ((obj == null) || (obj.getClass() != Triple.class)) {
            return false;
        }
        Triple<?, ?, ?> other = (Triple<?, ?, ?>) obj;

        return (((first != null) ? first.equals(other.first) : (other.first == null)) &&
                ((second != null) ? second.equals(other.second) : (other.second == null)) &&
                ((third != null) ? third.equals(other.third) : (other.third == null)));
    }
    
    @Override
    public int hashCode() {
        return 47 * ((first != null) ? first.hashCode() : 0) +
                    ((second != null) ? second.hashCode() : 0) +
                    ((third != null) ? third.hashCode() : 0);
    }

    @Override
    public String toString() {
        return "[" + first + "," + second + "," + third + "]";
    }

}
