package eu.dnetlib.pace.util;


import com.google.common.base.Function;
import org.apache.commons.lang3.text.WordUtils;

public class Capitalise implements Function<String, String> {

    private final char[] DELIM = {' ', '-'};

    @Override
    public String apply(final String s) {
        return WordUtils.capitalize(s.toLowerCase(), DELIM);
    }
};
