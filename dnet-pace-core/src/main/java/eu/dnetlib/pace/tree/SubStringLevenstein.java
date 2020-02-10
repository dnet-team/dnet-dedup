package eu.dnetlib.pace.tree;

import eu.dnetlib.pace.config.Config;
import org.apache.commons.lang3.StringUtils;

import com.wcohen.ss.AbstractStringDistance;
import eu.dnetlib.pace.config.Type;
import eu.dnetlib.pace.model.Field;
import eu.dnetlib.pace.tree.support.AbstractComparator;
import eu.dnetlib.pace.tree.support.ComparatorClass;


import java.util.Map;

/**
 * The Class SubStringLevenstein.
 */
@ComparatorClass("subStringLevenstein")
public class SubStringLevenstein extends AbstractComparator {

	/** The limit. */
	protected int limit;

	/**
	 * Instantiates a new sub string levenstein.
	 * 
	 * @param w
	 *            the w
	 */
	public SubStringLevenstein(final double w) {
		super(w, new com.wcohen.ss.Levenstein());
	}

	public SubStringLevenstein(Map<String, String> params){
		super(params, new com.wcohen.ss.Levenstein());
		this.limit = Integer.parseInt(params.getOrDefault("limit", "1"));
	}

	/**
	 * Instantiates a new sub string levenstein.
	 * 
	 * @param w
	 *            the w
	 * @param limit
	 *            the limit
	 */
	public SubStringLevenstein(final double w, final int limit) {
		super(w, new com.wcohen.ss.Levenstein());
		this.limit = limit;
	}

	/**
	 * Instantiates a new sub string levenstein.
	 * 
	 * @param w
	 *            the w
	 * @param limit
	 *            the limit
	 * @param ssalgo
	 *            the ssalgo
	 */
	protected SubStringLevenstein(final double w, final int limit, final AbstractStringDistance ssalgo) {
		super(w, ssalgo);
		this.limit = limit;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see eu.dnetlib.pace.compare.SecondStringDistanceAlgo#compare(eu.dnetlib.pace.model.Field, eu.dnetlib.pace.model.Field)
	 */
	@Override
	public double distance(final Field a, final Field b, final Config conf) {
		if (a.getType().equals(Type.String) && b.getType().equals(Type.String))
			return distance(StringUtils.left(a.stringValue(), limit), StringUtils.left(b.stringValue(), limit), conf);

		throw new IllegalArgumentException("invalid types\n- A: " + a.toString() + "\n- B: " + b.toString());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see eu.dnetlib.pace.compare.DistanceAlgo#getWeight()
	 */
	@Override
	public double getWeight() {
		return super.weight;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see eu.dnetlib.pace.compare.SecondStringDistanceAlgo#normalize(double)
	 */
	@Override
	protected double normalize(final double d) {
		return 1 / Math.pow(Math.abs(d) + 1, 0.1);
	}

}
