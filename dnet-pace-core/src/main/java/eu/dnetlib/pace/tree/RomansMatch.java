package eu.dnetlib.pace.tree;

import eu.dnetlib.pace.config.Config;
import eu.dnetlib.pace.tree.support.AbstractComparator;
import eu.dnetlib.pace.tree.support.ComparatorClass;

import java.util.Map;

@ComparatorClass("romansMatch")
public class RomansMatch extends AbstractComparator {


    public RomansMatch(Map<String, String> params) {
        super(params);
    }

    @Override
    public double distance(String a, String b, Config conf) {

        //extracts romans from the field
        String romans1 = getRomans(nfd(a));
        String romans2 = getRomans(nfd(b));

        if (romans1.isEmpty() && romans2.isEmpty())
            return 1.0;

        if (romans1.isEmpty() || romans2.isEmpty())
            return -1.0;

        if (romans1.equals(romans2))
            return 1.0;

        return 0.0;
    }
}
