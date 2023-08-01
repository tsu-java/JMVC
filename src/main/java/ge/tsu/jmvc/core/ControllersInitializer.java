package ge.tsu.jmvc.core;

import ge.tsu.jmvc.core.annotation.Controller;
import ge.tsu.jmvc.core.annotation.RequestMapping;
import org.jetbrains.annotations.NotNull;
import org.reflections.Reflections;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.reflections.scanners.Scanners.SubTypes;
import static org.reflections.scanners.Scanners.TypesAnnotated;

@WebListener("This listener registers controllers")
public class ControllersInitializer implements ServletContextListener {
    private static final Logger log = Logger.getLogger(ControllersInitializer.class.getName());

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        String basePackage = sce.getServletContext().getInitParameter("base-package");
        if (basePackage == null) {
            log.warning("No base package specified! Skipping component scan.");
            return;
        }
        log.log(Level.FINE, "Looking for controllers in base package: '{0}' ...", basePackage);
        try {
            int foundAmount = findControllersFrom(basePackage);
            log.log(Level.FINE, "Found {0} controllers", foundAmount);
        } catch (NoSuchMethodException | InvocationTargetException | InstantiationException |
                 IllegalAccessException e) {
            log.log(Level.SEVERE, "Error while registering controllers", e);
        }
    }

    private int findControllersFrom(@NotNull String basePackage) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        int foundControllers = 0;
        Reflections reflections = new Reflections(basePackage);

        Set<Class<?>> controllerClasses = reflections.get(
                SubTypes.of(TypesAnnotated.with(Controller.class)).asClass());

        for (Class<?> controllerClass : controllerClasses) {
            // Look for methods that are annotated with RequestMapping
            for (Method method : controllerClass.getDeclaredMethods()) {
                method.setAccessible(true);
                if (!method.isAnnotationPresent(RequestMapping.class)) {
                    continue;
                }
                Class<?>[] parameterTypes = method.getParameterTypes();
                if (parameterTypes.length < 2) {
                    log.log(Level.WARNING, "HttpServletRequest and HttpServletResponse parameters should be present in controller");
                    continue;
                }
                Class<?> firstParam = parameterTypes[0];
                if (!firstParam.isAssignableFrom(HttpServletRequest.class)) {
                    log.log(Level.WARNING, "{0} method's first parameter's type was not HttpServletRequest", method.getName());
                    continue;
                }
                Class<?> secondParam = parameterTypes[1];
                if (!secondParam.isAssignableFrom(HttpServletResponse.class)) {
                    log.log(Level.WARNING, "{0} method's second parameter's type was not HttpServletResponse", method.getName());
                    continue;
                }

                Constructor<?> constructor = controllerClass.getConstructor();
                Object constructorObject = constructor.newInstance();

                RequestMapping reqMapping = method.getAnnotation(RequestMapping.class);

                // Create set of paths
                Set<String> paths = new HashSet<>();
                Collections.addAll(paths, reqMapping.path());

                // Paths should be unique!
                for (ControllerInfo controllerInfo: Controllers.getControllers()) {
                    for (String existingPath : controllerInfo.getPath()) {
                        if (paths.contains(existingPath)) {
                            throw new RuntimeException("Already existing controller for path: " + existingPath);
                        }
                    }
                }
                ControllerInfo controllerInfo = new ControllerInfo(
                        paths, reqMapping.method(), method, constructorObject
                );
                Controllers.getControllers().add(controllerInfo);
                foundControllers += 1;
            }
        }
        return foundControllers;
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        ServletContextListener.super.contextDestroyed(sce);
    }
}
