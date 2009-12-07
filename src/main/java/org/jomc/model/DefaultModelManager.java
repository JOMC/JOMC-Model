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
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
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
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.ErrorListener;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.URIResolver;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.SchemaFactory;
import org.jomc.model.bootstrap.Schema;
import org.jomc.model.bootstrap.Schemas;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Default {@code ModelManager} implementation.
 *
 * <p><b>Schema management</b><ul>
 * <li>{@link #getBootstrapSchema() }</li>
 * <li>{@link #getBootstrapContext() }</li>
 * </ul></p>
 *
 * <p><b>Resource management</b><ul>
 * <li>{@link #getClasspathModules(java.lang.ClassLoader, java.lang.String) }</li>
 * <li>{@link #getClasspathSchemas(java.lang.ClassLoader, java.lang.String) }</li>
 * <li>{@link #getClasspathTransformers(java.lang.ClassLoader, java.lang.String) }</li>
 * </ul></p>
 *
 * <p><b>Log management</b><ul>
 * <li>{@link #getLogLevel() }</li>
 * <li>{@link #getListeners() }</li>
 * <li>{@link #log(java.util.logging.Level, java.lang.String, java.lang.Throwable) }</li>
 * </ul></p>
 *
 * @author <a href="mailto:cs@jomc.org">Christian Schulte</a>
 * @version $Id$
 */
public class DefaultModelManager implements ModelManager
{
    // SECTION-START[ModelManager]

    public EntityResolver getEntityResolver( final ClassLoader classLoader )
    {
        if ( classLoader == null )
        {
            throw new NullPointerException( "classLoader" );
        }

        return new EntityResolver()
        {

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
                    Schema s = null;
                    final Schemas classpathSchemas = getClasspathSchemas( classLoader, getDefaultSchemaLocation() );

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
                            final URL resource = classLoader.getResource( s.getClasspathId() );

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
                                    publicId + ":" + systemId,
                                    schemaSource.getPublicId() + ":" + schemaSource.getSystemId()
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

                            for ( URI uri : getSchemaResources( classLoader ) )
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
                catch ( final JAXBException e )
                {
                    throw (IOException) new IOException( e.getMessage() ).initCause( e );
                }

                return schemaSource;
            }

        };
    }

    public LSResourceResolver getResourceResolver( final ClassLoader classLoader )
    {
        if ( classLoader == null )
        {
            throw new NullPointerException( "classLoader" );
        }

        return new LSResourceResolver()
        {

            public LSInput resolveResource( final String type, final String namespaceURI, final String publicId,
                                            final String systemId, final String baseURI )
            {
                if ( XMLConstants.W3C_XML_SCHEMA_NS_URI.equals( type ) )
                {
                    try
                    {
                        final InputSource schemaSource = getEntityResolver( classLoader ).resolveEntity(
                            namespaceURI == null ? publicId : namespaceURI, systemId == null ? "" : systemId );

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
                                    throw new UnsupportedOperationException();
                                }

                                public InputStream getByteStream()
                                {
                                    return schemaSource.getByteStream();
                                }

                                public void setByteStream( final InputStream byteStream )
                                {
                                    throw new UnsupportedOperationException();
                                }

                                public String getStringData()
                                {
                                    return null;
                                }

                                public void setStringData( final String stringData )
                                {
                                    throw new UnsupportedOperationException();
                                }

                                public String getSystemId()
                                {
                                    return schemaSource.getSystemId();
                                }

                                public void setSystemId( final String systemId )
                                {
                                    throw new UnsupportedOperationException();
                                }

                                public String getPublicId()
                                {
                                    return schemaSource.getPublicId();
                                }

                                public void setPublicId( final String publicId )
                                {
                                    throw new UnsupportedOperationException();
                                }

                                public String getBaseURI()
                                {
                                    return baseURI;
                                }

                                public void setBaseURI( final String baseURI )
                                {
                                    throw new UnsupportedOperationException();
                                }

                                public String getEncoding()
                                {
                                    return schemaSource.getEncoding();
                                }

                                public void setEncoding( final String encoding )
                                {
                                    throw new UnsupportedOperationException();
                                }

                                public boolean getCertifiedText()
                                {
                                    return false;
                                }

                                public void setCertifiedText( final boolean certifiedText )
                                {
                                    throw new UnsupportedOperationException();
                                }

                            };
                        }

                    }
                    catch ( final SAXException e )
                    {
                        if ( isLoggable( Level.WARNING ) )
                        {
                            log( Level.WARNING, getMessage( "unsupportedSystemIdUri", new Object[]
                                {
                                    systemId, e.getMessage()
                                } ), null );

                        }
                    }
                    catch ( final IOException e )
                    {
                        if ( isLoggable( Level.WARNING ) )
                        {
                            log( Level.WARNING, getMessage( "unsupportedSystemIdUri", new Object[]
                                {
                                    systemId, e.getMessage()
                                } ), null );

                        }
                    }
                }
                else if ( isLoggable( Level.WARNING ) )
                {
                    log( Level.WARNING, getMessage( "unsupportedResourceType", new Object[]
                        {
                            type
                        } ), null );

                }

                return null;
            }

        };
    }

    public javax.xml.validation.Schema getSchema( final ClassLoader classLoader )
        throws IOException, SAXException, JAXBException
    {
        if ( classLoader == null )
        {
            throw new NullPointerException( "classLoader" );
        }

        final SchemaFactory f = SchemaFactory.newInstance( XMLConstants.W3C_XML_SCHEMA_NS_URI );
        final Schemas schemas = this.getClasspathSchemas( classLoader, getDefaultSchemaLocation() );
        final List<Source> sources = new ArrayList<Source>( schemas.getSchema().size() );
        final EntityResolver entityResolver = this.getEntityResolver( classLoader );

        for ( Schema s : schemas.getSchema() )
        {
            final InputSource inputSource = entityResolver.resolveEntity( s.getPublicId(), s.getSystemId() );

            if ( inputSource != null )
            {
                sources.add( new SAXSource( inputSource ) );
            }
        }

        return f.newSchema( sources.toArray( new Source[ sources.size() ] ) );
    }

    public JAXBContext getContext( final ClassLoader classLoader ) throws IOException, SAXException, JAXBException
    {
        if ( classLoader == null )
        {
            throw new NullPointerException( "classLoader" );
        }

        final StringBuilder packageNames = new StringBuilder();

        for ( final Iterator<Schema> s = this.getClasspathSchemas( classLoader, getDefaultSchemaLocation() ).
            getSchema().iterator(); s.hasNext(); )
        {
            final Schema schema = s.next();
            if ( schema.getContextId() != null )
            {
                packageNames.append( ':' ).append( schema.getContextId() );
                if ( this.isLoggable( Level.FINE ) )
                {
                    this.log( Level.FINE, this.getMessage( "addingContext", new Object[]
                        {
                            schema.getContextId()
                        } ), null );

                }
            }
        }

        if ( packageNames.length() == 0 )
        {
            throw new IOException( this.getMessage( "missingSchemas", new Object[]
                {
                    getDefaultSchemaLocation()
                } ) );

        }

        return JAXBContext.newInstance( packageNames.toString().substring( 1 ), classLoader );
    }

    public Marshaller getMarshaller( final ClassLoader classLoader ) throws IOException, SAXException, JAXBException
    {
        if ( classLoader == null )
        {
            throw new NullPointerException( "classLoader" );
        }

        final StringBuilder packageNames = new StringBuilder();
        final StringBuilder schemaLocation = new StringBuilder();

        for ( final Iterator<Schema> s = this.getClasspathSchemas( classLoader, getDefaultSchemaLocation() ).
            getSchema().iterator(); s.hasNext(); )
        {
            final Schema schema = s.next();
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
            throw new IOException( this.getMessage( "missingSchemas", new Object[]
                {
                    getDefaultSchemaLocation()
                } ) );

        }

        final JAXBContext context = JAXBContext.newInstance( packageNames.toString().substring( 1 ), classLoader );
        final Marshaller m = context.createMarshaller();

        if ( schemaLocation.length() != 0 )
        {
            m.setProperty( Marshaller.JAXB_SCHEMA_LOCATION, schemaLocation.toString().substring( 1 ) );
        }

        return m;
    }

    public Unmarshaller getUnmarshaller( final ClassLoader classLoader ) throws IOException, SAXException, JAXBException
    {
        if ( classLoader == null )
        {
            throw new NullPointerException( "classLoader" );
        }

        return this.getContext( classLoader ).createUnmarshaller();
    }

    // SECTION-END
    // SECTION-START[DefaultModelManager]
    /** Listener interface. */
    public interface Listener
    {

        /**
         * Get called on logging.
         *
         * @param level The level of the event.
         * @param message The message of the event or {@code null}.
         * @param t The throwable of the event or {@code null}.
         *
         * @throws NullPointerException if {@code level} is {@code null}.
         */
        void onLog( Level level, String message, Throwable t );

    }

    /**
     * Log level events are logged at by default.
     * @see #getDefaultLogLevel()
     */
    private static final Level DEFAULT_LOG_LEVEL = Level.WARNING;

    /**
     * Classpath location searched for modules by default.
     * @see #getDefaultModuleLocation()
     */
    private static final String DEFAULT_MODULE_LOCATION = "META-INF/jomc.xml";

    /**
     * Classpath location searched for transformers by default.
     * @see #getDefaultTransformerLocation()
     */
    private static final String DEFAULT_TRANSFORMER_LOCATION = "META-INF/jomc.xslt";

    /** Classpath location of the bootstrap schema. */
    private static final String BOOTSTRAP_SCHEMA_LOCATION =
        Schemas.class.getPackage().getName().replace( '.', '/' ) + "/jomc-bootstrap-1.0.xsd";

    /** Value for property {@link Marshaller#JAXB_SCHEMA_LOCATION} of any bootstrap JAXB marshaller instance. */
    private static final String BOOTSTRAP_JAXB_SCHEMA_LOCATION =
        "http://jomc.org/model/bootstrap http://jomc.org/model/bootstrap/jomc-bootstrap-1.0.xsd";

    /**
     * Classpath location searched for schemas by default.
     * @see #getDefaultSchemaLocation()
     */
    private static final String DEFAULT_SCHEMA_LOCATION = "META-INF/jomc-bootstrap.xml";

    /** JAXB context of the bootstrap schema. */
    private static final String BOOTSTRAP_CONTEXT = Schemas.class.getPackage().getName();

    /** Supported schema name extensions. */
    private static final String[] SCHEMA_EXTENSIONS = new String[]
    {
        "xsd"
    };

    /** Default log level. */
    private static volatile Level defaultLogLevel;

    /** Default module location. */
    private static volatile String defaultModuleLocation;

    /** Default schema location. */
    private static volatile String defaultSchemaLocation;

    /** Default transformer location. */
    private static volatile String defaultTransformerLocation;

    /** The listeners of the instance. */
    private List<Listener> listeners;

    /** Log level of the instance. */
    private Level logLevel;

    /** Creates a new {@code DefaultModelManager} instance. */
    public DefaultModelManager()
    {
        super();
    }

    /**
     * Gets the list of registered listeners.
     * <p>This accessor method returns a reference to the live list, not a snapshot. Therefore any modification you make
     * to the returned list will be present inside the object. This is why there is no {@code set} method for the
     * listeners property.</p>
     *
     * @return The list of registered listeners.
     *
     * @see #log(java.util.logging.Level, java.lang.String, java.lang.Throwable)
     */
    public List<Listener> getListeners()
    {
        if ( this.listeners == null )
        {
            this.listeners = new LinkedList<Listener>();
        }

        return this.listeners;
    }

    /**
     * Gets the default log level events are logged at.
     * <p>The default log level is controlled by system property
     * {@code org.jomc.model.DefaultModelManager.defaultLogLevel} holding the log level to log events at by default.
     * If that property is not set, the {@code WARNING} default is returned.</p>
     *
     * @return The log level events are logged at by default.
     *
     * @see #getLogLevel()
     * @see Level#parse(java.lang.String)
     */
    public static Level getDefaultLogLevel()
    {
        if ( defaultLogLevel == null )
        {
            defaultLogLevel = Level.parse( System.getProperty( "org.jomc.model.DefaultModelManager.defaultLogLevel",
                                                               DEFAULT_LOG_LEVEL.getName() ) );

        }

        return defaultLogLevel;
    }

    /**
     * Sets the default log level events are logged at.
     *
     * @param value The new default level events are logged at or {@code null}.
     *
     * @see #getDefaultLogLevel()
     */
    public static void setDefaultLogLevel( final Level value )
    {
        defaultLogLevel = value;
    }

    /**
     * Gets the log level of the instance.
     *
     * @return The log level of the instance.
     *
     * @see #getDefaultLogLevel()
     * @see #setLogLevel(java.util.logging.Level)
     * @see #isLoggable(java.util.logging.Level)
     */
    public Level getLogLevel()
    {
        if ( this.logLevel == null )
        {
            this.logLevel = getDefaultLogLevel();
            this.log( Level.CONFIG, this.getMessage( "defaultLogLevelInfo", new Object[]
                {
                    this.getClass().getCanonicalName(), this.logLevel.getLocalizedName()
                } ), null );

        }

        return this.logLevel;
    }

    /**
     * Sets the log level of the instance.
     *
     * @param value The new log level of the instance or {@code null}.
     *
     * @see #getLogLevel()
     * @see #isLoggable(java.util.logging.Level)
     */
    public void setLogLevel( final Level value )
    {
        this.logLevel = value;
    }

    /**
     * Checks if a message at a given level is provided to the listeners of the instance.
     *
     * @param level The level to test.
     *
     * @return {@code true} if messages at {@code level} are provided to the listeners of the instance;
     * {@code false} if messages at {@code level} are not provided to the listeners of the instance.
     *
     * @throws NullPointerException if {@code level} is {@code null}.
     *
     * @see #getLogLevel()
     * @see #setLogLevel(java.util.logging.Level)
     */
    public boolean isLoggable( final Level level )
    {
        if ( level == null )
        {
            throw new NullPointerException( "level" );
        }

        return level.intValue() >= this.getLogLevel().intValue();
    }

    /**
     * Notifies registered listeners.
     *
     * @param level The level of the event.
     * @param message The message of the event or {@code null}.
     * @param throwable The throwable of the event {@code null}.
     *
     * @throws NullPointerException if {@code level} is {@code null}.
     *
     * @see #getListeners()
     * @see #isLoggable(java.util.logging.Level)
     */
    protected void log( final Level level, final String message, final Throwable throwable )
    {
        if ( level == null )
        {
            throw new NullPointerException( "level" );
        }

        if ( this.isLoggable( level ) )
        {
            for ( Listener l : this.getListeners() )
            {
                l.onLog( level, message, throwable );
            }
        }
    }

    /**
     * Gets a new bootstrap JAXB context instance.
     *
     * @return A new bootstrap JAXB context instance.
     *
     * @throws JAXBException if creating a new bootstrap JAXB context instance fails.
     */
    public JAXBContext getBootstrapContext() throws JAXBException
    {
        return JAXBContext.newInstance( BOOTSTRAP_CONTEXT );
    }

    /**
     * Gets a new bootstrap JAXB marshaller instance.
     *
     * @return A new bootstrap JAXB marshaller instance.
     *
     * @throws JAXBException if creating a new bootstrap JAXB marshaller instance fails.
     */
    public Marshaller getBootstrapMarshaller() throws JAXBException
    {
        final Marshaller m = this.getBootstrapContext().createMarshaller();
        m.setProperty( Marshaller.JAXB_SCHEMA_LOCATION, BOOTSTRAP_JAXB_SCHEMA_LOCATION );
        return m;
    }

    /**
     * Gets a new bootstrap JAXB unmarshaller instance.
     *
     * @return A new bootstrap JAXB unmarshaller instance.
     *
     * @throws JAXBException if creating a new bootstrap JAXB unmarshaller instance fails.
     */
    public Unmarshaller getBootstrapUnmarshaller() throws JAXBException
    {
        return this.getBootstrapContext().createUnmarshaller();
    }

    /**
     * Gets a new bootstrap JAXP schema instance.
     *
     * @return A new bootstrap JAXP schema instance.
     *
     * @throws SAXException if parsing the bootstrap schema fails.
     */
    public javax.xml.validation.Schema getBootstrapSchema() throws SAXException
    {
        final ClassLoader classLoader = this.getClass().getClassLoader();
        return SchemaFactory.newInstance( XMLConstants.W3C_XML_SCHEMA_NS_URI ).newSchema(
            classLoader != null
            ? classLoader.getResource( BOOTSTRAP_SCHEMA_LOCATION )
            : ClassLoader.getSystemResource( BOOTSTRAP_SCHEMA_LOCATION ) );

    }

    /**
     * Gets the default location searched for schema resources.
     * <p>The default schema location is controlled by system property
     * {@code org.jomc.model.DefaultModelManager.defaultSchemaLocation} holding the location to search for schema
     * resources by default. If that property is not set, the {@code META-INF/jomc-bootstrap.xml} default is returned.
     * </p>
     *
     * @return The location searched for schema resources by default.
     *
     * @see #getClasspathSchemas(java.lang.ClassLoader, java.lang.String)
     */
    public static String getDefaultSchemaLocation()
    {
        if ( defaultSchemaLocation == null )
        {
            defaultSchemaLocation = System.getProperty( "org.jomc.model.DefaultModelManager.defaultSchemaLocation",
                                                        DEFAULT_SCHEMA_LOCATION );

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
     * Gets schemas by searching a given class loader for resources.
     *
     * @param classLoader The class loader to search for resources.
     * @param location The location to search at.
     *
     * @return All schemas found at {@code location} by querying {@code classLoader}.
     *
     * @throws NullPointerException if {@code classLoader} or {@code location} is {@code null}.
     * @throws IOException if reading resources fails.
     * @throws SAXException if parsing schema resources fails.
     * @throws JAXBException if unmarshalling schema resources fails.
     *
     * @see #getDefaultSchemaLocation()
     */
    public Schemas getClasspathSchemas( final ClassLoader classLoader, final String location )
        throws IOException, JAXBException, SAXException
    {
        if ( classLoader == null )
        {
            throw new NullPointerException( "classLoader" );
        }
        if ( location == null )
        {
            throw new NullPointerException( "location" );
        }

        final long t0 = System.currentTimeMillis();
        final Schemas schemas = new Schemas();
        final JAXBContext ctx = JAXBContext.newInstance( BOOTSTRAP_CONTEXT );
        final Unmarshaller u = ctx.createUnmarshaller();
        final Enumeration<URL> e = classLoader.getResources( location );
        u.setSchema( this.getBootstrapSchema() );
        int count = 0;

        while ( e.hasMoreElements() )
        {
            count++;
            final URL url = e.nextElement();

            if ( this.isLoggable( Level.FINE ) )
            {
                this.log( Level.FINE, this.getMessage( "processing", new Object[]
                    {
                        url.toExternalForm()
                    } ), null );

            }

            Object content = u.unmarshal( url );
            if ( content instanceof JAXBElement )
            {
                content = ( (JAXBElement) content ).getValue();
            }

            if ( content instanceof Schema )
            {
                final Schema s = (Schema) content;
                if ( this.isLoggable( Level.FINE ) )
                {
                    this.log( Level.FINE, this.getMessage( "addingSchema", new Object[]
                        {
                            s.getPublicId(), s.getSystemId(), s.getContextId(), s.getClasspathId()
                        } ), null );

                }

                schemas.getSchema().add( s );
            }
            else if ( content instanceof Schemas )
            {
                for ( Schema s : ( (Schemas) content ).getSchema() )
                {
                    if ( this.isLoggable( Level.FINE ) )
                    {
                        this.log( Level.FINE, this.getMessage( "addingSchema", new Object[]
                            {
                                s.getPublicId(), s.getSystemId(), s.getContextId(), s.getClasspathId()
                            } ), null );

                    }

                    schemas.getSchema().add( s );
                }
            }
        }

        if ( this.isLoggable( Level.FINE ) )
        {
            this.log( Level.FINE, this.getMessage( "classpathReport", new Object[]
                {
                    count, location, Long.valueOf( System.currentTimeMillis() - t0 )
                } ), null );

        }

        return schemas;
    }

    /**
     * Gets the default location searched for module resources.
     * <p>The default module location is controlled by system property
     * {@code org.jomc.model.DefaultModelManager.defaultModuleLocation} holding the location to search for module
     * resources by default. If that property is not set, the {@code META-INF/jomc.xml} default is returned.</p>
     *
     * @return The location searched for module resources by default.
     *
     * @see #getClasspathModules(java.lang.ClassLoader, java.lang.String)
     */
    public static String getDefaultModuleLocation()
    {
        if ( defaultModuleLocation == null )
        {
            defaultModuleLocation = System.getProperty( "org.jomc.model.DefaultModelManager.defaultModuleLocation",
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
     * Gets modules by searching a given class loader for resources.
     * <p><b>Note:</b><br/>
     * This method does not validate the modules.</p>
     *
     * @param classLoader The class loader to search for resources.
     * @param location The location to search at.
     *
     * @return All modules found at {@code location} by querying {@code classLoader}.
     *
     * @throws NullPointerException if {@code classLoader} or {@code location} is {@code null}.
     * @throws IOException if reading resources fails.
     * @throws SAXException if parsing schema resources fails.
     * @throws JAXBException if unmarshalling schema resources fails.
     *
     * @see #getDefaultModuleLocation()
     * @see ModelObjectValidator
     */
    public Modules getClasspathModules( final ClassLoader classLoader, final String location )
        throws IOException, SAXException, JAXBException
    {
        if ( classLoader == null )
        {
            throw new NullPointerException( "classLoader" );
        }
        if ( location == null )
        {
            throw new NullPointerException( "location" );
        }

        final long t0 = System.currentTimeMillis();
        final Text text = new Text();
        text.setLanguage( "en" );
        text.setValue( this.getMessage( "classpathModulesInfo", new Object[]
            {
                location
            } ) );

        final Modules modules = new Modules();
        modules.setDocumentation( new Texts() );
        modules.getDocumentation().setDefaultLanguage( "en" );
        modules.getDocumentation().getText().add( text );

        final Unmarshaller u = this.getUnmarshaller( classLoader );
        final Enumeration<URL> resources = classLoader.getResources( location );

        int count = 0;
        while ( resources.hasMoreElements() )
        {
            count++;
            final URL url = resources.nextElement();

            if ( this.isLoggable( Level.FINE ) )
            {
                this.log( Level.FINE, this.getMessage( "processing", new Object[]
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
                if ( this.isLoggable( Level.FINE ) )
                {
                    this.log( Level.FINE, this.getMessage( "addingModule", new Object[]
                        {
                            m.getName(), m.getVersion() == null ? "" : m.getVersion()
                        } ), null );

                }

                modules.getModule().add( m );
            }
            else if ( this.isLoggable( Level.WARNING ) )
            {
                this.log( Level.WARNING, this.getMessage( "ignoringDocument", new Object[]
                    {
                        content == null ? "<>" : content.toString(), url.toExternalForm()
                    } ), null );

            }
        }

        if ( this.isLoggable( Level.FINE ) )
        {
            this.log( Level.FINE, this.getMessage( "classpathReport", new Object[]
                {
                    count, location, Long.valueOf( System.currentTimeMillis() - t0 )
                } ), null );

        }

        return modules;
    }

    /**
     * Gets the default location searched for transformer resources.
     * <p>The default transformer location is controlled by system property
     * {@code org.jomc.model.DefaultModelManager.defaultTransformerLocation} holding the location to search for
     * transformer resources by default. If that property is not set, the {@code META-INF/jomc.xslt} default is
     * returned.</p>
     *
     * @return The location searched for transformer resources by default.
     *
     * @see #getClasspathTransformers(java.lang.ClassLoader, java.lang.String)
     */
    public static String getDefaultTransformerLocation()
    {
        if ( defaultTransformerLocation == null )
        {
            defaultTransformerLocation =
                System.getProperty( "org.jomc.model.DefaultModelManager.defaultTransformerLocation",
                                    DEFAULT_TRANSFORMER_LOCATION );

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
     * Gets transformers by searching a given class loader for resources.
     *
     * @param classLoader The class loader to search for resources.
     * @param location The location to search at.
     *
     * @return All transformers found at {@code location} by querying {@code classLoader}.
     *
     * @throws NullPointerException if {@code classLoader} or {@code location} is {@code null}.
     * @throws IOException if reading resources fails.
     * @throws TransformerConfigurationException if getting the transformers fails.
     *
     * @see #getDefaultTransformerLocation()
     */
    public List<Transformer> getClasspathTransformers( final ClassLoader classLoader, final String location )
        throws IOException, TransformerConfigurationException
    {
        if ( classLoader == null )
        {
            throw new NullPointerException( "classLoader" );
        }
        if ( location == null )
        {
            throw new NullPointerException( "location" );
        }

        final long t0 = System.currentTimeMillis();
        final List<Transformer> transformers = new LinkedList<Transformer>();
        final TransformerFactory transformerFactory = TransformerFactory.newInstance();
        final Enumeration<URL> resources = classLoader.getResources( location );
        final ErrorListener errorListener = new ErrorListener()
        {

            public void warning( final TransformerException exception ) throws TransformerException
            {
                if ( isLoggable( Level.WARNING ) )
                {
                    log( Level.WARNING, exception.getMessage(), exception );
                }
            }

            public void error( final TransformerException exception ) throws TransformerException
            {
                if ( isLoggable( Level.SEVERE ) )
                {
                    log( Level.SEVERE, exception.getMessage(), exception );
                }

                throw exception;
            }

            public void fatalError( final TransformerException exception ) throws TransformerException
            {
                if ( isLoggable( Level.SEVERE ) )
                {
                    log( Level.SEVERE, exception.getMessage(), exception );
                }

                throw exception;
            }

        };

        final URIResolver uriResolver = new URIResolver()
        {

            public Source resolve( final String href, final String base ) throws TransformerException
            {
                try
                {
                    final InputSource inputSource = getEntityResolver( classLoader ).resolveEntity( null, href );

                    if ( inputSource != null )
                    {
                        return new SAXSource( inputSource );
                    }

                    return null;
                }
                catch ( final SAXException e )
                {
                    if ( isLoggable( Level.SEVERE ) )
                    {
                        log( Level.SEVERE, e.getMessage(), e );
                    }

                    throw new TransformerException( e );
                }
                catch ( final IOException e )
                {
                    if ( isLoggable( Level.SEVERE ) )
                    {
                        log( Level.SEVERE, e.getMessage(), e );
                    }

                    throw new TransformerException( e );
                }
            }

        };

        transformerFactory.setErrorListener( errorListener );
        transformerFactory.setURIResolver( uriResolver );

        int count = 0;
        while ( resources.hasMoreElements() )
        {
            count++;
            final URL url = resources.nextElement();

            if ( this.isLoggable( Level.FINE ) )
            {
                this.log( Level.FINE, this.getMessage( "processing", new Object[]
                    {
                        url.toExternalForm()
                    } ), null );

            }

            final InputStream in = url.openStream();
            final Transformer transformer = transformerFactory.newTransformer( new StreamSource( in ) );
            in.close();

            transformer.setErrorListener( errorListener );
            transformer.setURIResolver( uriResolver );
            transformers.add( transformer );
        }

        if ( this.isLoggable( Level.FINE ) )
        {
            this.log( Level.FINE, this.getMessage( "classpathReport", new Object[]
                {
                    count, location, Long.valueOf( System.currentTimeMillis() - t0 )
                } ), null );

        }

        return transformers;
    }

    /**
     * Searches all available {@code META-INF/MANIFEST.MF} resources from a given class loader and returns a set
     * of URIs of entries whose name end with a known schema extension.
     *
     * @param classLoader The class loader to search for resources.
     *
     * @return Set of URIs of any matching entries.
     *
     * @throws NullPointerException if {@code classLoader} is {@code null}.
     * @throws IOException if reading or parsing fails.
     */
    private Set<URI> getSchemaResources( final ClassLoader classLoader ) throws IOException
    {
        if ( classLoader == null )
        {
            throw new NullPointerException( "classLoader" );
        }

        try
        {
            final Set<URI> resources = new HashSet<URI>();
            final long t0 = System.currentTimeMillis();
            int count = 0;

            for ( final Enumeration<URL> e = classLoader.getResources( "META-INF/MANIFEST.MF" ); e.hasMoreElements(); )
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
                                this.log( Level.FINE, this.getMessage( "addingSchemaCandidate", new Object[]
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
                this.log( Level.FINE, this.getMessage( "classpathReport", new Object[]
                    {
                        count, "META-INF/MANIFEST.MF", Long.valueOf( System.currentTimeMillis() - t0 )
                    } ), null );

            }

            return resources;
        }
        catch ( final URISyntaxException e )
        {
            throw (IOException) new IOException( e.getMessage() ).initCause( e );
        }
    }

    private String getMessage( final String key, final Object args )
    {
        return new MessageFormat(
            ResourceBundle.getBundle( DefaultModelManager.class.getName().replace( '.', '/' ), Locale.getDefault() ).
            getString( key ) ).format( args );

    }

    // SECTION-END
}
