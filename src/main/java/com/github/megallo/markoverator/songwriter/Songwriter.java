package com.github.megallo.markoverator.songwriter;

import com.github.megallo.markoverator.bigrammer.Bigrammer;
import com.github.megallo.markoverator.poet.Poet;
import com.github.megallo.markoverator.utils.Pair;
import com.google.common.base.Joiner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.github.megallo.markoverator.bigrammer.Bigrammer.DELIM;

/**
 * TODO this technically works as intended but none of the output is pleasant. Try word boundary matching next
 **/
public class Songwriter {
    private static final Logger loggie = LoggerFactory.getLogger(Songwriter.class);

    // here is the dumbest basic start possible
    // you want the model from bigrammer and the model from poet
    // instead of looking up the phonemes you just count the syllables
    // from the bigrammer model you just want the next possible list for the word(s)


    private final Bigrammer bigrammer;
    private final Poet poet;

    int targetSyllableCount = 6; // this is the line of the song that you'll look up or calculate

    public Songwriter(Bigrammer bigrammer, Poet poet) {
        this.bigrammer = bigrammer;
        this.poet = poet;
    }

    // TODO debug entry point, move to SongGenerator
    public static void main(String[] args) throws FileNotFoundException {


        Bigrammer bigrams = new Bigrammer();
        bigrams.loadModel(new FileInputStream(new File(args[0])));

        Poet poet = new Poet();

        Songwriter songwriter = new Songwriter(bigrams, poet);
        songwriter.writeSong("song");
        songwriter.writeSong("please");
        songwriter.writeSong("ping");
    }

    public void writeSong(String seedWord) {

        // TODO grab a random song and iterate over its chorus


        ArrayList<SongLine> songLines = new ArrayList<>();
//         all star
        loggie.info("To the tune of All Star");
        songLines.add(new SongLine(6, false, false));
        songLines.add(new SongLine(6, true, true));
        songLines.add(new SongLine(6, false, false));
        songLines.add(new SongLine(6, true, false));
//        songLines.add(new SongLine(7, false, false)); // last two lines get weird
//        songLines.add(new SongLine(8, false, false));
        writeSong(seedWord, songLines);

        // call me maybe
        loggie.info("To the tune of Call Me Maybe");
        songLines.clear();
        songLines.add(new SongLine(5, false, false));
        songLines.add(new SongLine(5, true, false));
        songLines.add(new SongLine(5, false, false));
        songLines.add(new SongLine(5, true, true));
        writeSong(seedWord, songLines);


//        // what is love
//        songLines.add(new SongLine(3, false, false));
//        songLines.add(new SongLine(5, true, false));
//        songLines.add(new SongLine(3, true, true));
//        songLines.add(new SongLine(2, false, false));

//        // killer queen
//        songLines.add(new SongLine(5, true, true));
//        songLines.add(new SongLine(6, true, false));
//        songLines.add(new SongLine(8, true, false));
//        songLines.add(new SongLine(8, false, false));

        // spin me right round
        // bad moon rising
        // RESPECT
        // it's my party, cry if I want to
        // down under, men at work



//        // don't stop believing
//        songLines.add(new SongLine(5, true, true));
//        songLines.add(new SongLine(6, true, false));
//        songLines.add(new SongLine(5, false, false));

        // TODO this would be easier with an optional syllable count
//        // I'm a little teapot
        songLines.clear();
        loggie.info("To the tune of I'm a Little Teapot");
        songLines.add(new SongLine(6, false, false));
        songLines.add(new SongLine(3, true, false));
        songLines.add(new SongLine(5, false, false));
        songLines.add(new SongLine(4, true, false));
        songLines.add(new SongLine(6, false, false));
        songLines.add(new SongLine(3, true, false));
        songLines.add(new SongLine(4, false, false));
        songLines.add(new SongLine(4, true, true));
        writeSong(seedWord, songLines);

//        // Gonna Be (500 Miles)
//        songLines.clear();
//        loggie.info("To the tune of Gonna Be (500 Miles)");
//        songLines.add(new SongLine(8, false, false));
//        songLines.add(new SongLine(8, true, false));
//        songLines.add(new SongLine(11, false, false)); // this appears to be impossible
//        songLines.add(new SongLine(6, true, true));
//        writeSong(seedWord, songLines);
    }

    public void writeSong(String seedWord, List<SongLine> songLines) {
        ArrayList<String> printme = new ArrayList<>();

        for (SongLine line : songLines) {
            if (line.targetWordHere) {
                String generatedLine = writeLineOfSong(seedWord, line.syllables);
                if (generatedLine == null) {
                    loggie.error("Can't find {} syllables ending with word {}", line.syllables, seedWord);
                }
                printme.add(generatedLine);
            } else if (line.shouldRhyme) {
                // TODO should we try to find rhyming words before we waste time on this song?
                // I think we'll probably have a Song object that can have a top-level boolean indicating
                // whether we'll need rhyming words for this song
                List<String> rhymingWords = poet.findRhymingWords(seedWord);
                if (rhymingWords != null) {
                    Collections.shuffle(rhymingWords);
                    loggie.debug("Rhyming words: {}", rhymingWords);
                    for (String rhyme : rhymingWords) {
                        String generatedLine = writeLineOfSong(rhyme, line.syllables);
                        if (generatedLine != null) {
                            printme.add(generatedLine);
                            break;
                        }
                    }
                }
            } else {

                String generatedLine;
                // no requirements for this line, just get some random word to make a line with
                String someWord;

                while (true) {
                    someWord = bigrammer.getRandomSeedWord();
                    if (poet.knownWord(someWord) && bigrammer.isDecentEndingWord(Arrays.asList(someWord))) {
                        generatedLine = writeLineOfSong(someWord, line.syllables);
                        if (generatedLine != null) {
                            printme.add(generatedLine);
                            break;
                        }
                    }
                }
            }
        }

        StringBuilder sb = new StringBuilder();
        for (String didit : printme) {
            sb.append(didit).append("\n");
        }
        loggie.info("\n{}", sb);

    }

    protected String writeLineOfSong(String seedWord, int targetSyllableCount) {

        List<Pair> allPossibleStarts = bigrammer.getAllPossibleStarts(seedWord);

        if (allPossibleStarts != null) {
            Collections.shuffle(allPossibleStarts);
        }

        loggie.debug("All possible starts: {}", allPossibleStarts);
        for (Pair targetStarts : allPossibleStarts) {
            ArrayList<String> sentence = new ArrayList<>();
            String w1 = targetStarts.getFirst();
            String w2 = targetStarts.getSecond();

            // quick eval to make sure we like these words
            if (!poet.knownWord(w1) || !poet.knownWord(w2)) {
                continue;
            }
            sentence.add(w1);
            sentence.add(w2);

            loggie.debug("making song line out of ::: {} {}", w1, w2);
            List<String> foundSentence = recursiveTreeWalker(w1, w2, sentence, targetSyllableCount);

            loggie.debug("+++++++++++++++++++++++++++++ {}", foundSentence);
            if (foundSentence != null) {
                return Joiner.on(' ').join(foundSentence);
            }
        }

        return null;

    }



    private int maxDepthSafetyCounter = 0;

    private List<String> recursiveTreeWalker(String w1, String w2, ArrayList<String> sentence, int targetSyllableCount) {

        int totalSyllablecount = countSyllables(sentence);
//        loggie.info("evaluating {}", sentence);

        if (totalSyllablecount == targetSyllableCount) {
            return sentence;
        }

        else if (totalSyllablecount > targetSyllableCount) {
            return null;
        }

        else if (totalSyllablecount < targetSyllableCount) {
            // keep going
            List<String> nextWords = bigrammer.model.getBackwardCache().get(new Pair(w1, w2));
            if (nextWords != null && maxDepthSafetyCounter++ < 20) {
                Collections.shuffle(nextWords);
                for (String w : nextWords) {
                    // here is where we evaluate w and decide if we like it
                    if (w.equals(DELIM)) {
                        continue; // end of the line, don't pursue this branch further but also don't log it
                    }
                    if (!poet.knownWord(w)) {
                        // TODO we don't really want to skip punctuation, I think it's hurting our chances of finding long strings
//                        loggie.info("Skipping unknown word {}", w);
                        continue; // if the word is not in the rhyming dictionary we can't count the syllables, abort this entire branch
                    }

                    ArrayList<String> nextSentence = new ArrayList<>();
                    nextSentence.add(w); // prepend
                    nextSentence.addAll(sentence);
                    List<String> next = recursiveTreeWalker(w, w1, nextSentence, targetSyllableCount);
                    if (next != null) {
                        return next;
                    }
                }
            } else {
                return null;
            }
        }

        return null;
    }

//
//    private int recursiveTreeWalkerasdfa(List<Integer> allWordSyllablesSumMe, int location) {
//
//        // start of the madness that is walking this tree
//        // todo make sure your seed is less than your target, not sure why it wouldn't be but ya know, code
//        String previousWord = bigrams.model.getFullWordList().get(location - 1);
//        int syllablesInCurrentWord = poet.countVowels(previousWord);
//        allWordSyllablesSumMe.add(syllablesInCurrentWord);
//
//        if (countSyllables(allWordSyllablesSumMe) < targetSyllableCount) {
//            // here is where the recursion starts, my brain can't do this right now
//
//            // if less than count, call itself
//            List<String> prevWordOptions = bigrams.model.getBackwardCache().get(new Pair(word2, word3));
//            String word1 = prevWordOptions.get(random.nextInt(prevWordOptions.size()));
//            // TODO end based on POS tag - make a method that checks end condition
//            if (word1.equals(DELIM)) {
//                break;
//            }
//            // if equal to count, we are done
//            // if greater than count, back up one level
//            // need a check to see if we're at square one, in which case it's impossible, move on to next song requirement syllable
//        }
//    }

    private int countSyllables(List<String> sumMe) {

        int sum = 0;

        for (String word : sumMe) {
            sum = sum + poet.countVowels(word);
        }

        return sum;
    }


    /// PHASE 1: JUST MAKE ONE LINE WITH A TARGET SYLLABLE COUNT
    // worry about generating next lines and rhymes later

    // take a target word
    // add its syllable count to target total count
    // start building a backwards thing
    // for each peek of possible next word, count its syllables
    // you will have to do a weird "go back one level in the tree" if the syllable count won't work

    // you need the power of both bigrammer and poet for this
    // you take the target word, count syllables using poet
    // you walk the potential previous word map, counting syllable combinations as you go, that's in bigrammer

}
