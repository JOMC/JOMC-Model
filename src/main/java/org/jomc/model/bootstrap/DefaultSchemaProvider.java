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
package org.jomc.model.bootstrap;

import java.net.URL;
import java.util.Enumeration;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

/**
 * Default {@code SchemaProvider} implementation.
 *
 * @author <a href="mailto:cs@jomc.org">Christian Schulte</a>
 * @version $Id$
 * @see BootstrapContext#findSchemas()
 */
public class DefaultSchemaProvider implements SchemaProvider
{

    /**
     * Classpath location searched for schemas by default.
     * @see #getDefaultSchemaLocation()
     */
    private static final String DEFAULT_SCHEMA_LOCATION = "META-INF/jomc-bootstrap.xml";

    /** Default schema location. */
    private static volatile String defaultSchemaLocation;

    /** Creates a new {@code DefaultSchemaProvider} instance. */
    public DefaultSchemaProvider()
    {
        super();
    }

    /**
     * Gets the default location searched for schema resources.
     * <p>The default schema location is controlled by system property
     * {@code org.jomc.model.bootstrap.DefaultSchemaProvider.defaultSchemaLocation} holding the location to search for
     * schema resources by default. If that property is not set, the {@code META-INF/jomc-bootstrap.xml} default is
     * returned.</p>
     *
     * @return The location searched for schema resources by default.
     *
     * @see #findSchemas(org.jomc.model.bootstrap.BootstrapContext, java.lang.String)
     */
    public static String getDefaultSchemaLocation()
    {
        if ( defaultSchemaLocation == null )
        {
            defaultSchemaLocation = System.getProperty(
                "org.jomc.model.bootstrap.DefaultSchemaProvider.defaultSchemaLocation", DEFAULT_SCHEMA_LOCATION );

        }

        return defaultSchemaLocation;
    }

    /**
     * Sets the default location searched for schema resources.
     *
     * @param value The new default location to search for schema resources or {@code null}.
     *
     * @see #getDefaultSchemaLocation()
     */
    public static void setDefaultSchemaLocation( final String value )
    {
        defaultSchemaLocation = value;
    }

    /**
     * Searches a given context for schemas.
     *
     * @param context The context to search for schemas.
     * @param location The location to search at.
     *
     * @return The schemas found at {@code location} in {@code context} or {@code null} of no schemas are found.
     *
     * @throws NullPointerException if {@code context} or {@code location} is {@code null}.
     * @throws BootstrapException if searching the context fails.
     *
     * @see #getDefaultSchemaLocation()
     */
    public Schemas findSchemas( final BootstrapContext context, final String location ) throws BootstrapException
    {
        if ( context == null )
        {
            throw new NullPointerException( "context" );
        }
        if ( location == null )
        {
            throw new NullPointerException( "location" );
        }

        try
        {
            final Schemas schemas = new Schemas();
            final JAXBContext ctx = context.createContext();
            final Unmarshaller u = ctx.createUnmarshaller();
            final Enumeration<URL> e = context.findResources( location );
            u.setSchema( context.createSchema() );

            while ( e.hasMoreElements() )
            {
                final URL url = e.nextElement();
                Object content = u.unmarshal( url );
                if ( content instanceof JAXBElement )
                {
                    content = ( (JAXBElement) content ).getValue();
                }

                if ( content instanceof Schema )
                {
                    schemas.getSchema().add( (Schema) content );
                }
                else if ( content instanceof Schemas )
                {
                    for ( Schema s : ( (Schemas) content ).getSchema() )
                    {
                        schemas.getSchema().add( s );
                    }
                }
            }

            return schemas.getSchema().isEmpty() ? null : schemas;
        }
        catch ( final JAXBException e )
        {
            throw new BootstrapException( e );
        }
    }

    /**
     * {@inheritDoc}
     *
     * @see #findSchemas(org.jomc.model.bootstrap.BootstrapContext, java.lang.String)
     */
    public Schemas findSchemas( final BootstrapContext context ) throws BootstrapException
    {
        if ( context == null )
        {
            throw new NullPointerException( "context" );
        }

        return this.findSchemas( context, getDefaultSchemaLocation() );
    }

}
