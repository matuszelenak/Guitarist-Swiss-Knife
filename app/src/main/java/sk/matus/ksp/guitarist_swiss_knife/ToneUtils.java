package sk.matus.ksp.guitarist_swiss_knife;

import android.content.Context;
import android.content.res.Resources;
import android.util.JsonReader;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;

/**
 * This class should handle all the request regarding tones and their properties
 */

public class ToneUtils {
    private TreeMap<Double,String> allSemiTones = new TreeMap<>();
    private HashMap<Double,Tuple<Double,Double>>precisionIntervals = new HashMap<>();
    private ArrayList<SemiTone> semiTones = new ArrayList<>();
    private ArrayList<SemiTone> currentScale = new ArrayList<>();
    private String currentScaleAsString;
    private String baseTones;

    /*
    * OnCreate a list containing the basic semiTones tones is read*/
    public ToneUtils(Resources res){
        InputStream io = res.openRawResource(R.raw.base_tones);
        baseTones = res.getString(R.string.base_tones);
        try {
            readJsonStream(io);
        }
        catch (IOException e){
            e.printStackTrace();
        }
        generateAlternativeNames();
        generateAllSemiTones();
        bindTones();
    }
    public void readJsonStream(InputStream in) throws IOException {
        JsonReader reader = new JsonReader(new InputStreamReader(in, "UTF-8"));
        try {
            readTonesArray(reader);
        }
        finally {
            reader.close();
        }
    }

    private void readTonesArray(JsonReader reader) throws IOException{
        reader.beginArray();
        String name;
        while (reader.hasNext()) {
            name = reader.nextString();
            semiTones.add(new SemiTone(name));
        }
        reader.endArray();
    }

    /*
    * A procedure to expand the basic semiTones into full scale of 8 octaves
    * Fills the balanced binary tree whose keys are frequencies and values are scientifical names of the tones.
    * In addition, it calculates the maximum precision margins (ranges in which the frequency is associated with the tone itself)
    * both for undertuned and overtuned frequency.*/
    private void generateAllSemiTones(){
        double previousFrequency = 0;
        for (int semitone = 0; semitone<108; semitone++){
            double frequency = Math.pow((double)2, ((double)(semitone - 57) / (double) semiTones.size()))*440;
            double nextFrequency = Math.pow((double)2, ((double)(semitone - 56) / (double) semiTones.size()))*440;

            String name = semiTones.get(semitone% semiTones.size()).getNames().get(0)+"_"+semitone/ semiTones.size();
            allSemiTones.put(frequency, name);
            double lowerMargin = (frequency - previousFrequency)/4;
            double upperMargin = (nextFrequency- frequency)/4;
            Tuple<Double,Double> margins = new Tuple<>(lowerMargin,upperMargin);
            precisionIntervals.put(frequency,margins);
            previousFrequency = frequency;
        }
    }

    private void generateAlternativeNames(){
        String[] suffix = new String[] {"##","#","","b","bb"};
        for (int i = 0; i < semiTones.size(); i++) {
            for (int offset = -2; offset <=2; offset++){
                semiTones.get(i).addName(
                        semiTones.get(
                                ((i + offset) % semiTones.size() + semiTones.size())%semiTones.size()
                        ).getNames().get(0).concat(suffix[offset+2])
                );
            }
        }
    }

    private void bindTones(){
        for (int i = 0; i < semiTones.size(); i++){
            semiTones.get(i).setHigher(semiTones.get(((i + 1) % 12 + 12) % 12));
            semiTones.get(i).setLower(semiTones.get(((i-1)%12+12)%12));
        }
    }

    /*Given a frequency, it tries to determine which valid tone is closest to it and whether the supplied frequency is undertuned or overtuned.
    * @return A tuple in which the first element is the found tone and the second element is the direction*/
    public Tuple<String, String> extractToneFromFrequency(double frequency){
        double lowerDiff=Double.MAX_VALUE;
        double upperDiff=Double.MAX_VALUE;
        double lower=-1;
        if (allSemiTones.floorKey(frequency) != null){
            lower = allSemiTones.floorKey(frequency);
            lowerDiff = frequency-lower;
        }
        double upper=-1;
        if (allSemiTones.ceilingKey(frequency) != null){
            upper = allSemiTones.ceilingKey(frequency);
            upperDiff = upper-frequency;
        }
        Tuple<String, String>result = new Tuple<>("","");
        if (lowerDiff < upperDiff){
            if (lowerDiff < precisionIntervals.get(lower).y){
                result.setX(allSemiTones.get(lower));
                result.setY("Precisely at");
            }
            else
            {
                result.setX(allSemiTones.get(lower));
                result.setY("Above");
            }
        }
        else
        {
            if (upperDiff < precisionIntervals.get(upper).x){
                result.setX(allSemiTones.get(upper));
                result.setY("Precisely at");
            }
            else
            {
                result.setX(allSemiTones.get(upper));
                result.setY("Below");
            }
        }
        return result;
    }

    public ArrayList<SemiTone> getSemiTones() {
        return semiTones;
    }

    private void constructScale(String root){
        StringBuilder scaleBuilder = new StringBuilder();
        currentScale = new ArrayList<>();
        currentScale.add(semiTones.get(getSemiTonePosition(root)));
        scaleBuilder.append(root).append(" ");
        int[] steps = new int[] {2,2,1,2,2,2,1};
        int pos = getSemiTonePosition(root);
        for (int i = 0, j=baseTones.indexOf(root.charAt(0))+1; i < 6; i++, j++) {
            SemiTone nextSemiTone = semiTones.get(((pos + steps[i])%12+12)%12);
            for (String nextToneName : nextSemiTone.getNames()){
                if (nextToneName.contains(Character.toString(baseTones.charAt(j%baseTones.length())))){
                    scaleBuilder.append(nextToneName).append(" ");
                    currentScale.add(nextSemiTone);
                    break;
                }
            }
            pos += steps[i];
        }
        scaleBuilder.append(root);
        currentScaleAsString = scaleBuilder.toString();
    }

    public String getScaleText(String root){
        constructScale(root);
        return currentScaleAsString;
    }

    public ArrayList<SemiTone> getScaleTones(String root){
        constructScale(root);
        return currentScale;
    }

    public int getSemiTonePosition(String tone){
        int i = 0;
        for (SemiTone semiTone : semiTones){
            if (semiTone.getNames().contains(tone)) break;
            i++;
        }
        return i;
    }
}
