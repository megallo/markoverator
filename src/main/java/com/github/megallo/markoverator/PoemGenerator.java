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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Make poems!
 * Example usage of the Poet rhyme finder combined with Markov generation.
 **/
public class PoemGenerator {

    private static final Logger loggie = LoggerFactory.getLogger(PoemGenerator.class);

    Bigrammer bigrammer;
    Poet poet;

    PoemGenerator(String modelFile) throws FileNotFoundException {
        this.bigrammer = new Bigrammer();
        // load an existing model from a file
        // example model creation is shown in MarkovGenerator
        this.bigrammer.loadModel(new FileInputStream(new File(modelFile)));
        this.bigrammer.setMaxHalfLength(6); // short and sweet
        poet = new Poet(bigrammer);
    }

    public static void main(String[] args) throws IOException {
        if (args.length < 1) {
            loggie.error("Nope!\n\nUsage: PoemGenerator <full path to model file>\n\n");
            return;
        }

        PoemGenerator pg = new PoemGenerator(args[0]);

        pg.poet.buildThreeLinePoem("alice");
        pg.poet.buildThreeLinePoem("anxiously");
        pg.poet.buildThreeLinePoem("queen");
        pg.poet.buildThreeLinePoem("interest");
        pg.poet.buildThreeLinePoem("interest");
        pg.poet.buildThreeLinePoem("mushroom");
        pg.poet.buildThreeLinePoem("conversation");

    }

}
