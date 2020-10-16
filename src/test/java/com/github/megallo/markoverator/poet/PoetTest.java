package com.github.megallo.markoverator.poet;

import com.github.megallo.markoverator.bigrammer.BigramModel;
import com.github.megallo.markoverator.bigrammer.BigramModelBuilder;
import com.github.megallo.markoverator.bigrammer.Bigrammer;
import com.google.common.collect.Lists;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

public class PoetTest {

    static Poet poet;
    static Bigrammer bigrammer;
    static Rhymer rhymer;

    private static final String mockDict = "/com/github/megallo/markoverator/poet/mockCmuDict.txt";
    private static final String realPhones = "/com/github/megallo/markoverator/poet/cmudict-0.7b-phones.txt";
    private static final String realSymbols = "/com/github/megallo/markoverator/poet/cmudict-0.7b-symbols.txt";

    @BeforeClass
    public static void setup() {

        BigramModel model = BigramModelBuilder.buildModel(Arrays.asList(
                Arrays.asList("reach", "for", "the", "sky", "!"),
                Arrays.asList("this", "town", "ain't", "big", "enough", "for", "the", "two", "of", "us", "!"),
                Arrays.asList("you're", "my", "favorite", "deputy", "!"),
                Arrays.asList("somebody's", "poisoned", "the", "water", "hole","!"),
                Arrays.asList("I'd", "like", "to", "join", "your", "posse", "boys", ",", "but" , "first", "I'm",
                        "gonna", "sing", "a", "little", "song")
        ));
        bigrammer = new Bigrammer(model);
        rhymer = new Rhymer(
                RhymerTest.class.getResourceAsStream(mockDict),
                RhymerTest.class.getResourceAsStream(realPhones),
                RhymerTest.class.getResourceAsStream(realSymbols),
                null);

        poet = new Poet(bigrammer, rhymer);
    }

    public PoetTest() {
        super();
    }

    @Test
    public void testHaiku() {
        String actual = poet.makePoemLine(bigrammer, "enough", 5);
        String expected = "Town ain't big enough ";
        Assert.assertEquals(expected, actual);

        actual = poet.makePoemLine(bigrammer, "deputy", 7);
        expected = "My favorite deputy ";
        Assert.assertEquals(expected, actual);

        actual = poet.makePoemLine(bigrammer, "song", 5);
        expected = "Sing a little song ";
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testRhymingWords() {
        String unexpected;
        String actual;

        for (int i = 0; i < 100; i++) {
            unexpected = "the";
            actual = poet.getRandomNonRhymingWord(bigrammer, Lists.newArrayList("a"));
            Assert.assertNotEquals("getRandomNonRhymingWord failed for \"a\" on attempt " + i, unexpected, actual);
        }

        for (int i = 0; i < 100; i++) {
            unexpected = "your";
            actual = poet.getRandomNonRhymingWord(bigrammer, Lists.newArrayList("for"));
            Assert.assertNotEquals("getRandomNonRhymingWord failed for \"for\" on attempt " + i, unexpected, actual);
        }

        for (int i = 0; i < 100; i++) {
            unexpected = "sky";
            actual = poet.getRandomNonRhymingWord(bigrammer, Lists.newArrayList("my"));
            Assert.assertNotEquals("getRandomNonRhymingWord failed for \"my\" on attempt " + i, unexpected, actual);
        }

        for (int i = 0; i < 100; i++) {
            unexpected = "my";
            actual = poet.getRandomNonRhymingWord(bigrammer, Lists.newArrayList("sky"));
            Assert.assertNotEquals("getRandomNonRhymingWord failed for \"sky\" on attempt " + i, unexpected, actual);
        }

        for (int i = 0; i < 100; i++) {
            unexpected = "a";
            actual = poet.getRandomNonRhymingWord(bigrammer, Lists.newArrayList("the"));
            Assert.assertNotEquals("getRandomNonRhymingWord failed for \"the\" on attempt " + i, unexpected, actual);
        }

        for (int i = 0; i < 100; i++) {
            unexpected = "two";
            actual = poet.getRandomNonRhymingWord(bigrammer, Lists.newArrayList("to"));
            Assert.assertNotEquals("getRandomNonRhymingWord failed for \"to\" on attempt " + i, unexpected, actual);
        }

        for (int i = 0; i < 100; i++) {
            unexpected = "to";
            actual = poet.getRandomNonRhymingWord(bigrammer, Lists.newArrayList("two"));
            Assert.assertNotEquals("getRandomNonRhymingWord failed for \"two\" on attempt " + i, unexpected, actual);
        }

        for (int i = 0; i < 100; i++) {
            unexpected = "for";
            actual = poet.getRandomNonRhymingWord(bigrammer, Lists.newArrayList("your"));
            Assert.assertNotEquals("getRandomNonRhymingWord failed for \"your\" on attempt " + i, unexpected, actual);
        }
    }

    @Test
    public void testNonRhymingWords() {
        List<String> expected = Lists.newArrayList("a", "the");
        List<String> actual = poet.findRhymingWords("a");
        actual.sort(String::compareTo);
        Assert.assertEquals(expected, actual);

        expected = Lists.newArrayList("ain't");
        actual = poet.findRhymingWords("ain't");
        actual.sort(String::compareTo);
        Assert.assertEquals(expected, actual);

        expected = Lists.newArrayList("big");
        actual = poet.findRhymingWords("big");
        actual.sort(String::compareTo);
        Assert.assertEquals(expected, actual);

        expected = Lists.newArrayList("boys");
        actual = poet.findRhymingWords("boys");
        actual.sort(String::compareTo);
        Assert.assertEquals(expected, actual);

        expected = Lists.newArrayList("but");
        actual = poet.findRhymingWords("but");
        actual.sort(String::compareTo);
        Assert.assertEquals(expected, actual);

        expected = Lists.newArrayList("deputy");
        actual = poet.findRhymingWords("deputy");
        actual.sort(String::compareTo);
        Assert.assertEquals(expected, actual);

        expected = Lists.newArrayList("enough");
        actual = poet.findRhymingWords("enough");
        actual.sort(String::compareTo);
        Assert.assertEquals(expected, actual);

        expected = Lists.newArrayList("favorite");
        actual = poet.findRhymingWords("favorite");
        actual.sort(String::compareTo);
        Assert.assertEquals(expected, actual);

        expected = Lists.newArrayList("first");
        actual = poet.findRhymingWords("first");
        actual.sort(String::compareTo);
        Assert.assertEquals(expected, actual);

        expected = Lists.newArrayList("for", "your");
        actual = poet.findRhymingWords("for");
        actual.sort(String::compareTo);
        Assert.assertEquals(expected, actual);

        expected = Lists.newArrayList("gonna");
        actual = poet.findRhymingWords("gonna");
        actual.sort(String::compareTo);
        Assert.assertEquals(expected, actual);

        expected = Lists.newArrayList("hole");
        actual = poet.findRhymingWords("hole");
        actual.sort(String::compareTo);
        Assert.assertEquals(expected, actual);

        expected = Lists.newArrayList("i'd");
        actual = poet.findRhymingWords("i'd");
        actual.sort(String::compareTo);
        Assert.assertEquals(expected, actual);

        expected = Lists.newArrayList("i'm");
        actual = poet.findRhymingWords("i'm");
        actual.sort(String::compareTo);
        Assert.assertEquals(expected, actual);

        expected = Lists.newArrayList("join");
        actual = poet.findRhymingWords("join");
        actual.sort(String::compareTo);
        Assert.assertEquals(expected, actual);

        expected = Lists.newArrayList("like");
        actual = poet.findRhymingWords("like");
        actual.sort(String::compareTo);
        Assert.assertEquals(expected, actual);

        expected = Lists.newArrayList("little");
        actual = poet.findRhymingWords("little");
        actual.sort(String::compareTo);
        Assert.assertEquals(expected, actual);

        expected = Lists.newArrayList("my", "sky");
        actual = poet.findRhymingWords("my");
        actual.sort(String::compareTo);
        Assert.assertEquals(expected, actual);

        expected = Lists.newArrayList("of");
        actual = poet.findRhymingWords("of");
        actual.sort(String::compareTo);
        Assert.assertEquals(expected, actual);

        expected = Lists.newArrayList("poisoned");
        actual = poet.findRhymingWords("poisoned");
        actual.sort(String::compareTo);
        Assert.assertEquals(expected, actual);

        expected = Lists.newArrayList("posse");
        actual = poet.findRhymingWords("posse");
        actual.sort(String::compareTo);
        Assert.assertEquals(expected, actual);

        expected = Lists.newArrayList("reach");
        actual = poet.findRhymingWords("reach");
        actual.sort(String::compareTo);
        Assert.assertEquals(expected, actual);

        expected = Lists.newArrayList("my", "sky");
        actual = poet.findRhymingWords("sky");
        actual.sort(String::compareTo);
        Assert.assertEquals(expected, actual);

        expected = Lists.newArrayList("sing");
        actual = poet.findRhymingWords("sing");
        actual.sort(String::compareTo);
        Assert.assertEquals(expected, actual);

        expected = Lists.newArrayList("somebody's");
        actual = poet.findRhymingWords("somebody's");
        actual.sort(String::compareTo);
        Assert.assertEquals(expected, actual);

        expected = Lists.newArrayList("song");
        actual = poet.findRhymingWords("song");
        actual.sort(String::compareTo);
        Assert.assertEquals(expected, actual);

        expected = Lists.newArrayList("a", "the");
        actual = poet.findRhymingWords("the");
        actual.sort(String::compareTo);
        Assert.assertEquals(expected, actual);

        expected = Lists.newArrayList("this");
        actual = poet.findRhymingWords("this");
        actual.sort(String::compareTo);
        Assert.assertEquals(expected, actual);

        expected = Lists.newArrayList("to", "two");
        actual = poet.findRhymingWords("to");
        actual.sort(String::compareTo);
        Assert.assertEquals(expected, actual);

        expected = Lists.newArrayList("town");
        actual = poet.findRhymingWords("town");
        actual.sort(String::compareTo);
        Assert.assertEquals(expected, actual);

        expected = Lists.newArrayList("to", "two");
        actual = poet.findRhymingWords("two");
        actual.sort(String::compareTo);
        Assert.assertEquals(expected, actual);

        expected = Lists.newArrayList("us", "virus");
        actual = poet.findRhymingWords("us");
        actual.sort(String::compareTo);
        Assert.assertEquals(expected, actual);

        expected = Lists.newArrayList("water");
        actual = poet.findRhymingWords("water");
        actual.sort(String::compareTo);
        Assert.assertEquals(expected, actual);

        expected = Lists.newArrayList("you're");
        actual = poet.findRhymingWords("you're");
        actual.sort(String::compareTo);
        Assert.assertEquals(expected, actual);

        expected = Lists.newArrayList("for", "your");
        actual = poet.findRhymingWords("your");
        actual.sort(String::compareTo);
        Assert.assertEquals(expected, actual);
    }
}
