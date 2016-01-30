package sk.matus.ksp.guitarist_swiss_knife;

import android.content.res.Resources;
import android.util.JsonReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 * This class should handle all the actions associated with it's functional dependencies.
 */
public class DependencyScheme {
    /**
     * A list of valid functional dependencies which is used for further calculations.
     */
	ArrayList<Dependency>dependencies = new ArrayList<>();

    /**On construction the dependency scheme loads the dependencies from the JSON file
    * @param resources The Resources to read JSON from*/
    public DependencyScheme(Resources resources){
        try{
            InputStream io = resources.openRawResource(R.raw.flag_dependencies);
            JsonReader reader = new JsonReader(new InputStreamReader(io, "UTF-8"));
            dependencies = readDependencyArray(reader);
            reader.close();
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    /**
    * Prints out all of the dependencies to the console output. Used for debugging.*/
	public void printDependencies(){
        for (Dependency d: dependencies){
            System.out.println(d);
        }
    }

    /**
    * This function calculates the transitive closure of the current functional dependencies given the set of initial terms
    * that are true. It does so by iterating the dependencies and adding newly discovered terms.
    * The construction terminates when no new terms can be derived from the current set of terms.
    * @param initialState The set of terms that are true before the calculation of the closure
    * @return A set of terms that should hold true*/
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
        } while (!equalSets(toChange, oldToChange));
        return toChange;
    }

    /**
    * A helper method that verifies if a set is a subset of another set.
    * @param A The first set
    * @param B The second set
    * @return True if A is a subset of B, else otherwise*/
    private boolean isSubset(HashMap<String,Boolean>A,HashMap<String,Boolean>B){
        for (HashMap.Entry<String,Boolean>e : A.entrySet()){
            if (!B.containsKey(e.getKey())) return false;
            if (B.get(e.getKey())!=e.getValue()) return false;
        }
        return true;
    }

    /**
    * A helper method to compare two sets for equality.
    * @param A The first set
    * @param B The second set
    * @return True if the sets contain the same values, false otherwise*/
    private boolean equalSets(HashMap<String, Boolean> A, HashMap<String, Boolean> B){
        for (HashMap.Entry<String,Boolean>e : A.entrySet()){
            if (B.containsKey(e.getKey())) return false;
        }
        for (HashMap.Entry<String,Boolean>e : B.entrySet()){
            if (A.containsKey(e.getKey())) return false;
        }
        return true;
    }

    /**
    * A helper method that unites the content of two sets into one.
    * @param A The first set
    * @param B The second set
    * @return The union of sets A and B*/
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

    /**
     * Method reads Dependencies from JSON file.
     * @param reader The JSONReader to use for reading
     * @return An ArrayList of Dependencies*/
    private ArrayList<Dependency> readDependencyArray(JsonReader reader) throws IOException {
        ArrayList<Dependency> dependencies = new ArrayList<>();
        reader.beginArray();
        while(reader.hasNext()){
            dependencies.add(readDependency(reader));
        }
        reader.endArray();
        return dependencies;
    }

    /**
     * Method reads a single Dependency from JSON file.
     * @param reader The JSONReader to use for reading
     * @return read Dependency*/
    private Dependency readDependency(JsonReader reader) throws IOException {
        reader.beginObject();
        ArrayList<DependencyTerm> newValues = new ArrayList<>();
        ArrayList<DependencyTerm> currentValues = new ArrayList<>();
        ArrayList<DependencyTerm> resultValues = new ArrayList<>();
        while (reader.hasNext()) {
            String name = reader.nextName();
            if (name.equals("new")) {
                newValues = readValues(reader);
            } else if (name.equals("current")) {
                currentValues = readValues(reader);
            } else if (name.equals("result")) {
                resultValues = readValues(reader);
            } else reader.skipValue();
        }
        reader.endObject();
        return new Dependency(newValues, currentValues, resultValues);
    }
    /**
    * Method reads DependencyTerms from JSON file
     * @param reader The JSONReader to use for reading
    * @return An ArrayList of DependencyTerms*/
    private ArrayList<DependencyTerm> readValues(JsonReader reader) throws IOException {
        ArrayList<DependencyTerm>values = new ArrayList<>();
        reader.beginArray();
        while (reader.hasNext()){
            reader.beginArray();
            boolean b = reader.nextBoolean();
            String flag = reader.nextString();
            reader.endArray();
            values.add(new DependencyTerm(b,flag));
        }
        reader.endArray();
        return values;
    }
}
