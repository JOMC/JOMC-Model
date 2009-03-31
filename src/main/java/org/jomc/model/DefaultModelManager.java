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
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.util.JAXBSource;
import javax.xml.validation.Validator;
import org.jomc.model.util.ParseException;
import org.jomc.model.util.TokenMgrError;
import org.jomc.model.util.VersionParser;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * Default {@code ModelManager} implementation.
 * <p>If not stated otherwise, all methods of this class throw an instance of {@code ModelError} for any model related
 * unrecoverable errors.</p>
 *
 * @author <a href="mailto:cs@schulte.it">Christian Schulte</a>
 * @version $Id$
 */
public class DefaultModelManager implements ModelManager
{
    // SECTION-START[ModelManager]

    /**
     * {@inheritDoc}
     *
     * @see #isClasspathAware()
     * @see #isValidating()
     */
    public Modules getModules()
    {
        try
        {
            if ( this.modules == null )
            {
                if ( this.isClasspathAware() )
                {
                    this.modules = this.getClasspathModules( MODEL_LOCATION );

                    final Module classpath = this.getClasspathModule();

                    if ( classpath != null )
                    {
                        this.modules.getModule().add( classpath );
                    }
                }
                else
                {
                    this.modules = new Modules();
                    final Texts texts = new Texts();
                    final Text text = new Text();

                    this.modules.setModelVersion( MODEL_VERSION );
                    this.modules.setDocumentation( texts );
                    texts.setDefaultLanguage( "en" );
                    texts.getText().add( text );
                    text.setLanguage( "en" );
                    text.setValue( new MessageFormat( this.getMessage( "defaultModulesInfo" ) ).format( null ) );
                }

                if ( this.isValidating() )
                {
                    this.assertValidModules( this.modules );
                }
            }

            return this.modules;
        }
        catch ( Throwable t )
        {
            throw new ModelError( t.getMessage(), t );
        }
    }

    public Module getModuleOfSpecification( final String specification )
    {
        if ( specification == null )
        {
            throw new NullPointerException( "specification" );
        }

        for ( Module m : this.getModules().getModule() )
        {
            if ( m.getSpecifications() != null )
            {
                for ( Specification s : m.getSpecifications().getSpecification() )
                {
                    if ( specification.equals( s.getIdentifier() ) )
                    {
                        return m;
                    }
                }
            }
        }

        return null;
    }

    public Module getModuleOfImplementation( final String implementation )
    {
        if ( implementation == null )
        {
            throw new NullPointerException( "implementation" );
        }

        for ( Module m : this.getModules().getModule() )
        {
            if ( m.getImplementations() != null )
            {
                for ( Implementation i : m.getImplementations().getImplementation() )
                {
                    if ( implementation.equals( i.getIdentifier() ) )
                    {
                        return m;
                    }
                }
            }
        }

        return null;
    }

    public Module getImplementationModule( final String implementation )
    {
        if ( implementation == null )
        {
            throw new NullPointerException( "implementation" );
        }

        Module module = null;
        final Implementation i = this.getImplementation( implementation );

        if ( i != null )
        {
            final Modules m = new Modules();
            module = new Module();
            module.setSpecifications( new Specifications() );
            module.setImplementations( new Implementations() );
            module.setName( i.getIdentifier() );
            module.setVersion( i.getVersion() );
            module.setModelVersion( MODEL_VERSION );
            m.getModule().add( module );

            this.collectImplementationModule( module, implementation );

            final Dependencies dependencies = this.getDependencies( implementation );
            final List<SpecificationReference> references = this.getSpecifications( implementation );

            if ( dependencies != null )
            {
                for ( Dependency d : dependencies.getDependency() )
                {
                    if ( module.getSpecifications().getSpecification( d.getIdentifier() ) == null )
                    {
                        final Specification s = this.getSpecification( d.getIdentifier() );

                        if ( s != null )
                        {
                            module.getSpecifications().getSpecification().add( s );
                        }
                    }
                }
            }

            if ( references != null )
            {
                for ( SpecificationReference ref : references )
                {
                    if ( module.getSpecifications().getSpecification( ref.getIdentifier() ) == null )
                    {
                        final Specification s = this.getSpecification( ref.getIdentifier() );
                        if ( s != null )
                        {
                            module.getSpecifications().getSpecification().add( s );
                        }
                    }
                }
            }
        }

        return module;
    }

    private void collectImplementationModule( final Module module, final String parent )
    {
        final Implementation i = getImplementation( parent );
        if ( i != null )
        {
            if ( i.getParent() != null )
            {
                this.collectImplementationModule( module, i.getParent() );
            }

            if ( module.getImplementations().getImplementation( i.getIdentifier() ) == null )
            {
                final Module m = getModuleOfImplementation( i.getIdentifier() );
                module.getImplementations().getImplementation().add( i );

                if ( i.getMessages() != null )
                {
                    for ( Iterator<MessageReference> ref = i.getMessages().getReference().iterator();
                          ref.hasNext(); )
                    {
                        i.getMessages().getMessage().add( m.getMessages().getMessage( ref.next().getName() ) );
                        ref.remove();
                    }
                }
            }
        }
    }

    public Specification getSpecification( final String specification )
    {
        if ( specification == null )
        {
            throw new NullPointerException( "specification" );
        }

        for ( Module m : this.getModules().getModule() )
        {
            if ( m.getSpecifications() != null )
            {
                final Specification s = m.getSpecifications().getSpecification( specification );
                if ( s != null )
                {
                    return s;
                }
            }
        }

        return null;
    }

    public List<SpecificationReference> getSpecifications( final String implementation )
    {
        if ( implementation == null )
        {
            throw new NullPointerException( "implementation" );
        }

        Specifications specs = new Specifications();
        this.collectSpecifications( implementation, specs );
        return specs.getReference().isEmpty() ? null : specs.getReference();
    }

    private void collectSpecifications( final String implementation, final Specifications specifications )
    {
        final Implementation i = getImplementation( implementation );

        if ( i != null )
        {
            if ( i.getParent() != null )
            {
                this.collectSpecifications( i.getParent(), specifications );
            }

            if ( i.getSpecifications() != null )
            {
                for ( SpecificationReference ref : i.getSpecifications().getReference() )
                {
                    if ( specifications.getReference( ref.getIdentifier() ) == null )
                    {
                        specifications.getReference().add( ref );
                    }
                }
            }
        }
    }

    public Implementation getImplementation( final String implementation )
    {
        if ( implementation == null )
        {
            throw new NullPointerException( "implementation" );
        }

        for ( Module m : this.getModules().getModule() )
        {
            if ( m.getImplementations() != null )
            {
                final Implementation i = m.getImplementations().getImplementation( implementation );
                if ( i != null )
                {
                    return i;
                }
            }
        }

        return null;
    }

    public Implementation getImplementation( final Class implementation )
    {
        if ( implementation == null )
        {
            throw new NullPointerException( "implementation" );
        }

        for ( Module m : this.getModules().getModule() )
        {
            if ( m.getImplementations() != null )
            {
                final Implementation i = m.getImplementations().getImplementation( implementation );
                if ( i != null )
                {
                    return i;
                }
            }
        }

        return null;
    }

    public Implementation getImplementation( final String specification, final String name )
    {
        if ( specification == null )
        {
            throw new NullPointerException( "specification" );
        }
        if ( name == null )
        {
            throw new NullPointerException( "name" );
        }

        final Implementations implementations = this.getImplementations( specification );
        if ( implementations != null )
        {
            return implementations.getImplementationByName( name );
        }

        return null;
    }

    public Dependencies getDependencies( final String implementation )
    {
        if ( implementation == null )
        {
            throw new NullPointerException( "implementation" );
        }

        final Dependencies dependencies = new Dependencies();
        this.collectDependencies( implementation, dependencies );
        Collections.sort( dependencies.getDependency(), new Comparator<Dependency>()
        {

            public int compare( final Dependency o1, final Dependency o2 )
            {
                return o1.getName().compareTo( o2.getName() );
            }

        } );

        return dependencies.getDependency().size() > 0 ? dependencies : null;
    }

    private void collectDependencies( final String implementation, final Dependencies dependencies )
    {
        final Implementation i = getImplementation( implementation );

        if ( i != null )
        {
            if ( i.getDependencies() != null )
            {
                for ( Dependency d : i.getDependencies().getDependency() )
                {
                    if ( dependencies.getDependency( d.getName() ) == null )
                    {
                        dependencies.getDependency().add( d );
                    }
                }
            }

            if ( i.getParent() != null )
            {
                this.collectDependencies( i.getParent(), dependencies );
            }
        }
    }

    public Properties getProperties( final String implementation )
    {
        if ( implementation == null )
        {
            throw new NullPointerException( "implementation" );
        }

        final Properties properties = new Properties();
        this.collectProperties( implementation, properties );
        final Properties specified = this.getSpecifiedProperties( implementation );

        if ( specified != null )
        {
            for ( Property p : specified.getProperty() )
            {
                if ( properties.getProperty( p.getName() ) == null )
                {
                    properties.getProperty().add( p );
                }
            }
        }

        Collections.sort( properties.getProperty(), new Comparator<Property>()
        {

            public int compare( final Property o1, final Property o2 )
            {
                return o1.getName().compareTo( o2.getName() );
            }

        } );

        return properties.getProperty().size() > 0 ? properties : null;
    }

    private void collectProperties( final String implementation, final Properties properties )
    {
        final Implementation i = getImplementation( implementation );

        if ( i != null )
        {
            if ( i.getProperties() != null )
            {
                for ( Property p : i.getProperties().getProperty() )
                {
                    if ( properties.getProperty( p.getName() ) == null )
                    {
                        properties.getProperty().add( p );
                    }
                }
            }

            if ( i.getParent() != null )
            {
                this.collectProperties( i.getParent(), properties );
            }
        }
    }

    public Properties getSpecifiedProperties( final String implementation )
    {
        if ( implementation == null )
        {
            throw new NullPointerException( "implementation" );
        }

        final Properties properties = new Properties();

        final List<SpecificationReference> references = this.getSpecifications( implementation );

        if ( references != null )
        {
            for ( SpecificationReference r : references )
            {
                final Specification s = this.getSpecification( r.getIdentifier() );

                if ( s != null && s.getProperties() != null )
                {
                    properties.getProperty().addAll( s.getProperties().getProperty() );
                }
            }
        }

        Collections.sort( properties.getProperty(), new Comparator<Property>()
        {

            public int compare( final Property o1, final Property o2 )
            {
                return o1.getName().compareTo( o2.getName() );
            }

        } );

        return properties.getProperty().size() > 0 ? properties : null;
    }

    public Messages getMessages( final String implementation )
    {
        if ( implementation == null )
        {
            throw new NullPointerException( "implementation" );
        }

        final Messages msgs = new Messages();
        this.collectMessages( implementation, msgs );
        Collections.sort( msgs.getMessage(), new Comparator<Message>()
        {

            public int compare( final Message o1, final Message o2 )
            {
                return o1.getName().compareTo( o2.getName() );
            }

        } );

        return msgs.getMessage().size() > 0 ? msgs : null;
    }

    private void collectMessages( final String implementation, final Messages messages )
    {
        final Implementation i = getImplementation( implementation );

        if ( i != null )
        {
            if ( i.getMessages() != null )
            {
                for ( Message msg : i.getMessages().getMessage() )
                {
                    if ( messages.getMessage( msg.getName() ) == null )
                    {
                        messages.getMessage().add( msg );
                    }
                }
                if ( !i.getMessages().getReference().isEmpty() )
                {
                    final Module m = getModuleOfImplementation( i.getIdentifier() );

                    if ( m != null )
                    {
                        for ( MessageReference ref : i.getMessages().getReference() )
                        {
                            if ( messages.getMessage( ref.getName() ) == null )
                            {
                                messages.getMessage().add( m.getMessages().getMessage( ref.getName() ) );
                            }
                        }
                    }
                }
            }

            if ( i.getParent() != null )
            {
                this.collectMessages( i.getParent(), messages );
            }
        }
    }

    public Implementations getImplementations( final String specification )
    {
        if ( specification == null )
        {
            throw new NullPointerException( "specification" );
        }

        final Implementations implementations = new Implementations();
        for ( Module m : this.getModules().getModule() )
        {
            if ( m.getImplementations() != null )
            {
                for ( Implementation i : m.getImplementations().getImplementation() )
                {
                    final List<SpecificationReference> references = this.getSpecifications( i.getIdentifier() );

                    if ( references != null )
                    {
                        for ( SpecificationReference ref : references )
                        {
                            if ( specification.equals( ref.getIdentifier() ) )
                            {
                                implementations.getImplementation().add( i );
                            }
                        }
                    }
                }
            }
        }

        return implementations.getImplementation().size() > 0 ? implementations : null;
    }

    public Implementations getImplementations( final String implementation, final String dependency )
    {
        if ( implementation == null )
        {
            throw new NullPointerException( "implementation" );
        }
        if ( dependency == null )
        {
            throw new NullPointerException( "dependency" );
        }

        final Implementations implementations = new Implementations();
        final Implementation i = this.getImplementation( implementation );

        if ( i != null )
        {
            final Dependencies dependencies = this.getDependencies( implementation );

            if ( dependencies != null )
            {
                final Dependency d = dependencies.getDependency( dependency );

                if ( d != null )
                {
                    if ( d.getImplementationName() != null )
                    {
                        final Implementation di =
                            this.getImplementation( d.getIdentifier(), d.getImplementationName() );

                        if ( di != null )
                        {
                            implementations.getImplementation().add( di );
                        }
                    }
                    else
                    {
                        final Implementations available = this.getImplementations( d.getIdentifier() );

                        if ( available != null )
                        {
                            implementations.getImplementation().addAll( available.getImplementation() );
                        }
                    }
                }
            }
        }

        return implementations.getImplementation().size() > 0 ? implementations : null;
    }

    public Instance getInstance( final String implementation )
    {
        if ( implementation == null )
        {
            throw new NullPointerException( "implementation" );
        }

        final Implementation i = this.getImplementation( implementation );

        Instance instance = null;

        if ( i != null )
        {
            instance = new Instance();
            instance.setIdentifier( i.getIdentifier() );
            instance.setClazz( i.getClazz() );
            instance.setModelVersion( MODEL_VERSION );
            instance.setScope( Scope.MULTITON );
            instance.setDependencies( this.getDependencies( i.getIdentifier() ) );
            instance.setProperties( this.getProperties( i.getIdentifier() ) );
            instance.setMessages( this.getMessages( i.getIdentifier() ) );
        }

        return instance;
    }

    public Instance getInstance( final String specification, final String name )
    {
        if ( specification == null )
        {
            throw new NullPointerException( "specification" );
        }
        if ( name == null )
        {
            throw new NullPointerException( "name" );
        }

        final Specification s = this.getSpecification( specification );

        Instance instance = null;

        if ( s != null )
        {
            final Implementation i = this.getImplementation( specification, name );

            if ( i != null )
            {
                instance = this.getInstance( i.getIdentifier() );

                if ( instance != null )
                {
                    instance.setScope( s.getScope() );
                }
            }
        }

        return instance;
    }

    public Instance getInstance( final String specification, final String name, final Dependency dependency )
    {
        if ( specification == null )
        {
            throw new NullPointerException( "specification" );
        }
        if ( name == null )
        {
            throw new NullPointerException( "name" );
        }
        if ( dependency == null )
        {
            throw new NullPointerException( "dependency" );
        }

        final Instance instance = this.getInstance( specification, name );

        if ( instance != null )
        {
            final Properties properties = new Properties();

            if ( dependency.getProperties() != null )
            {
                properties.getProperty().addAll( dependency.getProperties().getProperty() );
            }

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

            instance.setProperties( properties.getProperty().isEmpty() ? null : properties );
        }

        return instance;
    }

    public void assertValidModelObject( final JAXBElement<? extends ModelObject> modelObject ) throws ModelException
    {
        final StringBuffer validationEvents = new StringBuffer();
        validationEvents.append( "\n\n" );

        try
        {
            final Validator validator = this.getModelResolver().getSchema().newValidator();

            validator.setErrorHandler( new ErrorHandler()
            {

                public void warning( final SAXParseException exception ) throws SAXException
                {
                    validationEvents.append( "[WARNING]" );
                    validationEvents.append( " [" ).append( exception.getLineNumber() ).append( "," ).
                        append( exception.getColumnNumber() ).append( "]" );

                    if ( exception.getMessage() != null )
                    {
                        validationEvents.append( " " ).append( exception.getMessage() );
                    }

                    if ( exception.getPublicId() != null )
                    {
                        validationEvents.append( " [" ).append( exception.getPublicId() ).append( ']' );
                    }

                    if ( exception.getSystemId() != null )
                    {
                        validationEvents.append( " [" ).append( exception.getSystemId() ).append( ']' );
                    }

                    getLogger().log( Level.WARNING, validationEvents.toString() );
                    validationEvents.append( "\n" );
                }

                public void error( final SAXParseException exception ) throws SAXException
                {
                    validationEvents.append( "[ERROR]" );
                    validationEvents.append( " [" ).append( exception.getLineNumber() ).append( "," ).
                        append( exception.getColumnNumber() ).append( "]" );

                    if ( exception.getMessage() != null )
                    {
                        validationEvents.append( " " ).append( exception.getMessage() );
                    }

                    if ( exception.getPublicId() != null )
                    {
                        validationEvents.append( " [" ).append( exception.getPublicId() ).append( ']' );
                    }

                    if ( exception.getSystemId() != null )
                    {
                        validationEvents.append( " [" ).append( exception.getSystemId() ).append( ']' );
                    }

                    validationEvents.append( "\n" );
                    throw exception;
                }

                public void fatalError( final SAXParseException exception ) throws SAXException
                {
                    validationEvents.append( "[FATAL]" );
                    validationEvents.append( " [" ).append( exception.getLineNumber() ).append( "," ).
                        append( exception.getColumnNumber() ).append( "]" );

                    if ( exception.getMessage() != null )
                    {
                        validationEvents.append( " " ).append( exception.getMessage() );
                    }

                    if ( exception.getPublicId() != null )
                    {
                        validationEvents.append( " [" ).append( exception.getPublicId() ).append( ']' );
                    }

                    if ( exception.getSystemId() != null )
                    {
                        validationEvents.append( " [" ).append( exception.getSystemId() ).append( ']' );
                    }

                    validationEvents.append( "\n" );
                    throw exception;
                }

            } );

            validator.validate( new JAXBSource( this.getModelResolver().getMarshaller( false, false ), modelObject ) );
        }
        catch ( SAXException e )
        {
            throw new ModelException( validationEvents.toString(), e, modelObject );
        }
        catch ( Throwable t )
        {
            throw new ModelError( t.getMessage(), t );
        }
    }

    public void assertValidModules( final Modules modules ) throws ModelException
    {
        if ( modules == null )
        {
            throw new NullPointerException( "modules" );
        }

        this.assertValidModelObject( this.getModelResolver().getObjectFactory().createModules( modules ) );

        try
        {
            for ( Module m : modules.getModule() )
            {
                if ( m.getImplementations() != null )
                {
                    for ( Implementation i : m.getImplementations().getImplementation() )
                    {
                        final List<SpecificationReference> specs = this.getSpecifications( i.getIdentifier() );
                        final Dependencies deps = this.getDependencies( i.getIdentifier() );

                        if ( specs != null )
                        {
                            for ( SpecificationReference r : specs )
                            {
                                final Specification s = this.getSpecification( r.getIdentifier() );

                                if ( s != null && r.getVersion() != null && s.getVersion() != null &&
                                     VersionParser.compare( r.getVersion(), s.getVersion() ) != 0 )
                                {
                                    this.throwIncompatibleImplementationException(
                                        this.getModelResolver().getObjectFactory().createImplementation( i ),
                                        i.getIdentifier(), r.getIdentifier(), r.getVersion(), s.getVersion() );

                                }
                            }
                        }

                        if ( deps != null )
                        {
                            for ( Dependency d : deps.getDependency() )
                            {
                                final Specification s = this.getSpecification( d.getIdentifier() );

                                if ( s != null && s.getVersion() != null && d.getVersion() != null &&
                                     VersionParser.compare( d.getVersion(), s.getVersion() ) > 0 )
                                {
                                    this.throwIncompatibleDependencyException(
                                        this.getModelResolver().getObjectFactory().createDependency( d ),
                                        i.getIdentifier(), d.getIdentifier(), d.getVersion(), s.getVersion() );

                                }

                                if ( s.getScope() != Scope.MULTITON && d.getProperties() != null &&
                                     !d.getProperties().getProperty().isEmpty() )
                                {
                                    this.throwPropertyOverwriteConstraintException(
                                        this.getModelResolver().getObjectFactory().createDependency( d ),
                                        i.getIdentifier(), d.getName(), s.getIdentifier() );

                                }
                            }
                        }
                    }
                }

                if ( m.getSpecifications() != null )
                {
                    for ( Specification s : m.getSpecifications().getSpecification() )
                    {
                        final Implementations impls = this.getImplementations( s.getIdentifier() );

                        if ( impls != null )
                        {
                            final Map<String, Implementation> map = new HashMap<String, Implementation>();

                            for ( Implementation i : impls.getImplementation() )
                            {
                                if ( map.containsKey( i.getName() ) )
                                {
                                    this.throwImplementationNameConstraintException(
                                        this.getModelResolver().getObjectFactory().createSpecification( s ),
                                        s.getIdentifier(), i.getIdentifier() + ", " +
                                                           map.get( i.getName() ).getIdentifier() );

                                }
                            }
                        }
                    }
                }
            }
        }
        catch ( TokenMgrError e )
        {
            throw new ModelException( e, this.getModelResolver().getObjectFactory().createModules( modules ) );
        }
        catch ( ParseException e )
        {
            throw new ModelException( e, this.getModelResolver().getObjectFactory().createModules( modules ) );
        }
        catch ( ModelException e )
        {
            throw e;
        }
        catch ( Throwable t )
        {
            throw new ModelError( t.getMessage(), t );
        }
    }

    public Object createObject( final String specification, final String name, final ClassLoader classLoader )
        throws InstantiationException
    {
        if ( specification == null )
        {
            throw new NullPointerException( "specification" );
        }
        if ( name == null )
        {
            throw new NullPointerException( "name" );
        }
        if ( classLoader == null )
        {
            throw new NullPointerException( "classLoader" );
        }

        Object instance = null;

        try
        {
            final Specification s = this.getSpecification( specification );
            final Implementation i = this.getImplementation( specification, name );

            if ( s != null && i != null )
            {
                final Class specClass = Class.forName( s.getIdentifier(), true, classLoader );
                final Class implClass = Class.forName( i.getClazz(), true, classLoader );

                if ( Modifier.isPublic( implClass.getModifiers() ) )
                {
                    Constructor ctor = null;
                    try
                    {
                        ctor = implClass.getConstructor( new Class[ 0 ] );
                    }
                    catch ( NoSuchMethodException e )
                    {
                        if ( this.getLogger().isLoggable( Level.FINE ) )
                        {
                            this.getLogger().log( Level.FINE, e.getMessage(), e );
                        }

                        ctor = null;
                    }

                    if ( ctor != null && specClass.isAssignableFrom( implClass ) )
                    {
                        instance = implClass.newInstance();
                    }
                    else
                    {
                        final StringBuffer methodNames = new StringBuffer().append( '[' );
                        Method factoryMethod = null;
                        String methodName = null;

                        char[] c = i.getName().toCharArray();
                        c[0] = Character.toUpperCase( c[0] );
                        methodName = "get" + String.valueOf( c );
                        methodNames.append( methodName );
                        factoryMethod = this.getFactoryMethod( implClass, specClass, methodName );

                        if ( factoryMethod == null )
                        {
                            c = s.getIdentifier().substring( s.getIdentifier().lastIndexOf( '.' ) + 1 ).toCharArray();
                            c[0] = Character.toUpperCase( c[0] );
                            methodName = "get" + String.valueOf( c );
                            methodNames.append( ", " ).append( methodName );
                            factoryMethod = this.getFactoryMethod( implClass, specClass, "get" + String.valueOf( c ) );
                        }

                        if ( factoryMethod == null )
                        {
                            methodName = "getObject";
                            methodNames.append( ", " ).append( methodName );
                            factoryMethod = this.getFactoryMethod( implClass, null, methodName );
                        }

                        if ( factoryMethod == null )
                        {
                            throw new ModelError( new MessageFormat( this.getMessage( "missingFactoryMethod" ) ).format(
                                new Object[]
                                {
                                    implClass.getName(), specClass.getName(), methodNames.append( ']' ).toString()
                                } ) );

                        }

                        if ( Modifier.isStatic( factoryMethod.getModifiers() ) )
                        {
                            instance = factoryMethod.invoke( null, new Object[ 0 ] );
                        }
                        else if ( ctor != null )
                        {
                            instance = factoryMethod.invoke( ctor.newInstance(), new Object[ 0 ] );
                        }
                        else
                        {
                            throw new ModelError( new MessageFormat( this.getMessage( "missingFactoryMethod" ) ).format(
                                new Object[]
                                {
                                    implClass.getName(), specClass.getName(), methodNames.append( ']' ).toString()
                                } ) );

                        }
                    }
                }
            }

            return instance;
        }
        catch ( InstantiationException e )
        {
            throw e;
        }
        catch ( InvocationTargetException e )
        {
            throw new ModelError( e.getTargetException() != null
                                  ? e.getTargetException().getMessage() : e.getMessage(), e );

        }
        catch ( Throwable e )
        {
            throw new ModelError( e.getMessage(), e );
        }
    }

    // SECTION-END
    // SECTION-START[DefaultModelManager]

    /** Constant for the classpath module name. */
    public static final String CLASSPATH_MODULE_NAME = "Java Classpath";

    /** Classpath location searched for container documents by default. */
    public static final String MODEL_LOCATION = "META-INF/jomc.xml";

    /** Constant for the version of classpath module. */
    private static final String CLASSPATH_MODULE_VERSION = "1.0";

    /** Model version used by this manager. */
    private static final String MODEL_VERSION = "1.0";

    /** Classloader of the instance. */
    private ClassLoader classLoader;

    /** Model resolver of the instance. */
    private ModelResolver modelResolver;

    /** Modules of the instance. */
    private Modules modules;

    /** Flag indicating that classpath resolution is performed. */
    private boolean classpathAware;

    /** Flag indicating validation support. */
    private boolean validating = true;

    /** The logger of the instance. */
    private Logger logger;

    /** Creates a new {@code DefaultModelManager} instance. */
    public DefaultModelManager()
    {
        this( null );
    }

    /**
     * Creates a new {@code DefaultModelManager} instance taking a classloader to resolve entities with.
     *
     * @param classLoader The classloader to resolve entities with.
     */
    public DefaultModelManager( final ClassLoader classLoader )
    {
        super();
        this.classLoader = classLoader;
    }

    /**
     * Gets the flag indicating that classpath resolution is performed.
     *
     * @return {@code true} if the classloader of the instance is searched for container documents; {@code false} if no
     * classpath resolution is performed.
     */
    public boolean isClasspathAware()
    {
        return this.classpathAware;
    }

    /**
     * Sets the flag indicating that classpath resolution is performed.
     *
     * @param value {@code true} if the classloader of the instance should be searched for container documents;
     * {@code false} if no classpath resolution should be performed.
     */
    public void setClasspathAware( final boolean value )
    {
        if ( this.classpathAware != value )
        {
            this.modules = null;
        }

        this.classpathAware = value;
    }

    /**
     * Gets the flag indicating validation support.
     *
     * @return {@code true} if validation is performed; {@code false} if no validation is performed.
     */
    public boolean isValidating()
    {
        return this.validating;
    }

    /**
     * Sets the flag indicating validation support.
     *
     * @param value {@code true} to perform validation; {@code false} to not perform any validation.
     */
    public void setValidating( final boolean value )
    {
        this.validating = value;
    }

    /**
     * Gets modules by searching the classloader of the instance for container document resources.
     *
     * @param location The location to search at.
     *
     * @return All classpath documents from the classloader of the instance.
     *
     * @throws NullPointerException if {@code location} is {@code null}.
     * @throws IOException if reading resources fails.
     *
     * @see #MODEL_LOCATION
     */
    public Modules getClasspathModules( final String location ) throws IOException
    {
        if ( location == null )
        {
            throw new NullPointerException( "location" );
        }

        try
        {
            Modules mods = new Modules();
            final Texts texts = new Texts();
            final Text text = new Text();

            mods.setModelVersion( MODEL_VERSION );
            mods.setDocumentation( texts );
            texts.setDefaultLanguage( "en" );
            texts.getText().add( text );
            text.setLanguage( "en" );
            text.setValue( new MessageFormat( this.getMessage( "classpathModulesInfo" ) ).format( new Object[]
                {
                    location
                } ) );

            final Unmarshaller u = this.getModelResolver().getUnmarshaller( false );
            final Enumeration<URL> resources = this.getClassLoader().getResources( location );

            while ( resources.hasMoreElements() )
            {
                final Object content = ( (JAXBElement) u.unmarshal( resources.nextElement() ) ).getValue();

                if ( content instanceof Module )
                {
                    mods.getModule().add( (Module) content );
                }
                else if ( content instanceof Modules )
                {
                    mods = (Modules) content;
                    break;
                }
            }

            return mods;
        }
        catch ( IOException e )
        {
            throw e;
        }
        catch ( SAXException e )
        {
            throw new ModelError( e.getException() != null ? e.getException().getMessage() : e.getMessage(), e );
        }
        catch ( JAXBException e )
        {
            throw new ModelError( e.getLinkedException() != null
                                  ? e.getLinkedException().getMessage() : e.getMessage(), e );

        }
        catch ( Throwable t )
        {
            throw new ModelError( t.getMessage(), t );
        }
    }

    /**
     * Gets a module holding model objects resolved by inspecting the classloader of the instance.
     * <p>This method searches the modules of the instance for unresolved references and tries to resolve each
     * unresolved reference by inspecting the classloader of the instance.</p>
     *
     * @return A module holding model objects resolved by inspecting the classloader of the instance or {@code null} if
     * nothing could be resolved.
     *
     * @see #CLASSPATH_MODULE_NAME
     */
    public Module getClasspathModule()
    {
        try
        {
            final Module module = new Module();
            module.setModelVersion( MODEL_VERSION );
            module.setVersion( CLASSPATH_MODULE_VERSION );
            module.setName( CLASSPATH_MODULE_NAME );
            module.setVendor( System.getProperty( "java.vendor" ) );

            this.resolveClasspath( module );

            final boolean resolved = ( module.getSpecifications() != null &&
                                       !module.getSpecifications().getSpecification().isEmpty() ) ||
                                     ( module.getImplementations() != null &&
                                       !module.getImplementations().getImplementation().isEmpty() );

            return resolved ? module : null;
        }
        catch ( Throwable t )
        {
            throw new ModelError( t.getMessage(), t );
        }
    }

    /**
     * Gets the {@code ModelResolver} of the instance.
     *
     * @return The {@code ModelResolver} of the instance.
     */
    protected ModelResolver getModelResolver()
    {
        if ( this.modelResolver == null )
        {
            this.modelResolver = new ModelResolver( this.getClassLoader() );
        }

        return this.modelResolver;
    }

    /**
     * Gets the classloader of the instance.
     *
     * @return The classloader of the instance.
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

    /**
     * Gets the logger of the instance.
     *
     * @return The logger of the instance.
     */
    protected Logger getLogger()
    {
        if ( this.logger == null )
        {
            this.logger = Logger.getLogger( this.getClass().getName() );
        }

        return this.logger;
    }

    /**
     * Resolves references by inspecting the classloader of the instance.
     *
     * @param cpModule The module for resolved references.
     *
     * @throws NullPointerException if {@code cpModule} is {@code null}.
     */
    private void resolveClasspath( final Module cpModule )
    {
        for ( Module m : this.getModules().getModule() )
        {
            if ( m.getSpecifications() != null )
            {
                this.resolveClasspath( m.getSpecifications(), cpModule );
            }
            if ( m.getImplementations() != null )
            {
                this.resolveClasspath( m.getImplementations(), cpModule );
            }
        }
    }

    /**
     * Resolves references by inspecting a given classloader.
     *
     * @param ref The specification reference to resolve.
     * @param cpModule The module for resolved references.
     */
    private void resolveClasspath( final SpecificationReference ref, final Module cpModule )
    {
        Specification spec = this.getSpecification( ref.getIdentifier() );

        if ( spec == null )
        {
            if ( cpModule.getSpecifications() != null )
            {
                spec = cpModule.getSpecifications().getSpecification( ref.getIdentifier() );
            }

            if ( spec == null )
            {
                spec = this.resolveClasspath( ref.getIdentifier() );

                if ( spec != null )
                {
                    if ( cpModule.getSpecifications() == null )
                    {
                        cpModule.setSpecifications( new Specifications() );
                        cpModule.getSpecifications().setModelVersion( MODEL_VERSION );
                    }

                    cpModule.getSpecifications().getSpecification().add( spec );
                }
            }
        }
    }

    /**
     * Resolves references by inspecting a given classloader.
     *
     * @param references The specification references to resolve.
     * @param cpModule The module for resolved references.
     */
    private void resolveClasspath( final Specifications references, final Module cpModule )
    {
        for ( SpecificationReference ref : references.getReference() )
        {
            this.resolveClasspath( ref, cpModule );
        }
    }

    /**
     * Resolves references by inspecting a given classloader.
     *
     * @param implementations The implementations to resolve references with.
     * @param cpModule The module for resolved references.
     */
    private void resolveClasspath( final Implementations implementations, final Module cpModule )
    {
        for ( Implementation implementation : implementations.getImplementation() )
        {
            if ( implementation.getSpecifications() != null )
            {
                this.resolveClasspath( implementation.getSpecifications(), cpModule );
            }
            if ( implementation.getDependencies() != null )
            {
                this.resolveClasspath( implementation, implementation.getDependencies(), cpModule );
            }
        }
    }

    /**
     * Resolves references by inspecting a given classloader.
     *
     * @param implementation The implementation declaring {@code dependencies}.
     * @param dependencies The dependencies to resolve.
     * @param cpModule The classpath module for resolved references.
     */
    private void resolveClasspath( final Implementation implementation, final Dependencies dependencies,
                                   final Module cpModule )
    {
        for ( Dependency dependency : dependencies.getDependency() )
        {
            this.resolveClasspath( dependency, cpModule );

            Specification s = this.getSpecification( dependency.getIdentifier() );

            if ( s == null && cpModule.getSpecifications() != null )
            {
                s = cpModule.getSpecifications().getSpecification( dependency.getIdentifier() );
            }

            if ( s != null &&
                 ( s.getMultiplicity() == Multiplicity.ONE || dependency.getImplementationName() != null ) )
            {
                final Implementations a =
                    this.getImplementations( implementation.getIdentifier(), dependency.getName() );

                if ( a == null || a.getImplementation().isEmpty() ||
                     ( dependency.getImplementationName() != null &&
                       a.getImplementationByName( dependency.getImplementationName() ) == null ) )
                {
                    final Implementation resolved = this.resolveClasspath( s );

                    if ( resolved != null )
                    {
                        if ( cpModule.getImplementations() == null )
                        {
                            cpModule.setImplementations( new Implementations() );
                            cpModule.getImplementations().setModelVersion( MODEL_VERSION );
                        }

                        if ( cpModule.getImplementations().getImplementationByName( resolved.getName() ) == null )
                        {
                            cpModule.getImplementations().getImplementation().add( resolved );
                        }
                    }
                }
            }
        }
    }

    /**
     * Resolves references by inspecting the classloader of the instance.
     *
     * @param identifier The identifier to resolve.
     *
     * @return The specification resolved for {@code identifier} or {@code null}, if no manifest meta-data is available
     * for {@code identifier}.
     *
     * @throws NullPointerException if {@code identifier} is {@code null}.
     */
    private Specification resolveClasspath( final String identifier )
    {
        if ( identifier == null )
        {
            throw new NullPointerException( "identifier" );
        }

        Specification specification = null;

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
                specification.setModelVersion( MODEL_VERSION );
                specification.setMultiplicity( Multiplicity.MANY );
                specification.setScope( Scope.MULTITON );
                specification.setVendor( vendor );
                specification.setVersion( version );
            }
        }
        catch ( ClassNotFoundException e )
        {
            if ( this.getLogger().isLoggable( Level.FINE ) )
            {
                this.getLogger().log( Level.FINE, e.toString() );
            }
        }

        return specification;
    }

    /**
     * Resolves references by inspecting the classloader of the instance.
     *
     * @param specification The specification specifying the implementation to
     *
     * @return The resolved implementation as specified by {@code specification} or {@code null}, if no manifest
     * meta-data is available for {@code specification}.
     *
     * @throws NullPointerException if {@code specification} or is {@code null}.
     */
    private Implementation resolveClasspath( final Specification specification )
    {
        if ( specification == null )
        {
            throw new NullPointerException( "specification" );
        }

        Implementation implementation = null;
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
                        if ( this.getLogger().isLoggable( Level.FINE ) )
                        {
                            this.getLogger().log( Level.FINE, e.getMessage(), e );
                        }
                    }
                }

                if ( !classpathImplementation )
                {
                    final char[] c = specification.getIdentifier().substring(
                        specification.getIdentifier().lastIndexOf( '.' ) ).toCharArray();

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
                    implementation.setModelVersion( MODEL_VERSION );
                    implementation.setVendor( vendor );
                    implementation.setFinal( true );
                    implementation.setName( name );
                    implementation.setIdentifier( specification.getIdentifier() );
                    implementation.setClazz( specification.getIdentifier() );
                    implementation.setVersion( version );

                    final Specifications implemented = new Specifications();
                    final SpecificationReference ref = new SpecificationReference();
                    ref.setIdentifier( specification.getIdentifier() );
                    ref.setVersion( specification.getVersion() );
                    implemented.getReference().add( ref );
                    implementation.setSpecifications( implemented );
                }
            }
        }
        catch ( ClassNotFoundException e )
        {
            if ( this.getLogger().isLoggable( Level.FINE ) )
            {
                this.getLogger().log( Level.FINE, e.getMessage(), e );
            }
        }

        return implementation;
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
            if ( this.getLogger().isLoggable( Level.FINE ) )
            {
                this.getLogger().log( Level.FINE, e.getMessage(), e );
            }

            factoryMethod = false;
        }

        return factoryMethod;
    }

    private Method getFactoryMethod( final Class clazz, final Class type, final String methodName )
    {
        Method m = null;

        try
        {
            m = clazz.getMethod( methodName, new Class[ 0 ] );
            if ( type != null && !type.isAssignableFrom( m.getReturnType() ) )
            {
                m = null;
            }
        }
        catch ( NoSuchMethodException e )
        {
            if ( this.getLogger().isLoggable( Level.FINE ) )
            {
                this.getLogger().log( Level.FINE, e.getMessage(), e );
            }

            m = null;
        }

        return m;
    }

    private String getMessage( final String key )
    {
        return ResourceBundle.getBundle( "org/jomc/model/DefaultModelManager", Locale.getDefault() ).getString( key );
    }

    private void throwIncompatibleImplementationException( final JAXBElement element, final String implementation,
                                                           final String specification, final String implementedVersion,
                                                           final String specifiedVersion ) throws ModelException
    {
        final MessageFormat f = new MessageFormat( this.getMessage( "incompatibleImplementation" ) );
        throw new ModelException( f.format( new Object[]
            {
                implementation, specification, implementedVersion, specifiedVersion
            } ), element );

    }

    private void throwIncompatibleDependencyException( final JAXBElement element, final String implementation,
                                                       final String specification, final String requiredVersion,
                                                       final String availableVersion ) throws ModelException
    {
        final MessageFormat f = new MessageFormat( this.getMessage( "incompatibleDependency" ) );
        throw new ModelException( f.format( new Object[]
            {
                implementation, specification, requiredVersion, availableVersion
            } ), element );

    }

    private void throwPropertyOverwriteConstraintException( final JAXBElement element, final String implementation,
                                                            final String dependency, final String specification )
        throws ModelException
    {
        final MessageFormat f = new MessageFormat( this.getMessage( "propertyOverwriteConstraint" ) );
        throw new ModelException( f.format( new Object[]
            {
                implementation, dependency, specification
            } ), element );

    }

    private void throwImplementationNameConstraintException( final JAXBElement element, final String specification,
                                                             final String implementations ) throws ModelException
    {
        final MessageFormat f = new MessageFormat( this.getMessage( "implementationNameConstraint" ) );
        throw new ModelException( f.format( new Object[]
            {
                specification, implementations
            } ), element );

    }

    // SECTION-END
}
