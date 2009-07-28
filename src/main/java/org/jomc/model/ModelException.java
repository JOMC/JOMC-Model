/*
 *   Copyright (c) 2009 The JOMC Project
 *   Copyright (c) 2005 Christian Schulte <schulte2005@users.sourceforge.net>
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

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import javax.xml.bind.JAXBElement;

/**
 * Gets thrown for invalid model objects.
 *
 * @author <a href="mailto:schulte2005@users.sourceforge.net">Christian Schulte</a>
 * @version $Id$
 */
public class ModelException extends Exception
{

    /** {@code ModelException} detail. */
    public static class Detail implements Serializable
    {

        /**
         * The detail level.
         * @serial
         */
        private Level level;

        /**
         * The detail message.
         * @serial
         */
        private String message;

        /**
         * The element this detail is associated with.
         * @serial
         */
        private JAXBElement<? extends ModelObject> element;

        /**
         * Creates a new {@code Detail} taking a detail level and message.
         *
         * @param level The detail level.
         * @param message The detail message.
         */
        public Detail( final Level level, final String message )
        {
            this.level = level;
            this.message = message;
        }

        /**
         * Gets the level of this detail.
         *
         * @return The level of this detail.
         */
        public Level getLevel()
        {
            return this.level;
        }

        /**
         * Gets the message of this detail.
         *
         * @return The message of this detail.
         */
        public String getMessage()
        {
            return this.message;
        }

        /**
         * Gets the element of this detail.
         *
         * @return The element of this detail or {@code null}.
         */
        public JAXBElement<? extends ModelObject> getElement()
        {
            return this.element;
        }

        /**
         * Sets the element of this detail.
         *
         * @param value The new element of this detail or {@code null}.
         */
        public void setElement( final JAXBElement<? extends ModelObject> value )
        {
            this.element = value;
        }

    }

    /** Serial version UID for compatibility with 1.0.x object streams. */
    private static final long serialVersionUID = 6078527305669819171L;

    /**
     * Details of the instance.
     * @serial
     */
    private List<Detail> details;

    /** Creates a new {@code ModelException} instance. */
    public ModelException()
    {
        super();
    }

    /**
     * Creates a new {@code ModelException} instance taking a message.
     *
     * @param message The message of the exception.
     */
    public ModelException( final String message )
    {
        super( message );
    }

    /**
     * Creates a new {@code ModelException} instance taking a causing exception.
     *
     * @param t The causing exception.
     */
    public ModelException( final Throwable t )
    {
        super( t );
    }

    /**
     * Creates a new {@code ModelException} instance taking a message and a causing exception.
     *
     * @param message The message of the exception.
     * @param t The causing exception.
     */
    public ModelException( final String message, final Throwable t )
    {
        super( message, t );
    }

    /**
     * Gets the details of the instance.
     *
     * @return The details of the instance.
     */
    public List<Detail> getDetails()
    {
        if ( this.details == null )
        {
            this.details = new LinkedList<Detail>();
        }

        return this.details;
    }

}
