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

    public JaroWinklerNormalizedName(Map<String, Number> params){
        super(params, new com.wcohen.ss.JaroWinkler());
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

        ca = removeStopwords(ca);
        cb = removeStopwords(cb);

        //replace keywords with codes
        ca = translate(ca, translationMap);
        cb = translate(cb, translationMap);

        //replace cities with codes
//        String norm = normalizeCities(" " + ca + " ||| " + cb + " ", cityMap);
//        ca = norm.split("\\|\\|\\|")[0].trim();
//        cb = norm.split("\\|\\|\\|")[1].trim();

        ca = normalizeCities2(ca, cityMap, 4);
        cb = normalizeCities2(cb, cityMap, 4);


        if (sameCity(ca,cb)){
           if (sameKeywords(ca,cb)){
               ca = removeCodes(ca);
               cb = removeCodes(cb);
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

    public String removeStopwords(String s) {
        String normString = normalize(s);

        normString = filterStopWords(normString, stopwordsIt);
        normString = filterStopWords(normString, stopwordsEn);
        normString = filterStopWords(normString, stopwordsDe);
        normString = filterStopWords(normString, stopwordsFr);
        normString = filterStopWords(normString, stopwordsPt);
        normString = filterStopWords(normString, stopwordsEs);

        return normString;
    }
}
