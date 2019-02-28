package eu.dnetlib.pace.tree;

import com.wcohen.ss.AbstractStringDistance;
import eu.dnetlib.pace.tree.support.AbstractDistanceAlgo;
import eu.dnetlib.pace.tree.support.ComparatorClass;

import java.util.Map;

@ComparatorClass("JaroWinklerTitle")
public class JaroWinklerTitle extends AbstractDistanceAlgo {

    public JaroWinklerTitle(Map<String, Number> params){
        super(params, new com.wcohen.ss.JaroWinkler());
    }

    public JaroWinklerTitle(double weight) {
        super(weight, new com.wcohen.ss.JaroWinkler());
    }

    protected JaroWinklerTitle(double weight, AbstractStringDistance ssalgo) {
        super(weight, ssalgo);
    }

    @Override
    public double distance(String a, String b) {
        String ca = cleanup(a);
        String cb = cleanup(b);

        if (ca.isEmpty() || cb.isEmpty()) {
            return -1;
        }

        boolean check = checkNumbers(ca, cb);
        return check ? 0.5 : normalize(ssalgo.score(ca, cb));
    }

    @Override
    public double getWeight() {
        return super.weight;
    }

    @Override
    protected double normalize(double d) {
        return d;
    }

}
