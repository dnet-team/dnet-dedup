package eu.dnetlib.pace.tree;

import eu.dnetlib.pace.config.Config;
import eu.dnetlib.pace.tree.support.AbstractComparator;
import eu.dnetlib.pace.tree.support.ComparatorClass;

import java.util.Map;

@ComparatorClass("numbersMatch")
public class NumbersMatch extends AbstractComparator {


    public NumbersMatch(Map<String, String> params) {
        super(params);
    }

    @Override
    public double distance(String a, String b, Config conf) {

        //extracts numbers from the field
        String numbers1 = getNumbers(nfd(a));
        String numbers2 = getNumbers(nfd(b));

        if (numbers1.isEmpty() && numbers2.isEmpty())
            return 1.0;

        if (numbers1.isEmpty() || numbers2.isEmpty())
            return -1.0;

        if (numbers1.equals(numbers2))
            return 1.0;

        return 0.0;
    }
}