package sk.matus.ksp.guitarist_swiss_knife;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * This class is a representation of a functional dependency.
 * In case all of the terms on the left side of the dependency hold true, the terms
 * on its right side will become true as well
 */
public class Dependency {
	ArrayList<DependencyTerm>newValues = new ArrayList<>();
	ArrayList<DependencyTerm> currentValues = new ArrayList<>();
	ArrayList<DependencyTerm>resultValues = new ArrayList<>();

    /**
    * @param newValues list of terms that become true due to user's action.
    * @param currentValues list of terms that are true currently.
    * @param resultValues list of terms that should be true if the union of the preceeding two sets holds true.*/
	public Dependency(ArrayList<DependencyTerm>newValues,
					  ArrayList<DependencyTerm>currentValues,
					  ArrayList<DependencyTerm>resultValues){
		this.newValues = newValues;
		this.currentValues = currentValues;
		this.resultValues = resultValues;
	}

    /**
    * @return The set of user-modified terms.*/
	public HashMap<String,Boolean> getNewValues() {
		HashMap<String,Boolean> result = new HashMap<>();
		for (DependencyTerm dt : newValues){
			result.put(dt.statement,dt.bool);
		}
		return result;
	}

    /**
    * @return The set of currently true terms.*/
	public HashMap<String,Boolean> getCurrentValues() {
		HashMap<String,Boolean> result = new HashMap<>();
		for (DependencyTerm dt : currentValues){
			result.put(dt.statement,dt.bool);
		}
		return result;
	}

    /**
     * @return The set of terms resulting from the dependency.*/
	public HashMap<String,Boolean> getResultValues() {
		HashMap<String,Boolean> result = new HashMap<>();
		for (DependencyTerm dt : resultValues){
			result.put(dt.statement,dt.bool);
		}
		return result;
	}

    /**
     * @return String representation of the dependency in the form [newTerms + currentTerms] ==> [resultTerms].*/
	@Override
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append('[');
		for (DependencyTerm dv : newValues){
			sb.append(dv).append(',');
		}
		sb.deleteCharAt(sb.length()-1);
		sb.append(" + ");
		for (DependencyTerm dv : currentValues){
			sb.append(dv).append(',');
		}
		sb.deleteCharAt(sb.length()-1);
		sb.append(" ==> ");
		for (DependencyTerm dv : resultValues){
			sb.append(dv).append(',');
		}
		sb.deleteCharAt(sb.length() - 1);
		sb.append("]\n");
		return sb.toString();
	}
}
