package eu.dnetlib.graph;

import com.google.common.collect.Sets;
import eu.dnetlib.support.ConnectedComponent;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.graphx.*;
import org.apache.spark.rdd.RDD;
import org.apache.spark.storage.StorageLevel;
import scala.Tuple2;
import scala.reflect.ClassTag;
import scala.reflect.ClassTag$;

public class JavaGraphProcessor {

    public static RDD<ConnectedComponent> findCCs(JavaPairRDD<Object, String> vertexes, JavaRDD<Edge<String>> edges, int maxIterations) {

        ClassTag<String> stringTag = ClassTag$.MODULE$.apply(String.class);
        Graph<String, String> graph =
                Graph.apply(
                        vertexes.rdd(),
                        edges.rdd(),
                        "",
                        StorageLevel.MEMORY_ONLY(),
                        StorageLevel.MEMORY_ONLY(),
                        stringTag,
                        stringTag
                );

        GraphOps<String, String> graphOps = new GraphOps<>(graph, stringTag, stringTag);
        JavaRDD<Tuple2<Object, Object>> cc = graphOps.connectedComponents(maxIterations).vertices().toJavaRDD();

        JavaPairRDD<Object, String> joinResult = vertexes
                .leftOuterJoin(cc.mapToPair(x -> x))
                .mapToPair(x -> {
                    if (!x._2()._2().isPresent()) {
                        return new Tuple2<>(x._1(), x._2()._1());
                    } else {
                        return new Tuple2<>(x._2()._2(), x._2()._1());
                    }
                });

        return joinResult.groupByKey().map(x -> new ConnectedComponent(Sets.newHashSet(x._2()))).rdd();

    }

}
