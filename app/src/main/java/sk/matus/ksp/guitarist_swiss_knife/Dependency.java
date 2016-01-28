package sk.matus.ksp.guitarist_swiss_knife;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by whiskas on 28.1.2016.
 */
public class Dependency {
	ArrayList<DependencyValue>newValues = new ArrayList<>();
	ArrayList<DependencyValue> currentValues = new ArrayList<>();
	ArrayList<DependencyValue>resultValues = new ArrayList<>();
	public Dependency(ArrayList<DependencyValue>n,
					  ArrayList<DependencyValue>c,
					  ArrayList<DependencyValue>r){
		newValues = n;
		currentValues = c;
		resultValues = r;
	}

	public HashMap<String,Boolean> getNewValues() {
		HashMap<String,Boolean> result = new HashMap<>();
		for (DependencyValue dv : newValues){
			result.put(dv.predicate,dv.bool);
		}
		return result;
	}

	public HashMap<String,Boolean> getCurrentValues() {
		HashMap<String,Boolean> result = new HashMap<>();
		for (DependencyValue dv : currentValues){
			result.put(dv.predicate,dv.bool);
		}
		return result;
	}

	public HashMap<String,Boolean> getResultValues() {
		HashMap<String,Boolean> result = new HashMap<>();
		for (DependencyValue dv : resultValues){
			result.put(dv.predicate,dv.bool);
		}
		return result;
	}

	@Override
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append('[');
		for (DependencyValue dv : newValues){
			sb.append(dv).append(',');
		}
		sb.deleteCharAt(sb.length()-1);
		sb.append('+');
		for (DependencyValue dv : currentValues){
			sb.append(dv).append(',');
		}
		sb.deleteCharAt(sb.length()-1);
		sb.append("==>");
		for (DependencyValue dv : resultValues){
			sb.append(dv).append(',');
		}
		sb.deleteCharAt(sb.length() - 1);
		sb.append("]\n");
		return sb.toString();
	}
}
