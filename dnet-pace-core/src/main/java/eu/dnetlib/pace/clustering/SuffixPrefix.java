package eu.dnetlib.pace.clustering;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Sets;
import eu.dnetlib.pace.config.Config;

@ClusteringClass("suffixprefix")
public class SuffixPrefix extends AbstractClusteringFunction {

	public SuffixPrefix(Map<String, Integer> params) {
		super(params);
	}

	@Override
	protected Collection<String> doApply(Config conf, String s) {
		return suffixPrefix(s, param("len"), param("max"));
	}
	
	private Collection<String> suffixPrefix(String s, int len, int max) {
		final Set<String> bigrams = Sets.newLinkedHashSet();
		int i = 0;
		while (++i < s.length() && bigrams.size() < max) {
			int j = s.indexOf(" ", i);

			int offset = j + len + 1 < s.length() ? j + len + 1 : s.length();

			if (j - len > 0) {
				String bigram = s.substring(j - len, offset).replaceAll(" ", "").trim();
				if (bigram.length() >= 4) {
					bigrams.add(bigram);
				}
			}
		}
		return bigrams;
	}

}
