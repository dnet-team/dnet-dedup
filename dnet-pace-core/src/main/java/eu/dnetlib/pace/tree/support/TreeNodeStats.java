package eu.dnetlib.pace.tree.support;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import java.io.Serializable;

public class TreeNodeStats implements Serializable {

    private DescriptiveStatistics stats;
    private int undefinedCount = 0; //counter for the number of undefined comparisons between the fields in the tree node
    private int fieldsCount = 0;
    private double weightsSum = 0.0;

    public TreeNodeStats(){
        this.stats = new DescriptiveStatistics();
    }

    public TreeNodeStats(int undefinedCount, int fieldsCount, double weightsSum) {
        this.undefinedCount = undefinedCount;
        this.fieldsCount = fieldsCount;
        this.weightsSum = weightsSum;
    }

    public DescriptiveStatistics getStats() {
        return stats;
    }

    public void setStats(DescriptiveStatistics stats) {
        this.stats = stats;
    }

    public int getUndefinedCount() {
        return undefinedCount;
    }

    public void setUndefinedCount(int undefinedCount) {
        this.undefinedCount = undefinedCount;
    }

    public int getFieldsCount() {
        return fieldsCount;
    }

    public void setFieldsCount(int fields) {
        this.fieldsCount = fields;
    }

    public double getWeightsSum() {
        return weightsSum;
    }

    public void setWeightsSum(double weightsSum) {
        this.weightsSum = weightsSum;
    }

    public void incrementWeightsSum(double delta){
        this.weightsSum += delta;
    }

    public void incrementUndefinedCount(){
        this.undefinedCount += 1;
    }

    public void incrementScoresSum(double delta){
        this.stats.addValue(delta);
    }

    public double getFinalScore(AggType aggregation){

        switch (aggregation){
            case AVG:
                return stats.getMean();
            case SUM:
                return stats.getSum();
            case SC:
            case OR:
            case MAX:
                return stats.getMax();
            case NC:
            case AND:
            case MIN:
                return stats.getMin();
            case W_MEAN:
                return stats.getSum()/weightsSum;
            default:
                return 0.0;
        }
    }

}
