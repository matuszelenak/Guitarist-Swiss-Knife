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
	private ArrayList<Dependency>dependencies = new ArrayList<>();
    private HashMap<String,ToggleableRadioButton>buttonMapping = new HashMap<>();

    /**On construction the dependency scheme loads the dependencies from the JSON file
    * @param resources The Resources to read JSON from*/
    public DependencyScheme(Resources resources){
        try{
            InputStream io = resources.openRawResource(R.raw.flag_dependencies);
            JsonReader reader = new JsonReader(new InputStreamReader(io, "UTF-8"));
            reader.setLenient(true);

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
     * @param buttons A HashMap of buttonTag-to-button associations
     */
    public void setModifierButtons(HashMap<String,ToggleableRadioButton>buttons){
        buttonMapping = buttons;
    }

    public HashMap<String, ToggleableRadioButton> getButtonMapping() {
        return buttonMapping;
    }

    /**
     *
     * @return
     */
    private HashSet<DependencyTerm> getCurrentFlags(){
        HashSet<DependencyTerm>result = new HashSet<>();
        for (HashMap.Entry<String,ToggleableRadioButton>e : buttonMapping.entrySet()){
            result.add(new DependencyTerm(e.getValue().isChecked(), e.getKey()));
        }
        return result;
    }

    /**
     * Given a newly derived dependency term, the method updates the actual modifier
     * component so that it corresponds to the current state.
     * @param dt A dependency term describing the state of the modifier to be set.
     */
    private void performButtonAction(DependencyTerm dt){
        if (buttonMapping.get(dt.getStatement()).isChecked() != dt.isBool()){
            buttonMapping.get(dt.getStatement()).performClick();
        }
    }

    /**
     * Method called after a chord modifier has been changed.
     * The method iterates through all the dependencies and checks
     * if any additional changes to other modifiers are necessary to
     * maintain a consistent state.
     * If a dependency holds true, corresponding modifiers are updated
     * which can trigger this method to run again and so on...
     * @param newValues A HashSet of the DependencyTerm values which represents
     *                  the changes made to the modifier
     */
    public void deriveNew(HashSet<DependencyTerm>newValues){
        HashSet<DependencyTerm>currentValues;
        for (Dependency dependency : dependencies){
            currentValues = getCurrentFlags();
            for (DependencyTerm dt : newValues){
                if (currentValues.contains(dt)) currentValues.remove(dt);
            }
            if (isSubset(dependency.getNewValues(),newValues) && isSubset(dependency.getCurrentValues(),currentValues)){
                for (DependencyTerm dt : dependency.getResultValues()){
                    performButtonAction(dt);
                }
            }
        }
    }

    /**
    * A helper method that verifies if a set is a subset of another set.
    * @param A The first set
    * @param B The second set
    * @return True if A is a subset of B, else otherwise*/
    private boolean isSubset(HashSet<DependencyTerm>A,HashSet<DependencyTerm>B){
        for (DependencyTerm e : A){
            if (!B.contains(e)) return false;
        }
        return true;
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
        HashSet<DependencyTerm> newValues = new HashSet<>();
        HashSet<DependencyTerm> currentValues = new HashSet<>();
        HashSet<DependencyTerm> resultValues = new HashSet<>();
        while (reader.hasNext()) {
            String name = reader.nextName();
            switch (name) {
                case "new":
                    newValues = readValues(reader);
                    break;
                case "current":
                    currentValues = readValues(reader);
                    break;
                case "result":
                    resultValues = readValues(reader);
                    break;
                default:
                    reader.skipValue();
                    break;
            }
        }
        reader.endObject();
        return new Dependency(newValues, currentValues, resultValues);
    }
    /**
    * Method reads DependencyTerms from JSON file
     * @param reader The JSONReader to use for reading
    * @return An ArrayList of DependencyTerms*/
    private HashSet<DependencyTerm> readValues(JsonReader reader) throws IOException {
        HashSet<DependencyTerm>values = new HashSet<>();
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
