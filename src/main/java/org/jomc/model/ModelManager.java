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
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.validation.Schema;
import org.w3c.dom.ls.LSResourceResolver;
import org.xml.sax.EntityResolver;
import org.xml.sax.SAXException;

/**
 * Manages the object management and configuration model.
 *
 * <p><b>Entity resolution</b><ul>
 * <li>{@link #getEntityResolver() }</li>
 * <li>{@link #getLSResourceResolver() }</li>
 * </ul></p>
 *
 * <p><b>JAXB</b><ul>
 * <li>{@link #getContext() }</li>
 * <li>{@link #getMarshaller(boolean, boolean) }</li>
 * <li>{@link #getObjectFactory() }</li>
 * <li>{@link #getUnmarshaller(boolean) }</li>
 * </ul></p>
 *
 * <p><b>Validation</b><ul>
 * <li>{@link #getSchema() }</li>
 * <li>{@link #validateModelObject(javax.xml.bind.JAXBElement) }</li>
 * <li>{@link #validateModules(org.jomc.model.Modules) }</li>
 * </ul></p>
 *
 * <p><b>Transformation</b><ul>
 * <li>{@link #transformModelObject(javax.xml.bind.JAXBElement, javax.xml.transform.Transformer) }</li>
 * </ul></p>
 *
 * <p><b>Queries</b><ul>
 * <li>{@link #getInstance(org.jomc.model.Modules, org.jomc.model.Implementation, java.lang.ClassLoader) }</li>
 * <li>{@link #getInstance(org.jomc.model.Modules, org.jomc.model.Implementation, org.jomc.model.Dependency, java.lang.ClassLoader) }</li>
 * <li>{@link #getInstance(org.jomc.model.Modules, java.lang.Object) }</li>
 * <li>{@link #getObject(org.jomc.model.Modules, org.jomc.model.Specification, org.jomc.model.Instance) }</li>
 * </ul></p>
 *
 * @author <a href="mailto:cs@jomc.org">Christian Schulte</a>
 * @version $Id$
 */
public interface ModelManager
{

    /**
     * Gets the object management and configuration entity resolver.
     *
     * @return The object management and configuration entity resolver.
     */
    EntityResolver getEntityResolver();

    /**
     * Gets the object management and configuration L/S resolver.
     *
     * @return The object management and configuration L/S resolver.
     */
    LSResourceResolver getLSResourceResolver();

    /**
     * Gets the object management and configuration {@code ObjectFactory}.
     *
     * @return The object management and configuration {@code ObjectFactory}.
     */
    ObjectFactory getObjectFactory();

    /**
     * Gets the object management and configuration schema.
     *
     * @return The object management and configuration schema.
     *
     * @throws IOException if reading schema resources fails.
     * @throws SAXException if parsing schema resources fails.
     * @throws JAXBException if unmarshalling schema resources fails.
     */
    Schema getSchema() throws IOException, SAXException, JAXBException;

    /**
     * Gets the object management and configuration {@code JAXBContext}.
     *
     * @return The object management and configuration {@code JAXBContext}.
     *
     * @throws IOException if reading schema resources fails.
     * @throws SAXException if parsing schema resources fails.
     * @throws JAXBException if unmarshalling schema resources fails.
     */
    JAXBContext getContext() throws IOException, SAXException, JAXBException;

    /**
     * Gets an object management and configuration {@code Marshaller}.
     *
     * @param validating {@code true} for a marshaller with additional schema validation support enabled; {@code false}
     * for a marshaller without additional schema validation support enabled.
     * @param formattedOutput {@code true} for the marshaller to produce formatted output; {@code false} for the
     * marshaller to not apply any formatting when marshalling.
     *
     * @return An object management and configuration {@code Marshaller}.
     *
     * @throws IOException if reading schema resources fails.
     * @throws SAXException if parsing schema resources fails.
     * @throws JAXBException if unmarshalling schema resources fails.
     */
    Marshaller getMarshaller( boolean validating, boolean formattedOutput )
        throws IOException, SAXException, JAXBException;

    /**
     * Gets an object management and configuration {@code Unmarshaller}.
     *
     * @param validating {@code true} for an unmarshaller with additional schema validation support enabled;
     * {@code false} for an unmarshaller without additional schema validation support enabled.
     *
     * @return An object management and configuration {@code Unmarshaller}.
     *
     * @throws IOException if reading schema resources fails.
     * @throws SAXException if parsing schema resources fails.
     * @throws JAXBException if unmarshalling schema resources fails.
     */
    Unmarshaller getUnmarshaller( boolean validating ) throws IOException, SAXException, JAXBException;

    /**
     * Validates a given model object.
     *
     * @param modelObject The object to validate.
     *
     * @throws NullPointerException if {@code modelObject} is {@code null}.
     * @throws ModelException if {@code modelObject} is invalid.
     * @throws IOException if reading schema resources fails.
     * @throws SAXException if parsing schema resources fails.
     * @throws JAXBException if unmarshalling schema resources fails.
     */
    void validateModelObject( JAXBElement<? extends ModelObject> modelObject )
        throws NullPointerException, ModelException, IOException, SAXException, JAXBException;

    /**
     * Validates modules.
     *
     * @param modules The modules to validate.
     *
     * @throws NullPointerException if {@code modules} is {@code null}.
     * @throws ModelException if {@code modules} is invalid.
     * @throws IOException if reading schema resources fails.
     * @throws SAXException if parsing schema resources fails.
     * @throws JAXBException if unmarshalling schema resources fails.
     */
    void validateModules( Modules modules )
        throws NullPointerException, ModelException, IOException, SAXException, JAXBException;

    /**
     * Transforms a given {@code ModelObject} with a given {@code Transformer}.
     *
     * @param modelObject The {@code ModelObject} to transform.
     * @param transformer The {@code Transformer} to transform {@code modelObject} with.
     *
     * @throws NullPointerException if {@code modelObject} or {@code transformer} is {@code null}.
     * @throws IOException if reading schema resources fails.
     * @throws SAXException if parsing schema resources fails.
     * @throws JAXBException if binding fails.
     * @throws TransformerException if the transformation fails.
     */
    <T extends ModelObject> T transformModelObject( JAXBElement<T> modelObject, Transformer transformer )
        throws NullPointerException, IOException, SAXException, JAXBException, TransformerException;

    /**
     * Gets an instance of an implementation.
     *
     * @param modules The modules declaring the instance to get.
     * @param implementation The implementation to get an instance of.
     * @param classLoader The class loader of the instance to get.
     *
     * @return An instance of {@code implementation} or {@code null} if no instance is available.
     *
     * @throws NullPointerException if {@code modules}, {@code implementation} or {@code classLoader} is {@code null}.
     */
    Instance getInstance( Modules modules, Implementation implementation, ClassLoader classLoader )
        throws NullPointerException;

    /**
     * Gets an instance of an implementation for a dependency.
     *
     * @param modules The modules declaring the instance to get.
     * @param implementation The implementation to get an instance of.
     * @param dependency The dependency declaring the instance to get.
     * @param classLoader The class loader of the instance to get.
     *
     * @return An instance of {@code implementation} or {@code null} if no instance is available.
     *
     * @throws NullPointerException if {@code modules}, {@code implementation}, {@code dependency} or
     * {@code classLoader} is {@code null}.
     */
    Instance getInstance( Modules modules, Implementation implementation, Dependency dependency,
                          ClassLoader classLoader )
        throws NullPointerException;

    /**
     * Gets the instance of an object.
     *
     * @param modules The modules declaring the instance to get.
     * @param object The object to get the instance of.
     *
     * @return The instance of {@code object} or {@code null} of nothing is known about {@code object}.
     *
     * @throws NullPointerException if {@code modules} or {@code object} is {@code null},
     */
    Instance getInstance( Modules modules, Object object )
        throws NullPointerException;

    /**
     * Gets the object of an instance.
     *
     * @param modules The modules declaring the object to get.
     * @param specification The specification specifying the object to get.
     * @param instance The instance of the object to get.
     *
     * @return The object of {@code instance} or {@code null} if nothing is known about {@code instance}.
     *
     * @throws NullPointerException if {@code modules}, {@code specification} or {@code instance} is {@code null}.
     * @throws InstantiationException if instantiating the object fails.
     */
    Object getObject( Modules modules, Specification specification, Instance instance )
        throws NullPointerException, InstantiationException;

}
