package eu.dnetlib.reporter;

import eu.dnetlib.DnetAccumulator;
import org.apache.spark.api.java.JavaSparkContext;

import java.util.HashMap;
import java.util.Map;

public class SparkCounter {
    final JavaSparkContext javaSparkContext;


    public  SparkCounter(final JavaSparkContext context){
        this.javaSparkContext = context;
    }


    final Map<String, DnetAccumulator> accumulators = new HashMap<>();

    public void incrementCounter(String counterGroup, String counterName, long delta) {
        final String accumulatorName = String.format("%s::%s", counterGroup, counterName);
        DnetAccumulator currentAccumulator = null;
        if (!accumulators.containsKey(accumulatorName)) {
            currentAccumulator = new DnetAccumulator(counterGroup, counterName);
            javaSparkContext.sc().register(currentAccumulator,accumulatorName);
            accumulators.put(accumulatorName, currentAccumulator);
        } else {
            currentAccumulator = accumulators.get(accumulatorName);
        }
        currentAccumulator.add(delta);
    }

    public Map<String, DnetAccumulator> getAccumulators() {
        return accumulators;
    }
}
