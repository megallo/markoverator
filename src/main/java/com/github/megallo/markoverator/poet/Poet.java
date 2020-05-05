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

    // TODO make a bunch of maps populated with n last phonemes depending on word length?
    // TODO make that configurable in case memory is an issue?
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
            populatePhonemes(cmuPhonesStream, cmuSymbolsStream); // do this first to get the vowels
            populateCmuMap(cmuDictStream);
            if (extraDictStream != null) {
                populateCmuMap(extraDictStream);
            }
        } catch (IOException e) {
            loggie.error("Unable to load CMU files", e);
        }
        loggie.info("Loaded rhyme dictionary; found {} words", wordPhonemes.size());
    }

    public List<String> findRhymingWords(String targetWord) {
        if (wordPhonemes.containsKey(targetWord.toLowerCase())) {
            List<String> targetPhonemes = wordPhonemes.get(targetWord.toLowerCase());

            // be sure to use the same phoneme ending algorithm for both insertion and lookup
            String targetPhonemeMash = getRhymingSection(targetPhonemes);
            if (endingPhonemesWords.containsKey(targetPhonemeMash)) {
                // we have things that rhyme!
                loggie.info("Found {} words that rhyme with {}", endingPhonemesWords.get(targetPhonemeMash).size(), targetWord);
                return endingPhonemesWords.get(targetPhonemeMash);
            }
        }

        return null; // we don't have things that rhyme :(
    }

    /**
     * Extract the section to rhyme with. This is probably the last few letters
     * up to and including the last vowel
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
            while((line = reader.readLine()) != null) {
                if (!line.startsWith(cmuDictComment)) {
                    String[] split = line.split("\\s+");// break on whitespace
                    String word = split[0].toLowerCase();


                    List<String> phonemes = Lists.newArrayList(split);
                    phonemes.remove(0); // TODO this is O(n) but it's only as long as the phoneme list, so 16 max?

                    // TODO do something with the words like prestigious(1). I don't care enough to look up multiple
                    // pronunciations, but I want all of them to be stored in the reverse lookup map. I think that's ok?
                    wordPhonemes.put(word, phonemes);

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

    private String removeWordCounter(String originalWord) {
        // TODO this doesn't work
        String[] split = originalWord.split("\\(\\d\\)");
        return split[0];
    }

    private void populatePhonemes(InputStream phonemesStream, InputStream symbolsStream) throws IOException {
        List<String> allSymbols = new ArrayList<>();

        // read the symbols and then transfer them into the vowels set based on the phonemes input
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(symbolsStream))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.startsWith(cmuDictComment)) {
                    allSymbols.add(line.trim());
                }
            }
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(phonemesStream))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.startsWith(cmuDictComment)) {
                    String[] split = line.split("\\s+");// break on whitespace
                    String phonemeBase = split[0];
                    String phonemeType = split[1];
                    if (phonemeType.equals("vowel")) {
                        for (String symbol : allSymbols) {
                            // symbols are like AA1 and EH0 but phoneme-to-type mapping is AA EH
                            // so if the unique symbols start with a base that's marked as a vowel
                            // add it to the master vowel lookup set
                            // that way later we don't have to do a .startsWith() comparison on every word's phonemes
                            if (symbol.startsWith(phonemeBase)) {
                                vowels.add(symbol);
                            }
                        }
                    }
                }
            }
        }

        loggie.debug("vowels: {}", vowels.toString());
    }

}
