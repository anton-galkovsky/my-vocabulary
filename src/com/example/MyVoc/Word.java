package com.example.MyVoc;

import java.io.Serializable;
import java.util.ArrayList;

class Word implements Serializable {

    String foreignWord;
    ArrayList<String> translations;
    int time;
    ArrayList<Integer> groupNumber;
    PartOfSpeech partOfSpeech;

    Word(String foreignWord, int time, ArrayList<Integer> groupNumber, PartOfSpeech partOfSpeech, ArrayList<String> translations) {
        this.foreignWord = foreignWord;
        this.translations = translations;
        this.time = time;
        this.groupNumber = groupNumber;
        this.partOfSpeech = partOfSpeech;
    }
}
