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
@ComparatorClass("containsMatch")
public class ContainsMatch extends AbstractComparator {

    private Map<String, String> params;

    public ContainsMatch(Map<String, String> params) {
        super(params);
        this.params = params;
    }

    @Override
    public double distance(final String a, final String b, final Config conf) {

        //read parameters
        boolean caseSensitive = Boolean.parseBoolean(params.getOrDefault("caseSensitive", "false"));
        String string = params.get("string");
        String agg = params.get("bool");

        String ca = a;
        String cb = b;
        if (!caseSensitive) {
            ca = a.toLowerCase();
            cb = b.toLowerCase();
        }

        switch(agg) {
            case "AND":
                if(ca.contains(string) && cb.contains(string))
                    return 1.0;
                break;
            case "OR":
                if(ca.contains(string) || cb.contains(string))
                    return 1.0;
                break;
            case "XOR":
                if(ca.contains(string) ^ cb.contains(string))
                    return 1.0;
                break;
            default:
                return 0.0;
        }
        return 0.0;
    }
}
