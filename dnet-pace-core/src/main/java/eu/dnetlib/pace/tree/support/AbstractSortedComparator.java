package eu.dnetlib.pace.tree.support;

import com.google.common.collect.Lists;
import com.wcohen.ss.AbstractStringDistance;
import eu.dnetlib.pace.model.Field;
import eu.dnetlib.pace.model.FieldList;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public abstract class AbstractSortedComparator extends AbstractComparator {

    /**
     * Instantiates a new sorted second string compare algo.
     *
     * @param weight
     *            the weight
     * @param ssalgo
     *            the ssalgo
     */
    protected AbstractSortedComparator(final double weight, final AbstractStringDistance ssalgo) {
        super(weight, ssalgo);
    }

    protected AbstractSortedComparator(final Map<String, String> params, final AbstractStringDistance ssalgo){
        super(Double.parseDouble(params.get("weight")), ssalgo);
    }

    @Override
    protected List<String> toList(final Field list) {
        FieldList fl = (FieldList) list;
        List<String> values = Lists.newArrayList(fl.stringList());
        Collections.sort(values);
        return values;
    }

}
