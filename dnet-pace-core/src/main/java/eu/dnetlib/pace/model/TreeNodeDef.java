package eu.dnetlib.pace.model;

import eu.dnetlib.pace.config.PaceConfig;
import eu.dnetlib.pace.tree.TreeNode;
import eu.dnetlib.pace.util.PaceException;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;
import java.io.Serializable;
import java.util.Map;

public class TreeNodeDef implements Serializable {

    private String name;
    private String field;

    private String positive;
    private String negative;
    private String undefined;

    private Map<String, Number> params;

    public TreeNodeDef() {
    }

    public TreeNodeDef(String name, String field, String positive, String negative, String undefined, Map<String, Number> params) {
        this.name = name;
        this.field = field;
        this.positive = positive;
        this.negative = negative;
        this.undefined = undefined;
        this.params = params;
    }

    public TreeNode treeNode() {
        try {
            return PaceConfig.paceResolver.getTreeNode(getName(), params);
        } catch (PaceException e) {
            e.printStackTrace();
            return null;
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public String getPositive() {
        return positive;
    }

    public void setPositive(String positive) {
        this.positive = positive;
    }

    public String getNegative() {
        return negative;
    }

    public void setNegative(String negative) {
        this.negative = negative;
    }

    public String getUndefined() {
        return undefined;
    }

    public void setUndefined(String undefined) {
        this.undefined = undefined;
    }

    public Map<String, Number> getParams() {
        return params;
    }

    public void setParams(Map<String, Number> params) {
        this.params = params;
    }

    @Override
    public String toString() {
        try {
            return new ObjectMapper().writeValueAsString(this);
        } catch (IOException e) {
            return e.getStackTrace().toString();
        }
    }
}