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

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import javax.xml.bind.JAXBElement;

/**
 * Gets thrown for invalid model objects.
 *
 * @author <a href="mailto:cs@schulte.it">Christian Schulte</a>
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

    /** Details of the instance. */
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
