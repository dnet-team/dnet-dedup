package eu.dnetlib.graph

import eu.dnetlib.support.ConnectedComponent
import org.apache.spark.graphx._
import org.apache.spark.rdd.RDD

import scala.collection.JavaConversions;

object GraphProcessor {

  def findCCs(vertexes: RDD[(VertexId, String)], edges: RDD[Edge[String]], maxIterations: Int): RDD[ConnectedComponent] = {
    val graph: Graph[String, String] = Graph(vertexes, edges).partitionBy(PartitionStrategy.RandomVertexCut) //TODO remember to remove partitionby
    val cc = graph.connectedComponents(maxIterations).vertices

    val joinResult = vertexes.leftOuterJoin(cc).map {
      case (id, (rawId, cc)) => {
        if (cc.isEmpty) {
          (id, rawId)
        }
        else {
          (cc.get, rawId)
        }
      }
    }
    val connectedComponents = joinResult.groupByKey()
      .map[ConnectedComponent](cc => asConnectedComponent(cc))
    connectedComponents
  }


  def asConnectedComponent(group: (VertexId, Iterable[String])): ConnectedComponent = {
    val docs = group._2.toSet[String]
    val connectedComponent = new ConnectedComponent(JavaConversions.setAsJavaSet[String](docs));
    connectedComponent
  }

}