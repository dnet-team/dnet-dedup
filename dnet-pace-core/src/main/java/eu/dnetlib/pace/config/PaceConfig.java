package eu.dnetlib.pace.config;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.Maps;
import com.ibm.icu.text.Transliterator;
import eu.dnetlib.pace.common.AbstractPaceFunctions;
import eu.dnetlib.pace.model.ClusteringDef;
import eu.dnetlib.pace.model.FieldDef;
import eu.dnetlib.pace.tree.support.TreeNodeDef;
import eu.dnetlib.pace.util.PaceResolver;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class PaceConfig extends AbstractPaceFunctions implements Serializable {

	private List<FieldDef> model;

	private List<ClusteringDef> clustering;
	private Map<String, TreeNodeDef> decisionTree;

	private Map<String, List<String>> blacklists;
	private Map<String, List<String>> synonyms;

	@JsonIgnore
	private Map<String, String> translationMap;

	@JsonIgnore
	private Map<String, FieldDef> modelMap;

	@JsonIgnore
	public static PaceResolver resolver = new PaceResolver();

	public PaceConfig() {}

	public void initModel() {
		modelMap = Maps.newHashMap();
		for (FieldDef fd : getModel()) {
			modelMap.put(fd.getName(), fd);
		}
	}

	public void initTranslationMap(){
		translationMap = Maps.newHashMap();

		Transliterator transliterator = Transliterator.getInstance("Any-Eng");
		for (String key : synonyms.keySet()) {
			for (String term : synonyms.get(key)){
				translationMap.put(
						fixAliases(transliterator.transliterate(term.toLowerCase())),
				key);
			}
		}
	}

	public Map<String, String> translationMap(){
		return translationMap;
	}

	public List<FieldDef> getModel() {
		return model;
	}

	public void setModel(final List<FieldDef> model) {
		this.model = model;
	}

	public List<ClusteringDef> getClustering() {
		return clustering;
	}

	public void setClustering(final List<ClusteringDef> clustering) {
		this.clustering = clustering;
	}

	public Map<String, TreeNodeDef> getDecisionTree() {
		return decisionTree;
	}

	public void setDecisionTree(Map<String, TreeNodeDef> decisionTree) {
		this.decisionTree = decisionTree;
	}

	public Map<String, List<String>> getBlacklists() {
		return blacklists;
	}

	public void setBlacklists(final Map<String, List<String>> blacklists) {
		this.blacklists = blacklists;
	}

	public Map<String, List<String>> getSynonyms() {
		return synonyms;
	}

	public void setSynonyms(Map<String, List<String>> synonyms) {
		this.synonyms = synonyms;
	}

	public Map<String, FieldDef> getModelMap() {
		return modelMap;
	}

	public void setModelMap(final Map<String, FieldDef> modelMap) {
		this.modelMap = modelMap;
	}

}
