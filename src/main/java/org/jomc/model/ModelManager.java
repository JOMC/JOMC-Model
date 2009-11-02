/*
 *   Copyright (c) 2009 The JOMC Project
 *   Copyright (c) 2005 Christian Schulte <cs@jomc.org>
 *   All rights reserved.
 *
 *   Redistribution and use in source and binary forms, with or without
 *   modification, are permitted provided that the following conditions
 *   are met:
 *
 *     o Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *
 *     o Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in
 *       the documentation and/or other materials provided with the
 *       distribution.
 *
 *   THIS SOFTWARE IS PROVIDED BY THE JOMC PROJECT AND CONTRIBUTORS "AS IS"
 *   AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 *   THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 *   PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE JOMC PROJECT OR
 *   CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 *   EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 *   PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 *   OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 *   WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 *   OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 *   ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 *   $Id$
 *
 */
package org.jomc.model;

import java.io.IOException;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.validation.Schema;
import org.w3c.dom.ls.LSResourceResolver;
import org.xml.sax.EntityResolver;
import org.xml.sax.SAXException;

/**
 * Manages the object management and configuration model.
 *
 * <p><b>Resource management</b><ul>
 * <li>{@link #getEntityResolver(java.lang.ClassLoader) }</li>
 * <li>{@link #getResourceResolver(java.lang.ClassLoader) }</li>
 * </ul></p>
 *
 * <p><b>Binding management</b><ul>
 * <li>{@link #getContext(java.lang.ClassLoader) }</li>
 * <li>{@link #getMarshaller(java.lang.ClassLoader) }</li>
 * <li>{@link #getUnmarshaller(java.lang.ClassLoader) }</li>
 * </ul></p>
 *
 * <p><b>Validation management</b><ul>
 * <li>{@link #getSchema(java.lang.ClassLoader) }</li>
 * </ul></p>
 *
 * @author <a href="mailto:cs@jomc.org">Christian Schulte</a>
 * @version $Id$
 */
public interface ModelManager
{

    /**
     * Gets a new object management and configuration entity resolver instance.
     *
     * @param classLoader The class loader to use for resolving entities.
     *
     * @return A new object management and configuration entity resolver instance resolving entities using the given
     * class loader.
     *
     * @throws NullPointerException if {@code classLoader} is {@code null}.
     */
    EntityResolver getEntityResolver( ClassLoader classLoader ) throws NullPointerException;

    /**
     * Gets a new object management and configuration L/S resource resolver instance.
     *
     * @param classLoader The class loader to use for resolving entities.
     *
     * @return A new object management and configuration L/S resource resolver instance resolving entities using the
     * given class loader.
     *
     * @throws NullPointerException if {@code classLoader} is {@code null}.
     */
    LSResourceResolver getResourceResolver( ClassLoader classLoader ) throws NullPointerException;

    /**
     * Gets a new object management and configuration JAXP schema instance.
     *
     * @param classLoader The class loader to use for loading schema resources.
     *
     * @return A new object management and configuration JAXP schema instance loaded using the given class loader.
     *
     * @throws NullPointerException if {@code classLoader} is {@code null}.
     * @throws IOException if reading schema resources fails.
     * @throws SAXException if parsing schema resources fails.
     * @throws JAXBException if unmarshalling schema resources or creating a context fails.
     */
    Schema getSchema( ClassLoader classLoader ) throws NullPointerException, IOException, SAXException, JAXBException;

    /**
     * Gets a new object management and configuration JAXB context instance.
     *
     * @param classLoader The class loader to create the context with.
     *
     * @return A new object management and configuration JAXB context instance created using the given class loader.
     *
     * @throws NullPointerException if {@code classLoader} is {@code null}.
     * @throws IOException if reading schema resources fails.
     * @throws SAXException if parsing schema resources fails.
     * @throws JAXBException if unmarshalling schema resources or creating a context fails.
     */
    JAXBContext getContext( ClassLoader classLoader )
        throws NullPointerException, IOException, SAXException, JAXBException;

    /**
     * Gets a new object management and configuration JAXB marshaller instance.
     *
     * @param classLoader The class loader to create the marshaller with.
     *
     * @return A new object management and configuration JAXB marshaller instance created using the given class loader.
     *
     * @throws NullPointerException if {@code classLoader} is {@code null}.
     * @throws IOException if reading schema resources fails.
     * @throws SAXException if parsing schema resources fails.
     * @throws JAXBException if unmarshalling schema resources or creating a marshaller fails.
     */
    Marshaller getMarshaller( ClassLoader classLoader )
        throws NullPointerException, IOException, SAXException, JAXBException;

    /**
     * Gets a new object management and configuration JAXB unmarshaller instance.
     *
     * @param classLoader The class loader to create the unmarshaller with.
     *
     * @return A new object management and configuration JAXB unmarshaller instance created using the given class loader.
     *
     * @throws NullPointerException if {@code classLoader} is {@code null}.
     * @throws IOException if reading schema resources fails.
     * @throws SAXException if parsing schema resources fails.
     * @throws JAXBException if unmarshalling schema resources or creating an unmarshaller fails.
     */
    Unmarshaller getUnmarshaller( ClassLoader classLoader )
        throws NullPointerException, IOException, SAXException, JAXBException;

}
