package com.github.megallo.markoverator.utils;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class ListsTest {

    @Test
    public void testReverse() {
        // reverse an empty list
        List<String> empty = new ArrayList<>();
        List<String> reversedEmpty = Lists.reverse(empty);
        Assert.assertEquals(0, empty.size());
        Assert.assertEquals(0, reversedEmpty.size());

        // reverse a list of size 1
        List<String> single = new ArrayList<>();
        single.add("X");

        List<String> reversedSingle = Lists.reverse(single);
        Assert.assertEquals(1, single.size());
        Assert.assertEquals(1, reversedSingle.size());
        Assert.assertEquals("X", reversedSingle.get(0));

        // reverse a list of size 3
        List<String> triple = new ArrayList<>();
        triple.add("a");
        triple.add("b");
        triple.add("c");

        List<String> reversedTriple = Lists.reverse(triple);
        Assert.assertEquals(3, triple.size());
        Assert.assertEquals(3, reversedTriple.size());
        Assert.assertEquals("c", reversedTriple.get(0));
        Assert.assertEquals("b", reversedTriple.get(1));
        Assert.assertEquals("a", reversedTriple.get(2));

        // can we add to it?
        reversedTriple.add("d");
        Assert.assertEquals("d", reversedTriple.get(3));
    }
}
