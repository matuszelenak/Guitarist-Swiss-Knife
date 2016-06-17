package sk.matus.ksp.guitarist_swiss_knife;

/**
 * Interface that defines methods for a tuner visualization class
 */
public interface TunerVisualisation {
    void updateSamples(double[] data);
    void updateMaxFrequency(double maxFrequency);
    void updateTone(Tone tone);
}
