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

package com.github.megallo.markoverator.bigrammer;

import com.github.megallo.markoverator.annotations.VisibleForTesting;
import com.github.megallo.markoverator.utils.Lists;
import com.github.megallo.markoverator.utils.Pair;
import com.github.megallo.markoverator.utils.PartOfSpeechUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Build a bigram markov model out of sentences for random text generation
 *
 * Build or load the model before attempting to generate text.
 */
public class Bigrammer {

    private static final Logger loggie = LoggerFactory.getLogger(Bigrammer.class);

    private Random random = new Random();

    public final static int DEFAULT_MAX_HALF_LENGTH = 10;
    public final static String DELIM = "<DELIM>";

    private int maxHalfLength = DEFAULT_MAX_HALF_LENGTH;

    private Pattern BAD_BEGINNING_PUNCTUATION_REGEX = Pattern.compile("[\\.!\\?,;]+");
    private Pattern GOOD_ENDING_PUNCTUATION_REGEX = Pattern.compile("[\\.!\\?]+");

    private BigramModel model = null;
    private Map<String, List<Integer>> wordIndexMap; // calculated, so not part of the model object

    private PartOfSpeechUtils posUtil = new PartOfSpeechUtils();

    public Bigrammer() { }

    public Bigrammer(BigramModel model) {
        this.setModel(model);
    }

    @Deprecated
    // I thought this would be way more useful ¯\_(ツ)_/¯
    public Bigrammer(int maxHalfLength) {
        this.maxHalfLength = maxHalfLength;
    }

    public int getMaxHalfLength() {
        return maxHalfLength;
    }

    public void setMaxHalfLength(int maxHalfLength) {
        this.maxHalfLength = maxHalfLength;
    }

    public BigramModel getModel() {
        return model;
    }

    /**
     * Call me first!
     *
     * Use this to set the model used by Bigrammer. Call this before attempting to generate.
     *
     * This replaces Bigrammer.loadModel(), which is deprecated and scheduled for removal.
     *
     * @param model A fully deserialized model
     */
    public void setModel(BigramModel model) {
        this.model = model;
        calculateWordIndices();

        loggie.info("Loaded model; found {} words", model.getFullWordList().size());
    }

    /**
     * Generate a random sentence.
     */
    public List<String> generateRandom() {
        if (model == null) {
            throw new RuntimeException("No model generated or loaded");
        }

        int seed;
        String word1;
        String word2;
        do {
            // if you start too close to the end, you'll fall off it
            seed = random.nextInt(model.getFullWordList().size() - 3);
            word1 = model.getFullWordList().get(seed);
            word2 = model.getFullWordList().get(seed + 1);

            // keep trying until we get an optimal starting point
        } while (word1.equals(DELIM) || word2.equals(DELIM));

        return generatePhraseWithKnownPair(word1, word2);
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

        Integer chosenRandomLocation = getAnyLocationOfSeed(seedWord);

        if (chosenRandomLocation != null) {
            // now take that word plus the word immediately following it and start bigrammin'
            String wordFollowingSeed = model.getFullWordList().get(chosenRandomLocation + 1);

            return generatePhraseWithKnownPair(seedWord, wordFollowingSeed);
        }

        // TODO stemming or wordnet to try harder at finding the word

        return null;
    }

    /**
     * Attempts to find the exact word you're looking for,
     * and generate a sentence starting with that word.
     * Max half length is the actual whole length since this is the back half of a sentence
     * @return null if exact string is not found
     */
    public List<String> generateRandomForwards(String seedWord) {
        if (model == null) {
            throw new RuntimeException("No model generated or loaded");
        }

        Integer chosenRandomLocation = getAnyLocationOfSeed(seedWord);

        if (chosenRandomLocation != null) {
            // now take that word plus the word immediately following it and start bigrammin'
            String wordFollowingSeed = model.getFullWordList().get(chosenRandomLocation + 1);

            List<String> forwardText = generateForwardText(seedWord, wordFollowingSeed);
            forwardText.removeAll(Lists.newArrayList(DELIM));
            return forwardText;
        }

        // TODO stemming or wordnet to try harder at finding the word

        return null;
    }

    /**
     * If you're trying to make a poem and don't care too much about length,
     * this is the method for you
     * @param seedWord any word you want
     * @return null if that word is not in the model
     */
    public List<String> generateRandomBackwards(String seedWord) {
        return generateRandomBackwards(seedWord, 0, maxHalfLength);
    }

    /**
     * Attempts to find the exact word you're looking for,
     * and generate a sentence ending with that word.
     * Useful if you're trying to make poetry.
     * @return null if seed word is not in model, or an empty list if we found it but couldn't meet the min reqs. Try again?
     */
    public List<String> generateRandomBackwards(String seedWord, int minWordCount, int maxWordCount) {
        if (model == null) {
            throw new RuntimeException("No model generated or loaded");
        }

        Integer chosenRandomLocation = getAnyLocationOfSeed(seedWord);

        if (chosenRandomLocation != null) {
            // now take that word plus the word immediately before it and start bigrammin'
            String wordBeforeSeed = model.getFullWordList().get(chosenRandomLocation - 1);

            List<String> backwardText = generateBackwardText(wordBeforeSeed, seedWord, minWordCount, maxWordCount);

            // TODO we counted DELIM as part of the word count during generation,
            //  but now we remove it and then count again and that's not really fair

            backwardText.removeAll(Lists.newArrayList(DELIM));

            if (backwardText.size() >= minWordCount && backwardText.size() <= maxWordCount) {
                return backwardText;
            } else {
                return new ArrayList<>(); // we didn't succeed, indicate the caller could try again
            }
        }

        return null;
    }

    /**
     * Get any location of the single seed word, and may or may not have DELIM adjacent to it.
     */
    @VisibleForTesting
    Integer getAnyLocationOfSeed(String seedWord) {

        if (wordIndexMap.containsKey(seedWord)) {
            // look up every place this word occurs and pick a random location
            List<Integer> allPossibleLocations = wordIndexMap.get(seedWord);
            return allPossibleLocations.get(random.nextInt(allPossibleLocations.size()));
        }
        return null; // it's not in this model
    }

    /**
     * Attempts to find the exact two-word phrase you're looking for,
     * and generate a sentence based around it.
     * @return null if those exact words don't occur together
     */
    public List<String> generateRandom(String seedWord1, String seedWord2) {
        if (model == null) {
            throw new RuntimeException("No model generated or loaded");
        }

        if (model.getForwardCache().containsKey(new Pair(seedWord1, seedWord2))) {
            return generatePhraseWithKnownPair(seedWord1, seedWord2);
        }

        return null;
    }

    @VisibleForTesting
    List<String> generatePhraseWithKnownPair(String w1, String w2) {
        List<String> backwardText = generateBackwardText(w1, w2); // includes seed words at end
        List<String> forwardText = generateForwardText(w1, w2);   // includes seed words at beginning

        // we mucked with backwardText, so remove the seed words from forwardText
        backwardText.addAll(forwardText.subList(2, forwardText.size())); // remove seed words and mush 'em together
        // you need to remove the DELIMS here! right here! DELIM can appears in w1 or w2
        backwardText.removeAll(Lists.newArrayList(DELIM));
        return backwardText;
    }

    @VisibleForTesting
    List<String> generateForwardText(String word1, String word2) {

        if (word2.equals(DELIM)) {
            // if the starting phrase ends with DELIM, we're done
            return Lists.newArrayList(word1, word2);
        }

        List<String> generated = Lists.newArrayList(word1, word2);

        // loops until we reach a size we like, or the content reaches a good stopping point
        while (generated.size() <= maxHalfLength) {

            List<String> nextWordOptions = model.getForwardCache().get(new Pair(word1, word2));

            if (nextWordOptions == null) {
                break;
            }
            // choose a random possible next word based on the two given ones
            String nextWord = nextWordOptions.get(random.nextInt(nextWordOptions.size()));

            loggie.debug("Potential next word -={}=-", nextWord);

            // if the next word is the end, don't even bother adding it
            if (nextWord.equals(DELIM)) {
                break;
            }

            generated.add(nextWord);

            // TODO basically just make this go to DELIM
            if (checkEndCondition(generated)) { // OH SNAP this works because we check the length twice :facepalm:
                break;
            }

            word1 = word2;
            word2 = nextWord;

        }

        return generated;
    }

    /**
     * Used internally as a pass-through from generatePhraseWithKnownPair with default sizes
     */
    List<String> generateBackwardText(String word2, String word3) {
        return generateBackwardText(word2, word3, 0, maxHalfLength);
    }

    @VisibleForTesting
    List<String> generateBackwardText(String word2, String word3, int minWordCount, int maxWordCount) {
        Stack<String> generated = new Stack<>();

        // handle edge cases
        if (maxWordCount == 2 || word2.equals(DELIM)) {
            generated.push(word2);
            generated.push(word3);
            return generated;
        }

        generated.push(word3);
        generated.push(word2);

        while (generated.size() <= minWordCount || generated.size() < maxWordCount) {
            List<String> prevWordOptions = model.getBackwardCache().get(new Pair(word2, word3));
            if (prevWordOptions == null) {
                // we have exhausted our options but we didn't meet the minimum size requirement
                // but let the calling method decide if it is the right length or not
                return generated;
            }
            String word1 = prevWordOptions.get(random.nextInt(prevWordOptions.size()));

            generated.push(word1);

            if (checkBeginCondition(generated)) {
                break;
            }

            word3 = word2;
            word2 = word1;
        }

        // remove any leading punctuation from the beginning
        if (generated.peek().equals(DELIM)) {
            generated.pop();
        }

        Matcher m = BAD_BEGINNING_PUNCTUATION_REGEX.matcher(generated.peek());
        if (m.matches()) {
            generated.pop();
        }

        return Lists.reverse(generated);
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
     * Return true if this should be the beginning of the sentence.
     * @param words a sentence
     */
    private boolean checkBeginCondition(Stack<String> words) {
        // pass the min size in?

        String endWord = words.peek();

        if (endWord.equals(DELIM)) {
            return true;
        }

        // yeah this is silly, because it's going to return false regardless
        // but if we want more validation later then just be explicit
        Matcher m = BAD_BEGINNING_PUNCTUATION_REGEX.matcher(endWord);
        if (m.matches()) {
            return false;
        }

        return false;
    }

    /**
     * Return true if this should be the end of the sentence.
     * @param words a sentence
     */
    private boolean checkEndCondition(List<String> words) {
        // check length
        if (words.size() >= maxHalfLength) {
            return true;
        }

        String endWord = words.get(words.size()-1);

        if (endWord.equals(DELIM)) {
            return true;
        }

        Matcher m = GOOD_ENDING_PUNCTUATION_REGEX.matcher(endWord);
        if (m.matches()) {
            return true;
        }
        // TODO pretty sure this approach is garbage, how about just check for a delimiter or punctuation
        // starting partway through, figure out a good word to end on
        if ((words.size() > maxHalfLength/2 && isDecentEndingWord(words))) {
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
                endWord.equals("they") ||
                endWord.equals("i've") ||
                endWord.equals("it's") ||
                endWord.equals("just") ||   // I don't want to filter out all RB (adverbs), so this is stupid and I want to come up with a smarter thing
                endWord.endsWith(","))      // in reality this is its own word, but let's not assume that here
                {
            loggie.info("Rejecting ending of :: {}", endWord);
            return false;
        }

        return true;
    }
}
