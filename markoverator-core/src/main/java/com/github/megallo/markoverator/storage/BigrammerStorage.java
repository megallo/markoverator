package com.github.megallo.markoverator.storage;

import com.github.megallo.markoverator.utils.Pair;

import java.util.List;

public interface BigrammerStorage {
    /*
    // TODO replacing calls in Bigrammer is workable but need further refactoring for more compact storage
    - model.getFullWordList().size()
    - model.getFullWordList().get(seed + 1);
    - model.getForwardCache().containsKey(new Pair(seedWord1, seedWord2))
    - model.getForwardCache().get(new Pair(word1, word2));
    - model.getBackwardCache().get(new Pair(word2, word3));
     */

    /**
     * Return the size of the full word list. Each corpus word maps to an
     * integer from 0 to (size - 1).
     */
    int getFullWordListSize();

    /**
     * Return the word from the original corpus that corresponds to the given
     * index from 0 to the value returned by getFullWordListSize() - 1.
     *
     * @param index the integer index of the word
     * @return the word at the given index
     */
    String getByIndex(int index);

    /**
     * Return a list of all possible index locations for the given word or null
     * if there are 0 locations.
     *
     * @param word the word to search for possible index locations
     * @return a list of all index locations or null if there are 0
     */
    List<Integer> getAllPossibleLocations(String word);

    /**
     * Returns true if the given word pair exists in the forward chain.
     *
     * @param wordPair word pair to check
     * @return true if the pair exist, false otherwise
     */
    boolean containsForwardWordList(Pair wordPair);

    /**
     * For the given word pair in the forward chain, return all possible words
     * that might follow it. Higher probability next-words exist with higher
     * frequency in the returned list.
     *
     * @param wordPair a pair of first word followed by second word
     * @return list of all possible next words that might immediately follow in the chain
     */
    List<String> getForwardWordList(Pair wordPair);

    /**
     * For the given word pair in the backward chain, return all possible words
     * that might precede it. Higher probability previous-words exist with
     * higher frequency in the returned list.
     *
     * @param wordPair a pair of first word followed by second word
     * @return list of all possible previous words that might immediately precede in the chain
     */
    List<String> getBackwardWordList(Pair wordPair);
}
