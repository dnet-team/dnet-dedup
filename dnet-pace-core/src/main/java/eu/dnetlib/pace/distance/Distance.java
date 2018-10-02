package eu.dnetlib.pace.distance;

import eu.dnetlib.pace.config.Config;
import eu.dnetlib.pace.distance.eval.ScoreResult;

public interface Distance<A> {

	public ScoreResult between(A a, A b, Config config);
}
