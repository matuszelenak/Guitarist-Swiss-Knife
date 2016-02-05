package sk.matus.ksp.guitarist_swiss_knife;

import java.util.ArrayList;

/**
 * Created by whiskas on 5.2.2016.
 */
class Fingering implements Comparable{
    ArrayList<Integer> fingering = new ArrayList<>();
    int rating = 0;
    public Fingering(ArrayList<Integer>fingering){
        this.fingering = fingering;
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

    public int compareTo(Object o){
        Fingering f2 = (Fingering)o;
        return f2.rating - rating;
    }
}