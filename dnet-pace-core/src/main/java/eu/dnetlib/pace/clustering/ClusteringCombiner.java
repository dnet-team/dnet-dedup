package eu.dnetlib.pace.clustering;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import com.google.common.collect.Sets;

import eu.dnetlib.pace.config.Config;
import eu.dnetlib.pace.model.ClusteringDef;
import eu.dnetlib.pace.model.Document;
import eu.dnetlib.pace.model.Field;
import eu.dnetlib.pace.model.FieldValueImpl;
import org.apache.commons.lang3.StringUtils;

public class ClusteringCombiner {

	private static String SEPARATOR = ":";
	private static String COLLAPSE_ON= "collapseOn";

	public static Collection<String> combine(final Document a, final Config conf) {
		return new ClusteringCombiner().doCombine(a, conf);
	}

	private Collection<String> doCombine(final Document a, final Config conf) {
		final Collection<String> res = Sets.newLinkedHashSet();
		for (final ClusteringDef cd : conf.clusterings()) {
			for (final String fieldName : cd.getFields()) {
				String prefix = getPrefix(cd, fieldName);

				Field values = a.values(fieldName);
				List<Field> fields = new ArrayList<>();

				if (values instanceof FieldValueImpl) {
					fields.add(values);
				}
				else {
					fields.addAll((List<Field>) values);
				}

				res.addAll(
						cd.clusteringFunction()
								.apply(conf, fields)
								.stream()
								.map(k -> prefix + SEPARATOR +k)
								.collect(Collectors.toList())
				);
			}
		}
		return res;
	}

	private String getPrefix(ClusteringDef cd, String fieldName) {
		return cd.getName()+ SEPARATOR +
				cd.getParams().keySet()
						.stream()
						.filter(k -> k.contains(COLLAPSE_ON))
						.findFirst()
						.map(k -> StringUtils.substringAfter(k, SEPARATOR))
						.orElse(fieldName);
	}

}
