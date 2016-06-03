package com.cathive.convex.geometry;

import com.google.common.collect.ImmutableList;
import org.junit.Test;

import java.util.Iterator;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class CircularListTest {

    @Test
    public void testPositiveInRange(){
        List<Integer> wrapped = ImmutableList.of(1, 2, 3, 4, 5);
        CircularList<Integer> uut = new CircularList<>(wrapped);

        Iterator<Integer> wrappedIt = wrapped.iterator();
        Iterator<Integer> uutIt = uut.iterator();

        while (wrappedIt.hasNext()) {
            Integer nextWrapped = wrappedIt.next();
            assertEquals(nextWrapped, uutIt.next());
        }
    }


    @Test
    public void testPositiveOutOfRange(){
        List<Integer> wrapped = ImmutableList.of(1, 2, 3, 4, 5);
        CircularList<Integer> uut = new CircularList<>(wrapped);

        for (int i = 0; i < wrapped.size(); i++) {
            assertEquals(uut.get(i + wrapped.size()), wrapped.get(i));
        }

    }


    @Test
    public void testNegativeInRange(){
        List<Integer> wrapped = ImmutableList.of(1, 2, 3, 4, 5);
        CircularList<Integer> uut = new CircularList<>(wrapped);

        for (int i = -1; i > -wrapped.size(); i--) {
            assertEquals(uut.get(i), wrapped.get(wrapped.size() + i));
        }
    }


    @Test
    public void testNegativeOutOfRange(){
        List<Integer> wrapped = ImmutableList.of(1, 2, 3, 4, 5);
        CircularList<Integer> uut = new CircularList<>(wrapped);
        uut.get(-11);
        for (int i = -1; i > -wrapped.size(); i--) {
            assertEquals(uut.get(i - uut.size()), wrapped.get(wrapped.size() + i));
        }

    }
}