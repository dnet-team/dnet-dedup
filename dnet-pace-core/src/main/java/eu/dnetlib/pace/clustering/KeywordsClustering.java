package eu.dnetlib.pace.clustering;

import com.google.common.base.Joiner;
import eu.dnetlib.pace.common.AbstractPaceFunctions;

import java.util.*;

@ClusteringClass("keywordsclustering")
public class KeywordsClustering extends AbstractClusteringFunction {

    private static Map<String,String> translationMap = AbstractPaceFunctions.loadMapFromClasspath("/eu/dnetlib/pace/config/translation_map.csv");

    private static Map<String,String> cityMap = AbstractPaceFunctions.loadMapFromClasspath("/eu/dnetlib/pace/config/city_map.csv");

    public KeywordsClustering(Map<String, Integer> params) {
        super(params);
    }

    @Override
    protected Collection<String> doApply(String s) {

        List<String> keywords = getCodes(s, translationMap, params.getOrDefault("windowSize", 4));
        List<String> cities = getCodes(s, cityMap, params.getOrDefault("windowSize", 4));

        final Collection<String> combinations = new LinkedHashSet<String>();

        int size = 0;
        for (String keyword: keywords){
            for (String city: cities) {
                combinations.add(keyword+"-"+city);
                if (++size>params.getOrDefault("max", 2)) {
                    return combinations;
                }
            }
        }

        return combinations;
    }
}