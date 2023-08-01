package ge.tsu.jmvc.controller;

import ge.tsu.jmvc.core.RequestMethod;
import ge.tsu.jmvc.core.annotation.Controller;
import ge.tsu.jmvc.core.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Controller
public class HomeController {

    @RequestMapping(path = {"/", "/home"}, method = RequestMethod.GET)
    public void home(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.getWriter().println("Hello from home ;)");
    }

}
