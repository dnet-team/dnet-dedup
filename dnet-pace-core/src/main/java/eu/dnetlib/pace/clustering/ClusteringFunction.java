package eu.dnetlib.pace.clustering;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import eu.dnetlib.pace.config.Config;
import eu.dnetlib.pace.model.Field;

public interface ClusteringFunction {
	
	public Collection<String> apply(Config config, List<Field> fields);
	
	public Map<String, Integer> getParams();

}
