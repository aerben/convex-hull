package com.cathive.convex.ui;


import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyListWrapper;
import javafx.beans.value.ObservableBooleanValue;
import javafx.collections.ObservableListBase;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Optional;

/**
 * Handles undo and redo operations application-wide.
 * Not every operation is eligible to undo. A component may register an operation that can be undone by passing
 * an implementation of {@link UndoRedoUnit} to {@link #addUnit(UndoRedoUnit)}.
 * If {@link #undo()} is called, the last element of the stack of undo operations is undone and added to the front of
 * the redo stack. Redoing an operation will reintroduce it to the undo-stack. If another operation is introduced
 * through {@link #addUnit(UndoRedoUnit)}, the redo stack is lost.
 *
 * @author Alexander Erben
 */
public class UndoRedoHandler {

    /**
     * The last 10 undoable operations.
     */
    private final RestrictedStack undoStack = new RestrictedStack(10);

    /**
     * The last 10 redoable undone operations.
     */
    private final RestrictedStack redoStack = new RestrictedStack(10);

    /**
     * Inform the handler of an operation that has been performed by the user which can be undone.
     * This will invalidate the current stack of redo-able operations.
     *
     * @param unit to push to the undo stack.
     */
    public void addUnit(final UndoRedoUnit unit) {
        this.redoStack.clear();
        this.undoStack.push(unit);
    }

    /**
     * Undo the last operation that can be undone and push it to the redo stack.
     * Does nothing if no operation is present that can be undone.
     */
    public synchronized void undo() {
        this.undoStack.pop().ifPresent(u -> {
            u.undo();
            this.redoStack.push(u);
        });
    }

    /**
     * Redo the last undone operation and push it to the undo stack.
     * Does nothing if no operation is present that can be undone.
     */
    public synchronized void redo() {
        this.redoStack.pop().ifPresent(u -> {
            u.perform();
            this.undoStack.push(u);
        });
    }

    /**
     * Indicates if operations are present in the undo stack.
     *
     * @return undo available prop
     */
    public ObservableBooleanValue undoAvailableProperty() {
        return this.undoStack.emptyProperty();
    }

    /**
     * Indicates if operations are present in the redo stack.
     *
     * @return redo available prop
     */
    public ObservableBooleanValue redoAvailableProperty() {
        return this.redoStack.emptyProperty();
    }

    /**
     * Implementations describe operations that can be undone and redone.
     */
    interface UndoRedoUnit {
        /**
         * Builder instance
         */
        Factory factory = new Factory();

        /**
         * Builder
         */
        class Factory {
            /**
             * Create a new {@link UndoRedoUnit} from a function
             * that is to be performed on undo and one that is the actual operation to be performed.
             * It is assumed that the undo operation is the inverse of the actual operation.
             *
             * @param perform function to execute once on construction and then again on redo
             * @param undo    function to execute on undo
             * @return unit
             */
            UndoRedoUnit createAndPerformOnce(final Runnable perform, final Runnable undo) {
                perform.run();
                return new UndoRedoUnit() {
                    @Override
                    public void undo() {
                        undo.run();
                    }

                    @Override
                    public void perform() {
                        perform.run();
                    }
                };
            }
        }

        /**
         * Function that should execute on undo. May be called more than once with calls to {@link #perform()} in
         * between.
         */
        void undo();

        /**
         * Actual operation the user performs. May be called more than once with calls to {@link #undo()} in
         * between.
         */
        void perform();
    }

    /**
     * A stack that has a limited size. Has the usual FIFO-stack-semantics which will we not explain in detail
     * on each method.
     * @author Alexander Erben
     */
    private static final class RestrictedStack {
        /**
         * Delegate observable list implementation
         */
        private final ObservableLinkedList<UndoRedoUnit> delegate = new ObservableLinkedList<>();

        /**
         * Maximum size of the stack
         */
        private final int size;

        /**
         * Max size
         * @param size size
         */
        private RestrictedStack(final int size) {
            this.size = size;
        }

        /**
         * Push a new unit on the stack. Removes the last element from the stack if it is full.
         * @param unit to push
         */
        private void push(final UndoRedoUnit unit) {
            if (this.delegate.size() == this.size) {
                this.delegate.removeLast();
            }
            this.delegate.addFirst(unit);
        }

        /**
         * Pops the first element off the stack, or returns none if the stack is empty.
         * @return unit
         */
        private Optional<UndoRedoUnit> pop() {
            if (this.delegate.isEmpty()) {
                return Optional.empty();
            } else {
                return Optional.of(this.delegate.removeFirst());
            }
        }

        /**
         * Clear the stack.
         */
        private void clear() {
            this.delegate.clear();
        }

        /**
         * A property that indicates it the underlying delegate is empty.
         * @return empty prop
         */
        private ReadOnlyBooleanProperty emptyProperty() {
            return new ReadOnlyListWrapper<>(this.delegate).emptyProperty();
        }

    }

    /**
     * A wrapper around {@link LinkedList} and {@link ObservableListBase}
     * that exposes some functionality needed in {@link UndoRedoHandler}.
     *
     * @param <Item> element type
     * @author Alexander Erben
     */
    private static class ObservableLinkedList<Item> extends ObservableListBase<Item> {

        private final LinkedList<Item> delegate = new LinkedList<>();

        /**
         * Add an item to the front of the list
         * @param item to add
         */
        private void addFirst(final Item item) {
            beginChange();
            try {
                this.delegate.addFirst(item);
                nextAdd(0, 1);
            } finally {
                endChange();
            }
        }

        /**
         * Remove the first element of the list.
         * Throws a {@link java.util.NoSuchElementException} if the list is empty.
         * @return first element
         */
        private Item removeFirst() {
            beginChange();
            try {
                final Item item = this.delegate.removeFirst();
                nextRemove(0, item);
                return item;
            } finally {
                endChange();
            }
        }

        /**
         * Remove the last element of the list.
         * Throws a {@link java.util.NoSuchElementException} if the list is empty.
         */
        private void removeLast() {
            beginChange();
            try {
                final Item item = this.delegate.removeLast();
                nextRemove(this.delegate.size() - 1, item);
            } finally {
                endChange();
            }
        }

        @Override
        public Item get(final int index) {
            final Iterator<Item> iterator = this.delegate.iterator();
            for (int i = 0; i < index; i++) iterator.next();
            return iterator.next();
        }

        @Override
        public int size() {
            return this.delegate.size();
        }

        @Override
        public Item remove(final int index) {
            beginChange();
            try {
                final Item removed = this.delegate.remove(index);
                nextRemove(index, removed);
                return removed;
            } finally {
                endChange();
            }
        }
    }
}
