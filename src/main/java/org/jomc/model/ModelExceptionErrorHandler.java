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

import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * {@code ErrorHander} collecting {@code ModelException.Detail}s.
 *
 * @author <a href="mailto:cs@jomc.org">Christian Schulte</a>
 * @version $Id$
 *
 * @see #getDetails()
 */
final class ModelExceptionErrorHandler implements ErrorHandler
{

    /** The details of the instance. */
    private final List<ModelException.Detail> details = new LinkedList<ModelException.Detail>();

    /** Creates a new {@code ModelExceptionErrorHandler} instance. */
    ModelExceptionErrorHandler()
    {
        super();
    }

    /**
     * Gets the details of the instance.
     *
     * @return The details of the instance.
     */
    public List<ModelException.Detail> getDetails()
    {
        return this.details;
    }

    public void warning( final SAXParseException exception ) throws SAXException
    {
        if ( exception.getMessage() != null )
        {
            this.getDetails().add( new ModelException.Detail( Level.WARNING, exception.getMessage() ) );
        }
    }

    public void error( final SAXParseException exception ) throws SAXException
    {
        if ( exception.getMessage() != null )
        {
            this.getDetails().add( new ModelException.Detail( Level.SEVERE, exception.getMessage() ) );
        }

        throw exception;
    }

    public void fatalError( final SAXParseException exception ) throws SAXException
    {
        if ( exception.getMessage() != null )
        {
            this.getDetails().add( new ModelException.Detail( Level.SEVERE, exception.getMessage() ) );
        }

        throw exception;
    }

}
