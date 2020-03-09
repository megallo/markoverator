package com.github.megallo.markoverator.poet;

import com.google.common.collect.Lists;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;

public class PoetTest {

    static Poet poet;
    private static final String mockDict = "/com/github/megallo/markoverator/poet/mockCmuDict.txt";
    private static final String realPhones = "/com/github/megallo/markoverator/poet/cmudict-0.7b-phones.txt";
    private static final String realSymbols = "/com/github/megallo/markoverator/poet/cmudict-0.7b-symbols.txt";

    @BeforeClass
    public static void setup() {
        poet = new Poet(
                PoetTest.class.getResourceAsStream(mockDict),
                PoetTest.class.getResourceAsStream(realPhones),
                PoetTest.class.getResourceAsStream(realSymbols),
                null);
    }

    public PoetTest() {
        super();
    }

    /**
     * Verify that the same algorithm that creates rhymes is also used by the rhyme looker-upper
     */
    @Test
    public void testPutMatchesLookup() {
        // CAT-O-NINE-TAILS
        // K AE1 T OW0 N AY2 N T EY2 L Z
        String expected = "cat-o-nine-tails";
        Assert.assertEquals(expected, poet.findRhymingWords("CAT-O-NINE-TAILS").get(0));
    }

    @Test
    public void testRhymingSection() {
        // CAT-O-NINE-TAILS
        // K AE1 T OW0 N AY2 N T EY2 L Z
        List<String> input = Lists.newArrayList("K", "AE", "T", "OW", "N", "AY", "N", "T", "EY", "L", "Z");
        String expected = "EYLZ";
        Assert.assertEquals(expected, poet.getRhymingSection(input));

        // PONY
        // P OW1 N IY2
        input = Lists.newArrayList("P", "OW", "N", "IY");
        expected = "NIY";
        Assert.assertEquals(expected, poet.getRhymingSection(input));

        // PLAYS
        // P L EY1 Z
        input = Lists.newArrayList("P", "L", "EY", "Z");
        expected = "EYZ";
        Assert.assertEquals(expected, poet.getRhymingSection(input));

        // A
        // AH0
        input = Lists.newArrayList("AH");
        expected = "AH";
        Assert.assertEquals(expected, poet.getRhymingSection(input));
    }
}
