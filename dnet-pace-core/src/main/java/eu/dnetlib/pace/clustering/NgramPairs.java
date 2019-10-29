package eu.dnetlib.pace.clustering;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import eu.dnetlib.pace.config.Config;

@ClusteringClass("ngrampairs")
public class NgramPairs extends Ngrams {

	public NgramPairs(Map<String, Integer> params) {
		super(params);
	}
	
	@Override
	protected Collection<String> doApply(Config conf, String s) {
		return ngramPairs(Lists.newArrayList(getNgrams(s, param("ngramLen"), param("max") * 2, 1, 2)), param("max"));
	}

	protected Collection<String> ngramPairs(final List<String> ngrams, int maxNgrams) {
		Collection<String> res = Lists.newArrayList();
		int j = 0;
		for (int i = 0; i < ngrams.size() && res.size() < maxNgrams; i++) {
			if (++j >= ngrams.size()) {
				break;
			}
			res.add(ngrams.get(i) + ngrams.get(j));
			//System.out.println("-- " + concatNgrams);
		}
		return res;
	}

}
