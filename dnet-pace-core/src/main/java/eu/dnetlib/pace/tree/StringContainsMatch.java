package eu.dnetlib.pace.tree;

import eu.dnetlib.pace.config.Config;
import eu.dnetlib.pace.tree.support.AbstractComparator;
import eu.dnetlib.pace.tree.support.ComparatorClass;

import java.util.Map;

/**
 * The Class Contains match
 *
 * @author miconis
 * */
@ComparatorClass("stringContainsMatch")
public class StringContainsMatch extends AbstractComparator {

    private Map<String, String> params;

    private boolean CASE_SENSITIVE;
    private String STRING;
    private String AGGREGATOR;

    public StringContainsMatch(Map<String, String> params) {
        super(params);
        this.params = params;

        //read parameters
        CASE_SENSITIVE = Boolean.parseBoolean(params.getOrDefault("caseSensitive", "false"));
        STRING = params.get("string");
        AGGREGATOR = params.get("aggregator");

    }

    @Override
    public double distance(final String a, final String b, final Config conf) {

        String ca = a;
        String cb = b;
        if (!CASE_SENSITIVE) {
            ca = a.toLowerCase();
            cb = b.toLowerCase();
            STRING = STRING.toLowerCase();
        }

        switch(AGGREGATOR) {
            case "AND":
                if(ca.contains(STRING) && cb.contains(STRING))
                    return 1.0;
                break;
            case "OR":
                if(ca.contains(STRING) || cb.contains(STRING))
                    return 1.0;
                break;
            case "XOR":
                if(ca.contains(STRING) ^ cb.contains(STRING))
                    return 1.0;
                break;
            default:
                return 0.0;
        }
        return 0.0;
    }
}
