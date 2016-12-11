package com.di.nomothesia.controller;

import com.di.nomothesia.NomothesiaException;
import com.di.nomothesia.enums.LegislationServiceEnum;
import com.di.nomothesia.model.EndpointResultSet;
import com.di.nomothesia.model.Fragment;
import com.di.nomothesia.model.LegalDocument;
import com.di.nomothesia.model.Modification;
import com.di.nomothesia.service.LegislationService;
import com.di.nomothesia.service.impl.LegislationServiceImpl;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.StringJoiner;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBException;
import javax.xml.transform.TransformerException;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.ErrorAttributes;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.ModelAndView;

/**
 * Handles requests for the application home page.
 */
@Controller
public class LegislationController {

    private static final String LEGAL_DOC = "legaldoc";
    private static final String LEGAL_DOCUMENTS = "legalDocuments";
    private static final String LEGAL_MODS = "legalmods";
    private static final String TAGS = "tags";
    private static final String LOCALE = "locale";
    private static final String FORMAT = "format";
    private static final String BOOTSTRAP = "custom-bootstrap-menu";
    private static final String FRAGMENTS = "fragschanced";


    private static final String SEARCH = "search";
    private static final String ENDPOINT = "endpoint";
    private static final String ENDPOINT_RESULTS = "endpointResults";
    private static final String ERROR = "error";
    private static final String BASIC_LEG_1 = "basiclegislation";
    private static final String BASIC_LEG_2 = "basiclegislation2";
    private static final String BASIC_LEG_3 = "basiclegislation3";


    @Autowired
    LegislationService legislationService;

    @Autowired
    private ErrorAttributes errorAttributes;
	
    //private static final Logger logger = LoggerFactory.getLogger(LegislationController.class);
	
    @RequestMapping(value = "/gazette/a/{year:\\d+}/{id}", method = RequestMethod.GET)
    public void presentGovernmentGazettePDF(@PathVariable String year, @PathVariable String id, Model model, Locale
            locale,HttpServletResponse response) throws NomothesiaException {
      InputStream fis;
      fis = getClass().getResourceAsStream("/pdf/"+year+"/GG"+year+"_"+id+".pdf");
      try {
          org.apache.commons.io.IOUtils.copy(fis, response.getOutputStream());
          response.setContentType("application/pdf");
          response.flushBuffer();
      } catch (IOException e) {
          throw new NomothesiaException(e);
      }
    }
        
	@RequestMapping(value = "/eli/{type}/{year:\\d+}/{id}/enacted", method = RequestMethod.GET)
	public String presentOriginalLegalDocument(@PathVariable String type, @PathVariable String year, @PathVariable String id, Model model, Locale locale) throws NomothesiaException {
        LegalDocument legaldoc = legislationService.getById(type, year, id, 1);
        List<Modification> legalmods = legislationService.getAllModificationsById(type, year, id, 1, null);
        model.addAttribute(LEGAL_DOC, legaldoc);
        model.addAttribute(LEGAL_MODS, legalmods);
        model.addAttribute(LegislationServiceEnum.ID.getType(), BOOTSTRAP);
        model.addAttribute(LOCALE,locale);

        if (legaldoc == null || legaldoc.getPublicationDate() == null) {
            return ERROR;
        }

        if (!legaldoc.getParts().isEmpty()) {
            return BASIC_LEG_3;
        } else if(legaldoc.getChapters().isEmpty()) {
            return BASIC_LEG_1;
        } else{
            return BASIC_LEG_2;
        }
	}
        
    @RequestMapping(value = "/eli/{type}/{year:\\d+}/{id}", method = RequestMethod.GET)
	public String presentUpdatedLegalDocument(@PathVariable String type, @PathVariable String year, @PathVariable String id, Model model, Locale locale) throws NomothesiaException {
        LegalDocument legaldoc = legislationService.getById(type, year, id, 1);
        List<Modification> legalmods = legislationService.getAllModificationsById(type, year, id, 1, null);
        List<Fragment> frags = legislationService.getUpdatedById(legaldoc, legalmods);
        model.addAttribute(LEGAL_MODS, legalmods);
        model.addAttribute(FRAGMENTS, frags);
        model.addAttribute(LEGAL_DOC, legaldoc);
        model.addAttribute(LegislationServiceEnum.ID.getType(), BOOTSTRAP);
        model.addAttribute(LOCALE,locale);

        if (legaldoc == null || legaldoc.getPublicationDate() == null) {
            return ERROR;
        }

        if (!legaldoc.getParts().isEmpty()) {
            return BASIC_LEG_3;
        } else if(legaldoc.getChapters().isEmpty()) {
            return BASIC_LEG_1;
        } else{
            return BASIC_LEG_2;
        }
	}
        
    @RequestMapping(value = "/eli/{type}/{year:\\d+}/{id}/{type1:(article|part|section)}/{id1}/{type2:(article|part|section|paragraph)}/{id2}", method = RequestMethod.GET)
	public String presentLegalFragment(@PathVariable String type, @PathVariable String year, @PathVariable String id, @PathVariable String type1, @PathVariable String id1, @PathVariable String type2, @PathVariable String id2, Model model, Locale locale) throws NomothesiaException {
        LegalDocument legaldoc = legislationService.getById(type, year, id, 1);
        List<Modification> legalmods = legislationService.getAllModificationsById(type, year, id, 1, null);
        List<Fragment> frags = legislationService.getUpdatedById(legaldoc, legalmods);
        model.addAttribute(LEGAL_MODS, legalmods);
        model.addAttribute(FRAGMENTS, frags);
        model.addAttribute(LEGAL_DOC, legaldoc);
        model.addAttribute(LegislationServiceEnum.ID.getType(), type1 + "-" + id1 + "-" +type2 + "-" + id2);
        model.addAttribute(LOCALE,locale);

        if (legaldoc.getPublicationDate() == null) {
            return ERROR;
        }

        if (!legaldoc.getParts().isEmpty()) {
            return BASIC_LEG_3;
        } else if(legaldoc.getChapters().isEmpty()) {
            return BASIC_LEG_1;
        } else{
            return BASIC_LEG_2;
        }
	}
        
    @RequestMapping(value = "/eli/{type}/{year:\\d+}/{id}/{type1:(article|part)}/{id1}", method = RequestMethod.GET)
	public String presentLegalFragmentless(@PathVariable String type, @PathVariable String year, @PathVariable String id, @PathVariable String type1, @PathVariable String id1, Model model, Locale locale) throws NomothesiaException {
        LegalDocument legaldoc = legislationService.getById(type, year, id, 1);
        List<Modification> legalmods = legislationService.getAllModificationsById(type, year, id, 1, null);
        List<Fragment> frags = legislationService.getUpdatedById(legaldoc, legalmods);
        model.addAttribute(LEGAL_MODS, legalmods);
        model.addAttribute(FRAGMENTS, frags);
        model.addAttribute(LEGAL_DOC, legaldoc);
        model.addAttribute(LegislationServiceEnum.ID.getType(), type1 + "-" + id1);
        model.addAttribute(LOCALE,locale);

        if (legaldoc.getPublicationDate() == null) {
            return ERROR;
        }

        if (!legaldoc.getParts().isEmpty()) {
            return BASIC_LEG_3;
        } else if(legaldoc.getChapters().isEmpty()) {
            return BASIC_LEG_1;
        } else{
            return BASIC_LEG_2;
        }
	}
        
    @RequestMapping(value = "/eli/{type}/{year:\\d+}/{id}/{yyyy:\\d+}-{mm:\\d+}-{dd:\\d+}", method = RequestMethod.GET)
    public String presentModificationByDate(@PathVariable String type, @PathVariable String year, @PathVariable String id, @PathVariable String yyyy, @PathVariable String mm, @PathVariable String dd, Model model, Locale locale) throws NomothesiaException {
        String date = "";
        date += yyyy + "-" + mm + "-" + dd;

        LegalDocument legaldoc = legislationService.getById(type, year, id, 1);
        if (legaldoc.getPublicationDate().compareTo(date) > 0){
         legaldoc = null;
        }
        List<Modification> legalmods = legislationService.getAllModificationsById(type, year, id, 1, date);
        List<Fragment> frags = legislationService.getUpdatedById(legaldoc, legalmods);
        model.addAttribute(LEGAL_MODS, legalmods);
        model.addAttribute("fragschanced", frags);
        model.addAttribute(LEGAL_DOC, legaldoc);
        model.addAttribute(LegislationServiceEnum.ID.getType(), BOOTSTRAP);
        model.addAttribute(LOCALE,locale);

        if (legaldoc == null || legaldoc.getPublicationDate() == null) {
            return ERROR;
        }

        if (!legaldoc.getParts().isEmpty()) {
            return BASIC_LEG_3;
        } else if(legaldoc.getChapters().isEmpty()) {
            return BASIC_LEG_1;
        } else{
            return BASIC_LEG_2;
        }
	}
        
    @RequestMapping(value = "/eli/{type}/{year:\\d+}/{id}/{yyyy:\\d+}-{mm:\\d+}-{dd:\\d+}/data.xml", method = RequestMethod.GET, produces={"application/xml"})
    public ResponseEntity<String> exportDateToXML(@PathVariable String type, @PathVariable String year, @PathVariable
            String id, @PathVariable String yyyy, @PathVariable String mm, @PathVariable String dd, Locale locale)
            throws NomothesiaException {
        String date = "";
        date += yyyy + "-" + mm + "-" + dd;
        String xml = legislationService.getUpdatedXMLByIdDate(type,year,id,2,date);

        return new ResponseEntity<>(xml,new HttpHeaders(),HttpStatus.CREATED);
    }

    //TODO:FIX
    @RequestMapping(value = "/eli/{type}/{year:\\d+}/{id}/{yyyy:\\d+}-{mm:\\d+}-{dd:\\d+}/data.rdf", method = RequestMethod.GET, produces={"application/xml"})
    public ResponseEntity<String> exportDateToRDF(@PathVariable String type, @PathVariable String year, @PathVariable String id, @PathVariable String yyyy, @PathVariable String mm, @PathVariable String dd, Locale locale) throws NomothesiaException {
        String rdfResult = legislationService.getRDFById(type,year,id);

        return new ResponseEntity<>(rdfResult,new HttpHeaders(),HttpStatus.CREATED);
    }
        

    @RequestMapping(value="/eli/{type}/{year:\\d+}/{id}/{yyyy:\\d+}-{mm:\\d+}-{dd:\\d+}/data.json", method = RequestMethod.GET)
	public @ResponseBody LegalDocument exportDateToJSON(@PathVariable String type, @PathVariable String year, @PathVariable String id,@PathVariable String yyyy, @PathVariable String mm, @PathVariable String dd, Locale locale) throws NomothesiaException {
        String date = "";
        date += yyyy + "-" + mm + "-" + dd;
        LegalDocument legaldoc = legislationService.getById(type, year, id, 2);
        if (legaldoc.getPublicationDate().compareTo(date) > 0){
         legaldoc = null;
        }
        List<Modification> legalmods = legislationService.getAllModificationsById(type, year, id, 2, date);
        legislationService.getUpdatedById(legaldoc, legalmods);
        legaldoc.setPlace(null);

        return legaldoc;
	}
        
    @RequestMapping(value = "/eli/{type}/{year:\\d+}/{id}/{yyyy:\\d+}-{mm:\\d+}-{dd:\\d+}/data.pdf", method = RequestMethod.GET, produces={"application/xml"})
    public ModelAndView exportDateToPDF(@PathVariable String type, @PathVariable String year, @PathVariable String id, @PathVariable String yyyy, @PathVariable String mm, @PathVariable String dd, Locale locale) throws NomothesiaException {
        String date = "";
        date += yyyy + "-" + mm + "-" + dd;
        LegalDocument legaldoc = legislationService.getById(type, year, id, 2);
        if (legaldoc.getPublicationDate().compareTo(date) > 0){
         legaldoc = null;
        }
        List<Modification> legalmods = legislationService.getAllModificationsById(type, year, id, 2, date);
        legislationService.getUpdatedById(legaldoc, legalmods);

        if(!legaldoc.getParts().isEmpty()){
            return new ModelAndView("pdfView3", "legaldocument", legaldoc);
        }
        if(!legaldoc.getChapters().isEmpty()){
            return new ModelAndView("pdfView2", "legaldocument", legaldoc);
        } else{
            return new ModelAndView("pdfView", "legaldocument", legaldoc);
        }
	}
        
    @RequestMapping(value = "/{type}/{year:\\d+}/{id}/enacted/data.xml", method = RequestMethod.GET, produces={"application/xml"})
    public ResponseEntity<String> exportToXML(@PathVariable String type, @PathVariable String year, @PathVariable String id, Locale locale) throws NomothesiaException {
        String xml = legislationService.getXMLById(type,year,id,2);

        return new ResponseEntity<>(xml,new HttpHeaders(),HttpStatus.CREATED);
    }
        
        
        @RequestMapping(value = "/{type}/{year:\\d+}/{id}/enacted/data.rdf", method = RequestMethod.GET,  produces={"application/xml"})
	public ResponseEntity<String> exportToRDF(@PathVariable String type, @PathVariable String year, @PathVariable String id, Locale locale) throws NomothesiaException {
            
            String rdfResult = legislationService.getRDFById(type,year,id);
            
            return new ResponseEntity<>(rdfResult,new HttpHeaders(),HttpStatus.CREATED);
	
        }
        
        @RequestMapping(value="/{type}/{year:\\d+}/{id:\\d+}/enacted/data.json", method = RequestMethod.GET)
	public @ResponseBody LegalDocument exportToJSON(@PathVariable String type, @PathVariable String year, @PathVariable String id, Locale locale) throws NomothesiaException {
            LegalDocument legal = legislationService.getById(type,year,id,2);
            legal.setPlace(null);
            
            return legal;
 
	}
        
        @RequestMapping(value = "/{type}/{year:\\d+}/{id:\\d+}/enacted/data.pdf", method = RequestMethod.GET)
	public ModelAndView exportToPDF(@PathVariable String type, @PathVariable String year, @PathVariable String id, Locale locale) throws NomothesiaException {
            
            LegalDocument legaldoc = legislationService.getById(type,year,id,2);
            if(!legaldoc.getParts().isEmpty()){
                return new ModelAndView("pdfView3", "legaldocument", legaldoc);
            }
            if(!legaldoc.getChapters().isEmpty()){
                return new ModelAndView("pdfView2", "legaldocument", legaldoc);
            }
            else{
                return new ModelAndView("pdfView", "legaldocument", legaldoc);
            }
	}

    @RequestMapping(value = "/eli/{type}/{year:\\d+}/{id}/data.xml", method = RequestMethod.GET, produces={"application/xml"})
    public ResponseEntity<String> exportUpdatedToXML(@PathVariable String type, @PathVariable String year, @PathVariable String id, Locale locale) throws NomothesiaException {
        String xml = legislationService.getUpdatedXMLById(type,year,id,2);

        return new ResponseEntity<>(xml,new HttpHeaders(),HttpStatus.CREATED);
    }
        
    @RequestMapping(value = "/eli/{type}/{year:\\d+}/{id}/data.rdf", method = RequestMethod.GET,  produces={"application/xml"})
	public ResponseEntity<String> exportUpdatedToRDF(@PathVariable String type, @PathVariable String year, @PathVariable String id, Locale locale) throws  NomothesiaException {
        String rdfResult = legislationService.getRDFById(type,year,id);

        return new ResponseEntity<>(rdfResult,new HttpHeaders(),HttpStatus.CREATED);
    }
        
    @RequestMapping(value="/eli/{type}/{year:\\d+}/{id}/data.json", method = RequestMethod.GET)
	public @ResponseBody LegalDocument exportUpdatedToJSON(@PathVariable String type, @PathVariable String year, @PathVariable String id, Locale locale) throws NomothesiaException {
        LegalDocument legaldoc = legislationService.getById(type, year, id, 2);
        List<Modification> legalmods = legislationService.getAllModificationsById(type, year, id, 2, null);
        legislationService.getUpdatedById(legaldoc, legalmods);
        legaldoc.setPlace(null);

        return legaldoc;
	}
        
    @RequestMapping(value = "/eli/{type}/{year:\\d+}/{id}/data.pdf", method = RequestMethod.GET)
	public ModelAndView exportUpdatedToPDF(@PathVariable String type, @PathVariable String year, @PathVariable String id, Locale locale) throws NomothesiaException {
            LegalDocument legaldoc = legislationService.getById(type, year, id, 2);
            List<Modification> legalmods = legislationService.getAllModificationsById(type, year, id, 2, null);
            legislationService.getUpdatedById(legaldoc, legalmods);
            
            if(!legaldoc.getParts().isEmpty()){
                return new ModelAndView("/resources/pdfView3", "legaldocument", legaldoc);
            }
            if(!legaldoc.getChapters().isEmpty()){
                return new ModelAndView("/resources/pdfView2", "legaldocument", legaldoc);
            }
            else{
                return new ModelAndView("pdfView", "legaldocument", legaldoc);
            }
	}

    @RequestMapping(value = "/search", method = RequestMethod.GET)
	public String search(@RequestParam Map<String,String> params, Model model, Locale locale) throws NomothesiaException {
        if(params != null) {
            List<LegalDocument> legalDocumentL = legislationService.searchLegislation(params);
            List<String> tags = legislationService.getTags();
            model.addAttribute(LEGAL_DOCUMENTS, legalDocumentL);
            model.addAttribute(TAGS,tags);
            model.addAttribute(LOCALE,locale);

            if((params.get("keywords")!=null) && !"".equals(params.get("keywords"))) {
                model.addAttribute("keywords",params.get("keywords"));
            }

            if((params.get("date")!=null) && !"".equals(params.get("date"))) {
                model.addAttribute("date",params.get("date"));
            }

            if((params.get("datefrom")!=null) && !"".equals(params.get("datefrom"))) {
                model.addAttribute("datefrom",params.get("datefrom"));
            }

            if((params.get("dateto")!=null) && !"".equals(params.get("dateto"))) {
                model.addAttribute("dateto",params.get("dateto"));
            }

            if((params.get("year")!=null) && !"".equals(params.get("year"))) {
                model.addAttribute("year",params.get("year"));
            }

            if((params.get(LegislationServiceEnum.ID.getType())!=null) && !"".equals(params.get(LegislationServiceEnum.ID.getType()))) {
                model.addAttribute(LegislationServiceEnum.ID.getType(),params.get(LegislationServiceEnum.ID.getType()));
            }

            if((params.get("fek_year")!=null) && !"".equals(params.get("fek_year"))) {
                model.addAttribute("fek_year",params.get("fek_year"));
            }

            if((params.get("fek_id")!=null) && !"".equals(params.get("fek_id"))) {
                model.addAttribute("fek_id",params.get("fek_id"));
            }

            if((params.get("type")!=null) && !"".equals(params.get("type"))) {
                model.addAttribute("type",params.get("type"));
            }
        }

        return SEARCH;
	}
        
    @RequestMapping(value = "/endpoint", method = RequestMethod.GET)
	public String endpoint(@RequestParam Map<String,String> params, Model model, Locale locale) throws NomothesiaException {
        if(params.get("query") != null){
            EndpointResultSet eprs = legislationService.sparqlQuery(params.get("query"),params.get(FORMAT));
            model.addAttribute(ENDPOINT_RESULTS, eprs);
            model.addAttribute(FORMAT, params.get(FORMAT));
        }
        model.addAttribute(LOCALE,locale);

        return ENDPOINT;
	}
        
    @RequestMapping(value = "/endpoint/query/{id}", method = RequestMethod.GET)
	public String endpoint( @PathVariable String id, Model model, Locale locale) throws NomothesiaException {
        if(id != null){
            EndpointResultSet eprs = legislationService.sparqlQuery(id,"HTML");
            model.addAttribute(ENDPOINT_RESULTS, eprs);
            model.addAttribute(LOCALE,locale);
            //model.addAttribute(FORMAT, params.get(FORMAT));
        }

        return ENDPOINT;
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
