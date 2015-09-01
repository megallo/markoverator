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

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.github.megallo.markoverator.utils.BigramModel;
import com.github.megallo.markoverator.utils.Pair;
import com.github.megallo.markoverator.utils.PartOfSpeechUtils;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Stack;

/**
 * Build a bigram markov model out of sentences for random text generation
 *
 * Build or load the model before attempting to generate text.
 */
public class Bigrammer {

    private static final Logger loggie = LoggerFactory.getLogger(Bigrammer.class);

    private Random random = new Random();

    private int maxHalfLength = 8;
    private final static String DELIM = "<DELIM>";

    private BigramModel model = null;
    private Map<String, List<Integer>> wordIndexMap; // calculated, so not part of the model object

    private PartOfSpeechUtils posUtil = new PartOfSpeechUtils();

    public Bigrammer() { }

    public Bigrammer(int maxHalfLength) {
        this.maxHalfLength = maxHalfLength;
    }

    /**
     * Generate a random sentence.
     */
    public List<String> generateRandom() {
        if (model == null) {
            throw new RuntimeException("No model generated or loaded");
        }

        int seed;
        do {
            seed = random.nextInt(model.getFullWordList().size() - 3);
            // keep trying until we get a reasonable starting point
        } while (model.getFullWordList().get(seed).equals(DELIM) || model.getFullWordList().get(seed).equals(DELIM));
                // TODO this addition will prevent one-word responses; do I want that?
                // || model.getFullWordList().get(seed+1).equals(EOM) || model.getFullWordList().get(seed-1).equals(BOM));

        return generateRandom(seed);
    }

    /**
     * Attempts to find the exact word you're looking for,
     * and generate a sentence based around it.
     * @return null if exact string is not found
     */
    public List<String> generateRandom(String seedWord) {
        if (model == null) {
            throw new RuntimeException("No model generated or loaded");
        }

        List<Integer> allPossibleLocations;
        if (wordIndexMap.containsKey(seedWord)) {
            allPossibleLocations = wordIndexMap.get(seedWord);
            return generateRandom(allPossibleLocations.get(random.nextInt(allPossibleLocations.size())));
        }

        // TODO stemming or wordnet to try harder at finding the word

        return null;
    }

    private List<String> generateRandom(int seed) {

        String w1 = model.getFullWordList().get(seed);
        String w2 = model.getFullWordList().get(seed + 1);

        List<String> backwardText = generateBackwardText(w1, w2); // does not include seed word
        List<String> forwardText = generateForwardText(w1, w2);   // includes seed word
        backwardText.addAll(forwardText);// and mush 'em together
        backwardText.removeAll(Lists.newArrayList(DELIM));

        // backwardText is now the entire thing

        return backwardText;
    }

    private List<String> generateForwardText(String word1, String word2) {
        List<String> generated = Lists.newArrayList(word1, word2);

        // loops until we reach a size we like, or the content reaches a good stopping point
        while (generated.size() <= maxHalfLength) {

            List<String> nextWordOptions = model.getForwardCache().get(new Pair(word1, word2));
            // choose a random possible next word based on the two given ones
            String nextWord = nextWordOptions.get(random.nextInt(nextWordOptions.size()));

            loggie.debug("Potential next word -={}=-", nextWord);

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
        while (generated.size() <= maxHalfLength) {
            generated.push(word3);
            List<String> prevWordOptions = model.getBackwardCache().get(new Pair(word2, word3));
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
        HashMap<Pair, List<String>> forwardCache = new HashMap<>();
        HashMap<Pair, List<String>> backwardCache = new HashMap<>();
        List<String> fullWordList = new ArrayList<>();

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
        // and will save space in the model serialization
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

        model = new BigramModel(fullWordList, forwardCache, backwardCache);
        calculateWordIndices();
    }

    private void calculateWordIndices() {
        wordIndexMap = new HashMap<>();

        // make a list of the indices at which a given word appears
        for (int i = 0; i < model.getFullWordList().size(); i++) {
            String word = model.getFullWordList().get(i);
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
        if (words.size() > maxHalfLength) {
            return true;
        }

        // starting partway through, figure out a good word to end on
        if ((words.size() > maxHalfLength /2 && isDecentEndingWord(words))) {
            return true;
        }

        // not ready yet, keep going
        return false;
    }

    // TODO no commas, colons, ampersands, semicolons
    protected boolean isDecentEndingWord(List<String> sentence) {
        // avoid ending with a preposition, adjective, etc
        List<String> tags = posUtil.tagSentence(sentence);

        String endTag = tags.get(tags.size() - 1);
        String endWord = sentence.get(sentence.size()-1).toLowerCase();
        if (    endTag.equals("IN") ||      // preposition
                endTag.equals("CC") ||      // conjunction
                endTag.equals("TO") ||      // literally "to"
                endTag.equals("DT") ||      // determiner
                endTag.equals("PRP$") ||    // possessive pronoun
                endWord.equals("i")   ||    // subjective pronouns
                endWord.equals("she") ||
                endWord.equals("he")  ||
                endWord.equals("we")  ||
                endWord.equals("they"))
                {
            loggie.info("Rejecting ending of :: {}", endWord);
            return false;
        }

        return true;
    }

    public void saveModel(OutputStream outputStream) {
        if (model == null) {
            throw new RuntimeException("Refusing to write empty model.");
        }
        Kryo kryo = new Kryo();
        Output output = new Output(outputStream);
        kryo.writeObject(output, model);
        output.close();

        loggie.info("Wrote model");
    }

    public void loadModel(InputStream inputStream) {
        Kryo kryo = new Kryo();
        Input input = new Input(inputStream);
        model = kryo.readObject(input, BigramModel.class);
        calculateWordIndices();
        input.close();

        loggie.info("Loaded model; found {} words", model.getFullWordList().size());
    }

}
