package com.scottlogic.util;

import java.util.Comparator;

/**
 * Provides a {@code SortedList} which sorts the elements by their natural order.
 *
 * @param <T> any {@code Comparable}
 * @author Mark Rhodes
 * @version 1.1
 * @see SortedList
 */
public class NaturalSortedList<T extends Comparable<? super T>> extends SortedList<T> {

    private static final long serialVersionUID = -8834713008973648930L;

    /**
     * Constructs a new @{code NaturalSortedList} which sorts elements according to their <i>natural order</i>.
     */
    public NaturalSortedList() {
        super(new Comparator<T>() {
            @Override
            public int compare(final T one, final T two) {
                return one.compareTo(two);
            }
        });
    }
}
