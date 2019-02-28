package eu.dnetlib.pace.config;

import eu.dnetlib.pace.model.ClusteringDef;
import eu.dnetlib.pace.model.FieldDef;
import eu.dnetlib.pace.model.TreeNodeDef;

import java.util.List;
import java.util.Map;

/**
 * Interface for PACE configuration bean.
 *
 * @author claudio
 */
public interface Config {

	/**
	 * Field configuration definitions.
	 *
	 * @return the list of definitions
	 */
	public List<FieldDef> model();

	/**
	 * Field configuration definitions.
	 *
	 * @return the list of definitions
	 */
	public Map<String, FieldDef> modelMap();


	/**
	 * Decision tree.
	 *
	 * @return
	 */
	public Map<String, TreeNodeDef> decisionTree();

	/**
	 * Clusterings.
	 *
	 * @return the list
	 */
	public List<ClusteringDef> clusterings();

	/**
	 * Blacklists.
	 *
	 * @return the map
	 */
	public Map<String, List<String>> blacklists();

}
