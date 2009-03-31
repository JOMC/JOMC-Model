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
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.Source;
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
 * Object management and configuration entity resolution.
 *
 * @author <a href="mailto:cs@schulte.it">Christian Schulte</a>
 * @version $Id$
 */
public class ModelResolver implements EntityResolver, LSResourceResolver
{

    /** Classpath location of the bootstrap schema. */
    private static final String BOOTSTRAP_SCHEMA_LOCATION = "org/jomc/model/bootstrap/jomc-bootstrap-1.0.xsd";

    /** Classpath location searched for bootstrap resources. */
    private static final String BOOTSTRAP_RESOURCE_LOCATION = "META-INF/jomc-bootstrap.xml";

    /** JAXB context of the bootstrap schema. */
    private static final String BOOTSTRAP_CONTEXT = "org.jomc.model.bootstrap";

    /** Supported schema name extensions. */
    private static final String[] SCHEMA_EXTENSIONS = new String[]
    {
        "xsd"
    };

    /** The bootstrap schema. */
    private javax.xml.validation.Schema bootstrapSchema;

    /** The classloader to resolve entities with. */
    private ClassLoader classLoader;

    /** URLs of all available classpath schema resources. */
    private Set<URL> schemaResources;

    /** Schemas of the instance. */
    private Schemas schemas;

    /** Object factory of the instance. */
    private ObjectFactory objectFactory;

    /** Creates a new {@code ModelResolver} instance. */
    public ModelResolver()
    {
        this( null );
    }

    /**
     * Creates a new {@code ModelResolver} instance taking the classloader to resolve entities with.
     *
     * @param classLoader The classloader to resolve entities with.
     */
    public ModelResolver( final ClassLoader classLoader )
    {
        super();
        this.classLoader = classLoader;
    }

    /**
     * Gets the object management and configuration schema.
     *
     * @return The object management and configuration schema.
     *
     * @throws IOException if reading schema resources fails.
     * @throws SAXException if parsing schema resources fails.
     * @throws JAXBException if unmarshalling schema resources fails.
     */
    public javax.xml.validation.Schema getSchema() throws IOException, SAXException, JAXBException
    {
        final SchemaFactory f = SchemaFactory.newInstance( XMLConstants.W3C_XML_SCHEMA_NS_URI );
        final List<Source> sources = new ArrayList<Source>( this.getSchemas().getSchema().size() );

        for ( Schema s : this.getSchemas().getSchema() )
        {
            sources.add( new StreamSource( this.getClassLoader().getResourceAsStream( s.getClasspathId() ),
                                           s.getSystemId() ) );

        }

        return f.newSchema( sources.toArray( new Source[ sources.size() ] ) );
    }

    /**
     * Gets the schemas backing the instance.
     *
     * @return The schemas backing the instance.
     *
     * @throws IOException if reading schema resources fails.
     * @throws SAXException if parsing schema resources fails.
     * @throws JAXBException if unmarshalling schema resources fails.
     */
    public Schemas getSchemas() throws IOException, JAXBException, SAXException
    {
        if ( this.schemas == null )
        {
            this.schemas = new Schemas();
            final JAXBContext ctx = JAXBContext.newInstance( BOOTSTRAP_CONTEXT, this.getClassLoader() );
            final Unmarshaller u = ctx.createUnmarshaller();
            final Enumeration<URL> e = this.getClassLoader().getResources( BOOTSTRAP_RESOURCE_LOCATION );
            u.setSchema( this.getBootstrapSchema() );

            while ( e.hasMoreElements() )
            {
                final Object content = u.unmarshal( e.nextElement() );
                if ( content instanceof JAXBElement )
                {
                    final JAXBElement element = (JAXBElement) content;
                    if ( element.getValue() instanceof Schema )
                    {
                        this.schemas.getSchema().add( (Schema) element.getValue() );
                    }
                    else if ( element.getValue() instanceof Schemas )
                    {
                        this.schemas.getSchema().addAll( ( (Schemas) element.getValue() ).getSchema() );
                    }
                }
            }
        }

        return this.schemas;
    }

    /**
     * Gets the object management and configuration {@code JAXBContext}.
     *
     * @return The object management and configuration {@code JAXBContext}.
     *
     * @throws IOException if reading schema resources fails.
     * @throws SAXException if parsing schema resources fails.
     * @throws JAXBException if unmarshalling schema resources fails.
     */
    public JAXBContext getContext() throws IOException, SAXException, JAXBException
    {
        final StringBuffer context = new StringBuffer();

        for ( Iterator<Schema> s = this.getSchemas().getSchema().iterator(); s.hasNext(); )
        {
            context.append( s.next().getContextId() );
            if ( s.hasNext() )
            {
                context.append( ':' );
            }
        }

        return JAXBContext.newInstance( context.toString(), this.getClassLoader() );
    }

    /**
     * Gets an object management and configuration {@code Marshaller}.
     *
     * @param validating {@code true} for a marshaller with additional schema validation support enabled; {@code false}
     * for a marshaller without additional schema validation support enabled.
     * @param formattedOutput {@code true} for the marshaller to produce formatted output; {@code false} for the
     * marshaller to not apply any formatting when marshalling.
     *
     * @return An object management and configuration {@code Marshaller}.
     *
     * @throws IOException if reading schema resources fails.
     * @throws SAXException if parsing schema resources fails.
     * @throws JAXBException if unmarshalling schema resources fails.
     */
    public Marshaller getMarshaller( final boolean validating, final boolean formattedOutput )
        throws IOException, SAXException, JAXBException
    {
        final Marshaller m = this.getContext().createMarshaller();
        final StringBuffer schemaLocation = new StringBuffer();

        for ( Iterator<Schema> s = this.getSchemas().getSchema().iterator(); s.hasNext(); )
        {
            final Schema schema = s.next();
            schemaLocation.append( schema.getPublicId() ).append( ' ' ).append( schema.getSystemId() );
            if ( s.hasNext() )
            {
                schemaLocation.append( ' ' );
            }
        }

        m.setProperty( Marshaller.JAXB_SCHEMA_LOCATION, schemaLocation.toString() );
        m.setProperty( Marshaller.JAXB_ENCODING, "UTF-8" );
        m.setProperty( Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.valueOf( formattedOutput ) );

        if ( validating )
        {
            m.setSchema( this.getSchema() );
        }

        return m;
    }

    /**
     * Gets an object management and configuration {@code Unmarshaller}.
     *
     * @param validating {@code true} for an unmarshaller with additional schema validation support enabled;
     * {@code false} for an unmarshaller without additional schema validation support enabled.
     *
     * @return An object management and configuration {@code Unmarshaller}.
     *
     * @throws IOException if reading schema resources fails.
     * @throws SAXException if parsing schema resources fails.
     * @throws JAXBException if unmarshalling schema resources fails.
     */
    public Unmarshaller getUnmarshaller( final boolean validating ) throws IOException, SAXException, JAXBException
    {
        final Unmarshaller u = this.getContext().createUnmarshaller();

        if ( validating )
        {
            u.setSchema( this.getSchema() );
        }

        return u;
    }

    /**
     * Gets the object management and configuration {@code ObjectFactory}.
     *
     * @return The object management and configuration {@code ObjectFactory}.
     */
    public ObjectFactory getObjectFactory()
    {
        if ( this.objectFactory == null )
        {
            this.objectFactory = new ObjectFactory();
        }

        return this.objectFactory;
    }

    public InputSource resolveEntity( final String publicId, final String systemId ) throws SAXException, IOException
    {
        if ( systemId == null )
        {
            throw new NullPointerException( "systemId" );
        }

        InputSource schemaSource = null;

        try
        {
            final Schema s = this.getSchemas().getSchema( publicId );
            if ( s != null )
            {
                schemaSource = new InputSource();
                schemaSource.setPublicId( publicId );

                if ( s.getClasspathId() != null )
                {
                    schemaSource.setSystemId( this.getClassLoader().getResource(
                        s.getClasspathId() ).toExternalForm() );

                }
                else
                {
                    schemaSource.setSystemId( s.getSystemId() );
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

                    for ( URL url : this.getSchemaResources() )
                    {
                        if ( url.getPath().endsWith( schemaName ) )
                        {
                            schemaSource = new InputSource();
                            schemaSource.setPublicId( publicId );
                            schemaSource.setSystemId( url.toExternalForm() );

                            if ( Logger.getLogger( this.getClass().getName() ).isLoggable( Level.FINE ) )
                            {
                                final MessageFormat fmt = new MessageFormat( this.getMessage( "resolvedSystemIdUri" ) );

                                Logger.getLogger( this.getClass().getName() ).log( Level.FINE, fmt.format( new Object[]
                                    {
                                        systemUri.toASCIIString(),
                                        schemaSource.getSystemId()
                                    } ) );

                            }

                            break;
                        }
                    }
                }
                else
                {
                    final MessageFormat fmt = new MessageFormat( this.getMessage( "unsupportedSystemIdUri" ) );
                    Logger.getLogger( this.getClass().getName() ).log( Level.WARNING, fmt.format( new Object[]
                        {
                            systemId, systemUri.toASCIIString()
                        } ) );

                    schemaSource = null;
                }
            }
        }
        catch ( URISyntaxException e )
        {
            final MessageFormat fmt = new MessageFormat( this.getMessage( "unsupportedSystemIdUri" ) );
            Logger.getLogger( this.getClass().getName() ).log( Level.WARNING, fmt.format( new Object[]
                {
                    systemId, e.getMessage()
                } ) );

            schemaSource = null;
        }
        catch ( JAXBException e )
        {
            throw (IOException) new IOException( e.getMessage() ).initCause( e );
        }

        return schemaSource;
    }

    public LSInput resolveResource( final String type, final String namespaceURI, final String publicId,
                                    final String systemId, final String baseURI )
    {
        LSInput input = null;
        try
        {
            final InputSource schemaSource = this.resolveEntity( publicId, systemId );

            if ( schemaSource != null )
            {
                input = new LSInput()
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
                        return null;
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
        catch ( SAXException e )
        {
            final MessageFormat fmt = new MessageFormat( this.getMessage( "unsupportedSystemIdUri" ) );
            Logger.getLogger( this.getClass().getName() ).log( Level.WARNING, fmt.format( new Object[]
                {
                    systemId, e.getMessage()
                } ) );

            input = null;
        }
        catch ( IOException e )
        {
            final MessageFormat fmt = new MessageFormat( this.getMessage( "unsupportedSystemIdUri" ) );
            Logger.getLogger( this.getClass().getName() ).log( Level.WARNING, fmt.format( new Object[]
                {
                    systemId, e.getMessage()
                } ) );

            input = null;
        }

        return input;
    }

    /**
     * Searches all available {@code META-INF/MANIFEST.MF} resources and gets a
     * set containing URLs of entries whose name end with a known schema
     * extension.
     *
     * @return URLs of any matching entries.
     *
     * @throws IOException if reading or parsing fails.
     */
    private Set<URL> getSchemaResources() throws IOException
    {
        if ( this.schemaResources == null )
        {
            this.schemaResources = new HashSet();

            for ( Enumeration<URL> e = this.getClassLoader().getResources( "META-INF/MANIFEST.MF" );
                  e.hasMoreElements(); )
            {
                final URL manifestUrl = e.nextElement();
                final String externalForm = manifestUrl.toExternalForm();
                final String baseUrl = externalForm.substring( 0, externalForm.indexOf( "META-INF" ) );
                final InputStream manifestStream = manifestUrl.openStream();
                final Manifest mf = new Manifest( manifestStream );
                manifestStream.close();

                for ( Map.Entry<String, Attributes> entry : mf.getEntries().entrySet() )
                {
                    for ( int i = SCHEMA_EXTENSIONS.length - 1; i >= 0; i-- )
                    {
                        if ( entry.getKey().toLowerCase().endsWith( '.' + SCHEMA_EXTENSIONS[i].toLowerCase() ) )
                        {
                            final URL schemaUrl = new URL( baseUrl + entry.getKey() );
                            this.schemaResources.add( schemaUrl );
                        }
                    }
                }
            }
        }

        return this.schemaResources;
    }

    /**
     * Gets the bootstrap schema.
     *
     * @return The bootstrap schema.
     *
     * @throws SAXException if parsing the bootstrap schema fails.
     */
    private javax.xml.validation.Schema getBootstrapSchema() throws SAXException
    {
        if ( this.bootstrapSchema == null )
        {
            this.bootstrapSchema = SchemaFactory.newInstance( XMLConstants.W3C_XML_SCHEMA_NS_URI ).
                newSchema( this.getClassLoader().getResource( BOOTSTRAP_SCHEMA_LOCATION ) );

        }

        return this.bootstrapSchema;
    }

    /**
     * Gets the classloader entities are resolved with.
     *
     * @return The classloader entities are resolved with.
     *
     * @see ModelResolver#ModelResolver(java.lang.ClassLoader)
     */
    protected ClassLoader getClassLoader()
    {
        if ( this.classLoader == null )
        {
            this.classLoader = this.getClass().getClassLoader();
            if ( this.classLoader == null )
            {
                this.classLoader = ClassLoader.getSystemClassLoader();
            }
        }

        return this.classLoader;
    }

    private String getMessage( final String key )
    {
        return ResourceBundle.getBundle( "org/jomc/model/ModelResolver", Locale.getDefault(), this.getClassLoader() ).
            getString( key );

    }

}
