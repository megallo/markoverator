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

import com.github.megallo.markoverator.bigrammer.Bigrammer;
import com.github.megallo.markoverator.utils.TextUtils;
import com.google.common.annotations.VisibleForTesting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Find words that rhyme using cmuDict
 **/
public class Poet {

    private static final Logger loggie = LoggerFactory.getLogger(Poet.class);
    public final Rhymer rhymer;
    public final Bigrammer bigrammer;
    static TextUtils textUtils = new TextUtils();

    // TODO WHERE AM I?
    // poet needs a bigrammer
    // poet needs templates

    /**
     * Default constructor if you just want the CMU dictionaries
     */
    public Poet(Bigrammer bigrammer) {
        this.bigrammer = bigrammer;
        this.rhymer = new Rhymer();
    }

    /**
     * Constructor if you want to load your custom rhymes from a file
     */
    public Poet(Bigrammer bigrammer, Rhymer rhymer) {
        this.bigrammer = bigrammer;
        this.rhymer = rhymer;
    }

    /**
     * Look up all known English words that rhyme with the target word
     * Not limited by any Bigrammer user-defined model, this is all from CMU dict and extras-dict
     *
     * @param targetWord we want things that rhyme with this
     * @return list of unique words that rhyme with targetWord, including targetWord itself
     */
    public List<String> findRhymingWords(String targetWord) {
        return rhymer.findRhymingWords(targetWord);
    }


    public void buildThreeLinePoem(String targetWord) { // TODO pass in the line count and rename method
        // first, make sure our target word is in the model. Otherwise, how do we even know what we're talking about?
        String poemTopicWord = targetWord.toLowerCase();
        String topicPoemLine; // bonus: keep the line and use it in the poem
        if ((topicPoemLine = makePoemLine(bigrammer, poemTopicWord)) == null) {
            loggie.info("I don't know about {} :/", poemTopicWord);
            return;
        }

        loggie.info("Looking up words that rhyme with {}", poemTopicWord);
        List<String> rhymingWords = findRhymingWords(poemTopicWord); // returns a list including the target word, if we know how to rhyme it

        if (rhymingWords == null) {
            loggie.info("I don't know what rhymes with {} :(", poemTopicWord);
            return;
        }

        loggie.debug(rhymingWords.toString());

        rhymingWords.remove(poemTopicWord);

        // count up to a configurable poem line count
        int lineCount = 0;

        StringBuilder poem = new StringBuilder();
        // and now we just start trying to find words in the model
        for (String rhyme : rhymingWords) {
            String poemLine = makePoemLine(bigrammer, rhyme);
            if (poemLine != null) {
                poem.append(poemLine).append("\n");
                if (++lineCount >= 2) { // TODO configurable, this is the number of lines in the poem plus the last line
                    break;
                }
            }
        }

        poem.append(topicPoemLine); // artistically use the target rhyme word last, I guess

        loggie.info("I wrote this for you!\n{}", poem);
    }

    public void buildHaikuPoem(String targetWord) {
        String endingWord3 = targetWord.toLowerCase();
        List<String> endingWords = new ArrayList<>();

        endingWords.add(endingWord3);
        String line3 = makePoemLine(bigrammer, endingWord3, 5);
        if (line3 == null) {
            loggie.info("I don't know about {} :/", endingWord3);
        }

        String endingWord2 = getRandomNonRhymingWord(bigrammer, endingWords);
        endingWords.add(endingWord2);
        String line2 = makePoemLine(bigrammer, endingWord2, 7);
        if (line2 == null) {
            loggie.info("My bot brain hurts with {} - maybe ask me again?:/", endingWord3);
        }

        String endingWord1 = getRandomNonRhymingWord(bigrammer, endingWords);
        String line1 = this.makePoemLine(bigrammer, endingWord1, 5);
        if (line1 == null) {
            loggie.info("I'm having some writers block with {} - maybe ask me again?:/", endingWord3);
        }

        StringBuilder poem = new StringBuilder();
        poem.append(line1).append("\n").append(line2).append("\n").append(line3);

        loggie.info("I wrote this for you!\n{}", poem);
    }

    @VisibleForTesting
    protected String getRandomNonRhymingWord(Bigrammer bigrammer, List<String> antiRhymes) {
        List<String> rhymingWords = new ArrayList<>();

        for (String word : antiRhymes) {
            List<String> rhymes = findRhymingWords(word);
            if (rhymes != null) {
                rhymingWords.addAll(rhymes);
            }
        }

        String word  = bigrammer.getRandomWordFromModel();
        // attempt to fund a non-rhyming word, but don't try too hard
        for (int i = 0; i < 10 && rhymingWords.contains(word); i++) {
            word = bigrammer.getRandomWordFromModel();
        }

        return word;
    }

    protected String makePoemLine(Bigrammer bigrammer, String word) {
        List<String> tokens;
        if ((tokens = bigrammer.generateRandomBackwards(word)) != null) {
            // we found a word that is in the model
            return textUtils.stringify(textUtils.capitalizeInitialWord(textUtils.reattachPunctuation(tokens)));
        }
        return null;
    }

    @VisibleForTesting
    protected String makePoemLine(Bigrammer bigrammer, String word, int syllables) {
        List<String> tokens;
        if ((tokens = bigrammer.generateRandomBackwardsSyllables(word, syllables)) != null) {
            // we found a word that is in the model
            return textUtils.stringify(textUtils.capitalizeInitialWord(textUtils.reattachPunctuation(tokens)));
        }
        return null;
    }
}
