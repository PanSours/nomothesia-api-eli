package com.di.nomothesia.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.ErrorAttributes;
import org.springframework.boot.autoconfigure.web.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * Created by psour on 5/12/2016.
 *
 */
@Controller
public class NomothesiaErrorController implements ErrorController {
    public static final String PATH = "/error";
    public static final String V_ERROR = "error";
    public static final String M_ERROR = "error";

    //    @Value("${debug}")
    private boolean debug = false;

    @Autowired
    private ErrorAttributes errorAttributes;


    @RequestMapping (value = PATH)
    ModelAndView error(HttpServletRequest request, HttpServletResponse response) {
        // Appropriate HTTP response code (e.g. 404 or 500) is automatically set by Spring.
        // Here we just define response body.
        Error error = new Error(response.getStatus(), getErrorAttributes(request, debug));
        return new ModelAndView(V_ERROR, M_ERROR, error);
    }

    public Map<String, Object> getErrorAttributes(HttpServletRequest request, boolean includeStackTrace) {
        RequestAttributes requestAttributes = new ServletRequestAttributes(request);
        return errorAttributes.getErrorAttributes(requestAttributes, includeStackTrace);
    }

    @Override
    public String getErrorPath() {
        return PATH;
    }

}
