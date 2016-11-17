package com.di.nomothesia.enums;

/**
 * Created by psour on 14/11/2016.
 */
public enum QueriesEnum {
    QUERY_1("PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                    "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
                    "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n" +
                    "PREFIX owl: <http://www.w3.org/2002/07/owl#>\n" +
                    "PREFIX metalex:<http://www.metalex.eu/metalex/2008-05-02#>\n" +
                    "PREFIX nomothesia: <http://legislation.di.uoa.gr/ontology/>\n" +
                    "PREFIX dc: <http://purl.org/dc/terms/>\n" +
                    "\n" +
                    "SELECT ?modification ?type ?version ?competenceground \n" +
                    "WHERE{\n" +
                    "  ?version metalex:matterOf ?modification."+
                    "  ?modification metalex:legislativeCompetenceGround ?competenceground.\n" +
                    "  ?modification rdf:type ?type.\n" +
                    "  <http://legislation.di.uoa.gr/law/2013/4174> metalex:realizedBy ?version.\n"
                    + "}"),

    QUERY_2("PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                    "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
                    "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n" +
                    "PREFIX owl: <http://www.w3.org/2002/07/owl#>\n" +
                    "PREFIX metalex:<http://www.metalex.eu/metalex/2008-05-02#>\n" +
                    "PREFIX nomothesia: <http://legislation.di.uoa.gr/ontology/>\n" +
                    "PREFIX dc: <http://purl.org/dc/terms/>\n" +
                    "\n" +
                    "SELECT ?part ?type \n" +
                    "WHERE{\n" +
                    " <http://legislation.di.uoa.gr/pd/2014/65> metalex:part+  ?part.\n" +
                    " ?part rdf:type ?type.\n" +
                    "}" +
                    "ORDER BY ?part"),

    QUERY_3("PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                    "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
                    "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n" +
                    "PREFIX owl: <http://www.w3.org/2002/07/owl#>\n" +
                    "PREFIX metalex:<http://www.metalex.eu/metalex/2008-05-02#>\n" +
                    "PREFIX nomothesia: <http://legislation.di.uoa.gr/ontology/>\n" +
                    "PREFIX dc: <http://purl.org/dc/terms/>\n" +
                    "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\n" +
                    "\n" +
                    "SELECT DISTINCT ?name (COUNT(?decision) AS ?decisions)\n" +
                    "WHERE{\n" +
                    " ?decision nomothesia:signer  ?signer.\n" +
                    " ?signer foaf:name ?name.\n" +
                    "}" +
                    "GROUP BY ?name\n" +
                    "ORDER BY DESC(?decisions)"),

    QUERY_4("PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n" +
                    "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n" +
                    "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> \n" +
                    "PREFIX owl: <http://www.w3.org/2002/07/owl#> \n" +
                    "PREFIX metalex:<http://www.metalex.eu/metalex/2008-05-02#> \n" +
                    "PREFIX nomothesia: <http://legislation.di.uoa.gr/ontology/> \n" +
                    "PREFIX dc: <http://purl.org/dc/terms/>\n" +
                    "\n" +
                    "SELECT  ?legaldocumentURI ?title (COUNT(DISTINCT ?version) AS ?versions)\n" +
                    "WHERE{\n" +
                    "?legaldocumentURI metalex:realizedBy ?version.\n" +
                    "?legaldocumentURI dc:title ?title.\n" +
                    "?modification metalex:legislativeCompetenceGround ?work.\n" +
                    "?work dc:created ?date.\n" +
                    "FILTER (?date >= \"2008-01-01\"^^xsd:date && ?date <= \"2013-01-31\"^^xsd:date)\n" +
                    "} \n" +
                    "GROUP BY ?legaldocumentURI ?title\n" +
                    "ORDER BY DESC(?versions)\n" +
                    "LIMIT 10");

    private String query;

    QueriesEnum(String query) {
        this.query = query;
    }

    public String getQuery() {
        return this.query;
    }
}
