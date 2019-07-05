package com.fullsecurity.server;


import static android.R.attr.port;

public class MSSettings {
    private String name;
    private boolean returnToCaller;
    private String parent;
    private boolean active;

    public MSSettings(String name, boolean returnToCaller, String parent) {
        this.name = name;
        this.returnToCaller = returnToCaller;
        this.parent = parent;
        this.active = true;
    }

    public String toString() {
        return "["+name+","+port+","+returnToCaller+"]";
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isReturnToMe() {
        return returnToCaller;
    }

    public void setReturnToMe(boolean returnToCaller) {
        this.returnToCaller = returnToCaller;
    }

    public String getParent() {
        return parent;
    }

    public void setParent(String parent) {
        this.parent = parent;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
