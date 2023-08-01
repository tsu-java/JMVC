package ge.tsu.jmvc.core;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.InvocationTargetException;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This is the DispatcherServlet which captures all requests
 * and redirects them to registered controllers.
 */
public class DispatcherServlet extends HttpServlet {
    private static final Logger log = Logger.getLogger(DispatcherServlet.class.getName());

    private Pattern requestUrlPattern;
    private String contextPath;

    @Override
    public void init(ServletConfig config) {
        requestUrlPattern = Pattern.compile(
                "((http)|(https)):\\/\\/((\\w+:\\d+)|(\\w+\\.\\w+))(.*)", Pattern.CASE_INSENSITIVE);

        // Better have null than an empty string
        contextPath = config.getServletContext().getContextPath().isEmpty() ?
                null : config.getServletContext().getContextPath();
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) {
        log.log(Level.INFO, "Called dispatcher with remote host: {0}, context path: {1}, request URL: {2}, and query string: {3}",
                new Object[]{req.getRemoteHost(), req.getContextPath(), req.getRequestURL(), req.getQueryString()});

        // (1) Extract request path from request url
        String requestPath = getRequestPath(req);

        // (2) Find controller which is registered at that path
        Optional<ControllerInfo> foundController = Controllers.getControllers().stream()
                .filter(c -> c.getPath().contains(requestPath))
                .findFirst();

        ControllerInfo controllerInfo = foundController.orElseThrow(() -> new IllegalArgumentException("Couldn't find controller for that path!"));

        // (3) Forward request to that controller
        try {
            controllerInfo.getMethod().invoke(controllerInfo.getControllerObject(), req, resp);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    private String getRequestPath(HttpServletRequest req) {
        Matcher matcher = requestUrlPattern.matcher(req.getRequestURL());
        if (matcher.find()) {
            String requestPath = matcher.group(matcher.groupCount());
            if (contextPath != null) {
                requestPath = requestPath.substring(
                        requestPath.indexOf(contextPath) + contextPath.length());
            }
            return requestPath;
        } else {
            throw new IllegalArgumentException("Couldn't find request path in the request URL");
        }
    }

}
