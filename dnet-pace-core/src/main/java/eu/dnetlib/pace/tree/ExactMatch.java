package eu.dnetlib.pace.tree;

import com.wcohen.ss.AbstractStringDistance;
import eu.dnetlib.pace.config.Config;
import eu.dnetlib.pace.tree.support.AbstractComparator;
import eu.dnetlib.pace.tree.support.ComparatorClass;

import java.util.Map;

@ComparatorClass("exactMatch")
public class ExactMatch extends AbstractComparator {

    public ExactMatch(Map<String, String> params){
        super(params, new com.wcohen.ss.JaroWinkler());
    }

    public ExactMatch(final double weight) {
        super(weight, new com.wcohen.ss.JaroWinkler());
    }

    protected ExactMatch(final double weight, final AbstractStringDistance ssalgo) {
        super(weight, ssalgo);
    }

    @Override
    public double distance(final String a, final String b, final Config conf) {
        if (a.isEmpty() || b.isEmpty()) {
            return -1.0;  //return -1 if a field is missing
        }
        return a.equals(b) ? 1.0 : 0;
    }

    @Override
    public double getWeight() {
        return super.weight;
    }

    @Override
    protected double normalize(final double d) {
        return d;
    }
}
