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

import javax.xml.bind.JAXBElement;

/**
 * Gets thrown for model related recoverable exceptions.
 *
 * @author <a href="mailto:cs@schulte.it">Christian Schulte</a>
 * @version $Id$
 */
public class ModelException extends Exception
{

    /**
     * The element causing the exception.
     * @serial
     */
    private JAXBElement<? extends ModelObject> element;

    /**
     * Creates a new {@code ModelException} instance taking a message.
     *
     * @param message The message of the exception.
     * @param element The element causing the exception.
     */
    public ModelException( final String message, final JAXBElement<? extends ModelObject> element )
    {
        super( message );
        this.element = element;
    }

    /**
     * Creates a new {@code ModelException} instance taking a causing exception.
     *
     * @param t The causing exception.
     * @param element The element causing the exception.
     */
    public ModelException( final Throwable t, final JAXBElement<? extends ModelObject> element )
    {
        super( t );
        this.element = element;
    }

    /**
     * Creates a new {@code ModelException} instance taking a message and a causing exception.
     *
     * @param message The message of the exception.
     * @param t The causing exception.
     * @param element The element causing the exception.
     */
    public ModelException( final String message, final Throwable t, final JAXBElement<? extends ModelObject> element )
    {
        super( message, t );
        this.element = element;
    }

    /**
     * Gets the element causing the exception.
     *
     * @return The element causing the exception or {@code null}.
     */
    public JAXBElement<? extends ModelObject> getElement()
    {
        return this.element;
    }

}
