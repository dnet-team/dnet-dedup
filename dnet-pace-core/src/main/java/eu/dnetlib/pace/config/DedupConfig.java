package eu.dnetlib.pace.config;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.antlr.stringtemplate.StringTemplate;
import org.apache.commons.io.IOUtils;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import eu.dnetlib.pace.condition.ConditionAlgo;
import eu.dnetlib.pace.model.ClusteringDef;
import eu.dnetlib.pace.model.FieldDef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class DedupConfig implements Config, Serializable {

	private static final Log log = LogFactory.getLog(DedupConfig.class);

	private static String CONFIG_TEMPLATE = "dedupConfig.st";

	private PaceConfig pace;

	private WfConfig wf;

	private static Map<String, String> defaults = Maps.newHashMap();

	static {
		defaults.put("threshold", "0");
		defaults.put("run", "001");
		defaults.put("entityType", "result");
		defaults.put("orderField", "title");
		defaults.put("queueMaxSize", "2000");
		defaults.put("groupMaxSize", "10");
		defaults.put("slidingWindowSize", "200");
		defaults.put("rootBuilder", "result");
		defaults.put("includeChildren", "true");
	}

	public DedupConfig() {}

	public static DedupConfig load(final String json) {

		final DedupConfig config = new Gson().fromJson(json, DedupConfig.class);

		config.getPace().initModel();

		return config;
	}

	public static DedupConfig loadDefault() throws IOException {
		return loadDefault(new HashMap<String, String>());
	}

	public static DedupConfig loadDefault(final Map<String, String> params) throws IOException {

		final StringTemplate template = new StringTemplate(new DedupConfig().readFromClasspath(CONFIG_TEMPLATE));

		for (final Entry<String, String> e : defaults.entrySet()) {
			template.setAttribute(e.getKey(), e.getValue());
		}
		for (final Entry<String, String> e : params.entrySet()) {
			template.setAttribute(e.getKey(), e.getValue());
		}

		final String json = template.toString();
		return load(json);
	}

	private String readFromClasspath(final String resource) throws IOException {
		return IOUtils.toString(getClass().getResource(resource));
	}

	public PaceConfig getPace() {
		return pace;
	}

	public void setPace(final PaceConfig pace) {
		this.pace = pace;
	}

	public WfConfig getWf() {
		return wf;
	}

	public void setWf(final WfConfig wf) {
		this.wf = wf;
	}

	@Override
	public String toString() {
		return new GsonBuilder().setPrettyPrinting().create().toJson(this);
	}

	@Override
	public List<FieldDef> model() {
		return getPace().getModel();
	}

	@Override
	public Map<String, FieldDef> modelMap() {
		return getPace().getModelMap();
	}

	@Override
	public List<ConditionAlgo> strictConditions() {
		return getPace().getStrictConditionAlgos();
	}

	@Override
	public List<ConditionAlgo> conditions() {
		return getPace().getConditionAlgos();
	}

	@Override
	public List<ClusteringDef> clusterings() {
		return getPace().getClustering();
	}

	@Override
	public Map<String, List<String>> blacklists() {
		return getPace().getBlacklists();
	}

}
