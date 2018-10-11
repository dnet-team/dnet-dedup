package eu.dnetlib;

import org.apache.spark.util.AccumulatorV2;

public class DnetAccumulator extends AccumulatorV2<Long, Long> {

    private Long counter= 0L;

    private String group;

    private String name;


    public DnetAccumulator(final String group, final String name){
        this.group = group;
        this.name = name;
    }


    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean isZero() {
        return counter == 0;
    }

    @Override
    public AccumulatorV2<Long, Long> copy() {
        final DnetAccumulator acc = new DnetAccumulator(group, name);
        acc.add(counter);
        return acc;
    }

    @Override
    public void reset() {
        counter = 0L;
    }

    @Override
    public void add(Long aLong) {
        counter += aLong;
    }

    @Override
    public void merge(AccumulatorV2<Long, Long> accumulatorV2) {
        add(accumulatorV2.value());
    }

    @Override
    public Long value() {
        return counter;
    }
}
