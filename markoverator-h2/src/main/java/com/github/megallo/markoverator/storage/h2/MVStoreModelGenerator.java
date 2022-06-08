package com.github.megallo.markoverator.storage.h2;

import com.github.megallo.markoverator.utils.Pair;
import org.h2.mvstore.MVStore;
import org.h2.mvstore.MVStoreTool;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import static com.github.megallo.markoverator.bigrammer.Bigrammer.DELIM;

public class MVStoreModelGenerator {

    private final MVStore store;

    // all_words <counter, word>
    private final Map<Integer, String> fullWordList;

    // forward_chain <word_one, word_two, word_three>
    private final Map<Pair, List<String>> forwardCache;

    // backward_chain <word_two, word_three, word_one>
    private final Map<Pair, List<String>> backwardCache;

    // word_index <word, <list of word locations>>
    private final Map<String, List<Integer>> wordIndex;

    private volatile boolean alreadyGenerated = false;

    public MVStoreModelGenerator(String filename) {
        // don't overwrite existing file
        if (Files.exists(Paths.get(filename))) {
            throw new RuntimeException("File already exists: " + filename);
        }

        this.store = new MVStore.Builder()
                .fileName(filename)
                .compress()
                .open();

        this.fullWordList = store.openMap("fullWordList");
        this.forwardCache = store.openMap("forwardCache");
        this.backwardCache = store.openMap("backwardCache");
        this.wordIndex = store.openMap("wordIndex");
    }

    public synchronized void generateFromSentences(List<List<String>> sentencesList) {
        // this is a one time operation
        if (alreadyGenerated) {
            throw new RuntimeException("Model has already been generated");
        }

        // add sentence delimiters to get more natural sentence starts and ends
        int counter = 0;
        for (List<String> oneSentence : sentencesList) {
            fullWordList.put(counter++, DELIM);
            for (String word : oneSentence) {
                fullWordList.put(counter++, word);
            }
        }
        fullWordList.put(counter, DELIM); // don't forget the one at the end
        store.commit();

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
                forwardCache.put(forwardPair, new CopyOnWriteArrayList<>());
            }
            forwardCache.get(forwardPair).add(w3);

            if (!backwardCache.containsKey(backwardPair)) {
                backwardCache.put(backwardPair, new CopyOnWriteArrayList<>());
            }
            backwardCache.get(backwardPair).add(w1);

            // update word index to include current word
            addToWordIndex(w1, i);

            // prevent iteration over ArrayList during serialization outside this loop
            //store.commit();
        }

        // update wordIndex for the last 2 values from the loop
        int index = fullWordList.size() - 2;
        String word = fullWordList.get(index);
        addToWordIndex(word, index);

        index = fullWordList.size() - 1;
        word = fullWordList.get(index);
        addToWordIndex(word, index);

        // close and compact the file
        store.close();
        MVStoreTool.compact(store.getFileStore().getFileName(), true);
        alreadyGenerated = true;
    }

    private void addToWordIndex(String word, int index) {
        if (!wordIndex.containsKey(word)) {
            wordIndex.put(word, new CopyOnWriteArrayList<>());
        }
        wordIndex.get(word).add(index);
    }
}
