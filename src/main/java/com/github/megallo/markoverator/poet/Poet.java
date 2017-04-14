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
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Find words that rhyme using cmuDict
 **/
public class Poet {

    private static final Logger loggie = LoggerFactory.getLogger(PoemGenerator.class);

    // TODO pass this in?
    // TODO move the resources to their own module
    private final String cmuDictLocation = "/poet/cmudict-0.7b.txt";
    private final String myDictLocation = "/poet/extras-dict.txt";
    private final String phonenemeLocation = "/poet/cmudict-0.7b-phones.txt";
    private final static String cmuDictComment = ";;;";

    Map<String, List<String>> wordPhonemes = new HashMap<>();

    // TODO make a bunch of maps populated with n last phonemes depending on word length?
    // TODO make that configurable in case memory is an issue?
    Map<String, List<String>> endingPhonemesWords = new HashMap<>();

    public void initialize() throws IOException {
        populateCmuMap(cmuDictLocation);
        populateCmuMap(myDictLocation);
        loggie.info("Loaded rhyme dictionary; found {} words", wordPhonemes.size());
    }

    public List<String> findRhymingWords(String targetWord) {
        if (wordPhonemes.containsKey(targetWord.toLowerCase())) {
            List<String> targetPhonemes = wordPhonemes.get(targetWord.toLowerCase());

            // TODO here is a case where you'd need to iterate if you have multiple length phoneme hash mashes
            if (targetPhonemes.size() >= 2) {
                String targetPhonemeMash = targetPhonemes.get(targetPhonemes.size() - 2) + targetPhonemes.get(targetPhonemes.size() - 1);
                if (endingPhonemesWords.containsKey(targetPhonemeMash)) {
                    // we have things that rhyme!
                    loggie.info("Found {} words that rhyme with {}", endingPhonemesWords.get(targetPhonemeMash).size(), targetWord);
                    return endingPhonemesWords.get(targetPhonemeMash);
                }
            } else {
                // ??? this is a one-phoneme word, now what
                // TODO
            }
        }

        return null; // we don't have things that rhyme :(
    }

    private void populateCmuMap(String filename) throws IOException {
        try(BufferedReader reader = new BufferedReader(new InputStreamReader(Poet.class.getResourceAsStream(filename)))) {
            String line;
            while((line = reader.readLine()) != null) {
                if (!line.startsWith(cmuDictComment)) {
                    String[] split = line.split("\\s+");// break on whitespace
                    String word = split[0].toLowerCase();


                    List<String> phonemes = Lists.newArrayList(split);
                    phonemes.remove(0); // TODO this is O(n) but it's only as long as the phoneme list, so 12 as a max?

                    // TODO do something with the words like prestigious(1). I don't care enough to look up multiple
                    // pronunciations, but I want all of them to be stored in the reverse lookup map. I think that's ok?
                    wordPhonemes.put(word, phonemes);

                    // TODO these are the last two phonemes, but maybe configurable? Last n?
                    String lastNPhonemes = split[split.length - 1];
                    if (split.length > 2) {
                        lastNPhonemes = split[split.length - 2] + lastNPhonemes; // just straight up smush 'em together
                    }

                    List<String> wordsForThisPhoneme;
                    if (endingPhonemesWords.containsKey(lastNPhonemes)) {
                        wordsForThisPhoneme = endingPhonemesWords.get(lastNPhonemes);
                    } else {
                        wordsForThisPhoneme = new ArrayList<>();
                        // TODO make sure this does the thing
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

    private void populatePhonemes(String filename) {
        // TODO load up all the vowels, then we can look for the last vowel in the word and try to rhyme that
    }

}
