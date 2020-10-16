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
        BigramModel model = BigramModelBuilder.buildModel(Arrays.asList(
                Arrays.asList("howdy", "y'all", ".", "How", "are", "ya", "?"),
                Arrays.asList(",", "howdy", "pardner"),
                Arrays.asList("keep", "yer", "!", "boots", "on"),
                Arrays.asList(".", "I", "reckon"),
                Arrays.asList("who", "poisoned", "the", "water", "hole")
        ));
        bigrammer = new Bigrammer(model);
    }

    @Test
    public void testKnownPairGeneration() {
        List<String> expected = Lists.newArrayList("howdy", "y'all", ".", "How", "are", "ya", "?");
        List<String> generated = bigrammer.generatePhraseWithKnownPair("How", "are");
        Assert.assertEquals(expected, generated);

        expected = Lists.newArrayList("howdy", "pardner");
        generated = bigrammer.generatePhraseWithKnownPair("howdy", "pardner");
        Assert.assertEquals(expected, generated);

        expected = Lists.newArrayList("howdy", "y'all", ".");
        generated = bigrammer.generatePhraseWithKnownPair("howdy", "y'all");
        Assert.assertEquals(expected, generated);

        expected = Lists.newArrayList("I", "reckon");
        generated = bigrammer.generatePhraseWithKnownPair(".", "I");
        Assert.assertEquals(expected, generated);

        expected = Lists.newArrayList("howdy", "y'all", ".", "How", "are", "ya", "?");
        generated = bigrammer.generatePhraseWithKnownPair("?", DELIM);
        Assert.assertEquals(expected, generated);
    }

    @Test
    public void testRandomGeneration() {
        List<String> expected = Lists.newArrayList("howdy", "pardner");
        List<String> generated = bigrammer.generateRandom("pardner");
        Assert.assertEquals(expected, generated);

        expected = Lists.newArrayList("howdy", "pardner");
        generated = bigrammer.generateRandom("howdy", "pardner");
        Assert.assertEquals(expected, generated);

        expected = Lists.newArrayList("howdy", "y'all", ".", "How", "are", "ya", "?");
        generated = bigrammer.generateRandom("How", "are");
        Assert.assertEquals(expected, generated);

        // this is more subjective and addresses appropriate sentence endings. save it for a different refactor
        expected = Lists.newArrayList("howdy", "y'all", ".");
        generated = bigrammer.generateRandom("howdy", "y'all");
        Assert.assertEquals(expected, generated);
    }

    @Test
    public void testForwardsSentenceGenerationLongSentence() {
        List<String> expected = Lists.newArrayList("How", "are", "ya", "?");
        List<String> generated = bigrammer.generateRandomForwards("How");
        Assert.assertEquals(expected, generated);

        expected = Lists.newArrayList("are", "ya", "?");
        generated = bigrammer.generateRandomForwards("are");
         Assert.assertEquals(expected, generated);

        expected = Lists.newArrayList("ya", "?");
        generated = bigrammer.generateRandomForwards("ya");
         Assert.assertEquals(expected, generated);
    }

    @Test
    public void testForwardsSentenceGenerationTwoWords() {
        List<String> expected = Lists.newArrayList("pardner");
        List<String> generated = bigrammer.generateRandomForwards("pardner");
        Assert.assertEquals(expected, generated);

        expected = Lists.newArrayList("I", "reckon");
        generated = bigrammer.generateRandomForwards("I");
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

        expected = Lists.newArrayList("keep");
        generated = bigrammer.generateRandomBackwards("keep");
        Assert.assertEquals(expected, generated);
    }

    @Test
    public void testBackwardsSentenceGenerationWordCount() {
        List<String> expected = Lists.newArrayList("howdy", "y'all", ".", "How", "are");
        List<String> generated = bigrammer.generateRandomBackwards("are", 5, 5);
        Assert.assertEquals(expected, generated);

        // does not run over DELIM even though it's allowed to
        expected = Lists.newArrayList("keep", "yer", "!", "boots", "on");
        generated = bigrammer.generateRandomBackwards("on", 4, 8);
        Assert.assertEquals(expected, generated);

        // return an empty list to indicate we couldn't fulfill the requirements
        expected = Lists.newArrayList();
        generated = bigrammer.generateRandomBackwards("pardner", 4, 8);
        Assert.assertEquals(expected, generated);

        // return null to indicate we don't have that word in our model
        generated = bigrammer.generateRandomBackwards("WHATEVER", 4, 8);
        Assert.assertNull(generated);
    }

    @Test
    public void testBackwardsSentenceGenerationSyllables() {
        List<String> expected = Lists.newArrayList("hole");
        List<String> generated = bigrammer.generateRandomBackwardsSyllables("hole", 1);
        Assert.assertEquals(expected, generated);

        expected = Lists.newArrayList("water", "hole");
        generated = bigrammer.generateRandomBackwardsSyllables("hole", 2);
        Assert.assertEquals(expected, generated);

        expected = Lists.newArrayList("water");
        generated = bigrammer.generateRandomBackwardsSyllables("water", 2);
        Assert.assertEquals(expected, generated);

        expected = Lists.newArrayList("poisoned", "the", "water");
        generated = bigrammer.generateRandomBackwardsSyllables("water", 5);
        Assert.assertEquals(expected, generated);
    }

    @Test
    public void testForwardGeneration() {
        List<String> expected = Lists.newArrayList("How", "are", "ya", "?");
        List<String> forward = bigrammer.generateForwardText("How", "are");
        Assert.assertEquals(expected, forward);
    }

    @Test
    public void testForwardGenerationDELIM() {
        List<String> expected = Lists.newArrayList("pardner", DELIM);
        List<String> forward = bigrammer.generateForwardText("pardner", DELIM);
        Assert.assertEquals(expected, forward);

        expected = Lists.newArrayList(DELIM, "keep", "yer", "!");
        forward = bigrammer.generateForwardText(DELIM, "keep");
        Assert.assertEquals(expected, forward);
    }

    @Test
    public void testBackwardsGeneration() {
        // default maxHalfLength is 8, so this stops because of DELIM
        List<String> expected = Lists.newArrayList("howdy", "y'all", ".", "How", "are");
        List<String> back = bigrammer.generateBackwardText("How", "are");
        Assert.assertEquals(expected, back);
    }

    @Test
    public void testBackwardsGenerationDELIM() {
        List<String> expected = Lists.newArrayList(DELIM, "howdy");
        List<String> back = bigrammer.generateBackwardText(DELIM, "howdy");
        Assert.assertEquals(expected, back);
    }

    @Test
    public void testBackwardsGenerationBadParams() {
        // 8 > 4
        List<String> expected = Lists.newArrayList("keep", "yer");
        List<String> back = bigrammer.generateBackwardText("keep", "yer", 8, 4);
        Assert.assertEquals(expected, back);

        // 8 > 0
        expected = Lists.newArrayList();
        back = bigrammer.generateRandomBackwards("yer", 8, 0);
        Assert.assertEquals(expected, back);
    }

    @Test
    public void testBackwardsGenerationWordCount() {
        List<String> expected = Lists.newArrayList("howdy", "y'all", ".", "How", "are");
        List<String> back = bigrammer.generateBackwardText("How", "are", 0, 5);
        Assert.assertEquals(expected, back);

        for (int minWordCount = 0; minWordCount <= 4; minWordCount++) {
            back = bigrammer.generateBackwardText("How", "are", minWordCount, 5);
            Assert.assertEquals(expected, back);
        }

        back = bigrammer.generateBackwardText("How", "are", 0, 6);
        Assert.assertEquals(expected, back);

        back = bigrammer.generateBackwardText("How", "are", 0, 200);
        Assert.assertEquals(expected, back);

        expected = Lists.newArrayList("y'all", ".", "How", "are");
        back = bigrammer.generateBackwardText("How", "are", 0, 4);
        Assert.assertEquals(expected, back);

        expected = Lists.newArrayList("How", "are");
        back = bigrammer.generateBackwardText("How", "are", 0, 3);
        Assert.assertEquals(expected, back);

        expected = Lists.newArrayList("How", "are");
        back = bigrammer.generateBackwardText("How", "are", 0, 2);
        Assert.assertEquals(expected, back);

        expected = Lists.newArrayList("howdy", "y'all", ".", "How", "are");
        back = bigrammer.generateBackwardText("How", "are", 5, 5);
        Assert.assertEquals(expected, back);
    }

    @Test
    public void testBackwardsGenerationSyllables() {
        List<String> expected = Lists.newArrayList("water", "hole");
        List<String> back = bigrammer.generateBackwardSyllables("water", "hole", 1);
        Assert.assertEquals(expected, back);

        expected = Lists.newArrayList("water", "hole");
        back = bigrammer.generateBackwardSyllables("water", "hole", 3);
        Assert.assertEquals(expected, back);

        expected = Lists.newArrayList("the", "water", "hole");
        back = bigrammer.generateBackwardSyllables("water", "hole", 4);
        Assert.assertEquals(expected, back);

        expected = Lists.newArrayList("poisoned", "the", "water", "hole");
        back = bigrammer.generateBackwardSyllables("water", "hole", 5);
        Assert.assertEquals(expected, back);

        expected = Lists.newArrayList("poisoned", "the", "water", "hole");
        back = bigrammer.generateBackwardSyllables("water", "hole", 6);
        Assert.assertEquals(expected, back);

        expected = Lists.newArrayList("who", "poisoned", "the", "water", "hole");
        back = bigrammer.generateBackwardSyllables("water", "hole", 7);
        Assert.assertEquals(expected, back);

        expected = Lists.newArrayList("who", "poisoned", "the", "water", "hole");
        back = bigrammer.generateBackwardSyllables("water", "hole", 8);
        Assert.assertEquals(expected, back);
    }

    @Test
    public void testSeedWordBehavior() {
        Assert.assertNotNull(bigrammer.generateRandom("howdy"));
        Assert.assertNotNull(bigrammer.generateRandom("?"));
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
