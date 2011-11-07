/*
 *   Copyright (C) Christian Schulte, 2005-206
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.logging.Level;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.util.JAXBSource;
import javax.xml.transform.Source;
import org.jomc.model.Dependency;
import org.jomc.model.Implementation;
import org.jomc.model.ImplementationReference;
import org.jomc.model.Implementations;
import org.jomc.model.Message;
import org.jomc.model.MessageReference;
import org.jomc.model.ModelObject;
import org.jomc.model.Module;
import org.jomc.model.Modules;
import org.jomc.model.Multiplicity;
import org.jomc.model.ObjectFactory;
import org.jomc.model.Property;
import org.jomc.model.PropertyException;
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
import org.jomc.util.ParseException;
import org.jomc.util.TokenMgrError;
import org.jomc.util.VersionParser;

/**
 * Default object management and configuration {@code ModelValidator} implementation.
 *
 * @author <a href="mailto:schulte2005@users.sourceforge.net">Christian Schulte</a>
 * @version $JOMC$
 * @see ModelContext#validateModel(org.jomc.modlet.Model)
 */
public class DefaultModelValidator implements ModelValidator
{

    /** Creates a new {@code DefaultModelValidator} instance. */
    public DefaultModelValidator()
    {
        super();
    }

    public ModelValidationReport validateModel( final ModelContext context, final Model model ) throws ModelException
    {
        if ( context == null )
        {
            throw new NullPointerException( "context" );
        }
        if ( model == null )
        {
            throw new NullPointerException( "model" );
        }

        try
        {
            final Source source = new JAXBSource( context.createContext( model.getIdentifier() ),
                                                  new org.jomc.modlet.ObjectFactory().createModel( model ) );

            final ModelValidationReport report = context.validateModel( model.getIdentifier(), source );
            final Modules modules = ModelHelper.getModules( model );
            final ValidationContext validationContext = new ValidationContext( context, modules, report );

            if ( modules != null )
            {
                assertModulesValid( validationContext );
                assertSpecificationsValid( validationContext );
                assertImplementationsValid( validationContext );
            }

            return report;
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

    private static void assertModulesValid( final ValidationContext validationContext )
    {
        for ( int i = 0, s0 = validationContext.getModules().getModule().size(); i < s0; i++ )
        {
            final Module m = validationContext.getModules().getModule().get( i );

            if ( m.getImplementations() != null )
            {
                for ( int j = 0, s1 = m.getImplementations().getReference().size(); j < s1; j++ )
                {
                    final ImplementationReference r = m.getImplementations().getReference().get( j );
                    addDetail( validationContext.getReport(), "MODULE_IMPLEMENTATION_REFERENCE_DECLARATION_CONSTRAINT",
                               Level.SEVERE, new ObjectFactory().createModule( m ),
                               "moduleImplementationReferenceDeclarationConstraint", m.getName(), r.getIdentifier() );

                }
            }

            if ( m.getMessages() != null )
            {
                for ( int j = 0, s1 = m.getMessages().getMessage().size(); j < s1; j++ )
                {
                    final Message msg = m.getMessages().getMessage().get( j );

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

                    if ( msg.getTemplate() != null )
                    {
                        for ( int k = 0, s2 = msg.getTemplate().getText().size(); k < s2; k++ )
                        {
                            final Text t = msg.getTemplate().getText().get( k );

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
                                           "moduleMessageTemplateConstraint", m.getName(), msg.getName(), t.getValue(),
                                           message );

                            }
                        }
                    }
                }

                for ( int j = 0, s1 = m.getMessages().getReference().size(); j < s1; j++ )
                {
                    final MessageReference r = m.getMessages().getReference().get( j );
                    addDetail( validationContext.getReport(), "MODULE_MESSAGE_REFERENCE_DECLARATION_CONSTRAINT",
                               Level.SEVERE, new ObjectFactory().createModule( m ),
                               "moduleMessageReferenceDeclarationConstraint", m.getName(), r.getName() );

                }
            }

            if ( m.getProperties() != null )
            {
                for ( int j = 0, s1 = m.getProperties().getProperty().size(); j < s1; j++ )
                {
                    final Property p = m.getProperties().getProperty().get( j );

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

                    try
                    {
                        p.getJavaValue( validationContext.getModelContext().getClassLoader() );
                    }
                    catch ( final PropertyException e )
                    {
                        final String message = getMessage( e );

                        if ( validationContext.getModelContext().isLoggable( Level.FINE ) )
                        {
                            validationContext.getModelContext().log( Level.FINE, message, e );
                        }

                        addDetail( validationContext.getReport(), "MODULE_PROPERTY_JAVA_VALUE_CONSTRAINT", Level.SEVERE,
                                   new ObjectFactory().createModule( m ), "modulePropertyJavaValueConstraint",
                                   m.getName(), p.getName(), message );

                    }
                }

                for ( int j = 0, s1 = m.getProperties().getReference().size(); j < s1; j++ )
                {
                    final PropertyReference r = m.getProperties().getReference().get( j );
                    addDetail( validationContext.getReport(), "MODULE_PROPERTY_REFERENCE_DECLARATION_CONSTRAINT",
                               Level.SEVERE, new ObjectFactory().createModule( m ),
                               "modulePropertyReferenceDeclarationConstraint", m.getName(), r.getName() );

                }
            }

            if ( m.getSpecifications() != null )
            {
                for ( int j = 0, s1 = m.getSpecifications().getReference().size(); j < s1; j++ )
                {
                    final SpecificationReference r = m.getSpecifications().getReference().get( j );
                    addDetail( validationContext.getReport(), "MODULE_SPECIFICATION_REFERENCE_DECLARATION_CONSTRAINT",
                               Level.SEVERE, new ObjectFactory().createModule( m ),
                               "moduleSpecificationReferenceDeclarationConstraint", m.getName(), r.getIdentifier() );

                }
            }
        }
    }

    private static void assertImplementationsValid( final ValidationContext validationContext )
    {
        final Implementations implementations = validationContext.getModules().getImplementations();

        if ( implementations != null )
        {
            final Map<String, Implementation> implementationClassDeclarations = new HashMap<String, Implementation>();

            for ( int i = 0, s0 = implementations.getImplementation().size(); i < s0; i++ )
            {
                final Implementation impl = implementations.getImplementation().get( i );
                final InheritanceModel inheritanceModel = validationContext.getInheritanceModel( impl );
                final List<String> cyclePath = new LinkedList<String>();

                if ( isInheritanceCycle( validationContext, impl, null, cyclePath ) )
                {
                    final StringBuilder b = new StringBuilder( cyclePath.size() * 50 );

                    for ( int j = 0, s1 = cyclePath.size(); j < s1; j++ )
                    {
                        b.append( " -> " ).append( "'" ).append( cyclePath.get( j ) ).append( "'" );
                    }

                    addDetail( validationContext.getReport(), "IMPLEMENTATION_INHERITANCE_CYCLE_CONSTRAINT",
                               Level.SEVERE, new ObjectFactory().createImplementation( impl ),
                               "implementationInheritanceCycleConstraint", impl.getIdentifier(),
                               b.substring( " -> ".length() ) );

                }

                if ( impl.isClassDeclaration() )
                {
                    if ( impl.getClazz() == null )
                    {
                        addDetail( validationContext.getReport(), "IMPLEMENTATION_CLASS_CONSTRAINT", Level.SEVERE,
                                   new ObjectFactory().createImplementation( impl ), "implementationClassConstraint",
                                   impl.getIdentifier() );

                    }
                    else
                    {
                        final Implementation prev = implementationClassDeclarations.get( impl.getClazz() );

                        if ( prev != null )
                        {
                            addDetail( validationContext.getReport(), "IMPLEMENTATION_CLASS_DECLARATION_CONSTRAINT",
                                       Level.SEVERE, new ObjectFactory().createImplementation( impl ),
                                       "implementationClassDeclarationConstraint", impl.getIdentifier(),
                                       impl.getClazz(), prev.getIdentifier() );

                        }
                        else
                        {
                            implementationClassDeclarations.put( impl.getClazz(), impl );
                        }
                    }
                }

                if ( impl.isAbstract() && impl.getLocation() != null )
                {
                    addDetail( validationContext.getReport(), "IMPLEMENTATION_ABSTRACT_LOCATION_DECLARATION_CONSTRAINT",
                               Level.SEVERE, new ObjectFactory().createImplementation( impl ),
                               "implementationAbstractLocationDeclarationConstraint", impl.getIdentifier(),
                               impl.getLocation() );

                }

                if ( impl.getDependencies() != null )
                {
                    for ( int j = 0, s1 = impl.getDependencies().getDependency().size(); j < s1; j++ )
                    {
                        final Dependency d = impl.getDependencies().getDependency().get( j );

                        if ( d.isOverride() && !inheritanceModel.isAncestorDependencyOverridden( d.getName() )
                             && !inheritanceModel.isClassDeclarationDependencyOverridden( d.getName() ) )
                        {
                            addDetail( validationContext.getReport(), "IMPLEMENTATION_DEPENDENCY_OVERRIDE_CONSTRAINT",
                                       Level.SEVERE, new ObjectFactory().createImplementation( impl ),
                                       "implementationDependencyOverrideConstraint", impl.getIdentifier(),
                                       d.getName() );

                        }

                        if ( inheritanceModel.isFinalAncestorDependencyOverridden( d.getName() )
                             || inheritanceModel.isFinalClassDeclarationDependencyOverridden( d.getName() ) )
                        {
                            addDetail( validationContext.getReport(),
                                       "IMPLEMENTATION_DEPENDENCY_INHERITANCE_CONSTRAINT", Level.SEVERE,
                                       new ObjectFactory().createImplementation( impl ),
                                       "implementationDependencyFinalConstraint", impl.getIdentifier(), d.getName() );

                        }

                        assertDependencyValid( validationContext, impl, d );
                    }
                }

                if ( impl.getImplementations() != null )
                {
                    final Set<String> finalAncestors = inheritanceModel.getFinalAncestorImplementations();

                    for ( String finalAncestor : finalAncestors )
                    {
                        addDetail( validationContext.getReport(),
                                   "IMPLEMENTATION_IMPLEMENTATION_INHERITANCE_CONSTRAINT", Level.SEVERE,
                                   new ObjectFactory().createImplementation( impl ),
                                   "implementationFinalImplementationConstraint", impl.getIdentifier(),
                                   finalAncestor );

                    }

                    for ( int j = 0, s1 = impl.getImplementations().getImplementation().size(); j < s1; j++ )
                    {
                        final Implementation pi = impl.getImplementations().getImplementation().get( j );
                        addDetail( validationContext.getReport(),
                                   "IMPLEMENTATION_IMPLEMENTATION_DECLARATION_CONSTRAINT", Level.SEVERE,
                                   new ObjectFactory().createImplementation( impl ),
                                   "implementationImplementationDeclarationConstraint", impl.getIdentifier(),
                                   pi.getIdentifier() );

                    }

                    for ( int j = 0, s1 = impl.getImplementations().getReference().size(); j < s1; j++ )
                    {
                        final ImplementationReference r = impl.getImplementations().getReference().get( j );

                        if ( r.isOverride()
                             && !inheritanceModel.isAncestorImplReferenceOverridden( r.getIdentifier() ) )
                        {
                            addDetail( validationContext.getReport(),
                                       "IMPLEMENTATION_IMPLEMENTATION_OVERRIDE_CONSTRAINT", Level.SEVERE,
                                       new ObjectFactory().createImplementation( impl ),
                                       "implementationImplementationOverrideConstraint", impl.getIdentifier(),
                                       r.getIdentifier() );

                        }

                        if ( inheritanceModel.isFinalAncestorImplReferenceOverridden( r.getIdentifier() ) )
                        {
                            addDetail( validationContext.getReport(),
                                       "IMPLEMENTATION_IMPLEMENTATION_REFERENCE_INHERITANCE_CONSTRAINT", Level.SEVERE,
                                       new ObjectFactory().createImplementation( impl ),
                                       "implementationFinalImplementatioReferenceConstraint", impl.getIdentifier(),
                                       r.getIdentifier() );

                        }
                    }
                }

                if ( impl.getMessages() != null )
                {
                    for ( int j = 0, s1 = impl.getMessages().getMessage().size(); j < s1; j++ )
                    {
                        final Message m = impl.getMessages().getMessage().get( j );

                        if ( impl.getMessages().getReference( m.getName() ) != null )
                        {
                            addDetail( validationContext.getReport(), "IMPLEMENTATION_MESSAGES_UNIQUENESS_CONSTRAINT",
                                       Level.SEVERE, new ObjectFactory().createImplementation( impl ),
                                       "implementationMessagesUniquenessConstraint", impl.getIdentifier(),
                                       m.getName() );

                        }

                        if ( m.getTemplate() != null )
                        {
                            for ( int k = 0, s2 = m.getTemplate().getText().size(); k < s2; k++ )
                            {
                                final Text t = m.getTemplate().getText().get( k );

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
                                               m.getName(), t.getValue(), message );

                                }
                            }
                        }

                        if ( m.isOverride() && !inheritanceModel.isAncestorMessageOverridden( m.getName() )
                             && !inheritanceModel.isClassDeclarationMessageOverridden( m.getName() ) )
                        {
                            addDetail( validationContext.getReport(), "IMPLEMENTATION_MESSAGE_OVERRIDE_CONSTRAINT",
                                       Level.SEVERE, new ObjectFactory().createImplementation( impl ),
                                       "implementationMessageOverrideConstraint", impl.getIdentifier(), m.getName() );

                        }

                        if ( inheritanceModel.isFinalAncestorMessageOverridden( m.getName() )
                             || inheritanceModel.isFinalClassDeclarationMessageOverridden( m.getName() ) )
                        {
                            addDetail( validationContext.getReport(), "IMPLEMENTATION_MESSAGE_INHERITANCE_CONSTRAINT",
                                       Level.SEVERE, new ObjectFactory().createImplementation( impl ),
                                       "implementationMessageFinalConstraint", impl.getIdentifier(), m.getName() );

                        }
                    }

                    for ( int j = 0, s1 = impl.getMessages().getReference().size(); j < s1; j++ )
                    {
                        final MessageReference r = impl.getMessages().getReference().get( j );

                        if ( r.isOverride() && !inheritanceModel.isAncestorMessageOverridden( r.getName() )
                             && !inheritanceModel.isClassDeclarationMessageOverridden( r.getName() ) )
                        {
                            addDetail( validationContext.getReport(), "IMPLEMENTATION_MESSAGE_OVERRIDE_CONSTRAINT",
                                       Level.SEVERE, new ObjectFactory().createImplementation( impl ),
                                       "implementationMessageOverrideConstraint", impl.getIdentifier(), r.getName() );

                        }

                        if ( inheritanceModel.isFinalAncestorMessageOverridden( r.getName() )
                             || inheritanceModel.isFinalClassDeclarationMessageOverridden( r.getName() ) )
                        {
                            addDetail( validationContext.getReport(), "IMPLEMENTATION_MESSAGE_INHERITANCE_CONSTRAINT",
                                       Level.SEVERE, new ObjectFactory().createImplementation( impl ),
                                       "implementationMessageFinalConstraint", impl.getIdentifier(), r.getName() );

                        }
                    }
                }

                if ( impl.getProperties() != null )
                {
                    for ( int j = 0, s1 = impl.getProperties().getProperty().size(); j < s1; j++ )
                    {
                        final Property p = impl.getProperties().getProperty().get( j );

                        if ( impl.getProperties().getReference( p.getName() ) != null )
                        {
                            addDetail( validationContext.getReport(), "IMPLEMENTATION_PROPERTIES_UNIQUENESS_CONSTRAINT",
                                       Level.SEVERE, new ObjectFactory().createImplementation( impl ),
                                       "implementationPropertiesUniquenessConstraint", impl.getIdentifier(),
                                       p.getName() );

                        }

                        if ( p.getValue() != null && p.getAny() != null )
                        {
                            addDetail( validationContext.getReport(), "IMPLEMENTATION_PROPERTY_VALUE_CONSTRAINT",
                                       Level.SEVERE, new ObjectFactory().createImplementation( impl ),
                                       "implementationPropertyValueConstraint", impl.getIdentifier(), p.getName() );

                        }

                        if ( p.getAny() != null && p.getType() == null )
                        {
                            addDetail( validationContext.getReport(), "IMPLEMENTATION_PROPERTY_TYPE_CONSTRAINT",
                                       Level.SEVERE, new ObjectFactory().createImplementation( impl ),
                                       "implementationPropertyTypeConstraint", impl.getIdentifier(), p.getName() );

                        }

                        try
                        {
                            p.getJavaValue( validationContext.getModelContext().getClassLoader() );
                        }
                        catch ( final PropertyException e )
                        {
                            final String message = getMessage( e );

                            if ( validationContext.getModelContext().isLoggable( Level.FINE ) )
                            {
                                validationContext.getModelContext().log( Level.FINE, message, e );
                            }

                            addDetail( validationContext.getReport(), "IMPLEMENTATION_PROPERTY_JAVA_VALUE_CONSTRAINT",
                                       Level.SEVERE, new ObjectFactory().createImplementation( impl ),
                                       "implementationPropertyJavaValueConstraint", impl.getIdentifier(), p.getName(),
                                       message );

                        }

                        if ( p.isOverride() && !inheritanceModel.isAncestorPropertyOverridden( p.getName() )
                             && !inheritanceModel.isClassDeclarationPropertyOverridden( p.getName() ) )
                        {
                            addDetail( validationContext.getReport(), "IMPLEMENTATION_PROPERTY_OVERRIDE_CONSTRAINT",
                                       Level.SEVERE, new ObjectFactory().createImplementation( impl ),
                                       "implementationPropertyOverrideConstraint", impl.getIdentifier(), p.getName() );

                        }
                        if ( inheritanceModel.isFinalAncestorPropertyOverridden( p.getName() )
                             || inheritanceModel.isFinalClassDeclarationPropertyOverridden( p.getName() ) )
                        {
                            addDetail( validationContext.getReport(), "IMPLEMENTATION_PROPERTY_INHERITANCE_CONSTRAINT",
                                       Level.SEVERE, new ObjectFactory().createImplementation( impl ),
                                       "implementationPropertyFinalConstraint", impl.getIdentifier(), p.getName() );

                        }
                    }

                    for ( int j = 0, s1 = impl.getProperties().getReference().size(); j < s1; j++ )
                    {
                        final PropertyReference r = impl.getProperties().getReference().get( j );

                        if ( r.isOverride() && !inheritanceModel.isAncestorPropertyOverridden( r.getName() )
                             && !inheritanceModel.isClassDeclarationPropertyOverridden( r.getName() ) )
                        {
                            addDetail( validationContext.getReport(), "IMPLEMENTATION_PROPERTY_OVERRIDE_CONSTRAINT",
                                       Level.SEVERE, new ObjectFactory().createImplementation( impl ),
                                       "implementationPropertyOverrideConstraint", impl.getIdentifier(), r.getName() );

                        }

                        if ( inheritanceModel.isFinalAncestorPropertyOverridden( r.getName() )
                             || inheritanceModel.isFinalClassDeclarationPropertyOverridden( r.getName() ) )
                        {
                            addDetail( validationContext.getReport(), "IMPLEMENTATION_PROPERTY_INHERITANCE_CONSTRAINT",
                                       Level.SEVERE, new ObjectFactory().createImplementation( impl ),
                                       "implementationPropertyFinalConstraint", impl.getIdentifier(), r.getName() );

                        }
                    }
                }

                if ( impl.getSpecifications() != null )
                {
                    for ( int j = 0, s1 = impl.getSpecifications().getSpecification().size(); j < s1; j++ )
                    {
                        final Specification s = impl.getSpecifications().getSpecification().get( j );
                        addDetail( validationContext.getReport(), "IMPLEMENTATION_SPECIFICATION_DECLARATION_CONSTRAINT",
                                   Level.SEVERE, new ObjectFactory().createImplementation( impl ),
                                   "implementationSpecificationDeclarationConstraint", impl.getIdentifier(),
                                   s.getIdentifier() );

                    }

                    for ( int j = 0, s1 = impl.getSpecifications().getReference().size(); j < s1; j++ )
                    {
                        final SpecificationReference r = impl.getSpecifications().getReference().get( j );

                        if ( r.isOverride()
                             && !inheritanceModel.isAncestorSpecReferenceOverridden( r.getIdentifier() )
                             && !inheritanceModel.isClassDeclarationSpecReferenceOverridden( r.getIdentifier() ) )
                        {
                            addDetail( validationContext.getReport(),
                                       "IMPLEMENTATION_SPECIFICATION_OVERRIDE_CONSTRAINT", Level.SEVERE,
                                       new ObjectFactory().createImplementation( impl ),
                                       "implementationSpecificationOverrideConstraint", impl.getIdentifier(),
                                       r.getIdentifier() );

                        }

                        if ( inheritanceModel.isFinalAncestorSpecReferenceOverridden( r.getIdentifier() )
                             || inheritanceModel.isFinalClassDeclarationSpecReferenceOverridden( r.getIdentifier() ) )
                        {
                            addDetail( validationContext.getReport(),
                                       "IMPLEMENTATION_SPECIFICATION_INHERITANCE_CONSTRAINT", Level.SEVERE,
                                       new ObjectFactory().createImplementation( impl ),
                                       "implementationSpecificationFinalConstraint", impl.getIdentifier(),
                                       r.getIdentifier() );

                        }
                    }
                }

                final Map<String, List<InheritanceModel.Node<Dependency>>> dependencyNodes =
                    inheritanceModel.getEffectiveDependencies();

                if ( dependencyNodes != null )
                {
                    for ( Map.Entry<String, List<InheritanceModel.Node<Dependency>>> e : dependencyNodes.entrySet() )
                    {
                        if ( e.getValue().size() > 1 )
                        {
                            final StringBuilder path = new StringBuilder( e.getValue().size() * 255 );

                            for ( int j = 0, s1 = e.getValue().size(); j < s1; j++ )
                            {
                                path.append( ", " ).append( e.getValue().get( j ).pathToString() );
                            }

                            addDetail( validationContext.getReport(),
                                       "IMPLEMENTATION_DEPENDENCY_MULTIPLE_INHERITANCE_CONSTRAINT",
                                       Level.SEVERE, new ObjectFactory().createImplementation( impl ),
                                       "implementationMultipleInheritanceDependencyConstraint", impl.getIdentifier(),
                                       e.getKey(), path.substring( 2 ) );

                        }
                    }
                }

                final Map<String, List<InheritanceModel.Node<Message>>> messageNodes =
                    inheritanceModel.getEffectiveMessages();

                if ( messageNodes != null )
                {
                    for ( Map.Entry<String, List<InheritanceModel.Node<Message>>> e : messageNodes.entrySet() )
                    {
                        if ( e.getValue().size() > 1 )
                        {
                            final StringBuilder path = new StringBuilder( e.getValue().size() * 255 );

                            for ( int j = 0, s1 = e.getValue().size(); j < s1; j++ )
                            {
                                path.append( ", " ).append( e.getValue().get( j ).pathToString() );
                            }

                            addDetail( validationContext.getReport(),
                                       "IMPLEMENTATION_MESSAGE_MULTIPLE_INHERITANCE_CONSTRAINT", Level.SEVERE,
                                       new ObjectFactory().createImplementation( impl ),
                                       "implementationMultipleInheritanceMessageConstraint", impl.getIdentifier(),
                                       e.getKey(), path.substring( 2 ) );

                        }
                    }
                }

                final Map<String, List<InheritanceModel.Node<Property>>> propertyNodes =
                    inheritanceModel.getEffectiveProperties();

                if ( propertyNodes != null )
                {
                    for ( Map.Entry<String, List<InheritanceModel.Node<Property>>> e : propertyNodes.entrySet() )
                    {
                        if ( e.getValue().size() > 1 )
                        {
                            final StringBuilder path = new StringBuilder( e.getValue().size() * 255 );

                            for ( int j = 0, s1 = e.getValue().size(); j < s1; j++ )
                            {
                                path.append( ", " ).append( e.getValue().get( j ).pathToString() );
                            }

                            addDetail( validationContext.getReport(),
                                       "IMPLEMENTATION_PROPERTY_MULTIPLE_INHERITANCE_CONSTRAINT", Level.SEVERE,
                                       new ObjectFactory().createImplementation( impl ),
                                       "implementationMultipleInheritancePropertyConstraint", impl.getIdentifier(),
                                       e.getKey(), path.substring( 2 ) );

                        }
                    }
                }

                final Map<String, List<InheritanceModel.Node<SpecificationReference>>> specificationReferenceNodes =
                    inheritanceModel.getEffectiveSpecificationReferences();

                if ( specificationReferenceNodes != null )
                {
                    for ( Map.Entry<String, List<InheritanceModel.Node<SpecificationReference>>> e :
                          specificationReferenceNodes.entrySet() )
                    {
                        if ( e.getValue().size() > 1 )
                        {
                            final StringBuilder path = new StringBuilder( e.getValue().size() * 255 );

                            for ( int j = 0, s1 = e.getValue().size(); j < s1; j++ )
                            {
                                path.append( ", " ).append( e.getValue().get( j ).pathToString() );
                            }

                            addDetail( validationContext.getReport(),
                                       "IMPLEMENTATION_SPECIFICATION_MULTIPLE_INHERITANCE_CONSTRAINT",
                                       Level.SEVERE, new ObjectFactory().createImplementation( impl ),
                                       "implementationMultipleInheritanceSpecificationConstraint",
                                       impl.getIdentifier(), e.getKey(), path.substring( 2 ) );

                        }
                    }
                }

                final Map<String, List<InheritanceModel.Node<ImplementationReference>>> implementationReferenceNodes =
                    inheritanceModel.getEffectiveImplementationReferences();

                if ( implementationReferenceNodes != null )
                {
                    final Module moduleOfImplementation =
                        validationContext.getModules().getModuleOfImplementation( impl.getIdentifier() );

                    for ( Map.Entry<String, List<InheritanceModel.Node<ImplementationReference>>> e :
                          implementationReferenceNodes.entrySet() )
                    {
                        for ( int j = 0, s1 = e.getValue().size(); j < s1; j++ )
                        {
                            final ImplementationReference r = e.getValue().get( j ).getModelObject();
                            final Implementation referenced =
                                validationContext.getModules().getImplementation( r.getIdentifier() );

                            final Module moduleOfReferenced =
                                validationContext.getModules().getModuleOfImplementation( referenced.getIdentifier() );

                            if ( r.getVersion() != null && referenced != null )
                            {
                                if ( referenced.getVersion() == null )
                                {
                                    addDetail( validationContext.getReport(),
                                               "IMPLEMENTATION_IMPLEMENTATION_VERSIONING_CONSTRAINT", Level.SEVERE,
                                               new ObjectFactory().createImplementation( impl ),
                                               "implementationImplementationVersioningConstraint",
                                               impl.getIdentifier(),
                                               moduleOfImplementation != null ? moduleOfImplementation.getName() : "<>",
                                               r.getIdentifier(),
                                               moduleOfReferenced != null ? moduleOfReferenced.getName() : "<>" );

                                }
                                else
                                {
                                    try
                                    {
                                        if ( VersionParser.compare( r.getVersion(), referenced.getVersion() ) > 0 )
                                        {
                                            addDetail( validationContext.getReport(),
                                                       "IMPLEMENTATION_INHERITANCE_COMPATIBILITY_CONSTRAINT",
                                                       Level.SEVERE, new ObjectFactory().createImplementation( impl ),
                                                       "implementationInheritanceCompatibilityConstraint",
                                                       impl.getIdentifier(),
                                                       moduleOfImplementation != null
                                                       ? moduleOfImplementation.getName() : "<>",
                                                       referenced.getIdentifier(),
                                                       moduleOfReferenced != null
                                                       ? moduleOfReferenced.getName() : "<>",
                                                       r.getVersion(), referenced.getVersion() );

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
                                            impl.getIdentifier(),
                                            moduleOfImplementation != null ? moduleOfImplementation.getName() : "<>",
                                            r.getIdentifier(),
                                            moduleOfReferenced != null ? moduleOfReferenced.getName() : "<>",
                                            r.getVersion(),
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
                                            impl.getIdentifier(),
                                            moduleOfImplementation != null ? moduleOfImplementation.getName() : "<>",
                                            r.getIdentifier(),
                                            moduleOfReferenced != null ? moduleOfReferenced.getName() : "<>",
                                            r.getVersion(),
                                            message != null && message.length() > 0 ? " " + message : "" );

                                    }
                                }
                            }
                        }
                    }
                }

                assertImplementationSpecificationCompatibility( validationContext, impl );
            }
        }
    }

    private static void assertSpecificationsValid( final ValidationContext validationContext )
    {
        final Specifications specifications = validationContext.getModules().getSpecifications();
        final Map<String, Specification> specificationClassDeclarations = new HashMap<String, Specification>();

        if ( specifications != null )
        {
            for ( int i = 0, s0 = specifications.getSpecification().size(); i < s0; i++ )
            {
                final Specification s = specifications.getSpecification().get( i );
                final Implementations impls = validationContext.getModules().getImplementations( s.getIdentifier() );

                if ( s.isClassDeclaration() )
                {
                    if ( s.getClazz() == null )
                    {
                        addDetail( validationContext.getReport(), "SPECIFICATION_CLASS_CONSTRAINT", Level.SEVERE,
                                   new ObjectFactory().createSpecification( s ), "specificationClassConstraint",
                                   s.getIdentifier() );

                    }
                    else
                    {
                        final Specification prev = specificationClassDeclarations.get( s.getClazz() );
                        if ( prev != null )
                        {
                            addDetail( validationContext.getReport(), "SPECIFICATION_CLASS_DECLARATION_CONSTRAINT",
                                       Level.SEVERE, new ObjectFactory().createSpecification( s ),
                                       "specificationClassDeclarationConstraint", s.getIdentifier(), s.getClazz(),
                                       prev.getIdentifier() );

                        }
                        else
                        {
                            specificationClassDeclarations.put( s.getClazz(), s );
                        }
                    }
                }

                if ( impls != null )
                {
                    final Map<String, Implementations> map = new HashMap<String, Implementations>();

                    for ( int j = 0, s1 = impls.getImplementation().size(); j < s1; j++ )
                    {
                        final Implementation impl = impls.getImplementation().get( j );
                        Implementations implementations = map.get( impl.getName() );
                        if ( implementations == null )
                        {
                            implementations = new Implementations();
                            map.put( impl.getName(), implementations );
                        }

                        implementations.getImplementation().add( impl );
                    }

                    for ( Map.Entry<String, Implementations> e : map.entrySet() )
                    {
                        if ( e.getValue().getImplementation().size() > 1 )
                        {
                            for ( int j = 0, s1 = e.getValue().getImplementation().size(); j < s1; j++ )
                            {
                                final Implementation impl = e.getValue().getImplementation().get( j );
                                addDetail( validationContext.getReport(),
                                           "SPECIFICATION_IMPLEMENTATION_NAME_UNIQUENESS_CONSTRAINT",
                                           Level.SEVERE, new ObjectFactory().createImplementation( impl ),
                                           "specificationImplementationNameConstraint", impl.getIdentifier(),
                                           s.getIdentifier(), impl.getName() );

                            }
                        }
                    }

                    if ( s.getMultiplicity() == Multiplicity.ONE && impls.getImplementation().size() > 1 )
                    {
                        for ( int j = 0, s1 = impls.getImplementation().size(); j < s1; j++ )
                        {
                            final Implementation impl = impls.getImplementation().get( j );
                            addDetail( validationContext.getReport(),
                                       "SPECIFICATION_IMPLEMENTATION_MULTIPLICITY_CONSTRAINT", Level.SEVERE,
                                       new ObjectFactory().createImplementation( impl ),
                                       "specificationMultiplicityConstraint", impl.getIdentifier(), s.getIdentifier(),
                                       s.getMultiplicity() );

                        }
                    }
                }

                if ( s.getProperties() != null )
                {
                    for ( int j = 0, s1 = s.getProperties().getProperty().size(); j < s1; j++ )
                    {
                        final Property p = s.getProperties().getProperty().get( j );

                        if ( p.getValue() != null && p.getAny() != null )
                        {
                            addDetail( validationContext.getReport(), "SPECIFICATION_PROPERTY_VALUE_CONSTRAINT",
                                       Level.SEVERE, new ObjectFactory().createSpecification( s ),
                                       "specificationPropertyValueConstraint", s.getIdentifier(), p.getName() );

                        }

                        if ( p.getAny() != null && p.getType() == null )
                        {
                            addDetail( validationContext.getReport(), "SPECIFICATION_PROPERTY_TYPE_CONSTRAINT",
                                       Level.SEVERE, new ObjectFactory().createSpecification( s ),
                                       "specificationPropertyTypeConstraint", s.getIdentifier(), p.getName() );

                        }

                        try
                        {
                            p.getJavaValue( validationContext.getModelContext().getClassLoader() );
                        }
                        catch ( final PropertyException e )
                        {
                            final String message = getMessage( e );

                            if ( validationContext.getModelContext().isLoggable( Level.FINE ) )
                            {
                                validationContext.getModelContext().log( Level.FINE, message, e );
                            }

                            addDetail( validationContext.getReport(), "SPECIFICATION_PROPERTY_JAVA_VALUE_CONSTRAINT",
                                       Level.SEVERE, new ObjectFactory().createSpecification( s ),
                                       "specificationPropertyJavaValueConstraint", s.getIdentifier(), p.getName(),
                                       message );

                        }
                    }

                    for ( int j = 0, s1 = s.getProperties().getReference().size(); j < s1; j++ )
                    {
                        final PropertyReference r = s.getProperties().getReference().get( j );
                        addDetail( validationContext.getReport(),
                                   "SPECIFICATION_PROPERTY_REFERENCE_DECLARATION_CONSTRAINT", Level.SEVERE,
                                   new ObjectFactory().createSpecification( s ),
                                   "specificationPropertyReferenceDeclarationConstraint", s.getIdentifier(),
                                   r.getName() );

                    }
                }
            }
        }
    }

    private static void assertDependencyValid( final ValidationContext validationContext,
                                               final Implementation implementation, final Dependency dependency )
    {
        final Specification s = validationContext.getModules().getSpecification( dependency.getIdentifier() );
        final Implementations available =
            validationContext.getModules().getImplementations( dependency.getIdentifier() );

        if ( !dependency.isOptional()
             && ( available == null || available.getImplementation().isEmpty()
                  || ( dependency.getImplementationName() != null
                       && available.getImplementationByName( dependency.getImplementationName() ) == null ) ) )
        {
            addDetail( validationContext.getReport(), "IMPLEMENTATION_MANDATORY_DEPENDENCY_CONSTRAINT", Level.SEVERE,
                       new ObjectFactory().createImplementation( implementation ),
                       "implementationMandatoryDependencyConstraint", implementation.getIdentifier(),
                       dependency.getName() );

        }

        if ( s != null )
        {
            if ( s.getClazz() == null )
            {
                addDetail( validationContext.getReport(), "IMPLEMENTATION_DEPENDENCY_SPECIFICATION_CLASS_CONSTRAINT",
                           Level.SEVERE, new ObjectFactory().createImplementation( implementation ),
                           "implementationDependencySpecificationClassConstraint", implementation.getIdentifier(),
                           dependency.getName(), dependency.getIdentifier() );

            }

            if ( dependency.getVersion() != null )
            {
                if ( s.getVersion() == null )
                {
                    addDetail( validationContext.getReport(),
                               "IMPLEMENTATION_DEPENDENCY_SPECIFICATION_VERSIONING_CONSTRAINT", Level.SEVERE,
                               new ObjectFactory().createImplementation( implementation ),
                               "implementationDependencySpecificationVersioningConstraint",
                               implementation.getIdentifier(), dependency.getName(), s.getIdentifier() );

                }
                else
                {
                    final Module moduleOfSpecification =
                        validationContext.getModules().getModuleOfSpecification( s.getIdentifier() );

                    final Module moduleOfImplementation =
                        validationContext.getModules().getModuleOfImplementation( implementation.getIdentifier() );

                    try
                    {
                        if ( VersionParser.compare( dependency.getVersion(), s.getVersion() ) > 0 )
                        {
                            addDetail( validationContext.getReport(),
                                       "IMPLEMENTATION_DEPENDENCY_SPECIFICATION_COMPATIBILITY_CONSTRAINT",
                                       Level.SEVERE, new ObjectFactory().createImplementation( implementation ),
                                       "implementationDependencySpecificationCompatibilityConstraint",
                                       implementation.getIdentifier(),
                                       moduleOfImplementation != null ? moduleOfImplementation.getName() : "<>",
                                       s.getIdentifier(),
                                       moduleOfSpecification != null ? moduleOfSpecification.getName() : "<>",
                                       dependency.getVersion(), s.getVersion() );

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
                                   implementation.getIdentifier(),
                                   moduleOfImplementation != null ? moduleOfImplementation.getName() : "<>",
                                   s.getIdentifier(),
                                   moduleOfSpecification != null ? moduleOfSpecification.getName() : "<>",
                                   dependency.getVersion(),
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
                                   implementation.getIdentifier(),
                                   moduleOfImplementation != null ? moduleOfImplementation.getName() : "<>",
                                   s.getIdentifier(),
                                   moduleOfSpecification != null ? moduleOfSpecification.getName() : "<>",
                                   dependency.getVersion(),
                                   message != null && message.length() > 0 ? " " + message : "" );

                    }
                }
            }

            if ( s.getScope() != null )
            {
                if ( dependency.getDependencies() != null )
                {
                    for ( int i = 0, s0 = dependency.getDependencies().getDependency().size(); i < s0; i++ )
                    {
                        final Dependency d = dependency.getDependencies().getDependency().get( i );
                        addDetail( validationContext.getReport(),
                                   "IMPLEMENTATION_DEPENDENCY_DEPENDENCIES_OVERRIDE_CONSTRAINT", Level.SEVERE,
                                   new ObjectFactory().createImplementation( implementation ),
                                   "implementationDependencyDependenciesOverrideConstraint",
                                   implementation.getIdentifier(), dependency.getName(), s.getIdentifier(),
                                   s.getScope(), d.getName() );

                    }
                }

                if ( dependency.getMessages() != null )
                {
                    for ( int i = 0, s0 = dependency.getMessages().getMessage().size(); i < s0; i++ )
                    {
                        final Message m = dependency.getMessages().getMessage().get( i );
                        addDetail( validationContext.getReport(),
                                   "IMPLEMENTATION_DEPENDENCY_MESSAGES_OVERRIDE_CONSTRAINT", Level.SEVERE,
                                   new ObjectFactory().createImplementation( implementation ),
                                   "implementationDependencyMessagesOverrideConstraint", implementation.getIdentifier(),
                                   dependency.getName(), s.getIdentifier(), s.getScope(), m.getName() );

                    }
                }

                if ( dependency.getProperties() != null )
                {
                    for ( int i = 0, s0 = dependency.getProperties().getProperty().size(); i < s0; i++ )
                    {
                        final Property p = dependency.getProperties().getProperty().get( i );
                        addDetail( validationContext.getReport(),
                                   "IMPLEMENTATION_DEPENDENCY_PROPERTIES_OVERRIDE_CONSTRAINT", Level.SEVERE,
                                   new ObjectFactory().createImplementation( implementation ),
                                   "implementationDependencyPropertiesOverrideConstraint",
                                   implementation.getIdentifier(), dependency.getName(), s.getIdentifier(),
                                   s.getScope(), p.getName() );

                    }
                }
            }
        }

        if ( dependency.getMessages() != null )
        {
            for ( int i = 0, s0 = dependency.getMessages().getReference().size(); i < s0; i++ )
            {
                final MessageReference r = dependency.getMessages().getReference().get( i );
                addDetail( validationContext.getReport(),
                           "IMPLEMENTATION_DEPENDENCY_MESSAGE_REFERENCE_DECLARATION_CONSTRAINT", Level.SEVERE,
                           new ObjectFactory().createImplementation( implementation ),
                           "implementationDependencyMessageReferenceDeclarationConstraint",
                           implementation.getIdentifier(), dependency.getName(), r.getName() );

            }
        }

        if ( dependency.getProperties() != null )
        {
            for ( int i = 0, s0 = dependency.getProperties().getProperty().size(); i < s0; i++ )
            {
                final Property p = dependency.getProperties().getProperty().get( i );

                if ( p.getValue() != null && p.getAny() != null )
                {
                    addDetail( validationContext.getReport(), "IMPLEMENTATION_DEPENDENCY_PROPERTY_VALUE_CONSTRAINT",
                               Level.SEVERE, new ObjectFactory().createImplementation( implementation ),
                               "implementationDependencyPropertyValueConstraint", implementation.getIdentifier(),
                               dependency.getName(), p.getName() );

                }

                if ( p.getAny() != null && p.getType() == null )
                {
                    addDetail( validationContext.getReport(), "IMPLEMENTATION_DEPENDENCY_PROPERTY_TYPE_CONSTRAINT",
                               Level.SEVERE, new ObjectFactory().createImplementation( implementation ),
                               "implementationDependencyPropertyTypeConstraint", implementation.getIdentifier(),
                               dependency.getName(), p.getName() );

                }

                try
                {
                    p.getJavaValue( validationContext.getModelContext().getClassLoader() );
                }
                catch ( final PropertyException e )
                {
                    final String message = getMessage( e );

                    if ( validationContext.getModelContext().isLoggable( Level.FINE ) )
                    {
                        validationContext.getModelContext().log( Level.FINE, message, e );
                    }

                    addDetail( validationContext.getReport(),
                               "IMPLEMENTATION_DEPENDENCY_PROPERTY_JAVA_VALUE_CONSTRAINT", Level.SEVERE,
                               new ObjectFactory().createImplementation( implementation ),
                               "implementationDependencyPropertyJavaValueConstraint", implementation.getIdentifier(),
                               dependency.getName(), p.getName(), message );

                }
            }

            for ( int i = 0, s0 = dependency.getProperties().getReference().size(); i < s0; i++ )
            {
                final PropertyReference r = dependency.getProperties().getReference().get( i );
                addDetail( validationContext.getReport(),
                           "IMPLEMENTATION_DEPENDENCY_PROPERTY_REFERENCE_DECLARATION_CONSTRAINT", Level.SEVERE,
                           new ObjectFactory().createImplementation( implementation ),
                           "implementationDependencyPropertyReferenceDeclarationConstraint",
                           implementation.getIdentifier(), dependency.getName(), r.getName() );

            }
        }

        if ( available != null )
        {
            for ( int i = 0, s0 = available.getImplementation().size(); i < s0; i++ )
            {
                final Implementation a = available.getImplementation().get( i );

                if ( dependency.getImplementationName() != null
                     && !dependency.getImplementationName().equals( a.getName() ) )
                {
                    continue;
                }

                final InheritanceModel inheritanceModel = validationContext.getInheritanceModel( a );

                if ( dependency.getDependencies() != null )
                {
                    final Map<String, List<InheritanceModel.Node<Dependency>>> dependencies =
                        inheritanceModel.getEffectiveDependencies();

                    if ( dependencies != null )
                    {
                        for ( int j = 0, s1 = dependency.getDependencies().getDependency().size(); j < s1; j++ )
                        {
                            final Dependency override = dependency.getDependencies().getDependency().get( j );

                            if ( override.isOverride()
                                 && !inheritanceModel.isEffectiveDependencyOverridden( override.getName() ) )
                            {
                                addDetail( validationContext.getReport(),
                                           "IMPLEMENTATION_DEPENDENCY_OVERRIDE_DEPENDENCY_CONSTRAINT",
                                           Level.SEVERE, new ObjectFactory().createImplementation( implementation ),
                                           "implementationDependencyOverrideDependencyConstraint",
                                           implementation.getIdentifier(), dependency.getName(), a.getIdentifier(),
                                           override.getName() );

                            }

                            if ( inheritanceModel.isFinalEffectiveDependencyOverridden( override.getName() ) )
                            {
                                addDetail( validationContext.getReport(),
                                           "IMPLEMENTATION_DEPENDENCY_FINAL_DEPENDENCY_CONSTRAINT",
                                           Level.SEVERE, new ObjectFactory().createImplementation( implementation ),
                                           "implementationDependencyFinalDependencyConstraint",
                                           implementation.getIdentifier(), dependency.getName(), a.getIdentifier(),
                                           override.getName() );

                            }

                            if ( inheritanceModel.isEffectiveDependencyOverridden( override.getName() ) )
                            {
                                final List<InheritanceModel.Node<Dependency>> nodes =
                                    dependencies.get( override.getName() );

                                for ( int k = 0, s2 = nodes.size(); k < s2; k++ )
                                {
                                    final Dependency overridden = nodes.get( k ).getModelObject();
                                    final Specification overrideSpecification =
                                        validationContext.getModules().getSpecification( override.getIdentifier() );

                                    final Specification overriddenSpecification =
                                        validationContext.getModules().getSpecification( overridden.getIdentifier() );

                                    if ( overrideSpecification != null && overriddenSpecification != null )
                                    {
                                        if ( overrideSpecification.getMultiplicity()
                                             != overriddenSpecification.getMultiplicity() )
                                        {
                                            addDetail( validationContext.getReport(),
                                                       "IMPLEMENTATION_DEPENDENCY_MULTIPLICITY_CONSTRAINT",
                                                       Level.SEVERE,
                                                       new ObjectFactory().createImplementation( implementation ),
                                                       "implementationDependencyMultiplicityConstraint",
                                                       implementation.getIdentifier(), dependency.getName(),
                                                       a.getIdentifier(), overridden.getName(),
                                                       overrideSpecification.getMultiplicity().value(),
                                                       overriddenSpecification.getMultiplicity().value() );

                                        }

                                        if ( overrideSpecification.getScope() != null
                                             ? !overrideSpecification.getScope().equals(
                                            overriddenSpecification.getScope() )
                                             : overriddenSpecification.getScope() != null )
                                        {
                                            addDetail( validationContext.getReport(),
                                                       "IMPLEMENTATION_DEPENDENCY_SCOPE_CONSTRAINT",
                                                       Level.SEVERE,
                                                       new ObjectFactory().createImplementation( implementation ),
                                                       "implementationDependencyScopeConstraint",
                                                       implementation.getIdentifier(), dependency.getName(),
                                                       a.getIdentifier(), overridden.getName(),
                                                       overrideSpecification.getScope() == null
                                                       ? "Multiton" : overrideSpecification.getScope(),
                                                       overriddenSpecification.getScope() == null
                                                       ? "Multiton" : overriddenSpecification.getScope() );

                                        }

                                        if ( overriddenSpecification.getMultiplicity() == Multiplicity.MANY )
                                        {
                                            if ( override.getImplementationName() == null
                                                 && overridden.getImplementationName() != null )
                                            {
                                                addDetail( validationContext.getReport(),
                                                           "IMPLEMENTATION_DEPENDENCY_NO_IMPLEMENTATION_NAME_CONSTRAINT",
                                                           Level.SEVERE,
                                                           new ObjectFactory().createImplementation( implementation ),
                                                           "implementationDependencyNoImplementationNameConstraint",
                                                           implementation.getIdentifier(), dependency.getName(),
                                                           a.getIdentifier(), overridden.getName() );

                                            }

                                            if ( override.getImplementationName() != null
                                                 && overridden.getImplementationName() == null )
                                            {
                                                addDetail( validationContext.getReport(),
                                                           "IMPLEMENTATION_DEPENDENCY_IMPLEMENTATION_NAME_CONSTRAINT",
                                                           Level.SEVERE,
                                                           new ObjectFactory().createImplementation( implementation ),
                                                           "implementationDependencyImplementationNameConstraint",
                                                           implementation.getIdentifier(), dependency.getName(),
                                                           a.getIdentifier(), overridden.getName(),
                                                           override.getImplementationName() );

                                            }
                                        }
                                    }

                                    if ( override.isOptional() != overridden.isOptional() )
                                    {
                                        addDetail( validationContext.getReport(),
                                                   "IMPLEMENTATION_DEPENDENCY_OPTIONALITY_CONSTRAINT", Level.SEVERE,
                                                   new ObjectFactory().createImplementation( implementation ),
                                                   "implementationDependencyOptonalityConstraint",
                                                   implementation.getIdentifier(), dependency.getName(),
                                                   a.getIdentifier(), overridden.getName() );

                                    }
                                }
                            }
                        }
                    }
                }

                if ( dependency.getMessages() != null )
                {
                    final Map<String, List<InheritanceModel.Node<Message>>> messages =
                        inheritanceModel.getEffectiveMessages();

                    if ( messages != null )
                    {
                        for ( int j = 0, s1 = dependency.getMessages().getMessage().size(); j < s1; j++ )
                        {
                            final Message override = dependency.getMessages().getMessage().get( j );

                            if ( override.isOverride()
                                 && !inheritanceModel.isEffectiveMessageOverridden( override.getName() ) )
                            {
                                addDetail( validationContext.getReport(),
                                           "IMPLEMENTATION_DEPENDENCY_OVERRIDE_MESSAGE_CONSTRAINT", Level.SEVERE,
                                           new ObjectFactory().createImplementation( implementation ),
                                           "implementationDependencyOverrideMessageConstraint",
                                           implementation.getIdentifier(), dependency.getName(), a.getIdentifier(),
                                           override.getName() );

                            }

                            if ( inheritanceModel.isFinalEffectiveMessageOverridden( override.getName() ) )
                            {
                                addDetail( validationContext.getReport(),
                                           "IMPLEMENTATION_DEPENDENCY_FINAL_MESSAGE_CONSTRAINT", Level.SEVERE,
                                           new ObjectFactory().createImplementation( implementation ),
                                           "implementationDependencyFinalMessageConstraint",
                                           implementation.getIdentifier(), dependency.getName(), a.getIdentifier(),
                                           override.getName() );

                            }
                        }
                    }
                }

                if ( dependency.getProperties() != null )
                {
                    final Map<String, List<InheritanceModel.Node<Property>>> properties =
                        inheritanceModel.getEffectiveProperties();

                    if ( properties != null )
                    {
                        for ( int j = 0, s1 = dependency.getProperties().getProperty().size(); j < s1; j++ )
                        {
                            final Property override = dependency.getProperties().getProperty().get( j );

                            if ( override.isOverride()
                                 && !inheritanceModel.isEffectivePropertyOverridden( override.getName() ) )
                            {
                                addDetail( validationContext.getReport(),
                                           "IMPLEMENTATION_DEPENDENCY_OVERRIDE_PROPERTY_CONSTRAINT", Level.SEVERE,
                                           new ObjectFactory().createImplementation( implementation ),
                                           "implementationDependencyOverridePropertyConstraint",
                                           implementation.getIdentifier(), dependency.getName(), a.getIdentifier(),
                                           override.getName() );

                            }

                            if ( inheritanceModel.isFinalEffectivePropertyOverridden( override.getName() ) )
                            {
                                addDetail( validationContext.getReport(),
                                           "IMPLEMENTATION_DEPENDENCY_FINAL_PROPERTY_CONSTRAINT", Level.SEVERE,
                                           new ObjectFactory().createImplementation( implementation ),
                                           "implementationDependencyFinalPropertyConstraint",
                                           implementation.getIdentifier(), dependency.getName(), a.getIdentifier(),
                                           override.getName() );

                            }
                        }
                    }
                }
            }
        }

        if ( dependency.getDependencies() != null )
        {
            for ( int i = 0, s0 = dependency.getDependencies().getDependency().size(); i < s0; i++ )
            {
                final Dependency d = dependency.getDependencies().getDependency().get( i );
                assertDependencyValid( validationContext, implementation, d );
            }
        }
    }

    private static void assertImplementationSpecificationCompatibility(
        final ValidationContext validationContext, final Implementation implementation )
    {
        final Specifications specs = validationContext.getModules().getSpecifications( implementation.getIdentifier() );

        if ( specs != null )
        {
            for ( int i = 0, s0 = specs.getReference().size(); i < s0; i++ )
            {
                final SpecificationReference r = specs.getReference().get( i );
                final Specification s = specs.getSpecification( r.getIdentifier() );

                if ( s != null && r.getVersion() != null )
                {
                    if ( s.getVersion() == null )
                    {
                        addDetail( validationContext.getReport(),
                                   "IMPLEMENTATION_SPECIFICATION_VERSIONING_CONSTRAINT", Level.SEVERE,
                                   new ObjectFactory().createImplementation( implementation ),
                                   "implementationSpecificationVersioningConstraint", implementation.getIdentifier(),
                                   s.getIdentifier() );

                    }
                    else
                    {
                        final Module moduleOfImplementation =
                            validationContext.getModules().getModuleOfImplementation( implementation.getIdentifier() );

                        final Module moduleOfSpecification =
                            validationContext.getModules().getModuleOfSpecification( s.getIdentifier() );

                        try
                        {
                            if ( VersionParser.compare( r.getVersion(), s.getVersion() ) != 0 )
                            {
                                addDetail( validationContext.getReport(),
                                           "IMPLEMENTATION_SPECIFICATION_COMPATIBILITY_CONSTRAINT", Level.SEVERE,
                                           new ObjectFactory().createImplementation( implementation ),
                                           "implementationSpecificationCompatibilityConstraint",
                                           implementation.getIdentifier(),
                                           moduleOfImplementation != null ? moduleOfImplementation.getName() : "<>",
                                           s.getIdentifier(),
                                           moduleOfSpecification != null ? moduleOfSpecification.getName() : "<>",
                                           r.getVersion(), s.getVersion() );

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
                                       implementation.getIdentifier(),
                                       moduleOfImplementation != null ? moduleOfImplementation.getName() : "<>",
                                       s.getIdentifier(),
                                       moduleOfSpecification != null ? moduleOfSpecification.getName() : "<>",
                                       r.getVersion(), message != null && message.length() > 0 ? " " + message : "" );

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
                                       implementation.getIdentifier(),
                                       moduleOfImplementation != null ? moduleOfImplementation.getName() : "<>",
                                       s.getIdentifier(),
                                       moduleOfSpecification != null ? moduleOfSpecification.getName() : "<>",
                                       r.getVersion(), message != null && message.length() > 0 ? " " + message : "" );

                        }
                    }
                }
            }
        }
    }

    private static boolean isInheritanceCycle( final ValidationContext validationContext, final Implementation current,
                                               Map<String, Implementation> implementations, final List<String> path )
    {
        if ( implementations == null )
        {
            implementations = new HashMap<String, Implementation>();
        }

        if ( current != null )
        {
            path.add( current.getIdentifier() );

            if ( implementations.containsKey( current.getIdentifier() ) )
            {
                return true;
            }

            implementations.put( current.getIdentifier(), current );

            if ( current.getImplementations() != null )
            {
                for ( int i = 0, s0 = current.getImplementations().getReference().size(); i < s0; i++ )
                {
                    final ImplementationReference r = current.getImplementations().getReference().get( i );
                    return isInheritanceCycle(
                        validationContext, validationContext.getModules().getImplementation( r.getIdentifier() ),
                        implementations, path );

                }
            }

            path.remove( current.getIdentifier() );
        }

        return false;
    }

    private static void addDetail(
        final ModelValidationReport report, final String identifier, final Level level,
        final JAXBElement<? extends ModelObject> element, final String messageKey, final Object... messageArguments )
    {
        report.getDetails().add( new ModelValidationReport.Detail(
            identifier, level, getMessage( messageKey, messageArguments ), element ) );

    }

    private static String getMessage( final String key, final Object... messageArguments )
    {
        return MessageFormat.format( ResourceBundle.getBundle(
            DefaultModelValidator.class.getName().replace( '.', '/' ),
            Locale.getDefault() ).getString( key ), messageArguments );

    }

    private static String getMessage( final Throwable t )
    {
        return t != null ? t.getMessage() != null ? t.getMessage() : getMessage( t.getCause() ) : null;
    }

    /** @since 1.2 */
    private static class ValidationContext
    {

        private final ModelContext modelContext;

        private final Modules modules;

        private final ModelValidationReport report;

        private final Map<String, InheritanceModel> inheritanceModels = new HashMap<String, InheritanceModel>();

        private ValidationContext( final ModelContext modelContext, final Modules modules,
                                   final ModelValidationReport report )
        {
            super();
            this.modelContext = modelContext;
            this.modules = modules;
            this.report = report;
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

        private InheritanceModel getInheritanceModel( final Implementation implementation )
        {
            InheritanceModel inheritanceModel = this.inheritanceModels.get( implementation.getIdentifier() );

            if ( inheritanceModel == null )
            {
                inheritanceModel = new InheritanceModel( this, implementation );
                this.inheritanceModels.put( implementation.getIdentifier(), inheritanceModel );
            }

            return inheritanceModel;
        }

    }

    /** @since 1.2 */
    private static class InheritanceModel
    {

        /** @since 1.2 */
        private static class Node<T extends ModelObject>
        {

            private final Implementation implementation;

            private final Specification specification;

            private final Implementation classDeclaration;

            private final T modelObject;

            private final boolean _final;

            private final boolean override;

            private final List<String> path;

            private final Set<Node<T>> overrides = newSet();

            private Node( final Implementation implementation, final Specification specification,
                          final Implementation classDeclaration, final T modelObject, final boolean _final,
                          final boolean override, final List<String> path )
            {
                super();
                this.implementation = implementation;
                this.specification = specification;
                this.classDeclaration = classDeclaration;
                this.modelObject = modelObject;
                this._final = _final;
                this.override = override;
                this.path = new ArrayList<String>( path );
            }

            private T getModelObject()
            {
                return this.modelObject;
            }

            private Implementation getImplementation()
            {
                return this.implementation;
            }

            private Specification getSpecification()
            {
                return this.specification;
            }

            private Implementation getClassDeclaration()
            {
                return this.classDeclaration;
            }

            private boolean isFinal()
            {
                return this._final;
            }

            private boolean isOverride()
            {
                return this.override;
            }

            private List<String> getPath()
            {
                return this.path;
            }

            private Set<Node<T>> getOverrides()
            {
                return this.overrides;
            }

            private boolean isScope( final String implementation )
            {
                return this.implementation.getIdentifier().equals( implementation );
            }

            private boolean isScope( final Implementation implementation )
            {
                return this.isScope( implementation.getIdentifier() );
            }

            private String pathToString()
            {
                final StringBuilder b = new StringBuilder( this.path.size() * 50 );

                for ( int i = 0, s0 = this.path.size(); i < s0; i++ )
                {
                    final String s = this.path.get( i );
                    b.append( " -> " );

                    if ( s.startsWith( "CLASS_DECL:" ) )
                    {
                        b.append( ":" ).append( s.substring( "CLASS_DECL:".length() ) ).append( ":" );
                    }
                    else if ( s.startsWith( "SPECIFICATION:" ) )
                    {
                        b.append( "<" ).append( s.substring( "SPECIFICATION:".length() ) ).append( ">" );
                    }
                    else
                    {
                        b.append( "'" ).append( s ).append( "'" );
                    }
                }

                return b.substring( " -> ".length() );
            }

        }

        private final ValidationContext context;

        private final Implementation implementation;

        private final Implementation classDeclaration;

        private final Map<String, List<Node<Dependency>>> dependencies = newMap();

        private final Map<String, Map<String, List<Node<Dependency>>>> effectiveDependencies = newMap();

        private final Map<String, List<Node<Message>>> messages = newMap();

        private final Map<String, Map<String, List<Node<Message>>>> effectiveMessages = newMap();

        private final Map<String, List<Node<Property>>> properties = newMap();

        private final Map<String, Map<String, List<Node<Property>>>> effectiveProperties = newMap();

        private final Map<String, List<Node<SpecificationReference>>> specReferences = newMap();

        private final Map<String, Map<String, List<Node<SpecificationReference>>>> effectiveSpecReferences = newMap();

        private final Map<String, List<Node<ImplementationReference>>> implReferences = newMap();

        private final Map<String, Map<String, List<Node<ImplementationReference>>>> effectiveImplReferences = newMap();

        private final Map<String, Implementation> implementations = newMap();

        private final Map<String, Set<Implementation>> descendants = newMap();

        private final Set<Implementation> roots = newSet();

        private InheritanceModel( final ValidationContext context, final Implementation implementation )
        {
            super();
            this.context = context;
            this.implementation = implementation;
            this.classDeclaration = this.findClassDeclaration( implementation );

            this.collectNodes( implementation, implementation, null );

            for ( Implementation root : this.roots )
            {
                this.collectEffectiveNodes( root );
            }
        }

        private boolean isEffectiveDependencyOverridden( final String name )
        {
            return isEffectiveModelObjectOverridden(
                this.effectiveDependencies, this.implementation.getIdentifier(), name );

        }

        private boolean isFinalEffectiveDependencyOverridden( final String name )
        {
            return isFinalEffectiveModelObjectOverridden(
                this.effectiveDependencies, this.implementation.getIdentifier(), name );

        }

        private boolean isAncestorDependencyOverridden( final String name )
        {
            return isAncestorModelObjectOverridden( this.dependencies, this.implementation.getIdentifier(), name );
        }

        private boolean isFinalAncestorDependencyOverridden( final String name )
        {
            return isFinalAncestorModelObjectOverridden( this.dependencies, this.implementation.getIdentifier(), name );
        }

        private boolean isClassDeclarationDependencyOverridden( final String name )
        {
            return this.classDeclaration != null && this.context.getInheritanceModel( this.classDeclaration ).
                isEffectiveDependencyOverridden( name );

        }

        private boolean isFinalClassDeclarationDependencyOverridden( final String name )
        {
            return this.classDeclaration != null && this.context.getInheritanceModel( this.classDeclaration ).
                isFinalEffectiveDependencyOverridden( name );

        }

        private boolean isEffectiveMessageOverridden( final String name )
        {
            return isEffectiveModelObjectOverridden(
                this.effectiveMessages, this.implementation.getIdentifier(), name );

        }

        private boolean isFinalEffectiveMessageOverridden( final String name )
        {
            return isFinalEffectiveModelObjectOverridden(
                this.effectiveMessages, this.implementation.getIdentifier(), name );

        }

        private boolean isAncestorMessageOverridden( final String name )
        {
            return isAncestorModelObjectOverridden( this.messages, this.implementation.getIdentifier(), name );
        }

        private boolean isFinalAncestorMessageOverridden( final String name )
        {
            return isFinalAncestorModelObjectOverridden( this.messages, this.implementation.getIdentifier(), name );
        }

        private boolean isClassDeclarationMessageOverridden( final String name )
        {
            return this.classDeclaration != null && this.context.getInheritanceModel( this.classDeclaration ).
                isEffectiveMessageOverridden( name );

        }

        private boolean isFinalClassDeclarationMessageOverridden( final String name )
        {
            return this.classDeclaration != null && this.context.getInheritanceModel( this.classDeclaration ).
                isFinalEffectiveMessageOverridden( name );

        }

        private boolean isEffectivePropertyOverridden( final String name )
        {
            return isEffectiveModelObjectOverridden(
                this.effectiveProperties, this.implementation.getIdentifier(), name );

        }

        private boolean isFinalEffectivePropertyOverridden( final String name )
        {
            return isFinalEffectiveModelObjectOverridden(
                this.effectiveProperties, this.implementation.getIdentifier(), name );

        }

        private boolean isAncestorPropertyOverridden( final String name )
        {
            return isAncestorModelObjectOverridden( this.properties, this.implementation.getIdentifier(), name );
        }

        private boolean isFinalAncestorPropertyOverridden( final String name )
        {
            return isFinalAncestorModelObjectOverridden( this.properties, this.implementation.getIdentifier(), name );
        }

        private boolean isClassDeclarationPropertyOverridden( final String name )
        {
            return this.classDeclaration != null && this.context.getInheritanceModel( this.classDeclaration ).
                isEffectivePropertyOverridden( name );

        }

        private boolean isFinalClassDeclarationPropertyOverridden( final String name )
        {
            return this.classDeclaration != null && this.context.getInheritanceModel( this.classDeclaration ).
                isFinalEffectivePropertyOverridden( name );

        }

        private boolean isEffectiveSpecReferenceOverridden( final String identifier )
        {
            return isEffectiveModelObjectOverridden(
                this.effectiveSpecReferences, this.implementation.getIdentifier(), identifier );

        }

        private boolean isFinalEffectiveSpecReferenceOverridden( final String identifier )
        {
            return isFinalEffectiveModelObjectOverridden(
                this.effectiveSpecReferences, this.implementation.getIdentifier(), identifier );

        }

        private boolean isAncestorSpecReferenceOverridden( final String identifier )
        {
            return isAncestorModelObjectOverridden(
                this.specReferences, this.implementation.getIdentifier(), identifier );

        }

        private boolean isFinalAncestorSpecReferenceOverridden( final String identifier )
        {
            return isFinalAncestorModelObjectOverridden(
                this.specReferences, this.implementation.getIdentifier(), identifier );

        }

        private boolean isClassDeclarationSpecReferenceOverridden( final String identifier )
        {
            return this.classDeclaration != null && this.context.getInheritanceModel( this.classDeclaration ).
                isEffectiveSpecReferenceOverridden( identifier );

        }

        private boolean isFinalClassDeclarationSpecReferenceOverridden( final String identifier )
        {
            return this.classDeclaration != null && this.context.getInheritanceModel( this.classDeclaration ).
                isFinalEffectiveSpecReferenceOverridden( identifier );

        }

        private boolean isAncestorImplReferenceOverridden( final String identifier )
        {
            return isAncestorModelObjectOverridden(
                this.implReferences, this.implementation.getIdentifier(), identifier );

        }

        private boolean isFinalAncestorImplReferenceOverridden( final String identifier )
        {
            return isFinalAncestorModelObjectOverridden(
                this.implReferences, this.implementation.getIdentifier(), identifier );

        }

        private Set<String> getFinalAncestorImplementations()
        {
            final Set<String> set = newSet( this.implementations.size() );

            for ( final Iterator<Implementation> it = this.implementations.values().iterator(); it.hasNext(); )
            {
                final Implementation i = it.next();

                if ( !i.getIdentifier().equals( this.implementation.getIdentifier() ) && i.isFinal() )
                {
                    set.add( i.getIdentifier() );
                }
            }

            return set;
        }

        private Map<String, List<Node<Dependency>>> getDeclaredDependencies( final String impl )
        {
            return getDeclarationNodes( this.dependencies, impl );
        }

        private Map<String, List<Node<Message>>> getDeclaredMessages( final String impl )
        {
            return getDeclarationNodes( this.messages, impl );
        }

        private Map<String, List<Node<Property>>> getDeclaredProperties( final String impl )
        {
            return getDeclarationNodes( this.properties, impl );
        }

        private Map<String, List<Node<SpecificationReference>>> getDeclaredSpecificationReferences( final String i )
        {
            return getDeclarationNodes( this.specReferences, i );
        }

        private Map<String, List<Node<ImplementationReference>>> getDeclaredImplementationReferences( final String i )
        {
            return getDeclarationNodes( this.implReferences, i );
        }

        private Map<String, List<Node<Dependency>>> getEffectiveDependencies()
        {
            return this.effectiveDependencies.get( this.implementation.getIdentifier() );
        }

        private Map<String, List<Node<Message>>> getEffectiveMessages()
        {
            return this.effectiveMessages.get( this.implementation.getIdentifier() );
        }

        private Map<String, List<Node<Property>>> getEffectiveProperties()
        {
            return this.effectiveProperties.get( this.implementation.getIdentifier() );
        }

        private Map<String, List<Node<SpecificationReference>>> getEffectiveSpecificationReferences()
        {
            return this.effectiveSpecReferences.get( this.implementation.getIdentifier() );
        }

        private Map<String, List<Node<ImplementationReference>>> getEffectiveImplementationReferences()
        {
            return this.effectiveImplReferences.get( this.implementation.getIdentifier() );
        }

        private void inheritDependencies( final Map<String, List<Node<Dependency>>> ancestor, final String descendant )
        {
            inheritModelObjects( this.effectiveDependencies, ancestor, descendant );
        }

        private void inheritMessages( final Map<String, List<Node<Message>>> ancestor, final String descendant )
        {
            inheritModelObjects( this.effectiveMessages, ancestor, descendant );
        }

        private void inheritProperties( final Map<String, List<Node<Property>>> ancestor, final String descendant )
        {
            inheritModelObjects( this.effectiveProperties, ancestor, descendant );
        }

        private void inheritSpecificationReferences(
            final Map<String, List<Node<SpecificationReference>>> ancestor, final String descendant )
        {
            inheritModelObjects( this.effectiveSpecReferences, ancestor, descendant );
        }

        private void inheritImplementationReferences(
            final Map<String, List<Node<ImplementationReference>>> ancestor, final String descendant )
        {
            inheritModelObjects( this.effectiveImplReferences, ancestor, descendant );
        }

        private void addDependencyNode( final Node<Dependency> node )
        {
            addNode( this.dependencies, node, node.getModelObject().getName() );
        }

        private void addMessageNode( final Node<Message> node )
        {
            addNode( this.messages, node, node.getModelObject().getName() );
        }

        private void addPropertyNode( final Node<Property> node )
        {
            addNode( this.properties, node, node.getModelObject().getName() );
        }

        private void addSpecificationNode( final Node<SpecificationReference> node )
        {
            addNode( this.specReferences, node, node.getModelObject().getIdentifier() );
        }

        private void addImplementationReferenceNode( final Node<ImplementationReference> node )
        {
            addNode( this.implReferences, node, node.getModelObject().getIdentifier() );
        }

        private void collectNodes(
            final Implementation declaration, final Implementation descendant, List<String> path )
        {
            if ( path == null )
            {
                path = newList();
            }

            if ( declaration != null && !this.implementations.containsKey( declaration.getIdentifier() ) )
            {
                this.implementations.put( declaration.getIdentifier(), declaration );

                path.add( declaration.getIdentifier() );

                Set<Implementation> set = this.descendants.get( declaration.getIdentifier() );

                if ( set == null )
                {
                    set = newSet();
                    this.descendants.put( declaration.getIdentifier(), set );
                }

                set.add( descendant );

                if ( declaration.getDependencies() != null )
                {
                    for ( int i = 0, s0 = declaration.getDependencies().getDependency().size(); i < s0; i++ )
                    {
                        final Dependency d = declaration.getDependencies().getDependency().get( i );
                        this.addDependencyNode( new Node<Dependency>(
                            declaration, null, null, d, d.isFinal(), d.isOverride(), path ) );

                    }
                }

                if ( declaration.getMessages() != null )
                {
                    for ( int i = 0, s0 = declaration.getMessages().getMessage().size(); i < s0; i++ )
                    {
                        final Message m = declaration.getMessages().getMessage().get( i );
                        this.addMessageNode( new Node<Message>(
                            declaration, null, null, m, m.isFinal(), m.isOverride(), path ) );

                    }

                    if ( !declaration.getMessages().getReference().isEmpty() )
                    {
                        final Module m =
                            this.context.getModules().getModuleOfImplementation( declaration.getIdentifier() );

                        if ( m != null && m.getMessages() != null )
                        {
                            for ( int i = 0, s0 = declaration.getMessages().getReference().size(); i < s0; i++ )
                            {
                                final MessageReference r = declaration.getMessages().getReference().get( i );
                                Message msg = m.getMessages().getMessage( r.getName() );
                                if ( msg != null )
                                {
                                    msg = msg.clone();
                                    msg.setFinal( r.isFinal() );
                                    msg.setOverride( r.isOverride() );
                                    this.addMessageNode( new Node<Message>(
                                        declaration, null, null, msg, msg.isFinal(), msg.isOverride(), path ) );

                                }
                            }
                        }
                    }
                }

                if ( declaration.getProperties() != null )
                {
                    for ( int i = 0, s0 = declaration.getProperties().getProperty().size(); i < s0; i++ )
                    {
                        final Property p = declaration.getProperties().getProperty().get( i );
                        this.addPropertyNode( new Node<Property>(
                            declaration, null, null, p, p.isFinal(), p.isOverride(), path ) );

                    }

                    if ( !declaration.getProperties().getReference().isEmpty() )
                    {
                        final Module m =
                            this.context.getModules().getModuleOfImplementation( declaration.getIdentifier() );

                        if ( m != null && m.getProperties() != null )
                        {
                            for ( int i = 0, s0 = declaration.getProperties().getReference().size(); i < s0; i++ )
                            {
                                final PropertyReference r = declaration.getProperties().getReference().get( i );
                                Property p = m.getProperties().getProperty( r.getName() );
                                if ( p != null )
                                {
                                    p = p.clone();
                                    p.setFinal( r.isFinal() );
                                    p.setOverride( r.isOverride() );
                                    this.addPropertyNode( new Node<Property>(
                                        declaration, null, null, p, p.isFinal(), p.isOverride(), path ) );

                                }
                            }
                        }
                    }
                }

                if ( declaration.getSpecifications() != null )
                {
                    for ( int i = 0, s0 = declaration.getSpecifications().getReference().size(); i < s0; i++ )
                    {
                        final SpecificationReference r = declaration.getSpecifications().getReference().get( i );
                        this.addSpecificationNode( new Node<SpecificationReference>(
                            declaration, null, null, r, r.isFinal(), r.isOverride(), path ) );

                        final Specification s = this.context.getModules().getSpecification( r.getIdentifier() );
                        if ( s != null && s.getProperties() != null )
                        {
                            final String str = "SPECIFICATION:" + s.getIdentifier();
                            path.add( str );

                            for ( int j = 0, s1 = s.getProperties().getProperty().size(); j < s1; j++ )
                            {
                                final Property p = s.getProperties().getProperty().get( j );
                                this.addPropertyNode( new Node<Property>(
                                    declaration, s, null, p, p.isFinal(), p.isOverride(), path ) );

                            }

                            path.remove( str );
                        }
                    }
                }

                if ( declaration.getImplementations() != null
                     && !declaration.getImplementations().getReference().isEmpty() )
                {
                    for ( int i = 0, s0 = declaration.getImplementations().getReference().size(); i < s0; i++ )
                    {
                        final ImplementationReference r = declaration.getImplementations().getReference().get( i );
                        this.addImplementationReferenceNode( new Node<ImplementationReference>(
                            declaration, null, null, r, r.isFinal(), r.isOverride(), path ) );

                        final Implementation ancestor =
                            this.context.getModules().getImplementation( r.getIdentifier() );

                        this.collectNodes( ancestor, declaration, path );
                    }
                }
                else
                {
                    this.roots.add( declaration );
                }

                path.remove( declaration.getIdentifier() );
            }
        }

        private void collectEffectiveNodes( final Implementation declaration )
        {
            final Map<String, List<Node<SpecificationReference>>> specificationReferenceDeclarations =
                this.getDeclaredSpecificationReferences( declaration.getIdentifier() );

            for ( Map.Entry<String, List<Node<SpecificationReference>>> e :
                  specificationReferenceDeclarations.entrySet() )
            {
                for ( int i = 0, s0 = e.getValue().size(); i < s0; i++ )
                {
                    addEffectiveDeclarationNode( this.effectiveSpecReferences, e.getValue().get( i ),
                                                 e.getValue().get( i ).getModelObject().getIdentifier() );

                }
            }

            final Map<String, List<Node<Dependency>>> dependencyDeclarations =
                this.getDeclaredDependencies( declaration.getIdentifier() );

            for ( Map.Entry<String, List<Node<Dependency>>> e : dependencyDeclarations.entrySet() )
            {
                for ( int i = 0, s0 = e.getValue().size(); i < s0; i++ )
                {
                    addEffectiveDeclarationNode( this.effectiveDependencies, e.getValue().get( i ),
                                                 e.getValue().get( i ).getModelObject().getName() );

                }
            }

            final Map<String, List<Node<Message>>> messageDeclarations =
                this.getDeclaredMessages( declaration.getIdentifier() );

            for ( Map.Entry<String, List<Node<Message>>> e : messageDeclarations.entrySet() )
            {
                for ( int i = 0, s0 = e.getValue().size(); i < s0; i++ )
                {
                    addEffectiveDeclarationNode( this.effectiveMessages, e.getValue().get( i ),
                                                 e.getValue().get( i ).getModelObject().getName() );

                }
            }

            final Map<String, List<Node<Property>>> propertyDeclarations =
                this.getDeclaredProperties( declaration.getIdentifier() );

            for ( Map.Entry<String, List<Node<Property>>> e : propertyDeclarations.entrySet() )
            {
                for ( int i = 0, s0 = e.getValue().size(); i < s0; i++ )
                {
                    addEffectiveDeclarationNode( this.effectiveProperties, e.getValue().get( i ),
                                                 e.getValue().get( i ).getModelObject().getName() );

                }
            }

            final Map<String, List<Node<ImplementationReference>>> implementationReferenceDeclarations =
                this.getDeclaredImplementationReferences( declaration.getIdentifier() );

            for ( Map.Entry<String, List<Node<ImplementationReference>>> e :
                  implementationReferenceDeclarations.entrySet() )
            {
                for ( int i = 0, s0 = e.getValue().size(); i < s0; i++ )
                {
                    addEffectiveDeclarationNode( this.effectiveImplReferences, e.getValue().get( i ),
                                                 e.getValue().get( i ).getModelObject().getIdentifier() );

                }
            }

            this.declareClassDeclarationModelObjects( declaration );

            final Map<String, List<Node<SpecificationReference>>> ancestorSpecificationReferences =
                this.effectiveSpecReferences.get( declaration.getIdentifier() );

            final Map<String, List<Node<Dependency>>> ancestorDependencies =
                this.effectiveDependencies.get( declaration.getIdentifier() );

            final Map<String, List<Node<Message>>> ancestorMessages =
                this.effectiveMessages.get( declaration.getIdentifier() );

            final Map<String, List<Node<Property>>> ancestorProperties =
                this.effectiveProperties.get( declaration.getIdentifier() );

            final Map<String, List<Node<ImplementationReference>>> ancestorImplementationReferences =
                this.effectiveImplReferences.get( declaration.getIdentifier() );

            for ( Implementation descendant : this.descendants.get( declaration.getIdentifier() ) )
            {
                if ( descendant.getIdentifier().equals( declaration.getIdentifier() ) )
                {
                    continue;
                }

                if ( ancestorSpecificationReferences != null )
                {
                    this.inheritSpecificationReferences( ancestorSpecificationReferences, descendant.getIdentifier() );
                }

                if ( ancestorDependencies != null )
                {
                    this.inheritDependencies( ancestorDependencies, descendant.getIdentifier() );
                }

                if ( ancestorProperties != null )
                {
                    this.inheritProperties( ancestorProperties, descendant.getIdentifier() );
                }

                if ( ancestorMessages != null )
                {
                    this.inheritMessages( ancestorMessages, descendant.getIdentifier() );
                }

                if ( ancestorImplementationReferences != null )
                {
                    this.inheritImplementationReferences(
                        ancestorImplementationReferences, descendant.getIdentifier() );

                }

                collectEffectiveNodes( descendant );
            }
        }

        private void declareClassDeclarationModelObjects( final Implementation implementation )
        {
            if ( this.classDeclaration != null )
            {
                final InheritanceModel classDeclarationModel =
                    this.context.getInheritanceModel( this.classDeclaration );

                Map<String, List<Node<Dependency>>> effDependencies =
                    this.effectiveDependencies.get( implementation.getIdentifier() );

                Map<String, List<Node<Message>>> effMessages =
                    this.effectiveMessages.get( implementation.getIdentifier() );

                Map<String, List<Node<Property>>> effProperties =
                    this.effectiveProperties.get( implementation.getIdentifier() );

                Map<String, List<Node<SpecificationReference>>> effSpecificationReferences =
                    this.effectiveSpecReferences.get( implementation.getIdentifier() );

                Map<String, List<Node<Dependency>>> declDependencies =
                    classDeclarationModel.getEffectiveDependencies();

                Map<String, List<Node<Message>>> declMessages =
                    classDeclarationModel.getEffectiveMessages();

                Map<String, List<Node<Property>>> declProperties =
                    classDeclarationModel.getEffectiveProperties();

                Map<String, List<Node<SpecificationReference>>> declSpecReferences =
                    classDeclarationModel.getEffectiveSpecificationReferences();

                if ( declDependencies != null )
                {
                    if ( effDependencies == null )
                    {
                        effDependencies = newMap();
                        this.effectiveDependencies.put( implementation.getIdentifier(), effDependencies );
                    }

                    for ( Map.Entry<String, List<Node<Dependency>>> e : declDependencies.entrySet() )
                    {
                        if ( !effDependencies.containsKey( e.getKey() ) )
                        {
                            final List<Node<Dependency>> list = newList( e.getValue().size() );

                            for ( int i = 0, s0 = e.getValue().size(); i < s0; i++ )
                            {
                                final Node<Dependency> n = e.getValue().get( i );
                                final List<String> effPath = newList();
                                effPath.add( implementation.getIdentifier() );
                                effPath.add( "CLASS_DECL:" + n.getImplementation().getIdentifier() ); // pathToString
                                effPath.addAll( n.getPath().subList( 1, n.getPath().size() ) );

                                final Node<Dependency> effNode = new Node<Dependency>(
                                    implementation, null, n.getImplementation(), n.getModelObject(), n.isFinal(),
                                    n.isOverride(), effPath );

                                list.add( effNode );
                            }

                            effDependencies.put( e.getKey(), list );
                        }
                    }
                }

                if ( declSpecReferences != null )
                {
                    if ( effSpecificationReferences == null )
                    {
                        effSpecificationReferences = newMap();
                        this.effectiveSpecReferences.put( implementation.getIdentifier(), effSpecificationReferences );
                    }

                    for ( Map.Entry<String, List<Node<SpecificationReference>>> e : declSpecReferences.entrySet() )
                    {
                        if ( !effSpecificationReferences.containsKey( e.getKey() ) )
                        {
                            final List<Node<SpecificationReference>> list = newList( e.getValue().size() );

                            for ( int i = 0, s0 = e.getValue().size(); i < s0; i++ )
                            {
                                final Node<SpecificationReference> n = e.getValue().get( i );
                                final List<String> effPath = newList();
                                effPath.add( implementation.getIdentifier() );
                                effPath.add( "CLASS_DECL:" + n.getImplementation().getIdentifier() ); // pathToString
                                effPath.addAll( n.getPath().subList( 1, n.getPath().size() ) );

                                final Node<SpecificationReference> effNode = new Node<SpecificationReference>(
                                    implementation, null, n.getImplementation(), n.getModelObject(), n.isFinal(),
                                    n.isOverride(), effPath );

                                list.add( effNode );
                            }

                            effSpecificationReferences.put( e.getKey(), list );
                        }
                    }
                }

                if ( declMessages != null )
                {
                    if ( effMessages == null )
                    {
                        effMessages = newMap();
                        this.effectiveMessages.put( implementation.getIdentifier(), effMessages );
                    }

                    for ( Map.Entry<String, List<Node<Message>>> e : declMessages.entrySet() )
                    {
                        if ( !effMessages.containsKey( e.getKey() ) )
                        {
                            final List<Node<Message>> list = newList( e.getValue().size() );

                            for ( int i = 0, s0 = e.getValue().size(); i < s0; i++ )
                            {
                                final Node<Message> n = e.getValue().get( i );
                                final List<String> effPath = newList();
                                effPath.add( implementation.getIdentifier() );
                                effPath.add( "CLASS_DECL:" + n.getImplementation().getIdentifier() ); // pathToString
                                effPath.addAll( n.getPath().subList( 1, n.getPath().size() ) );

                                final Node<Message> effNode = new Node<Message>(
                                    implementation, null, n.getImplementation(), n.getModelObject(), n.isFinal(),
                                    n.isOverride(), effPath );

                                list.add( effNode );
                            }

                            effMessages.put( e.getKey(), list );
                        }
                    }
                }

                if ( declProperties != null )
                {
                    if ( effProperties == null )
                    {
                        effProperties = newMap();
                        this.effectiveProperties.put( implementation.getIdentifier(), effProperties );
                    }

                    for ( Map.Entry<String, List<Node<Property>>> e : declProperties.entrySet() )
                    {
                        if ( !effProperties.containsKey( e.getKey() ) )
                        {
                            final List<Node<Property>> list = newList( e.getValue().size() );

                            for ( int i = 0, s0 = e.getValue().size(); i < s0; i++ )
                            {
                                final Node<Property> n = e.getValue().get( i );
                                final List<String> effPath = newList();
                                effPath.add( implementation.getIdentifier() );
                                effPath.add( "CLASS_DECL:" + n.getImplementation().getIdentifier() ); // pathToString
                                effPath.addAll( n.getPath().subList( 1, n.getPath().size() ) );

                                final Node<Property> effNode = new Node<Property>(
                                    implementation, null, n.getImplementation(), n.getModelObject(), n.isFinal(),
                                    n.isOverride(), effPath );

                                list.add( effNode );
                            }

                            effProperties.put( e.getKey(), list );
                        }
                    }
                }
            }
        }

        private Implementation findClassDeclaration( final Implementation implementation )
        {
            Implementation declaration = null;

            if ( implementation.getClazz() != null && !implementation.isClassDeclaration() )
            {
                find:
                for ( int i = 0, s0 = this.context.getModules().getModule().size(); i < s0; i++ )
                {
                    final Module candidateModule = this.context.getModules().getModule().get( i );

                    if ( candidateModule.getImplementations() != null )
                    {
                        for ( int j = 0, s1 = candidateModule.getImplementations().getImplementation().size();
                              j < s1; j++ )
                        {
                            final Implementation candidate =
                                candidateModule.getImplementations().getImplementation().get( j );

                            if ( candidate.isClassDeclaration()
                                 && candidate.getClazz().equals( implementation.getClazz() ) )
                            {
                                declaration = candidate;
                                break find;
                            }
                        }
                    }
                }
            }

            return declaration;
        }

        private static <T extends ModelObject> void addNode(
            final Map<String, List<Node<T>>> map, final Node<T> node, final String key )
        {
            List<Node<T>> list = map.get( key );
            if ( list == null )
            {
                list = newList();
                map.put( key, list );
            }

            list.add( node );
        }

        private static <T extends ModelObject> void addEffectiveDeclarationNode(
            final Map<String, Map<String, List<Node<T>>>> map, final Node<T> node, final String nodeKey )
        {
            Map<String, List<Node<T>>> nodeMap = map.get( node.getImplementation().getIdentifier() );

            if ( nodeMap == null )
            {
                nodeMap = newMap();
                map.put( node.getImplementation().getIdentifier(), nodeMap );
            }

            List<Node<T>> list = nodeMap.get( nodeKey );
            if ( list == null )
            {
                list = newList();
                nodeMap.put( nodeKey, list );
            }

            boolean found = false;
            final Set<Node<T>> overrides = newSet();

            node:
            for ( final Iterator<Node<T>> it = list.iterator(); it.hasNext(); )
            {
                final Node<T> n = it.next();

                if ( !n.isScope( node.getImplementation() ) )
                {
                    if ( node.getSpecification() != null )
                    {
                        boolean overridden = false;

                        for ( Node<T> override : n.getOverrides() )
                        {
                            if ( override.getSpecification() != null && override.getSpecification().getIdentifier().
                                equals( node.getSpecification().getIdentifier() ) )
                            {
                                overridden = true;
                                break;
                            }
                        }

                        if ( overridden )
                        {
                            n.getOverrides().add( node );
                            found = true;
                        }
                    }
                    else
                    {
                        it.remove();
                        overrides.add( n );
                    }

                    continue node;
                }

                if ( n.getSpecification() == null )
                {
                    found = true;
                    n.getOverrides().add( node );
                    continue node;
                }

                if ( node.getSpecification() != null
                     && n.getSpecification().getIdentifier().equals( node.getSpecification().getIdentifier() ) )
                {
                    found = true;
                    n.getOverrides().add( node );
                    continue node;
                }
            }

            if ( !found )
            {
                node.getOverrides().addAll( overrides );
                list.add( node );
            }
        }

        private static <T extends ModelObject> Map<String, List<Node<T>>> getDeclarationNodes(
            final Map<String, List<Node<T>>> map, final String origin )
        {
            final Map<String, List<Node<T>>> declarationMap = newMap( map.size() );

            for ( Map.Entry<String, List<Node<T>>> e : map.entrySet() )
            {
                for ( int i = 0, s0 = e.getValue().size(); i < s0; i++ )
                {
                    final Node<T> n = e.getValue().get( i );

                    if ( n.isScope( origin ) )
                    {
                        List<Node<T>> list = declarationMap.get( e.getKey() );

                        if ( list == null )
                        {
                            list = newList();
                            declarationMap.put( e.getKey(), list );
                        }

                        list.add( n );
                    }
                }
            }

            return declarationMap;
        }

        private static <T extends ModelObject> void inheritModelObjects(
            final Map<String, Map<String, List<Node<T>>>> effective, final Map<String, List<Node<T>>> ancestor,
            final String descendant )
        {
            Map<String, List<Node<T>>> descendantModelObjects = effective.get( descendant );

            if ( descendantModelObjects == null )
            {
                descendantModelObjects = newMap();
                effective.put( descendant, descendantModelObjects );
            }

            for ( Map.Entry<String, List<Node<T>>> e : ancestor.entrySet() )
            {
                List<Node<T>> list = descendantModelObjects.get( e.getKey() );

                if ( list == null )
                {
                    list = newList();
                    descendantModelObjects.put( e.getKey(), list );
                }

                for ( int i = 0, s0 = e.getValue().size(); i < s0; i++ )
                {
                    boolean overridden = false;
                    final Node<T> inherit = e.getValue().get( i );

                    if ( inherit.getClassDeclaration() == null )
                    {
                        for ( int j = 0, s1 = list.size(); j < s1; j++ )
                        {
                            final Node<T> n = list.get( j );
                            if ( n.isScope( inherit.getImplementation() ) )
                            {
                                overridden = true;
                                break;
                            }
                        }

                        if ( !overridden )
                        {
                            list.add( inherit );
                        }
                    }
                }
            }
        }

        private static <T extends ModelObject> boolean isAncestorModelObjectOverridden(
            final Map<String, List<Node<T>>> modelObjects, final String implementation, final String modelObject )
        {
            List<Node<T>> nodes = modelObjects.get( modelObject );

            if ( nodes != null )
            {
                for ( int i = 0, s0 = nodes.size(); i < s0; i++ )
                {
                    final Node<T> node = nodes.get( i );
                    if ( !node.isScope( implementation ) || node.getSpecification() != null )
                    {
                        return true;
                    }
                }
            }

            return false;
        }

        private static <T extends ModelObject> boolean isFinalAncestorModelObjectOverridden(
            final Map<String, List<Node<T>>> modelObjects, final String implementation, final String modelObject )
        {
            List<Node<T>> nodes = modelObjects.get( modelObject );

            if ( nodes != null )
            {
                for ( int i = 0, s0 = nodes.size(); i < s0; i++ )
                {
                    final Node<T> node = nodes.get( i );
                    if ( node.isFinal() && ( !node.isScope( implementation ) || node.getSpecification() != null ) )
                    {
                        return true;
                    }
                }
            }

            return false;
        }

        private static <T extends ModelObject> boolean isEffectiveModelObjectOverridden(
            final Map<String, Map<String, List<Node<T>>>> modelObjects, final String implementation,
            final String modelObject )
        {
            return modelObjects.containsKey( implementation )
                   && modelObjects.get( implementation ).containsKey( modelObject );

        }

        private static <T extends ModelObject> boolean isFinalEffectiveModelObjectOverridden(
            final Map<String, Map<String, List<Node<T>>>> modelObjects, final String implementation,
            final String modelObject )
        {
            Map<String, List<Node<T>>> map = modelObjects.get( implementation );

            if ( map != null )
            {
                List<Node<T>> nodes = map.get( modelObject );

                if ( nodes != null )
                {
                    for ( int i = 0, s0 = nodes.size(); i < s0; i++ )
                    {
                        final Node<T> n = nodes.get( i );
                        if ( n.isFinal() )
                        {
                            return true;
                        }
                    }
                }
            }

            return false;
        }

        private static <T> List<T> newList()
        {
            return new ArrayList<T>();
        }

        private static <T> List<T> newList( final int initialCapacity )
        {
            return new ArrayList<T>( initialCapacity );
        }

        private static <K, V> Map<K, V> newMap()
        {
            return new HashMap<K, V>();
        }

        private static <K, V> Map<K, V> newMap( final int initialCapacity )
        {
            return new HashMap<K, V>( initialCapacity );
        }

        private static <T> Set<T> newSet()
        {
            return new HashSet<T>();
        }

        private static <T> Set<T> newSet( final int initialCapacity )
        {
            return new HashSet<T>( initialCapacity );
        }

    }
}
