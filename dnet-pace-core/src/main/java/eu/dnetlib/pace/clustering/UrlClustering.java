package eu.dnetlib.pace.clustering;

import eu.dnetlib.pace.common.AbstractPaceFunctions;
import eu.dnetlib.pace.config.Config;
import eu.dnetlib.pace.model.Field;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@ClusteringClass("urlclustering")
public class UrlClustering extends AbstractPaceFunctions implements ClusteringFunction {

    protected Map<String, Integer> params;

    public UrlClustering(final Map<String, Integer> params) {
        this.params = params;
    }

    @Override
    public Collection<String> apply(final Config conf, List<Field> fields) {
        try {
            return fields.stream()
                    .filter(f -> !f.isEmpty())
                    .map(Field::stringValue)
                    .map(this::asUrl)
                    .map(URL::getHost)
                    .collect(Collectors.toCollection(HashSet::new));
        }
        catch (IllegalStateException e){
            return new HashSet<>();
        }
    }

    @Override
    public Map<String, Integer> getParams() {
        return null;
    }

    private URL asUrl(String value) {
        try {
            return new URL(value);
        } catch (MalformedURLException e) {
            // should not happen as checked by pace typing
            throw new IllegalStateException("invalid URL: " + value);
        }
    }


}
