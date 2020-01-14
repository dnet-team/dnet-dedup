package eu.dnetlib.pace.utils;

import com.google.common.collect.Sets;
import com.google.common.hash.Hashing;
import eu.dnetlib.pace.clustering.BlacklistAwareClusteringCombiner;
import eu.dnetlib.pace.config.DedupConfig;
import eu.dnetlib.pace.model.MapDocument;
import org.apache.commons.io.IOUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.spark.SparkContext;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.util.LongAccumulator;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Utility {

    public static Map<String, LongAccumulator> constructAccumulator(final DedupConfig dedupConf, final SparkContext context) {

        Map<String, LongAccumulator> accumulators = new HashMap<>();

        String acc1 = String.format("%s::%s",dedupConf.getWf().getEntityType(), "records per hash key = 1");
        accumulators.put(acc1, context.longAccumulator(acc1));
        String acc2 = String.format("%s::%s",dedupConf.getWf().getEntityType(), "missing " + dedupConf.getWf().getOrderField());
        accumulators.put(acc2, context.longAccumulator(acc2));
        String acc3 = String.format("%s::%s",dedupConf.getWf().getEntityType(), String.format("Skipped records for count(%s) >= %s", dedupConf.getWf().getOrderField(), dedupConf.getWf().getGroupMaxSize()));
        accumulators.put(acc3, context.longAccumulator(acc3));
        String acc4 = String.format("%s::%s",dedupConf.getWf().getEntityType(), "skip list");
        accumulators.put(acc4, context.longAccumulator(acc4));
        String acc5 = String.format("%s::%s",dedupConf.getWf().getEntityType(), "dedupSimilarity (x2)");
        accumulators.put(acc5, context.longAccumulator(acc5));
        String acc6 = String.format("%s::%s",dedupConf.getWf().getEntityType(), "d < " + dedupConf.getWf().getThreshold());
        accumulators.put(acc6, context.longAccumulator(acc6));

        return accumulators;
    }

    public static JavaRDD<String> loadDataFromHDFS(String path, JavaSparkContext context) {
        return context.textFile(path);
    }

    public static void deleteIfExists(String path) throws IOException {
        Configuration conf = new Configuration();
        FileSystem fileSystem = FileSystem.get(conf);
        if (fileSystem.exists(new Path(path))){
            fileSystem.delete(new Path(path), true);
        }
    }

    public static DedupConfig loadConfigFromHDFS(String path) throws IOException {

        Configuration conf = new Configuration();
        FileSystem fileSystem = FileSystem.get(conf);
        FSDataInputStream inputStream = new FSDataInputStream(fileSystem.open(new Path(path)));

        return DedupConfig.load(IOUtils.toString(inputStream, StandardCharsets.UTF_8.name()));

    }

    public static <T> String readFromClasspath(final String filename, final Class<T> clazz) {
        final StringWriter sw = new StringWriter();
        try {
            IOUtils.copy(clazz.getResourceAsStream(filename), sw);
            return sw.toString();
        } catch (final IOException e) {
            throw new RuntimeException("cannot load resource from classpath: " + filename);
        }
    }

    public static Set<String> getGroupingKeys(DedupConfig conf, MapDocument doc) {
        return Sets.newHashSet(BlacklistAwareClusteringCombiner.filterAndCombine(doc, conf));
    }

    public  static long getHashcode(final String id) {
        return Hashing.murmur3_128().hashUnencodedChars(id).asLong();
    }
}
