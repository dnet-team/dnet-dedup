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
        return asUrl(super.getValue(f)).getHost();
    }

    @Override
    public double compare(Field a, Field b) {

        try {
            final String fa = getValue(a);
            final String fb = getValue(b);

            if (fa.isEmpty() || fb.isEmpty())
                return -1;

            return fa.equalsIgnoreCase(fb) ? 1 : 0;
        }
        catch (IllegalStateException e) {
            return -1;
        }
    }

    private URL asUrl(final String value) {
        try {
            return new URL(value);
        } catch (MalformedURLException e) {
            throw new IllegalStateException("invalid URL: " + value);
        }
    }
}