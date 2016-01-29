package sk.matus.ksp.guitarist_swiss_knife;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.EditText;

import java.util.Arrays;

/**
 * The class that is partially responsible for the UI of the tuner app component. The structure is inspired by some of the tutorials
 * regarding the topic of high-performance canvas handling (e.g. for games)
 */
public class EqualizerView extends SurfaceView implements SurfaceHolder.Callback {
    private SurfaceHolder sh;
    UpdateThread updateThread;
    Rect canvasDimensions;
    Paint wavePaint = new Paint();
    Paint freqPaint = new Paint();
    int backgroundColor;
    double[] freqData;
    double currentFreq = 0;
    String currentTone = "";
    String currentDirection = "";

    public EqualizerView(Context context) {
        super(context);
        init(context);
    }

    public EqualizerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public EqualizerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    /*
    * Method instantiates the necessary variables such as the drawing surface and paint objects.
    * @param context A context to use for accessing resources*/
    private void init(Context context){
        sh = getHolder();
        sh.addCallback(this);
        wavePaint.setColor(context.getResources().getColor(R.color.colorKSPGreen));
        freqPaint.setColor(context.getResources().getColor(R.color.colorKSPGreen));
        freqPaint.setTextSize(40);
        backgroundColor = context.getResources().getColor(R.color.colorActivityBackground);
    }

    /*
    * Method that creates an updateThread for the canvas surface when it is created*/
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

    /*
    * Method that kills the updateThread when the canvas surface no longer exists*/
    public void surfaceDestroyed(SurfaceHolder holder) {
        updateThread.setRunning(false);
    }

    /*Method that updates the content of the canvas.
    * @param canvas A canvas to be drawn to*/
    public void doDraw(Canvas canvas) {
        if ((canvas==null) || (freqData==null)) return;
        canvas.drawColor(backgroundColor);
        drawEqualizer(canvas);
        drawEstimation(canvas);

        /*
        int yOffset = canvasDimensions.height()/2;
        for (int i = 0; i < freqData.length; i+=4) {
            int x = i/4;
            int y =(int)(yOffset - freqData[i]*50000);
            canvas.drawLine(x,yOffset,x,y,wavePaint);
            if (i%400==0) canvas.drawText(Integer.toString(i*11025  /16384),x,yOffset+20,wavePaint);
        }*/
    }

    /*Method draws the visual representation of the recorded sound
    * The visualisation is a graph where the y-axis is logarithmically scaled amplitude and x-axis is
    * the frequency of the sound.
    * @param canvas The canvas to be drawn to*/
    private void drawEqualizer(Canvas canvas){
        int baseLineY = (int)(canvasDimensions.height()*0.8);
        for (int i = 0, x=10;  i < freqData.length; i+=freqData.length/(canvasDimensions.width()-20), x++){
            int amplitudePeak = (int)(baseLineY - Math.max(4/baseLineY,Math.log(freqData[i]*10000))*baseLineY/4);
            canvas.drawLine(x,baseLineY,x,amplitudePeak,wavePaint);
        }
    }

    /*
    * Method draws the current prevalent frequency and a tone estimation for it
    * @param canvas The canvas to be drawn to*/
    private void drawEstimation(Canvas canvas){
        int x = (int)(canvasDimensions.width()*0.1);
        canvas.drawText(String.format("Current frequency is %.2f Hz",currentFreq),x,(int)(canvasDimensions.height()*0.05), freqPaint);
        canvas.drawText(String.format("%s %s", currentDirection, currentTone), x, (int) (canvasDimensions.height() * 0.05)+freqPaint.getTextSize()+5,freqPaint);
    }

    public void updateWaves(double[] data){
        this.freqData = Arrays.copyOf(data,data.length);
    }

    public void updateFreq(double freq){
        this.currentFreq = freq;
    }

    public void updateTone(String tone, String tuningDirection){
        this.currentTone = tone;
        this.currentDirection = tuningDirection;
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
