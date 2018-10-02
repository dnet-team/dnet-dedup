package eu.dnetlib.pace.distance.eval;

import java.util.HashMap;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

/**
 * Created by claudio on 09/03/16.
 */
public class ConditionEvalMap extends HashMap<String, ConditionEval> {


	public ConditionEvalMap mergeFrom(ConditionEvalMap map) {
		putAll(map);
		return this;
	}

	public boolean anyNegative() {
		return values().stream()
				.allMatch(ec -> ec.getResult() < 0);
	}

	public boolean isZero() {
		return result() == 0;
	}

	public int result() {
		int res = 0;
		for(ConditionEval ec : values()) {
			final int verify = ec.getResult();
			if (verify < 0) return -1;
			res += verify;
		}
		return res;
	}

}
