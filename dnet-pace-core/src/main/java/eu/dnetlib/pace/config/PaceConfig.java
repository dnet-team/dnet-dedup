package eu.dnetlib.pace.config;

import com.google.common.collect.Maps;
import eu.dnetlib.pace.model.ClusteringDef;
import eu.dnetlib.pace.model.FieldDef;
import eu.dnetlib.pace.model.TreeNodeDef;
import eu.dnetlib.pace.util.PaceResolver;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class PaceConfig implements Serializable {

	private List<FieldDef> model;
	private List<ClusteringDef> clustering;
	private Map<String, List<String>> blacklists;

	private Map<String, TreeNodeDef> decisionTree;

	private Map<String, FieldDef> modelMap;

	public static PaceResolver paceResolver;

	public PaceConfig() {}

	public void initModel() {
		modelMap = Maps.newHashMap();
		for(FieldDef fd : getModel()) {
			modelMap.put(fd.getName(), fd);
		}

		paceResolver = new PaceResolver();
	}

	public List<FieldDef> getModel() {
		return model;
	}

	public void setModel(final List<FieldDef> model) {
		this.model = model;
	}

	public Map<String, TreeNodeDef> getDecisionTree() {
		return decisionTree;
	}

	public void setDecisionTree(Map<String, TreeNodeDef> decisionTree) {
		this.decisionTree = decisionTree;
	}

	public List<ClusteringDef> getClustering() {
		return clustering;
	}

	public void setClustering(final List<ClusteringDef> clustering) {
		this.clustering = clustering;
	}

	public Map<String, List<String>> getBlacklists() {
		return blacklists;
	}

	public void setBlacklists(final Map<String, List<String>> blacklists) {
		this.blacklists = blacklists;
	}

	public Map<String, FieldDef> getModelMap() {
		return modelMap;
	}

	public void setModelMap(final Map<String, FieldDef> modelMap) {
		this.modelMap = modelMap;
	}
}
