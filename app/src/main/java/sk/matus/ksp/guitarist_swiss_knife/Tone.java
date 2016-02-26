package sk.matus.ksp.guitarist_swiss_knife;

import android.graphics.PointF;

import java.util.ArrayList;

/**
 * This class represents the atomic part of the tone scale - a semitone.
 */
class Tone {
    private Tone higherTone;
    private Tone lowerTone;
    private ArrayList<ToneName> toneNames = new ArrayList<>();
    private int positionInOctave;
    private double frequency = 0;
    private int octave = 0;
    private PointF frequencyInterval = new PointF(0,0);

    public void setPositionInOctave(int positionInOctave) {
        this.positionInOctave = positionInOctave;
    }

    public void setOctave(int octave){
        this.octave = octave;
    }

    public void setFrequency(double frequency) {
        this.frequency = frequency;
    }

    public void setFrequencyInterval(PointF frequencyInterval) {
        this.frequencyInterval = frequencyInterval;
    }

    public PointF getFrequencyInterval() {
        return frequencyInterval;
    }

    public double getFrequency() {
        return frequency;
    }

    public ArrayList<ToneName> getNames() {
        return toneNames;
    }

    public ToneName getPrimaryName(){
        return toneNames.get(0);
    }

    /**Method to add possible names for this semitone.
    * Rejects names with too many # or b*/
    public void addName(char baseName, String accident, int octave){
        if (accident.contains("♯") && accident.contains("♭")) return;
        if (accident.contains("♯♯♯") || accident.contains("♭♭♭")) return;
        toneNames.add(new ToneName(baseName,accident,octave));
    }

    /**@return A Tone that is higherTone by one semitone*/
    public Tone getHigherTone() {
        return higherTone;
    }

    /**@return A Tone that is lowerTone by one semitone*/
    public Tone getLowerTone() {
        return lowerTone;
    }

    public void setHigherTone(Tone higherTone) {
        this.higherTone = higherTone;
    }

    public void setLowerTone(Tone lowerTone) {
        this.lowerTone = lowerTone;
    }

    @Override
    public String toString(){
        return toneNames.get(0).toString();
    }
}