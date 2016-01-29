package sk.matus.ksp.guitarist_swiss_knife;

/**
 * This class represents an atomic term for the Dependency class.
 */
public class DependencyTerm {
	boolean bool;
	String statement;
    /**
    * Constructor uses the parameters to set it's internal state.
    * In effect the class represents a statement in either a normal or negated form.*/
	DependencyTerm(boolean b, String statement){
		this.bool = b;
		this.statement = statement;
    }

    /**
    * @return String representation of the therm*/
    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        if (!bool) sb.append("¬ ");
        sb.append(statement);
        return sb.toString();
    }
}
