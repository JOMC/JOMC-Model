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
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

/**
 * Object management and configuration model modlet context interface.
 *
 * @author <a href="mailto:schulte2005@users.sourceforge.net">Christian Schulte</a>
 * @version $Id$
 * @see #createModletContext(java.lang.ClassLoader)
 */
public abstract class ModletContext
{

    /** Class name of the {@code ModletContext} implementation. */
    private static volatile String modletContextClassName;

    /** The attributes of the instance. */
    private Map<String, Object> attributes;

    /** The class loader of the context. */
    private ClassLoader classLoader;

    /**
     * Creates a new {@code ModletContext} instance taking a class loader.
     *
     * @param classLoader The class loader of the context.
     */
    public ModletContext( final ClassLoader classLoader )
    {
        super();
        this.classLoader = classLoader;
    }

    /**
     * Gets the attributes of the context.
     * <p>This accessor method returns a reference to the live map, not a snapshot. Therefore any modification you make
     * to the returned map will be present inside the object.</p>
     *
     * @return The map of attributes of the context.
     */
    public Map<String, Object> getAttributes()
    {
        if ( this.attributes == null )
        {
            this.attributes = new HashMap<String, Object>();
        }

        return this.attributes;
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
                    return ModletContext.class.getName() + ".modletClassLoader@" + System.identityHashCode( this );
                }

            };

        }

        return this.classLoader;
    }

    /**
     * Gets the name of the class providing the {@code ModletContext} implementation.
     * <p>The name of the class providing the {@code ModletContext} implementation returned by method
     * {@link #createModletContext(java.lang.ClassLoader)} is controlled by system property
     * {@code org.jomc.model.modlet.ModletContext.className}. If that property is not set, the name of the
     * {@link DefaultModletContext} class is returned.</p>
     *
     * @return The name of the class providing the {@code ModletContext} implementation.
     *
     * @see #setModletContextClassName(java.lang.String)
     */
    public static String getModletContextClassName()
    {
        if ( modletContextClassName == null )
        {
            modletContextClassName = System.getProperty( "org.jomc.model.modlet.ModletContext.className",
                                                         DefaultModletContext.class.getName() );

        }

        return modletContextClassName;
    }

    /**
     * Sets the name of the class providing the {@code ModletContext} implementation.
     *
     * @param value The new name of the class providing the {@code ModletContext} implementation or {@code null}.
     *
     * @see #getModletContextClassName()
     */
    public static void setModletContextClassName( final String value )
    {
        modletContextClassName = value;
    }

    /**
     * Searches the context for a class with a given name.
     *
     * @param name The name of the class to return.
     *
     * @return A class object of the class with name {@code name} or {@code null} if no such class is found.
     *
     * @throws NullPointerException if {@code name} is {@code null}.
     * @throws ModletException if searching fails.
     */
    public Class<?> findClass( final String name ) throws ModletException
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
     * @throws ModletException if searching fails.
     */
    public URL findResource( final String name ) throws ModletException
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
     * @throws ModletException if searching fails.
     */
    public Enumeration<URL> findResources( final String name ) throws ModletException
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
            throw new ModletException( e.getMessage(), e );
        }
    }

    /**
     * Searches the context for modlets.
     *
     * @return The modlets found in the context.
     *
     * @throws ModletException if searching modlets fails.
     */
    public abstract Modlets findModlets() throws ModletException;

    /**
     * Creates a new object management and configuration model {@code ModletContext} instance.
     *
     * @param classLoader The class loader to create a new object management and configuration model modlet context
     * instance with or {@code null} to create a new context using the platform's bootstrap class loader.
     *
     * @return A new {@code ModletContext} instance.
     *
     * @throws ModletException if creating a new object management and configuration model modlet context instance
     * fails.
     *
     * @see #getModletContextClassName()
     */
    public static ModletContext createModletContext( final ClassLoader classLoader ) throws ModletException
    {
        if ( DefaultModletContext.class.getName().equals( getModletContextClassName() ) )
        {
            return new DefaultModletContext( classLoader );
        }

        try
        {
            final Class<?> clazz = Class.forName( getModletContextClassName(), true, classLoader );
            final Constructor<? extends ModletContext> ctor =
                clazz.asSubclass( ModletContext.class ).getConstructor( ClassLoader.class );

            return ctor.newInstance( classLoader );
        }
        catch ( final ClassNotFoundException e )
        {
            throw new ModletException( e.getMessage(), e );
        }
        catch ( final NoSuchMethodException e )
        {
            throw new ModletException( e.getMessage(), e );
        }
        catch ( final InstantiationException e )
        {
            throw new ModletException( e.getMessage(), e );
        }
        catch ( final IllegalAccessException e )
        {
            throw new ModletException( e.getMessage(), e );
        }
        catch ( final InvocationTargetException e )
        {
            throw new ModletException( e.getMessage(), e );
        }
        catch ( final ClassCastException e )
        {
            throw new ModletException( e.getMessage(), e );
        }
    }

    /**
     * Creates a new object management and configuration model modlet JAXP schema instance.
     *
     * @return A new object management and configuration model modlet JAXP schema instance.
     *
     * @throws ModletException if creating a new object management and configuration model modlet JAXP schema instance
     * fails.
     */
    public abstract javax.xml.validation.Schema createSchema() throws ModletException;

    /**
     * Creates a new object management and configuration model modlet JAXB context instance.
     *
     * @return A new object management and configuration model modlet JAXB context instance.
     *
     * @throws ModletException if creating a new object management and configuration model modlet JAXB context instance
     * fails.
     */
    public abstract JAXBContext createContext() throws ModletException;

    /**
     * Creates a new object management and configuration model modlet JAXB marshaller instance.
     *
     * @return A new object management and configuration model modlet JAXB marshaller instance.
     *
     * @throws ModletException if creating a new object management and configuration model modlet JAXB marshaller
     * instance fails.
     */
    public abstract Marshaller createMarshaller() throws ModletException;

    /**
     * Creates a new object management and configuration model modlet JAXB unmarshaller instance.
     *
     * @return A new object management and configuration model modlet JAXB unmarshaller instance.
     *
     * @throws ModletException if creating a new object management and configuration model modlet JAXB unmarshaller
     * instance fails.
     */
    public abstract Unmarshaller createUnmarshaller() throws ModletException;

}
