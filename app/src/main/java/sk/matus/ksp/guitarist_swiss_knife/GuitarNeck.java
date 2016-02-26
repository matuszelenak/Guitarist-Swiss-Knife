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
            fret.getPaint().setColor(Color.GRAY);
            fretLines.add(fret);
        }
        for (int i = 0; i < 6; i++){
            ShapeDrawable string = new ShapeDrawable(new RectShape());
            string.getPaint().setColor(Color.LTGRAY);
            ShapeDrawable finger = new ShapeDrawable(new OvalShape());
            finger.getPaint().setColor(Color.WHITE);
            stringLines.add(string);
            fingerMarkers.add(finger);
        }
    }

    public void setTuning(ArrayList<Tone>tuning){
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

    public ArrayList<Fingering> findFingerings(HashSet<Tone>chord){
        HashSet<Fingering> result = new HashSet<>();
        long startTime = System.nanoTime();
        bruteFingerings(chord, result, 0, new ArrayList<Integer>(), new ArrayList<Tone>());
        long difference = System.nanoTime() - startTime;
        ArrayList<Fingering>sorted = new ArrayList<>(result);
        sortFingerings(sorted);
        Log.i("Fingerings took", Double.toString(difference / 1000000.0));
        return sorted;
    }

    private void sortFingerings(ArrayList<Fingering>fingerings){
        Collections.sort(fingerings);
    }

    private void bruteFingerings(HashSet<Tone> chord,
                                 HashSet<Fingering>found,
                                 int stringIndex,
                                 ArrayList<Integer>currentFingers,
                                 ArrayList<Tone>currentTones
    ){
        if (stringIndex > 5){
            if (currentTones.containsAll(chord)){
                ArrayList<Integer>newFingering = new ArrayList<>();
                for (Integer i : currentFingers){
                    newFingering.add(i);
                }
                Fingering fingering = new Fingering(newFingering);
                ArrayList<Tone>tones = new ArrayList<>();
                for (Tone tone : currentTones){
                    tones.add(tone);
                }
                fingering.setTones(tones);
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
        if (max != Integer.MIN_VALUE) upperBound = Math.min(9, max + fretSpan); else upperBound = 15;
        Tone newTone;
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
            bruteFingerings(chord, found, stringIndex + 1, currentFingers, currentTones);
            currentFingers.remove(currentFingers.size() - 1);
            currentTones.remove(currentTones.size()-1);
        }
        currentFingers.add(-1);
        currentTones.add(null);
        bruteFingerings(chord, found, stringIndex + 1, currentFingers, currentTones);
        currentTones.remove(currentTones.size()-1);
        currentFingers.remove(currentFingers.size()-1);
    }

    public Drawable renderFingering(Fingering fingering, int width){
        Bitmap bitmap = Bitmap.createBitmap(width, width*8, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(Color.rgb(0x30,0x30,0x30));
        Paint p = new Paint();
        p.setColor(Color.WHITE);
        int textSize = width / 10;
        p.setTextSize(textSize);
        p.setAntiAlias(true);
        int textOffset = (int)p.measureText("666");
        int neckWidth = width - textOffset - 5;
        int radius = (width-textOffset)/12-1;
        int headerHeight = textSize + width/20;
        int x = textOffset;
        int y = headerHeight;
        int fretThickness = neckWidth/32;
        for (int i = 0; i < fretLines.size(); i++){
            int textWidth = (int)p.measureText(Integer.toString(i));
            fretLines.get(i).setBounds(x, y, x + neckWidth, y + fretThickness);
            fretLines.get(i).draw(canvas);
            canvas.drawText(Integer.toString(i), (textOffset - textWidth) / 2, y + textSize / 2, p);
            y+=width/3;
        }
        int stringThickness = neckWidth/48;
        for (int i = 0; i < stringLines.size(); i++){
            x = textOffset+i*neckWidth/6+(neckWidth/12);
            stringLines.get(i).setBounds(x - stringThickness / 2, headerHeight, x + stringThickness / 2, canvas.getHeight());
            stringLines.get(i).draw(canvas);
            stringThickness = (int)Math.max(stringThickness*0.97,2.0);
        }
        Bitmap header = Bitmap.createBitmap(width, headerHeight, Bitmap.Config.ARGB_8888);
        Canvas headerCanvas = new Canvas(header);
        headerCanvas.drawColor(Color.rgb(0x30,0x30,0x30));
        ArrayList<String>headerData = fingering.getHeaderData();
        for (int i = 0; i < fingering.getFingering().size(); i++){
            x = textOffset+i*neckWidth/6+(neckWidth/12);
            if (fingering.getFingering().get(i)>0){
                int position = fingering.getFingering().get(i);
                fingerMarkers.get(i).setBounds(x - radius, headerHeight + position * width / 3 - radius, x + radius, headerHeight + position * width / 3 + radius);
                fingerMarkers.get(i).draw(canvas);

            }
            String s = headerData.get(i);
            headerCanvas.drawText(s,x-(int)p.measureText(s)/2,headerHeight-width/40,p);
        }
        Bitmap resized = Bitmap.createBitmap(bitmap,0,headerHeight+Math.max((int)Math.round((fingering.neckDistance()-0.5)*width/3),0),width, 4*width/3);
        Bitmap combined = Bitmap.createBitmap(width,headerHeight+resized.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas combinedCanvas = new Canvas(combined);
        combinedCanvas.drawBitmap(header,0f,0f,null);
        combinedCanvas.drawBitmap(resized,0f,headerCanvas.getHeight(),null);
        return new BitmapDrawable(combined);
    }

    public Drawable renderFingeringThumbnail(Fingering fingering, int width){
        Bitmap bitmap = Bitmap.createBitmap(width, width*7, Bitmap.Config.ARGB_8888);
        Canvas myCanvas = new Canvas(bitmap);
        myCanvas.drawColor(Color.DKGRAY);
        int radius = (width)/12;
        int x = 0;
        int y = 0;
        int fretThickness = width/32;
        for (int i = 0; i < fretLines.size(); i++){
            fretLines.get(i).setBounds(x, y, width, y + fretThickness);
            fretLines.get(i).draw(myCanvas);
            y+=width/3;
        }
        int stringThickness = width/48;
        for (int i = 0; i < stringLines.size(); i++){
            x = i*width/6+(width/12-stringThickness/2);
            stringLines.get(i).setBounds(x, 0, x + stringThickness, myCanvas.getHeight());
            stringLines.get(i).draw(myCanvas);
            stringThickness = (int)Math.max(stringThickness*0.97,2.0);
        }
        for (int i = 0; i < fingering.getFingering().size(); i++){
            x = i*width/6+(width/12);
            if (fingering.getFingering().get(i)>0){
                fingerMarkers.get(i).setBounds(x-radius,fingering.getFingering().get(i)*width/3- radius,x+radius,fingering.getFingering().get(i)*width/3+radius);
                fingerMarkers.get(i).draw(myCanvas);
            }
        }
        Bitmap resized = Bitmap.createBitmap(bitmap,0,Math.max((int)Math.round((fingering.neckDistance()-0.5)*width/3),0),width, 4*width/3);
        return new BitmapDrawable(resized);
    }
}
