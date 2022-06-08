package com.github.megallo.markoverator.storage.h2;

import com.github.megallo.markoverator.storage.BigrammerStorage;
import com.github.megallo.markoverator.utils.Pair;
import org.h2.mvstore.MVStore;

import java.util.List;
import java.util.Map;

public class MVStoreBigrammerStorage implements BigrammerStorage {

    private final MVStore store;

    private final Map<Integer, String> fullWordList;
    private final Map<Pair, List<String>> forwardCache;
    private final Map<Pair, List<String>> backwardCache;
    private final Map<String, List<Integer>> wordIndex;

    public MVStoreBigrammerStorage(String filename) {
        this.store = new MVStore.Builder()
                .fileName(filename)
                .readOnly()
                .open();

        // all_words <counter, word>
        this.fullWordList = store.openMap("fullWordList");

        // forward_chain <word_one, word_two, word_three>
        this.forwardCache = store.openMap("forwardCache");

        // backward_chain <word_two, word_three, word_one>
        this.backwardCache = store.openMap("backwardCache");

        // word_index <word, <list of word locations>>
        this.wordIndex = store.openMap("wordIndex");
    }

    @Override
    public int getFullWordListSize() {
        return fullWordList.size();
    }

    @Override
    public String getByIndex(int index) {
        return fullWordList.get(index);
    }

    @Override
    public List<Integer> getAllPossibleLocations(String word) {
        return wordIndex.get(word);
    }

    @Override
    public boolean containsForwardWordList(Pair key) {
        return forwardCache.containsKey(key);
    }

    @Override
    public List<String> getForwardWordList(Pair key) {
        return forwardCache.get(key);
    }

    @Override
    public List<String> getBackwardWordList(Pair key) {
        return backwardCache.get(key);
    }

    public String getFileName() {
        return store.getFileStore().getFileName();
    }
}
