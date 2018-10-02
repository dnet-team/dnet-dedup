package eu.dnetlib.pace.util;

import com.google.common.base.Function;

public class DotAbbreviations implements Function<String, String> {
	@Override
	public String apply(String s) {
		return s.length() == 1 ? s + "." : s;
	}
};