package com.runrmby.runner;

import java.util.ArrayList;

/**
 * Created by benjamin on 2/8/17.
 *
 */
public class Location {

    public String name;
    public int titleScreenArt;
    public int roadArt;
    public ArrayList<Integer> itemArtArray = new ArrayList<>();

    public Location(String name, int titleScreenArt, int roadArt, ArrayList<Integer> itemArtArray) {
        this.name = name;
        this.titleScreenArt = titleScreenArt;
        this.roadArt = roadArt;
        this.itemArtArray = itemArtArray;
    }
}
