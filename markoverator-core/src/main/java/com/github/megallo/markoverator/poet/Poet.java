/*
 * Copyright 2017 Megan Galloway
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

package com.github.megallo.markoverator.poet;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

/**
 * Find words that rhyme using cmuDict
 **/
public class Poet {

    private static final Logger loggie = LoggerFactory.getLogger(Poet.class);

    public static final String cmuDictLocation = "/com/github/megallo/markoverator/poet/cmudict-0.7b.txt";
    public static final String cmuPhonemeLocation = "/com/github/megallo/markoverator/poet/cmudict-0.7b-phones.txt";
    public static final String cmuSymbolsLocation = "/com/github/megallo/markoverator/poet/cmudict-0.7b-symbols.txt";
    public static final String myDictLocation = "/com/github/megallo/markoverator/poet/extras-dict.txt";

    private final static String cmuDictComment = ";;;";

    // TODO srsly this doesn't change once you build the model, so use Kryo and dump it somewhere. add a method to load the model for initializeDictionary

    Set<String> vowels = new HashSet<>();

    // the raw words mapped to their rhyming sections
    // lestrange -> [EHSTREYNJH, STREYNJH, TREYNJH, REYNJH, EYNJH]
    Map<String, List<String>> wordToRhymes = new HashMap<>();

    // the rhyming sections mapped to lists of words that end with that rhyming section
    // EYNJH -> [strange, mange, arrange, prearrange, ... ]
    Map<String, List<String>> rhymeToWords = new HashMap<>();

    /**
     * Default constructor if you just want the CMU dictionaries
     */
    public Poet() {
        initializeDictionaries(cmuDictLocation, cmuPhonemeLocation, cmuSymbolsLocation, myDictLocation);
    }

    public Poet(InputStream cmuDictStream, InputStream cmuPhonesStream, InputStream cmuSymbolsStream, InputStream extraDictStream) {
        initializeDictionaries(cmuDictStream, cmuPhonesStream, cmuSymbolsStream, extraDictStream);
    }

    // call initializeDictionaries before first use so we have something to work with
    public void initializeDictionaries(String cmuDictClasspath, String cmuPhonesClasspath, String cmuSymbolsClasspath) {
        initializeDictionaries(cmuDictClasspath, cmuPhonesClasspath, cmuSymbolsClasspath, null);
    }

    public void initializeDictionaries(String cmuDictClasspath, String cmuPhonesClasspath, String cmuSymbolsClasspath, String extraDictClasspath) {
        InputStream extra = null;
        if (extraDictClasspath != null) {
            extra =Poet.class.getResourceAsStream(extraDictClasspath);
        }
        initializeDictionaries(Poet.class.getResourceAsStream(cmuDictClasspath), Poet.class.getResourceAsStream(cmuPhonesClasspath),
                Poet.class.getResourceAsStream(cmuSymbolsClasspath), extra);
    }

    public void initializeDictionaries(InputStream cmuDictStream, InputStream cmuPhonesStream, InputStream cmuSymbolsStream, InputStream extraDictStream) {
        try {
            populatePhonemes(cmuPhonesStream); // do this first to get the vowels
            if (extraDictStream != null) { // load any custom entries first so they take priority
                populateCmuMap(extraDictStream);
            }
            populateCmuMap(cmuDictStream);
        } catch (IOException e) {
            loggie.error("Unable to load CMU files", e);
        }
        loggie.info("Loaded rhyme dictionary; found {} words", wordToRhymes.size());

        if (loggie.isDebugEnabled()) {
            logStats();
        }
    }

    /**
     * Look up all known English words that rhyme with the target word
     * Not limited by any Bigrammer user-defined model, this is all from CMU dict and extras-dict
     *
     * @param targetWord we want things that rhyme with this
     * @return list of unique words that rhyme with targetWord, including targetWord itself
     */
    public List<String> findRhymingWords(String targetWord) {
        if (wordToRhymes.containsKey(targetWord.toLowerCase())) { // is this word in the dictionary?
            Set<String> allRhymingWords = new LinkedHashSet<>();

            List<String> rhymingSections = wordToRhymes.get(targetWord.toLowerCase()); // wordPhonemes contained numbers but now does not
            // wordToRhymes.get("KERFUFFLE") -> ["ERFAHFAHL", "FAHFAHL", "AHFAHL", "FAHL", "AHL"]
            // notably here, the list is ordered from largest to smallest
            // that means the order in which we look up and append the words will be most-rhyming to least-rhyming
            for (String rhyme : rhymingSections) {
                if (rhymeToWords.containsKey(rhyme)) { // TODO I'm pretty sure we can't have something in wordToRhymes unless it's also in endingPhonemesWords, you can remove this check
                    // we have things that rhyme!
                    List<String> wordsThatRhymeWithTargetWord = rhymeToWords.get(rhyme);
                    // if we don't shuffle here then the caller will end up choosing the same words out of their model every time
                    // and I don't like any of my options for how to inform them where the "good rhymes" and the "bad rhymes" are so they could shuffle it themselves
                    Collections.shuffle(wordsThatRhymeWithTargetWord); // TODO I don't like returning non-deterministic results, but this will be mitigated when we add structure to Poet in the future
                    loggie.info("Found {} words that rhyme with {}", wordsThatRhymeWithTargetWord.size(), rhyme);
                    allRhymingWords.addAll(wordsThatRhymeWithTargetWord);
                }
            }

            // this contains the targetWord itself for as many rhyming sections as it had, and it's up to the caller to remove it if they want to???
            return Lists.newArrayList(allRhymingWords);
        }

        return null; // we don't have that word in the dictionary :c
    }

    /**
     * Extract all possible sections that this word could rhyme with.
     * Return them in a list ordered from longest to shortest
     *
     * @param phonemeList for this word
     */
    @VisibleForTesting
    List<String> getRhymingSection(List<String> phonemeList) {
        StringBuilder rhymeMe = new StringBuilder();
        Stack<String> rhymeMeStack = new Stack<>();

        // find the locations of the first and last vowels
        int firstVowel = -1;
        int lastVowel = -2;

        for (int j = 0; j < phonemeList.size(); j++) {
            String pho = phonemeList.get(j);
            if (vowels.contains(pho)) {
                lastVowel = j;
                if (firstVowel == -1) {
                    firstVowel = j;
                }
            }
        }

        // TODO I am deciding not to add only the last vowel in a multi-syllable word. The goal is
        // to prevent terrible rhymes like meander + sister

        // TODO should we keep the whole word in a single-syllable word?
        // Do these rhyme: bye + goodbye

        // TODO what do about prefixes on the base word?
        // Do these rhyme: agreeable + disagreeable
        // but how do we not become overzealous in throwing things out?
        // Do these rhyme: acts + artifacts + contracts

        // TODO you removed the emphasis markers for more rhymes. Good choice or no?
        // Do these rhyme: agreeable + permeable

        int i;

        // if we have one phoneme, then it's a vowel, add it and don't iterate
        if (phonemeList.size() == 1) {
            // A  AH0
            rhymeMeStack.push(phonemeList.get(0));
        }
        else {
            // if we have only one vowel, add one rhyme which is vowel + everything after it
            if (firstVowel == lastVowel) {
                // BYE  B AY1 -> AY
                // ACT  AE1 K T -> AEKT
                // PLAYS  P L EY1 Z -> EYZ
                for (i = lastVowel; i < phonemeList.size(); i++) {
                    String pho = phonemeList.get(i);
                    rhymeMe.append(pho);
                }
                rhymeMeStack.push(rhymeMe.toString());
            } else {
                int startHere;
                // we have more than one vowel, therefore more than one syllable
                if (lastVowel == phonemeList.size()-1) {
                    // if it ends with a vowel, add rhyme of previous non-vowel + vowel
                    // that means start right before the last vowel
                    // PONY  P OW1 N IY2 -> NIY
                    // ALTER  AO1 L T ER0 -> TER
                    startHere = lastVowel - 1;
                } else {
                    // if it doesn't end with a vowel, start at the last vowel instead and take it and the stuff after it to the end of the word
                    // PLAYS  P L EY1 Z
                    startHere = lastVowel;
                }

                for (i = startHere; i < phonemeList.size(); i++) {
                    String pho = phonemeList.get(i);
                    rhymeMe.append(pho);
                }
                // push that last syllable into the final list
                rhymeMeStack.push(rhymeMe.toString());

                // now take that last syllable and keep prepending the phones to it until you reach the first vowel
                // PONY  P OW1 N IY2 -> OWNIY
                // ALTER  AO1 L T ER0 -> LTER, AOLTER
                for (i = startHere - 1; i >= firstVowel; i--) {
                    String pho = phonemeList.get(i);
                    rhymeMe.insert(0, pho);
                    rhymeMeStack.push(rhymeMe.toString());
                }

            }
        }

        Collections.reverse(rhymeMeStack);
        return rhymeMeStack;
    }

    private void populateCmuMap(InputStream cmuDict) throws IOException {
        try(BufferedReader reader = new BufferedReader(new InputStreamReader(cmuDict))) {
            String line;
            // a line looks like this:
            // COAXIAL  K OW1 AE1 K S IY0 AH0 L
            while((line = reader.readLine()) != null) {
                if (!line.isEmpty() && !line.startsWith(cmuDictComment)) {
                    String[] split = line.split("\\s+"); // break on whitespace
                    String word = split[0].toLowerCase(); // the first item on the line is the word in plaintext

                    List<String> phonemes = Lists.newArrayList();
                    // start at 1 so we skip the word and only iterate over the phonemes
                    // remove the numbers from the end of the vowel phonemes
                    // these indicate emphasis, which we might want someday, but they'd need to go in their own lookup table because they reduce rhymability
                    // K OW1 AE1 K S IY0 AH0 L ---> K OW AE K S IY AH L
                    for (int i = 1; i < split.length; i++) {
                        phonemes.add(split[i].replaceAll("\\d$", ""));
                    }

                    List<String> rhymingSections = getRhymingSection(phonemes);
                    String actualWord = removeWordCounter(word); // prestigious(1) -> prestigious

                    /* map it from word -> rhyme */

                    // we encounter the word multiple times in the case that it has different pronunciations, like
                    // ACTS  AE1 K T S
                    // ACTS(1)  AE1 K S
                    // but we want to put all rhymes for the homographs into the same list
                    // then we sort by length so in theory we still mostly get the maximum rhyme first
                    // also remove duplicates
                    // This might be heavy but we save computation at runtime by doing it now
                    // also it's only 8,780 times plus the custom dict entries
                    if (wordToRhymes.containsKey(actualWord)) {
                        List<String> rhymesForActualWord = wordToRhymes.get(actualWord);
                        rhymesForActualWord.addAll(rhymingSections);
                        rhymesForActualWord.sort(Comparator.comparingInt(String::length).reversed());
                        wordToRhymes.put(actualWord, Lists.newArrayList(new LinkedHashSet<>(rhymesForActualWord)));
                    } else {
                        wordToRhymes.put(actualWord, Lists.newArrayList(rhymingSections));
                    }

                    /* map it from rhyme -> word */

                    // we have this:
                    // getRhymingSections("KERFUFFLE") -> ["ERFAHFAHL", "FAHFAHL", "AHFAHL", "FAHL", "AHL"]
                    // so for each of the known rhymes for this word, go find the rhyme in the map and add this word
                    // to the list of known words for that rhyme
                    List<String> wordsForThisRhyme;
                    for (String rhyme : rhymingSections) {

                        if (rhymeToWords.containsKey(rhyme)) {
                            wordsForThisRhyme = rhymeToWords.get(rhyme);
                        } else {
                            wordsForThisRhyme = new ArrayList<>();
                            rhymeToWords.put(rhyme, wordsForThisRhyme);
                        }
                        wordsForThisRhyme.add(actualWord);

                    }
                }
            }
        }
    }

    /**
     * Method to log rhyme stats to see if changes to the rhyming algorithm actually did anything.
     * Heavy, should only be used for analyzing changes
     */
    private void logStats() {

        loggie.info("Count of distinct rhymes: {}", rhymeToWords.keySet().size());

        HashMap<Integer, Integer> lengthCountMap = new HashMap<>();
        Collection<List<String>> listsOfRhymingWords = rhymeToWords.values();
        for (List<String> thisList : listsOfRhymingWords) {
            // collect length
            int numberOfWordsThatRhymeWithEachOther = thisList.size();
            Integer currentCount = 1;
            if (lengthCountMap.containsKey(numberOfWordsThatRhymeWithEachOther)) {
                currentCount = lengthCountMap.get(numberOfWordsThatRhymeWithEachOther);
                currentCount++;
            }
            // add 1 to the existing count, or put 1 if this
            lengthCountMap.put(numberOfWordsThatRhymeWithEachOther, currentCount);

            if (numberOfWordsThatRhymeWithEachOther > 1000) {
                loggie.info("Whoa! This word has {} rhymes! {} {} {}", numberOfWordsThatRhymeWithEachOther, thisList.get(0), thisList.get(500), thisList.get(thisList.size()-1));
            }
        }

        ArrayList<Integer> printme2 = new ArrayList<>(lengthCountMap.keySet());
        Collections.sort(printme2);

        for(Integer sortedKey : printme2) {
            loggie.info("number of words that rhyme with each other = {} and count of those is {}", sortedKey, lengthCountMap.get(sortedKey));
        }
    }

    @VisibleForTesting
    protected String removeWordCounter(String originalWord) {
        String[] split = originalWord.split("\\(\\d\\)");
        return split[0];
    }

    private void populatePhonemes(InputStream phonemesStream) throws IOException {

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(phonemesStream))) {
            String line;
            // A line looks like
            // AE	vowel
            // TH	fricative
            // and we only want the vowels
            while ((line = reader.readLine()) != null) {
                if (!line.startsWith(cmuDictComment)) {
                    String[] split = line.split("\\s+"); // break on whitespace
                    // ["AE", "vowel"]
                    String phonemeBase = split[0];
                    String phonemeType = split[1];
                    if (phonemeType.equals("vowel")) {
                        vowels.add(phonemeBase);
                    }
                }
            }
        }

        loggie.info("vowels: {}", vowels.toString());
    }

}
