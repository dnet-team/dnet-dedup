package eu.dnetlib.pace.tree.support;

import eu.dnetlib.pace.common.AbstractPaceFunctions;
import eu.dnetlib.pace.model.Field;

import java.util.Map;

public abstract class AbstractCondition extends AbstractPaceFunctions implements Comparator {

    public Map<String, Number> params;

    public AbstractCondition(Map<String, Number> params){
        this.params = params;
    }

    @Override
    public double compare(Field a, Field b) {
        return -1;      //returns undefined by default
    }

}
