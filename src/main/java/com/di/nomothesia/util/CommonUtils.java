package com.di.nomothesia.util;

import com.di.nomothesia.NomothesiaException;
import com.di.nomothesia.config.AppConfig;
import com.di.nomothesia.model.LegalDocument;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.http.HTTPRepository;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * CommonUtils code for various operations.
 * <p>
 * Created by psour on 16/11/2016.
 */
public final class CommonUtils {

    //TODO make this a property
    private static final String URI_BASE = "http://legislation.di.uoa.gr/";

    private CommonUtils(){
        //Empty Constructor
    }

    public static String trimDoubleQuotes(String text) {
        int textLength = text.length();
        if (textLength >= 2 && text.charAt(0) == '"' && text.charAt(textLength - 1) == '"') {
            return text.substring(1, textLength - 1);
        }

        return text;
    }

    public static LegalDocument decideLegalDocumentType (LegalDocument legalDocument, String type) {
        if (type.equals(URI_BASE+ "ontology/Constitution")) {
            legalDocument.setDecisionType("con");
        } else if (type.equals(URI_BASE+ "ontology/PresidentialDecree")) {
            legalDocument.setDecisionType("pd");
        } else if (type.equals(URI_BASE+ "ontology/Law")) {
            legalDocument.setDecisionType("law");
        } else if (type.equals(URI_BASE+ "ontology/ActOfMinisterialCabinet")) {
            legalDocument.setDecisionType("amc");
        } else if (type.equals(URI_BASE+ "ontology/MinisterialDecision")) {
            legalDocument.setDecisionType("md");
        } else if (type.equals(URI_BASE+ "ontology/RoyalDecree")) {
            legalDocument.setDecisionType("rd");
        } else if (type.equals(URI_BASE+ "ontology/LegislativeAct")) {
            legalDocument.setDecisionType("la");
        } else if (type.equals(URI_BASE+ "ontology/RegulatoryProvision")) {
            legalDocument.setDecisionType("rp");
        }

        return legalDocument;
    }

}
