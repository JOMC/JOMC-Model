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
import org.jomc.modlet.Model;
import org.jomc.modlet.ModelContext;
import org.jomc.modlet.ModelException;
import org.jomc.modlet.ModelProcessor;

/**
 * Default object management and configuration {@code ModelProcessor} implementation.
 *
 * @author <a href="mailto:schulte2005@users.sourceforge.net">Christian Schulte</a>
 * @version $Id$
 * @see ModelContext#processModel(org.jomc.modlet.Model)
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

    /** Flag indicating the processor is enabled by default. */
    private static volatile Boolean defaultEnabled;

    /** Flag indicating the processor is enabled. */
    private Boolean enabled;

    /** Creates a new {@code DefaultModelProcessor} instance. */
    public DefaultModelProcessor()
    {
        super();
    }

    /**
     * Gets a flag indicating the processor is enabled by default.
     * <p>The default enabled flag is controlled by system property
     * {@code org.jomc.model.DefaultModelProcessor.defaultEnabled} holding a value indicating the processor is enabled
     * by default. If that property is not set, the {@code true} default is returned.</p>
     *
     * @return {@code true} if the processor is enabled by default; {@code false} if the processor is disabled by
     * default.
     *
     * @see #setDefaultEnabled(java.lang.Boolean)
     */
    public static boolean isDefaultEnabled()
    {
        if ( defaultEnabled == null )
        {
            defaultEnabled = Boolean.valueOf( System.getProperty(
                "org.jomc.model.DefaultModelProcessor.defaultEnabled", Boolean.toString( true ) ) );

        }

        return defaultEnabled;
    }

    /**
     * Sets the flag indicating the processor is enabled by default.
     *
     * @param value The new value of the flag indicating the processor is enabled by default or {@code null}.
     *
     * @see #isDefaultEnabled()
     */
    public static void setDefaultEnabled( final Boolean value )
    {
        defaultEnabled = value;
    }

    /**
     * Gets a flag indicating the processor is enabled.
     *
     * @return {@code true} if the processor is enabled; {@code false} if the processor is disabled.
     *
     * @see #isDefaultEnabled()
     * @see #setEnabled(java.lang.Boolean)
     */
    public boolean isEnabled()
    {
        if ( this.enabled == null )
        {
            this.enabled = isDefaultEnabled();
        }

        return this.enabled;
    }

    /**
     * Sets the flag indicating the processor is enabled.
     *
     * @param value The new value of the flag indicating the processor is enabled or {@code null}.
     *
     * @see #isEnabled()
     */
    public void setEnabled( final Boolean value )
    {
        this.enabled = value;
    }

    /**
     * Gets the default location searched for transformer resources.
     * <p>The default transformer location is controlled by system property
     * {@code org.jomc.model.DefaultModelProcessor.defaultTransformerLocation} holding the location to search for
     * transformer resources by default. If that property is not set, the {@code META-INF/jomc.xsl} default is
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
                    context.log( Level.FINE, this.getMessage( "processing", this.getClass().getName(),
                                                              url.toExternalForm() ), null );

                }

                final InputStream in = url.openStream();
                final Transformer transformer = transformerFactory.newTransformer( new StreamSource( in ) );
                in.close();

                transformer.setErrorListener( errorListener );
                transformers.add( transformer );
            }

            if ( context.isLoggable( Level.FINE ) )
            {
                context.log( Level.FINE, this.getMessage( "contextReport", this.getClass().getName(), count, location,
                                                          Long.valueOf( System.currentTimeMillis() - t0 ) ), null );

            }

            return transformers.isEmpty() ? null : transformers;
        }
        catch ( final IOException e )
        {
            throw new ModelException( e.getMessage(), e );
        }
        catch ( final TransformerConfigurationException e )
        {
            throw new ModelException( e.getMessage(), e );
        }
    }

    /**
     * {@inheritDoc}
     *
     * @see #isEnabled()
     * @see #getTransformerLocation()
     * @see #findTransformers(org.jomc.modlet.ModelContext, java.lang.String)
     */
    public Model processModel( final ModelContext context, final Model model ) throws ModelException
    {
        if ( context == null )
        {
            throw new NullPointerException( "context" );
        }
        if ( model == null )
        {
            throw new NullPointerException( "model" );
        }

        try
        {
            Model processed = null;

            if ( this.isEnabled() )
            {
                if ( context.isLoggable( Level.FINE ) )
                {
                    context.log( Level.FINE, getMessage( "processingModel", this.getClass().getName(),
                                                         model.getIdentifier() ), null );

                }

                final org.jomc.modlet.ObjectFactory objectFactory = new org.jomc.modlet.ObjectFactory();
                final JAXBContext jaxbContext = context.createContext( model.getIdentifier() );
                final List<Transformer> transformers = this.findTransformers( context, this.getTransformerLocation() );
                processed = new Model( model );

                if ( transformers != null )
                {
                    for ( Transformer t : transformers )
                    {
                        final JAXBElement<Model> e = objectFactory.createModel( processed );
                        final JAXBSource source = new JAXBSource( jaxbContext, e );
                        final JAXBResult result = new JAXBResult( jaxbContext );
                        t.transform( source, result );
                        processed = ( (JAXBElement<Model>) result.getResult() ).getValue();
                    }
                }
            }
            else if ( context.isLoggable( Level.FINE ) )
            {
                context.log( Level.FINE, getMessage( "disabled", this.getClass().getName(),
                                                     model.getIdentifier() ), null );

            }

            return processed;
        }
        catch ( final TransformerException e )
        {
            throw new ModelException( e.getMessage(), e );
        }
        catch ( final JAXBException e )
        {
            if ( e.getLinkedException() != null )
            {
                throw new ModelException( e.getLinkedException().getMessage(), e.getLinkedException() );
            }
            else
            {
                throw new ModelException( e.getMessage(), e );
            }
        }
    }

    private String getMessage( final String key, final Object... args )
    {
        return MessageFormat.format( ResourceBundle.getBundle(
            DefaultModelProcessor.class.getName().replace( '.', '/' ), Locale.getDefault() ).getString( key ), args );

    }

}
