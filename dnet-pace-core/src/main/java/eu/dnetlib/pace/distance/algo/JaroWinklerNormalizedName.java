package eu.dnetlib.pace.distance.algo;

import com.wcohen.ss.AbstractStringDistance;
import eu.dnetlib.pace.common.AbstractPaceFunctions;
import eu.dnetlib.pace.config.Config;
import eu.dnetlib.pace.distance.DistanceClass;
import eu.dnetlib.pace.distance.SecondStringDistanceAlgo;

import java.util.Map;
import java.util.Set;

@DistanceClass("JaroWinklerNormalizedName")
public class JaroWinklerNormalizedName extends SecondStringDistanceAlgo {

    private Map<String, Number> params;

    public JaroWinklerNormalizedName(Map<String, Number> params){
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

        Set<String> keywords1 = getKeywords(ca, conf.translationMap(), params.getOrDefault("windowSize", 4).intValue());
        Set<String> keywords2 = getKeywords(cb, conf.translationMap(), params.getOrDefault("windowSize", 4).intValue());

        Set<String> cities1 = getCities(ca, params.getOrDefault("windowSize", 4).intValue());
        Set<String> cities2 = getCities(cb, params.getOrDefault("windowSize", 4).intValue());

        if (sameCity(cities1,cities2)) {

            if (keywordsCompare(keywords1, keywords2, conf.translationMap())>params.getOrDefault("threshold", 0.5).doubleValue()) {

                ca = removeKeywords(ca, keywords1);
                ca = removeKeywords(ca, cities1);
                cb = removeKeywords(cb, keywords2);
                cb = removeKeywords(cb, cities2);

                if (ca.isEmpty() && cb.isEmpty())
                    return 1.0;
                else
                    return normalize(ssalgo.score(ca,cb));

            }
        }

        return 0.0;
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
