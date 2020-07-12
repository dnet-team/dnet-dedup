package eu.dnetlib.pace;

import eu.dnetlib.ClusteringTester;
import eu.dnetlib.Deduper;
import eu.dnetlib.pace.config.DedupConfig;
import eu.dnetlib.pace.model.Field;
import eu.dnetlib.pace.model.MapDocument;
import eu.dnetlib.pace.tree.support.TreeProcessor;
import eu.dnetlib.pace.tree.support.TreeStats;
import eu.dnetlib.pace.util.MapDocumentUtil;
import eu.dnetlib.pace.utils.Utility;
import eu.dnetlib.support.Block;
import eu.dnetlib.support.ConnectedComponent;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.PairFunction;
import org.apache.spark.sql.SparkSession;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import scala.Tuple2;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Map;

public class DedupLocalTest extends DedupTestUtils {

    SparkSession spark;
    DedupConfig config;
    JavaSparkContext context;

    final String entitiesPath = "/Users/miconis/Desktop/publications_to_fix.json";

    @Before
    public void setup() {

        config = DedupConfig.load(Utility.readFromClasspath("/eu/dnetlib/pace/config/publication.current.conf.json", DedupLocalTest.class));

        spark = SparkSession
                .builder()
                .appName("Deduplication")
                .master("local[*]")
                .getOrCreate();
        context = JavaSparkContext.fromSparkContext(spark.sparkContext());

    }

    @Test
    public void deduplicationTest() {

        Deduper.createSimRels(
                config,
                spark,
                entitiesPath,
                "/tmp/deduptest/publication_simrel"
        );

        Deduper.createMergeRels(
                config,
                entitiesPath,
                "/tmp/deduptest/publication_mergerel",
                "/tmp/deduptest/publication_simrel",
                spark
        );

        Deduper.createDedupEntity(
                config,
                "/tmp/deduptest/publication_mergerel",
                entitiesPath,
                spark,
                "/tmp/deduptest/dedupentity"
        );

    }

}