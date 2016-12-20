package com.di.nomothesia.dao;

import com.di.nomothesia.NomothesiaException;
import com.di.nomothesia.comparators.LegalDocumentSort;
import com.di.nomothesia.config.AppConfig;
import com.di.nomothesia.enums.LegislationTypesEnum;
import com.di.nomothesia.enums.YearsEnum;
import com.di.nomothesia.model.*;
import com.di.nomothesia.util.CommonUtils;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.*;
import org.apache.lucene.search.Query;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.openrdf.OpenRDFException;
import org.openrdf.query.*;
import org.openrdf.query.resultio.sparqlxml.SPARQLResultsXMLWriter;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.http.HTTPRepository;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.rdfxml.RDFXMLWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
//import org.openrdf.query.resultio.stSPARQLQueryResultFormat;
//import eu.earthobservatory.org.StrabonEndpoint.client.*;

@Service
public class LegalDocumentDAOImpl implements LegalDocumentDAO {

    private static final String uriBase = "http://legislation.di.uoa.gr/";

    @Autowired
    AppConfig.ApplicationProperties applicationProperties;

    @Override
    public LegalDocument getMetadataById(String decisionType, String year, String id) throws NomothesiaException {
        LegalDocument legald = new LegalDocument();
        legald.setId(id);
        legald.setYear(year);
        legald.setDecisionType(decisionType);
        legald.setURI(uriBase + decisionType + "/" + year + "/" + id);

        TupleQueryResult result;

        try {
            RepositoryConnection con = getSesameConnection();
            String generalQuery = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                    "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
                    "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n" +
                    "PREFIX owl: <http://www.w3.org/2002/07/owl#>\n" +
                    "PREFIX metalex:<http://www.metalex.eu/metalex/2008-05-02#>\n" +
                    "PREFIX leg: <" + uriBase + "ontology/>\n" +
                    "PREFIX dc: <http://purl.org/dc/terms/>\n" +
                    "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\n" +
                    "\n" +
                    "SELECT DISTINCT ?title ?date ?gaztitle ?signername ?views ?place ?htmltitle\n" +
                    "WHERE{\n" +
                    "<" + uriBase + decisionType + "/" + year + "/" + id + ">" +
                    " dc:created ?date.\n" +
                    "<" + uriBase + decisionType + "/" + year + "/" + id + ">" +
                    " leg:gazette ?gazette.\n" +
                    "<" + uriBase + decisionType + "/" + year + "/" + id + ">" +
                    " leg:views ?views.\n" +
                    "?gazette dc:title ?gaztitle.\n" +
                    //"?gazette leg:pdfFile ?pdfile.\n" +
                    "OPTIONAL{<" + uriBase + decisionType + "/" + year + "/" + id + ">" +
                    " dc:title ?title.}\n" +
                    "OPTIONAL{<" + uriBase + decisionType + "/" + year + "/" + id + ">" +
                    " leg:html ?htmltitle.}\n" +
                    "OPTIONAL{ <" + uriBase + decisionType + "/" + year + "/" + id +
                    "> leg:place ?place.}" +
                    "FILTER(langMatches(lang(?title), \"el\"))\n" +
                    "}";

            //System.out.println(generalQuery);
            TupleQuery tupleQuery = con.prepareTupleQuery(QueryLanguage.SPARQL, generalQuery);
            result = tupleQuery.evaluate();

            //int flag = 0;
            // iterate the result set
            while (result.hasNext()) {
                BindingSet bindingSet = result.next();
                //Signer sign = new Signer();
                legald.setURI(uriBase + decisionType + "/" + year + "/" + id);
                String title_el = "";
                if (bindingSet.hasBinding("title")) {
                    title_el = bindingSet.getValue("title").toString().replace("@el", "");
                } else {
                    title_el = legald.getDecisionType() + "" + legald.getYear() + " " + legald.getId();
                }
                if (bindingSet.hasBinding("htmltitle")) {
                    title_el = bindingSet.getValue("htmltitle").toString().split("\\^\\^", 2)[0];
                }
                legald.setTitle(CommonUtils.trimDoubleQuotes(title_el));
                String date2 =
                        bindingSet.getValue("date").toString().replace("^^<http://www.w3.org/2001/XMLSchema#date>",
                                "");
                legald.setPublicationDate(CommonUtils.trimDoubleQuotes(date2));
                String fek = bindingSet.getValue("gaztitle").toString().replace("^^", "");
                legald.setFEK(CommonUtils.trimDoubleQuotes(fek));
                //String fekfile = bindingSet.getValue("pdfile").toString().replace("^^","");
                //legald.setFEKfile(CommonUtils.trimDoubleQuotes(fekfile));
                String views = bindingSet.getValue("views").toString().replace(
                        "^^<http://www.w3.org/2001/XMLSchema#integer>", "");
                legald.setViews(CommonUtils.trimDoubleQuotes(views));

                /*if((flag ==0) &&(bindingSet.hasBinding("place"))){
                    flag = 1;
                    legald.setPlace(this.getKML(bindingSet.getValue("place").toString()));
                }*/
            }

            result.close();

            //GET TAGS
            String getTagsQuery = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                    "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
                    "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n" +
                    "PREFIX owl: <http://www.w3.org/2002/07/owl#>\n" +
                    "PREFIX metalex:<http://www.metalex.eu/metalex/2008-05-02#>\n" +
                    "PREFIX leg: <" + uriBase + "ontology/>\n" +
                    "PREFIX dc: <http://purl.org/dc/terms/>\n" +
                    "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\n" +
                    "\n" +
                    "SELECT ?tag\n" +
                    "WHERE{\n" +
                    "<" + uriBase + decisionType + "/" + year + "/" + id + ">" +
                    " leg:tag ?tag.\n" +
                    "FILTER(langMatches(lang(?tag), \"el\"))\n" +
                    "}";

            //System.out.println(getTagsQuery);
            TupleQuery tupleQuery2 = con.prepareTupleQuery(QueryLanguage.SPARQL, getTagsQuery);
            result = tupleQuery2.evaluate();

            // iterate the result set
            while (result.hasNext()) {
                BindingSet bindingSet = result.next();
                String tag_el = bindingSet.getValue("tag").toString().replace("@el", "");
                legald.getTags().add(CommonUtils.trimDoubleQuotes(tag_el));
            }

            result.close();

            //GET SIGNERS
            String getSingersQuery = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                    "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
                    "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n" +
                    "PREFIX owl: <http://www.w3.org/2002/07/owl#>\n" +
                    "PREFIX metalex:<http://www.metalex.eu/metalex/2008-05-02#>\n" +
                    "PREFIX leg: <" + uriBase + "ontology/>\n" +
                    "PREFIX dc: <http://purl.org/dc/terms/>\n" +
                    "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\n" +
                    "\n" +
                    "SELECT DISTINCT ?signername ?html\n" +
                    "WHERE{\n" +
                    "<" + uriBase + decisionType + "/" + year + "/" + id + ">" +
                    " leg:signer ?signer.\n" +
                    "?signer foaf:name ?signername.\n" +
                    //"?signer foaf:title ?signertitle.\n" +
                    "OPTIONAL{?signer leg:html ?html.}\n" +
                    "FILTER(langMatches(lang(?signername), \"el\"))\n" +
                    "}";

            //System.out.println(getSingersQuery);
            TupleQuery tupleQuery3 = con.prepareTupleQuery(QueryLanguage.SPARQL, getSingersQuery);
            result = tupleQuery3.evaluate();

            // iterate the result set
            while (result.hasNext()) {
                BindingSet bindingSet = result.next();
                Signer sign = new Signer();
                String name_el = bindingSet.getValue("signername").toString().replace("@el", "");
                if (bindingSet.getValue("html") != null) {
                    name_el = bindingSet.getValue("html").toString().split("\\^\\^", 2)[0];
                }
                sign.setFullName(CommonUtils.trimDoubleQuotes(name_el));
                //String signer_el = bindingSet.getValue("signertitle").toString().replace("@el", "");
                //sign.setTitle(CommonUtils.trimDoubleQuotes(signer_el));
                legald.getSigners().add(sign);
            }

            result.close();

            /*try {
                int views = Integer.parseInt(legald.getViews()) + 1;
                //UPDATE VIEWS
                String queryString4 = "DELETE {\n" +
                        "   <" + legald.getURI() + "> <" + uriBase + "ontology/views> ?views\n" +
                        "}\n" +
                        "INSERT {\n" +
                        "   <" + legald.getURI() + "> <" + uriBase + "ontology/views> \"" + views +
                        "\"^^<http://www.w3.org/2001/XMLSchema#integer>\n" +
                        "}\n";
                Update update = con.prepareUpdate(QueryLanguage.SPARQL, queryString4);
                update.execute();
            } catch (Exception ex) {
                throw new NomothesiaException(ex);
            } finally {
                con.close();
            }*/
        } catch (OpenRDFException e) {
            throw new NomothesiaException(e);
        }

        return legald;
    }

    @Override
    public LegalDocument getCitationsById(String decisionType, String year, String id, int req, LegalDocument legald) throws NomothesiaException {
        //LegalDocument legald = this.getMetadataById(decisionType, year, id);

        TupleQueryResult result = null;

        try {
            RepositoryConnection con = getSesameConnection();
            String getCitationsQuery = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                    "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
                    "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n" +
                    "PREFIX owl: <http://www.w3.org/2002/07/owl#>\n" +
                    "PREFIX metalex:<http://www.metalex.eu/metalex/2008-05-02#>\n" +
                    "PREFIX leg: <" + uriBase + "ontology/>\n" +
                    "PREFIX dc: <http://purl.org/dc/terms/>\n" +
                    "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\n" +
                    "\n" +
                    "SELECT  DISTINCT ?citation ?cittext ?cituri ?cithtml\n" +
                    "WHERE{\n" +
                    "<" + uriBase + decisionType + "/" + year + "/" + id +
                    "> metalex:part ?citation.\n" +
                    "?citation a metalex:BibliographicCitation.\n" +
                    "?citation leg:context ?cittext.\n" +
                    "OPTIONAL {?citation metalex:cites ?cituri.}.\n" +
                    "OPTIONAL {?citation leg:html ?cithtml.}\n";

            if (req == 1) {
                getCitationsQuery += "}";
            } else if (req == 2) {
                getCitationsQuery += "FILTER(langMatches(lang(?cittext), \"el\"))\n" +
                        "}";
            }

            //System.out.println(getCitationsQuery);
            TupleQuery tupleQuery = con.prepareTupleQuery(QueryLanguage.SPARQL, getCitationsQuery);
            result = tupleQuery.evaluate();
            int cid = 1;
            String old2 = "old2";
            String old = "old";
            String current = "current";

            // iterate the result set
            while (result.hasNext()) {
                BindingSet bindingSet = result.next();
                current = CommonUtils.trimDoubleQuotes(bindingSet.getValue("citation").toString());

                if (current.equals(old)) {
                    if (bindingSet.getValue("cituri") != null) {
                        if (old2.equals(bindingSet.getValue("cittext").toString()) &&
                                !bindingSet.getValue("cittext").toString().contains("@html")) {
                            Citation cit = new Citation();
                            cit = legald.getCitations().get(cid - 2);
                            cit.gettargetURIs().add(CommonUtils.trimDoubleQuotes(bindingSet.getValue("cituri").toString()));
                        }

                        old = current;
                        old2 = bindingSet.getValue("cittext").toString();
                    }
                } else {
                    Citation cit = new Citation();
                    cit.setURI(CommonUtils.trimDoubleQuotes(bindingSet.getValue("citation").toString()));

                    if (bindingSet.getValue("cituri") != null) {
                        cit.gettargetURIs().add(CommonUtils.trimDoubleQuotes(bindingSet.getValue("cituri").toString()));
                    }

                    if (bindingSet.getValue("cittext").toString().contains("@html")) {
                        String text = bindingSet.getValue("cittext").toString().replace("@html", "");
                        cit.setDescription(CommonUtils.trimDoubleQuotes(text));
                    } else if (bindingSet.getValue("cittext").toString().contains("@el")) {
                        String text = bindingSet.getValue("cittext").toString().replace("@el", "");
                        cit.setDescription(CommonUtils.trimDoubleQuotes(text));
                    }

                    if (bindingSet.getValue("cithtml") != null) {
                        String text = bindingSet.getValue("cithtml").toString().split("\\^\\^", 2)[0];
                        cit.setDescription(CommonUtils.trimDoubleQuotes(text));
                    }

                    cit.setId(cid);
                    cid++;
                    legald.getCitations().add(cit);
                    old = current;
                    old2 = bindingSet.getValue("cittext").toString();
                }
            }

            result.close();
            con.close();

        } catch (OpenRDFException e) {
            throw new NomothesiaException(e);
        }

        /*System.out.println("=========================================================================================");
        for(int i=0; i < legald.getCitations().size(); i++){
            System.out.println(legald.getCitations().get(i).getDescription() + "\n" + legald.getCitations().get(i).getURI() + "\n");
            for(int j=0; j < legald.getCitations().get(i).gettargetURIs().size(); j++){
                System.out.println(legald.getCitations().get(i).gettargetURIs().get(j) + "\n");
            }
        }
        System.out.println("=========================================================================================");*/

        return legald;
    }

    @Override
    public LegalDocument getById(String decisionType, String year, String id, int req, LegalDocument legald) throws NomothesiaException {
        TupleQueryResult result;

        try {
            RepositoryConnection con = getSesameConnection();
            try {
                String queryString = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                        "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
                        "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n" +
                        "PREFIX owl: <http://www.w3.org/2002/07/owl#>\n" +
                        "PREFIX metalex:<http://www.metalex.eu/metalex/2008-05-02#>\n" +
                        "PREFIX leg: <" + uriBase + "ontology/>\n" +
                        "PREFIX dc: <http://purl.org/dc/terms/>\n" +
                        "\n" +
                        "SELECT DISTINCT ?part ?text ?html ?type ?title ?filename\n" +
                        "WHERE{\n" +
                        "<" + uriBase + decisionType + "/" + year + "/" + id + ">" +
                        " metalex:part+  ?part.\n" +
                        "?part rdf:type ?type.\n" +
                        "OPTIONAL{ ?part leg:text ?text.}.\n" +
                        "OPTIONAL{ ?part leg:html ?html.}.\n" +
                        "OPTIONAL{ ?part dc:title ?title.}.\n" +
                        "OPTIONAL{ ?part leg:imageName ?filename.}." +
                        "}\n" +
                        "ORDER BY ?part";

                //System.out.println(queryString);
                TupleQuery tupleQuery = con.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
                result = tupleQuery.evaluate();

                try {
                    // iterate the result set
                    int part_count = -1;
                    int chap_count = -1;
                    int art_count = -1;
                    int art_count2 = 0;
                    int count2 = -1;
                    int count3 = -1;
                    int count4 = -1;
                    int mod = 0;
                    int mod_count3 = -1;
                    int mod_count4 = -1;
                    String old = "old";
                    Paragraph paragraph = null;
                    Article article = null;

                    while (result.hasNext()) {
                        BindingSet bindingSet = result.next();

                        if (bindingSet.getValue("type").toString().equals(uriBase + "ontology/Chapter")) {
                            Chapter chapter = new Chapter();
                            chapter.setURI(bindingSet.getValue("part").toString());
                            if (mod == 1 && chapter.getURI().contains("modification")) {
                                chapter.setId(Integer.parseInt(
                                        chapter.getURI().split("chapter\\/")[2].replaceAll("[Î“Â?-Î“Â™]+", "")));
                            } else {
                                chapter.setId(Integer.parseInt(
                                        chapter.getURI().split("chapter\\/")[1].replaceAll("[Î“Â?-Î“Â™]+", "")));
                            }
                            if (bindingSet.getValue("title") != null) {
                                String title = bindingSet.getValue("title").toString().replace("@el", "");
                                title = title.replace("^^", "");
                                chapter.setTitle(CommonUtils.trimDoubleQuotes(title));
                            }

                            //System.out.println(article.getURI());
                            //System.out.println("NEW CHAPTER"+chap_count);
                            if (!legald.getParts().isEmpty()) {
                                legald.getParts().get(part_count).getChapters().add(chapter);
                            } else {
                                legald.getChapters().add(chapter);
                            }

                            chap_count++;
                            art_count = -1;
                            count2 = -1;
                            count3 = -1;
                            count4 = -1;
                            mod = 0;
                            int mod_count = 3;
                        } else if (bindingSet.getValue("type").toString().equals(uriBase + "ontology/Part")) {
                            Part part = new Part();
                            part.setURI(bindingSet.getValue("part").toString());
                            if (mod == 1 && part.getURI().contains("modification")) {
                                part.setId(Integer.parseInt(
                                        part.getURI().split("part\\/")[2].replaceAll("[Î“Â?-Î“Â™]+", "")));
                            } else {
                                part.setId(Integer.parseInt(
                                        part.getURI().split("part\\/")[1].replaceAll("[Î“Â?-Î“Â™]+", "")));
                            }
                            if (bindingSet.getValue("title") != null) {
                                String title = bindingSet.getValue("title").toString().replace("@el", "");
                                title = title.replace("^^", "");
                                part.setTitle(CommonUtils.trimDoubleQuotes(title));
                            }

                            //System.out.println(article.getURI());
                            //System.out.println("NEW CHAPTER"+chap_count);
                            legald.getParts().add(part);
                            part_count++;
                            chap_count = -1;
                            art_count = -1;
                            count2 = -1;
                            count3 = -1;
                            count4 = -1;
                            mod = 0;
                        } else if (bindingSet.getValue("type").toString().equals(uriBase + "ontology/Article")) {
                            article = new Article();
                            article.setURI(bindingSet.getValue("part").toString());
                            if (mod == 1 && article.getURI().contains("modification")) {
                                article.setId(article.getURI().split("article\\/")[2]);
                            } else {
                                article.setId(article.getURI().split("article\\/")[1]);
                            }
                            if (bindingSet.getValue("title") != null) {
                                String title = bindingSet.getValue("title").toString().replace("@el", "");
                                title = title.replace("^^", "");
                                article.setTitle(CommonUtils.trimDoubleQuotes(title));
                            }

                            //System.out.println(article.getURI());
                            //System.out.println("NEW ARTICLE");
                            if ((mod == 0) || (mod == 2) || (mod == 3)) {
                                //System.out.println("NEW ARTICLE"+art_count);
                                if (!legald.getParts().isEmpty()) {
                                    if (legald.getParts().get(part_count).getChapters().isEmpty()) {
                                        legald.getParts().get(part_count).getArticles().add(article);
                                    } else {
                                        legald.getParts().get(part_count).getChapters().get(
                                                chap_count).getArticles().add(article);
                                    }
                                } else {
                                    if (legald.getChapters().isEmpty()) {
                                        legald.getArticles().add(article);
                                    } else {
                                        legald.getChapters().get(chap_count).getArticles().add(article);
                                    }
                                }

                                art_count++;
                                art_count2++;
                                count2 = -1;
                                count3 = -1;
                                count4 = -1;
                                mod = 0;
                            } else if (article.getURI().contains("modification")) {
                                //System.out.println("MODIFICATION ARTICLE");
                                mod = 3;
                                if (legald.getParts().isEmpty()) {
                                    if (legald.getChapters().isEmpty()) {
                                        if (article.getURI().split("\\/[0-9]+\\/modification")[0].endsWith("passage")) {
                                            legald.getArticles().get(art_count).getParagraphs().get(
                                                    count2).getPassages().get(count3).getModification().setType(
                                                    "Article");
                                            legald.getArticles().get(art_count).getParagraphs().get(
                                                    count2).getPassages().get(count3).getModification().setFragment(
                                                    article);
                                        } else {
                                            legald.getArticles().get(art_count).getParagraphs().get(
                                                    count2).getCaseList().get(count4).getModification().setType(
                                                    "Article");
                                            legald.getArticles().get(art_count).getParagraphs().get(
                                                    count2).getCaseList().get(count4).getModification().setFragment(
                                                    article);
                                        }
                                    } else {
                                        if (article.getURI().split("\\/[0-9]+\\/modification")[0].endsWith("passage")) {
                                            legald.getChapters().get(chap_count).getArticles().get(
                                                    art_count).getParagraphs().get(count2).getPassages().get(
                                                    count3).getModification().setType("Article");
                                            legald.getChapters().get(chap_count).getArticles().get(
                                                    art_count).getParagraphs().get(count2).getPassages().get(
                                                    count3).getModification().setFragment(article);
                                        } else {
                                            // System.out.println("=======================================================================================");
                                            //System.out.println(article.getURI());
                                            //System.out.println("ERT:  chapter/"+chap_count+"/article/"+art_count+"/paragraph/"+count2+"/passage/"+count4);
                                            legald.getChapters().get(chap_count).getArticles().get(
                                                    art_count).getParagraphs().get(count2).getCaseList().get(
                                                    count4).getModification().setType("Article");
                                            legald.getChapters().get(chap_count).getArticles().get(
                                                    art_count).getParagraphs().get(count2).getCaseList().get(
                                                    count4).getModification().setFragment(article);
                                        }
                                    }
                                } else {
                                    if (legald.getParts().get(part_count).getChapters().isEmpty()) {
                                        if (article.getURI().split("\\/[0-9]+\\/modification")[0].endsWith("passage")) {
                                            legald.getParts().get(part_count).getArticles().get(
                                                    art_count).getParagraphs().get(count2).getPassages().get(
                                                    count3).getModification().setType("Article");
                                            legald.getParts().get(part_count).getArticles().get(
                                                    art_count).getParagraphs().get(count2).getPassages().get(
                                                    count3).getModification().setFragment(article);
                                        } else {
                                            legald.getParts().get(part_count).getArticles().get(
                                                    art_count).getParagraphs().get(count2).getCaseList().get(
                                                    count4).getModification().setType("Article");
                                            legald.getParts().get(part_count).getArticles().get(
                                                    art_count).getParagraphs().get(count2).getCaseList().get(
                                                    count4).getModification().setFragment(article);
                                        }
                                    } else {
                                        if (article.getURI().split("\\/[0-9]+\\/modification")[0].endsWith("passage")) {
                                            legald.getParts().get(part_count).getChapters().get(
                                                    chap_count).getArticles().get(art_count).getParagraphs().get(
                                                    count2).getPassages().get(count3).getModification().setType(
                                                    "Article");
                                            legald.getParts().get(part_count).getChapters().get(
                                                    chap_count).getArticles().get(art_count).getParagraphs().get(
                                                    count2).getPassages().get(count3).getModification().setFragment(
                                                    article);
                                        } else {
                                            //System.out.println("=======================================================================================");
                                            // System.out.println(article.getURI());
                                            //System.out.println("ERT:  chapter/"+chap_count+"/article/"+art_count+"/paragraph/"+count2+"/passage/"+count4);
                                            legald.getParts().get(part_count).getChapters().get(
                                                    chap_count).getArticles().get(art_count).getParagraphs().get(
                                                    count2).getCaseList().get(count4).getModification().setType(
                                                    "Article");
                                            legald.getParts().get(part_count).getChapters().get(
                                                    chap_count).getArticles().get(art_count).getParagraphs().get(
                                                    count2).getCaseList().get(count4).getModification().setFragment(
                                                    article);
                                        }
                                    }
                                }
                                mod_count3 = -1;
                                mod_count4 = -1;
                            } else {
                                //System.out.println("NEW ARTICLE"+art_count);
                                if (legald.getParts().isEmpty()) {
                                    if (legald.getChapters().isEmpty()) {
                                        legald.getArticles().add(article);
                                    } else {
                                        legald.getChapters().get(chap_count).getArticles().add(article);
                                    }
                                } else {
                                    if (legald.getParts().get(part_count).getChapters().isEmpty()) {
                                        legald.getParts().get(part_count).getArticles().add(article);
                                    } else {
                                        legald.getParts().get(part_count).getChapters().get(
                                                chap_count).getArticles().add(article);
                                    }
                                }
                                art_count++;
                                art_count2++;
                                count2 = -1;
                                count3 = -1;
                                count4 = -1;
                                mod = 0;
                            }
                        } else if (bindingSet.getValue("type").toString().equals(uriBase + "ontology/Image")) {
                            String number = bindingSet.getValue("part").toString().split("image\\/")[1];
                            if (legald.getParts().isEmpty()) {
                                if (legald.getChapters().isEmpty()) {
                                    legald.getArticles().get(art_count).getParagraphs().get(count2).getImages().add(
                                            CommonUtils.trimDoubleQuotes(
                                                    bindingSet.getValue("filename").toString().split("\"")[1]));
                                } else {
                                    legald.getChapters().get(chap_count).getArticles().get(
                                            art_count).getParagraphs().get(count2).getImages().add(CommonUtils.trimDoubleQuotes(
                                            bindingSet.getValue("filename").toString().split("\"")[1]));

                                }
                            } else {
                                if (legald.getParts().get(part_count).getChapters().isEmpty()) {
                                    legald.getParts().get(part_count).getArticles().get(art_count).getParagraphs().get(
                                            count2).getImages().add(CommonUtils.trimDoubleQuotes(
                                            bindingSet.getValue("filename").toString().split("\"")[1]));
                                } else {
                                    legald.getParts().get(part_count).getChapters().get(chap_count).getArticles().get(
                                            art_count).getParagraphs().get(count2).getImages().add(CommonUtils.trimDoubleQuotes(
                                            bindingSet.getValue("filename").toString().split("\"")[1]));

                                }
                            }
                            legald.getImages().add(
                                    CommonUtils.trimDoubleQuotes(bindingSet.getValue("filename").toString().split("\"")[1]));
                        } else if (bindingSet.getValue("type").toString().equals(uriBase + "ontology/Paragraph")) {
                            paragraph = new Paragraph();
                            paragraph.setURI(bindingSet.getValue("part").toString());

                            if (mod >= 1 && paragraph.getURI().contains("modification")) {
                                paragraph.setId(paragraph.getURI().split("paragraph\\/")[2]);
                            } else {
                                paragraph.setId(paragraph.getURI().split("paragraph\\/")[1]);
                            }

                            if ((mod == 0) || (mod == 2) ||
                                    ((mod == 1) && (!paragraph.getURI().contains("modification")))) {
                                //System.out.println("NEW PARAGRAPH"+count2);
                                if (legald.getParts().isEmpty()) {
                                    if (legald.getChapters().isEmpty()) {
                                        legald.getArticles().get(art_count).getParagraphs().add(paragraph);
                                    } else {
                                        legald.getChapters().get(chap_count).getArticles().get(
                                                art_count).getParagraphs().add(paragraph);
                                    }
                                } else {
                                    if (legald.getParts().get(part_count).getChapters().isEmpty()) {
                                        legald.getParts().get(part_count).getArticles().get(
                                                art_count).getParagraphs().add(paragraph);
                                    } else {
                                        legald.getParts().get(part_count).getChapters().get(
                                                chap_count).getArticles().get(art_count).getParagraphs().add(paragraph);
                                    }
                                }
                                //legald.getArticles().get(art_count).getParagraphs().add(paragraph);
                                count2++;
                                count3 = -1;
                                count4 = -1;
                                mod = 0;
                            } else if ((mod == 1) && (paragraph.getURI().contains("modification"))) {
                                //System.out.println("MODIFICATION PARAGRAPH");
                                mod = 2;
                                if (legald.getParts().isEmpty()) {
                                    if (legald.getChapters().isEmpty()) {
                                        if (paragraph.getURI().split("\\/[0-9]+\\/modification")[0].endsWith(
                                                "passage")) {
                                            legald.getArticles().get(art_count).getParagraphs().get(
                                                    count2).getPassages().get(count3).getModification().setType(
                                                    "Paragraph");
                                            legald.getArticles().get(art_count).getParagraphs().get(
                                                    count2).getPassages().get(count3).getModification().setFragment(
                                                    paragraph);
                                        } else {
                                            legald.getArticles().get(art_count).getParagraphs().get(
                                                    count2).getCaseList().get(count4).getModification().setType(
                                                    "Paragraph");
                                            legald.getArticles().get(art_count).getParagraphs().get(
                                                    count2).getCaseList().get(count4).getModification().setFragment(
                                                    paragraph);
                                        }
                                    } else {
                                        if (paragraph.getURI().split("\\/[0-9]+\\/modification")[0].endsWith(
                                                "passage")) {
                                            legald.getChapters().get(chap_count).getArticles().get(
                                                    art_count).getParagraphs().get(count2).getPassages().get(
                                                    count3).getModification().setType("Paragraph");
                                            legald.getChapters().get(chap_count).getArticles().get(
                                                    art_count).getParagraphs().get(count2).getPassages().get(
                                                    count3).getModification().setFragment(paragraph);
                                        } else {
                                            legald.getChapters().get(chap_count).getArticles().get(
                                                    art_count).getParagraphs().get(count2).getCaseList().get(
                                                    count4).getModification().setType("Paragraph");
                                            legald.getChapters().get(chap_count).getArticles().get(
                                                    art_count).getParagraphs().get(count2).getCaseList().get(
                                                    count4).getModification().setFragment(paragraph);
                                        }
                                    }
                                } else {
                                    if (legald.getParts().get(part_count).getChapters().isEmpty()) {
                                        if (paragraph.getURI().split("\\/[0-9]+\\/modification")[0].endsWith(
                                                "passage")) {
                                            legald.getParts().get(part_count).getArticles().get(
                                                    art_count).getParagraphs().get(count2).getPassages().get(
                                                    count3).getModification().setType("Paragraph");
                                            legald.getParts().get(part_count).getArticles().get(
                                                    art_count).getParagraphs().get(count2).getPassages().get(
                                                    count3).getModification().setFragment(paragraph);
                                        } else {
                                            legald.getParts().get(part_count).getArticles().get(
                                                    art_count).getParagraphs().get(count2).getCaseList().get(
                                                    count4).getModification().setType("Paragraph");
                                            legald.getParts().get(part_count).getArticles().get(
                                                    art_count).getParagraphs().get(count2).getCaseList().get(
                                                    count4).getModification().setFragment(paragraph);
                                        }
                                    } else {
                                        if (paragraph.getURI().split("\\/[0-9]+\\/modification")[0].endsWith(
                                                "passage")) {
                                            legald.getParts().get(part_count).getChapters().get(
                                                    chap_count).getArticles().get(art_count).getParagraphs().get(
                                                    count2).getPassages().get(count3).getModification().setType(
                                                    "Paragraph");
                                            legald.getParts().get(part_count).getChapters().get(
                                                    chap_count).getArticles().get(art_count).getParagraphs().get(
                                                    count2).getPassages().get(count3).getModification().setFragment(
                                                    paragraph);
                                        } else {
                                            legald.getParts().get(part_count).getChapters().get(
                                                    chap_count).getArticles().get(art_count).getParagraphs().get(
                                                    count2).getCaseList().get(count4).getModification().setType(
                                                    "Paragraph");
                                            legald.getParts().get(part_count).getChapters().get(
                                                    chap_count).getArticles().get(art_count).getParagraphs().get(
                                                    count2).getCaseList().get(count4).getModification().setFragment(
                                                    paragraph);
                                        }
                                    }
                                }
                                mod_count3 = -1;
                                mod_count4 = -1;
                            } else if ((mod == 3) && (paragraph.getURI().contains("modification"))) {
                                //System.out.println("MODIFICATION ARTICLE PARAGRAPH"+count2);
                                article.getParagraphs().add(paragraph);
                                if (legald.getParts().isEmpty()) {
                                    if (legald.getChapters().isEmpty()) {
                                        if (article.getURI().split("\\/[0-9]+\\/modification")[0].endsWith("passage")) {
                                            legald.getArticles().get(art_count).getParagraphs().get(
                                                    count2).getPassages().get(count3).getModification().setFragment(
                                                    article);
                                        } else {
                                            legald.getArticles().get(art_count).getParagraphs().get(
                                                    count2).getCaseList().get(count4).getModification().setFragment(
                                                    article);
                                        }
                                    } else {
                                        if (article.getURI().split("\\/[0-9]+\\/modification")[0].endsWith("passage")) {
                                            legald.getChapters().get(chap_count).getArticles().get(
                                                    art_count).getParagraphs().get(count2).getPassages().get(
                                                    count3).getModification().setFragment(article);
                                        } else {
                                            legald.getChapters().get(chap_count).getArticles().get(
                                                    art_count).getParagraphs().get(count2).getCaseList().get(
                                                    count4).getModification().setFragment(article);
                                        }
                                    }
                                } else {
                                    if (legald.getParts().get(part_count).getChapters().isEmpty()) {
                                        if (article.getURI().split("\\/[0-9]+\\/modification")[0].endsWith("passage")) {
                                            legald.getParts().get(part_count).getArticles().get(
                                                    art_count).getParagraphs().get(count2).getPassages().get(
                                                    count3).getModification().setFragment(article);
                                        } else {
                                            legald.getParts().get(part_count).getArticles().get(
                                                    art_count).getParagraphs().get(count2).getCaseList().get(
                                                    count4).getModification().setFragment(article);
                                        }
                                    } else {
                                        if (article.getURI().split("\\/[0-9]+\\/modification")[0].endsWith("passage")) {
                                            legald.getParts().get(part_count).getChapters().get(
                                                    chap_count).getArticles().get(art_count).getParagraphs().get(
                                                    count2).getPassages().get(count3).getModification().setFragment(
                                                    article);
                                        } else {
                                            legald.getParts().get(part_count).getChapters().get(
                                                    chap_count).getArticles().get(art_count).getParagraphs().get(
                                                    count2).getCaseList().get(count4).getModification().setFragment(
                                                    article);
                                        }
                                    }
                                }
                                mod_count3 = -1;
                                mod_count4 = -1;
                            } else if ((mod == 3) && (!paragraph.getURI().contains("modification"))) {
                                //System.out.println("NEW PARAGRAPH"+count2);
                                if (legald.getParts().isEmpty()) {
                                    if (legald.getChapters().isEmpty()) {
                                        legald.getArticles().get(art_count).getParagraphs().add(paragraph);
                                    } else {
                                        legald.getChapters().get(chap_count).getArticles().get(
                                                art_count).getParagraphs().add(paragraph);
                                    }
                                } else {
                                    if (legald.getParts().get(part_count).getChapters().isEmpty()) {
                                        legald.getParts().get(part_count).getArticles().get(
                                                art_count).getParagraphs().add(paragraph);
                                    } else {
                                        legald.getParts().get(part_count).getChapters().get(
                                                chap_count).getArticles().get(art_count).getParagraphs().add(paragraph);
                                    }
                                }
                                count2++;
                                count3 = -1;
                                count4 = -1;
                                mod = 0;
                            }

                        } else if (bindingSet.getValue("type").toString().equals(uriBase + "ontology/Passage")) {

                            if (!old.equals(bindingSet.getValue("part").toString())) {

                                Passage passage = new Passage();

                                passage.setURI(bindingSet.getValue("part").toString());

                                if (req == 1 && bindingSet.getValue("html") != null) {
                                    String text = bindingSet.getValue("html").toString().split("\\^\\^", 2)[0];
                                    passage.setText(CommonUtils.trimDoubleQuotes(text));
                                } else {
                                    String text = bindingSet.getValue("text").toString().replace("@el", "");
                                    passage.setText(CommonUtils.trimDoubleQuotes(text));
                                }

                                //System.out.println(passage.getURI());

                                if ((mod == 0) || (!passage.getURI().contains("modification"))) {
                                    passage.setId(count3 + 2);
                                    //System.out.println("NEW PASSAGE"+count3);
                                    if (legald.getParts().isEmpty()) {
                                        if (legald.getChapters().isEmpty()) {
                                            legald.getArticles().get(art_count).getParagraphs().get(
                                                    count2).getPassages().add(passage);
                                        } else {
                                            legald.getChapters().get(chap_count).getArticles().get(
                                                    art_count).getParagraphs().get(count2).getPassages().add(passage);
                                        }
                                    } else {
                                        if (legald.getParts().get(part_count).getChapters().isEmpty()) {
                                            legald.getParts().get(part_count).getArticles().get(
                                                    art_count).getParagraphs().get(count2).getPassages().add(passage);
                                        } else {
                                            legald.getParts().get(part_count).getChapters().get(
                                                    chap_count).getArticles().get(art_count).getParagraphs().get(
                                                    count2).getPassages().add(passage);
                                        }
                                    }
                                    count3++;
                                } else if (mod == 1) {
                                    passage.setId(mod_count3 + 2);
                                    //System.out.println("MODIFICATION PASSAGE");
                                    if (legald.getParts().isEmpty()) {
                                        if (legald.getChapters().isEmpty()) {
                                            if (passage.getURI().split("\\/[0-9]+\\/modification")[0].endsWith(
                                                    "passage")) {
                                                legald.getArticles().get(art_count).getParagraphs().get(
                                                        count2).getPassages().get(count3).getModification().setType(
                                                        "Passage");
                                                legald.getArticles().get(art_count).getParagraphs().get(
                                                        count2).getPassages().get(count3).getModification().setFragment(
                                                        passage);
                                            } else {
                                                legald.getArticles().get(art_count).getParagraphs().get(
                                                        count2).getCaseList().get(count4).getModification().setType(
                                                        "Passage");
                                                legald.getArticles().get(art_count).getParagraphs().get(
                                                        count2).getCaseList().get(count4).getModification().setFragment(
                                                        passage);
                                            }
                                        } else {
                                            if (passage.getURI().split("\\/[0-9]+\\/modification")[0].endsWith(
                                                    "passage")) {
                                                legald.getChapters().get(chap_count).getArticles().get(
                                                        art_count).getParagraphs().get(count2).getPassages().get(
                                                        count3).getModification().setType("Passage");
                                                legald.getChapters().get(chap_count).getArticles().get(
                                                        art_count).getParagraphs().get(count2).getPassages().get(
                                                        count3).getModification().setFragment(passage);
                                            } else {
                                                legald.getChapters().get(chap_count).getArticles().get(
                                                        art_count).getParagraphs().get(count2).getCaseList().get(
                                                        count4).getModification().setType("Passage");
                                                legald.getChapters().get(chap_count).getArticles().get(
                                                        art_count).getParagraphs().get(count2).getCaseList().get(
                                                        count4).getModification().setFragment(passage);
                                            }
                                        }
                                    } else {
                                        if (legald.getParts().get(part_count).getChapters().isEmpty()) {
                                            if (passage.getURI().split("\\/[0-9]+\\/modification")[0].endsWith(
                                                    "passage")) {
                                                legald.getParts().get(part_count).getArticles().get(
                                                        art_count).getParagraphs().get(count2).getPassages().get(
                                                        count3).getModification().setType("Passage");
                                                legald.getParts().get(part_count).getArticles().get(
                                                        art_count).getParagraphs().get(count2).getPassages().get(
                                                        count3).getModification().setFragment(passage);
                                            } else {
                                                legald.getParts().get(part_count).getArticles().get(
                                                        art_count).getParagraphs().get(count2).getCaseList().get(
                                                        count4).getModification().setType("Passage");
                                                legald.getParts().get(part_count).getArticles().get(
                                                        art_count).getParagraphs().get(count2).getCaseList().get(
                                                        count4).getModification().setFragment(passage);
                                            }
                                        } else {
                                            if (passage.getURI().split("\\/[0-9]+\\/modification")[0].endsWith(
                                                    "passage")) {
                                                legald.getParts().get(part_count).getChapters().get(
                                                        chap_count).getArticles().get(art_count).getParagraphs().get(
                                                        count2).getPassages().get(count3).getModification().setType(
                                                        "Passage");
                                                legald.getParts().get(part_count).getChapters().get(
                                                        chap_count).getArticles().get(art_count).getParagraphs().get(
                                                        count2).getPassages().get(count3).getModification().setFragment(
                                                        passage);
                                            } else {
                                                legald.getParts().get(part_count).getChapters().get(
                                                        chap_count).getArticles().get(art_count).getParagraphs().get(
                                                        count2).getCaseList().get(count4).getModification().setType(
                                                        "Passage");
                                                legald.getParts().get(part_count).getChapters().get(
                                                        chap_count).getArticles().get(art_count).getParagraphs().get(
                                                        count2).getCaseList().get(count4).getModification().setFragment(
                                                        passage);
                                            }
                                        }
                                    }
                                    mod = 0;
                                    mod_count3++;
                                } else if (mod == 2) {
                                    passage.setId(mod_count3 + 2);
                                    //System.out.println("PARAGRAPH MODIFICATION PASSAGE");
                                    paragraph.getPassages().add(passage);
                                    if (legald.getParts().isEmpty()) {
                                        if (legald.getChapters().isEmpty()) {
                                            if (paragraph.getURI().split("\\/[0-9]+\\/modification")[0].endsWith(
                                                    "passage")) {
                                                legald.getArticles().get(art_count).getParagraphs().get(
                                                        count2).getPassages().get(count3).getModification().setFragment(
                                                        paragraph);
                                            } else {
                                                legald.getArticles().get(art_count).getParagraphs().get(
                                                        count2).getCaseList().get(count4).getModification().setFragment(
                                                        paragraph);
                                            }
                                        } else {
                                            if (paragraph.getURI().split("\\/[0-9]+\\/modification")[0].endsWith(
                                                    "passage")) {
                                                legald.getChapters().get(chap_count).getArticles().get(
                                                        art_count).getParagraphs().get(count2).getPassages().get(
                                                        count3).getModification().setFragment(paragraph);
                                            } else {
                                                legald.getChapters().get(chap_count).getArticles().get(
                                                        art_count).getParagraphs().get(count2).getCaseList().get(
                                                        count4).getModification().setFragment(paragraph);
                                            }
                                        }
                                    } else {
                                        if (legald.getParts().get(part_count).getChapters().isEmpty()) {
                                            if (paragraph.getURI().split("\\/[0-9]+\\/modification")[0].endsWith(
                                                    "passage")) {
                                                legald.getParts().get(part_count).getArticles().get(
                                                        art_count).getParagraphs().get(count2).getPassages().get(
                                                        count3).getModification().setFragment(paragraph);
                                            } else {
                                                legald.getParts().get(part_count).getArticles().get(
                                                        art_count).getParagraphs().get(count2).getCaseList().get(
                                                        count4).getModification().setFragment(paragraph);
                                            }
                                        } else {
                                            if (paragraph.getURI().split("\\/[0-9]+\\/modification")[0].endsWith(
                                                    "passage")) {
                                                legald.getParts().get(part_count).getChapters().get(
                                                        chap_count).getArticles().get(art_count).getParagraphs().get(
                                                        count2).getPassages().get(count3).getModification().setFragment(
                                                        paragraph);
                                            } else {
                                                legald.getParts().get(part_count).getChapters().get(
                                                        chap_count).getArticles().get(art_count).getParagraphs().get(
                                                        count2).getCaseList().get(count4).getModification().setFragment(
                                                        paragraph);
                                            }
                                        }
                                    }
                                    mod_count3++;
                                } else if ((mod == 3) && (passage.getURI().contains("modification"))) {
                                    passage.setId(mod_count3 + 2);
                                    //System.out.println("ARTICLE PARAGRAPH MODIFICATION PASSAGE");
                                    article.getParagraphs().get(article.getParagraphs().size() - 1).getPassages().add(
                                            passage);
                                    if (legald.getParts().isEmpty()) {
                                        if (legald.getChapters().isEmpty()) {
                                            if (article.getURI().split("\\/[0-9]+\\/modification")[0].endsWith(
                                                    "passage")) {
                                                legald.getArticles().get(art_count).getParagraphs().get(
                                                        count2).getPassages().get(count3).getModification().setFragment(
                                                        article);
                                            } else {
                                                legald.getArticles().get(art_count).getParagraphs().get(
                                                        count2).getCaseList().get(count4).getModification().setFragment(
                                                        article);
                                            }
                                        } else {
                                            if (article.getURI().split("\\/[0-9]+\\/modification")[0].endsWith(
                                                    "passage")) {
                                                legald.getChapters().get(chap_count).getArticles().get(
                                                        art_count).getParagraphs().get(count2).getPassages().get(
                                                        count3).getModification().setFragment(article);
                                            } else {
                                                legald.getChapters().get(chap_count).getArticles().get(
                                                        art_count).getParagraphs().get(count2).getCaseList().get(
                                                        count4).getModification().setFragment(article);
                                            }
                                        }
                                    } else {
                                        if (legald.getParts().get(part_count).getChapters().isEmpty()) {
                                            if (article.getURI().split("\\/[0-9]+\\/modification")[0].endsWith(
                                                    "passage")) {
                                                legald.getParts().get(part_count).getArticles().get(
                                                        art_count).getParagraphs().get(count2).getPassages().get(
                                                        count3).getModification().setFragment(article);
                                            } else {
                                                legald.getParts().get(part_count).getArticles().get(
                                                        art_count).getParagraphs().get(count2).getCaseList().get(
                                                        count4).getModification().setFragment(article);
                                            }
                                        } else {
                                            if (article.getURI().split("\\/[0-9]+\\/modification")[0].endsWith(
                                                    "passage")) {
                                                legald.getParts().get(part_count).getChapters().get(
                                                        chap_count).getArticles().get(art_count).getParagraphs().get(
                                                        count2).getPassages().get(count3).getModification().setFragment(
                                                        article);
                                            } else {
                                                legald.getParts().get(part_count).getChapters().get(
                                                        chap_count).getArticles().get(art_count).getParagraphs().get(
                                                        count2).getCaseList().get(count4).getModification().setFragment(
                                                        article);
                                            }
                                        }
                                    }
                                    mod_count3++;
                                } else if ((mod == 3) && (!passage.getURI().contains("modification"))) {
                                    passage.setId(count3 + 2);
                                    //System.out.println("NEW PASSAGE"+count3);
                                    if (legald.getParts().isEmpty()) {
                                        if (legald.getChapters().isEmpty()) {
                                            legald.getArticles().get(art_count).getParagraphs().get(
                                                    legald.getArticles().get(art_count).getParagraphs().size() -
                                                            1).getPassages().add(passage);
                                        } else {
                                            legald.getChapters().get(chap_count).getArticles().get(
                                                    art_count).getParagraphs().get(
                                                    legald.getChapters().get(chap_count).getArticles().get(
                                                            art_count).getParagraphs().size() - 1).getPassages().add(
                                                    passage);
                                        }
                                    } else {
                                        if (legald.getParts().get(part_count).getChapters().isEmpty()) {
                                            legald.getParts().get(part_count).getArticles().get(
                                                    art_count).getParagraphs().get(
                                                    legald.getArticles().get(art_count).getParagraphs().size() -
                                                            1).getPassages().add(passage);
                                        } else {
                                            legald.getParts().get(part_count).getChapters().get(
                                                    chap_count).getArticles().get(art_count).getParagraphs().get(
                                                    legald.getChapters().get(chap_count).getArticles().get(
                                                            art_count).getParagraphs().size() - 1).getPassages().add(
                                                    passage);
                                        }
                                    }
                                    mod = 0;
                                    count3++;
                                }


                                old = bindingSet.getValue("part").toString();

                            }

                        } else if (bindingSet.getValue("type").toString().equals(uriBase + "ontology/Table")) {

                            String table = bindingSet.getValue("text").toString().replace("@html", "");
                            //System.out.println(bindingSet.getValue("part").toString());

                            if ((mod == 0) || (!table.contains("modification"))) {
                                //System.out.println("NEW TABLE");
                                if (legald.getChapters().isEmpty()) {
                                    legald.getArticles().get(art_count).getParagraphs().get(count2).setTable(
                                            CommonUtils.trimDoubleQuotes(table));
                                } else {
                                    legald.getChapters().get(chap_count).getArticles().get(
                                            art_count).getParagraphs().get(count2).setTable(CommonUtils.trimDoubleQuotes(table));
                                }
                                //legald.getArticles().get(art_count).getParagraphs().get(count2).setTable(CommonUtils.trimDoubleQuotes(table));
                            } else if ((mod == 1) || (mod == 2)) {
                                //System.out.println("MODIFICATION TABLE");
                                paragraph.setTable(CommonUtils.trimDoubleQuotes(table));
                                if (legald.getChapters().isEmpty()) {
                                    if (paragraph.getURI().split("\\/[0-9]+\\/modification")[0].endsWith("passage")) {
                                        legald.getArticles().get(art_count).getParagraphs().get(
                                                count2).getPassages().get(count3).getModification().setFragment(
                                                paragraph);
                                    } else {
                                        legald.getArticles().get(art_count).getParagraphs().get(
                                                count2).getCaseList().get(count4).getModification().setFragment(
                                                paragraph);
                                    }
                                } else {
                                    if (paragraph.getURI().split("\\/[0-9]+\\/modification")[0].endsWith("passage")) {
                                        legald.getChapters().get(chap_count).getArticles().get(
                                                art_count).getParagraphs().get(count2).getPassages().get(
                                                count3).getModification().setFragment(paragraph);
                                    } else {
                                        legald.getChapters().get(chap_count).getArticles().get(
                                                art_count).getParagraphs().get(count2).getCaseList().get(
                                                count4).getModification().setFragment(paragraph);
                                    }
                                }
                            } else {
                                article.getParagraphs().get(article.getParagraphs().size() - 1).setTable(
                                        CommonUtils.trimDoubleQuotes(table));
                                if (legald.getChapters().isEmpty()) {
                                    if (article.getURI().split("\\/[0-9]+\\/modification")[0].endsWith("passage")) {
                                        legald.getArticles().get(art_count).getParagraphs().get(
                                                count2).getPassages().get(count3).getModification().setFragment(
                                                article);
                                    } else {
                                        legald.getArticles().get(art_count).getParagraphs().get(
                                                count2).getCaseList().get(count4).getModification().setFragment(
                                                article);
                                    }
                                } else {
                                    if (article.getURI().split("\\/[0-9]+\\/modification")[0].endsWith("passage")) {
                                        legald.getChapters().get(chap_count).getArticles().get(
                                                art_count).getParagraphs().get(count2).getPassages().get(
                                                count3).getModification().setFragment(article);
                                    } else {
                                        legald.getChapters().get(chap_count).getArticles().get(
                                                art_count).getParagraphs().get(count2).getCaseList().get(
                                                count4).getModification().setFragment(article);
                                    }
                                }
                            }

                        } else if (bindingSet.getValue("type").toString().equals(uriBase + "ontology/Case")) {

                            if (!old.equals(bindingSet.getValue("part").toString())) {

                                Case case1 = new Case();

                                case1.setURI(bindingSet.getValue("part").toString());
                                Passage passage = new Passage();

                                if (req == 1 && bindingSet.getValue("html") != null) {
                                    String text = bindingSet.getValue("html").toString().split("\\^\\^", 2)[0];
                                    passage.setText(CommonUtils.trimDoubleQuotes(text));
                                } else {
                                    String text = bindingSet.getValue("text").toString().replace("@el", "");
                                    passage.setText(CommonUtils.trimDoubleQuotes(text));
                                }

                                //System.out.println(case1.getURI());
                                case1.getPassages().add(passage);
                                String[] cases = case1.getURI().split("case");
                                old = bindingSet.getValue("part").toString();
                                //case1.setText(bindingSet.getValue("text").toString());

                                if (cases.length > 2) {
                                    case1.setId(Integer.parseInt(case1.getURI().split("case\\/")[1]));
                                    //System.out.println("NEW CASE"+count4);
                                    if (legald.getParts().isEmpty()) {
                                        if (legald.getChapters().isEmpty()) {
                                            legald.getArticles().get(art_count).getParagraphs().get(
                                                    count2).getCaseList().get(count4).getCaseList().add(case1);
                                        } else {
                                            legald.getChapters().get(chap_count).getArticles().get(
                                                    art_count).getParagraphs().get(count2).getCaseList().get(
                                                    count4).getCaseList().add(case1);
                                        }
                                    } else {
                                        if (legald.getParts().get(part_count).getChapters().isEmpty()) {
                                            legald.getParts().get(part_count).getArticles().get(
                                                    art_count).getParagraphs().get(count2).getCaseList().get(
                                                    count4).getCaseList().add(case1);
                                        } else {
                                            legald.getParts().get(part_count).getChapters().get(
                                                    chap_count).getArticles().get(art_count).getParagraphs().get(
                                                    count2).getCaseList().get(count4).getCaseList().add(case1);
                                        }
                                    }
                                } else if (mod == 0) {
                                    case1.setId(Integer.parseInt(case1.getURI().split("case\\/")[1]));
                                    //System.out.println("NEW CASE"+count4);
                                    if (legald.getParts().isEmpty()) {
                                        if (legald.getChapters().isEmpty()) {
                                            legald.getArticles().get(art_count).getParagraphs().get(
                                                    count2).getCaseList().add(case1);
                                        } else {
                                            legald.getChapters().get(chap_count).getArticles().get(
                                                    art_count).getParagraphs().get(count2).getCaseList().add(case1);
                                        }
                                    } else {
                                        if (legald.getParts().get(part_count).getChapters().isEmpty()) {
                                            legald.getParts().get(part_count).getArticles().get(
                                                    art_count).getParagraphs().get(count2).getCaseList().add(case1);
                                        } else {
                                            legald.getParts().get(part_count).getChapters().get(
                                                    chap_count).getArticles().get(art_count).getParagraphs().get(
                                                    count2).getCaseList().add(case1);
                                        }
                                    }
                                    count4++;
                                } else if (mod == 1) {
                                    case1.setId(Integer.parseInt(case1.getURI().split("case\\/")[1]));
                                    //System.out.println("MODIFICATION CASE");
                                    if (legald.getParts().isEmpty()) {
                                        if (legald.getChapters().isEmpty()) {
                                            if (case1.getURI().split("\\/[0-9]+\\/modification")[0].endsWith(
                                                    "passage")) {
                                                legald.getArticles().get(art_count).getParagraphs().get(
                                                        count2).getPassages().get(count3).getModification().setType(
                                                        "Case");
                                                legald.getArticles().get(art_count).getParagraphs().get(
                                                        count2).getPassages().get(count3).getModification().setFragment(
                                                        case1);
                                            } else {
                                                legald.getArticles().get(art_count).getParagraphs().get(
                                                        count2).getCaseList().get(count4).getModification().setType(
                                                        "Case");
                                                legald.getArticles().get(art_count).getParagraphs().get(
                                                        count2).getCaseList().get(count4).getModification().setFragment(
                                                        case1);
                                            }
                                        } else {
                                            if (case1.getURI().split("\\/[0-9]+\\/modification")[0].endsWith(
                                                    "passage")) {
                                                legald.getChapters().get(chap_count).getArticles().get(
                                                        art_count).getParagraphs().get(count2).getPassages().get(
                                                        count3).getModification().setType("Case");
                                                legald.getChapters().get(chap_count).getArticles().get(
                                                        art_count).getParagraphs().get(count2).getPassages().get(
                                                        count3).getModification().setFragment(case1);
                                            } else {
                                                legald.getChapters().get(chap_count).getArticles().get(
                                                        art_count).getParagraphs().get(count2).getCaseList().get(
                                                        count4).getModification().setType("Case");
                                                legald.getChapters().get(chap_count).getArticles().get(
                                                        art_count).getParagraphs().get(count2).getCaseList().get(
                                                        count4).getModification().setFragment(case1);
                                            }
                                        }
                                    } else {
                                        if (legald.getParts().get(part_count).getChapters().isEmpty()) {
                                            if (case1.getURI().split("\\/[0-9]+\\/modification")[0].endsWith(
                                                    "passage")) {
                                                legald.getParts().get(part_count).getArticles().get(
                                                        art_count).getParagraphs().get(count2).getPassages().get(
                                                        count3).getModification().setType("Case");
                                                legald.getParts().get(part_count).getArticles().get(
                                                        art_count).getParagraphs().get(count2).getPassages().get(
                                                        count3).getModification().setFragment(case1);
                                            } else {
                                                legald.getParts().get(part_count).getArticles().get(
                                                        art_count).getParagraphs().get(count2).getCaseList().get(
                                                        count4).getModification().setType("Case");
                                                legald.getParts().get(part_count).getArticles().get(
                                                        art_count).getParagraphs().get(count2).getCaseList().get(
                                                        count4).getModification().setFragment(case1);
                                            }
                                        } else {
                                            if (case1.getURI().split("\\/[0-9]+\\/modification")[0].endsWith(
                                                    "passage")) {
                                                legald.getParts().get(part_count).getChapters().get(
                                                        chap_count).getArticles().get(art_count).getParagraphs().get(
                                                        count2).getPassages().get(count3).getModification().setType(
                                                        "Case");
                                                legald.getParts().get(part_count).getChapters().get(
                                                        chap_count).getArticles().get(art_count).getParagraphs().get(
                                                        count2).getPassages().get(count3).getModification().setFragment(
                                                        case1);
                                            } else {
                                                legald.getParts().get(part_count).getChapters().get(
                                                        chap_count).getArticles().get(art_count).getParagraphs().get(
                                                        count2).getCaseList().get(count4).getModification().setType(
                                                        "Case");
                                                legald.getParts().get(part_count).getChapters().get(
                                                        chap_count).getArticles().get(art_count).getParagraphs().get(
                                                        count2).getCaseList().get(count4).getModification().setFragment(
                                                        case1);
                                            }
                                        }
                                    }
                                    mod = 0;
                                    mod_count4++;
                                } else if (mod == 2) {
                                    case1.setId(Integer.parseInt(
                                            case1.getURI().split("modification")[1].split("case\\/")[1]));
                                    //System.out.println("PARAGPAPH MODIFICATION CASE");
                                    paragraph.getCaseList().add(case1);
                                    if (legald.getParts().isEmpty()) {
                                        if (legald.getChapters().isEmpty()) {
                                            if (paragraph.getURI().split("\\/[0-9]+\\/modification")[0].endsWith(
                                                    "passage")) {
                                                legald.getArticles().get(art_count).getParagraphs().get(
                                                        count2).getPassages().get(count3).getModification().setFragment(
                                                        paragraph);
                                            } else {
                                                legald.getArticles().get(art_count).getParagraphs().get(
                                                        count2).getCaseList().get(count4).getModification().setFragment(
                                                        paragraph);
                                            }
                                        } else {
                                            if (paragraph.getURI().split("\\/[0-9]+\\/modification")[0].endsWith(
                                                    "passage")) {
                                                legald.getChapters().get(chap_count).getArticles().get(
                                                        art_count).getParagraphs().get(count2).getPassages().get(
                                                        count3).getModification().setFragment(paragraph);
                                            } else {
                                                legald.getChapters().get(chap_count).getArticles().get(
                                                        art_count).getParagraphs().get(count2).getCaseList().get(
                                                        count4).getModification().setFragment(paragraph);
                                            }
                                        }
                                    } else {
                                        if (legald.getParts().get(part_count).getChapters().isEmpty()) {
                                            if (paragraph.getURI().split("\\/[0-9]+\\/modification")[0].endsWith(
                                                    "passage")) {
                                                legald.getParts().get(part_count).getArticles().get(
                                                        art_count).getParagraphs().get(count2).getPassages().get(
                                                        count3).getModification().setFragment(paragraph);
                                            } else {
                                                legald.getParts().get(part_count).getArticles().get(
                                                        art_count).getParagraphs().get(count2).getCaseList().get(
                                                        count4).getModification().setFragment(paragraph);
                                            }
                                        } else {
                                            if (paragraph.getURI().split("\\/[0-9]+\\/modification")[0].endsWith(
                                                    "passage")) {
                                                legald.getParts().get(part_count).getChapters().get(
                                                        chap_count).getArticles().get(art_count).getParagraphs().get(
                                                        count2).getPassages().get(count3).getModification().setFragment(
                                                        paragraph);
                                            } else {
                                                legald.getParts().get(part_count).getChapters().get(
                                                        chap_count).getArticles().get(art_count).getParagraphs().get(
                                                        count2).getCaseList().get(count4).getModification().setFragment(
                                                        paragraph);
                                            }
                                        }
                                    }
                                    //legald.getArticles().get(art_count).getParagraphs().get(count2).getModification().setFragment(paragraph);
                                    mod_count4++;
                                } else {
                                    case1.setId(Integer.parseInt(
                                            case1.getURI().split("modification")[1].split("case\\/")[1]));
                                    //System.out.println("ARTICLE PARAGPAPH MODIFICATION CASE");
                                    article.getParagraphs().get(article.getParagraphs().size() - 1).getCaseList().add(
                                            case1);
                                    if (legald.getParts().isEmpty()) {
                                        if (legald.getChapters().isEmpty()) {
                                            if (article.getURI().split("\\/[0-9]+\\/modification")[0].endsWith(
                                                    "passage")) {
                                                legald.getArticles().get(art_count).getParagraphs().get(
                                                        count2).getPassages().get(count3).getModification().setFragment(
                                                        article);
                                            } else {
                                                legald.getArticles().get(art_count).getParagraphs().get(
                                                        count2).getCaseList().get(count4).getModification().setFragment(
                                                        article);
                                            }
                                        } else {
                                            if (article.getURI().split("\\/[0-9]+\\/modification")[0].endsWith(
                                                    "passage")) {
                                                legald.getChapters().get(chap_count).getArticles().get(
                                                        art_count).getParagraphs().get(count2).getPassages().get(
                                                        count3).getModification().setFragment(article);
                                            } else {
                                                legald.getChapters().get(chap_count).getArticles().get(
                                                        art_count).getParagraphs().get(count2).getCaseList().get(
                                                        count4).getModification().setFragment(article);
                                            }
                                        }
                                    } else {
                                        if (legald.getParts().get(part_count).getChapters().isEmpty()) {
                                            if (article.getURI().split("\\/[0-9]+\\/modification")[0].endsWith(
                                                    "passage")) {
                                                legald.getParts().get(part_count).getArticles().get(
                                                        art_count).getParagraphs().get(count2).getPassages().get(
                                                        count3).getModification().setFragment(article);
                                            } else {
                                                legald.getParts().get(part_count).getArticles().get(
                                                        art_count).getParagraphs().get(count2).getCaseList().get(
                                                        count4).getModification().setFragment(article);
                                            }
                                        } else {
                                            if (article.getURI().split("\\/[0-9]+\\/modification")[0].endsWith(
                                                    "passage")) {
                                                legald.getParts().get(part_count).getChapters().get(
                                                        chap_count).getArticles().get(art_count).getParagraphs().get(
                                                        count2).getPassages().get(count3).getModification().setFragment(
                                                        article);
                                            } else {
                                                legald.getParts().get(part_count).getChapters().get(
                                                        chap_count).getArticles().get(art_count).getParagraphs().get(
                                                        count2).getCaseList().get(count4).getModification().setFragment(
                                                        article);
                                            }
                                        }
                                    }
                                    //legald.getArticles().get(art_count).getParagraphs().get(count2).getModification().setFragment(paragraph);
                                    mod_count4++;
                                }

                            }

                        } else if ((bindingSet.getValue("type").toString().equals(uriBase + "ontology/Edit")) ||
                                (bindingSet.getValue("type").toString().equals(uriBase + "ontology/Addition")) ||
                                (bindingSet.getValue("type").toString().equals(uriBase + "ontology/Deletion"))) {

                            Modification modification = new Modification();
                            modification.setURI(bindingSet.getValue("part").toString());
                            //System.out.println(modification.getURI());
                            //System.out.println("MODIFICATION");
                            modification.setType(bindingSet.getValue("type").toString());
                            if (legald.getParts().isEmpty()) {
                                if (legald.getChapters().isEmpty()) {
                                    if (modification.getURI().split("\\/[0-9]+\\/modification")[0].endsWith(
                                            "passage")) {
                                        legald.getArticles().get(art_count).getParagraphs().get(
                                                count2).getPassages().get(count3).setModification(modification);
                                    } else {
                                        legald.getArticles().get(art_count).getParagraphs().get(
                                                count2).getCaseList().get(count4).setModification(modification);
                                    }
                                } else {
                                    if (modification.getURI().split("\\/[0-9]+\\/modification")[0].endsWith(
                                            "passage")) {
                                        legald.getChapters().get(chap_count).getArticles().get(
                                                art_count).getParagraphs().get(count2).getPassages().get(
                                                count3).setModification(modification);
                                    } else {
                                        legald.getChapters().get(chap_count).getArticles().get(
                                                art_count).getParagraphs().get(count2).getCaseList().get(
                                                count4).setModification(modification);
                                    }
                                }
                            } else {
                                if (legald.getParts().get(part_count).getChapters().isEmpty()) {
                                    if (modification.getURI().split("\\/[0-9]+\\/modification")[0].endsWith(
                                            "passage")) {
                                        legald.getParts().get(part_count).getArticles().get(
                                                art_count).getParagraphs().get(count2).getPassages().get(
                                                count3).setModification(modification);
                                    } else {
                                        legald.getParts().get(part_count).getArticles().get(
                                                art_count).getParagraphs().get(count2).getCaseList().get(
                                                count4).setModification(modification);
                                    }
                                } else {
                                    if (modification.getURI().split("\\/[0-9]+\\/modification")[0].endsWith(
                                            "passage")) {
                                        legald.getParts().get(part_count).getChapters().get(
                                                chap_count).getArticles().get(art_count).getParagraphs().get(
                                                count2).getPassages().get(count3).setModification(modification);
                                    } else {
                                        legald.getParts().get(part_count).getChapters().get(
                                                chap_count).getArticles().get(art_count).getParagraphs().get(
                                                count2).getCaseList().get(count4).setModification(modification);
                                    }
                                }
                            }
                            mod = 1;
                            mod_count3 = -1;
                            mod_count4 = -1;
                        } else if (bindingSet.getValue("type").toString().equals(uriBase + "ontology/ParsingIssue")) {
                            String text = bindingSet.getValue("text").toString().replace("@el", "");
                            legald.getIssues().add(CommonUtils.trimDoubleQuotes(text));
                        }

                    }
                } finally {
                    result.close();
                }

                //                int view = Integer.getInteger(legald.getViews());
                //
                //                String queryString2 = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                //                "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
                //                "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n" +
                //                "PREFIX owl: <http://www.w3.org/2002/07/owl#>\n" +
                //                "PREFIX metalex:<http://www.metalex.eu/metalex/2008-05-02#>\n" +
                //                "PREFIX leg: <" + uriBase + "ontology/>\n" +
                //                "PREFIX dc: <http://purl.org/dc/terms/>\n" +
                //                "\n" +
                //                "DELETE WHERE {\n" +
                //                "<" + uriBase + decisionType + "/" + year + "/" + id +">" +
                //                " <" + uriBase + "ontology/views>" +
                //                " \"" + view + "\"^^<http://www.w3.org/2001/XMLSchema#integer>\n" +
                //                "}";
                //
                //                //System.out.println(queryString2);
                //                Update update = con.prepareUpdate(QueryLanguage.SPARQL, queryString2);
                //                update.execute();
                //                con.commit();
                //
                //                view = view+1;
                //
                //                String queryString3 = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                //                "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
                //                "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n" +
                //                "PREFIX owl: <http://www.w3.org/2002/07/owl#>\n" +
                //                "PREFIX metalex:<http://www.metalex.eu/metalex/2008-05-02#>\n" +
                //                "PREFIX leg: <" + uriBase + "ontology/>\n" +
                //                "PREFIX dc: <http://purl.org/dc/terms/>\n" +
                //                "\n" +
                //                "INSERT DATA {\n" +
                //                "<" + uriBase + decisionType + "/" + year + "/" + id +">" +
                //                " <" + uriBase + "ontology/views>" +
                //                " \"" + view + "\"^^<http://www.w3.org/2001/XMLSchema#integer>\n" +
                //                "}";
                //
                //                //System.out.println(queryString3);
                //                Update update2 = con.prepareUpdate(QueryLanguage.SPARQL, queryString3);
                //                update2.execute();
                //                con.commit();

            } finally {
                con.close();
            }

        } catch (OpenRDFException e) {
            throw new NomothesiaException(e);
        }

        //sort containts of legald before displayiing it
        LegalDocumentSort srt = new LegalDocumentSort();

        //getHTMLById(decisionType, year, id, legald);
        return srt.sortld(legald);
    }

    @Override
    public String getRDFById(String decisionType, String year, String id) throws NomothesiaException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        String result = "";

        try {
            RepositoryConnection con = getSesameConnection();
            try {
                String getRdfQuery =
                        "DESCRIBE <" + uriBase + decisionType + "/" + year + "/" + id + ">";
                //System.out.println(getRdfQuery);

                try {
                    // use SPARQL query
                    RDFXMLWriter writer = new RDFXMLWriter(out);
                    con.prepareGraphQuery(QueryLanguage.SPARQL, getRdfQuery).evaluate(writer);
                    out.writeTo(System.out);
                    result = out.toString("ISO-8859-1");
                } catch (IOException ex) {
                    throw new NomothesiaException(ex);
                }
            } finally {
                con.close();
            }
        } catch (OpenRDFException e) {
            throw new NomothesiaException(e);
        }

        return result;
    }

    @Override
    public EndpointResultSet sparqlQuery(EndpointResultSet endpointResult, String format) throws NomothesiaException {
        String results = "";

        if (endpointResult.getQuery().contains("DESCRIBE")) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            try {
                RepositoryConnection con = getSesameConnection();
                try {
                    // use SPARQL query
                    RDFXMLWriter writer = new RDFXMLWriter(out);
                    con.prepareGraphQuery(QueryLanguage.SPARQL, endpointResult.getQuery()).evaluate(writer);
                    out.writeTo(System.out);
                    results += "<tr><td>Result</td></tr><tr><td>";
                    results += "<pre>";
                    String xml = out.toString("UTF-8");
                    xml = xml.replaceAll("<", "&lt");
                    xml = xml.replaceAll(">", "&gt");
                    results += xml + "</pre>";
                    results += "</td></tr>";
                } catch (MalformedQueryException | QueryEvaluationException | IOException | RDFHandlerException ex) {
                    endpointResult.setMessage(ex.toString());
                }
            } catch (RepositoryException ex) {
                endpointResult.setMessage(ex.toString());
            }
        } else {
            if (format.equals("XML")) {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                try {
                    SPARQLResultsXMLWriter sparqlWriter = new SPARQLResultsXMLWriter(out);
                    RepositoryConnection con = getSesameConnection();
                    try {
                        TupleQuery tupleQuery = con.prepareTupleQuery(QueryLanguage.SPARQL, endpointResult.getQuery());
                        tupleQuery.evaluate(sparqlWriter);
                        results += "<tr><td>Result</td></tr><tr><td>";
                        results += "<pre>";
                        String xml = out.toString("UTF-8");
                        xml = xml.replaceAll("<", "&lt");
                        xml = xml.replaceAll(">", "&gt");
                        results += xml + "</pre>";
                        results += "</td></tr>";
                    } catch (IOException | MalformedQueryException | QueryEvaluationException | RepositoryException | TupleQueryResultHandlerException ex) {
                        endpointResult.setMessage(ex.toString());
                    } finally {
                        try {
                            con.close();
                        } catch (RepositoryException ex) {
                            endpointResult.setMessage(ex.toString());
                        }
                    }
                } finally {
                    try {
                        out.close();
                    } catch (IOException ex) {
                        endpointResult.setMessage(ex.toString());
                    }
                }
            } else if (format.equals("HTML")) {
                TupleQueryResult result;
                try {
                    RepositoryConnection con = getSesameConnection();
                    try {
                        //System.out.println(endpointResult.getQuery());
                        TupleQuery tupleQuery = con.prepareTupleQuery(QueryLanguage.SPARQL, endpointResult.getQuery());
                        result = tupleQuery.evaluate();
                        try {
                            //iterate the result set
                            List<String> bindingNames = result.getBindingNames();
                            results += "<tr>";
                            for (String bindingName : bindingNames) {
                                results += "<td>" + bindingName + "</td>";
                            }
                            results += "</tr>";
                            while (result.hasNext()) {
                                BindingSet bindingSet = result.next();
                                results += "<tr>";
                                for (String bindingName : bindingNames) {
                                    if (bindingSet.getValue(bindingName) != null) {
                                        results += "<td>" + bindingSet.getValue(bindingName).toString() + "</td>";
                                    } else {
                                        results += "<td></td>";
                                    }
                                }
                                results += "</tr>";
                            }
                        } finally {
                            try {
                                result.close();
                            } catch (QueryEvaluationException qe) {
                                throw new NomothesiaException(qe);
                            }
                        }
                    } finally {
                        try {
                            con.close();
                        } catch (RepositoryException re) {
                            throw new NomothesiaException(re);
                        }
                    }
                } catch (OpenRDFException e) {
                    //System.out.println("3");
                    endpointResult.setMessage(e.toString());
                    // handle exception
                }
            }

        }
        endpointResult.setResults(results);

        return endpointResult;
    }

    @Override
    public List<Modification> getAllModifications(String decisionType, String year, String id, String date, int req)
            throws NomothesiaException{
        List<Modification> modifications = new ArrayList<>();

        TupleQueryResult result;

        try {
            RepositoryConnection con = getSesameConnection();
            try {
                String getAllModsQuery = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                        "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
                        "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n" +
                        "PREFIX owl: <http://www.w3.org/2002/07/owl#>\n" +
                        "PREFIX metalex:<http://www.metalex.eu/metalex/2008-05-02#>\n" +
                        "PREFIX leg: <" + uriBase + "ontology/>\n" +
                        "PREFIX dc: <http://purl.org/dc/terms/>\n" +
                        "\n" +
                        "SELECT DISTINCT ?mod ?type ?patient ?work ?title ?date ?gaztitle ?part ?type2 ?text\n" +
                        "WHERE{\n" +
                        "<" + uriBase + decisionType + "/" + year + "/" + id + ">" +
                        " metalex:realizedBy  ?version.\n" +
                        " ?version metalex:matterOf ?mod.\n" +
                        " ?mod rdf:type ?type.\n" +
                        " ?mod metalex:patient ?patient.\n" +
                        " ?mod metalex:part+ ?part.\n" +
                        " ?part rdf:type ?type2.\n" +
                        " ?mod  metalex:legislativeCompetenceGround ?work.\n" +
                        " ?work leg:gazette ?gazette.\n" +
                        " ?work dc:created ?date.\n" +
                        " ?gazette dc:title ?gaztitle.\n" +
                        " OPTIONAL{?work  dc:title ?title.}\n";

                if (date != null) {
                    if (req == 1) {
                        getAllModsQuery += "FILTER (?date <= \"" + date + "\"^^xsd:date)\n" +
                                "OPTIONAL{\n" +
                                " ?part leg:text ?text.\n" +
                                "}.}\n" +
                                "ORDER BY ?mod";
                    } else if (req == 2) {
                        getAllModsQuery += "FILTER (?date <= \"" + date + "\"^^xsd:date)\n" +
                                "FILTER NOT EXISTS {FILTER(langMatches(lang(?text), \"html\"))}\n" +
                                "OPTIONAL{\n" +
                                " ?part leg:text ?text.\n" +
                                "}.}\n" +
                                "ORDER BY ?mod";
                    }
                } else {
                    if (req == 1) {
                        getAllModsQuery += "OPTIONAL{\n" +
                                "?part leg:text ?text.\n" +
                                "}.}\n" +
                                "ORDER BY ?mod";
                    } else if (req == 2) {
                        getAllModsQuery += "FILTER NOT EXISTS {FILTER(langMatches(lang(?text), \"html\"))}\n" +
                                "OPTIONAL{\n" +
                                "?part leg:text ?text.\n" +
                                "}.}\n" +
                                "ORDER BY ?mod";
                    }

                }

                //System.out.println(getAllModsQuery);
                TupleQuery tupleQuery = con.prepareTupleQuery(QueryLanguage.SPARQL, getAllModsQuery);
                result = tupleQuery.evaluate();

                try {
                    // iterate the result set
                    int counter = -1;
                    int count = -1;
                    int count2 = -1;
                    int count3 = -1;
                    int count4 = -1;
                    int frag = 0;

                    Fragment fragment = null;
                    Modification mod = null;
                    String old = "old";
                    String current = "";

                    while (result.hasNext()) {
                        BindingSet bindingSet = result.next();
                        if (!bindingSet.getValue("mod").toString().equals(current)) {
                            if (mod != null) {
                                mod.setFragment(fragment);
                                modifications.add(mod);
                            }
                            mod = new Modification();
                            mod.setURI(bindingSet.getValue("mod").toString());
                            mod.setType(bindingSet.getValue("type").toString());
                            mod.setPatient(bindingSet.getValue("patient").toString());
                            mod.getCompetenceGround().setURI(bindingSet.getValue("work").toString());
                            String gaztitle = bindingSet.getValue("gaztitle").toString().replace("^^", "");
                            mod.getCompetenceGround().setFEK(CommonUtils.trimDoubleQuotes(gaztitle));
                            String date2 = bindingSet.getValue("date").toString().replace(
                                    "^^<http://www.w3.org/2001/XMLSchema#date>", "");
                            mod.getCompetenceGround().setPublicationDate(CommonUtils.trimDoubleQuotes(date2));
                            if (bindingSet.getValue("title") != null) {
                                mod.getCompetenceGround().setTitle(
                                        CommonUtils.trimDoubleQuotes(bindingSet.getValue("title").toString().replace("@el", "")));
                            }
                            current = bindingSet.getValue("mod").toString();
                            frag = 0;
                        }

                        if (bindingSet.getValue("type2").toString().equals(uriBase + "ontology/Article")) {
                            Article article = new Article();
                            article.setId(Integer.toString(count + 2));
                            article.setURI(bindingSet.getValue("part").toString());
                            //System.out.println(article.getURI());
                            //System.out.println("NEW ARTICLE");
                            count2 = -1;
                            count3 = -1;
                            count4 = -1;
                            fragment = article;
                            if (mod.getType().contains("Edit")) {
                                fragment.setStatus(2);
                            } else {
                                fragment.setStatus(1);
                            }
                            fragment.setType("Article");
                            frag = 1;

                        } else if (bindingSet.getValue("type2").toString().equals(uriBase + "ontology/Paragraph")) {
                            Paragraph paragraph = new Paragraph();
                            paragraph.setId(Integer.toString(count2 + 2));
                            paragraph.setURI(bindingSet.getValue("part").toString());
                            //System.out.println(paragraph.getURI());
                            //System.out.println("NEW PARAGRAPH");
                            if (frag == 0) {
                                fragment = paragraph;
                                if (mod.getType().contains("Edit")) {
                                    fragment.setStatus(2);
                                } else {
                                    fragment.setStatus(1);
                                }
                                fragment.setType("Paragraph");
                                frag = 2;
                            } else {
                                Article article = (Article) fragment;
                                article.getParagraphs().add(paragraph);
                                fragment = article;
                            }

                            count2++;
                            count3 = -1;
                            count4 = -1;
                        } else if (bindingSet.getValue("type2").toString().equals(uriBase + "ontology/Passage")) {
                            Passage passage = new Passage();
                            passage.setId(count3 + 2);
                            passage.setURI(bindingSet.getValue("part").toString());
                            if (bindingSet.getValue("text").toString().contains("@html")) {
                                String text = bindingSet.getValue("text").toString().replace("@html", "");
                                passage.setText(CommonUtils.trimDoubleQuotes(text));
                            } else if (bindingSet.getValue("text").toString().contains("@el")) {
                                String text = bindingSet.getValue("text").toString().replace("@el", "");
                                passage.setText(CommonUtils.trimDoubleQuotes(text));
                            }

                            //System.out.println(passage.getURI());
                            //System.out.println("NEW PASSAGE");

                            if (frag == 0) {
                                fragment = passage;
                                if (mod.getType().contains("Edit")) {
                                    fragment.setStatus(2);
                                } else {
                                    fragment.setStatus(1);
                                }
                                fragment.setType("Passage");
                            } else if (frag == 1) {
                                Article article = (Article) fragment;
                                article.getParagraphs().get(count2).getPassages().add(passage);
                                fragment = article;
                            } else if (frag == 2) {
                                Paragraph paragraph = (Paragraph) fragment;
                                paragraph.getPassages().add(passage);
                                fragment = paragraph;
                            } else {
                                Paragraph paragraph = (Paragraph) fragment;
                                paragraph.getPassages().add(passage);
                                fragment = paragraph;
                            }

                            count3++;
                        } else if (bindingSet.getValue("type2").toString().equals(uriBase + "ontology/Case")) {
                            Case case1 = new Case();
                            case1.setId(count4 + 2);
                            case1.setURI(bindingSet.getValue("part").toString());
                            Passage passage = new Passage();
                            if (bindingSet.getValue("text").toString().contains("@html")) {
                                String text = bindingSet.getValue("text").toString().replace("@html", "");
                                passage.setText(CommonUtils.trimDoubleQuotes(text));
                            } else if (bindingSet.getValue("text").toString().contains("@el")) {
                                String text = bindingSet.getValue("text").toString().replace("@el", "");
                                passage.setText(CommonUtils.trimDoubleQuotes(text));
                            }

                            case1.getPassages().add(passage);
                            //System.out.println(case1.getURI());
                            //System.out.println("NEW CASE");

                            if (frag == 0) {
                                fragment = case1;
                                if (mod.getType().contains("Edit")) {
                                    fragment.setStatus(2);
                                } else {
                                    fragment.setStatus(1);
                                }
                                fragment.setType("Case");
                            } else if (frag == 1) {
                                Article article = (Article) fragment;
                                article.getParagraphs().get(count2).getCaseList().add(case1);
                                fragment = article;
                            } else if (frag == 2) {
                                Paragraph paragraph = (Paragraph) fragment;
                                paragraph.getCaseList().add(case1);
                                fragment = paragraph;
                            } else {
                                Paragraph paragraph = (Paragraph) fragment;
                                paragraph.getCaseList().add(case1);
                                fragment = paragraph;
                            }

                            count4++;
                        }
                    }
                    if (mod != null) {
                        mod.setFragment(fragment);
                        modifications.add(mod);
                    }
                } finally {
                    try {
                        result.close();
                    } catch (QueryEvaluationException qe) {
                        throw new NomothesiaException(qe);
                    }
                }
            } finally {
                try {
                    con.close();
                } catch (RepositoryException re) {
                    throw new NomothesiaException(re);
                }
            }

        } catch (OpenRDFException e) {
            throw new NomothesiaException(e);
        }

        /*for(int i =0; i< modifications.size(); i++) {

            System.out.println(modifications.get(i).getURI());
            System.out.println(modifications.get(i).getType());
            System.out.println(modifications.get(i).getPatient());

        }*/

        return modifications;
    }

    @Override
    public List<LegalDocument> search(Map<String, String> params) throws NomothesiaException {
        List<LegalDocument> legalDocumentL = new ArrayList<>();
        //Apache Lucene searching via criteria
        try {
            Path path = Paths.get("C:\\Users\\Panagiotis\\Documents\\IntellijProjects\\nomothesia-api-eli\\src\\main\\resources\\fek_index");
            Directory directory2 = FSDirectory.open(path);
            IndexReader indexReader = DirectoryReader.open(directory2);
            IndexSearcher indexSearcher = new IndexSearcher(indexReader);
            BooleanQuery finalQuery = new BooleanQuery();

            if (params.get("keywords") != null && !params.get("keywords").isEmpty()) {
                if (params.get("keywords").contains(",")) {
                    BooleanQuery multipleKeyWords = new BooleanQuery();
                    multipleKeyWords.setMinimumNumberShouldMatch(1);
                    String[] keywords = params.get("keywords").split(",");
                    for (String keyword : keywords) {
                        QueryParser queryParser = new QueryParser("text", new StandardAnalyzer());
                        Query query = queryParser.parse("\"" + keyword.trim() + "\"~");
                        multipleKeyWords.add(query, Occur.SHOULD);
                    }

                    finalQuery.add(multipleKeyWords, Occur.MUST);
                }
                if (params.get("keywords").contains(" ")) {
                    BooleanQuery multipleKeyWords = new BooleanQuery();
                    multipleKeyWords.setMinimumNumberShouldMatch(1);
                    String[] keywords = params.get("keywords").split("( )+");
                    for (String keyword : keywords) {
                        QueryParser queryParser = new QueryParser("text", new StandardAnalyzer());
                        Query query = queryParser.parse("\"" + keyword.trim() + "\"~");
                        multipleKeyWords.add(query, Occur.SHOULD);
                    }

                    finalQuery.add(multipleKeyWords, Occur.MUST);
                } else {
                    QueryParser queryParser = new QueryParser("text", new StandardAnalyzer());
                    Query query = queryParser.parse("\"" + params.get("keywords") + "\"~");
                    finalQuery.add(query, Occur.MUST);
                }
            }
            if (params.get("year") != null && !params.get("year").isEmpty()) {
                Query query2 = NumericRangeQuery.newIntRange("year", 1, Integer.parseInt(params.get("year")), Integer.parseInt(params.get("year")), true, true);
                //queryParser2.parse(params.get("year"));
                finalQuery.add(query2, Occur.MUST);
            }
            if (params.get("fek_year") != null && !params.get("fek_year").isEmpty()) {
                Query query3 = NumericRangeQuery.newIntRange("year", 1, Integer.parseInt(params.get("fek_year")), Integer.parseInt(params.get("fek_year")), true, true);
                //queryParser3.parse(params.get("fek_year"));
                finalQuery.add(query3, Occur.MUST);
            }
            if (params.get("fek_id") != null && !params.get("fek_id").isEmpty()) {
                Query query4 = NumericRangeQuery.newIntRange("fek_id", 1, Integer.parseInt(params.get("fek_id")), Integer.parseInt(params.get("fek_id")), true, true);
                //queryParser4.parse(params.get("fek_id"));
                finalQuery.add(query4, Occur.MUST);
            }
            if (params.get("type") != null && !params.get("type").isEmpty()) {
                if (params.get("type").contains(",")) {
                    BooleanQuery multipleTypes = new BooleanQuery();
                    multipleTypes.setMinimumNumberShouldMatch(1);
                    String[] types = params.get("type").split(",");
                    for (String type : types) {
                        multipleTypes.add(new TermQuery(new Term("type", type)), Occur.SHOULD);
                    }

                    finalQuery.add(multipleTypes, Occur.MUST);
                } else {
                    QueryParser queryParser5 = new QueryParser("type", new StandardAnalyzer());
                    Query query5 = queryParser5.parse(params.get("type"));
                    finalQuery.add(query5, Occur.MUST);
                }
            }
            if (params.get("id") != null && !params.get("id").isEmpty()) {
                Query query6 = NumericRangeQuery.newLongRange("numeric_id", 1, Long.parseLong(params.get("id")), Long.parseLong(params.get("id")), true, true);
                //queryParser6.parse(params.get("id"));
                finalQuery.add(query6, Occur.MUST);
            }
            if (params.get("date") != null && !params.get("date").isEmpty()) {
                Query query6 =
                        NumericRangeQuery.newIntRange("date", 1, Integer.parseInt(params.get("date").replace("-", "")), Integer.parseInt(params.get("date").replace("-", "")), true, true);
                //queryParser6.parse(params.get("id"));
                finalQuery.add(query6, Occur.MUST);
            }
            if (params.get("datefrom") != null && !params.get("datefrom").isEmpty()) {
                Query query6 = NumericRangeQuery.newIntRange("date", 1, Integer.parseInt(params.get("datefrom").replace("-", "")), Integer.parseInt(params.get("dateto").replace("-", "")), true, true);
                //queryParser6.parse(params.get("id"));
                finalQuery.add(query6, Occur.MUST);
            }

            TopDocs topDocs = indexSearcher.search(finalQuery, 300);
            if (topDocs.totalHits > 0) {
                for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
                    Document document = indexSearcher.doc(scoreDoc.doc);
                    LegalDocument legalDocument = new LegalDocument();
                    legalDocument.setURI(document.get("uri"));
                    legalDocument.setFEK(document.get("fek"));
                    legalDocument.setPublicationDate(
                            document.get("date").substring(0, 4) + "-" + document.get("date").substring(4, 6) + "-" +
                                    document.get("date").substring(6, 8));
                    legalDocument.setDecisionType(document.get("type"));
                    legalDocument.setYear(document.get("year"));
                    legalDocument.setId(document.get("id"));
                    String title = CommonUtils.trimDoubleQuotes(document.get("title"));
                    if (title.length() > 400) {
                        title = title.substring(0, 350) + "[...]";
                    }
                    legalDocument.setTitle(title);
                    legalDocumentL.add(legalDocument);
                }
            }
        } catch (IOException | ParseException e) {
            throw new NomothesiaException(e);
        }

        return legalDocumentL;
    }

    @Override
    public List<String> getTags() throws NomothesiaException {
        List<String> tags = new ArrayList<>();

        TupleQueryResult result;

        try {
            RepositoryConnection con = getSesameConnection();
            try {
                String getTagsQuery = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                        "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
                        "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n" +
                        "PREFIX owl: <http://www.w3.org/2002/07/owl#>\n" +
                        "PREFIX metalex:<http://www.metalex.eu/metalex/2008-05-02#>\n" +
                        "PREFIX leg: <" + uriBase + "ontology/>\n" +
                        "PREFIX dc: <http://purl.org/dc/terms/>\n" +
                        "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\n" +
                        "\n" +
                        "SELECT DISTINCT ?tag \n" +
                        "WHERE{\n" +
                        "?legaldocument leg:tag ?tag.\n" +
                        "}\n" +
                        "ORDER BY ?tag";

                //System.out.println(getTagsQuery);
                TupleQuery tupleQuery = con.prepareTupleQuery(QueryLanguage.SPARQL, getTagsQuery);
                result = tupleQuery.evaluate();

                try {
                    // iterate the result set
                    while (result.hasNext()) {
                        BindingSet bindingSet = result.next();
                        String tag = bindingSet.getValue("tag").toString().replace("@el", "");
                        tags.add(CommonUtils.trimDoubleQuotes(tag));
                    }

                } finally {
                    try {
                        result.close();
                    } catch (QueryEvaluationException qe) {
                        throw new NomothesiaException(qe);
                    }
                }
            } finally {
                try {
                    con.close();
                } catch (RepositoryException re) {
                    throw new NomothesiaException(re);
                }
            }
        } catch (OpenRDFException e) {
            throw new NomothesiaException(e);
        }

        return tags;

    }

    @Override
    public List<LegalDocument> getViewed() throws NomothesiaException {
        List<LegalDocument> legalviewed = new ArrayList<>();

        TupleQueryResult result;

        try {
            RepositoryConnection con = getSesameConnection();
            try {
                String getMostViewedQuery = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                        "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
                        "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n" +
                        "PREFIX owl: <http://www.w3.org/2002/07/owl#>\n" +
                        "PREFIX metalex:<http://www.metalex.eu/metalex/2008-05-02#>\n" +
                        "PREFIX leg: <" + uriBase + "ontology/>\n" +
                        "PREFIX dc: <http://purl.org/dc/terms/>\n" +
                        "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\n" +
                        "\n" +
                        "SELECT ?uri ?title ?date ?type ?views ?id\n" +
                        "WHERE{\n" +
                        "?uri dc:title ?title.\n" +
                        "?uri dc:created ?date.\n" +
                        "?uri leg:views ?views.\n" +
                        "?uri rdf:type ?type.\n" +
                        "?uri leg:legislationID ?id.\n" +
                        "FILTER(langMatches(lang(?title), \"el\"))\n" +
                        "}\n" +
                        "ORDER BY ASC(?views)\n" +
                        "LIMIT 6";

                //System.out.println(getMostViewedQuery);
                TupleQuery tupleQuery = con.prepareTupleQuery(QueryLanguage.SPARQL, getMostViewedQuery);
                result = tupleQuery.evaluate();

                try {
                    // iterate the result set
                    while (result.hasNext()) {
                        BindingSet bindingSet = result.next();
                        LegalDocument legalDocument = new LegalDocument();
                        //String[] URIs = bindingSet.getValue("uri").toString().split("uoa.gr/");
                        legalDocument.setURI(CommonUtils.trimDoubleQuotes(bindingSet.getValue("uri").toString()));
                        String title = "";
                        if (bindingSet.getValue("title") != null) {
                            title = bindingSet.getValue("title").toString().replace("@el", "");
                        } else {
                            title = legalDocument.getDecisionType() + "" + legalDocument.getYear() + " " + legalDocument.getId();
                        }
                        if (title.length() > 200) {
                            title = title.substring(0, 150) + "[...]\"";
                        }
                        legalDocument.setTitle(CommonUtils.trimDoubleQuotes(title));
                        String id = bindingSet.getValue("id").toString().replace("^^", "").replace("\"", "");
                        legalDocument.setId(CommonUtils.trimDoubleQuotes(id));
                        String date = bindingSet.getValue("date").toString().replace(
                                "^^<http://www.w3.org/2001/XMLSchema#date>", "");
                        date = CommonUtils.trimDoubleQuotes(date);
                        legalDocument.setPublicationDate(date);
                        String[] year = date.split("-");
                        legalDocument.setYear(year[0]);

                        String type = bindingSet.getValue("type").toString();
                        legalviewed.add(CommonUtils.decideLegalDocumentType(legalDocument, type));
                    }

                } finally {
                    try {
                        result.close();
                    } catch (QueryEvaluationException qe) {
                        throw new NomothesiaException(qe);
                    }
                }

            } finally {
                try {
                    con.close();
                } catch (RepositoryException re) {
                    throw new NomothesiaException(re);
                }
            }

        } catch (OpenRDFException e) {
            throw new NomothesiaException(e);
        }

        return legalviewed;
    }

    @Override
    public String getLegislationTypeByYear() throws NomothesiaException {
        List<LegalDocument> legalviewed = new ArrayList<>();

        TupleQueryResult result;

        try {
            RepositoryConnection con = getSesameConnection();
            try {
                String getTypeYearQuery = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                        "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
                        "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n" +
                        "PREFIX owl: <http://www.w3.org/2002/07/owl#>\n" +
                        "PREFIX metalex:<http://www.metalex.eu/metalex/2008-05-02#>\n" +
                        "PREFIX leg: <" + uriBase + "ontology/>\n" +
                        "PREFIX dc: <http://purl.org/dc/terms/>\n" +
                        "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\n" +
                        "\n" +
                        "SELECT ?uri ?date ?type\n" +
                        "WHERE{\n" +
                        "?uri dc:title ?title.\n" +
                        "?uri dc:created ?date.\n" +
                        "?uri rdf:type ?type.\n" +
                        "FILTER(langMatches(lang(?title), \"el\"))\n" +
                        "}\n" +
                        "ORDER BY DESC(?views)\n" +
                        "LIMIT 10";

                //System.out.println(getTypeYearQuery);
                TupleQuery tupleQuery = con.prepareTupleQuery(QueryLanguage.SPARQL, getTypeYearQuery);
                result = tupleQuery.evaluate();

                try {
                    // iterate the result set
                    while (result.hasNext()) {
                        BindingSet bindingSet = result.next();
                        // TODO implement
                    }
                } finally {
                    try {
                        result.close();
                    } catch (QueryEvaluationException qe) {
                        throw new NomothesiaException(qe);
                    }
                }
            } finally {
                try {
                    con.close();
                } catch (RepositoryException re) {
                    throw new NomothesiaException(re);
                }
            }
        } catch (OpenRDFException e) {
            throw new NomothesiaException(e);
        }

        return "";
    }

    @Override
    public List<LegalDocument> getRecent() throws NomothesiaException {
        List<LegalDocument> legalrecent = new ArrayList<>();

        TupleQueryResult result;

        try {
            RepositoryConnection con = getSesameConnection();
            try {
                String getMostRecentQuery = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                        "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
                        "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n" +
                        "PREFIX owl: <http://www.w3.org/2002/07/owl#>\n" +
                        "PREFIX metalex:<http://www.metalex.eu/metalex/2008-05-02#>\n" +
                        "PREFIX leg: <" + uriBase + "ontology/>\n" +
                        "PREFIX dc: <http://purl.org/dc/terms/>\n" +
                        "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\n" +
                        "\n" +
                        "SELECT ?uri ?title ?date ?type ?views ?id\n" +
                        "WHERE{\n" +
                        "?uri dc:title ?title.\n" +
                        "?uri dc:created ?date.\n" +
                        "?uri leg:views ?views.\n" +
                        "?uri rdf:type ?type.\n" +
                        "?uri leg:legislationID ?id.\n" +
                        "FILTER(langMatches(lang(?title), \"el\"))\n" +
                        "}\n" +
                        "ORDER BY DESC(?date)\n" +
                        "LIMIT 10";

                //System.out.println(getMostRecentQuery);
                TupleQuery tupleQuery = con.prepareTupleQuery(QueryLanguage.SPARQL, getMostRecentQuery);
                result = tupleQuery.evaluate();

                try {
                    // iterate the result set
                    while (result.hasNext()) {
                        BindingSet bindingSet = result.next();
                        LegalDocument legalDocument = new LegalDocument();
                        //String[] URIs = bindingSet.getValue("uri").toString().split("uoa.gr/");
                        legalDocument.setURI(CommonUtils.trimDoubleQuotes(bindingSet.getValue("uri").toString()));
                        String title = "";
                        if (bindingSet.getValue("title") != null) {
                            title = bindingSet.getValue("title").toString().replace("@el", "");
                        } else {
                            title = legalDocument.getDecisionType() + "" + legalDocument.getYear() + " " + legalDocument.getId();
                        }
                        if (title.length() > 200) {
                            title = title.substring(0, 150) + "[...]\"";
                        }
                        legalDocument.setTitle(CommonUtils.trimDoubleQuotes(title));
                        String id = bindingSet.getValue("id").toString().replace("^^", "").replace("\"", "");
                        legalDocument.setId(CommonUtils.trimDoubleQuotes(id));
                        String date = bindingSet.getValue("date").toString().replace(
                                "^^<http://www.w3.org/2001/XMLSchema#date>", "");
                        date = CommonUtils.trimDoubleQuotes(date);
                        legalDocument.setPublicationDate(date);
                        String[] year = date.split("-");
                        legalDocument.setYear(year[0]);

                        String type = bindingSet.getValue("type").toString();
                        legalrecent.add(CommonUtils.decideLegalDocumentType(legalDocument, type));
                    }

                } finally {
                    try {
                        result.close();
                    } catch (QueryEvaluationException qe) {
                        throw new NomothesiaException(qe);
                    }
                }

            } finally {
                try {
                    con.close();
                } catch (RepositoryException re) {
                    throw new NomothesiaException(re);
                }
            }

        } catch (OpenRDFException e) {
            throw new NomothesiaException(e);
        }

        return legalrecent;
    }

    @Override
    public List<GovernmentGazette> getFEKStatistics() throws NomothesiaException {
        List<GovernmentGazette> governmentGazetteL = new ArrayList<>();
        String uri = "";

        TupleQueryResult result;

        try {
            RepositoryConnection con = getSesameConnection();
            try {
                String getFekQuery = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                        "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
                        "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n" +
                        "PREFIX owl: <http://www.w3.org/2002/07/owl#>\n" +
                        "PREFIX metalex:<http://www.metalex.eu/metalex/2008-05-02#>\n" +
                        "PREFIX leg: <" + uriBase + "ontology/>\n" +
                        "PREFIX dc: <http://purl.org/dc/terms/>\n" +
                        "SELECT ?gaz ?title ?pdf ?doc ?type ?date (COUNT (?doc) AS ?docs) (COUNT (?part) AS ?issues)\n" +
                        "WHERE{\n" +
                        "?gaz rdf:type leg:GovernmentGazette.\n" +
                        "?gaz dc:title ?title.\n" +
                        "?gaz dc:created ?date.\n" +
                        "?gaz leg:pdfFile ?pdf.\n" +
                        "    OPTIONAL {?doc leg:gazette ?gaz. ?doc rdf:type ?type.}\n" +
                        "    OPTIONAL{?doc2 leg:gazette ?gaz. ?doc2 rdf:type ?type. ?doc2 metalex:part+ ?part. ?part rdf:type leg:ParsingIssue.}\n" +
                        "}\n" +
                        "GROUP BY ?gaz ?title ?pdf ?doc ?type ?date\n" +
                        "ORDER BY ?gaz";

                //System.out.println(getFekQuery);
                TupleQuery tupleQuery = con.prepareTupleQuery(QueryLanguage.SPARQL, getFekQuery);
                result = tupleQuery.evaluate();
                GovernmentGazette governmentGazette = new GovernmentGazette();
                ArrayList<LegalDocument> legalDocumentL = new ArrayList<>();

                try {
                    // iterate the result set
                    while (result.hasNext()) {
                        BindingSet bindingSet = result.next();
                        if (!uri.equals(bindingSet.getValue("gaz").toString())) {
                            governmentGazette.setList(legalDocumentL);
                            governmentGazetteL.add(governmentGazette);
                            governmentGazette = new GovernmentGazette();
                            legalDocumentL = new ArrayList<>();
                            governmentGazette.setURI(bindingSet.getValue("gaz").toString());
                            //gaz.setId(CommonUtils.trimDoubleQuotes(bindingSet.getValue("title").toString().split("^^")[0]).split("\\/")[2]);
                            uri = governmentGazette.getURI();
                            String title = bindingSet.getValue("title").toString().replace("^^", "");
                            governmentGazette.setTitle(CommonUtils.trimDoubleQuotes(title));
                            String pdf = bindingSet.getValue("pdf").toString().replace("^^", "");
                            governmentGazette.setFileName(CommonUtils.trimDoubleQuotes(pdf));
                            String date = bindingSet.getValue("date").toString().replace(
                                    "^^<http://www.w3.org/2001/XMLSchema#date>", "");
                            governmentGazette.setPublicationDate(CommonUtils.trimDoubleQuotes(date));
                            String docs = bindingSet.getValue("docs").toString().replace(
                                    "^^<http://www.w3.org/2001/XMLSchema#integer>", "");
                            if (Integer.parseInt(CommonUtils.trimDoubleQuotes(docs)) != 0) {
                                governmentGazette.setDocs(1);
                                //Integer.parseInt(CommonUtils.trimDoubleQuotes(docs)));
                            }
                            String issues = bindingSet.getValue("issues").toString().replace(
                                    "^^<http://www.w3.org/2001/XMLSchema#integer>", "");
                            governmentGazette.setIssues(Integer.parseInt(CommonUtils.trimDoubleQuotes(issues)));
                            if (governmentGazette.getDocs() != 0) {
                                String type = bindingSet.getValue("type").toString();
                                LegalDocument legalDocument = CommonUtils.decideLegalDocumentType(new LegalDocument()
                                        , type);
                                legalDocument.setURI(bindingSet.getValue("doc").toString());
                                legalDocument.setId(legalDocument.getURI().split("gr\\/")[1].split("\\/", 2)[1]);
                                legalDocumentL.add(legalDocument);
                            }
                        } else {
                            String type = bindingSet.getValue("type").toString();
                            LegalDocument legalDocument = CommonUtils.decideLegalDocumentType(new LegalDocument()
                                    , type);
                            governmentGazette.setDocs(governmentGazette.getDocs() + 1);
                            String issues = bindingSet.getValue("issues").toString().replace(
                                    "^^<http://www.w3.org/2001/XMLSchema#integer>", "");
                            governmentGazette.setIssues(governmentGazette.getIssues() + Integer.parseInt(CommonUtils.trimDoubleQuotes(issues)));
                            legalDocument.setURI(bindingSet.getValue("doc").toString());
                            legalDocument.setId(legalDocument.getURI().split("gr\\/")[1].split("\\/", 2)[1]);
                            legalDocumentL.add(legalDocument);
                        }
                    }
                } finally {
                    try {
                        result.close();
                    } catch (QueryEvaluationException qe) {
                        throw new NomothesiaException(qe);
                    }
                }
            } finally {
                try {
                    con.close();
                } catch (RepositoryException re) {
                    throw new NomothesiaException(re);
                }
            }
        } catch (OpenRDFException e) {
            throw new NomothesiaException(e);
        }

        governmentGazetteL.remove(0);
        //Collections.sort(gazs, new GGComparator());

        return governmentGazetteL;
    }

    @Override
    public List<ArrayList<String>> getStatistics() throws NomothesiaException {
        List<ArrayList<String>> statistics = new ArrayList<>();
        List<LegislationTypesEnum> types = Arrays.asList(LegislationTypesEnum.values());
        List<YearsEnum> yenum = Arrays.asList(YearsEnum.values());

        TupleQueryResult result;

        try {
            RepositoryConnection con = getSesameConnection();
            try {
                for (LegislationTypesEnum type : types) {
                    String getTypeQuery = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                            "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
                            "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n" +
                            "PREFIX owl: <http://www.w3.org/2002/07/owl#>\n" +
                            "PREFIX leg: <" + uriBase + "ontology/>\n" +
                            "PREFIX dc: <http://purl.org/dc/terms/>\n" +
                            "SELECT (COUNT (?doc) AS ?sum) ?year\n" +
                            "WHERE{\n" +
                            "?doc rdf:type leg:" + type.getType() + ".\n" +
                            "?doc dc:created ?date.\n" +
                            "BIND (year(?date) AS ?year).\n" +
                            "}\n" +
                            "GROUP BY ?year \n" +
                            "ORDER BY ASC(?year)";

                    //System.out.println(getTypeQuery);
                    TupleQuery tupleQuery = con.prepareTupleQuery(QueryLanguage.SPARQL, getTypeQuery);
                    result = tupleQuery.evaluate();

                    try {
                        ArrayList<String> typed = new ArrayList<>();
                        // iterate the result set
                        int yearIndex = 0;
                        while (result.hasNext()) {
                            BindingSet bindingSet = result.next();
                            if (bindingSet.getValue("year") != null) {
                                while (!CommonUtils.trimDoubleQuotes(bindingSet.getValue("year").toString().replace(
                                        "^^<http://www.w3.org/2001/XMLSchema#integer>", "")).equals(yenum.get(yearIndex).getYear())) {
                                    typed.add("0");
                                    if (yearIndex < yenum.size() - 1) {
                                        yearIndex++;
                                    } else {
                                        break;
                                    }
                                }
                                typed.add(CommonUtils.trimDoubleQuotes(bindingSet.getValue("sum").toString().replace(
                                        "^^<http://www.w3.org/2001/XMLSchema#integer>", "")));
                                yearIndex++;
                            }
                        }
                        statistics.add(typed);
                    } finally {
                        try {
                            result.close();
                        } catch (QueryEvaluationException qe) {
                            throw new NomothesiaException(qe);
                        }
                    }
                }
            } finally {
                try {
                    con.close();
                } catch (RepositoryException re) {
                    throw new NomothesiaException(re);
                }
            }
        } catch (OpenRDFException e) {
            throw new NomothesiaException(e);
        }

        ArrayList<String> all = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            all.add("0");
        }

        //lists.remove(0);

        for (int i = 0; i < statistics.size(); i++) {
            for (int j = 0; j < statistics.get(i).size(); j++) {
                int sum = Integer.parseInt(all.get(j)) + Integer.parseInt(statistics.get(i).get(j));
                all.set(j, Integer.toString(sum));
            }
        }

        statistics.add(0, all);

        //Collections.sort(gazs, new GGComparator());
        return statistics;
    }

    //    // HTTP GET request
    //    private String getKML(String place) {
    //
    //            EndpointResult result = null;
    //            Properties props = new Properties();
    //            InputStream fis = null;
    //            String host = "";
    //            int port = 0;
    //            String appName = "";
    //            try {
    //
    //                fis = getClass().getResourceAsStream("/nomothesia.properties");
    //                props.load(fis);
    //
    //                // get the properties values
    //                host = props.getProperty("StrabonHost");
    //                port = Integer.parseInt(props.getProperty("StrabonPort"));
    //                appName = props.getProperty("StrabonAppName");
    //            } catch (IOException e) {
    //                e.printStackTrace();
    //            }
    //            String KML = "";
    //            String query = "SELECT ?geo WHERE {<"+place+"> ?hasgeometry ?geo. ?hasgeometry <http://www.w3.org/2000/01/rdf-schema#label> \"has_geometry\"@en.}";
    //
    //            SPARQLEndpoint endpoint = new SPARQLEndpoint(host, port, appName);
    //
    //            try {
    //
    //                result = endpoint.query(query, stSPARQLQueryResultFormat.KML);
    //
    //                System.out.println("Status code: " + result.getStatusCode());
    //                System.out.println("Query: " + query);
    //                System.out.println("Status text: " + result.getStatusText());
    //                KML = result.getResponse().replace("<?xml version='1.0' encoding='UTF-8'?>", "");
    //                KML = KML.replaceAll("\n", "");
    //                KML = KML.replaceAll("\r", "");
    //                System.out.println("<----- Result ----->");
    //                System.out.println(KML);
    //                System.out.println("<----- Result ----->");
    //
    //            } catch (IOException e) {
    //                e.printStackTrace();
    //            }
    //
    //            return KML;
    //
    //    }

    private RepositoryConnection getSesameConnection() throws NomothesiaException {
        // Connect to Sesame
        try {
            Repository repo = new HTTPRepository(applicationProperties.getSesameServer(), applicationProperties.getSesameRepositoryID());
            repo.initialize();
            return repo.getConnection();
        } catch (RepositoryException ex) {
            throw new NomothesiaException(ex);
        }
    }

}
