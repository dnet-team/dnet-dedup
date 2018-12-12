package eu.dnetlib.pace.tree;

import eu.dnetlib.pace.model.Field;
import eu.dnetlib.pace.model.FieldListImpl;

import java.util.Map;

@TreeNodeClass("topicsMatch")
public class TopicsMatch extends AbstractTreeNode {

    public TopicsMatch(Map<String, Number> params) {
        super(params);
    }

    @Override
    public int compare(Field a, Field b) {

        double[] t1 = ((FieldListImpl) a).doubleArray();
        double[] t2 = ((FieldListImpl) b).doubleArray();

        if (t1 == null || t2 == null)
            return 0; //0 similarity if no topics in one of the authors or in both

        double area = 0.0;

        double min_value[] = new double[t1.length];
        for(int i=0; i<t1.length; i++){

            min_value[i] = (t1[i]<t2[i])?t1[i]:t2[i];
            area += min_value[i];
        }

        return area>params.getOrDefault("th", 0.7).doubleValue()?+1:-1;

    }
}
