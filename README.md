[![Build Status](http://img.shields.io/travis/megallo/markoverator.svg)](https://travis-ci.org/megallo/markoverator) [![License](http://img.shields.io/badge/license-apache%202-brightgreen.svg)](https://github.com/megallo/markoverator/blob/master/LICENSE)

## What is this?

Backwards/forwards Markov text generator specific to HipChat logs.

Currently the TextUtils can be used to take raw chat sentences and remove the @mentions and URLs (also removes punctuation, but I'm working on that) while leaving the emoticons. 

Feed the cleaned sentences into the model builder of the generator, and then you can generate sentences. Optionally pass in a seed word and it will generate a sentence with that word somewhere in the middle, or return null if that word doesn't exist in the source model.

The model can be serialized to a file and loaded as needed instead of generating it every time.

Example usage can be found in the [MarkovGenerator](src/main/java/com/github/megallo/markoverator/MarkovGenerator.java) class.

#### Input
The MarkovGenerator's main() method takes a path to a file as its only argument.
The file contains one message per line, e.g.
```
Test. Test. Is this thing on?
(awwyiss)
@here can someone take a look at that pull request
/code 127.0.0.1:8080
```

## Installation

### Maven
```xml
    <dependency>
      <groupId>com.github.megallo</groupId>
      <artifactId>markoverator</artifactId>
      <version>1.0.2</version>
    </dependency>
```

### Gradle
```groovy
    compile "com.github.megallo:markoverator:1.0.2"
```

### Building from source
This module uses a [Gradle](http://gradle.org)-based build system. In the instructions
below, [`./gradlew`](http://vimeo.com/34436402) is invoked from the root of the source tree and serves as
a cross-platform, self-contained bootstrap mechanism for the build. The only
prerequisites are [Git](https://help.github.com/articles/set-up-git) and JDK 1.6+.

#### check out sources
`git clone git://github.com/megallo/markoverator.git`

#### compile and test, build all jars
`./gradlew build`

#### install all jars into your local Maven cache
`./gradlew install`

###License
This module is released under version 2.0 of the
[Apache License](http://www.apache.org/licenses/LICENSE-2.0).
