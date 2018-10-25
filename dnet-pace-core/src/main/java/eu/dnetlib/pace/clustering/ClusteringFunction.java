package eu.dnetlib.pace.clustering;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import eu.dnetlib.pace.model.Field;

public interface ClusteringFunction {
	
	public Collection<String> apply(List<Field> fields); 
	
	public Map<String, Integer> getParams();

}
