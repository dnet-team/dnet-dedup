package eu.dnetlib.pace.distance.algo;

import com.wcohen.ss.AbstractStringDistance;
import eu.dnetlib.pace.common.AbstractPaceFunctions;
import eu.dnetlib.pace.distance.DistanceClass;
import eu.dnetlib.pace.distance.SecondStringDistanceAlgo;

import java.util.Map;
import java.util.Set;

@DistanceClass("JaroWinklerNormalizedName")
public class JaroWinklerNormalizedName extends SecondStringDistanceAlgo {

    private static Set<String> stopwordsEn = AbstractPaceFunctions.loadFromClasspath("/eu/dnetlib/pace/config/stopwords_en.txt");
    private static Set<String> stopwordsIt = AbstractPaceFunctions.loadFromClasspath("/eu/dnetlib/pace/config/stopwords_it.txt");
    private static Set<String> stopwordsDe = AbstractPaceFunctions.loadFromClasspath("/eu/dnetlib/pace/config/stopwords_de.txt");
    private static Set<String> stopwordsFr = AbstractPaceFunctions.loadFromClasspath("/eu/dnetlib/pace/config/stopwords_fr.txt");
    private static Set<String> stopwordsPt = AbstractPaceFunctions.loadFromClasspath("/eu/dnetlib/pace/config/stopwords_pt.txt");
    private static Set<String> stopwordsEs = AbstractPaceFunctions.loadFromClasspath("/eu/dnetlib/pace/config/stopwords_es.txt");

    //key=word, value=global identifier => example: "università"->"university", used to substitute the word with the global identifier
    private static Map<String,String> translationMap = AbstractPaceFunctions.loadMapFromClasspath("/eu/dnetlib/pace/config/translation_map.csv");

    private static Map<String,String> cityMap = AbstractPaceFunctions.loadMapFromClasspath("/eu/dnetlib/pace/config/city_map.csv");

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
    public double distance(String a, String b) {
        String ca = cleanup(a);
        String cb = cleanup(b);

        ca = normalize(ca);
        cb = normalize(cb);

        ca = filterAllStopWords(ca);
        cb = filterAllStopWords(cb);

        //replace keywords with codes
        String codesA = keywordsToCode(ca, translationMap, params.getOrDefault("windowSize", 4).intValue());
        String codesB = keywordsToCode(cb, translationMap, params.getOrDefault("windowSize",4).intValue());

        //replace cities with codes
        codesA = keywordsToCode(codesA, cityMap, params.getOrDefault("windowSize", 4).intValue());
        codesB = keywordsToCode(codesB, cityMap, params.getOrDefault("windowSize", 4).intValue());

        //if two names have same city
        if (sameCity(codesA,codesB)){
            if (keywordsCompare(codesA, codesB)>params.getOrDefault("threshold", 0.5).doubleValue()) {
                ca = removeCodes(codesA);
                cb = removeCodes(codesB);
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
