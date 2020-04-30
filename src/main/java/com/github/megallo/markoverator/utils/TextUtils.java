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

import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;

/**
 * Stuff for cleaning and sanitizing Slack and HipChat text.
 */
public class TextUtils {

    private  final Logger loggie = LoggerFactory.getLogger(TextUtils.class);

    private static final List<String> URL_THINGS =
            Arrays.asList(".com", ".net", ".org", "www.", "http", "://");
    private static final List<String> REMOVE_THIS_PUNCTUATION =
            Arrays.asList("\"", "…", "^", "*", "“", "”", "•" , ">"); // future me: leave # because issue numbers
    private static final Pattern ENDING_PUNCTUATION_REGEX =
            Pattern.compile("[\\.!\\?,;]+$");
    private static final Pattern REATTACH_PUNCTUATION_REGEX =
            Pattern.compile("[\\.!\\?,;:]+");
    private static final String PARENTHESES_REGEX = "[()]";

    // TODO make a regex to remove logging and stack traces

    /**
     * This is probably what you want to use. It has just about everything for cleaning up a slack export
     * invoked in the right order and tested thoroughly
     * @param sentence String to clean
     * @return tokenized and cleaned string
     */
    public List<String> cleanUpLine(String sentence) {
        String[] split = sentence.split("\\s+"); // break on whitespace
        List<String> splitSentence = new LinkedList<>(Arrays.asList(split));

        splitSentence = removeSlackMentions(splitSentence);
        splitSentence = removeHereAllMentions(splitSentence); // hipchat
        splitSentence = removeAtsFromMentions(splitSentence); // hipchat

        splitSentence = lowercaseAll(splitSentence, true);
        splitSentence = removeUrls(splitSentence);


        splitSentence = removeExplicitNewlines(splitSentence);

        /*
            Punctuation in this order!
              - space out first
              - remove stuff that can't be paired back with its partner, or does not provide semantic value
              - do that again to catch doubled up punctuation like \"hi!\"
              - remove doubled/redundant punctuation usually caused by removing @mentions
              - remove parentheses and colons - these need special care because of Hipchat and Slack emoticons
         */
        splitSentence = spaceOutPunctuation(splitSentence);
        splitSentence = handlePunctuation(splitSentence);
        splitSentence = spaceOutPunctuation(splitSentence);
        splitSentence = handlePunctuation(splitSentence);
        // remove redundant AFTER space out, otherwise we won't find it
        splitSentence = removeRedundantPunctuation(splitSentence);
        // remove parentheses after other punctuation
        splitSentence = removeUnmatchedParenthesesAndColons(splitSentence);
        // if you have hipchat logs, you may be interested in convertHipchatToSlackEmoticons()
        splitSentence = removeEmptyWords(splitSentence);

        if (loggie.isDebugEnabled()) {
            StringBuilder sb = new StringBuilder();
            for (String word : splitSentence) {
                sb.append(word).append(" ");
            }
            loggie.info(sb.toString());
        }

        return splitSentence;
    }

    /**
     * Make every word lowercase, with an option to leave special snowflake "I" alone
     * @param leaveIIntact good ol' capital I, leave it as capital I
     */
    public List<String> lowercaseAll(List<String> sentence, boolean leaveIIntact) {
        ListIterator<String> it = sentence.listIterator();
        while (it.hasNext()) {
            String word = it.next();
            if (leaveIIntact) {
                if (word.equals("I") || word.equals("I'm") || word.equals("I'll") || word.equals("I've") || word.equals("I'd")) {
                    continue;
                }
            }
            it.set(word.toLowerCase());
        }
        return sentence;
    }

    /**
     * Remove anything with angle brackets. For Slack dumps this will remove
     *    user mentions <@U123456>
     *    <!here> <!channel>
     *    URLs
     */
    public List<String> removeSlackMentions(List<String> sentence) {
        ListIterator<String> it = sentence.listIterator();
        while (it.hasNext()) {
            String word = it.next();
            word = word.replaceAll("<.+>", "");
            it.set(word);
        }

        return sentence;
    }

    /**
     * Remove entire words that are reminiscent of raw URLs. Doesn't work with Slack.
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
     * Remove mentions that will ping everyone, specifically @here and @all
     * Specific to Hipchat.
     */
    public List<String> removeHereAllMentions(List<String> sentence) {
        ListIterator<String> it = sentence.listIterator();
        while (it.hasNext()) {
            String word = it.next();
            if (word.startsWith("@all") || word.startsWith("@here")) {
                it.remove();
            }
        }
        return sentence;
    }

    /**
     * Remove the @ on words starting with '@'
     * Specific to Hipchat
     */
    public List<String> removeAtsFromMentions(List<String> sentence) {
        ListIterator<String> it = sentence.listIterator();
        while (it.hasNext()) {
            String word = it.next();
            if (word.startsWith("@")) {
                it.set(word.replace("@", ""));
            }
        }
        return sentence;
    }

    /**
     * Remove words starting with '@'
     * Specific to Hipchat
     */
    public List<String> removeEveryMention(List<String> sentence) {
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
     * Remove punctuation that comes before other punctuation
     * Looking at cases where we removed the @mention and left a weird string, like
     *   Happy birthday , !!
     * but NOT
     *   happy birthday ! :cupcake:
     * and it will remove ALL repetitive punctuation up until the last one, so
     *   . . . . . wat
     * becomes
     *   . wat
     * (for better or worse)
     */
    public List<String> removeRedundantPunctuation(List<String> sentenceTokens) {

        if (sentenceTokens == null) {
            return null;
        }

        for (int i = 1; i < sentenceTokens.size(); i++) {
            // start with 1 so we don't try to look back to a previous at index 0
            String word = sentenceTokens.get(i);
            String previousWord = sentenceTokens.get(i-1);
            Matcher m = ENDING_PUNCTUATION_REGEX.matcher(word);
            Matcher m2 = ENDING_PUNCTUATION_REGEX.matcher(previousWord);
            if (m.matches() && m2.matches()) { // we only want to keep one punctuation, the last one
                sentenceTokens.remove(i-1);
                i--; // back up, you just shifted i-n down one index
            }
        }

        return sentenceTokens;

    }

    /**
     * Convert HipChat emoticons (mindblown)
     * to slack emoticons :palm_tree:
     */
    public  List<String> convertHipchatToSlackEmoticons(List<String> sentence) {
        ListIterator<String> it = sentence.listIterator();
        while (it.hasNext()) {
            String word = it.next().trim();

            // an emoticon, hooray. convert to slack
            if (word.startsWith("(") && word.endsWith(")")) {
                word = word.replaceAll(PARENTHESES_REGEX, ":");
                it.set(word);
            }
        }

        return sentence;
    }

    /**
     * Remove unmatched parentheses stuck to words, but leave
     * HipChat emoticons (mindblown)
     * and slack emoticons :cool:
     * and smiley faces :) :( (: ): ;) :P
     */
    public  List<String> removeUnmatchedParenthesesAndColons(List<String> sentence) {
        ListIterator<String> it = sentence.listIterator();
        while (it.hasNext()) {
            String word = it.next().trim();

            // assume this is just a word in parentheses
            if (word.startsWith("(") && word.endsWith(")")) {
                continue;
            }

            // a smiley face, hooray
            if (
                    word.equals(":)") ||
                    word.equals(":(") ||
                    word.equals("(:") ||
                    word.equals(";)") ||
                    word.equals(";-)") ||
                    word.equals(":'(") ||
                    word.equals(":-(") ||
                    word.equals(":-D") ||
                    word.equals(":-)") ||
                    word.equals(":D") ||
                    word.equals(":d") ||
                    word.equals(":p") ||
                    word.equals(":P"))
            {
                continue;
            }

            // part of a parenthetical phrase, noooo
            if (word.startsWith("(") && !word.endsWith(")")) {
                // "(or"
                word = word.replace("(", "");
            }
            if (!word.startsWith("(") && word.endsWith(")")) {
                // "else)"
                word = word.replace(")", "");
            }

            // go away colons that aren't emoticons, do not want you
            if (word.startsWith(":") && !word.endsWith(":")) {
                // ":or"
                word = word.replace(":", "");
            }
            if (!word.startsWith(":") && word.endsWith(":")) {
                // "else:"
                word = word.replace(":", "");
            }

            it.set(word);
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

    /**
     * Remove punctuation that does not contribute to sentence or word meaning
     * or that will lose its buddy when markovified, like double quotes
     */
    public List<String> handlePunctuation(List<String> sentence) {
        ListIterator<String> it = sentence.listIterator();

        while (it.hasNext()) {
            String word = it.next().trim();

            // handle parentheses in the parentheses method, not here

            // leave in single quotes within words, we're ignoring those so
            // possessives and contractions don't get messed up.
            // unless it's standalone, in which case kill it with fire
            if(word.equals("'")) {
                it.remove();
                continue;
            }

            // remove colons that are all alone
            if(word.equals(":")) {
                it.remove();
                continue;
            }

            // remove parentheses that are all alone
            if(word.equals("(") || word.equals(")")) {
                it.remove();
                continue;
            }

            // remove ellipses that are all alone
            if(word.equals("...")) {
                it.remove();
                continue;
            }

            if (word.startsWith("'")) {
                word = word.substring(1);
            }

            if (word.endsWith("'")) {
                word = word.substring(0, word.length()-1);
            }

            // remove multi-word-spanning stuff like quotation marks and ellipses
            for (String punc : REMOVE_THIS_PUNCTUATION) {
                word = word.replace(punc, "");
            }

            // write the modified word back into the list
            it.set(word);

        }

        return sentence;
    }

    /**
     * When we find punctuation attached to a word, make it its own word. Internal punctuation stays put
     * e.g.
     * Howdy! -> Howdy !
     *
     * Useful when combined with reattachPunctuation() after generation
     */
    public List<String> spaceOutPunctuation(List<String> sentence) {
        ListIterator<String> it = sentence.listIterator();

        while (it.hasNext()) {
            String word = it.next().trim();

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
     * "Hey !" = "Hey!"
     * "wat ?!" = "wat?!"
     * "beep - boop" = "beep - boop"
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

    /**
     * Capitalize the first letter in the first word of the sentence
     */
    public List<String> capitalizeInitialWord(List<String> sentenceTokens) {

        if (sentenceTokens == null) {
            return null;
        }

        if (sentenceTokens.size() > 0) {
            StringBuilder initialWord = new StringBuilder(sentenceTokens.get(0));
            if (Character.isAlphabetic(initialWord.charAt(0))) {
                initialWord.setCharAt(0, Character.toUpperCase(initialWord.charAt(0)));
                sentenceTokens.set(0, initialWord.toString());
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
