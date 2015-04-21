package com.github.megallo.markoverator;

import com.github.rholder.nlp.tagging.FastTag;
import com.github.rholder.nlp.tagging.Lexicon;

import java.util.List;

/**
 * Takes a sentence and returns a halfway decent guess
 * about the POS tags for each token.
 */
public class PartOfSpeechUtils {

    String lexiconPath = "/com/github/rholder/nlp/tagging/fast-tag-lexicon.txt";
    FastTag fastTag = new FastTag(Lexicon.fromClasspath(lexiconPath));

    public List<String> tagSentence(List<String> sentence) {

//        List<String> words = Tokenizer.wordsToList("The pig flew quickly around the yard.");
        return fastTag.tag(sentence);
    }
}
