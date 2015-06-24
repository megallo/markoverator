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

package com.github.megallo.markoverator.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;

/**
 * Stuff for cleaning and sanitizing HipChat text.
 */
public class TextUtils {

    private  final Logger loggie = LoggerFactory.getLogger(TextUtils.class);

    private static final List<String> URL_THINGS =
            Arrays.asList(".com", ".net", ".org", "www", "http", "://");
    private static final List<String> PUNCTUATION =
            Arrays.asList(",", ".", "?", "!", "\"", ";", "…", " ");

    // TODO handle punctuation instead of pretending it doesn't exist

    /**
     * Remove entire words that are reminiscent of URLs.
     */
    public List<String> removeUrls(List<String> sentence) {
        ListIterator<String> it = sentence.listIterator();
        while (it.hasNext()) {
            String word = it.next();
            for (String thing : URL_THINGS) {
                if (word.contains(thing)) {
                    it.remove();
                    break; // break out of the for loop so it doesn't try to remove the same list item again
                }
            }
        }
        return sentence;
    }

    /**
     * Remove words starting with '@'
     */
    public List<String> removeMentions(List<String> sentence) {
        ListIterator<String> it = sentence.listIterator();
        while (it.hasNext()) {
            String word = it.next();
            if (word.startsWith("@")) {
                it.remove();
            }
        }
        return sentence;
    }

    /**
     * Remove the actual text "\n", often common in
     * stack traces and other pasting
     */
    public List<String> removeExplicitNewlines(List<String> sentence) {
        ListIterator<String> it = sentence.listIterator();
        while (it.hasNext()) {
            String word = it.next();
            it.set(word.replace("\\n", " "));
        }
        return sentence;
    }

    /**
     * Remove selected punctuation.
     */
    public List<String> removePunctuation(List<String> sentence) {
        ListIterator<String> it = sentence.listIterator();
        while (it.hasNext()) {
            String word = it.next();
            for (String punc : PUNCTUATION) {
                word = word.replace(punc, "");
            }
            it.set(word);
        }
        return sentence;
    }


    /**
     * Remove unmatched parentheses stuck to words, but leave HipChat emoticons (mindblown)
     * and smiley faces :) :( (: ): ;) :P
     */
    public  List<String> removeUnmatchedParentheses(List<String> sentence) {
        ListIterator<String> it = sentence.listIterator();
        while (it.hasNext()) {
            String word = it.next();

            // an emoticon, hooray
            if (word.startsWith("(") && word.endsWith(")")) {
                continue;
            }

            // a smiley face, hooray
            if (word.equals(":)") || word.equals("(:") || word.equals(";)") || word.toLowerCase().equals(":p")) {
                continue;
            }

            // part of a parenthetical phrase, noooo
            if (word.startsWith("(") && !word.endsWith(")")) {
                // "(or"
                it.set(word.replace("(", ""));
            }
            if (!word.startsWith("(") && word.endsWith(")")) {
                // "else)"
                it.set(word.replace(")", ""));
            }
        }

        return sentence;
    }

    public List<String> removeBotCommands(List<String> sentence) {
        StringBuilder sb = new StringBuilder();

        for (String word : sentence) {
            sb.append(word.toLowerCase()).append(" ");
        }
        String recompiledSentence = sb.toString();
        if (recompiledSentence.contains("image me") || recompiledSentence.contains("gif me") || recompiledSentence.contains("/gif")) {
            sentence.clear();
        }

        return sentence;
    }

    public List<String> removeEmptyWords(List<String> sentence) {
        ListIterator<String> it = sentence.listIterator();
        while (it.hasNext()) {
            String word = it.next();
            if (word.length() == 0 || word.equals("'")) {
                it.remove();
            }
        }

        return sentence;
    }

}
