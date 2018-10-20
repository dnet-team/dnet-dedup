package eu.dnetlib.pace.condition;

import java.util.Map;

import eu.dnetlib.pace.distance.eval.ConditionEvalMap;
import eu.dnetlib.pace.model.Document;

/**
 * Allows to express general conditions to be satisfied or not between two Documents.
 * 
 * @author claudio
 */
public interface ConditionAlgo {

	/**
	 * Verify a condition.
	 * 
	 * @param a
	 *            the Document a
	 * @param b
	 *            the Document b
	 * @return 0 when condition cannot be verified (ignoremissing = true). Positive int when the condition is verified. Negative int when
	 *         the condition is not verified.
	 */
	public abstract ConditionEvalMap verify(Document a, Document b);

}