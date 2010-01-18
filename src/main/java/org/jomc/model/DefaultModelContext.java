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
import java.io.Reader;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.logging.Level;
import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.Source;
import javax.xml.transform.sax.SAXSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import org.jomc.model.bootstrap.BootstrapContext;
import org.jomc.model.bootstrap.BootstrapException;
import org.jomc.model.bootstrap.Schemas;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Default {@code ModelContext} implementation.
 *
 * @author <a href="mailto:cs@jomc.org">Christian Schulte</a>
 * @version $Id$
 * @see ModelContext#createModelContext(java.lang.ClassLoader)
 */
public class DefaultModelContext extends ModelContext
{

    /** Supported schema name extensions. */
    private static final String[] SCHEMA_EXTENSIONS = new String[]
    {
        "xsd"
    };

    /** Cached {@code Schemas}. */
    private Reference<Schemas> cachedSchemas = new SoftReference<Schemas>( null );

    /** Cached schema resources. */
    private Reference<Set<URI>> cachedSchemaResources = new SoftReference<Set<URI>>( null );

    /**
     * Creates a new {@code DefaultModelContext} instance taking a class loader.
     *
     * @param classLoader The class loader of the context.
     */
    public DefaultModelContext( final ClassLoader classLoader )
    {
        super( classLoader );
    }

    @Override
    public EntityResolver createEntityResolver() throws ModelException
    {
        return new DefaultHandler()
        {

            @Override
            public InputSource resolveEntity( final String publicId, final String systemId )
                throws SAXException, IOException
            {
                if ( systemId == null )
                {
                    throw new NullPointerException( "systemId" );
                }

                InputSource schemaSource = null;

                try
                {
                    org.jomc.model.bootstrap.Schema s = null;
                    final Schemas classpathSchemas = getSchemas();

                    if ( publicId != null )
                    {
                        s = classpathSchemas.getSchemaByPublicId( publicId );
                    }
                    if ( s == null )
                    {
                        s = classpathSchemas.getSchemaBySystemId( systemId );
                    }

                    if ( s != null )
                    {
                        schemaSource = new InputSource();
                        schemaSource.setPublicId( s.getPublicId() != null ? s.getPublicId() : publicId );
                        schemaSource.setSystemId( s.getSystemId() );

                        if ( s.getClasspathId() != null )
                        {
                            final URL resource = findResource( s.getClasspathId() );

                            if ( resource != null )
                            {
                                schemaSource.setSystemId( resource.toExternalForm() );
                            }
                            else
                            {
                                if ( isLoggable( Level.WARNING ) )
                                {
                                    log( Level.WARNING, getMessage( "resourceNotFound", new Object[]
                                        {
                                            s.getClasspathId()
                                        } ), null );

                                }
                            }
                        }

                        if ( isLoggable( Level.FINE ) )
                        {
                            log( Level.FINE, getMessage( "resolutionInfo", new Object[]
                                {
                                    publicId + ", " + systemId,
                                    schemaSource.getPublicId() + ", " + schemaSource.getSystemId()
                                } ), null );

                        }
                    }

                    if ( schemaSource == null )
                    {
                        final URI systemUri = new URI( systemId );
                        String schemaName = systemUri.getPath();
                        if ( schemaName != null )
                        {
                            final int lastIndexOfSlash = schemaName.lastIndexOf( '/' );
                            if ( lastIndexOfSlash != -1 && lastIndexOfSlash < schemaName.length() )
                            {
                                schemaName = schemaName.substring( lastIndexOfSlash + 1 );
                            }

                            for ( URI uri : getSchemaResources() )
                            {
                                if ( uri.getPath().endsWith( schemaName ) )
                                {
                                    schemaSource = new InputSource();
                                    schemaSource.setPublicId( publicId );
                                    schemaSource.setSystemId( uri.toASCIIString() );

                                    if ( isLoggable( Level.FINE ) )
                                    {
                                        log( Level.FINE, getMessage( "resolutionInfo", new Object[]
                                            {
                                                systemUri.toASCIIString(),
                                                schemaSource.getSystemId()
                                            } ), null );

                                    }

                                    break;
                                }
                            }
                        }
                        else
                        {
                            if ( isLoggable( Level.WARNING ) )
                            {
                                log( Level.WARNING, getMessage( "unsupportedSystemIdUri", new Object[]
                                    {
                                        systemId, systemUri.toASCIIString()
                                    } ), null );

                            }

                            schemaSource = null;
                        }
                    }
                }
                catch ( final URISyntaxException e )
                {
                    if ( isLoggable( Level.WARNING ) )
                    {
                        log( Level.WARNING, getMessage( "unsupportedSystemIdUri", new Object[]
                            {
                                systemId, e.getMessage()
                            } ), null );

                    }

                    schemaSource = null;
                }
                catch ( final BootstrapException e )
                {
                    throw (IOException) new IOException( getMessage( "failedResolvingSchemas", null ) ).initCause( e );
                }
                catch ( final ModelException e )
                {
                    throw (IOException) new IOException( getMessage( "failedResolving", new Object[]
                        {
                            publicId, systemId, e.getMessage()
                        } ) ).initCause( e );

                }

                return schemaSource;
            }

        };
    }

    @Override
    public LSResourceResolver createResourceResolver() throws ModelException
    {
        return new LSResourceResolver()
        {

            public LSInput resolveResource( final String type, final String namespaceURI, final String publicId,
                                            final String systemId, final String baseURI )
            {
                final String resolvePublicId = namespaceURI == null ? publicId : namespaceURI;
                final String resolveSystemId = systemId == null ? "" : systemId;

                try
                {
                    if ( XMLConstants.W3C_XML_SCHEMA_NS_URI.equals( type ) )
                    {
                        final InputSource schemaSource =
                            createEntityResolver().resolveEntity( resolvePublicId, resolveSystemId );

                        if ( schemaSource != null )
                        {
                            return new LSInput()
                            {

                                public Reader getCharacterStream()
                                {
                                    return schemaSource.getCharacterStream();
                                }

                                public void setCharacterStream( final Reader characterStream )
                                {
                                    log( Level.WARNING, getMessage( "unsupportedOperation", new Object[]
                                        {
                                            "setCharacterStream",
                                            DefaultModelContext.class.getName() + ".LSResourceResolver"
                                        } ), null );

                                }

                                public InputStream getByteStream()
                                {
                                    return schemaSource.getByteStream();
                                }

                                public void setByteStream( final InputStream byteStream )
                                {
                                    log( Level.WARNING, getMessage( "unsupportedOperation", new Object[]
                                        {
                                            "setByteStream",
                                            DefaultModelContext.class.getName() + ".LSResourceResolver"
                                        } ), null );

                                }

                                public String getStringData()
                                {
                                    return null;
                                }

                                public void setStringData( final String stringData )
                                {
                                    log( Level.WARNING, getMessage( "unsupportedOperation", new Object[]
                                        {
                                            "setStringData",
                                            DefaultModelContext.class.getName() + ".LSResourceResolver"
                                        } ), null );

                                }

                                public String getSystemId()
                                {
                                    return schemaSource.getSystemId();
                                }

                                public void setSystemId( final String systemId )
                                {
                                    log( Level.WARNING, getMessage( "unsupportedOperation", new Object[]
                                        {
                                            "setSystemId",
                                            DefaultModelContext.class.getName() + ".LSResourceResolver"
                                        } ), null );

                                }

                                public String getPublicId()
                                {
                                    return schemaSource.getPublicId();
                                }

                                public void setPublicId( final String publicId )
                                {
                                    log( Level.WARNING, getMessage( "unsupportedOperation", new Object[]
                                        {
                                            "setPublicId",
                                            DefaultModelContext.class.getName() + ".LSResourceResolver"
                                        } ), null );

                                }

                                public String getBaseURI()
                                {
                                    return baseURI;
                                }

                                public void setBaseURI( final String baseURI )
                                {
                                    log( Level.WARNING, getMessage( "unsupportedOperation", new Object[]
                                        {
                                            "setBaseURI",
                                            DefaultModelContext.class.getName() + ".LSResourceResolver"
                                        } ), null );

                                }

                                public String getEncoding()
                                {
                                    return schemaSource.getEncoding();
                                }

                                public void setEncoding( final String encoding )
                                {
                                    log( Level.WARNING, getMessage( "unsupportedOperation", new Object[]
                                        {
                                            "setEncoding",
                                            DefaultModelContext.class.getName() + ".LSResourceResolver"
                                        } ), null );

                                }

                                public boolean getCertifiedText()
                                {
                                    return false;
                                }

                                public void setCertifiedText( final boolean certifiedText )
                                {
                                    log( Level.WARNING, getMessage( "unsupportedOperation", new Object[]
                                        {
                                            "setCertifiedText",
                                            DefaultModelContext.class.getName() + ".LSResourceResolver"
                                        } ), null );

                                }

                            };
                        }

                    }
                    else if ( isLoggable( Level.WARNING ) )
                    {
                        log( Level.WARNING, getMessage( "unsupportedResourceType", new Object[]
                            {
                                type
                            } ), null );

                    }
                }
                catch ( final SAXException e )
                {
                    if ( isLoggable( Level.SEVERE ) )
                    {
                        log( Level.SEVERE, getMessage( "failedResolving", new Object[]
                            {
                                resolvePublicId, resolveSystemId, e.getMessage()
                            } ), e );

                    }
                }
                catch ( final IOException e )
                {
                    if ( isLoggable( Level.SEVERE ) )
                    {
                        log( Level.SEVERE, getMessage( "failedResolving", new Object[]
                            {
                                resolvePublicId, resolveSystemId, e.getMessage()
                            } ), e );

                    }
                }
                catch ( final ModelException e )
                {
                    if ( isLoggable( Level.SEVERE ) )
                    {
                        log( Level.SEVERE, getMessage( "failedResolving", new Object[]
                            {
                                resolvePublicId, resolveSystemId, e.getMessage()
                            } ), e );

                    }
                }

                return null;
            }

        };

    }

    @Override
    public Schema createSchema() throws ModelException
    {
        try
        {
            final SchemaFactory f = SchemaFactory.newInstance( XMLConstants.W3C_XML_SCHEMA_NS_URI );
            final Schemas schemas = this.getSchemas();
            final List<Source> sources = new ArrayList<Source>( schemas.getSchema().size() );
            final EntityResolver entityResolver = this.createEntityResolver();

            for ( org.jomc.model.bootstrap.Schema s : schemas.getSchema() )
            {
                final InputSource inputSource = entityResolver.resolveEntity( s.getPublicId(), s.getSystemId() );

                if ( inputSource != null )
                {
                    sources.add( new SAXSource( inputSource ) );
                }
            }

            if ( sources.isEmpty() )
            {
                throw new ModelException( this.getMessage( "missingSchemas", null ) );
            }

            f.setResourceResolver( this.createResourceResolver() );
            return f.newSchema( sources.toArray( new Source[ sources.size() ] ) );
        }
        catch ( final BootstrapException e )
        {
            throw new ModelException( e );
        }
        catch ( final IOException e )
        {
            throw new ModelException( e );
        }
        catch ( final SAXException e )
        {
            throw new ModelException( e );
        }
    }

    @Override
    public JAXBContext createContext() throws ModelException
    {
        try
        {
            final StringBuilder packageNames = new StringBuilder();

            for ( final Iterator<org.jomc.model.bootstrap.Schema> s = this.getSchemas().getSchema().iterator();
                  s.hasNext(); )
            {
                final org.jomc.model.bootstrap.Schema schema = s.next();
                if ( schema.getContextId() != null )
                {
                    packageNames.append( ':' ).append( schema.getContextId() );
                    if ( this.isLoggable( Level.CONFIG ) )
                    {
                        this.log( Level.CONFIG, this.getMessage( "foundContext", new Object[]
                            {
                                schema.getContextId()
                            } ), null );

                    }
                }
            }

            if ( packageNames.length() == 0 )
            {
                throw new ModelException( this.getMessage( "missingSchemas", null ) );
            }

            return JAXBContext.newInstance( packageNames.toString().substring( 1 ), this.getClassLoader() );
        }
        catch ( final BootstrapException e )
        {
            throw new ModelException( e );
        }
        catch ( final JAXBException e )
        {
            throw new ModelException( e );
        }
    }

    @Override
    public Marshaller createMarshaller() throws ModelException
    {
        try
        {
            final StringBuilder packageNames = new StringBuilder();
            final StringBuilder schemaLocation = new StringBuilder();

            for ( final Iterator<org.jomc.model.bootstrap.Schema> s = this.getSchemas().getSchema().iterator();
                  s.hasNext(); )
            {
                final org.jomc.model.bootstrap.Schema schema = s.next();
                if ( schema.getContextId() != null )
                {
                    packageNames.append( ':' ).append( schema.getContextId() );
                }
                if ( schema.getPublicId() != null && schema.getSystemId() != null )
                {
                    schemaLocation.append( ' ' ).append( schema.getPublicId() ).append( ' ' ).
                        append( schema.getSystemId() );

                }
            }

            if ( packageNames.length() == 0 )
            {
                throw new ModelException( this.getMessage( "missingSchemas", null ) );
            }

            final Marshaller m =
                JAXBContext.newInstance( packageNames.toString().substring( 1 ), this.getClassLoader() ).
                createMarshaller();

            if ( schemaLocation.length() != 0 )
            {
                m.setProperty( Marshaller.JAXB_SCHEMA_LOCATION, schemaLocation.toString().substring( 1 ) );
            }

            return m;
        }
        catch ( final BootstrapException e )
        {
            throw new ModelException( e );
        }
        catch ( final JAXBException e )
        {
            throw new ModelException( e );
        }
    }

    @Override
    public Unmarshaller createUnmarshaller() throws ModelException
    {
        try
        {
            return this.createContext().createUnmarshaller();
        }
        catch ( final JAXBException e )
        {
            throw new ModelException( e );
        }
    }

    /**
     * Gets the schemas of the instance.
     *
     * @return The schemas of the instance.
     *
     * @throws BootstrapException if getting the schemas fails.
     */
    private Schemas getSchemas() throws BootstrapException
    {
        Schemas schemas = this.cachedSchemas.get();

        if ( schemas == null )
        {
            schemas = BootstrapContext.createBootstrapContext( this.getClassLoader() ).findSchemas();

            if ( schemas != null && this.isLoggable( Level.CONFIG ) )
            {
                for ( org.jomc.model.bootstrap.Schema s : schemas.getSchema() )
                {
                    this.log( Level.CONFIG, this.getMessage( "foundSchema", new Object[]
                        {
                            s.getPublicId(), s.getSystemId(), s.getContextId(), s.getClasspathId()
                        } ), null );

                }
            }

            this.cachedSchemas = new SoftReference( schemas );
        }

        return schemas;
    }

    /**
     * Searches the context for {@code META-INF/MANIFEST.MF} resources and returns a set of URIs of entries whose name
     * end with a known schema extension.
     *
     * @return Set of URIs of any matching entries.
     *
     * @throws IOException if reading fails.
     * @throws URISyntaxException if parsing fails.
     */
    private Set<URI> getSchemaResources() throws IOException, URISyntaxException
    {
        Set<URI> resources = this.cachedSchemaResources.get();

        if ( resources == null )
        {
            resources = new HashSet<URI>();
            final long t0 = System.currentTimeMillis();
            int count = 0;

            for ( final Enumeration<URL> e = this.getClassLoader().getResources( "META-INF/MANIFEST.MF" );
                  e.hasMoreElements(); )
            {
                count++;
                final URL manifestUrl = e.nextElement();
                final String externalForm = manifestUrl.toExternalForm();
                final String baseUrl = externalForm.substring( 0, externalForm.indexOf( "META-INF" ) );
                final InputStream manifestStream = manifestUrl.openStream();
                final Manifest mf = new Manifest( manifestStream );
                manifestStream.close();

                if ( this.isLoggable( Level.FINE ) )
                {
                    this.log( Level.FINE, this.getMessage( "processing", new Object[]
                        {
                            externalForm
                        } ), null );

                }

                for ( Map.Entry<String, Attributes> entry : mf.getEntries().entrySet() )
                {
                    for ( int i = SCHEMA_EXTENSIONS.length - 1; i >= 0; i-- )
                    {
                        if ( entry.getKey().toLowerCase().endsWith( '.' + SCHEMA_EXTENSIONS[i].toLowerCase() ) )
                        {
                            final URL schemaUrl = new URL( baseUrl + entry.getKey() );
                            resources.add( schemaUrl.toURI() );

                            if ( this.isLoggable( Level.FINE ) )
                            {
                                this.log( Level.FINE, this.getMessage( "foundSchemaCandidate", new Object[]
                                    {
                                        schemaUrl.toExternalForm()
                                    } ), null );

                            }
                        }
                    }
                }
            }

            if ( this.isLoggable( Level.FINE ) )
            {
                this.log( Level.FINE, this.getMessage( "contextReport", new Object[]
                    {
                        count, "META-INF/MANIFEST.MF", Long.valueOf( System.currentTimeMillis() - t0 )
                    } ), null );

            }

            this.cachedSchemaResources = new SoftReference( resources );
        }

        return resources;
    }

    private String getMessage( final String key, final Object args )
    {
        return new MessageFormat( ResourceBundle.getBundle( DefaultModelContext.class.getName().replace( '.', '/' ),
                                                            Locale.getDefault() ).getString( key ) ).format( args );

    }

}
