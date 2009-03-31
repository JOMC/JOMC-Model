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

import java.util.List;
import javax.xml.bind.JAXBElement;

/**
 * Manages the object management and configuration model.
 *
 * @author <a href="mailto:cs@schulte.it">Christian Schulte</a>
 * @version $Id$
 */
public interface ModelManager
{

    /**
     * Gets all modules provided by an implementation.
     *
     * @return All modules provided by an implementation.
     */
    Modules getModules();

    /**
     * Gets the module declaring a given specification.
     *
     * @param specification The identifier of the specification whose module to return.
     *
     * @return The module declaring {@code specification} or {@code null}, if no module could be resolved.
     *
     * @throws NullPointerException if {@code specification} is {@code null}.
     * @throws ModelError if getting the module fails due to unrecoverable model errors.
     */
    Module getModuleOfSpecification( String specification ) throws NullPointerException, ModelError;

    /**
     * Gets the module declaring a given implementation.
     *
     * @param implementation The identifier of the implementation whose module to return.
     *
     * @return The module declaring {@code implementation} or {@code null}, if no module could be resolved.
     *
     * @throws NullPointerException if {@code implementation} is {@code null}.
     * @throws ModelError if getting the module fails due to unrecoverable model errors.
     */
    Module getModuleOfImplementation( String implementation ) throws NullPointerException, ModelError;

    /**
     * Gets an implementation including referenced model objects as a module.
     *
     * @param implementation The identifier of the implementation to resolve.
     *
     * @return A module declaring {@code implementation} in addition to all referenced model objects or {@code null} if
     * no implementation matching {@code implementation} is available.
     *
     * @throws NullPointerException if {@code implementation} is {@code null}.
     * @throws ModelError if getting the module fails due to unrecoverable model errors.
     */
    Module getImplementationModule( String implementation ) throws NullPointerException, ModelError;

    /**
     * Gets a specification for a given identifier.
     *
     * @param specification The identifier of the specification to return.
     *
     * @return The specification identified by {@code specification} from the modules provided by an implementation or
     * {@code null}, if no specification matching {@code specification} is available.
     *
     * @throws NullPointerException if {@code specification} is {@code null}.
     * @throws ModelError if getting the specification fails due to unrecoverable model errors.
     */
    Specification getSpecification( String specification ) throws NullPointerException, ModelError;

    /**
     * Gets references to all specifications an implementation implements.
     *
     * @param implementation The identifier of the implementation to get all implemented specifications of.
     *
     * @return List of references to all specifications implemented by {@code implementation} or {@code null}, if no
     * implementation matching {@code implementation} is available.
     *
     * @throws NullPointerException if {@code implementation} is {@code null}.
     * @throws ModelError if getting the specifications fails due to unrecoverable model errors.
     */
    List<SpecificationReference> getSpecifications( String implementation ) throws NullPointerException, ModelError;

    /**
     * Gets an implementation for a given identifier.
     *
     * @param implementation The identifier of the implementation to return.
     *
     * @return The implementation identified by {@code implementation} from the modules provided by an implementation or
     * {@code null}, if no implementation matching {@code implementation} is available.
     *
     * @throws NullPointerException if {@code implementation} is {@code null}.
     * @throws ModelError if getting the implementation fails due to unrecoverable model errors.
     */
    Implementation getImplementation( String implementation ) throws NullPointerException, ModelError;

    /**
     * Gets an implementation for a given class.
     *
     * @param implementation The class of the implementation to return.
     *
     * @return The implementation identified by {@code implementation} from the modules provided by an implementation or
     * {@code null}, if no implementation matching {@code implementation} is available.
     *
     * @throws NullPointerException if {@code implementation} is {@code null}.
     * @throws ModelError if getting the implementation fails due to unrecoverable model errors.
     */
    Implementation getImplementation( Class implementation ) throws NullPointerException, ModelError;

    /**
     * Gets an implementation for a given name implementing a given specification.
     *
     * @param specification The identifier of the specification to return an implementation of.
     * @param name The name of the implementation to return.
     *
     * @return The implementation with name {@code name} implementing the specification identified by
     * {@code specification} from the modules provided by an implementation or {@code null}, if no such implementation
     * is found.
     *
     * @throws NullPointerException if {@code specification} or {@code name} is {@code null}.
     * @throws ModelError if getting the implementation fails due to unrecoverable model errors.
     */
    Implementation getImplementation( String specification, String name ) throws NullPointerException, ModelError;

    /**
     * Gets all dependencies of an implementation.
     *
     * @param implementation The identifier of the implementation to get all dependencies of.
     *
     * @return List of all dependencies of {@code implementation} or {@code null}, if nothing could be resolved.
     *
     * @throws NullPointerException if {@code implementation} is {@code null}.
     * @throws ModelError if getting the dependencies fails due to unrecoverable model errors.
     */
    Dependencies getDependencies( String implementation ) throws NullPointerException, ModelError;

    /**
     * Gets all properties of an implementation.
     *
     * @param implementation The identifier of the implementation to get all properties of.
     *
     * @return List of all properties of {@code implementation} or {@code null}, if nothing could be resolved.
     *
     * @throws NullPointerException if {@code implementation} is {@code null}.
     * @throws ModelError if getting the properties fails due to unrecoverable model errors.
     */
    Properties getProperties( String implementation ) throws NullPointerException, ModelError;

    /**
     * Gets all properties specified for an implementation.
     *
     * @param implementation The identifier of the implementation to return specified properties of.
     *
     * @return List of all properties specified for {@code implementation} or {@code null}, if nothing could be
     * resolved.
     *
     * @throws NullPointerException if {@code implementation} is {@code null}.
     * @throws ModelError if getting the properties fails due to unrecoverable model errors.
     */
    Properties getSpecifiedProperties( String implementation ) throws NullPointerException, ModelError;

    /**
     * Gets all messages of an implementation.
     *
     * @param implementation The identifier of the implementation to get all messages of.
     *
     * @return List of messages of {@code implementation} or {@code null}, if nothing could be resolved.
     *
     * @throws NullPointerException if {@code implementation} is {@code null}.
     * @throws ModelError if getting the messages fails due to unrecoverable model errors.
     */
    Messages getMessages( String implementation ) throws NullPointerException, ModelError;

    /**
     * Gets all implementations implementing a given specification.
     *
     * @param specification The identifier of the specification to return implementations of.
     *
     * @return All implementations implementing the specification identified by {@code specification} from the modules
     * provided by an implementation or {@code null}, if no implementation implementing {@code specification} is
     * available.
     *
     * @throws NullPointerException if {@code specification} is {@code null}.
     * @throws ModelError if getting the implementations fails due to unrecoverable model errors.
     */
    Implementations getImplementations( String specification ) throws NullPointerException, ModelError;

    /**
     * Gets all implementations implementing a given dependency from a list of modules.
     *
     * @param implementation The identifier of the implementation declaring {@code dependency}.
     * @param dependency The name of the dependency to return implementations of.
     *
     * @return All implementations of {@code dependency} from the modules provided by an implementation or {@code null}
     * if no implementation is available.
     *
     * @throws NullPointerException if {@code implementation} or {@code dependency} is {@code null}.
     * @throws ModelError if getting the implementations fails due to unrecoverable model errors.
     */
    Implementations getImplementations( String implementation, String dependency )
        throws NullPointerException, ModelError;

    /**
     * Gets an instance of an implementation.
     *
     * @param implementation The identifier of the implementation to return an instance of.
     *
     * @return An instance of the implementation identified by {@code implementation} from the modules provided by an
     * implementation or {@code null}, if no such instance is available.
     *
     * @throws NullPointerException if {@code implementation} is {@code null}.
     * @throws ModelError if getting the instance fails due to unrecoverable model errors.
     */
    Instance getInstance( String implementation ) throws NullPointerException, ModelError;

    /**
     * Gets an instance of an implementation of a given specification.
     *
     * @param specification The identifier of the specification to return an instance of.
     * @param name The name of the implementation implementing {@code specification} to return an instance of.
     *
     * @return An instance of the implementation with name {@code name} implementing the specification identified by
     * {@code specification} from the modules provided by an implementation or {@code null}, if no such instance is
     * available.
     *
     * @throws NullPointerException if {@code specification} or {@code name} is {@code null}.
     * @throws ModelError if getting the instance fails due to unrecoverable model errors.
     */
    Instance getInstance( String specification, String name ) throws NullPointerException, ModelError;

    /**
     * Gets an instance of an implementation of a given specification as required by a given dependency.
     *
     * @param specification The identifier of the specification to return an implementation instance of.
     * @param name The name of the implementation to return an instance of.
     * @param dependency The dependency requiring the instance.
     *
     * @return An instance of the implementation with name {@code name} implementing the specification identified by
     * {@code specification} as required by {@code dependency} from the modules provided by an implementation or
     * {@code null}, if no such instance is available.
     *
     * @throws NullPointerException if {@code specification}, {@code name} or {@code dependency} is {@code null}.
     * @throws ModelError if getting the instance fails due to unrecoverable model errors.
     */
    Instance getInstance( String specification, String name, Dependency dependency )
        throws NullPointerException, ModelError;

    /**
     * Validates a given model object.
     *
     * @param modelObject The object to validate.
     *
     * @throws NullPointerException if {@code modelObject} is {@code null}.
     * @throws ModelException if {@code modelObject} is invalid.
     * @throws ModelError if validating the model object fails due to unrecoverable model errors.
     */
    void assertValidModelObject( JAXBElement<? extends ModelObject> modelObject )
        throws NullPointerException, ModelException, ModelError;

    /**
     * Validates modules.
     *
     * @param modules The modules to validate.
     *
     * @throws NullPointerException if {@code modules} is {@code null}.
     * @throws ModelException if {@code modules} is invalid.
     * @throws ModelError if validating the modules fails due to unrecoverable model errors.
     */
    void assertValidModules( Modules modules ) throws NullPointerException, ModelException, ModelError;

    /**
     * Creates an object.
     *
     * @param specification The identifier of the specification specifying the object to create.
     * @param name The name of the implementation implementing the object to create.
     * @param classLoader The classloader to use for loading classes.
     *
     * @return A new object or {@code null} if nothing could be resolved.
     *
     * @throws NullPointerException if {@code specification}, {@code name} or {@code classLoader} is {@code null}.
     * @throws InstantiationException if instantiation fails.
     * @throws ModelError if creating the object fails due to unrecoverable model errors.
     */
    Object createObject( String specification, String name, ClassLoader classLoader )
        throws NullPointerException, InstantiationException, ModelError;

}
