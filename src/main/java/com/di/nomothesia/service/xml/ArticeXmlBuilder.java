package com.di.nomothesia.service.xml;

import com.di.nomothesia.NomothesiaException;
import com.di.nomothesia.model.*;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringWriter;
import java.io.Writer;

/**
 * Created by psour on 9/12/2016.
 *
 */
@Service
public class ArticeXmlBuilder implements XmlBuilder<LegalDocument, String> {

    private static final String XML_NS = "http://dublincore.org/documentuments/dcmi-terms/";

    @Override
    public String buildXml(LegalDocument legalDocument) throws NomothesiaException {
        String xml = "";

        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            docFactory.setNamespaceAware(true);
            Document document = docBuilder.newDocument();

            //Root Element
            Element rootElement = document.createElement("LegalDocument");
            rootElement.setAttribute("id", legalDocument.getId());
            rootElement.setAttribute("documentURI", legalDocument.getURI());
            document.appendChild(rootElement);

            //Metadata Branch
            Element metadata = document.createElement("Metadata");
            rootElement.appendChild(metadata);

            Element title = document.createElementNS(XML_NS,"dc:title");
            title.setTextContent(legalDocument.getTitle());
            metadata.appendChild(title);

            Element year = document.createElement("year");
            year.setTextContent(legalDocument.getYear());
            metadata.appendChild(year);

            Element fek = document.createElement("FEK");
            fek.setTextContent(legalDocument.getFEK());
            metadata.appendChild(fek);

            Element views = document.createElement("views");
            views.setTextContent(legalDocument.getViews());
            metadata.appendChild(views);

            Element type = document.createElement("decisiontype");
            type.setTextContent(legalDocument.getDecisionType());
            metadata.appendChild(type);

            Element pdate = document.createElementNS(XML_NS,"dc:created");
            pdate.setTextContent(legalDocument.getPublicationDate());
            metadata.appendChild(pdate);

            Element  dctype = document.createElementNS(XML_NS,"dc:type");
            dctype.setTextContent("text");
            metadata.appendChild(dctype);

            Element  dcformat = document.createElementNS(XML_NS,"dc:format");
            dcformat.setTextContent("text/xml");
            metadata.appendChild(dcformat);

            Element  dclang = document.createElementNS(XML_NS,"dc:language");
            dclang.setTextContent("gr");
            metadata.appendChild(dclang);

            Element  dcident = document.createElementNS(XML_NS,"dc:identifier");
            dcident.setTextContent(legalDocument.getURI());
            metadata.appendChild(dcident);

            //Tags Branch
            if (legalDocument.getTags().size()>1) {
                Element tags = document.createElement("Tags");
                metadata.appendChild(tags);
                //for every tag
                for (int i=0;i<legalDocument.getTags().size();i++) {
                    Element tag = document.createElement("Tag");
                    tag.setAttribute("id", "" + (i+1));
                    tag.setTextContent(legalDocument.getTags().get(i));
                    tags.appendChild(tag);
                }
            }

            //Signers Branch
            Element signers = document.createElement("ListOfSigners");
            rootElement.appendChild(signers);

            for (Signer signer : legalDocument.getSigners()) {
                //signers
                Element signerE = document.createElement("Signer");
                signers.appendChild(signerE);
                //signers title
                Element signtitle = document.createElementNS("http://xmlns.com/foaf/spec/","foaf:title");
                signtitle.setTextContent(signer.getTitle());
                signerE.appendChild(signtitle);
                //signers fullname
                Element signname = document.createElementNS("http://xmlns.com/foaf/spec/","foaf:name");
                signname.setTextContent(signer.getFullName());
                signerE.appendChild(signname);
            }

            //Citation Branch
            if (legalDocument.getCitations() != null){
                Element introduction = document.createElement("Introduction");
                rootElement.appendChild(introduction);

                Element citations = document.createElement("Citations");
                introduction.appendChild(citations);

                //for every ciation
                for (Citation citation : legalDocument.getCitations()) {
                    Element citationE = document.createElement("Citation");
                    citationE.setAttribute("id", "" + citation.getId());
                    citationE.setAttribute("documentURI", citation.getURI());
                    Element citText = document.createElement("text");
                    citText.setTextContent(citation.getDescription());
                    citationE.appendChild(citText);
                    //if citaiton cites target
                    if(!citation.gettargetURIs().isEmpty()) {
                        Element cites = document.createElement("Cites");
                        citationE.appendChild(cites);
                        //for every targetURI
                        for (String target: citation.gettargetURIs()) {
                            Element targetE = document.createElement("TargetURI");
                            targetE.setTextContent(target);
                            cites.appendChild(targetE);
                        }
                    }
                    citations.appendChild(citationE);
                }
            }

            //Body Branch
            //NOT IMPLEMENTED YET
            /*Element body = doc.createElement("Body");
            rootElement.appendChild(body);

            Element book = doc.createElement("Book");
            body.appendChild(book);

            Element section = doc.createElement("Section");
            book.appendChild(section);

            Element chapter = doc.createElement("Chapter");
            section.appendChild(chapter);

            Element part = doc.createElement("Part");
            chapter.appendChild(part);*/

            //Article Branch (inside body when body is completed)
            for (Article article : legalDocument.getArticles()) {
                //article
                Element articleE = document.createElement("Article");
                //article.setAttribute("number","" + legald.getArticles().get(i));
                articleE.setAttribute("id", "" + article.getId());
                articleE.setAttribute("documentURI", article.getURI());
                rootElement.appendChild(articleE);

                //article title
                if (article.getTitle() != null) {
                    Element artTitle = document.createElement("Title");
                    artTitle.setTextContent(article.getTitle());
                    articleE.appendChild(artTitle);
                }

                //article id
                //Element artId = doc.createElement("Id");
                //artId.setTextContent("" + legald.getArticles().get(i).getId());
                //article.appendChild(artId);

                for (Paragraph paragraph : article.getParagraphs()) {
                    //paragraph
                    Element paragraphE = document.createElement("Paragraph");
                    paragraphE.setAttribute("id", "" + paragraph.getId());
                    paragraphE.setAttribute("documentURI", paragraph.getURI());
                    //paragraph.setAttribute("number","" + legald.getArticles().get(i).getParagraphs().get(j));

                    //paragraph id
                    //Element parId = doc.createElement("Id");
                    //parId.setTextContent("" + legald.getArticles().get(i).getParagraphs().get(j).getId());
                    //paragraph.appendChild(parId);

                    //NOT IMPLEMENTED YET
                    //Element parTable = doc.createElement("Table");
                    //parTable.setTextContent(legald.getArticles().get(i).getParagraphs().get(j).getTable());
                    //paragraph.appendChild(parTable);

                    Element list = document.createElement("List");

                    for(Case caseL : paragraph.getCaseList()) {
                        //paragraph case list
                        paragraphE.appendChild(list);
                        Element pcase = document.createElement("Case");
                        pcase.setAttribute("id", "" + caseL.getId());
                        pcase.setAttribute("documentURI", caseL.getURI());
                        list.appendChild(pcase);

                        //case id
                        //Element casId = doc.createElement("Id");
                        //casId.setTextContent("" + legald.getArticles().get(i).getParagraphs().get(j).getCaseList().get(n).getId());
                        //pcase.appendChild(casId);

                        //case passages
                        for (Passage passage :caseL.getPassages()) {
                            //case passages
                            Element cpassage = document.createElement("Passage");
                            cpassage.setAttribute("id", "" + passage.getId());
                            cpassage.setAttribute("documentURI", passage.getURI());

                            //case passage id
                            //Element caseId = doc.createElement("Id");
                            //caseId.setTextContent("" + legald.getArticles().get(i).getParagraphs().get(j).getCaseList().get(n).getPassages().get(m).getId());
                            //cpassage.appendChild(caseId);

                            //case passage text
                            Element casText = document.createElement("text");
                            casText.setTextContent("" + passage.getText());
                            cpassage.appendChild(casText);

                            pcase.appendChild(cpassage);
                        }

                        //case in case
                        if (caseL.getCaseList() != null) {
                            Element list2 = document.createElement("List");

                            for(Case caseR : caseL.getCaseList()) {
                                //paragraph case list
                                pcase.appendChild(list2);
                                Element pcase2 = document.createElement("Case");
                                pcase2.setAttribute("id", "" + caseR.getId());
                                pcase2.setAttribute("documentURI", caseR.getURI());
                                list2.appendChild(pcase2);

                                //case passages
                                for (Passage passageR : caseR.getPassages()) {
                                    //case passages
                                    Element cpassage2 = document.createElement("Passage");
                                    cpassage2.setAttribute("id", "" + passageR.getId());
                                    cpassage2.setAttribute("documentURI", passageR.getURI());
                                    //case passage text
                                    Element casText2 = document.createElement("text");
                                    casText2.setTextContent("" + passageR.getText());
                                    cpassage2.appendChild(casText2);

                                    pcase2.appendChild(cpassage2);
                                }
                            }
                        }

                        if(caseL.getModification() != null) {
                            //modification
                            Element modification = document.createElement("Modification");
                            modification.setAttribute("documentURI", caseL.getModification().getURI());
                            paragraphE.appendChild(modification);

                            //modification type
                            //Element modType = doc.createElement("type");
                            //modType.setTextContent(legald.getArticles().get(i).getParagraphs().get(j).getModification().getType());
                            //modification.appendChild(modType);

                            if(caseL.getType().equals("Article")) {
                                Article a = (Article) caseL.getModification().getFragment();
                                Element paragraphin = document.createElement("Article");
                                paragraphin.setAttribute("id", "" + a.getId());
                                paragraphin.setAttribute("documentURI", a.getURI());
                                modification.appendChild(paragraphin);

                                for (Paragraph par : a.getParagraphs()) {
                                    paragraphin = document.createElement("Paragraph");
                                    paragraphin.setAttribute("id", "" + par.getId());
                                    paragraphin.setAttribute("documentURI", par.getURI());
                                    modification.appendChild(paragraphin);

                                    for (Passage pass: par.getPassages()) {
                                        Element passagein = document.createElement("Passage");
                                        passagein.setAttribute("id", "" + pass.getId());
                                        passagein.setAttribute("documentURI", pass.getURI());
                                        Element pasTextin = document.createElement("text");
                                        pasTextin.setTextContent(pass.getText());
                                        passagein.appendChild(pasTextin);

                                        paragraphin.appendChild(passagein);
                                    }

                                    for (Case casL : par.getCaseList()) {
                                        for (Passage pass2: casL.getPassages()) {
                                            Element passagein2 = document.createElement("Passage");
                                            passagein2.setAttribute("id", "" + pass2.getId());
                                            passagein2.setAttribute("documentURI", pass2.getURI());
                                            Element pasTextin2 = document.createElement("text");
                                            pasTextin2.setTextContent(pass2.getText());
                                            passagein2.appendChild(pasTextin2);

                                            paragraphin.appendChild(passagein2);
                                        }
                                    }
                                }

                                //paragraph.appendChild(modification);
                            } else if(caseL.getModification().getType().equals("Paragraph")) {
                                Paragraph p = (Paragraph) caseL.getModification().getFragment();
                                Element paragraphin = document.createElement("Paragraph");
                                paragraphin.setAttribute("id", "" + p.getId());
                                paragraphin.setAttribute("documentURI", p.getURI());
                                modification.appendChild(paragraphin);

                                for (Passage pass: p.getPassages()) {
                                    Element passagein = document.createElement("Passage");
                                    passagein.setAttribute("id", "" + pass.getId());
                                    passagein.setAttribute("documentURI", pass.getURI());
                                    Element pasTextin = document.createElement("text");
                                    pasTextin.setTextContent(pass.getText());
                                    passagein.appendChild(pasTextin);

                                    paragraphin.appendChild(passagein);
                                }

                                for (Case case2 : p.getCaseList()) {
                                    for (Passage pass2 : case2.getPassages()) {
                                        Element passagein2 = document.createElement("Passage");
                                        passagein2.setAttribute("id", "" + pass2.getId());
                                        passagein2.setAttribute("documentURI", pass2.getURI());
                                        Element pasTextin2 = document.createElement("text");
                                        pasTextin2.setTextContent(pass2.getText());
                                        passagein2.appendChild(pasTextin2);

                                        paragraphin.appendChild(passagein2);
                                    }
                                }

                                //paragraph.appendChild(modification);
                            } else if(caseL.getModification().getType().equals("Case")) {
                                Case c = (Case) caseL.getModification().getFragment();
                                Element paragraphin = document.createElement("Case");
                                paragraphin.setAttribute("id", "" + c.getId());
                                paragraphin.setAttribute("documentURI", c.getURI());
                                modification.appendChild(paragraphin);

                                for (Passage pass : c.getPassages()) {
                                    Element passage = document.createElement("Passage");
                                    passage.setAttribute("id", "" + pass.getId());
                                    passage.setAttribute("documentURI", pass.getURI());
                                    Element pasText = document.createElement("text");
                                    pasText.setTextContent(pass.getText());
                                    passage.appendChild(pasText);

                                    paragraphin.appendChild(passage);
                                }

                                //paragraph.appendChild(modification);
                            } else if(caseL.getModification().getType().equals("Passage")) {
                                Passage pas = (Passage) caseL.getModification().getFragment();
                                Element passage = document.createElement("Passage");
                                passage.setAttribute("id", "" + pas.getId());
                                passage.setAttribute("documentURI", pas.getURI());
                                Element pasText = document.createElement("text");
                                pasText.setTextContent(pas.getText());
                                passage.appendChild(pasText);

                                modification.appendChild(passage);
                            }
                        }
                    }

                    for (Passage passPar: paragraph.getPassages()) {
                        //paragraph passages
                        Element passage = document.createElement("Passage");
                        passage.setAttribute("id", "" + passPar.getId());
                        passage.setAttribute("documentURI", passPar.getURI());

                        //passage id
                        //Element pasId = doc.createElement("Id");
                        //pasId.setTextContent("" + legald.getArticles().get(i).getParagraphs().get(j).getPassages().get(k).getId());
                        //passage.appendChild(pasId);

                        //passage text
                        Element pasText = document.createElement("text");
                        pasText.setTextContent("" + passPar.getText());
                        passage.appendChild(pasText);

                        paragraphE.appendChild(passage);

                        if(passPar.getModification() != null) {
                            //modification
                            Element modification = document.createElement("Modification");
                            modification.setAttribute("documentURI", passPar.getModification().getURI());
                            paragraphE.appendChild(modification);

                            //modification type
                            //Element modType = doc.createElement("type");
                            //modType.setTextContent(legald.getArticles().get(i).getParagraphs().get(j).getModification().getType());
                            //modification.appendChild(modType);

                            if(passPar.getModification().getType().equals("Article")) {
                                Article a = (Article) passPar.getModification().getFragment();
                                Element paragraphin = document.createElement("Article");
                                paragraphin.setAttribute("id", "" + a.getId());
                                paragraphin.setAttribute("documentURI", a.getURI());
                                modification.appendChild(paragraphin);

                                for (Paragraph par : a.getParagraphs()) {
                                    paragraphin = document.createElement("Paragraph");
                                    paragraphin.setAttribute("id", "" + par.getId());
                                    paragraphin.setAttribute("documentURI", par.getURI());
                                    modification.appendChild(paragraphin);

                                    for (Passage pass : par.getPassages()) {
                                        Element passagein = document.createElement("Passage");
                                        passagein.setAttribute("id", "" + pass.getId());
                                        passagein.setAttribute("documentURI",pass.getURI());
                                        Element pasTextin = document.createElement("text");
                                        pasTextin.setTextContent(pass.getText());
                                        passagein.appendChild(pasTextin);

                                        paragraphin.appendChild(passagein);
                                    }

                                    for (Case case3 : paragraph.getCaseList()) {
                                        for (Passage pass3 : case3.getPassages()) {
                                            Element passagein2 = document.createElement("Passage");
                                            passagein2.setAttribute("id", "" + pass3.getId());
                                            passagein2.setAttribute("documentURI", pass3.getURI());
                                            Element pasTextin2 = document.createElement("text");
                                            pasTextin2.setTextContent(pass3.getText());
                                            passagein2.appendChild(pasTextin2);

                                            paragraphin.appendChild(passagein2);
                                        }
                                    }
                                }

                                //paragraph.appendChild(modification);
                            } else if(passPar.getModification().getType().equals("Paragraph")) {
                                Paragraph p = (Paragraph) passPar.getModification().getFragment();
                                Element paragraphin = document.createElement("Paragraph");
                                paragraphin.setAttribute("id", "" + p.getId());
                                paragraphin.setAttribute("documentURI", p.getURI());
                                modification.appendChild(paragraphin);

                                for (Passage pass: p.getPassages()) {
                                    Element passagein = document.createElement("Passage");
                                    passagein.setAttribute("id", "" + pass.getId());
                                    passagein.setAttribute("documentURI", pass.getURI());
                                    Element pasTextin = document.createElement("text");
                                    pasTextin.setTextContent(pass.getText());
                                    passagein.appendChild(pasTextin);

                                    paragraphin.appendChild(passagein);
                                }

                                for (Case caseL: p.getCaseList()) {
                                    for (Passage pass2: caseL.getPassages()) {
                                        Element passagein2 = document.createElement("Passage");
                                        passagein2.setAttribute("id", "" + pass2.getId());
                                        passagein2.setAttribute("documentURI", pass2.getURI());
                                        Element pasTextin2 = document.createElement("text");
                                        pasTextin2.setTextContent(pass2.getText());
                                        passagein2.appendChild(pasTextin2);

                                        paragraphin.appendChild(passagein2);
                                    }
                                }

                                //paragraph.appendChild(modification);
                            } else if(passPar.getModification().getType().equals("Case")) {
                                Case c = (Case) passPar.getModification().getFragment();
                                Element paragraphin = document.createElement("Case");
                                paragraphin.setAttribute("id", "" + c.getId());
                                paragraphin.setAttribute("documentURI", c.getURI());
                                modification.appendChild(paragraphin);

                                for (Passage pass: c.getPassages()) {
                                    passage = document.createElement("Passage");
                                    passage.setAttribute("id", "" + pass.getId());
                                    passage.setAttribute("documentURI",pass.getURI());
                                    pasText = document.createElement("text");
                                    pasText.setTextContent(pass.getText());
                                    passage.appendChild(pasText);

                                    paragraphin.appendChild(passage);
                                }

                                //paragraph.appendChild(modification);
                            } else if(passPar.getModification().getType().equals("Passage")) {
                                Passage pas = (Passage) passPar.getModification().getFragment();
                                passage = document.createElement("Passage");
                                passage.setAttribute("id", "" + pas.getId());
                                passage.setAttribute("documentURI", pas.getURI());
                                pasText = document.createElement("text");
                                pasText.setTextContent(pas.getText());
                                passage.appendChild(pasText);

                                modification.appendChild(passage);
                            }
                        }
                    }

                    articleE.appendChild(paragraphE);
                }
            }

            //Convert Document DOM type to String with greek characters support
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.ENCODING, "ISO-8859-1");
            //transformer.setOutputProperty(OutputKeys.METHOD,"html");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.VERSION,"1.0");
            //transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            DOMSource source = new DOMSource(document);
            Writer stringWriter = new StringWriter();
            StreamResult streamResult = new StreamResult(stringWriter);
            transformer.transform(source, streamResult);
            xml = stringWriter.toString();

            //DOMImplementationLS domImplementation = (DOMImplementationLS) doc.getImplementation();
            //LSSerializer lsSerializer = domImplementation.createLSSerializer();
            //lsSerializer.getDomConfig().setParameter("format-pretty-print", true);
            //xml = lsSerializer.writeToString(doc);

        } catch (ParserConfigurationException | TransformerException e) {
            throw new NomothesiaException(e);
        }

        return xml;
    }
}
