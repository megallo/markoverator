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

import com.github.megallo.markoverator.utils.TextUtils;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Build a bigram model out of text and generate some sentences.
 */
public class MarkovGenerator {

    private static final Logger loggie = LoggerFactory.getLogger(MarkovGenerator.class);

    private TextUtils textUtils;

    /**
     * Do all the things!
     * Usage: MarkovGenerator [full path to corpus txt file] [output model file name]
     *
     * Hint: there's a sample txt file in root dir, alice.txt
     */
    public static void main(String[] args) throws IOException {

        if (args.length != 2) {
            loggie.error("Nope!\n\nUsage: MarkovGenerator <full path to corpus txt file> <output model file name>\n\n");
            return;
        }

        MarkovGenerator mg = new MarkovGenerator();
        Bigrammer bigrams = new Bigrammer(12); // this number is maximum half-sentence length, does not affect model creation

        loggie.info("Doing things, hang on");
        bigrams.buildModel(mg.readAndCleanFile(args[0]));
        bigrams.saveModel(new FileOutputStream(new File(args[1])));
//        bigrams.loadModel(new FileInputStream(new File(args[1])));

        // generate totally random sentences
        for (int i = 0; i < 10; i++) {
            List<String> generatedTokens = bigrams.generateRandom();
            loggie.info(mg.postProcess(generatedTokens));
        }

        // generate sentences built around one or two words
        loggie.info(mg.postProcess(bigrams.generateRandom("questions")));
        loggie.info(mg.postProcess(bigrams.generateRandom("hookah")));
        loggie.info(mg.postProcess(bigrams.generateRandom("quite")));
        loggie.info(mg.postProcess(bigrams.generateRandom("blue", "caterpillar")));
        loggie.info(mg.postProcess(bigrams.generateRandom("white", "rabbit")));

        // generate sentences that starts with a given word
        loggie.info(mg.postProcess(bigrams.generateRandomForwards("alice")));
        loggie.info(mg.postProcess(bigrams.generateRandomForwards("she")));
        loggie.info(mg.postProcess(bigrams.generateRandomForwards("they")));

        // generate a poem
        // the max half size will be the actual max length. shorter is nice here
        loggie.info(mg.postProcess(bigrams.generateRandomBackwards("find")));
        loggie.info(mg.postProcess(bigrams.generateRandomBackwards("kind")));
        loggie.info(mg.postProcess(bigrams.generateRandomBackwards("mind")));
    }

    public MarkovGenerator() {
        textUtils = new TextUtils();
    }

    public String postProcess(List<String> tokens) {
        return textUtils.stringify(textUtils.capitalizeInitialWord(textUtils.reattachPunctuation(tokens)));
    }

    private List<List<String>> readAndCleanFile(String filename) throws IOException {
        List<List<String>> cleanedTokenizedLines = new ArrayList<>();

        BufferedReader br = null;
        try {
            File file = new File(filename);
            br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));

            String line;
            while ((line = br.readLine()) != null)   {
                cleanedTokenizedLines.add(cleanUpLine(line));
            }
        } catch (FileNotFoundException e) {
            loggie.error("Unable to find file " + filename, e);
        } finally {
            if (br != null) {
                br.close();
            }
        }

        return cleanedTokenizedLines;
    }

    private List<String> cleanUpLine(String sentence) {
        String[] split = sentence.split("\\s+"); // break on whitespace
        List<String> splitSentence = new LinkedList<>(Arrays.asList(split));
        splitSentence = textUtils.lowercaseAll(splitSentence, true);
//        splitSentence = textUtils.removeBotCommands(splitSentence); // I moved this to the hipchat extractor
        splitSentence = textUtils.removeUrls(splitSentence);
        splitSentence = textUtils.removeExplicitNewlines(splitSentence);
        splitSentence = textUtils.handlePunctuation(splitSentence);
        // remove parentheses after punctuation
        splitSentence = textUtils.removeUnmatchedParentheses(splitSentence);
        // order matters! get rid of punctuation first so we can find things like (@zeus
        splitSentence = textUtils.removeHereAllMentions(splitSentence);
        splitSentence = textUtils.removeAtsFromMentions(splitSentence);
        splitSentence = textUtils.removeEmptyWords(splitSentence);

        if (loggie.isDebugEnabled()) {
            StringBuilder sb = new StringBuilder();
            for (String word : splitSentence) {
                sb.append(word).append(" ");
            }
            loggie.debug(sb.toString());
        }

        return splitSentence;
    }
}
