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

package com.github.megallo.markoverator;

import com.github.megallo.markoverator.bigrammer.Bigrammer;
import com.github.megallo.markoverator.poet.Poet;
import com.github.megallo.markoverator.utils.TextUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * Make poems!
 * Example usage of the Poet rhyme finder combined with Markov generation.
 **/
public class PoemGenerator {

    private static final Logger loggie = LoggerFactory.getLogger(PoemGenerator.class);

    static TextUtils textUtils = new TextUtils(); // TODO
    Poet poet = new Poet();
    Bigrammer bigrams = new Bigrammer(7);

    public static void main(String[] args) throws IOException {
        if (args.length != 2) {
            loggie.error("Nope!\n\nUsage: PoemGenerator <full path to model file> <word to make a poem from>\n\n");
            return;
        }
        PoemGenerator pg = new PoemGenerator();
        pg.doStuff(args[1], args[0]);
    }

    public void doStuff(String targetWord, String modelLocation) throws IOException {
        loggie.info("Loading rhyme dictionary");
        poet.initialize();


        loggie.info("Loading markov model");
        // example model creation is shown in MarkovGenerator
        bigrams.loadModel(new FileInputStream(new File(modelLocation)));

        // first, make sure our target word is in the model. Otherwise, how do we even know what we're talking about?
        String poemTopicWord = targetWord.toLowerCase();
        String topicPoemLine;
        if ((topicPoemLine = makePoemLine(poemTopicWord)) == null) {
            loggie.info("I don't know about {} :/", poemTopicWord);
            return;
        }

        loggie.info("Looking up words that rhyme with {}", poemTopicWord);
        List<String> rhymingWords = poet.findRhymingWords(poemTopicWord);

        if (rhymingWords == null) {
            loggie.info("I don't know what rhymes with {} :(", poemTopicWord);
        }

        loggie.info(rhymingWords.toString());

        Collections.shuffle(rhymingWords);

        // count up to a configurable poem line count
        int lineCount = 0;

        StringBuilder poem = new StringBuilder();
        // and now we just start trying to find words in the model
        for (String rhyme : rhymingWords) {
            if (rhyme.equals(poemTopicWord)) {
                continue; // the same word doesn't rhyme with itself, that's just silly
            }
            String poemLine = makePoemLine(rhyme);
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

    private String makePoemLine(String word) {
        List<String> tokens;
        if ((tokens = bigrams.generateRandomBackwards(word)) != null) {
            // we found a word that is in the model
            return textUtils.stringify(textUtils.capitalizeInitialWord(textUtils.reattachPunctuation(tokens)));
        }
        return null;
    }
}
