package eu.dnetlib;


import java.io.IOException;
import java.io.Serializable;

public interface Reporter extends Serializable {

    void incrementCounter(String counterGroup, String counterName, long delta);

    void emit(final String type, final String from, final String to) throws IOException, InterruptedException;

}
