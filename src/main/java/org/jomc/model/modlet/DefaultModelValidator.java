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
import java.util.Collection;
import java.util.Collections;
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
import javax.xml.namespace.QName;
import javax.xml.transform.Source;
import org.jomc.model.Dependency;
import org.jomc.model.Implementation;
import org.jomc.model.ImplementationReference;
import org.jomc.model.Implementations;
import org.jomc.model.Inheritable;
import org.jomc.model.InheritanceModel;
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
import org.w3c.dom.Element;

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

            if ( modules != null )
            {
                final ValidationContext validationContext = new ValidationContext( context, modules, report );
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
                final InheritanceModel imodel = validationContext.getInheritanceModel();
                final List<String> cyclePath = new LinkedList<String>();
                final Module moduleOfImpl =
                    validationContext.getModules().getModuleOfImplementation( impl.getIdentifier() );

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
                               moduleOfImpl.getName(), b.substring( " -> ".length() ) );

                }

                if ( impl.isClassDeclaration() )
                {
                    if ( impl.getClazz() == null )
                    {
                        addDetail( validationContext.getReport(), "IMPLEMENTATION_CLASS_CONSTRAINT", Level.SEVERE,
                                   new ObjectFactory().createImplementation( impl ), "implementationClassConstraint",
                                   impl.getIdentifier(), moduleOfImpl.getName() );

                    }
                    else
                    {
                        final Implementation prev = implementationClassDeclarations.get( impl.getClazz() );

                        if ( prev != null && !prev.getIdentifier().equals( impl.getIdentifier() ) )
                        {
                            final Module moduleOfPrev =
                                validationContext.getModules().getModuleOfImplementation( prev.getIdentifier() );

                            addDetail( validationContext.getReport(), "IMPLEMENTATION_CLASS_DECLARATION_CONSTRAINT",
                                       Level.SEVERE, new ObjectFactory().createImplementation( impl ),
                                       "implementationClassDeclarationConstraint", impl.getIdentifier(),
                                       moduleOfImpl.getName(), impl.getClazz(), prev.getIdentifier(),
                                       moduleOfPrev.getName() );

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
                               moduleOfImpl.getName(), impl.getLocation() );

                }

                if ( impl.getDependencies() != null )
                {
                    for ( int j = 0, s1 = impl.getDependencies().getDependency().size(); j < s1; j++ )
                    {
                        final Dependency d = impl.getDependencies().getDependency().get( j );

                        final Set<InheritanceModel.Node<Dependency>> effDependencies =
                            imodel.getEffectiveDependencyNodes( impl.getIdentifier(), d.getName() );

                        for ( final InheritanceModel.Node<Dependency> effDependency : effDependencies )
                        {
                            final Set<InheritanceModel.Node<Dependency>> overriddenDependencies =
                                modifiableSet( effDependency.getOverriddenNodes() );

                            if ( d.isOverride() && effDependency.getOverriddenNodes().isEmpty() )
                            {
                                addDetail( validationContext.getReport(),
                                           "IMPLEMENTATION_DEPENDENCY_OVERRIDE_CONSTRAINT",
                                           Level.SEVERE, new ObjectFactory().createImplementation( impl ),
                                           "implementationDependencyOverrideConstraint", impl.getIdentifier(),
                                           moduleOfImpl.getName(), d.getName() );

                            }

                            if ( !( d.isOverride() || overriddenDependencies.isEmpty() ) )
                            {
                                for ( final InheritanceModel.Node<Dependency> overriddenDependency :
                                      overriddenDependencies )
                                {
                                    Implementation overriddenImplementation = overriddenDependency.getImplementation();
                                    if ( overriddenDependency.getClassDeclaration() != null )
                                    {
                                        overriddenImplementation = overriddenDependency.getClassDeclaration();
                                    }

                                    final Module moduleOfDependency =
                                        validationContext.getModules().getModuleOfImplementation(
                                        overriddenImplementation.getIdentifier() );

                                    addDetail( validationContext.getReport(),
                                               "IMPLEMENTATION_DEPENDENCY_OVERRIDE_WARNING",
                                               Level.WARNING, new ObjectFactory().createImplementation( impl ),
                                               "implementationDependencyOverrideWarning", impl.getIdentifier(),
                                               moduleOfImpl.getName(), d.getName(),
                                               overriddenImplementation.getIdentifier(),
                                               moduleOfDependency.getName(),
                                               getNodePathString( overriddenDependency ) );

                                }
                            }

                            retainFinalNodes( overriddenDependencies );

                            for ( final InheritanceModel.Node<Dependency> overriddenDependency :
                                  overriddenDependencies )
                            {
                                Implementation overriddenImplementation = overriddenDependency.getImplementation();
                                if ( overriddenDependency.getClassDeclaration() != null )
                                {
                                    overriddenImplementation = overriddenDependency.getClassDeclaration();
                                }

                                final Module moduleOfDependency =
                                    validationContext.getModules().getModuleOfImplementation(
                                    overriddenImplementation.getIdentifier() );

                                addDetail( validationContext.getReport(),
                                           "IMPLEMENTATION_DEPENDENCY_INHERITANCE_CONSTRAINT", Level.SEVERE,
                                           new ObjectFactory().createImplementation( impl ),
                                           "implementationDependencyFinalConstraint", impl.getIdentifier(),
                                           moduleOfImpl.getName(), d.getName(),
                                           overriddenImplementation.getIdentifier(),
                                           moduleOfDependency.getName(),
                                           getNodePathString( overriddenDependency ) );

                            }
                        }

                        assertDependencyValid( validationContext, impl, d );
                    }
                }

                if ( impl.getImplementations() != null )
                {
                    final Set<InheritanceModel.Node<Implementation>> finalImplementations = retainFinalNodes(
                        modifiableSet( imodel.getImplementationNodes( impl.getIdentifier() ) ) );

                    for ( final InheritanceModel.Node<Implementation> finalImplementation : finalImplementations )
                    {
                        if ( !finalImplementation.getModelObject().getIdentifier().equals( impl.getIdentifier() ) )
                        {
                            final Module moduleOfFinal = validationContext.getModules().getModuleOfImplementation(
                                finalImplementation.getModelObject().getIdentifier() );

                            addDetail( validationContext.getReport(),
                                       "IMPLEMENTATION_IMPLEMENTATION_INHERITANCE_CONSTRAINT", Level.SEVERE,
                                       new ObjectFactory().createImplementation( impl ),
                                       "implementationFinalImplementationConstraint", impl.getIdentifier(),
                                       moduleOfImpl.getName(), finalImplementation.getModelObject().getIdentifier(),
                                       moduleOfFinal.getName() );

                        }
                    }

                    for ( int j = 0, s1 = impl.getImplementations().getImplementation().size(); j < s1; j++ )
                    {
                        final Implementation pi = impl.getImplementations().getImplementation().get( j );

                        addDetail( validationContext.getReport(),
                                   "IMPLEMENTATION_IMPLEMENTATION_DECLARATION_CONSTRAINT", Level.SEVERE,
                                   new ObjectFactory().createImplementation( impl ),
                                   "implementationImplementationDeclarationConstraint", impl.getIdentifier(),
                                   moduleOfImpl.getName(), pi.getIdentifier() );

                    }

                    for ( int j = 0, s1 = impl.getImplementations().getReference().size(); j < s1; j++ )
                    {
                        final ImplementationReference r = impl.getImplementations().getReference().get( j );

                        final Set<InheritanceModel.Node<ImplementationReference>> effReferences =
                            imodel.getEffectiveImplementationReferenceNodes( impl.getIdentifier(), r.getIdentifier() );

                        for ( final InheritanceModel.Node<ImplementationReference> effReference : effReferences )
                        {
                            final Set<InheritanceModel.Node<ImplementationReference>> overriddenReferences =
                                modifiableSet( effReference.getOverriddenNodes() );

                            if ( r.isOverride() && overriddenReferences.isEmpty() )
                            {
                                addDetail( validationContext.getReport(),
                                           "IMPLEMENTATION_IMPLEMENTATION_OVERRIDE_CONSTRAINT", Level.SEVERE,
                                           new ObjectFactory().createImplementation( impl ),
                                           "implementationImplementationOverrideConstraint", impl.getIdentifier(),
                                           moduleOfImpl.getName(), r.getIdentifier() );

                            }

                            if ( !( r.isOverride() || overriddenReferences.isEmpty() ) )
                            {
                                for ( final InheritanceModel.Node<ImplementationReference> overriddenReference :
                                      overriddenReferences )
                                {
                                    Implementation overriddenImplementation = overriddenReference.getImplementation();
                                    if ( overriddenReference.getClassDeclaration() != null )
                                    {
                                        overriddenImplementation = overriddenReference.getClassDeclaration();
                                    }

                                    final Module moduleOfReference =
                                        validationContext.getModules().getModuleOfImplementation(
                                        overriddenImplementation.getIdentifier() );

                                    addDetail( validationContext.getReport(),
                                               "IMPLEMENTATION_IMPLEMENTATION_REFERENCE_OVERRIDE_WARNING",
                                               Level.WARNING, new ObjectFactory().createImplementation( impl ),
                                               "implementationImplementationOverrideWarning", impl.getIdentifier(),
                                               moduleOfImpl.getName(), r.getIdentifier(),
                                               overriddenImplementation.getIdentifier(),
                                               moduleOfReference.getName(),
                                               getNodePathString( overriddenReference ) );

                                }
                            }

                            retainFinalNodes( overriddenReferences );

                            for ( final InheritanceModel.Node<ImplementationReference> overriddenReference :
                                  overriddenReferences )
                            {
                                Implementation overriddenImplementation = overriddenReference.getImplementation();
                                if ( overriddenReference.getClassDeclaration() != null )
                                {
                                    overriddenImplementation = overriddenReference.getClassDeclaration();
                                }

                                final Module moduleOfReference =
                                    validationContext.getModules().getModuleOfImplementation(
                                    overriddenImplementation.getIdentifier() );

                                addDetail( validationContext.getReport(),
                                           "IMPLEMENTATION_IMPLEMENTATION_REFERENCE_INHERITANCE_CONSTRAINT",
                                           Level.SEVERE, new ObjectFactory().createImplementation( impl ),
                                           "implementationFinalImplementatioReferenceConstraint", impl.getIdentifier(),
                                           moduleOfImpl.getName(), r.getIdentifier(),
                                           overriddenImplementation.getIdentifier(),
                                           moduleOfReference.getName(), getNodePathString( overriddenReference ) );

                            }
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
                                       moduleOfImpl.getName(), m.getName() );

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
                                               moduleOfImpl.getName(), m.getName(), t.getValue(),
                                               message != null && message.length() > 0 ? " " + message : "" );

                                }
                            }
                        }

                        final Set<InheritanceModel.Node<Message>> effMessages =
                            imodel.getEffectiveMessageNodes( impl.getIdentifier(), m.getName() );

                        for ( final InheritanceModel.Node<Message> effMessage : effMessages )
                        {
                            final Set<InheritanceModel.Node<Message>> overriddenMessages =
                                modifiableSet( effMessage.getOverriddenNodes() );

                            if ( m.isOverride() && overriddenMessages.isEmpty() )
                            {
                                addDetail( validationContext.getReport(), "IMPLEMENTATION_MESSAGE_OVERRIDE_CONSTRAINT",
                                           Level.SEVERE, new ObjectFactory().createImplementation( impl ),
                                           "implementationMessageOverrideConstraint", impl.getIdentifier(),
                                           moduleOfImpl.getName(), m.getName() );

                            }

                            if ( !( m.isOverride() || overriddenMessages.isEmpty() ) )
                            {
                                for ( final InheritanceModel.Node<Message> overriddenMessage : overriddenMessages )
                                {
                                    Implementation overriddenImplementation = overriddenMessage.getImplementation();
                                    if ( overriddenMessage.getClassDeclaration() != null )
                                    {
                                        overriddenImplementation = overriddenMessage.getClassDeclaration();
                                    }

                                    final Module moduleOfMessage =
                                        validationContext.getModules().getModuleOfImplementation(
                                        overriddenImplementation.getIdentifier() );

                                    addDetail( validationContext.getReport(), "IMPLEMENTATION_MESSAGE_OVERRIDE_WARNING",
                                               Level.WARNING, new ObjectFactory().createImplementation( impl ),
                                               "implementationMessageOverrideWarning", impl.getIdentifier(),
                                               moduleOfImpl.getName(), m.getName(),
                                               overriddenImplementation.getIdentifier(),
                                               moduleOfMessage.getName(), getNodePathString( overriddenMessage ) );

                                }
                            }

                            retainFinalNodes( overriddenMessages );

                            for ( final InheritanceModel.Node<Message> overriddenMessage : overriddenMessages )
                            {
                                Implementation overriddenImplementation = overriddenMessage.getImplementation();
                                if ( overriddenMessage.getClassDeclaration() != null )
                                {
                                    overriddenImplementation = overriddenMessage.getClassDeclaration();
                                }

                                final Module moduleOfMessage = validationContext.getModules().getModuleOfImplementation(
                                    overriddenImplementation.getIdentifier() );

                                addDetail( validationContext.getReport(),
                                           "IMPLEMENTATION_MESSAGE_INHERITANCE_CONSTRAINT",
                                           Level.SEVERE, new ObjectFactory().createImplementation( impl ),
                                           "implementationMessageFinalConstraint", impl.getIdentifier(),
                                           moduleOfImpl.getName(), m.getName(),
                                           overriddenImplementation.getIdentifier(),
                                           moduleOfMessage.getName(), getNodePathString( overriddenMessage ) );

                            }
                        }
                    }

                    for ( int j = 0, s1 = impl.getMessages().getReference().size(); j < s1; j++ )
                    {
                        final MessageReference r = impl.getMessages().getReference().get( j );

                        final Set<InheritanceModel.Node<Message>> effMessages =
                            imodel.getEffectiveMessageNodes( impl.getIdentifier(), r.getName() );

                        for ( final InheritanceModel.Node<Message> effMessage : effMessages )
                        {
                            final Set<InheritanceModel.Node<Message>> overriddenMessages =
                                modifiableSet( effMessage.getOverriddenNodes() );

                            if ( r.isOverride() && overriddenMessages.isEmpty() )
                            {
                                addDetail( validationContext.getReport(), "IMPLEMENTATION_MESSAGE_OVERRIDE_CONSTRAINT",
                                           Level.SEVERE, new ObjectFactory().createImplementation( impl ),
                                           "implementationMessageOverrideConstraint", impl.getIdentifier(),
                                           r.getName() );

                            }

                            if ( !( r.isOverride() || overriddenMessages.isEmpty() ) )
                            {
                                for ( final InheritanceModel.Node<Message> overriddenMessage : overriddenMessages )
                                {
                                    Implementation overriddenImplementation = overriddenMessage.getImplementation();
                                    if ( overriddenMessage.getClassDeclaration() != null )
                                    {
                                        overriddenImplementation = overriddenMessage.getClassDeclaration();
                                    }

                                    final Module moduleOfMessage =
                                        validationContext.getModules().getModuleOfImplementation(
                                        overriddenImplementation.getIdentifier() );

                                    addDetail( validationContext.getReport(), "IMPLEMENTATION_MESSAGE_OVERRIDE_WARNING",
                                               Level.WARNING, new ObjectFactory().createImplementation( impl ),
                                               "implementationMessageOverrideWarning", impl.getIdentifier(),
                                               r.getName(), overriddenImplementation.getIdentifier(),
                                               moduleOfMessage.getName(), getNodePathString( overriddenMessage ) );

                                }
                            }

                            retainFinalNodes( overriddenMessages );

                            for ( final InheritanceModel.Node<Message> overriddenMessage : overriddenMessages )
                            {
                                Implementation overriddenImplementation = overriddenMessage.getImplementation();
                                if ( overriddenMessage.getClassDeclaration() != null )
                                {
                                    overriddenImplementation = overriddenMessage.getClassDeclaration();
                                }

                                final Module moduleOfMessage =
                                    validationContext.getModules().getModuleOfImplementation(
                                    overriddenImplementation.getIdentifier() );

                                addDetail( validationContext.getReport(),
                                           "IMPLEMENTATION_MESSAGE_INHERITANCE_CONSTRAINT",
                                           Level.SEVERE, new ObjectFactory().createImplementation( impl ),
                                           "implementationMessageFinalConstraint", impl.getIdentifier(), r.getName(),
                                           overriddenImplementation.getIdentifier(),
                                           moduleOfMessage.getName(), getNodePathString( overriddenMessage ) );

                            }
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
                                       moduleOfImpl.getName(), p.getName() );

                        }

                        if ( p.getValue() != null && p.getAny() != null )
                        {
                            addDetail( validationContext.getReport(), "IMPLEMENTATION_PROPERTY_VALUE_CONSTRAINT",
                                       Level.SEVERE, new ObjectFactory().createImplementation( impl ),
                                       "implementationPropertyValueConstraint", impl.getIdentifier(),
                                       moduleOfImpl.getName(), p.getName() );

                        }

                        if ( p.getAny() != null && p.getType() == null )
                        {
                            addDetail( validationContext.getReport(), "IMPLEMENTATION_PROPERTY_TYPE_CONSTRAINT",
                                       Level.SEVERE, new ObjectFactory().createImplementation( impl ),
                                       "implementationPropertyTypeConstraint", impl.getIdentifier(),
                                       moduleOfImpl.getName(), p.getName() );

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
                                       "implementationPropertyJavaValueConstraint", impl.getIdentifier(),
                                       moduleOfImpl.getName(), p.getName(),
                                       message != null && message.length() > 0 ? " " + message : "" );

                        }

                        final Set<InheritanceModel.Node<Property>> effProperties =
                            imodel.getEffectivePropertyNodes( impl.getIdentifier(), p.getName() );

                        for ( final InheritanceModel.Node<Property> effProperty : effProperties )
                        {
                            final Set<InheritanceModel.Node<Property>> overriddenProperties =
                                modifiableSet( effProperty.getOverriddenNodes() );

                            if ( p.isOverride() && overriddenProperties.isEmpty() )
                            {
                                addDetail( validationContext.getReport(), "IMPLEMENTATION_PROPERTY_OVERRIDE_CONSTRAINT",
                                           Level.SEVERE, new ObjectFactory().createImplementation( impl ),
                                           "implementationPropertyOverrideConstraint", impl.getIdentifier(),
                                           p.getName() );

                            }

                            if ( !( p.isOverride() || overriddenProperties.isEmpty() ) )
                            {
                                for ( final InheritanceModel.Node<Property> overriddenProperty : overriddenProperties )
                                {
                                    Implementation overriddenImplementation = overriddenProperty.getImplementation();
                                    if ( overriddenProperty.getClassDeclaration() != null )
                                    {
                                        overriddenImplementation = overriddenProperty.getClassDeclaration();
                                    }

                                    final Module moduleOfProperty =
                                        validationContext.getModules().getModuleOfImplementation(
                                        overriddenImplementation.getIdentifier() );

                                    addDetail( validationContext.getReport(),
                                               "IMPLEMENTATION_PROPERTY_OVERRIDE_WARNING", Level.WARNING,
                                               new ObjectFactory().createImplementation( impl ),
                                               "implementationPropertyOverrideWarning", impl.getIdentifier(),
                                               moduleOfImpl.getName(), p.getName(),
                                               overriddenImplementation.getIdentifier(),
                                               moduleOfProperty.getName(), getNodePathString( overriddenProperty ) );

                                }
                            }

                            retainFinalNodes( overriddenProperties );

                            for ( final InheritanceModel.Node<Property> overriddenProperty : overriddenProperties )
                            {
                                Implementation overriddenImplementation = overriddenProperty.getImplementation();
                                if ( overriddenProperty.getClassDeclaration() != null )
                                {
                                    overriddenImplementation = overriddenProperty.getClassDeclaration();
                                }

                                final Module moduleOfProperty =
                                    validationContext.getModules().getModuleOfImplementation(
                                    overriddenImplementation.getIdentifier() );

                                addDetail( validationContext.getReport(),
                                           "IMPLEMENTATION_PROPERTY_INHERITANCE_CONSTRAINT",
                                           Level.SEVERE, new ObjectFactory().createImplementation( impl ),
                                           "implementationPropertyFinalConstraint", impl.getIdentifier(),
                                           moduleOfImpl.getName(), p.getName(),
                                           overriddenImplementation.getIdentifier(),
                                           moduleOfProperty.getName(), getNodePathString( overriddenProperty ) );

                            }
                        }
                    }

                    for ( int j = 0, s1 = impl.getProperties().getReference().size(); j < s1; j++ )
                    {
                        final PropertyReference r = impl.getProperties().getReference().get( j );

                        final Set<InheritanceModel.Node<Property>> effProperties =
                            imodel.getEffectivePropertyNodes( impl.getIdentifier(), r.getName() );

                        for ( final InheritanceModel.Node<Property> effProperty : effProperties )
                        {
                            final Set<InheritanceModel.Node<Property>> overriddenProperties =
                                modifiableSet( effProperty.getOverriddenNodes() );

                            if ( r.isOverride() && overriddenProperties.isEmpty() )
                            {
                                addDetail( validationContext.getReport(), "IMPLEMENTATION_PROPERTY_OVERRIDE_CONSTRAINT",
                                           Level.SEVERE, new ObjectFactory().createImplementation( impl ),
                                           "implementationPropertyOverrideConstraint", impl.getIdentifier(),
                                           r.getName() );

                            }

                            if ( !( r.isOverride() || overriddenProperties.isEmpty() ) )
                            {
                                for ( final InheritanceModel.Node<Property> overriddenProperty : overriddenProperties )
                                {
                                    Implementation overriddenImplementation = overriddenProperty.getImplementation();
                                    if ( overriddenProperty.getClassDeclaration() != null )
                                    {
                                        overriddenImplementation = overriddenProperty.getClassDeclaration();
                                    }

                                    final Module moduleOfProperty =
                                        validationContext.getModules().getModuleOfImplementation(
                                        overriddenImplementation.getIdentifier() );

                                    addDetail( validationContext.getReport(),
                                               "IMPLEMENTATION_PROPERTY_OVERRIDE_WARNING", Level.WARNING,
                                               new ObjectFactory().createImplementation( impl ),
                                               "implementationPropertyOverrideWarning", impl.getIdentifier(),
                                               moduleOfImpl.getName(), r.getName(),
                                               overriddenImplementation.getIdentifier(),
                                               moduleOfProperty.getName(), getNodePathString( overriddenProperty ) );

                                }
                            }

                            retainFinalNodes( overriddenProperties );

                            for ( final InheritanceModel.Node<Property> overriddenProperty : overriddenProperties )
                            {
                                Implementation overriddenImplementation = overriddenProperty.getImplementation();
                                if ( overriddenProperty.getClassDeclaration() != null )
                                {
                                    overriddenImplementation = overriddenProperty.getClassDeclaration();
                                }

                                final Module moduleOfProperty =
                                    validationContext.getModules().getModuleOfImplementation(
                                    overriddenImplementation.getIdentifier() );

                                addDetail( validationContext.getReport(),
                                           "IMPLEMENTATION_PROPERTY_INHERITANCE_CONSTRAINT",
                                           Level.SEVERE, new ObjectFactory().createImplementation( impl ),
                                           "implementationPropertyFinalConstraint", impl.getIdentifier(),
                                           moduleOfImpl.getName(), r.getName(),
                                           overriddenImplementation.getIdentifier(),
                                           moduleOfProperty.getName(), getNodePathString( overriddenProperty ) );

                            }
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
                                   moduleOfImpl.getName(), s.getIdentifier() );

                    }

                    for ( int j = 0, s1 = impl.getSpecifications().getReference().size(); j < s1; j++ )
                    {
                        final SpecificationReference r = impl.getSpecifications().getReference().get( j );

                        final Set<InheritanceModel.Node<SpecificationReference>> effReferences =
                            imodel.getEffectiveSpecificationReferenceNodes( impl.getIdentifier(), r.getIdentifier() );

                        for ( final InheritanceModel.Node<SpecificationReference> effReference : effReferences )
                        {
                            final Set<InheritanceModel.Node<SpecificationReference>> overriddenReferences =
                                modifiableSet( effReference.getOverriddenNodes() );

                            if ( r.isOverride() && overriddenReferences.isEmpty() )
                            {
                                addDetail( validationContext.getReport(),
                                           "IMPLEMENTATION_SPECIFICATION_OVERRIDE_CONSTRAINT", Level.SEVERE,
                                           new ObjectFactory().createImplementation( impl ),
                                           "implementationSpecificationOverrideConstraint", impl.getIdentifier(),
                                           r.getIdentifier() );

                            }

                            if ( !( r.isOverride() || overriddenReferences.isEmpty() ) )
                            {
                                for ( final InheritanceModel.Node<SpecificationReference> overriddenReference :
                                      overriddenReferences )
                                {
                                    Implementation overriddenImplementation = overriddenReference.getImplementation();
                                    if ( overriddenReference.getClassDeclaration() != null )
                                    {
                                        overriddenImplementation = overriddenReference.getClassDeclaration();
                                    }

                                    final Module moduleOfReference =
                                        validationContext.getModules().getModuleOfImplementation(
                                        overriddenImplementation.getIdentifier() );

                                    addDetail( validationContext.getReport(),
                                               "IMPLEMENTATION_SPECIFICATION_REFERENCE_OVERRIDE_WARNING",
                                               Level.WARNING, new ObjectFactory().createImplementation( impl ),
                                               "implementationSpecificationOverrideWarning", impl.getIdentifier(),
                                               moduleOfImpl.getName(), r.getIdentifier(),
                                               overriddenImplementation.getIdentifier(),
                                               moduleOfReference.getName(), getNodePathString( overriddenReference ) );

                                }
                            }

                            retainFinalNodes( overriddenReferences );

                            for ( final InheritanceModel.Node<SpecificationReference> overriddenReference :
                                  overriddenReferences )
                            {
                                Implementation overriddenImplementation = overriddenReference.getImplementation();
                                if ( overriddenReference.getClassDeclaration() != null )
                                {
                                    overriddenImplementation = overriddenReference.getClassDeclaration();
                                }

                                final Module moduleOfReference =
                                    validationContext.getModules().getModuleOfImplementation(
                                    overriddenImplementation.getIdentifier() );

                                addDetail( validationContext.getReport(),
                                           "IMPLEMENTATION_SPECIFICATION_INHERITANCE_CONSTRAINT", Level.SEVERE,
                                           new ObjectFactory().createImplementation( impl ),
                                           "implementationSpecificationFinalConstraint", impl.getIdentifier(),
                                           moduleOfImpl.getName(), r.getIdentifier(),
                                           overriddenImplementation.getIdentifier(),
                                           moduleOfReference.getName(), getNodePathString( overriddenReference ) );

                            }
                        }
                    }
                }

                if ( !impl.getAny().isEmpty() )
                {
                    for ( int j = 0, s1 = impl.getAny().size(); j < s1; j++ )
                    {
                        final Object any = impl.getAny().get( j );

                        if ( any instanceof JAXBElement<?> )
                        {
                            final JAXBElement<?> jaxbElement = (JAXBElement<?>) any;
                            boolean overrideNode = false;

                            if ( jaxbElement.getValue() instanceof Inheritable )
                            {
                                overrideNode = ( (Inheritable) jaxbElement.getValue() ).isOverride();
                            }

                            final Set<InheritanceModel.Node<JAXBElement<?>>> effElements =
                                imodel.getEffectiveJaxbElementNodes( impl.getIdentifier(), jaxbElement.getName() );

                            for ( final InheritanceModel.Node<JAXBElement<?>> effElement : effElements )
                            {
                                final Set<InheritanceModel.Node<JAXBElement<?>>> overriddenElements =
                                    modifiableSet( effElement.getOverriddenNodes() );

                                if ( overrideNode && overriddenElements.isEmpty() )
                                {
                                    addDetail( validationContext.getReport(),
                                               "IMPLEMENTATION_JAXB_ELEMENT_OVERRIDE_CONSTRAINT",
                                               Level.SEVERE, new ObjectFactory().createImplementation( impl ),
                                               "implementationJaxbElementOverrideConstraint", impl.getIdentifier(),
                                               moduleOfImpl.getName(), jaxbElement.getName().toString() );

                                }

                                if ( !( overrideNode || overriddenElements.isEmpty() ) )
                                {
                                    for ( final InheritanceModel.Node<JAXBElement<?>> overriddenElement :
                                          overriddenElements )
                                    {
                                        Implementation overriddenImplementation = overriddenElement.getImplementation();
                                        if ( overriddenElement.getClassDeclaration() != null )
                                        {
                                            overriddenImplementation = overriddenElement.getClassDeclaration();
                                        }

                                        final Module moduleOfElement =
                                            validationContext.getModules().getModuleOfImplementation(
                                            overriddenElement.getImplementation().getIdentifier() );

                                        addDetail( validationContext.getReport(),
                                                   "IMPLEMENTATION_JAXB_ELEMENT_OVERRIDE_WARNING",
                                                   Level.WARNING, new ObjectFactory().createImplementation( impl ),
                                                   "implementationJaxbElementOverrideWarning", impl.getIdentifier(),
                                                   moduleOfImpl.getName(), jaxbElement.getName().toString(),
                                                   overriddenImplementation.getIdentifier(),
                                                   moduleOfElement.getName(), getNodePathString( overriddenElement ) );

                                    }
                                }

                                retainFinalNodes( overriddenElements );

                                for ( final InheritanceModel.Node<JAXBElement<?>> overriddenElement :
                                      overriddenElements )
                                {
                                    Implementation overriddenImplementation = overriddenElement.getImplementation();
                                    if ( overriddenElement.getClassDeclaration() != null )
                                    {
                                        overriddenImplementation = overriddenElement.getClassDeclaration();
                                    }

                                    final Module moduleOfElement =
                                        validationContext.getModules().getModuleOfImplementation(
                                        overriddenImplementation.getIdentifier() );

                                    addDetail( validationContext.getReport(),
                                               "IMPLEMENTATION_JAXB_ELEMENT_INHERITANCE_CONSTRAINT",
                                               Level.SEVERE, new ObjectFactory().createImplementation( impl ),
                                               "implementationJaxbElementFinalConstraint", impl.getIdentifier(),
                                               moduleOfImpl.getName(), jaxbElement.getName().toString(),
                                               overriddenImplementation.getIdentifier(),
                                               moduleOfElement.getName(), getNodePathString( overriddenElement ) );

                                }
                            }
                        }
                    }
                }

                final Set<String> dependencyNames = imodel.getDependencyNames( impl.getIdentifier() );

                for ( String dependencyName : dependencyNames )
                {
                    final Set<InheritanceModel.Node<Dependency>> dependencyNodes =
                        imodel.getEffectiveDependencyNodes( impl.getIdentifier(), dependencyName );

                    if ( dependencyNodes.size() > 1 )
                    {
                        addDetail( validationContext.getReport(),
                                   "IMPLEMENTATION_DEPENDENCY_MULTIPLE_INHERITANCE_CONSTRAINT",
                                   Level.SEVERE, new ObjectFactory().createImplementation( impl ),
                                   "implementationMultipleInheritanceDependencyConstraint", impl.getIdentifier(),
                                   moduleOfImpl.getName(), dependencyName, getNodeListPathString( dependencyNodes ) );

                    }
                }

                final Set<String> messageNames = imodel.getMessageNames( impl.getIdentifier() );

                for ( String messageName : messageNames )
                {
                    final Set<InheritanceModel.Node<Message>> messageNodes =
                        imodel.getEffectiveMessageNodes( impl.getIdentifier(), messageName );

                    if ( messageNodes.size() > 1 )
                    {
                        addDetail( validationContext.getReport(),
                                   "IMPLEMENTATION_MESSAGE_MULTIPLE_INHERITANCE_CONSTRAINT", Level.SEVERE,
                                   new ObjectFactory().createImplementation( impl ),
                                   "implementationMultipleInheritanceMessageConstraint", impl.getIdentifier(),
                                   moduleOfImpl.getName(), messageName, getNodeListPathString( messageNodes ) );

                    }
                }

                final Set<String> propertyNames = imodel.getPropertyNames( impl.getIdentifier() );

                for ( String propertyName : propertyNames )
                {
                    final Set<InheritanceModel.Node<Property>> propertyNodes =
                        imodel.getEffectivePropertyNodes( impl.getIdentifier(), propertyName );

                    if ( propertyNodes.size() > 1 )
                    {
                        addDetail( validationContext.getReport(),
                                   "IMPLEMENTATION_PROPERTY_MULTIPLE_INHERITANCE_CONSTRAINT", Level.SEVERE,
                                   new ObjectFactory().createImplementation( impl ),
                                   "implementationMultipleInheritancePropertyConstraint", impl.getIdentifier(),
                                   moduleOfImpl.getName(), propertyName, getNodeListPathString( propertyNodes ) );

                    }
                }

                final Set<String> specificationReferenceIdentifiers =
                    imodel.getSpecificationReferenceIdentifiers( impl.getIdentifier() );

                for ( String specificationRefereneIdentifier : specificationReferenceIdentifiers )
                {
                    final Set<InheritanceModel.Node<SpecificationReference>> specificationReferenceNodes =
                        imodel.getEffectiveSpecificationReferenceNodes(
                        impl.getIdentifier(), specificationRefereneIdentifier );

                    if ( specificationReferenceNodes.size() > 1 )
                    {
                        addDetail( validationContext.getReport(),
                                   "IMPLEMENTATION_SPECIFICATION_MULTIPLE_INHERITANCE_CONSTRAINT",
                                   Level.SEVERE, new ObjectFactory().createImplementation( impl ),
                                   "implementationMultipleInheritanceSpecificationConstraint",
                                   impl.getIdentifier(), moduleOfImpl.getName(), specificationRefereneIdentifier,
                                   getNodeListPathString( specificationReferenceNodes ) );

                    }
                }

                final Set<QName> xmlElementNames = imodel.getXmlElementNames( impl.getIdentifier() );

                for ( QName xmlElementName : xmlElementNames )
                {
                    final Set<InheritanceModel.Node<Element>> xmlElementNodes =
                        imodel.getEffectiveXmlElementNodes( impl.getIdentifier(), xmlElementName );

                    if ( xmlElementNodes.size() > 1 )
                    {
                        addDetail( validationContext.getReport(),
                                   "IMPLEMENTATION_XML_ELEMENT_MULTIPLE_INHERITANCE_CONSTRAINT",
                                   Level.SEVERE, new ObjectFactory().createImplementation( impl ),
                                   "implementationMultipleInheritanceXmlElementConstraint",
                                   impl.getIdentifier(), moduleOfImpl.getName(), xmlElementName.toString(),
                                   getNodeListPathString( xmlElementNodes ) );

                    }
                }

                final Set<QName> jaxbElementNames = imodel.getJaxbElementNames( impl.getIdentifier() );

                for ( QName jaxbElementName : jaxbElementNames )
                {
                    final Set<InheritanceModel.Node<JAXBElement<?>>> jaxbElementNodes =
                        imodel.getEffectiveJaxbElementNodes( impl.getIdentifier(), jaxbElementName );

                    if ( jaxbElementNodes.size() > 1 )
                    {
                        addDetail( validationContext.getReport(),
                                   "IMPLEMENTATION_JAXB_ELEMENT_MULTIPLE_INHERITANCE_CONSTRAINT",
                                   Level.SEVERE, new ObjectFactory().createImplementation( impl ),
                                   "implementationMultipleInheritanceJaxbElementConstraint",
                                   impl.getIdentifier(), moduleOfImpl.getName(), jaxbElementName.toString(),
                                   getNodeListPathString( jaxbElementNodes ) );

                    }
                }

                final Set<String> implementationReferenceIdentifiers =
                    imodel.getImplementationReferenceIdentifiers( impl.getIdentifier() );

                for ( String implementationReferenceIdentifier : implementationReferenceIdentifiers )
                {
                    final Set<InheritanceModel.Node<ImplementationReference>> implementationReferenceNodes =
                        imodel.getEffectiveImplementationReferenceNodes( impl.getIdentifier(),
                                                                         implementationReferenceIdentifier );

                    for ( final InheritanceModel.Node<ImplementationReference> node : implementationReferenceNodes )
                    {
                        final ImplementationReference r = node.getModelObject();

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
                                           impl.getIdentifier(), moduleOfImpl.getName(), r.getIdentifier(),
                                           moduleOfReferenced.getName() );

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
                                                   impl.getIdentifier(), moduleOfImpl.getName(),
                                                   referenced.getIdentifier(), moduleOfReferenced.getName(),
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
                                        impl.getIdentifier(), moduleOfImpl.getName(), r.getIdentifier(),
                                        moduleOfReferenced.getName(), r.getVersion(),
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
                                        impl.getIdentifier(), moduleOfImpl.getName(), r.getIdentifier(),
                                        moduleOfReferenced.getName(), r.getVersion(),
                                        message != null && message.length() > 0 ? " " + message : "" );

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
                final Module moduleOfS = validationContext.getModules().getModuleOfSpecification( s.getIdentifier() );

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
                        final Specification prev = specificationClassDeclarations.get( s.getClazz() );

                        if ( prev != null && !prev.getIdentifier().equals( s.getIdentifier() ) )
                        {
                            final Module moduleOfPrev = validationContext.getModules().getModuleOfSpecification(
                                prev.getIdentifier() );

                            addDetail( validationContext.getReport(), "SPECIFICATION_CLASS_DECLARATION_CONSTRAINT",
                                       Level.SEVERE, new ObjectFactory().createSpecification( s ),
                                       "specificationClassDeclarationConstraint", s.getIdentifier(),
                                       moduleOfS.getName(), s.getClazz(), prev.getIdentifier(),
                                       moduleOfPrev.getName() );

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
                                final Module moduleOfImpl = validationContext.getModules().getModuleOfImplementation(
                                    impl.getIdentifier() );

                                addDetail( validationContext.getReport(),
                                           "SPECIFICATION_IMPLEMENTATION_NAME_UNIQUENESS_CONSTRAINT",
                                           Level.SEVERE, new ObjectFactory().createImplementation( impl ),
                                           "specificationImplementationNameConstraint", impl.getIdentifier(),
                                           moduleOfImpl.getName(), s.getIdentifier(), moduleOfS.getName(),
                                           impl.getName() );

                            }
                        }
                    }

                    if ( s.getMultiplicity() == Multiplicity.ONE && impls.getImplementation().size() > 1 )
                    {
                        for ( int j = 0, s1 = impls.getImplementation().size(); j < s1; j++ )
                        {
                            final Implementation impl = impls.getImplementation().get( j );
                            final Module moduleOfImpl = validationContext.getModules().getModuleOfImplementation(
                                impl.getIdentifier() );

                            addDetail( validationContext.getReport(),
                                       "SPECIFICATION_IMPLEMENTATION_MULTIPLICITY_CONSTRAINT", Level.SEVERE,
                                       new ObjectFactory().createImplementation( impl ),
                                       "specificationMultiplicityConstraint", impl.getIdentifier(),
                                       moduleOfImpl.getName(), s.getIdentifier(), moduleOfS.getName(),
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
                                       "specificationPropertyJavaValueConstraint", s.getIdentifier(),
                                       moduleOfS.getName(), p.getName(),
                                       message != null && message.length() > 0 ? " " + message : "" );

                        }
                    }

                    for ( int j = 0, s1 = s.getProperties().getReference().size(); j < s1; j++ )
                    {
                        final PropertyReference r = s.getProperties().getReference().get( j );

                        addDetail( validationContext.getReport(),
                                   "SPECIFICATION_PROPERTY_REFERENCE_DECLARATION_CONSTRAINT", Level.SEVERE,
                                   new ObjectFactory().createSpecification( s ),
                                   "specificationPropertyReferenceDeclarationConstraint", s.getIdentifier(),
                                   moduleOfS.getName(), r.getName() );

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

        final Module moduleOfImpl =
            validationContext.getModules().getModuleOfImplementation( implementation.getIdentifier() );

        if ( !dependency.isOptional()
             && ( available == null || available.getImplementation().isEmpty()
                  || ( dependency.getImplementationName() != null
                       && available.getImplementationByName( dependency.getImplementationName() ) == null ) ) )
        {
            addDetail( validationContext.getReport(), "IMPLEMENTATION_MANDATORY_DEPENDENCY_CONSTRAINT", Level.SEVERE,
                       new ObjectFactory().createImplementation( implementation ),
                       "implementationMandatoryDependencyConstraint", implementation.getIdentifier(),
                       moduleOfImpl.getName(), dependency.getName() );

        }

        if ( s != null )
        {
            final Module moduleOfS = validationContext.getModules().getModuleOfSpecification( s.getIdentifier() );

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
                    for ( int i = 0, s0 = dependency.getDependencies().getDependency().size(); i < s0; i++ )
                    {
                        final Dependency d = dependency.getDependencies().getDependency().get( i );

                        addDetail( validationContext.getReport(),
                                   "IMPLEMENTATION_DEPENDENCY_DEPENDENCIES_OVERRIDE_CONSTRAINT", Level.SEVERE,
                                   new ObjectFactory().createImplementation( implementation ),
                                   "implementationDependencyDependenciesOverrideConstraint",
                                   implementation.getIdentifier(), moduleOfImpl.getName(), dependency.getName(),
                                   d.getName(), s.getIdentifier(), moduleOfS.getName(), s.getScope() );

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
                                   moduleOfImpl.getName(), dependency.getName(), m.getName(), s.getIdentifier(),
                                   moduleOfS.getName(), s.getScope() );

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
                                   implementation.getIdentifier(), moduleOfImpl.getName(), dependency.getName(),
                                   p.getName(), s.getIdentifier(), moduleOfS.getName(), s.getScope() );

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
                           implementation.getIdentifier(), moduleOfImpl.getName(), dependency.getName(), r.getName() );

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
                               moduleOfImpl.getName(), dependency.getName(), p.getName() );

                }

                if ( p.getAny() != null && p.getType() == null )
                {
                    addDetail( validationContext.getReport(), "IMPLEMENTATION_DEPENDENCY_PROPERTY_TYPE_CONSTRAINT",
                               Level.SEVERE, new ObjectFactory().createImplementation( implementation ),
                               "implementationDependencyPropertyTypeConstraint", implementation.getIdentifier(),
                               moduleOfImpl.getName(), dependency.getName(), p.getName() );

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
                               moduleOfImpl.getName(), dependency.getName(), p.getName(),
                               message != null && message.length() > 0 ? " " + message : "" );

                }
            }

            for ( int i = 0, s0 = dependency.getProperties().getReference().size(); i < s0; i++ )
            {
                final PropertyReference r = dependency.getProperties().getReference().get( i );

                addDetail( validationContext.getReport(),
                           "IMPLEMENTATION_DEPENDENCY_PROPERTY_REFERENCE_DECLARATION_CONSTRAINT", Level.SEVERE,
                           new ObjectFactory().createImplementation( implementation ),
                           "implementationDependencyPropertyReferenceDeclarationConstraint",
                           implementation.getIdentifier(), moduleOfImpl.getName(), dependency.getName(), r.getName() );

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

                final InheritanceModel imodel = validationContext.getInheritanceModel();
                final Module moduleOfA = validationContext.getModules().getModuleOfImplementation( a.getIdentifier() );

                if ( dependency.getDependencies() != null )
                {
                    for ( int j = 0, s1 = dependency.getDependencies().getDependency().size(); j < s1; j++ )
                    {
                        final Dependency override = dependency.getDependencies().getDependency().get( j );

                        final Set<InheritanceModel.Node<Dependency>> effDependencies =
                            imodel.getEffectiveDependencyNodes( a.getIdentifier(), override.getName() );

                        final Set<InheritanceModel.Node<Dependency>> overriddenDependencies =
                            modifiableSet( effDependencies );

                        final boolean effectiveDependencyOverridden = !overriddenDependencies.isEmpty();

                        if ( override.isOverride() && overriddenDependencies.isEmpty() )
                        {
                            addDetail( validationContext.getReport(),
                                       "IMPLEMENTATION_DEPENDENCY_OVERRIDE_DEPENDENCY_CONSTRAINT",
                                       Level.SEVERE, new ObjectFactory().createImplementation( implementation ),
                                       "implementationDependencyOverrideDependencyConstraint",
                                       implementation.getIdentifier(), moduleOfImpl.getName(), dependency.getName(),
                                       override.getName(), a.getIdentifier(), moduleOfA.getName() );

                        }

                        if ( !( override.isOverride() || overriddenDependencies.isEmpty() ) )
                        {
                            for ( final InheritanceModel.Node<Dependency> overriddenDependency :
                                  overriddenDependencies )
                            {
                                addDetail( validationContext.getReport(),
                                           "IMPLEMENTATION_DEPENDENCY_OVERRIDE_DEPENDENCY_WARNING",
                                           Level.WARNING, new ObjectFactory().createImplementation( implementation ),
                                           "implementationDependencyOverrideDependencyWarning",
                                           implementation.getIdentifier(), moduleOfImpl.getName(),
                                           dependency.getName(), override.getName(), a.getIdentifier(),
                                           moduleOfA.getName(), getNodePathString( overriddenDependency ) );

                            }
                        }

                        retainFinalNodes( overriddenDependencies );

                        for ( final InheritanceModel.Node<Dependency> overriddenDependency :
                              overriddenDependencies )
                        {
                            addDetail( validationContext.getReport(),
                                       "IMPLEMENTATION_DEPENDENCY_FINAL_DEPENDENCY_CONSTRAINT",
                                       Level.SEVERE, new ObjectFactory().createImplementation( implementation ),
                                       "implementationDependencyFinalDependencyConstraint",
                                       implementation.getIdentifier(), moduleOfImpl.getName(), dependency.getName(),
                                       override.getName(), a.getIdentifier(), moduleOfA.getName(),
                                       getNodePathString( overriddenDependency ) );

                        }

                        if ( effectiveDependencyOverridden )
                        {
                            for ( InheritanceModel.Node<Dependency> node : effDependencies )
                            {
                                final Dependency overridden = node.getModelObject();

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
                                                   implementation.getIdentifier(), moduleOfImpl.getName(),
                                                   dependency.getName(), overridden.getName(),
                                                   a.getIdentifier(), moduleOfA.getName(),
                                                   overrideSpecification.getMultiplicity().value(),
                                                   overriddenSpecification.getMultiplicity().value() );

                                    }

                                    if ( overrideSpecification.getScope() != null
                                         ? !overrideSpecification.getScope().equals(
                                        overriddenSpecification.getScope() )
                                         : overriddenSpecification.getScope() != null )
                                    {
                                        addDetail( validationContext.getReport(),
                                                   "IMPLEMENTATION_DEPENDENCY_SCOPE_CONSTRAINT", Level.SEVERE,
                                                   new ObjectFactory().createImplementation( implementation ),
                                                   "implementationDependencyScopeConstraint",
                                                   implementation.getIdentifier(), moduleOfImpl.getName(),
                                                   dependency.getName(), override.getName(),
                                                   a.getIdentifier(), moduleOfA.getName(),
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
                                                       implementation.getIdentifier(), moduleOfImpl.getName(),
                                                       dependency.getName(), override.getName(),
                                                       a.getIdentifier(), moduleOfA.getName() );

                                        }

                                        if ( override.getImplementationName() != null
                                             && overridden.getImplementationName() == null )
                                        {
                                            addDetail( validationContext.getReport(),
                                                       "IMPLEMENTATION_DEPENDENCY_IMPLEMENTATION_NAME_CONSTRAINT",
                                                       Level.SEVERE,
                                                       new ObjectFactory().createImplementation( implementation ),
                                                       "implementationDependencyImplementationNameConstraint",
                                                       implementation.getIdentifier(), moduleOfImpl.getName(),
                                                       dependency.getName(), overridden.getName(),
                                                       a.getIdentifier(), moduleOfA.getName(),
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
                                               implementation.getIdentifier(), moduleOfImpl.getName(),
                                               dependency.getName(), overridden.getName(),
                                               a.getIdentifier(), moduleOfA.getName() );

                                }
                            }
                        }
                    }
                }

                if ( dependency.getMessages() != null )
                {
                    for ( int j = 0, s1 = dependency.getMessages().getMessage().size(); j < s1; j++ )
                    {
                        final Message override = dependency.getMessages().getMessage().get( j );

                        final Set<InheritanceModel.Node<Message>> overriddenMessages = modifiableSet(
                            imodel.getEffectiveMessageNodes( a.getIdentifier(), override.getName() ) );

                        if ( override.isOverride() && overriddenMessages.isEmpty() )
                        {
                            addDetail( validationContext.getReport(),
                                       "IMPLEMENTATION_DEPENDENCY_OVERRIDE_MESSAGE_CONSTRAINT", Level.SEVERE,
                                       new ObjectFactory().createImplementation( implementation ),
                                       "implementationDependencyOverrideMessageConstraint",
                                       implementation.getIdentifier(), moduleOfImpl.getName(), dependency.getName(),
                                       override.getName(), a.getIdentifier(), moduleOfA.getName() );

                        }

                        if ( !( override.isOverride() || overriddenMessages.isEmpty() ) )
                        {
                            for ( final InheritanceModel.Node<Message> overriddenMessage : overriddenMessages )
                            {
                                addDetail( validationContext.getReport(),
                                           "IMPLEMENTATION_DEPENDENCY_OVERRIDE_MESSAGE_WARNING", Level.WARNING,
                                           new ObjectFactory().createImplementation( implementation ),
                                           "implementationDependencyOverrideMessageWarning",
                                           implementation.getIdentifier(), moduleOfImpl.getName(), dependency.getName(),
                                           override.getName(), a.getIdentifier(), moduleOfA.getName(),
                                           getNodePathString( overriddenMessage ) );

                            }
                        }

                        retainFinalNodes( overriddenMessages );

                        for ( final InheritanceModel.Node<Message> overriddenMessage : overriddenMessages )
                        {
                            addDetail( validationContext.getReport(),
                                       "IMPLEMENTATION_DEPENDENCY_FINAL_MESSAGE_CONSTRAINT", Level.SEVERE,
                                       new ObjectFactory().createImplementation( implementation ),
                                       "implementationDependencyFinalMessageConstraint",
                                       implementation.getIdentifier(), moduleOfImpl.getName(),
                                       dependency.getName(), override.getName(), a.getIdentifier(),
                                       moduleOfA.getName(), getNodePathString( overriddenMessage ) );

                        }
                    }
                }

                if ( dependency.getProperties() != null )
                {
                    for ( int j = 0, s1 = dependency.getProperties().getProperty().size(); j < s1; j++ )
                    {
                        final Property override = dependency.getProperties().getProperty().get( j );

                        final Set<InheritanceModel.Node<Property>> overriddenProperties = modifiableSet(
                            imodel.getEffectivePropertyNodes( a.getIdentifier(), override.getName() ) );

                        if ( override.isOverride() && overriddenProperties.isEmpty() )
                        {
                            addDetail( validationContext.getReport(),
                                       "IMPLEMENTATION_DEPENDENCY_OVERRIDE_PROPERTY_CONSTRAINT", Level.SEVERE,
                                       new ObjectFactory().createImplementation( implementation ),
                                       "implementationDependencyOverridePropertyConstraint",
                                       implementation.getIdentifier(), moduleOfImpl.getName(), dependency.getName(),
                                       override.getName(), a.getIdentifier(), moduleOfA.getName() );

                        }

                        if ( !( override.isOverride() || overriddenProperties.isEmpty() ) )
                        {
                            for ( final InheritanceModel.Node<Property> overriddenProperty : overriddenProperties )
                            {
                                addDetail( validationContext.getReport(),
                                           "IMPLEMENTATION_DEPENDENCY_OVERRIDE_PROPERTY_WARNING", Level.WARNING,
                                           new ObjectFactory().createImplementation( implementation ),
                                           "implementationDependencyOverridePropertyWarning",
                                           implementation.getIdentifier(), dependency.getName(), override.getName(),
                                           a.getIdentifier(), moduleOfA.getName(),
                                           getNodePathString( overriddenProperty ) );

                            }
                        }

                        retainFinalNodes( overriddenProperties );

                        for ( final InheritanceModel.Node<Property> overriddenProperty : overriddenProperties )
                        {
                            addDetail( validationContext.getReport(),
                                       "IMPLEMENTATION_DEPENDENCY_FINAL_PROPERTY_CONSTRAINT", Level.SEVERE,
                                       new ObjectFactory().createImplementation( implementation ),
                                       "implementationDependencyFinalPropertyConstraint",
                                       implementation.getIdentifier(), moduleOfImpl.getName(), dependency.getName(),
                                       override.getName(), a.getIdentifier(), moduleOfA.getName(),
                                       getNodePathString( overriddenProperty ) );

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
        final Module moduleOfImpl =
            validationContext.getModules().getModuleOfImplementation( implementation.getIdentifier() );

        if ( specs != null )
        {
            for ( int i = 0, s0 = specs.getReference().size(); i < s0; i++ )
            {
                final SpecificationReference r = specs.getReference().get( i );
                final Specification s = specs.getSpecification( r.getIdentifier() );

                if ( s != null && r.getVersion() != null )
                {
                    final Module moduleOfS =
                        validationContext.getModules().getModuleOfSpecification( s.getIdentifier() );

                    if ( s.getVersion() == null )
                    {
                        addDetail( validationContext.getReport(),
                                   "IMPLEMENTATION_SPECIFICATION_VERSIONING_CONSTRAINT", Level.SEVERE,
                                   new ObjectFactory().createImplementation( implementation ),
                                   "implementationSpecificationVersioningConstraint", implementation.getIdentifier(),
                                   moduleOfImpl.getName(), s.getIdentifier(), moduleOfS.getName() );

                    }
                    else
                    {
                        try
                        {
                            if ( VersionParser.compare( r.getVersion(), s.getVersion() ) != 0 )
                            {
                                addDetail( validationContext.getReport(),
                                           "IMPLEMENTATION_SPECIFICATION_COMPATIBILITY_CONSTRAINT", Level.SEVERE,
                                           new ObjectFactory().createImplementation( implementation ),
                                           "implementationSpecificationCompatibilityConstraint",
                                           implementation.getIdentifier(), moduleOfImpl.getName(), s.getIdentifier(),
                                           moduleOfS.getName(), r.getVersion(), s.getVersion() );

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
                                       implementation.getIdentifier(), moduleOfImpl.getName(), s.getIdentifier(),
                                       moduleOfS.getName(), r.getVersion(),
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
                                       implementation.getIdentifier(), moduleOfImpl.getName(), s.getIdentifier(),
                                       moduleOfS.getName(), r.getVersion(),
                                       message != null && message.length() > 0 ? " " + message : "" );

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

        for ( final InheritanceModel.Node<T> node : nodes )
        {
            path.append( ", " ).append( getNodePathString( node ) );
        }

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
            set = new HashSet<T>( col );
        }

        return set;
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

        private final InheritanceModel inheritanceModel;

        private ValidationContext( final ModelContext modelContext, final Modules modules,
                                   final ModelValidationReport report )
        {
            super();
            this.modelContext = modelContext;
            this.modules = modules;
            this.report = report;
            this.inheritanceModel = new InheritanceModel( modules );
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

    }
}
