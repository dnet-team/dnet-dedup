package eu.dnetlib.pace.tree;

import com.wcohen.ss.AbstractStringDistance;
import eu.dnetlib.pace.tree.support.AbstractDistanceAlgo;
import eu.dnetlib.pace.tree.support.ComparatorClass;

import java.util.Map;

@ComparatorClass("LevenshteinTitle")
public class LevenshteinTitle extends AbstractDistanceAlgo {

    public LevenshteinTitle(Map<String,Number> params){
        super(params, new com.wcohen.ss.Levenstein());
    }

    public LevenshteinTitle(final double w) {
        super(w, new com.wcohen.ss.Levenstein());
    }

    protected LevenshteinTitle(final double w, final AbstractStringDistance ssalgo) {
        super(w, ssalgo);
    }

    @Override
    public double distance(final String a, final String b) {
        final String ca = cleanup(a);
        final String cb = cleanup(b);

        final boolean check = checkNumbers(ca, cb);

        if (check) return 0.5;

        final String cca = finalCleanup(ca);
        final String ccb = finalCleanup(cb);

        if (cca.isEmpty() || ccb.isEmpty()) {
            return -1;
        }

        return normalize(ssalgo.score(cca, ccb), cca.length(), ccb.length());
    }

    private double normalize(final double score, final int la, final int lb) {
        return 1 - (Math.abs(score) / Math.max(la, lb));
    }

    @Override
    public double getWeight() {
        return super.weight;
    }

    @Override
    protected double normalize(final double d) {
        return 1 / Math.pow(Math.abs(d) + 1, 0.1);
    }

}
