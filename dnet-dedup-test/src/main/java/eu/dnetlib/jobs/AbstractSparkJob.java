package eu.dnetlib.jobs;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.dnetlib.pace.config.DedupConfig;
import eu.dnetlib.pace.utils.Utility;
import eu.dnetlib.support.ArgumentApplicationParser;
import org.apache.spark.SparkConf;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.SaveMode;
import org.apache.spark.sql.SparkSession;

import java.io.IOException;
import java.io.Serializable;

abstract class AbstractSparkJob implements Serializable {

    protected static final int NUM_PARTITIONS = 1000;

    protected static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    public ArgumentApplicationParser parser; // parameters for the spark action
    public SparkSession spark; // the spark session

    public AbstractSparkJob() {}

    public AbstractSparkJob(ArgumentApplicationParser parser, SparkSession spark) {

        this.parser = parser;
        this.spark = spark;
    }

    abstract void run();

    protected static SparkSession getSparkSession(SparkConf conf) {
        return SparkSession.builder().config(conf).getOrCreate();
    }

    protected static <T> void save(Dataset<T> dataset, String outPath, SaveMode mode) {
        dataset.write().option("compression", "gzip").mode(mode).json(outPath);
    }

    protected static DedupConfig loadDedupConfig(String dedupConfPath) {
        return DedupConfig.load(Utility.readFromClasspath("/eu/dnetlib/pace/config/organization.strict.conf.json", AbstractSparkJob.class));
    }

}