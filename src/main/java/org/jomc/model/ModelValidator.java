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

/**
 * Object management and configuration model validator interface.
 *
 * @author <a href="mailto:cs@jomc.org">Christian Schulte</a>
 * @version $Id$
 */
public interface ModelValidator
{

    /**
     * Validates a given model object.
     *
     * @param context The context to use for validating {@code modelObject}.
     * @param modelObject The model object to validate.
     *
     * @return Validation report.
     *
     * @throws NullPointerException if {@code context} or {@code modelObject} is {@code null}.
     * @throws ModelException if validation fails.
     *
     * @see ModelValidationReport#isModelValid()
     */
    ModelValidationReport validateModelObject( ModelContext context, ModelObject modelObject )
        throws NullPointerException, ModelException;

    /**
     * Validates a given list of modules.
     *
     * @param context The context to use for validating {@code modules}.
     * @param modules The list of modules to validate.
     *
     * @return Validation report.
     *
     * @throws NullPointerException if {@code context} or {@code modules} is {@code null}.
     * @throws ModelException if validation fails.
     *
     * @see ModelValidationReport#isModelValid()
     */
    ModelValidationReport validateModules( ModelContext context, Modules modules )
        throws NullPointerException, ModelException;

}