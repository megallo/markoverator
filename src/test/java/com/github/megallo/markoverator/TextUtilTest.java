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

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class TextUtilTest {

    @Test
    public void testRemoveUrls() {
        String withUrl = "go to http://whatever.com/lol, ok?";
        List<String> withoutUrl = new LinkedList<>();
        withoutUrl.add("go");
        withoutUrl.add("to");
        withoutUrl.add("ok?");

        Assert.assertEquals(withoutUrl, TextUtils.removeUrls(new LinkedList<>(Arrays.asList(withUrl.split("\\s+")))));
    }

    @Test
    public void testRemoveMentions() {
        String withUrl = "go see @jack, ok?";
        List<String> withoutUrl = new LinkedList<>();
        withoutUrl.add("go");
        withoutUrl.add("see");
        withoutUrl.add("ok?");

        Assert.assertEquals(withoutUrl, TextUtils.removeMentions(new LinkedList<>(Arrays.asList(withUrl.split("\\s+")))));
    }

    @Test
    public void testRemoveNewlines() {
        String withUrl = "go see, ok?\\n";
        List<String> withoutUrl = new LinkedList<>();
        withoutUrl.add("go");
        withoutUrl.add("see,");
        withoutUrl.add("ok? ");

        Assert.assertEquals(withoutUrl, TextUtils.removeExplicitNewlines(new LinkedList<>(Arrays.asList(withUrl.split("\\s+")))));
    }

    @Test
    public void testRemovePunctuation() {
        String withUrl = "go see, ok? (awthanks) :)";
        List<String> withoutUrl = new LinkedList<>();
        withoutUrl.add("go");
        withoutUrl.add("see");
        withoutUrl.add("ok");
        withoutUrl.add("(awthanks)");
        withoutUrl.add(":)");

        Assert.assertEquals(withoutUrl, TextUtils.removePunctuation(new LinkedList<>(Arrays.asList(withUrl.split("\\s+")))));
    }

    @Test
    public void testRemoveParens() {
        String withUrl = "(go see, ok?) (awthanks) :)";
        List<String> withoutUrl = new LinkedList<>();
        withoutUrl.add("go");
        withoutUrl.add("see,");
        withoutUrl.add("ok?");
        withoutUrl.add("(awthanks)");
        withoutUrl.add(":)");

        Assert.assertEquals(withoutUrl, TextUtils.removeUnmatchedParentheses(new LinkedList<>(Arrays.asList(withUrl.split("\\s+")))));
    }
}
