package eu.dnetlib.support;

public class OptionsParameter {

    private String paramName;
    private String paramLongName;
    private String paramDescription;
    private boolean paramRequired;
    private boolean compressed;

    public OptionsParameter() {
    }

    public String getParamName() {
        return paramName;
    }

    public String getParamLongName() {
        return paramLongName;
    }

    public String getParamDescription() {
        return paramDescription;
    }

    public boolean isParamRequired() {
        return paramRequired;
    }

    public boolean isCompressed() {
        return compressed;
    }

    public void setCompressed(boolean compressed) {
        this.compressed = compressed;
    }
}