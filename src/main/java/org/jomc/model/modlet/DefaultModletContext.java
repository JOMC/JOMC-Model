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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.TreeMap;
import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.util.JAXBSource;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import org.xml.sax.SAXException;

/**
 * Default {@code ModletContext} implementation.
 *
 * @author <a href="mailto:schulte2005@users.sourceforge.net">Christian Schulte</a>
 * @version $Id$
 * @see ModletContext#createModletContext(java.lang.ClassLoader)
 */
public class DefaultModletContext extends ModletContext
{

    /**
     * Classpath location searched for providers by default.
     * @see #getDefaultProviderLocation()
     */
    private static final String DEFAULT_PROVIDER_LOCATION = "META-INF/services";

    /**
     * Location searched for platform providers by default.
     * @see #getDefaultPlatformProviderLocation()
     */
    private static final String DEFAULT_PLATFORM_PROVIDER_LOCATION =
        new StringBuilder().append( System.getProperty( "java.home" ) ).
        append( File.separator ).append( "jre" ).append( File.separator ).append( "lib" ).
        append( File.separator ).append( "jomc.properties" ).toString();

    /**
     * Default modlet schema system id.
     * @see #getDefaultModletSchemaSystemId()
     */
    private static final String DEFAULT_MODLET_SCHEMA_SYSTEM_ID =
        "http://jomc.sourceforge.net/model/modlet/jomc-modlet-1.0.xsd";

    /** Default provider location. */
    private static volatile String defaultProviderLocation;

    /** Default platform provider location. */
    private static volatile String defaultPlatformProviderLocation;

    /** Default modlet schema system id. */
    private static volatile String defaultModletSchemaSystemId;

    /** Provider location of the instance. */
    private String providerLocation;

    /** Platform provider location of the instance. */
    private String platformProviderLocation;

    /** Modlet schema system id of the instance. */
    private String modletSchemaSystemId;

    /**
     * Creates a new {@code DefaultModletContext} instance taking a class loader.
     *
     * @param classLoader The class loader of the context.
     */
    public DefaultModletContext( final ClassLoader classLoader )
    {
        super( classLoader );
    }

    /**
     * Gets the default location searched for provider resources.
     * <p>The default provider location is controlled by system property
     * {@code org.jomc.model.modlet.DefaultModletContext.defaultProviderLocation} holding the location to search
     * for provider resources by default. If that property is not set, the {@code META-INF/services} default is
     * returned.</p>
     *
     * @return The location searched for provider resources by default.
     *
     * @see #setDefaultProviderLocation(java.lang.String)
     */
    public static String getDefaultProviderLocation()
    {
        if ( defaultProviderLocation == null )
        {
            defaultProviderLocation = System.getProperty(
                "org.jomc.model.modlet.DefaultModletContext.defaultProviderLocation", DEFAULT_PROVIDER_LOCATION );

        }

        return defaultProviderLocation;
    }

    /**
     * Sets the default location searched for provider resources.
     *
     * @param value The new default location to search for provider resources or {@code null}.
     *
     * @see #getDefaultProviderLocation()
     */
    public static void setDefaultProviderLocation( final String value )
    {
        defaultProviderLocation = value;
    }

    /**
     * Gets the location searched for provider resources.
     *
     * @return The location searched for provider resources.
     *
     * @see #getDefaultProviderLocation()
     * @see #setProviderLocation(java.lang.String)
     */
    public String getProviderLocation()
    {
        if ( this.providerLocation == null )
        {
            this.providerLocation = getDefaultProviderLocation();
        }

        return this.providerLocation;
    }

    /**
     * Sets the location searched for provider resources.
     *
     * @param value The new location to search for provider resources or {@code null}.
     *
     * @see #getProviderLocation()
     */
    public void setProviderLocation( final String value )
    {
        this.providerLocation = value;
    }

    /**
     * Gets the default location searched for platform provider resources.
     * <p>The default platform provider location is controlled by system property
     * {@code org.jomc.model.modlet.DefaultModletContext.defaultPlatformProviderLocation} holding the location to
     * search for platform provider resources by default. If that property is not set, the
     * {@code <java-home>/jre/lib/jomc.properties} default is returned.</p>
     *
     * @return The location searched for platform provider resources by default.
     *
     * @see #setDefaultPlatformProviderLocation(java.lang.String)
     */
    public static String getDefaultPlatformProviderLocation()
    {
        if ( defaultPlatformProviderLocation == null )
        {
            defaultPlatformProviderLocation = System.getProperty(
                "org.jomc.model.modlet.DefaultModletContext.defaultPlatformProviderLocation",
                DEFAULT_PLATFORM_PROVIDER_LOCATION );

        }

        return defaultPlatformProviderLocation;
    }

    /**
     * Sets the default location searched for platform provider resources.
     *
     * @param value The new default location to search for platform provider resources or {@code null}.
     *
     * @see #getDefaultPlatformProviderLocation()
     */
    public static void setDefaultPlatformProviderLocation( final String value )
    {
        defaultPlatformProviderLocation = value;
    }

    /**
     * Gets the location searched for platform provider resources.
     *
     * @return The location searched for platform provider resources.
     *
     * @see #getDefaultPlatformProviderLocation()
     * @see #setPlatformProviderLocation(java.lang.String)
     */
    public String getPlatformProviderLocation()
    {
        if ( this.platformProviderLocation == null )
        {
            this.platformProviderLocation = getDefaultPlatformProviderLocation();
        }

        return this.platformProviderLocation;
    }

    /**
     * Sets the location searched for platform provider resources.
     *
     * @param value The new location to search for platform provider resources or {@code null}.
     *
     * @see #getPlatformProviderLocation()
     */
    public void setPlatformProviderLocation( final String value )
    {
        this.platformProviderLocation = value;
    }

    /**
     * Gets the default modlet schema system id.
     * <p>The default modlet schema system id is controlled by system property
     * {@code org.jomc.model.modlet.DefaultModletContext.defaultModletSchemaSystemId} holding a system id URI.
     * If that property is not set, the
     * {@code http://jomc.sourceforge.net/model/modlet/jomc-modlet-1.0.xsd} default is returned.</p>
     *
     * @return The default system id of the modlet schema.
     *
     * @see #setDefaultModletSchemaSystemId(java.lang.String)
     */
    public static String getDefaultModletSchemaSystemId()
    {
        if ( defaultModletSchemaSystemId == null )
        {
            defaultModletSchemaSystemId = System.getProperty(
                "org.jomc.model.modlet.DefaultModletContext.defaultModletSchemaSystemId",
                DEFAULT_MODLET_SCHEMA_SYSTEM_ID );

        }

        return defaultModletSchemaSystemId;
    }

    /**
     * Sets the default modlet schema system id.
     *
     * @param value The new default modlet schema system id or {@code null}.
     *
     * @see #getDefaultModletSchemaSystemId()
     */
    public static void setDefaultModletSchemaSystemId( final String value )
    {
        defaultModletSchemaSystemId = value;
    }

    /**
     * Gets the modlet schema system id.
     *
     * @return The modlet schema system id.
     *
     * @see #getDefaultModletSchemaSystemId()
     * @see #setModletSchemaSystemId(java.lang.String)
     */
    public String getModletSchemaSystemId()
    {
        if ( this.modletSchemaSystemId == null )
        {
            this.modletSchemaSystemId = getDefaultModletSchemaSystemId();
        }

        return this.modletSchemaSystemId;
    }

    /**
     * Sets the modlet schema system id.
     *
     * @param value The new modlet schema system id or {@code null}.
     *
     * @see #getModletSchemaSystemId()
     */
    public void setModletSchemaSystemId( final String value )
    {
        this.modletSchemaSystemId = value;
    }

    /**
     * Searches the context for modlets.
     * <p>This method loads {@code ModletProvider} classes setup via the platform provider configuration file and
     * {@code <provider-location>/org.jomc.model.modlet.ModletProvider} resources to return a list of modlets.</p>
     *
     * @return The modlets found in the context.
     *
     * @throws ModletException if searching modlets fails.
     *
     * @see #getProviderLocation()
     * @see #getPlatformProviderLocation()
     * @see ModletProvider#findModlets(org.jomc.model.modlet.ModletContext)
     */
    @Override
    public Modlets findModlets() throws ModletException
    {
        try
        {
            final Modlets modlets = new Modlets();
            final Collection<Class<? extends ModletProvider>> providers = this.loadProviders( ModletProvider.class );

            for ( Class<? extends ModletProvider> provider : providers )
            {
                final ModletProvider modletProvider = provider.newInstance();
                final Modlets provided = modletProvider.findModlets( this );
                if ( provided != null )
                {
                    modlets.getModlet().addAll( provided.getModlet() );
                }
            }

            final javax.xml.validation.Schema modletSchema = this.createSchema();
            final Validator validator = modletSchema.newValidator();
            validator.validate( new JAXBSource( this.createContext(), new ObjectFactory().createModlets( modlets ) ) );

            return modlets;
        }
        catch ( final InstantiationException e )
        {
            throw new ModletException( e.getMessage(), e );
        }
        catch ( final IllegalAccessException e )
        {
            throw new ModletException( e.getMessage(), e );
        }
        catch ( final JAXBException e )
        {
            if ( e.getLinkedException() != null )
            {
                throw new ModletException( e.getLinkedException().getMessage(), e.getLinkedException() );
            }
            else
            {
                throw new ModletException( e.getMessage(), e );
            }
        }
        catch ( final SAXException e )
        {
            throw new ModletException( e.getMessage(), e );
        }
        catch ( final IOException e )
        {
            throw new ModletException( e.getMessage(), e );
        }
    }

    @Override
    public javax.xml.validation.Schema createSchema() throws ModletException
    {
        try
        {
            return SchemaFactory.newInstance( XMLConstants.W3C_XML_SCHEMA_NS_URI ).
                newSchema( this.getClass().getResource( "/org/jomc/model/modlet/jomc-modlet-1.0.xsd" ) );

        }
        catch ( final SAXException e )
        {
            throw new ModletException( e.getMessage(), e );
        }
    }

    @Override
    public JAXBContext createContext() throws ModletException
    {
        try
        {
            return JAXBContext.newInstance( Modlets.class.getPackage().getName(), this.getClassLoader() );
        }
        catch ( final JAXBException e )
        {
            if ( e.getLinkedException() != null )
            {
                throw new ModletException( e.getLinkedException().getMessage(), e.getLinkedException() );
            }
            else
            {
                throw new ModletException( e.getMessage(), e );
            }
        }
    }

    @Override
    public Marshaller createMarshaller() throws ModletException
    {
        try
        {
            final Marshaller m = this.createContext().createMarshaller();
            m.setProperty( Marshaller.JAXB_SCHEMA_LOCATION,
                           "http://jomc.org/model/modlet " + this.getModletSchemaSystemId() );

            return m;
        }
        catch ( final JAXBException e )
        {
            if ( e.getLinkedException() != null )
            {
                throw new ModletException( e.getLinkedException().getMessage(), e.getLinkedException() );
            }
            else
            {
                throw new ModletException( e.getMessage(), e );
            }
        }
    }

    @Override
    public Unmarshaller createUnmarshaller() throws ModletException
    {
        try
        {
            return this.createContext().createUnmarshaller();
        }
        catch ( final JAXBException e )
        {
            if ( e.getLinkedException() != null )
            {
                throw new ModletException( e.getLinkedException().getMessage(), e.getLinkedException() );
            }
            else
            {
                throw new ModletException( e.getMessage(), e );
            }
        }
    }

    private <T> Collection<Class<? extends T>> loadProviders( final Class<T> providerClass ) throws ModletException
    {
        try
        {
            final String providerNamePrefix = providerClass.getName() + ".";
            final Map<String, Class<? extends T>> providers =
                new TreeMap<String, Class<? extends T>>( new Comparator<String>()
            {

                public int compare( final String key1, final String key2 )
                {
                    return key1.compareTo( key2 );
                }

            } );

            final File platformProviders = new File( this.getPlatformProviderLocation() );

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
                        final Class<?> provider = this.findClass( e.getValue().toString() );

                        if ( provider == null )
                        {
                            throw new ModletException( getMessage( "implementationNotFound", providerClass.getName(),
                                                                   e.getValue().toString(),
                                                                   platformProviders.getAbsolutePath() ) );

                        }

                        if ( !providerClass.isAssignableFrom( provider ) )
                        {
                            throw new ModletException( getMessage( "illegalImplementation", providerClass.getName(),
                                                                   e.getValue().toString(),
                                                                   platformProviders.getAbsolutePath() ) );

                        }

                        providers.put( e.getKey().toString(), provider.asSubclass( providerClass ) );
                    }
                }
            }

            final Enumeration<URL> classpathProviders =
                this.findResources( this.getProviderLocation() + '/' + providerClass.getName() );

            while ( classpathProviders.hasMoreElements() )
            {
                final URL url = classpathProviders.nextElement();
                final BufferedReader reader = new BufferedReader( new InputStreamReader( url.openStream(), "UTF-8" ) );

                String line = null;
                while ( ( line = reader.readLine() ) != null )
                {
                    if ( line.contains( "#" ) )
                    {
                        continue;
                    }

                    final Class<?> provider = this.findClass( line );

                    if ( provider == null )
                    {
                        throw new ModletException( getMessage(
                            "implementationNotFound", providerClass.getName(), line, url.toExternalForm() ) );

                    }

                    if ( !providerClass.isAssignableFrom( provider ) )
                    {
                        throw new ModletException( getMessage(
                            "illegalImplementation", providerClass.getName(), line, url.toExternalForm() ) );

                    }

                    providers.put( providerNamePrefix + providers.size(), provider.asSubclass( providerClass ) );
                }

                reader.close();
            }

            return providers.values();
        }
        catch ( final IOException e )
        {
            throw new ModletException( e.getMessage(), e );
        }
    }

    private static String getMessage( final String key, final Object... arguments )
    {
        return MessageFormat.format( ResourceBundle.getBundle(
            DefaultModletContext.class.getName().replace( '.', '/' ) ).getString( key ), arguments );

    }

    // SECTION-END
}
