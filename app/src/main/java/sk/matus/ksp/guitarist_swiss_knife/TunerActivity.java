package sk.matus.ksp.guitarist_swiss_knife;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.ViewFlipper;

import org.jtransforms.fft.DoubleFFT_1D;

import java.util.ArrayList;
import java.util.Collections;

public class TunerActivity extends AppCompatActivity {

    //constants chosen so that the tradeoff between real-time performance and accuracy of FFT is optimal
    int sampleRate = 11025;
    int channelConfiguration = AudioFormat.CHANNEL_IN_MONO;
    int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;
    int blockSize = 8192;
    private AudioRecord audioRecord;
    private ProcessAudio processTask;
    private boolean started = false;
    private EqualizerView equalizerView;
    private GaugeView gaugeView;
    private DoubleFFT_1D fft = new DoubleFFT_1D(blockSize);
    private ToneUtils toneUtils;
    private int measurementNo = 0;
    private ArrayList<Double>gatheredMaxFreq = new ArrayList<>();
    ViewFlipper viewFlipper;
    private float lastX;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        toneUtils = new ToneUtils(this.getResources());

        viewFlipper = (ViewFlipper) findViewById(R.id.viewFlipper);
        //equalizerView = (EqualizerView) findViewById(R.id.equalizerView);
        //gaugeView = (GaugeView) findViewById(R.id.gaugView);
        equalizerView = new EqualizerView(this);
        equalizerView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        setContentView(equalizerView);
        //setContentView(R.layout.activity_tuner);
        GaugeView gaugeView = new GaugeView(this);
    }

    /*mostly taken from http://stackoverflow.com/questions/5511250/capturing-sound-for-analysis-and-visualizing-frequencies-in-android
    *
    * A background task that reads blocks of @blockSize audio samples at specified @sampleRate from the microphone input and performs FFT in order to determine frequency*/
    private class ProcessAudio extends AsyncTask<Void, double[], Void> {
        @Override
        protected Void doInBackground(Void...params) {
            try{
                int bufferSize = AudioRecord.getMinBufferSize(sampleRate,
                        channelConfiguration, audioEncoding);
                audioRecord = new AudioRecord(
                        MediaRecorder.AudioSource.MIC, sampleRate,
                        channelConfiguration, audioEncoding, bufferSize);
                short[] buffer = new short[blockSize];
                double[] toTransform = new double[blockSize];

                audioRecord.startRecording();
                while (started) {
                    int bufferReadResult = audioRecord.read(buffer, 0, blockSize);
                    for (int i = 0; i < blockSize && i < bufferReadResult; i++) {
                        toTransform[i] = (double) buffer[i] / 32768.0; // no idea what dark magic was this supposed to do
                    }
                    fft.realForward(toTransform);
                    //normalisation
                    for (int i = 0; i<toTransform.length; i++){
                        toTransform[i] = Math.abs(toTransform[i]/blockSize);
                    }
                    publishProgress(toTransform);
                }

            }catch(Exception e){
                e.printStackTrace();
            }
            return null;
        }

        /*
        * Method called after each block of samples has been read and processed with FFT. Updates the data and UI with current values.
        * The update to the UI is executed every @measurementCount-th the procedure is run - updating it every time results in fast
        * flickering of the UI elements and inconvenience of reading data out of it.
        * @params an array of doubles containing the block of FFT-processed samples*/
        @Override
        protected void onProgressUpdate(double[]... waves) {
            if (equalizerView == null){
                Log.i("NULL","");
                return;
            }
            equalizerView.updateWaves(waves[0]);
            double currentMax = findStrongestFreq(waves[0]);
            int measurementCount = 10;
            if (measurementNo != measurementCount){
                gatheredMaxFreq.add(currentMax);
                measurementNo++;
            }
            else
            {
                double overallMax = findPrevalentFreq(gatheredMaxFreq);
                equalizerView.updateFreq(overallMax);
                Tuple<String,String> noteData = toneUtils.extractToneFromFrequency(overallMax);
                equalizerView.updateTone(noteData.x, noteData.y);
                gatheredMaxFreq = new ArrayList<>();
                measurementNo = 1;
            }
        }

        /*Early naive way of figuring out current sampleRate from the spectrum
        * @param an array of sampleRate amplitudes
        * @return : the sampleRate with the highest amplitude
        * */
        private double findStrongestFreq(double[] freqList){
            double maximum = -1;
            int index= -1;
            for (int i = 0; i < freqList.length; i++){
                if (freqList[i]>maximum){
                    maximum = freqList[i];
                    index = i;
                }
            }
            return (index* sampleRate /(blockSize*2));
        }

        /*
        * Finds the largest group of frequencies that are close to each other and returns their average.
        * The practical effect is, that occasional high-amplitude noises don't affect the result of the measurement
        * @param : ArrayList of gathered frequencies to filter
        * @return : the average from the largest group of similar frequencies*/
        private double findPrevalentFreq(ArrayList<Double> frequencies){
            Collections.sort(frequencies);
            ArrayList<ArrayList<Double>>groups = new ArrayList<>();
            double prev = 0.00000001;
            for (int i = 0; i < frequencies.size(); i++){
                if (frequencies.get(i)/prev > 1.25 || frequencies.get(i)/prev < 0.75){
                    groups.add(new ArrayList<Double>());
                }
                groups.get(groups.size()-1).add(frequencies.get(i));
                prev = frequencies.get(i);
            }
            int largest = 0;
            int index=0;
            for (int i = 0; i < groups.size(); i++){
                if (groups.get(i).size() > largest){
                    largest = groups.get(i).size();
                    index = i;
                }
            }
            double sum = 0;
            for (Double freq : groups.get(index)){
                sum += freq;
            }
            return sum/(double)groups.get(index).size();
        }
    }
/*
    public boolean onTouchEvent(MotionEvent touchevent) {
        switch (touchevent.getAction()) {

            case MotionEvent.ACTION_DOWN:
                lastX = touchevent.getX();
                break;
            case MotionEvent.ACTION_UP:
                float currentX = touchevent.getX();

                // Handling left to right screen swap.
                if (lastX < currentX) {

                    // If there aren't any other children, just break.
                    if (viewFlipper.getDisplayedChild() == 0)
                        break;

                    // Next screen comes in from left.
                    viewFlipper.setInAnimation(this, R.anim.slide_in_from_left);
                    // Current screen goes out from right.
                    viewFlipper.setOutAnimation(this, R.anim.slide_out_to_right);

                    // Display next screen.
                    viewFlipper.showNext();
                }

                // Handling right to left screen swap.
                if (lastX > currentX) {

                    // If there is a child (to the left), kust break.
                    if (viewFlipper.getDisplayedChild() == 1)
                        break;

                    // Next screen comes in from right.
                    viewFlipper.setInAnimation(this, R.anim.slide_in_from_right);
                    // Current screen goes out from left.
                    viewFlipper.setOutAnimation(this, R.anim.slide_out_to_left);

                    // Display previous screen.
                    viewFlipper.showPrevious();
                }
                break;
        }
        return false;
    }*/



    @Override
    public void onPause() {
        super.onPause();
        started = false;
        if (audioRecord!=null) audioRecord.stop();
        if (processTask!=null) processTask.cancel(true);
        Log.i("TEST_STOP", "STOPPING");
    }

    @Override
    public void onResume(){
        super.onResume();
        started = true;
        Log.i("TEST_RESUME", "Resuming");
        processTask = new ProcessAudio();
        processTask.execute();
    }

}