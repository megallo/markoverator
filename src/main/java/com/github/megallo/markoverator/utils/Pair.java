package com.github.megallo.markoverator.utils;

/**
 * Serializable pojo
 **/
public class Pair {

    private String first;
    private String second;

    public Pair() {
    }

    public Pair(String one, String two) {
        this.first = one;
        this.second = two;
    }

    public String getFirst() {
        return first;
    }

    public void setFirst(String first) {
        this.first = first;
    }

    public String getSecond() {
        return second;
    }

    public void setSecond(String second) {
        this.second = second;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Pair pair = (Pair) o;

        if (!first.toLowerCase().equals(pair.first.toLowerCase())) return false;
        if (!second.toLowerCase().equals(pair.second.toLowerCase())) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = first.toLowerCase().hashCode();
        result = 31 * result + second.toLowerCase().hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "Pair{ " + first + " " + second + " }";
    }
}
