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
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class BigrammerTest {

    Bigrammer bigrammer;

    @Before
    public void setup() {
        bigrammer = new Bigrammer();
        bigrammer.buildModel(Arrays.asList(
                Arrays.asList("howdy", "y'all", ".", "How", "are", "ya", "?"),
                Arrays.asList("howdy", "pardner")
        ));
    }

    @Test
    public void testSeedWordBehavior() {
        Assert.assertNotNull(bigrammer.generateRandom("howdy"));
        Assert.assertNull(bigrammer.generateRandom("asdfpoiu123456789||Dffesd"));
    }

    @Test
    public void testDecentEnding() {
        List<String> yep = Arrays.asList("it was I who found the lamp".split(" "));
        List<String> yep2 = Arrays.asList("this is funny".split(" "));
        List<String> nope = Arrays.asList("who found the lamp ? it was I".split(" "));
        List<String> nope2 = Arrays.asList("what kind of thing is her".split(" "));
        List<String> nope3 = Arrays.asList("what kind of thing is this and".split(" "));

        Assert.assertTrue(bigrammer.isDecentEndingWord(yep));
        Assert.assertTrue(bigrammer.isDecentEndingWord(yep2));
        Assert.assertFalse(bigrammer.isDecentEndingWord(nope));
        Assert.assertFalse(bigrammer.isDecentEndingWord(nope2));
        Assert.assertFalse(bigrammer.isDecentEndingWord(nope3));
    }
}
