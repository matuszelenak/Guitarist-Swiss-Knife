package sk.matus.ksp.guitarist_swiss_knife;

import android.util.Log;

/**
 * Created by whiskas on 28.1.2016.
 */
public class DependencyValue {
	boolean bool;
	String predicate;
	DependencyValue(boolean b, String predicate){
		this.bool = b;
		this.predicate = predicate;
    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        if (!bool) sb.append("¬ ");
        sb.append(predicate);
        return sb.toString();
    }
}
