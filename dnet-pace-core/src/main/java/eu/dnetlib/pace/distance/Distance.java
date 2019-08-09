package eu.dnetlib.pace.distance;

import eu.dnetlib.pace.config.Config;

public interface Distance<A> {

	public boolean between(A a, A b, Config config);
}
