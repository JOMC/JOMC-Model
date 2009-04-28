/*
 *  JOMC Model
 *  Copyright (c) 2005 Christian Schulte <cs@schulte.it>
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 *
 */
package org.jomc.model;

import java.io.IOException;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
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
 * @author <a href="mailto:cs@schulte.it">Christian Schulte</a>
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
     */
    void validateModelObject( JAXBElement<? extends ModelObject> modelObject )
        throws NullPointerException, ModelException;

    /**
     * Validates modules.
     *
     * @param modules The modules to validate.
     *
     * @throws NullPointerException if {@code modules} is {@code null}.
     * @throws ModelException if {@code modules} is invalid.
     */
    void validateModules( Modules modules ) throws NullPointerException, ModelException;

    /**
     * Creates an object.
     *
     * @param modules The modules declaring the object to create.
     * @param specification The identifier of the specification specifying the object to create.
     * @param name The name of the implementation implementing the object to create.
     * @param classLoader The classloader to use for loading classes.
     *
     * @return A new object or {@code null} if nothing could be resolved.
     *
     * @throws NullPointerException if {@code specification}, {@code name} or {@code classLoader} is {@code null}.
     * @throws InstantiationException if instantiation fails.
     */
    Object createObject( Modules modules, String specification, String name, ClassLoader classLoader )
        throws NullPointerException, InstantiationException;

}
