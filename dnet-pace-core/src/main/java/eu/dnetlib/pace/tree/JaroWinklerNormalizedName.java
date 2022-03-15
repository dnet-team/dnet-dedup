package eu.dnetlib.pace.tree;

import com.wcohen.ss.AbstractStringDistance;
import eu.dnetlib.pace.tree.support.AbstractComparator;
import eu.dnetlib.pace.tree.support.ComparatorClass;

import eu.dnetlib.pace.config.Config;


import java.util.Map;
import java.util.Set;

@ComparatorClass("jaroWinklerNormalizedName")
public class JaroWinklerNormalizedName extends AbstractComparator {

    private Map<String, String> params;

    public JaroWinklerNormalizedName(Map<String, String> params){
        super(params, new com.wcohen.ss.JaroWinkler());
        this.params = params;
    }

    public JaroWinklerNormalizedName(double weight) {
        super(weight, new com.wcohen.ss.JaroWinkler());
    }

    protected JaroWinklerNormalizedName(double weight, AbstractStringDistance ssalgo) {
        super(weight, ssalgo);
    }

    @Override
    public double distance(String a, String b, final Config conf) {
        String ca = cleanup(a);
        String cb = cleanup(b);

        ca = normalize(ca);
        cb = normalize(cb);

        ca = filterAllStopWords(ca);
        cb = filterAllStopWords(cb);

        Set<String> keywords1 = getKeywords(ca, conf.translationMap(), Integer.parseInt(params.getOrDefault("windowSize", "4")));
        Set<String> keywords2 = getKeywords(cb, conf.translationMap(), Integer.parseInt(params.getOrDefault("windowSize", "4")));

        Set<String> cities1 = getCities(ca, Integer.parseInt(params.getOrDefault("windowSize", "4")));
        Set<String> cities2 = getCities(cb, Integer.parseInt(params.getOrDefault("windowSize", "4")));

        ca = removeKeywords(ca, keywords1);
        ca = removeKeywords(ca, cities1);
        cb = removeKeywords(cb, keywords2);
        cb = removeKeywords(cb, cities2);

        ca = ca.replaceAll("[ ]{2,}", " ");
        cb = cb.replaceAll("[ ]{2,}", " ");

        if (ca.isEmpty() && cb.isEmpty())
            return 1.0;
        else
            return normalize(ssalgo.score(ca,cb));
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
