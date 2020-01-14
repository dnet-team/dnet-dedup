package eu.dnetlib.pace;

import eu.dnetlib.pace.model.MapDocument;
import eu.dnetlib.support.ConnectedComponent;
import org.apache.spark.api.java.JavaRDD;

import java.util.List;

public abstract class DedupTestUtils {


    public static void printStatistics(JavaRDD<ConnectedComponent> ccs){
        final JavaRDD<ConnectedComponent> connectedComponents = ccs.filter(cc -> cc.getDocs().size()>1);
        final JavaRDD<ConnectedComponent> nonDeduplicated = ccs.filter(cc -> cc.getDocs().size()==1);

        //print deduped
        connectedComponents.map(cc -> {
            StringBuilder sb = new StringBuilder();
            for (MapDocument m : cc.getDocs()){
                sb.append(m.getFieldMap().get("originalId").stringValue() + " - "+ m.getFieldMap().get("legalname").stringValue() + "\n");
            }
            return sb.toString();
        }).foreach(s -> System.out.println("*******\n" + s + "*******\n"));

        //print nondeduped
        nonDeduplicated.foreach(cc -> {
            System.out.println(cc.getId() + " - " + cc.getFieldMap().get("legalname").stringValue());
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
