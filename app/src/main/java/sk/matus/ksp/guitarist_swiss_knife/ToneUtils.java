package sk.matus.ksp.guitarist_swiss_knife;

import android.content.res.Resources;
import android.graphics.PointF;
import android.util.JsonReader;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.TreeMap;

/**
 * This class should handle all the request regarding tones and their properties
 */

public class ToneUtils {
    private TreeMap<Double,Tone> frequencyMapping = new TreeMap<>();
    /**
     * Contains the full range of semitones found in one octave (From "C" to "B")
     */
    private ArrayList<Tone> tones = new ArrayList<>();
    /**
     * Contains the progression of SemiTones that form the current scale.
     */
    private ArrayList<Tone> currentScale = new ArrayList<>();
    /**
     * Describes the same scale as the currentScale variable but is a string instead.
     */
    private String currentScaleAsString;

    /**
    * Constructor reads a list containing the tones
    * @param res Resources to be read from*/
    public ToneUtils(Resources res){
        InputStream io = res.openRawResource(R.raw.base_tones);
        try {
            tones = readJsonStream(io);
        }
        catch (IOException e){
            e.printStackTrace();
        }
        generateAlternativeNames();
        generateAllTones();
        bindTones();
    }

    /**
    * @param in InputStream from which to read the JSON file*/
    public ArrayList<Tone> readJsonStream(InputStream in) throws IOException {
        JsonReader reader = new JsonReader(new InputStreamReader(in, "UTF-8"));
        try {
            return readTonesArray(reader);
        }
        finally {
            reader.close();
        }
    }

    /**
    * A method to read a semitone array
    * @param reader The JsonReader to use for reading*/
    private ArrayList<Tone> readTonesArray(JsonReader reader) throws IOException{
        ArrayList<Tone>tones = new ArrayList<>();
        reader.beginArray();
        while (reader.hasNext()) {
            tones.add(readTone(reader));
        }
        reader.endArray();
        return tones;
    }

    private Tone readTone(JsonReader reader) throws IOException{
        reader.beginObject();
        Tone tone = new Tone();
        char baseName='C';
        String accidental="";
        int octave=0;
        while (reader.hasNext()){
            String varName = reader.nextName();
            switch (varName){
                case "baseName":
                    baseName = reader.nextString().charAt(0);
                    break;
                case "accidental":
                    accidental = reader.nextString();
                    break;
                case "octave":
                    octave = reader.nextInt();
                    break;
                default: reader.skipValue();
                    break;
            }
        }
        reader.endObject();
        tone.addName(baseName,accidental,octave);
        return tone;
    }

    /**
    * A procedure to expand the basic tones into full range of 8 octaves.
    * Fills the balanced binary tree whose keys are frequencies and values are scientifical names of the tones.
    * In addition, it calculates the maximum precision margins (ranges in which the frequency is associated with the tone itself)
    * both for undertuned and overtuned frequency.*/
    private void generateAllTones(){
        double previousFrequency = 0;
        for (int i = 0; i<108; i++){
            double frequency = Math.pow((double)2, ((double)(i - 57) / (double) tones.size()))*440;
            double nextFrequency = Math.pow((double)2, ((double)(i - 56) / (double) tones.size()))*440;

            double lowerBound = (frequency + previousFrequency)/2;
            double upperBound = (frequency + nextFrequency)/2;
            Tone tone = new Tone();
            ToneName template = tones.get(i%tones.size()).getPrimaryName();
            tone.addName(template.baseName,template.accidental,i/tones.size());
            tone.setOctave(i / tones.size());
            tone.setFrequency(frequency);
            tone.setPositionInOctave(i % tones.size());
            tone.setFrequencyInterval(new PointF((float) lowerBound, (float) upperBound));
            frequencyMapping.put(frequency, tone);
            previousFrequency = frequency;
        }
    }

    /**This method calculates alternative names for all of the semitones.
    * It does so by either lifting the lowerSemitone semitones with # flag[s]
    * or by lowering the higherSemitone semitones with b flag[s]*/
    private void generateAlternativeNames(){
        String[] suffix = new String[] {"♯♯","♯","","♭","♭♭"};
        for (int i = 0; i < tones.size(); i++) {
            for (int offset = -2; offset <=2; offset++){
                char baseName = tones.get(
                                ((i + offset) % tones.size() + tones.size())%tones.size()
                        ).getPrimaryName().baseName;
                String accidental = tones.get(
                        ((i + offset) % tones.size() + tones.size())%tones.size()
                ).getPrimaryName().accidental;
                tones.get(i).addName(baseName,accidental.concat(suffix[offset+2]),4);
            }
        }
    }

    /**
     * * Binds the tones in the semiTone array together: each semitone will know,
     * which semitone is higherSemitone and lowerSemitone than itself*/
    private void bindTones(){
        for (int i = 0; i < tones.size(); i++){
            tones.get(i).setHigherTone(tones.get(((i + 1) % 12 + 12) % 12));
            tones.get(i).setLowerTone(tones.get(((i - 1) % 12 + 12) % 12));
        }
    }

    private boolean isInInterval(float f,PointF p){
        return (f<p.y && f >=p.x);
    }

    public Tone analyseFrequency(double frequency){
        Tone higher = new Tone();
        higher.setFrequencyInterval(new PointF(-1,-1));
        Tone lower = new Tone();
        lower.setFrequencyInterval(new PointF(-1, -1));
        if (frequencyMapping.ceilingKey(frequency) != null){
            higher = frequencyMapping.get(frequencyMapping.ceilingKey(frequency));
        }
        if (frequencyMapping.floorKey(frequency) != null){
            lower = frequencyMapping.get(frequencyMapping.floorKey(frequency));
        }
        if (isInInterval((float)frequency,lower.getFrequencyInterval())){
            return lower;
        } else
        if (isInInterval((float)frequency,higher.getFrequencyInterval())){
            return higher;
        } else return higher;
    }

    /**
    * @return An ArrayList of SemiTones in octave*/
    public ArrayList<Tone> getTones() {
        return tones;
    }

    /**
    * This method constructs the harmonic scale starting from the baseName note.
    * The semitones in the scale obey the standard naming conventions (e.g. No letter is used more than once)
    * It stores the scale both as a list of Tone classes and as
    * a string representation.
    * @param root The baseName tone from which to build up the scale
    * */
    private void constructScale(ToneName root){
        StringBuilder scaleBuilder = new StringBuilder();
        currentScale = new ArrayList<>();
        currentScale.add(tones.get(getSemiTonePosition(root)));
        scaleBuilder.append(root.format("%b%a")).append(" ");
        int[] steps = new int[] {2,2,1,2,2,2,1};
        int pos = getSemiTonePosition(root);
        for (int i = 0, j="CDEFGAB".indexOf(root.baseName)+1; i < 6; i++, j++) {
            Tone nextTone = tones.get(((pos + steps[i])%12+12)%12);
            for (ToneName nextToneName : nextTone.getNames()){
                if (nextToneName.baseName == "CDEFGAB".charAt(j % "CDEFGAB".length())){
                    scaleBuilder.append(nextToneName.format("%b%a")).append(" ");
                    currentScale.add(nextTone);
                    break;
                }
            }
            pos += steps[i];
        }
        scaleBuilder.append(root.format("%b%a"));
        currentScaleAsString = scaleBuilder.toString();
    }

    /**
    * Given the baseName note (String) it constructs the scale and returns its string representation.
    * @param root The baseName note of the scale*/
    public String getScaleText(ToneName root){
        constructScale(root);
        return currentScaleAsString;
    }

    /**
    * Given the baseName note (String) it constructs the scale and returns it as an Array of SemiTones.
    * @param root The baseName note of the scale*/
    public ArrayList<Tone> getScaleTones(ToneName root){
        constructScale(root);
        return currentScale;
    }

    /**
    * Method resolves the String representation of a tone into its position in the octave.
    * @param toneName The toneName to be analysed
    * @return The position of the supplied tone in the semitTone array*/
    public int getSemiTonePosition(ToneName toneName){
        int i = 0;
        for (Tone t : tones){
            for (ToneName tn : t.getNames()){
                if (tn.baseName == toneName.baseName && tn.accidental.equals(toneName.accidental)) return i;
            }
            i++;
        }
        return 0;
    }
}
