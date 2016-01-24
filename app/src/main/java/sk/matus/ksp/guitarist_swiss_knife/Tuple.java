package sk.matus.ksp.guitarist_swiss_knife;

/**
 * A helper class for pair-like objects
 */
public class Tuple<X, Y> {
    public final X x;
    public final Y y;
    public Tuple(X x, Y y) {
        this.x = x;
        this.y = y;
    }
}