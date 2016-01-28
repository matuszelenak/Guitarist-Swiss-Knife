package sk.matus.ksp.guitarist_swiss_knife;

import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Created by whiskas on 28.1.2016.
 */
public class DependencyScheme {
	ArrayList<Dependency>dependencies = new ArrayList<>();

	public DependencyScheme(){

	}

	public void setDependencies(ArrayList<Dependency> dependencies){
		this.dependencies = dependencies;
	}

	public void printDependencies(){
        for (Dependency d: dependencies){
            System.out.println(d);
        }
    }

    public HashMap<String,Boolean> constructClosure(HashSet<String> initialState){
        HashMap<String,Boolean>initState = new HashMap<>();
        for (String s: initialState){
            initState.put(s,true);
        }
        HashMap<String,Boolean>toChange = new HashMap<>();
        HashMap<String,Boolean>oldToChange = new HashMap<>();
        do {
            oldToChange = toChange;
            toChange = new HashMap<>();
            for (HashMap.Entry<String,Boolean>e : oldToChange.entrySet()){
                toChange.put(e.getKey(),e.getValue());
            }
            for (Dependency dep : dependencies){
                HashMap<String,Boolean>leftSide = uniteSets(initState,toChange);
                if (!isSubset(uniteSets(dep.getNewValues(), dep.getCurrentValues()), leftSide)){
                    System.out.print("Using dependency ");
                    System.out.println(dep);
                    toChange = uniteSets(toChange,dep.getResultValues());
                }
            }
        } while (!compareSets(toChange,oldToChange));
        return toChange;
    }

    private boolean isSubset(HashMap<String,Boolean>A,HashMap<String,Boolean>B){
        for (HashMap.Entry<String,Boolean>e : A.entrySet()){
            if (!B.containsKey(e.getKey())) return false;
            if (B.get(e.getKey())!=e.getValue()) return false;
        }
        return true;
    }

    private boolean compareSets(HashMap<String,Boolean>A, HashMap<String,Boolean> B){
        for (HashMap.Entry<String,Boolean>e : A.entrySet()){
            if (B.containsKey(e.getKey())) return false;
        }
        for (HashMap.Entry<String,Boolean>e : B.entrySet()){
            if (A.containsKey(e.getKey())) return false;
        }
        return true;
    }

    private HashMap<String,Boolean> uniteSets(HashMap<String,Boolean>A, HashMap<String,Boolean>B){
        HashMap<String,Boolean>result = new HashMap<>();
        for (HashMap.Entry<String,Boolean>e : A.entrySet()){
            result.put(e.getKey(),e.getValue());
        }
        for (HashMap.Entry<String,Boolean>e : B.entrySet()){
            if (!result.containsKey(e.getKey())) result.put(e.getKey(),e.getValue());
        }
        return result;
    }
}
