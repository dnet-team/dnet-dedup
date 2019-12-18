package eu.dnetlib.pace.tree.support;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class TreeNodeStats implements Serializable {

    private Map<String, FieldStats> results; //this is an accumulator for the results of the node

    public TreeNodeStats(){
        this.results = new HashMap<>();
    }

    public Map<String, FieldStats> getResults() {
        return results;
    }

    public void addFieldStats(String id, FieldStats fieldStats){
        this.results.put(id, fieldStats);
    }

    public int fieldsCount(){
        return this.results.size();
    }

    public int undefinedCount(){
        int undefinedCount = 0;
        for(FieldStats fs: this.results.values()){
            if(fs.getResult() == -1)
                undefinedCount ++;
        }
        return undefinedCount;
    }

    public double scoreSum(){
        double scoreSum = 0.0;
        for(FieldStats fs: this.results.values()){
            if(fs.getResult()>=0.0) {
                scoreSum += fs.getResult();
            }
        }
        return scoreSum;
    }

    //return the sum of the weights without considering the fields with countIfMissing=false && result=-1
    public double weightSum(){
        double weightSum = 0.0;
        for(FieldStats fs: this.results.values()){
            if(fs.getResult()>=0.0 || (fs.getResult()<0.0 && fs.isCountIfUndefined())) {
                weightSum += fs.getWeight();
            }
        }
        return weightSum;
    }

    public double weightedScoreSum(){
        double weightedScoreSum = 0.0;
        for(FieldStats fs: this.results.values()){
            if(fs.getResult()>=0.0) {
                weightedScoreSum += fs.getResult()*fs.getWeight();
            }
        }
        return weightedScoreSum;
    }

    public double max(){
        double max = -1.0;
        for(FieldStats fs: this.results.values()){
            if(fs.getResult()>max)
                max = fs.getResult();
        }
        return max;
    }

    public double min(){
        double min = 100.0;  //random high value
        for(FieldStats fs: this.results.values()){
            if(fs.getResult()<min) {
                if (fs.getResult()>=0.0 || (fs.getResult() == -1 && fs.isCountIfUndefined()))
                    min = fs.getResult();
            }
        }
        return min;
    }

    //if at least one is true, return 1.0
    public double or(){
        for (FieldStats fieldStats : this.results.values()) {
            if (fieldStats.getResult() >= fieldStats.getThreshold())
                return 1.0;
        }
        return 0.0;
    }

    //if at least one is false, return 0.0
    public double and() {
        for (FieldStats fieldStats : this.results.values()) {

            if (fieldStats.getResult() == -1) {
                if (fieldStats.isCountIfUndefined())
                    return 0.0;
            }
            else {
                if (fieldStats.getResult() < fieldStats.getThreshold())
                    return 0.0;
            }

        }
        return 1.0;
    }

    public double getFinalScore(AggType aggregation){

        switch (aggregation){
            case AVG:
                return scoreSum()/fieldsCount();
            case SUM:
                return scoreSum();
            case MAX:
                return max();
            case MIN:
                return min();
            case W_MEAN:
                return weightedScoreSum()/weightSum();
            case OR:
                return or();
            case AND:
                return and();
            default:
                return 0.0;
        }
    }
}
