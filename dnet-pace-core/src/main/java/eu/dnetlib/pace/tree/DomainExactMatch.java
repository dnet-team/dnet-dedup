package eu.dnetlib.pace.tree;

import eu.dnetlib.pace.model.Field;
import eu.dnetlib.pace.tree.support.ComparatorClass;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

@ComparatorClass("domainExactMatch")
public class DomainExactMatch extends ExactMatchIgnoreCase {

    public DomainExactMatch(final Map<String, Number> params) {
        super(params);
    }

    @Override
    protected String getValue(final Field f) {
        try {
            return asUrl(super.getValue(f)).getHost();
        } catch (MalformedURLException e) {
            return "";
        }
    }

    private URL asUrl(final String value) throws MalformedURLException {
        return new URL(value);
    }
}
