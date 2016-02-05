package sk.matus.ksp.guitarist_swiss_knife;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;

/**
 * Created by whiskas on 30.1.2016.
 */
public class GuitarNeck {

    class StrumThread extends Thread{
        ArrayList<GuitarString>strings;
        int time = 75;
        public StrumThread(ArrayList<GuitarString>strings){
            super();
            this.strings = strings;
        }

        @Override
        public void run() {
            for (GuitarString gs : strings){
                gs.pick();
                try {
                    Thread.sleep(time);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                time -=2;
            }
        }
    }

    Context context;
    ArrayList<GuitarString>strings = new ArrayList<>();
    int fretSpan = 3;

    public GuitarNeck(Context context){
        this.context = context;
        for (int i = 0; i < 6; i++){
            strings.add(new GuitarString(context, i));
        }
    }

    public void setTuning(ArrayList<SemiTone>tuning){
        for (int i = 0; i < 6; i++){
            strings.get(i).setOpenTone(tuning.get(i));
        }
    }

    public void strum(Fingering fingering){
        if (fingering == null) return;
        for (int i = 0; i < fingering.fingering.size(); i++){
            strings.get(i).setFret(fingering.fingering.get(i));
        }
        StrumThread strumThread = new StrumThread(strings);
        strumThread.run();
    }


    public ArrayList<Fingering> findFingerings(HashSet<SemiTone>chord){
        ArrayList<Fingering> result = new ArrayList<>();
        bruteFingerings(chord, result, 0, new ArrayList<Integer>(), new ArrayList<SemiTone>());
        sortFingerings(result);
        for (Fingering f : result) System.out.println(f);
        return result;
    }

    private void sortFingerings(ArrayList<Fingering>fingerings){
        ArrayList<Fingering>result = new ArrayList<>();
        for (Fingering f : fingerings){
            f.rating = rateFingering(f);
        }
        Collections.sort(fingerings);
    }

    private int rateFingering(Fingering fingering){
        return openStrings(fingering)*2 + continuousSeqLen(fingering)*2 - jumps(fingering)*3 - neckDistance(fingering);
    }

    private int neckDistance(Fingering fingering){
        int min = Integer.MAX_VALUE;
        for (Integer i : fingering.fingering){
            if (i <= 0) continue;
            min = Math.min(min,i);
        }
        return min;
    }

    private int jumps(Fingering fingering){
        int result=0;
        boolean b = false;
        for (Integer i : fingering.fingering){
            if (i == -1 && b){
                result++;
            } else b=true;
        }
        return result;
    }

    private int openStrings(Fingering fingering){
        int result = 0;
        for (Integer i : fingering.fingering){
            if (i==0) result++;
        }
        return result;
    }

    private int continuousSeqLen(Fingering f){
        int max = 0;
        int gathered = 0;
        for (int i = 0; i < f.fingering.size(); i++){
            if (f.fingering.get(i) == -1){
                max = Math.max(gathered,max);
                gathered = 0;
            } else gathered++;
        }
        max = Math.max(gathered,max);
        return max;
    }

    private boolean isComplete(ArrayList<SemiTone>tones,HashSet<SemiTone>chord){
        for (SemiTone st : chord){
            if (!tones.contains(st)) return false;
        }
        return true;
    }

    private void bruteFingerings(HashSet<SemiTone> chord,ArrayList<Fingering>found, int stringIndex, ArrayList<Integer>currentFingers, ArrayList<SemiTone>currentTones){
        if (stringIndex > 5){
            if (isComplete(currentTones, chord)){
                ArrayList<Integer>newFingering = new ArrayList<>();
                for (Integer i : currentFingers){
                    newFingering.add(i);
                }
                Fingering fingering = new Fingering(newFingering);
                found.add(fingering);
            }
            return;
        }
        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;
        for (Integer i : currentFingers){
            if (i<=0) continue;
            min = Math.min(min,i);
            max = Math.max(max,i);
        }
        int lowerBound = 1;
        int upperBound = 15;
        if (min != Integer.MAX_VALUE) lowerBound = Math.max(1,min-fretSpan);
        if (max != Integer.MIN_VALUE) upperBound = Math.min(15, max + fretSpan);
        SemiTone newTone;
        for (int i = lowerBound; i < upperBound; i++){

            strings.get(stringIndex).setFret(i);
            newTone = strings.get(stringIndex).getSemiTone();
            if (chord.contains(newTone)){
                currentTones.add(newTone);
                currentFingers.add(i);
                bruteFingerings(chord,found,stringIndex+1,currentFingers,currentTones);
                currentFingers.remove(currentFingers.size()-1);
                currentTones.remove(currentTones.size()-1);
            }
        }
        strings.get(stringIndex).setFret(0);
        newTone = strings.get(stringIndex).getSemiTone();
        boolean openString = false;
        if (chord.contains(newTone)){
            openString = true;
            currentTones.add(newTone);
            currentFingers.add(0);
            bruteFingerings(chord,found,stringIndex+1,currentFingers,currentTones);
            currentFingers.remove(currentFingers.size()-1);
            currentTones.remove(currentTones.size()-1);
        }
        currentFingers.add(-1);
        bruteFingerings(chord,found,stringIndex+1,currentFingers,currentTones);
        currentFingers.remove(currentFingers.size()-1);
    }
}
