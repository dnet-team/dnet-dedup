//package eu.dnetlib.pace.distance;
//
//import eu.dnetlib.pace.config.Config;
//import eu.dnetlib.pace.distance.eval.ScoreResult;
//import eu.dnetlib.pace.model.Document;
//
//public abstract class AbstractDistance<A> implements Distance<A> {
//
//	protected abstract Document toDocument(A a);
//
//	@Override
//	public boolean between(final A a, final A b, final Config config) {
//		return new PairwiseComparison(config).compare(toDocument(a), toDocument(b));
//	}
//}
