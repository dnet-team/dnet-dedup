package eu.dnetlib.pace;

import eu.dnetlib.pace.config.DedupConfig;
import eu.dnetlib.pace.model.MapDocument;
import eu.dnetlib.pace.tree.support.TreeNodeDef;
import eu.dnetlib.pace.tree.support.TreeNodeStats;
import eu.dnetlib.support.ConnectedComponent;
import org.apache.spark.api.java.JavaRDD;

import java.util.List;
import java.util.Map;

public abstract class DedupTestUtils {


    public static void printStatistics(JavaRDD<ConnectedComponent> ccs){
        final JavaRDD<ConnectedComponent> connectedComponents = ccs.filter(cc -> cc.getDocs().size()>1);
        final JavaRDD<ConnectedComponent> nonDeduplicated = ccs.filter(cc -> cc.getDocs().size()==1);

        //print deduped
        connectedComponents.foreach(cc -> {
            System.out.println(cc);
        });
//        connectedComponents.foreach(cc -> {
//            cc.getDocs().stream().forEach(d -> {
//                System.out.println(d.getFieldMap().get("legalname") + " | " + d.getFieldMap().get("legalshortname"));
//            });
//        });
        //print nondeduped
        nonDeduplicated.foreach(cc -> {
            System.out.println(cc);
        });

        System.out.println("Non duplicates: " + nonDeduplicated.count());
        System.out.println("Duplicates: " + connectedComponents.flatMap(cc -> cc.getDocs().iterator()).count());
        System.out.println("Connected Components: " + connectedComponents.count());

    }

    public static String getOrganizationLegalname(MapDocument mapDocument){
        return mapDocument.getFieldMap().get("legalname").stringValue();
    }

    public static String getJSONEntity(List<String> entities, String id){

        for (String entity: entities) {
            if(entity.contains(id))
                return entity;
        }
        return "";
    }

}
