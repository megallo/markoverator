package com.github.megallo.markoverator.bigrammer;

import com.github.megallo.markoverator.utils.Pair;

import java.util.HashMap;
import java.util.List;

/**
 * Serializable pojo
 **/
public class BigramModel {

    private List<String> fullWordList;
    private HashMap<Pair, List<String>> forwardCache = new HashMap<>();
    private HashMap<Pair, List<String>> backwardCache = new HashMap<>();

    public BigramModel() { }

    public BigramModel(List<String> fullWordList, HashMap<Pair, List<String>> forwardCache, HashMap<Pair, List<String>> backwardCache) {
        this.fullWordList = fullWordList;
        this.forwardCache = forwardCache;
        this.backwardCache = backwardCache;
    }

    public List<String> getFullWordList() {
        return fullWordList;
    }

    public void setFullWordList(List<String> fullWordList) {
        this.fullWordList = fullWordList;
    }

    public HashMap<Pair, List<String>> getForwardCache() {
        return forwardCache;
    }

    public void setForwardCache(HashMap<Pair, List<String>> forwardCache) {
        this.forwardCache = forwardCache;
    }

    public HashMap<Pair, List<String>> getBackwardCache() {
        return backwardCache;
    }

    public void setBackwardCache(HashMap<Pair, List<String>> backwardCache) {
        this.backwardCache = backwardCache;
    }
}
