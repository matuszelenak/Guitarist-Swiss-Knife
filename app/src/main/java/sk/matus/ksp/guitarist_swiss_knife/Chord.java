package sk.matus.ksp.guitarist_swiss_knife;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

/**
 * Created by whiskas on 26.1.2016.
 */
public class Chord {
    ArrayList<SemiTone>scale;
    ToneScale toneScale;
    String root = "C";
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

    public Chord(ToneScale toneScale){
        this.toneScale = toneScale;
    }

    public void setScale(ArrayList<SemiTone>scale){
        this.scale = scale;
    }

    public void setRoot(String root){
        this.root = root;
    }

    private void collectTones(){
        chord.clear();
        chord.put(scale.get(0),true);
        switch(type){
            case "Major": chord.put(scale.get(2),true); break;
            case "minor": chord.put(scale.get(2).getLower(),true); break;
        }
        switch (fifth){
            case "5": chord.put(scale.get(4),true); break;
            case "5#" : chord.put(scale.get(4).getHigher(),true); break;
            case "5b": chord.put(scale.get(4).getLower(),true); break;
        }
        switch (seventh){
            case "7": chord.put(scale.get(6).getLower(),true); break;
            case "7M" : chord.put(scale.get(6),true); break;
            case "6": chord.put(scale.get(5),true); break;
        }
    }
    public String getProgression(){
        collectTones();
        StringBuilder sb = new StringBuilder();
        int i = toneScale.getTonePosition(root);
        int count = 0;
        while (count < 12){
            if (chord.containsKey(toneScale.semiTones.get(i % 12))){
                sb.append(toneScale.semiTones.get(i%12).getNames().get(0)).append(" ");
            }
            i++;
            count++;
        }
        return sb.toString();
    }

}
