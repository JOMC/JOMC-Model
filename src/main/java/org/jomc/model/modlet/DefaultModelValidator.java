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

import java.text.MessageFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.stream.Stream;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.util.JAXBSource;
import javax.xml.namespace.QName;
import javax.xml.transform.Source;
import org.jomc.model.Argument;
import org.jomc.model.Dependency;
import org.jomc.model.Implementation;
import org.jomc.model.ImplementationReference;
import org.jomc.model.Implementations;
import org.jomc.model.Inheritable;
import org.jomc.model.InheritanceModel;
import org.jomc.model.Message;
import org.jomc.model.MessageReference;
import org.jomc.model.ModelObjectException;
import org.jomc.model.Module;
import org.jomc.model.Modules;
import org.jomc.model.ObjectFactory;
import org.jomc.model.Property;
import org.jomc.model.PropertyReference;
import org.jomc.model.Specification;
import org.jomc.model.SpecificationReference;
import org.jomc.model.Specifications;
import org.jomc.model.Text;
import org.jomc.modlet.Model;
import org.jomc.modlet.ModelContext;
import org.jomc.modlet.ModelException;
import org.jomc.modlet.ModelValidationReport;
import org.jomc.modlet.ModelValidator;
import org.jomc.jls.JavaIdentifier;
import org.jomc.model.ModelObject;
import org.jomc.model.Multiplicity;
import org.jomc.util.ParseException;
import org.jomc.util.TokenMgrError;
import org.jomc.util.VersionParser;
import org.w3c.dom.Element;

/**
 * Default object management and configuration {@code ModelValidator} implementation.
 *
 * @author <a href="mailto:cs@schulte.it">Christian Schulte</a>
 * @version $JOMC$
 * @see ModelContext#validateModel(org.jomc.modlet.Model)
 */
public class DefaultModelValidator implements ModelValidator
{

    /**
     * Constant for the name of the model context attribute backing property {@code enabled}.
     *
     * @see #validateModel(org.jomc.modlet.ModelContext, org.jomc.modlet.Model)
     * @see ModelContext#getAttribute(java.lang.String)
     * @since 1.7
     */
    public static final String ENABLED_ATTRIBUTE_NAME = "org.jomc.model.modlet.DefaultModelValidator.enabledAttribute";

    /**
     * Constant for the name of the system property controlling property {@code defaultEnabled}.
     *
     * @see #isDefaultEnabled()
     * @since 1.7
     */
    private static final String DEFAULT_ENABLED_PROPERTY_NAME =
        "org.jomc.model.modlet.DefaultModelValidator.defaultEnabled";

    /**
     * Default value of the flag indicating the validator is enabled by default.
     *
     * @see #isDefaultEnabled()
     * @since 1.7
     */
    private static final Boolean DEFAULT_ENABLED = Boolean.TRUE;

    /**
     * Flag indicating the validator is enabled by default.
     *
     * @since 1.7
     */
    private static volatile Boolean defaultEnabled;

    /**
     * Flag indicating the validator is enabled.
     *
     * @since 1.7
     */
    private Boolean enabled;

    /**
     * Constant for the name of the model context attribute backing property {@code validateJava}.
     *
     * @see ModelContext#getAttribute(java.lang.String)
     * @since 1.4
     */
    public static final String VALIDATE_JAVA_ATTRIBUTE_NAME =
        "org.jomc.model.modlet.DefaultModelValidator.validateJavaAttribute";

    /**
     * Constant for the name of the system property controlling property {@code defaultValidateJava}.
     *
     * @see #isDefaultValidateJava()
     * @since 1.4
     */
    private static final String DEFAULT_VALIDATE_JAVA_PROPERTY_NAME =
        "org.jomc.model.modlet.DefaultModelValidator.defaultValidateJava";

    /**
     * Default value of the flag indicating the validator is performing Java related validation by default.
     *
     * @see #isDefaultValidateJava()
     * @since 1.4
     */
    private static final Boolean DEFAULT_VALIDATE_JAVA = Boolean.TRUE;

    /**
     * Flag indicating the validator is performing Java related validation by default.
     *
     * @since 1.4
     */
    private static volatile Boolean defaultValidateJava;

    /**
     * Flag indicating the validator is performing Java related validation.
     *
     * @since 1.4
     */
    private Boolean validateJava;

    /**
     * Creates a new {@code DefaultModelValidator} instance.
     */
    public DefaultModelValidator()
    {
        super();
    }

    /**
     * Gets a flag indicating the validator is enabled by default.
     * <p>
     * The default enabled flag is controlled by system property
     * {@code org.jomc.model.modlet.DefaultModelValidator.defaultEnabled} holding a value indicating the validator is
     * enabled by default. If that property is not set, the {@code true} default is returned.
     * </p>
     *
     * @return {@code true}, if the validator is enabled by default; {@code false}, if the validator is disabled by
     * default.
     *
     * @see #setDefaultEnabled(java.lang.Boolean)
     * @since 1.7
     */
    public static boolean isDefaultEnabled()
    {
        if ( defaultEnabled == null )
        {
            defaultEnabled = Boolean.valueOf( System.getProperty( DEFAULT_ENABLED_PROPERTY_NAME,
                                                                  Boolean.toString( DEFAULT_ENABLED ) ) );

        }

        return defaultEnabled;
    }

    /**
     * Sets the flag indicating the validator is enabled by default.
     *
     * @param value The new value of the flag indicating the validator is enabled by default or {@code null}.
     *
     * @see #isDefaultEnabled()
     * @since 1.7
     */
    public static void setDefaultEnabled( final Boolean value )
    {
        defaultEnabled = value;
    }

    /**
     * Gets a flag indicating the validator is enabled.
     *
     * @return {@code true}, if the validator is enabled; {@code false}, if the validator is disabled.
     *
     * @see #isDefaultEnabled()
     * @see #setEnabled(java.lang.Boolean)
     * @since 1.7
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
     * Sets the flag indicating the validator is enabled.
     *
     * @param value The new value of the flag indicating the validator is enabled or {@code null}.
     *
     * @see #isEnabled()
     * @since 1.7
     */
    public final void setEnabled( final Boolean value )
    {
        this.enabled = value;
    }

    /**
     * Gets a flag indicating the validator is performing Java related validation by default.
     * <p>
     * The default validate Java flag is controlled by system property
     * {@code org.jomc.model.modlet.DefaultModelValidator.defaultValidateJava} holding a value indicating the validator
     * is performing Java related validation by default. If that property is not set, the {@code true} default is
     * returned.
     * </p>
     *
     * @return {@code true}, if the validator is performing Java related validation by default; {@code false}, if the
     * validator is not performing Java related validation by default.
     *
     * @see #setDefaultValidateJava(java.lang.Boolean)
     *
     * @since 1.4
     */
    public static boolean isDefaultValidateJava()
    {
        if ( defaultValidateJava == null )
        {
            defaultValidateJava = Boolean.valueOf( System.getProperty( DEFAULT_VALIDATE_JAVA_PROPERTY_NAME,
                                                                       Boolean.toString( DEFAULT_VALIDATE_JAVA ) ) );

        }

        return defaultValidateJava;
    }

    /**
     * Sets the flag indicating the validator is performing Java related validation by default.
     *
     * @param value The new value of the flag indicating the validator is performing Java related validation by default
     * or {@code null}.
     *
     * @see #isDefaultValidateJava()
     *
     * @since 1.4
     */
    public static void setDefaultValidateJava( final Boolean value )
    {
        defaultValidateJava = value;
    }

    /**
     * Gets a flag indicating the validator is performing Java related validation.
     *
     * @return {@code true}, if the validator is performing Java related validation; {@code false}, if the the validator
     * is not performing Java related validation.
     *
     * @see #isDefaultValidateJava()
     * @see #setValidateJava(java.lang.Boolean)
     *
     * @since 1.4
     */
    public final boolean isValidateJava()
    {
        if ( this.validateJava == null )
        {
            this.validateJava = isDefaultValidateJava();
        }

        return this.validateJava;
    }

    /**
     * Sets the flag indicating the validator is performing Java related validation.
     *
     * @param value The new value of the flag indicating the validator is performing Java related validation or
     * {@code null}.
     *
     * @see #isValidateJava()
     *
     * @since 1.4
     */
    public final void setValidateJava( final Boolean value )
    {
        this.validateJava = value;
    }

    @Override
    public Optional<ModelValidationReport> validateModel( final ModelContext context, final Model model )
        throws ModelException
    {
        Objects.requireNonNull( context, "context" );
        Objects.requireNonNull( model, "model" );

        boolean contextEnabled = this.isEnabled();
        if ( DEFAULT_ENABLED == contextEnabled )
        {
            final Optional<Object> enabledAttribute = context.getAttribute( ENABLED_ATTRIBUTE_NAME );

            if ( enabledAttribute.isPresent() && enabledAttribute.get() instanceof Boolean )
            {
                contextEnabled = (Boolean) enabledAttribute.get();
            }
        }

        boolean contextValidateJava = this.isValidateJava();
        if ( DEFAULT_VALIDATE_JAVA == contextValidateJava )
        {
            final Optional<Object> validateJavaAttribute = context.getAttribute( VALIDATE_JAVA_ATTRIBUTE_NAME );

            if ( validateJavaAttribute.isPresent() && validateJavaAttribute.get() instanceof Boolean )
            {
                contextValidateJava = (Boolean) validateJavaAttribute.get();
            }
        }

        try
        {
            ModelValidationReport report = new ModelValidationReport();

            if ( contextEnabled )
            {
                final Source source = new JAXBSource( context.createContext( model.getIdentifier() ),
                                                      new org.jomc.modlet.ObjectFactory().createModel( model ) );

                report = context.validateModel( model.getIdentifier(), source );

                final Optional<Modules> modules = ModelHelper.getModules( model );

                if ( modules.isPresent() )
                {
                    final ValidationContext validationContext =
                        new ValidationContext( context, modules.get(), report, contextValidateJava );

                    assertModulesValid( validationContext );
                    assertSpecificationsValid( validationContext );
                    assertImplementationsValid( validationContext );
                }
            }
            else if ( context.isLoggable( Level.FINER ) )
            {
                context.log( Level.FINER, getMessage( "disabled", this.getClass().getSimpleName(),
                                                      model.getIdentifier() ), null );

            }

            return Optional.of( report );
        }
        catch ( final JAXBException e )
        {
            String message = getMessage( e );
            if ( message == null && e.getLinkedException() != null )
            {
                message = getMessage( e.getLinkedException() );
            }

            if ( context.isLoggable( Level.FINE ) )
            {
                context.log( Level.FINE, message, e );
            }

            throw new ModelException( message, e );
        }
    }

    private static void assertModulesValid( final ValidationContext validationContext ) throws ModelException
    {
        try ( final Stream<Module> st0 = validationContext.getModules().getModule().parallelStream().unordered() )
        {
            st0.forEach( m  -> assertModuleValid( m, validationContext ) );
        }
    }

    private static void assertModuleValid( final Module m, final ValidationContext validationContext )
    {
        if ( m.getImplementations() != null )
        {
            try ( final Stream<ImplementationReference> st0 =
                m.getImplementations().getReference().parallelStream().unordered() )
            {
                st0.forEach( r  ->
                {
                    addDetail( validationContext.getReport(), "MODULE_IMPLEMENTATION_REFERENCE_DECLARATION_CONSTRAINT",
                               Level.SEVERE, new ObjectFactory().createModule( m ),
                               "moduleImplementationReferenceDeclarationConstraint", m.getName(), r.getIdentifier() );

                } );
            }
        }

        if ( m.getMessages() != null )
        {
            try ( final Stream<Message> st0 = m.getMessages().getMessage().parallelStream().unordered() )
            {
                st0.forEach( msg  ->
                {
                    if ( msg.isFinal() )
                    {
                        addDetail( validationContext.getReport(), "MODULE_FINAL_MESSAGE_DECLARATION_CONSTRAINT",
                                   Level.SEVERE, new ObjectFactory().createModule( m ), "moduleFinalMessageConstraint",
                                   m.getName(), msg.getName() );

                    }

                    if ( msg.isOverride() )
                    {
                        addDetail( validationContext.getReport(), "MODULE_OVERRIDE_MESSAGE_DECLARATION_CONSTRAINT",
                                   Level.SEVERE, new ObjectFactory().createModule( m ),
                                   "moduleOverrideMessageConstraint", m.getName(), msg.getName() );

                    }

                    if ( validationContext.isValidateJava() )
                    {
                        try
                        {
                            msg.getJavaConstantName();
                        }
                        catch ( final ModelObjectException e )
                        {
                            final String message = getMessage( e );

                            if ( validationContext.getModelContext().isLoggable( Level.FINE ) )
                            {
                                validationContext.getModelContext().log( Level.FINE, message, e );
                            }

                            addDetail( validationContext.getReport(),
                                       "MODULE_MESSAGE_JAVA_CONSTANT_NAME_CONSTRAINT",
                                       Level.SEVERE, new ObjectFactory().createModule( m ),
                                       "moduleMessageJavaConstantNameConstraint", m.getName(), msg.getName(),
                                       message != null && message.length() > 0 ? " " + message : "" );

                        }

                        try
                        {
                            msg.getJavaGetterMethodName();
                        }
                        catch ( final ModelObjectException e )
                        {
                            final String message = getMessage( e );

                            if ( validationContext.getModelContext().isLoggable( Level.FINE ) )
                            {
                                validationContext.getModelContext().log( Level.FINE, message, e );
                            }

                            addDetail( validationContext.getReport(),
                                       "MODULE_MESSAGE_JAVA_GETTER_METHOD_NAME_CONSTRAINT",
                                       Level.SEVERE, new ObjectFactory().createModule( m ),
                                       "moduleMessageJavaGetterMethodNameConstraint", m.getName(), msg.getName(),
                                       message != null && message.length() > 0 ? " " + message : "" );

                        }

                        try
                        {
                            msg.getJavaSetterMethodName();
                        }
                        catch ( final ModelObjectException e )
                        {
                            final String message = getMessage( e );

                            if ( validationContext.getModelContext().isLoggable( Level.FINE ) )
                            {
                                validationContext.getModelContext().log( Level.FINE, message, e );
                            }

                            addDetail( validationContext.getReport(),
                                       "MODULE_MESSAGE_JAVA_SETTER_METHOD_NAME_CONSTRAINT",
                                       Level.SEVERE, new ObjectFactory().createModule( m ),
                                       "moduleMessageJavaSetterMethodNameConstraint", m.getName(), msg.getName(),
                                       message != null && message.length() > 0 ? " " + message : "" );

                        }

                        try
                        {
                            msg.getJavaVariableName();
                        }
                        catch ( final ModelObjectException e )
                        {
                            final String message = getMessage( e );

                            if ( validationContext.getModelContext().isLoggable( Level.FINE ) )
                            {
                                validationContext.getModelContext().log( Level.FINE, message, e );
                            }

                            addDetail( validationContext.getReport(),
                                       "MODULE_MESSAGE_JAVA_VARIABLE_NAME_CONSTRAINT",
                                       Level.SEVERE, new ObjectFactory().createModule( m ),
                                       "moduleMessageJavaVariableNameConstraint", m.getName(), msg.getName(),
                                       message != null && message.length() > 0 ? " " + message : "" );

                        }
                    }

                    if ( msg.getTemplate() != null )
                    {
                        try ( final Stream<Text> st1 = msg.getTemplate().getText().parallelStream().unordered() )
                        {
                            st1.forEach( t  ->
                            {
                                try
                                {
                                    t.getMimeType();
                                }
                                catch ( final ModelObjectException e )
                                {
                                    final String message = getMessage( e );

                                    if ( validationContext.getModelContext().isLoggable( Level.FINE ) )
                                    {
                                        validationContext.getModelContext().log( Level.FINE, message, e );
                                    }

                                    addDetail( validationContext.getReport(),
                                               "MODULE_MESSAGE_TEMPLATE_MIME_TYPE_CONSTRAINT",
                                               Level.SEVERE, new ObjectFactory().createModule( m ),
                                               "moduleMessageTemplateMimeTypeConstraint", m.getName(), msg.getName(),
                                               t.getLanguage(),
                                               message != null && message.length() > 0 ? " " + message : "" );

                                }

                                if ( validationContext.isValidateJava() )
                                {
                                    try
                                    {
                                        new MessageFormat( t.getValue(), new Locale( t.getLanguage() ) );
                                    }
                                    catch ( final IllegalArgumentException e )
                                    {
                                        final String message = getMessage( e );

                                        if ( validationContext.getModelContext().isLoggable( Level.FINE ) )
                                        {
                                            validationContext.getModelContext().log( Level.FINE, message, e );
                                        }

                                        addDetail( validationContext.getReport(), "MODULE_MESSAGE_TEMPLATE_CONSTRAINT",
                                                   Level.SEVERE, new ObjectFactory().createModule( m ),
                                                   "moduleMessageTemplateConstraint", m.getName(), msg.getName(),
                                                   t.getValue(),
                                                   message != null && message.length() > 0 ? " " + message : "" );

                                    }
                                }
                            } );
                        }
                    }

                    if ( msg.getArguments() != null )
                    {
                        final Map<JavaIdentifier, Argument> javaVariableNames =
                            new ConcurrentHashMap<>( msg.getArguments().getArgument().size() );

                        try ( final Stream<Argument> st1 =
                            msg.getArguments().getArgument().parallelStream().unordered() )
                        {
                            st1.forEach( a  ->
                            {
                                if ( validationContext.isValidateJava() )
                                {
                                    try
                                    {
                                        a.getJavaTypeName();
                                    }
                                    catch ( final ModelObjectException e )
                                    {
                                        final String message = getMessage( e );

                                        if ( validationContext.getModelContext().isLoggable( Level.FINE ) )
                                        {
                                            validationContext.getModelContext().log( Level.FINE, message, e );
                                        }

                                        addDetail( validationContext.getReport(),
                                                   "MODULE_MESSAGE_ARGUMENT_JAVA_TYPE_NAME_CONSTRAINT",
                                                   Level.SEVERE, new ObjectFactory().createModule( m ),
                                                   "moduleMessageArgumentJavaTypeNameConstraint", m.getName(),
                                                   msg.getName(), a.getIndex(),
                                                   message != null && message.length() > 0 ? " " + message : "" );

                                    }

                                    try
                                    {
                                        final JavaIdentifier javaIdentifier = a.getJavaVariableName();
                                        final Argument existingArgument =
                                            javaVariableNames.putIfAbsent( javaIdentifier, a );

                                        if ( existingArgument != null )
                                        {
                                            addDetail( validationContext.getReport(),
                                                       "MODULE_MESSAGE_ARGUMENT_JAVA_VARIABLE_NAME_UNIQUENESS_CONSTRAINT",
                                                       Level.SEVERE, new ObjectFactory().createModule( m ),
                                                       "moduleMessageArgumentJavaVariableNameUniquenessConstraint",
                                                       m.getName(), msg.getName(), a.getName(),
                                                       javaIdentifier, existingArgument.getName() );

                                        }
                                    }
                                    catch ( final ModelObjectException e )
                                    {
                                        final String message = getMessage( e );

                                        if ( validationContext.getModelContext().isLoggable( Level.FINE ) )
                                        {
                                            validationContext.getModelContext().log( Level.FINE, message, e );
                                        }

                                        addDetail( validationContext.getReport(),
                                                   "MODULE_MESSAGE_ARGUMENT_JAVA_VARIABLE_NAME_CONSTRAINT",
                                                   Level.SEVERE, new ObjectFactory().createModule( m ),
                                                   "moduleMessageArgumentJavaVariableNameConstraint", m.getName(),
                                                   msg.getName(), a.getIndex(),
                                                   message != null && message.length() > 0 ? " " + message : "" );

                                    }
                                }
                            } );
                        }
                    }
                } );
            }

            try ( final Stream<MessageReference> st0 = m.getMessages().getReference().parallelStream().unordered() )
            {
                st0.forEach( r  ->
                {
                    addDetail( validationContext.getReport(), "MODULE_MESSAGE_REFERENCE_DECLARATION_CONSTRAINT",
                               Level.SEVERE, new ObjectFactory().createModule( m ),
                               "moduleMessageReferenceDeclarationConstraint", m.getName(), r.getName() );

                } );
            }
        }

        if ( m.getProperties() != null )
        {
            try ( final Stream<Property> st0 = m.getProperties().getProperty().parallelStream().unordered() )
            {
                st0.forEach( p  ->
                {
                    if ( p.isFinal() )
                    {
                        addDetail( validationContext.getReport(), "MODULE_FINAL_PROPERTY_DECLARATION_CONSTRAINT",
                                   Level.SEVERE, new ObjectFactory().createModule( m ), "moduleFinalPropertyConstraint",
                                   m.getName(), p.getName() );

                    }

                    if ( p.isOverride() )
                    {
                        addDetail( validationContext.getReport(), "MODULE_OVERRIDE_PROPERTY_DECLARATION_CONSTRAINT",
                                   Level.SEVERE, new ObjectFactory().createModule( m ),
                                   "moduleOverridePropertyConstraint", m.getName(), p.getName() );

                    }

                    if ( p.getValue() != null && p.getAny() != null )
                    {
                        addDetail( validationContext.getReport(), "MODULE_PROPERTY_VALUE_CONSTRAINT", Level.SEVERE,
                                   new ObjectFactory().createModule( m ), "modulePropertyValueConstraint", m.getName(),
                                   p.getName() );

                    }

                    if ( p.getAny() != null && p.getType() == null )
                    {
                        addDetail( validationContext.getReport(), "MODULE_PROPERTY_TYPE_CONSTRAINT", Level.SEVERE,
                                   new ObjectFactory().createModule( m ), "modulePropertyTypeConstraint", m.getName(),
                                   p.getName() );

                    }

                    if ( validationContext.isValidateJava() )
                    {
                        try
                        {
                            p.getJavaConstantName();
                        }
                        catch ( final ModelObjectException e )
                        {
                            final String message = getMessage( e );

                            if ( validationContext.getModelContext().isLoggable( Level.FINE ) )
                            {
                                validationContext.getModelContext().log( Level.FINE, message, e );
                            }

                            addDetail( validationContext.getReport(),
                                       "MODULE_PROPERTY_JAVA_CONSTANT_NAME_CONSTRAINT",
                                       Level.SEVERE, new ObjectFactory().createModule( m ),
                                       "modulePropertyJavaConstantNameConstraint", m.getName(), p.getName(),
                                       message != null && message.length() > 0 ? " " + message : "" );

                        }

                        try
                        {
                            p.getJavaGetterMethodName();
                        }
                        catch ( final ModelObjectException e )
                        {
                            final String message = getMessage( e );

                            if ( validationContext.getModelContext().isLoggable( Level.FINE ) )
                            {
                                validationContext.getModelContext().log( Level.FINE, message, e );
                            }

                            addDetail( validationContext.getReport(),
                                       "MODULE_PROPERTY_JAVA_GETTER_METHOD_NAME_CONSTRAINT",
                                       Level.SEVERE, new ObjectFactory().createModule( m ),
                                       "modulePropertyJavaGetterMethodNameConstraint", m.getName(), p.getName(),
                                       message != null && message.length() > 0 ? " " + message : "" );

                        }

                        try
                        {
                            p.getJavaSetterMethodName();
                        }
                        catch ( final ModelObjectException e )
                        {
                            final String message = getMessage( e );

                            if ( validationContext.getModelContext().isLoggable( Level.FINE ) )
                            {
                                validationContext.getModelContext().log( Level.FINE, message, e );
                            }

                            addDetail( validationContext.getReport(),
                                       "MODULE_PROPERTY_JAVA_SETTER_METHOD_NAME_CONSTRAINT",
                                       Level.SEVERE, new ObjectFactory().createModule( m ),
                                       "modulePropertyJavaSetterMethodNameConstraint", m.getName(), p.getName(),
                                       message != null && message.length() > 0 ? " " + message : "" );

                        }

                        try
                        {
                            p.getJavaTypeName();
                        }
                        catch ( final ModelObjectException e )
                        {
                            final String message = getMessage( e );

                            if ( validationContext.getModelContext().isLoggable( Level.FINE ) )
                            {
                                validationContext.getModelContext().log( Level.FINE, message, e );
                            }

                            addDetail( validationContext.getReport(),
                                       "MODULE_PROPERTY_JAVA_TYPE_NAME_CONSTRAINT",
                                       Level.SEVERE, new ObjectFactory().createModule( m ),
                                       "modulePropertyJavaTypeNameConstraint", m.getName(), p.getName(),
                                       message != null && message.length() > 0 ? " " + message : "" );

                        }

                        try
                        {
                            p.getJavaVariableName();
                        }
                        catch ( final ModelObjectException e )
                        {
                            final String message = getMessage( e );

                            if ( validationContext.getModelContext().isLoggable( Level.FINE ) )
                            {
                                validationContext.getModelContext().log( Level.FINE, message, e );
                            }

                            addDetail( validationContext.getReport(),
                                       "MODULE_PROPERTY_JAVA_VARIABLE_NAME_CONSTRAINT",
                                       Level.SEVERE, new ObjectFactory().createModule( m ),
                                       "modulePropertyJavaVariableNameConstraint", m.getName(), p.getName(),
                                       message != null && message.length() > 0 ? " " + message : "" );

                        }

                        try
                        {
                            p.getJavaValue( validationContext.getModelContext().getClassLoader() );
                        }
                        catch ( final ModelObjectException e )
                        {
                            final String message = getMessage( e );

                            if ( validationContext.getModelContext().isLoggable( Level.FINE ) )
                            {
                                validationContext.getModelContext().log( Level.FINE, message, e );
                            }

                            addDetail( validationContext.getReport(), "MODULE_PROPERTY_JAVA_VALUE_CONSTRAINT",
                                       Level.SEVERE, new ObjectFactory().createModule( m ),
                                       "modulePropertyJavaValueConstraint", m.getName(), p.getName(),
                                       message != null && message.length() > 0 ? " " + message : "" );

                        }
                    }
                } );
            }

            try ( final Stream<PropertyReference> st0 = m.getProperties().getReference().parallelStream().unordered() )
            {
                st0.forEach( r  ->
                {
                    addDetail( validationContext.getReport(), "MODULE_PROPERTY_REFERENCE_DECLARATION_CONSTRAINT",
                               Level.SEVERE, new ObjectFactory().createModule( m ),
                               "modulePropertyReferenceDeclarationConstraint", m.getName(), r.getName() );

                } );
            }
        }

        if ( m.getSpecifications() != null )
        {
            try ( final Stream<SpecificationReference> st0 =
                m.getSpecifications().getReference().parallelStream().unordered() )
            {
                st0.forEach( r  ->
                {
                    addDetail( validationContext.getReport(), "MODULE_SPECIFICATION_REFERENCE_DECLARATION_CONSTRAINT",
                               Level.SEVERE, new ObjectFactory().createModule( m ),
                               "moduleSpecificationReferenceDeclarationConstraint", m.getName(), r.getIdentifier() );
                } );
            }
        }
    }

    private static void assertImplementationsValid( final ValidationContext validationContext ) throws ModelException
    {
        final Implementations implementations = validationContext.getAllImplementations();

        if ( implementations != null )
        {
            try ( final Stream<Implementation> st0 = implementations.getImplementation().parallelStream() )
            {
                final Map<String, Implementation> implementationClassDeclarations = new ConcurrentHashMap<>();
                final Map<String, Implementation> implementationJavaClassDeclarations = new ConcurrentHashMap<>();
                final InheritanceModel imodel = validationContext.getInheritanceModel();

                st0.forEach( impl  ->
                {
                    final Module moduleOfImpl = validationContext.getModuleOfImplementation( impl.getIdentifier() );

                    final Set<InheritanceModel.Node<ImplementationReference>> cycleNodes =
                        imodel.getCycleNodes( impl.getIdentifier() );

                    try ( final Stream<InheritanceModel.Node<ImplementationReference>> st1 =
                        cycleNodes.parallelStream().unordered() )
                    {
                        st1.forEach( n  ->
                        {
                            addDetail( validationContext.getReport(), "IMPLEMENTATION_INHERITANCE_CYCLE_CONSTRAINT",
                                       Level.SEVERE, new ObjectFactory().createImplementation( impl ),
                                       "implementationInheritanceCycleConstraint", impl.getIdentifier(),
                                       moduleOfImpl.getName(), getNodePathString( n ) );

                        } );
                    }

                    if ( validationContext.isValidateJava() )
                    {
                        try
                        {
                            impl.getJavaTypeName();
                        }
                        catch ( final ModelObjectException e )
                        {
                            final String message = getMessage( e );

                            if ( validationContext.getModelContext().isLoggable( Level.FINE ) )
                            {
                                validationContext.getModelContext().log( Level.FINE, message, e );
                            }

                            addDetail( validationContext.getReport(),
                                       "IMPLEMENTATION_JAVA_TYPE_NAME_CONSTRAINT",
                                       Level.SEVERE, new ObjectFactory().createImplementation( impl ),
                                       "implementationJavaTypeNameConstraint", impl.getIdentifier(),
                                       moduleOfImpl.getName(), impl.getClazz(),
                                       message != null && message.length() > 0 ? " " + message : "" );

                        }
                    }

                    if ( impl.isClassDeclaration() )
                    {
                        if ( impl.getClazz() == null )
                        {
                            addDetail( validationContext.getReport(), "IMPLEMENTATION_CLASS_CONSTRAINT", Level.SEVERE,
                                       new ObjectFactory().createImplementation( impl ),
                                       "implementationClassConstraint", impl.getIdentifier(), moduleOfImpl.getName() );

                        }
                        else
                        {
                            final Implementation prev =
                                implementationClassDeclarations.putIfAbsent( impl.getClazz(), impl );

                            if ( prev != null && !prev.getIdentifier().equals( impl.getIdentifier() ) )
                            {
                                final Module moduleOfPrev =
                                    validationContext.getModuleOfImplementation( prev.getIdentifier() );

                                addDetail( validationContext.getReport(),
                                           "IMPLEMENTATION_CLASS_DECLARATION_CONSTRAINT",
                                           Level.SEVERE, new ObjectFactory().createImplementation( impl ),
                                           "implementationClassDeclarationConstraint", impl.getIdentifier(),
                                           moduleOfImpl.getName(), impl.getClazz(), prev.getIdentifier(),
                                           moduleOfPrev.getName() );

                            }

                            try
                            {
                                if ( validationContext.isValidateJava() && impl.getJavaTypeName().isPresent() )
                                {
                                    final Implementation java =
                                        implementationJavaClassDeclarations.putIfAbsent(
                                            impl.getJavaTypeName().get().getClassName(), impl );

                                    if ( java != null && !java.getIdentifier().equals( impl.getIdentifier() ) )
                                    {
                                        final Module moduleOfJava =
                                            validationContext.getModuleOfImplementation( java.getIdentifier() );

                                        addDetail( validationContext.getReport(),
                                                   "IMPLEMENTATION_JAVA_CLASS_DECLARATION_CONSTRAINT",
                                                   Level.SEVERE, new ObjectFactory().createImplementation( impl ),
                                                   "implementationJavaClassDeclarationConstraint",
                                                   impl.getIdentifier(), moduleOfImpl.getName(),
                                                   impl.getJavaTypeName().get().getClassName(), java.getIdentifier(),
                                                   moduleOfJava.getName() );

                                    }
                                }
                            }
                            catch ( final ModelObjectException e )
                            {
                                // Already validated above.
                            }
                        }
                    }

                    if ( impl.isAbstract() && impl.getLocation() != null )
                    {
                        addDetail( validationContext.getReport(),
                                   "IMPLEMENTATION_ABSTRACT_LOCATION_DECLARATION_CONSTRAINT",
                                   Level.SEVERE, new ObjectFactory().createImplementation( impl ),
                                   "implementationAbstractLocationDeclarationConstraint", impl.getIdentifier(),
                                   moduleOfImpl.getName(), impl.getLocation() );

                    }

                    if ( impl.getImplementations() != null )
                    {
                        final Set<String> effImplementationReferences =
                            imodel.getImplementationReferenceIdentifiers( impl.getIdentifier() );

                        try ( final Stream<String> st1 = effImplementationReferences.parallelStream().unordered() )
                        {
                            st1.forEach( r  ->
                            {
                                final Implementation ancestorImplementation = validationContext.getImplementation( r );

                                if ( ancestorImplementation != null && ancestorImplementation.isFinal() )
                                {
                                    final Module moduleOfFinal = validationContext.getModuleOfImplementation(
                                        ancestorImplementation.getIdentifier() );

                                    addDetail( validationContext.getReport(),
                                               "IMPLEMENTATION_IMPLEMENTATION_INHERITANCE_CONSTRAINT", Level.SEVERE,
                                               new ObjectFactory().createImplementation( impl ),
                                               "implementationFinalImplementationConstraint", impl.getIdentifier(),
                                               moduleOfImpl.getName(), ancestorImplementation.getIdentifier(),
                                               moduleOfFinal.getName() );

                                }
                            } );
                        }

                        try ( final Stream<Implementation> st1 = impl.getImplementations().getImplementation().
                            parallelStream().unordered() )
                        {
                            st1.forEach( i  ->
                            {
                                addDetail( validationContext.getReport(),
                                           "IMPLEMENTATION_IMPLEMENTATION_DECLARATION_CONSTRAINT", Level.SEVERE,
                                           new ObjectFactory().createImplementation( impl ),
                                           "implementationImplementationDeclarationConstraint", impl.getIdentifier(),
                                           moduleOfImpl.getName(), i.getIdentifier() );

                            } );
                        }

                        try ( final Stream<ImplementationReference> st1 = impl.getImplementations().getReference().
                            parallelStream().unordered() )
                        {
                            st1.forEach( r  -> assertValidImplementationReference( validationContext, impl, r ) );
                        }
                    }

                    if ( impl.getDependencies() != null )
                    {
                        try ( final Stream<Dependency> st1 = impl.getDependencies().getDependency().parallelStream().
                            unordered() )
                        {
                            st1.forEach( d  -> assertValidDependency( validationContext, impl, d ) );
                        }
                    }

                    assertUniqueDependencies( validationContext, impl );

                    if ( impl.getMessages() != null )
                    {
                        try ( final Stream<Message> st1 = impl.getMessages().getMessage().parallelStream().unordered() )
                        {
                            st1.forEach( m  -> assertValidMessage( validationContext, impl, m ) );
                        }
                        try ( final Stream<MessageReference> st1 = impl.getMessages().getReference().parallelStream().
                            unordered() )
                        {
                            st1.forEach( r  -> assertValidMessageReference( validationContext, impl, r ) );
                        }
                    }

                    assertUniqueMessages( validationContext, impl );

                    if ( impl.getProperties() != null )
                    {
                        try ( final Stream<Property> st1 = impl.getProperties().getProperty().parallelStream().
                            unordered() )
                        {
                            st1.forEach( p  -> assertValidProperty( validationContext, impl, p ) );
                        }
                        try ( final Stream<PropertyReference> st1 = impl.getProperties().getReference().
                            parallelStream() )
                        {
                            st1.forEach( r  -> assertValidPropertyReference( validationContext, impl, r ) );
                        }
                    }

                    assertUniqueProperties( validationContext, impl );

                    if ( impl.getSpecifications() != null )
                    {
                        try ( final Stream<Specification> st1 = impl.getSpecifications().getSpecification().
                            parallelStream().unordered() )
                        {
                            st1.forEach( s  ->
                            {
                                addDetail( validationContext.getReport(),
                                           "IMPLEMENTATION_SPECIFICATION_DECLARATION_CONSTRAINT",
                                           Level.SEVERE, new ObjectFactory().createImplementation( impl ),
                                           "implementationSpecificationDeclarationConstraint", impl.getIdentifier(),
                                           moduleOfImpl.getName(), s.getIdentifier() );

                            } );
                        }

                        try ( final Stream<SpecificationReference> st1 = impl.getSpecifications().getReference().
                            parallelStream().unordered() )
                        {
                            st1.forEach( r  -> assertValidSpecificationReference( validationContext, impl, r ) );
                        }
                    }

                    if ( !impl.getAny().isEmpty() )
                    {
                        try ( final Stream<?> st1 = impl.getAny().parallelStream() )
                        {
                            st1.forEach( any  -> assertValidAnyObject( validationContext, impl, any ) );
                        }
                    }

                    final Set<String> specificationReferenceIdentifiers =
                        imodel.getSpecificationReferenceIdentifiers( impl.getIdentifier() );

                    try ( final Stream<String> st1 = specificationReferenceIdentifiers.parallelStream().unordered() )
                    {
                        st1.forEach( r  ->
                        {
                            final Set<InheritanceModel.Node<SpecificationReference>> specificationReferenceNodes =
                                imodel.getSpecificationReferenceNodes( impl.getIdentifier(), r );

                            if ( specificationReferenceNodes.size() > 1 )
                            {
                                addDetail( validationContext.getReport(),
                                           "IMPLEMENTATION_SPECIFICATION_MULTIPLE_INHERITANCE_CONSTRAINT",
                                           Level.SEVERE, new ObjectFactory().createImplementation( impl ),
                                           "implementationMultipleInheritanceSpecificationConstraint",
                                           impl.getIdentifier(), moduleOfImpl.getName(), r,
                                           getNodeListPathString( specificationReferenceNodes ) );

                            }
                        } );
                    }

                    assertImplementationSpecificationCompatibility( validationContext, impl );

                    final Set<QName> xmlElementNames = imodel.getXmlElementNames( impl.getIdentifier() );

                    try ( final Stream<QName> st1 = xmlElementNames.parallelStream().unordered() )
                    {
                        st1.forEach( n  ->
                        {
                            final Set<InheritanceModel.Node<Element>> xmlElementNodes =
                                imodel.getXmlElementNodes( impl.getIdentifier(), n );

                            if ( xmlElementNodes.size() > 1 )
                            {
                                addDetail( validationContext.getReport(),
                                           "IMPLEMENTATION_XML_ELEMENT_MULTIPLE_INHERITANCE_CONSTRAINT",
                                           Level.SEVERE, new ObjectFactory().createImplementation( impl ),
                                           "implementationMultipleInheritanceXmlElementConstraint",
                                           impl.getIdentifier(), moduleOfImpl.getName(), n.toString(),
                                           getNodeListPathString( xmlElementNodes ) );

                            }
                        } );
                    }

                    final Set<QName> jaxbElementNames = imodel.getJaxbElementNames( impl.getIdentifier() );

                    try ( final Stream<QName> st1 = jaxbElementNames.parallelStream().unordered() )
                    {
                        st1.forEach( n  ->
                        {
                            final Set<InheritanceModel.Node<JAXBElement<?>>> jaxbElementNodes =
                                imodel.getJaxbElementNodes( impl.getIdentifier(), n );

                            if ( jaxbElementNodes.size() > 1 )
                            {
                                addDetail( validationContext.getReport(),
                                           "IMPLEMENTATION_JAXB_ELEMENT_MULTIPLE_INHERITANCE_CONSTRAINT",
                                           Level.SEVERE, new ObjectFactory().createImplementation( impl ),
                                           "implementationMultipleInheritanceJaxbElementConstraint",
                                           impl.getIdentifier(), moduleOfImpl.getName(), n.toString(),
                                           getNodeListPathString( jaxbElementNodes ) );

                            }
                        } );
                    }

                    final Set<String> implementationReferenceIdentifiers =
                        imodel.getImplementationReferenceIdentifiers( impl.getIdentifier() );

                    try ( final Stream<String> st1 = implementationReferenceIdentifiers.parallelStream().unordered() )
                    {
                        st1.forEach( r  ->
                        {
                            final Set<InheritanceModel.Node<ImplementationReference>> implementationReferenceNodes =
                                imodel.getImplementationReferenceNodes( impl.getIdentifier(), r );

                            try ( final Stream<InheritanceModel.Node<ImplementationReference>> st2 =
                                implementationReferenceNodes.parallelStream().unordered() )
                            {
                                st2.forEach( n  ->
                                {
                                    final ImplementationReference ref = n.getModelObject();

                                    final Implementation referenced =
                                        validationContext.getImplementation( ref.getIdentifier() );

                                    if ( ref.getVersion() != null && referenced != null )
                                    {
                                        final Module moduleOfReferenced =
                                            validationContext.getModuleOfImplementation( referenced.getIdentifier() );

                                        if ( referenced.getVersion() == null )
                                        {
                                            addDetail( validationContext.getReport(),
                                                       "IMPLEMENTATION_IMPLEMENTATION_VERSIONING_CONSTRAINT",
                                                       Level.SEVERE,
                                                       new ObjectFactory().createImplementation( impl ),
                                                       "implementationImplementationVersioningConstraint",
                                                       impl.getIdentifier(), moduleOfImpl.getName(),
                                                       ref.getIdentifier(), moduleOfReferenced.getName() );

                                        }
                                        else
                                        {
                                            try
                                            {
                                                if ( VersionParser.compare( ref.getVersion(),
                                                                            referenced.getVersion() ) > 0 )
                                                {
                                                    addDetail( validationContext.getReport(),
                                                               "IMPLEMENTATION_INHERITANCE_COMPATIBILITY_CONSTRAINT",
                                                               Level.SEVERE, new ObjectFactory().
                                                                   createImplementation( impl ),
                                                               "implementationInheritanceCompatibilityConstraint",
                                                               impl.getIdentifier(), moduleOfImpl.getName(),
                                                               referenced.getIdentifier(), moduleOfReferenced.getName(),
                                                               ref.getVersion(), referenced.getVersion() );

                                                }
                                            }
                                            catch ( final ParseException ex )
                                            {
                                                final String message = getMessage( ex );

                                                if ( validationContext.getModelContext().isLoggable( Level.FINE ) )
                                                {
                                                    validationContext.getModelContext().log( Level.FINE, message, ex );
                                                }

                                                addDetail(
                                                    validationContext.getReport(),
                                                    "IMPLEMENTATION_INHERITANCE_COMPATIBILITY_VERSIONING_PARSE_EXCEPTION",
                                                    Level.SEVERE, new ObjectFactory().createImplementation( impl ),
                                                    "implementationInheritanceCompatibilityParseException",
                                                    impl.getIdentifier(), moduleOfImpl.getName(), ref.getIdentifier(),
                                                    moduleOfReferenced.getName(), ref.getVersion(),
                                                    message != null && message.length() > 0 ? " " + message : "" );

                                            }
                                            catch ( final TokenMgrError ex )
                                            {
                                                final String message = getMessage( ex );

                                                if ( validationContext.getModelContext().isLoggable( Level.FINE ) )
                                                {
                                                    validationContext.getModelContext().log( Level.FINE, message, ex );
                                                }

                                                addDetail(
                                                    validationContext.getReport(),
                                                    "IMPLEMENTATION_INHERITANCE_COMPATIBILITY_VERSIONING_TOKEN_MANAGER_ERROR",
                                                    Level.SEVERE, new ObjectFactory().createImplementation( impl ),
                                                    "implementationInheritanceCompatiblityVersioningTokenManagerError",
                                                    impl.getIdentifier(), moduleOfImpl.getName(), ref.getIdentifier(),
                                                    moduleOfReferenced.getName(), ref.getVersion(),
                                                    message != null && message.length() > 0 ? " " + message : "" );

                                            }
                                        }
                                    }
                                } );
                            }
                        } );

                    }
                } );
            }
        }
    }

    private static void assertValidDependency( final ValidationContext validationContext,
                                               final Implementation impl,
                                               final Dependency d )
    {
        try ( final Stream<InheritanceModel.Node<Dependency>> st0 = validationContext.getInheritanceModel().
            getDependencyNodes( impl.getIdentifier(), d.getName() ).parallelStream().unordered() )
        {
            st0.forEach( effDependency  ->
            {
                final Set<InheritanceModel.Node<Dependency>> overriddenDependencies =
                    modifiableSet( effDependency.getOverriddenNodes() );

                if ( d.isOverride() && effDependency.getOverriddenNodes().isEmpty() )
                {
                    addDetail( validationContext.getReport(),
                               "IMPLEMENTATION_DEPENDENCY_OVERRIDE_CONSTRAINT",
                               Level.SEVERE, new ObjectFactory().createImplementation( impl ),
                               "implementationDependencyOverrideConstraint", impl.getIdentifier(),
                               validationContext.getModuleOfImplementation( impl.getIdentifier() ).getName(),
                               d.getName() );

                }

                if ( !( d.isOverride() || overriddenDependencies.isEmpty() ) )
                {
                    try ( final Stream<InheritanceModel.Node<Dependency>> st1 =
                        overriddenDependencies.parallelStream().unordered() )
                    {
                        st1.forEach( overriddenDependency  ->
                        {
                            Implementation overriddenImplementation = overriddenDependency.getImplementation();
                            if ( overriddenDependency.getClassDeclaration() != null )
                            {
                                overriddenImplementation = overriddenDependency.getClassDeclaration();
                            }

                            final Module moduleOfDependency =
                                validationContext.getModuleOfImplementation( overriddenImplementation.getIdentifier() );

                            addDetail( validationContext.getReport(),
                                       "IMPLEMENTATION_DEPENDENCY_OVERRIDE_WARNING",
                                       Level.WARNING, new ObjectFactory().createImplementation( impl ),
                                       "implementationDependencyOverrideWarning", impl.getIdentifier(),
                                       validationContext.getModuleOfImplementation( impl.getIdentifier() ).getName(),
                                       d.getName(), overriddenImplementation.getIdentifier(),
                                       moduleOfDependency.getName(), getNodePathString( overriddenDependency ) );

                        } );
                    }
                }

                retainFinalNodes( overriddenDependencies );

                try ( final Stream<InheritanceModel.Node<Dependency>> st1 = overriddenDependencies.parallelStream() )
                {
                    st1.forEach( overriddenDependency  ->
                    {
                        Implementation overriddenImplementation = overriddenDependency.getImplementation();
                        if ( overriddenDependency.getClassDeclaration() != null )
                        {
                            overriddenImplementation = overriddenDependency.getClassDeclaration();
                        }

                        final Module moduleOfDependency =
                            validationContext.getModuleOfImplementation( overriddenImplementation.getIdentifier() );

                        addDetail( validationContext.getReport(),
                                   "IMPLEMENTATION_DEPENDENCY_INHERITANCE_CONSTRAINT", Level.SEVERE,
                                   new ObjectFactory().createImplementation( impl ),
                                   "implementationDependencyFinalConstraint", impl.getIdentifier(),
                                   validationContext.getModuleOfImplementation( impl.getIdentifier() ).getName(),
                                   d.getName(), overriddenImplementation.getIdentifier(), moduleOfDependency.getName(),
                                   getNodePathString( overriddenDependency ) );

                    } );
                }
            } );

            if ( validationContext.isValidateJava() )
            {
                try
                {
                    d.getJavaConstantName();
                }
                catch ( final ModelObjectException e )
                {
                    final String message = getMessage( e );

                    if ( validationContext.getModelContext().isLoggable( Level.FINE ) )
                    {
                        validationContext.getModelContext().log( Level.FINE, message, e );
                    }

                    addDetail( validationContext.getReport(),
                               "IMPLEMENTATION_DEPENDENCY_JAVA_CONSTANT_NAME_CONSTRAINT",
                               Level.SEVERE, new ObjectFactory().createImplementation( impl ),
                               "implementationDependencyJavaConstantNameConstraint", impl.getIdentifier(),
                               validationContext.getModuleOfImplementation( impl.getIdentifier() ).getName(),
                               d.getName(), message != null && message.length() > 0 ? " " + message : "" );

                }

                try
                {
                    d.getJavaGetterMethodName();
                }
                catch ( final ModelObjectException e )
                {
                    final String message = getMessage( e );

                    if ( validationContext.getModelContext().isLoggable( Level.FINE ) )
                    {
                        validationContext.getModelContext().log( Level.FINE, message, e );
                    }

                    addDetail( validationContext.getReport(),
                               "IMPLEMENTATION_DEPENDENCY_JAVA_GETTER_METHOD_NAME_CONSTRAINT",
                               Level.SEVERE, new ObjectFactory().createImplementation( impl ),
                               "implementationDependencyJavaGetterMethodNameConstraint",
                               impl.getIdentifier(),
                               validationContext.getModuleOfImplementation( impl.getIdentifier() ).getName(),
                               d.getName(), message != null && message.length() > 0 ? " " + message : "" );

                }

                try
                {
                    d.getJavaSetterMethodName();
                }
                catch ( final ModelObjectException e )
                {
                    final String message = getMessage( e );

                    if ( validationContext.getModelContext().isLoggable( Level.FINE ) )
                    {
                        validationContext.getModelContext().log( Level.FINE, message, e );
                    }

                    addDetail( validationContext.getReport(),
                               "IMPLEMENTATION_DEPENDENCY_JAVA_SETTER_METHOD_NAME_CONSTRAINT",
                               Level.SEVERE, new ObjectFactory().createImplementation( impl ),
                               "implementationDependencyJavaSetterMethodNameConstraint",
                               impl.getIdentifier(),
                               validationContext.getModuleOfImplementation( impl.getIdentifier() ).getName(),
                               d.getName(), message != null && message.length() > 0 ? " " + message : "" );

                }

                try
                {
                    d.getJavaVariableName();
                }
                catch ( final ModelObjectException e )
                {
                    final String message = getMessage( e );

                    if ( validationContext.getModelContext().isLoggable( Level.FINE ) )
                    {
                        validationContext.getModelContext().log( Level.FINE, message, e );
                    }

                    addDetail( validationContext.getReport(),
                               "IMPLEMENTATION_DEPENDENCY_JAVA_VARIABLE_NAME_CONSTRAINT",
                               Level.SEVERE, new ObjectFactory().createImplementation( impl ),
                               "implementationDependencyJavaVariableNameConstraint",
                               impl.getIdentifier(),
                               validationContext.getModuleOfImplementation( impl.getIdentifier() ).getName(),
                               d.getName(), message != null && message.length() > 0 ? " " + message : "" );

                }
            }
        }

        assertDependencyValid( validationContext, impl, d );
    }

    private static void assertValidImplementationReference( final ValidationContext validationContext,
                                                            final Implementation impl,
                                                            final ImplementationReference r )
    {
        try ( final Stream<InheritanceModel.Node<ImplementationReference>> st0 =
            validationContext.getInheritanceModel().getImplementationReferenceNodes(
                impl.getIdentifier(), r.getIdentifier() ).parallelStream().unordered() )
        {
            st0.forEach( effReference  ->
            {
                final Set<InheritanceModel.Node<ImplementationReference>> overriddenReferences =
                    modifiableSet( effReference.getOverriddenNodes() );

                if ( r.isOverride() && overriddenReferences.isEmpty() )
                {
                    addDetail( validationContext.getReport(),
                               "IMPLEMENTATION_IMPLEMENTATION_OVERRIDE_CONSTRAINT", Level.SEVERE,
                               new ObjectFactory().createImplementation( impl ),
                               "implementationImplementationOverrideConstraint", impl.getIdentifier(),
                               validationContext.getModuleOfImplementation( impl.getIdentifier() ).getName(),
                               r.getIdentifier() );

                }

                if ( !( r.isOverride() || overriddenReferences.isEmpty() ) )
                {
                    try ( final Stream<InheritanceModel.Node<ImplementationReference>> st1 =
                        overriddenReferences.parallelStream() )
                    {
                        st1.forEach( overriddenReference  ->
                        {
                            Implementation overriddenImplementation = overriddenReference.getImplementation();
                            if ( overriddenReference.getClassDeclaration() != null )
                            {
                                overriddenImplementation = overriddenReference.getClassDeclaration();
                            }

                            final Module moduleOfReference =
                                validationContext.getModuleOfImplementation( overriddenImplementation.getIdentifier() );

                            addDetail( validationContext.getReport(),
                                       "IMPLEMENTATION_IMPLEMENTATION_REFERENCE_OVERRIDE_WARNING",
                                       Level.WARNING, new ObjectFactory().createImplementation( impl ),
                                       "implementationImplementationOverrideWarning", impl.getIdentifier(),
                                       validationContext.getModuleOfImplementation( impl.getIdentifier() ).getName(),
                                       r.getIdentifier(), overriddenImplementation.getIdentifier(),
                                       moduleOfReference.getName(), getNodePathString( overriddenReference ) );

                        } );
                    }
                }

                retainFinalNodes( overriddenReferences );

                try ( final Stream<InheritanceModel.Node<ImplementationReference>> st1 =
                    overriddenReferences.parallelStream() )
                {
                    st1.forEach( overriddenReference  ->
                    {
                        Implementation overriddenImplementation = overriddenReference.getImplementation();
                        if ( overriddenReference.getClassDeclaration() != null )
                        {
                            overriddenImplementation = overriddenReference.getClassDeclaration();
                        }

                        final Module moduleOfReference =
                            validationContext.getModuleOfImplementation( overriddenImplementation.getIdentifier() );

                        addDetail( validationContext.getReport(),
                                   "IMPLEMENTATION_IMPLEMENTATION_REFERENCE_INHERITANCE_CONSTRAINT",
                                   Level.SEVERE, new ObjectFactory().createImplementation( impl ),
                                   "implementationFinalImplementatioReferenceConstraint", impl.getIdentifier(),
                                   validationContext.getModuleOfImplementation( impl.getIdentifier() ).getName(),
                                   r.getIdentifier(), overriddenImplementation.getIdentifier(),
                                   moduleOfReference.getName(), getNodePathString( overriddenReference ) );

                    } );
                }
            } );
        }
    }

    private static void assertValidMessage( final ValidationContext validationContext,
                                            final Implementation impl,
                                            final Message m )
    {
        if ( impl.getMessages().getReference( m.getName() ).isPresent() )
        {
            addDetail( validationContext.getReport(), "IMPLEMENTATION_MESSAGES_UNIQUENESS_CONSTRAINT",
                       Level.SEVERE, new ObjectFactory().createImplementation( impl ),
                       "implementationMessagesUniquenessConstraint", impl.getIdentifier(),
                       validationContext.getModuleOfImplementation( impl.getIdentifier() ).getName(), m.getName() );

        }

        if ( validationContext.isValidateJava() )
        {
            try
            {
                m.getJavaConstantName();
            }
            catch ( final ModelObjectException e )
            {
                final String message = getMessage( e );

                if ( validationContext.getModelContext().isLoggable( Level.FINE ) )
                {
                    validationContext.getModelContext().log( Level.FINE, message, e );
                }

                addDetail( validationContext.getReport(),
                           "IMPLEMENTATION_MESSAGE_JAVA_CONSTANT_NAME_CONSTRAINT", Level.SEVERE,
                           new ObjectFactory().createImplementation( impl ),
                           "implementationMessageJavaConstantNameConstraint", impl.getIdentifier(),
                           validationContext.getModuleOfImplementation( impl.getIdentifier() ).getName(), m.getName(),
                           message != null && message.length() > 0 ? " " + message : "" );

            }

            try
            {
                m.getJavaGetterMethodName();
            }
            catch ( final ModelObjectException e )
            {
                final String message = getMessage( e );

                if ( validationContext.getModelContext().isLoggable( Level.FINE ) )
                {
                    validationContext.getModelContext().log( Level.FINE, message, e );
                }

                addDetail( validationContext.getReport(),
                           "IMPLEMENTATION_MESSAGE_JAVA_GETTER_METHOD_NAME_CONSTRAINT", Level.SEVERE,
                           new ObjectFactory().createImplementation( impl ),
                           "implementationMessageJavaGetterMethodNameConstraint", impl.getIdentifier(),
                           validationContext.getModuleOfImplementation( impl.getIdentifier() ).getName(), m.getName(),
                           message != null && message.length() > 0 ? " " + message : "" );

            }

            try
            {
                m.getJavaSetterMethodName();
            }
            catch ( final ModelObjectException e )
            {
                final String message = getMessage( e );

                if ( validationContext.getModelContext().isLoggable( Level.FINE ) )
                {
                    validationContext.getModelContext().log( Level.FINE, message, e );
                }

                addDetail( validationContext.getReport(),
                           "IMPLEMENTATION_MESSAGE_JAVA_SETTER_METHOD_NAME_CONSTRAINT", Level.SEVERE,
                           new ObjectFactory().createImplementation( impl ),
                           "implementationMessageJavaSetterMethodNameConstraint", impl.getIdentifier(),
                           validationContext.getModuleOfImplementation( impl.getIdentifier() ).getName(), m.getName(),
                           message != null && message.length() > 0 ? " " + message : "" );

            }

            try
            {
                m.getJavaVariableName();
            }
            catch ( final ModelObjectException e )
            {
                final String message = getMessage( e );

                if ( validationContext.getModelContext().isLoggable( Level.FINE ) )
                {
                    validationContext.getModelContext().log( Level.FINE, message, e );
                }

                addDetail( validationContext.getReport(),
                           "IMPLEMENTATION_MESSAGE_JAVA_VARIABLE_NAME_CONSTRAINT", Level.SEVERE,
                           new ObjectFactory().createImplementation( impl ),
                           "implementationMessageJavaVariableNameConstraint", impl.getIdentifier(),
                           validationContext.getModuleOfImplementation( impl.getIdentifier() ).getName(), m.getName(),
                           message != null && message.length() > 0 ? " " + message : "" );

            }
        }

        if ( m.getTemplate() != null )
        {
            try ( final Stream<Text> st0 = m.getTemplate().getText().parallelStream().unordered() )
            {
                st0.forEach( t  ->
                {
                    try
                    {
                        t.getMimeType();
                    }
                    catch ( final ModelObjectException e )
                    {
                        final String message = getMessage( e );

                        if ( validationContext.getModelContext().isLoggable( Level.FINE ) )
                        {
                            validationContext.getModelContext().log( Level.FINE, message, e );
                        }

                        addDetail( validationContext.getReport(),
                                   "IMPLEMENTATION_MESSAGE_TEMPLATE_MIME_TYPE_CONSTRAINT", Level.SEVERE,
                                   new ObjectFactory().createImplementation( impl ),
                                   "implementationMessageTemplateMimeTypeConstraint", impl.getIdentifier(),
                                   validationContext.getModuleOfImplementation( impl.getIdentifier() ).getName(),
                                   m.getName(), t.getLanguage(),
                                   message != null && message.length() > 0 ? " " + message : "" );

                    }

                    if ( validationContext.isValidateJava() )
                    {
                        try
                        {
                            new MessageFormat( t.getValue(), new Locale( t.getLanguage() ) );
                        }
                        catch ( final IllegalArgumentException e )
                        {
                            final String message = getMessage( e );

                            if ( validationContext.getModelContext().isLoggable( Level.FINE ) )
                            {
                                validationContext.getModelContext().log( Level.FINE, message, e );
                            }

                            addDetail( validationContext.getReport(),
                                       "IMPLEMENTATION_MESSAGE_TEMPLATE_CONSTRAINT", Level.SEVERE,
                                       new ObjectFactory().createImplementation( impl ),
                                       "implementationMessageTemplateConstraint", impl.getIdentifier(),
                                       validationContext.getModuleOfImplementation( impl.getIdentifier() ).getName(),
                                       m.getName(), t.getLanguage(),
                                       message != null && message.length() > 0 ? " " + message : "" );

                        }
                    }
                } );
            }
        }

        try ( final Stream<InheritanceModel.Node<Message>> st0 =
            validationContext.getInheritanceModel().getMessageNodes( impl.getIdentifier(), m.getName() ).
                parallelStream().unordered() )
        {
            st0.forEach( effMessage  ->
            {
                final Set<InheritanceModel.Node<Message>> overriddenMessages =
                    modifiableSet( effMessage.getOverriddenNodes() );

                if ( m.isOverride() && overriddenMessages.isEmpty() )
                {
                    addDetail( validationContext.getReport(), "IMPLEMENTATION_MESSAGE_OVERRIDE_CONSTRAINT",
                               Level.SEVERE, new ObjectFactory().createImplementation( impl ),
                               "implementationMessageOverrideConstraint", impl.getIdentifier(),
                               validationContext.getModuleOfImplementation( impl.getIdentifier() ).getName(),
                               m.getName() );

                }

                if ( !( m.isOverride() || overriddenMessages.isEmpty() ) )
                {
                    try ( final Stream<InheritanceModel.Node<Message>> st1 = overriddenMessages.parallelStream() )
                    {
                        st1.forEach( overriddenMessage  ->
                        {
                            Implementation overriddenImplementation = overriddenMessage.getImplementation();
                            if ( overriddenMessage.getClassDeclaration() != null )
                            {
                                overriddenImplementation = overriddenMessage.getClassDeclaration();
                            }

                            final Module moduleOfMessage =
                                validationContext.getModuleOfImplementation( overriddenImplementation.getIdentifier() );

                            addDetail( validationContext.getReport(), "IMPLEMENTATION_MESSAGE_OVERRIDE_WARNING",
                                       Level.WARNING, new ObjectFactory().createImplementation( impl ),
                                       "implementationMessageOverrideWarning", impl.getIdentifier(),
                                       validationContext.getModuleOfImplementation( impl.getIdentifier() ).getName(),
                                       m.getName(), overriddenImplementation.getIdentifier(), moduleOfMessage.getName(),
                                       getNodePathString( overriddenMessage ) );

                        } );
                    }
                }

                retainFinalNodes( overriddenMessages );

                try ( final Stream<InheritanceModel.Node<Message>> st1 = overriddenMessages.parallelStream() )
                {
                    st1.forEach( overriddenMessage  ->
                    {
                        Implementation overriddenImplementation = overriddenMessage.getImplementation();
                        if ( overriddenMessage.getClassDeclaration() != null )
                        {
                            overriddenImplementation = overriddenMessage.getClassDeclaration();
                        }

                        final Module moduleOfMessage = validationContext.getModuleOfImplementation(
                            overriddenImplementation.getIdentifier() );

                        addDetail( validationContext.getReport(),
                                   "IMPLEMENTATION_MESSAGE_INHERITANCE_CONSTRAINT",
                                   Level.SEVERE, new ObjectFactory().createImplementation( impl ),
                                   "implementationMessageFinalConstraint", impl.getIdentifier(),
                                   validationContext.getModuleOfImplementation( impl.getIdentifier() ).getName(),
                                   m.getName(), overriddenImplementation.getIdentifier(), moduleOfMessage.getName(),
                                   getNodePathString( overriddenMessage ) );

                    } );
                }
            } );
        }

        if ( m.getArguments() != null )
        {
            final Map<JavaIdentifier, Argument> javaVariableNames =
                new ConcurrentHashMap<>( m.getArguments().getArgument().size() );

            try ( final Stream<Argument> st0 = m.getArguments().getArgument().parallelStream().unordered() )
            {
                st0.forEach( a  ->
                {
                    if ( validationContext.isValidateJava() )
                    {
                        try
                        {
                            a.getJavaTypeName();
                        }
                        catch ( final ModelObjectException e )
                        {
                            final String message = getMessage( e );

                            if ( validationContext.getModelContext().isLoggable( Level.FINE ) )
                            {
                                validationContext.getModelContext().log( Level.FINE, message, e );
                            }

                            addDetail( validationContext.getReport(),
                                       "IMPLEMENTATION_MESSAGE_ARGUMENT_JAVA_TYPE_NAME_CONSTRAINT",
                                       Level.SEVERE, new ObjectFactory().createImplementation( impl ),
                                       "implementationMessageArgumentJavaTypeNameConstraint",
                                       impl.getIdentifier(),
                                       validationContext.getModuleOfImplementation( impl.getIdentifier() ).getName(),
                                       m.getName(), a.getName(),
                                       message != null && message.length() > 0 ? " " + message : "" );

                        }

                        try
                        {
                            final JavaIdentifier javaIdentifier = a.getJavaVariableName();
                            final Argument existingArgument = javaVariableNames.putIfAbsent( javaIdentifier, a );

                            if ( existingArgument != null )
                            {
                                addDetail( validationContext.getReport(),
                                           "IMPLEMENTATION_MESSAGE_ARGUMENT_JAVA_VARIABLE_NAME_UNIQUENESS_CONSTRAINT",
                                           Level.SEVERE, new ObjectFactory().createImplementation( impl ),
                                           "implementationMessageArgumentJavaVariableNameUniquenessConstraint",
                                           impl.getIdentifier(), validationContext.getModuleOfImplementation(
                                           impl.getIdentifier() ).getName(), m.getName(), a.getName(),
                                           javaIdentifier, existingArgument.getName() );

                            }
                        }
                        catch ( final ModelObjectException e )
                        {
                            final String message = getMessage( e );

                            if ( validationContext.getModelContext().isLoggable( Level.FINE ) )
                            {
                                validationContext.getModelContext().log( Level.FINE, message, e );
                            }

                            addDetail( validationContext.getReport(),
                                       "IMPLEMENTATION_MESSAGE_ARGUMENT_JAVA_VARIABLE_NAME_CONSTRAINT",
                                       Level.SEVERE, new ObjectFactory().createImplementation( impl ),
                                       "implementationMessageArgumentJavaVariableNameConstraint",
                                       impl.getIdentifier(),
                                       validationContext.getModuleOfImplementation( impl.getIdentifier() ).getName(),
                                       m.getName(), a.getIndex(),
                                       message != null && message.length() > 0 ? " " + message : "" );

                        }
                    }
                } );
            }
        }
    }

    private static void assertValidMessageReference( final ValidationContext validationContext,
                                                     final Implementation impl,
                                                     final MessageReference r )
    {
        try ( final Stream<InheritanceModel.Node<Message>> st0 =
            validationContext.getInheritanceModel().getMessageNodes( impl.getIdentifier(), r.getName() ).
                parallelStream().unordered() )
        {
            st0.forEach( effMessage  ->
            {
                final Set<InheritanceModel.Node<Message>> overriddenMessages =
                    modifiableSet( effMessage.getOverriddenNodes() );

                if ( r.isOverride() && overriddenMessages.isEmpty() )
                {
                    addDetail( validationContext.getReport(), "IMPLEMENTATION_MESSAGE_OVERRIDE_CONSTRAINT",
                               Level.SEVERE, new ObjectFactory().createImplementation( impl ),
                               "implementationMessageOverrideConstraint", impl.getIdentifier(),
                               validationContext.getModuleOfImplementation( impl.getIdentifier() ).getName(),
                               r.getName() );

                }

                if ( !( r.isOverride() || overriddenMessages.isEmpty() ) )
                {
                    try ( final Stream<InheritanceModel.Node<Message>> st1 =
                        overriddenMessages.parallelStream().unordered() )
                    {
                        st1.forEach( overriddenMessage  ->
                        {
                            Implementation overriddenImplementation = overriddenMessage.getImplementation();
                            if ( overriddenMessage.getClassDeclaration() != null )
                            {
                                overriddenImplementation = overriddenMessage.getClassDeclaration();
                            }

                            final Module moduleOfMessage =
                                validationContext.getModuleOfImplementation(
                                    overriddenImplementation.getIdentifier() );

                            addDetail( validationContext.getReport(), "IMPLEMENTATION_MESSAGE_OVERRIDE_WARNING",
                                       Level.WARNING, new ObjectFactory().createImplementation( impl ),
                                       "implementationMessageOverrideWarning", impl.getIdentifier(),
                                       validationContext.getModuleOfImplementation( impl.getIdentifier() ).getName(),
                                       r.getName(), overriddenImplementation.getIdentifier(),
                                       moduleOfMessage.getName(), getNodePathString( overriddenMessage ) );

                        } );
                    }
                }

                retainFinalNodes( overriddenMessages );

                try ( final Stream<InheritanceModel.Node<Message>> st1 =
                    overriddenMessages.parallelStream().unordered() )
                {
                    st1.forEach( overriddenMessage  ->
                    {
                        Implementation overriddenImplementation = overriddenMessage.getImplementation();
                        if ( overriddenMessage.getClassDeclaration() != null )
                        {
                            overriddenImplementation = overriddenMessage.getClassDeclaration();
                        }

                        final Module moduleOfMessage =
                            validationContext.getModuleOfImplementation( overriddenImplementation.getIdentifier() );

                        addDetail( validationContext.getReport(),
                                   "IMPLEMENTATION_MESSAGE_INHERITANCE_CONSTRAINT",
                                   Level.SEVERE, new ObjectFactory().createImplementation( impl ),
                                   "implementationMessageFinalConstraint", impl.getIdentifier(),
                                   validationContext.getModuleOfImplementation( impl.getIdentifier() ).getName(),
                                   r.getName(), overriddenImplementation.getIdentifier(),
                                   moduleOfMessage.getName(), getNodePathString( overriddenMessage ) );

                    } );
                }
            } );
        }
    }

    private static void assertValidProperty( final ValidationContext validationContext,
                                             final Implementation impl,
                                             final Property p )
    {
        if ( impl.getProperties().getReference( p.getName() ).isPresent() )
        {
            addDetail( validationContext.getReport(), "IMPLEMENTATION_PROPERTIES_UNIQUENESS_CONSTRAINT",
                       Level.SEVERE, new ObjectFactory().createImplementation( impl ),
                       "implementationPropertiesUniquenessConstraint", impl.getIdentifier(),
                       validationContext.getModuleOfImplementation( impl.getIdentifier() ).getName(), p.getName() );

        }

        if ( p.getValue() != null && p.getAny() != null )
        {
            addDetail( validationContext.getReport(), "IMPLEMENTATION_PROPERTY_VALUE_CONSTRAINT",
                       Level.SEVERE, new ObjectFactory().createImplementation( impl ),
                       "implementationPropertyValueConstraint", impl.getIdentifier(),
                       validationContext.getModuleOfImplementation( impl.getIdentifier() ).getName(), p.getName() );

        }

        if ( p.getAny() != null && p.getType() == null )
        {
            addDetail( validationContext.getReport(), "IMPLEMENTATION_PROPERTY_TYPE_CONSTRAINT",
                       Level.SEVERE, new ObjectFactory().createImplementation( impl ),
                       "implementationPropertyTypeConstraint", impl.getIdentifier(),
                       validationContext.getModuleOfImplementation( impl.getIdentifier() ).getName(), p.getName() );

        }

        if ( validationContext.isValidateJava() )
        {
            try
            {
                p.getJavaConstantName();
            }
            catch ( final ModelObjectException e )
            {
                final String message = getMessage( e );

                if ( validationContext.getModelContext().isLoggable( Level.FINE ) )
                {
                    validationContext.getModelContext().log( Level.FINE, message, e );
                }

                addDetail( validationContext.getReport(),
                           "IMPLEMENTATION_PROPERTY_JAVA_CONSTANT_NAME_CONSTRAINT",
                           Level.SEVERE, new ObjectFactory().createImplementation( impl ),
                           "implementationPropertyJavaConstantNameConstraint", impl.getIdentifier(),
                           validationContext.getModuleOfImplementation( impl.getIdentifier() ).getName(), p.getName(),
                           message != null && message.length() > 0 ? " " + message : "" );

            }

            try
            {
                p.getJavaGetterMethodName();
            }
            catch ( final ModelObjectException e )
            {
                final String message = getMessage( e );

                if ( validationContext.getModelContext().isLoggable( Level.FINE ) )
                {
                    validationContext.getModelContext().log( Level.FINE, message, e );
                }

                addDetail( validationContext.getReport(),
                           "IMPLEMENTATION_PROPERTY_JAVA_GETTER_METHOD_NAME_CONSTRAINT",
                           Level.SEVERE, new ObjectFactory().createImplementation( impl ),
                           "implementationPropertyJavaGetterMethodNameConstraint", impl.getIdentifier(),
                           validationContext.getModuleOfImplementation( impl.getIdentifier() ).getName(), p.getName(),
                           message != null && message.length() > 0 ? " " + message : "" );

            }

            try
            {
                p.getJavaSetterMethodName();
            }
            catch ( final ModelObjectException e )
            {
                final String message = getMessage( e );

                if ( validationContext.getModelContext().isLoggable( Level.FINE ) )
                {
                    validationContext.getModelContext().log( Level.FINE, message, e );
                }

                addDetail( validationContext.getReport(),
                           "IMPLEMENTATION_PROPERTY_JAVA_SETTER_METHOD_NAME_CONSTRAINT",
                           Level.SEVERE, new ObjectFactory().createImplementation( impl ),
                           "implementationPropertyJavaSetterMethodNameConstraint", impl.getIdentifier(),
                           validationContext.getModuleOfImplementation( impl.getIdentifier() ).getName(), p.getName(),
                           message != null && message.length() > 0 ? " " + message : "" );

            }

            try
            {
                p.getJavaTypeName();
            }
            catch ( final ModelObjectException e )
            {
                final String message = getMessage( e );

                if ( validationContext.getModelContext().isLoggable( Level.FINE ) )
                {
                    validationContext.getModelContext().log( Level.FINE, message, e );
                }

                addDetail( validationContext.getReport(),
                           "IMPLEMENTATION_PROPERTY_JAVA_TYPE_NAME_CONSTRAINT",
                           Level.SEVERE, new ObjectFactory().createImplementation( impl ),
                           "implementationPropertyJavaTypeNameConstraint", impl.getIdentifier(),
                           validationContext.getModuleOfImplementation( impl.getIdentifier() ).getName(), p.getName(),
                           message != null && message.length() > 0 ? " " + message : "" );

            }

            try
            {
                p.getJavaVariableName();
            }
            catch ( final ModelObjectException e )
            {
                final String message = getMessage( e );

                if ( validationContext.getModelContext().isLoggable( Level.FINE ) )
                {
                    validationContext.getModelContext().log( Level.FINE, message, e );
                }

                addDetail( validationContext.getReport(),
                           "IMPLEMENTATION_PROPERTY_JAVA_VARIABLE_NAME_CONSTRAINT",
                           Level.SEVERE, new ObjectFactory().createImplementation( impl ),
                           "implementationPropertyJavaVariableNameConstraint", impl.getIdentifier(),
                           validationContext.getModuleOfImplementation( impl.getIdentifier() ).getName(), p.getName(),
                           message != null && message.length() > 0 ? " " + message : "" );

            }

            try
            {
                p.getJavaValue( validationContext.getModelContext().getClassLoader() );
            }
            catch ( final ModelObjectException e )
            {
                final String message = getMessage( e );

                if ( validationContext.getModelContext().isLoggable( Level.FINE ) )
                {
                    validationContext.getModelContext().log( Level.FINE, message, e );
                }

                addDetail( validationContext.getReport(),
                           "IMPLEMENTATION_PROPERTY_JAVA_VALUE_CONSTRAINT",
                           Level.SEVERE, new ObjectFactory().createImplementation( impl ),
                           "implementationPropertyJavaValueConstraint", impl.getIdentifier(),
                           validationContext.getModuleOfImplementation( impl.getIdentifier() ).getName(), p.getName(),
                           message != null && message.length() > 0 ? " " + message : "" );

            }
        }

        try ( final Stream<InheritanceModel.Node<Property>> st0 = validationContext.getInheritanceModel().
            getPropertyNodes( impl.getIdentifier(), p.getName() ).parallelStream().unordered() )
        {
            st0.forEach( effProperty  ->
            {
                final Set<InheritanceModel.Node<Property>> overriddenProperties =
                    modifiableSet( effProperty.getOverriddenNodes() );

                if ( p.isOverride() && overriddenProperties.isEmpty() )
                {
                    addDetail( validationContext.getReport(), "IMPLEMENTATION_PROPERTY_OVERRIDE_CONSTRAINT",
                               Level.SEVERE, new ObjectFactory().createImplementation( impl ),
                               "implementationPropertyOverrideConstraint", impl.getIdentifier(),
                               validationContext.getModuleOfImplementation( impl.getIdentifier() ).getName(),
                               p.getName() );

                }

                if ( !( p.isOverride() || overriddenProperties.isEmpty() ) )
                {
                    try ( final Stream<InheritanceModel.Node<Property>> st1 =
                        overriddenProperties.parallelStream().unordered() )
                    {
                        st1.forEach( overriddenProperty  ->
                        {
                            if ( overriddenProperty.getSpecification() != null )
                            {
                                final Module moduleOfProperty =
                                    validationContext.getModuleOfSpecification(
                                        overriddenProperty.getSpecification().getIdentifier() );

                                addDetail( validationContext.getReport(),
                                           "IMPLEMENTATION_PROPERTY_OVERRIDE_WARNING", Level.WARNING,
                                           new ObjectFactory().createImplementation( impl ),
                                           "implementationSpecificationPropertyOverrideWarning",
                                           impl.getIdentifier(),
                                           validationContext.getModuleOfImplementation( impl.getIdentifier() ).getName(),
                                           p.getName(), overriddenProperty.getSpecification().getIdentifier(),
                                           moduleOfProperty.getName(),
                                           getNodePathString( overriddenProperty ) );

                            }
                            else
                            {
                                Implementation overriddenImplementation =
                                    overriddenProperty.getImplementation();

                                if ( overriddenProperty.getClassDeclaration() != null )
                                {
                                    overriddenImplementation = overriddenProperty.getClassDeclaration();
                                }

                                final Module moduleOfProperty =
                                    validationContext.getModuleOfImplementation( overriddenImplementation.
                                        getIdentifier() );

                                addDetail( validationContext.getReport(),
                                           "IMPLEMENTATION_PROPERTY_OVERRIDE_WARNING", Level.WARNING,
                                           new ObjectFactory().createImplementation( impl ),
                                           "implementationPropertyOverrideWarning", impl.getIdentifier(),
                                           validationContext.getModuleOfImplementation( impl.getIdentifier() ).getName(),
                                           p.getName(), overriddenImplementation.getIdentifier(),
                                           moduleOfProperty.getName(), getNodePathString( overriddenProperty ) );

                            }
                        } );
                    }
                }

                retainFinalNodes( overriddenProperties );

                try ( final Stream<InheritanceModel.Node<Property>> st1 =
                    overriddenProperties.parallelStream().unordered() )
                {
                    st1.forEach( overriddenProperty  ->
                    {
                        Implementation overriddenImplementation = overriddenProperty.getImplementation();
                        if ( overriddenProperty.getClassDeclaration() != null )
                        {
                            overriddenImplementation = overriddenProperty.getClassDeclaration();
                        }

                        final Module moduleOfProperty =
                            validationContext.getModuleOfImplementation( overriddenImplementation.getIdentifier() );

                        addDetail( validationContext.getReport(),
                                   "IMPLEMENTATION_PROPERTY_INHERITANCE_CONSTRAINT",
                                   Level.SEVERE, new ObjectFactory().createImplementation( impl ),
                                   "implementationPropertyFinalConstraint", impl.getIdentifier(),
                                   validationContext.getModuleOfImplementation( impl.getIdentifier() ).getName(),
                                   p.getName(), overriddenImplementation.getIdentifier(),
                                   moduleOfProperty.getName(), getNodePathString( overriddenProperty ) );

                    } );
                }
            } );
        }
    }

    private static void assertValidPropertyReference( final ValidationContext validationContext,
                                                      final Implementation impl,
                                                      final PropertyReference r )
    {
        try ( final Stream<InheritanceModel.Node<Property>> st0 = validationContext.getInheritanceModel().
            getPropertyNodes( impl.getIdentifier(), r.getName() ).parallelStream().unordered() )
        {
            st0.forEach( effProperty  ->
            {
                final Set<InheritanceModel.Node<Property>> overriddenProperties =
                    modifiableSet( effProperty.getOverriddenNodes() );

                if ( r.isOverride() && overriddenProperties.isEmpty() )
                {
                    addDetail( validationContext.getReport(), "IMPLEMENTATION_PROPERTY_OVERRIDE_CONSTRAINT",
                               Level.SEVERE, new ObjectFactory().createImplementation( impl ),
                               "implementationPropertyOverrideConstraint", impl.getIdentifier(),
                               validationContext.getModuleOfImplementation( impl.getIdentifier() ).getName(),
                               r.getName() );

                }

                if ( !( r.isOverride() || overriddenProperties.isEmpty() ) )
                {
                    try ( final Stream<InheritanceModel.Node<Property>> st1 =
                        overriddenProperties.parallelStream().unordered() )
                    {
                        st1.forEach( overriddenProperty  ->
                        {
                            Implementation overriddenImplementation = overriddenProperty.getImplementation();
                            if ( overriddenProperty.getClassDeclaration() != null )
                            {
                                overriddenImplementation = overriddenProperty.getClassDeclaration();
                            }

                            final Module moduleOfProperty =
                                validationContext.getModuleOfImplementation( overriddenImplementation.getIdentifier() );

                            addDetail( validationContext.getReport(),
                                       "IMPLEMENTATION_PROPERTY_OVERRIDE_WARNING", Level.WARNING,
                                       new ObjectFactory().createImplementation( impl ),
                                       "implementationPropertyOverrideWarning", impl.getIdentifier(),
                                       validationContext.getModuleOfImplementation( impl.getIdentifier() ).getName(),
                                       r.getName(), overriddenImplementation.getIdentifier(),
                                       moduleOfProperty.getName(), getNodePathString( overriddenProperty ) );
                        } );
                    }
                }

                retainFinalNodes( overriddenProperties );

                try ( final Stream<InheritanceModel.Node<Property>> st1 = overriddenProperties.parallelStream() )
                {
                    st1.forEach( overriddenProperty  ->
                    {
                        Implementation overriddenImplementation = overriddenProperty.getImplementation();
                        if ( overriddenProperty.getClassDeclaration() != null )
                        {
                            overriddenImplementation = overriddenProperty.getClassDeclaration();
                        }

                        final Module moduleOfProperty =
                            validationContext.getModuleOfImplementation( overriddenImplementation.getIdentifier() );

                        addDetail( validationContext.getReport(),
                                   "IMPLEMENTATION_PROPERTY_INHERITANCE_CONSTRAINT",
                                   Level.SEVERE, new ObjectFactory().createImplementation( impl ),
                                   "implementationPropertyFinalConstraint", impl.getIdentifier(),
                                   validationContext.getModuleOfImplementation( impl.getIdentifier() ).getName(),
                                   r.getName(), overriddenImplementation.getIdentifier(), moduleOfProperty.getName(),
                                   getNodePathString( overriddenProperty ) );

                    } );
                }
            } );
        }
    }

    private static void assertValidSpecificationReference( final ValidationContext validationContext,
                                                           final Implementation impl,
                                                           final SpecificationReference r )
    {
        try ( final Stream<InheritanceModel.Node<SpecificationReference>> st0 = validationContext.getInheritanceModel().
            getSpecificationReferenceNodes( impl.getIdentifier(), r.getIdentifier() ).parallelStream().unordered() )
        {
            st0.forEach( effReference  ->
            {
                final Set<InheritanceModel.Node<SpecificationReference>> overriddenReferences =
                    modifiableSet( effReference.getOverriddenNodes() );

                if ( r.isOverride() && overriddenReferences.isEmpty() )
                {
                    addDetail( validationContext.getReport(),
                               "IMPLEMENTATION_SPECIFICATION_OVERRIDE_CONSTRAINT", Level.SEVERE,
                               new ObjectFactory().createImplementation( impl ),
                               "implementationSpecificationOverrideConstraint", impl.getIdentifier(),
                               validationContext.getModuleOfImplementation( impl.getIdentifier() ).getName(),
                               r.getIdentifier() );

                }

                if ( !( r.isOverride() || overriddenReferences.isEmpty() ) )
                {
                    try ( final Stream<InheritanceModel.Node<SpecificationReference>> st1 =
                        overriddenReferences.parallelStream().unordered() )
                    {
                        st1.forEach( overriddenReference  ->
                        {
                            Implementation overriddenImplementation = overriddenReference.getImplementation();
                            if ( overriddenReference.getClassDeclaration() != null )
                            {
                                overriddenImplementation = overriddenReference.getClassDeclaration();
                            }

                            final Module moduleOfReference =
                                validationContext.getModuleOfImplementation( overriddenImplementation.getIdentifier() );

                            addDetail( validationContext.getReport(),
                                       "IMPLEMENTATION_SPECIFICATION_REFERENCE_OVERRIDE_WARNING",
                                       Level.WARNING, new ObjectFactory().createImplementation( impl ),
                                       "implementationSpecificationOverrideWarning", impl.getIdentifier(),
                                       validationContext.getModuleOfImplementation( impl.getIdentifier() ).getName(),
                                       r.getIdentifier(), overriddenImplementation.getIdentifier(),
                                       moduleOfReference.getName(), getNodePathString( overriddenReference ) );

                        } );
                    }
                }

                retainFinalNodes( overriddenReferences );

                try ( final Stream<InheritanceModel.Node<SpecificationReference>> st1 =
                    overriddenReferences.parallelStream().unordered() )
                {
                    st1.forEach( overriddenReference  ->
                    {
                        Implementation overriddenImplementation = overriddenReference.getImplementation();
                        if ( overriddenReference.getClassDeclaration() != null )
                        {
                            overriddenImplementation = overriddenReference.getClassDeclaration();
                        }

                        final Module moduleOfReference =
                            validationContext.getModuleOfImplementation( overriddenImplementation.getIdentifier() );

                        addDetail( validationContext.getReport(),
                                   "IMPLEMENTATION_SPECIFICATION_INHERITANCE_CONSTRAINT", Level.SEVERE,
                                   new ObjectFactory().createImplementation( impl ),
                                   "implementationSpecificationFinalConstraint", impl.getIdentifier(),
                                   validationContext.getModuleOfImplementation( impl.getIdentifier() ).getName(),
                                   r.getIdentifier(), overriddenImplementation.getIdentifier(),
                                   moduleOfReference.getName(), getNodePathString( overriddenReference ) );

                    } );
                }
            } );
        }
    }

    private static void assertValidAnyObject( final ValidationContext validationContext,
                                              final Implementation impl,
                                              final Object any )
    {

        if ( any instanceof JAXBElement<?> )
        {
            final JAXBElement<?> jaxbElement = (JAXBElement<?>) any;
            final boolean overrideNode = jaxbElement.getValue() instanceof Inheritable
                                             ? ( (Inheritable) jaxbElement.getValue() ).isOverride()
                                             : false;

            try ( final Stream<InheritanceModel.Node<JAXBElement<?>>> st0 = validationContext.getInheritanceModel().
                getJaxbElementNodes( impl.getIdentifier(), jaxbElement.getName() ).parallelStream().unordered() )
            {
                st0.forEach( effElement  ->
                {
                    final Set<InheritanceModel.Node<JAXBElement<?>>> overriddenElements =
                        modifiableSet( effElement.getOverriddenNodes() );

                    if ( overrideNode && overriddenElements.isEmpty() )
                    {
                        addDetail( validationContext.getReport(),
                                   "IMPLEMENTATION_JAXB_ELEMENT_OVERRIDE_CONSTRAINT",
                                   Level.SEVERE, new ObjectFactory().createImplementation( impl ),
                                   "implementationJaxbElementOverrideConstraint", impl.getIdentifier(),
                                   validationContext.getModuleOfImplementation( impl.getIdentifier() ).getName(),
                                   jaxbElement.getName().toString() );

                    }

                    if ( !( overrideNode || overriddenElements.isEmpty() ) )
                    {
                        try ( final Stream<InheritanceModel.Node<JAXBElement<?>>> st1 =
                            overriddenElements.parallelStream().unordered() )
                        {
                            st1.forEach( overriddenElement  ->
                            {
                                Implementation overriddenImplementation = overriddenElement.getImplementation();
                                if ( overriddenElement.getClassDeclaration() != null )
                                {
                                    overriddenImplementation = overriddenElement.getClassDeclaration();
                                }

                                final Module moduleOfElement = validationContext.getModuleOfImplementation(
                                    overriddenElement.getImplementation().getIdentifier() );

                                addDetail( validationContext.getReport(),
                                           "IMPLEMENTATION_JAXB_ELEMENT_OVERRIDE_WARNING",
                                           Level.WARNING, new ObjectFactory().createImplementation( impl ),
                                           "implementationJaxbElementOverrideWarning", impl.getIdentifier(),
                                           validationContext.getModuleOfImplementation( impl.getIdentifier() ).getName(),
                                           jaxbElement.getName().toString(), overriddenImplementation.getIdentifier(),
                                           moduleOfElement.getName(), getNodePathString( overriddenElement ) );

                            } );
                        }
                    }

                    retainFinalNodes( overriddenElements );

                    try ( final Stream<InheritanceModel.Node<JAXBElement<?>>> st1 =
                        overriddenElements.parallelStream().unordered() )
                    {
                        st1.forEach( overriddenElement  ->
                        {
                            Implementation overriddenImplementation = overriddenElement.getImplementation();
                            if ( overriddenElement.getClassDeclaration() != null )
                            {
                                overriddenImplementation = overriddenElement.getClassDeclaration();
                            }

                            final Module moduleOfElement =
                                validationContext.getModuleOfImplementation( overriddenImplementation.getIdentifier() );

                            addDetail( validationContext.getReport(),
                                       "IMPLEMENTATION_JAXB_ELEMENT_INHERITANCE_CONSTRAINT",
                                       Level.SEVERE, new ObjectFactory().createImplementation( impl ),
                                       "implementationJaxbElementFinalConstraint", impl.getIdentifier(),
                                       validationContext.getModuleOfImplementation( impl.getIdentifier() ).getName(),
                                       jaxbElement.getName().toString(), overriddenImplementation.getIdentifier(),
                                       moduleOfElement.getName(), getNodePathString( overriddenElement ) );

                        } );
                    }
                } );
            }
        }
    }

    private static void assertUniqueDependencies( final ValidationContext validationContext,
                                                  final Implementation impl )
    {
        final Set<String> dependencyNames =
            validationContext.getInheritanceModel().getDependencyNames( impl.getIdentifier() );

        final Map<JavaIdentifier, InheritanceModel.Node<Dependency>> dependencyJavaConstantNames =
            new ConcurrentHashMap<>( dependencyNames.size() );

        final Map<JavaIdentifier, InheritanceModel.Node<Dependency>> dependencyJavaGetterMethodNames =
            new ConcurrentHashMap<>( dependencyNames.size() );

        final Map<JavaIdentifier, InheritanceModel.Node<Dependency>> dependencyJavaSetterMethodNames =
            new ConcurrentHashMap<>( dependencyNames.size() );

        final Map<JavaIdentifier, InheritanceModel.Node<Dependency>> dependencyJavaVariableNames =
            new ConcurrentHashMap<>( dependencyNames.size() );

        try ( final Stream<String> st0 = dependencyNames.parallelStream().unordered() )
        {
            st0.forEach( dependencyName  ->
            {
                final Set<InheritanceModel.Node<Dependency>> dependencyNodes =
                    validationContext.getInheritanceModel().getDependencyNodes( impl.getIdentifier(), dependencyName );

                if ( dependencyNodes.size() > 1 )
                {
                    addDetail( validationContext.getReport(),
                               "IMPLEMENTATION_DEPENDENCY_MULTIPLE_INHERITANCE_CONSTRAINT",
                               Level.SEVERE, new ObjectFactory().createImplementation( impl ),
                               "implementationMultipleInheritanceDependencyConstraint", impl.getIdentifier(),
                               validationContext.getModuleOfImplementation( impl.getIdentifier() ).getName(),
                               dependencyName, getNodeListPathString( dependencyNodes ) );

                }

                if ( validationContext.isValidateJava() )
                {
                    try ( final Stream<InheritanceModel.Node<Dependency>> st1 =
                        dependencyNodes.parallelStream().unordered() )
                    {
                        st1.forEach( node  ->
                        {
                            try
                            {
                                final JavaIdentifier javaIdentifier = node.getModelObject().getJavaConstantName();
                                final InheritanceModel.Node<Dependency> existingNode =
                                    dependencyJavaConstantNames.putIfAbsent( javaIdentifier, node );

                                if ( existingNode != null )
                                {
                                    addDetail( validationContext.getReport(),
                                               "IMPLEMENTATION_DEPENDENCY_JAVA_CONSTANT_NAME_UNIQUENESS_CONSTRAINT",
                                               Level.SEVERE, new ObjectFactory().createImplementation( impl ),
                                               "implementationDependencyJavaConstantNameUniquenessConstraint",
                                               impl.getIdentifier(), validationContext.getModuleOfImplementation(
                                               impl.getIdentifier() ).getName(), dependencyName,
                                               getNodePathString( node ), existingNode.getModelObject().getName(),
                                               getNodePathString( existingNode ), javaIdentifier );

                                }
                            }
                            catch ( final ModelObjectException e )
                            {
                                // Validated elsewhere.
                            }

                            try
                            {
                                final JavaIdentifier javaIdentifier = node.getModelObject().getJavaGetterMethodName();
                                final InheritanceModel.Node<Dependency> existingNode =
                                    dependencyJavaGetterMethodNames.putIfAbsent( javaIdentifier, node );

                                if ( existingNode != null )
                                {
                                    addDetail( validationContext.getReport(),
                                               "IMPLEMENTATION_DEPENDENCY_JAVA_GETTER_METHOD_NAME_UNIQUENESS_CONSTRAINT",
                                               Level.SEVERE, new ObjectFactory().createImplementation( impl ),
                                               "implementationDependencyJavaGetterMethodNameUniquenessConstraint",
                                               impl.getIdentifier(), validationContext.getModuleOfImplementation(
                                               impl.getIdentifier() ).getName(), dependencyName,
                                               getNodePathString( node ), existingNode.getModelObject().getName(),
                                               getNodePathString( existingNode ), javaIdentifier );

                                }
                            }
                            catch ( final ModelObjectException e )
                            {
                                // Validated elsewhere.
                            }

                            try
                            {
                                final JavaIdentifier javaIdentifier = node.getModelObject().getJavaSetterMethodName();
                                final InheritanceModel.Node<Dependency> existingNode =
                                    dependencyJavaSetterMethodNames.putIfAbsent( javaIdentifier, node );

                                if ( existingNode != null )
                                {
                                    addDetail( validationContext.getReport(),
                                               "IMPLEMENTATION_DEPENDENCY_JAVA_SETTER_METHOD_NAME_UNIQUENESS_CONSTRAINT",
                                               Level.SEVERE, new ObjectFactory().createImplementation( impl ),
                                               "implementationDependencyJavaSetterMethodNameUniquenessConstraint",
                                               impl.getIdentifier(), validationContext.getModuleOfImplementation(
                                               impl.getIdentifier() ).getName(), dependencyName,
                                               getNodePathString( node ), existingNode.getModelObject().getName(),
                                               getNodePathString( existingNode ), javaIdentifier );

                                }
                            }
                            catch ( final ModelObjectException e )
                            {
                                // Validated elsewhere.
                            }

                            try
                            {
                                final JavaIdentifier javaIdentifier = node.getModelObject().getJavaSetterMethodName();
                                final InheritanceModel.Node<Dependency> existingNode =
                                    dependencyJavaVariableNames.putIfAbsent( javaIdentifier, node );

                                if ( existingNode != null )
                                {
                                    addDetail( validationContext.getReport(),
                                               "IMPLEMENTATION_DEPENDENCY_JAVA_VARIABLE_NAME_UNIQUENESS_CONSTRAINT",
                                               Level.SEVERE, new ObjectFactory().createImplementation( impl ),
                                               "implementationDependencyJavaVariableNameUniquenessConstraint",
                                               impl.getIdentifier(), validationContext.getModuleOfImplementation(
                                               impl.getIdentifier() ).getName(), dependencyName,
                                               getNodePathString( node ), existingNode.getModelObject().getName(),
                                               getNodePathString( existingNode ), javaIdentifier );

                                }
                            }
                            catch ( final ModelObjectException e )
                            {
                                // Validated elsewhere.
                            }
                        } );
                    }
                }
            } );
        }
    }

    private static void assertUniqueMessages( final ValidationContext validationContext,
                                              final Implementation impl )
    {
        final Set<String> messageNames =
            validationContext.getInheritanceModel().getMessageNames( impl.getIdentifier() );

        final Map<JavaIdentifier, InheritanceModel.Node<Message>> messageJavaConstantNames =
            new ConcurrentHashMap<>( messageNames.size() );

        final Map<JavaIdentifier, InheritanceModel.Node<Message>> messageJavaGetterMethodNames =
            new ConcurrentHashMap<>( messageNames.size() );

        final Map<JavaIdentifier, InheritanceModel.Node<Message>> messageJavaSetterMethodNames =
            new ConcurrentHashMap<>( messageNames.size() );

        final Map<JavaIdentifier, InheritanceModel.Node<Message>> messageJavaVariableNames =
            new ConcurrentHashMap<>( messageNames.size() );

        try ( final Stream<String> st0 = messageNames.parallelStream().unordered() )
        {
            st0.forEach( messageName  ->
            {
                final Set<InheritanceModel.Node<Message>> messageNodes =
                    validationContext.getInheritanceModel().getMessageNodes( impl.getIdentifier(), messageName );

                if ( messageNodes.size() > 1 )
                {
                    addDetail( validationContext.getReport(),
                               "IMPLEMENTATION_MESSAGE_MULTIPLE_INHERITANCE_CONSTRAINT", Level.SEVERE,
                               new ObjectFactory().createImplementation( impl ),
                               "implementationMultipleInheritanceMessageConstraint", impl.getIdentifier(),
                               validationContext.getModuleOfImplementation( impl.getIdentifier() ).getName(),
                               messageName, getNodeListPathString( messageNodes ) );

                }

                if ( validationContext.isValidateJava() )
                {
                    try ( final Stream<InheritanceModel.Node<Message>> st1 = messageNodes.parallelStream().unordered() )
                    {
                        st1.forEach( node  ->
                        {
                            try
                            {
                                final JavaIdentifier javaIdentifier = node.getModelObject().getJavaConstantName();
                                final InheritanceModel.Node<Message> existingNode =
                                    messageJavaConstantNames.putIfAbsent( javaIdentifier, node );

                                if ( existingNode != null )
                                {
                                    addDetail( validationContext.getReport(),
                                               "IMPLEMENTATION_MESSAGE_JAVA_CONSTANT_NAME_UNIQUENESS_CONSTRAINT",
                                               Level.SEVERE, new ObjectFactory().createImplementation( impl ),
                                               "implementationMessageJavaConstantNameUniquenessConstraint",
                                               impl.getIdentifier(), validationContext.getModuleOfImplementation(
                                               impl.getIdentifier() ).getName(), messageName, getNodePathString(
                                               node ), existingNode.getModelObject().getName(),
                                               getNodePathString( existingNode ), javaIdentifier );

                                }
                            }
                            catch ( final ModelObjectException e )
                            {
                                // Validated elsewhere.
                            }

                            try
                            {
                                final JavaIdentifier javaIdentifier = node.getModelObject().getJavaGetterMethodName();
                                final InheritanceModel.Node<Message> existingNode =
                                    messageJavaGetterMethodNames.putIfAbsent( javaIdentifier, node );

                                if ( existingNode != null )
                                {
                                    addDetail( validationContext.getReport(),
                                               "IMPLEMENTATION_MESSAGE_JAVA_GETTER_METHOD_NAME_UNIQUENESS_CONSTRAINT",
                                               Level.SEVERE, new ObjectFactory().createImplementation( impl ),
                                               "implementationMessageJavaGetterMethodNameUniquenessConstraint",
                                               impl.getIdentifier(), validationContext.getModuleOfImplementation(
                                               impl.getIdentifier() ).getName(), messageName, getNodePathString(
                                               node ), existingNode.getModelObject().getName(), getNodePathString(
                                               existingNode ), javaIdentifier );

                                }
                            }
                            catch ( final ModelObjectException e )
                            {
                                // Validated elsewhere.
                            }

                            try
                            {
                                final JavaIdentifier javaIdentifier = node.getModelObject().getJavaSetterMethodName();
                                final InheritanceModel.Node<Message> existingNode =
                                    messageJavaSetterMethodNames.putIfAbsent( javaIdentifier, node );

                                if ( existingNode != null )
                                {
                                    addDetail( validationContext.getReport(),
                                               "IMPLEMENTATION_MESSAGE_JAVA_SETTER_METHOD_NAME_UNIQUENESS_CONSTRAINT",
                                               Level.SEVERE, new ObjectFactory().createImplementation( impl ),
                                               "implementationMessageJavaSetterMethodNameUniquenessConstraint",
                                               impl.getIdentifier(), validationContext.getModuleOfImplementation(
                                               impl.getIdentifier() ).getName(), messageName, getNodePathString(
                                               node ), existingNode.getModelObject().getName(), getNodePathString(
                                               existingNode ), javaIdentifier );

                                }
                            }
                            catch ( final ModelObjectException e )
                            {
                                // Validated elsewhere.
                            }

                            try
                            {
                                final JavaIdentifier javaIdentifier = node.getModelObject().getJavaSetterMethodName();
                                final InheritanceModel.Node<Message> existingNode =
                                    messageJavaVariableNames.putIfAbsent( javaIdentifier, node );

                                if ( existingNode != null )
                                {
                                    addDetail( validationContext.getReport(),
                                               "IMPLEMENTATION_MESSAGE_JAVA_VARIABLE_NAME_UNIQUENESS_CONSTRAINT",
                                               Level.SEVERE, new ObjectFactory().createImplementation( impl ),
                                               "implementationMessageJavaVariableNameUniquenessConstraint",
                                               impl.getIdentifier(), validationContext.getModuleOfImplementation(
                                               impl.getIdentifier() ).getName(), messageName, getNodePathString(
                                               node ), existingNode.getModelObject().getName(), getNodePathString(
                                               existingNode ), javaIdentifier );

                                }
                            }
                            catch ( final ModelObjectException e )
                            {
                                // Validated elsewhere.
                            }
                        } );
                    }
                }
            } );
        }
    }

    private static void assertUniqueProperties( final ValidationContext validationContext,
                                                final Implementation impl )
    {
        final Set<String> propertyNames =
            validationContext.getInheritanceModel().getPropertyNames( impl.getIdentifier() );

        final Map<JavaIdentifier, InheritanceModel.Node<Property>> propertyJavaConstantNames =
            new ConcurrentHashMap<>( propertyNames.size() );

        final Map<JavaIdentifier, InheritanceModel.Node<Property>> propertyJavaGetterMethodNames =
            new ConcurrentHashMap<>( propertyNames.size() );

        final Map<JavaIdentifier, InheritanceModel.Node<Property>> propertyJavaSetterMethodNames =
            new ConcurrentHashMap<>( propertyNames.size() );

        final Map<JavaIdentifier, InheritanceModel.Node<Property>> propertyJavaVariableNames =
            new ConcurrentHashMap<>( propertyNames.size() );

        try ( final Stream<String> st0 = propertyNames.parallelStream().unordered() )
        {
            st0.forEach( propertyName  ->
            {
                final Set<InheritanceModel.Node<Property>> propertyNodes =
                    validationContext.getInheritanceModel().getPropertyNodes( impl.getIdentifier(), propertyName );

                if ( propertyNodes.size() > 1 )
                {
                    addDetail( validationContext.getReport(),
                               "IMPLEMENTATION_PROPERTY_MULTIPLE_INHERITANCE_CONSTRAINT", Level.SEVERE,
                               new ObjectFactory().createImplementation( impl ),
                               "implementationMultipleInheritancePropertyConstraint", impl.getIdentifier(),
                               validationContext.getModuleOfImplementation( impl.getIdentifier() ).getName(),
                               propertyName, getNodeListPathString( propertyNodes ) );

                }

                if ( validationContext.isValidateJava() )
                {
                    try ( final Stream<InheritanceModel.Node<Property>> st1 =
                        propertyNodes.parallelStream().unordered() )
                    {
                        st1.forEach( node  ->
                        {
                            try
                            {
                                final JavaIdentifier javaIdentifier = node.getModelObject().getJavaConstantName();
                                final InheritanceModel.Node<Property> existingNode =
                                    propertyJavaConstantNames.putIfAbsent( javaIdentifier, node );

                                if ( existingNode != null )
                                {
                                    addDetail( validationContext.getReport(),
                                               "IMPLEMENTATION_PROPERTY_JAVA_CONSTANT_NAME_UNIQUENESS_CONSTRAINT",
                                               Level.SEVERE, new ObjectFactory().createImplementation( impl ),
                                               "implementationPropertyJavaConstantNameUniquenessConstraint",
                                               impl.getIdentifier(), validationContext.getModuleOfImplementation(
                                               impl.getIdentifier() ).getName(), propertyName,
                                               getNodePathString( node ), existingNode.getModelObject().
                                               getName(), getNodePathString( existingNode ), javaIdentifier );

                                }
                            }
                            catch ( final ModelObjectException e )
                            {
                                // Validated elsewhere.
                            }

                            try
                            {
                                final JavaIdentifier javaIdentifier = node.getModelObject().getJavaGetterMethodName();
                                final InheritanceModel.Node<Property> existingNode =
                                    propertyJavaGetterMethodNames.putIfAbsent( javaIdentifier, node );

                                if ( existingNode != null )
                                {
                                    addDetail( validationContext.getReport(),
                                               "IMPLEMENTATION_PROPERTY_JAVA_GETTER_METHOD_NAME_UNIQUENESS_CONSTRAINT",
                                               Level.SEVERE, new ObjectFactory().createImplementation( impl ),
                                               "implementationPropertyJavaGetterMethodNameUniquenessConstraint",
                                               impl.getIdentifier(), validationContext.getModuleOfImplementation(
                                               impl.getIdentifier() ).getName(), propertyName,
                                               getNodePathString( node ), existingNode.getModelObject().
                                               getName(), getNodePathString( existingNode ), javaIdentifier );

                                }
                            }
                            catch ( final ModelObjectException e )
                            {
                                // Validated elsewhere.
                            }

                            try
                            {
                                final JavaIdentifier javaIdentifier = node.getModelObject().getJavaSetterMethodName();
                                final InheritanceModel.Node<Property> existingNode =
                                    propertyJavaSetterMethodNames.putIfAbsent( javaIdentifier, node );

                                if ( existingNode != null )
                                {
                                    addDetail( validationContext.getReport(),
                                               "IMPLEMENTATION_PROPERTY_JAVA_SETTER_METHOD_NAME_UNIQUENESS_CONSTRAINT",
                                               Level.SEVERE, new ObjectFactory().createImplementation( impl ),
                                               "implementationPropertyJavaSetterMethodNameUniquenessConstraint",
                                               impl.getIdentifier(), validationContext.getModuleOfImplementation(
                                               impl.getIdentifier() ).getName(), propertyName,
                                               getNodePathString( node ), existingNode.getModelObject().
                                               getName(), getNodePathString( existingNode ), javaIdentifier );

                                }
                            }
                            catch ( final ModelObjectException e )
                            {
                                // Validated elsewhere.
                            }

                            try
                            {
                                final JavaIdentifier javaIdentifier = node.getModelObject().getJavaSetterMethodName();
                                final InheritanceModel.Node<Property> existingNode =
                                    propertyJavaVariableNames.putIfAbsent( javaIdentifier, node );

                                if ( existingNode != null )
                                {
                                    addDetail( validationContext.getReport(),
                                               "IMPLEMENTATION_PROPERTY_JAVA_VARIABLE_NAME_UNIQUENESS_CONSTRAINT",
                                               Level.SEVERE, new ObjectFactory().createImplementation( impl ),
                                               "implementationPropertyJavaVariableNameUniquenessConstraint",
                                               impl.getIdentifier(), validationContext.getModuleOfImplementation(
                                               impl.getIdentifier() ).getName(), propertyName,
                                               getNodePathString( node ), existingNode.getModelObject().getName(),
                                               getNodePathString( existingNode ), javaIdentifier );

                                }
                            }
                            catch ( final ModelObjectException e )
                            {
                                // Validated elsewhere.
                            }
                        } );
                    }
                }
            } );
        }
    }

    private static void assertSpecificationsValid( final ValidationContext validationContext )
    {
        final Specifications specifications = validationContext.getAllSpecifications();
        final Map<String, Specification> specificationClassDeclarations =
            new ConcurrentHashMap<String, Specification>();

        final Map<String, Specification> specificationJavaClassDeclarations =
            new ConcurrentHashMap<String, Specification>();

        if ( specifications != null )
        {
            try ( final Stream<Specification> st0 = specifications.getSpecification().parallelStream().unordered() )
            {
                st0.forEach( s  ->
                {
                    final Implementations impls = validationContext.getImplementations( s.getIdentifier() );
                    final Module moduleOfS = validationContext.getModuleOfSpecification( s.getIdentifier() );

                    if ( validationContext.isValidateJava() )
                    {
                        try
                        {
                            s.getJavaTypeName();
                        }
                        catch ( final ModelObjectException e )
                        {
                            final String message = getMessage( e );

                            if ( validationContext.getModelContext().isLoggable( Level.FINE ) )
                            {
                                validationContext.getModelContext().log( Level.FINE, message, e );
                            }

                            addDetail( validationContext.getReport(),
                                       "SPECIFICATION_JAVA_TYPE_NAME_CONSTRAINT",
                                       Level.SEVERE, new ObjectFactory().createSpecification( s ),
                                       "specificationJavaTypeNameConstraint", s.getIdentifier(),
                                       moduleOfS.getName(), s.getClazz(),
                                       message != null && message.length() > 0 ? " " + message : "" );

                        }
                    }

                    if ( s.isClassDeclaration() )
                    {
                        if ( s.getClazz() == null )
                        {
                            addDetail( validationContext.getReport(), "SPECIFICATION_CLASS_CONSTRAINT", Level.SEVERE,
                                       new ObjectFactory().createSpecification( s ), "specificationClassConstraint",
                                       s.getIdentifier(), moduleOfS.getName() );

                        }
                        else
                        {
                            final Specification prev = specificationClassDeclarations.putIfAbsent( s.getClazz(), s );
                            if ( prev != null && !prev.getIdentifier().equals( s.getIdentifier() ) )
                            {
                                final Module moduleOfPrev =
                                    validationContext.getModuleOfSpecification( prev.getIdentifier() );

                                addDetail( validationContext.getReport(),
                                           "SPECIFICATION_CLASS_DECLARATION_CONSTRAINT",
                                           Level.SEVERE, new ObjectFactory().createSpecification( s ),
                                           "specificationClassDeclarationConstraint", s.getIdentifier(),
                                           moduleOfS.getName(), s.getClazz(), prev.getIdentifier(),
                                           moduleOfPrev.getName() );

                            }

                            try
                            {
                                if ( validationContext.isValidateJava() && s.getJavaTypeName().isPresent() )
                                {
                                    final Specification java = specificationJavaClassDeclarations.putIfAbsent(
                                        s.getJavaTypeName().get().getClassName(), s );

                                    if ( java != null && !java.getIdentifier().equals( s.getIdentifier() ) )
                                    {
                                        final Module moduleOfJava =
                                            validationContext.getModuleOfSpecification( java.getIdentifier() );

                                        addDetail( validationContext.getReport(),
                                                   "SPECIFICATION_JAVA_CLASS_DECLARATION_CONSTRAINT",
                                                   Level.SEVERE, new ObjectFactory().createSpecification( s ),
                                                   "specificationJavaClassDeclarationConstraint", s.getIdentifier(),
                                                   moduleOfS.getName(), s.getJavaTypeName().get().getClassName(),
                                                   java.getIdentifier(), moduleOfJava.getName() );

                                    }
                                }
                            }
                            catch ( final ModelObjectException e )
                            {
                                // Already validated above.
                            }
                        }
                    }

                    if ( impls != null )
                    {
                        final Map<String, Collection<Implementation>> map = new ConcurrentHashMap<>();

                        try ( final Stream<Implementation> st1 =
                            impls.getImplementation().parallelStream().unordered() )
                        {
                            st1.forEach( impl  ->
                            {
                                final Collection<Implementation> implementations = map.computeIfAbsent(
                                    impl.getName(), name  -> new CopyOnWriteArrayList<>() );

                                implementations.add( impl );
                            } );
                        }

                        try ( final Stream<Map.Entry<String, Collection<Implementation>>> st1 =
                            map.entrySet().parallelStream().unordered() )
                        {
                            st1.filter( e  -> e.getValue().size() > 1 ).
                                forEach( e  ->
                                {
                                    try ( final Stream<Implementation> st2 = e.getValue().parallelStream().unordered() )
                                    {
                                        st2.forEach( impl  ->
                                        {
                                            final Module moduleOfImpl =
                                                validationContext.getModuleOfImplementation( impl.getIdentifier() );

                                            addDetail( validationContext.getReport(),
                                                       "SPECIFICATION_IMPLEMENTATION_NAME_UNIQUENESS_CONSTRAINT",
                                                       Level.SEVERE, new ObjectFactory().createImplementation( impl ),
                                                       "specificationImplementationNameConstraint",
                                                       impl.getIdentifier(), moduleOfImpl.getName(), s.getIdentifier(),
                                                       moduleOfS.getName(), impl.getName() );

                                        } );
                                    }
                                } );
                        }

                        if ( s.getMultiplicity() == Multiplicity.ONE && impls.getImplementation().size() > 1 )
                        {
                            try ( final Stream<Implementation> st1 =
                                impls.getImplementation().parallelStream().unordered() )
                            {
                                st1.forEach( impl  ->
                                {
                                    final Module moduleOfImpl =
                                        validationContext.getModuleOfImplementation( impl.getIdentifier() );

                                    addDetail( validationContext.getReport(),
                                               "SPECIFICATION_IMPLEMENTATION_MULTIPLICITY_CONSTRAINT", Level.SEVERE,
                                               new ObjectFactory().createImplementation( impl ),
                                               "specificationMultiplicityConstraint", impl.getIdentifier(),
                                               moduleOfImpl.getName(), s.getIdentifier(), moduleOfS.getName(),
                                               s.getMultiplicity() );

                                } );
                            }
                        }
                    }

                    if ( s.getProperties() != null )
                    {
                        try ( final Stream<Property> st1 =
                            s.getProperties().getProperty().parallelStream().unordered() )
                        {
                            st1.forEach( p  ->
                            {
                                if ( p.getValue() != null && p.getAny() != null )
                                {
                                    addDetail( validationContext.getReport(), "SPECIFICATION_PROPERTY_VALUE_CONSTRAINT",
                                               Level.SEVERE, new ObjectFactory().createSpecification( s ),
                                               "specificationPropertyValueConstraint", s.getIdentifier(),
                                               moduleOfS.getName(), p.getName() );

                                }

                                if ( p.getAny() != null && p.getType() == null )
                                {
                                    addDetail( validationContext.getReport(), "SPECIFICATION_PROPERTY_TYPE_CONSTRAINT",
                                               Level.SEVERE, new ObjectFactory().createSpecification( s ),
                                               "specificationPropertyTypeConstraint", s.getIdentifier(),
                                               moduleOfS.getName(), p.getName() );

                                }

                                if ( validationContext.isValidateJava() )
                                {
                                    try
                                    {
                                        p.getJavaConstantName();
                                    }
                                    catch ( final ModelObjectException e )
                                    {
                                        final String message = getMessage( e );

                                        if ( validationContext.getModelContext().isLoggable( Level.FINE ) )
                                        {
                                            validationContext.getModelContext().log( Level.FINE, message, e );
                                        }

                                        addDetail( validationContext.getReport(),
                                                   "SPECIFICATION_PROPERTY_JAVA_CONSTANT_NAME_CONSTRAINT",
                                                   Level.SEVERE, new ObjectFactory().createSpecification( s ),
                                                   "specificationPropertyJavaConstantNameConstraint", s.getIdentifier(),
                                                   moduleOfS.getName(), p.getName(),
                                                   message != null && message.length() > 0 ? " " + message : "" );

                                    }

                                    try
                                    {
                                        p.getJavaGetterMethodName();
                                    }
                                    catch ( final ModelObjectException e )
                                    {
                                        final String message = getMessage( e );

                                        if ( validationContext.getModelContext().isLoggable( Level.FINE ) )
                                        {
                                            validationContext.getModelContext().log( Level.FINE, message, e );
                                        }

                                        addDetail( validationContext.getReport(),
                                                   "SPECIFICATION_PROPERTY_JAVA_GETTER_METHOD_NAME_CONSTRAINT",
                                                   Level.SEVERE, new ObjectFactory().createSpecification( s ),
                                                   "specificationPropertyJavaGetterMethodNameConstraint",
                                                   s.getIdentifier(), moduleOfS.getName(), p.getName(),
                                                   message != null && message.length() > 0 ? " " + message : "" );

                                    }

                                    try
                                    {
                                        p.getJavaSetterMethodName();
                                    }
                                    catch ( final ModelObjectException e )
                                    {
                                        final String message = getMessage( e );

                                        if ( validationContext.getModelContext().isLoggable( Level.FINE ) )
                                        {
                                            validationContext.getModelContext().log( Level.FINE, message, e );
                                        }

                                        addDetail( validationContext.getReport(),
                                                   "SPECIFICATION_PROPERTY_JAVA_SETTER_METHOD_NAME_CONSTRAINT",
                                                   Level.SEVERE, new ObjectFactory().createSpecification( s ),
                                                   "specificationPropertyJavaSetterMethodNameConstraint",
                                                   s.getIdentifier(), moduleOfS.getName(), p.getName(),
                                                   message != null && message.length() > 0 ? " " + message : "" );

                                    }

                                    try
                                    {
                                        p.getJavaTypeName();
                                    }
                                    catch ( final ModelObjectException e )
                                    {
                                        final String message = getMessage( e );

                                        if ( validationContext.getModelContext().isLoggable( Level.FINE ) )
                                        {
                                            validationContext.getModelContext().log( Level.FINE, message, e );
                                        }

                                        addDetail( validationContext.getReport(),
                                                   "SPECIFICATION_PROPERTY_JAVA_TYPE_NAME_CONSTRAINT",
                                                   Level.SEVERE, new ObjectFactory().createSpecification( s ),
                                                   "specificationPropertyJavaTypeNameConstraint", s.getIdentifier(),
                                                   moduleOfS.getName(), p.getName(),
                                                   message != null && message.length() > 0 ? " " + message : "" );

                                    }

                                    try
                                    {
                                        p.getJavaVariableName();
                                    }
                                    catch ( final ModelObjectException e )
                                    {
                                        final String message = getMessage( e );

                                        if ( validationContext.getModelContext().isLoggable( Level.FINE ) )
                                        {
                                            validationContext.getModelContext().log( Level.FINE, message, e );
                                        }

                                        addDetail( validationContext.getReport(),
                                                   "SPECIFICATION_PROPERTY_JAVA_VARIABLE_NAME_CONSTRAINT",
                                                   Level.SEVERE, new ObjectFactory().createSpecification( s ),
                                                   "specificationPropertyJavaVariableNameConstraint", s.getIdentifier(),
                                                   moduleOfS.getName(), p.getName(),
                                                   message != null && message.length() > 0 ? " " + message : "" );

                                    }

                                    try
                                    {
                                        p.getJavaValue( validationContext.getModelContext().getClassLoader() );
                                    }
                                    catch ( final ModelObjectException e )
                                    {
                                        final String message = getMessage( e );

                                        if ( validationContext.getModelContext().isLoggable( Level.FINE ) )
                                        {
                                            validationContext.getModelContext().log( Level.FINE, message, e );
                                        }

                                        addDetail( validationContext.getReport(),
                                                   "SPECIFICATION_PROPERTY_JAVA_VALUE_CONSTRAINT",
                                                   Level.SEVERE, new ObjectFactory().createSpecification( s ),
                                                   "specificationPropertyJavaValueConstraint", s.getIdentifier(),
                                                   moduleOfS.getName(), p.getName(),
                                                   message != null && message.length() > 0 ? " " + message : "" );

                                    }
                                }
                            } );
                        }

                        try ( final Stream<PropertyReference> st1 =
                            s.getProperties().getReference().parallelStream().unordered() )
                        {
                            st1.forEach( r  ->
                            {
                                addDetail( validationContext.getReport(),
                                           "SPECIFICATION_PROPERTY_REFERENCE_DECLARATION_CONSTRAINT", Level.SEVERE,
                                           new ObjectFactory().createSpecification( s ),
                                           "specificationPropertyReferenceDeclarationConstraint", s.getIdentifier(),
                                           moduleOfS.getName(), r.getName() );

                            } );
                        }
                    }
                } );
            }
        }
    }

    private static void assertDependencyValid( final ValidationContext validationContext,
                                               final Implementation implementation, final Dependency dependency )
    {
        final Specification s = validationContext.getSpecification( dependency.getIdentifier() );
        final Implementations available = validationContext.getImplementations( dependency.getIdentifier() );
        final Module moduleOfImpl =
            validationContext.getModuleOfImplementation( implementation.getIdentifier() );

        if ( !dependency.isOptional()
                 && ( available == null || available.getImplementation().isEmpty()
                      || ( dependency.getImplementationName() != null
                           && !available.getImplementationByName( dependency.getImplementationName() ).isPresent() ) ) )
        {
            addDetail( validationContext.getReport(), "IMPLEMENTATION_MANDATORY_DEPENDENCY_CONSTRAINT", Level.SEVERE,
                       new ObjectFactory().createImplementation( implementation ),
                       "implementationMandatoryDependencyConstraint", implementation.getIdentifier(),
                       moduleOfImpl.getName(), dependency.getName() );

        }

        if ( s != null )
        {
            final Module moduleOfS = validationContext.getModuleOfSpecification( s.getIdentifier() );

            if ( s.getClazz() == null )
            {
                addDetail( validationContext.getReport(), "IMPLEMENTATION_DEPENDENCY_SPECIFICATION_CLASS_CONSTRAINT",
                           Level.SEVERE, new ObjectFactory().createImplementation( implementation ),
                           "implementationDependencySpecificationClassConstraint", implementation.getIdentifier(),
                           moduleOfImpl.getName(), dependency.getName(), dependency.getIdentifier(),
                           moduleOfS.getName() );

            }

            if ( dependency.getVersion() != null )
            {
                if ( s.getVersion() == null )
                {
                    addDetail( validationContext.getReport(),
                               "IMPLEMENTATION_DEPENDENCY_SPECIFICATION_VERSIONING_CONSTRAINT", Level.SEVERE,
                               new ObjectFactory().createImplementation( implementation ),
                               "implementationDependencySpecificationVersioningConstraint",
                               implementation.getIdentifier(), moduleOfImpl.getName(), dependency.getName(),
                               s.getIdentifier(), moduleOfS.getName() );

                }
                else
                {
                    try
                    {
                        if ( VersionParser.compare( dependency.getVersion(), s.getVersion() ) > 0 )
                        {
                            addDetail( validationContext.getReport(),
                                       "IMPLEMENTATION_DEPENDENCY_SPECIFICATION_COMPATIBILITY_CONSTRAINT",
                                       Level.SEVERE, new ObjectFactory().createImplementation( implementation ),
                                       "implementationDependencySpecificationCompatibilityConstraint",
                                       implementation.getIdentifier(), moduleOfImpl.getName(), s.getIdentifier(),
                                       moduleOfS.getName(), dependency.getVersion(), s.getVersion() );

                        }
                    }
                    catch ( final ParseException e )
                    {
                        final String message = getMessage( e );

                        if ( validationContext.getModelContext().isLoggable( Level.FINE ) )
                        {
                            validationContext.getModelContext().log( Level.FINE, message, e );
                        }

                        addDetail( validationContext.getReport(),
                                   "IMPLEMENTATION_DEPENDENCY_SPECIFICATION_COMPATIBILITY_VERSIONING_PARSE_EXCEPTION",
                                   Level.SEVERE, new ObjectFactory().createImplementation( implementation ),
                                   "implementationDependencySpecificationCompatibilityParseException",
                                   implementation.getIdentifier(), moduleOfImpl.getName(), s.getIdentifier(),
                                   moduleOfS.getName(), dependency.getVersion(),
                                   message != null && message.length() > 0 ? " " + message : "" );

                    }
                    catch ( final TokenMgrError e )
                    {
                        final String message = getMessage( e );

                        if ( validationContext.getModelContext().isLoggable( Level.FINE ) )
                        {
                            validationContext.getModelContext().log( Level.FINE, message, e );
                        }

                        addDetail( validationContext.getReport(),
                                   "IMPLEMENTATION_DEPENDENCY_SPECIFICATION_COMPATIBILITY_VERSIONING_TOKEN_MANAGER_ERROR",
                                   Level.SEVERE, new ObjectFactory().createImplementation( implementation ),
                                   "implementationDependencySpecificationCompatibilityTokenMgrError",
                                   implementation.getIdentifier(), moduleOfImpl.getName(), s.getIdentifier(),
                                   moduleOfS.getName(), dependency.getVersion(),
                                   message != null && message.length() > 0 ? " " + message : "" );

                    }
                }
            }

            if ( s.getScope() != null )
            {
                if ( dependency.getDependencies() != null )
                {
                    try ( final Stream<Dependency> st0 =
                        dependency.getDependencies().getDependency().parallelStream().unordered() )
                    {
                        st0.forEach( d  ->
                        {
                            addDetail( validationContext.getReport(),
                                       "IMPLEMENTATION_DEPENDENCY_DEPENDENCIES_OVERRIDE_CONSTRAINT", Level.SEVERE,
                                       new ObjectFactory().createImplementation( implementation ),
                                       "implementationDependencyDependenciesOverrideConstraint",
                                       implementation.getIdentifier(), moduleOfImpl.getName(), dependency.getName(),
                                       d.getName(), s.getIdentifier(), moduleOfS.getName(), s.getScope() );
                        } );
                    }
                }

                if ( dependency.getMessages() != null )
                {
                    try ( final Stream<Message> st0 =
                        dependency.getMessages().getMessage().parallelStream().unordered() )
                    {
                        st0.forEach( m  ->
                        {
                            addDetail( validationContext.getReport(),
                                       "IMPLEMENTATION_DEPENDENCY_MESSAGES_OVERRIDE_CONSTRAINT", Level.SEVERE,
                                       new ObjectFactory().createImplementation( implementation ),
                                       "implementationDependencyMessagesOverrideConstraint",
                                       implementation.getIdentifier(), moduleOfImpl.getName(), dependency.getName(),
                                       m.getName(), s.getIdentifier(), moduleOfS.getName(), s.getScope() );

                        } );
                    }
                }

                if ( dependency.getProperties() != null )
                {
                    try ( final Stream<Property> st0 =
                        dependency.getProperties().getProperty().parallelStream().unordered() )
                    {
                        st0.forEach( p  ->
                        {
                            addDetail( validationContext.getReport(),
                                       "IMPLEMENTATION_DEPENDENCY_PROPERTIES_OVERRIDE_CONSTRAINT", Level.SEVERE,
                                       new ObjectFactory().createImplementation( implementation ),
                                       "implementationDependencyPropertiesOverrideConstraint",
                                       implementation.getIdentifier(), moduleOfImpl.getName(), dependency.getName(),
                                       p.getName(), s.getIdentifier(), moduleOfS.getName(), s.getScope() );

                        } );
                    }
                }
            }
        }

        if ( dependency.getMessages() != null )
        {
            try ( final Stream<Message> st0 = dependency.getMessages().getMessage().parallelStream().unordered() )
            {
                st0.forEach( m  ->
                {
                    if ( validationContext.isValidateJava() )
                    {
                        try
                        {
                            m.getJavaConstantName();
                        }
                        catch ( final ModelObjectException e )
                        {
                            final String message = getMessage( e );

                            if ( validationContext.getModelContext().isLoggable( Level.FINE ) )
                            {
                                validationContext.getModelContext().log( Level.FINE, message, e );
                            }

                            addDetail( validationContext.getReport(),
                                       "IMPLEMENTATION_DEPENDENCY_MESSAGE_JAVA_CONSTANT_NAME_CONSTRAINT", Level.SEVERE,
                                       new ObjectFactory().createImplementation( implementation ),
                                       "implementationDependencyMessageJavaConstantNameConstraint",
                                       implementation.getIdentifier(), moduleOfImpl.getName(), dependency.getName(),
                                       m.getName(), message != null && message.length() > 0 ? " " + message : "" );
                        }

                        try
                        {
                            m.getJavaGetterMethodName();
                        }
                        catch ( final ModelObjectException e )
                        {
                            final String message = getMessage( e );

                            if ( validationContext.getModelContext().isLoggable( Level.FINE ) )
                            {
                                validationContext.getModelContext().log( Level.FINE, message, e );
                            }

                            addDetail( validationContext.getReport(),
                                       "IMPLEMENTATION_DEPENDENCY_MESSAGE_JAVA_GETTER_METHOD_NAME_CONSTRAINT",
                                       Level.SEVERE, new ObjectFactory().createImplementation( implementation ),
                                       "implementationDependencyMessageJavaGetterMethodNameConstraint",
                                       implementation.getIdentifier(), moduleOfImpl.getName(), dependency.getName(),
                                       m.getName(), message != null && message.length() > 0 ? " " + message : "" );
                        }

                        try
                        {
                            m.getJavaSetterMethodName();
                        }
                        catch ( final ModelObjectException e )
                        {
                            final String message = getMessage( e );

                            if ( validationContext.getModelContext().isLoggable( Level.FINE ) )
                            {
                                validationContext.getModelContext().log( Level.FINE, message, e );
                            }

                            addDetail( validationContext.getReport(),
                                       "IMPLEMENTATION_DEPENDENCY_MESSAGE_JAVA_SETTER_METHOD_NAME_CONSTRAINT",
                                       Level.SEVERE, new ObjectFactory().createImplementation( implementation ),
                                       "implementationDependencyMessageJavaSetterMethodNameConstraint",
                                       implementation.getIdentifier(), moduleOfImpl.getName(), dependency.getName(),
                                       m.getName(), message != null && message.length() > 0 ? " " + message : "" );
                        }

                        try
                        {
                            m.getJavaVariableName();
                        }
                        catch ( final ModelObjectException e )
                        {
                            final String message = getMessage( e );

                            if ( validationContext.getModelContext().isLoggable( Level.FINE ) )
                            {
                                validationContext.getModelContext().log( Level.FINE, message, e );
                            }

                            addDetail( validationContext.getReport(),
                                       "IMPLEMENTATION_DEPENDENCY_MESSAGE_JAVA_VARIABLE_NAME_CONSTRAINT", Level.SEVERE,
                                       new ObjectFactory().createImplementation( implementation ),
                                       "implementationDependencyMessageJavaVariableNameConstraint",
                                       implementation.getIdentifier(), moduleOfImpl.getName(), dependency.getName(),
                                       m.getName(), message != null && message.length() > 0 ? " " + message : "" );
                        }
                    }

                    if ( m.getTemplate() != null )
                    {
                        try ( final Stream<Text> st1 = m.getTemplate().getText().parallelStream().unordered() )
                        {
                            st1.forEach( t  ->
                            {

                                try
                                {
                                    t.getMimeType();
                                }
                                catch ( final ModelObjectException e )
                                {
                                    final String message = getMessage( e );

                                    if ( validationContext.getModelContext().isLoggable( Level.FINE ) )
                                    {
                                        validationContext.getModelContext().log( Level.FINE, message, e );
                                    }

                                    addDetail( validationContext.getReport(),
                                               "IMPLEMENTATION_DEPENDENCY_MESSAGE_TEMPLATE_MIME_TYPE_CONSTRAINT",
                                               Level.SEVERE, new ObjectFactory().createImplementation( implementation ),
                                               "implementationDependencyMessageTemplateMimeTypeConstraint",
                                               implementation.getIdentifier(), moduleOfImpl.getName(),
                                               dependency.getName(), m.getName(), t.getLanguage(),
                                               message != null && message.length() > 0 ? " " + message : "" );

                                }

                                if ( validationContext.isValidateJava() )
                                {
                                    try
                                    {
                                        new MessageFormat( t.getValue(), new Locale( t.getLanguage() ) );
                                    }
                                    catch ( final IllegalArgumentException e )
                                    {
                                        final String message = getMessage( e );

                                        if ( validationContext.getModelContext().isLoggable( Level.FINE ) )
                                        {
                                            validationContext.getModelContext().log( Level.FINE, message, e );
                                        }

                                        addDetail( validationContext.getReport(),
                                                   "IMPLEMENTATION_DEPENDENCY_MESSAGE_TEMPLATE_CONSTRAINT",
                                                   Level.SEVERE,
                                                   new ObjectFactory().createImplementation( implementation ),
                                                   "implementationDependencyMessageTemplateConstraint",
                                                   implementation.getIdentifier(), moduleOfImpl.getName(),
                                                   dependency.getName(), m.getName(), t.getLanguage(),
                                                   message != null && message.length() > 0 ? " " + message : "" );

                                    }
                                }
                            } );
                        }
                    }

                    if ( m.getArguments() != null )
                    {
                        final Map<JavaIdentifier, Argument> javaVariableNames =
                            new ConcurrentHashMap<>( m.getArguments().getArgument().size() );

                        try ( final Stream<Argument> st1 = m.getArguments().getArgument().parallelStream().unordered() )
                        {
                            st1.forEach( a  ->
                            {

                                if ( validationContext.isValidateJava() )
                                {
                                    try
                                    {
                                        a.getJavaTypeName();
                                    }
                                    catch ( final ModelObjectException e )
                                    {
                                        final String message = getMessage( e );

                                        if ( validationContext.getModelContext().isLoggable( Level.FINE ) )
                                        {
                                            validationContext.getModelContext().log( Level.FINE, message, e );
                                        }

                                        addDetail( validationContext.getReport(),
                                                   "IMPLEMENTATION_DEPENDENCY_MESSAGE_ARGUMENT_JAVA_TYPE_NAME_CONSTRAINT",
                                                   Level.SEVERE,
                                                   new ObjectFactory().createImplementation( implementation ),
                                                   "implementationDependencyMessageArgumentJavaTypeNameConstraint",
                                                   implementation.getIdentifier(), moduleOfImpl.getName(),
                                                   dependency.getName(), m.getName(), a.getName(),
                                                   message != null && message.length() > 0 ? " " + message : "" );

                                    }

                                    try
                                    {
                                        final JavaIdentifier javaIdentifier = a.getJavaVariableName();
                                        final Argument existingArgument =
                                            javaVariableNames.putIfAbsent( javaIdentifier, a );

                                        if ( existingArgument != null )
                                        {
                                            addDetail( validationContext.getReport(),
                                                       "IMPLEMENTATION_DEPENDENCY_MESSAGE_ARGUMENT_JAVA_VARIABLE_NAME_UNIQUENESS_CONSTRAINT",
                                                       Level.SEVERE,
                                                       new ObjectFactory().createImplementation( implementation ),
                                                       "implementationDependencyMessageArgumentJavaVariableNameUniquenessConstraint",
                                                       implementation.getIdentifier(), moduleOfImpl.getName(),
                                                       dependency.getName(), m.getName(), a.getName(),
                                                       javaIdentifier, existingArgument.getName() );

                                        }
                                    }
                                    catch ( final ModelObjectException e )
                                    {
                                        final String message = getMessage( e );

                                        if ( validationContext.getModelContext().isLoggable( Level.FINE ) )
                                        {
                                            validationContext.getModelContext().log( Level.FINE, message, e );
                                        }

                                        addDetail( validationContext.getReport(),
                                                   "IMPLEMENTATION_DEPENDENCY_MESSAGE_ARGUMENT_JAVA_VARIABLE_NAME_CONSTRAINT",
                                                   Level.SEVERE,
                                                   new ObjectFactory().createImplementation( implementation ),
                                                   "implementationDependencyMessageArgumentJavaVariableNameConstraint",
                                                   implementation.getIdentifier(), moduleOfImpl.getName(), dependency.
                                                   getName(), m.getName(), a.getIndex(),
                                                   message != null && message.length() > 0 ? " " + message : "" );

                                    }
                                }
                            } );
                        }
                    }
                } );
            }

            try ( final Stream<MessageReference> st1 =
                dependency.getMessages().getReference().parallelStream().unordered() )
            {
                st1.forEach( r  ->
                {
                    addDetail( validationContext.getReport(),
                               "IMPLEMENTATION_DEPENDENCY_MESSAGE_REFERENCE_DECLARATION_CONSTRAINT", Level.SEVERE,
                               new ObjectFactory().createImplementation( implementation ),
                               "implementationDependencyMessageReferenceDeclarationConstraint",
                               implementation.getIdentifier(), moduleOfImpl.getName(), dependency.getName(),
                               r.getName() );

                } );
            }
        }

        if ( dependency.getProperties() != null )
        {
            try ( final Stream<Property> st0 = dependency.getProperties().getProperty().parallelStream().unordered() )
            {
                st0.forEach( p  ->
                {
                    if ( p.getValue() != null && p.getAny() != null )
                    {
                        addDetail( validationContext.getReport(), "IMPLEMENTATION_DEPENDENCY_PROPERTY_VALUE_CONSTRAINT",
                                   Level.SEVERE, new ObjectFactory().createImplementation( implementation ),
                                   "implementationDependencyPropertyValueConstraint", implementation.getIdentifier(),
                                   moduleOfImpl.getName(), dependency.getName(), p.getName() );

                    }

                    if ( p.getAny() != null && p.getType() == null )
                    {
                        addDetail( validationContext.getReport(), "IMPLEMENTATION_DEPENDENCY_PROPERTY_TYPE_CONSTRAINT",
                                   Level.SEVERE, new ObjectFactory().createImplementation( implementation ),
                                   "implementationDependencyPropertyTypeConstraint", implementation.getIdentifier(),
                                   moduleOfImpl.getName(), dependency.getName(), p.getName() );

                    }

                    if ( validationContext.isValidateJava() )
                    {
                        try
                        {
                            p.getJavaConstantName();
                        }
                        catch ( final ModelObjectException e )
                        {
                            final String message = getMessage( e );

                            if ( validationContext.getModelContext().isLoggable( Level.FINE ) )
                            {
                                validationContext.getModelContext().log( Level.FINE, message, e );
                            }

                            addDetail( validationContext.getReport(),
                                       "IMPLEMENTATION_DEPENDENCY_PROPERTY_JAVA_CONSTANT_NAME_CONSTRAINT", Level.SEVERE,
                                       new ObjectFactory().createImplementation( implementation ),
                                       "implementationDependencyPropertyJavaConstantNameConstraint",
                                       implementation.getIdentifier(), moduleOfImpl.getName(), dependency.getName(),
                                       p.getName(), message != null && message.length() > 0 ? " " + message : "" );

                        }

                        try
                        {
                            p.getJavaGetterMethodName();
                        }
                        catch ( final ModelObjectException e )
                        {
                            final String message = getMessage( e );

                            if ( validationContext.getModelContext().isLoggable( Level.FINE ) )
                            {
                                validationContext.getModelContext().log( Level.FINE, message, e );
                            }

                            addDetail( validationContext.getReport(),
                                       "IMPLEMENTATION_DEPENDENCY_PROPERTY_JAVA_GETTER_METHOD_NAME_CONSTRAINT",
                                       Level.SEVERE, new ObjectFactory().createImplementation( implementation ),
                                       "implementationDependencyPropertyJavaGetterMethodNameConstraint",
                                       implementation.getIdentifier(), moduleOfImpl.getName(), dependency.getName(),
                                       p.getName(), message != null && message.length() > 0 ? " " + message : "" );

                        }

                        try
                        {
                            p.getJavaSetterMethodName();
                        }
                        catch ( final ModelObjectException e )
                        {
                            final String message = getMessage( e );

                            if ( validationContext.getModelContext().isLoggable( Level.FINE ) )
                            {
                                validationContext.getModelContext().log( Level.FINE, message, e );
                            }

                            addDetail( validationContext.getReport(),
                                       "IMPLEMENTATION_DEPENDENCY_PROPERTY_JAVA_SETTER_METHOD_NAME_CONSTRAINT",
                                       Level.SEVERE, new ObjectFactory().createImplementation( implementation ),
                                       "implementationDependencyPropertyJavaSetterMethodNameConstraint",
                                       implementation.getIdentifier(), moduleOfImpl.getName(), dependency.getName(),
                                       p.getName(), message != null && message.length() > 0 ? " " + message : "" );

                        }

                        try
                        {
                            p.getJavaTypeName();
                        }
                        catch ( final ModelObjectException e )
                        {
                            final String message = getMessage( e );

                            if ( validationContext.getModelContext().isLoggable( Level.FINE ) )
                            {
                                validationContext.getModelContext().log( Level.FINE, message, e );
                            }

                            addDetail( validationContext.getReport(),
                                       "IMPLEMENTATION_DEPENDENCY_PROPERTY_JAVA_TYPE_NAME_CONSTRAINT",
                                       Level.SEVERE, new ObjectFactory().createImplementation( implementation ),
                                       "implementationDependencyPropertyJavaTypeNameConstraint",
                                       implementation.getIdentifier(), moduleOfImpl.getName(), dependency.getName(),
                                       p.getName(), message != null && message.length() > 0 ? " " + message : "" );

                        }

                        try
                        {
                            p.getJavaVariableName();
                        }
                        catch ( final ModelObjectException e )
                        {
                            final String message = getMessage( e );

                            if ( validationContext.getModelContext().isLoggable( Level.FINE ) )
                            {
                                validationContext.getModelContext().log( Level.FINE, message, e );
                            }

                            addDetail( validationContext.getReport(),
                                       "IMPLEMENTATION_DEPENDENCY_PROPERTY_JAVA_VARIABLE_NAME_CONSTRAINT",
                                       Level.SEVERE, new ObjectFactory().createImplementation( implementation ),
                                       "implementationDependencyPropertyJavaVariableNameConstraint",
                                       implementation.getIdentifier(), moduleOfImpl.getName(), dependency.getName(),
                                       p.getName(), message != null && message.length() > 0 ? " " + message : "" );

                        }

                        try
                        {
                            p.getJavaValue( validationContext.getModelContext().getClassLoader() );
                        }
                        catch ( final ModelObjectException e )
                        {
                            final String message = getMessage( e );

                            if ( validationContext.getModelContext().isLoggable( Level.FINE ) )
                            {
                                validationContext.getModelContext().log( Level.FINE, message, e );
                            }

                            addDetail( validationContext.getReport(),
                                       "IMPLEMENTATION_DEPENDENCY_PROPERTY_JAVA_VALUE_CONSTRAINT", Level.SEVERE,
                                       new ObjectFactory().createImplementation( implementation ),
                                       "implementationDependencyPropertyJavaValueConstraint",
                                       implementation.getIdentifier(), moduleOfImpl.getName(), dependency.getName(),
                                       p.getName(), message != null && message.length() > 0 ? " " + message : "" );

                        }
                    }
                } );
            }

            try ( final Stream<PropertyReference> st0 =
                dependency.getProperties().getReference().parallelStream().unordered() )
            {
                st0.forEach( r  ->
                {
                    addDetail( validationContext.getReport(),
                               "IMPLEMENTATION_DEPENDENCY_PROPERTY_REFERENCE_DECLARATION_CONSTRAINT", Level.SEVERE,
                               new ObjectFactory().createImplementation( implementation ),
                               "implementationDependencyPropertyReferenceDeclarationConstraint",
                               implementation.getIdentifier(), moduleOfImpl.getName(), dependency.getName(),
                               r.getName() );

                } );
            }
        }

        if ( available != null )
        {
            try ( final Stream<Implementation> st0 = available.getImplementation().parallelStream().unordered() )
            {
                st0.filter( a  -> dependency.getImplementationName() == null
                                       || dependency.getImplementationName().equals( a.getName() ) )
                    .forEach( a  ->
                    {
                        final InheritanceModel imodel = validationContext.getInheritanceModel();
                        final Module moduleOfA = validationContext.getModuleOfImplementation( a.getIdentifier() );

                        if ( dependency.getDependencies() != null )
                        {
                            try ( final Stream<Dependency> st1 = dependency.getDependencies().getDependency().
                                parallelStream().unordered() )
                            {
                                st1.forEach( override  ->
                                {
                                    final Set<InheritanceModel.Node<Dependency>> effDependencies =
                                        imodel.getDependencyNodes( a.getIdentifier(), override.getName() );

                                    final Set<InheritanceModel.Node<Dependency>> overriddenDependencies =
                                        modifiableSet( effDependencies );

                                    final boolean effectiveDependencyOverridden = !overriddenDependencies.isEmpty();

                                    if ( override.isOverride() && overriddenDependencies.isEmpty() )
                                    {
                                        addDetail( validationContext.getReport(),
                                                   "IMPLEMENTATION_DEPENDENCY_OVERRIDE_DEPENDENCY_CONSTRAINT",
                                                   Level.SEVERE,
                                                   new ObjectFactory().createImplementation( implementation ),
                                                   "implementationDependencyOverrideDependencyConstraint",
                                                   implementation.getIdentifier(), moduleOfImpl.getName(),
                                                   dependency.getName(), override.getName(), a.getIdentifier(),
                                                   moduleOfA.getName() );

                                    }

                                    if ( !( override.isOverride() || overriddenDependencies.isEmpty() ) )
                                    {
                                        try ( final Stream<InheritanceModel.Node<Dependency>> st2 =
                                            overriddenDependencies.parallelStream().unordered() )
                                        {
                                            st2.forEach( overriddenDependency  ->
                                            {
                                                addDetail( validationContext.getReport(),
                                                           "IMPLEMENTATION_DEPENDENCY_OVERRIDE_DEPENDENCY_WARNING",
                                                           Level.WARNING,
                                                           new ObjectFactory().createImplementation( implementation ),
                                                           "implementationDependencyOverrideDependencyWarning",
                                                           implementation.getIdentifier(), moduleOfImpl.getName(),
                                                           dependency.getName(), override.getName(), a.getIdentifier(),
                                                           moduleOfA.getName(),
                                                           getNodePathString( overriddenDependency ) );

                                            } );
                                        }
                                    }

                                    retainFinalNodes( overriddenDependencies );

                                    try ( final Stream<InheritanceModel.Node<Dependency>> st2 =
                                        overriddenDependencies.parallelStream().unordered() )
                                    {
                                        st2.forEach( overriddenDependency  ->
                                        {
                                            addDetail( validationContext.getReport(),
                                                       "IMPLEMENTATION_DEPENDENCY_FINAL_DEPENDENCY_CONSTRAINT",
                                                       Level.SEVERE,
                                                       new ObjectFactory().createImplementation( implementation ),
                                                       "implementationDependencyFinalDependencyConstraint",
                                                       implementation.getIdentifier(), moduleOfImpl.getName(),
                                                       dependency.getName(), override.getName(), a.getIdentifier(),
                                                       moduleOfA.getName(), getNodePathString( overriddenDependency ) );

                                        } );
                                    }

                                    if ( effectiveDependencyOverridden )
                                    {
                                        try ( final Stream<InheritanceModel.Node<Dependency>> st2 =
                                            effDependencies.parallelStream().unordered() )
                                        {
                                            st2.forEach( node  ->
                                            {
                                                final Dependency overridden = node.getModelObject();

                                                final Specification overrideSpecification =
                                                    validationContext.getSpecification( override.getIdentifier() );

                                                final Specification overriddenSpecification =
                                                    validationContext.getSpecification( overridden.getIdentifier() );

                                                if ( overrideSpecification != null && overriddenSpecification != null )
                                                {
                                                    if ( overrideSpecification.getMultiplicity()
                                                             != overriddenSpecification.getMultiplicity() )
                                                    {
                                                        addDetail( validationContext.getReport(),
                                                                   "IMPLEMENTATION_DEPENDENCY_MULTIPLICITY_CONSTRAINT",
                                                                   Level.SEVERE, new ObjectFactory().
                                                                       createImplementation( implementation ),
                                                                   "implementationDependencyMultiplicityConstraint",
                                                                   implementation.getIdentifier(),
                                                                   moduleOfImpl.getName(), dependency.getName(),
                                                                   overridden.getName(), a.getIdentifier(),
                                                                   moduleOfA.getName(), overrideSpecification.
                                                                   getMultiplicity().value(),
                                                                   overriddenSpecification.getMultiplicity().value() );

                                                    }

                                                    if ( overrideSpecification.getScope() != null
                                                             ? !overrideSpecification.getScope().
                                                            equals( overriddenSpecification.getScope() )
                                                             : overriddenSpecification.getScope() != null )
                                                    {
                                                        addDetail( validationContext.getReport(),
                                                                   "IMPLEMENTATION_DEPENDENCY_SCOPE_CONSTRAINT",
                                                                   Level.SEVERE,
                                                                   new ObjectFactory().
                                                                       createImplementation( implementation ),
                                                                   "implementationDependencyScopeConstraint",
                                                                   implementation.getIdentifier(),
                                                                   moduleOfImpl.getName(), dependency.getName(),
                                                                   override.getName(), a.getIdentifier(),
                                                                   moduleOfA.getName(),
                                                                   overrideSpecification.getScope() == null
                                                                       ? "Multiton"
                                                                       : overrideSpecification.getScope(),
                                                                   overriddenSpecification.getScope() == null
                                                                       ? "Multiton"
                                                                       : overriddenSpecification.getScope() );

                                                    }

                                                    if ( overriddenSpecification.getMultiplicity() == Multiplicity.MANY )
                                                    {
                                                        if ( override.getImplementationName() == null
                                                                 && overridden.getImplementationName() != null )
                                                        {
                                                            addDetail( validationContext.getReport(),
                                                                       "IMPLEMENTATION_DEPENDENCY_NO_IMPLEMENTATION_NAME_CONSTRAINT",
                                                                       Level.SEVERE, new ObjectFactory().
                                                                           createImplementation( implementation ),
                                                                       "implementationDependencyNoImplementationNameConstraint",
                                                                       implementation.getIdentifier(),
                                                                       moduleOfImpl.getName(), dependency.getName(),
                                                                       override.getName(), a.getIdentifier(),
                                                                       moduleOfA.getName() );

                                                        }

                                                        if ( override.getImplementationName() != null
                                                                 && overridden.getImplementationName() == null )
                                                        {
                                                            addDetail( validationContext.getReport(),
                                                                       "IMPLEMENTATION_DEPENDENCY_IMPLEMENTATION_NAME_CONSTRAINT",
                                                                       Level.SEVERE,
                                                                       new ObjectFactory().
                                                                           createImplementation( implementation ),
                                                                       "implementationDependencyImplementationNameConstraint",
                                                                       implementation.getIdentifier(),
                                                                       moduleOfImpl.getName(), dependency.getName(),
                                                                       overridden.getName(), a.getIdentifier(),
                                                                       moduleOfA.getName(),
                                                                       override.getImplementationName() );

                                                        }
                                                    }
                                                }

                                                if ( override.isOptional() != overridden.isOptional() )
                                                {
                                                    addDetail( validationContext.getReport(),
                                                               "IMPLEMENTATION_DEPENDENCY_OPTIONALITY_CONSTRAINT",
                                                               Level.SEVERE,
                                                               new ObjectFactory().
                                                                   createImplementation( implementation ),
                                                               "implementationDependencyOptonalityConstraint",
                                                               implementation.getIdentifier(), moduleOfImpl.getName(),
                                                               dependency.getName(), overridden.getName(),
                                                               a.getIdentifier(), moduleOfA.getName() );

                                                }
                                            } );
                                        }
                                    }
                                } );
                            }
                        }

                        if ( dependency.getMessages() != null )
                        {
                            try ( final Stream<Message> st1 =
                                dependency.getMessages().getMessage().parallelStream().unordered() )
                            {
                                st1.forEach( override  ->
                                {
                                    final Set<InheritanceModel.Node<Message>> overriddenMessages =
                                        modifiableSet( imodel.getMessageNodes( a.getIdentifier(), override.getName() ) );

                                    if ( override.isOverride() && overriddenMessages.isEmpty() )
                                    {
                                        addDetail( validationContext.getReport(),
                                                   "IMPLEMENTATION_DEPENDENCY_OVERRIDE_MESSAGE_CONSTRAINT",
                                                   Level.SEVERE,
                                                   new ObjectFactory().createImplementation( implementation ),
                                                   "implementationDependencyOverrideMessageConstraint",
                                                   implementation.getIdentifier(), moduleOfImpl.getName(),
                                                   dependency.getName(), override.getName(), a.getIdentifier(),
                                                   moduleOfA.getName() );

                                    }

                                    if ( !( override.isOverride() || overriddenMessages.isEmpty() ) )
                                    {
                                        try ( final Stream<InheritanceModel.Node<Message>> st2 =
                                            overriddenMessages.parallelStream().unordered() )
                                        {
                                            st2.forEach( overriddenMessage  ->
                                            {
                                                addDetail( validationContext.getReport(),
                                                           "IMPLEMENTATION_DEPENDENCY_OVERRIDE_MESSAGE_WARNING",
                                                           Level.WARNING,
                                                           new ObjectFactory().createImplementation( implementation ),
                                                           "implementationDependencyOverrideMessageWarning",
                                                           implementation.getIdentifier(), moduleOfImpl.getName(),
                                                           dependency.getName(), override.getName(), a.getIdentifier(),
                                                           moduleOfA.getName(),
                                                           getNodePathString( overriddenMessage ) );

                                            } );
                                        }
                                    }

                                    retainFinalNodes( overriddenMessages );

                                    try ( final Stream<InheritanceModel.Node<Message>> st2 =
                                        overriddenMessages.parallelStream().unordered() )
                                    {
                                        st2.forEach( overriddenMessage  ->
                                        {
                                            addDetail( validationContext.getReport(),
                                                       "IMPLEMENTATION_DEPENDENCY_FINAL_MESSAGE_CONSTRAINT",
                                                       Level.SEVERE,
                                                       new ObjectFactory().createImplementation( implementation ),
                                                       "implementationDependencyFinalMessageConstraint",
                                                       implementation.getIdentifier(), moduleOfImpl.getName(),
                                                       dependency.getName(), override.getName(), a.getIdentifier(),
                                                       moduleOfA.getName(), getNodePathString( overriddenMessage ) );

                                        } );
                                    }
                                } );
                            }
                        }

                        if ( dependency.getProperties() != null )
                        {
                            try ( final Stream<Property> st1 =
                                dependency.getProperties().getProperty().parallelStream().unordered() )
                            {
                                st1.forEach( override  ->
                                {
                                    final Set<InheritanceModel.Node<Property>> overriddenProperties =
                                        modifiableSet( imodel.getPropertyNodes( a.getIdentifier(),
                                                                                override.getName() ) );

                                    if ( override.isOverride() && overriddenProperties.isEmpty() )
                                    {
                                        addDetail( validationContext.getReport(),
                                                   "IMPLEMENTATION_DEPENDENCY_OVERRIDE_PROPERTY_CONSTRAINT",
                                                   Level.SEVERE,
                                                   new ObjectFactory().createImplementation( implementation ),
                                                   "implementationDependencyOverridePropertyConstraint",
                                                   implementation.getIdentifier(), moduleOfImpl.getName(),
                                                   dependency.getName(), override.getName(), a.getIdentifier(),
                                                   moduleOfA.getName() );

                                    }

                                    if ( !( override.isOverride() || overriddenProperties.isEmpty() ) )
                                    {
                                        try ( final Stream<InheritanceModel.Node<Property>> st2 =
                                            overriddenProperties.parallelStream().unordered() )
                                        {
                                            st2.forEach( overriddenProperty  ->
                                            {
                                                addDetail( validationContext.getReport(),
                                                           "IMPLEMENTATION_DEPENDENCY_OVERRIDE_PROPERTY_WARNING",
                                                           Level.WARNING,
                                                           new ObjectFactory().createImplementation( implementation ),
                                                           "implementationDependencyOverridePropertyWarning",
                                                           implementation.getIdentifier(), moduleOfImpl.getName(),
                                                           dependency.getName(), override.getName(), a.getIdentifier(),
                                                           moduleOfA.getName(),
                                                           getNodePathString( overriddenProperty ) );

                                            } );
                                        }
                                    }

                                    retainFinalNodes( overriddenProperties );

                                    try ( final Stream<InheritanceModel.Node<Property>> st2 =
                                        overriddenProperties.parallelStream().unordered() )
                                    {
                                        st2.forEach( overriddenProperty  ->
                                        {
                                            addDetail( validationContext.getReport(),
                                                       "IMPLEMENTATION_DEPENDENCY_FINAL_PROPERTY_CONSTRAINT",
                                                       Level.SEVERE,
                                                       new ObjectFactory().createImplementation( implementation ),
                                                       "implementationDependencyFinalPropertyConstraint",
                                                       implementation.getIdentifier(), moduleOfImpl.getName(),
                                                       dependency.getName(), override.getName(), a.getIdentifier(),
                                                       moduleOfA.getName(), getNodePathString( overriddenProperty ) );

                                        } );
                                    }
                                } );
                            }
                        }
                    } );
            }
        }

        if ( dependency.getDependencies() != null )
        {
            try ( final Stream<Dependency> st1 =
                dependency.getDependencies().getDependency().parallelStream().unordered() )
            {
                st1.forEach( d  ->
                {
                    if ( validationContext.isValidateJava() )
                    {
                        try
                        {
                            d.getJavaConstantName();
                        }
                        catch ( final ModelObjectException e )
                        {
                            final String message = getMessage( e );

                            if ( validationContext.getModelContext().isLoggable( Level.FINE ) )
                            {
                                validationContext.getModelContext().log( Level.FINE, message, e );
                            }

                            addDetail( validationContext.getReport(),
                                       "IMPLEMENTATION_DEPENDENCY_DEPENDENCY_JAVA_CONSTANT_NAME_CONSTRAINT",
                                       Level.SEVERE,
                                       new ObjectFactory().createImplementation( implementation ),
                                       "implementationDependencyDependencyJavaConstantNameConstraint",
                                       implementation.getIdentifier(), moduleOfImpl.getName(), dependency.getName(),
                                       d.getName(), message != null && message.length() > 0 ? " " + message : "" );

                        }

                        try
                        {
                            d.getJavaGetterMethodName();
                        }
                        catch ( final ModelObjectException e )
                        {
                            final String message = getMessage( e );

                            if ( validationContext.getModelContext().isLoggable( Level.FINE ) )
                            {
                                validationContext.getModelContext().log( Level.FINE, message, e );
                            }

                            addDetail( validationContext.getReport(),
                                       "IMPLEMENTATION_DEPENDENCY_DEPENDENCY_JAVA_GETTER_METHOD_NAME_CONSTRAINT",
                                       Level.SEVERE, new ObjectFactory().createImplementation( implementation ),
                                       "implementationDependencyDependencyJavaGetterMethodNameConstraint",
                                       implementation.getIdentifier(), moduleOfImpl.getName(), dependency.getName(),
                                       d.getName(), message != null && message.length() > 0 ? " " + message : "" );

                        }

                        try
                        {
                            d.getJavaSetterMethodName();
                        }
                        catch ( final ModelObjectException e )
                        {
                            final String message = getMessage( e );

                            if ( validationContext.getModelContext().isLoggable( Level.FINE ) )
                            {
                                validationContext.getModelContext().log( Level.FINE, message, e );
                            }

                            addDetail( validationContext.getReport(),
                                       "IMPLEMENTATION_DEPENDENCY_DEPENDENCY_JAVA_SETTER_METHOD_NAME_CONSTRAINT",
                                       Level.SEVERE, new ObjectFactory().createImplementation( implementation ),
                                       "implementationDependencyDependencyJavaSetterMethodNameConstraint",
                                       implementation.getIdentifier(), moduleOfImpl.getName(), dependency.getName(),
                                       d.getName(), message != null && message.length() > 0 ? " " + message : "" );

                        }

                        try
                        {
                            d.getJavaVariableName();
                        }
                        catch ( final ModelObjectException e )
                        {
                            final String message = getMessage( e );

                            if ( validationContext.getModelContext().isLoggable( Level.FINE ) )
                            {
                                validationContext.getModelContext().log( Level.FINE, message, e );
                            }

                            addDetail( validationContext.getReport(),
                                       "IMPLEMENTATION_DEPENDENCY_DEPENDENCY_JAVA_VARIABLE_NAME_CONSTRAINT",
                                       Level.SEVERE, new ObjectFactory().createImplementation( implementation ),
                                       "implementationDependencyDependencyJavaVariableNameConstraint",
                                       implementation.getIdentifier(), moduleOfImpl.getName(), dependency.getName(),
                                       d.getName(), message != null && message.length() > 0 ? " " + message : "" );

                        }
                    }

                    assertDependencyValid( validationContext, implementation, d );
                } );
            }
        }
    }

    private static void assertImplementationSpecificationCompatibility(
        final ValidationContext validationContext, final Implementation implementation )
    {
        final Specifications specs = validationContext.getSpecifications( implementation.getIdentifier() );
        final Module moduleOfImpl =
            validationContext.getModuleOfImplementation( implementation.getIdentifier() );

        if ( specs != null )
        {
            try ( final Stream<SpecificationReference> st0 = specs.getReference().parallelStream() )
            {
                st0.forEach( r  ->
                {
                    final Optional<Specification> s = specs.getSpecification( r.getIdentifier() );

                    if ( s.isPresent() && r.getVersion() != null )
                    {
                        final Module moduleOfS =
                            validationContext.getModuleOfSpecification( s.get().getIdentifier() );

                        if ( s.get().getVersion() == null )
                        {
                            addDetail( validationContext.getReport(),
                                       "IMPLEMENTATION_SPECIFICATION_VERSIONING_CONSTRAINT", Level.SEVERE,
                                       new ObjectFactory().createImplementation( implementation ),
                                       "implementationSpecificationVersioningConstraint",
                                       implementation.getIdentifier(), moduleOfImpl.getName(), s.get().getIdentifier(),
                                       moduleOfS.getName() );

                        }
                        else
                        {
                            try
                            {
                                if ( VersionParser.compare( r.getVersion(), s.get().getVersion() ) != 0 )
                                {
                                    addDetail( validationContext.getReport(),
                                               "IMPLEMENTATION_SPECIFICATION_COMPATIBILITY_CONSTRAINT", Level.SEVERE,
                                               new ObjectFactory().createImplementation( implementation ),
                                               "implementationSpecificationCompatibilityConstraint",
                                               implementation.getIdentifier(), moduleOfImpl.getName(),
                                               s.get().getIdentifier(), moduleOfS.getName(), r.getVersion(),
                                               s.get().getVersion() );

                                }
                            }
                            catch ( final ParseException e )
                            {
                                final String message = getMessage( e );

                                if ( validationContext.getModelContext().isLoggable( Level.FINE ) )
                                {
                                    validationContext.getModelContext().log( Level.FINE, message, e );
                                }

                                addDetail( validationContext.getReport(),
                                           "IMPLEMENTATION_SPECIFICATION_COMPATIBILITY_VERSIONING_PARSE_EXCEPTION",
                                           Level.SEVERE, new ObjectFactory().createImplementation( implementation ),
                                           "implementationSpecificationCompatibilityVersioningParseException",
                                           implementation.getIdentifier(), moduleOfImpl.getName(),
                                           s.get().getIdentifier(), moduleOfS.getName(), r.getVersion(),
                                           message != null && message.length() > 0 ? " " + message : "" );

                            }
                            catch ( final TokenMgrError e )
                            {
                                final String message = getMessage( e );

                                if ( validationContext.getModelContext().isLoggable( Level.FINE ) )
                                {
                                    validationContext.getModelContext().log( Level.FINE, message, e );
                                }

                                addDetail( validationContext.getReport(),
                                           "IMPLEMENTATION_SPECIFICATION_COMPATIBILITY_VERSIONING_TOKEN_MANAGER_ERROR",
                                           Level.SEVERE, new ObjectFactory().createImplementation( implementation ),
                                           "implementationSpecificationCompatibilityVersioningTokenManagerError",
                                           implementation.getIdentifier(), moduleOfImpl.getName(),
                                           s.get().getIdentifier(), moduleOfS.getName(), r.getVersion(),
                                           message != null && message.length() > 0 ? " " + message : "" );

                            }
                        }
                    }
                } );
            }
        }
    }

    private static <T> String getNodePathString( final InheritanceModel.Node<T> node )
    {
        final StringBuilder b = new StringBuilder( node.getPath().size() * 50 );

        for ( int i = 0, s0 = node.getPath().size(); i < s0; i++ )
        {
            final InheritanceModel.Node<Implementation> pathNode = node.getPath().get( i );

            if ( pathNode.getClassDeclaration() != null )
            {
                b.append( " -> [" ).append( pathNode.getClassDeclaration().getClazz() ).append( "] @ '" ).
                    append( pathNode.getImplementation().getIdentifier() ).append( "'" );

            }
            if ( pathNode.getSpecification() != null )
            {
                b.append( " -> <" ).append( pathNode.getSpecification().getIdentifier() ).append( "> @ '" ).
                    append( pathNode.getImplementation().getIdentifier() ).append( "'" );

            }
            else
            {
                b.append( " -> '" ).append( pathNode.getImplementation().getIdentifier() ).append( "'" );
            }
        }

        if ( node.getClassDeclaration() != null )
        {
            b.append( " -> [" ).append( node.getClassDeclaration().getClazz() ).append( "] @ '" ).
                append( node.getImplementation().getIdentifier() ).append( "'" );

        }
        if ( node.getSpecification() != null )
        {
            b.append( " -> <" ).append( node.getSpecification().getIdentifier() ).append( "> @ '" ).
                append( node.getImplementation().getIdentifier() ).append( "'" );

        }

        return b.length() > 0 ? b.substring( " -> ".length() ) : b.toString();
    }

    private static <T> String getNodeListPathString( final Collection<? extends InheritanceModel.Node<T>> nodes )
    {
        final StringBuilder path = new StringBuilder( nodes.size() * 255 );

        nodes.forEach( node  ->
        {
            path.append( ", " ).append( getNodePathString( node ) );
        } );

        return path.length() > 1 ? path.substring( 2 ) : path.toString();
    }

    private static <T> Set<InheritanceModel.Node<T>> retainFinalNodes( final Set<InheritanceModel.Node<T>> set )
    {
        if ( set != null )
        {
            for ( final Iterator<InheritanceModel.Node<T>> it = set.iterator(); it.hasNext(); )
            {
                if ( !it.next().isFinal() )
                {
                    it.remove();
                }
            }
        }

        return set;
    }

    private static void addDetail(
        final ModelValidationReport report, final String identifier, final Level level,
        final JAXBElement<? extends ModelObject> element, final String messageKey, final Object... messageArguments )
    {
        report.getDetails().add( new ModelValidationReport.Detail(
            identifier, level, getMessage( messageKey, messageArguments ), element ) );

    }

    private static <T> Set<T> modifiableSet( final Collection<? extends T> col )
    {
        Set<T> set = Collections.emptySet();

        if ( col != null )
        {
            set = new HashSet<>( col );
        }

        return set;
    }

    private static String getMessage( final String key, final Object... messageArguments )
    {
        return MessageFormat.format( ResourceBundle.getBundle(
            DefaultModelValidator.class.getName(), Locale.getDefault() ).getString( key ), messageArguments );

    }

    private static String getMessage( final Throwable t )
    {
        return t != null
                   ? t.getMessage() != null && t.getMessage().trim().length() > 0
                         ? t.getMessage()
                         : getMessage( t.getCause() )
                   : null;

    }

    /**
     * @since 1.2
     */
    private static final class ValidationContext
    {

        private final ModelContext modelContext;

        private final Modules modules;

        private final ModelValidationReport report;

        private final InheritanceModel inheritanceModel;

        private final boolean validateJava;

        private final Specifications allSpecifications;

        private final Implementations allImplementations;

        private final Map<String, Specification> specifications = new ConcurrentHashMap<>( 1024 );

        private final Map<String, Specifications> specificationsByImplementation = new ConcurrentHashMap<>( 1024 );

        private final Map<String, Module> modulesOfSpecifications = new ConcurrentHashMap<>( 1024 );

        private final Map<String, Implementation> implementations = new ConcurrentHashMap<>( 1024 );

        private final Map<String, Implementations> implementationsBySpecification = new ConcurrentHashMap<>( 1024 );

        private final Map<String, Module> modulesOfImplementations = new ConcurrentHashMap<>( 1024 );

        private ValidationContext( final ModelContext modelContext, final Modules modules,
                                   final ModelValidationReport report, final boolean validateJava )
        {
            super();
            this.modelContext = modelContext;
            this.modules = modules;
            this.report = report;
            this.inheritanceModel = new InheritanceModel( modules );
            this.validateJava = validateJava;
            this.allImplementations = modules.getImplementations();
            this.allSpecifications = modules.getSpecifications();

            if ( this.allSpecifications != null )
            {
                try ( final Stream<Specification> st0 =
                    this.allSpecifications.getSpecification().parallelStream().unordered() )
                {
                    st0.forEach( s  ->
                    {
                        specifications.put( s.getIdentifier(), s );

                        final Optional<Implementations> i = modules.getImplementations( s.getIdentifier() );

                        if ( i.isPresent() )
                        {
                            implementationsBySpecification.put( s.getIdentifier(), i.get() );
                        }

                        final Optional<Module> m = modules.getModuleOfSpecification( s.getIdentifier() );

                        if ( m.isPresent() )
                        {
                            modulesOfSpecifications.put( s.getIdentifier(), m.get() );
                        }
                    } );
                }
            }

            if ( this.allImplementations != null )
            {
                try ( final Stream<Implementation> st0 =
                    this.allImplementations.getImplementation().parallelStream().unordered() )
                {
                    st0.forEach( i  ->
                    {
                        implementations.put( i.getIdentifier(), i );

                        final Optional<Specifications> s = modules.getSpecifications( i.getIdentifier() );

                        if ( s.isPresent() )
                        {
                            specificationsByImplementation.put( i.getIdentifier(), s.get() );
                        }

                        final Optional<Module> m = modules.getModuleOfImplementation( i.getIdentifier() );

                        if ( m.isPresent() )
                        {
                            modulesOfImplementations.put( i.getIdentifier(), m.get() );
                        }

                        // Prepares the inheritance model for concurrent access.
                        synchronized ( inheritanceModel )
                        {
                            inheritanceModel.getSourceNodes( i.getIdentifier() );
                        }
                    } );
                }
            }
        }

        private ModelContext getModelContext()
        {
            return modelContext;
        }

        private Modules getModules()
        {
            return modules;
        }

        private ModelValidationReport getReport()
        {
            return report;
        }

        private InheritanceModel getInheritanceModel()
        {
            return this.inheritanceModel;
        }

        private boolean isValidateJava()
        {
            return this.validateJava;
        }

        private Specifications getAllSpecifications()
        {
            return this.allSpecifications;
        }

        private Implementations getAllImplementations()
        {
            return this.allImplementations;
        }

        private Specification getSpecification( final String identifier )
        {
            return this.specifications.get( identifier );
        }

        private Specifications getSpecifications( final String implementation )
        {
            return this.specificationsByImplementation.get( implementation );
        }

        private Implementation getImplementation( final String identifier )
        {
            return this.implementations.get( identifier );
        }

        private Implementations getImplementations( final String specification )
        {
            return this.implementationsBySpecification.get( specification );
        }

        private Module getModuleOfSpecification( final String identifier )
        {
            return this.modulesOfSpecifications.get( identifier );
        }

        private Module getModuleOfImplementation( final String identifier )
        {
            return this.modulesOfImplementations.get( identifier );
        }

    }

}
