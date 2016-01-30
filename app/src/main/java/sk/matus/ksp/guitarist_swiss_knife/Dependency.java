package sk.matus.ksp.guitarist_swiss_knife;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 * This class is a representation of a functional dependency.
 * In case all of the terms on the left side of the dependency hold true, the terms
 * on its right side will become true as well
 */
public class Dependency {
	HashSet<DependencyTerm> newValues = new HashSet<>();
	HashSet<DependencyTerm>currentValues = new HashSet<>();
	HashSet<DependencyTerm>resultValues = new HashSet<>();

    /**
    * @param newValues list of terms that become true due to user's action.
    * @param currentValues list of terms that are true currently.
    * @param resultValues list of terms that should be true if the union of the preceeding two sets holds true.*/
	public Dependency(HashSet<DependencyTerm>newValues,
					  HashSet<DependencyTerm>currentValues,
					  HashSet<DependencyTerm>resultValues){
		this.newValues = newValues;
		this.currentValues = currentValues;
		this.resultValues = resultValues;
	}

    /**
    * @return The set of user-modified terms.*/
	public HashSet<DependencyTerm> getNewValues() {
		return newValues;
	}

    /**
    * @return The set of currently true terms.*/
	public HashSet<DependencyTerm> getCurrentValues() {
		return currentValues;
	}

    /**
     * @return The set of terms resulting from the dependency.*/
	public HashSet<DependencyTerm> getResultValues() {
		return resultValues;
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
