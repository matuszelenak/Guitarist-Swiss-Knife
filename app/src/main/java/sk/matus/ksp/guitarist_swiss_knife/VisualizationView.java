package sk.matus.ksp.guitarist_swiss_knife;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.Arrays;

/**
 * The class that is responsible for the UI of the tuner app component. The structure is inspired by some of the tutorials
 * regarding the topic of high-performance canvas handling (e.g. for games)
 */
public class VisualizationView extends SurfaceView implements SurfaceHolder.Callback {
    private SurfaceHolder sh;
    UpdateThread updateThread;
    Rect canvasDimensions;
    Paint wavePaint = new Paint();
    double[] freqData;
    int currentFreq = 0;

    public VisualizationView(Context context) {
        super(context);
        sh = getHolder();
        sh.addCallback(this);
        wavePaint.setColor(Color.CYAN);
    }
    public void surfaceCreated(SurfaceHolder holder) {
        Canvas canvas = sh.lockCanvas();
        sh.unlockCanvasAndPost(canvas);
        updateThread = new UpdateThread(getHolder());
        canvasDimensions = holder.getSurfaceFrame();
        updateThread.setRunning(true);
        updateThread.start();
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        updateThread.setRunning(false);
    }

    /*Redrawing the content of canvas*/
    public void doDraw(Canvas canvas) {
        if ((canvas==null) || (freqData==null)) return;
        canvas.drawColor(Color.BLACK);
        canvas.drawText("Current frequency: "+currentFreq,50,50,wavePaint);
        int yOffset = canvasDimensions.height()/2;
        for (int i = 0; i < freqData.length; i+=4) {
            int x = i/4;
            int y =(int)(yOffset - freqData[i]*50000);
            canvas.drawLine(x,500,x,y,wavePaint);
            if (i%400==0) canvas.drawText(Integer.toString(i*11025/16384),x,yOffset+20,wavePaint);
        }
    }

    public void updateWaves(double[] data){
        this.freqData = Arrays.copyOf(data,data.length);
    }

    public void updateFreq(int freq){
        this.currentFreq = freq;
    }

    /* Thread that permanently updates the surfaceView component*/
    class UpdateThread extends Thread {

        private SurfaceHolder surfaceHolder;
        private boolean runFlag = false;

        public UpdateThread(SurfaceHolder surfaceHolder) {
            this.surfaceHolder = surfaceHolder;
        }

        public void setRunning(boolean run) {
            this.runFlag = run;
        }

        @Override
        public void run() {
            Canvas c;
            while (this.runFlag) {
                c = null;
                try {

                    c = this.surfaceHolder.lockCanvas(null);
                    synchronized (this.surfaceHolder) {
                        doDraw(c);      //redraw the canvas
                    }
                } finally {

                    if (c != null) {
                        this.surfaceHolder.unlockCanvasAndPost(c);

                    }
                }
            }
        }

    }
}
