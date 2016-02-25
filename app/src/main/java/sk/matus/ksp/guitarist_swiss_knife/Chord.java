package sk.matus.ksp.guitarist_swiss_knife;

import android.content.res.Resources;
import android.util.JsonReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 * This class is a representation of the musical chord.
 */
public class Chord {
    /**
     * A list that holds the musical scale from which the chord progression is to be derived.
     */
    private ArrayList<SemiTone>scale;
    /**
     * An instance of ToneUtils class for resolving tone related queries.
     */
    private ToneUtils toneUtils;
    /**
     * Set containing the flags that modify the tones in the chord.
     */
    private HashSet<String>flags = new HashSet<>();
    /**
     * This HashMap should contain the tone progression that is currently present in the chord.
     * */
    private HashSet<SemiTone> progression = new HashSet<>();

    private String stringProgression;
    /**
     * HashMap that assigns a meaning to each flag. The meaning is basically a complex index
     * into complete progression of 12 semitones.
     */
    private HashMap<String,Double>flagMeaning = new HashMap<>();

    /**
    * @param toneUtils The ToneUtils class. The Chord uses this instance to
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
    * Method that will add the flag to the set of current flags.
    * @param flag A flag to be set*/
    public void setFlag(String flag){
        flags.add(flag);
    }

    public void clearFlags(){
        flags.clear();
    }

    /**
    * @return The set of current flags.*/
    public HashSet<String> getFlags(){
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
    * This method starts the actual reading of the flag JSON file.
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
        System.out.println(Double.toString(index)+flag);
        flagMeaning.put(flag, index);
        reader.endArray();
    }
    /**
     * Not yet implemented version of the collectTones() method that will do the
    * same thing, but will be working on the dependency-resolving method and a dynamic
    * set of flags instead of hardcoded variables.*/
    private void collectTones(){
        progression.add(scale.get(0));
        for (String flag : flags){
            progression.add(resolveFlag(flag));
        }
    }

    /**
    * This method is supposed to resolve the flag to a SemiTone that the flag is supposed to add.
    * Each flag has a meaning which is a double value. The integer part of this value represents
    * the position at the scale, the (value - integer part) represents the semitone shift if there is any
    * (0.5 = semitone higherSemitone, 0.0 no shift)
    * @param flag A flag to be processed
    * @return A semiTone that is resulting from the flag being set*/
    private SemiTone resolveFlag(String flag){
        double rawIndex = flagMeaning.get(flag);
        int index = (int)Math.floor(rawIndex);
        if (rawIndex - index > 0)  return scale.get(index).getHigherSemitone();
        return scale.get(index);
    }

    /**
     * @return string representation of the tone progression in a the chord
     */
    public String getTextProgression(){
       return stringProgression;
    }

    /**
     * @return A Set of SemiTone instances which represent the chord
     */
    public HashSet<SemiTone> getSemiToneProgression() {
        return progression;
    }

    /**
     * Iterates through the scale from which the chord is to be derived (starts at the
     * root note position) and appends the tones present in the chord to the resulting
     * string representation. This way the chord tones are printed out in the correct
     * order and with names identical to those in the scale
     * @param root The starting tone of the chord*/
    public void constructProgression(String root){
        progression = new HashSet<>();
        collectTones();
        StringBuilder sb = new StringBuilder();
        int i = toneUtils.getSemiTonePosition(root);
        int count = 0;
        while (count < 12){
            if (progression.contains(toneUtils.getSemiTones().get(i % 12))){
                sb.append(toneUtils.getSemiTones().get(i%12).getNames().get(0)).append(" ");
            }
            i++;
            count++;
        }
        stringProgression = sb.toString();
    }
}
