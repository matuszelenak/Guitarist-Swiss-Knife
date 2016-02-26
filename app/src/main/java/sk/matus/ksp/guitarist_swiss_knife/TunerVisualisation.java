package sk.matus.ksp.guitarist_swiss_knife;

/**
 * Created by whiskas on 25.2.2016.
 */
public interface TunerVisualisation {
    void updateSamples(double[] data);
    void updateMaxFrequency(double maxFrequency);
    void updateTone(Tone tone);
}
