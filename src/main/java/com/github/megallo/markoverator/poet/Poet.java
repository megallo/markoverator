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

import com.github.megallo.markoverator.PoemGenerator;
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

/**
 * Find words that rhyme using cmuDict
 **/
public class Poet {

    private static final Logger loggie = LoggerFactory.getLogger(PoemGenerator.class);

    public static final String cmuDictLocation = "/com/github/megallo/markoverator/poet/cmudict-0.7b.txt";
    public static final String cmuPhonemeLocation = "/com/github/megallo/markoverator/poet/cmudict-0.7b-phones.txt";
    public static final String cmuSymbolsLocation = "/com/github/megallo/markoverator/poet/cmudict-0.7b-symbols.txt";
    public static final String myDictLocation = "/com/github/megallo/markoverator/poet/extras-dict.txt";

    private final static String cmuDictComment = ";;;";

    Map<String, List<String>> wordPhonemes = new HashMap<>();
    Set<String> vowels = new HashSet<>();

    Map<String, List<String>> endingPhonemesWords = new HashMap<>();

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
            populateCmuMap(cmuDictStream);
            if (extraDictStream != null) {
                populateCmuMap(extraDictStream);
            }
        } catch (IOException e) {
            loggie.error("Unable to load CMU files", e);
        }
        loggie.info("Loaded rhyme dictionary; found {} words", wordPhonemes.size());

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
        if (wordPhonemes.containsKey(targetWord.toLowerCase())) {
            List<String> targetPhonemes = wordPhonemes.get(targetWord.toLowerCase()); // wordPhonemes contains numbers

            // TODO here is where you would skip the call to getRhymingSection() if you had the mashed strings already loaded into wordPhonemes
            // and you could keep the list and have "most rhyming" ordered to "least rhyming"
            // e.g. KERFUFFLE  K ER0 F AH1 F AH0 L -> ["ERFAHFAHL", "FAHFAHL", "AHFAHL", "FAHL", "AHL"]
            // be sure to use the same phoneme ending algorithm for both insertion and lookup
            String targetPhonemeMash = getRhymingSection(targetPhonemes);
            // this is the dictionary holding the rhyming last few syllables mapped to a list of things that rhyme with it
            if (endingPhonemesWords.containsKey(targetPhonemeMash)) {
                // we have things that rhyme!
                loggie.info("Found {} words that rhyme with {}", endingPhonemesWords.get(targetPhonemeMash).size(), targetWord);
                // this contains the targetWord itself, and it's up to the caller to remove it if they want to???
                return new ArrayList<>(endingPhonemesWords.get(targetPhonemeMash));
            }
        }

        return null; // we don't have things that rhyme :(
    }

    /**
     * Extract the section to rhyme with. This is probably the last few letters
     * up to and including the last vowel
     *
     * This MUST be used by both the dictionary builder and dictionary lookup, so they stay in sync with each other
     * ...UNLESS you store the precalculated phonemes in wordPhonemes map as strings. Do that
     *
     * @param phonemeList for this word
     */
    @VisibleForTesting
    String getRhymingSection(List<String> phonemeList) {
        StringBuilder rhymeMe = new StringBuilder();
        Stack<String> rhymeMeStack = new Stack<>();
        // walk the list backwards until we find the last vowel
        int i;
        for (i = phonemeList.size() - 1; i >= 0; i--) {
            String pho = phonemeList.get(i);
            rhymeMeStack.push(pho);
            if (vowels.contains(pho)) {
                // we did it
                break;
            }
        }

        // TODO I googled and this is a "single" rhyme. Make more extractions to try for double and dactylic rhymes

        if (rhymeMeStack.size() == 1) { // this means we only pulled off the last phoneme and it was a vowel
            // so just grab one more if available
            i = phonemeList.size() - 2;
            if (i >= 0) {
                rhymeMeStack.push(phonemeList.get(i));
            }
        }

        // stacks still append to the back, so it's just less annoying reverse traversal
        while (!rhymeMeStack.empty()) {
            // pop so they're in the right order
            rhymeMe.append(rhymeMeStack.pop());
        }
        return rhymeMe.toString();
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

                    // TODO do something with the words like prestigious(1). I don't care enough to look up multiple
                    // pronunciations, but I want all of them to be stored in the reverse lookup map. I think that's ok?
                    wordPhonemes.put(word, phonemes);  // TODO you could just store the rhyming section of a word instead of the whole phoneme set of the word, so you don't have to recalculate the rhyming section on lookup
                    // that makes so much sense, why I am not doing that already. it saves calculation on lookup and also storage in memory

                    String lastNPhonemes = getRhymingSection(phonemes);

                    List<String> wordsForThisPhoneme;
                    if (endingPhonemesWords.containsKey(lastNPhonemes)) {
                        wordsForThisPhoneme = endingPhonemesWords.get(lastNPhonemes);
                    } else {
                        wordsForThisPhoneme = new ArrayList<>();
                        endingPhonemesWords.put(lastNPhonemes, wordsForThisPhoneme);
                    }
                    // TODO remove punctuation from words, e.g. PRESTIGIOUS(1)
                    // TODO remove punctuation from words, e.g. "quote
                    wordsForThisPhoneme.add(removeWordCounter(word));

                }
            }
        }
    }

    /**
     * Method to log rhyme stats to see if changes to the rhyming algorithm actually did anything.
     * Heavy, should only be used for analyzing changes
     */
    private void logStats() {

        loggie.info("Count of distinct rhymes: {}", endingPhonemesWords.keySet().size());

        HashMap<Integer, Integer> lengthCountMap = new HashMap<>();
        Collection<List<String>> listsOfRhymingWords = endingPhonemesWords.values();
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
        // TODO this doesn't work
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
