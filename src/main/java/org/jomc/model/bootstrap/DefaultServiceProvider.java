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
package org.jomc.model.bootstrap;

import java.net.URL;
import java.util.Enumeration;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

/**
 * Default {@code ServiceProvider} implementation.
 *
 * @author <a href="mailto:schulte2005@users.sourceforge.net">Christian Schulte</a>
 * @version $Id$
 * @see BootstrapContext#findServices()
 */
public class DefaultServiceProvider implements ServiceProvider
{

    /**
     * Classpath location searched for services by default.
     * @see #getDefaultServiceLocation()
     */
    private static final String DEFAULT_SERVICE_LOCATION = "META-INF/jomc-services.xml";

    /** Default service location. */
    private static volatile String defaultServiceLocation;

    /** Service location of the instance. */
    private String serviceLocation;

    /** Creates a new {@code DefaultServiceProvider} instance. */
    public DefaultServiceProvider()
    {
        super();
    }

    /**
     * Gets the default location searched for service resources.
     * <p>The default service location is controlled by system property
     * {@code org.jomc.model.bootstrap.DefaultServiceProvider.defaultServiceLocation} holding the location to search for
     * service resources by default. If that property is not set, the {@code META-INF/jomc-services.xml} default is
     * returned.</p>
     *
     * @return The location searched for service resources by default.
     *
     * @see #setDefaultServiceLocation(java.lang.String)
     */
    public static String getDefaultServiceLocation()
    {
        if ( defaultServiceLocation == null )
        {
            defaultServiceLocation = System.getProperty(
                "org.jomc.model.bootstrap.DefaultServiceProvider.defaultServiceLocation", DEFAULT_SERVICE_LOCATION );

        }

        return defaultServiceLocation;
    }

    /**
     * Sets the default location searched for service resources.
     *
     * @param value The new default location to search for service resources or {@code null}.
     *
     * @see #getDefaultServiceLocation()
     */
    public static void setDefaultServiceLocation( final String value )
    {
        defaultServiceLocation = value;
    }

    /**
     * Gets the location searched for service resources.
     *
     * @return The location searched for service resources.
     *
     * @see #getDefaultServiceLocation()
     * @see #setServiceLocation(java.lang.String)
     */
    public String getServiceLocation()
    {
        if ( this.serviceLocation == null )
        {
            this.serviceLocation = getDefaultServiceLocation();
        }

        return this.serviceLocation;
    }

    /**
     * Sets the location searched for service resources.
     *
     * @param value The new location to search for service resources or {@code null}.
     *
     * @see #getServiceLocation()
     */
    public void setServiceLocation( final String value )
    {
        this.serviceLocation = value;
    }

    /**
     * Searches a given context for services.
     *
     * @param context The context to search for services.
     * @param location The location to search at.
     *
     * @return The services found at {@code location} in {@code context} or {@code null} of no services are found.
     *
     * @throws NullPointerException if {@code context} or {@code location} is {@code null}.
     * @throws BootstrapException if searching the context fails.
     */
    public Services findServices( final BootstrapContext context, final String location ) throws BootstrapException
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
            final Services services = new Services();
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

                if ( content instanceof Service )
                {
                    services.getService().add( (Service) content );
                }
                else if ( content instanceof Services )
                {
                    for ( Service s : ( (Services) content ).getService() )
                    {
                        services.getService().add( s );
                    }
                }
            }

            return services.getService().isEmpty() ? null : services;
        }
        catch ( final JAXBException e )
        {
            throw new BootstrapException( e.getMessage(), e );
        }
    }

    /**
     * {@inheritDoc}
     *
     * @see #getServiceLocation()
     * @see #findServices(org.jomc.model.bootstrap.BootstrapContext, java.lang.String)
     */
    public Services findServices( final BootstrapContext context ) throws BootstrapException
    {
        if ( context == null )
        {
            throw new NullPointerException( "context" );
        }

        return this.findServices( context, this.getServiceLocation() );
    }

}
