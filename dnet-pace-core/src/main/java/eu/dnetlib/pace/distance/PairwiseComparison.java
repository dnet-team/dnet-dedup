package eu.dnetlib.pace.distance;

import eu.dnetlib.pace.condition.ConditionAlgo;
import eu.dnetlib.pace.config.Config;
import eu.dnetlib.pace.distance.eval.ConditionEvalMap;
import eu.dnetlib.pace.model.*;
import eu.dnetlib.pace.tree.support.MatchType;
import eu.dnetlib.pace.tree.support.TreeNodeDef;
import eu.dnetlib.pace.util.PaceException;
import eu.dnetlib.pace.util.Reporter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.List;
import java.util.Map;

/**
 * The compare between two documents is given by the weighted mean of the field distances
 */
public class PairwiseComparison {

	private static final Log log = LogFactory.getLog(PairwiseComparison.class);

	private Config config;

	public PairwiseComparison(final Config config) {
		this.config = config;
	}

	public boolean compare(final MapDocument a, final MapDocument b) {

		//verify sufficientConditions
		if (verify(a, b, config.sufficientConditions()).result() > 0)
			return true;

		//verify necessaryConditions
		if (verify(a, b, config.necessaryConditions()).result() < 0)
			return false;

		//evaluate the decision tree
		return evaluateTree(a, b, config.decisionTree()) == MatchType.MATCH;
	}

	private ConditionEvalMap verify(final Document a, final Document b, final List<ConditionAlgo> conditions) {
		final ConditionEvalMap res = new ConditionEvalMap();

		for (final ConditionAlgo cd : conditions) {
			final ConditionEvalMap map = cd.verify(a, b);
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

	public MatchType evaluateTree(final MapDocument doc1, final MapDocument doc2, final Map<String, TreeNodeDef> decisionTree){

		String current = "start";
		double similarity;

		while (MatchType.parse(current)==MatchType.UNDEFINED) {

			TreeNodeDef currentNode = decisionTree.get(current);
			//throw an exception if the node doesn't exist
			if (currentNode == null)
				throw new PaceException("The Tree Node doesn't exist: " + current);

			similarity = currentNode.evaluate(doc1, doc2);

			if (similarity == -1) {
				current = currentNode.getUndefined();
			}
			else if (similarity>=currentNode.getThreshold()){
				current = currentNode.getPositive();
			}
			else {
				current = currentNode.getNegative();
			}

		}

		return MatchType.parse(current);
	}

//	private Field getValue(final Document d, final FieldDef fd) {
//		final Field v = d.values(fd.getName());
//		if (fd.getLength() > 0) {
//
//			if (v instanceof FieldValueImpl) {
//				((FieldValueImpl) v).setValue(StringUtils.substring(v.stringValue(), 0, fd.getLength()));
//			} else if (v instanceof FieldListImpl) {
//				List<String> strings = ((FieldListImpl) v).stringList();
//				strings = strings.stream()
//						.limit(fd.getSize() > 0 ? fd.getSize() : strings.size())
//						.map(s -> StringUtils.substring(s, 0, fd.getLength()))
//						.collect(Collectors.toList());
//				((FieldListImpl) v).clear();
//				((FieldListImpl) v).addAll(strings.stream()
//						.limit(fd.getSize() > 0 ? fd.getSize() : strings.size())
//						.map(s -> StringUtils.substring(s, 0, fd.getLength()))
//						.map(s -> new FieldValueImpl(v.getType(), v.getName(), s))
//						.collect(Collectors.toList()));
//			}
//		}
//
//		return v;
//	}
//
//	private double sumWeights(final Collection<FieldDef> fields) {
//		double sum = 0.0;
//		for (final FieldDef fd : fields) {
//			sum += fd.getWeight();
//		}
//		return sum;
//	}

}
