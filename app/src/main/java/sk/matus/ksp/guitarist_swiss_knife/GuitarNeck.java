package sk.matus.ksp.guitarist_swiss_knife;

import android.content.Context;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;

public class GuitarNeck {

    class StrumThread extends Thread{
        ArrayList<GuitarString>strings;
        int time = 40;
        boolean seqStart = false;
        public StrumThread(ArrayList<GuitarString>strings){
            this.strings = strings;
        }

        @Override
        public void run() {
            for (GuitarString gs : strings){
                if (Thread.interrupted()) return;
                if (gs.pick() && !seqStart){
                    seqStart = true;
                }
                if (!seqStart) continue;
                try {
                    Thread.sleep(time);
                }
                catch(Exception e){
                    e.printStackTrace();
                }
                time ++;
            }
        }
    }

    Context context;
    ArrayList<GuitarString>strings = new ArrayList<>();
    int fretSpan = 3;
    StrumThread strumThread;

    public GuitarNeck(Context context){
        this.context = context;
        for (int i = 0; i < 6; i++){
            strings.add(new GuitarString(context, i));
        }
        strumThread = new StrumThread(strings);
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
        if (strumThread.isAlive()){
            strumThread.interrupt();
        }
        strumThread = new StrumThread(strings);
        strumThread.start();
    }


    public ArrayList<Fingering> findFingerings(HashSet<SemiTone>chord){
        ArrayList<Fingering> result = new ArrayList<>();
        bruteFingerings(chord, result, 0, new ArrayList<Integer>(), new ArrayList<SemiTone>());
        sortFingerings(result);
        return result;
    }

    private void sortFingerings(ArrayList<Fingering>fingerings){
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
        if (chord.contains(newTone)){
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
