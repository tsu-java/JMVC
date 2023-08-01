package ge.tsu.jmvc.core;

import java.lang.reflect.Method;
import java.util.Objects;
import java.util.Set;

public class ControllerInfo {

    private Set<String> path;
    private RequestMethod requestMethod;
    private Method method;
    private Object controllerObject;

    public ControllerInfo(Set<String> path, RequestMethod requestMethod, Method method, Object controllerObject) {
        this.path = path;
        this.requestMethod = requestMethod;
        this.method = method;
        this.controllerObject = controllerObject;
    }

    public Set<String> getPath() {
        return path;
    }

    public void setPath(Set<String> path) {
        this.path = path;
    }

    public RequestMethod getRequestMethod() {
        return requestMethod;
    }

    public void setRequestMethod(RequestMethod requestMethod) {
        this.requestMethod = requestMethod;
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public Object getControllerObject() {
        return controllerObject;
    }

    public void setControllerObject(Object controllerObject) {
        this.controllerObject = controllerObject;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ControllerInfo that = (ControllerInfo) o;
        return Objects.equals(path, that.path) && requestMethod == that.requestMethod && Objects.equals(method, that.method) && Objects.equals(controllerObject, that.controllerObject);
    }

    @Override
    public int hashCode() {
        return Objects.hash(path, requestMethod, method, controllerObject);
    }

}
