package eu.dnetlib.reporter;

import eu.dnetlib.DnetAccumulator;
import eu.dnetlib.Reporter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.api.java.JavaSparkContext;
import org.glassfish.jersey.internal.util.collection.StringIgnoreCaseKeyComparator;
import scala.Tuple2;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SparkReporter implements Reporter {

    final SparkCounter counter;

    final List<Tuple2<String, String>> report = new ArrayList<>();
    private static final Log log = LogFactory.getLog(SparkReporter.class);

    public SparkReporter(SparkCounter counter){
        this.counter = counter;
    }


    @Override
    public void incrementCounter(String counterGroup, String counterName, long delta) {
        counter.incrementCounter(counterGroup, counterName, delta);
    }

    @Override
    public void emit(String type, String from, String to) {


        report.add(new Tuple2<>(from, to));
    }

    public List<Tuple2<String, String>> getReport() {
        return report;
    }
}
