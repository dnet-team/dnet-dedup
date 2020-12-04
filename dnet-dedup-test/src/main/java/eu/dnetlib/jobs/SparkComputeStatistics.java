package eu.dnetlib.jobs;

import eu.dnetlib.support.ArgumentApplicationParser;
import eu.dnetlib.support.Relation;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.sql.Encoders;
import org.apache.spark.sql.SparkSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Optional;

public class SparkComputeStatistics extends AbstractSparkJob {

        private static final Logger log = LoggerFactory.getLogger(eu.dnetlib.jobs.SparkComputeStatistics.class);

        public SparkComputeStatistics(ArgumentApplicationParser parser, SparkSession spark) {
            super(parser, spark);
        }

        public static void main(String[] args) throws Exception {

            ArgumentApplicationParser parser = new ArgumentApplicationParser(
                    readResource("/jobs/parameters/computeStatistics_parameters.json", eu.dnetlib.jobs.SparkCreateSimRels.class)
            );

            parser.parseArgument(args);

            SparkConf conf = new SparkConf();

            new eu.dnetlib.jobs.SparkComputeStatistics(
                    parser,
                    getSparkSession(conf)
            ).run();
        }

        @Override
        public void run() throws IOException {

            // read oozie parameters
            final String entitiesPath = parser.get("entitiesPath");
            final String workingPath = parser.get("workingPath");
            final int numPartitions = Optional
                    .ofNullable(parser.get("numPartitions"))
                    .map(Integer::valueOf)
                    .orElse(NUM_PARTITIONS);

            log.info("entitiesPath:  '{}'", entitiesPath);
            log.info("workingPath:   '{}'", workingPath);
            log.info("numPartitions: '{}'", numPartitions);

            // <source, target>: source is the dedup_id, target is the id of the mergedIn
            JavaRDD<Relation> mergerels = spark
                    .read()
                    .load(workingPath + "/mergerels")
                    .as(Encoders.bean(Relation.class))
                    .toJavaRDD();

            JavaRDD<Relation> simrels = spark
                    .read()
                    .load(workingPath + "/simrels")
                    .as(Encoders.bean(Relation.class))
                    .toJavaRDD();

            long simrels_number = simrels.count();
            long mergerels_number = mergerels.count();
            long connected_components = mergerels.groupBy(Relation::getSource).count();

            writeStatsFileToHDFS(simrels_number, mergerels_number, connected_components, workingPath + "/stats_file");

        }

        public static void writeStatsFileToHDFS(long simrels_number, long mergerels_number, long connected_components, String filePath) throws IOException {
            Configuration conf = new Configuration();

            FileSystem fs = FileSystem.get(conf);
            fs.delete(new Path(filePath), true);

            try {
                fs = FileSystem.get(conf);

                Path outFile = new Path(filePath);
                // Verification
                if (fs.exists(outFile)) {
                    System.out.println("Output file already exists");
                    throw new IOException("Output file already exists");
                }

                String print =
                        "Similarity Relations : " + simrels_number + "\n" +
                        "Merge Relations : " + mergerels_number + "\n" +
                        "Connected Components : " + connected_components;

                // Create file to write
                FSDataOutputStream out = fs.create(outFile);
                try{
                    out.writeBytes(print);
                }
                finally {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
}

