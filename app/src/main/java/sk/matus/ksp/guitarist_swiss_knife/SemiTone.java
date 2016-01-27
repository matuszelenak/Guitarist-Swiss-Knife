package sk.matus.ksp.guitarist_swiss_knife;

import android.util.Log;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by whiskas on 26.1.2016.
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

    public void addName(String name){
        if (name.contains("#") && name.contains("b")) return;
        if (name.contains("###") || name.contains("bbb")) return;
        if (!names.contains(name)) names.add(name);
    }

    public SemiTone getHigher() {
        return higher;
    }

    public SemiTone getLower() {
        return lower;
    }

    public void setHigher(SemiTone higher) {
        this.higher = higher;
    }

    public void setLower(SemiTone lower) {
        this.lower = lower;
    }
}