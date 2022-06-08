package com.github.megallo.markoverator.bigrammer;

import com.github.megallo.markoverator.utils.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.github.megallo.markoverator.bigrammer.Bigrammer.DELIM;

public class BigramModelBuilder {

    /**
     * Call me first!
     * Initialize this object with a model based on the provided sentences.
     *
     * @param sentencesList a list of sentences: each sentence is pre-tokenized, usually into words
     */
    public static BigramModel buildModel(List<List<String>> sentencesList) {
        HashMap<Pair, List<String>> forwardCache = new HashMap<>();
        HashMap<Pair, List<String>> backwardCache = new HashMap<>();
        List<String> fullWordList = new ArrayList<>();

        // add sentence delimiters to get more natural sentence starts and ends
        for (List<String> oneSentence : sentencesList) {
            fullWordList.add(DELIM);
            fullWordList.addAll(oneSentence);
        }
        fullWordList.add(DELIM); // don't forget the one at the end

        // for each triplet
        //   map of (<w1, w2> -> w3) = generates forward text
        //   map of (<w2, w3> -> w1) = generates backward text

        for (int i = 0; i < fullWordList.size() - 2; i++) {
            String w1 = fullWordList.get(i);
            String w2 = fullWordList.get(i + 1);
            String w3 = fullWordList.get(i + 2);

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

        return new BigramModel(fullWordList, forwardCache, backwardCache);
    }
}
