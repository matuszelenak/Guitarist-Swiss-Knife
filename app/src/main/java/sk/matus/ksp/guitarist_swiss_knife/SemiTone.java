package sk.matus.ksp.guitarist_swiss_knife;

import android.util.Log;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class represents the atomic part of the tone scale - a semitone.
 */
class SemiTone {
    SemiTone higher;
    SemiTone lower;
    ArrayList<String> names = new ArrayList<>();
    public SemiTone(String name){
        names.add(name);
    }

    public ArrayList<String> getNames() {
        return names;
    }

    /**Method to add possible names for this semitone.
    * Rejects names with too many # or b
    * @param name A name to be processed and added*/
    public void addName(String name){
        if (name.contains("#") && name.contains("b")) return;
        if (name.contains("###") || name.contains("bbb")) return;
        if (!names.contains(name)) names.add(name);
    }

    /**@return A Tone that is higher by one semitone*/
    public SemiTone getHigher() {
        return higher;
    }

    /**@return A Tone that is lower by one semitone*/
    public SemiTone getLower() {
        return lower;
    }

    public void setHigher(SemiTone higher) {
        this.higher = higher;
    }

    public void setLower(SemiTone lower) {
        this.lower = lower;
    }

    @Override
    public String toString(){
        return names.get(0);
    }
}