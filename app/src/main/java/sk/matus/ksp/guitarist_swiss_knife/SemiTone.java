package sk.matus.ksp.guitarist_swiss_knife;

import java.util.ArrayList;

/**
 * This class represents the atomic part of the tone scale - a semitone.
 */
class SemiTone {
    SemiTone higherSemitone;
    SemiTone lowerSemitone;
    ArrayList<String> names = new ArrayList<>();
    public SemiTone(String name){
        names.add(name);
    }

    public ArrayList<String> getNames() {
        return names;
    }

    /**Method to add possible names for this semitone.
    * Rejects names with too many # or b
    * @param name A name to be processed and added*/
    public void addName(String name){
        if (name.contains("#") && name.contains("b")) return;
        if (name.contains("###") || name.contains("bbb")) return;
        if (!names.contains(name)) names.add(name);
    }

    /**@return A Tone that is higherSemitone by one semitone*/
    public SemiTone getHigherSemitone() {
        return higherSemitone;
    }

    /**@return A Tone that is lowerSemitone by one semitone*/
    public SemiTone getLowerSemitone() {
        return lowerSemitone;
    }

    public void setHigherSemitone(SemiTone higherSemitone) {
        this.higherSemitone = higherSemitone;
    }

    public void setLowerSemitone(SemiTone lowerSemitone) {
        this.lowerSemitone = lowerSemitone;
    }

    @Override
    public String toString(){
        return names.get(0);
    }
}