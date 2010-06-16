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
package org.jomc.model.modlet;

import java.net.URL;
import java.util.Enumeration;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

/**
 * Default {@code ModletProvider} implementation.
 *
 * @author <a href="mailto:schulte2005@users.sourceforge.net">Christian Schulte</a>
 * @version $Id$
 * @see ModletContext#findModlets()
 */
public class DefaultModletProvider implements ModletProvider
{

    /**
     * Classpath location searched for modlets by default.
     * @see #getDefaultModletLocation()
     */
    private static final String DEFAULT_MODLET_LOCATION = "META-INF/jomc-modlet.xml";

    /** Default modlet location. */
    private static volatile String defaultModletLocation;

    /** Modlet location of the instance. */
    private String modletLocation;

    /** Creates a new {@code DefaultModletProvider} instance. */
    public DefaultModletProvider()
    {
        super();
    }

    /**
     * Gets the default location searched for modlet resources.
     * <p>The default modlet location is controlled by system property
     * {@code org.jomc.model.modlet.DefaultModletProvider.defaultModletLocation} holding the location to search for
     * modlet resources by default. If that property is not set, the {@code META-INF/jomc-modlet.xml} default is
     * returned.</p>
     *
     * @return The location searched for modlet resources by default.
     *
     * @see #setDefaultModletLocation(java.lang.String)
     */
    public static String getDefaultModletLocation()
    {
        if ( defaultModletLocation == null )
        {
            defaultModletLocation = System.getProperty(
                "org.jomc.model.modlet.DefaultModletProvider.defaultModletLocation", DEFAULT_MODLET_LOCATION );

        }

        return defaultModletLocation;
    }

    /**
     * Sets the default location searched for modlet resources.
     *
     * @param value The new default location to search for modlet resources or {@code null}.
     *
     * @see #getDefaultModletLocation()
     */
    public static void setDefaultModletLocation( final String value )
    {
        defaultModletLocation = value;
    }

    /**
     * Gets the location searched for modlet resources.
     *
     * @return The location searched for modlet resources.
     *
     * @see #getDefaultModletLocation()
     * @see #setModletLocation(java.lang.String)
     */
    public String getModletLocation()
    {
        if ( this.modletLocation == null )
        {
            this.modletLocation = getDefaultModletLocation();
        }

        return this.modletLocation;
    }

    /**
     * Sets the location searched for modlet resources.
     *
     * @param value The new location to search for modlet resources or {@code null}.
     *
     * @see #getModletLocation()
     */
    public void setModletLocation( final String value )
    {
        this.modletLocation = value;
    }

    /**
     * Searches a given context for modlets.
     *
     * @param context The context to search for modlets.
     * @param location The location to search at.
     *
     * @return The modlets found at {@code location} in {@code context} or {@code null} if no modlets are found.
     *
     * @throws NullPointerException if {@code context} or {@code location} is {@code null}.
     * @throws ModletException if searching the context fails.
     */
    public Modlets findModlets( final ModletContext context, final String location ) throws ModletException
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
            final Modlets modlets = new Modlets();
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

                if ( content instanceof Modlet )
                {
                    modlets.getModlet().add( (Modlet) content );
                }
                else if ( content instanceof Modlets )
                {
                    for ( Modlet m : ( (Modlets) content ).getModlet() )
                    {
                        modlets.getModlet().add( m );
                    }
                }
            }

            return modlets.getModlet().isEmpty() ? null : modlets;
        }
        catch ( final JAXBException e )
        {
            if ( e.getLinkedException() != null )
            {
                throw new ModletException( e.getLinkedException().getMessage(), e.getLinkedException() );
            }
            else
            {
                throw new ModletException( e.getMessage(), e );
            }
        }
    }

    /**
     * {@inheritDoc}
     *
     * @see #getModletLocation()
     * @see #findModlets(org.jomc.model.modlet.ModletContext, java.lang.String)
     */
    public Modlets findModlets( final ModletContext context ) throws ModletException
    {
        if ( context == null )
        {
            throw new NullPointerException( "context" );
        }

        return this.findModlets( context, this.getModletLocation() );
    }

}
