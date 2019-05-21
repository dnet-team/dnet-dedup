package eu.dnetlib.reporter;

import eu.dnetlib.pace.util.Reporter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.util.LongAccumulator;
import scala.Serializable;
import scala.Tuple2;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SparkReporter implements Serializable {

    final List<Tuple2<String, String>> report = new ArrayList<>();
    private static final Log log = LogFactory.getLog(SparkReporter.class);

    public SparkReporter(){}

    public void incrementCounter(String counterGroup, String counterName, long delta, Map<String, LongAccumulator> accumulators) {

        final String accumulatorName = String.format("%s::%s", counterGroup, counterName);
        if (accumulators.containsKey(accumulatorName)){
            accumulators.get(accumulatorName).add(delta);
        }

    }

    public void emit(String type, String from, String to) {
        report.add(new Tuple2<>(from, to));
    }

    public List<Tuple2<String, String>> getReport() {
        return report;
    }
}
