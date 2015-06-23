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

package com.github.megallo.markoverator;

import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Stack;

/**
 * Build a bigram markov model out of sentences for random text generation
 *
 * Build the model before attempting to generate text.
 */
public class Bigrammer {

    private static final Logger loggie = LoggerFactory.getLogger(Bigrammer.class);

    private Random random = new Random();

    private final static int MAX_HALF_LENGTH = 8; // TODO configurable
    private final static String DELIM = "<DELIM>";

    private List<String> fullWordList;
    private Map<String, List<Integer>> wordIndexMap;
    private HashMap<Pair, List<String>> forwardCache = new HashMap<>();
    private HashMap<Pair, List<String>> backwardCache = new HashMap<>();

    private PartOfSpeechUtils posUtil = new PartOfSpeechUtils();

    public String generateRandom() {
        int seed;
        do {
            seed = random.nextInt(fullWordList.size() - 3);
            // keep trying until we get a reasonable starting point
        } while (fullWordList.get(seed).equals(DELIM) || fullWordList.get(seed).equals(DELIM));
                // TODO this addition will prevent one-word responses; do I want that?
                // || fullWordList.get(seed+1).equals(EOM) || fullWordList.get(seed-1).equals(BOM));

        return generateRandom(seed);
    }

    public String generateRandom(String seedWord) {
        List<Integer> allPossibleLocations;
        if (wordIndexMap.containsKey(seedWord)) {
            allPossibleLocations = wordIndexMap.get(seedWord);
            return generateRandom(allPossibleLocations.get(random.nextInt(allPossibleLocations.size())));
        }

        // TODO stemming or wordnet to try harder at finding the word

        // we couldn't find that word, so I guess second best is to return *something*
        return generateRandom();
    }

    private String generateRandom(int seed) {

        String w1 = fullWordList.get(seed);
        String w2 = fullWordList.get(seed+1);

        List<String> backwardText = generateBackwardText(w1, w2); // does not include seed word
        List<String> forwardText = generateForwardText(w1, w2);   // includes seed word
        backwardText.addAll(forwardText);                         // and mush 'em together
        backwardText.removeAll(Lists.newArrayList(DELIM));

        StringBuilder sb = new StringBuilder();
        for (String word : backwardText) {
            sb.append(word).append(" ");
        }

        return sb.toString();
    }

    private List<String> generateForwardText(String word1, String word2) {
        List<String> generated = Lists.newArrayList(word1, word2);

        // loops until we reach a size we like, or the content reaches a good stopping point
        while (generated.size() <= MAX_HALF_LENGTH) {

            List<String> nextWordOptions = forwardCache.get(new Pair(word1, word2));
            // choose a random possible next word based on the two given ones
            String nextWord = nextWordOptions.get(random.nextInt(nextWordOptions.size()));

            // if the next word is the end, don't even bother adding it
            if (nextWord.equals(DELIM)) {
                break;
            }

            generated.add(nextWord);

            if (checkEndCondition(generated)) { // do we like it?
                break;
            }

            word1 = word2;
            word2 = nextWord;

        }

        return generated;
    }

    private List<String> generateBackwardText(String word2, String word3) {
        Stack<String> generated = new Stack<>();
        while (generated.size() <= MAX_HALF_LENGTH) {
            generated.push(word3);
            List<String> prevWordOptions = backwardCache.get(new Pair(word2, word3));
            String word1 = prevWordOptions.get(random.nextInt(prevWordOptions.size()));
            // TODO end based on POS tag - make a method that checks end condition
            if (word1.equals(DELIM)) {
                break;
            }
            word3 = word2;
            word2 = word1;
        }
        generated.push(word2);

        ArrayList<String> forwardList = new ArrayList<>();

        // don't include the original two words, as they will be in the forward text
        // it's easier to do it here because we know what the data structure is
        for (int i = 2; i < generated.size();) {
            forwardList.add(generated.pop());
        }
        return forwardList;
    }

    /**
     * Call me first!
     * Initialize this object with a model based on the provided sentences.
     * @param sentencesList a list of sentences: each sentence is pre-tokenized, usually into words
     */
    public void buildModel(List<List<String>> sentencesList) {

        fullWordList = new ArrayList<>();
        wordIndexMap = new HashMap<>();

        // add sentence delimiters to get more natural sentence starts and ends
        for (List<String> oneSentence : sentencesList) {
            if (oneSentence.size() > 2) {
                fullWordList.add(DELIM);
                fullWordList.addAll(oneSentence);
            }
        }
        fullWordList.add(DELIM); // don't forget the one at the end

        // for each triplet
        //   map of (<w1, w2> -> w3) = generates forward text
        //   map of (<w2, w3> -> w1) = generates backward text

        // TODO try out only adding a word if it's not in the list yet
        // makes it less natural statistically but probably adds more fun randomness
        for (int i = 0; i < fullWordList.size() - 2; i++) {
            String w1 = fullWordList.get(i);
            String w2 = fullWordList.get(i+1);
            String w3 = fullWordList.get(i+2);

            Pair forwardPair = new Pair(w1, w2);
            Pair backwardPair = new Pair(w2, w3);
            
            if (!forwardCache.containsKey(forwardPair)) {
                forwardCache.put(forwardPair, new ArrayList<String>());
            }
            forwardCache.get(forwardPair).add(w3);
            
            if (!backwardCache.containsKey(backwardPair)) {
                backwardCache.put(backwardPair, new ArrayList<String>());
            }
            backwardCache.get(backwardPair).add(w1);
        }

        // make a list of the indices at which a given word appears
        for (int i = 0; i < fullWordList.size(); i++) {
            String word = fullWordList.get(i);
            if (!wordIndexMap.containsKey(word)) {
                wordIndexMap.put(word, new ArrayList<Integer>());
            }
            wordIndexMap.get(word).add(i);
            // DELIM could be removed here if space is a concern
        }
    }

    /**
     * Return true if this should be the end of the sentence.
     * @param words a sentence
     */
    private boolean checkEndCondition(List<String> words) {
        // check length
        if (words.size() > MAX_HALF_LENGTH) {
            return true;
        }

        // starting partway through, figure out a good word to end on
        if ((words.size() > MAX_HALF_LENGTH/2 && isDecentEndingWord(words))) {
            return true;
        }

        // not ready yet, keep going
        return false;
    }

    private boolean isDecentEndingWord(List<String> sentence) {
        // right now just checks to see if it's an adverb
        List<String> tags = posUtil.tagSentence(sentence);

        String endTag = tags.get(tags.size() - 1);
        if (endTag.equals("RB")) {
            loggie.info("Succeeding at adverb: word is {}", sentence.get(sentence.size()-1));
            return true;
        }
        // this is duplicated for debugging only
        if (endTag.equals("NP") || endTag.equals("NNP")) {
            loggie.info("Succeeding at noun: word is {}", sentence.get(sentence.size()-1));
            return true;
        }
        return false;
    }

    private class Pair {
        String first;
        String second;

        Pair (String one, String two) {
            this.first = one;
            this.second = two;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Pair pair = (Pair) o;

            if (!first.toLowerCase().equals(pair.first.toLowerCase())) return false;
            if (!second.toLowerCase().equals(pair.second.toLowerCase())) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = first.toLowerCase().hashCode();
            result = 31 * result + second.toLowerCase().hashCode();
            return result;
        }
    }
}
