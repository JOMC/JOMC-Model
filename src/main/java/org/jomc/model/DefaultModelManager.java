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
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

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
 * @author <a href="mailto:cs@jomc.org">Christian Schulte</a>
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
                    catch ( final URISyntaxException e )
                    {
                        log( Level.WARNING, getMessage( "unsupportedSystemIdUri", new Object[]
                            {
                                systemId, e.getMessage()
                            } ), null );

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
                    catch ( final SAXException e )
                    {
                        log( Level.WARNING, getMessage( "unsupportedSystemIdUri", new Object[]
                            {
                                systemId, e.getMessage()
                            } ), null );

                        input = null;
                    }
                    catch ( final IOException e )
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
        if ( this.schema == null )
        {
            final SchemaFactory f = SchemaFactory.newInstance( XMLConstants.W3C_XML_SCHEMA_NS_URI );
            final List<Source> sources = new ArrayList<Source>( this.getSchemas().getSchema().size() );

            for ( Schema s : this.getSchemas().getSchema() )
            {
                sources.add( new StreamSource( this.getClassLoader().getResourceAsStream( s.getClasspathId() ),
                                               s.getSystemId() ) );

            }

            this.schema = f.newSchema( sources.toArray( new Source[ sources.size() ] ) );
        }

        return this.schema;
    }

    public JAXBContext getContext() throws IOException, SAXException, JAXBException
    {
        if ( this.context == null )
        {
            final StringBuilder pkgs = new StringBuilder();

            for ( final Iterator<Schema> s = this.getSchemas().getSchema().iterator(); s.hasNext(); )
            {
                pkgs.append( s.next().getContextId() );
                if ( s.hasNext() )
                {
                    pkgs.append( ':' );
                }
            }

            if ( pkgs.length() == 0 )
            {
                throw new IOException( this.getMessage( "missingSchemas", new Object[]
                    {
                        this.getBootstrapDocumentLocation()
                    } ) );

            }

            this.context = JAXBContext.newInstance( pkgs.toString(), this.getClassLoader() );
        }

        return this.context;
    }

    public Marshaller getMarshaller( final boolean validating, final boolean formattedOutput )
        throws IOException, SAXException, JAXBException
    {
        final Marshaller m = this.getContext().createMarshaller();
        final StringBuilder schemaLocation = new StringBuilder();

        for ( final Iterator<Schema> it = this.getSchemas().getSchema().iterator(); it.hasNext(); )
        {
            final Schema s = it.next();
            schemaLocation.append( s.getPublicId() ).append( ' ' ).append( s.getSystemId() );
            if ( it.hasNext() )
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
        final Validator validator = this.getSchema().newValidator();
        final ModelExceptionErrorHandler errorHandler = new ModelExceptionErrorHandler();
        validator.setErrorHandler( errorHandler );
        this.getMarshaller( false, false ).marshal( modelObject, stringWriter );

        try
        {
            validator.validate( new StreamSource( new StringReader( stringWriter.toString() ) ) );
        }
        catch ( final SAXException e )
        {
            final ModelException modelException = new ModelException( this.getMessage( "validationFailed", null ), e );
            modelException.getDetails().addAll( errorHandler.getDetails() );
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
                if ( m.getSpecifications() != null )
                {
                    for ( SpecificationReference r : m.getSpecifications().getReference() )
                    {
                        details.add( this.newModuleSpecificationReferenceDeclarationConstraintDetail(
                            this.getObjectFactory().createModule( m ), m, r ) );

                    }
                }

                if ( m.getImplementations() != null )
                {
                    for ( ImplementationReference r : m.getImplementations().getReference() )
                    {
                        details.add( this.newModuleImplementationReferenceDeclarationConstraintDetail(
                            this.getObjectFactory().createImplementations( m.getImplementations() ), m, r ) );

                    }
                }

                if ( m.getMessages() != null )
                {
                    for ( Message msg : m.getMessages().getMessage() )
                    {
                        if ( msg.isFinal() )
                        {
                            details.add( this.newFinalModuleMessageConstraintDetail(
                                this.getObjectFactory().createMessage( msg ), m, msg ) );

                        }
                        if ( msg.isOverride() )
                        {
                            details.add( this.newOverrideModuleMessageConstraintDetail(
                                this.getObjectFactory().createMessage( msg ), m, msg ) );

                        }
                    }
                    for ( MessageReference r : m.getMessages().getReference() )
                    {
                        details.add( this.newModuleMessageReferenceDeclarationConstraintDetail(
                            this.getObjectFactory().createMessages( m.getMessages() ), m, r ) );

                    }
                }

                if ( m.getProperties() != null )
                {
                    for ( Property p : m.getProperties().getProperty() )
                    {
                        if ( p.isFinal() )
                        {
                            details.add( this.newFinalModulePropertyConstraintDetail(
                                this.getObjectFactory().createProperty( p ), m, p ) );

                        }
                        if ( p.isOverride() )
                        {
                            details.add( this.newOverrideModulePropertyConstraintDetail(
                                this.getObjectFactory().createProperty( p ), m, p ) );

                        }
                    }
                    for ( PropertyReference r : m.getProperties().getReference() )
                    {
                        details.add( this.newModulePropertyReferenceDeclarationConstraintDetail(
                            this.getObjectFactory().createProperties( m.getProperties() ), m, r ) );

                    }
                }

                if ( m.getImplementations() != null )
                {
                    for ( Implementation i : m.getImplementations().getImplementation() )
                    {
                        this.assertImplementationMessagesUniqueness( i, details );
                        this.assertImplementationPropertiesUniqueness( i, details );

                        if ( i.getImplementations() != null )
                        {
                            for ( Implementation decl : i.getImplementations().getImplementation() )
                            {
                                details.add( this.newImplementationImplementationDeclarationConstraintDetail(
                                    this.getObjectFactory().createImplementations( i.getImplementations() ), i, decl ) );

                            }
                        }

                        if ( i.getSpecifications() != null )
                        {
                            for ( Specification s : i.getSpecifications().getSpecification() )
                            {
                                details.add( this.newImplementationSpecificationDeclarationConstraintDetail(
                                    this.getObjectFactory().createImplementation( i ), i, s ) );

                            }
                        }

                        if ( i.isAbstract() && i.getLocation() != null )
                        {
                            details.add( this.newAbstractLocationConstraintDetail(
                                this.getObjectFactory().createImplementation( i ), i, i.getLocation() ) );

                        }

                        final Implementation cycle = this.findInheritanceCycle( modules, i, i, new Implementations() );
                        if ( cycle != null )
                        {
                            details.add( this.newImplementationInheritanceCycleConstraintDetail(
                                this.getObjectFactory().createImplementation( i ), i, cycle ) );

                        }

                        final Specifications specs = modules.getSpecifications( i.getIdentifier() );
                        final Dependencies deps = modules.getDependencies( i.getIdentifier() );

                        if ( specs != null )
                        {
                            for ( SpecificationReference r : specs.getReference() )
                            {
                                final Specification s = specs.getSpecification( r.getIdentifier() );

                                if ( s != null && r.getVersion() != null )
                                {
                                    if ( s.getVersion() == null )
                                    {
                                        details.add( this.newSpecificationVersioningConstraintDetail(
                                            this.getObjectFactory().createSpecifications( specs ), i, s ) );

                                    }
                                    else if ( VersionParser.compare( r.getVersion(), s.getVersion() ) != 0 )
                                    {
                                        final Module moduleOfSpecification =
                                            modules.getModuleOfSpecification( s.getIdentifier() );

                                        details.add( this.newIncompatibleImplementationDetail(
                                            this.getObjectFactory().createImplementation( i ),
                                            i.getIdentifier(), m.getName(),
                                            r.getIdentifier(), moduleOfSpecification == null
                                                               ? "<>" : moduleOfSpecification.getName(),
                                            r.getVersion(), s.getVersion() ) );

                                    }
                                }
                            }
                        }

                        if ( deps != null )
                        {
                            for ( Dependency d : deps.getDependency() )
                            {
                                final Specification s = modules.getSpecification( d.getIdentifier() );

                                if ( s != null )
                                {
                                    if ( d.getVersion() != null )
                                    {
                                        if ( s.getVersion() == null )
                                        {
                                            details.add( this.newSpecificationVersioningConstraintDetail(
                                                this.getObjectFactory().createDependency( d ), i, s ) );

                                        }
                                        else if ( VersionParser.compare( d.getVersion(), s.getVersion() ) > 0 )
                                        {
                                            final Module moduleOfSpecification =
                                                modules.getModuleOfSpecification( s.getIdentifier() );

                                            details.add( this.newIncompatibleDependencyDetail(
                                                this.getObjectFactory().createDependency( d ),
                                                i.getIdentifier(), m.getName(),
                                                d.getIdentifier(), moduleOfSpecification == null
                                                                   ? "<>" : moduleOfSpecification.getName(),
                                                d.getVersion(), s.getVersion() ) );

                                        }
                                    }

                                    if ( d.getProperties() != null )
                                    {
                                        for ( PropertyReference r : d.getProperties().getReference() )
                                        {
                                            details.add( this.newDependencyPropertyReferenceDeclarationConstraintDetail(
                                                this.getObjectFactory().createDependency( d ), i, d, r ) );

                                        }

                                        if ( s.getScope() != null )
                                        {
                                            for ( Property p : d.getProperties().getProperty() )
                                            {
                                                details.add( this.newDependencyPropertiesOverrideConstraintDetail(
                                                    this.getObjectFactory().createDependency( d ), i, d, s, p ) );

                                            }
                                        }
                                    }
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

                        if ( i.getImplementations() != null )
                        {
                            final Implementations finalSuperImplementations = new Implementations();
                            this.collectFinalSuperImplementations( modules, i, finalSuperImplementations,
                                                                   new Implementations(), false );

                            for ( Implementation finalSuper : finalSuperImplementations.getImplementation() )
                            {
                                details.add( this.newImplementationInheritanceConstraintDetail(
                                    this.getObjectFactory().createImplementation( i ), i, finalSuper ) );

                            }

                            for ( ImplementationReference r : i.getImplementations().getReference() )
                            {
                                final Implementation referenced = modules.getImplementation( r.getIdentifier() );
                                if ( referenced != null && r.getVersion() != null )
                                {
                                    if ( referenced.getVersion() == null )
                                    {
                                        details.add( this.newImplementationVersioningConstraintDetail(
                                            this.getObjectFactory().createImplementations( i.getImplementations() ),
                                            i, referenced ) );

                                    }
                                    else if ( VersionParser.compare( r.getVersion(), referenced.getVersion() ) > 0 )
                                    {
                                        details.add( this.newImplementationInheritanceCompatibilityConstraintDetail(
                                            this.getObjectFactory().createImplementation( i ), i, referenced,
                                            r.getVersion() ) );

                                    }
                                }
                            }
                        }

                        if ( i.getSpecifications() != null )
                        {
                            final Specifications superSpecifications = new Specifications();
                            modules.collectSpecifications( i, superSpecifications, new Implementations(), false );

                            for ( SpecificationReference r : i.getSpecifications().getReference() )
                            {
                                boolean override = false;

                                if ( i.getImplementations() != null )
                                {
                                    final Implementations finalSuperSpecifications = new Implementations();
                                    this.collectFinalSuperSpecifications( modules, i, r.getIdentifier(),
                                                                          finalSuperSpecifications,
                                                                          new Implementations(), false );

                                    for ( Implementation finalSuper : finalSuperSpecifications.getImplementation() )
                                    {
                                        details.add( this.newSpecificationInheritanceConstraintDetail(
                                            this.getObjectFactory().createImplementation( i ), i, r, finalSuper ) );

                                    }

                                    override = superSpecifications.getReference( r.getIdentifier() ) != null;
                                }

                                if ( r.isOverride() && !override )
                                {
                                    details.add( this.newSpecificationOverrideConstraintDetail(
                                        this.getObjectFactory().createSpecifications( i.getSpecifications() ), i, r ) );

                                }
                                if ( !r.isOverride() && override )
                                {
                                    this.log( Level.WARNING, this.getMessage( "specificationOverrideWarning",
                                                                              new Object[]
                                        {
                                            i.getIdentifier(), r.getIdentifier()
                                        } ), null );

                                }
                            }
                        }

                        if ( i.getDependencies() != null )
                        {
                            final Dependencies superDependencies = new Dependencies();
                            modules.collectDependencies( i, superDependencies, new Implementations(), false );

                            for ( Dependency d : i.getDependencies().getDependency() )
                            {
                                boolean override = false;

                                if ( i.getImplementations() != null )
                                {
                                    final Implementations finalSuperDependencies = new Implementations();
                                    this.collectFinalSuperDependencies( modules, i, d.getName(), finalSuperDependencies,
                                                                        new Implementations(), false );

                                    for ( Implementation finalSuper : finalSuperDependencies.getImplementation() )
                                    {
                                        details.add( this.newDependencyInheritanceConstraintDetail(
                                            this.getObjectFactory().createImplementation( i ), i, d, finalSuper ) );

                                    }

                                    override = superDependencies.getDependency( d.getName() ) != null;
                                }

                                if ( d.isOverride() && !override )
                                {
                                    details.add( this.newDependencyOverrideConstraintDetail(
                                        this.getObjectFactory().createDependency( d ), i, d ) );

                                }
                                if ( !d.isOverride() && override )
                                {
                                    this.log( Level.WARNING, this.getMessage( "dependencyOverrideWarning", new Object[]
                                        {
                                            i.getIdentifier(), d.getName()
                                        } ), null );

                                }
                            }
                        }

                        if ( i.getProperties() != null )
                        {
                            final Properties superProperties = new Properties();
                            modules.collectProperties( i, superProperties, new Implementations(), false );

                            for ( Property p : i.getProperties().getProperty() )
                            {
                                boolean override = false;

                                if ( i.getImplementations() != null )
                                {
                                    final Implementations finalSuperProperties = new Implementations();
                                    this.collectFinalSuperProperties( modules, i, p.getName(), finalSuperProperties,
                                                                      new Implementations(), false );

                                    for ( Implementation finalSuper : finalSuperProperties.getImplementation() )
                                    {
                                        details.add( this.newPropertyInheritanceConstraintDetail(
                                            this.getObjectFactory().createImplementation( i ), i, p, finalSuper ) );

                                    }

                                    override = superProperties.getProperty( p.getName() ) != null;
                                }

                                if ( p.isOverride() && !override )
                                {
                                    details.add( this.newPropertyOverrideConstraintDetail(
                                        this.getObjectFactory().createProperty( p ), i, p ) );

                                }
                                if ( !p.isOverride() && override )
                                {
                                    this.log( Level.WARNING, this.getMessage( "propertyOverrideWarning", new Object[]
                                        {
                                            i.getIdentifier(), p.getName()
                                        } ), null );

                                }
                            }

                            for ( PropertyReference r : i.getProperties().getReference() )
                            {
                                boolean override = false;

                                if ( i.getImplementations() != null )
                                {
                                    final Implementations finalSuperProperties = new Implementations();
                                    this.collectFinalSuperProperties( modules, i, r.getName(), finalSuperProperties,
                                                                      new Implementations(), false );

                                    for ( Implementation finalSuper : finalSuperProperties.getImplementation() )
                                    {
                                        details.add( this.newPropertyInheritanceConstraintDetail(
                                            this.getObjectFactory().createImplementation( i ), i, r, finalSuper ) );

                                    }

                                    override = superProperties.getProperty( r.getName() ) != null;
                                }

                                if ( r.isOverride() && !override )
                                {
                                    details.add( this.newPropertyOverrideConstraintDetail(
                                        this.getObjectFactory().createProperties( i.getProperties() ), i, r ) );

                                }
                                if ( !r.isOverride() && override )
                                {
                                    this.log( Level.WARNING, this.getMessage( "propertyOverrideWarning", new Object[]
                                        {
                                            i.getIdentifier(), r.getName()
                                        } ), null );

                                }
                            }
                        }

                        if ( i.getMessages() != null )
                        {
                            final Messages superMessages = new Messages();
                            modules.collectMessages( i, superMessages, new Implementations(), false );

                            for ( Message msg : i.getMessages().getMessage() )
                            {
                                boolean override = false;

                                if ( i.getImplementations() != null )
                                {
                                    final Implementations finalSuperMessages = new Implementations();
                                    this.collectFinalSuperMessages( modules, i, msg.getName(), finalSuperMessages,
                                                                    new Implementations(), false );

                                    for ( Implementation finalSuper : finalSuperMessages.getImplementation() )
                                    {
                                        details.add( this.newMessageInheritanceConstraintDetail(
                                            this.getObjectFactory().createImplementation( i ), i, msg, finalSuper ) );

                                    }

                                    override = superMessages.getMessage( msg.getName() ) != null;
                                }

                                if ( msg.isOverride() && !override )
                                {
                                    details.add( this.newMessageOverrideConstraintDetail(
                                        this.getObjectFactory().createMessage( msg ), i, msg ) );

                                }
                                if ( !msg.isOverride() && override )
                                {
                                    this.log( Level.WARNING, this.getMessage( "messageOverrideWarning", new Object[]
                                        {
                                            i.getIdentifier(), msg.getName()
                                        } ), null );

                                }
                            }

                            for ( MessageReference r : i.getMessages().getReference() )
                            {
                                boolean override = false;

                                if ( i.getImplementations() != null )
                                {
                                    final Implementations finalSuperMessages = new Implementations();
                                    this.collectFinalSuperMessages( modules, i, r.getName(), finalSuperMessages,
                                                                    new Implementations(), false );

                                    for ( Implementation finalSuper : finalSuperMessages.getImplementation() )
                                    {
                                        details.add( this.newMessageInheritanceConstraintDetail(
                                            this.getObjectFactory().createImplementation( i ), i, r, finalSuper ) );

                                    }

                                    override = superMessages.getMessage( r.getName() ) != null;
                                }

                                if ( r.isOverride() && !override )
                                {
                                    details.add( this.newMessageOverrideConstraintDetail(
                                        this.getObjectFactory().createMessages( i.getMessages() ), i, r ) );

                                }
                                if ( !r.isOverride() && override )
                                {
                                    this.log( Level.WARNING, this.getMessage( "messageOverrideWarning", new Object[]
                                        {
                                            i.getIdentifier(), r.getName()
                                        } ), null );

                                }
                            }
                        }

                        if ( i.getImplementations() != null )
                        {
                            final Map<String, List<SpecificationReference>> specMap =
                                new HashMap<String, List<SpecificationReference>>();

                            final Map<String, List<Dependency>> dependencyMap =
                                new HashMap<String, List<Dependency>>();

                            final Map<String, List<Message>> messageMap =
                                new HashMap<String, List<Message>>();

                            final Map<String, List<Property>> propertyMap =
                                new HashMap<String, List<Property>>();

                            for ( ImplementationReference r : i.getImplementations().getReference() )
                            {
                                final Specifications currentSpecs = new Specifications();
                                final Dependencies currentDependencies = new Dependencies();
                                final Properties currentProperties = new Properties();
                                final Messages currentMessages = new Messages();
                                final Implementation current = modules.getImplementation( r.getIdentifier() );

                                modules.collectSpecifications( current, currentSpecs, new Implementations(), true );
                                modules.collectDependencies( current, currentDependencies, new Implementations(), true );
                                modules.collectMessages( current, currentMessages, new Implementations(), true );
                                modules.collectProperties( current, currentProperties, new Implementations(), true );

                                for ( SpecificationReference ref : currentSpecs.getReference() )
                                {
                                    List<SpecificationReference> list = specMap.get( ref.getIdentifier() );
                                    if ( list == null )
                                    {
                                        list = new LinkedList<SpecificationReference>();
                                        specMap.put( ref.getIdentifier(), list );
                                    }

                                    list.add( ref );
                                }

                                for ( Dependency d : currentDependencies.getDependency() )
                                {
                                    List<Dependency> list = dependencyMap.get( d.getName() );
                                    if ( list == null )
                                    {
                                        list = new LinkedList<Dependency>();
                                        dependencyMap.put( d.getName(), list );
                                    }

                                    list.add( d );
                                }

                                for ( Message msg : currentMessages.getMessage() )
                                {
                                    List<Message> list = messageMap.get( msg.getName() );
                                    if ( list == null )
                                    {
                                        list = new LinkedList<Message>();
                                        messageMap.put( msg.getName(), list );
                                    }

                                    list.add( msg );
                                }

                                for ( Property p : currentProperties.getProperty() )
                                {
                                    List<Property> list = propertyMap.get( p.getName() );
                                    if ( list == null )
                                    {
                                        list = new LinkedList<Property>();
                                        propertyMap.put( p.getName(), list );
                                    }

                                    list.add( p );
                                }
                            }

                            for ( Map.Entry<String, List<SpecificationReference>> e : specMap.entrySet() )
                            {
                                if ( e.getValue().size() > 1 &&
                                     ( i.getSpecifications() == null ||
                                       i.getSpecifications().getReference( e.getKey() ) == null ) )
                                {
                                    details.add(
                                        this.newSpecificationMultipleInheritanceContraintDetail(
                                        this.getObjectFactory().createImplementation( i ), i, e.getValue().get( 0 ) ) );

                                }
                            }

                            for ( Map.Entry<String, List<Dependency>> e : dependencyMap.entrySet() )
                            {
                                if ( e.getValue().size() > 1 &&
                                     ( i.getDependencies() == null ||
                                       i.getDependencies().getDependency( e.getKey() ) == null ) )
                                {
                                    details.add(
                                        this.newDependencyMultipleInheritanceContraintDetail(
                                        this.getObjectFactory().createImplementation( i ), i, e.getValue().get( 0 ) ) );

                                }
                            }

                            for ( Map.Entry<String, List<Message>> e : messageMap.entrySet() )
                            {
                                if ( e.getValue().size() > 1 &&
                                     ( i.getMessages() == null ||
                                       ( i.getMessages().getMessage( e.getKey() ) == null &&
                                         i.getMessages().getReference( e.getKey() ) == null ) ) )
                                {
                                    details.add(
                                        this.newMessageMultipleInheritanceContraintDetail(
                                        this.getObjectFactory().createImplementation( i ), i, e.getValue().get( 0 ) ) );

                                }
                            }

                            for ( Map.Entry<String, List<Property>> e : propertyMap.entrySet() )
                            {
                                if ( e.getValue().size() > 1 &&
                                     ( i.getProperties() == null ||
                                       ( i.getProperties().getProperty( e.getKey() ) == null &&
                                         i.getProperties().getReference( e.getKey() ) == null ) ) )
                                {
                                    details.add(
                                        this.newPropertyMultipleInheritanceContraintDetail(
                                        this.getObjectFactory().createImplementation( i ), i, e.getValue().get( 0 ) ) );

                                }
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
                            for ( PropertyReference r : s.getProperties().getReference() )
                            {
                                details.add( this.newSpecificationPropertyReferenceDeclarationConstraintDetail(
                                    this.getObjectFactory().createSpecification( s ), s, r ) );

                            }
                        }

                        final Implementations impls = modules.getImplementations( s.getIdentifier() );

                        if ( impls != null )
                        {
                            final Map<String, Implementations> map = new HashMap<String, Implementations>();

                            for ( Implementation i : impls.getImplementation() )
                            {
                                Implementations implementations = map.get( i.getName() );
                                if ( implementations == null )
                                {
                                    implementations = new Implementations();
                                    map.put( i.getName(), implementations );
                                }

                                implementations.getImplementation().add( i );
                            }

                            for ( Map.Entry<String, Implementations> e : map.entrySet() )
                            {
                                if ( e.getValue().getImplementation().size() > 1 )
                                {
                                    for ( Implementation i : e.getValue().getImplementation() )
                                    {
                                        details.add( this.newImplementationNameConstraintDetail(
                                            this.getObjectFactory().createSpecification( s ), s, i ) );

                                    }
                                }
                            }

                            if ( s.getMultiplicity() == Multiplicity.ONE && impls.getImplementation().size() > 1 )
                            {
                                for ( Implementation i : impls.getImplementation() )
                                {
                                    details.add( this.newMultiplicityConstraintDetail(
                                        this.getObjectFactory().createImplementation( i ), s, i ) );

                                }
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
        catch ( final TokenMgrError e )
        {
            throw new ModelException( e.getMessage(), e );
        }
        catch ( final ParseException e )
        {
            throw new ModelException( e.getMessage(), e );
        }
    }

    public <T extends ModelObject> T transformModelObject(
        final JAXBElement<T> modelObject, final Transformer transformer )
        throws IOException, SAXException, JAXBException, TransformerException
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

    public Instance getInstance( final Modules modules, final Implementation implementation, final ClassLoader cl )
    {
        if ( modules == null )
        {
            throw new NullPointerException( "modules" );
        }
        if ( implementation == null )
        {
            throw new NullPointerException( "implementation" );
        }
        if ( cl == null )
        {
            throw new NullPointerException( "classLoader" );
        }

        final Instance instance = new Instance();
        instance.setIdentifier( implementation.getIdentifier() );
        instance.setImplementationName( implementation.getName() );
        instance.setClazz( implementation.getClazz() );
        instance.setClassLoader( cl );
        instance.setStateless( implementation.isStateless() );
        instance.setDependencies( modules.getDependencies( implementation.getIdentifier() ) );
        instance.setProperties( modules.getProperties( implementation.getIdentifier() ) );
        instance.setMessages( modules.getMessages( implementation.getIdentifier() ) );
        instance.setSpecifications( modules.getSpecifications( implementation.getIdentifier() ) );
        return instance;
    }

    public Instance getInstance( final Modules modules, final Implementation implementation,
                                 final Dependency dependency, final ClassLoader cl )
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
        if ( cl == null )
        {
            throw new NullPointerException( "cl" );
        }

        final Instance instance = this.getInstance( modules, implementation, cl );
        final Specification dependencySpecification = modules.getSpecification( dependency.getIdentifier() );

        if ( dependencySpecification != null && dependencySpecification.getScope() == null &&
             dependency.getProperties() != null && !dependency.getProperties().getProperty().isEmpty() )
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
            final Class specClass = Class.forName( specification.getClazz(), true, instance.getClassLoader() );
            final Class clazz = Class.forName( instance.getClazz(), true, instance.getClassLoader() );

            if ( Modifier.isPublic( clazz.getModifiers() ) )
            {
                Constructor ctor = null;

                try
                {
                    ctor = clazz.getConstructor( NO_CLASSES );
                }
                catch ( final NoSuchMethodException e )
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
                    final StringBuilder methodNames = new StringBuilder().append( '[' );
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
        catch ( final InvocationTargetException e )
        {
            throw (InstantiationException) new InstantiationException().initCause(
                e.getTargetException() != null ? e.getTargetException() : e );

        }
        catch ( final IllegalAccessException e )
        {
            throw (InstantiationException) new InstantiationException().initCause( e );
        }
        catch ( final ClassNotFoundException e )
        {
            throw (InstantiationException) new InstantiationException().initCause( e );
        }
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
         */
        void onLog( Level level, String message, Throwable t );

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

    /** The context of the instance. */
    private JAXBContext context;

    /** The schema of the instance. */
    private javax.xml.validation.Schema schema;

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
    public Marshaller getBootstrapMarshaller( final boolean validating, final boolean formattedOutput )
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
    public Unmarshaller getBootstrapUnmarshaller( final boolean validating )
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
    public void validateBootstrapObject( final JAXBElement<? extends BootstrapObject> bootstrapObject )
        throws ModelException, IOException, SAXException, JAXBException
    {
        if ( bootstrapObject == null )
        {
            throw new NullPointerException( "bootstrapObject" );
        }

        final StringWriter stringWriter = new StringWriter();
        final Validator validator = this.getBootstrapSchema().newValidator();
        final ModelExceptionErrorHandler errorHandler = new ModelExceptionErrorHandler();
        validator.setErrorHandler( errorHandler );
        this.getBootstrapMarshaller( false, false ).marshal( bootstrapObject, stringWriter );

        try
        {
            validator.validate( new StreamSource( new StringReader( stringWriter.toString() ) ) );
        }
        catch ( final SAXException e )
        {
            final ModelException modelException = new ModelException( this.getMessage( "validationFailed", null ), e );
            modelException.getDetails().addAll( errorHandler.getDetails() );
            throw modelException;
        }
    }

    /**
     * Transforms a given {@code BootstrapObject} with a given {@code Transformer}.
     *
     * @param bootstrapObject The {@code BootstrapObject} to transform.
     * @param transformer The {@code Transformer} to transform {@code bootstrapObject} with.
     * @param <T> The type of {@code bootstrapObject}.
     *
     * @return {@code bootstrapObject} transformed with {@code transformer}.
     *
     * @throws NullPointerException if {@code bootstrapObject} or {@code transformer} is {@code null}.
     * @throws IOException if reading schema resources fails.
     * @throws SAXException if parsing schema resources fails.
     * @throws JAXBException if binding fails.
     * @throws TransformerException if the transformation fails.
     */
    public <T extends BootstrapObject> T transformBootstrapObject(
        final JAXBElement<T> bootstrapObject, final Transformer transformer )
        throws IOException, SAXException, JAXBException, TransformerException
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
     * @param value The new object factory of the instance or {@code null}.
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
     * @param value The new entity resolver of the instance or {@code null}.
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
     * @param value The new L/S resolver of the instance or {@code null}.
     *
     * @see #getLSResourceResolver()
     */
    public void setLSResourceResolver( final LSResourceResolver value )
    {
        this.resourceResolver = value;
    }

    /**
     * Sets the JAXB context of the instance.
     *
     * @param value The new JAXB context of the instance or {@code null}.
     *
     * @see #getContext()
     */
    public void setContext( final JAXBContext value )
    {
        this.context = value;
    }

    /**
     * Sets the schema of the instance.
     *
     * @param value The new schema of the instance or {@code null}.
     *
     * @see #getSchema()
     */
    public void setSchema( final javax.xml.validation.Schema value )
    {
        this.schema = value;
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

                Object content = u.unmarshal( url );
                if ( content instanceof JAXBElement )
                {
                    content = ( (JAXBElement) content ).getValue();
                }

                if ( content instanceof Schema )
                {
                    final Schema s = (Schema) content;
                    this.log( Level.FINE, this.getMessage( "addingSchema", new Object[]
                        {
                            s.getPublicId(), s.getSystemId(), s.getContextId(), s.getClasspathId()
                        } ), null );

                    this.schemas.getSchema().add( s );
                }
                else if ( content instanceof Schemas )
                {
                    for ( Schema s : ( (Schemas) content ).getSchema() )
                    {
                        this.log( Level.FINE, this.getMessage( "addingSchema", new Object[]
                            {
                                s.getPublicId(), s.getSystemId(), s.getContextId(), s.getClasspathId()
                            } ), null );

                        this.schemas.getSchema().add( s );
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

        final long t0 = System.currentTimeMillis();
        final Text text = new Text();
        text.setLanguage( "en" );
        text.setValue( this.getMessage( "classpathModulesInfo", new Object[]
            {
                location
            } ) );

        final Modules mods = new Modules();
        mods.setDocumentation( new Texts() );
        mods.getDocumentation().setDefaultLanguage( "en" );
        mods.getDocumentation().getText().add( text );

        final Unmarshaller u = this.getUnmarshaller( false );
        final Enumeration<URL> resources = this.getClassLoader().getResources( location );

        Integer count = 0;
        while ( resources.hasMoreElements() )
        {
            count++;
            final URL url = resources.nextElement();

            this.log( Level.FINE, this.getMessage( "processing", new Object[]
                {
                    url.toExternalForm()
                } ), null );

            Object content = u.unmarshal( url );
            if ( content instanceof JAXBElement )
            {
                content = ( (JAXBElement) content ).getValue();
            }

            if ( content instanceof Module )
            {
                mods.getModule().add( (Module) content );
            }
            else
            {
                this.log( Level.WARNING, this.getMessage( "ignoringDocument", new Object[]
                    {
                        content == null ? "<>" : content.toString(), url.toExternalForm()
                    } ), null );

            }
        }

        this.log( Level.FINE, this.getMessage( "classpathReport", new Object[]
            {
                count, Long.valueOf( System.currentTimeMillis() - t0 )
            } ), null );

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

        final long t0 = System.currentTimeMillis();
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
                catch ( final SAXException e )
                {
                    log( Level.SEVERE, e.getMessage(), e );
                    throw new TransformerException( e );
                }
                catch ( final IOException e )
                {
                    log( Level.SEVERE, e.getMessage(), e );
                    throw new TransformerException( e );
                }
            }

        };

        transformerFactory.setErrorListener( errorListener );
        transformerFactory.setURIResolver( uriResolver );

        Integer count = 0;
        while ( resources.hasMoreElements() )
        {
            count++;
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

        this.log( Level.FINE, this.getMessage( "classpathReport", new Object[]
            {
                count, Long.valueOf( System.currentTimeMillis() - t0 )
            } ), null );

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
        this.schema = null;
        this.schemas = null;
        this.schemaResources = null;
        this.entityResolver = null;
        this.resourceResolver = null;
        this.context = null;
    }

    /**
     * Notifies registered listeners.
     *
     * @param level The level of the event.
     * @param message The message of the event or {@code null}.
     * @param throwable The throwable of the event {@code null}.
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
                    specification.setClazz( classpathSpec.getName() );
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
            catch ( final ClassNotFoundException e )
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
                final Class classpathImpl = Class.forName( specification.getClazz(), true, this.getClassLoader() );
                boolean classpathImplementation = false;

                if ( Modifier.isPublic( classpathImpl.getModifiers() ) )
                {
                    if ( !Modifier.isAbstract( classpathImpl.getModifiers() ) )
                    {
                        try
                        {
                            classpathImpl.getConstructor( NO_CLASSES );
                            name = "init";
                            classpathImplementation = true;
                        }
                        catch ( final NoSuchMethodException e )
                        {
                            this.log( Level.FINE, this.getMessage( "noSuchMethod", new Object[]
                                {
                                    e.getMessage()
                                } ), null );

                        }
                    }

                    if ( !classpathImplementation )
                    {
                        final char[] c = classpathImpl.getName().substring(
                            classpathImpl.getPackage().getName().length() + 1 ).toCharArray();

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
                        implementation.setClazz( classpathImpl.getName() );
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
            catch ( final ClassNotFoundException e )
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
            final Method m = clazz.getMethod( methodName, NO_CLASSES );
            factoryMethod = Modifier.isStatic( m.getModifiers() ) && type.isAssignableFrom( m.getReturnType() );
        }
        catch ( final NoSuchMethodException e )
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
        catch ( final NoSuchMethodException e )
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

            for ( final Enumeration<URL> e = this.getClassLoader().getResources( "META-INF/MANIFEST.MF" );
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
     * @param modules The modules to search for the implementation of {@code object}.
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

    private void collectFinalSuperDependencies(
        final Modules modules, final Implementation implementation, final String dependencyName,
        final Implementations implementations, final Implementations seen, final boolean includeImplementation )
    {
        if ( implementation != null && seen.getImplementation( implementation.getIdentifier() ) == null )
        {
            seen.getImplementation().add( implementation );

            if ( includeImplementation )
            {
                final Dependencies dependencies = modules.getDependencies( implementation.getIdentifier() );

                if ( dependencies != null )
                {
                    for ( Dependency d : dependencies.getDependency() )
                    {
                        if ( dependencyName.equals( d.getName() ) && d.isFinal() &&
                             implementations.getImplementation( implementation.getIdentifier() ) == null )
                        {
                            implementations.getImplementation().add( implementation );
                        }
                    }
                }
            }

            if ( implementation.getImplementations() != null )
            {
                for ( ImplementationReference r : implementation.getImplementations().getReference() )
                {
                    this.collectFinalSuperDependencies( modules, modules.getImplementation( r.getIdentifier() ),
                                                        dependencyName, implementations, seen, true );

                }
            }
        }
    }

    private void collectFinalSuperMessages(
        final Modules modules, final Implementation implementation, final String messageName,
        final Implementations implementations, final Implementations seen, final boolean includeImplementation )
    {
        if ( implementation != null && seen.getImplementation( implementation.getIdentifier() ) == null )
        {
            seen.getImplementation().add( implementation );

            if ( includeImplementation )
            {
                final Messages messages = modules.getMessages( implementation.getIdentifier() );

                if ( messages != null )
                {
                    for ( Message m : messages.getMessage() )
                    {
                        if ( messageName.equals( m.getName() ) && m.isFinal() &&
                             implementations.getImplementation( implementation.getIdentifier() ) == null )
                        {
                            implementations.getImplementation().add( implementation );
                        }
                    }
                }
            }

            if ( implementation.getImplementations() != null )
            {
                for ( ImplementationReference r : implementation.getImplementations().getReference() )
                {
                    this.collectFinalSuperMessages( modules, modules.getImplementation( r.getIdentifier() ),
                                                    messageName, implementations, seen, true );

                }
            }
        }
    }

    private void collectFinalSuperProperties(
        final Modules modules, final Implementation implementation, final String propertyName,
        final Implementations implementations, final Implementations seen, final boolean includeImplementation )
    {
        if ( implementation != null && seen.getImplementation( implementation.getIdentifier() ) == null )
        {
            seen.getImplementation().add( implementation );

            if ( includeImplementation )
            {
                final Properties properties = modules.getProperties( implementation.getIdentifier() );

                if ( properties != null )
                {
                    for ( Property p : properties.getProperty() )
                    {
                        if ( propertyName.equals( p.getName() ) && p.isFinal() &&
                             implementations.getImplementation( implementation.getIdentifier() ) == null )
                        {
                            implementations.getImplementation().add( implementation );
                        }
                    }
                }
            }

            if ( implementation.getImplementations() != null )
            {
                for ( ImplementationReference r : implementation.getImplementations().getReference() )
                {
                    this.collectFinalSuperProperties( modules, modules.getImplementation( r.getIdentifier() ),
                                                      propertyName, implementations, seen, true );

                }
            }
        }
    }

    private void collectFinalSuperSpecifications(
        final Modules modules, final Implementation implementation, final String specificationIdentifier,
        final Implementations implementations, final Implementations seen, final boolean includeImplementation )
    {
        if ( implementation != null && seen.getImplementation( implementation.getIdentifier() ) == null )
        {
            seen.getImplementation().add( implementation );

            if ( includeImplementation )
            {
                final Specifications specifications = modules.getSpecifications( implementation.getIdentifier() );

                if ( specifications != null )
                {
                    for ( SpecificationReference r : specifications.getReference() )
                    {
                        if ( specificationIdentifier.equals( r.getIdentifier() ) && r.isFinal() &&
                             implementations.getImplementation( implementation.getIdentifier() ) == null )
                        {
                            implementations.getImplementation().add( implementation );
                        }
                    }
                }
            }

            if ( implementation.getImplementations() != null )
            {
                for ( ImplementationReference r : implementation.getImplementations().getReference() )
                {
                    this.collectFinalSuperSpecifications( modules, modules.getImplementation( r.getIdentifier() ),
                                                          specificationIdentifier, implementations, seen, true );

                }
            }
        }
    }

    private void collectFinalSuperImplementations( final Modules modules, final Implementation implementation,
                                                   final Implementations implementations, final Implementations seen,
                                                   final boolean includeImplementation )
    {
        if ( implementation != null && seen.getImplementation( implementation.getIdentifier() ) == null )
        {
            seen.getImplementation().add( implementation );

            if ( includeImplementation && implementation.isFinal() &&
                 implementations.getImplementation( implementation.getIdentifier() ) == null )
            {
                implementations.getImplementation().add( implementation );
            }

            if ( implementation.getImplementations() != null )
            {
                for ( ImplementationReference r : implementation.getImplementations().getReference() )
                {
                    this.collectFinalSuperImplementations( modules, modules.getImplementation( r.getIdentifier() ),
                                                           implementations, seen, true );

                }
            }
        }
    }

    private Implementation findInheritanceCycle( final Modules modules, final Implementation current,
                                                 final Implementation report, final Implementations implementations )
    {
        if ( current != null )
        {
            if ( implementations.getImplementation( current.getIdentifier() ) != null )
            {
                return report;
            }

            implementations.getImplementation().add( current );

            if ( current.getImplementations() != null )
            {
                for ( ImplementationReference r : current.getImplementations().getReference() )
                {
                    return this.findInheritanceCycle( modules, modules.getImplementation( r.getIdentifier() ),
                                                      current, implementations );

                }
            }
        }

        return null;
    }

    private String getMessage( final String key, final Object args )
    {
        return new MessageFormat(
            ResourceBundle.getBundle( DefaultModelManager.class.getName().replace( '.', '/' ), Locale.getDefault() ).
            getString( key ) ).format( args );

    }

    private void assertImplementationMessagesUniqueness(
        final Implementation implementation, final List<ModelException.Detail> details )
    {
        if ( implementation.getMessages() != null )
        {
            for ( Message m : implementation.getMessages().getMessage() )
            {
                if ( implementation.getMessages().getReference( m.getName() ) != null )
                {
                    final ModelException.Detail detail = new ModelException.Detail(
                        "IMPLEMENTATION_MESSAGES_UNIQUENESS_CONSTRAINT", Level.SEVERE,
                        this.getMessage( "messagesUniquenessConstraint", new Object[]
                        {
                            implementation.getIdentifier(), m.getName()
                        } ) );

                    detail.setElement( this.getObjectFactory().createImplementation( implementation ) );
                    details.add( detail );
                }
            }
        }
    }

    private void assertImplementationPropertiesUniqueness(
        final Implementation implementation, final List<ModelException.Detail> details )
    {
        if ( implementation.getProperties() != null )
        {
            for ( Property p : implementation.getProperties().getProperty() )
            {
                if ( implementation.getProperties().getReference( p.getName() ) != null )
                {
                    final ModelException.Detail detail = new ModelException.Detail(
                        "IMPLEMENTATION_PROPERTIES_UNIQUENESS_CONSTRAINT", Level.SEVERE,
                        this.getMessage( "propertiesUniquenessConstraint", new Object[]
                        {
                            implementation.getIdentifier(), p.getName()
                        } ) );

                    detail.setElement( this.getObjectFactory().createImplementation( implementation ) );
                    details.add( detail );
                }
            }
        }
    }

    private ModelException.Detail newIncompatibleImplementationDetail(
        final JAXBElement<? extends ModelObject> element, final String implementation,
        final String implementationModule, final String specification, final String specificationModule,
        final String implementedVersion, final String specifiedVersion )
    {
        final ModelException.Detail detail = new ModelException.Detail(
            "IMPLEMENTATION_COMPATIBILITY_CONSTRAINT", Level.SEVERE,
            this.getMessage( "incompatibleImplementation", new Object[]
            {
                implementation, implementationModule, specification, specificationModule,
                implementedVersion, specifiedVersion
            } ) );

        detail.setElement( element );
        return detail;
    }

    private ModelException.Detail newIncompatibleDependencyDetail(
        final JAXBElement<? extends ModelObject> element, final String implementation,
        final String implementationModule, final String specification, final String specificationModule,
        final String requiredVersion, final String availableVersion )
    {
        final ModelException.Detail detail = new ModelException.Detail(
            "DEPENDENCY_COMPATIBILITY_CONSTRAINT", Level.SEVERE, this.getMessage( "incompatibleDependency", new Object[]
            {
                implementation, implementationModule, specification, specificationModule,
                requiredVersion, availableVersion
            } ) );

        detail.setElement( element );
        return detail;
    }

    private ModelException.Detail newImplementationNameConstraintDetail(
        final JAXBElement<? extends ModelObject> element, final Specification specification,
        final Implementation implementation )
    {
        final ModelException.Detail detail = new ModelException.Detail(
            "IMPLEMENTATION_NAME_CONSTRAINT", Level.SEVERE,
            this.getMessage( "implementationNameConstraint", new Object[]
            {
                implementation.getIdentifier(), specification.getIdentifier(), implementation.getName()
            } ) );

        detail.setElement( element );
        return detail;
    }

    private ModelException.Detail newMandatoryDependencyConstraintDetail(
        final JAXBElement<? extends ModelObject> element, final String implementation, final String dependencyName )
    {
        final ModelException.Detail detail = new ModelException.Detail(
            "MANDATORY_DEPENDENCY_CONSTRAINT", Level.SEVERE,
            this.getMessage( "mandatoryDependencyConstraint", new Object[]
            {
                implementation, dependencyName
            } ) );

        detail.setElement( element );
        return detail;
    }

    private ModelException.Detail newMultiplicityConstraintDetail(
        final JAXBElement<? extends ModelObject> element, final Specification specification,
        final Implementation implementation )
    {
        final ModelException.Detail detail = new ModelException.Detail(
            "MULTIPLICITY_CONSTRAINT", Level.SEVERE, this.getMessage( "multiplicityConstraint", new Object[]
            {
                implementation.getIdentifier(), specification.getIdentifier(), specification.getMultiplicity().value()
            } ) );

        detail.setElement( element );
        return detail;
    }

    private ModelException.Detail newImplementationInheritanceConstraintDetail(
        final JAXBElement<? extends ModelObject> element, final Implementation implementation,
        final Implementation finalSuperImplementation )
    {
        final ModelException.Detail detail = new ModelException.Detail(
            "IMPLEMENTATION_INHERITANCE_CONSTRAINT", Level.SEVERE,
            this.getMessage( "implementationInheritanceConstraint", new Object[]
            {
                implementation.getIdentifier(), finalSuperImplementation.getIdentifier()
            } ) );

        detail.setElement( element );
        return detail;
    }

    private ModelException.Detail newSpecificationInheritanceConstraintDetail(
        final JAXBElement<? extends ModelObject> element, final Implementation implementation,
        final SpecificationReference specification, final Implementation finalSuperSpecification )
    {
        final ModelException.Detail detail = new ModelException.Detail(
            "SPECIFICATION_INHERITANCE_CONSTRANT", Level.SEVERE,
            this.getMessage( "specificationInheritanceConstraint", new Object[]
            {
                implementation.getIdentifier(), specification.getIdentifier(), finalSuperSpecification.getIdentifier()
            } ) );

        detail.setElement( element );
        return detail;
    }

    private ModelException.Detail newDependencyInheritanceConstraintDetail(
        final JAXBElement<? extends ModelObject> element, final Implementation implementation,
        final Dependency dependency, final Implementation finalSuperDependency )
    {
        final ModelException.Detail detail = new ModelException.Detail(
            "DEPENDENCY_INHERITANCE_CONSTRAINT", Level.SEVERE,
            this.getMessage( "dependencyInheritanceConstraint", new Object[]
            {
                implementation.getIdentifier(), dependency.getName(), finalSuperDependency.getIdentifier()
            } ) );

        detail.setElement( element );
        return detail;
    }

    private ModelException.Detail newPropertyInheritanceConstraintDetail(
        final JAXBElement<? extends ModelObject> element, final Implementation implementation,
        final Property property, final Implementation finalSuperProperty )
    {
        final ModelException.Detail detail = new ModelException.Detail(
            "PROPERTY_INHERITANCE_CONSTRAINT", Level.SEVERE,
            this.getMessage( "propertyInheritanceConstraint", new Object[]
            {
                implementation.getIdentifier(), property.getName(), finalSuperProperty.getIdentifier()
            } ) );

        detail.setElement( element );
        return detail;
    }

    private ModelException.Detail newPropertyInheritanceConstraintDetail(
        final JAXBElement<? extends ModelObject> element, final Implementation implementation,
        final PropertyReference reference, final Implementation finalSuperProperty )
    {
        final ModelException.Detail detail = new ModelException.Detail(
            "PROPERTY_INHERITANCE_CONSTRAINT", Level.SEVERE,
            this.getMessage( "propertyInheritanceConstraint", new Object[]
            {
                implementation.getIdentifier(), reference.getName(), finalSuperProperty.getIdentifier()
            } ) );

        detail.setElement( element );
        return detail;
    }

    private ModelException.Detail newMessageInheritanceConstraintDetail(
        final JAXBElement<? extends ModelObject> element, final Implementation implementation,
        final Message message, final Implementation finalSuperMessage )
    {
        final ModelException.Detail detail = new ModelException.Detail(
            "MESSAGE_INHERITANCE_CONSTRAINT", Level.SEVERE,
            this.getMessage( "messageInheritanceConstraint", new Object[]
            {
                implementation.getIdentifier(), message.getName(), finalSuperMessage.getIdentifier()
            } ) );

        detail.setElement( element );
        return detail;
    }

    private ModelException.Detail newMessageInheritanceConstraintDetail(
        final JAXBElement<? extends ModelObject> element, final Implementation implementation,
        final MessageReference reference, final Implementation finalSuperMessage )
    {
        final ModelException.Detail detail = new ModelException.Detail(
            "MESSAGE_INHERITANCE_CONSTRAINT", Level.SEVERE,
            this.getMessage( "messageInheritanceConstraint", new Object[]
            {
                implementation.getIdentifier(), reference.getName(), finalSuperMessage.getIdentifier()
            } ) );

        detail.setElement( element );
        return detail;
    }

    private ModelException.Detail newDependencyPropertyReferenceDeclarationConstraintDetail(
        final JAXBElement<? extends ModelObject> element, final Implementation implementation,
        final Dependency dependency, final PropertyReference reference )
    {
        final ModelException.Detail detail = new ModelException.Detail(
            "DEPENDENCY_PROPERTY_REFERENCE_DECLARATION_CONSTRAINT", Level.SEVERE,
            this.getMessage( "dependencyPropertyReferenceDeclarationConstraint", new Object[]
            {
                implementation.getIdentifier(), dependency.getName(), reference.getName()
            } ) );

        detail.setElement( element );
        return detail;
    }

    private ModelException.Detail newDependencyPropertiesOverrideConstraintDetail(
        final JAXBElement<? extends ModelObject> element, final Implementation implementation,
        final Dependency dependency, final Specification specification, final Property property )
    {
        final ModelException.Detail detail = new ModelException.Detail(
            "DEPENDENCY_PROPERTIES_OVERRIDE_CONSTRAINT", Level.SEVERE,
            this.getMessage( "dependencyPropertiesOverrideConstraint", new Object[]
            {
                implementation.getIdentifier(), dependency.getName(), specification.getIdentifier(),
                specification.getScope(), property.getName()
            } ) );

        detail.setElement( element );
        return detail;
    }

    private ModelException.Detail newImplementationSpecificationDeclarationConstraintDetail(
        final JAXBElement<? extends ModelObject> element, final Implementation implementation,
        final Specification specification )
    {
        final ModelException.Detail detail = new ModelException.Detail(
            "IMPLEMENTATION_SPECIFICATION_DECLARATION_CONSTRAINT", Level.SEVERE,
            this.getMessage( "implementationSpecificationDeclarationConstraint", new Object[]
            {
                implementation.getIdentifier(), specification.getIdentifier()
            } ) );

        detail.setElement( element );
        return detail;
    }

    private ModelException.Detail newModuleMessageReferenceDeclarationConstraintDetail(
        final JAXBElement<? extends ModelObject> element, final Module module, final MessageReference reference )
    {
        final ModelException.Detail detail = new ModelException.Detail(
            "MODULE_MESSAGE_REFERENCE_DECLARATION_CONSTRAINT", Level.SEVERE,
            this.getMessage( "moduleMessageReferenceDeclarationConstraint", new Object[]
            {
                module.getName(), reference.getName()
            } ) );

        detail.setElement( element );
        return detail;
    }

    private ModelException.Detail newModulePropertyReferenceDeclarationConstraintDetail(
        final JAXBElement<? extends ModelObject> element, final Module module, final PropertyReference reference )
    {
        final ModelException.Detail detail = new ModelException.Detail(
            "MODULE_PROPERTY_REFERENCE_DECLARATION_CONSTRAINT", Level.SEVERE,
            this.getMessage( "modulePropertyReferenceDeclarationConstraint", new Object[]
            {
                module.getName(), reference.getName()
            } ) );

        detail.setElement( element );
        return detail;
    }

    private ModelException.Detail newModuleImplementationReferenceDeclarationConstraintDetail(
        final JAXBElement<? extends ModelObject> element, final Module module, final ImplementationReference reference )
    {
        final ModelException.Detail detail = new ModelException.Detail(
            "MODULE_IMPLEMENTATION_REFERENCE_DECLARATION_CONSTRAINT", Level.SEVERE,
            this.getMessage( "moduleImplementationReferenceDeclarationConstraint", new Object[]
            {
                module.getName(), reference.getIdentifier()
            } ) );

        detail.setElement( element );
        return detail;
    }

    private ModelException.Detail newImplementationImplementationDeclarationConstraintDetail(
        final JAXBElement<? extends ModelObject> element, final Implementation implementation,
        final Implementation declaration )
    {
        final ModelException.Detail detail = new ModelException.Detail(
            "IMPLEMENTATION_IMPLEMENTATION_DECLARATION_CONSTRAINT", Level.SEVERE,
            this.getMessage( "implementationImplementationDeclarationConstraint", new Object[]
            {
                implementation.getIdentifier(), declaration.getIdentifier()
            } ) );

        detail.setElement( element );
        return detail;
    }

    private ModelException.Detail newModuleSpecificationReferenceDeclarationConstraintDetail(
        final JAXBElement<? extends ModelObject> element, final Module module, final SpecificationReference reference )
    {
        final ModelException.Detail detail = new ModelException.Detail(
            "MODULE_SPECIFICATION_REFERENCE_DECLARATION_CONSTRAINT", Level.SEVERE,
            this.getMessage( "moduleSpecificationReferenceDeclarationConstraint", new Object[]
            {
                module.getName(), reference.getIdentifier()
            } ) );

        detail.setElement( element );
        return detail;
    }

    private ModelException.Detail newDependencyOverrideConstraintDetail(
        final JAXBElement<? extends ModelObject> element, final Implementation implementation,
        final Dependency dependency )
    {
        final ModelException.Detail detail = new ModelException.Detail(
            "DEPENDENCY_OVERRIDE_CONSTRAINT", Level.SEVERE,
            this.getMessage( "dependencyOverrideConstraint", new Object[]
            {
                implementation.getIdentifier(), dependency.getName()
            } ) );

        detail.setElement( element );
        return detail;
    }

    private ModelException.Detail newMessageOverrideConstraintDetail(
        final JAXBElement<? extends ModelObject> element, final Implementation implementation,
        final Message message )
    {
        final ModelException.Detail detail = new ModelException.Detail(
            "MESSAGE_OVERRIDE_CONSTRAINT", Level.SEVERE, this.getMessage( "messageOverrideConstraint", new Object[]
            {
                implementation.getIdentifier(), message.getName()
            } ) );

        detail.setElement( element );
        return detail;
    }

    private ModelException.Detail newMessageOverrideConstraintDetail(
        final JAXBElement<? extends ModelObject> element, final Implementation implementation,
        final MessageReference reference )
    {
        final ModelException.Detail detail = new ModelException.Detail(
            "MESSAGE_OVERRIDE_CONSTRAINT", Level.SEVERE, this.getMessage( "messageOverrideConstraint", new Object[]
            {
                implementation.getIdentifier(), reference.getName()
            } ) );

        detail.setElement( element );
        return detail;
    }

    private ModelException.Detail newPropertyOverrideConstraintDetail(
        final JAXBElement<? extends ModelObject> element, final Implementation implementation,
        final Property property )
    {
        final ModelException.Detail detail = new ModelException.Detail(
            "PROPERTY_OVERRIDE_CONSTRAINT", Level.SEVERE, this.getMessage( "propertyOverrideConstraint", new Object[]
            {
                implementation.getIdentifier(), property.getName()
            } ) );

        detail.setElement( element );
        return detail;
    }

    private ModelException.Detail newPropertyOverrideConstraintDetail(
        final JAXBElement<? extends ModelObject> element, final Implementation implementation,
        final PropertyReference reference )
    {
        final ModelException.Detail detail = new ModelException.Detail(
            "PROPERTY_OVERRIDE_CONSTRAINT", Level.SEVERE, this.getMessage( "propertyOverrideConstraint", new Object[]
            {
                implementation.getIdentifier(), reference.getName()
            } ) );

        detail.setElement( element );
        return detail;
    }

    private ModelException.Detail newSpecificationOverrideConstraintDetail(
        final JAXBElement<? extends ModelObject> element, final Implementation implementation,
        final SpecificationReference reference )
    {
        final ModelException.Detail detail = new ModelException.Detail(
            "SPECIFICATION_OVERRIDE_CONSTRAINT", Level.SEVERE,
            this.getMessage( "specificationOverrideConstraint", new Object[]
            {
                implementation.getIdentifier(), reference.getIdentifier()
            } ) );

        detail.setElement( element );
        return detail;
    }

    private ModelException.Detail newAbstractLocationConstraintDetail(
        final JAXBElement<? extends ModelObject> element, final Implementation implementation, final String location )
    {
        final ModelException.Detail detail = new ModelException.Detail(
            "ABSTRACT_IMPLEMENTATION_LOCATION_CONSTRAINT", Level.SEVERE,
            this.getMessage( "abstractLocationConstraint", new Object[]
            {
                implementation.getIdentifier(), location
            } ) );

        detail.setElement( element );
        return detail;
    }

    private ModelException.Detail newFinalModuleMessageConstraintDetail(
        final JAXBElement<? extends ModelObject> element, final Module module, final Message message )
    {
        final ModelException.Detail detail = new ModelException.Detail(
            "FINAL_MODULE_MESSAGE_CONSTRAINT", Level.SEVERE,
            this.getMessage( "finalModuleMessageConstraint", new Object[]
            {
                module.getName(), message.getName()
            } ) );

        detail.setElement( element );
        return detail;
    }

    private ModelException.Detail newOverrideModuleMessageConstraintDetail(
        final JAXBElement<? extends ModelObject> element, final Module module, final Message message )
    {
        final ModelException.Detail detail = new ModelException.Detail(
            "OVERRIDE_MODULE_MESSAGE_CONSTRAINT", Level.SEVERE,
            this.getMessage( "overrideModuleMessageConstraint", new Object[]
            {
                module.getName(), message.getName()
            } ) );

        detail.setElement( element );
        return detail;
    }

    private ModelException.Detail newFinalModulePropertyConstraintDetail(
        final JAXBElement<? extends ModelObject> element, final Module module, final Property property )
    {
        final ModelException.Detail detail = new ModelException.Detail(
            "FINAL_MODULE_PROPERTY_CONSTRAINT", Level.SEVERE,
            this.getMessage( "finalModulePropertyConstraint", new Object[]
            {
                module.getName(), property.getName()
            } ) );

        detail.setElement( element );
        return detail;
    }

    private ModelException.Detail newOverrideModulePropertyConstraintDetail(
        final JAXBElement<? extends ModelObject> element, final Module module, final Property property )
    {
        final ModelException.Detail detail = new ModelException.Detail(
            "OVERRIDE_MODULE_PROPERTY_CONSTRAINT", Level.SEVERE,
            this.getMessage( "overrideModulePropertyConstraint", new Object[]
            {
                module.getName(), property.getName()
            } ) );

        detail.setElement( element );
        return detail;
    }

    private ModelException.Detail newSpecificationPropertyReferenceDeclarationConstraintDetail(
        final JAXBElement<? extends ModelObject> element, final Specification specification,
        final PropertyReference reference )
    {
        final ModelException.Detail detail = new ModelException.Detail(
            "SPECIFICATION_PROPERTY_REFERENCE_DECLARATION_CONSTRAINT", Level.SEVERE,
            this.getMessage( "specificationPropertyReferenceDeclarationConstraint", new Object[]
            {
                specification.getIdentifier(), reference.getName()
            } ) );

        detail.setElement( element );
        return detail;
    }

    private ModelException.Detail newSpecificationMultipleInheritanceContraintDetail(
        final JAXBElement<? extends ModelObject> element, final Implementation implementation,
        final SpecificationReference reference )
    {
        final ModelException.Detail detail = new ModelException.Detail(
            "SPECIFICATION_MULTIPLE_INHERITANCE_CONSTRAINT", Level.SEVERE,
            this.getMessage( "multipleInheritanceSpecificationConstraint", new Object[]
            {
                implementation.getIdentifier(), reference.getIdentifier()
            } ) );

        detail.setElement( element );
        return detail;
    }

    private ModelException.Detail newDependencyMultipleInheritanceContraintDetail(
        final JAXBElement<? extends ModelObject> element, final Implementation implementation,
        final Dependency dependency )
    {
        final ModelException.Detail detail = new ModelException.Detail(
            "DEPENDENCY_MULTIPLE_INHERITANCE_CONSTRAINT", Level.SEVERE,
            this.getMessage( "multipleInheritanceDependencyConstraint", new Object[]
            {
                implementation.getIdentifier(), dependency.getName()
            } ) );

        detail.setElement( element );
        return detail;
    }

    private ModelException.Detail newMessageMultipleInheritanceContraintDetail(
        final JAXBElement<? extends ModelObject> element, final Implementation implementation,
        final Message message )
    {
        final ModelException.Detail detail = new ModelException.Detail(
            "MESSAGE_MULTIPLE_INHERITANCE_CONSTRAINT", Level.SEVERE,
            this.getMessage( "multipleInheritanceMessageConstraint", new Object[]
            {
                implementation.getIdentifier(), message.getName()
            } ) );

        detail.setElement( element );
        return detail;
    }

    private ModelException.Detail newPropertyMultipleInheritanceContraintDetail(
        final JAXBElement<? extends ModelObject> element, final Implementation implementation,
        final Property property )
    {
        final ModelException.Detail detail = new ModelException.Detail(
            "PROPERTY_MULTIPLE_INHERITANCE_CONSTRAINT", Level.SEVERE,
            this.getMessage( "multipleInheritancePropertyConstraint", new Object[]
            {
                implementation.getIdentifier(), property.getName()
            } ) );

        detail.setElement( element );
        return detail;
    }

    private ModelException.Detail newImplementationInheritanceCycleConstraintDetail(
        final JAXBElement<? extends ModelObject> element, final Implementation implementation,
        final Implementation cycle )
    {
        final ModelException.Detail detail = new ModelException.Detail(
            "IMPLEMENTATION_INHERITANCE_CYCLE_CONSTRAINT", Level.SEVERE,
            this.getMessage( "implementationInheritanceCycleConstraint", new Object[]
            {
                implementation.getIdentifier(), cycle.getIdentifier()
            } ) );

        detail.setElement( element );
        return detail;
    }

    private ModelException.Detail newImplementationInheritanceCompatibilityConstraintDetail(
        final JAXBElement<? extends ModelObject> element, final Implementation implementation,
        final Implementation superImplementation, final String expectedVersion )
    {
        final ModelException.Detail detail = new ModelException.Detail(
            "IMPLEMENTATION_INHERITANCE_COMPATIBILITY_CONSTRAINT", Level.SEVERE,
            this.getMessage( "implementationInheritanceCompatibilityConstraint", new Object[]
            {
                implementation.getIdentifier(), superImplementation.getIdentifier(), superImplementation.getVersion(),
                expectedVersion
            } ) );

        detail.setElement( element );
        return detail;
    }

    private ModelException.Detail newSpecificationVersioningConstraintDetail(
        final JAXBElement<? extends ModelObject> element, final Implementation implementation,
        final Specification specification )
    {
        final ModelException.Detail detail = new ModelException.Detail(
            "SPECIFICATION_VERSIONING_CONSTRAINT", Level.SEVERE,
            this.getMessage( "specificationVersioningConstraint", new Object[]
            {
                implementation.getIdentifier(), specification.getIdentifier()
            } ) );

        detail.setElement( element );
        return detail;
    }

    private ModelException.Detail newImplementationVersioningConstraintDetail(
        final JAXBElement<? extends ModelObject> element, final Implementation declaring,
        final Implementation implementation )
    {
        final ModelException.Detail detail = new ModelException.Detail(
            "IMPLEMENTATION_VERSIONING_CONSTRAINT", Level.SEVERE,
            this.getMessage( "implementationVersioningConstraint", new Object[]
            {
                declaring.getIdentifier(), implementation.getIdentifier()
            } ) );

        detail.setElement( element );
        return detail;
    }

    // SECTION-END
}
