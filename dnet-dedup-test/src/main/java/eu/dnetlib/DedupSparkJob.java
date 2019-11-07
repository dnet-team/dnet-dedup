package eu.dnetlib;

import eu.dnetlib.pace.config.DedupConfig;
import eu.dnetlib.pace.utils.Utility;
import eu.dnetlib.support.ConnectedComponent;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.SparkSession;

import java.io.IOException;

public class DedupSparkJob {

    private static Log log = LogFactory.getLog(DedupSparkJob.class);

    public static void main(String[] args) throws IOException {

        final String inputSpacePath = args[0];
        final String dedupConfigPath = args[1];
        final String outputPath = args[2] + "_output";

        final SparkSession spark = SparkSession
                .builder()
                .appName("Deduplication")
                .master("yarn")
                .getOrCreate();

        final JavaSparkContext context = new JavaSparkContext(spark.sparkContext());

        final JavaRDD<String> entities = Utility.loadDataFromHDFS(inputSpacePath, context);

        final DedupConfig config = Utility.loadConfigFromHDFS(dedupConfigPath);

        JavaRDD<ConnectedComponent> ccs = Deduper.dedup(context, entities, config);

        //save connected components on textfile
        Utility.deleteIfExists(outputPath);
        ccs.saveAsTextFile(outputPath);

        final JavaRDD<ConnectedComponent> connectedComponents = ccs.filter(cc -> cc.getDocs().size()>1);
        final JavaRDD<ConnectedComponent> nonDeduplicated = ccs.filter(cc -> cc.getDocs().size()==1);

        log.info("Non duplicates: " + nonDeduplicated.count());
        log.info("Duplicates: " + connectedComponents.flatMap(cc -> cc.getDocs().iterator()).count());
        log.info("Connected Components: " + connectedComponents.count());
    }

}