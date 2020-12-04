package eu.dnetlib.jobs;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.dnetlib.pace.config.DedupConfig;
import eu.dnetlib.pace.utils.Utility;
import eu.dnetlib.support.ArgumentApplicationParser;
import org.apache.commons.io.IOUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.spark.SparkConf;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.SaveMode;
import org.apache.spark.sql.SparkSession;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.stream.Collectors;

public abstract class AbstractSparkJob implements Serializable {

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

    abstract void run() throws IOException;

    protected static SparkSession getSparkSession(SparkConf conf) {
        return SparkSession.builder().config(conf).getOrCreate();
    }

    protected static <T> void save(Dataset<T> dataset, String outPath, SaveMode mode) {
        dataset.write().option("compression", "gzip").mode(mode).json(outPath);
    }

    protected static DedupConfig loadDedupConfig(String dedupConfPath) throws IOException {
        return DedupConfig.load(
            readFileFromHDFS(dedupConfPath)
        );
    }

    protected static String readFileFromHDFS(String filePath) throws IOException {

        Path path=new Path(filePath);
        FileSystem fs = FileSystem.get(new Configuration());
        BufferedReader br=new BufferedReader(new InputStreamReader(fs.open(path)));
        try {
            return String.join("", br.lines().collect(Collectors.toList()));
        } finally {
            br.close();
        }
    }

    public static String readResource(String path, Class<? extends AbstractSparkJob> clazz) throws IOException {
        return IOUtils.toString(clazz.getResourceAsStream(path));
    }
}