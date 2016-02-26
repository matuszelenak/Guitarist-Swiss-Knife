package sk.matus.ksp.guitarist_swiss_knife;

import android.support.annotation.NonNull;

import java.lang.reflect.Array;
import java.util.ArrayList;

/**
 *
 */
class Fingering implements Comparable{
    final static int openStringWeight = 2;
    final static int continuousWeight = 2;
    final static int jumpWeight = 3;
    final static int neckDistWeight = 1;
    final static int fretSpanWeight = 3;

    private ArrayList<Integer> fingering = new ArrayList<>();
    private int rating = 0;
    ArrayList<Tone>tones = new ArrayList<>();
    String hashString;
    public Fingering(ArrayList<Integer>fingering) {
        this.fingering = fingering;
        StringBuilder sb = new StringBuilder();
        for (Integer i : fingering){
            if (i == -1){
                sb.append('x');
            }
            else
                sb.append((char)(i.intValue()+65));
        }
        hashString = sb.toString();
        rate();
    }

    public ArrayList<Tone> getTones() {
        return tones;
    }

    public ArrayList<String> getHeaderData(){
        ArrayList<String>result = new ArrayList<>();
        for (Tone t : tones){
            if (t == null) result.add("✕"); else
            result.add(t.getPrimaryName().format("%b%a"));
        }
        return result;
    }

    public void setTones(ArrayList<Tone> tones) {
        this.tones = tones;
    }

    public String getHashString() {
        return hashString;
    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append('[');
        for (Integer i : fingering){
            if (i == -1) sb.append('x').append(' ');
            else sb.append(i).append(' ');
        }
        sb.append(']');
        return sb.toString();
    }

    @Override
    public boolean equals(Object o){
        if (!(o instanceof Fingering)){
            return false;
        }
        if (!((Fingering) o).getFingering().containsAll(fingering)) return false;
        return true;
    }

    /**
     * Overriding this method is quite a dirty hack
     * only made to simplify the set operations on the
     * objects of this type.
     * @return a custom HashCode of this instance
     */
    @Override
    public int hashCode(){
        return hashString.hashCode();
    }

    public ArrayList<String> getStringRepresentation(){
        ArrayList<String>result = new ArrayList<>();
        for (Integer i : fingering){
            if (i == -1) result.add("X");
                else result.add(" ");
        }
        return result;
    }

    public int compareTo(@NonNull Object o){
        Fingering f2 = (Fingering)o;
        return f2.rating - rating;
    }

    public ArrayList<Integer> getFingering() {
        return fingering;
    }

    public int getRating() {
        return rating;
    }

    /**
     * Method that rates the difficulty of the fingering.
     * The bigger the number, the more difficult the fingering is.
     * Currently
     */
    private void rate(){
        rating = numberOfOpenStrings()*openStringWeight
                 + bestContinuousSequenceLen()*continuousWeight
                 - numberOfJumps()*jumpWeight
                 - neckDistance()*neckDistWeight
                 - fretSpan()*fretSpanWeight;
    }

    public int neckDistance(){
        int min = Integer.MAX_VALUE;
        for (Integer i : fingering){
            if (i <= 0) continue;
            min = Math.min(min,i);
        }
        return min;
    }

    public int fretSpan(){
        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;
        for (Integer i : fingering){
            if (i<=0) continue;
            min = Math.min(min,i);
            max = Math.max(max,i);
        }
        return max-min;
    }

    public int numberOfJumps(){
        int result=0;
        boolean b = false;
        for (Integer i : fingering){
            if (i == -1 && b){
                result++;
            } else b=true;
        }
        return result;
    }

    public int numberOfOpenStrings(){
        int result = 0;
        for (Integer i : fingering){
            if (i==0) result++;
        }
        return result;
    }

    public int bestContinuousSequenceLen(){
        int max = 0;
        int gathered = 0;
        for (int i = 0; i < fingering.size(); i++){
            if (fingering.get(i) == -1){
                max = Math.max(gathered,max);
                gathered = 0;
            } else gathered++;
        }
        max = Math.max(gathered,max);
        return max;
    }
}