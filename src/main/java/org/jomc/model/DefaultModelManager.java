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
package org.jomc.model;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
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
import javax.xml.bind.util.JAXBResult;
import javax.xml.bind.util.JAXBSource;
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
import javax.xml.validation.Validator;
import org.jomc.model.bootstrap.BootstrapObject;
import org.jomc.model.bootstrap.Schema;
import org.jomc.model.bootstrap.Schemas;
import org.jomc.util.ParseException;
import org.jomc.util.TokenMgrError;
import org.jomc.util.VersionParser;
import org.jomc.util.WeakIdentityHashMap;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * Default {@code ModelManager} implementation.
 *
 * <p><b>Classpath support</b><ul>
 * <li>{@link #getClassLoader() }</li>
 * <li>{@link #setClassLoader(java.lang.ClassLoader) }</li>
 * <li>{@link #getDefaultDocumentLocation() }</li>
 * <li>{@link #getClasspathModule(org.jomc.model.Modules) }</li>
 * <li>{@link #getClasspathModules(java.lang.String) }</li>
 * <li>{@link #getDefaultStylesheetLocation() }</li>
 * <li>{@link #getClasspathTransformers(java.lang.String) }</li>
 * </ul></p>
 *
 * <p><b>Logging</b><ul>
 * <li>{@link #getListeners() }</li>
 * <li>{@link #log(java.util.logging.Level, java.lang.String, java.lang.Throwable) }</li>
 * </ul></p>
 *
 * <p><b>Model bootstrapping</b><ul>
 * <li>{@link #getBootstrapContext() }</li>
 * <li>{@link #getBootstrapDocumentLocation() }</li>
 * <li>{@link #getBootstrapMarshaller(boolean, boolean) }</li>
 * <li>{@link #getBootstrapObjectFactory() }</li>
 * <li>{@link #getBootstrapSchema() }</li>
 * <li>{@link #getBootstrapUnmarshaller(boolean) }</li>
 * <li>{@link #validateBootstrapObject(javax.xml.bind.JAXBElement) }</li>
 * <li>{@link #transformBootstrapObject(javax.xml.bind.JAXBElement, javax.xml.transform.Transformer) }</li>
 * </ul></p>
 *
 * @author <a href="mailto:schulte2005@users.sourceforge.net">Christian Schulte</a>
 * @version $Id$
 */
public class DefaultModelManager implements ModelManager
{
    // SECTION-START[ModelManager]

    public ObjectFactory getObjectFactory()
    {
        if ( this.objectFactory == null )
        {
            this.objectFactory = new ObjectFactory();
        }

        return this.objectFactory;
    }

    public EntityResolver getEntityResolver()
    {
        if ( this.entityResolver == null )
        {
            this.entityResolver = new EntityResolver()
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
                        final Schema s = getSchemas().getSchema( publicId );
                        if ( s != null )
                        {
                            schemaSource = new InputSource();
                            schemaSource.setPublicId( publicId );

                            if ( s.getClasspathId() != null )
                            {
                                schemaSource.setSystemId( getClassLoader().getResource( s.getClasspathId() ).
                                    toExternalForm() );

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

                                for ( URL url : getSchemaResources() )
                                {
                                    if ( url.getPath().endsWith( schemaName ) )
                                    {
                                        schemaSource = new InputSource();
                                        schemaSource.setPublicId( publicId );
                                        schemaSource.setSystemId( url.toExternalForm() );

                                        log( Level.FINE, getMessage( "resolvedSystemIdUri", new Object[]
                                            {
                                                systemUri.toASCIIString(),
                                                schemaSource.getSystemId()
                                            } ), null );

                                        break;
                                    }
                                }
                            }
                            else
                            {
                                log( Level.WARNING, getMessage( "unsupportedSystemIdUri", new Object[]
                                    {
                                        systemId, systemUri.toASCIIString()
                                    } ), null );

                                schemaSource = null;
                            }
                        }
                    }
                    catch ( URISyntaxException e )
                    {
                        log( Level.WARNING, getMessage( "unsupportedSystemIdUri", new Object[]
                            {
                                systemId, e.getMessage()
                            } ), null );

                        schemaSource = null;
                    }
                    catch ( JAXBException e )
                    {
                        throw (IOException) new IOException( e.getMessage() ).initCause( e );
                    }

                    return schemaSource;
                }

            };
        }

        return this.entityResolver;
    }

    public LSResourceResolver getLSResourceResolver()
    {
        if ( this.resourceResolver == null )
        {
            this.resourceResolver = new LSResourceResolver()
            {

                public LSInput resolveResource( final String type, final String namespaceURI, final String publicId,
                                                final String systemId, final String baseURI )
                {
                    LSInput input = null;
                    try
                    {
                        final InputSource schemaSource = getEntityResolver().resolveEntity( publicId, systemId );

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
                        log( Level.WARNING, getMessage( "unsupportedSystemIdUri", new Object[]
                            {
                                systemId, e.getMessage()
                            } ), null );

                        input = null;
                    }
                    catch ( IOException e )
                    {
                        log( Level.WARNING, getMessage( "unsupportedSystemIdUri", new Object[]
                            {
                                systemId, e.getMessage()
                            } ), null );

                        input = null;
                    }

                    return input;
                }

            };
        }

        return this.resourceResolver;
    }

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

    public JAXBContext getContext() throws IOException, SAXException, JAXBException
    {
        final StringBuffer context = new StringBuffer();

        for ( Iterator<Schema> s = this.getSchemas().getSchema().iterator(); s.hasNext(); )
        {
            final Schema schema = s.next();
            context.append( schema.getContextId() );
            if ( s.hasNext() )
            {
                context.append( ':' );
            }
        }

        if ( context.length() == 0 )
        {
            throw new IOException( this.getMessage( "missingSchemas", new Object[]
                {
                    this.getBootstrapDocumentLocation()
                } ) );

        }

        return JAXBContext.newInstance( context.toString(), this.getClassLoader() );
    }

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

    public Unmarshaller getUnmarshaller( final boolean validating ) throws IOException, SAXException, JAXBException
    {
        final Unmarshaller u = this.getContext().createUnmarshaller();

        if ( validating )
        {
            u.setSchema( this.getSchema() );
        }

        return u;
    }

    public void validateModelObject( final JAXBElement<? extends ModelObject> modelObject )
        throws ModelException, IOException, SAXException, JAXBException
    {
        if ( modelObject == null )
        {
            throw new NullPointerException( "modelObject" );
        }

        final StringWriter stringWriter = new StringWriter();
        final List<ModelException.Detail> details = new LinkedList<ModelException.Detail>();
        final Validator validator = this.getSchema().newValidator();
        validator.setErrorHandler( new ErrorHandler()
        {

            public void warning( final SAXParseException exception ) throws SAXException
            {
                if ( exception.getMessage() != null )
                {
                    details.add( new ModelException.Detail( Level.WARNING, exception.getMessage() ) );
                }
            }

            public void error( final SAXParseException exception ) throws SAXException
            {
                if ( exception.getMessage() != null )
                {
                    details.add( new ModelException.Detail( Level.SEVERE, exception.getMessage() ) );
                }

                throw exception;
            }

            public void fatalError( final SAXParseException exception ) throws SAXException
            {
                if ( exception.getMessage() != null )
                {
                    details.add( new ModelException.Detail( Level.SEVERE, exception.getMessage() ) );
                }

                throw exception;
            }

        } );

        this.getMarshaller( false, false ).marshal( modelObject, stringWriter );

        try
        {
            validator.validate( new StreamSource( new StringReader( stringWriter.toString() ) ) );
        }
        catch ( SAXException e )
        {
            final ModelException modelException = new ModelException( e.getMessage(), e );
            modelException.getDetails().addAll( details );
            throw modelException;
        }
    }

    public void validateModules( final Modules modules )
        throws ModelException, IOException, SAXException, JAXBException
    {
        if ( modules == null )
        {
            throw new NullPointerException( "modules" );
        }

        this.validateModelObject( this.getObjectFactory().createModules( modules ) );
        final List<ModelException.Detail> details = new LinkedList<ModelException.Detail>();

        try
        {
            for ( Module m : modules.getModule() )
            {
                if ( m.getMessages() != null )
                {
                    this.assertMessagesUniqueness( m.getMessages(), details );
                }

                if ( m.getProperties() != null )
                {
                    this.assertPropertiesUniqueness( m.getProperties(), details );
                }

                if ( m.getImplementations() != null )
                {
                    for ( Implementation i : m.getImplementations().getImplementation() )
                    {
                        if ( i.getMessages() != null )
                        {
                            this.assertMessagesUniqueness( i.getMessages(), details );
                        }

                        if ( i.getProperties() != null )
                        {
                            this.assertPropertiesUniqueness( i.getProperties(), details );
                        }

                        final List<SpecificationReference> specs =
                            modules.getSpecifications( i.getIdentifier() );

                        final Dependencies deps = modules.getDependencies( i.getIdentifier() );

                        if ( specs != null )
                        {
                            for ( SpecificationReference r : specs )
                            {
                                final Specification s = modules.getSpecification( r.getIdentifier() );

                                if ( s != null && r.getVersion() != null && s.getVersion() != null &&
                                     VersionParser.compare( r.getVersion(), s.getVersion() ) != 0 )
                                {
                                    details.add( this.newIncompatibleImplementationDetail(
                                        this.getObjectFactory().createImplementation( i ), i.getIdentifier(),
                                        r.getIdentifier(), r.getVersion(), s.getVersion() ) );

                                }
                            }
                        }

                        if ( deps != null )
                        {
                            for ( Dependency d : deps.getDependency() )
                            {
                                if ( d.getProperties() != null )
                                {
                                    this.assertPropertiesUniqueness( d.getProperties(), details );
                                }

                                final Specification s = modules.getSpecification( d.getIdentifier() );

                                if ( s != null && s.getVersion() != null && d.getVersion() != null &&
                                     VersionParser.compare( d.getVersion(), s.getVersion() ) > 0 )
                                {
                                    details.add( this.newIncompatibleDependencyDetail(
                                        this.getObjectFactory().createDependency( d ), i.getIdentifier(),
                                        d.getIdentifier(), d.getVersion(), s.getVersion() ) );

                                }

                                if ( d.getProperties() != null && !d.getProperties().getReference().isEmpty() )
                                {
                                    details.add( this.newDependencyPropertyReferenceConstraintDetail(
                                        this.getObjectFactory().createDependency( d ), i, d ) );

                                }

                                final Implementations available = modules.getImplementations( d.getIdentifier() );

                                if ( !d.isOptional() )
                                {
                                    boolean missing = false;

                                    if ( available == null )
                                    {
                                        missing = true;
                                    }
                                    else if ( available.getImplementation().isEmpty() )
                                    {
                                        missing = true;
                                    }
                                    else if ( d.getImplementationName() != null &&
                                              available.getImplementationByName( d.getImplementationName() ) == null )
                                    {
                                        missing = true;
                                    }

                                    if ( missing )
                                    {
                                        details.add( this.newMandatoryDependencyConstraintDetail(
                                            this.getObjectFactory().createDependency( d ), i.getIdentifier(),
                                            d.getName() ) );

                                    }
                                }
                            }
                        }

                        if ( i.getParent() != null )
                        {
                            final Implementation parent = modules.getImplementation( i.getParent() );
                            if ( parent != null && parent.isFinal() )
                            {
                                details.add( this.newInheritanceConstraintDetail(
                                    this.getObjectFactory().createImplementation( i ), i, parent ) );

                            }
                        }
                    }
                }

                if ( m.getSpecifications() != null )
                {
                    for ( Specification s : m.getSpecifications().getSpecification() )
                    {
                        if ( s.getProperties() != null )
                        {
                            this.assertPropertiesUniqueness( s.getProperties(), details );
                        }

                        final Implementations impls = modules.getImplementations( s.getIdentifier() );

                        if ( impls != null )
                        {
                            final Map<String, Implementation> map = new HashMap<String, Implementation>();

                            for ( Implementation i : impls.getImplementation() )
                            {
                                if ( map.containsKey( i.getName() ) )
                                {
                                    details.add( this.newImplementationNameConstraintDetail(
                                        this.getObjectFactory().createSpecification( s ), s.getIdentifier(),
                                        i.getIdentifier() + ", " + map.get( i.getName() ).getIdentifier() ) );

                                }
                            }

                            if ( s.getMultiplicity() == Multiplicity.ONE && impls.getImplementation().size() > 1 )
                            {
                                details.add( this.newMultiplicityConstraintDetail(
                                    this.getObjectFactory().createSpecification( s ), impls.getImplementation().size(),
                                    s.getIdentifier(), 1, s.getMultiplicity() ) );

                            }
                        }
                    }
                }
            }

            if ( !details.isEmpty() )
            {
                final ModelException modelException = new ModelException( this.getMessage( "validationFailed", null ) );
                modelException.getDetails().addAll( details );
                throw modelException;
            }
        }
        catch ( TokenMgrError e )
        {
            throw new ModelException( e.getMessage(), e );
        }
        catch ( ParseException e )
        {
            throw new ModelException( e.getMessage(), e );
        }
    }

    public <T extends ModelObject> T transformModelObject(
        final JAXBElement<T> modelObject, final Transformer transformer )
        throws NullPointerException, IOException, SAXException, JAXBException, TransformerException
    {
        if ( modelObject == null )
        {
            throw new NullPointerException( "modelObject" );
        }
        if ( transformer == null )
        {
            throw new NullPointerException( "transformer" );
        }

        final JAXBContext ctx = this.getContext();
        final JAXBSource source = new JAXBSource( ctx, modelObject );
        final JAXBResult result = new JAXBResult( ctx );
        transformer.transform( source, result );
        return ( (JAXBElement<T>) result.getResult() ).getValue();
    }

    public Instance getInstance( final Modules modules, final Implementation implementation,
                                 final ClassLoader classLoader )
    {
        if ( modules == null )
        {
            throw new NullPointerException( "modules" );
        }
        if ( implementation == null )
        {
            throw new NullPointerException( "implementation" );
        }
        if ( classLoader == null )
        {
            throw new NullPointerException( "classLoader" );
        }

        final Instance instance = new Instance();
        instance.setIdentifier( implementation.getIdentifier() );
        instance.setImplementationName( implementation.getName() );
        instance.setClazz( implementation.getClazz() );
        instance.setClassLoader( classLoader );
        instance.setScope( implementation.getScope() );
        instance.setStateless( implementation.isStateless() );
        instance.setDependencies( modules.getDependencies( implementation.getIdentifier() ) );
        instance.setProperties( modules.getProperties( implementation.getIdentifier() ) );
        instance.setMessages( modules.getMessages( implementation.getIdentifier() ) );

        final List<SpecificationReference> specifications = modules.getSpecifications( implementation.getIdentifier() );
        if ( specifications != null && !specifications.isEmpty() )
        {
            instance.setSpecifications( new Specifications() );
            for ( SpecificationReference ref : specifications )
            {
                final Specification s = modules.getSpecification( ref.getIdentifier() );
                if ( s != null )
                {
                    instance.getSpecifications().getSpecification().add( s );
                }
            }
        }

        return instance;
    }

    public Instance getInstance( final Modules modules, final Implementation implementation,
                                 final Dependency dependency, final ClassLoader classLoader )
    {
        if ( modules == null )
        {
            throw new NullPointerException( "modules" );
        }
        if ( implementation == null )
        {
            throw new NullPointerException( "implementation" );
        }
        if ( dependency == null )
        {
            throw new NullPointerException( "dependency" );
        }
        if ( classLoader == null )
        {
            throw new NullPointerException( "classLoader" );
        }

        final Instance instance = this.getInstance( modules, implementation, classLoader );
        if ( dependency.getProperties() != null && !dependency.getProperties().getProperty().isEmpty() )
        {
            if ( instance.getScope().equals( "Multiton" ) )
            {
                final Properties properties = new Properties();
                properties.getProperty().addAll( dependency.getProperties().getProperty() );

                if ( instance.getProperties() != null )
                {
                    for ( Property p : instance.getProperties().getProperty() )
                    {
                        if ( properties.getProperty( p.getName() ) == null )
                        {
                            properties.getProperty().add( p );
                        }
                    }
                }

                instance.setProperties( properties );
            }
            else
            {
                this.log( Level.WARNING, this.getPropertyOverwriteConstraintMessage(
                    instance.getIdentifier(), instance.getScope(), dependency.getName() ), null );

            }
        }

        return instance;
    }

    public Instance getInstance( final Modules modules, final Object object )
    {
        if ( modules == null )
        {
            throw new NullPointerException( "modules" );
        }
        if ( object == null )
        {
            throw new NullPointerException( "object" );
        }

        synchronized ( this.objects )
        {
            Instance instance = (Instance) this.objects.get( object );

            if ( instance == null )
            {
                final Implementation i = this.getImplementation( modules, object );

                if ( i != null )
                {
                    ClassLoader cl = object.getClass().getClassLoader();
                    if ( cl == null )
                    {
                        cl = ClassLoader.getSystemClassLoader();
                    }

                    instance = this.getInstance( modules, i, cl );
                    if ( instance != null )
                    {
                        this.objects.put( object, instance );
                    }
                }
            }

            return instance;
        }
    }

    public Object getObject( final Modules modules, final Specification specification, final Instance instance )
        throws InstantiationException
    {
        if ( modules == null )
        {
            throw new NullPointerException( "modules" );
        }
        if ( specification == null )
        {
            throw new NullPointerException( "specification" );
        }
        if ( instance == null )
        {
            throw new NullPointerException( "instance" );
        }

        Object object = null;

        try
        {
            final Class specClass = Class.forName( specification.getIdentifier(), true, instance.getClassLoader() );
            final Class clazz = Class.forName( instance.getClazz(), true, instance.getClassLoader() );

            if ( Modifier.isPublic( clazz.getModifiers() ) )
            {
                Constructor ctor = null;

                try
                {
                    ctor = clazz.getConstructor( NO_CLASSES );
                }
                catch ( NoSuchMethodException e )
                {
                    this.log( Level.FINE, this.getMessage( "noSuchMethod", new Object[]
                        {
                            e.getMessage()
                        } ), null );

                    ctor = null;
                }

                if ( ctor != null && specClass.isAssignableFrom( clazz ) )
                {
                    synchronized ( this.objects )
                    {
                        object = clazz.newInstance();
                        this.objects.put( object, instance );
                    }
                }
                else
                {
                    final StringBuffer methodNames = new StringBuffer().append( '[' );
                    Method factoryMethod = null;
                    String methodName = null;

                    char[] c = instance.getImplementationName().toCharArray();
                    c[0] = Character.toUpperCase( c[0] );
                    methodName = "get" + String.valueOf( c );

                    boolean javaIdentifier = Character.isJavaIdentifierStart( c[0] );
                    if ( javaIdentifier )
                    {
                        for ( int idx = c.length - 1; idx > 0; idx-- )
                        {
                            if ( !Character.isJavaIdentifierPart( c[idx] ) )
                            {
                                javaIdentifier = false;
                                break;
                            }
                        }
                    }

                    if ( javaIdentifier )
                    {
                        methodNames.append( methodName );
                        factoryMethod = this.getFactoryMethod( clazz, methodName );
                    }

                    if ( factoryMethod == null )
                    {
                        methodName = specification.getIdentifier().substring(
                            specification.getIdentifier().lastIndexOf( '.' ) + 1 );

                        c = methodName.toCharArray();
                        c[0] = Character.toUpperCase( c[0] );

                        javaIdentifier = Character.isJavaIdentifierStart( c[0] );
                        if ( javaIdentifier )
                        {
                            for ( int idx = c.length - 1; idx > 0; idx-- )
                            {
                                if ( !Character.isJavaIdentifierPart( c[idx] ) )
                                {
                                    javaIdentifier = false;
                                    break;
                                }
                            }
                        }

                        if ( javaIdentifier )
                        {
                            methodName = "get" + String.valueOf( c );
                            methodNames.append( " " ).append( methodName );
                            factoryMethod = this.getFactoryMethod( clazz, methodName );
                        }
                    }

                    if ( factoryMethod == null )
                    {
                        methodName = "getObject";
                        methodNames.append( " " ).append( methodName );
                        factoryMethod = this.getFactoryMethod( clazz, methodName );
                    }

                    methodNames.append( ']' );

                    if ( factoryMethod == null )
                    {
                        throw new InstantiationException( this.getMessage( "missingFactoryMethod", new Object[]
                            {
                                clazz.getName(), instance.getIdentifier(), methodNames.toString()
                            } ) );

                    }

                    if ( Modifier.isStatic( factoryMethod.getModifiers() ) )
                    {
                        object = factoryMethod.invoke( null, NO_OBJECTS );
                    }
                    else if ( ctor != null )
                    {
                        synchronized ( this.objects )
                        {
                            object = ctor.newInstance();
                            this.objects.put( object, instance );
                            object = factoryMethod.invoke( object, NO_OBJECTS );
                            this.objects.put( object, instance );
                        }
                    }
                    else
                    {
                        throw new InstantiationException( this.getMessage( "missingFactoryMethod", new Object[]
                            {
                                clazz.getName(), instance.getIdentifier(), methodNames.toString()
                            } ) );

                    }
                }
            }

            return object;
        }
        catch ( InvocationTargetException e )
        {
            throw (InstantiationException) new InstantiationException().initCause(
                e.getTargetException() != null ? e.getTargetException() : e );

        }
        catch ( IllegalAccessException e )
        {
            throw (InstantiationException) new InstantiationException().initCause( e );
        }
        catch ( ClassNotFoundException e )
        {
            throw (InstantiationException) new InstantiationException().initCause( e );
        }
    }

    // SECTION-END
    // SECTION-START[DefaultModelManager]
    /** Listener interface. */
    public static abstract class Listener
    {

        /**
         * Get called on logging.
         *
         * @param level The level of the event.
         * @param message The message of the event or {@code null}.
         * @param t The throwable of the event or {@code null}.
         */
        public abstract void onLog( Level level, String message, Throwable t );

    }

    /**
     * Constant for the name of the classpath module.
     * @see #getClasspathModuleName()
     */
    private static final String DEFAULT_CLASSPATH_MODULE_NAME = "Java Classpath";

    /**
     * Classpath location searched for documents by default.
     * @see #getDefaultDocumentLocation()
     */
    private static final String DEFAULT_DOCUMENT_LOCATION = "META-INF/jomc.xml";

    /**
     * Classpath location searched for style sheets by default.
     * @see #getDefaultStylesheetLocation()
     */
    private static final String DEFAULT_STYLESHEET_LOCATION = "META-INF/jomc.xslt";

    /** Classpath location of the bootstrap schema. */
    private static final String BOOTSTRAP_SCHEMA_LOCATION =
        Schemas.class.getPackage().getName().replace( '.', '/' ) + "/jomc-bootstrap-1.0.xsd";

    /**
     * Classpath location searched for bootstrap documents by default.
     * @see #getBootstrapDocumentLocation()
     */
    private static final String DEFAULT_BOOTSTRAP_DOCUMENT_LOCATION = "META-INF/jomc-bootstrap.xml";

    /** JAXB context of the bootstrap schema. */
    private static final String BOOTSTRAP_CONTEXT = Schemas.class.getPackage().getName();

    /** Supported schema name extensions. */
    private static final String[] SCHEMA_EXTENSIONS = new String[]
    {
        "xsd"
    };

    /** Empty {@code Class} array. */
    private static final Class[] NO_CLASSES =
    {
    };

    /** Empty {@code Object} array. */
    private static final Object[] NO_OBJECTS =
    {
    };

    /** Class loader of the instance. */
    private ClassLoader classLoader;

    /** The entity resolver of the instance. */
    private EntityResolver entityResolver;

    /** The L/S resolver of the instance. */
    private LSResourceResolver resourceResolver;

    /** The bootstrap schema. */
    private javax.xml.validation.Schema bootstrapSchema;

    /** URLs of all available classpath schema resources. */
    private Set<URL> schemaResources;

    /** Schemas of the instance. */
    private Schemas schemas;

    /** Object factory of the instance. */
    private ObjectFactory objectFactory;

    /** Bootstrap object factory of the instance. */
    private org.jomc.model.bootstrap.ObjectFactory bootstrapObjectFactory;

    /** The listeners of the instance. */
    private List<Listener> listeners;

    /** Maps objects to {@code Instance}s. */
    private final Map objects = new WeakIdentityHashMap( 1024 );

    /** Creates a new {@code DefaultModelManager} instance. */
    public DefaultModelManager()
    {
        super();
    }

    /**
     * Gets the bootstrap object factory of the instance.
     *
     * @return The bootstrap object factory of the instance.
     */
    public org.jomc.model.bootstrap.ObjectFactory getBootstrapObjectFactory()
    {
        if ( this.bootstrapObjectFactory == null )
        {
            this.bootstrapObjectFactory = new org.jomc.model.bootstrap.ObjectFactory();
        }

        return this.bootstrapObjectFactory;
    }

    /**
     * Gets a new bootstrap context instance.
     *
     * @return A new bootstrap context instance.
     *
     * @throws JAXBException if creating a new bootstrap context instance fails.
     */
    public JAXBContext getBootstrapContext() throws JAXBException
    {
        return JAXBContext.newInstance( BOOTSTRAP_CONTEXT, this.getClassLoader() );
    }

    /**
     * Gets a new bootstrap {@code Marshaller}.
     *
     * @param validating {@code true} for a marshaller with additional schema validation support enabled; {@code false}
     * for a marshaller without additional schema validation support enabled.
     * @param formattedOutput {@code true} for the marshaller to produce formatted output; {@code false} for the
     * marshaller to not apply any formatting when marshalling.
     *
     * @return A new bootstrap {@code Marshaller}.
     *
     * @throws IOException if reading schema resources fails.
     * @throws SAXException if parsing schema resources fails.
     * @throws JAXBException if unmarshalling schema resources fails.
     */
    public Marshaller getBootstrapMarshaller( boolean validating, boolean formattedOutput )
        throws IOException, SAXException, JAXBException
    {
        final Marshaller m = this.getBootstrapContext().createMarshaller();
        m.setProperty( Marshaller.JAXB_ENCODING, "UTF-8" );
        m.setProperty( Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.valueOf( formattedOutput ) );

        if ( validating )
        {
            m.setSchema( this.getBootstrapSchema() );
        }

        return m;
    }

    /**
     * Gets a new bootstrap {@code Unmarshaller}.
     *
     * @param validating {@code true} for an unmarshaller with additional schema validation support enabled;
     * {@code false} for an unmarshaller without additional schema validation support enabled.
     *
     * @return A new bootstrap {@code Unmarshaller}.
     *
     * @throws IOException if reading schema resources fails.
     * @throws SAXException if parsing schema resources fails.
     * @throws JAXBException if unmarshalling schema resources fails.
     */
    public Unmarshaller getBootstrapUnmarshaller( boolean validating )
        throws IOException, SAXException, JAXBException
    {
        final Unmarshaller u = this.getBootstrapContext().createUnmarshaller();
        if ( validating )
        {
            u.setSchema( this.getBootstrapSchema() );
        }

        return u;
    }

    /**
     * Gets the bootstrap schema.
     *
     * @return The bootstrap schema.
     *
     * @throws SAXException if parsing the bootstrap schema fails.
     */
    public javax.xml.validation.Schema getBootstrapSchema() throws SAXException
    {
        if ( this.bootstrapSchema == null )
        {
            final URL url = this.getClassLoader().getResource( BOOTSTRAP_SCHEMA_LOCATION );
            this.log( Level.FINE, this.getMessage( "processing", new Object[]
                {
                    url.toExternalForm()
                } ), null );

            this.bootstrapSchema = SchemaFactory.newInstance( XMLConstants.W3C_XML_SCHEMA_NS_URI ).newSchema( url );
        }

        return this.bootstrapSchema;
    }

    /**
     * Validates a given bootstrap object.
     *
     * @param bootstrapObject The object to validate.
     *
     * @throws NullPointerException if {@code bootstrapObject} is {@code null}.
     * @throws ModelException if {@code bootstrapObject} is invalid.
     * @throws IOException if reading schema resources fails.
     * @throws SAXException if parsing schema resources fails.
     * @throws JAXBException if unmarshalling schema resources fails.
     */
    public void validateBootstrapObject( JAXBElement<? extends BootstrapObject> bootstrapObject )
        throws NullPointerException, ModelException, IOException, SAXException, JAXBException
    {
        if ( bootstrapObject == null )
        {
            throw new NullPointerException( "bootstrapObject" );
        }

        final StringWriter stringWriter = new StringWriter();
        final List<ModelException.Detail> details = new LinkedList<ModelException.Detail>();
        final Validator validator = this.getBootstrapSchema().newValidator();
        validator.setErrorHandler( new ErrorHandler()
        {

            public void warning( final SAXParseException exception ) throws SAXException
            {
                if ( exception.getMessage() != null )
                {
                    details.add( new ModelException.Detail( Level.WARNING, exception.getMessage() ) );
                }
            }

            public void error( final SAXParseException exception ) throws SAXException
            {
                if ( exception.getMessage() != null )
                {
                    details.add( new ModelException.Detail( Level.SEVERE, exception.getMessage() ) );
                }

                throw exception;
            }

            public void fatalError( final SAXParseException exception ) throws SAXException
            {
                if ( exception.getMessage() != null )
                {
                    details.add( new ModelException.Detail( Level.SEVERE, exception.getMessage() ) );
                }

                throw exception;
            }

        } );

        this.getBootstrapMarshaller( false, false ).marshal( bootstrapObject, stringWriter );

        try
        {
            validator.validate( new StreamSource( new StringReader( stringWriter.toString() ) ) );
        }
        catch ( SAXException e )
        {
            final ModelException modelException = new ModelException( e.getMessage(), e );
            modelException.getDetails().addAll( details );
            throw modelException;
        }
    }

    /**
     * Transforms a given {@code BootstrapObject} with a given {@code Transformer}.
     *
     * @param bootstrapObject The {@code BootstrapObject} to transform.
     * @param transformer The {@code Transformer} to transform {@code bootstrapObject} with.
     *
     * @throws NullPointerException if {@code bootstrapObject} or {@code transformer} is {@code null}.
     * @throws IOException if reading schema resources fails.
     * @throws SAXException if parsing schema resources fails.
     * @throws JAXBException if binding fails.
     * @throws TransformerException if the transformation fails.
     */
    public <T extends BootstrapObject> T transformBootstrapObject(
        final JAXBElement<T> bootstrapObject, final Transformer transformer )
        throws NullPointerException, IOException, SAXException, JAXBException, TransformerException
    {
        if ( bootstrapObject == null )
        {
            throw new NullPointerException( "bootstrapObject" );
        }
        if ( transformer == null )
        {
            throw new NullPointerException( "transformer" );
        }

        final JAXBContext ctx = this.getBootstrapContext();
        final JAXBSource source = new JAXBSource( ctx, bootstrapObject );
        final JAXBResult result = new JAXBResult( ctx );
        transformer.transform( source, result );
        return ( (JAXBElement<T>) result.getResult() ).getValue();
    }

    /**
     * Sets the object factory of the instance.
     *
     * @param value The new object factory of the instance.
     *
     * @see #getObjectFactory()
     */
    public void setObjectFactory( final ObjectFactory value )
    {
        this.objectFactory = value;
    }

    /**
     * Sets the entity resolver of the instance.
     *
     * @param value The new entity resolver of the instance.
     *
     * @see #getEntityResolver()
     */
    public void setEntityResolver( final EntityResolver value )
    {
        this.entityResolver = value;
    }

    /**
     * Sets the L/S resolver of the instance.
     *
     * @param value The new L/S resolver of the instance.
     *
     * @see #getLSResourceResolver()
     */
    public void setLSResourceResolver( final LSResourceResolver value )
    {
        this.resourceResolver = value;
    }

    /**
     * Gets the list of registered listeners.
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
     * Gets the location to search for bootstrap documents.
     * <p>The bootstrap document location is controlled by system property
     * {@code org.jomc.model.DefaultModelManager.bootstrapDocumentLocation} holding the location to search at. If that
     * property is not set, the {@code META-INF/jomc-bootstrap.xml} default is returned.</p>
     *
     * @return The location to search for bootstrap documents.
     *
     * @see #getSchemas()
     */
    public String getBootstrapDocumentLocation()
    {
        return System.getProperty( "org.jomc.model.DefaultModelManager.bootstrapDocumentLocation",
                                   DEFAULT_BOOTSTRAP_DOCUMENT_LOCATION );

    }

    /**
     * Gets the schemas backing the instance.
     *
     * @return The schemas backing the instance.
     *
     * @throws IOException if reading schema resources fails.
     * @throws SAXException if parsing schema resources fails.
     * @throws JAXBException if unmarshalling schema resources fails.
     *
     * @see #getBootstrapDocumentLocation()
     */
    public Schemas getSchemas() throws IOException, JAXBException, SAXException
    {
        if ( this.schemas == null )
        {
            this.schemas = new Schemas();

            final JAXBContext ctx = JAXBContext.newInstance( BOOTSTRAP_CONTEXT, this.getClassLoader() );
            final Unmarshaller u = ctx.createUnmarshaller();
            final String bootstrapLocation = this.getBootstrapDocumentLocation();
            this.log( Level.FINE, this.getMessage( "bootstrapLocation", new Object[]
                {
                    bootstrapLocation
                } ), null );

            final Enumeration<URL> e = this.getClassLoader().getResources( bootstrapLocation );
            u.setSchema( this.getBootstrapSchema() );

            while ( e.hasMoreElements() )
            {
                final URL url = e.nextElement();
                this.log( Level.FINE, this.getMessage( "processing", new Object[]
                    {
                        url.toExternalForm()
                    } ), null );

                final Object content = u.unmarshal( url );
                if ( content instanceof JAXBElement )
                {
                    final JAXBElement element = (JAXBElement) content;
                    if ( element.getValue() instanceof Schema )
                    {
                        final Schema schema = (Schema) element.getValue();
                        this.log( Level.FINE, this.getMessage( "addingSchema", new Object[]
                            {
                                schema.getPublicId(), schema.getSystemId(), schema.getContextId(),
                                schema.getClasspathId()
                            } ), null );

                        this.schemas.getSchema().add( (Schema) element.getValue() );
                    }
                    else if ( element.getValue() instanceof Schemas )
                    {
                        for ( Schema schema : ( (Schemas) element.getValue() ).getSchema() )
                        {
                            this.log( Level.FINE, this.getMessage( "addingSchema", new Object[]
                                {
                                    schema.getPublicId(), schema.getSystemId(), schema.getContextId(),
                                    schema.getClasspathId()
                                } ), null );

                            this.schemas.getSchema().add( schema );
                        }
                    }
                }
            }
        }

        return this.schemas;
    }

    /**
     * Gets the default location to search for documents.
     * <p>The default document location is controlled by system property
     * {@code org.jomc.model.DefaultModelManager.defaultDocumentLocation} holding the location to search at by default.
     * If that property is not set, the {@code META-INF/jomc.xml} default is returned.</p>
     *
     * @return The default location to search for documents.
     *
     * @see #getClasspathModules(java.lang.String)
     */
    public String getDefaultDocumentLocation()
    {
        return System.getProperty( "org.jomc.model.DefaultModelManager.defaultDocumentLocation",
                                   DEFAULT_DOCUMENT_LOCATION );

    }

    /**
     * Gets modules by searching the class loader of the instance for resources.
     * <p><b>Note:</b><br/>
     * This method does not validate the modules.</p>
     *
     * @param location The location to search at.
     *
     * @return All resources from the class loader of the instance matching {@code location}.
     *
     * @throws NullPointerException if {@code location} is {@code null}.
     * @throws IOException if reading resources fails.
     * @throws SAXException if parsing schema resources fails.
     * @throws JAXBException if unmarshalling schema resources fails.
     *
     * @see #getDefaultDocumentLocation()
     */
    public Modules getClasspathModules( final String location ) throws IOException, SAXException, JAXBException
    {
        if ( location == null )
        {
            throw new NullPointerException( "location" );
        }

        this.log( Level.FINE, this.getMessage( "documentLocation", new Object[]
            {
                location
            } ), null );

        final Text text = new Text();
        text.setLanguage( "en" );
        text.setValue( this.getMessage( "classpathModulesInfo", new Object[]
            {
                location
            } ) );

        Modules mods = new Modules();
        mods.setDocumentation( new Texts() );
        mods.getDocumentation().setDefaultLanguage( "en" );
        mods.getDocumentation().getText().add( text );

        final Unmarshaller u = this.getUnmarshaller( false );
        final Enumeration<URL> resources = this.getClassLoader().getResources( location );

        while ( resources.hasMoreElements() )
        {
            final URL url = resources.nextElement();

            this.log( Level.FINE, this.getMessage( "processing", new Object[]
                {
                    url.toExternalForm()
                } ), null );

            final Object content = ( (JAXBElement) u.unmarshal( url ) ).getValue();

            if ( content instanceof Module )
            {
                mods.getModule().add( (Module) content );
            }
            else if ( content instanceof Modules )
            {
                this.log( Level.FINE, this.getMessage( "usingModules", new Object[]
                    {
                        ( mods.getDocumentation() != null
                          ? mods.getDocumentation().getText( Locale.getDefault().getLanguage() ).getValue()
                          : "<>" ),
                        url.toExternalForm()
                    } ), null );

                mods = (Modules) content;
                break;
            }
        }

        return mods;
    }

    /**
     * Gets the classpath module name.
     * <p>The classpath module name is controlled by system property
     * {@code org.jomc.model.DefaultModelManager.classpathModuleName} holding the classpath module name.
     * If that property is not set, the {@code Java Classpath} default is returned.</p>
     *
     * @return The name of the classpath module.
     *
     * @see #getClasspathModule(org.jomc.model.Modules)
     */
    public String getClasspathModuleName()
    {
        return System.getProperty( "org.jomc.model.DefaultModelManager.classpathModuleName",
                                   DEFAULT_CLASSPATH_MODULE_NAME );

    }

    /**
     * Gets a module holding model objects resolved by inspecting the class loader of the instance.
     * <p>This method searches the given modules for unresolved references and tries to resolve each unresolved
     * reference by inspecting the class loader of the instance.</p>
     *
     * @param modules The modules to resolve by inspecting the class loader of the instance.
     *
     * @return A module holding model objects resolved by inspecting the class loader of the instance or {@code null} if
     * nothing could be resolved.
     *
     * @see #getClasspathModuleName()
     */
    public Module getClasspathModule( final Modules modules )
    {
        final Module module = new Module();
        module.setVersion( System.getProperty( "java.specification.version" ) );
        module.setName( this.getClasspathModuleName() );

        this.resolveClasspath( modules, module );

        final boolean resolved = ( module.getSpecifications() != null &&
                                   !module.getSpecifications().getSpecification().isEmpty() ) ||
                                 ( module.getImplementations() != null &&
                                   !module.getImplementations().getImplementation().isEmpty() );

        return resolved ? module : null;
    }

    /**
     * Gets the default location to search for style sheets.
     * <p>The default style sheet location is controlled by system property
     * {@code org.jomc.model.DefaultModelManager.defaultStylesheetLocation} holding the location to search at by
     * default. If that property is not set, the {@code META-INF/jomc.xslt} default is returned.</p>
     *
     * @return The default location to search for style sheets.
     *
     * @see #getClasspathTransformers(java.lang.String)
     */
    public String getDefaultStylesheetLocation()
    {
        return System.getProperty( "org.jomc.model.DefaultModelManager.defaultStylesheetLocation",
                                   DEFAULT_STYLESHEET_LOCATION );

    }

    /**
     * Gets transformers by searching the class loader of the instance for resources.
     *
     * @param location The location to search at.
     *
     * @return All resources from the class loader of the instance matching {@code location}.
     *
     * @throws NullPointerException if {@code location} is {@code null}.
     * @throws IOException if reading resources fails.
     * @throws TransformerConfigurationException if getting the transformers fails.
     *
     * @see #getDefaultStylesheetLocation()
     */
    public List<Transformer> getClasspathTransformers( final String location )
        throws IOException, TransformerConfigurationException
    {
        if ( location == null )
        {
            throw new NullPointerException( "location" );
        }

        this.log( Level.FINE, this.getMessage( "stylesheetLocation", new Object[]
            {
                location
            } ), null );

        final List<Transformer> transformers = new LinkedList<Transformer>();
        final TransformerFactory transformerFactory = TransformerFactory.newInstance();
        final Enumeration<URL> resources = this.getClassLoader().getResources( location );
        final ErrorListener errorListener = new ErrorListener()
        {

            public void warning( final TransformerException exception ) throws TransformerException
            {
                log( Level.WARNING, exception.getMessage(), exception );
            }

            public void error( final TransformerException exception ) throws TransformerException
            {
                log( Level.SEVERE, exception.getMessage(), exception );
                throw exception;
            }

            public void fatalError( final TransformerException exception ) throws TransformerException
            {
                log( Level.SEVERE, exception.getMessage(), exception );
                throw exception;
            }

        };

        final URIResolver uriResolver = new URIResolver()
        {

            public Source resolve( final String href, final String base ) throws TransformerException
            {
                try
                {
                    Source source = null;
                    final InputSource inputSource = getEntityResolver().resolveEntity( null, href );

                    if ( inputSource != null )
                    {
                        source = new SAXSource( inputSource );
                    }

                    return source;
                }
                catch ( SAXException e )
                {
                    log( Level.SEVERE, e.getMessage(), e );
                    throw new TransformerException( e );
                }
                catch ( IOException e )
                {
                    log( Level.SEVERE, e.getMessage(), e );
                    throw new TransformerException( e );
                }
            }

        };

        transformerFactory.setErrorListener( errorListener );
        transformerFactory.setURIResolver( uriResolver );

        while ( resources.hasMoreElements() )
        {
            final URL url = resources.nextElement();

            this.log( Level.FINE, this.getMessage( "processing", new Object[]
                {
                    url.toExternalForm()
                } ), null );

            final InputStream in = url.openStream();
            final Transformer transformer = transformerFactory.newTransformer( new StreamSource( in ) );
            in.close();

            transformer.setErrorListener( errorListener );
            transformer.setURIResolver( uriResolver );
            transformers.add( transformer );
        }

        return transformers;
    }

    /**
     * Gets the class loader of the instance.
     *
     * @return The class loader of the instance.
     *
     * @see #setClassLoader(java.lang.ClassLoader)
     */
    public ClassLoader getClassLoader()
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

    /**
     * Sets the class loader of the instance.
     *
     * @param value The new class loader of the instance.
     *
     * @see #getClassLoader()
     */
    public void setClassLoader( final ClassLoader value )
    {
        this.classLoader = value;
        this.bootstrapSchema = null;
        this.schemaResources = null;
        this.schemas = null;
        this.entityResolver = null;
        this.resourceResolver = null;
    }

    /**
     * Notifies registered listeners.
     *
     * @param level The level of the event.
     * @param message The message of the event.
     * @param throwable The throwable of the event.
     *
     * @see #getListeners()
     */
    protected void log( final Level level, final String message, final Throwable throwable )
    {
        for ( Listener l : this.getListeners() )
        {
            l.onLog( level, message, throwable );
        }
    }

    /**
     * Resolves references by inspecting the class loader of the instance.
     *
     * @param modules The modules to resolve.
     * @param cpModule The module for resolved references.
     *
     * @throws NullPointerException if {@code cpModule} is {@code null}.
     */
    private void resolveClasspath( final Modules modules, final Module cpModule )
    {
        for ( Module m : modules.getModule() )
        {
            if ( m.getSpecifications() != null )
            {
                this.resolveClasspath( modules, m.getSpecifications(), cpModule );
            }

            if ( m.getImplementations() != null )
            {
                this.resolveClasspath( modules, m.getImplementations(), cpModule );
            }

        }
    }

    private void resolveClasspath( final Modules modules, final SpecificationReference ref, final Module cpModule )
    {
        if ( modules.getSpecification( ref.getIdentifier() ) == null )
        {
            this.resolveClasspath( ref.getIdentifier(), cpModule );
        }
    }

    private void resolveClasspath( final Modules modules, final Specifications references, final Module cpModule )
    {
        for ( SpecificationReference ref : references.getReference() )
        {
            this.resolveClasspath( modules, ref, cpModule );
        }

    }

    private void resolveClasspath( final Modules modules, final Implementations implementations, final Module cpModule )
    {
        for ( Implementation implementation : implementations.getImplementation() )
        {
            if ( implementation.getSpecifications() != null )
            {
                this.resolveClasspath( modules, implementation.getSpecifications(), cpModule );
            }

            if ( implementation.getDependencies() != null )
            {
                this.resolveClasspath( modules, implementation.getDependencies(), cpModule );
            }
        }
    }

    private void resolveClasspath( final Modules modules, final Dependencies dependencies, final Module cpModule )
    {
        for ( Dependency dependency : dependencies.getDependency() )
        {
            this.resolveClasspath( modules, dependency, cpModule );
        }
    }

    private void resolveClasspath( final String identifier, final Module cpModule )
    {
        Specification specification =
            cpModule.getSpecifications() == null ? null
            : cpModule.getSpecifications().getSpecification( identifier );

        if ( specification == null )
        {
            try
            {
                final Class classpathSpec = Class.forName( identifier, true, this.getClassLoader() );
                if ( Modifier.isPublic( classpathSpec.getModifiers() ) )
                {
                    String vendor = null;
                    String version = null;

                    if ( classpathSpec.getPackage() != null )
                    {
                        vendor = classpathSpec.getPackage().getSpecificationVendor();
                        version = classpathSpec.getPackage().getSpecificationVersion();
                    }

                    specification = new Specification();
                    specification.setIdentifier( identifier );
                    specification.setMultiplicity( Multiplicity.MANY );
                    specification.setVendor( vendor );
                    specification.setVersion( version );

                    this.log( Level.FINE, this.getMessage( "classpathSpecification", new Object[]
                        {
                            specification.getIdentifier(),
                            specification.getMultiplicity().value()
                        } ), null );


                    if ( cpModule.getSpecifications() == null )
                    {
                        cpModule.setSpecifications( new Specifications() );
                    }

                    cpModule.getSpecifications().getSpecification().add( specification );

                    this.resolveClasspath( specification, cpModule );
                }

            }
            catch ( ClassNotFoundException e )
            {
                this.log( Level.FINE, this.getMessage( "noSuchClass", new Object[]
                    {
                        e.getMessage()
                    } ), null );

            }
        }
    }

    private void resolveClasspath( final Specification specification, final Module cpModule )
    {
        if ( specification == null )
        {
            throw new NullPointerException( "specification" );
        }

        Implementation implementation =
            cpModule.getImplementations() == null ? null
            : cpModule.getImplementations().getImplementation( specification.getIdentifier() );

        if ( implementation == null )
        {
            String name = null;

            try
            {
                final Class classpathImpl = Class.forName( specification.getIdentifier(), true, this.getClassLoader() );
                boolean classpathImplementation = false;

                if ( Modifier.isPublic( classpathImpl.getModifiers() ) )
                {
                    if ( !Modifier.isAbstract( classpathImpl.getModifiers() ) )
                    {
                        try
                        {
                            classpathImpl.getConstructor( new Class[ 0 ] );
                            name = "init";
                            classpathImplementation = true;
                        }
                        catch ( NoSuchMethodException e )
                        {
                            this.log( Level.FINE, this.getMessage( "noSuchMethod", new Object[]
                                {
                                    e.getMessage()
                                } ), null );

                        }

                    }

                    if ( !classpathImplementation )
                    {
                        final char[] c = specification.getIdentifier().substring(
                            specification.getIdentifier().lastIndexOf( '.' ) + 1 ).toCharArray();

                        name = String.valueOf( c );
                        c[0] = Character.toUpperCase( c[0] );

                        if ( this.checkFactoryMethod( classpathImpl, classpathImpl, "getDefault" ) )
                        {
                            name = "default";
                            classpathImplementation = true;
                        }
                        else if ( this.checkFactoryMethod( classpathImpl, classpathImpl, "getInstance" ) )
                        {
                            name = "instance";
                            classpathImplementation = true;
                        }
                        else if ( this.checkFactoryMethod( classpathImpl, classpathImpl, "get" + String.valueOf( c ) ) )
                        {
                            classpathImplementation = true;
                        }

                    }

                    if ( classpathImplementation )
                    {
                        String vendor = null;
                        String version = null;
                        if ( classpathImpl.getPackage() != null )
                        {
                            vendor = classpathImpl.getPackage().getImplementationVendor();
                            version = classpathImpl.getPackage().getImplementationVersion();
                        }

                        implementation = new Implementation();
                        implementation.setVendor( vendor );
                        implementation.setFinal( true );
                        implementation.setName( name );
                        implementation.setIdentifier( specification.getIdentifier() );
                        implementation.setClazz( specification.getIdentifier() );
                        implementation.setScope( "Multiton" );
                        implementation.setVersion( version );

                        final Specifications implemented = new Specifications();
                        final SpecificationReference ref = new SpecificationReference();
                        ref.setIdentifier( specification.getIdentifier() );
                        ref.setVersion( specification.getVersion() );
                        implemented.getReference().add( ref );
                        implementation.setSpecifications( implemented );

                        this.log( Level.FINE, this.getMessage( "classpathImplementation", new Object[]
                            {
                                implementation.getIdentifier(),
                                specification.getIdentifier(),
                                implementation.getName()
                            } ), null );

                        if ( cpModule.getImplementations() == null )
                        {
                            cpModule.setImplementations( new Implementations() );
                        }

                        cpModule.getImplementations().getImplementation().add( implementation );
                    }
                    else
                    {
                        this.log( Level.FINE, this.getMessage( "noClasspathImplementation", new Object[]
                            {
                                specification.getIdentifier()
                            } ), null );

                    }
                }
            }
            catch ( ClassNotFoundException e )
            {
                this.log( Level.FINE, this.getMessage( "noSuchClass", new Object[]
                    {
                        e.getMessage()
                    } ), null );

            }
        }
    }

    private boolean checkFactoryMethod( final Class clazz, final Class type, final String methodName )
    {
        boolean factoryMethod = false;

        try
        {
            final Method m = clazz.getMethod( methodName, new Class[ 0 ] );
            factoryMethod = Modifier.isStatic( m.getModifiers() ) && type.isAssignableFrom( m.getReturnType() );
        }
        catch ( NoSuchMethodException e )
        {
            this.log( Level.FINE, this.getMessage( "noSuchMethod", new Object[]
                {
                    e.getMessage()
                } ), null );

            factoryMethod = false;
        }

        return factoryMethod;
    }

    private Method getFactoryMethod( final Class clazz, final String methodName )
    {
        Method m = null;

        try
        {
            m = clazz.getMethod( methodName, NO_CLASSES );
        }
        catch ( NoSuchMethodException e )
        {
            this.log( Level.FINE, this.getMessage( "noSuchMethod", new Object[]
                {
                    e.getMessage()
                } ), null );

            m = null;
        }

        return m;
    }

    /**
     * Searches all available {@code META-INF/MANIFEST.MF} resources and gets a set containing URLs of entries whose
     * name end with a known schema extension.
     *
     * @return URLs of any matching entries.
     *
     * @throws IOException if reading or parsing fails.
     */
    private Set<URL> getSchemaResources() throws IOException
    {
        if ( this.schemaResources == null )
        {
            this.schemaResources = new HashSet<URL>();

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
                            this.log( Level.FINE, this.getMessage( "processing", new Object[]
                                {
                                    schemaUrl.toExternalForm()
                                } ), null );

                        }
                    }
                }
            }
        }

        return this.schemaResources;
    }

    /**
     * Gets the implementation of an object.
     *
     * @param object The object to get the implementation for.
     *
     * @return The implementation for {@code object} or {@code null}, if nothing is known about {@code object}.
     */
    private Implementation getImplementation( final Modules modules, final Object object )
    {
        return this.collectImplementation( modules, object.getClass() );
    }

    private Implementation collectImplementation( final Modules modules, final Class clazz )
    {
        Implementation i = modules.getImplementation( clazz );
        if ( i == null && clazz.getSuperclass() != null )
        {
            i = this.collectImplementation( modules, clazz.getSuperclass() );
        }

        return i;
    }

    private String getMessage( final String key, final Object args )
    {
        return new MessageFormat(
            ResourceBundle.getBundle( DefaultModelManager.class.getName().replace( '.', '/' ), Locale.getDefault() ).
            getString( key ) ).format( args );

    }

    private String getPropertyOverwriteConstraintMessage( final String instance,
                                                          final String scope,
                                                          final String dependency )
    {
        return this.getMessage( "propertyOverwriteConstraint", new Object[]
            {
                instance, scope, dependency
            } );

    }

    private void assertMessagesUniqueness( final Messages messages, final List<ModelException.Detail> details )
    {
        for ( Message m : messages.getMessage() )
        {
            if ( messages.getReference( m.getName() ) != null )
            {
                final ModelException.Detail detail = new ModelException.Detail( Level.SEVERE, this.getMessage(
                    "messagesUniquessConstraint", new Object[]
                    {
                        m.getName()
                    } ) );

                detail.setElement( this.getObjectFactory().createMessages( messages ) );
                details.add( detail );
            }
        }
    }

    private void assertPropertiesUniqueness( final Properties properties, final List<ModelException.Detail> details )
    {
        for ( Property p : properties.getProperty() )
        {
            if ( properties.getReference( p.getName() ) != null )
            {
                final ModelException.Detail detail = new ModelException.Detail( Level.SEVERE, this.getMessage(
                    "propertiesUniquessConstraint", new Object[]
                    {
                        p.getName()
                    } ) );

                detail.setElement( this.getObjectFactory().createProperties( properties ) );
                details.add( detail );
            }
        }
    }

    private ModelException.Detail newIncompatibleImplementationDetail( final JAXBElement element,
                                                                       final String implementation,
                                                                       final String specification,
                                                                       final String implementedVersion,
                                                                       final String specifiedVersion )
    {
        final ModelException.Detail detail =
            new ModelException.Detail( Level.SEVERE, this.getMessage( "incompatibleImplementation", new Object[]
            {
                implementation, specification, implementedVersion, specifiedVersion
            } ) );

        detail.setElement( element );
        return detail;
    }

    private ModelException.Detail newIncompatibleDependencyDetail( final JAXBElement element,
                                                                   final String implementation,
                                                                   final String specification,
                                                                   final String requiredVersion,
                                                                   final String availableVersion )
    {
        final ModelException.Detail detail =
            new ModelException.Detail( Level.SEVERE, this.getMessage( "incompatibleDependency", new Object[]
            {
                implementation, specification, requiredVersion, availableVersion
            } ) );

        detail.setElement( element );
        return detail;
    }

    private ModelException.Detail newImplementationNameConstraintDetail( final JAXBElement element,
                                                                         final String specification,
                                                                         final String implementations )
    {
        final ModelException.Detail detail =
            new ModelException.Detail( Level.SEVERE, this.getMessage( "implementationNameConstraint", new Object[]
            {
                specification, implementations
            } ) );

        detail.setElement( element );
        return detail;
    }

    private ModelException.Detail newMandatoryDependencyConstraintDetail( final JAXBElement element,
                                                                          final String implementation,
                                                                          final String dependencyName )
    {
        final ModelException.Detail detail =
            new ModelException.Detail( Level.SEVERE, this.getMessage( "mandatoryDependencyConstraint", new Object[]
            {
                implementation, dependencyName
            } ) );

        detail.setElement( element );
        return detail;
    }

    private ModelException.Detail newMultiplicityConstraintDetail( final JAXBElement element,
                                                                   final Number implementations,
                                                                   final String specification,
                                                                   final Number expected,
                                                                   final Multiplicity multiplicity )
    {
        final ModelException.Detail detail =
            new ModelException.Detail( Level.SEVERE, this.getMessage( "multiplicityConstraint", new Object[]
            {
                implementations, specification, expected, multiplicity.value()
            } ) );

        detail.setElement( element );
        return detail;
    }

    private ModelException.Detail newInheritanceConstraintDetail( final JAXBElement element,
                                                                  final Implementation child,
                                                                  final Implementation parent )
    {
        final ModelException.Detail detail =
            new ModelException.Detail( Level.SEVERE, this.getMessage( "inheritanceConstraint", new Object[]
            {
                child.getIdentifier(), parent.getIdentifier()
            } ) );

        detail.setElement( element );
        return detail;
    }

    private ModelException.Detail newDependencyPropertyReferenceConstraintDetail( final JAXBElement element,
                                                                                  final Implementation implementation,
                                                                                  final Dependency dependency )
    {
        final ModelException.Detail detail = new ModelException.Detail(
            Level.SEVERE, this.getMessage( "dependencyPropertyReferenceConstraint", new Object[]
            {
                implementation.getIdentifier(), dependency.getName()
            } ) );

        detail.setElement( element );
        return detail;
    }

    // SECTION-END
}
