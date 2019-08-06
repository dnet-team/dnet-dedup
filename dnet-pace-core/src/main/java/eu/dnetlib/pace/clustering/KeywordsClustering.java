package eu.dnetlib.pace.clustering;

import eu.dnetlib.pace.common.AbstractPaceFunctions;
import eu.dnetlib.pace.model.Field;
import org.apache.commons.lang.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

@ClusteringClass("keywordsclustering")
public class KeywordsClustering extends AbstractClusteringFunction {

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

    @Override
    public Collection<String> apply(List<Field> fields) {
        return fields.stream().filter(f -> !f.isEmpty())
                .map(Field::stringValue)
                .map(this::cleanup) //TODO can I add this to the AbstractClusteringFunction without overriding the method here?
                .map(this::normalize)
                .map(s -> filterAllStopWords(s))
                .map(this::doApply)
                .map(c -> filterBlacklisted(c, ngramBlacklist))
                .flatMap(c -> c.stream())
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toCollection(HashSet::new));
    }
}