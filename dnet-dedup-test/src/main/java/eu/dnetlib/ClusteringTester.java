package eu.dnetlib;

import eu.dnetlib.pace.config.DedupConfig;
import eu.dnetlib.pace.model.MapDocument;
import eu.dnetlib.pace.util.MapDocumentUtil;
import eu.dnetlib.pace.utils.Utility;
import eu.dnetlib.support.Block;
import org.apache.commons.io.IOUtils;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaDoubleRDD;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.DoubleFunction;
import org.apache.spark.api.java.function.PairFunction;
import org.apache.spark.sql.SparkSession;
import scala.Tuple2;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class ClusteringTester {

    public static void main(String[] args) throws Exception {

        String configPath = args[0];
        String entitiesPath = args[1];

        new ClusteringTester()
                .run(configPath, entitiesPath);
    }

    public void run(String configPath, String entitiesPath) throws IOException {

        DedupConfig dedupConf = DedupConfig.load(readJson(configPath));

        SparkSession spark = SparkSession
                .builder()
                .appName("ClusteringTester")
                .master("local[*]")
                .getOrCreate();

        JavaSparkContext sc = JavaSparkContext.fromSparkContext(spark.sparkContext());

        JavaPairRDD<String, MapDocument> mapDocuments = sc
                .textFile(entitiesPath)
                .mapToPair(
                        (PairFunction<String, String, MapDocument>) s -> {
                            MapDocument d = MapDocumentUtil.asMapDocumentWithJPath(dedupConf, s);
                            return new Tuple2<>(d.getIdentifier(), d);
                        });

        long totalRecords = mapDocuments.count();

        // create blocks for deduplication
        JavaPairRDD<String, Block> blocks = Deduper.createSortedBlocks(mapDocuments, dedupConf);

        //block_key, cardinality, comparisons
        JavaRDD<Tuple2<String, Tuple2<Integer, Long>>> blockStats = blocks.map(b -> new Tuple2<>(b._1(), new Tuple2<>((b._2().elements()), comparisonsNumber(b._2(), dedupConf))));

        Long totalComparisons = blockStats.map(b -> b._2()._2()).reduce((a, b) -> a + b);

        Long blocksNumber = blockStats.count();

        JavaDoubleRDD blockSizeRDD = blockStats.mapToDouble(b -> Double.parseDouble(b._2()._1().toString()));

        Double maxBlockSize = blockSizeRDD.max();

        double[] buckets = new double[(int) (maxBlockSize/10 + 3)];

        double bucketSize = 10.0;

        double bucketBase = 0.0;
        for (int i=0; i < buckets.length; i++) {
            buckets[i] = bucketBase;
            bucketBase += bucketSize;
        }

        long[] histogram = blockSizeRDD.histogram(buckets);

        System.out.println("b | n");
        for (int i=0; i< histogram.length; i++) {
            System.out.println(buckets[i] + " | " + histogram[i]);
        }

        System.out.println("max block size = " + maxBlockSize);
        System.out.println("number of records = " + totalRecords);
        System.out.println("number of blocks = " + blocksNumber);
        System.out.println("total number of comparisons = " + totalComparisons);
    }

    //compute the number of comparisons considering the sliding window
    public static Long comparisonsNumber(Block b, DedupConfig dedupConfig){
        long blockSize = b.elements();
        long slidingWindowSize = dedupConfig.getWf().getSlidingWindowSize();
        if (slidingWindowSize >= blockSize)
            return ((slidingWindowSize*(slidingWindowSize-1))/2);
        return (blockSize-slidingWindowSize+1)*((slidingWindowSize*(slidingWindowSize-1))/2);
    }

    public String readJson(String fileName) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(fileName));
        StringBuilder stringBuilder = new StringBuilder();
        char[] buffer = new char[10];
        while (reader.read(buffer) != -1) {
            stringBuilder.append(new String(buffer));
            buffer = new char[10];
        }
        reader.close();

        return stringBuilder.toString();
    }

}
