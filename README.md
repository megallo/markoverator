[![Build Status](http://img.shields.io/travis/megallo/markoverator.svg)](https://travis-ci.org/megallo/markoverator) [![License](http://img.shields.io/badge/license-apache%202-brightgreen.svg)](https://github.com/megallo/markoverator/blob/master/LICENSE)

##What is this?

Markov text generator and stuff.

##Installation

###Maven
```xml
    <dependency>
      <groupId>com.github.megallo</groupId>
      <artifactId>markoverator</artifactId>
      <version>1.0.2</version>
    </dependency>
```

###Gradle
```groovy
    compile "com.github.megallo:markoverator:1.0.2"
```

###Building from source
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
