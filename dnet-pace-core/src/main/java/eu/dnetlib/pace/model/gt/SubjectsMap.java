package eu.dnetlib.pace.model.gt;

import java.util.HashMap;
import java.util.Map.Entry;

/**
 * Created by claudio on 07/03/16.
 */
public class SubjectsMap extends HashMap<String, Subjects> {

	public SubjectsMap mergeFrom(SubjectsMap sm) {

		for(Entry<String, Subjects> e : sm.entrySet()) {
			if (!this.containsKey(e.getKey())) {
				Subjects sub = new Subjects();

				sub.putAll(e.getValue());

				this.put(e.getKey(), sub);
			} else {
				for (Entry<String, Integer> es : e.getValue().entrySet()) {
					final Subjects subjects = this.get(e.getKey());
					if (subjects.containsKey(es.getKey())) {
						subjects.put(es.getKey(), es.getValue() + subjects.get(es.getKey()));
					} else {
						subjects.put(es.getKey(), new Integer(1));
					}
				}
			}
		}

		return this;
	}

}
