package eu.dnetlib.pace.tree.support;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.dnetlib.pace.config.Config;
import eu.dnetlib.pace.config.PaceConfig;
import eu.dnetlib.pace.model.MapDocument;
import eu.dnetlib.pace.util.PaceException;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.Serializable;
import java.io.StringWriter;
import java.util.List;

public class TreeNodeDef implements Serializable {

    final static String CROSS_COMPARE = "crossCompare";

    private List<FieldConf> fields;
    private AggType aggregation;

    private double threshold;

    private String positive;
    private String negative;
    private String undefined;

    boolean ignoreUndefined;

    public TreeNodeDef(List<FieldConf> fields, AggType aggregation, double threshold, String positive, String negative, String undefined, boolean ignoreUndefined) {
        this.fields = fields;
        this.aggregation = aggregation;
        this.threshold = threshold;
        this.positive = positive;
        this.negative = negative;
        this.undefined = undefined;
        this.ignoreUndefined = ignoreUndefined;
    }

    public TreeNodeDef() {}

    //function for the evaluation of the node
    public TreeNodeStats evaluate(MapDocument doc1, MapDocument doc2, Config conf) {

        TreeNodeStats stats = new TreeNodeStats();

        //for each field in the node, it computes the
        for (FieldConf fieldConf : fields) {

            double weight = fieldConf.getWeight();

            double result;

            //if the param specifies a cross comparison (i.e. compare elements from different fields), compute the result for both sides and return the maximum
            if(fieldConf.getParams().keySet().stream().anyMatch(k -> k.contains(CROSS_COMPARE))) {
                String crossField = fieldConf.getParams().get(CROSS_COMPARE);
                double result1 = comparator(fieldConf).compare(doc1.getFieldMap().get(fieldConf.getField()), doc2.getFieldMap().get(crossField), conf);
                double result2 = comparator(fieldConf).compare(doc1.getFieldMap().get(crossField), doc2.getFieldMap().get(fieldConf.getField()), conf);
                result = Math.max(result1,result2);
            }
            else {
                result = comparator(fieldConf).compare(doc1.getFieldMap().get(fieldConf.getField()), doc2.getFieldMap().get(fieldConf.getField()), conf);
            }

            stats.addFieldStats(
                    fieldConf.getComparator() + " on " + fieldConf.getField() + " " + fields.indexOf(fieldConf),
                    new FieldStats(
                            weight,
                            Double.parseDouble(fieldConf.getParams().getOrDefault("threshold", "1.0")),
                            result,
                            fieldConf.isCountIfUndefined(),
                            doc1.getFieldMap().get(fieldConf.getField()),
                            doc2.getFieldMap().get(fieldConf.getField())
                    ));
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

    public boolean isIgnoreUndefined() {
        return ignoreUndefined;
    }

    public void setIgnoreUndefined(boolean ignoreUndefined) {
        this.ignoreUndefined = ignoreUndefined;
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
