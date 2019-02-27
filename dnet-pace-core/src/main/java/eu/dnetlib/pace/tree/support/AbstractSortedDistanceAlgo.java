package eu.dnetlib.pace.tree.support;

import com.google.common.collect.Lists;
import com.wcohen.ss.AbstractStringDistance;
import eu.dnetlib.pace.model.Field;
import eu.dnetlib.pace.model.FieldList;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public abstract class AbstractSortedDistanceAlgo extends AbstractDistanceAlgo {

    /**
     * Instantiates a new sorted second string distance algo.
     *
     * @param weight
     *            the weight
     * @param ssalgo
     *            the ssalgo
     */
    protected AbstractSortedDistanceAlgo(final double weight, final AbstractStringDistance ssalgo) {
        super(weight, ssalgo);
    }

    protected AbstractSortedDistanceAlgo(final Map<String, Number> params, final AbstractStringDistance ssalgo){
        super(params.get("weight").doubleValue(), ssalgo);
    }

    /*
     * (non-Javadoc)
     *
     * @see eu.dnetlib.pace.distance.AbstractDistanceAlgo#toList(eu.dnetlib.pace.model.Field)
     */
    @Override
    protected List<String> toList(final Field list) {
        FieldList fl = (FieldList) list;
        List<String> values = Lists.newArrayList(fl.stringList());
        Collections.sort(values);
        return values;
    }

}
