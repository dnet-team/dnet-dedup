package eu.dnetlib.pace.tree;

import eu.dnetlib.pace.model.Field;

import java.util.Map;

@ComparatorClass("similar")
public class SimilarMatch extends AbstractComparator {

    public SimilarMatch(Map<String, Number> params) {
        super(params);
    }

    @Override
    public double compare(Field a, Field b) {

        if (a.stringValue().isEmpty() || b.stringValue().isEmpty())
            return -1; //undefined if one name is missing

        //take only the first name
        String firstname1 = a.stringValue().split(" ")[0];
        String firstname2 = b.stringValue().split(" ")[0];

        if (firstname1.toLowerCase().trim().replaceAll("\\.","").replaceAll("\\s","").length()<=2 || firstname2.toLowerCase().replaceAll("\\.", "").replaceAll("\\s","").length()<=2)		//too short names (considered similar)
            return 1;

        return stringSimilarity(firstname1,firstname2);

    }

}
