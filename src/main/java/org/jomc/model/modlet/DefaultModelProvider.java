/*
 *   Copyright (C) Christian Schulte <cs@schulte.it>, 2005-206
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
 *   THIS SOFTWARE IS PROVIDED "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 *   INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY
 *   AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL
 *   THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY DIRECT, INDIRECT,
 *   INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 *   NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 *   DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 *   THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *   (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 *   THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 *   $JOMC$
 *
 */
package org.jomc.model.modlet;

import java.lang.reflect.UndeclaredThrowableException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.UnmarshalException;
import javax.xml.bind.Unmarshaller;
import javax.xml.validation.Schema;
import org.jomc.model.Module;
import org.jomc.model.Modules;
import org.jomc.model.Text;
import org.jomc.model.Texts;
import org.jomc.modlet.Model;
import org.jomc.modlet.ModelContext;
import org.jomc.modlet.ModelException;
import org.jomc.modlet.ModelProvider;

/**
 * Default object management and configuration {@code ModelProvider} implementation.
 *
 * @author <a href="mailto:cs@schulte.it">Christian Schulte</a>
 * @version $JOMC$
 * @see ModelContext#findModel(java.lang.String)
 */
public class DefaultModelProvider implements ModelProvider
{

    /**
     * Constant for the name of the model context attribute backing property {@code enabled}.
     *
     * @see #findModel(org.jomc.modlet.ModelContext, org.jomc.modlet.Model)
     * @see ModelContext#getAttribute(java.lang.String)
     * @since 1.2
     */
    public static final String ENABLED_ATTRIBUTE_NAME = "org.jomc.model.modlet.DefaultModelProvider.enabledAttribute";

    /**
     * Constant for the name of the system property controlling property {@code defaultEnabled}.
     *
     * @see #isDefaultEnabled()
     */
    private static final String DEFAULT_ENABLED_PROPERTY_NAME =
        "org.jomc.model.modlet.DefaultModelProvider.defaultEnabled";

    /**
     * Constant for the name of the deprecated system property controlling property {@code defaultEnabled}.
     * @see #isDefaultEnabled()
     */
    private static final String DEPRECATED_DEFAULT_ENABLED_PROPERTY_NAME =
        "org.jomc.model.DefaultModelProvider.defaultEnabled";

    /**
     * Default value of the flag indicating the provider is enabled by default.
     *
     * @see #isDefaultEnabled()
     * @since 1.2
     */
    private static final Boolean DEFAULT_ENABLED = Boolean.TRUE;

    /**
     * Flag indicating the provider is enabled by default.
     */
    private static volatile Boolean defaultEnabled;

    /**
     * Flag indicating the provider is enabled.
     */
    private Boolean enabled;

    /**
     * Constant for the name of the model context attribute backing property {@code moduleLocation}.
     *
     * @see #findModel(org.jomc.modlet.ModelContext, org.jomc.modlet.Model)
     * @see ModelContext#getAttribute(java.lang.String)
     * @since 1.2
     */
    public static final String MODULE_LOCATION_ATTRIBUTE_NAME =
        "org.jomc.model.modlet.DefaultModelProvider.moduleLocationAttribute";

    /**
     * Constant for the name of the system property controlling property {@code defaultModuleLocation}.
     *
     * @see #getDefaultModuleLocation()
     */
    private static final String DEFAULT_MODULE_LOCATION_PROPERTY_NAME =
        "org.jomc.model.modlet.DefaultModelProvider.defaultModuleLocation";

    /**
     * Constant for the name of the deprecated system property controlling property {@code defaultModuleLocation}.
     * @see #getDefaultModuleLocation()
     */
    private static final String DEPRECATED_DEFAULT_MODULE_LOCATION_PROPERTY_NAME =
        "org.jomc.model.DefaultModelProvider.defaultModuleLocation";

    /**
     * Class path location searched for modules by default.
     *
     * @see #getDefaultModuleLocation()
     */
    private static final String DEFAULT_MODULE_LOCATION = "META-INF/jomc.xml";

    /**
     * Default module location.
     */
    private static volatile String defaultModuleLocation;

    /**
     * Module location of the instance.
     */
    private String moduleLocation;

    /**
     * Constant for the name of the model context attribute backing property {@code validating}.
     *
     * @see #findModules(org.jomc.modlet.ModelContext, java.lang.String, java.lang.String)
     * @see ModelContext#getAttribute(java.lang.String)
     * @since 1.2
     */
    public static final String VALIDATING_ATTRIBUTE_NAME =
        "org.jomc.model.modlet.DefaultModelProvider.validatingAttribute";

    /**
     * Constant for the name of the system property controlling property {@code defaultValidating}.
     *
     * @see #isDefaultValidating()
     * @since 1.2
     */
    private static final String DEFAULT_VALIDATING_PROPERTY_NAME =
        "org.jomc.model.modlet.DefaultModelProvider.defaultValidating";

    /**
     * Default value of the flag indicating the provider is validating resources by default.
     *
     * @see #isDefaultValidating()
     * @since 1.2
     */
    private static final Boolean DEFAULT_VALIDATING = Boolean.TRUE;

    /**
     * Flag indicating the provider is validating resources by default.
     *
     * @since 1.2
     */
    private static volatile Boolean defaultValidating;

    /**
     * Flag indicating the provider is validating resources.
     *
     * @since 1.2
     */
    private Boolean validating;

    /**
     * Creates a new {@code DefaultModelProvider} instance.
     */
    public DefaultModelProvider()
    {
        super();
    }

    /**
     * Gets a flag indicating the provider is enabled by default.
     * <p>
     * The default enabled flag is controlled by system property
     * {@code org.jomc.model.modlet.DefaultModelProvider.defaultEnabled} holding a value indicating the provider is
     * enabled by default. If that property is not set, the {@code true} default is returned.
     * </p>
     *
     * @return {@code true}, if the provider is enabled by default; {@code false}, if the provider is disabled by
     * default.
     *
     * @see #setDefaultEnabled(java.lang.Boolean)
     */
    public static boolean isDefaultEnabled()
    {
        if ( defaultEnabled == null )
        {
            defaultEnabled =
                Boolean.valueOf( System.getProperty( DEFAULT_ENABLED_PROPERTY_NAME,
                                                     System.getProperty( DEPRECATED_DEFAULT_ENABLED_PROPERTY_NAME,
                                                                         Boolean.toString( DEFAULT_ENABLED ) ) ) );

        }

        return defaultEnabled;
    }

    /**
     * Sets the flag indicating the provider is enabled by default.
     *
     * @param value The new value of the flag indicating the provider is enabled by default or {@code null}.
     *
     * @see #isDefaultEnabled()
     */
    public static void setDefaultEnabled( final Boolean value )
    {
        defaultEnabled = value;
    }

    /**
     * Gets a flag indicating the provider is enabled.
     *
     * @return {@code true}, if the provider is enabled; {@code false}, if the provider is disabled.
     *
     * @see #isDefaultEnabled()
     * @see #setEnabled(java.lang.Boolean)
     */
    public final boolean isEnabled()
    {
        if ( this.enabled == null )
        {
            this.enabled = isDefaultEnabled();
        }

        return this.enabled;
    }

    /**
     * Sets the flag indicating the provider is enabled.
     *
     * @param value The new value of the flag indicating the provider is enabled or {@code null}.
     *
     * @see #isEnabled()
     */
    public final void setEnabled( final Boolean value )
    {
        this.enabled = value;
    }

    /**
     * Gets the default location searched for module resources.
     * <p>
     * The default module location is controlled by system property
     * {@code org.jomc.model.modlet.DefaultModelProvider.defaultModuleLocation} holding the location to search for
     * module resources by default. If that property is not set, the {@code META-INF/jomc.xml} default is returned.
     * </p>
     *
     * @return The location searched for module resources by default.
     *
     * @see #setDefaultModuleLocation(java.lang.String)
     */
    public static String getDefaultModuleLocation()
    {
        if ( defaultModuleLocation == null )
        {
            defaultModuleLocation =
                System.getProperty( DEFAULT_MODULE_LOCATION_PROPERTY_NAME,
                                    System.getProperty( DEPRECATED_DEFAULT_MODULE_LOCATION_PROPERTY_NAME,
                                                        DEFAULT_MODULE_LOCATION ) );

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
     * Gets the location searched for module resources.
     *
     * @return The location searched for module resources.
     *
     * @see #getDefaultModuleLocation()
     * @see #setModuleLocation(java.lang.String)
     */
    public final String getModuleLocation()
    {
        if ( this.moduleLocation == null )
        {
            this.moduleLocation = getDefaultModuleLocation();
        }

        return this.moduleLocation;
    }

    /**
     * Sets the location searched for module resources.
     *
     * @param value The new location to search for module resources or {@code null}.
     *
     * @see #getModuleLocation()
     */
    public final void setModuleLocation( final String value )
    {
        this.moduleLocation = value;
    }

    /**
     * Gets a flag indicating the provider is validating resources by default.
     * <p>
     * The default validating flag is controlled by system property
     * {@code org.jomc.model.modlet.DefaultModelProvider.defaultValidating} holding a value indicating the provider is
     * validating resources by default. If that property is not set, the {@code true} default is returned.
     * </p>
     *
     * @return {@code true}, if the provider is validating resources by default; {@code false}, if the provider is not
     * validating resources by default.
     *
     * @see #isValidating()
     * @see #setDefaultValidating(java.lang.Boolean)
     *
     * @since 1.2
     */
    public static boolean isDefaultValidating()
    {
        if ( defaultValidating == null )
        {
            defaultValidating = Boolean.valueOf( System.getProperty(
                DEFAULT_VALIDATING_PROPERTY_NAME, Boolean.toString( DEFAULT_VALIDATING ) ) );

        }

        return defaultValidating;
    }

    /**
     * Sets the flag indicating the provider is validating resources by default.
     *
     * @param value The new value of the flag indicating the provider is validating resources by default or
     * {@code null}.
     *
     * @see #isDefaultValidating()
     *
     * @since 1.2
     */
    public static void setDefaultValidating( final Boolean value )
    {
        defaultValidating = value;
    }

    /**
     * Gets a flag indicating the provider is validating resources.
     *
     * @return {@code true}, if the provider is validating resources; {@code false}, if the provider is not validating
     * resources.
     *
     * @see #isDefaultValidating()
     * @see #setValidating(java.lang.Boolean)
     *
     * @since 1.2
     */
    public final boolean isValidating()
    {
        if ( this.validating == null )
        {
            this.validating = isDefaultValidating();
        }

        return this.validating;
    }

    /**
     * Sets the flag indicating the provider is validating resources.
     *
     * @param value The new value of the flag indicating the provider is validating resources or {@code null}.
     *
     * @see #isValidating()
     *
     * @since 1.2
     */
    public final void setValidating( final Boolean value )
    {
        this.validating = value;
    }

    /**
     * Searches a given context for modules.
     *
     * @param context The context to search for modules.
     * @param model The identifier of the model to search for modules.
     * @param location The location to search at.
     *
     * @return The modules found at {@code location} in {@code context} or {@code null}, if no modules are found.
     *
     * @throws NullPointerException if {@code context}, {@code model} or {@code location} is {@code null}.
     * @throws ModelException if searching the context fails.
     *
     * @see #isValidating()
     * @see #VALIDATING_ATTRIBUTE_NAME
     */
    public Modules findModules( final ModelContext context, final String model, final String location )
        throws ModelException
    {
        if ( context == null )
        {
            throw new NullPointerException( "context" );
        }
        if ( model == null )
        {
            throw new NullPointerException( "model" );
        }
        if ( location == null )
        {
            throw new NullPointerException( "location" );
        }

        try
        {
            boolean contextValidating = this.isValidating();
            if ( DEFAULT_VALIDATING == contextValidating
                     && context.getAttribute( VALIDATING_ATTRIBUTE_NAME ) instanceof Boolean )
            {
                contextValidating = (Boolean) context.getAttribute( VALIDATING_ATTRIBUTE_NAME );
            }

            final long t0 = System.nanoTime();
            final Text text = new Text();
            text.setLanguage( "en" );
            text.setValue( getMessage( "contextModulesInfo", location ) );

            final Modules modules = new Modules();
            modules.setDocumentation( new Texts() );
            modules.getDocumentation().setDefaultLanguage( "en" );
            modules.getDocumentation().getText().add( text );

            final ThreadLocal<Unmarshaller> threadLocalUnmarshaller = new ThreadLocal<Unmarshaller>();
            final Schema schema = contextValidating ? context.createSchema( model ) : null;

            class UnmarshallTask implements Callable<Module>
            {

                private final URL resource;

                UnmarshallTask( final URL resource )
                {
                    super();
                    this.resource = resource;
                }

                public Module call() throws ModelException
                {
                    try
                    {
                        Module module = null;

                        if ( context.isLoggable( Level.FINEST ) )
                        {
                            context.log( Level.FINEST, getMessage( "processing", this.resource.toExternalForm() ),
                                         null );

                        }

                        Unmarshaller u = threadLocalUnmarshaller.get();
                        if ( u == null )
                        {
                            u = context.createUnmarshaller( model );
                            u.setSchema( schema );
                            threadLocalUnmarshaller.set( u );
                        }

                        Object content = u.unmarshal( this.resource );
                        if ( content instanceof JAXBElement<?> )
                        {
                            content = ( (JAXBElement<?>) content ).getValue();
                        }

                        if ( content instanceof Module )
                        {
                            final Module m = (Module) content;

                            if ( context.isLoggable( Level.FINEST ) )
                            {
                                context.log( Level.FINEST, getMessage(
                                             "foundModule", m.getName(), m.getVersion() == null ? "" : m.getVersion() ),
                                             null );

                            }

                            module = m;
                        }
                        else if ( context.isLoggable( Level.WARNING ) )
                        {
                            context.log( Level.WARNING, getMessage( "ignoringDocument",
                                                                    content == null ? "<>" : content.toString(),
                                                                    this.resource.toExternalForm() ), null );

                        }

                        return module;
                    }
                    catch ( final UnmarshalException e )
                    {
                        String message = getMessage( e );
                        if ( message == null && e.getLinkedException() != null )
                        {
                            message = getMessage( e.getLinkedException() );
                        }

                        message = getMessage( "unmarshalException", this.resource.toExternalForm(),
                                              message != null ? " " + message : "" );

                        throw new ModelException( message, e );
                    }
                    catch ( final JAXBException e )
                    {
                        String message = getMessage( e );
                        if ( message == null && e.getLinkedException() != null )
                        {
                            message = getMessage( e.getLinkedException() );
                        }

                        throw new ModelException( message, e );
                    }
                }

            }

            final List<UnmarshallTask> tasks = new LinkedList<UnmarshallTask>();
            final Enumeration<URL> resources = context.findResources( location );

            while ( resources.hasMoreElements() )
            {
                tasks.add( new UnmarshallTask( resources.nextElement() ) );
            }

            int count = 0;
            if ( context.getExecutorService() != null && tasks.size() > 1 )
            {
                for ( final Future<Module> task : context.getExecutorService().invokeAll( tasks ) )
                {
                    final Module m = task.get();

                    if ( m != null )
                    {
                        modules.getModule().add( m );
                        count++;
                    }
                }
            }
            else
            {
                for ( final UnmarshallTask task : tasks )
                {
                    final Module m = task.call();
                    if ( m != null )
                    {
                        modules.getModule().add( m );
                        count++;
                    }
                }
            }

            if ( context.isLoggable( Level.FINE ) )
            {
                context.log( Level.FINE, getMessage( "contextReport", count, location, System.nanoTime() - t0 ), null );
            }

            return modules.getModule().isEmpty() ? null : modules;
        }
        catch ( final CancellationException e )
        {
            throw new ModelException( getMessage( e ), e );
        }
        catch ( final InterruptedException e )
        {
            throw new ModelException( getMessage( e ), e );
        }
        catch ( final ExecutionException e )
        {
            if ( e.getCause() instanceof ModelException )
            {
                throw (ModelException) e.getCause();
            }
            else if ( e.getCause() instanceof RuntimeException )
            {
                // The fork-join framework breaks the exception handling contract of Callable by re-throwing any
                // exception caught using a runtime exception.
                if ( e.getCause().getCause() instanceof ModelException )
                {
                    throw (ModelException) e.getCause().getCause();
                }
                else if ( e.getCause().getCause() instanceof RuntimeException )
                {
                    throw (RuntimeException) e.getCause().getCause();
                }
                else if ( e.getCause().getCause() instanceof Error )
                {
                    throw (Error) e.getCause().getCause();
                }
                else if ( e.getCause().getCause() instanceof Exception )
                {
                    // Checked exception not declared to be thrown by the Callable's 'call' method.
                    throw new UndeclaredThrowableException( e.getCause().getCause() );
                }
                else
                {
                    throw (RuntimeException) e.getCause();
                }
            }
            else if ( e.getCause() instanceof Error )
            {
                throw (Error) e.getCause();
            }
            else
            {
                // Checked exception not declared to be thrown by the Callable's 'call' method.
                throw new UndeclaredThrowableException( e.getCause() );
            }
        }
    }

    /**
     * {@inheritDoc}
     *
     * @return The {@code Model} found in the context or {@code null}, if no {@code Model} is found or the provider is
     * disabled.
     *
     * @see #isEnabled()
     * @see #getModuleLocation()
     * @see #findModules(org.jomc.modlet.ModelContext, java.lang.String, java.lang.String)
     * @see #ENABLED_ATTRIBUTE_NAME
     * @see #MODULE_LOCATION_ATTRIBUTE_NAME
     */
    public Model findModel( final ModelContext context, final Model model ) throws ModelException
    {
        if ( context == null )
        {
            throw new NullPointerException( "context" );
        }
        if ( model == null )
        {
            throw new NullPointerException( "model" );
        }

        Model found = null;

        boolean contextEnabled = this.isEnabled();
        if ( DEFAULT_ENABLED == contextEnabled && context.getAttribute( ENABLED_ATTRIBUTE_NAME ) instanceof Boolean )
        {
            contextEnabled = (Boolean) context.getAttribute( ENABLED_ATTRIBUTE_NAME );
        }

        String contextModuleLocation = this.getModuleLocation();
        if ( DEFAULT_MODULE_LOCATION.equals( contextModuleLocation )
                 && context.getAttribute( MODULE_LOCATION_ATTRIBUTE_NAME ) instanceof String )
        {
            contextModuleLocation = (String) context.getAttribute( MODULE_LOCATION_ATTRIBUTE_NAME );
        }

        if ( contextEnabled )
        {
            final Modules modules = this.findModules( context, model.getIdentifier(), contextModuleLocation );

            if ( modules != null )
            {
                found = model.clone();
                ModelHelper.addModules( found, modules );
            }
        }
        else if ( context.isLoggable( Level.FINER ) )
        {
            context.log( Level.FINER, getMessage( "disabled", this.getClass().getSimpleName(),
                                                  model.getIdentifier() ), null );

        }

        return found;
    }

    private static String getMessage( final String key, final Object... args )
    {
        return MessageFormat.format( ResourceBundle.getBundle(
            DefaultModelProvider.class.getName().replace( '.', '/' ), Locale.getDefault() ).getString( key ), args );

    }

    private static String getMessage( final Throwable t )
    {
        return t != null
                   ? t.getMessage() != null && t.getMessage().trim().length() > 0
                         ? t.getMessage()
                         : getMessage( t.getCause() )
                   : null;

    }

}
