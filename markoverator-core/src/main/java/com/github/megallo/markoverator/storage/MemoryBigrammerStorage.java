package com.github.megallo.markoverator.storage;

import com.github.megallo.markoverator.bigrammer.BigramModel;
import com.github.megallo.markoverator.utils.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MemoryBigrammerStorage implements BigrammerStorage {

    private final Map<String, List<Integer>> wordIndexMap; // calculated, so not part of the model object
    private final BigramModel model;

    public MemoryBigrammerStorage(BigramModel model) {
        this.model = model;
        this.wordIndexMap = calculateWordIndices();
    }

    @Override
    public int getFullWordListSize() {
        return model.getFullWordList().size();
    }

    @Override
    public String getByIndex(int index) {
        return model.getFullWordList().get(index);
    }

    @Override
    public List<Integer> getAllPossibleLocations(String word) {
        return wordIndexMap.get(word);
    }

    @Override
    public boolean containsForwardWordList(Pair key) {
        return model.getForwardCache().containsKey(key);
    }

    @Override
    public List<String> getForwardWordList(Pair key) {
        return model.getForwardCache().get(key);
    }

    @Override
    public List<String> getBackwardWordList(Pair key) {
        return model.getBackwardCache().get(key);
    }

    private Map<String, List<Integer>> calculateWordIndices() {
        Map<String, List<Integer>> wordIndexMap = new HashMap<>();

        // make a list of the indices at which a given word appears
        for (int i = 0; i < model.getFullWordList().size(); i++) {
            String word = model.getFullWordList().get(i);
            if (!wordIndexMap.containsKey(word)) {
                wordIndexMap.put(word, new ArrayList<>());
            }
            wordIndexMap.get(word).add(i);
            // DELIM could be removed here if space is a concern
        }

        return wordIndexMap;
    }
}
