package eu.dnetlib.pace.clustering;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import com.google.common.collect.Sets;
import eu.dnetlib.pace.config.Config;

@ClusteringClass("acronyms")
public class Acronyms extends AbstractClusteringFunction {

	public Acronyms(Map<String, Integer> params) {
		super(params);
	}

	@Override
	protected Collection<String> doApply(Config conf, String s) {
		return extractAcronyms(s, param("max"), param("minLen"), param("maxLen"));
	}
	
	private Set<String> extractAcronyms(final String s, int maxAcronyms, int minLen, int maxLen) {
		
		final Set<String> acronyms = Sets.newLinkedHashSet();
		
		for (int i = 0; i < maxAcronyms; i++) {
			
			final StringTokenizer st = new StringTokenizer(s);
			final StringBuilder sb = new StringBuilder();
			
			while (st.hasMoreTokens()) {
				final String token = st.nextToken();
				if (sb.length() > maxLen) {
					break;
				}
				if (token.length() > 1 && i < token.length()) {
					sb.append(token.charAt(i));
				}
			}
			String acronym = sb.toString();
			if (acronym.length() > minLen) {
				acronyms.add(acronym);
			}
		}
		return acronyms;
	}

}
