package eu.dnetlib.reporter;

import eu.dnetlib.pace.util.Reporter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import scala.Tuple2;

import java.util.ArrayList;
import java.util.List;

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
