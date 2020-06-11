package eu.dnetlib.support;

import java.io.IOException;
import java.io.Serializable;
import java.util.Set;

import eu.dnetlib.pace.utils.Utility;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.annotate.JsonIgnore;

import com.fasterxml.jackson.databind.ObjectMapper;

import eu.dnetlib.pace.util.PaceException;

public class ConnectedComponent implements Serializable {

    private Set<String> docs;
    private String ccId;

    public ConnectedComponent() {
    }

    public ConnectedComponent(Set<String> docs) {
        this.docs = docs;
        createID();
    }

    public String createID() {
        if (docs.size() > 1) {
            final String s = getMin();
            ccId = "dedup::" + Utility.md5(s);
            return ccId;
        } else {
            return docs.iterator().next();
        }
    }

    @JsonIgnore
    public String getMin() {

        final StringBuilder min = new StringBuilder();
        docs
                .forEach(
                        i -> {
                            if (StringUtils.isBlank(min.toString())) {
                                min.append(i);
                            } else {
                                if (min.toString().compareTo(i) > 0) {
                                    min.setLength(0);
                                    min.append(i);
                                }
                            }
                        });
        return min.toString();
    }

    @Override
    public String toString() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(this);
        } catch (IOException e) {
            throw new PaceException("Failed to create Json: ", e);
        }
    }

    public Set<String> getDocs() {
        return docs;
    }

    public void setDocs(Set<String> docs) {
        this.docs = docs;
    }

    public String getCcId() {
        return ccId;
    }

    public void setCcId(String ccId) {
        this.ccId = ccId;
    }
}
