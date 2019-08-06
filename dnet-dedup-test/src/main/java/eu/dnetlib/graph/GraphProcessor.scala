package eu.dnetlib.graph

import eu.dnetlib.ConnectedComponent
import eu.dnetlib.pace.model.MapDocument
import org.apache.spark.graphx._
import org.apache.spark.rdd.RDD

import scala.collection.JavaConversions;

object GraphProcessor {

  def findCCs(vertexes: RDD[(VertexId, MapDocument)], edges: RDD[Edge[String]], maxIterations: Int): RDD[ConnectedComponent] = {
    val graph: Graph[MapDocument, String] = Graph(vertexes, edges).partitionBy(PartitionStrategy.RandomVertexCut) //TODO remember to remove partitionby
    val cc = graph.connectedComponents(maxIterations).vertices

    val joinResult = vertexes.leftOuterJoin(cc).map {
      case (id, (openaireId, cc)) => {
        if (cc.isEmpty) {
          (id, openaireId)
        }
        else {
          (cc.get, openaireId)
        }
      }
    }

    val connectedComponents = joinResult.groupByKey().map[ConnectedComponent](cc => asConnectedComponent(cc))

    (connectedComponents)

  }

  def asConnectedComponent(group: (VertexId, Iterable[MapDocument])): ConnectedComponent = {
    val docs = group._2.toSet[MapDocument]
    val connectedComponent = new ConnectedComponent(JavaConversions.setAsJavaSet[MapDocument](docs));
    connectedComponent
  }

}