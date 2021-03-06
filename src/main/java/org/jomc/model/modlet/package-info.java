/*
 *   Copyright (C) 2020 Christian Schulte <cs@schulte.it>
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
 *   THIS SOFTWARE IS PROVIDED "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 *   INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY
 *   AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL
 *   THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY DIRECT, INDIRECT,
 *   INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 *   NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 *   DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 *   THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *   (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 *   THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 *   $JOMC$
 *
 */
/**
 * Object management and configuration modlet classes.
 * <p>
 * The object management and configuration model is identified by the {@code http://jomc.org/model} public id in
 * a {@code ModelContext}. The {@code any} property of a {@code Model} instance searched using that public id will
 * hold a {@code JAXBElement<Modules>} instance to access using the {@code getAnyElement} method of class {@code Model}.
 * <blockquote><pre>
 * final ModelContext modelContext = ModelContextFactory.newInstance().newModelContext( a class loader );
 * final Model model = modelContext.findModel( ModelObject.MODEL_PUBLIC_ID );
 * final Model processed = modelContext.processModel( model );
 * final ModelValidationReport validationReport = modelContext.validateModel( processed );
 * final Modules modules = ModelHelper.getModules( model );
 *
 * final Model model = new Model();
 * model.setIdentifier( ModelObject.MODEL_PUBLIC_ID );
 * ModelHelper.setModules( model, new Modules() );
 * </pre></blockquote>
 * </p>
 * @see org.jomc.model.modlet.ModelHelper
 */
package org.jomc.model.modlet;
