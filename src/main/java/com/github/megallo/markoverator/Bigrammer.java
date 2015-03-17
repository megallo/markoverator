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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Stack;

/**
 * Build a bigram markov model out of sentences for random text generation
 */
public class Bigrammer {

    private Random random = new Random();

    private final static int MAX_HALF_LENGTH = 8; // TODO configurable
    private final static String BOM = "<BOM>"; // TODO since these are always together they could just be one delimiter, derp
    private final static String EOM = "<EOM>";

    private List<String> fullWordList;
    private Map<String, List<Integer>> wordIndexMap;
    private HashMap<Pair, List<String>> forwardCache = new HashMap<>();
    private HashMap<Pair, List<String>> backwardCache = new HashMap<>();


    public String generateRandom() {
        int seed;
        do {
            seed = random.nextInt(fullWordList.size() - 3);
            // keep trying until we get a reasonable starting point
        } while (fullWordList.get(seed).equals(EOM) || fullWordList.get(seed).equals(BOM)
                || fullWordList.get(seed+1).equals(EOM) || fullWordList.get(seed+1).equals(BOM));

        return generateRandom(seed);
    }

    public String generateRandom(String seedWord) {
        List<Integer> allPossibleLocations;
        if (wordIndexMap.containsKey(seedWord)) {
            allPossibleLocations = wordIndexMap.get(seedWord);
            return generateRandom(allPossibleLocations.get(random.nextInt(allPossibleLocations.size())));
            // TODO hack something in here to force removal of EOM/BOM tags? haaaax
        }

        // TODO stemming or wordnet to try harder at finding the word

        // we couldn't find that word, so I guess second best is to return *something*
        return generateRandom();
    }

    public String generateRandom(int seed) {

        String w1 = fullWordList.get(seed);
        String w2 = fullWordList.get(seed+1);

        List<String> backwardText = generateBackwardText(w1, w2); // does not include seed words
        List<String> forwardText = generateForwardText(w1, w2);   // includes seed words
        backwardText.addAll(forwardText);                         // and mush

        StringBuilder sb = new StringBuilder();
        for (String word : backwardText) {
            sb.append(word).append(" ");
        }

        return sb.toString();
    }

    private List<String> generateForwardText(String word1, String word2) {
        List<String> generated = new ArrayList<>();
        while (generated.size() <= MAX_HALF_LENGTH) {
            generated.add(word1);
            List<String> nextWordOptions = forwardCache.get(new Pair(word1, word2));
            String word3 = nextWordOptions.get(random.nextInt(nextWordOptions.size()));
            // TODO end based on POS tag - make a method that checks end condition
            if (word3.equals(EOM)) {
                break;
            }
            word1 = word2;
            word2 = word3;
        }
        generated.add(word2);

        return generated;
    }

    private List<String> generateBackwardText(String word2, String word3) {
        Stack<String> generated = new Stack<>();
        while (generated.size() <= MAX_HALF_LENGTH) {
            generated.push(word3);
            List<String> prevWordOptions = backwardCache.get(new Pair(word2, word3));
            String word1 = prevWordOptions.get(random.nextInt(prevWordOptions.size()));
            // TODO end based on POS tag - make a method that checks end condition
            if (word1.equals(BOM)) {
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

    public void buildModel(List<List<String>> sentencesList) {

        fullWordList = new ArrayList<>();
        wordIndexMap = new HashMap<>();

        // add BOM and EOM to get more natural sentence starts and ends
        for (List<String> oneSentence : sentencesList) {
            fullWordList.add(BOM);
            fullWordList.addAll(oneSentence);
            fullWordList.add(EOM);
        }

        //for each triplet
        // map of (<w1, w2> -> w3) = generates forward text
        // map of (<w2, w3> -> w1) = generates backward text

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
            // BOM and EOM could be removed here if space is a concern
        }
    }

    class Pair {
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

            if (!first.equals(pair.first)) return false;
            if (!second.equals(pair.second)) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = first.hashCode();
            result = 31 * result + second.hashCode();
            return result;
        }
    }
}
