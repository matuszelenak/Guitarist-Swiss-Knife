package sk.matus.ksp.guitarist_swiss_knife;

import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by whiskas on 26.1.2016.
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

    public Chord(ToneUtils toneUtils){
        this.toneUtils = toneUtils;
    }

    public void setScale(ArrayList<SemiTone>scale){
        this.scale = scale;
    }

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
    public String getProgression(String root){
        Log.i("STARTING FROM",root);
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
