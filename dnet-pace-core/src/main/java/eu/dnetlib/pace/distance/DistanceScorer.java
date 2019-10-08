package eu.dnetlib.pace.distance;

import eu.dnetlib.pace.condition.ConditionAlgo;
import eu.dnetlib.pace.config.Config;
import eu.dnetlib.pace.distance.eval.ConditionEvalMap;
import eu.dnetlib.pace.distance.eval.DistanceEval;
import eu.dnetlib.pace.distance.eval.DistanceEvalMap;
import eu.dnetlib.pace.distance.eval.ScoreResult;
import eu.dnetlib.pace.model.*;
import eu.dnetlib.pace.util.PaceException;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * The distance between two documents is given by the weighted mean of the field distances
 */
public class DistanceScorer {

	private static final Log log = LogFactory.getLog(DistanceScorer.class);

	private Config config;

	public DistanceScorer(final Config config) {
		this.config = config;
	}

	public ScoreResult distance(final Document a, final Document b) {
		final ScoreResult sr = new ScoreResult();	//to keep track of the result of the comparison

		sr.setStrictConditions(verify(a, b, config.strictConditions()));
		sr.setConditions(verify(a, b, config.conditions()));

		final DistanceEvalMap dMap = new DistanceEvalMap(sumWeights(config.model()));

		for (final FieldDef fd : config.model()) {

			dMap.updateDistance(fieldDistance(a, b, fd));
		}
		sr.setDistances(dMap);
		return sr;
	}

	private ConditionEvalMap verify(final Document a, final Document b, final List<ConditionAlgo> conditions) {
		final ConditionEvalMap res = new ConditionEvalMap();

		for (final ConditionAlgo cd : conditions) {
			final ConditionEvalMap map = cd.verify(a, b, config);
			res.mergeFrom(map);

			// commented out shortcuts
			/*
			if (map.anyNegative()) {
				return res;
			}
			*/

			//if (strict && (res < 0)) return -1;
			//cond += verify;
		}
		return res;
	}

	private DistanceEval fieldDistance(final Document a, final Document b, final FieldDef fd) {

		final double w = fd.getWeight();
		final Field va = getValue(a, fd);
		final Field vb = getValue(b, fd);

		final DistanceEval de = new DistanceEval(fd, va, vb);
		if ((w == 0)) return de; // optimization for 0 weight
		else {
			if (va.isEmpty() || vb.isEmpty()) {
				if (fd.isIgnoreMissing()) {
					de.setDistance(-1);
				} else {
					de.setDistance(w);
				}
			} else {
				if (va.getType().equals(vb.getType())) {
					de.setDistance(w * fd.distanceAlgo().distance(va, vb, config));
				} else {
					throw new PaceException(String.format("Types are different: %s:%s - %s:%s", va, va.getType(), vb, vb.getType()));
				}
			}
			return de;
		}
	}

	private Field getValue(final Document d, final FieldDef fd) {
		final Field v = d.values(fd.getName());
		if (fd.getLength() > 0) {

			if (v instanceof FieldValueImpl) {
				((FieldValueImpl) v).setValue(StringUtils.substring(v.stringValue(), 0, fd.getLength()));
			} else if (v instanceof FieldListImpl) {
				List<String> strings = ((FieldListImpl) v).stringList();
				strings = strings.stream()
						.limit(fd.getSize() > 0 ? fd.getSize() : strings.size())
						.map(s -> StringUtils.substring(s, 0, fd.getLength()))
						.collect(Collectors.toList());
				((FieldListImpl) v).clear();
				((FieldListImpl) v).addAll(strings.stream()
						.limit(fd.getSize() > 0 ? fd.getSize() : strings.size())
						.map(s -> StringUtils.substring(s, 0, fd.getLength()))
						.map(s -> new FieldValueImpl(v.getType(), v.getName(), s))
						.collect(Collectors.toList()));
			}
		}

		return v;
	}

	private double sumWeights(final Collection<FieldDef> fields) {
		double sum = 0.0;
		for (final FieldDef fd : fields) {
			sum += fd.getWeight();
		}
		return sum;
	}

}
