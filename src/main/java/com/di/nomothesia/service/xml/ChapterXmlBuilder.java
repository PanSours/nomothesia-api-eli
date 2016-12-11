package com.di.nomothesia.service.xml;

import com.di.nomothesia.NomothesiaException;
import com.di.nomothesia.enums.LegislationServiceEnum;
import com.di.nomothesia.model.*;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringWriter;
import java.io.Writer;

/**
 * Created by Panagiotis on 10/12/2016.
 *
 */
@Service
public class ChapterXmlBuilder implements XmlBuilder<LegalDocument, String> {

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
            rootElement.setAttribute(LegislationServiceEnum.ID.getType(), legalDocument.getId());
            rootElement.setAttribute(LegislationServiceEnum.DOC_URI.getType(), legalDocument.getURI());
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
                    tag.setAttribute(LegislationServiceEnum.ID.getType(), Integer.toString(i+1));
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
                    citationE.setAttribute(LegislationServiceEnum.ID.getType(), Integer.toString(citation.getId()));
                    citationE.setAttribute(LegislationServiceEnum.DOC_URI.getType(), citation.getURI());
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

            for (Chapter chapter: legalDocument.getChapters()) {
                //article
                Element chapterE = document.createElement("Chapter");
                chapterE.setAttribute(LegislationServiceEnum.ID.getType(), Integer.toString(chapter.getId()));
                chapterE.setAttribute(LegislationServiceEnum.DOC_URI.getType(), chapter.getURI());
                rootElement.appendChild(chapterE);

                //article title
                if (chapter.getTitle() != null) {
                    Element artTitle = document.createElement("Title");
                    artTitle.setTextContent(chapter.getTitle());
                    chapterE.appendChild(artTitle);
                }

                //Article Branch (inside body when body is completed)
                for (Article article: chapter.getArticles()) {
                    //article
                    Element articleE = document.createElement(LegislationServiceEnum.ARTICLE.getType());
                    articleE.setAttribute(LegislationServiceEnum.ID.getType(), article.getId());
                    articleE.setAttribute(LegislationServiceEnum.DOC_URI.getType(), article.getURI());

                    //article title
                    if (article.getTitle() != null) {
                        Element artTitle = document.createElement("Title");
                        artTitle.setTextContent(article.getTitle());
                        articleE.appendChild(artTitle);
                    }

                    for (Paragraph paragraph: article.getParagraphs()) {
                        //paragraph
                        Element paragraphE = document.createElement(LegislationServiceEnum.PARAGRAPH.getType());
                        paragraphE.setAttribute(LegislationServiceEnum.ID.getType(), paragraph.getId());
                        paragraphE.setAttribute(LegislationServiceEnum.DOC_URI.getType(), paragraph.getURI());

                        Element list = document.createElement("List");

                        for(Case caseL: paragraph.getCaseList()) {
                            //paragraph case list
                            paragraphE.appendChild(list);
                            Element pcase = document.createElement("Case");
                            pcase.setAttribute(LegislationServiceEnum.ID.getType(), Integer.toString(caseL.getId()));
                            pcase.setAttribute(LegislationServiceEnum.DOC_URI.getType(), caseL.getURI());
                            list.appendChild(pcase);

                            //case passages
                            for (Passage passageC: caseL.getPassages()) {
                                //case passages
                                Element cpassage = document.createElement(LegislationServiceEnum.PASSAGE.getType());
                                cpassage.setAttribute(LegislationServiceEnum.ID.getType(), Integer.toString(passageC.getId()));
                                cpassage.setAttribute(LegislationServiceEnum.DOC_URI.getType(), passageC.getURI());

                                //case passage text
                                Element casText = document.createElement("text");
                                casText.setTextContent("" + passageC.getText());
                                cpassage.appendChild(casText);

                                pcase.appendChild(cpassage);
                            }

                            //case in case
                            if (caseL.getCaseList() != null) {
                                Element list2 = document.createElement("List");

                                for(Case caseNested: caseL.getCaseList()) {
                                    //paragraph case list
                                    pcase.appendChild(list2);
                                    Element pcase2 = document.createElement("Case");
                                    pcase2.setAttribute(LegislationServiceEnum.ID.getType(), Integer.toString(caseNested.getId()));
                                    pcase2.setAttribute(LegislationServiceEnum.DOC_URI.getType(), caseNested.getURI());
                                    list2.appendChild(pcase2);

                                    //case passages
                                    for (Passage passageNested: caseNested.getPassages()) {
                                        //case passages
                                        Element cpassage2 = document.createElement(LegislationServiceEnum.PASSAGE.getType());
                                        cpassage2.setAttribute(LegislationServiceEnum.ID.getType(), Integer.toString(passageNested.getId()));
                                        cpassage2.setAttribute(LegislationServiceEnum.DOC_URI.getType(), passageNested.getURI());

                                        //case passage text
                                        Element casText2 = document.createElement("text");
                                        casText2.setTextContent("" + passageNested.getText());
                                        cpassage2.appendChild(casText2);

                                        pcase2.appendChild(cpassage2);
                                    }
                                }
                            }

                            if(caseL.getModification() != null) {
                                //modification
                                Element modification = document.createElement(LegislationServiceEnum.MODIFICATION.getType());
                                modification.setAttribute(LegislationServiceEnum.DOC_URI.getType(), caseL.getModification().getURI());
                                paragraphE.appendChild(modification);

                                if(LegislationServiceEnum.ARTICLE.getType().equals(caseL.getModification().getType())) {
                                    Article a = (Article) caseL.getModification().getFragment();
                                    Element paragraphin = document.createElement(LegislationServiceEnum.ARTICLE.getType());
                                    paragraphin.setAttribute(LegislationServiceEnum.ID.getType(), "" + a.getId());
                                    paragraphin.setAttribute(LegislationServiceEnum.DOC_URI.getType(), a.getURI());
                                    modification.appendChild(paragraphin);

                                    for (Paragraph paragraph1: a.getParagraphs()) {
                                        paragraphin = document.createElement(LegislationServiceEnum.PARAGRAPH.getType());
                                        paragraphin.setAttribute(LegislationServiceEnum.ID.getType(), "" + paragraph1.getId());
                                        paragraphin.setAttribute(LegislationServiceEnum.DOC_URI.getType(), paragraph1.getURI());
                                        modification.appendChild(paragraphin);

                                        for (Passage passage: paragraph1.getPassages()) {
                                            Element passagein = document.createElement(LegislationServiceEnum.PASSAGE.getType());
                                            passagein.setAttribute(LegislationServiceEnum.ID.getType(), Integer.toString(passage.getId()));
                                            passagein.setAttribute(LegislationServiceEnum.DOC_URI.getType(), passage.getURI());
                                            Element pasTextin = document.createElement("text");
                                            pasTextin.setTextContent(passage.getText());
                                            passagein.appendChild(pasTextin);

                                            paragraphin.appendChild(passagein);
                                        }

                                        for (Case caseL1: paragraph1.getCaseList()) {
                                            for (Passage passage: caseL1.getPassages()) {
                                                Element passagein2 = document.createElement(LegislationServiceEnum.PASSAGE.getType());
                                                passagein2.setAttribute(LegislationServiceEnum.ID.getType(), Integer.toString(passage.getId()));
                                                passagein2.setAttribute(LegislationServiceEnum.DOC_URI.getType(),passage.getURI());
                                                Element pasTextin2 = document.createElement("text");
                                                pasTextin2.setTextContent(passage.getText());
                                                passagein2.appendChild(pasTextin2);

                                                paragraphin.appendChild(passagein2);
                                            }
                                        }
                                    }
                                } else if(LegislationServiceEnum.PARAGRAPH.getType().equals(caseL.getModification().getType())) {
                                    Paragraph p = (Paragraph) caseL.getModification().getFragment();
                                    Element paragraphin = document.createElement(LegislationServiceEnum.PARAGRAPH.getType());
                                    paragraphin.setAttribute(LegislationServiceEnum.ID.getType(), p.getId());
                                    paragraphin.setAttribute(LegislationServiceEnum.DOC_URI.getType(), p.getURI());
                                    modification.appendChild(paragraphin);

                                    for (Passage passage2: p.getPassages()) {
                                        Element passagein = document.createElement(LegislationServiceEnum.PASSAGE.getType());
                                        passagein.setAttribute(LegislationServiceEnum.ID.getType(), Integer.toString(passage2.getId()));
                                        passagein.setAttribute(LegislationServiceEnum.DOC_URI.getType(), passage2.getURI());
                                        Element pasTextin = document.createElement("text");
                                        pasTextin.setTextContent(passage2.getText());
                                        passagein.appendChild(pasTextin);

                                        paragraphin.appendChild(passagein);
                                    }

                                    for (Case caseL2: p.getCaseList()) {
                                        for (Passage passage3: caseL2.getPassages()) {
                                            Element passagein2 = document.createElement(LegislationServiceEnum.PASSAGE.getType());
                                            passagein2.setAttribute(LegislationServiceEnum.ID.getType(), Integer.toString(passage3.getId()));
                                            passagein2.setAttribute(LegislationServiceEnum.DOC_URI.getType(), passage3.getURI());
                                            Element pasTextin2 = document.createElement("text");
                                            pasTextin2.setTextContent(passage3.getText());
                                            passagein2.appendChild(pasTextin2);

                                            paragraphin.appendChild(passagein2);
                                        }
                                    }
                                } else if(LegislationServiceEnum.CASE.getType().equals(caseL.getModification().getType())) {
                                    Case c = (Case) caseL.getModification().getFragment();
                                    Element paragraphin = document.createElement(LegislationServiceEnum.CASE.getType());
                                    paragraphin.setAttribute(LegislationServiceEnum.ID.getType(), Integer.toString(c.getId()));
                                    paragraphin.setAttribute(LegislationServiceEnum.DOC_URI.getType(), c.getURI());
                                    modification.appendChild(paragraphin);

                                    for (Passage passageC: c.getPassages()) {
                                        Element passage = document.createElement(LegislationServiceEnum.PASSAGE.getType());
                                        passage.setAttribute(LegislationServiceEnum.ID.getType(), Integer.toString( passageC.getId()));
                                        passage.setAttribute(LegislationServiceEnum.DOC_URI.getType(), passageC.getURI());
                                        Element pasText = document.createElement("text");
                                        pasText.setTextContent(passageC.getText());
                                        passage.appendChild(pasText);

                                        paragraphin.appendChild(passage);
                                    }
                                } else if(LegislationServiceEnum.PASSAGE.getType().equals(caseL.getModification().getType())) {
                                    Passage pas = (Passage) caseL.getModification().getFragment();
                                    Element passage = document.createElement(LegislationServiceEnum.PASSAGE.getType());
                                    passage.setAttribute(LegislationServiceEnum.ID.getType(), Integer.toString(pas.getId()));
                                    passage.setAttribute(LegislationServiceEnum.DOC_URI.getType(), pas.getURI());
                                    Element pasText = document.createElement("text");
                                    pasText.setTextContent(pas.getText());
                                    passage.appendChild(pasText);

                                    modification.appendChild(passage);
                                }
                            }
                        }

                        for (Passage passageP: paragraph.getPassages()) {
                            //paragraph passages
                            Element passage = document.createElement(LegislationServiceEnum.PASSAGE.getType());
                            passage.setAttribute(LegislationServiceEnum.ID.getType(), Integer.toString(passageP.getId()));
                            passage.setAttribute(LegislationServiceEnum.DOC_URI.getType(), passageP.getURI());

                            //passage text
                            Element pasText = document.createElement("text");
                            pasText.setTextContent("" + passageP.getText());
                            passage.appendChild(pasText);

                            paragraphE.appendChild(passage);

                            if(passageP.getModification() != null) {
                                //modification
                                Element modification = document.createElement(LegislationServiceEnum.MODIFICATION.getType());
                                modification.setAttribute(LegislationServiceEnum.DOC_URI.getType(), passageP.getModification().getURI());
                                paragraphE.appendChild(modification);

                                if(LegislationServiceEnum.ARTICLE.getType().equals(passageP.getModification().getType())) {
                                    Article a = (Article) passageP.getModification().getFragment();
                                    Element paragraphin = document.createElement(LegislationServiceEnum.ARTICLE.getType());
                                    paragraphin.setAttribute(LegislationServiceEnum.ID.getType(), a.getId());
                                    paragraphin.setAttribute(LegislationServiceEnum.DOC_URI.getType(), a.getURI());
                                    modification.appendChild(paragraphin);

                                    for (Paragraph paragraphA: a.getParagraphs()) {
                                        paragraphin = document.createElement(LegislationServiceEnum.PARAGRAPH.getType());
                                        paragraphin.setAttribute(LegislationServiceEnum.ID.getType(), paragraphA.getId());
                                        paragraphin.setAttribute(LegislationServiceEnum.DOC_URI.getType(), paragraphA.getURI());
                                        modification.appendChild(paragraphin);

                                        for (Passage passageA1: paragraphA.getPassages()) {

                                            Element passagein = document.createElement(LegislationServiceEnum.PASSAGE.getType());
                                            passagein.setAttribute(LegislationServiceEnum.ID.getType(), Integer.toString(passageA1.getId()));
                                            passagein.setAttribute(LegislationServiceEnum.DOC_URI.getType(), passageA1.getURI());
                                            Element pasTextin = document.createElement("text");
                                            pasTextin.setTextContent(passageA1.getText());
                                            passagein.appendChild(pasTextin);

                                            paragraphin.appendChild(passagein);
                                        }

                                        for (Case caseLA1: paragraphA.getCaseList()) {
                                            for (Passage passageA1C: caseLA1.getPassages()) {
                                                Element passagein2 = document.createElement(LegislationServiceEnum.PASSAGE.getType());
                                                passagein2.setAttribute(LegislationServiceEnum.ID.getType(),Integer.toString(passageA1C.getId()));
                                                passagein2.setAttribute(LegislationServiceEnum.DOC_URI.getType(), passageA1C.getURI());
                                                Element pasTextin2 = document.createElement("text");
                                                pasTextin2.setTextContent(passageA1C.getText());
                                                passagein2.appendChild(pasTextin2);

                                                paragraphin.appendChild(passagein2);
                                            }
                                        }
                                    }
                                } else if(LegislationServiceEnum.PARAGRAPH.getType().equals(passageP.getModification().getType())) {
                                    Paragraph p = (Paragraph) passageP.getModification().getFragment();
                                    Element paragraphin = document.createElement(LegislationServiceEnum.PARAGRAPH.getType());
                                    paragraphin.setAttribute(LegislationServiceEnum.ID.getType(), "" + p.getId());
                                    paragraphin.setAttribute(LegislationServiceEnum.DOC_URI.getType(), p.getURI());
                                    modification.appendChild(paragraphin);

                                    for (Passage passage4: p.getPassages()) {
                                        Element passagein = document.createElement(LegislationServiceEnum.PASSAGE.getType());
                                        passagein.setAttribute(LegislationServiceEnum.ID.getType(), Integer.toString( passage4.getId()));
                                        passagein.setAttribute(LegislationServiceEnum.DOC_URI.getType(), passage4.getURI());
                                        Element pasTextin = document.createElement("text");
                                        pasTextin.setTextContent(passage4.getText());
                                        passagein.appendChild(pasTextin);

                                        paragraphin.appendChild(passagein);
                                    }

                                    for (Case caseL4: p.getCaseList()) {
                                        for (Passage passage5: caseL4.getPassages()) {
                                            Element passagein2 = document.createElement(LegislationServiceEnum.PASSAGE.getType());
                                            passagein2.setAttribute(LegislationServiceEnum.ID.getType(), Integer.toString(passage5.getId()));
                                            passagein2.setAttribute(LegislationServiceEnum.DOC_URI.getType(), passage5.getURI());
                                            Element pasTextin2 = document.createElement("text");
                                            pasTextin2.setTextContent(passage5.getText());
                                            passagein2.appendChild(pasTextin2);

                                            paragraphin.appendChild(passagein2);
                                        }
                                    }
                                } else if(LegislationServiceEnum.CASE.getType().equals(passageP.getModification().getType())) {
                                    Case c = (Case) passageP.getModification().getFragment();
                                    Element paragraphin = document.createElement(LegislationServiceEnum.CASE.getType());
                                    paragraphin.setAttribute(LegislationServiceEnum.ID.getType(), Integer.toString(c.getId()));
                                    paragraphin.setAttribute(LegislationServiceEnum.DOC_URI.getType(), c.getURI());
                                    modification.appendChild(paragraphin);

                                    for (Passage passage6: c.getPassages()) {
                                        passage = document.createElement(LegislationServiceEnum.PASSAGE.getType());
                                        passage.setAttribute(LegislationServiceEnum.ID.getType(), Integer.toString(passage6.getId()));
                                        passage.setAttribute(LegislationServiceEnum.DOC_URI.getType(), passage6.getURI());
                                        pasText = document.createElement("text");
                                        pasText.setTextContent(passage6.getText());
                                        passage.appendChild(pasText);

                                        paragraphin.appendChild(passage);
                                    }

                                } else if(LegislationServiceEnum.PASSAGE.getType().equals(passageP.getModification().getType())) {
                                    Passage pas = (Passage) passageP.getModification().getFragment();
                                    passage = document.createElement(LegislationServiceEnum.PASSAGE.getType());
                                    passage.setAttribute(LegislationServiceEnum.ID.getType(), Integer.toString(pas.getId()));
                                    passage.setAttribute(LegislationServiceEnum.DOC_URI.getType(), pas.getURI());
                                    pasText = document.createElement("text");
                                    pasText.setTextContent(pas.getText());
                                    passage.appendChild(pasText);

                                    modification.appendChild(passage);
                                }
                            }
                        }

                        articleE.appendChild(paragraphE);
                    }

                    chapterE.appendChild(articleE);
                }
            }

            //Convert Document DOM type to String with greek characters support
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.ENCODING, "ISO-8859-1");
            //transformer.setOutputProperty(OutputKeys.METHOD,"html");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.VERSION,"1.0");
            //transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            DOMSource source = new DOMSource(dcformat);
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
