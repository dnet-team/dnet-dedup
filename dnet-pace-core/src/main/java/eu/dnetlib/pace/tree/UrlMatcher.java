package eu.dnetlib.pace.tree;

import eu.dnetlib.pace.model.Field;
import eu.dnetlib.pace.tree.support.ComparatorClass;
import org.apache.commons.lang.StringUtils;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

@ComparatorClass("urlMatcher")
public class UrlMatcher extends Levenstein {

    private Map<String, Number> params;

    public UrlMatcher(Map<String, Number> params){
        super(params);
        this.params = params;
    }

    public UrlMatcher(double weight, Map<String, Number> params) {
        super(weight);
        this.params = params;
    }

    public void setParams(Map<String, Number> params) {
        this.params = params;
    }

    @Override
    public double compare(Field a, Field b) {

        final URL urlA = asUrl(getFirstValue(a));
        final URL urlB = asUrl(getFirstValue(b));

        if (!urlA.getHost().equalsIgnoreCase(urlB.getHost())) {
            return 0.0;
        }

        Double hostW = params.get("host").doubleValue();
        Double pathW = params.get("path").doubleValue();

        if (StringUtils.isBlank(urlA.getPath()) || StringUtils.isBlank(urlB.getPath())) {
            return hostW * 0.5;
        }

        return hostW + pathW * super.distance(urlA.getPath(), urlB.getPath());
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
