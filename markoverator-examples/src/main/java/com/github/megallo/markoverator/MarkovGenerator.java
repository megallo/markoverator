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

import com.github.megallo.markoverator.bigrammer.BigramModel;
import com.github.megallo.markoverator.bigrammer.BigramModelBuilder;
import com.github.megallo.markoverator.bigrammer.Bigrammer;
import com.github.megallo.markoverator.kryo.utils.KryoModelUtils;
import com.github.megallo.markoverator.storage.MemoryBigrammerStorage;
import com.github.megallo.markoverator.utils.TextUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;


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

        loggie.info("Doing things, hang on");

        /*
        Choose your own adventure! You can either make a new model and save it for future use,
        or you can load up a model that you created previously. Use the method that's right for you!
         */

        //BigramModel model = mg.buildAndSaveModel(args[0], args[1]);
        BigramModel model = mg.loadModelFromFile(args[1]);

        Bigrammer bigrams = new Bigrammer(new MemoryBigrammerStorage(model));

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

    /**
     * Example usage of how to use BigramModelBuilder to generate a new model from input sentence file.
     * The saved model can be loaded and used later instead of rebuilding.
     * @param inputFilePath fully qualified path to a file containing sentences to build a model from, one per line
     * @param modelFilePath path or name of a file to serialize the model to
     * @return the newly created model object
     */
    public BigramModel buildAndSaveModel(String inputFilePath, String modelFilePath) {

        // load up and clean the strings that we want to build a new model from
        List<List<String>> inputSentences = this.readAndCleanFile(inputFilePath);

        // build the model
        BigramModel model = BigramModelBuilder.buildModel(inputSentences);

        // save the model to a file (not required in order to use it to generate random with the Bigrammer)
        try {
            KryoModelUtils.saveModel(model, new FileOutputStream(new File(modelFilePath)));
        } catch (FileNotFoundException e) {
            throw new RuntimeException("Couldn't write model to file " + modelFilePath, e);
        }

        return model;
    }

    /**
     * If you have previously built a model and now want to use it to generate text with the Bigrammer,
     * here is an example of how to load it up for use (as opposed to rebuilding it from the corpus).
     *
     * @param modelFilePath Fully qualified path to previously created model file
     * @return model ready for use in Bigrammer
     */
    public BigramModel loadModelFromFile(String modelFilePath) {
        BigramModel model;
        try {
            model = KryoModelUtils.loadModel(new FileInputStream(new File(modelFilePath)));
        } catch (FileNotFoundException e) {
            throw new RuntimeException("Couldn't find model file at " + modelFilePath, e);
        }

        return model;
    }

    public String postProcess(List<String> tokens) {
        return textUtils.stringify(textUtils.capitalizeInitialWord(textUtils.reattachPunctuation(tokens)));
    }

    private List<List<String>> readAndCleanFile(String filename) {
        List<List<String>> cleanedTokenizedLines = new ArrayList<>();

        BufferedReader br;
        try {
            File file = new File(filename);
            br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));

            String line;
            while ((line = br.readLine()) != null)   {
                cleanedTokenizedLines.add(textUtils.cleanUpLine(line));
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException("Unable to find file " + filename, e);
        } catch (IOException e) {
            throw new RuntimeException("We found the file but couldn't read it: " + filename, e);
        }

        return cleanedTokenizedLines;
    }

}
