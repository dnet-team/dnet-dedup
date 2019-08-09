package eu.dnetlib.pace.tree.support;

import eu.dnetlib.pace.config.PaceConfig;
import eu.dnetlib.pace.model.MapDocument;
import eu.dnetlib.pace.util.PaceException;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;

public class TreeNodeDef implements Serializable {

    private List<FieldConf> fields;
    private AggType aggregation;

    private double threshold;

    private String positive;
    private String negative;
    private String undefined;

    boolean ignoreMissing;

    public TreeNodeDef(List<FieldConf> fields, AggType aggregation, double threshold, String positive, String negative, String undefined, boolean ignoreMissing) {
        this.fields = fields;
        this.aggregation = aggregation;
        this.threshold = threshold;
        this.positive = positive;
        this.negative = negative;
        this.undefined = undefined;
        this.ignoreMissing = ignoreMissing;
    }

    public TreeNodeDef() {
    }

    public double evaluate(MapDocument doc1, MapDocument doc2) {

        DescriptiveStatistics stats = new DescriptiveStatistics();
        double sumWeights = 0.0;   //for the weighted mean

        int missCount = 0; //counter for the number of misses

        for (FieldConf fieldConf : fields) {

            double weight = fieldConf.getWeight();

            double result = comparator(fieldConf).compare(doc1.getFieldMap().get(fieldConf.getField()), doc2.getFieldMap().get(fieldConf.getField()));

            if (result >= 0.0) { //if the field is not missing
                stats.addValue(weight * result);
                sumWeights += weight; //sum weights, to be used in case of weighted mean
            }
            else { //if the field is missing
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

        return PaceConfig.resolver.getComparator(field.getComparator(), field.getParams());
    }

    public List<FieldConf> getFields() {
        return fields;
    }

    public void setFields(List<FieldConf> fields) {
        this.fields = fields;
    }

    public AggType getAggregation() {
        return aggregation;
    }

    public void setAggregation(AggType aggregation) {
        this.aggregation = aggregation;
    }

    public double getThreshold() {
        return threshold;
    }

    public void setThreshold(double threshold) {
        this.threshold = threshold;
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

    public boolean isIgnoreMissing() {
        return ignoreMissing;
    }

    public void setIgnoreMissing(boolean ignoreMissing) {
        this.ignoreMissing = ignoreMissing;
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
