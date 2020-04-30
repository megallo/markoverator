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
    public void testRemoveRedundantPunctuation() {
        String withPunc = "... , . ! Hey , , ! How's it ... going? ? (awwyiss) :) ? ?";
        LinkedList<String> split = new LinkedList<>(Arrays.asList(withPunc.split("\\s+")));

        List<String> fixedPunc = new LinkedList<>();
        fixedPunc.add("!");
        fixedPunc.add("Hey");
        // no commas
        fixedPunc.add("!");
        fixedPunc.add("How's");
        fixedPunc.add("it");
        fixedPunc.add("...");
        fixedPunc.add("going?");
        fixedPunc.add("?"); // not removed because no standalone punc around it
        fixedPunc.add("(awwyiss)");
        fixedPunc.add(":)");
        fixedPunc.add("?");

        Assert.assertEquals(fixedPunc, textUtils.removeRedundantPunctuation(split));

    }

    @Test
    public void testParensAndColons() {
        String withParens = "(go see, ok?) (:whatever:) :shrug: (awthanks) :) ;) :P :-) :cake::cake:cake:";
        List<String> withoutParens = new LinkedList<>();
        withoutParens.add("go");
        withoutParens.add("see,");
        withoutParens.add("ok?");
        withoutParens.add("(:whatever:)"); // shrug, there's only so much I can do
        withoutParens.add(":shrug:");
        withoutParens.add("(awthanks)"); // single-word parentheses conversion got moved to convertHipchatToSlackEmoticons()
        withoutParens.add(":)");
        withoutParens.add(";)");
        withoutParens.add(":P");
        withoutParens.add(":-)");
        withoutParens.add(":cake::cake:cake:");

        Assert.assertEquals(withoutParens, textUtils.removeUnmatchedParenthesesAndColons(Arrays.asList(withParens.split("\\s+"))));
    }

    @Test
    public void testHandlePunctuationRegex() {
        String withPunc = "Hey! How's it going? (awwyiss) :)";
        LinkedList<String> split = new LinkedList<>(Arrays.asList(withPunc.split("\\s+")));

        List<String> fixedPunc = new LinkedList<>();
        fixedPunc.add("Hey!");
        fixedPunc.add("How's");
        fixedPunc.add("it");
        fixedPunc.add("going?");
        fixedPunc.add("(awwyiss)");
        fixedPunc.add(":)");

        Assert.assertEquals(fixedPunc, textUtils.handlePunctuation(split));

    }

    @Test
    public void testRemoveUgly() {
        String withPunc = "( Hey! ) 'How's : this (awwyiss) :)";
        LinkedList<String> split = new LinkedList<>(Arrays.asList(withPunc.split("\\s+")));

        List<String> fixedPunc = new LinkedList<>();
        fixedPunc.add("Hey!");
        fixedPunc.add("How's");
        fixedPunc.add("this");
        fixedPunc.add("(awwyiss)");
        fixedPunc.add(":)");

        Assert.assertEquals(fixedPunc, textUtils.handlePunctuation(split));

    }

    @Test
    public void testHandlePunctuationMultiples() {
        String withPunc = "Hey!!! Wat??";
        LinkedList<String> split = new LinkedList<>(Arrays.asList(withPunc.split("\\s+")));

        List<String> fixedPunc = new LinkedList<>();
        fixedPunc.add("Hey!!!");
        fixedPunc.add("Wat??");

        Assert.assertEquals(fixedPunc, textUtils.handlePunctuation(split));

    }

    @Test
    public void testSpaceOutPunctuation() {
        String withPunc = "Hey!!! Wat??";
        LinkedList<String> split = new LinkedList<>(Arrays.asList(withPunc.split("\\s+")));

        List<String> fixedPunc = new LinkedList<>();
        fixedPunc.add("Hey");
        fixedPunc.add("!!!");
        fixedPunc.add("Wat");
        fixedPunc.add("??");

        Assert.assertEquals(fixedPunc, textUtils.spaceOutPunctuation(split));

    }

    @Test
    public void testHandlePunctuationRemoval() {
        String orig = "\"what?\" 'hey nope, (disappear) ' 'whatevs :facepalm:,";
        LinkedList<String> origSplit = new LinkedList<>(Arrays.asList(orig.split("\\s+")));

        List<String> fixedPunc = new LinkedList<>();
        fixedPunc.add("what?");
        fixedPunc.add("hey");
        fixedPunc.add("nope,");
        fixedPunc.add("(disappear)");
        fixedPunc.add("whatevs");
        fixedPunc.add(":facepalm:,");

        Assert.assertEquals(fixedPunc, textUtils.handlePunctuation(origSplit));
    }

    @Test
    public void testHipchatToSlackEmoticons() {
        String orig = "(sure) \"now?\" 'hey wat, (ok ok) (disappear) ' 'whatevs :facepalm:,";
        LinkedList<String> origSplit = new LinkedList<>(Arrays.asList(orig.split("\\s+")));

        List<String> fixedPunc = new LinkedList<>();
        fixedPunc.add(":sure:");
        fixedPunc.add("\"now?\"");
        fixedPunc.add("'hey");
        fixedPunc.add("wat,");
        fixedPunc.add("(ok");
        fixedPunc.add("ok)");
        fixedPunc.add(":disappear:");
        fixedPunc.add("'");
        fixedPunc.add("'whatevs");
        fixedPunc.add(":facepalm:,");

        Assert.assertEquals(fixedPunc, textUtils.convertHipchatToSlackEmoticons(origSplit));
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
    public void testRemoveSlackMentions() {
        String orig = "Happy Birthday, <@U111111>!!";
        LinkedList<String> origSplit = new LinkedList<>(Arrays.asList(orig.split("\\s+")));

        List<String> fixedPunc = new LinkedList<>();
        fixedPunc.add("Happy");
        fixedPunc.add("Birthday,");
        fixedPunc.add("!!");

        Assert.assertEquals(fixedPunc, textUtils.removeSlackMentions(origSplit));
    }

    @Test
    public void testRemoveSlackMentionsSimple() {
        String orig = "hi <@U111111>";
        LinkedList<String> origSplit = new LinkedList<>(Arrays.asList(orig.split("\\s+")));

        List<String> fixedPunc = new LinkedList<>();
        fixedPunc.add("hi");
        fixedPunc.add("");

        Assert.assertEquals(fixedPunc, textUtils.removeSlackMentions(origSplit));
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

    @Test
    public void testFullSuite() {
        String orig = "(just looking at work currently in progress)";
        String expected = "just looking at work currently in progress";
        Assert.assertEquals(expected, reString(textUtils.cleanUpLine(orig)).trim());

        orig = "Tell me if I'm reading this right. It looks like it's listing Gray's 5-day peak at 100%, but the red horizontal line is Purple's, right?";
        expected = "tell me if I'm reading this right . it looks like it's listing gray's 5-day peak at 100% , but the red horizontal line is purple's , right ?";
        Assert.assertEquals(expected, reString(textUtils.cleanUpLine(orig)).trim());

        orig = ":thumbsup:";
        expected = ":thumbsup:";
        Assert.assertEquals(expected, reString(textUtils.cleanUpLine(orig)).trim());

        orig = "Thank you! Makes much more sense";
        expected = "thank you ! makes much more sense";
        Assert.assertEquals(expected, reString(textUtils.cleanUpLine(orig)).trim());

        orig = ":sparkles: :party:";
        expected = ":sparkles: :party:";
        Assert.assertEquals(expected, reString(textUtils.cleanUpLine(orig)).trim());

        orig = "I hope it stays that way :crossedfingers:";
        expected = "I hope it stays that way :crossedfingers:";
        Assert.assertEquals(expected, reString(textUtils.cleanUpLine(orig)).trim());

        orig = "idk if you remember :)";
        expected = "idk if you remember :)";
        Assert.assertEquals(expected, reString(textUtils.cleanUpLine(orig)).trim());

        orig = "^ Ruh roh. What'd 5 do wrong?";
        expected = "ruh roh . what'd 5 do wrong ?";
        Assert.assertEquals(expected, reString(textUtils.cleanUpLine(orig)).trim());

        orig = "I mean, we did just update 11...";
        expected = "I mean , we did just update 11";
        Assert.assertEquals(expected, reString(textUtils.cleanUpLine(orig)).trim());

        orig = "it means we gotta get good at stuff (:";
        expected = "it means we gotta get good at stuff (:";
        Assert.assertEquals(expected, reString(textUtils.cleanUpLine(orig)).trim());

        orig = "bowser, as identified by its \"little head + big shell\" was what that drawing is all about";
        expected = "bowser , as identified by its little head + big shell was what that drawing is all about";
        Assert.assertEquals(expected, reString(textUtils.cleanUpLine(orig)).trim());

        orig = "Surely. Will link rn!";
        expected = "surely . will link rn !";
        Assert.assertEquals(expected, reString(textUtils.cleanUpLine(orig)).trim());

        orig = "i forgot that part, thanks for that addition :pray::skin-tone-2:";
        expected = "i forgot that part , thanks for that addition :pray::skin-tone-2:";
        Assert.assertEquals(expected, reString(textUtils.cleanUpLine(orig)).trim());

        orig = "googling: whatever";
        expected = "googling whatever";
        Assert.assertEquals(expected, reString(textUtils.cleanUpLine(orig)).trim());

        orig = "secrets:::";
        expected = "secrets";
        Assert.assertEquals(expected, reString(textUtils.cleanUpLine(orig)).trim());

        orig = "It’s on special now ($1/mo) for anyone who wants to try: ";
        expected = "it’s on special now ($1/mo) for anyone who wants to try";
        Assert.assertEquals(expected, reString(textUtils.cleanUpLine(orig)).trim());

        orig = "this ^^^^^^^^^^^^";
        expected = "this";
        Assert.assertEquals(expected, reString(textUtils.cleanUpLine(orig)).trim());

        orig = "Happy Birthday, <@U111111>!! :palmtree: Hope it's a great one!";
        expected = "happy birthday !! :palmtree: hope it's a great one !";
        Assert.assertEquals(expected, reString(textUtils.cleanUpLine(orig)).trim());

        orig = "<!here> what's up?";
        expected = "what's up ?";
        Assert.assertEquals(expected, reString(textUtils.cleanUpLine(orig)).trim());

        orig = "https://twitter.com/wtf_/status/675378565676830720";
        expected = "";
        Assert.assertEquals(expected, reString(textUtils.cleanUpLine(orig)).trim());

        orig = "http://what.org/wtf_/status/675378565676830720";
        expected = "";
        Assert.assertEquals(expected, reString(textUtils.cleanUpLine(orig)).trim());

        orig = "¯\\_(ツ)_/¯";
        expected = "¯\\_(ツ)_/¯";
        Assert.assertEquals(expected, reString(textUtils.cleanUpLine(orig)).trim());

        orig = "\"we're kissed by fire, just like you.\"";
        expected = "we're kissed by fire , just like you .";
        Assert.assertEquals(expected, reString(textUtils.cleanUpLine(orig)).trim());

        orig = "\"hi!\"";
        expected = "hi !";
        Assert.assertEquals(expected, reString(textUtils.cleanUpLine(orig)).trim());

        orig = "(partytrain)";
        expected = "(partytrain)";
        Assert.assertEquals(expected, reString(textUtils.cleanUpLine(orig)).trim());

        orig = " \uD83D\uDE09";
        expected = "\uD83D\uDE09";
        Assert.assertEquals(expected, reString(textUtils.cleanUpLine(orig)).trim());

        orig = "awww";
        expected = "awww";
        Assert.assertEquals(expected, reString(textUtils.cleanUpLine(orig)).trim());

        orig = "";
        expected = "";
        Assert.assertEquals(expected, reString(textUtils.cleanUpLine(orig)).trim());
    }

    private String reString(List<String> tokens) {
        StringBuilder sb = new StringBuilder();
        for (String word : tokens) {
            sb.append(word).append(" ");
        }
        return sb.toString();
    }
    
}
