package com.github.megallo.markoverator.songwriter;

import java.util.ArrayList;
import java.util.List;

/**
 * Placeholder for now. Eventually,
 *
 * Add structure to a song across lines, e.g. ABAB rhyming
 *
 **/
public class Song {

    List<SongLine> songLines = new ArrayList<>();

    /**
     * Add a line to the end of the song
     * @param line
     */
    public void addLine(SongLine line) {
        songLines.add(line);
    }
}
