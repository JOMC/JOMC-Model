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

import java.net.URL;
import java.text.MessageFormat;
import java.util.Enumeration;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.logging.Level;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

/**
 * Default {@code ModelProvider} implementation.
 *
 * @author <a href="mailto:cs@jomc.org">Christian Schulte</a>
 * @version $Id$
 * @see ModelContext#findModules()
 */
public class DefaultModelProvider implements ModelProvider
{

    /**
     * Classpath location searched for modules by default.
     * @see #getDefaultModuleLocation()
     */
    private static final String DEFAULT_MODULE_LOCATION = "META-INF/jomc.xml";

    /** Default module location. */
    private static volatile String defaultModuleLocation;

    /** Creates a new {@code DefaultModelProvider} instance. */
    public DefaultModelProvider()
    {
        super();
    }

    /**
     * Gets the default location searched for module resources.
     * <p>The default module location is controlled by system property
     * {@code org.jomc.model.DefaultModelProvider.defaultModuleLocation} holding the location to search for module
     * resources by default. If that property is not set, the {@code META-INF/jomc.xml} default is returned.</p>
     *
     * @return The location searched for module resources by default.
     *
     * @see #setDefaultModuleLocation(java.lang.String)
     */
    public static String getDefaultModuleLocation()
    {
        if ( defaultModuleLocation == null )
        {
            defaultModuleLocation = System.getProperty( "org.jomc.model.DefaultModelProvider.defaultModuleLocation",
                                                        DEFAULT_MODULE_LOCATION );

        }

        return defaultModuleLocation;
    }

    /**
     * Sets the default location searched for module resources.
     *
     * @param value The new default location to search for module resources or {@code null}.
     *
     * @see #getDefaultModuleLocation()
     */
    public static void setDefaultModuleLocation( final String value )
    {
        defaultModuleLocation = value;
    }

    /**
     * Searches a given context for modules.
     *
     * @param context The context to search for modules.
     * @param location The location to search at.
     *
     * @return The modules found at {@code location} in {@code context} or {@code null} of no modules are found.
     *
     * @throws NullPointerException if {@code context} or {@code location} is {@code null}.
     * @throws ModelException if searching the context fails.
     *
     * @see #getDefaultModuleLocation()
     */
    public Modules findModules( final ModelContext context, final String location ) throws ModelException
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
            final long t0 = System.currentTimeMillis();
            final Text text = new Text();
            text.setLanguage( "en" );
            text.setValue( this.getMessage( "contextModulesInfo", new Object[]
                {
                    location
                } ) );

            final Modules modules = new Modules();
            modules.setDocumentation( new Texts() );
            modules.getDocumentation().setDefaultLanguage( "en" );
            modules.getDocumentation().getText().add( text );

            final Unmarshaller u = context.createUnmarshaller();
            final Enumeration<URL> resources = context.findResources( location );

            int count = 0;
            while ( resources.hasMoreElements() )
            {
                count++;
                final URL url = resources.nextElement();

                if ( context.isLoggable( Level.FINE ) )
                {
                    context.log( Level.FINE, this.getMessage( "processing", new Object[]
                        {
                            url.toExternalForm()
                        } ), null );

                }

                Object content = u.unmarshal( url );
                if ( content instanceof JAXBElement )
                {
                    content = ( (JAXBElement) content ).getValue();
                }

                if ( content instanceof Module )
                {
                    final Module m = (Module) content;
                    if ( context.isLoggable( Level.FINE ) )
                    {
                        context.log( Level.FINE, this.getMessage( "foundModule", new Object[]
                            {
                                m.getName(), m.getVersion() == null ? "" : m.getVersion()
                            } ), null );

                    }

                    modules.getModule().add( m );
                }
                else if ( context.isLoggable( Level.WARNING ) )
                {
                    context.log( Level.WARNING, this.getMessage( "ignoringDocument", new Object[]
                        {
                            content == null ? "<>" : content.toString(), url.toExternalForm()
                        } ), null );

                }
            }

            if ( context.isLoggable( Level.FINE ) )
            {
                context.log( Level.FINE, this.getMessage( "contextReport", new Object[]
                    {
                        count, location, Long.valueOf( System.currentTimeMillis() - t0 )
                    } ), null );

            }

            return modules.getModule().isEmpty() ? null : modules;
        }
        catch ( final JAXBException e )
        {
            throw new ModelException( e );
        }
    }

    /**
     * {@inheritDoc}
     *
     * @see #findModules(org.jomc.model.ModelContext, java.lang.String)
     */
    public Modules findModules( final ModelContext context ) throws ModelException
    {
        if ( context == null )
        {
            throw new NullPointerException( "context" );
        }

        return this.findModules( context, getDefaultModuleLocation() );
    }

    private String getMessage( final String key, final Object args )
    {
        return new MessageFormat(
            ResourceBundle.getBundle( DefaultModelProvider.class.getName().replace( '.', '/' ), Locale.getDefault() ).
            getString( key ) ).format( args );

    }

}
