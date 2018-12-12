package eu.dnetlib.pace.util;

public class PaceException extends RuntimeException {

    public PaceException(String s, Throwable e){
        super(s, e);
    }

    public PaceException(String s){
        super(s);
    }

}
