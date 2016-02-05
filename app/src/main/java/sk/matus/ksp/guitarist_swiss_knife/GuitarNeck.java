package sk.matus.ksp.guitarist_swiss_knife;

import android.content.Context;
import android.media.MediaPlayer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;

/**
 * Created by whiskas on 30.1.2016.
 */
public class GuitarNeck {
    class GuitarString{
        MediaPlayer player;
        int stringIndex;
        int currentFret;
        Context context;
        SemiTone startTone;
        public GuitarString(Context context,int index){
            this.context = context;
            this.stringIndex = index;
        }

        public void setOpenTone(SemiTone tone){
            this.startTone = tone;
        }

        public void setFret(int fretIndex){
            currentFret = fretIndex;
        }

        public SemiTone getSemiTone(){
            if (currentFret == -1) return null;
            SemiTone result = startTone;
            for (int i = 0; i < currentFret; i++){
                result = result.getHigher();
            }
            return result;
        }

        public void pick(){
            if (currentFret == -1) return;
            System.out.println(String.format("guitar_%d_%d", stringIndex, currentFret));
            int id = context.getResources().getIdentifier(String.format("guitar_%d_%d", stringIndex, currentFret), "raw", context.getPackageName());
            player = MediaPlayer.create(context,id);
            player.start();
            player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    mp.stop();
                    mp.release();
                }
            });
        }
    }

    class Fingering{
        ArrayList<Integer>fingering = new ArrayList<>();
        public Fingering(ArrayList<Integer>fingering){
            this.fingering = fingering;
        }
        @Override
        public String toString(){
            StringBuilder sb = new StringBuilder();
            for (Integer i : fingering){
                if (i == -1) sb.append('x');
                    else sb.append(i);
            }
            return sb.toString();
        }
    }

    Context context;
    ArrayList<GuitarString>strings = new ArrayList<>();

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
            strings.get(i).pick();
        }
    }


    public ArrayList<Fingering> findFingerings(HashSet<SemiTone>chord){
        ArrayList<Fingering> result = new ArrayList<>();
        bruteFingerings(chord,result,0,new ArrayList<Integer>(),new ArrayList<SemiTone>());
        return result;
    }

    private boolean isComplete(ArrayList<SemiTone>tones,HashSet<SemiTone>chord){
        for (SemiTone st : chord){
            if (!tones.contains(st)) return false;
        }
        return true;
    }

    private void bruteFingerings(HashSet<SemiTone> chord,ArrayList<Fingering>found, int stringIndex, ArrayList<Integer>currentFingers, ArrayList<SemiTone>currentTones){
        if (stringIndex == 6){
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
        for (int i = -1; i < 4; i++){
            strings.get(stringIndex).setFret(i);
            SemiTone newTone = strings.get(stringIndex).getSemiTone();
            if (chord.contains(newTone)){
                currentTones.add(newTone);
                currentFingers.add(i);
                bruteFingerings(chord,found,stringIndex+1,currentFingers,currentTones);
                currentFingers.remove(currentFingers.size()-1);
                currentTones.remove(currentTones.size()-1);
            }
            if (newTone == null){
                currentFingers.add(i);
                bruteFingerings(chord,found,stringIndex+1,currentFingers,currentTones);
                currentFingers.remove(currentFingers.size()-1);
            }
        }
    }
}
