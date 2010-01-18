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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.MessageFormat;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.logging.Level;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.util.JAXBResult;
import javax.xml.bind.util.JAXBSource;
import javax.xml.transform.ErrorListener;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamSource;

/**
 * Default {@code ModelProcessor} implementation.
 *
 * @author <a href="mailto:cs@jomc.org">Christian Schulte</a>
 * @version $Id$
 * @see ModelContext#processModules(org.jomc.model.Modules)
 */
public class DefaultModelProcessor implements ModelProcessor
{

    /**
     * Classpath location searched for transformers by default.
     * @see #getDefaultTransformerLocation()
     */
    private static final String DEFAULT_TRANSFORMER_LOCATION = "META-INF/jomc.xsl";

    /** Default transformer location. */
    private static volatile String defaultTransformerLocation;

    /** Transformer location of the instance. */
    private String transformerLocation;

    /** Creates a new {@code DefaultModelProcessor} instance. */
    public DefaultModelProcessor()
    {
        super();
    }

    /**
     * Gets the default location searched for transformer resources.
     * <p>The default transformer location is controlled by system property
     * {@code org.jomc.model.DefaultModelProcessor.defaultTransformerLocation} holding the location to search for
     * transformer resources by default. If that property is not set, the {@code META-INF/jomc.xslt} default is
     * returned.</p>
     *
     * @return The location searched for transformer resources by default.
     *
     * @see #setDefaultTransformerLocation(java.lang.String)
     */
    public static String getDefaultTransformerLocation()
    {
        if ( defaultTransformerLocation == null )
        {
            defaultTransformerLocation = System.getProperty(
                "org.jomc.model.DefaultModelProcessor.defaultTransformerLocation", DEFAULT_TRANSFORMER_LOCATION );

        }

        return defaultTransformerLocation;
    }

    /**
     * Sets the default location searched for transformer resources.
     *
     * @param value The new default location to search for transformer resources or {@code null}.
     *
     * @see #getDefaultTransformerLocation()
     */
    public static void setDefaultTransformerLocation( final String value )
    {
        defaultTransformerLocation = value;
    }

    /**
     * Gets the location searched for transformer resources.
     *
     * @return The location searched for transformer resources.
     *
     * @see #getDefaultTransformerLocation()
     * @see #setTransformerLocation(java.lang.String)
     */
    public String getTransformerLocation()
    {
        if ( this.transformerLocation == null )
        {
            this.transformerLocation = getDefaultTransformerLocation();
        }

        return this.transformerLocation;
    }

    /**
     * Sets the location searched for transformer resources.
     *
     * @param value The new location to search for transformer resources or {@code null}.
     *
     * @see #getTransformerLocation()
     */
    public void setTransformerLocation( final String value )
    {
        this.transformerLocation = value;
    }

    /**
     * Searches a given context for transformers.
     *
     * @param context The context to search for transformers.
     * @param location The location to search at.
     *
     * @return The transformers found at {@code location} in {@code context} or {@code null} of no transformers are
     * found.
     *
     * @throws NullPointerException if {@code context} or {@code location} is {@code null}.
     * @throws ModelException if getting the transformers fails.
     */
    public List<Transformer> findTransformers( final ModelContext context, final String location ) throws ModelException
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
            final List<Transformer> transformers = new LinkedList<Transformer>();
            final TransformerFactory transformerFactory = TransformerFactory.newInstance();
            final Enumeration<URL> resources = context.findResources( location );
            final ErrorListener errorListener = new ErrorListener()
            {

                public void warning( final TransformerException exception ) throws TransformerException
                {
                    if ( context.isLoggable( Level.WARNING ) )
                    {
                        context.log( Level.WARNING, exception.getMessage(), exception );
                    }
                }

                public void error( final TransformerException exception ) throws TransformerException
                {
                    if ( context.isLoggable( Level.SEVERE ) )
                    {
                        context.log( Level.SEVERE, exception.getMessage(), exception );
                    }

                    throw exception;
                }

                public void fatalError( final TransformerException exception ) throws TransformerException
                {
                    if ( context.isLoggable( Level.SEVERE ) )
                    {
                        context.log( Level.SEVERE, exception.getMessage(), exception );
                    }

                    throw exception;
                }

            };

            transformerFactory.setErrorListener( errorListener );

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

                final InputStream in = url.openStream();
                final Transformer transformer = transformerFactory.newTransformer( new StreamSource( in ) );
                in.close();

                transformer.setErrorListener( errorListener );
                transformers.add( transformer );
            }

            if ( context.isLoggable( Level.FINE ) )
            {
                context.log( Level.FINE, this.getMessage( "contextReport", new Object[]
                    {
                        count, location, Long.valueOf( System.currentTimeMillis() - t0 )
                    } ), null );

            }

            return transformers.isEmpty() ? null : transformers;
        }
        catch ( final IOException e )
        {
            throw new ModelException( e );
        }
        catch ( final TransformerConfigurationException e )
        {
            throw new ModelException( e );
        }
    }

    /**
     * {@inheritDoc}
     *
     * @see #getTransformerLocation()
     * @see #findTransformers(org.jomc.model.ModelContext, java.lang.String)
     */
    public Modules processModules( final ModelContext context, final Modules modules ) throws ModelException
    {
        if ( context == null )
        {
            throw new NullPointerException( "context" );
        }
        if ( modules == null )
        {
            throw new NullPointerException( "modules" );
        }

        try
        {
            final ObjectFactory objectFactory = new ObjectFactory();
            final JAXBContext jaxbContext = context.createContext();
            final List<Transformer> transformers = this.findTransformers( context, this.getTransformerLocation() );
            Modules processed = new Modules( modules );

            if ( transformers != null )
            {
                for ( Transformer t : transformers )
                {
                    final JAXBElement<Modules> e = objectFactory.createModules( processed );
                    final JAXBSource source = new JAXBSource( jaxbContext, e );
                    final JAXBResult result = new JAXBResult( jaxbContext );
                    t.transform( source, result );
                    processed = ( (JAXBElement<Modules>) result.getResult() ).getValue();
                }
            }

            return processed;
        }
        catch ( final TransformerException e )
        {
            throw new ModelException( e );
        }
        catch ( final JAXBException e )
        {
            throw new ModelException( e );
        }
    }

    private String getMessage( final String key, final Object args )
    {
        return new MessageFormat(
            ResourceBundle.getBundle( DefaultModelProcessor.class.getName().replace( '.', '/' ), Locale.getDefault() ).
            getString( key ) ).format( args );

    }

}
