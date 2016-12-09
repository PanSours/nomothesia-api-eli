package com.di.nomothesia.service.xml;

import com.di.nomothesia.NomothesiaException;

/**
 * Giving a parametr of type P as arguments this interface
 * creates an xml returning tyoe of E.
 *
 * @param <P> param of type P
 * @param <E> param of type E
 */
public interface XmlBuilder<P,E> {
    /**
     * Builds an xml file given a document.
     *
     * @param document the legal document
     * @return xml document in type E
     * @throws NomothesiaException
     */
    public E buildXml(P document) throws NomothesiaException;
}
