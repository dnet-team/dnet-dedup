package eu.dnetlib.pace.tree;

import eu.dnetlib.pace.config.Config;
import eu.dnetlib.pace.tree.support.AbstractComparator;
import eu.dnetlib.pace.tree.support.ComparatorClass;

import java.util.Map;
import java.util.Set;

@ComparatorClass("keywordMatch")
public class KeywordMatch extends AbstractComparator {

    Map<String, String> params;

    public KeywordMatch(Map<String, String> params) {
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

        Set<String> keywords1 = getKeywords(ca, conf.translationMap(), Integer.parseInt(params.getOrDefault("windowSize", "4")));
        Set<String> keywords2 = getKeywords(cb, conf.translationMap(), Integer.parseInt(params.getOrDefault("windowSize", "4")));

        Set<String> codes1 = toCodes(keywords1, conf.translationMap());
        Set<String> codes2 = toCodes(keywords2, conf.translationMap());

        //if no cities are detected, the comparator gives 1.0
        if (codes1.isEmpty() && codes2.isEmpty())
            return 1.0;
        else {
            if (codes1.isEmpty() ^ codes2.isEmpty())
                return -1.0; //undefined if one of the two has no keywords
            return commonElementsPercentage(codes1, codes2);
        }
    }
}
