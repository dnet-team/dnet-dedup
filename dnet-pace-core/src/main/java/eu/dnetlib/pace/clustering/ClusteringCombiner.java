package eu.dnetlib.pace.clustering;

import java.util.Collection;
import java.util.List;

import com.google.common.collect.Sets;

import eu.dnetlib.pace.config.Config;
import eu.dnetlib.pace.model.ClusteringDef;
import eu.dnetlib.pace.model.Document;
import eu.dnetlib.pace.model.Field;

public class ClusteringCombiner {

	public static Collection<String> combine(final Document a, final Config conf) {
		return new ClusteringCombiner().doCombine(a, conf.clusterings());
	}

	private Collection<String> doCombine(final Document a, final List<ClusteringDef> defs) {
		final Collection<String> res = Sets.newLinkedHashSet();
		for (final ClusteringDef cd : defs) {
			for (final String fieldName : cd.getFields()) {
				final Field values = a.values(fieldName);
				res.addAll(cd.clusteringFunction().apply((List<Field>) values));
			}
		}
		return res;
	}
}
