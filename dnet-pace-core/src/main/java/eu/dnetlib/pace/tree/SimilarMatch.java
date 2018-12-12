package eu.dnetlib.pace.tree;

import eu.dnetlib.pace.model.Field;

import java.util.Map;

@TreeNodeClass("similar")
public class SimilarMatch extends AbstractTreeNode {

    public SimilarMatch(Map<String, Number> params) {
        super(params);
    }

    @Override
    public int compare(Field a, Field b) {

        if (a.stringValue().isEmpty() || b.stringValue().isEmpty())
            return 0; //undefined if one name is missing

        //take only the first name
        String firstname1 = a.stringValue().split(" ")[0];
        String firstname2 = b.stringValue().split(" ")[0];

        if (firstname1.toLowerCase().trim().replaceAll("\\.","").replaceAll("\\s","").length()<=2 || firstname2.toLowerCase().replaceAll("\\.", "").replaceAll("\\s","").length()<=2)		//too short names (considered similar)
            return 1;

        if (stringSimilarity(firstname1,firstname2)>params.getOrDefault("th", 0.7).doubleValue()){
            return 1;    //similar names, go on with the analysis
        }
        else {
            return -1;   //names too different, no need to compare
        }

    }

}
