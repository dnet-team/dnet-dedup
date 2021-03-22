package eu.dnetlib.pace.tree;

import com.wcohen.ss.AbstractStringDistance;
import eu.dnetlib.pace.config.Config;
import eu.dnetlib.pace.model.Field;
import eu.dnetlib.pace.tree.support.AbstractComparator;
import eu.dnetlib.pace.tree.support.ComparatorClass;

import java.util.Map;

@ComparatorClass("alwaysMatch")
public class AlwaysMatch extends AbstractComparator {

    public AlwaysMatch(final Map<String, String> params){
        super(params, new com.wcohen.ss.JaroWinkler());
    }

    public AlwaysMatch(final double weight) {
        super(weight, new com.wcohen.ss.JaroWinkler());
    }

    protected AlwaysMatch(final double weight, final AbstractStringDistance ssalgo) {
        super(weight, ssalgo);
    }

    @Override
    public double compare(final Field a, final Field b, final Config conf) {
        return 1.0;
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

