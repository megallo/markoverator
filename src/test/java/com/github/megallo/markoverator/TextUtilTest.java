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

import com.github.megallo.markoverator.utils.TextUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class TextUtilTest {

    TextUtils textUtils;

    @Before
    public void setUp() throws Exception {
        textUtils = new TextUtils();
    }

    @Test
    public void testLowercase() {
        String original = "Hey I am Here";
        List<String> split = new LinkedList<>();
        split.add("hey");
        split.add("i");
        split.add("am");
        split.add("here");

        Assert.assertEquals(split, textUtils.lowercaseAll(new LinkedList<>(Arrays.asList(original.split("\\s+"))), false));

        split = new LinkedList<>();
        split.add("hey");
        split.add("I");
        split.add("am");
        split.add("here");

        Assert.assertEquals(split, textUtils.lowercaseAll(new LinkedList<>(Arrays.asList(original.split("\\s+"))), true));
    }

    @Test
    public void testRemoveUrls() {
        String withUrl = "go to http://whatever.com/lol, ok?";
        List<String> withoutUrl = new LinkedList<>();
        withoutUrl.add("go");
        withoutUrl.add("to");
        withoutUrl.add("ok?");

        Assert.assertEquals(withoutUrl, textUtils.removeUrls(new LinkedList<>(Arrays.asList(withUrl.split("\\s+")))));
    }

    @Test
    public void testRemoveMentions() {
        String original = "go see @jack, ok?";
        List<String> split = new LinkedList<>();
        split.add("go");
        split.add("see");
        split.add("ok?");

        Assert.assertEquals(split, textUtils.removeEveryMention(new LinkedList<>(Arrays.asList(original.split("\\s+")))));
    }

    @Test
    public void testRemoveHereAllMentions() {
        String original = "@here go see @jack, ok?";
        List<String> split = new LinkedList<>();
        split.add("go");
        split.add("see");
        split.add("@jack,");
        split.add("ok?");

        Assert.assertEquals(split, textUtils.removeHereAllMentions(new LinkedList<>(Arrays.asList(original.split("\\s+")))));

    }

    @Test
    public void testRemoveNewlines() {
        String original = "go see, ok?\\n";
        List<String> split = new LinkedList<>();
        split.add("go");
        split.add("see,");
        split.add("ok? ");

        Assert.assertEquals(split, textUtils.removeExplicitNewlines(new LinkedList<>(Arrays.asList(original.split("\\s+")))));
    }

    @Test
    public void testRemovePunctuation() {
        String orig = "go see, ok? (awthanks) :)";
        LinkedList<String> origSplit = new LinkedList<>(Arrays.asList(orig.split("\\s+")));
        List<String> withoutPunc = new LinkedList<>();
        withoutPunc.add("go");
        withoutPunc.add("see");
        withoutPunc.add("ok");
        withoutPunc.add("(awthanks)");
        withoutPunc.add(":)");

        Assert.assertEquals(withoutPunc,
                textUtils.removePunctuation(origSplit, Arrays.asList(",", "?")));
    }

    @Test
    public void testRemoveParens() {
        String withParens = "(go see, ok?) (awthanks) :) ;) :P :-)";
        List<String> withoutParens = new LinkedList<>();
        withoutParens.add("go");
        withoutParens.add("see,");
        withoutParens.add("ok?");
        withoutParens.add("(awthanks)");
        withoutParens.add(":)");
        withoutParens.add(";)");
        withoutParens.add(":P");
        withoutParens.add(":-)");

        Assert.assertEquals(withoutParens, textUtils.removeUnmatchedParentheses(Arrays.asList(withParens.split("\\s+"))));
    }

    @Test
    public void testHandlePunctuationRegex() {
        String withPunc = "Hey! How's it going? (awwyiss) :)";
        LinkedList<String> split = new LinkedList<>(Arrays.asList(withPunc.split("\\s+")));

        List<String> fixedPunc = new LinkedList<>();
        fixedPunc.add("Hey");
        fixedPunc.add("!");
        fixedPunc.add("How's");
        fixedPunc.add("it");
        fixedPunc.add("going");
        fixedPunc.add("?");
        fixedPunc.add("(awwyiss)");
        fixedPunc.add(":)");

        Assert.assertEquals(fixedPunc, textUtils.handlePunctuation(split));

    }

    @Test
    public void testHandlePunctuationMultiples() {
        String withPunc = "Hey!!! Wat??";
        LinkedList<String> split = new LinkedList<>(Arrays.asList(withPunc.split("\\s+")));

        List<String> fixedPunc = new LinkedList<>();
        fixedPunc.add("Hey");
        fixedPunc.add("!!!");
        fixedPunc.add("Wat");
        fixedPunc.add("??");

        Assert.assertEquals(fixedPunc, textUtils.handlePunctuation(split));

    }

    @Test
    public void testHandlePunctuationRemoval() {
        String orig = "\"what?\" 'hey nope, (disappear) ' 'whatevs (facepalm),";
        LinkedList<String> origSplit = new LinkedList<>(Arrays.asList(orig.split("\\s+")));

        List<String> fixedPunc = new LinkedList<>();
        fixedPunc.add("what");
        fixedPunc.add("?");
        fixedPunc.add("hey");
        fixedPunc.add("nope");
        fixedPunc.add(",");
        fixedPunc.add("(disappear)");
        fixedPunc.add("whatevs");
        fixedPunc.add("(facepalm)");
        fixedPunc.add(",");

        Assert.assertEquals(fixedPunc, textUtils.handlePunctuation(origSplit));
    }

    @Test
    public void testHandlePunctuationByLeavingItAlone() {
        String orig = "? bwah ?";
        LinkedList<String> origSplit = new LinkedList<>(Arrays.asList(orig.split("\\s+")));

        List<String> fixedPunc = new LinkedList<>();
        fixedPunc.add("?");
        fixedPunc.add("bwah");
        fixedPunc.add("?");

        Assert.assertEquals(fixedPunc, textUtils.handlePunctuation(origSplit));
    }

    @Test
    public void testReattachPunctuation() {

        String orig = "Hey , how about tomorrow ?! dogs & cats";
        LinkedList<String> origSplit = new LinkedList<>(Arrays.asList(orig.split("\\s+")));

        List<String> fixedPunc = new LinkedList<>();
        fixedPunc.add("Hey,");
        fixedPunc.add("how");
        fixedPunc.add("about");
        fixedPunc.add("tomorrow?!");
        fixedPunc.add("dogs");
        fixedPunc.add("&");
        fixedPunc.add("cats");

        Assert.assertEquals(fixedPunc, textUtils.reattachPunctuation(origSplit));
    }

    @Test
    public void testReattachPunctuationNotNPE() {

        LinkedList<String> origSplit = null;

        List<String> fixedPunc = null;

        Assert.assertEquals(fixedPunc, textUtils.reattachPunctuation(origSplit));
    }

    @Test
    public void testInitCaps() {
        String lower = "hey what";
        List<String> upper = new LinkedList<>();
        upper.add("Hey");
        upper.add("what");

        Assert.assertEquals(upper, textUtils.capitalizeInitialWord(new LinkedList<>(Arrays.asList(lower.split("\\s+")))));

    }

    @Test
    public void testRemoveAts() {
        String orig = "hey @boop what is @foop doing";
        List<String> fixed = new LinkedList<>();
        fixed.add("hey");
        fixed.add("boop");
        fixed.add("what");
        fixed.add("is");
        fixed.add("foop");
        fixed.add("doing");

        Assert.assertEquals(fixed, textUtils.removeAtsFromMentions(new LinkedList<>(Arrays.asList(orig.split("\\s+")))));

    }
}
