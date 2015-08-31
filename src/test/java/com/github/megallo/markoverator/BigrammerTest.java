package com.github.megallo.markoverator;

import java.util.Arrays;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;

public class BigrammerTest {

    // TODO these tests are really about more text utilities; the called methods could live somewhere else

    Bigrammer bigrammer = new Bigrammer();

    @Test
    public void testDecentEnding() {
        List<String> yep = Arrays.asList("it was I who found the lamp".split(" "));
        List<String> yep2 = Arrays.asList("this is funny".split(" "));
        List<String> nope = Arrays.asList("who found the lamp ? it was I".split(" "));
        List<String> nope2 = Arrays.asList("what kind of thing is her".split(" "));
        List<String> nope3 = Arrays.asList("what kind of thing is this and".split(" "));

        Assert.assertTrue(bigrammer.isDecentEndingWord(yep));
        Assert.assertTrue(bigrammer.isDecentEndingWord(yep2));
        Assert.assertFalse(bigrammer.isDecentEndingWord(nope));
        Assert.assertFalse(bigrammer.isDecentEndingWord(nope2));
        Assert.assertFalse(bigrammer.isDecentEndingWord(nope3));
    }
}
