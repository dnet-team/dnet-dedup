package eu.dnetlib.pace;

import eu.dnetlib.pace.config.Type;
import eu.dnetlib.pace.model.Field;
import eu.dnetlib.pace.model.FieldListImpl;
import eu.dnetlib.pace.model.FieldValueImpl;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.StringWriter;
import java.util.List;
import java.util.stream.Collectors;

public abstract class AbstractPaceTest {

	protected String readFromClasspath(final String filename) {
		final StringWriter sw = new StringWriter();
		try {
			IOUtils.copy(getClass().getResourceAsStream(filename), sw);
			return sw.toString();
		} catch (final IOException e) {
			throw new RuntimeException("cannot load resource from classpath: " + filename);
		}
	}

	protected Field title(final String s) {
		return new FieldValueImpl(Type.String, "title", s);
	}

	protected Field person(final String s) {
		return new FieldValueImpl(Type.JSON, "person", s);
	}

	protected Field url(final String s) {
		return new FieldValueImpl(Type.URL, "url", s);
	}

	protected Field createFieldList(List<String> strings, String fieldName){

		List<FieldValueImpl> fieldValueStream = strings.stream().map(s -> new FieldValueImpl(Type.String, fieldName, s)).collect(Collectors.toList());

		FieldListImpl a = new FieldListImpl();
		a.addAll(fieldValueStream);

		return a;

	}
}
