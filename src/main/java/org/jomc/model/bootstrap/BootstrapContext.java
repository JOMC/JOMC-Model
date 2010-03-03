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
package org.jomc.model.bootstrap;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.Collection;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Map;
import java.util.TreeMap;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.util.JAXBSource;
import javax.xml.validation.Validator;
import org.xml.sax.SAXException;

/**
 * Object management and configuration model bootstrap context interface.
 *
 * @author <a href="mailto:schulte2005@users.sourceforge.net">Christian Schulte</a>
 * @version $Id$
 * @see #createBootstrapContext(java.lang.ClassLoader)
 */
public abstract class BootstrapContext
{

    /** Class name of the {@code BootstrapContext} implementation. */
    private static volatile String bootstrapContextClassName;

    /** The class loader of the context. */
    private ClassLoader classLoader;

    /**
     * Creates a new {@code BootstrapContext} instance taking a class loader.
     *
     * @param classLoader The class loader of the context.
     */
    public BootstrapContext( final ClassLoader classLoader )
    {
        super();
        this.classLoader = classLoader;
    }

    /**
     * Gets the class loader of the context.
     *
     * @return The class loader of the context.
     */
    public ClassLoader getClassLoader()
    {
        if ( this.classLoader == null )
        {
            this.classLoader = new ClassLoader( null )
            {

                @Override
                public String toString()
                {
                    return BootstrapContext.class.getName() + ".BootstrapClassLoader@" +
                           System.identityHashCode( this );

                }

            };

        }

        return this.classLoader;
    }

    /**
     * Gets the name of the class providing the {@code BootstrapContext} implementation.
     * <p>The name of the class providing the {@code BootstrapContext} implementation returned by method
     * {@link #createBootstrapContext(java.lang.ClassLoader)} is controlled by system property
     * {@code org.jomc.model.bootstrap.BootstrapContext.className}. If that property is not set, the name of the
     * {@link DefaultBootstrapContext} class is returned.</p>
     *
     * @return The name of the class providing the {@code BootstrapContext} implementation.
     *
     * @see #setBootstrapContextClassName(java.lang.String)
     */
    public static String getBootstrapContextClassName()
    {
        if ( bootstrapContextClassName == null )
        {
            bootstrapContextClassName = System.getProperty( "org.jomc.model.bootstrap.BootstrapContext.className",
                                                            DefaultBootstrapContext.class.getName() );

        }

        return bootstrapContextClassName;
    }

    /**
     * Sets the name of the class providing the {@code BootstrapContext} implementation.
     *
     * @param value The new name of the class providing the {@code BootstrapContext} implementation or {@code null}.
     *
     * @see #getBootstrapContextClassName()
     */
    public static void setBootstrapContextClassName( final String value )
    {
        bootstrapContextClassName = value;
    }

    /**
     * Searches the context for a class with a given name.
     *
     * @param name The name of the class to return.
     *
     * @return A Class object of the class with name {@code name} or {@code null} if no such class is found.
     *
     * @throws NullPointerException if {@code name} is {@code null}.
     * @throws BootstrapException if searching fails.
     */
    public Class findClass( final String name ) throws BootstrapException
    {
        if ( name == null )
        {
            throw new NullPointerException( "name" );
        }

        try
        {
            return Class.forName( name, true, this.getClassLoader() );
        }
        catch ( final ClassNotFoundException e )
        {
            return null;
        }
    }

    /**
     * Searches the context for a resource with a given name.
     *
     * @param name The name of the resource to return.
     *
     * @return An URL object for reading the resource or {@code null} if no such resource is found.
     *
     * @throws NullPointerException if {@code name} is {@code null}.
     * @throws BootstrapException if searching fails.
     */
    public URL findResource( final String name ) throws BootstrapException
    {
        if ( name == null )
        {
            throw new NullPointerException( "name" );
        }

        return this.getClassLoader().getResource( name );
    }

    /**
     * Searches the context for resources with a given name.
     *
     * @param name The name of the resources to return.
     *
     * @return An enumeration of URL objects for reading the resources. If no resources are found, the enumeration will
     * be empty.
     *
     * @throws NullPointerException if {@code name} is {@code null}.
     * @throws BootstrapException if searching fails.
     */
    public Enumeration<URL> findResources( final String name ) throws BootstrapException
    {
        if ( name == null )
        {
            throw new NullPointerException( "name" );
        }

        try
        {
            return this.getClassLoader().getResources( name );
        }
        catch ( final IOException e )
        {
            throw new BootstrapException( e.getMessage(), e );
        }
    }

    /**
     * Searches the context for schemas.
     * <p>This method loads {@code SchemaProvider} classes setup via
     * {@code META-INF/services/org.jomc.model.bootstrap.SchemaProvider} resources and returns a list of provided
     * schemas.</p>
     *
     * @return The schemas found in the context.
     *
     * @throws BootstrapException if searching schemas fails.
     *
     * @see SchemaProvider#findSchemas(org.jomc.model.bootstrap.BootstrapContext)
     */
    public Schemas findSchemas() throws BootstrapException
    {
        try
        {
            final Schemas schemas = new Schemas();

            final Collection<Class<SchemaProvider>> providers = this.loadProviders( SchemaProvider.class );
            for ( Class<SchemaProvider> provider : providers )
            {
                final SchemaProvider schemaProvider = provider.newInstance();
                final Schemas provided = schemaProvider.findSchemas( this );
                if ( provided != null )
                {
                    schemas.getSchema().addAll( provided.getSchema() );
                }
            }

            final javax.xml.validation.Schema bootstrapSchema = this.createSchema();
            final Validator validator = bootstrapSchema.newValidator();
            validator.validate( new JAXBSource( this.createContext(), new ObjectFactory().createSchemas( schemas ) ) );

            return schemas;
        }
        catch ( final InstantiationException e )
        {
            throw new BootstrapException( e.getMessage(), e );
        }
        catch ( final IllegalAccessException e )
        {
            throw new BootstrapException( e.getMessage(), e );
        }
        catch ( final JAXBException e )
        {
            throw new BootstrapException( e.getMessage(), e );
        }
        catch ( final SAXException e )
        {
            throw new BootstrapException( e.getMessage(), e );
        }
        catch ( final IOException e )
        {
            throw new BootstrapException( e.getMessage(), e );
        }
    }

    /**
     * Searches the context for services.
     * <p>This method loads {@code ServiceProvider} classes setup via
     * {@code META-INF/services/org.jomc.model.bootstrap.ServiceProvider} resources and returns a list of provided
     * services.</p>
     *
     * @return The services found in the context.
     *
     * @throws BootstrapException if searching services fails.
     *
     * @see ServiceProvider#findServices(org.jomc.model.bootstrap.BootstrapContext)
     */
    public Services findServices() throws BootstrapException
    {
        try
        {
            final Services services = new Services();

            final Collection<Class<ServiceProvider>> providers = this.loadProviders( ServiceProvider.class );
            for ( Class<ServiceProvider> provider : providers )
            {
                final ServiceProvider serviceProvider = provider.newInstance();
                final Services provided = serviceProvider.findServices( this );
                if ( provided != null )
                {
                    services.getService().addAll( provided.getService() );
                }
            }

            final javax.xml.validation.Schema bootstrapSchema = this.createSchema();
            final Validator validator = bootstrapSchema.newValidator();
            validator.validate( new JAXBSource( this.createContext(), new ObjectFactory().createServices( services ) ) );

            return services;
        }
        catch ( final InstantiationException e )
        {
            throw new BootstrapException( e.getMessage(), e );
        }
        catch ( final IllegalAccessException e )
        {
            throw new BootstrapException( e.getMessage(), e );
        }
        catch ( final JAXBException e )
        {
            throw new BootstrapException( e.getMessage(), e );
        }
        catch ( final SAXException e )
        {
            throw new BootstrapException( e.getMessage(), e );
        }
        catch ( final IOException e )
        {
            throw new BootstrapException( e.getMessage(), e );
        }
    }

    /**
     * Creates a new object management and configuration model {@code BootstrapContext} instance.
     *
     * @param classLoader The class loader to create a new object management and configuration model bootstrap context
     * instance with or {@code null} to create a new bootstrap context using the platform's bootstrap class loader.
     *
     * @return A new {@code BootstrapContext} instance.
     *
     * @throws BootstrapException if creating a new object management and configuration model bootstrap context instance
     * fails.
     *
     * @see #getBootstrapContextClassName()
     */
    public static BootstrapContext createBootstrapContext( final ClassLoader classLoader ) throws BootstrapException
    {
        if ( DefaultBootstrapContext.class.getName().equals( getBootstrapContextClassName() ) )
        {
            return new DefaultBootstrapContext( classLoader );
        }

        try
        {
            final Class clazz = Class.forName( getBootstrapContextClassName(), true, classLoader );
            final Constructor ctor = clazz.getConstructor( ClassLoader.class );
            return (BootstrapContext) ctor.newInstance( classLoader );
        }
        catch ( final ClassNotFoundException e )
        {
            throw new BootstrapException( e.getMessage(), e );
        }
        catch ( final NoSuchMethodException e )
        {
            throw new BootstrapException( e.getMessage(), e );
        }
        catch ( final InstantiationException e )
        {
            throw new BootstrapException( e.getMessage(), e );
        }
        catch ( final IllegalAccessException e )
        {
            throw new BootstrapException( e.getMessage(), e );
        }
        catch ( final InvocationTargetException e )
        {
            throw new BootstrapException( e.getMessage(), e );
        }
        catch ( final ClassCastException e )
        {
            throw new BootstrapException( e.getMessage(), e );
        }
    }

    /**
     * Creates a new object management and configuration model bootstrap JAXP schema instance.
     *
     * @return A new object management and configuration model bootstrap JAXP schema instance.
     *
     * @throws BootstrapException if creating a new object management and configuration model bootstrap JAXP schema
     * instance fails.
     */
    public abstract javax.xml.validation.Schema createSchema() throws BootstrapException;

    /**
     * Creates a new object management and configuration model bootstrap JAXB context instance.
     *
     * @return A new object management and configuration model bootstrap JAXB context instance.
     *
     * @throws BootstrapException if creating a new object management and configuration model bootstrap JAXB context
     * instance fails.
     */
    public abstract JAXBContext createContext() throws BootstrapException;

    /**
     * Creates a new object management and configuration model bootstrap JAXB marshaller instance.
     *
     * @return A new object management and configuration model bootstrap JAXB marshaller instance.
     *
     * @throws BootstrapException if creating a new object management and configuration model bootstrap JAXB marshaller
     * instance fails.
     */
    public abstract Marshaller createMarshaller() throws BootstrapException;

    /**
     * Creates a new object management and configuration model bootstrap JAXB unmarshaller instance.
     *
     * @return A new object management and configuration model bootstrap JAXB unmarshaller instance.
     *
     * @throws BootstrapException if creating a new object management and configuration model bootstrap JAXB
     * unmarshaller instance fails.
     */
    public abstract Unmarshaller createUnmarshaller() throws BootstrapException;

    private <T> Collection<Class<T>> loadProviders( final Class<T> providerClass ) throws BootstrapException
    {
        try
        {
            final String providerNamePrefix = providerClass.getName() + ".";
            final Map<String, Class<T>> providers = new TreeMap<String, Class<T>>( new Comparator<String>()
            {

                public int compare( final String key1, final String key2 )
                {
                    return key1.compareTo( key2 );
                }

            } );

            final File platformProviders = new File( new StringBuilder().append( System.getProperty( "java.home" ) ).
                append( File.separator ).append( "jre" ).append( File.separator ).append( "lib" ).
                append( File.separator ).append( "jomc.properties" ).toString() );

            if ( platformProviders.exists() )
            {
                InputStream in = null;
                final java.util.Properties p = new java.util.Properties();

                try
                {
                    in = new FileInputStream( platformProviders );
                    p.load( in );
                }
                finally
                {
                    if ( in != null )
                    {
                        in.close();
                    }
                }

                for ( Map.Entry e : p.entrySet() )
                {
                    if ( e.getKey().toString().startsWith( providerNamePrefix ) )
                    {
                        final Class<T> provider = this.findClass( e.getValue().toString() );
                        if ( provider != null )
                        {
                            providers.put( e.getKey().toString(), provider );
                        }
                    }
                }
            }

            final Enumeration<URL> serviceProviders =
                this.findResources( "META-INF/services/" + providerClass.getName() );

            while ( serviceProviders.hasMoreElements() )
            {
                final URL url = serviceProviders.nextElement();
                final BufferedReader reader = new BufferedReader( new InputStreamReader( url.openStream(), "UTF-8" ) );

                String line = null;
                while ( ( line = reader.readLine() ) != null )
                {
                    if ( line.contains( "#" ) )
                    {
                        continue;
                    }

                    final Class<T> provider = this.findClass( line );
                    if ( provider != null )
                    {
                        providers.put( providerNamePrefix + providers.size(), provider );
                    }
                }

                reader.close();
            }

            return providers.values();
        }
        catch ( final IOException e )
        {
            throw new BootstrapException( e.getMessage(), e );
        }
    }

}
