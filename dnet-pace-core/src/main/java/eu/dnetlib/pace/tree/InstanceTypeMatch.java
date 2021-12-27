package eu.dnetlib.pace.tree;

import com.google.common.collect.Sets;
import eu.dnetlib.pace.config.Config;
import eu.dnetlib.pace.model.Field;
import eu.dnetlib.pace.model.FieldList;
import eu.dnetlib.pace.tree.support.AbstractComparator;
import eu.dnetlib.pace.tree.support.ComparatorClass;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@ComparatorClass("instanceTypeMatch")
public class InstanceTypeMatch extends AbstractComparator {

    final Map<String, String> translationMap = new HashMap<>();

    public InstanceTypeMatch(Map<String, String> params){
        super(params);

        //jolly types
        translationMap.put("Conference object", "*");
        translationMap.put("Other literature type", "*");
        translationMap.put("Unknown", "*");

        //article types
        translationMap.put("Article", "Article");
        translationMap.put("Data Paper", "Article");
        translationMap.put("Software Paper", "Article");
        translationMap.put("Preprint", "Article");

        //thesis types
        translationMap.put("Thesis", "Thesis");
        translationMap.put("Master thesis", "Thesis");
        translationMap.put("Bachelor thesis", "Thesis");
        translationMap.put("Doctoral thesis", "Thesis");
    }


    @Override
    public double compare(final Field a, final Field b, final Config conf) {

        if (a == null || b == null) {
            return -1;
        }

        final List<String> sa = ((FieldList) a).stringList();
        final List<String> sb = ((FieldList) b).stringList();

        if (sa.isEmpty() || sb.isEmpty()) {
            return -1;
        }

        final Set<String> ca = sa.stream().map(this::translate).collect(Collectors.toSet());
        final Set<String> cb = sb.stream().map(this::translate).collect(Collectors.toSet());

        //if at least one is a jolly type, it must produce a match
        if (ca.contains("*") || cb.contains("*"))
            return 1.0;

        int incommon = Sets.intersection(ca, cb).size();

        //if at least one is in common, it must produce a match
        return incommon >= 1 ? 1 : 0;
    }

    public String translate(String term){
        return translationMap.getOrDefault(term, term);
    }

    @Override
    public double getWeight() {
        return super.weight;
    }

    @Override
    protected double normalize(final double d) {
        return d;
    }

}
