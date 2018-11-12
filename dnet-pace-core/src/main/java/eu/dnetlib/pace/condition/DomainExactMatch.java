package eu.dnetlib.pace.condition;

import eu.dnetlib.pace.model.Field;
import eu.dnetlib.pace.model.FieldDef;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

@ConditionClass("DomainExactMatch")
public class DomainExactMatch extends ExactMatchIgnoreCase {

    public DomainExactMatch(String cond, List<FieldDef> fields) {
        super(cond, fields);
    }

    @Override
    protected String getValue(final Field f) {
        return asUrl(super.getValue(f)).getHost();
    }

    private URL asUrl(final String value) {
        try {
            return new URL(value);
        } catch (MalformedURLException e) {
            // should not happen as checked by pace typing
            throw new IllegalStateException("invalid URL: " + value);
        }
    }
}
