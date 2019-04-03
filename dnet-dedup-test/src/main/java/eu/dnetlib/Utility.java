package eu.dnetlib;

import com.google.common.collect.Sets;
import eu.dnetlib.pace.clustering.BlacklistAwareClusteringCombiner;
import eu.dnetlib.pace.config.DedupConfig;
import eu.dnetlib.pace.model.MapDocument;
import org.apache.commons.io.IOUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Set;

public class Utility {

    public static JavaRDD<String> loadDataFromHDFS(String path, JavaSparkContext context) {
        return context.textFile(path);
    }

    public static DedupConfig loadConfigFromHDFS(String path) throws IOException {

        Configuration conf = new Configuration();
//        conf.set("fs.defaultFS", "");
        FileSystem fileSystem = FileSystem.get(conf);
        FSDataInputStream inputStream = new FSDataInputStream(fileSystem.open(new Path(path)));

        return DedupConfig.load(IOUtils.toString(inputStream, StandardCharsets.UTF_8.name()));

    }

    static String readFromClasspath(final String filename) {
        final StringWriter sw = new StringWriter();
        try {
            IOUtils.copy(SparkTest.class.getResourceAsStream(filename), sw);
            return sw.toString();
        } catch (final IOException e) {
            throw new RuntimeException("cannot load resource from classpath: " + filename);
        }
    }

    static Set<String> getGroupingKeys(DedupConfig conf, MapDocument doc) {
        return Sets.newHashSet(BlacklistAwareClusteringCombiner.filterAndCombine(doc, conf));
    }
}
