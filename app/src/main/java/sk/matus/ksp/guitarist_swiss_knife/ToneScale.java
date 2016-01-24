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

public class ToneScale {
    TreeMap<Double,String>allTones = new TreeMap<>();
    HashMap<Double,Tuple<Double,Double>>precisionIntervals = new HashMap<>();
    ArrayList<String>octave = new ArrayList<>();

    /*
    * OnCreate a list containing the basic octave tones is read*/
    public ToneScale(Context context){
        InputStream io = context.getResources().openRawResource(R.raw.base_tones);
        try {
            readJsonStream(io);
        }
        catch (IOException e){}
        finally {
        }
        expandAll();
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
        String name = "";
        while (reader.hasNext()) {
            name = reader.nextString();
            octave.add(name);
        }
        reader.endArray();
    }

    /*
    * A procedure to expand the basic octave into full scale of 8 octaves
    * Fills the balanced binary tree whose keys are frequencies and values are scientifical names of the tones.
    * In addition, it calculates the maximum precision margins (ranges in which the frequency is associated with the tone itself)
    * both for undertuned and overtuned frequency.*/
    private void expandAll(){
        double previousFrequency = 0;
        for (int semitone = 0; semitone<108; semitone++){
            double frequency = Math.pow((double)2, ((double)(semitone - 57) / (double)octave.size()))*440;
            double nextFrequency = Math.pow((double)2, ((double)(semitone - 56) / (double)octave.size()))*440;
            String name = octave.get(semitone%octave.size())+"_"+semitone/octave.size();
            allTones.put(frequency, name);
            double lowerMargin = (frequency - previousFrequency)/4;
            double upperMargin = (nextFrequency- frequency)/4;
            precisionIntervals.put(frequency,new Tuple(lowerMargin,upperMargin));
            previousFrequency = frequency;
            Log.i("TONE",frequency+name);
        }

        for(TreeMap.Entry<Double,String> entry : allTones.entrySet()) {
            Log.i(Double.toString(entry.getKey()),entry.getValue());
        }
    }

    /*Given a frequency, it tries to determine which valid tone is closest to it and whether the supplied frequency is undertuned or overtuned.
    * @return A tuple in which the first element is the found tone and the second element is the direction*/
    public Tuple<String, String> extractNote(double frequency){
        double lowerDiff=Double.MAX_VALUE;
        double upperDiff=Double.MAX_VALUE;
        double lower=-1;
        if (allTones.floorKey(frequency) != null){
            lower = allTones.floorKey(frequency);
            lowerDiff = frequency-lower;
        }
        double upper=-1;
        if (allTones.ceilingKey(frequency) != null){
            upper = allTones.ceilingKey(frequency);
            upperDiff = upper-frequency;
        }
        if (lowerDiff < upperDiff){
            if (lowerDiff < precisionIntervals.get(lower).y) return new Tuple(allTones.get(lower),"Precisely at");
            return new Tuple(allTones.get(lower),"Above");
        } else {
            if (upperDiff < precisionIntervals.get(upper).x) return new Tuple(allTones.get(upper),"Precisely at");
            return new Tuple(allTones.get(upper),"Below");
        }
    }





}
