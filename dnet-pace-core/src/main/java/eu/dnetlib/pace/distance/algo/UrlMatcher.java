package eu.dnetlib.pace.distance.algo;

import eu.dnetlib.pace.config.Config;
import eu.dnetlib.pace.distance.DistanceClass;
import eu.dnetlib.pace.model.Field;
import org.apache.commons.lang.StringUtils;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

@DistanceClass("urlMatcher")
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
    public double distance(Field a, Field b, final Config conf) {

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

        return hostW + pathW * super.distance(urlA.getPath(), urlB.getPath(), conf);
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
