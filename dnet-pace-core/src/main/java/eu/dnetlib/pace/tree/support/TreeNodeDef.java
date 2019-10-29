package eu.dnetlib.pace.tree.support;

import eu.dnetlib.pace.config.Config;
import eu.dnetlib.pace.config.PaceConfig;
import eu.dnetlib.pace.model.MapDocument;
import eu.dnetlib.pace.util.PaceException;
import org.codehaus.jackson.annotate.JsonIgnore;
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

    public TreeNodeStats evaluate(MapDocument doc1, MapDocument doc2, Config conf) {

        TreeNodeStats stats = new TreeNodeStats();
        stats.setFieldsCount(fields.size());

        for (FieldConf fieldConf : fields) {

            double weight = fieldConf.getWeight();

            double result = comparator(fieldConf).compare(doc1.getFieldMap().get(fieldConf.getField()), doc2.getFieldMap().get(fieldConf.getField()), conf);

            if (result == -1) { //if the field is missing
                stats.incrementMissCount();
                if (!fieldConf.isIgnoreMissing()) {
                    stats.incrementWeightsSum(weight);
                }
            }
            else {  //if the field is not missing
                stats.incrementScoresSum(weight * result);
                stats.incrementWeightsSum(weight);
            }

        }

        return stats;
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
