package eu.dnetlib.pace.tree.support;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import java.io.Serializable;

public class TreeNodeStats implements Serializable {

    private DescriptiveStatistics stats;
    private int    missCount  = 0;
    private int fieldsCount = 0;
    private double weightsSum = 0.0;

    public TreeNodeStats(){
        this.stats = new DescriptiveStatistics();
    }

    public TreeNodeStats(int missCount, int fieldsCount, double weightsSum) {
        this.missCount = missCount;
        this.fieldsCount = fieldsCount;
        this.weightsSum = weightsSum;
    }

    public DescriptiveStatistics getStats() {
        return stats;
    }

    public void setStats(DescriptiveStatistics stats) {
        this.stats = stats;
    }

    public int getMissCount() {
        return missCount;
    }

    public void setMissCount(int missCount) {
        this.missCount = missCount;
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

    public void incrementMissCount(){
        this.missCount += 1;
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
            case MAX:
                return stats.getMax();
            case MIN:
                return stats.getMin();
            case WEIGHTED_MEAN:
                return stats.getSum()/weightsSum;
            default:
                return 0.0;
        }
    }

}
