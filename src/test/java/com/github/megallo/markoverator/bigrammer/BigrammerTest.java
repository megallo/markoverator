/*
 * Copyright 2015 Megan Galloway
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.megallo.markoverator.bigrammer;

import com.google.common.collect.Lists;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static com.github.megallo.markoverator.bigrammer.Bigrammer.DELIM;

public class BigrammerTest {

    static Bigrammer bigrammer;

    @BeforeClass
    public static void setup() {
        bigrammer = new Bigrammer();
        bigrammer.buildModel(Arrays.asList(
                Arrays.asList("howdy", "y'all", ".", "How", "are", "ya", "?", DELIM),
                Arrays.asList(DELIM, "howdy", "pardner", DELIM)
        ));
    }

    @Test
    public void testSentenceGeneration() {
        List<String> expected = Lists.newArrayList("howdy", "y'all", ".", "How", "are", "ya", "?");
        List<String> generated = bigrammer.generatePhraseWithKnownPair("How", "are");
        Assert.assertEquals(expected, generated);

        expected = Lists.newArrayList("howdy", "pardner");
        generated = bigrammer.generatePhraseWithKnownPair("howdy", "pardner");
         Assert.assertEquals(expected, generated);
    }

    @Test
    public void testForwardsSentenceGeneration() {
        List<String> expected = Lists.newArrayList("How", "are", "ya", "?");
        List<String> generated = bigrammer.generateRandomForwards("How");
        Assert.assertEquals(expected, generated);

        expected = Lists.newArrayList("pardner");
        generated = bigrammer.generateRandomForwards("pardner");
         Assert.assertEquals(expected, generated);
    }

    @Test
    public void testBackwardsSentenceGeneration() {
        List<String> expected = Lists.newArrayList("howdy", "y'all", ".", "How", "are");
        List<String> generated = bigrammer.generateRandomBackwards("are");
        Assert.assertEquals(expected, generated);

        expected = Lists.newArrayList("howdy", "pardner");
        generated = bigrammer.generateRandomBackwards("pardner");
         Assert.assertEquals(expected, generated);
    }

    @Test
    public void testForwardGeneration() {
        List<String> expected = Lists.newArrayList("How", "are", "ya", "?");
        List<String> forward = bigrammer.generateForwardText("How", "are");
        Assert.assertEquals(expected, forward);
    }

    @Test
    public void testBackwardsGeneration() {
        List<String> expected = Lists.newArrayList("howdy", "y'all", ".", "How", "are");
        List<String> back = bigrammer.generateBackwardText("How", "are");
        Assert.assertEquals(expected, back);
    }

    @Test
    public void testSeedWordBehavior() {
        Assert.assertNotNull(bigrammer.generateRandom("howdy"));
        Assert.assertNotNull(bigrammer.getAnyLocationOfSeed("howdy"));

        Assert.assertNull(bigrammer.generateRandom("asdfpoiu123456789||Dffesd"));
        Assert.assertNull(bigrammer.getAnyLocationOfSeed("asdfpoiu123456789||Dffesd"));
    }

    @Test
    public void testDecentEnding() {
        List<String> yep = Arrays.asList("it was I who found the lamp".split(" "));
        List<String> yep2 = Arrays.asList("this is funny".split(" "));
        List<String> nope = Arrays.asList("who found the lamp ? it was I".split(" "));
        List<String> nope2 = Arrays.asList("what kind of thing is her".split(" "));
        List<String> nope3 = Arrays.asList("what kind of thing is this and".split(" "));
        List<String> nope4 = Arrays.asList("what kind of thing is this and just".split(" "));

        Assert.assertTrue(bigrammer.isDecentEndingWord(yep));
        Assert.assertTrue(bigrammer.isDecentEndingWord(yep2));
        Assert.assertFalse(bigrammer.isDecentEndingWord(nope));
        Assert.assertFalse(bigrammer.isDecentEndingWord(nope2));
        Assert.assertFalse(bigrammer.isDecentEndingWord(nope3));
        Assert.assertFalse(bigrammer.isDecentEndingWord(nope4));
    }
}
