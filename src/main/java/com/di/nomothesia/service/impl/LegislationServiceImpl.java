package com.di.nomothesia.service.impl;

import com.di.nomothesia.NomothesiaException;
import com.di.nomothesia.controller.XMLBuilder;
import com.di.nomothesia.controller.XMLBuilder2;
import com.di.nomothesia.dao.LegalDocumentDAO;
import com.di.nomothesia.model.EndpointResultSet;
import com.di.nomothesia.model.Fragment;
import com.di.nomothesia.model.GovernmentGazette;
import com.di.nomothesia.model.LegalDocument;
import com.di.nomothesia.model.Modification;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.xml.transform.TransformerException;

import com.di.nomothesia.service.LegislationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.di.nomothesia.enums.QueriesEnum.*;

@Service
public class LegislationServiceImpl implements LegislationService {

    @Autowired
    LegalDocumentDAO legalDocumentDAO;

    @Autowired
    XMLBuilder xmlBuilder;

    @Autowired
    XMLBuilder2 xmlBuilder2;

    @Override
    public LegalDocument getById(String decisionType, String year, String id, int request) throws NomothesiaException {
        //Get Metadata
        LegalDocument legald = legalDocumentDAO.getMetadataById(decisionType, year, id);
        //Get Citations
        legald = legalDocumentDAO.getCitationsById(decisionType, year, id, request, legald);
        //Get Legal Document
        return legalDocumentDAO.getById(decisionType, year, id, request, legald);
    }

    @Override
    public List<Modification> getAllModificationsById(String type, String year, String id, int request, String date) throws NomothesiaException {
        return legalDocumentDAO.getAllModifications(type, year, id, date, request);
    }

    //@Cacheable(value="NomothesiaCache", key="#query")
    @Override
    public EndpointResultSet sparqlQuery(String query, String format) throws NomothesiaException {
        EndpointResultSet eprs = new EndpointResultSet();
        if ("1".equals(query)) {
            eprs.setQuery(QUERY_1.getQuery());
        } else if ("2".equals(query)) {
            eprs.setQuery(QUERY_2.getQuery());
        } else if ("3".equals(query)) {
            eprs.setQuery(QUERY_3.getQuery());
        } else if ("4".equals(query)) {
            eprs.setQuery(QUERY_4.getQuery());
        } else {
            eprs.setQuery(query);
        }
        
        //Get Query Result
        return legalDocumentDAO.sparqlQuery(eprs, format);
    }

    @Override
    public String getRDFById(String type, String year, String id) throws NomothesiaException {
        return legalDocumentDAO.getRDFById(type, year, id);
    }

    @Override
    public String getXMLById(String type, String year, String id, int request) throws TransformerException, NomothesiaException {
        //Get Metadata
        LegalDocument legald = legalDocumentDAO.getMetadataById(type, year, id);
        //Get Citations
        legald = legalDocumentDAO.getCitationsById(type, year, id, request, legald);
        //Get Legal Document
        legald = legalDocumentDAO.getById(type, year, id, request, legald);

        return xmlBuilder.XMLbuilder(legald);
    }

    @Override
    public List<LegalDocument> searchLegislation(Map<String, String> params) throws NomothesiaException {
        return legalDocumentDAO.search(params);
    }

    @Override
    public List<LegalDocument> mostViewed() throws NomothesiaException {
        return legalDocumentDAO.getViewed();
    }

    @Override
    public List<LegalDocument> mostRecent() throws NomothesiaException {
        return legalDocumentDAO.getRecent();
    }

    @Override
    public List<String> getTags() throws NomothesiaException {
        return legalDocumentDAO.getTags();
    }

    @Override
    public List<Fragment> getUpdatedById(LegalDocument legald, List<Modification> mods) {
        return legald.applyModifications(mods);
    }

    @Override
    public String getUpdatedXMLById(String type, String year, String id, int request) throws TransformerException, NomothesiaException {
        //Get Metadata
        LegalDocument legald = legalDocumentDAO.getMetadataById(type, year, id);
        //Get Citations
        legald = legalDocumentDAO.getCitationsById(type, year, id, request, legald);
        //Get Legal Document
        legald = legalDocumentDAO.getById(type, year, id, request, legald);
        //Get all Modifications
        List<Modification> mods = legalDocumentDAO.getAllModifications(type, year, id, null, request);
        //Apply Modifications
        legald.applyModifications(mods);
        // Build XML
        if (!legald.getChapters().isEmpty()) {
            return xmlBuilder2.XMLbuilder2(legald);
        } else {
            return xmlBuilder.XMLbuilder(legald);
        }
    }

    @Override
    public String getUpdatedXMLByIdDate(String type, String year, String id, int request,
                                        String date) throws TransformerException, NomothesiaException {
        //Get Metadata
        LegalDocument legald = legalDocumentDAO.getMetadataById(type, year, id);
        //Get Citations
        legald = legalDocumentDAO.getCitationsById(type, year, id, request, legald);
        //Get Legal Document
        legald = legalDocumentDAO.getById(type, year, id, request, legald);
        //Get all Modifications
        List<Modification> mods = legalDocumentDAO.getAllModifications(type, year, id, date, request);
        //Apply Modifications
        legald.applyModifications(mods);
        // Build XML
        if (!legald.getChapters().isEmpty()) {
            return xmlBuilder2.XMLbuilder2(legald);
        } else {
            return xmlBuilder.XMLbuilder(legald);
        }
    }

    @Override
    public List<GovernmentGazette> getFEKStatistics() throws NomothesiaException {
        return legalDocumentDAO.getFEKStatistics();
    }

    @Override
    public List<ArrayList<String>> getStats() throws NomothesiaException {
        return legalDocumentDAO.getStatistics();
    }
}
