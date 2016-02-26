package sk.matus.ksp.guitarist_swiss_knife;

/**
 * This class represents an atomic term for the Dependency class.
 */
public class DependencyTerm {
	private boolean bool;
	private String statement;
    /**
    * Constructor uses the parameters to set it's internal state.
    * In effect the class represents a statement in either a normal or negated form.*/
	DependencyTerm(boolean b, String statement){
		this.bool = b;
		this.statement = statement;
    }

    public boolean isBool() {
        return bool;
    }

    public String getStatement() {
        return statement;
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

    /**
     * Overriding this method is quite a dirty hack
     * only made to simplify the set operations on the
     * objects of this type.
     * @return a custom HashCode of this instance
     */
    @Override
    public int hashCode(){
        return (String.valueOf(bool) + statement).hashCode();
    }
}
