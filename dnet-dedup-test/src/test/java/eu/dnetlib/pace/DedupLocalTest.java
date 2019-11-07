package eu.dnetlib.pace;

import eu.dnetlib.Deduper;
import eu.dnetlib.pace.config.DedupConfig;
import eu.dnetlib.pace.utils.Utility;
import eu.dnetlib.support.ConnectedComponent;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.SparkSession;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.net.URL;

public class DedupLocalTest {

    JavaSparkContext context;
    JavaRDD<String> entities;
    DedupConfig conf;

    @Before
    public void setup() {

        conf = DedupConfig.load(Utility.readFromClasspath("/eu/dnetlib/pace/config/organization.strict.conf", DedupLocalTest.class));

        final SparkSession spark = SparkSession
                .builder()
                .appName("Deduplication")
                .master("local[*]")
                .getOrCreate();
        context = new JavaSparkContext(spark.sparkContext());
        final URL dataset = getClass().getResource("/eu/dnetlib/pace/examples/organization.to.fix.json");
        entities = context.textFile(dataset.getPath());

    }

    @Ignore
    @Test
    public void dedupTest(){

        double startTime = System.currentTimeMillis();

        JavaRDD<ConnectedComponent> ccs = Deduper.dedup(context, entities, conf);

        System.out.println("total time = " + (System.currentTimeMillis()-startTime));

        printStatistics(ccs);
//        accumulators.forEach((name, acc) -> System.out.println(name + " -> " + acc.value()));

    }

    public static void printStatistics(JavaRDD<ConnectedComponent> ccs){
        final JavaRDD<ConnectedComponent> connectedComponents = ccs.filter(cc -> cc.getDocs().size()>1);
        final JavaRDD<ConnectedComponent> nonDeduplicated = ccs.filter(cc -> cc.getDocs().size()==1);

        //print deduped
        connectedComponents.foreach(cc -> {
            System.out.println(cc);
        });
//        connectedComponents.foreach(cc -> {
//            cc.getDocs().stream().forEach(d -> {
//                System.out.println(d.getFieldMap().get("legalname") + " | " + d.getFieldMap().get("legalshortname"));
//            });
//        });
        //print nondeduped
        nonDeduplicated.foreach(cc -> {
            System.out.println(cc);
        });

        System.out.println("Non duplicates: " + nonDeduplicated.count());
        System.out.println("Duplicates: " + connectedComponents.flatMap(cc -> cc.getDocs().iterator()).count());
        System.out.println("Connected Components: " + connectedComponents.count());

    }

}