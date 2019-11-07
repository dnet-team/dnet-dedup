package eu.dnetlib.support;

import org.apache.commons.logging.Log;

import java.io.Serializable;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicLong;

class Counters extends HashMap<String, HashMap<String, AtomicLong>> implements Serializable {

    public AtomicLong get(String counterGroup, String counterName) {
        if (!super.containsKey(counterGroup)) {
            super.put(counterGroup, new HashMap<>());
        }
        if (!super.get(counterGroup).containsKey(counterName)) {
            super.get(counterGroup).put(counterName, new AtomicLong(0));
        }
        return super.get(counterGroup).get(counterName);
    }

    public void print(final Log log) {
        entrySet().forEach(cg -> {
            cg.getValue().entrySet().forEach(cn -> {
                log.info(cg.getKey() + "  " + cn.getKey() + "  " + cn.getValue());
            });
        });
    }
}