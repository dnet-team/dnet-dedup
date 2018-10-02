package eu.dnetlib.pace.model;

import java.lang.reflect.Type;

import com.google.gson.GsonBuilder;
import com.google.gson.InstanceCreator;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

/**
 * The Class MapDocumentSerializer.
 */
public class MapDocumentSerializer implements InstanceCreator<MapDocument> {

	@Override
	public MapDocument createInstance(final Type type) {
		return new MapDocument();
	}

	/**
	 * Decode.
	 *
	 * @param s
	 *            the String
	 * @return the map document
	 */
	public static MapDocument decode(final String s) {
		final GsonBuilder gson = new GsonBuilder();

		gson.registerTypeAdapter(Field.class, new JsonDeserializer<Field>() {

			@Override
			public Field deserialize(final JsonElement json, final Type typeOfT, final JsonDeserializationContext context) throws JsonParseException {
				final FieldListImpl fl = new FieldListImpl();
				if (json.isJsonObject()) {

					fl.add(handleJsonObject(json.getAsJsonObject()));

				} else if (json.isJsonArray()) {

					for (final JsonElement e : json.getAsJsonArray()) {
						if (e.isJsonObject()) {
							fl.add(handleJsonObject(e.getAsJsonObject()));
						}
					}
				}
				return fl;
			}

			private Field handleJsonObject(final JsonObject o) {
				final FieldListImpl fl = new FieldListImpl();
				final String name = o.get("name").getAsString();
				final String type = o.get("type").getAsString();
				final String value = o.get("value").getAsString();
				fl.add(new FieldValueImpl(eu.dnetlib.pace.config.Type.valueOf(type), name, value));
				return fl;
			}
		});

		return gson.create().fromJson(s, MapDocument.class);
	}

	/**
	 * Decode.
	 *
	 * @param bytes
	 *            the bytes
	 * @return the map document
	 */
	public static MapDocument decode(final byte[] bytes) {
		return decode(new String(bytes));
	}

	/**
	 * To string.
	 *
	 * @param doc
	 *            the doc
	 * @return the string
	 */
	public static String toString(final MapDocument doc) {
		final GsonBuilder b = new GsonBuilder();
		return b.setPrettyPrinting().create().toJson(doc);

	}

	/**
	 * To byte array.
	 *
	 * @param doc
	 *            the doc
	 * @return the byte[]
	 */
	public static byte[] toByteArray(final MapDocument doc) {
		return toString(doc).getBytes();
	}

}
