package sk.matus.ksp.guitarist_swiss_knife;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.graphics.drawable.shapes.RectShape;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;

public class GuitarNeck {

	private static final int fretSpan = 3;

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

    private Context context;
    private ArrayList<GuitarString>strings = new ArrayList<>();
    private StrumThread strumThread;
    private ArrayList<ShapeDrawable>fretLines = new ArrayList<>();
    private ArrayList<ShapeDrawable>stringLines = new ArrayList<>();
    private ArrayList<ShapeDrawable>fingerMarkers = new ArrayList<>();

    public GuitarNeck(Context context){
        this.context = context;
        for (int i = 0; i < 6; i++){
            strings.add(new GuitarString(context, i, 19));
        }
        strumThread = new StrumThread(strings);
        initVisualComponents();
    }

    private void initVisualComponents(){
        ShapeDrawable nut = new ShapeDrawable(new RectShape());
        fretLines.add(nut);
        for (int i = 0; i <19; i++){
            ShapeDrawable fret = new ShapeDrawable(new RectShape());
            fret.getPaint().setColor(Color.WHITE);
            fretLines.add(fret);
        }
        for (int i = 0; i < 6; i++){
            ShapeDrawable string = new ShapeDrawable(new RectShape());
            string.getPaint().setColor(Color.WHITE);
            ShapeDrawable finger = new ShapeDrawable(new OvalShape());
            finger.getPaint().setColor(Color.WHITE);
            stringLines.add(string);
            fingerMarkers.add(finger);
        }
    }

    public void setTuning(ArrayList<SemiTone>tuning){
        for (int i = 0; i < 6; i++){
            strings.get(i).setOpenTone(tuning.get(i));
        }
    }

    public void strum(Fingering fingering){
        if (fingering == null) return;
        for (int i = 0; i < fingering.getFingering().size(); i++){
            strings.get(i).setFret(fingering.getFingering().get(i));
        }
        if (strumThread.isAlive()){
            strumThread.interrupt();
        }
        strumThread = new StrumThread(strings);
        strumThread.start();
    }


    public ArrayList<Fingering> findFingerings(HashSet<SemiTone>chord){
        ArrayList<Fingering> result = new ArrayList<>();
        long startTime = System.nanoTime();
        bruteFingerings(chord, result, 0, new ArrayList<Integer>(), new ArrayList<SemiTone>());
        long difference = System.nanoTime() - startTime;
        Log.i("Fingerings took", Double.toString(difference / 1000000.0));
        sortFingerings(result);
        return result;
    }

    private void sortFingerings(ArrayList<Fingering>fingerings){
        Collections.sort(fingerings);
    }

    private boolean isComplete(ArrayList<SemiTone>tones,HashSet<SemiTone>chord){
        for (SemiTone st : chord){
            if (!tones.contains(st)) return false;
        }
        return true;
    }

    private void bruteFingerings(HashSet<SemiTone> chord,
                                 ArrayList<Fingering>found,
                                 int stringIndex,
                                 ArrayList<Integer>currentFingers,
                                 ArrayList<SemiTone>currentTones
    ){
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
        int lowerBound, upperBound;
        if (min != Integer.MAX_VALUE) lowerBound = Math.max(1,min-fretSpan); else lowerBound = 1;
        if (max != Integer.MIN_VALUE) upperBound = Math.min(15, max + fretSpan); else upperBound = 15;
        SemiTone newTone;
        for (int i = lowerBound; i < upperBound; i++){
            strings.get(stringIndex).setFret(i);
            newTone = strings.get(stringIndex).getTone();
            if (chord.contains(newTone)){
                currentTones.add(newTone);
                currentFingers.add(i);
                bruteFingerings(chord,found,stringIndex+1,currentFingers,currentTones);
                currentFingers.remove(currentFingers.size()-1);
                currentTones.remove(currentTones.size()-1);
            }
        }
        strings.get(stringIndex).setFret(0);
        newTone = strings.get(stringIndex).getTone();
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

    public Drawable renderFingering(Fingering fingering, int width){
        int canvasWidth = width;
        Bitmap bitmap = Bitmap.createBitmap(canvasWidth, canvasWidth*21/3, Bitmap.Config.ARGB_8888);
        Canvas myCanvas = new Canvas(bitmap);
        myCanvas.drawColor(Color.DKGRAY);
        Paint p = new Paint();
        p.setColor(Color.WHITE);
        p.setTextSize(width / 8);
        int textOffset = (int)p.measureText("88")+10;
        int radius = (width-textOffset)/12-1;
        int x = textOffset;
        int y = 0;
        int fretThickness = width/50;
        for (int i = 0; i < fretLines.size(); i++){
            fretLines.get(i).setBounds(x, y, canvasWidth, y + fretThickness);
            fretLines.get(i).draw(myCanvas);
            myCanvas.drawText(Integer.toString(i),5,y+10,p);
            y+=canvasWidth/3;
        }
        x = textOffset;
        int stringThickness = width/70;
        for (int i = 0; i < stringLines.size(); i++){
            stringLines.get(i).setBounds(x,0,x+stringThickness,myCanvas.getHeight());
            stringLines.get(i).draw(myCanvas);
            x+=(canvasWidth-textOffset-2*radius)/(stringLines.size()-1);
            stringThickness = (int)Math.max(stringThickness*0.95,2.0);
        }
        x = textOffset;
        for (int i = 0; i < fingering.getFingering().size(); i++){
            if (fingering.getFingering().get(i)>0){
                fingerMarkers.get(i).setBounds(x-radius,fingering.getFingering().get(i)*canvasWidth/3- radius,x+radius,fingering.getFingering().get(i)*canvasWidth/3+radius);
                fingerMarkers.get(i).draw(myCanvas);
            }
            x+=(canvasWidth-textOffset-2*radius)/(stringLines.size()-1);
        }
        Bitmap resized = Bitmap.createBitmap(bitmap,0,(int)Math.round((fingering.neckDistance()-0.5)*canvasWidth/3),canvasWidth, 4*canvasWidth/3);
        return new BitmapDrawable(resized);
    }
}
