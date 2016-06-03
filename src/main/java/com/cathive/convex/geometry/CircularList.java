package com.cathive.convex.geometry;

import java.util.AbstractList;
import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * An immutable circular list. Allows traversal over the list in both directions.
 *
 * @param <E> element type
 * @author Alexander Erben
 */
class CircularList<E> extends AbstractList<E> {

    private final List<E> wrapped;

    /**
     * Create the list. This class only supports rings with a minimum size of 1.
     *
     * @param wrapped backing list
     */
    CircularList(List<E> wrapped) {
        checkArgument(!wrapped.isEmpty(), "Rings must contain at least one entry.");
        this.wrapped = wrapped;
    }

    @Override
    public E get(int index) {
        if (index >= 0) {
            return this.wrapped.get(index % size());
        } else {
            return this.wrapped.get(size() + (index % size()));
        }
    }

    @Override
    public int size() {
        return this.wrapped.size();
    }

    /**
     * Return the first entry of the ring, which by definition is the entry with the index 0.
     *
     * @return first entry
     */
    Entry<E> first() {
        return new Entry<>(this, 0);
    }

    /**
     * An immutable entry in the ring. Allows for access of the next and previous element in the ring.
     *
     * @param <F>
     */
    static class Entry<F> {
        private final int idx;
        private final CircularList<F> wrapper;

        private Entry(CircularList<F> wrapper, int idx) {
            this.wrapper = wrapper;
            this.idx = idx;
        }

        /**
         * Get the wrapped element of this entry.
         *
         * @return wrapped
         */
        F get() {
            return this.wrapper.get(this.idx);
        }

        /**
         * Get the next entry in the source ring from this entry on.
         *
         * @return next entry
         */
        Entry<F> next() {
            return new Entry<>(this.wrapper, this.idx + 1);
        }

        /**
         * Get the previous entry in the source ring from this entry on.
         *
         * @return previous entry.
         */
        Entry<F> prev() {
            return new Entry<>(this.wrapper, this.idx - 1);
        }

        /**
         * Check if the content of the other entry is the same as the content of this entry.
         *
         * @param other to compare
         * @return true if equal, false if not.
         */
        boolean equalContent(Entry<F> other) {
            return get().equals(other.get());
        }

        @Override
        public String toString() {
            return "Entry{" +
                    "idx=" + this.idx +
                    ", wrapper=" + this.wrapper +
                    '}';
        }
    }
}
