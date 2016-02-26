package sk.matus.ksp.guitarist_swiss_knife;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import org.jtransforms.fft.DoubleFFT_1D;
import java.util.ArrayList;
import java.util.Collections;

/**
* The activity that records audio from the microphone in real time
* and presents the user with visualisations of this audio*/
public class TunerActivity extends AppCompatActivity {
    ViewPager viewPager;
    TunerPagerAdapter tunerPagerAdapter;

    //constants chosen so that the tradeoff between real-time performance and accuracy of FFT is optimal
    int sampleRate = 11025;
    int channelConfiguration = AudioFormat.CHANNEL_IN_MONO;
    int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;
    int blockSize = 8192;
    private AudioRecord audioRecord;
    private ProcessAudio processTask;
    private boolean started = false;
    private EqualizerVisualisation equalizerView;
    private GaugeVisualisation gaugeView;
    private DoubleFFT_1D fft = new DoubleFFT_1D(blockSize);
    /**
     * An instance of ToneUtils class for resolving tone related queries.
     */
    private ToneUtils toneUtils;
    private int measurementNo = 0;
    private ArrayList<Double>gatheredMaxFreq = new ArrayList<>();
    private ArrayList<TunerVisualisation> visualisations = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        toneUtils = new ToneUtils(this.getResources());
        equalizerView = new EqualizerVisualisation(this);
        gaugeView = new GaugeVisualisation(this);
        visualisations.add(equalizerView);
        visualisations.add(gaugeView);
        setContentView(R.layout.activity_tuner);
        viewPager = (ViewPager)findViewById(R.id.tunerViewPager);
        tunerPagerAdapter = new TunerPagerAdapter(this);
        tunerPagerAdapter.addPage(equalizerView);
        tunerPagerAdapter.addPage(gaugeView);
        viewPager.setAdapter(tunerPagerAdapter);
    }

    /**
    * A background task that reads blocks of @blockSize audio samples at specified @sampleRate from the microphone input and performs FFT in order to determine frequency.
    * mostly taken from http://stackoverflow.com/questions/5511250/capturing-sound-for-analysis-and-visualizing-frequencies-in-android
    */
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

        /**
        * Method called after each block of samples has been read and processed by FFT. Updates the data and UI with current values.
        * The update to the UI is executed every measurementCount-th the procedure is run - updating it every time results in fast
        * flickering of the UI elements and inconvenience of reading data out of it.
        * @param waves An array of doubles containing the block of FFT-processed samples*/
        @Override
        protected void onProgressUpdate(double[]... waves) {
            for (TunerVisualisation tunerVisualisation : visualisations){
                tunerVisualisation.updateSamples(waves[0]);
            }
            double currentMax = findStrongestFreq(waves[0]);
            int measurementCount = 5;
            if (measurementNo != measurementCount){
                gatheredMaxFreq.add(currentMax);
                measurementNo++;
            }
            else
            {
                double overallMax = findPrevalentFreq(gatheredMaxFreq);
                Tone tone = toneUtils.analyseFrequency(overallMax);
                for (TunerVisualisation tv : visualisations){
                    tv.updateMaxFrequency(overallMax);
                    tv.updateTone(tone);
                }
                gatheredMaxFreq = new ArrayList<>();
                measurementNo = 1;
            }
        }

        /**Naive but reliable way of figuring out current frequency from the spectrum
        * @param freqList An array of sampleRate amplitudes
        * @return The frequency with the highest amplitude
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

        /**
        * Finds the largest group of frequencies that are close to each other and returns their average.
        * The practical effect is, that occasional high-amplitude noises don't affect the result of the measurement.
        * @param frequencies ArrayList of gathered frequencies to filter
        * @return The average from the largest group of similar frequencies*/
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