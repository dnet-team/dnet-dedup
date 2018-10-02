package eu.dnetlib.pace.util;

import org.apache.commons.lang.WordUtils;

import com.google.common.base.Function;

public class Capitalise implements Function<String, String> {

	private final char[] DELIM = { ' ', '-' };

	@Override
	public String apply(final String s) {
		return WordUtils.capitalize(s.toLowerCase(), DELIM);
	}
};
