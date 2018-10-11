package  eu.dnetlib.graph
import org.apache.spark.graphx._
import org.apache.spark.rdd.RDD
;


object GraphProcessor {

  def findCCs(vertexes: RDD[(VertexId,String)], edges:RDD[Edge[String]], maxIterations: Int): (Long, RDD[String]) = {
    val graph: Graph[String, String] = Graph(vertexes, edges)
    val cc = graph.connectedComponents(maxIterations).vertices


    val totalCC =cc.map{
      case (openaireId, ccId) =>ccId
    }.distinct().count()

   val connectedComponents: RDD[String] = vertexes.join(cc).map {
     case (id, (openaireId, ccId)) => openaireId
   }.distinct()
    (totalCC, connectedComponents)
  }

}