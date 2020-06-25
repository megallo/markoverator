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

    @Test
    public void testFindRhymes() {
        List<String> actual = poet.findRhymingWords("curb");
        List<String> expected = Lists.newArrayList("blurb", "curb"); // does not rhyme with blur
        actual.sort(String::compareTo); // we shuffled, so un-shuffle
        Assert.assertEquals(expected, actual);

        actual = poet.findRhymingWords("acts");
        expected = Lists.newArrayList("acts", "facts");
        actual.sort(String::compareTo); // we shuffled, so un-shuffle
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testRhymingSection() {
        /* A bunch of syllables, starts with a consonant */
        // CAT-O-NINE-TAILS
        // K AE1 T OW0 N AY2 N T EY2 L Z
        List<String> input = Lists.newArrayList("K", "AE", "T", "OW", "N", "AY", "N", "T", "EY", "L", "Z");
        List<String> expected = Lists.newArrayList("AETOWNAYNTEYLZ", "TOWNAYNTEYLZ", "OWNAYNTEYLZ", "NAYNTEYLZ", "AYNTEYLZ", "NTEYLZ", "TEYLZ", "EYLZ");
        Assert.assertEquals(expected, poet.getRhymingSection(input));

        /* Three syllables, starts with a consonant and ends with a consonant*/
        // QUARANTINE
        // K W AO1 R AH0 N T IY2 N
        input = Lists.newArrayList("K", "W", "AO", "R", "AH", "N", "T", "IY", "N");
        expected = Lists.newArrayList("AORAHNTIYN", "RAHNTIYN", "AHNTIYN", "NTIYN", "TIYN", "IYN");
        Assert.assertEquals(expected, poet.getRhymingSection(input));

        /* Three syllables, starts with a vowel and ends with a consonant*/
        // OBSOLETE
        // AA1 B S AH0 L IY2 T
        input = Lists.newArrayList("AA", "B", "S", "AH", "L", "IY", "T");
        expected = Lists.newArrayList("AABSAHLIYT", "BSAHLIYT", "SAHLIYT", "AHLIYT", "LIYT", "IYT");
        Assert.assertEquals(expected, poet.getRhymingSection(input));

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
    public void testRhymeLengthOrder() {
        List<String> rhymesOfTomato = poet.wordToRhymes.get("tomato");
        List<String> expected = Lists.newArrayList("AHMEYTOW", "AHMAATOW", "MEYTOW", "MAATOW", "EYTOW", "AATOW", "TOW");

        Assert.assertEquals(expected, rhymesOfTomato);

        // after sorting it should be ordered based off of the pronunciation we read in first
        // this allows us to be confident in the user's custom pronunciation being available before CMU
        List<String> actual = poet.findRhymingWords("tomato");
        expected = Lists.newArrayList("tomato", "potato", "grotto");
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testRhymeLengthOrder1000Times() {

        // the sort order of longest - shortest keeps the initial order within a given length
        // this allows us to be confident in the user's custom pronunciation being available before CMU
        for (int i = 0; i < 1000; i++) {
            Poet localPoet = new Poet(
                    PoetTest.class.getResourceAsStream(mockDict),
                    PoetTest.class.getResourceAsStream(realPhones),
                    PoetTest.class.getResourceAsStream(realSymbols),
                    null);
            List<String> rhymesOfTomato = localPoet.wordToRhymes.get("tomato");
            List<String> expected = Lists.newArrayList("AHMEYTOW", "AHMAATOW", "MEYTOW", "MAATOW", "EYTOW", "AATOW", "TOW");

            Assert.assertEquals(expected, rhymesOfTomato);
        }
    }

    @Test
    public void testRemoveWordCounter() {
        Assert.assertEquals("PRESTIGIOUS", poet.removeWordCounter("PRESTIGIOUS(1)"));
        Assert.assertEquals("FACTS", poet.removeWordCounter("FACTS(1)"));
    }
}
