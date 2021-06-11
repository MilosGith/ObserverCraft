package mcproxy.util;

public class Modified {
    private Boolean isModified;

    public Modified(Boolean bool) {
        this.setModified(bool);
    }

    public Boolean getModified() {
        return isModified;
    }

    public void setModified(Boolean modified) {
        isModified = modified;
    }
}
