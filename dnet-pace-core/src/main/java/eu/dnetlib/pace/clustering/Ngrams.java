package eu.dnetlib.pace.clustering;

import eu.dnetlib.pace.config.Config;

import java.util.*;

@ClusteringClass("ngrams")
public class Ngrams extends AbstractClusteringFunction {

	public Ngrams(Map<String, Integer> params) {
		super(params);
	}

	@Override
	protected Collection<String> doApply(Config conf, String s) {
		return getNgrams(s, param("ngramLen"), param("max"), param("maxPerToken"), param("minNgramLen"));
	}

	protected Collection<String> getNgrams(String s, int ngramLen, int max, int maxPerToken, int minNgramLen) {

		final Collection<String> ngrams = new LinkedHashSet<String>();
		final StringTokenizer st = new StringTokenizer(s);

		while (st.hasMoreTokens()) {
			final String token = st.nextToken();
			if (!token.isEmpty()) {

				for (int i = 0; i < maxPerToken && ngramLen + i <= token.length(); i++) {
					String ngram = (token + "    ").substring(i, ngramLen + i).trim();
					if (ngrams.size() >= max) {
						return ngrams;
					}
					if (ngram.length() >= minNgramLen) {
						ngrams.add(ngram);
					}
				}
			}
		}
		//System.out.println(ngrams + " n: " + ngrams.size());
		return ngrams;
	}

}
