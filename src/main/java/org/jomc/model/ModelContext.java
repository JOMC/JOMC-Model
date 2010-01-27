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
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.logging.Level;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.Source;
import javax.xml.validation.Schema;
import org.w3c.dom.ls.LSResourceResolver;
import org.xml.sax.EntityResolver;

/**
 * Object management and configuration model context interface.
 *
 * @author <a href="mailto:cs@jomc.org">Christian Schulte</a>
 * @version $Id$
 * @see #createModelContext(java.lang.ClassLoader)
 */
public abstract class ModelContext
{

    /** Listener interface. */
    public abstract static class Listener
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
        public abstract void onLog( Level level, String message, Throwable t );

    }

    /**
     * Log level events are logged at by default.
     * @see #getDefaultLogLevel()
     */
    private static final Level DEFAULT_LOG_LEVEL = Level.WARNING;

    /** Default log level. */
    private static volatile Level defaultLogLevel;

    /** Class name of the {@code ModelContext} implementation. */
    private static volatile String modelContextClassName;

    /** The listeners of the instance. */
    private List<Listener> listeners;

    /** Log level of the instance. */
    private Level logLevel;

    /** The class loader of the context. */
    private ClassLoader classLoader;

    /**
     * Creates a new {@code ModelContext} instance taking a class loader.
     *
     * @param classLoader The class loader of the context.
     */
    public ModelContext( final ClassLoader classLoader )
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
                    return ModelContext.class.getName() + ".BootstrapClassLoader@" + System.identityHashCode( this );
                }

            };

        }

        return this.classLoader;
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
     * {@code org.jomc.model.ModelContext.defaultLogLevel} holding the log level to log events at by default.
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
            defaultLogLevel = Level.parse( System.getProperty(
                "org.jomc.model.ModelContext.defaultLogLevel", DEFAULT_LOG_LEVEL.getName() ) );

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
            this.log( Level.CONFIG, getMessage( "defaultLogLevelInfo", this.getClass().getCanonicalName(),
                                                this.logLevel.getLocalizedName() ), null );

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
    public void log( final Level level, final String message, final Throwable throwable )
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
     * Gets the name of the class providing the {@code ModelContext} implementation.
     * <p>The name of the class providing the {@code ModelContext} implementation returned by method
     * {@link #createModelContext(java.lang.ClassLoader)} is controlled by system property
     * {@code org.jomc.model.ModelContext.className}. If that property is not set, the name of the
     * {@link DefaultModelContext} class is returned.</p>
     *
     * @return The name of the class providing the {@code ModelContext} implementation.
     *
     * @see #setModelContextClassName(java.lang.String)
     */
    public static String getModelContextClassName()
    {
        if ( modelContextClassName == null )
        {
            modelContextClassName = System.getProperty( "org.jomc.model.ModelContext.className",
                                                        DefaultModelContext.class.getName() );

        }

        return modelContextClassName;
    }

    /**
     * Sets the name of the class providing the ModelContext implementation.
     *
     * @param value The new name of the class providing the ModelContext implementation or {@code null}.
     *
     * @see #getModelContextClassName()
     */
    public static void setModelContextClassName( final String value )
    {
        modelContextClassName = value;
    }

    /**
     * Searches the context for a class with a given name.
     *
     * @param name The name of the class to return.
     *
     * @return A Class object of the class with name {@code name} or {@code null} if no such class is found.
     *
     * @throws NullPointerException if {@code name} is {@code null}.
     * @throws ModelException if searching fails.
     */
    public Class findClass( final String name ) throws ModelException
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
            if ( this.isLoggable( Level.FINE ) )
            {
                this.log( Level.FINE, getMessage( "classNotFound", name ), e );
            }

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
     * @throws ModelException if searching fails.
     */
    public URL findResource( final String name ) throws ModelException
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
     * @throws ModelException if searching fails.
     */
    public Enumeration<URL> findResources( final String name ) throws ModelException
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
            throw new ModelException( e );
        }
    }

    /**
     * Searches the context for modules.
     *
     * @return The modules found in the context.
     *
     * @throws ModelException if searching modules fails.
     */
    public abstract Modules findModules() throws ModelException;

    /**
     * Processes modules.
     *
     * @param modules The modules to process.
     *
     * @return The processed modules.
     *
     * @throws NullPointerException if {@code modules} is {@code null}.
     * @throws ModelException if processing modules fails.
     */
    public abstract Modules processModules( final Modules modules ) throws ModelException;

    /**
     * Validates a given model.
     *
     * @param model A source providing the model to validate.
     *
     * @return Validation report.
     *
     * @throws NullPointerException if {@code model} is {@code null}.
     * @throws ModelException if validating the model fails.
     */
    public abstract ModelValidationReport validateModel( final Source model ) throws ModelException;

    /**
     * Validates a given list of modules.
     *
     * @param modules The list of modules to validate.
     *
     * @return Validation report.
     *
     * @throws NullPointerException if {@code modules} is {@code null}.
     * @throws ModelException if validating the modules fails.
     */
    public abstract ModelValidationReport validateModel( final Modules modules ) throws ModelException;

    /**
     * Creates a new object management and configuration model {@code ModelContext} instance.
     *
     * @param classLoader The class loader to create a new object management and configuration model context instance
     * with or {@code null} to create a new context using the platform's bootstrap class loader.
     *
     * @return A new {@code ModelContext} instance.
     *
     * @throws ModelException if creating a new object management and configuration model context instance fails.
     *
     * @see #getModelContextClassName()
     */
    public static ModelContext createModelContext( final ClassLoader classLoader ) throws ModelException
    {
        if ( DefaultModelContext.class.getName().equals( getModelContextClassName() ) )
        {
            return new DefaultModelContext( classLoader );
        }

        try
        {
            final Class clazz = Class.forName( getModelContextClassName(), true, classLoader );
            final Constructor ctor = clazz.getConstructor( ClassLoader.class );
            return (ModelContext) ctor.newInstance( classLoader );
        }
        catch ( final ClassNotFoundException e )
        {
            throw new ModelException( e );
        }
        catch ( final NoSuchMethodException e )
        {
            throw new ModelException( e );
        }
        catch ( final InstantiationException e )
        {
            throw new ModelException( e );
        }
        catch ( final IllegalAccessException e )
        {
            throw new ModelException( e );
        }
        catch ( final InvocationTargetException e )
        {
            throw new ModelException( e );
        }
        catch ( final ClassCastException e )
        {
            throw new ModelException( e );
        }
    }

    /**
     * Creates a new object management and configuration model SAX entity resolver instance.
     *
     * @return A new object management and configuration model SAX entity resolver instance.
     *
     * @throws ModelException if creating a new object management and configuration model SAX entity resolver instance
     * fails.
     */
    public abstract EntityResolver createEntityResolver() throws ModelException;

    /**
     * Creates a new object management and configuration model L/S resource resolver instance.
     *
     * @return A new object management and configuration model L/S resource resolver instance.
     *
     * @throws ModelException if creating a new object management and configuration model L/S resource resolver instance
     * fails.
     */
    public abstract LSResourceResolver createResourceResolver() throws ModelException;

    /**
     * Creates a new object management and configuration model JAXP schema instance.
     *
     * @return A new object management and configuration model JAXP schema instance.
     *
     * @throws ModelException if creating a new object management and configuration model JAXP schema instance fails.
     */
    public abstract Schema createSchema() throws ModelException;

    /**
     * Creates a new object management and configuration model JAXB context instance.
     *
     * @return A new object management and configuration model JAXB context instance.
     *
     * @throws ModelException if creating a new object management and configuration model JAXB context instance fails.
     */
    public abstract JAXBContext createContext() throws ModelException;

    /**
     * Creates a new object management and configuration model JAXB marshaller instance.
     *
     * @return A new object management and configuration model JAXB marshaller instance.
     *
     * @throws ModelException if creating a new object management and configuration model JAXB marshaller instance
     * fails.
     */
    public abstract Marshaller createMarshaller() throws ModelException;

    /**
     * Creates a new object management and configuration model JAXB unmarshaller instance.
     *
     * @return A new object management and configuration model JAXB unmarshaller instance.
     *
     * @throws ModelException if creating a new object management and configuration model JAXB unmarshaller instance
     * fails.
     */
    public abstract Unmarshaller createUnmarshaller() throws ModelException;

    private static String getMessage( final String key, final Object... args )
    {
        return MessageFormat.format( ResourceBundle.getBundle( ModelContext.class.getName().replace( '.', '/' ),
                                                               Locale.getDefault() ).getString( key ), args );

    }

}
