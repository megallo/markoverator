package com.github.megallo.markoverator.songwriter;

/**
 * Hello I am a line of a song
 **/
public class SongLine {


    int syllables;

    // these are obviously orthogonal
    boolean shouldRhyme;
    boolean targetWordHere;


    public SongLine(int syllables, boolean shouldRhyme, boolean targetWordHere) {
        this.syllables = syllables;
        this.shouldRhyme = shouldRhyme;
        this.targetWordHere = targetWordHere;
    }
}