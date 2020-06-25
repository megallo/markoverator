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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

/**
 * Make poems!
 * Example usage of the Poet rhyme finder combined with Markov generation.
 **/
public class PoemGenerator {

    private static final Logger loggie = LoggerFactory.getLogger(PoemGenerator.class);

    static TextUtils textUtils = new TextUtils(); // TODO
    Bigrammer bigrammer;
    Poet poet;

    PoemGenerator(String modelFile) throws FileNotFoundException {
        this.bigrammer = new Bigrammer();
        // load an existing model from a file
        // example model creation is shown in MarkovGenerator
        this.bigrammer.loadModel(new FileInputStream(new File(modelFile)));
        this.bigrammer.setMaxHalfLength(6); // short and sweet
        poet = new Poet();
    }

    // TODO I think I want a helper class that accepts a model, poem length, and line max length
    // to do all this for you
    public static void main(String[] args) throws IOException {
        if (args.length < 1) {
            loggie.error("Nope!\n\nUsage: PoemGenerator <full path to model file>\n\n");
            return;
        }

        PoemGenerator pg = new PoemGenerator(args[0]);

        pg.buildThreeLinePoem("alice");
        pg.buildThreeLinePoem("anxiously");
        pg.buildThreeLinePoem("queen");
        pg.buildThreeLinePoem("interest");
        pg.buildThreeLinePoem("interest");
        pg.buildThreeLinePoem("mushroom");
        pg.buildThreeLinePoem("conversation");

    }

    public void buildThreeLinePoem(String targetWord) {
        // first, make sure our target word is in the model. Otherwise, how do we even know what we're talking about?
        String poemTopicWord = targetWord.toLowerCase();
        String topicPoemLine; // bonus: keep the line and use it in the poem
        if ((topicPoemLine = makePoemLine(bigrammer, poemTopicWord)) == null) {
            loggie.info("I don't know about {} :/", poemTopicWord);
            return;
        }

        loggie.info("Looking up words that rhyme with {}", poemTopicWord);
        List<String> rhymingWords = poet.findRhymingWords(poemTopicWord); // returns a list including the target word, if we know how to rhyme it

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

    private String makePoemLine(Bigrammer bigrammer, String word) {
        List<String> tokens;
        if ((tokens = bigrammer.generateRandomBackwards(word)) != null) {
            // we found a word that is in the model
            return textUtils.stringify(textUtils.capitalizeInitialWord(textUtils.reattachPunctuation(tokens)));
        }
        return null;
    }
}
