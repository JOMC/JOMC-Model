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

/**
 * Gets thrown for model related unrecoverable errors.
 *
 * @author <a href="mailto:cs@schulte.it">Christian Schulte</a>
 * @version $Id$
 */
public class ModelError extends Error
{

    /**
     * Creates a new {@code ModelError} instance taking a message.
     *
     * @param message The message of the exception.
     */
    public ModelError( final String message )
    {
        super( message );
    }

    /**
     * Creates a new {@code ModelError} instance taking a causing throwable.
     *
     * @param cause The causing throwable of the exception.
     */
    public ModelError( final Throwable cause )
    {
        super( cause );
    }

    /**
     * Creates a new {@code ModelError} instance taking a message and a causing throwable.
     *
     * @param message The message of the exception.
     * @param cause The causing throwable of the exception.
     */
    public ModelError( final String message, final Throwable cause )
    {
        super( message, cause );
    }

}
