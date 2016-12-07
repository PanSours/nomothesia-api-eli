package com.di.nomothesia.controller;

import com.di.nomothesia.NomothesiaException;
import com.di.nomothesia.model.GovernmentGazette;
import com.di.nomothesia.model.LegalDocument;
import com.di.nomothesia.service.LegislationService;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.ErrorAttributes;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.ModelAndView;

/**
 * Handles requests for the application home page.
 */
@Controller
public class HomeController {

    @Autowired
    LegislationService legislationService;

    @Autowired
    private ErrorAttributes errorAttributes;

    private static final int BUFFER_SIZE = 4096;
    private static final String TAGS = "tags";
    private static final String LD_VIEWED = "ldviewed";
    private static final String LD_RECENT = "ldrecent";
    private static final String LOCALE = "locale";
    private static final String HOME = "home";
    private static final String GAZS = "gazs";
    private static final String GAZETTE = "gazette";
    private static final String ABOUT_US = "aboutus";
    private static final String DEVELOPER = "developer";
    private static final String LISTS = "lists";
    private static final String STATS = "stats";

    @RequestMapping (value = "/", method = RequestMethod.GET)
    public String home(Model model, Locale locale) throws NomothesiaException {
        List<String> tags = legislationService.getTags();
        List<LegalDocument> ldviewed = legislationService.mostViewed();
        List<LegalDocument> ldrecent = legislationService.mostRecent();
        model.addAttribute(TAGS, tags);
        model.addAttribute(LD_VIEWED, ldviewed);
        model.addAttribute(LD_RECENT, ldrecent);
        model.addAttribute(LOCALE, locale);

        return HOME;
    }

    @RequestMapping (value = "/gazette", method = RequestMethod.GET)
    public String gazette(Model model, Locale locale) throws NomothesiaException {
        List<GovernmentGazette> gazs = legislationService.getFEKStatistics();
        model.addAttribute(GAZS, gazs);
        model.addAttribute(LOCALE, locale);

        return GAZETTE;
    }

    @RequestMapping (value = "/aboutus", method = RequestMethod.GET)
    public String aboutus(Locale locale, Model model) {
        model.addAttribute(LOCALE, locale);

        return ABOUT_US;
    }

    @RequestMapping (value = "/developer", method = RequestMethod.GET)
    public String developer(Locale locale, Model model) {
        model.addAttribute(LOCALE, locale);

        return DEVELOPER;
    }

    @RequestMapping (value = "/statistics", method = RequestMethod.GET)
    public String stats(Locale locale, Model model) throws NomothesiaException {
        List<ArrayList<String>> lists = legislationService.getStats();
        model.addAttribute(LISTS, lists);
        model.addAttribute(LOCALE, locale);

        return STATS;
    }

    @RequestMapping (value = "/legislation.owl", method = RequestMethod.GET)
    public void owlDownload(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String filePath = "/resources/datasets/legislation.owl";
        // get absolute path of the application
        //servletContext.contextPath
        ServletContext context = request.getSession().getServletContext();
        String appPath = context.getRealPath("");
        //System.out.println("appPath = " + appPath);
        // construct the complete absolute path of the file
        String fullPath = appPath + filePath;
        File downloadFile = new File(fullPath);
        FileInputStream inputStream = new FileInputStream(downloadFile);
        // get MIME type of the file
        String mimeType = context.getMimeType(fullPath);
        if (mimeType == null) {
            // set to binary type if MIME mapping not found
            mimeType = "application/octet-stream";
        }
        //System.out.println("MIME type: " + mimeType);
        // set content attributes for the response
        response.setContentType(mimeType);
        response.setContentLength((int) downloadFile.length());
        // set headers for the response
        String headerKey = "Content-Disposition";
        String headerValue = String.format("attachment; filename=\"%s\"", downloadFile.getName());
        response.setHeader(headerKey, headerValue);
        // get output stream of the response
        OutputStream outStream = response.getOutputStream();
        byte[] buffer = new byte[BUFFER_SIZE];
        int bytesRead = -1;
        // write bytes read from the input stream into the output stream
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            outStream.write(buffer, 0, bytesRead);
        }

        inputStream.close();
        outStream.close();
    }

    @RequestMapping (value = "/legislation.n3", method = RequestMethod.GET)
    public void n3Download(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String filePath = "/resources/datasets/legislation.n3";
        // get absolute path of the application
        //servletContext.contextPath
        ServletContext context = request.getSession().getServletContext();
        String appPath = context.getRealPath("");
        //System.out.println("appPath = " + appPath);
        // construct the complete absolute path of the file
        String fullPath = appPath + filePath;
        File downloadFile = new File(fullPath);
        FileInputStream inputStream = new FileInputStream(downloadFile);
        // get MIME type of the file
        String mimeType = context.getMimeType(fullPath);
        if (mimeType == null) {
            // set to binary type if MIME mapping not found
            mimeType = "application/octet-stream";
        }
        //System.out.println("MIME type: " + mimeType);
        // set content attributes for the response
        response.setContentType(mimeType);
        response.setContentLength((int) downloadFile.length());
        // set headers for the response
        String headerKey = "Content-Disposition";
        String headerValue = String.format("attachment; filename=\"%s\"", downloadFile.getName());
        response.setHeader(headerKey, headerValue);
        // get output stream of the response
        OutputStream outStream = response.getOutputStream();
        byte[] buffer = new byte[BUFFER_SIZE];
        int bytesRead = -1;
        // write bytes read from the input stream into the output stream
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            outStream.write(buffer, 0, bytesRead);
        }

        inputStream.close();
        outStream.close();
    }

    @ExceptionHandler ({NomothesiaException.class, Exception.class})
    @ResponseStatus (value = HttpStatus.INTERNAL_SERVER_ERROR)
    public ModelAndView handleError(HttpServletRequest request, Exception ex) {
        RequestAttributes requestAttributes = new ServletRequestAttributes(request);
        errorAttributes.getErrorAttributes(requestAttributes, false);
        Error error = new Error(HttpStatus.INTERNAL_SERVER_ERROR.value(), errorAttributes.getErrorAttributes
                (requestAttributes, false));

        return new ModelAndView(NomothesiaErrorController.V_ERROR, NomothesiaErrorController.M_ERROR, error);
    }

}
