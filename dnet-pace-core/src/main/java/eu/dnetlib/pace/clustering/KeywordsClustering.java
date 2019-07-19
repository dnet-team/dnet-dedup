package eu.dnetlib.pace.clustering;

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

        //takes city codes and keywords codes without duplicates
        Set<String> keywords = getKeywords(s, params.getOrDefault("windowSize", 4));
        Set<String> cities = getCities(s, params.getOrDefault("windowSize", 4));

        //list of combination to return as result
        final Collection<String> combinations = new LinkedHashSet<String>();

        for (String keyword: keywordsToCodes(keywords)){
            for (String city: citiesToCodes(cities)) {
                combinations.add(keyword+"-"+city);
                if (combinations.size()>=params.getOrDefault("max", 2)) {
                    return combinations;
                }
            }
        }

        return combinations;
    }
}