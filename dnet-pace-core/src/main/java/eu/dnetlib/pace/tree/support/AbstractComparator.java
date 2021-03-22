package eu.dnetlib.pace.tree.support;

import com.wcohen.ss.AbstractStringDistance;
import eu.dnetlib.pace.common.AbstractPaceFunctions;
import eu.dnetlib.pace.config.Config;
import eu.dnetlib.pace.config.Type;
import eu.dnetlib.pace.model.Field;
import eu.dnetlib.pace.model.FieldList;

import java.util.List;
import java.util.Map;

public abstract class AbstractComparator extends AbstractPaceFunctions implements Comparator {

    /** The ssalgo. */
    protected AbstractStringDistance ssalgo;

    /** The weight. */
    protected double weight = 0.0;

    private Map<String, String> params;

    protected AbstractComparator(Map<String, String> params) {
        this.params = params;
    }

    protected AbstractComparator(Map<String, String> params, final AbstractStringDistance ssalgo){
        this.params = params;
        this.weight = 1.0;
        this.ssalgo = ssalgo;
    }

    /**
     * Instantiates a new second string compare algo.
     *
     * @param weight
     *            the weight
     * @param ssalgo
     *            the ssalgo
     */
    protected AbstractComparator(final double weight, final AbstractStringDistance ssalgo) {
        this.ssalgo = ssalgo;
        this.weight = weight;
    }

    protected AbstractComparator(final AbstractStringDistance ssalgo){
        this.ssalgo = ssalgo;
    }

    /**
     * Normalize.
     *
     * @param d
     *            the d
     * @return the double
     */
    protected double normalize(double d) {
        return d;
    }

    /**
     * Distance.
     *
     * @param a
     *            the a
     * @param b
     *            the b
     * @return the double
     */
    public double distance(final String a, final String b, final Config conf) {

        if (a.isEmpty() || b.isEmpty()) {
            return -1;  //return -1 if a field is missing
        }
        double score = ssalgo.score(a, b);
        return normalize(score);
    }

    /**
     * Distance.
     *
     * @param a
     *            the a
     * @param b
     *            the b
     * @return the double
     */
    protected double distance(final List<String> a, final List<String> b, final Config conf) {
        return distance(concat(a), concat(b), conf);
    }

    public double distance(final Field a, final Field b,  final Config conf) {
        if (a.getType().equals(Type.String) && b.getType().equals(Type.String)) return distance(a.stringValue(), b.stringValue(), conf);
        if (a.getType().equals(Type.List) && b.getType().equals(Type.List)) return distance(toList(a), toList(b), conf);

        throw new IllegalArgumentException("invalid types\n- A: " + a.toString() + "\n- B: " + b.toString());
    }

    @Override
    public double compare(final Field a, final Field b, final Config conf) {
        if (a.isEmpty() || b.isEmpty())
            return -1;
        if (a.getType().equals(Type.String) && b.getType().equals(Type.String)) return distance(a.stringValue(), b.stringValue(), conf);
        if (a.getType().equals(Type.List) && b.getType().equals(Type.List)) return distance(toList(a), toList(b), conf);

        throw new IllegalArgumentException("invalid types\n- A: " + a.toString() + "\n- B: " + b.toString());
    }

    /**
     * To list.
     *
     * @param list
     *            the list
     * @return the list
     */
    protected List<String> toList(final Field list) {
        return ((FieldList) list).stringList();
    }

    public double getWeight(){
        return this.weight;
    }

}
