package ge.tsu.jmvc.core;

import java.util.HashSet;
import java.util.Set;

public abstract class Controllers {

    private static Set<ControllerInfo> CONTROLLERS;

    static {
        CONTROLLERS = new HashSet<>();
    }

    private Controllers() {
    }

    public synchronized static Set<ControllerInfo> getControllers() {
        return CONTROLLERS;
    }
}
