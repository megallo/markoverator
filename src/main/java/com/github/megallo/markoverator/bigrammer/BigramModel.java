package com.github.megallo.markoverator.bigrammer;

import com.github.megallo.markoverator.utils.Pair;

import java.util.HashMap;
import java.util.List;

/**
 * Serializable pojo
 **/
public class BigramModel {

    private List<String> fullWordList;
    private List<List<String>> fullPhraseList;
    private HashMap<Pair, List<String>> forwardCache = new HashMap<>();
    private HashMap<Pair, List<String>> backwardCache = new HashMap<>();

    public BigramModel() { }

    public BigramModel(List<String> fullWordList, List<List<String>> fullPhraseList, HashMap<Pair, List<String>> forwardCache, HashMap<Pair, List<String>> backwardCache) {
        this.fullWordList = fullWordList;
        this.fullPhraseList = fullPhraseList;
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

    public List<List<String>> getFullPhraseList() {
        return fullPhraseList;
    }

    public void setFullPhraseList(List<List<String>> fullPhraseList) {
        this.fullPhraseList = fullPhraseList;
    }
}
