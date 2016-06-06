package sk.matus.ksp.guitarist_swiss_knife;

import android.content.res.Resources;
import android.util.JsonReader;
import android.util.Log;

import com.udojava.evalex.Expression;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.jar.Attributes;


/**
 * This class is a representation of the musical chord.
 */
public class Chord {

    class NameComponent{
        String value;
        int position;
        String condition;
        NameComponent(String value, int position, String condition){
            this.value = value;
            this.position = position;
            this.condition = condition;
        }
    }
    private String name;
    /**
     * A list that holds the musical scale from which the chord progression is to be derived.
     */
    private ArrayList<Tone>scale;
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
    private HashSet<Tone> progression = new HashSet<>();

    private String stringProgression;
    /**
     * HashMap that assigns a meaning to each flag. The meaning is basically a complex index
     * into complete progression of 12 semitones.
     */
    private HashMap<String,Double>flagMeaning = new HashMap<>();

    private ArrayList<NameComponent>nameComponents;

    /**
    * @param toneUtils The ToneUtils class. The Chord uses this instance to
    * resolve any requests regarding the tone operations*/
    public Chord(ToneUtils toneUtils){
        this.toneUtils = toneUtils;
    }

    /**
    * @param scale The scale from which the chord is to be derived.*/
    public void setScale(ArrayList<Tone>scale){
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

    public String getName() {
        return name;
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
    * This method is supposed to resolve the flag to a Tone that the flag is supposed to add.
    * Each flag has a meaning which is a double value. The integer part of this value represents
    * the position at the scale, the (value - integer part) represents the semitone shift if there is any
    * (0.5 = semitone higherSemitone, 0.0 no shift)
    * @param flag A flag to be processed
    * @return A semiTone that is resulting from the flag being set*/
    private Tone resolveFlag(String flag){
        double rawIndex = flagMeaning.get(flag);
        int index = (int)Math.floor(rawIndex);
        if (rawIndex - index > 0)  return scale.get(index).getHigherTone();
        return scale.get(index);
    }

    /**
     * @return string representation of the tone progression in a the chord
     */
    public String getTextProgression(){
       return stringProgression;
    }

    /**
     * @return A Set of Tone instances which represent the chord
     */
    public HashSet<Tone> getSemiToneProgression() {
        return progression;
    }

    /**
     * Iterates through the scale from which the chord is to be derived (starts at the
     * baseName note position) and appends the tones present in the chord to the resulting
     * string representation. This way the chord tones are printed out in the correct
     * order and with names identical to those in the scale
     * @param root The starting tone of the chord*/
    public void constructProgression(ToneName root){
        progression = new HashSet<>();
        collectTones();
        StringBuilder sb = new StringBuilder();
        int i = toneUtils.getSemiTonePosition(root);
        int count = 0;
        while (count < 12){
            if (progression.contains(toneUtils.getTones().get(i % 12))){
                sb.append(toneUtils.getTones().get(i%12).getPrimaryName().format("%b%a")).append(" ");
            }
            i++;
            count++;
        }
        stringProgression = sb.toString();
        name = resolveName();
    }

    public boolean evaluateExpression(String exp){
        String oldexp = exp;
        int unused = 0;
        for (String flag : flags){
            StringBuilder sb = new StringBuilder();
            sb.append('x');
            sb.append(flag);
            sb.append('x');
            exp = oldexp.replaceAll(sb.toString(),"1");
            if (exp.equals(oldexp)){
                unused++;
            }
            oldexp = exp;
        }
        if (unused > 0){
            exp = exp.replaceAll("Y","1");
        }
        else
        {
            exp = exp.replaceAll("Y","0");
        }
        exp = exp.replaceAll("x[^()]+x","0");
        BigDecimal result = null;

        Expression expression = new Expression(exp);
        result = expression.eval();
        return result.toString().equals("1");
    }

    public String resolveName(){
        for (String s: flags){
            Log.i("FLAG", s);
        }
        StringBuilder sb = new StringBuilder();
        for (NameComponent nc : nameComponents){
            if (evaluateExpression(nc.condition)) sb.append(nc.value);
        }
        return sb.toString();
    }

    public void loadNameResolutionData(Resources res){
        InputStream io = res.openRawResource(R.raw.chord_name_resolution_conditions);
        try {
            JsonReader reader = new JsonReader(new InputStreamReader(io, "UTF-8"));
            nameComponents = readNameComponentArray(reader);
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }

    private ArrayList<NameComponent> readNameComponentArray(JsonReader reader) throws IOException{
        ArrayList<NameComponent>result = new ArrayList<>();
        reader.beginArray();
        while (reader.hasNext()) {
            result.add(readNameComponent(reader));
        }
        reader.endArray();
        return result;
    }

    private NameComponent readNameComponent(JsonReader reader) throws IOException{
        reader.beginObject();
        String value = "";
        String condition = "";
        int position = 0;
        int octave=0;
        while (reader.hasNext()){
            String varName = reader.nextName();
            switch (varName){
                case "value":
                    value = reader.nextString();
                    break;
                case "condition":
                    condition = reader.nextString();
                    break;
                case "position":
                    position = reader.nextInt();
                    break;
                default: reader.skipValue();
                    break;
            }
        }
        reader.endObject();
        return new NameComponent(value,position,condition);
    }
}
