package eu.dnetlib.pace.config;

import java.util.List;
import java.util.Map;

import eu.dnetlib.pace.condition.ConditionAlgo;
import eu.dnetlib.pace.model.ClusteringDef;
import eu.dnetlib.pace.model.FieldDef;

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
	 * Strict Pre-Condition definitions.
	 *
	 * @return the list of conditions
	 */
	public List<ConditionAlgo> strictConditions();

	/**
	 * Pre-Condition definitions.
	 *
	 * @return the list of conditions
	 */
	public List<ConditionAlgo> conditions();

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
