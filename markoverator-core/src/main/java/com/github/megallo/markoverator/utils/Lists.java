package com.github.megallo.markoverator.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class Lists {

    /**
     * Create a new ArrayList from varargs.
     *
     * @param elements the elements to add to the ArrayList
     * @param <E>      the type of elements
     * @return a new ArrayList of elements
     */
    @SafeVarargs
    public static <E> ArrayList<E> newArrayList(E... elements) {
        ArrayList<E> list = new ArrayList<>(elements.length);
        Collections.addAll(list, elements);
        return list;
    }

    /**
     * Return a new ArrayList containing everything from the given Collection
     * in the order they are returned by the collection's iterator. This is
     * syntactic sugar for the ArrayList constructor.
     *
     * @param collection the collection to convert to an ArrayList
     * @param <E>        the type of elements
     * @return a new ArrayList
     */
    public static <E> ArrayList<E> newArrayList(Collection<? extends E> collection) {
        return new ArrayList<>(collection);
    }

    /**
     * Return a reversed copy of the given list leaving the original list
     * unmodified and iterating over the original values only once.
     *
     * @param list the original list
     * @param <T>  type of elements in the list
     * @return a reversed copy of the original, unmodified list
     */
    public static <T> List<T> reverse(List<T> list) {
        int size = list.size();
        int lastIndex = size - 1;

        // reversed list with same capacity
        List<T> reversed = new ArrayList<>(size);

        // iterate through list in reverse order, appending results
        for (int i = lastIndex; i >= 0; --i) {
            T element = list.get(i);
            reversed.add(element);
        }

        return reversed;
    }
}
