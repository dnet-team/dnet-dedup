package eu.dnetlib.pace;

import java.io.IOException;
import java.io.StringWriter;

import com.sun.webkit.network.URLs;
import org.apache.commons.io.IOUtils;

import eu.dnetlib.pace.config.Type;
import eu.dnetlib.pace.model.Field;
import eu.dnetlib.pace.model.FieldValueImpl;
import org.junit.Test;

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

}
