package eu.dnetlib.pace.model.adaptor;

import java.lang.reflect.Type;
import java.util.List;

import com.google.common.collect.Lists;
import com.google.gson.*;
import eu.dnetlib.pace.model.gt.GTAuthor;

/**
 * Created by claudio on 01/03/16.
 */
public class PidOafSerialiser implements JsonDeserializer<Pid> {

	private static final String VALUE = "value";

	private static final String QUALIFIER = "qualifier";
	private static final String CLASSID = "classid";

	@Override
	public Pid deserialize(final JsonElement json, final Type typeOfT, final JsonDeserializationContext context) throws JsonParseException {

		final Pid pid = new Pid();

		pid.setType(getType(json));
		pid.setValue(getValue(json));

		return pid;
	}

	private String getValue(final JsonElement json) {
		final JsonObject obj =json.getAsJsonObject();
		return obj.get(VALUE).getAsString();

	}

	private String getType(final JsonElement json) {

		final JsonObject obj =json.getAsJsonObject();

		if (!obj.has(QUALIFIER))
			throw new IllegalArgumentException("pid does not contain any type: " + json.toString());

		final JsonObject qualifier = obj.getAsJsonObject(QUALIFIER);

		final JsonElement classid = qualifier.get(CLASSID);

		return classid.getAsString();
	}
}
