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

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.validation.Schema;

/**
 * Validates object management and configuration model objects.
 *
 * @author <a href="mailto:cs@jomc.org">Christian Schulte</a>
 * @version $Id$
 */
public interface ModelObjectValidator
{

    /**
     * Validates a given model object to conform to a given schema using a given context.
     *
     * @param modelObject The model object to validate.
     * @param context The context to use for validating {@code modelObject}.
     * @param schema The schema to use for validating {@code modelObect}.
     *
     * @return Report about the given model object.
     *
     * @throws NullPointerException if {@code modelObject}, {@code context} or {@code schema} is {@code null}.
     * @throws JAXBException if validation fails.
     *
     * @see ModelManager#getContext(java.lang.ClassLoader)
     * @see ModelManager#getSchema(java.lang.ClassLoader)
     * @see ModelObjectValidationReport#isModelObjectValid()
     */
    ModelObjectValidationReport validateModelObject( JAXBElement modelObject, JAXBContext context, Schema schema )
        throws NullPointerException, JAXBException;

    /**
     * Validates a given list of modules to conform to a given schema using a given context and to form a valid object
     * management and configuration runtime model.
     *
     * @param modules The modules to validate.
     * @param context The context to use for validating {@code modules}.
     * @param schema The schema to use for validating {@code modules}.
     *
     * @return Report about the given modules.
     *
     * @throws NullPointerException if {@code modules}, {@code context} or {@code schema} is {@code null}.
     * @throws JAXBException if validation fails.
     *
     * @see ModelManager#getContext(java.lang.ClassLoader)
     * @see ModelManager#getSchema(java.lang.ClassLoader)
     * @see ModelObjectValidationReport#isModelObjectValid()
     */
    ModelObjectValidationReport validateModules( JAXBElement<Modules> modules, JAXBContext context, Schema schema )
        throws NullPointerException, JAXBException;

}
