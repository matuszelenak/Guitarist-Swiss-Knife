package sk.matus.ksp.guitarist_swiss_knife;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.Arrays;
import java.util.Random;

/**
 * Created by whiskas on 22.1.2016.
 */
public class VisualizationView extends SurfaceView implements SurfaceHolder.Callback {
    private SurfaceHolder sh;
    MainThread _thread;
    Rect rect;
    Random r = new Random();
    double[] data;
    public VisualizationView(Context context) {
        super(context);
        sh = getHolder();
        sh.addCallback(this);
    }
    public void surfaceCreated(SurfaceHolder holder) {
        Canvas canvas = sh.lockCanvas();
        sh.unlockCanvasAndPost(canvas);
        _thread = new MainThread(getHolder());
        rect = holder.getSurfaceFrame();
        _thread.setRunning(true);
        _thread.start();
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {
    }
    public void surfaceDestroyed(SurfaceHolder holder) {
        _thread.setRunning(false);
    }

    public void doDraw(Canvas canvas) {
        if ((canvas==null) || (data==null)) return;
        Log.i("DRAW", "Drawing");
        //canvas.drawBitmap(Bitmap.createBitmap(rect.width(),rect.height(),Bitmap.Config.ARGB_8888),0,0,null);
        canvas.drawColor(Color.DKGRAY);
        for (int i = 0; i < data.length; i++) {
            int x = i;
            int downy =(int)(300 - data[i]*100);
            int upy = 100;
            canvas.drawCircle(x, downy, 30f, new Paint(Color.RED));
        }

        //canvas.drawText("WHYYYY", 400, 400, new Paint(Color.WHITE));
        //Log.i("DRAW", "Drawing");
    }

    public void update(double[] data){
        //Log.i("UPDATE","Updated data in view");
        this.data = Arrays.copyOf(data,data.length);
        Log.i("SUM2",Double.toString(sum(this.data)));
    }

    public double sum(double[] a){
        double res = 0;
        for (int i = 0; i < a.length; i++) res+=a[i];
        return res;
    }

    class MainThread extends Thread {

        private SurfaceHolder surfaceHolder;
        private boolean runFlag = false;

        public MainThread(SurfaceHolder surfaceHolder) {
            this.surfaceHolder = surfaceHolder;
        }

        public void setRunning(boolean run) {
            this.runFlag = run;
        }

        @Override
        public void run() {
            Canvas c;
            Log.i("RUN","Thread Running");
            while (this.runFlag) {

                c = null;
                try {

                    c = this.surfaceHolder.lockCanvas(null);
                    synchronized (this.surfaceHolder) {
                        doDraw(c);
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
