package eu.dnetlib.pace.model.gt;

import java.lang.reflect.Type;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

public class GTAuthorOafSerialiser implements JsonDeserializer<GTAuthor> {

	private static final String VALUE = "value";
	private static final String SECONDNAMES = "secondnames";
	private static final String FIRSTNAME = "firstname";
	private static final String FULLNAME = "fullname";
	private static final String ID = "id";
	private static final String MERGEDPERSON = "mergedperson";
	private static final String METADATA = "metadata";
	private static final String ANCHOR_ID = "anchorId";
	private static final String COAUTHOR = "coauthor";

	@Override
	public GTAuthor deserialize(final JsonElement json, final Type typeOfT, final JsonDeserializationContext context) throws JsonParseException {
		final GTAuthor gta = new GTAuthor();

		gta.setAuthor(getAuthor(json));
		gta.setMerged(getMerged(json));

		gta.setCoAuthors(getCoAuthors(json));

		return gta;
	}

	private CoAuthors getCoAuthors(final JsonElement json) {
		final JsonObject obj = json.getAsJsonObject();
		if (!obj.has(COAUTHOR)) return null;
		return new CoAuthors(Lists.newArrayList(Iterables.transform(obj.get(COAUTHOR).getAsJsonArray(),
				new Function<JsonElement, CoAuthor>() {

					@Override
					public CoAuthor apply(final JsonElement in) {
						final CoAuthor a = new CoAuthor(getAuthor(in));
						final JsonObject jsonObject = in.getAsJsonObject();
						if (jsonObject.has(ANCHOR_ID)) {
							a.setAnchorId(jsonObject.get(ANCHOR_ID).getAsString());
						}
						return a;
					}
				})));
	}

	private Author getAuthor(final JsonElement json) {

		final Author a = new Author();
		a.setCoauthors(null);
		a.setMatches(null);

		final JsonObject jso = json.getAsJsonObject();

		a.setId(jso.has(ID) ? jso.get(ID).getAsString() : null);

		final JsonObject jsonObject = json.getAsJsonObject();
		if (jsonObject.has(METADATA)) {
			final JsonObject m = jsonObject.get(METADATA).getAsJsonObject();
			a.setFullname(getValue(m, FULLNAME));
			a.setFirstname(getValue(m, FIRSTNAME));
			a.setSecondnames(getValues(m, SECONDNAMES));
		}
		return a;
	}

	private Authors getMerged(final JsonElement json) {
		final JsonObject obj = json.getAsJsonObject();
		if (!obj.has(MERGEDPERSON)) return null;
		return new Authors(Lists.newArrayList(Iterables.transform(obj.get(MERGEDPERSON).getAsJsonArray(),
				new Function<JsonElement, Author>() {

					@Override
					public Author apply(final JsonElement in) {
						return getAuthor(in);
					}
				})));
	}

	private String getValues(final JsonObject m, final String fieldName) {
		return m.has(fieldName) ? Joiner.on(" ").join(Iterables.transform(m.get(fieldName).getAsJsonArray(), new Function<JsonElement, String>() {

			@Override
			public String apply(final JsonElement in) {
				return in.getAsJsonObject().get(VALUE).getAsString();
			}
		})) : null;
	}

	private String getValue(final JsonObject m, final String fieldName) {
		return m.has(fieldName) ? m.get(fieldName).getAsJsonObject().get(VALUE).getAsString() : null;
	}

}
