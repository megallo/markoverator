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

import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
    private static final List<String> REMOVE_THIS_PUNCTUATION =
            Arrays.asList("\"", "…");
    private static final Pattern ENDING_PUNCTUATION_REGEX =
            Pattern.compile("[\\.!\\?,;:]+$");
    private static final Pattern REATTACH_PUNCTUATION_REGEX =
            Pattern.compile("[\\.!\\?,;:]+");

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
    public List<String> removePunctuation(List<String> sentence, List<String> punctuationToRemove) {
        ListIterator<String> it = sentence.listIterator();
        while (it.hasNext()) {
            String word = it.next();
            for (String punc : punctuationToRemove) {
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
            String word = it.next().trim();

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
            if (word.trim().length() == 0) {
                it.remove();
            }
        }

        return sentence;
    }

    public List<String> handlePunctuation(List<String> sentence) {
        ListIterator<String> it = sentence.listIterator();

        while (it.hasNext()) {
            String word = it.next().trim();

            // handle parentheses in the parentheses method, not here

            // leave in single quotes, we're ignoring those completely so
            // possessives and contractions don't get messed up.
            // unless it's standalone, in which case kill it with fire
            if(word.equals("'")) {
                it.remove();
                break;
            }

            // remove multi-word-spanning stuff like quotation marks and ellipses
            for (String punc : REMOVE_THIS_PUNCTUATION) {
                word = word.replace(punc, "");
            }
            it.set(word);

            // space out delimiter punctuation like periods and semicolons
            Matcher m = ENDING_PUNCTUATION_REGEX.matcher(word);
            if (m.find()) {
                if (m.start() == 0 && m.end() == word.length()) {
                    // if the entire word is punctuation, we're all set. leave it alone
                } else {
                    String group = m.group();

                    // remove the punctuation from where we found it
                    String replaced = word.replace(group, "");
                    it.set(replaced);

                    it.add(group); // add the found punctuation as a standalone token
                }
            }

        }

        return sentence;
    }

    /**
     * Post-processing step.
     *
     * In order to make a sentence look normally formatted,
     * take all of the detached punctuation and stick it back onto the
     * ends of the words occurring right before them.
     *
     * "Hey !" -> "Hey!"
     * "wat ?!" -> "wat?!"
     * "beep & boop" -> "beep & boop"
     *
     * @param sentenceTokens presumably a generated sentence, but you do you
     * @return the same sentence with punctuation reattached and those same
     *         punctuation tokens deleted
     */
    public List<String> reattachPunctuation(List<String> sentenceTokens) {

        if (sentenceTokens == null) {
            return null;
        }

        for (int i = 1; i < sentenceTokens.size(); i++) {
            // start with 1 so we don't try to reattach to a previous at index 0
            String word = sentenceTokens.get(i);

            Matcher m = REATTACH_PUNCTUATION_REGEX.matcher(word);
            if (m.matches()) { // we only want exactly all punctuation
                sentenceTokens.set(i-1, sentenceTokens.get(i-1) + word);
                sentenceTokens.remove(i);
            }
        }

        return sentenceTokens;
    }

    public String stringify(List<String> tokens) {
        if (tokens == null) {
            return null;
        }

        StringBuilder sb = new StringBuilder();
        for (String word : tokens) {
            sb.append(word).append(" ");
        }

        return sb.toString();
    }

}
