package eu.dnetlib.pace.util;


import java.io.Serializable;

public interface Reporter extends Serializable {

    void incrementCounter(String counterGroup, String counterName, long delta);

    void emit(String type, String from, String to);
}
