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
import java.util.LinkedList;
import java.util.List;

import static com.github.megallo.markoverator.bigrammer.Bigrammer.DELIM;

/**
 * Go find full phrases that work with a syllable count instead of markov generating them
 * This is boring and done before but hey here's another one
 *
 * Also this file is a copy/paste of Songwriter with lazy edits
 **/
public class Songfinder {
    private static final Logger loggie = LoggerFactory.getLogger(Songfinder.class);

    // here is the dumbest basic start possible
    // you want the model from bigrammer and the model from poet
    // instead of looking up the phonemes you just count the syllables
    // from the bigrammer model you just want the next possible list for the word(s)


    private final Bigrammer bigrammer;
    private final Poet poet;

    int targetSyllableCount = 6; // this is the line of the song that you'll look up or calculate

    public Songfinder(Bigrammer bigrammer, Poet poet) {
        this.bigrammer = bigrammer;
        this.poet = poet;
    }

    public static void main(String[] args) throws FileNotFoundException {


        Bigrammer bigrams = new Bigrammer();
        bigrams.loadModel(new FileInputStream(new File(args[0])));

        Poet poet = new Poet();

        Songfinder songwriter = new Songfinder(bigrams, poet);
//        String seedWord = "dogs";
        songwriter.findSong("dogs");
        songwriter.findSong("nice");

/*
        ArrayList<String> printme = new ArrayList<>();
        loggie.info("To the tune of Call Me Maybe");
        List<String> songLine = songwriter.findSongLine(seedWord, 5);

        printme.add(songLine == null ? "null" : Joiner.on(' ').join(songLine));

        songLine = songwriter.findSongLine(seedWord, 5);
        printme.add(songLine == null ? "null" : Joiner.on(' ').join(songLine));
        songLine = songwriter.findSongLine(seedWord, 5);
        printme.add(songLine == null ? "null" : Joiner.on(' ').join(songLine));
        songLine = songwriter.findSongLine(seedWord, 5);
        printme.add(songLine == null ? "null" : Joiner.on(' ').join(songLine));

        StringBuilder sb = new StringBuilder();
        for (String didit : printme) {
            sb.append(didit).append("\n");
        }
        loggie.info("\n{}", sb);*/
    }

    public void findSong(String seedWord) {

        ArrayList<SongLine> songLines = new ArrayList<>();
//        loggie.info("To the tune of Call Me Maybe");
//        songLines.add(new SongLine(5, false, false));
//        songLines.add(new SongLine(5, true, false));
//        songLines.add(new SongLine(5, false, false));
//        songLines.add(new SongLine(5, true, true));
//        findSong(seedWord, songLines);
//
//
//        loggie.info("To the tune of I'm a Little Teapot");
//        songLines.add(new SongLine(9, true, false));
//        songLines.add(new SongLine(9, true, false));
//        songLines.add(new SongLine(9, true, false));
//        songLines.add(new SongLine(8, true, true));
//        findSong(seedWord, songLines);
//
//        loggie.info("To the tune of Gonna Be (500 Miles)");
//        songLines.add(new SongLine(8, false, false));
//        songLines.add(new SongLine(8, true, false));
//        songLines.add(new SongLine(11, false, false));
//        songLines.add(new SongLine(6, true, true));
//        findSong(seedWord, songLines);
//
//        loggie.info("To the tune of All Star");
//        songLines.add(new SongLine(12, true, true));
//        songLines.add(new SongLine(12, true, false));
//        songLines.add(new SongLine(7, false, false));
//        songLines.add(new SongLine(8, false, false));
//        findSong(seedWord, songLines);
//
//        loggie.info("To the tune of Wannabe");
//        songLines.add(new SongLine(7, true, true));
//        songLines.add(new SongLine(5, true, false));
//        songLines.add(new SongLine(6, false, false));
//        songLines.add(new SongLine(5, false, false));
//        findSong(seedWord, songLines);

        loggie.info("To the tune of Pretty Woman");
        songLines.add(new SongLine(4, true, true));
        songLines.add(new SongLine(5, true, false));
        songLines.add(new SongLine(4, false, false));
        songLines.add(new SongLine(5, false, false));
        findSong(seedWord, songLines);

        // TODO I think for this a Song overlord makes more sense
        // because if we do find a line or set of lines that work
        // and we want them to rhyme elsewhere, then we need to know what the new target word is
        // since we aren't enforcing line ending to be the word

    }

    public void findSong(String seedWord, List<SongLine> songLines) {
        ArrayList<String> printme = new ArrayList<>();

        for (SongLine line : songLines) {
            List<String> songLine = findSongLine(seedWord, line.syllables);

            if (songLine == null) {
                loggie.error("Can't find {} syllables ending with word {}", line.syllables, seedWord);
            }
            printme.add(songLine == null ? "null" : Joiner.on(' ').join(songLine));
        }

        StringBuilder sb = new StringBuilder();
        for (String didit : printme) {
            sb.append(didit).append("\n");
        }
        loggie.info("\n{}", sb);
    }

    public List<String> findSongLine(String seedWord, int targetSyllableCount) {

        List<List<String>> phrasesContainingWord = bigrammer.getPhrasesContainingWord(seedWord);
        Collections.shuffle(phrasesContainingWord);
        for (List<String> phrase : phrasesContainingWord) {
            int syllables = countSyllables(phrase);
            if (syllables == targetSyllableCount) {
                // we found a phrase to put in the song
                // it doesn't rhyme or anything, just the right length
                return phrase;
            }
        }

        return null;

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
