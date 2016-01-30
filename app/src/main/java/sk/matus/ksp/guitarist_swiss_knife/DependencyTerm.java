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
    * @return String representation of the term.*/
    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        if (!bool) sb.append("¬ ");
        sb.append(statement);
        return sb.toString();
    }

    @Override
    public boolean equals(Object o){
        if (!(o instanceof DependencyTerm)){
            return false;
        }
        if (!(((DependencyTerm) o).statement.equals(statement) || ((DependencyTerm) o).bool!=bool)) return false;
        return true;
    }

    @Override
    public int hashCode(){
        int hash = 7;
        for (int i = 0; i < statement.length(); i++) {
            hash = hash*31 + statement.charAt(i);
        }
        if (bool) hash = hash*47;
        return hash;
    }
}
