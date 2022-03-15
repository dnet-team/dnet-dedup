package eu.dnetlib.pace.clustering;

import eu.dnetlib.pace.common.AbstractPaceFunctions;
import eu.dnetlib.pace.config.Config;
import eu.dnetlib.pace.model.Field;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

@ClusteringClass("keywordsclustering")
public class KeywordsClustering extends AbstractClusteringFunction {

    public KeywordsClustering(Map<String, Integer> params) {
        super(params);
    }

    @Override
    protected Collection<String> doApply(final Config conf, String s) {

        //takes city codes and keywords codes without duplicates
        Set<String> keywords = getKeywords(s, conf.translationMap(), params.getOrDefault("windowSize", 4));
        Set<String> cities = getCities(s, params.getOrDefault("windowSize", 4));

        //list of combination to return as result
        final Collection<String> combinations = new LinkedHashSet<String>();

        for (String keyword: keywordsToCodes(keywords, conf.translationMap())){
            for (String city: citiesToCodes(cities)) {
                combinations.add(keyword+"-"+city);
                if (combinations.size()>=params.getOrDefault("max", 2)) {
                    return combinations;
                }
            }
        }

        return combinations;
    }

    @Override
    public Collection<String> apply(final Config conf, List<Field> fields) {
        return fields.stream().filter(f -> !f.isEmpty())
                .map(Field::stringValue)
                .map(this::cleanup)
                .map(this::normalize)
                .map(s -> filterAllStopWords(s))
                .map(s -> doApply(conf, s))
                .map(c -> filterBlacklisted(c, ngramBlacklist))
                .flatMap(c -> c.stream())
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toCollection(HashSet::new));
    }
}