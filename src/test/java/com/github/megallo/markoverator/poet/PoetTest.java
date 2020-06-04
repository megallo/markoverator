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

    // TODO test the model, since you loaded a sample you know exactly what's in your maps at this point
    @Test
    public void testFindRhymes() {
        List<String> rhymesWithActs = poet.findRhymingWords("acts");
        List<String> expected = Lists.newArrayList("acts", "facts");
        Assert.assertEquals(expected, rhymesWithActs);
    }

    @Test
    public void testRhymingSection() {
        /* A bunch of syllables, starts with a consonant */
        // CAT-O-NINE-TAILS
        // K AE1 T OW0 N AY2 N T EY2 L Z
        List<String> input = Lists.newArrayList("K", "AE", "T", "OW", "N", "AY", "N", "T", "EY", "L", "Z");
        List<String> expected = Lists.newArrayList("AETOWNAYNTEYLZ", "TOWNAYNTEYLZ", "OWNAYNTEYLZ", "NAYNTEYLZ", "AYNTEYLZ", "NTEYLZ", "TEYLZ", "EYLZ");
        Assert.assertEquals(expected, poet.getRhymingSection(input));

        // TODO figure out if 2 and 3 syllable behavior is the same, write a test here for 3

        /* Two syllables, starts with a vowel */
        // EXCHANGE
        // IH0 K S CH EY1 N JH
        input = Lists.newArrayList("IH", "K", "S", "CH", "EY", "N", "JH");
        expected = Lists.newArrayList("IHKSCHEYNJH", "KSCHEYNJH", "SCHEYNJH", "CHEYNJH", "EYNJH");
        Assert.assertEquals(expected, poet.getRhymingSection(input));

        /* Two syllables, starts with a consonant and ends with a vowel*/
        // PONY
        // P OW1 N IY2
        input = Lists.newArrayList("P", "OW", "N", "IY");
        expected = Lists.newArrayList("OWNIY", "NIY");
        Assert.assertEquals(expected, poet.getRhymingSection(input));

        /* Two syllables, starts with a vowel and ends with a vowel*/
        // ALTER  AO1 L T ER0
        // rhymes with falter, halter
        input = Lists.newArrayList("AO", "L", "T", "ER");
        expected = Lists.newArrayList("AOLTER", "LTER", "TER");
        Assert.assertEquals(expected, poet.getRhymingSection(input));

        /* One syllable, is only a vowel */
        // A
        // AH0
        input = Lists.newArrayList("AH");
        expected = Lists.newArrayList("AH");
        Assert.assertEquals(expected, poet.getRhymingSection(input));

        /* One syllable, ends with a vowel */
        // BYE
        // B AY1
        input = Lists.newArrayList("B", "AY");
        expected = Lists.newArrayList("AY");
        Assert.assertEquals(expected, poet.getRhymingSection(input));

        /* One syllable, starts with a vowel */
        // ACT
        // AE1 K T
        // rhymes with fact, intact
        input = Lists.newArrayList("AE", "K", "T");
        expected = Lists.newArrayList("AEKT");
        Assert.assertEquals(expected, poet.getRhymingSection(input));

        /* One syllable, starts and ends with a consonant */
        // PLAYS
        // P L EY1 Z
        input = Lists.newArrayList("P", "L", "EY", "Z");
        expected = Lists.newArrayList("EYZ");
        Assert.assertEquals(expected, poet.getRhymingSection(input));

    }

    @Test
    public void testRemoveWordCounter() {
        Assert.assertEquals("PRESTIGIOUS", poet.removeWordCounter("PRESTIGIOUS(1)"));
        Assert.assertEquals("FACTS", poet.removeWordCounter("FACTS(1)"));
    }
}
