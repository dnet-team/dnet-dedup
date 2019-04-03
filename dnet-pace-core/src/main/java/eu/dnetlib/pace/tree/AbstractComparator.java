package eu.dnetlib.pace.tree;

import eu.dnetlib.pace.model.Field;
import org.apache.commons.lang.StringUtils;

import java.util.Map;

abstract class AbstractComparator implements Comparator {

    Map<String, Number> params;

    public AbstractComparator(Map<String, Number> params){
        this.params = params;
    }

    @Override
    public double compare(Field a, Field b) {
        return 0.0;
    }

    public static double stringSimilarity(String s1, String s2) {
        String longer = s1, shorter = s2;
        if (s1.length() < s2.length()) { // longer should always have greater length
            longer = s2; shorter = s1;
        }
        int longerLength = longer.length();
        if (longerLength == 0)	//if strings have 0 length return 0 (no similarity)
            return 0.0;

        return (longerLength - StringUtils.getLevenshteinDistance(longer, shorter)) / (double) longerLength;
    }

}
