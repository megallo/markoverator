package com.github.megallo.markoverator.songwriter;

import com.github.megallo.markoverator.bigrammer.Bigrammer;
import com.github.megallo.markoverator.poet.Rhymer;
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

    private final Bigrammer bigrammer;
    private final Rhymer rhymer;

    public Songwriter(Bigrammer bigrammer, Rhymer rhymer) {
        this.bigrammer = bigrammer;
        this.rhymer = rhymer;
    }

    // TODO debug entry point, move to SongGenerator
    public static void main(String[] args) throws FileNotFoundException {


        Bigrammer bigrams = new Bigrammer();
        bigrams.loadModel(new FileInputStream(new File(args[0])));

        Rhymer rhymer = new Rhymer();

        Songwriter songwriter = new Songwriter(bigrams, rhymer);
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

    // TODO still a work in progress, sometimes the song line is null and sometimes it has an extra syllable
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
                List<String> rhymingWords = rhymer.findRhymingWords(seedWord);
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
                    someWord = bigrammer.getRandomWordFromModel();
                    if (usableSongWord(someWord) && bigrammer.isDecentEndingWord(Arrays.asList(someWord))) {
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

        List<Pair> allPossibleStarts = bigrammer.getAllPossiblePairsEndingWithWord(seedWord);

        if (allPossibleStarts != null) {
            Collections.shuffle(allPossibleStarts);
        }

        loggie.debug("All possible starts: {}", allPossibleStarts);
        for (Pair targetStarts : allPossibleStarts) {
            ArrayList<String> sentence = new ArrayList<>();
            String w1 = targetStarts.getFirst();
            String w2 = targetStarts.getSecond();

            // quick eval to make sure we like these words
            if (!usableSongWord(w1) || !usableSongWord(w2)) {
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

    /**
     * Decide if we can use this word in our song. Specifically, we need to know if we can count the syllables for it.
     * That means check for punctuation, which is valid as a "word" but counts as zero syllables
     *
     * @param word any string that we want to include in the poem
     * @return true if its syllables can be counted because it is either in the rhyming dictionary OR it is punctuation
     */
    private boolean usableSongWord(String word) {

        if (word.length() == 1 && !Character.isLetter(word.charAt(0))) {
            return true;
        }

        return rhymer.knownWord(word);
    }



    private int maxDepthSafetyCounter = 0;

    private List<String> recursiveTreeWalker(String w1, String w2, ArrayList<String> sentence, int targetSyllableCount) {

        int totalSyllableCount = countSyllables(sentence);
//        loggie.info("evaluating {}", sentence);

        if (totalSyllableCount == targetSyllableCount) {
            return sentence;
        }

        else if (totalSyllableCount > targetSyllableCount) {
            return null;
        }

        else if (totalSyllableCount < targetSyllableCount) {
            // keep going
            List<String> nextWords = bigrammer.getModel().getBackwardCache().get(new Pair(w1, w2));
            if (nextWords != null && maxDepthSafetyCounter++ < 20) {
                Collections.shuffle(nextWords);
                for (String w : nextWords) {
                    // here is where we evaluate w and decide if we like it
                    if (w.equals(DELIM)) {
                        continue; // end of the line, don't pursue this branch further but also don't log it
                    }
                    if (!usableSongWord(w)) {
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


    private int countSyllables(List<String> sumMe) {

        int sum = 0;

        for (String word : sumMe) {
            sum = sum + rhymer.getSyllableCount(word);
        }

        return sum;
    }

}
