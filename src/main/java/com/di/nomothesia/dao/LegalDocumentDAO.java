package com.di.nomothesia.dao;

import com.di.nomothesia.NomothesiaException;
import com.di.nomothesia.model.EndpointResultSet;
import com.di.nomothesia.model.GovernmentGazette;
import com.di.nomothesia.model.LegalDocument;
import com.di.nomothesia.model.Modification;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public interface LegalDocumentDAO {

    /**
     * Gets Metadata for the given legal document (tags, signers etc.).
     *
     * @param decisionType the decision type
     * @param year the year
     * @param id the id
     * @return LegalDocument
     * @throws NomothesiaException
     */
    LegalDocument getMetadataById(String decisionType, String year, String id) throws NomothesiaException;

    /**
     * Gets Citations of a legal document given its Id.
     *
     * @param decisionType the decision type
     * @param year the year
     * @param id the id
     * @param req the request
     * @param legald the legal document
     * @return LegalDocument
     * @throws NomothesiaException
     */
    LegalDocument getCitationsById(String decisionType, String year, String id, int req, LegalDocument legald) throws NomothesiaException;

    /**
     * Get the complete structure of a legal document given its id.
     *
     * @param decisionType the decision type
     * @param year the year
     * @param id the id
     * @param req the request
     * @param legald the ledagl document
     * @return LegalDocument
     * @throws NomothesiaException
     */
    LegalDocument getById(String decisionType, String year, String id, int req, LegalDocument legald) throws NomothesiaException;

    /**
     * Gets RDF graph given a legal document id.
     *
     * @param decisionType the decision type
     * @param year the year
     * @param id the id
     * @return String
     * @throws NomothesiaException
     */
    String getRDFById(String decisionType, String year, String id) throws NomothesiaException;

    /**
     * Executes the SPARQL query given on the Sesame Server with the given format.
     *
     * @param endpointResult the endpoint result
     * @param format the format
     * @return EndpointResultSet
     * @throws NomothesiaException
     */
    EndpointResultSet sparqlQuery(EndpointResultSet endpointResult, String format) throws NomothesiaException;

    /**
     * Gets all modifications of a legad document if they exist for the given id.
     *
     * @param decisionType the decision type
     * @param year the year
     * @param id the id
     * @param date the date
     * @param req the request
     * @return List<Modification>
     * @throws NomothesiaException
     */
    List<Modification> getAllModifications(String decisionType, String year, String id, String date, int req) throws NomothesiaException;

    /**
     * Performs a search given certain params and returns a list matching legal documents.
     *
     * @param params the parameters
     * @return List<LegalDocument>
     * @throws NomothesiaException
     */
    List<LegalDocument> search(Map<String, String> params) throws NomothesiaException;

    /**
     * Gets the tags of a legal document.
     *
     * @return ListM<String>
     * @throws NomothesiaException
     */
    List<String> getTags() throws NomothesiaException;

    List<LegalDocument> getViewed() throws NomothesiaException;

    List<LegalDocument> getRecent() throws NomothesiaException;

    String getLegislationTypeByYear() throws NomothesiaException;

    List<GovernmentGazette> getFEKStatistics() throws NomothesiaException;

    List<ArrayList<String>> getStatistics() throws NomothesiaException;
}
