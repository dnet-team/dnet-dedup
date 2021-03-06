package eu.dnetlib.pace.config;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gson.GsonBuilder;
import eu.dnetlib.pace.util.PaceException;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;

public class WfConfig implements Serializable {

	/**
	 * Entity type.
	 */
	private String entityType = "";

	/**
	 * Sub-Entity type refers to one of fields declared in the model. See eu.dnetlib.pace.config.PaceConfig.modelMap
	 */
	private String subEntityType = "";

	/**
	 * Sub-Entity value declares a value for subTypes to be considered.
	 */
	private String subEntityValue = "";

	/**
	 * Field name used to sort the values in the reducer phase.
	 */
	private String orderField = "";

	/**
	 * Column Families involved in the relations redirection.
	 */
	private List<String> rootBuilder = Lists.newArrayList();

	/**
	 * Set of datasource namespace prefixes that won't be deduplicated.
	 */
	private Set<String> skipList = Sets.newHashSet();

	/**
	 * Subprefix used to build the root id, allows multiple dedup runs.
	 */
	private String dedupRun = "";

	/**
	 * Similarity threshold.
	 */
	private double threshold = 0;

	/** The queue max size. */
	private int queueMaxSize = 2000;

	/** The group max size. */
	private int groupMaxSize;

	/** The sliding window size. */
	private int slidingWindowSize;

	/** The configuration id. */
	private String configurationId;

	/** The include children. */
	private boolean includeChildren;

	/** Default maximum number of allowed children. */
	private final static int MAX_CHILDREN = 10;

	/** Maximum number of allowed children. */
	private int maxChildren = MAX_CHILDREN;

	public WfConfig() {}

	/**
	 * Instantiates a new dedup config.
	 *
	 * @param entityType
	 *            the entity type
	 * @param orderField
	 *            the order field
	 * @param rootBuilder
	 *            the root builder families
	 * @param dedupRun
	 *            the dedup run
	 * @param threshold
	 *            the threshold
	 * @param skipList
	 *            the skip list
	 * @param queueMaxSize
	 *            the queue max size
	 * @param groupMaxSize
	 *            the group max size
	 * @param slidingWindowSize
	 *            the sliding window size
	 * @param includeChildren
	 *            allows the children to be included in the representative records or not.
	 */
	public WfConfig(final String entityType, final String orderField, final List<String> rootBuilder, final String dedupRun,
			final double threshold,
			final Set<String> skipList, final int queueMaxSize, final int groupMaxSize, final int slidingWindowSize, final boolean includeChildren) {
		super();
		this.entityType = entityType;
		this.orderField = orderField;
		this.rootBuilder = rootBuilder;
		this.dedupRun = cleanupStringNumber(dedupRun);
		this.threshold = threshold;
		this.skipList = skipList;
		this.queueMaxSize = queueMaxSize;
		this.groupMaxSize = groupMaxSize;
		this.slidingWindowSize = slidingWindowSize;
		this.includeChildren = includeChildren;
	}

	/**
	 * Cleanup string number.
	 *
	 * @param s
	 *            the s
	 * @return the string
	 */
	private String cleanupStringNumber(final String s) {
		return s.contains("'") ? s.replaceAll("'", "") : s;
	}

	public boolean hasSubType() {
		return StringUtils.isNotBlank(getSubEntityType()) && StringUtils.isNotBlank(getSubEntityValue());
	}

	public String getEntityType() {
		return entityType;
	}

	public void setEntityType(final String entityType) {
		this.entityType = entityType;
	}

	public String getSubEntityType() {
		return subEntityType;
	}

	public void setSubEntityType(final String subEntityType) {
		this.subEntityType = subEntityType;
	}

	public String getSubEntityValue() {
		return subEntityValue;
	}

	public void setSubEntityValue(final String subEntityValue) {
		this.subEntityValue = subEntityValue;
	}

	public String getOrderField() {
		return orderField;
	}

	public void setOrderField(final String orderField) {
		this.orderField = orderField;
	}

	public List<String> getRootBuilder() {
		return rootBuilder;
	}

	public void setRootBuilder(final List<String> rootBuilder) {
		this.rootBuilder = rootBuilder;
	}

	public Set<String> getSkipList() {
		return skipList != null ? skipList : new HashSet<String>();
	}

	public void setSkipList(final Set<String> skipList) {
		this.skipList = skipList;
	}

	public String getDedupRun() {
		return dedupRun;
	}

	public void setDedupRun(final String dedupRun) {
		this.dedupRun = dedupRun;
	}

	public double getThreshold() {
		return threshold;
	}

	public void setThreshold(final double threshold) {
		this.threshold = threshold;
	}

	public int getQueueMaxSize() {
		return queueMaxSize;
	}

	public void setQueueMaxSize(final int queueMaxSize) {
		this.queueMaxSize = queueMaxSize;
	}

	public int getGroupMaxSize() {
		return groupMaxSize;
	}

	public void setGroupMaxSize(final int groupMaxSize) {
		this.groupMaxSize = groupMaxSize;
	}

	public int getSlidingWindowSize() {
		return slidingWindowSize;
	}

	public void setSlidingWindowSize(final int slidingWindowSize) {
		this.slidingWindowSize = slidingWindowSize;
	}

	public String getConfigurationId() {
		return configurationId;
	}

	public void setConfigurationId(final String configurationId) {
		this.configurationId = configurationId;
	}

	public boolean isIncludeChildren() {
		return includeChildren;
	}

	public void setIncludeChildren(final boolean includeChildren) {
		this.includeChildren = includeChildren;
	}

	public int getMaxChildren() {
		return maxChildren;
	}

	public void setMaxChildren(final int maxChildren) {
		this.maxChildren = maxChildren;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		try {
			return new ObjectMapper().writeValueAsString(this);
		} catch (IOException e) {
			throw new PaceException("unable to serialise " + this.getClass().getName(), e);
		}
	}

}
