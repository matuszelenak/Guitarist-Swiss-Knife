package sk.matus.ksp.guitarist_swiss_knife;

import android.content.res.Resources;
import android.util.JsonReader;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * This class is a representation of the musical chord
 */
public class Chord {
    ArrayList<SemiTone>scale;
    ToneUtils toneUtils;
    String type = "Major";
    String fifth = "5";
    String seventh = "";
    String ninth = "";
    String eleventh = "";
    String thirteenth = "";
    String sus = "";
    String augdim = "";
    String add29 = "";
    String add411 = "";
    String add613 = "";
    HashMap<SemiTone,Boolean>chord = new HashMap<>();
    private HashSet<String>flags = new HashSet<>();
    HashSet<SemiTone> progression = new HashSet<>();
    private HashMap<String,Double>flagMeaning = new HashMap<>();

    /**
    * @param toneUtils The ToneUtils class. The Chord flag uses this instance to
    * resolve any requests regarding the tone operations*/
    public Chord(ToneUtils toneUtils){
        this.toneUtils = toneUtils;
    }

    /**
    * @param scale The scale from which the chord is to be derived.*/
    public void setScale(ArrayList<SemiTone>scale){
        this.scale = scale;
    }

    /**
    * A not-yet implemented method that will add the flag to the set of current flags.
    * @param flag A flag to be set*/
    public void setFlag(String flag){
        flags.add(flag);
    }

    /**
    * @return The set of current flags.*/
    public HashSet getFlags(){
        return flags;
    }

    /**
    * The method loads flags with corresponding meaning (where meaning is the tone to alter in the chord)
    * from .json file.
    * @param resources The Resources class that should be used for loading the flags*/
    public void assignFlagMeaning(Resources resources){
        InputStream io = resources.openRawResource(R.raw.chord_flags);
        try {
            readJsonStream(io);
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }

    /**
    * This method starts the actual reading of the JSON file.
    * @param in Input stream from which to read json file*/
    public void readJsonStream(InputStream in) throws IOException {
        JsonReader reader = new JsonReader(new InputStreamReader(in, "UTF-8"));
        try {
            readFlagsArray(reader);
        }
        finally {
            reader.close();
        }
    }

    /**
    * Method reads the array of flags.
    * @param reader Used JsonReader*/
    private void readFlagsArray(JsonReader reader) throws IOException{
        reader.beginArray();
        while (reader.hasNext()) {
            readFlag(reader);
        }
        reader.endArray();
    }

    /**
    * Method reads a single flag.
    * @param reader Used JsonReader*/
    private void readFlag(JsonReader reader) throws IOException{
        reader.beginArray();
        String flag = reader.nextString();
        double index = reader.nextDouble();
        flagMeaning.put(flag,index);
        reader.endArray();
    }

    /**
    * This method iterates through the flags and adds
    * the tones to the chord according to them.
     * Will be deprecated once DependencyScheme works properly.
    * */
    private void collectTones(){
        chord = new HashMap<>();
        chord.put(scale.get(0),true);
        switch(type){
            case "Major": chord.put(scale.get(2),true); break;
            case "minor": chord.put(scale.get(2).getLower(),true); break;
            default:break;
        }
        switch (fifth){
            case "5": chord.put(scale.get(4),true); break;
            case "5#" : chord.put(scale.get(4).getHigher(),true); break;
            case "5b": chord.put(scale.get(4).getLower(),true); break;
            default:break;
        }
        switch (seventh){
            case "7": chord.put(scale.get(6).getLower(),true); break;
            case "7M" : chord.put(scale.get(6),true); break;
            case "6": chord.put(scale.get(5),true); break;
            default:break;
        }
        switch (sus){
            case "sus2": chord.put(scale.get(1),true); break;
            case "sus4" : chord.put(scale.get(3),true); break;
            default:break;
        }
        switch (ninth){
            case "9#": chord.put(scale.get(8%scale.size()).getHigher(),true); break;
            case "9" : chord.put(scale.get(8%scale.size()),true); break;
            case "9b": chord.put(scale.get(8%scale.size()).getLower(),true); break;
            default:break;
        }
        switch (eleventh){
            case "11#": chord.put(scale.get(10%scale.size()).getHigher(),true); break;
            case "11" : chord.put(scale.get(10%scale.size()),true); break;
            case "11b": chord.put(scale.get(10%scale.size()).getLower(),true); break;
            default:break;
        }
        switch (thirteenth){
            case "13#": chord.put(scale.get(12%scale.size()).getHigher(),true); break;
            case "13" : chord.put(scale.get(12%scale.size()),true); break;
            case "13b": chord.put(scale.get(12%scale.size()).getLower(),true); break;
            default:break;
        }
        switch (add29){
            case "add2": chord.put(scale.get(1%scale.size()),true); break;
            case "add9" : chord.put(scale.get(8%scale.size()),true); break;
            default:break;
        }
        switch (add411){
            case "add4": chord.put(scale.get(3%scale.size()),true); break;
            case "add11" : chord.put(scale.get(10%scale.size()),true); break;
            default:break;
        }
        switch (add613){
            case "add6": chord.put(scale.get(5%scale.size()).getHigher(),true); break;
            case "add13" : chord.put(scale.get(12%scale.size()),true); break;
            default:break;
        }
    }

    /**
     * Not yet implemented version of the collectTones() method that will do the
    * same thing, but will be working on the dependency-resolving method and a dynamic
    * set of flags instead of hardcoded variables.*/
    private void collectTonesAlt(){
        progression.add(scale.get(0));
        for (String flag : flags){
            progression.add(resolveFlag(flag));
        }
    }

    /**
    * This method is supposed to resolve the flag to a SemiTone that the flag is supposed to add.
    * Each flag has a meaning which is a double value. The integer part of this value represents
    * the position at the scale, the (value - integer part) represents the semitone shift if there is any
    * (-0.5 = semitone lower, 0.5 = semitone higher, 0.0 no shift)
    * @param flag A flag to be processed
    * @return A semiTone that is resulting from the flag being set*/
    private SemiTone resolveFlag(String flag){
        double rawIndex = flagMeaning.get(flag);
        int index = (int)Math.floor(rawIndex);
        if (rawIndex - index > 0)  return scale.get(index).getHigher();
        if (rawIndex - index < 0) return scale.get(index).getLower();
        return scale.get(index);
    }

    /**
    * Iterates through the scale from which the chord is to be derived (starts at the
    * root note position) and appends the tones present in the chord to the resulting
    * string representation. This way the chord tones are printed out in the correct
    * order and with names identical to those in the scale
    * @param root The starting tone of the chord
    * @return A string representation of the current chord*/
    public String getProgression(String root){
        collectTones();
        StringBuilder sb = new StringBuilder();
        int i = toneUtils.getSemiTonePosition(root);
        int count = 0;
        while (count < 12){
            if (chord.containsKey(toneUtils.getSemiTones().get(i % 12))){
                sb.append(toneUtils.getSemiTones().get(i%12).getNames().get(0)).append(" ");
            }
            i++;
            count++;
        }
        return sb.toString();
    }

}
