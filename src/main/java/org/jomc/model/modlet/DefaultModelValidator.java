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

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Level;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.util.JAXBSource;
import javax.xml.transform.Source;
import org.jomc.model.Dependencies;
import org.jomc.model.Dependency;
import org.jomc.model.Implementation;
import org.jomc.model.ImplementationReference;
import org.jomc.model.Implementations;
import org.jomc.model.Message;
import org.jomc.model.MessageReference;
import org.jomc.model.Messages;
import org.jomc.model.ModelObject;
import org.jomc.model.Module;
import org.jomc.model.Modules;
import org.jomc.model.Multiplicity;
import org.jomc.model.ObjectFactory;
import org.jomc.model.Properties;
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
 * @version $Id$
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
            if ( context.isLoggable( Level.FINE ) )
            {
                context.log( Level.FINE, getMessage( "validatingModel", new Object[]
                    {
                        this.getClass().getName(), model.getIdentifier()
                    } ), null );

            }

            final Source source = new JAXBSource( context.createContext( model.getIdentifier() ),
                                                  new org.jomc.modlet.ObjectFactory().createModel( model ) );

            final ModelValidationReport report = context.validateModel( model.getIdentifier(), source );
            final Modules modules = ModelHelper.getModules( model );

            if ( modules != null )
            {
                this.assertModulesValid( context, modules, report );
                this.assertSpecificationsValid( context, modules, report );
                this.assertImplementationsValid( context, modules, report );
            }

            return report;
        }
        catch ( final JAXBException e )
        {
            String message = e.getMessage();
            if ( message == null && e.getLinkedException() != null )
            {
                message = e.getLinkedException().getMessage();
            }

            if ( context.isLoggable( Level.FINE ) )
            {
                context.log( Level.FINE, message, e );
            }

            throw new ModelException( message, e );
        }
    }

    private void assertModulesValid( final ModelContext context, final Modules modules,
                                     final ModelValidationReport report )
    {
        for ( Module m : modules.getModule() )
        {
            if ( m.getImplementations() != null )
            {
                for ( ImplementationReference r : m.getImplementations().getReference() )
                {
                    report.getDetails().add( this.createDetail(
                        "MODULE_IMPLEMENTATION_REFERENCE_DECLARATION_CONSTRAINT", Level.SEVERE,
                        "moduleImplementationReferenceDeclarationConstraint", new Object[]
                        {
                            m.getName(), r.getIdentifier()
                        }, new ObjectFactory().createModule( m ) ) );

                }
            }

            if ( m.getMessages() != null )
            {
                for ( Message msg : m.getMessages().getMessage() )
                {
                    if ( msg.isFinal() )
                    {
                        report.getDetails().add( this.createDetail(
                            "MODULE_FINAL_MESSAGE_DECLARATION_CONSTRAINT", Level.SEVERE,
                            "moduleFinalMessageConstraint", new Object[]
                            {
                                m.getName(), msg.getName()
                            }, new ObjectFactory().createModule( m ) ) );

                    }

                    if ( msg.isOverride() )
                    {
                        report.getDetails().add( this.createDetail(
                            "MODULE_OVERRIDE_MESSAGE_DECLARATION_CONSTRAINT", Level.SEVERE,
                            "moduleOverrideMessageConstraint", new Object[]
                            {
                                m.getName(), msg.getName()
                            }, new ObjectFactory().createModule( m ) ) );

                    }

                    if ( msg.getTemplate() != null )
                    {
                        for ( Text t : msg.getTemplate().getText() )
                        {
                            try
                            {
                                new MessageFormat( t.getValue(), new Locale( t.getLanguage() ) );
                            }
                            catch ( final IllegalArgumentException e )
                            {
                                if ( context.isLoggable( Level.FINE ) )
                                {
                                    context.log( Level.FINE, e.getMessage(), e );
                                }

                                report.getDetails().add( this.createDetail(
                                    "MODULE_MESSAGE_TEMPLATE_CONSTRAINT", Level.SEVERE,
                                    "moduleMessageTemplateConstraint", new Object[]
                                    {
                                        m.getName(), msg.getName(), t.getValue(), e.getMessage()
                                    }, new ObjectFactory().createModule( m ) ) );

                            }
                        }
                    }
                }

                for ( MessageReference r : m.getMessages().getReference() )
                {
                    report.getDetails().add( this.createDetail(
                        "MODULE_MESSAGE_REFERENCE_DECLARATION_CONSTRAINT", Level.SEVERE,
                        "moduleMessageReferenceDeclarationConstraint", new Object[]
                        {
                            m.getName(), r.getName()
                        }, new ObjectFactory().createModule( m ) ) );

                }
            }

            if ( m.getProperties() != null )
            {
                for ( Property p : m.getProperties().getProperty() )
                {
                    if ( p.isFinal() )
                    {
                        report.getDetails().add( this.createDetail(
                            "MODULE_FINAL_PROPERTY_DECLARATION_CONSTRAINT", Level.SEVERE,
                            "moduleFinalPropertyConstraint", new Object[]
                            {
                                m.getName(), p.getName()
                            }, new ObjectFactory().createModule( m ) ) );

                    }

                    if ( p.isOverride() )
                    {
                        report.getDetails().add( this.createDetail(
                            "MODULE_OVERRIDE_PROPERTY_DECLARATION_CONSTRAINT", Level.SEVERE,
                            "moduleOverridePropertyConstraint", new Object[]
                            {
                                m.getName(), p.getName()
                            }, new ObjectFactory().createModule( m ) ) );

                    }

                    if ( p.getValue() != null && p.getAny() != null )
                    {
                        report.getDetails().add( this.createDetail( "MODULE_PROPERTY_VALUE_CONSTRAINT", Level.SEVERE,
                                                                    "modulePropertyValueConstraint", new Object[]
                            {
                                m.getName(), p.getName()
                            }, new ObjectFactory().createModule( m ) ) );

                    }

                    if ( p.getAny() != null && p.getType() == null )
                    {
                        report.getDetails().add( this.createDetail( "MODULE_PROPERTY_TYPE_CONSTRAINT", Level.SEVERE,
                                                                    "modulePropertyTypeConstraint", new Object[]
                            {
                                m.getName(), p.getName()
                            }, new ObjectFactory().createModule( m ) ) );

                    }

                    try
                    {
                        p.getJavaValue( context.getClassLoader() );
                    }
                    catch ( final PropertyException e )
                    {
                        if ( context.isLoggable( Level.FINE ) )
                        {
                            context.log( Level.FINE, e.getMessage(), e );
                        }

                        report.getDetails().add( this.createDetail(
                            "MODULE_PROPERTY_JAVA_VALUE_CONSTRAINT", Level.SEVERE,
                            "modulePropertyJavaValueConstraint", new Object[]
                            {
                                m.getName(), p.getName(), e.getMessage()
                            }, new ObjectFactory().createModule( m ) ) );

                    }
                }

                for ( PropertyReference r : m.getProperties().getReference() )
                {
                    report.getDetails().add( this.createDetail(
                        "MODULE_PROPERTY_REFERENCE_DECLARATION_CONSTRAINT", Level.SEVERE,
                        "modulePropertyReferenceDeclarationConstraint", new Object[]
                        {
                            m.getName(), r.getName()
                        }, new ObjectFactory().createModule( m ) ) );

                }
            }

            if ( m.getSpecifications() != null )
            {
                for ( SpecificationReference r : m.getSpecifications().getReference() )
                {
                    report.getDetails().add( this.createDetail(
                        "MODULE_SPECIFICATION_REFERENCE_DECLARATION_CONSTRAINT", Level.SEVERE,
                        "moduleSpecificationReferenceDeclarationConstraint", new Object[]
                        {
                            m.getName(), r.getIdentifier()
                        }, new ObjectFactory().createModule( m ) ) );

                }
            }
        }
    }

    private void assertImplementationsValid( final ModelContext context, final Modules modules,
                                             final ModelValidationReport report )
    {
        final Implementations implementations = modules.getImplementations();

        if ( implementations != null )
        {
            final Map<String, Implementation> implementationClassDeclarations = new HashMap<String, Implementation>();

            for ( Implementation i : implementations.getImplementation() )
            {
                final Implementation cycle = this.findInheritanceCycle( modules, i, i, new Implementations() );

                if ( cycle != null )
                {
                    report.getDetails().add( this.createDetail(
                        "IMPLEMENTATION_INHERITANCE_CYCLE_CONSTRAINT", Level.SEVERE,
                        "implementationInheritanceCycleConstraint", new Object[]
                        {
                            i.getIdentifier(), cycle.getIdentifier()
                        }, new ObjectFactory().createImplementation( i ) ) );

                }

                if ( i.isClassDeclaration() )
                {
                    if ( i.getClazz() == null )
                    {
                        report.getDetails().add( this.createDetail(
                            "IMPLEMENTATION_CLASS_CONSTRAINT", Level.SEVERE,
                            "implementationClassConstraint", new Object[]
                            {
                                i.getIdentifier()
                            }, new ObjectFactory().createImplementation( i ) ) );

                    }
                    else
                    {
                        final Implementation prev = implementationClassDeclarations.get( i.getClazz() );

                        if ( prev != null )
                        {
                            report.getDetails().add( this.createDetail(
                                "IMPLEMENTATION_CLASS_DECLARATION_CONSTRAINT",
                                Level.SEVERE, "implementationClassDeclarationConstraint", new Object[]
                                {
                                    i.getIdentifier(), i.getClazz(), prev.getIdentifier()
                                }, new ObjectFactory().createImplementation( i ) ) );

                        }
                        else
                        {
                            implementationClassDeclarations.put( i.getClazz(), i );
                        }
                    }
                }

                if ( i.isAbstract() && i.getLocation() != null )
                {
                    report.getDetails().add( this.createDetail(
                        "IMPLEMENTATION_ABSTRACT_LOCATION_DECLARATION_CONSTRAINT", Level.SEVERE,
                        "implementationAbstractLocationDeclarationConstraint", new Object[]
                        {
                            i.getIdentifier(), i.getLocation()
                        }, new ObjectFactory().createImplementation( i ) ) );

                }

                if ( i.getDependencies() != null )
                {
                    final Dependencies parentDependencies = new Dependencies();
                    this.collectParentDependencies( modules, i, parentDependencies, new Implementations(), false );

                    for ( Dependency d : i.getDependencies().getDependency() )
                    {
                        final Dependency parent = parentDependencies.getDependency( d.getName() );

                        if ( d.isOverride() && parent == null )
                        {
                            report.getDetails().add( this.createDetail(
                                "IMPLEMENTATION_DEPENDENCY_OVERRIDE_CONSTRAINT", Level.SEVERE,
                                "implementationDependencyOverrideConstraint", new Object[]
                                {
                                    i.getIdentifier(), d.getName(),
                                }, new ObjectFactory().createImplementation( i ) ) );

                        }
                        if ( parent != null && parent.isFinal() )
                        {
                            report.getDetails().add( this.createDetail(
                                "IMPLEMENTATION_DEPENDENCY_INHERITANCE_CONSTRAINT", Level.SEVERE,
                                "implementationDependencyFinalConstraint", new Object[]
                                {
                                    i.getIdentifier(), d.getName(),
                                }, new ObjectFactory().createImplementation( i ) ) );

                        }

                        this.assertDependencyValid( context, modules, i, d, report );
                    }
                }

                if ( i.getImplementations() != null )
                {
                    for ( Implementation pi : i.getImplementations().getImplementation() )
                    {
                        report.getDetails().add( this.createDetail(
                            "IMPLEMENTATION_IMPLEMENTATION_DECLARATION_CONSTRAINT", Level.SEVERE,
                            "implementationImplementationDeclarationConstraint", new Object[]
                            {
                                i.getIdentifier(), pi.getIdentifier()
                            }, new ObjectFactory().createImplementation( i ) ) );

                    }
                }

                if ( i.getMessages() != null )
                {
                    final Messages parentMessages = new Messages();
                    this.collectParentMessages( modules, i, parentMessages, new Implementations(), false );

                    for ( Message m : i.getMessages().getMessage() )
                    {
                        final Message parentMessage = parentMessages.getMessage( m.getName() );
                        final MessageReference parentReference = parentMessages.getReference( m.getName() );

                        if ( i.getMessages().getReference( m.getName() ) != null )
                        {
                            report.getDetails().add( this.createDetail(
                                "IMPLEMENTATION_MESSAGES_UNIQUENESS_CONSTRAINT", Level.SEVERE,
                                "implementationMessagesUniquenessConstraint", new Object[]
                                {
                                    i.getIdentifier(), m.getName(),
                                }, new ObjectFactory().createImplementation( i ) ) );

                        }

                        if ( m.getTemplate() != null )
                        {
                            for ( Text t : m.getTemplate().getText() )
                            {
                                try
                                {
                                    new MessageFormat( t.getValue(), new Locale( t.getLanguage() ) );
                                }
                                catch ( final IllegalArgumentException e )
                                {
                                    if ( context.isLoggable( Level.FINE ) )
                                    {
                                        context.log( Level.FINE, e.getMessage(), e );
                                    }

                                    report.getDetails().add( this.createDetail(
                                        "IMPLEMENTATION_MESSAGE_TEMPLATE_CONSTRAINT", Level.SEVERE,
                                        "implementationMessageTemplateConstraint", new Object[]
                                        {
                                            i.getIdentifier(), m.getName(), t.getValue(), e.getMessage()
                                        }, new ObjectFactory().createImplementation( i ) ) );

                                }
                            }
                        }

                        if ( m.isOverride() && parentMessage == null && parentReference == null )
                        {
                            report.getDetails().add( this.createDetail(
                                "IMPLEMENTATION_MESSAGE_OVERRIDE_CONSTRAINT", Level.SEVERE,
                                "implementationMessageOverrideConstraint", new Object[]
                                {
                                    i.getIdentifier(), m.getName(),
                                }, new ObjectFactory().createImplementation( i ) ) );

                        }

                        if ( ( parentMessage != null && parentMessage.isFinal() )
                             || ( parentReference != null && parentReference.isFinal() ) )
                        {
                            report.getDetails().add( this.createDetail(
                                "IMPLEMENTATION_MESSAGE_INHERITANCE_CONSTRAINT", Level.SEVERE,
                                "implementationMessageFinalConstraint", new Object[]
                                {
                                    i.getIdentifier(), m.getName(),
                                }, new ObjectFactory().createImplementation( i ) ) );

                        }
                    }

                    for ( MessageReference r : i.getMessages().getReference() )
                    {
                        final Message parentMessage = parentMessages.getMessage( r.getName() );
                        final MessageReference parentReference = parentMessages.getReference( r.getName() );

                        if ( r.isOverride() && parentMessage == null && parentReference == null )
                        {
                            report.getDetails().add( this.createDetail(
                                "IMPLEMENTATION_MESSAGE_OVERRIDE_CONSTRAINT", Level.SEVERE,
                                "implementationMessageOverrideConstraint", new Object[]
                                {
                                    i.getIdentifier(), r.getName(),
                                }, new ObjectFactory().createImplementation( i ) ) );

                        }

                        if ( ( parentMessage != null && parentMessage.isFinal() )
                             || ( parentReference != null && parentReference.isFinal() ) )
                        {
                            report.getDetails().add( this.createDetail(
                                "IMPLEMENTATION_MESSAGE_INHERITANCE_CONSTRAINT", Level.SEVERE,
                                "implementationMessageFinalConstraint", new Object[]
                                {
                                    i.getIdentifier(), r.getName(),
                                }, new ObjectFactory().createImplementation( i ) ) );

                        }
                    }
                }

                if ( i.getProperties() != null )
                {
                    final Properties parentProperties = new Properties();
                    this.collectParentProperties( modules, i, parentProperties, new Implementations(), false );

                    for ( Property p : i.getProperties().getProperty() )
                    {
                        final Property parentProperty = parentProperties.getProperty( p.getName() );
                        final PropertyReference parentReference = parentProperties.getReference( p.getName() );

                        if ( i.getProperties().getReference( p.getName() ) != null )
                        {
                            report.getDetails().add( this.createDetail(
                                "IMPLEMENTATION_PROPERTIES_UNIQUENESS_CONSTRAINT", Level.SEVERE,
                                "implementationPropertiesUniquenessConstraint", new Object[]
                                {
                                    i.getIdentifier(), p.getName(),
                                }, new ObjectFactory().createImplementation( i ) ) );

                        }

                        if ( p.getValue() != null && p.getAny() != null )
                        {
                            report.getDetails().add( this.createDetail(
                                "IMPLEMENTATION_PROPERTY_VALUE_CONSTRAINT", Level.SEVERE,
                                "implementationPropertyValueConstraint", new Object[]
                                {
                                    i.getIdentifier(), p.getName()
                                }, new ObjectFactory().createImplementation( i ) ) );

                        }

                        if ( p.getAny() != null && p.getType() == null )
                        {
                            report.getDetails().add( this.createDetail(
                                "IMPLEMENTATION_PROPERTY_TYPE_CONSTRAINT", Level.SEVERE,
                                "implementationPropertyTypeConstraint", new Object[]
                                {
                                    i.getIdentifier(), p.getName()
                                }, new ObjectFactory().createImplementation( i ) ) );

                        }

                        try
                        {
                            p.getJavaValue( context.getClassLoader() );
                        }
                        catch ( final PropertyException e )
                        {
                            if ( context.isLoggable( Level.FINE ) )
                            {
                                context.log( Level.FINE, e.getMessage(), e );
                            }

                            report.getDetails().add( this.createDetail(
                                "IMPLEMENTATION_PROPERTY_JAVA_VALUE_CONSTRAINT", Level.SEVERE,
                                "implementationPropertyJavaValueConstraint", new Object[]
                                {
                                    i.getIdentifier(), p.getName(), e.getMessage()
                                }, new ObjectFactory().createImplementation( i ) ) );

                        }

                        if ( p.isOverride() && parentProperty == null && parentReference == null )
                        {
                            report.getDetails().add( this.createDetail(
                                "IMPLEMENTATION_PROPERTY_OVERRIDE_CONSTRAINT", Level.SEVERE,
                                "implementationPropertyOverrideConstraint", new Object[]
                                {
                                    i.getIdentifier(), p.getName(),
                                }, new ObjectFactory().createImplementation( i ) ) );

                        }
                        if ( ( parentProperty != null && parentProperty.isFinal() )
                             || ( parentReference != null && parentReference.isFinal() ) )
                        {
                            report.getDetails().add( this.createDetail(
                                "IMPLEMENTATION_PROPERTY_INHERITANCE_CONSTRAINT", Level.SEVERE,
                                "implementationPropertyFinalConstraint", new Object[]
                                {
                                    i.getIdentifier(), p.getName(),
                                }, new ObjectFactory().createImplementation( i ) ) );

                        }
                    }

                    for ( PropertyReference r : i.getProperties().getReference() )
                    {
                        final Property parentProperty = parentProperties.getProperty( r.getName() );
                        final PropertyReference parentReference = parentProperties.getReference( r.getName() );

                        if ( r.isOverride() && parentProperty == null && parentReference == null )
                        {
                            report.getDetails().add( this.createDetail(
                                "IMPLEMENTATION_PROPERTY_OVERRIDE_CONSTRAINT", Level.SEVERE,
                                "implementationPropertyOverrideConstraint", new Object[]
                                {
                                    i.getIdentifier(), r.getName(),
                                }, new ObjectFactory().createImplementation( i ) ) );

                        }

                        if ( ( parentProperty != null && parentProperty.isFinal() )
                             || ( parentReference != null && parentReference.isFinal() ) )
                        {
                            report.getDetails().add( this.createDetail(
                                "IMPLEMENTATION_PROPERTY_INHERITANCE_CONSTRAINT", Level.SEVERE,
                                "implementationPropertyFinalConstraint", new Object[]
                                {
                                    i.getIdentifier(), r.getName(),
                                }, new ObjectFactory().createImplementation( i ) ) );

                        }
                    }
                }

                if ( i.getSpecifications() != null )
                {
                    final Specifications parentSpecifications = new Specifications();
                    this.collectParentSpecifications( modules, i, parentSpecifications, new Implementations(), false );

                    for ( Specification s : i.getSpecifications().getSpecification() )
                    {
                        report.getDetails().add( this.createDetail(
                            "IMPLEMENTATION_SPECIFICATION_DECLARATION_CONSTRAINT", Level.SEVERE,
                            "implementationSpecificationDeclarationConstraint", new Object[]
                            {
                                i.getIdentifier(), s.getIdentifier()
                            }, new ObjectFactory().createImplementation( i ) ) );

                    }

                    for ( SpecificationReference r : i.getSpecifications().getReference() )
                    {
                        final SpecificationReference parent = parentSpecifications.getReference( r.getIdentifier() );

                        if ( r.isOverride() && parent == null )
                        {
                            report.getDetails().add( this.createDetail(
                                "IMPLEMENTATION_SPECIFICATION_OVERRIDE_CONSTRAINT", Level.SEVERE,
                                "implementationSpecificationOverrideConstraint", new Object[]
                                {
                                    i.getIdentifier(), r.getIdentifier(),
                                }, new ObjectFactory().createImplementation( i ) ) );

                        }

                        if ( parent != null && parent.isFinal() )
                        {
                            report.getDetails().add( this.createDetail(
                                "IMPLEMENTATION_SPECIFICATION_INHERITANCE_CONSTRAINT", Level.SEVERE,
                                "implementationSpecificationFinalConstraint", new Object[]
                                {
                                    i.getIdentifier(), r.getIdentifier(),
                                }, new ObjectFactory().createImplementation( i ) ) );

                        }
                    }
                }

                this.assertValidImplementationInheritanceConstraints( context, modules, i, report );
                this.assertImplementationSpecificationCompatibility( context, modules, i, report );
            }
        }
    }

    private void assertSpecificationsValid( final ModelContext context, final Modules modules,
                                            final ModelValidationReport report )
    {
        final Specifications specifications = modules.getSpecifications();
        final Map<String, Specification> specificationClassDeclarations = new HashMap<String, Specification>();

        if ( specifications != null )
        {
            for ( Specification s : specifications.getSpecification() )
            {
                final Implementations impls = modules.getImplementations( s.getIdentifier() );

                if ( s.isClassDeclaration() )
                {
                    if ( s.getClazz() == null )
                    {
                        report.getDetails().add( this.createDetail(
                            "SPECIFICATION_CLASS_CONSTRAINT", Level.SEVERE, "specificationClassConstraint",
                            new Object[]
                            {
                                s.getIdentifier()
                            }, new ObjectFactory().createSpecification( s ) ) );

                    }
                    else
                    {
                        final Specification prev = specificationClassDeclarations.get( s.getClazz() );
                        if ( prev != null )
                        {
                            report.getDetails().add( this.createDetail(
                                "SPECIFICATION_CLASS_DECLARATION_CONSTRAINT", Level.SEVERE,
                                "specificationClassDeclarationConstraint", new Object[]
                                {
                                    s.getIdentifier(), s.getClazz(), prev.getIdentifier()
                                }, new ObjectFactory().createSpecification( s ) ) );

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
                                report.getDetails().add( this.createDetail(
                                    "SPECIFICATION_IMPLEMENTATION_NAME_UNIQUENESS_CONSTRAINT", Level.SEVERE,
                                    "specificationImplementationNameConstraint", new Object[]
                                    {
                                        i.getIdentifier(), s.getIdentifier(), i.getName()
                                    }, new ObjectFactory().createImplementation( i ) ) );

                            }
                        }
                    }

                    if ( s.getMultiplicity() == Multiplicity.ONE && impls.getImplementation().size() > 1 )
                    {
                        for ( Implementation i : impls.getImplementation() )
                        {
                            report.getDetails().add( this.createDetail(
                                "SPECIFICATION_IMPLEMENTATION_MULTIPLICITY_CONSTRAINT", Level.SEVERE,
                                "specificationMultiplicityConstraint", new Object[]
                                {
                                    i.getIdentifier(), s.getIdentifier(), s.getMultiplicity()
                                }, new ObjectFactory().createImplementation( i ) ) );

                        }
                    }
                }

                if ( s.getProperties() != null )
                {
                    for ( Property p : s.getProperties().getProperty() )
                    {
                        if ( p.getValue() != null && p.getAny() != null )
                        {
                            report.getDetails().add( this.createDetail(
                                "SPECIFICATION_PROPERTY_VALUE_CONSTRAINT", Level.SEVERE,
                                "specificationPropertyValueConstraint", new Object[]
                                {
                                    s.getIdentifier(), p.getName()
                                }, new ObjectFactory().createSpecification( s ) ) );

                        }

                        if ( p.getAny() != null && p.getType() == null )
                        {
                            report.getDetails().add( this.createDetail(
                                "SPECIFICATION_PROPERTY_TYPE_CONSTRAINT", Level.SEVERE,
                                "specificationPropertyTypeConstraint", new Object[]
                                {
                                    s.getIdentifier(), p.getName()
                                }, new ObjectFactory().createSpecification( s ) ) );

                        }

                        try
                        {
                            p.getJavaValue( context.getClassLoader() );
                        }
                        catch ( final PropertyException e )
                        {
                            if ( context.isLoggable( Level.FINE ) )
                            {
                                context.log( Level.FINE, e.getMessage(), e );
                            }

                            report.getDetails().add( this.createDetail(
                                "SPECIFICATION_PROPERTY_JAVA_VALUE_CONSTRAINT", Level.SEVERE,
                                "specificationPropertyJavaValueConstraint", new Object[]
                                {
                                    s.getIdentifier(), p.getName(), e.getMessage()
                                }, new ObjectFactory().createSpecification( s ) ) );

                        }
                    }

                    for ( PropertyReference r : s.getProperties().getReference() )
                    {
                        report.getDetails().add( this.createDetail(
                            "SPECIFICATION_PROPERTY_REFERENCE_DECLARATION_CONSTRAINT", Level.SEVERE,
                            "specificationPropertyReferenceDeclarationConstraint", new Object[]
                            {
                                s.getIdentifier(), r.getName()
                            }, new ObjectFactory().createSpecification( s ) ) );

                    }
                }
            }
        }
    }

    private void assertDependencyValid( final ModelContext context, final Modules modules,
                                        final Implementation implementation, final Dependency dependency,
                                        final ModelValidationReport report )
    {
        final Implementations available = modules.getImplementations( dependency.getIdentifier() );
        final Specification s = modules.getSpecification( dependency.getIdentifier() );

        if ( !dependency.isOptional()
             && ( available == null || available.getImplementation().isEmpty()
                  || ( dependency.getImplementationName() != null
                       && available.getImplementationByName( dependency.getImplementationName() ) == null ) ) )
        {
            report.getDetails().add( this.createDetail( "IMPLEMENTATION_MANDATORY_DEPENDENCY_CONSTRAINT", Level.SEVERE,
                                                        "implementationMandatoryDependencyConstraint", new Object[]
                {
                    implementation.getIdentifier(), dependency.getName()
                }, new ObjectFactory().createImplementation( implementation ) ) );

        }

        if ( s != null )
        {
            if ( s.getClazz() == null )
            {
                report.getDetails().add( this.createDetail(
                    "IMPLEMENTATION_DEPENDENCY_SPECIFICATION_CLASS_CONSTRAINT", Level.SEVERE,
                    "implementationDependencySpecificationClassConstraint", new Object[]
                    {
                        implementation.getIdentifier(), dependency.getName(), dependency.getIdentifier()
                    }, new ObjectFactory().createImplementation( implementation ) ) );

            }

            if ( dependency.getVersion() != null )
            {
                if ( s.getVersion() == null )
                {
                    report.getDetails().add( this.createDetail(
                        "IMPLEMENTATION_DEPENDENCY_SPECIFICATION_VERSIONING_CONSTRAINT", Level.SEVERE,
                        "implementationDependencySpecificationVersioningConstraint", new Object[]
                        {
                            implementation.getIdentifier(), dependency.getName(), s.getIdentifier()
                        }, new ObjectFactory().createImplementation( implementation ) ) );

                }
                else
                {
                    try
                    {
                        if ( VersionParser.compare( dependency.getVersion(), s.getVersion() ) > 0 )
                        {
                            final Module moduleOfImplementation =
                                modules.getModuleOfImplementation( implementation.getIdentifier() );

                            final Module moduleOfSpecification = modules.getModuleOfSpecification( s.getIdentifier() );

                            report.getDetails().add( this.createDetail(
                                "IMPLEMENTATION_DEPENDENCY_SPECIFICATION_COMPATIBILITY_CONSTRAINT", Level.SEVERE,
                                "implementationDependencySpecificationCompatibilityConstraint", new Object[]
                                {
                                    implementation.getIdentifier(),
                                    moduleOfImplementation != null ? moduleOfImplementation.getName() : "<>",
                                    s.getIdentifier(),
                                    moduleOfSpecification != null ? moduleOfSpecification.getName() : "<>",
                                    dependency.getVersion(), s.getVersion()
                                }, new ObjectFactory().createImplementation( implementation ) ) );

                        }
                    }
                    catch ( final ParseException e )
                    {
                        if ( context.isLoggable( Level.FINE ) )
                        {
                            context.log( Level.FINE, e.getMessage(), e );
                        }

                        final ModelValidationReport.Detail detail = new ModelValidationReport.Detail(
                            "IMPLEMENTATION_DEPENDENCY_SPECIFICATION_COMPATIBILITY_VERSIONING_PARSE_EXCEPTION",
                            Level.SEVERE, e.getMessage(), new ObjectFactory().createImplementation( implementation ) );

                        report.getDetails().add( detail );
                    }
                    catch ( final TokenMgrError e )
                    {
                        if ( context.isLoggable( Level.FINE ) )
                        {
                            context.log( Level.FINE, e.getMessage(), e );
                        }

                        final ModelValidationReport.Detail detail = new ModelValidationReport.Detail(
                            "IMPLEMENTATION_DEPENDENCY_SPECIFICATION_COMPATIBILITY_VERSIONING_TOKEN_MANAGER_ERROR",
                            Level.SEVERE, e.getMessage(), new ObjectFactory().createImplementation( implementation ) );

                        report.getDetails().add( detail );
                    }
                }
            }

            if ( s.getScope() != null )
            {
                if ( dependency.getDependencies() != null )
                {
                    for ( Dependency d : dependency.getDependencies().getDependency() )
                    {
                        report.getDetails().add( this.createDetail(
                            "IMPLEMENTATION_DEPENDENCY_DEPENDENCIES_OVERRIDE_CONSTRAINT", Level.SEVERE,
                            "implementationDependencyDependenciesOverrideConstraint", new Object[]
                            {
                                implementation.getIdentifier(), dependency.getName(), s.getIdentifier(), s.getScope(),
                                d.getName()
                            }, new ObjectFactory().createImplementation( implementation ) ) );

                    }
                }

                if ( dependency.getMessages() != null )
                {
                    for ( Message m : dependency.getMessages().getMessage() )
                    {
                        report.getDetails().add( this.createDetail(
                            "IMPLEMENTATION_DEPENDENCY_MESSAGES_OVERRIDE_CONSTRAINT", Level.SEVERE,
                            "implementationDependencyMessagesOverrideConstraint", new Object[]
                            {
                                implementation.getIdentifier(), dependency.getName(), s.getIdentifier(), s.getScope(),
                                m.getName()
                            }, new ObjectFactory().createImplementation( implementation ) ) );

                    }
                }

                if ( dependency.getProperties() != null )
                {
                    for ( Property p : dependency.getProperties().getProperty() )
                    {
                        report.getDetails().add( this.createDetail(
                            "IMPLEMENTATION_DEPENDENCY_PROPERTIES_OVERRIDE_CONSTRAINT", Level.SEVERE,
                            "implementationDependencyPropertiesOverrideConstraint", new Object[]
                            {
                                implementation.getIdentifier(), dependency.getName(), s.getIdentifier(), s.getScope(),
                                p.getName()
                            }, new ObjectFactory().createImplementation( implementation ) ) );

                    }
                }
            }
        }

        if ( dependency.getMessages() != null )
        {
            for ( MessageReference r : dependency.getMessages().getReference() )
            {
                report.getDetails().add( this.createDetail(
                    "IMPLEMENTATION_DEPENDENCY_MESSAGE_REFERENCE_DECLARATION_CONSTRAINT", Level.SEVERE,
                    "implementationDependencyMessageReferenceDeclarationConstraint", new Object[]
                    {
                        implementation.getIdentifier(), dependency.getName(), r.getName()
                    }, new ObjectFactory().createImplementation( implementation ) ) );

            }
        }

        if ( dependency.getProperties() != null )
        {
            for ( Property p : dependency.getProperties().getProperty() )
            {
                if ( p.getValue() != null && p.getAny() != null )
                {
                    report.getDetails().add( this.createDetail(
                        "IMPLEMENTATION_DEPENDENCY_PROPERTY_VALUE_CONSTRAINT", Level.SEVERE,
                        "implementationDependencyPropertyValueConstraint", new Object[]
                        {
                            implementation.getIdentifier(), dependency.getName(), p.getName()
                        }, new ObjectFactory().createImplementation( implementation ) ) );

                }

                if ( p.getAny() != null && p.getType() == null )
                {
                    report.getDetails().add( this.createDetail(
                        "IMPLEMENTATION_DEPENDENCY_PROPERTY_TYPE_CONSTRAINT", Level.SEVERE,
                        "implementationDependencyPropertyTypeConstraint", new Object[]
                        {
                            implementation.getIdentifier(), dependency.getName(), p.getName()
                        }, new ObjectFactory().createImplementation( implementation ) ) );

                }

                try
                {
                    p.getJavaValue( context.getClassLoader() );
                }
                catch ( final PropertyException e )
                {
                    if ( context.isLoggable( Level.FINE ) )
                    {
                        context.log( Level.FINE, e.getMessage(), e );
                    }

                    report.getDetails().add( this.createDetail(
                        "IMPLEMENTATION_DEPENDENCY_PROPERTY_JAVA_VALUE_CONSTRAINT", Level.SEVERE,
                        "implementationDependencyPropertyJavaValueConstraint", new Object[]
                        {
                            implementation.getIdentifier(), dependency.getName(), p.getName(), e.getMessage()
                        }, new ObjectFactory().createImplementation( implementation ) ) );

                }
            }

            for ( PropertyReference r : dependency.getProperties().getReference() )
            {
                report.getDetails().add( this.createDetail(
                    "IMPLEMENTATION_DEPENDENCY_PROPERTY_REFERENCE_DECLARATION_CONSTRAINT", Level.SEVERE,
                    "implementationDependencyPropertyReferenceDeclarationConstraint", new Object[]
                    {
                        implementation.getIdentifier(), dependency.getName(), r.getName()
                    }, new ObjectFactory().createImplementation( implementation ) ) );

            }
        }

        if ( available != null )
        {
            for ( Implementation a : available.getImplementation() )
            {
                if ( dependency.getImplementationName() != null
                     && !dependency.getImplementationName().equals( a.getName() ) )
                {
                    continue;
                }

                if ( dependency.getDependencies() != null )
                {
                    final Dependencies dependencies = modules.getDependencies( a.getIdentifier() );

                    if ( dependencies != null )
                    {
                        for ( Dependency override : dependency.getDependencies().getDependency() )
                        {
                            final Dependency overriden = dependencies.getDependency( override.getName() );

                            if ( overriden == null && override.isOverride() )
                            {
                                report.getDetails().add( this.createDetail(
                                    "IMPLEMENTATION_DEPENDENCY_OVERRIDE_DEPENDENCY_CONSTRAINT", Level.SEVERE,
                                    "implementationDependencyOverrideDependencyConstraint", new Object[]
                                    {
                                        implementation.getIdentifier(), dependency.getName(), a.getIdentifier(),
                                        override.getName()
                                    }, new ObjectFactory().createImplementation( implementation ) ) );

                            }

                            if ( overriden != null )
                            {
                                final Specification overrideSpecification =
                                    modules.getSpecification( override.getIdentifier() );

                                final Specification overridenSpecification =
                                    modules.getSpecification( overriden.getIdentifier() );

                                if ( overriden.isFinal() )
                                {
                                    report.getDetails().add( this.createDetail(
                                        "IMPLEMENTATION_DEPENDENCY_FINAL_DEPENDENCY_CONSTRAINT", Level.SEVERE,
                                        "implementationDependencyFinalDependencyConstraint", new Object[]
                                        {
                                            implementation.getIdentifier(), dependency.getName(), a.getIdentifier(),
                                            override.getName()
                                        }, new ObjectFactory().createImplementation( implementation ) ) );

                                }

                                if ( overrideSpecification != null && overridenSpecification != null )
                                {
                                    if ( overrideSpecification.getMultiplicity()
                                         != overridenSpecification.getMultiplicity() )
                                    {
                                        report.getDetails().add( this.createDetail(
                                            "IMPLEMENTATION_DEPENDENCY_MULTIPLICITY_CONSTRAINT", Level.SEVERE,
                                            "implementationDependencyMultiplicityConstraint", new Object[]
                                            {
                                                implementation.getIdentifier(), dependency.getName(), a.getIdentifier(),
                                                overriden.getName(), overrideSpecification.getMultiplicity().value(),
                                                overridenSpecification.getMultiplicity().value()
                                            }, new ObjectFactory().createImplementation( implementation ) ) );

                                    }

                                    if ( overrideSpecification.getScope() != null
                                         ? !overrideSpecification.getScope().equals( overridenSpecification.getScope() )
                                         : overridenSpecification.getScope() != null )
                                    {
                                        report.getDetails().add( this.createDetail(
                                            "IMPLEMENTATION_DEPENDENCY_SCOPE_CONSTRAINT", Level.SEVERE,
                                            "implementationDependencyScopeConstraint", new Object[]
                                            {
                                                implementation.getIdentifier(), dependency.getName(), a.getIdentifier(),
                                                overriden.getName(),
                                                overrideSpecification.getScope() == null
                                                ? "Multiton" : overrideSpecification.getScope(),
                                                overridenSpecification.getScope() == null
                                                ? "Multiton" : overridenSpecification.getScope()
                                            }, new ObjectFactory().createImplementation( implementation ) ) );

                                    }

                                    if ( overridenSpecification.getMultiplicity() == Multiplicity.MANY )
                                    {
                                        if ( override.getImplementationName() == null
                                             && overriden.getImplementationName() != null )
                                        {
                                            report.getDetails().add( this.createDetail(
                                                "IMPLEMENTATION_DEPENDENCY_NO_IMPLEMENTATION_NAME_CONSTRAINT",
                                                Level.SEVERE,
                                                "implementationDependencyNoImplementationNameConstraint", new Object[]
                                                {
                                                    implementation.getIdentifier(), dependency.getName(),
                                                    a.getIdentifier(), overriden.getName()
                                                }, new ObjectFactory().createImplementation( implementation ) ) );

                                        }

                                        if ( override.getImplementationName() != null
                                             && overriden.getImplementationName() == null )
                                        {
                                            report.getDetails().add( this.createDetail(
                                                "IMPLEMENTATION_DEPENDENCY_IMPLEMENTATION_NAME_CONSTRAINT",
                                                Level.SEVERE,
                                                "implementationDependencyImplementationNameConstraint", new Object[]
                                                {
                                                    implementation.getIdentifier(), dependency.getName(),
                                                    a.getIdentifier(), overriden.getName(),
                                                    override.getImplementationName()
                                                }, new ObjectFactory().createImplementation( implementation ) ) );

                                        }
                                    }
                                }

                                if ( override.isOptional() != overriden.isOptional() )
                                {
                                    report.getDetails().add( this.createDetail(
                                        "IMPLEMENTATION_DEPENDENCY_OPTIONALITY_CONSTRAINT", Level.SEVERE,
                                        "implementationDependencyOptonalityConstraint", new Object[]
                                        {
                                            implementation.getIdentifier(), dependency.getName(), a.getIdentifier(),
                                            overriden.getName()
                                        }, new ObjectFactory().createImplementation( implementation ) ) );

                                }
                            }
                        }
                    }
                }

                if ( dependency.getMessages() != null )
                {
                    final Messages messages = modules.getMessages( a.getIdentifier() );

                    if ( messages != null )
                    {
                        for ( Message override : dependency.getMessages().getMessage() )
                        {
                            final Message overriden = messages.getMessage( override.getName() );

                            if ( overriden != null && overriden.isFinal() )
                            {
                                report.getDetails().add( this.createDetail(
                                    "IMPLEMENTATION_DEPENDENCY_FINAL_MESSAGE_CONSTRAINT", Level.SEVERE,
                                    "implementationDependencyFinalMessageConstraint", new Object[]
                                    {
                                        implementation.getIdentifier(), dependency.getName(), a.getIdentifier(),
                                        override.getName()
                                    }, new ObjectFactory().createImplementation( implementation ) ) );

                            }
                            if ( overriden == null && override.isOverride() )
                            {
                                report.getDetails().add( this.createDetail(
                                    "IMPLEMENTATION_DEPENDENCY_OVERRIDE_MESSAGE_CONSTRAINT", Level.SEVERE,
                                    "implementationDependencyOverrideMessageConstraint", new Object[]
                                    {
                                        implementation.getIdentifier(), dependency.getName(), a.getIdentifier(),
                                        override.getName()
                                    }, new ObjectFactory().createImplementation( implementation ) ) );

                            }
                        }
                    }
                }

                if ( dependency.getProperties() != null )
                {
                    final Properties properties = modules.getProperties( a.getIdentifier() );

                    if ( properties != null )
                    {
                        for ( Property override : dependency.getProperties().getProperty() )
                        {
                            final Property overriden = properties.getProperty( override.getName() );

                            if ( overriden != null && overriden.isFinal() )
                            {
                                report.getDetails().add( this.createDetail(
                                    "IMPLEMENTATION_DEPENDENCY_FINAL_PROPERTY_CONSTRAINT", Level.SEVERE,
                                    "implementationDependencyFinalPropertyConstraint", new Object[]
                                    {
                                        implementation.getIdentifier(), dependency.getName(), a.getIdentifier(),
                                        override.getName()
                                    }, new ObjectFactory().createImplementation( implementation ) ) );

                            }
                            if ( overriden == null && override.isOverride() )
                            {
                                report.getDetails().add( this.createDetail(
                                    "IMPLEMENTATION_DEPENDENCY_OVERRIDE_PROPERTY_CONSTRAINT", Level.SEVERE,
                                    "implementationDependencyOverridePropertyConstraint", new Object[]
                                    {
                                        implementation.getIdentifier(), dependency.getName(), a.getIdentifier(),
                                        override.getName()
                                    }, new ObjectFactory().createImplementation( implementation ) ) );

                            }
                        }
                    }
                }
            }
        }

        if ( dependency.getDependencies() != null )
        {
            for ( Dependency d : dependency.getDependencies().getDependency() )
            {
                this.assertDependencyValid( context, modules, implementation, d, report );
            }
        }
    }

    private void assertValidImplementationInheritanceConstraints( final ModelContext context, final Modules modules,
                                                                  final Implementation implementation,
                                                                  final ModelValidationReport report )
    {
        if ( implementation.getImplementations() != null )
        {
            final Implementations parentImplementations = new Implementations();
            final Map<String, List<Dependency>> dependencyMap = new HashMap<String, List<Dependency>>();
            final Map<String, List<Message>> messageMap = new HashMap<String, List<Message>>();
            final Map<String, List<Property>> propertyMap = new HashMap<String, List<Property>>();
            final Map<String, List<SpecificationReference>> specMap =
                new HashMap<String, List<SpecificationReference>>();

            this.collectParentImplementations(
                modules, implementation, parentImplementations, new Implementations(), false );

            for ( ImplementationReference r : implementation.getImplementations().getReference() )
            {
                final Specifications currentSpecs = new Specifications();
                final Dependencies currentDependencies = new Dependencies();
                final Properties currentProperties = new Properties();
                final Messages currentMessages = new Messages();
                final Implementation current = modules.getImplementation( r.getIdentifier() );
                final ImplementationReference parentReference = parentImplementations.getReference( r.getIdentifier() );

                if ( current != null )
                {
                    this.collectSpecifications( modules, current, currentSpecs, new Implementations(), true );
                    this.collectDependencies( modules, current, currentDependencies, new Implementations(), true );
                    this.collectMessages( modules, current, currentMessages, new Implementations(), true );
                    this.collectProperties( modules, current, currentProperties, new Implementations(), true );

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

                    if ( r.getVersion() != null )
                    {
                        if ( current.getVersion() == null )
                        {
                            report.getDetails().add( this.createDetail(
                                "IMPLEMENTATION_IMPLEMENTATION_VERSIONING_CONSTRAINT", Level.SEVERE,
                                "implementationImplementationVersioningConstraint", new Object[]
                                {
                                    implementation.getIdentifier(), current.getIdentifier()
                                }, new ObjectFactory().createImplementation( implementation ) ) );

                        }
                        else
                        {
                            try
                            {
                                if ( VersionParser.compare( r.getVersion(), current.getVersion() ) > 0 )
                                {
                                    report.getDetails().add( this.createDetail(
                                        "IMPLEMENTATION_INHERITANCE_COMPATIBILITY_CONSTRAINT", Level.SEVERE,
                                        "implementationInheritanceCompatibilityConstraint", new Object[]
                                        {
                                            implementation.getIdentifier(), current.getIdentifier(),
                                            r.getVersion(), current.getVersion()
                                        }, new ObjectFactory().createImplementation( implementation ) ) );

                                }
                            }
                            catch ( final ParseException e )
                            {
                                if ( context.isLoggable( Level.FINE ) )
                                {
                                    context.log( Level.FINE, e.getMessage(), e );
                                }

                                final ModelValidationReport.Detail detail = new ModelValidationReport.Detail(
                                    "IMPLEMENTATION_INHERITANCE_COMPATIBILITY_VERSIONING_PARSE_EXCEPTION",
                                    Level.SEVERE, e.getMessage(),
                                    new ObjectFactory().createImplementation( implementation ) );

                                report.getDetails().add( detail );
                            }
                            catch ( final TokenMgrError e )
                            {
                                if ( context.isLoggable( Level.FINE ) )
                                {
                                    context.log( Level.FINE, e.getMessage(), e );
                                }

                                final ModelValidationReport.Detail detail = new ModelValidationReport.Detail(
                                    "IMPLEMENTATION_INHERITANCE_COMPATIBILITY_VERSIONING_TOKEN_MANAGER_ERROR",
                                    Level.SEVERE, e.getMessage(),
                                    new ObjectFactory().createImplementation( implementation ) );

                                report.getDetails().add( detail );
                            }
                        }
                    }

                    if ( current.isFinal() )
                    {
                        report.getDetails().add( this.createDetail(
                            "IMPLEMENTATION_IMPLEMENTATION_INHERITANCE_CONSTRAINT", Level.SEVERE,
                            "implementationFinalImplementationConstraint", new Object[]
                            {
                                implementation.getIdentifier(), current.getIdentifier(),
                            }, new ObjectFactory().createImplementation( implementation ) ) );

                    }

                    if ( parentReference != null && parentReference.isFinal() )
                    {
                        report.getDetails().add( this.createDetail(
                            "IMPLEMENTATION_IMPLEMENTATION_REFERENCE_INHERITANCE_CONSTRAINT", Level.SEVERE,
                            "implementationFinalImplementatioReferenceConstraint", new Object[]
                            {
                                implementation.getIdentifier(), parentReference.getIdentifier(),
                            }, new ObjectFactory().createImplementation( implementation ) ) );

                    }

                    if ( r.isOverride() && parentReference == null )
                    {
                        report.getDetails().add( this.createDetail(
                            "IMPLEMENTATION_IMPLEMENTATION_OVERRIDE_CONSTRAINT", Level.SEVERE,
                            "implementationImplementationOverrideConstraint", new Object[]
                            {
                                implementation.getIdentifier(), r.getIdentifier(),
                            }, new ObjectFactory().createImplementation( implementation ) ) );

                    }
                }
            }

            for ( Map.Entry<String, List<SpecificationReference>> e : specMap.entrySet() )
            {
                if ( e.getValue().size() > 1
                     && ( implementation.getSpecifications() == null
                          || implementation.getSpecifications().getReference( e.getKey() ) == null ) )
                {
                    report.getDetails().add( this.createDetail(
                        "IMPLEMENTATION_SPECIFICATION_MULTIPLE_INHERITANCE_CONSTRAINT", Level.SEVERE,
                        "implementationMultipleInheritanceSpecificationConstraint", new Object[]
                        {
                            implementation.getIdentifier(), e.getKey()
                        }, new ObjectFactory().createImplementation( implementation ) ) );

                }
            }

            for ( Map.Entry<String, List<Dependency>> e : dependencyMap.entrySet() )
            {
                if ( e.getValue().size() > 1
                     && ( implementation.getDependencies() == null
                          || implementation.getDependencies().getDependency( e.getKey() ) == null ) )
                {
                    report.getDetails().add( this.createDetail(
                        "IMPLEMENTATION_DEPENDENCY_MULTIPLE_INHERITANCE_CONSTRAINT", Level.SEVERE,
                        "implementationMultipleInheritanceDependencyConstraint", new Object[]
                        {
                            implementation.getIdentifier(), e.getKey()
                        }, new ObjectFactory().createImplementation( implementation ) ) );

                }
            }

            for ( Map.Entry<String, List<Message>> e : messageMap.entrySet() )
            {
                if ( e.getValue().size() > 1
                     && ( implementation.getMessages() == null
                          || ( implementation.getMessages().getMessage( e.getKey() ) == null
                               && implementation.getMessages().getReference( e.getKey() ) == null ) ) )
                {
                    report.getDetails().add( this.createDetail(
                        "IMPLEMENTATION_MESSAGE_MULTIPLE_INHERITANCE_CONSTRAINT", Level.SEVERE,
                        "implementationMultipleInheritanceMessageConstraint", new Object[]
                        {
                            implementation.getIdentifier(), e.getKey()
                        }, new ObjectFactory().createImplementation( implementation ) ) );

                }
            }

            for ( Map.Entry<String, List<Property>> e : propertyMap.entrySet() )
            {
                if ( e.getValue().size() > 1
                     && ( implementation.getProperties() == null
                          || ( implementation.getProperties().getProperty( e.getKey() ) == null
                               && implementation.getProperties().getReference( e.getKey() ) == null ) ) )
                {
                    report.getDetails().add( this.createDetail(
                        "IMPLEMENTATION_PROPERTY_MULTIPLE_INHERITANCE_CONSTRAINT", Level.SEVERE,
                        "implementationMultipleInheritancePropertyConstraint", new Object[]
                        {
                            implementation.getIdentifier(), e.getKey()
                        }, new ObjectFactory().createImplementation( implementation ) ) );

                }
            }
        }
    }

    private void assertImplementationSpecificationCompatibility( final ModelContext context, final Modules modules,
                                                                 final Implementation implementation,
                                                                 final ModelValidationReport report )
    {
        final Specifications specs = modules.getSpecifications( implementation.getIdentifier() );

        if ( specs != null )
        {
            for ( SpecificationReference r : specs.getReference() )
            {
                final Specification s = specs.getSpecification( r.getIdentifier() );

                if ( s != null && r.getVersion() != null )
                {
                    if ( s.getVersion() == null )
                    {
                        report.getDetails().add( this.createDetail(
                            "IMPLEMENTATION_SPECIFICATION_VERSIONING_CONSTRAINT", Level.SEVERE,
                            "implementationSpecificationVersioningConstraint", new Object[]
                            {
                                implementation.getIdentifier(), s.getIdentifier()
                            }, new ObjectFactory().createImplementation( implementation ) ) );

                    }
                    else
                    {
                        try
                        {
                            if ( VersionParser.compare( r.getVersion(), s.getVersion() ) != 0 )
                            {
                                final Module moduleOfImplementation =
                                    modules.getModuleOfImplementation( implementation.getIdentifier() );

                                final Module moduleOfSpecification =
                                    modules.getModuleOfSpecification( s.getIdentifier() );

                                report.getDetails().add( this.createDetail(
                                    "IMPLEMENTATION_SPECIFICATION_COMPATIBILITY_CONSTRAINT", Level.SEVERE,
                                    "implementationSpecificationCompatibilityConstraint", new Object[]
                                    {
                                        implementation.getIdentifier(),
                                        moduleOfImplementation != null ? moduleOfImplementation.getName() : "<>",
                                        s.getIdentifier(),
                                        moduleOfSpecification != null ? moduleOfSpecification.getName() : "<>",
                                        r.getVersion(), s.getVersion()
                                    }, new ObjectFactory().createImplementation( implementation ) ) );

                            }
                        }
                        catch ( final ParseException e )
                        {
                            if ( context.isLoggable( Level.FINE ) )
                            {
                                context.log( Level.FINE, e.getMessage(), e );
                            }

                            final ModelValidationReport.Detail d = new ModelValidationReport.Detail(
                                "IMPLEMENTATION_SPECIFICATION_COMPATIBILITY_VERSIONING_PARSE_EXCEPTION",
                                Level.SEVERE, e.getMessage(),
                                new ObjectFactory().createImplementation( implementation ) );

                            report.getDetails().add( d );
                        }
                        catch ( final TokenMgrError e )
                        {
                            if ( context.isLoggable( Level.FINE ) )
                            {
                                context.log( Level.FINE, e.getMessage(), e );
                            }

                            final ModelValidationReport.Detail d = new ModelValidationReport.Detail(
                                "IMPLEMENTATION_SPECIFICATION_COMPATIBILITY_VERSIONING_TOKEN_MANAGER_ERROR",
                                Level.SEVERE, e.getMessage(),
                                new ObjectFactory().createImplementation( implementation ) );

                            report.getDetails().add( d );
                        }
                    }
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

    private void collectParentImplementations( final Modules modules, final Implementation implementation,
                                               final Implementations implementations, final Implementations seen,
                                               final boolean includeImplementation )
    {
        if ( implementation != null && seen.getImplementation( implementation.getIdentifier() ) == null )
        {
            seen.getImplementation().add( implementation );

            if ( includeImplementation && implementations.getImplementation( implementation.getIdentifier() ) == null )
            {
                implementations.getImplementation().add( implementation );
            }

            if ( implementation.getImplementations() != null )
            {
                for ( ImplementationReference r : implementation.getImplementations().getReference() )
                {
                    if ( includeImplementation && implementations.getReference( r.getIdentifier() ) == null )
                    {
                        implementations.getReference().add( r );
                    }

                    this.collectParentImplementations( modules, modules.getImplementation( r.getIdentifier() ),
                                                       implementations, seen, true );

                }
            }
        }
    }

    private void collectParentSpecifications( final Modules modules, final Implementation implementation,
                                              final Specifications specifications, final Implementations seen,
                                              final boolean includeImplementation )
    {
        if ( implementation != null && seen.getImplementation( implementation.getIdentifier() ) == null )
        {
            seen.getImplementation().add( implementation );

            if ( includeImplementation && implementation.getSpecifications() != null )
            {
                for ( SpecificationReference r : implementation.getSpecifications().getReference() )
                {
                    if ( specifications.getReference( r.getIdentifier() ) == null )
                    {
                        specifications.getReference().add( r );
                    }
                }
            }

            if ( implementation.getImplementations() != null )
            {
                for ( ImplementationReference r : implementation.getImplementations().getReference() )
                {
                    this.collectParentSpecifications( modules, modules.getImplementation( r.getIdentifier() ),
                                                      specifications, seen, true );

                }
            }
        }
    }

    private void collectParentDependencies( final Modules modules, final Implementation implementation,
                                            final Dependencies dependencies, final Implementations seen,
                                            final boolean includeImplementation )
    {
        if ( implementation != null && seen.getImplementation( implementation.getIdentifier() ) == null )
        {
            seen.getImplementation().add( implementation );

            if ( includeImplementation && implementation.getDependencies() != null )
            {
                for ( Dependency d : implementation.getDependencies().getDependency() )
                {
                    if ( dependencies.getDependency( d.getName() ) == null )
                    {
                        dependencies.getDependency().add( d );
                    }
                }
            }

            if ( implementation.getImplementations() != null )
            {
                for ( ImplementationReference r : implementation.getImplementations().getReference() )
                {
                    this.collectParentDependencies( modules, modules.getImplementation( r.getIdentifier() ),
                                                    dependencies, seen, true );

                }
            }
        }
    }

    private void collectParentMessages( final Modules modules, final Implementation implementation,
                                        final Messages messages, final Implementations seen,
                                        final boolean includeImplementation )
    {
        if ( implementation != null && seen.getImplementation( implementation.getIdentifier() ) == null )
        {
            seen.getImplementation().add( implementation );

            if ( includeImplementation && implementation.getMessages() != null )
            {
                for ( Message m : implementation.getMessages().getMessage() )
                {
                    if ( messages.getMessage( m.getName() ) == null )
                    {
                        messages.getMessage().add( m );
                    }
                }
                for ( MessageReference r : implementation.getMessages().getReference() )
                {
                    if ( messages.getReference( r.getName() ) == null )
                    {
                        messages.getReference().add( r );
                    }
                }
            }

            if ( implementation.getImplementations() != null )
            {
                for ( ImplementationReference r : implementation.getImplementations().getReference() )
                {
                    this.collectParentMessages( modules, modules.getImplementation( r.getIdentifier() ),
                                                messages, seen, true );

                }
            }
        }
    }

    private void collectParentProperties( final Modules modules, final Implementation implementation,
                                          final Properties properties, final Implementations seen,
                                          final boolean includeImplementation )
    {
        if ( implementation != null && seen.getImplementation( implementation.getIdentifier() ) == null )
        {
            seen.getImplementation().add( implementation );

            if ( includeImplementation && implementation.getProperties() != null )
            {
                for ( Property p : implementation.getProperties().getProperty() )
                {
                    if ( properties.getProperty( p.getName() ) == null )
                    {
                        properties.getProperty().add( p );
                    }
                }
                for ( PropertyReference r : implementation.getProperties().getReference() )
                {
                    if ( properties.getReference( r.getName() ) == null )
                    {
                        properties.getReference().add( r );
                    }
                }
            }

            if ( implementation.getImplementations() != null )
            {
                for ( ImplementationReference r : implementation.getImplementations().getReference() )
                {
                    this.collectParentProperties( modules, modules.getImplementation( r.getIdentifier() ),
                                                  properties, seen, true );

                }
            }
        }
    }

    private void collectSpecifications( final Modules modules, final Implementation implementation,
                                        final Specifications specifications, final Implementations seen,
                                        final boolean includeDeclared )
    {
        if ( implementation != null && seen.getImplementation( implementation.getIdentifier() ) == null )
        {
            seen.getImplementation().add( implementation );

            if ( includeDeclared && implementation.getSpecifications() != null )
            {
                for ( SpecificationReference r : implementation.getSpecifications().getReference() )
                {
                    if ( specifications.getReference( r.getIdentifier() ) == null )
                    {
                        specifications.getReference().add( r );

                        final Specification s = modules.getSpecification( r.getIdentifier() );
                        if ( s != null && specifications.getSpecification( s.getIdentifier() ) == null )
                        {
                            specifications.getSpecification().add( s );
                        }
                    }
                }
            }

            if ( implementation.getImplementations() != null )
            {
                for ( ImplementationReference r : implementation.getImplementations().getReference() )
                {
                    this.collectSpecifications( modules, modules.getImplementation( r.getIdentifier() ),
                                                specifications, seen, true );

                }
            }
        }
    }

    private void collectDependencies( final Modules modules, final Implementation implementation,
                                      final Dependencies dependencies, final Implementations seen,
                                      final boolean includeDeclared )
    {
        if ( implementation != null && seen.getImplementation( implementation.getIdentifier() ) == null )
        {
            seen.getImplementation().add( implementation );

            if ( includeDeclared && implementation.getDependencies() != null )
            {
                for ( Dependency d : implementation.getDependencies().getDependency() )
                {
                    final Dependency dependency = dependencies.getDependency( d.getName() );

                    if ( dependency == null )
                    {
                        dependencies.getDependency().add( d );
                    }
                    else
                    {
                        this.collectDependencies( d, dependency );
                    }
                }
            }

            if ( implementation.getImplementations() != null )
            {
                for ( ImplementationReference r : implementation.getImplementations().getReference() )
                {
                    this.collectDependencies( modules, modules.getImplementation( r.getIdentifier() ), dependencies,
                                              seen, true );

                }
            }
        }
    }

    private void collectDependencies( final Dependency source, final Dependency target )
    {
        if ( source.getMessages() != null )
        {
            if ( target.getMessages() == null )
            {
                target.setMessages( new Messages() );
            }

            for ( Message m : source.getMessages().getMessage() )
            {
                if ( target.getMessages().getMessage( m.getName() ) == null )
                {
                    target.getMessages().getMessage().add( m );
                }
            }
        }

        if ( source.getProperties() != null )
        {
            if ( target.getProperties() == null )
            {
                target.setProperties( new Properties() );
            }

            for ( Property p : source.getProperties().getProperty() )
            {
                if ( target.getProperties().getProperty( p.getName() ) == null )
                {
                    target.getProperties().getProperty().add( p );
                }
            }
        }

        if ( source.getDependencies() != null )
        {
            if ( target.getDependencies() == null )
            {
                target.setDependencies( new Dependencies() );
            }

            for ( Dependency sd : source.getDependencies().getDependency() )
            {
                final Dependency td = target.getDependencies().getDependency( sd.getName() );

                if ( td == null )
                {
                    target.getDependencies().getDependency().add( sd );
                }
                else
                {
                    this.collectDependencies( sd, td );
                }
            }
        }
    }

    private void collectProperties( final Modules modules, final Implementation implementation,
                                    final Properties properties, final Implementations seen,
                                    final boolean includeDeclared )
    {
        if ( implementation != null && seen.getImplementation( implementation.getIdentifier() ) == null )
        {
            seen.getImplementation().add( implementation );

            if ( includeDeclared && implementation.getProperties() != null )
            {
                for ( Property p : implementation.getProperties().getProperty() )
                {
                    if ( properties.getProperty( p.getName() ) == null )
                    {
                        properties.getProperty().add( p );
                    }
                }
                if ( !implementation.getProperties().getReference().isEmpty() )
                {
                    final Module m = modules.getModuleOfImplementation( implementation.getIdentifier() );

                    if ( m != null )
                    {
                        for ( PropertyReference ref : implementation.getProperties().getReference() )
                        {
                            if ( properties.getProperty( ref.getName() ) == null )
                            {
                                Property referenced = m.getProperties().getProperty( ref.getName() );
                                if ( referenced != null )
                                {
                                    referenced = new Property( referenced );
                                    referenced.setDeprecated( ref.isDeprecated() );
                                    referenced.setFinal( ref.isFinal() );
                                    referenced.setOverride( ref.isOverride() );
                                    properties.getProperty().add( referenced );
                                }
                            }
                        }
                    }
                }
            }

            if ( implementation.getImplementations() != null )
            {
                for ( ImplementationReference r : implementation.getImplementations().getReference() )
                {
                    this.collectProperties( modules, modules.getImplementation( r.getIdentifier() ), properties,
                                            seen, true );

                }
            }
        }
    }

    private void collectMessages( final Modules modules, final Implementation implementation, final Messages messages,
                                  final Implementations seen, final boolean includeDeclared )
    {
        if ( implementation != null && seen.getImplementation( implementation.getIdentifier() ) == null )
        {
            seen.getImplementation().add( implementation );

            if ( includeDeclared && implementation.getMessages() != null )
            {
                for ( Message msg : implementation.getMessages().getMessage() )
                {
                    if ( messages.getMessage( msg.getName() ) == null )
                    {
                        messages.getMessage().add( msg );
                    }
                }
                if ( !implementation.getMessages().getReference().isEmpty() )
                {
                    final Module m = modules.getModuleOfImplementation( implementation.getIdentifier() );

                    if ( m != null )
                    {
                        for ( MessageReference ref : implementation.getMessages().getReference() )
                        {
                            if ( messages.getMessage( ref.getName() ) == null )
                            {
                                Message referenced = m.getMessages().getMessage( ref.getName() );
                                if ( referenced != null )
                                {
                                    referenced = new Message( referenced );
                                    referenced.setDeprecated( ref.isDeprecated() );
                                    referenced.setFinal( ref.isFinal() );
                                    referenced.setOverride( ref.isOverride() );
                                    messages.getMessage().add( referenced );
                                }
                            }
                        }
                    }
                }
            }

            if ( implementation.getImplementations() != null )
            {
                for ( ImplementationReference r : implementation.getImplementations().getReference() )
                {
                    this.collectMessages( modules, modules.getImplementation( r.getIdentifier() ), messages, seen,
                                          true );

                }
            }
        }
    }

    private ModelValidationReport.Detail createDetail( final String identifier, final Level level,
                                                       final String messageKey, final Object messageArguments,
                                                       final JAXBElement<? extends ModelObject> element )
    {
        return new ModelValidationReport.Detail(
            identifier, level, this.getMessage( messageKey, messageArguments ), element );

    }

    private String getMessage( final String key, final Object args )
    {
        return new MessageFormat( ResourceBundle.getBundle(
            DefaultModelValidator.class.getName().replace( '.', '/' ),
            Locale.getDefault() ).getString( key ) ).format( args );

    }

}
