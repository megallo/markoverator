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

import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;

/**
 * Stuff for cleaning and sanitizing text.
 */
public class TextUtils {

    private static final List<String> URL_THINGS = 
        Arrays.asList(".com", ".net", ".org", "www", "http", "://");
    private static final List<String> PUNCTUATION =
        Arrays.asList(",", ".", "?", "!", "\"", ";");

    /**
     * Remove entire words that are reminiscent of URLs.
     */
    public static List<String> removeUrls(List<String> sentence) {
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
    public static List<String> removeMentions(List<String> sentence) {
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
    public static List<String> removeExplicitNewlines(List<String> sentence) {
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
    public static List<String> removePunctuation(List<String> sentence) {
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
     * and smiley faces :) :( (: ):
     */
    public static List<String> removeUnmatchedParentheses(List<String> sentence) {
        ListIterator<String> it = sentence.listIterator();
        while (it.hasNext()) {
            String word = it.next();
            // smilies are a-ok
            if (word.equals(":)") || word.equals("(:")) { // TODO see if ;) is causing the blank words
                continue;
            }
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
}
