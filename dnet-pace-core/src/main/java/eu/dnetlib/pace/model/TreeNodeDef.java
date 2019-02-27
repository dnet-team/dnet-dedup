package eu.dnetlib.pace.model;

import eu.dnetlib.pace.config.PaceConfig;
import eu.dnetlib.pace.tree.support.Comparator;
import eu.dnetlib.pace.tree.support.AggType;
import eu.dnetlib.pace.util.PaceException;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;

public class TreeNodeDef implements Serializable {

    private List<FieldConf> fields; //list of fields involved in the tree node (contains comparators to be used and field on which apply the comparator)
    private AggType aggregation;    //how to aggregate similarity measures for every field

    private double threshold;       //threshold on the similarity measure

    private String positive;        //specifies the next node in case of positive result: similarity>=th
    private String negative;        //specifies the next node in case of negative result: similarity<th
    private String undefined;       //specifies the next node in case of undefined result: similarity=-1

    boolean ignoreMissing = true;   //specifies what to do in case of missing field

    public TreeNodeDef() {
    }

    //compute the similarity measure between two documents
    public double evaluate(MapDocument doc1, MapDocument doc2) {

        DescriptiveStatistics stats = new DescriptiveStatistics();
        double sumWeights = 0.0;   //for the weighted mean

        int missCount = 0; //counter for the number of miss
        int fieldsNumber = fields.size();   //number of fields involved in the node

        for (FieldConf fieldConf : fields) {

            double weight = fieldConf.getWeight();

            double result = comparator(fieldConf).compare(doc1.getFieldMap().get(fieldConf.getField()), doc2.getFieldMap().get(fieldConf.getField()));

            if (result != -1) { //if result is not undefined
                stats.addValue(weight * result);
                sumWeights += weight; //sum weights, to be used in case of weighted mean
            }
            else { //if result is undefined
                missCount += 1;
                if (!fieldConf.isIgnoreMissing()){  //if the miss has not to be ignored
                    stats.addValue(weight * 0);
                    sumWeights += weight;
                }
            }
        }

        //global ignoremissing (if one of the field is missing, return undefined)
        if (!ignoreMissing && missCount>0) {
            return -1;
        }

        switch (aggregation){

            case AVG:
                return stats.getMean();
            case SUM:
                return stats.getSum();
            case MAX:
                return stats.getMax();
            case MIN:
                return stats.getMin();
            case WEIGHTED_MEAN:
                return stats.getSum()/sumWeights;
            default:
                return 0.0;
        }

    }

    private Comparator comparator(final FieldConf field){

        return PaceConfig.paceResolver.getComparator(field.getComparator(), field.getParams());
    }

    public TreeNodeDef(List<FieldConf> fields, double threshold, AggType aggregation, String positive, String negative, String undefined) {
        this.fields = fields;
        this.threshold = threshold;
        this.aggregation = aggregation;
        this.positive = positive;
        this.negative = negative;
        this.undefined = undefined;
    }

    public boolean isIgnoreMissing() {
        return ignoreMissing;
    }

    public void setIgnoreMissing(boolean ignoreMissing) {
        this.ignoreMissing = ignoreMissing;
    }

    public List<FieldConf> getFields() {
        return fields;
    }

    public void setFields(List<FieldConf> fields) {
        this.fields = fields;
    }

    public double getThreshold() {
        return threshold;
    }

    public void setThreshold(double threshold) {
        this.threshold = threshold;
    }

    public AggType getAggregation() {
        return aggregation;
    }

    public void setAggregation(AggType aggregation) {
        this.aggregation = aggregation;
    }

    public String getPositive() {
        return positive;
    }

    public void setPositive(String positive) {
        this.positive = positive;
    }

    public String getNegative() {
        return negative;
    }

    public void setNegative(String negative) {
        this.negative = negative;
    }

    public String getUndefined() {
        return undefined;
    }

    public void setUndefined(String undefined) {
        this.undefined = undefined;
    }

    @Override
    public String toString() {
        try {
            return new ObjectMapper().writeValueAsString(this);
        } catch (IOException e) {
            throw new PaceException("Impossible to convert to JSON: ", e);
        }
    }
}