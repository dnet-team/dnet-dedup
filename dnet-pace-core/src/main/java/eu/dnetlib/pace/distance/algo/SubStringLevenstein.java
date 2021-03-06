package eu.dnetlib.pace.distance.algo;

import eu.dnetlib.pace.config.Config;
import eu.dnetlib.pace.distance.DistanceClass;
import eu.dnetlib.pace.distance.SecondStringDistanceAlgo;
import org.apache.commons.lang.StringUtils;

import com.wcohen.ss.AbstractStringDistance;

import eu.dnetlib.pace.config.Type;
import eu.dnetlib.pace.model.Field;

import java.util.Map;

/**
 * The Class SubStringLevenstein.
 */
@DistanceClass("SubStringLevenstein")
public class SubStringLevenstein extends SecondStringDistanceAlgo {

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

	public SubStringLevenstein(Map<String, Number> params){
		super(params, new com.wcohen.ss.Levenstein());
		this.limit = params.get("limit").intValue();
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
	 * @see eu.dnetlib.pace.distance.SecondStringDistanceAlgo#distance(eu.dnetlib.pace.model.Field, eu.dnetlib.pace.model.Field)
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
	 * @see eu.dnetlib.pace.distance.DistanceAlgo#getWeight()
	 */
	@Override
	public double getWeight() {
		return super.weight;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see eu.dnetlib.pace.distance.SecondStringDistanceAlgo#normalize(double)
	 */
	@Override
	protected double normalize(final double d) {
		return 1 / Math.pow(Math.abs(d) + 1, 0.1);
	}

}
