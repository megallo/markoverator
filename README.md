[![Build Status](http://img.shields.io/travis/megallo/markoverator.svg)](https://travis-ci.org/megallo/markoverator) [![License](http://img.shields.io/badge/license-apache%202-brightgreen.svg)](https://github.com/megallo/markoverator/blob/master/LICENSE)

## Markoverator

Backwards/forwards Markov text generator specific to HipChat logs. Can generate sentences around a specified seed word or words.

The expected corpus should be one message or sentence per line in a txt file.:sparkles: Currently the TextUtils can be used to take raw chat sentences and remove the @mentions and URLs while leaving the emoticons.  It can also tokenize or remove punctuation. Now you have cleaned sentences.

Feed those cleaned sentences into the model builder of [Bigrammer](src/main/java/com/github/megallo/markoverator/Bigrammer.java), and then you can generate sentences. Optionally pass in a seed word and it will generate a sentence with that word somewhere in the middle, or return null if that word doesn't exist in the source model. You can also pass in two seed words, and if they occur in the corpus adjacent and in that order, it will happily generate a sentence around the two words.

The model can be serialized to a file and loaded as needed instead of generating it every time.

Example usage can be found in the [MarkovGenerator](src/main/java/com/github/megallo/markoverator/MarkovGenerator.java) class. A sample corpus, Alice in Wonderland, is in the root directory as `alice.txt`. 

:sparkles: I use a quick 'n dirty python script to preprocess the hipchat export. I'll put it up here in a future release.

### Input
The MarkovGenerator's main() method takes two arguments: a path to a corpus file, and the name of the output model file.
The input corpus file contains one message per line, e.g.
```
Test. Test. Is this thing on?
(awwyiss)
@here can someone take a look at that pull request
/code 127.0.0.1:8080
```

## Poet
Now you can made poems from your model! See example usage in [PoemGenerator](src/main/java/com/github/megallo/markoverator/PoemGenerator.java). This uses the [CMU Pronouncing Dictionary](http://www.speech.cs.cmu.edu/cgi-bin/cmudict).

So far it only rhymes on the last syllable. I hope to dig into the syllable information in the CMU dict next to make more interesting rhymes.

## Release Notes
#### New in 2.1.0 :exclamation:
- Poet: find words that rhyme with a seed word
- PoemGenerator: example usage of how to make a poem from your Bigrammer model based on a seed word

#### New in 2.0.0
- Ability to generate either forwards-only or backwards-only sentences from single seed word
- Even more text utils

#### New in 1.0.5
- Ability to generate a phrase around two seed words instead of just one
- Sample corpus for trying out model building


## Installation

### Maven
```xml
    <dependency>
      <groupId>com.github.megallo</groupId>
      <artifactId>markoverator</artifactId>
      <version>2.1.0</version>
    </dependency>
```

### Gradle
```groovy
    compile "com.github.megallo:markoverator:2.1.0"
```

### Building from source
This module uses a [Gradle](http://gradle.org)-based build system. In the instructions
below, [`./gradlew`](http://vimeo.com/34436402) is invoked from the root of the source tree and serves as
a cross-platform, self-contained bootstrap mechanism for the build. The only
prerequisites are [Git](https://help.github.com/articles/set-up-git) and JDK 1.7+.

#### check out sources
`git clone git://github.com/megallo/markoverator.git`

#### compile and test, build all jars
`./gradlew build`

#### install all jars into your local Maven cache
`./gradlew install`

###License
This module is released under version 2.0 of the
[Apache License](http://www.apache.org/licenses/LICENSE-2.0).
