package sk.matus.ksp.guitarist_swiss_knife;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.SurfaceTexture;
import android.view.TextureView;
import android.view.TextureView.SurfaceTextureListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import java.util.Arrays;

/**
 * A kind of tuner visualization that shows the frequency on the gauge
 * where the gauge arm deviation describes the frequency deviation from the nearest
 * clear tone.
 */
public class GaugeVisualisation extends LinearLayout implements TunerVisualisation{
    private TextureView mTextureView;
    private RenderThread mThread;
    Context context;
    private int mWidth;
    private int mHeight;
    private int backgroundColor;
    /**
     * An array containing the current block of samples processed by FFT.
     */
    private double[] freqData;
    /**
     * Self-descriptive, contains the current frequency taken from freqData
     */
    private double currentFreq = 0;
    /**
     * Contains the string representation of the tone which corresponds to currentFreq
     */
    private Tone currentTone;

    public GaugeVisualisation(Context context){
        super(context);
        this.context = context;
        backgroundColor = context.getResources().getColor(R.color.colorActivityBackground);
        mTextureView = new TextureView(context);
        mTextureView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        mTextureView.setSurfaceTextureListener(new CanvasListener());
        mTextureView.setOpaque(false);
        setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        this.addView(mTextureView);
    }

    /**
     * Method that updates the content of the canvas.
     * @param canvas A canvas to be drawn to*/
    public void doDraw(Canvas canvas) {
        if ((canvas==null) || (freqData==null)) return;
        canvas.drawColor(backgroundColor);
        drawDial(canvas);
        drawIndicator(canvas);
        drawEstimation(canvas);
    }

    private double dialOuterRadius;
    private double dialInnerRadius;
    private double centerX;
    private double centerY;
    private void drawDial(Canvas canvas){
        Paint p = new Paint();
        p.setAntiAlias(true);
        double angle = 90;
        for (int i = 0; i<20; i++){
            double innerMarkerRadius = dialInnerRadius;
            double outerMarkerRadius;
            if (i % 5 == 0){
                p.setStrokeWidth(4);
                p.setColor(Color.WHITE);
                outerMarkerRadius = dialOuterRadius;
            } else{
                p.setStrokeWidth(2);
                p.setColor(Color.LTGRAY);
                double len = (dialOuterRadius - dialInnerRadius)*0.7;
                outerMarkerRadius = dialOuterRadius-len/2;
            }
            double rad = (angle-i*(2.5))*Math.PI/180;
            int outerX = (int)(centerX - outerMarkerRadius*Math.cos(rad));
            int outerY = (int)(centerY - outerMarkerRadius*Math.sin(rad));
            int innerX = (int)(centerX - innerMarkerRadius*Math.cos(rad));
            int innerY = (int)(centerY - innerMarkerRadius*Math.sin(rad));
            canvas.drawLine(outerX, outerY, innerX, innerY,p);
            rad = (angle+i*(2.5))*Math.PI/180;
            outerX = (int)(centerX - outerMarkerRadius*Math.cos(rad));
            outerY = (int)(centerY - outerMarkerRadius*Math.sin(rad));
            innerX = (int)(centerX - innerMarkerRadius*Math.cos(rad));
            innerY = (int)(centerY - innerMarkerRadius*Math.sin(rad));
            canvas.drawLine(outerX, outerY, innerX, innerY,p);
        }
    }

    private double indicatorRadius;
    private double oldAngle = 90;
    private void drawIndicator(Canvas canvas){
        double angle = oldAngle;
        if (steps > 0) angle+=correctionStep;
        Paint p = new Paint();
        p.setAntiAlias(true);
        p.setStrokeWidth(5);
        p.setColor(context.getResources().getColor(R.color.colorKSPGreen));
        int indicatorX = (int)(centerX - indicatorRadius*Math.cos(angle*Math.PI/180));
        int indicatorY = (int)(centerY - indicatorRadius*Math.sin(angle*Math.PI/180));
        canvas.drawLine((int) centerX, (int) centerY, indicatorX, indicatorY, p);
        oldAngle = angle;
        steps--;
    }

    /**
     * Method draws the current prevalent frequency and a tone estimation for it.
     * @param canvas The canvas to be drawn to*/
    private void drawEstimation(Canvas canvas){
        if (currentTone == null) return;
        Paint p = new Paint();
        p.setColor(backgroundColor);
        p.setAntiAlias(true);
        p.setTextSize(mHeight / 10);
        canvas.drawRect((float) (centerX - dialInnerRadius * 0.6), (float) (centerY - indicatorRadius * 0.2), (float) (centerX + dialInnerRadius * 0.6), mHeight, p);
        ToneName name = currentTone.getPrimaryName();
        float textWidth = p.measureText(name.baseName +Integer.toString(name.octave)+name.accidental);
        p.setColor(Color.WHITE);
        canvas.drawText(Character.toString(name.baseName), (mWidth-textWidth)/2, mHeight - p.getTextSize()*2, p);
        canvas.drawText(Integer.toString(name.octave), (mWidth-textWidth)/2 + p.measureText(Character.toString(name.baseName)), mHeight - p.getTextSize()*2 + p.getTextSize()*2/3, p);
        canvas.drawText(name.accidental, (mWidth-textWidth)/2+textWidth-p.measureText(Integer.toString(name.octave)), mHeight - p.getTextSize()*2, p);
    }

    /**
     * Method updates its current freqData with a new block.
     * @param data the new block of data to use
     */
    public void updateSamples(double[] data){
        this.freqData = Arrays.copyOf(data, data.length);
    }

    /**
     * Method updates its currenFreq with a new value.
     * @param freq the new frequency to use.
     */
    public void updateMaxFrequency(double freq){
        this.currentFreq = freq;
    }

    private double correctionStep = 0;
    private int steps;
    public void updateTone(Tone tone){
        this.currentTone = tone;
        double error = currentFreq - tone.getFrequency();
        double lowerLimit = (tone.getFrequencyInterval().x - tone.getFrequency())/2;
        double upperLimit = (tone.getFrequencyInterval().y - tone.getFrequency())/2;
        double angleError;
        if (error > 0){
            angleError = (error /(upperLimit*2))*50;
        } else
        {
            angleError = (-error /(lowerLimit*2))*50;
        }
        double newAngle = Math.min(Math.max(90 + angleError, 40), 140);
        steps = Math.max(2, (int) Math.ceil(Math.log(Math.pow(Math.abs(newAngle - oldAngle), 3))));
        correctionStep = (newAngle - oldAngle) / steps;
        steps++;
    }

    private class RenderThread extends Thread {
        private volatile boolean mRunning = true;

        @Override
        public void run() {

            while (mRunning && !Thread.interrupted()) {
                final Canvas canvas = mTextureView.lockCanvas(null);
                try {
                    doDraw(canvas);
                } finally {
                    mTextureView.unlockCanvasAndPost(canvas);
                }

                try {
                    Thread.sleep(30);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        public void stopRendering() {
            interrupt();
            mRunning = false;
        }

    }

    private class CanvasListener implements SurfaceTextureListener {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface,
                                              int width, int height) {
            mThread = new RenderThread();
            mThread.start();
            mWidth = mTextureView.getWidth();
            mHeight = mTextureView.getHeight();
            if (mWidth > mHeight){
                indicatorRadius = (mHeight*0.8);
            } else {
                indicatorRadius = 3*mWidth/4;
            }
            dialOuterRadius = (indicatorRadius*0.8);
            centerX = mWidth/2;
            centerY = (mHeight-indicatorRadius)/2 + indicatorRadius;
            dialInnerRadius = (dialOuterRadius*0.8);
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            if (mThread != null) {
                mThread.stopRendering();
            }
            return true;
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface,
                                                int width, int height) {
            mWidth = mTextureView.getWidth();
            mHeight = mTextureView.getHeight();
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {
        }
    }
}
