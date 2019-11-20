package eu.dnetlib.pace.tree;

import eu.dnetlib.pace.config.Config;
import eu.dnetlib.pace.tree.support.AbstractComparator;
import eu.dnetlib.pace.tree.support.ComparatorClass;

import java.util.Map;
import java.util.Set;

@ComparatorClass("cityMatch")
public class CityMatch extends AbstractComparator {

    private Map<String, Number> params;

    public CityMatch(Map<String, Number> params) {
        super(params);
        this.params = params;
    }

    @Override
    public double distance(final String a, final String b, final Config conf) {

        String ca = cleanup(a);
        String cb = cleanup(b);

        ca = normalize(ca);
        cb = normalize(cb);

        ca = filterAllStopWords(ca);
        cb = filterAllStopWords(cb);

        Set<String> cities1 = getCities(ca, params.getOrDefault("windowSize", 4).intValue());
        Set<String> cities2 = getCities(cb, params.getOrDefault("windowSize", 4).intValue());

        Set<String> codes1 = citiesToCodes(cities1);
        Set<String> codes2 = citiesToCodes(cities2);

        //if no cities are detected, the comparator gives 1.0
        if (codes1.isEmpty() && codes2.isEmpty())
            return 1.0;
        else {
            if (codes1.isEmpty() ^ codes2.isEmpty())
                return -1; //undefined if one of the two has no cities
            return commonElementsPercentage(codes1, codes2) > params.getOrDefault("threshold", 0).intValue() ? 1.0 : 0.0;
        }
    }
}
