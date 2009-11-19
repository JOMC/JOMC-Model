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

import java.io.StringWriter;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Level;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventHandler;
import javax.xml.validation.Schema;
import org.jomc.util.ParseException;
import org.jomc.util.TokenMgrError;
import org.jomc.util.VersionParser;

/**
 * Default {@code ModelObjectValidator} implementation.
 *
 * @author <a href="mailto:cs@jomc.org">Christian Schulte</a>
 * @version $Id$
 */
public class DefaultModelObjectValidator implements ModelObjectValidator
{

    /** Creates a new {@code DefaultModelObjectValidator} instance. */
    public DefaultModelObjectValidator()
    {
        super();
    }

    public ModelObjectValidationReport validateModelObject(
        final JAXBElement modelObject, final JAXBContext context, final Schema schema ) throws JAXBException
    {
        if ( modelObject == null )
        {
            throw new NullPointerException( "modelObject" );
        }
        if ( modelObject.getValue() == null )
        {
            throw new NullPointerException( "modelObject" );
        }
        if ( context == null )
        {
            throw new NullPointerException( "context" );
        }
        if ( schema == null )
        {
            throw new NullPointerException( "schema" );
        }

        final Marshaller marshaller = context.createMarshaller();
        final ModelObjectValidationReport report = new ModelObjectValidationReport( modelObject );
        marshaller.setSchema( schema );
        marshaller.setEventHandler( new ModelObjectValidationEventHandler( report ) );

        try
        {
            marshaller.marshal( modelObject, new StringWriter() );
        }
        catch ( final JAXBException e )
        {
            if ( report.getDetails().isEmpty() )
            {
                throw e;
            }
        }

        return report;
    }

    public ModelObjectValidationReport validateModules(
        final JAXBElement<Modules> modules, final JAXBContext context, final Schema schema ) throws JAXBException
    {
        if ( modules == null )
        {
            throw new NullPointerException( "modules" );
        }
        if ( modules.getValue() == null )
        {
            throw new NullPointerException( "modules" );
        }
        if ( context == null )
        {
            throw new NullPointerException( "context" );
        }
        if ( schema == null )
        {
            throw new NullPointerException( "schema" );
        }

        final ModelObjectValidationReport report = this.validateModelObject( modules, context, schema );

        for ( Module m : modules.getValue().getModule() )
        {
            this.assertNoSpecificationReferenceDeclarations( m, report );
            this.assertNoImplementationReferenceDeclarations( m, report );
            this.assertNoMessageReferenceDeclarations( m, report );
            this.assertNoFinalMessageDeclarations( m, report );
            this.assertNoOverrideMessageDeclarations( m, report );
            this.assertNoPropertyReferenceDeclarations( m, report );
            this.assertNoFinalPropertyDeclarations( m, report );
            this.assertNoOverridePropertyDeclarations( m, report );

            if ( m.getImplementations() != null )
            {
                for ( Implementation i : m.getImplementations().getImplementation() )
                {
                    this.assertNoDependencyPropertyReferenceDeclarations( i, report );
                    this.assertNoImplementationDeclarations( i, report );
                    this.assertNoLocationWhenAbstract( i, report );
                    this.assertNoSpecificationDeclarations( i, report );
                    this.assertImplementationMessagesUniqueness( i, report );
                    this.assertImplementationPropertiesUniqueness( i, report );
                    this.assertImplementationDependencyCompatibility( modules.getValue(), i, report );
                    this.assertImplementationInheritanceCompatibility( modules.getValue(), i, report );
                    this.assertImplementationSpecificationCompatibility( modules.getValue(), i, report );
                    this.assertNoMissingMandatoryDependencies( modules.getValue(), i, report );
                    this.assertNoDependenciesWithoutSpecificationClass( modules.getValue(), i, report );
                    this.assertNoInheritanceCycle( modules.getValue(), i, report );
                    this.assertNoInheritanceClashes( modules.getValue(), i, report );
                    this.assertNoOverridenDependencyPropertiesWhenNotMultiton( modules.getValue(), i, report );
                    this.assertImplementationOverrideConstraints( modules.getValue(), i, report );
                    this.assertSpecificationOverrideConstraints( modules.getValue(), i, report );
                    this.assertDependencyOverrideConstraints( modules.getValue(), i, report );
                    this.assertMessageOverrideConstraints( modules.getValue(), i, report );
                    this.assertPropertyOverrideConstraints( modules.getValue(), i, report );
                }
            }

            if ( m.getSpecifications() != null )
            {
                for ( Specification s : m.getSpecifications().getSpecification() )
                {
                    this.assertNoSpecificationPropertyReferenceDeclarations( s, report );
                    this.assertSpecificationImplementationNameUniqueness( modules.getValue(), s, report );
                    this.assertSpecificationMultiplicityConstraint( modules.getValue(), s, report );
                }
            }
        }

        return report;
    }

    private void assertNoSpecificationReferenceDeclarations( final Module module,
                                                             final ModelObjectValidationReport report )
    {
        if ( module.getSpecifications() != null )
        {
            for ( SpecificationReference r : module.getSpecifications().getReference() )
            {
                report.getDetails().add( this.createDetail(
                    "MODULE_SPECIFICATION_REFERENCE_DECLARATION_CONSTRAINT", Level.SEVERE,
                    "moduleSpecificationReferenceDeclarationConstraint", new Object[]
                    {
                        module.getName(), r.getIdentifier()
                    }, new ObjectFactory().createModule( module ) ) );

            }
        }
    }

    private void assertNoImplementationReferenceDeclarations( final Module module,
                                                              final ModelObjectValidationReport report )
    {
        if ( module.getImplementations() != null )
        {
            for ( ImplementationReference r : module.getImplementations().getReference() )
            {
                report.getDetails().add( this.createDetail(
                    "MODULE_IMPLEMENTATION_REFERENCE_DECLARATION_CONSTRAINT", Level.SEVERE,
                    "moduleImplementationReferenceDeclarationConstraint", new Object[]
                    {
                        module.getName(), r.getIdentifier()
                    }, new ObjectFactory().createModule( module ) ) );

            }
        }
    }

    private void assertNoMessageReferenceDeclarations( final Module module,
                                                       final ModelObjectValidationReport report )
    {
        if ( module.getMessages() != null )
        {
            for ( MessageReference r : module.getMessages().getReference() )
            {
                report.getDetails().add( this.createDetail(
                    "MODULE_MESSAGE_REFERENCE_DECLARATION_CONSTRAINT", Level.SEVERE,
                    "moduleMessageReferenceDeclarationConstraint", new Object[]
                    {
                        module.getName(), r.getName()
                    }, new ObjectFactory().createModule( module ) ) );

            }
        }
    }

    private void assertNoFinalMessageDeclarations( final Module module,
                                                   final ModelObjectValidationReport report )
    {
        if ( module.getMessages() != null )
        {
            for ( Message m : module.getMessages().getMessage() )
            {
                if ( m.isFinal() )
                {
                    report.getDetails().add( this.createDetail(
                        "MODULE_FINAL_MESSAGE_DECLARATION_CONSTRAINT", Level.SEVERE,
                        "moduleFinalMessageConstraint", new Object[]
                        {
                            module.getName(), m.getName()
                        }, new ObjectFactory().createModule( module ) ) );

                }
            }
        }
    }

    private void assertNoOverrideMessageDeclarations( final Module module,
                                                      final ModelObjectValidationReport report )
    {
        if ( module.getMessages() != null )
        {
            for ( Message m : module.getMessages().getMessage() )
            {
                if ( m.isOverride() )
                {
                    report.getDetails().add( this.createDetail(
                        "MODULE_OVERRIDE_MESSAGE_DECLARATION_CONSTRAINT", Level.SEVERE,
                        "moduleOverrideMessageConstraint", new Object[]
                        {
                            module.getName(), m.getName()
                        }, new ObjectFactory().createModule( module ) ) );

                }
            }
        }
    }

    private void assertNoPropertyReferenceDeclarations( final Module module,
                                                        final ModelObjectValidationReport report )
    {
        if ( module.getProperties() != null )
        {
            for ( PropertyReference r : module.getProperties().getReference() )
            {
                report.getDetails().add( this.createDetail(
                    "MODULE_PROPERTY_REFERENCE_DECLARATION_CONSTRAINT", Level.SEVERE,
                    "modulePropertyReferenceDeclarationConstraint", new Object[]
                    {
                        module.getName(), r.getName()
                    }, new ObjectFactory().createModule( module ) ) );

            }
        }
    }

    private void assertNoFinalPropertyDeclarations( final Module module,
                                                    final ModelObjectValidationReport report )
    {
        if ( module.getProperties() != null )
        {
            for ( Property p : module.getProperties().getProperty() )
            {
                if ( p.isFinal() )
                {
                    report.getDetails().add( this.createDetail(
                        "MODULE_FINAL_PROPERTY_DECLARATION_CONSTRAINT", Level.SEVERE,
                        "moduleFinalPropertyConstraint", new Object[]
                        {
                            module.getName(), p.getName()
                        }, new ObjectFactory().createModule( module ) ) );

                }
            }
        }
    }

    private void assertNoOverridePropertyDeclarations( final Module module,
                                                       final ModelObjectValidationReport report )
    {
        if ( module.getProperties() != null )
        {
            for ( Property p : module.getProperties().getProperty() )
            {
                if ( p.isOverride() )
                {
                    report.getDetails().add( this.createDetail(
                        "MODULE_OVERRIDE_PROPERTY_DECLARATION_CONSTRAINT", Level.SEVERE,
                        "moduleOverridePropertyConstraint", new Object[]
                        {
                            module.getName(), p.getName()
                        }, new ObjectFactory().createModule( module ) ) );

                }
            }
        }
    }

    private void assertNoSpecificationDeclarations( final Implementation implementation,
                                                    final ModelObjectValidationReport report )
    {
        if ( implementation.getSpecifications() != null )
        {
            for ( Specification s : implementation.getSpecifications().getSpecification() )
            {
                report.getDetails().add( this.createDetail(
                    "IMPLEMENTATION_SPECIFICATION_DECLARATION_CONSTRAINT", Level.SEVERE,
                    "implementationSpecificationDeclarationConstraint", new Object[]
                    {
                        implementation.getIdentifier(), s.getIdentifier()
                    }, new ObjectFactory().createImplementation( implementation ) ) );

            }
        }
    }

    private void assertNoImplementationDeclarations( final Implementation implementation,
                                                     final ModelObjectValidationReport report )
    {
        if ( implementation.getImplementations() != null )
        {
            for ( Implementation i : implementation.getImplementations().getImplementation() )
            {
                report.getDetails().add( this.createDetail(
                    "IMPLEMENTATION_IMPLEMENTATION_DECLARATION_CONSTRAINT", Level.SEVERE,
                    "implementationImplementationDeclarationConstraint", new Object[]
                    {
                        implementation.getIdentifier(), i.getIdentifier()
                    }, new ObjectFactory().createImplementation( implementation ) ) );

            }
        }
    }

    private void assertNoLocationWhenAbstract( final Implementation implementation,
                                               final ModelObjectValidationReport report )
    {
        if ( implementation.isAbstract() && implementation.getLocation() != null )
        {
            report.getDetails().add( this.createDetail(
                "IMPLEMENTATION_ABSTRACT_LOCATION_DECLARATION_CONSTRAINT", Level.SEVERE,
                "implementationAbstractLocationDeclarationConstraint", new Object[]
                {
                    implementation.getIdentifier(), implementation.getLocation()
                }, new ObjectFactory().createImplementation( implementation ) ) );

        }
    }

    private void assertNoInheritanceCycle( final Modules modules,
                                           final Implementation implementation,
                                           final ModelObjectValidationReport report )
    {
        final Implementation cycle =
            this.findInheritanceCycle( modules, implementation, implementation, new Implementations() );

        if ( cycle != null )
        {
            report.getDetails().add( this.createDetail(
                "IMPLEMENTATION_INHERITANCE_CYCLE_CONSTRAINT", Level.SEVERE,
                "implementationInheritanceCycleConstraint", new Object[]
                {
                    implementation.getIdentifier(), cycle.getIdentifier()
                }, new ObjectFactory().createImplementation( implementation ) ) );

        }
    }

    private void assertImplementationSpecificationCompatibility( final Modules modules,
                                                                 final Implementation implementation,
                                                                 final ModelObjectValidationReport report )
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
                            final ModelObjectValidationReport.Detail d = new ModelObjectValidationReport.Detail(
                                e.getClass().getSimpleName(), Level.SEVERE, e.getMessage() );

                            d.setElement( new ObjectFactory().createImplementation( implementation ) );
                            report.getDetails().add( d );
                        }
                        catch ( final TokenMgrError e )
                        {
                            final ModelObjectValidationReport.Detail d = new ModelObjectValidationReport.Detail(
                                e.getClass().getSimpleName(), Level.SEVERE, e.getMessage() );

                            d.setElement( new ObjectFactory().createImplementation( implementation ) );
                            report.getDetails().add( d );
                        }
                    }
                }
            }
        }
    }

    private void assertImplementationDependencyCompatibility( final Modules modules,
                                                              final Implementation implementation,
                                                              final ModelObjectValidationReport report )
    {
        final Dependencies dependencies = modules.getDependencies( implementation.getIdentifier() );
        if ( dependencies != null )
        {
            for ( Dependency d : dependencies.getDependency() )
            {
                final Specification s = modules.getSpecification( d.getIdentifier() );
                if ( s != null && d.getVersion() != null )
                {
                    if ( s.getVersion() == null )
                    {
                        report.getDetails().add( this.createDetail(
                            "IMPLEMENTATION_DEPENDENCY_SPECIFICATION_VERSIONING_CONSTRAINT", Level.SEVERE,
                            "implementationDependencySpecificationVersioningConstraint", new Object[]
                            {
                                implementation.getIdentifier(), d.getName(), s.getIdentifier()
                            }, new ObjectFactory().createImplementation( implementation ) ) );

                    }
                    else
                    {
                        try
                        {
                            if ( VersionParser.compare( d.getVersion(), s.getVersion() ) > 0 )
                            {
                                final Module moduleOfImplementation =
                                    modules.getModuleOfImplementation( implementation.getIdentifier() );

                                final Module moduleOfSpecification =
                                    modules.getModuleOfSpecification( s.getIdentifier() );

                                report.getDetails().add( this.createDetail(
                                    "IMPLEMENTATION_DEPENDENCY_SPECIFICATION_COMPATIBILITY_CONSTRAINT", Level.SEVERE,
                                    "implementationDependencySpecificationCompatibilityConstraint", new Object[]
                                    {
                                        implementation.getIdentifier(),
                                        moduleOfImplementation != null ? moduleOfImplementation.getName() : "<>",
                                        s.getIdentifier(),
                                        moduleOfSpecification != null ? moduleOfSpecification.getName() : "<>",
                                        d.getVersion(), s.getVersion()
                                    }, new ObjectFactory().createImplementation( implementation ) ) );

                            }
                        }
                        catch ( final ParseException e )
                        {
                            final ModelObjectValidationReport.Detail detail = new ModelObjectValidationReport.Detail(
                                e.getClass().getSimpleName(), Level.SEVERE, e.getMessage() );

                            detail.setElement( new ObjectFactory().createImplementation( implementation ) );
                            report.getDetails().add( detail );
                        }
                        catch ( final TokenMgrError e )
                        {
                            final ModelObjectValidationReport.Detail detail = new ModelObjectValidationReport.Detail(
                                e.getClass().getSimpleName(), Level.SEVERE, e.getMessage() );

                            detail.setElement( new ObjectFactory().createImplementation( implementation ) );
                            report.getDetails().add( detail );
                        }
                    }
                }
            }
        }
    }

    private void assertNoDependencyPropertyReferenceDeclarations( final Implementation implementation,
                                                                  final ModelObjectValidationReport report )
    {
        if ( implementation.getDependencies() != null )
        {
            for ( Dependency d : implementation.getDependencies().getDependency() )
            {
                if ( d.getProperties() != null )
                {
                    for ( PropertyReference r : d.getProperties().getReference() )
                    {
                        report.getDetails().add( this.createDetail(
                            "IMPLEMENTATION_DEPENDENCY_PROPERTY_REFERENCE_DECLARATION_CONSTRAINT", Level.SEVERE,
                            "implementationDependencyPropertyReferenceDeclarationConstraint", new Object[]
                            {
                                implementation.getIdentifier(), d.getName(), r.getName()
                            }, new ObjectFactory().createImplementation( implementation ) ) );

                    }
                }
            }
        }
    }

    private void assertNoOverridenDependencyPropertiesWhenNotMultiton( final Modules modules,
                                                                       final Implementation implementation,
                                                                       final ModelObjectValidationReport report )
    {
        if ( implementation.getDependencies() != null )
        {
            for ( Dependency d : implementation.getDependencies().getDependency() )
            {
                final Specification s = modules.getSpecification( d.getIdentifier() );

                if ( s != null && s.getScope() != null && d.getProperties() != null )
                {
                    for ( Property p : d.getProperties().getProperty() )
                    {
                        report.getDetails().add( this.createDetail(
                            "IMPLEMENTATION_DEPENDENCY_PROPERTIES_OVERRIDE_CONSTRAINT", Level.SEVERE,
                            "implementationDependencyPropertiesOverrideConstraint", new Object[]
                            {
                                implementation.getIdentifier(), d.getName(), s.getIdentifier(), s.getScope(),
                                p.getName()
                            }, new ObjectFactory().createImplementation( implementation ) ) );

                    }
                }
            }
        }
    }

    private void assertNoMissingMandatoryDependencies( final Modules modules, final Implementation implementation,
                                                       final ModelObjectValidationReport report )
    {
        if ( implementation.getDependencies() != null )
        {
            for ( Dependency d : implementation.getDependencies().getDependency() )
            {
                final Implementations available = modules.getImplementations( d.getIdentifier() );

                if ( !d.isOptional() )
                {
                    boolean missing = false;

                    if ( available == null )
                    {
                        missing = true;
                    }
                    else if ( available.getImplementation().isEmpty() )
                    {
                        missing = true;
                    }
                    else if ( d.getImplementationName() != null &&
                              available.getImplementationByName( d.getImplementationName() ) == null )
                    {
                        missing = true;
                    }

                    if ( missing )
                    {
                        report.getDetails().add( this.createDetail(
                            "IMPLEMENTATION_MANDATORY_DEPENDENCY_CONSTRAINT", Level.SEVERE,
                            "implementationMandatoryDependencyConstraint", new Object[]
                            {
                                implementation.getIdentifier(), d.getName()
                            }, new ObjectFactory().createImplementation( implementation ) ) );

                    }
                }
            }
        }
    }

    private void assertNoDependenciesWithoutSpecificationClass( final Modules modules,
                                                                final Implementation implementation,
                                                                final ModelObjectValidationReport report )
    {
        if ( implementation.getDependencies() != null )
        {
            for ( Dependency d : implementation.getDependencies().getDependency() )
            {
                final Specification s = modules.getSpecification( d.getIdentifier() );

                if ( s != null && s.getClazz() == null )
                {
                    report.getDetails().add( this.createDetail(
                        "IMPLEMENTATION_DEPENDENCY_SPECIFICATION_CLASS_CONSTRAINT", Level.SEVERE,
                        "implementationDependencySpecificationClassConstraint", new Object[]
                        {
                            implementation.getIdentifier(), d.getName(), d.getIdentifier()
                        }, new ObjectFactory().createImplementation( implementation ) ) );

                }
            }
        }
    }

    private void assertImplementationInheritanceCompatibility( final Modules modules,
                                                               final Implementation implementation,
                                                               final ModelObjectValidationReport report )
    {
        if ( implementation.getImplementations() != null )
        {
            for ( ImplementationReference r : implementation.getImplementations().getReference() )
            {
                final Implementation referenced = modules.getImplementation( r.getIdentifier() );
                if ( referenced != null && r.getVersion() != null )
                {
                    if ( referenced.getVersion() == null )
                    {
                        report.getDetails().add( this.createDetail(
                            "IMPLEMENTATION_IMPLEMENTATION_VERSIONING_CONSTRAINT", Level.SEVERE,
                            "implementationImplementationVersioningConstraint", new Object[]
                            {
                                implementation.getIdentifier(), referenced.getIdentifier()
                            }, new ObjectFactory().createImplementation( implementation ) ) );

                    }
                    else
                    {
                        try
                        {
                            if ( VersionParser.compare( r.getVersion(), referenced.getVersion() ) > 0 )
                            {
                                report.getDetails().add( this.createDetail(
                                    "IMPLEMENTATION_INHERITANCE_COMPATIBILITY_CONSTRAINT", Level.SEVERE,
                                    "implementationInheritanceCompatibilityConstraint", new Object[]
                                    {
                                        implementation.getIdentifier(), referenced.getIdentifier(),
                                        r.getVersion(), referenced.getVersion()
                                    }, new ObjectFactory().createImplementation( implementation ) ) );

                            }
                        }
                        catch ( final ParseException e )
                        {
                            final ModelObjectValidationReport.Detail detail = new ModelObjectValidationReport.Detail(
                                e.getClass().getSimpleName(), Level.SEVERE, e.getMessage() );

                            detail.setElement( new ObjectFactory().createImplementation( implementation ) );
                            report.getDetails().add( detail );
                        }
                        catch ( final TokenMgrError e )
                        {
                            final ModelObjectValidationReport.Detail detail = new ModelObjectValidationReport.Detail(
                                e.getClass().getSimpleName(), Level.SEVERE, e.getMessage() );

                            detail.setElement( new ObjectFactory().createImplementation( implementation ) );
                            report.getDetails().add( detail );
                        }
                    }
                }
            }
        }
    }

    private void assertImplementationOverrideConstraints( final Modules modules,
                                                          final Implementation implementation,
                                                          final ModelObjectValidationReport report )
    {
        final Implementations parentImplementations = new Implementations();
        this.collectParentImplementations(
            modules, implementation, parentImplementations, new Implementations(), false );

        if ( implementation.getImplementations() != null )
        {
            for ( ImplementationReference r : implementation.getImplementations().getReference() )
            {
                final Implementation referenced = modules.getImplementation( r.getIdentifier() );
                final ImplementationReference parentReference = parentImplementations.getReference( r.getIdentifier() );

                if ( referenced.isFinal() )
                {
                    report.getDetails().add( this.createDetail(
                        "IMPLEMENTATION_IMPLEMENTATION_INHERITANCE_CONSTRAINT", Level.SEVERE,
                        "implementationFinalImplementationConstraint", new Object[]
                        {
                            implementation.getIdentifier(), referenced.getIdentifier(),
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
    }

    private void assertSpecificationOverrideConstraints( final Modules modules,
                                                         final Implementation implementation,
                                                         final ModelObjectValidationReport report )
    {
        final Specifications parentSpecifications = new Specifications();
        this.collectParentSpecifications( modules, implementation, parentSpecifications, new Implementations(), false );

        if ( implementation.getSpecifications() != null )
        {
            for ( SpecificationReference r : implementation.getSpecifications().getReference() )
            {
                final SpecificationReference parent = parentSpecifications.getReference( r.getIdentifier() );

                if ( r.isOverride() && parent == null )
                {
                    report.getDetails().add( this.createDetail(
                        "IMPLEMENTATION_SPECIFICATION_OVERRIDE_CONSTRAINT", Level.SEVERE,
                        "implementationSpecificationOverrideConstraint", new Object[]
                        {
                            implementation.getIdentifier(), r.getIdentifier(),
                        }, new ObjectFactory().createImplementation( implementation ) ) );

                }
                if ( parent != null && parent.isFinal() )
                {
                    report.getDetails().add( this.createDetail(
                        "IMPLEMENTATION_SPECIFICATION_INHERITANCE_CONSTRAINT", Level.SEVERE,
                        "implementationSpecificationFinalConstraint", new Object[]
                        {
                            implementation.getIdentifier(), r.getIdentifier(),
                        }, new ObjectFactory().createImplementation( implementation ) ) );

                }
            }
        }
    }

    private void assertDependencyOverrideConstraints( final Modules modules,
                                                      final Implementation implementation,
                                                      final ModelObjectValidationReport report )
    {
        final Dependencies parentDependencies = new Dependencies();
        this.collectParentDependencies( modules, implementation, parentDependencies, new Implementations(), false );

        if ( implementation.getDependencies() != null )
        {
            for ( Dependency d : implementation.getDependencies().getDependency() )
            {
                final Dependency parent = parentDependencies.getDependency( d.getName() );
                if ( d.isOverride() && parent == null )
                {
                    report.getDetails().add( this.createDetail(
                        "IMPLEMENTATION_DEPENDENCY_OVERRIDE_CONSTRAINT", Level.SEVERE,
                        "implementationDependencyOverrideConstraint", new Object[]
                        {
                            implementation.getIdentifier(), d.getName(),
                        }, new ObjectFactory().createImplementation( implementation ) ) );

                }
                if ( parent != null && parent.isFinal() )
                {
                    report.getDetails().add( this.createDetail(
                        "IMPLEMENTATION_DEPENDENCY_INHERITANCE_CONSTRAINT", Level.SEVERE,
                        "implementationDependencyFinalConstraint", new Object[]
                        {
                            implementation.getIdentifier(), d.getName(),
                        }, new ObjectFactory().createImplementation( implementation ) ) );

                }
            }
        }
    }

    private void assertMessageOverrideConstraints( final Modules modules,
                                                   final Implementation implementation,
                                                   final ModelObjectValidationReport report )
    {
        final Messages parentMessages = new Messages();
        this.collectParentMessages( modules, implementation, parentMessages, new Implementations(), false );

        if ( implementation.getMessages() != null )
        {
            for ( Message m : implementation.getMessages().getMessage() )
            {
                final Message parentMessage = parentMessages.getMessage( m.getName() );
                final MessageReference parentReference = parentMessages.getReference( m.getName() );

                if ( m.isOverride() && parentMessage == null && parentReference == null )
                {
                    report.getDetails().add( this.createDetail(
                        "IMPLEMENTATION_MESSAGE_OVERRIDE_CONSTRAINT", Level.SEVERE,
                        "implementationMessageOverrideConstraint", new Object[]
                        {
                            implementation.getIdentifier(), m.getName(),
                        }, new ObjectFactory().createImplementation( implementation ) ) );

                }
                if ( ( parentMessage != null && parentMessage.isFinal() ) ||
                     ( parentReference != null && parentReference.isFinal() ) )
                {
                    report.getDetails().add( this.createDetail(
                        "IMPLEMENTATION_MESSAGE_INHERITANCE_CONSTRAINT", Level.SEVERE,
                        "implementationMessageFinalConstraint", new Object[]
                        {
                            implementation.getIdentifier(), m.getName(),
                        }, new ObjectFactory().createImplementation( implementation ) ) );

                }
            }
            for ( MessageReference r : implementation.getMessages().getReference() )
            {
                final Message parentMessage = parentMessages.getMessage( r.getName() );
                final MessageReference parentReference = parentMessages.getReference( r.getName() );

                if ( r.isOverride() && parentMessage == null && parentReference == null )
                {
                    report.getDetails().add( this.createDetail(
                        "IMPLEMENTATION_MESSAGE_OVERRIDE_CONSTRAINT", Level.SEVERE,
                        "implementationMessageOverrideConstraint", new Object[]
                        {
                            implementation.getIdentifier(), r.getName(),
                        }, new ObjectFactory().createImplementation( implementation ) ) );

                }
                if ( ( parentMessage != null && parentMessage.isFinal() ) ||
                     ( parentReference != null && parentReference.isFinal() ) )
                {
                    report.getDetails().add( this.createDetail(
                        "IMPLEMENTATION_MESSAGE_INHERITANCE_CONSTRAINT", Level.SEVERE,
                        "implementationMessageFinalConstraint", new Object[]
                        {
                            implementation.getIdentifier(), r.getName(),
                        }, new ObjectFactory().createImplementation( implementation ) ) );

                }
            }
        }
    }

    private void assertPropertyOverrideConstraints( final Modules modules,
                                                    final Implementation implementation,
                                                    final ModelObjectValidationReport report )
    {
        final Properties parentProperties = new Properties();
        this.collectParentProperties( modules, implementation, parentProperties, new Implementations(), false );

        if ( implementation.getProperties() != null )
        {
            for ( Property p : implementation.getProperties().getProperty() )
            {
                final Property parentProperty = parentProperties.getProperty( p.getName() );
                final PropertyReference parentReference = parentProperties.getReference( p.getName() );

                if ( p.isOverride() && parentProperty == null && parentReference == null )
                {
                    report.getDetails().add( this.createDetail(
                        "IMPLEMENTATION_PROPERTY_OVERRIDE_CONSTRAINT", Level.SEVERE,
                        "implementationPropertyOverrideConstraint", new Object[]
                        {
                            implementation.getIdentifier(), p.getName(),
                        }, new ObjectFactory().createImplementation( implementation ) ) );

                }
                if ( ( parentProperty != null && parentProperty.isFinal() ) ||
                     ( parentReference != null && parentReference.isFinal() ) )
                {
                    report.getDetails().add( this.createDetail(
                        "IMPLEMENTATION_PROPERTY_INHERITANCE_CONSTRAINT", Level.SEVERE,
                        "implementationPropertyFinalConstraint", new Object[]
                        {
                            implementation.getIdentifier(), p.getName(),
                        }, new ObjectFactory().createImplementation( implementation ) ) );

                }
            }
            for ( PropertyReference r : implementation.getProperties().getReference() )
            {
                final Property parentProperty = parentProperties.getProperty( r.getName() );
                final PropertyReference parentReference = parentProperties.getReference( r.getName() );

                if ( r.isOverride() && parentProperty == null && parentReference == null )
                {
                    report.getDetails().add( this.createDetail(
                        "IMPLEMENTATION_PROPERTY_OVERRIDE_CONSTRAINT", Level.SEVERE,
                        "implementationPropertyOverrideConstraint", new Object[]
                        {
                            implementation.getIdentifier(), r.getName(),
                        }, new ObjectFactory().createImplementation( implementation ) ) );

                }
                if ( ( parentProperty != null && parentProperty.isFinal() ) ||
                     ( parentReference != null && parentReference.isFinal() ) )
                {
                    report.getDetails().add( this.createDetail(
                        "IMPLEMENTATION_PROPERTY_INHERITANCE_CONSTRAINT", Level.SEVERE,
                        "implementationPropertyFinalConstraint", new Object[]
                        {
                            implementation.getIdentifier(), r.getName(),
                        }, new ObjectFactory().createImplementation( implementation ) ) );

                }
            }
        }
    }

    private void assertImplementationMessagesUniqueness(
        final Implementation implementation, final ModelObjectValidationReport report )
    {
        if ( implementation.getMessages() != null )
        {
            for ( Message m : implementation.getMessages().getMessage() )
            {
                if ( implementation.getMessages().getReference( m.getName() ) != null )
                {
                    report.getDetails().add( this.createDetail(
                        "IMPLEMENTATION_MESSAGES_UNIQUENESS_CONSTRAINT", Level.SEVERE,
                        "implementationMessagesUniquenessConstraint", new Object[]
                        {
                            implementation.getIdentifier(), m.getName(),
                        }, new ObjectFactory().createImplementation( implementation ) ) );

                }
            }
        }
    }

    private void assertImplementationPropertiesUniqueness(
        final Implementation implementation, final ModelObjectValidationReport report )
    {
        if ( implementation.getProperties() != null )
        {
            for ( Property p : implementation.getProperties().getProperty() )
            {
                if ( implementation.getProperties().getReference( p.getName() ) != null )
                {
                    report.getDetails().add( this.createDetail(
                        "IMPLEMENTATION_PROPERTIES_UNIQUENESS_CONSTRAINT", Level.SEVERE,
                        "implementationPropertiesUniquenessConstraint", new Object[]
                        {
                            implementation.getIdentifier(), p.getName(),
                        }, new ObjectFactory().createImplementation( implementation ) ) );

                }
            }
        }
    }

    private void assertNoInheritanceClashes( final Modules modules,
                                             final Implementation implementation,
                                             final ModelObjectValidationReport report )
    {
        if ( implementation.getImplementations() != null )
        {
            final Map<String, List<Dependency>> dependencyMap = new HashMap<String, List<Dependency>>();
            final Map<String, List<Message>> messageMap = new HashMap<String, List<Message>>();
            final Map<String, List<Property>> propertyMap = new HashMap<String, List<Property>>();
            final Map<String, List<SpecificationReference>> specMap =
                new HashMap<String, List<SpecificationReference>>();

            for ( ImplementationReference r : implementation.getImplementations().getReference() )
            {
                final Specifications currentSpecs = new Specifications();
                final Dependencies currentDependencies = new Dependencies();
                final Properties currentProperties = new Properties();
                final Messages currentMessages = new Messages();
                final Implementation current = modules.getImplementation( r.getIdentifier() );

                modules.collectSpecifications( current, currentSpecs, new Implementations(), true );
                modules.collectDependencies( current, currentDependencies, new Implementations(), true );
                modules.collectMessages( current, currentMessages, new Implementations(), true );
                modules.collectProperties( current, currentProperties, new Implementations(), true );

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
            }

            for ( Map.Entry<String, List<SpecificationReference>> e : specMap.entrySet() )
            {
                if ( e.getValue().size() > 1 &&
                     ( implementation.getSpecifications() == null ||
                       implementation.getSpecifications().getReference( e.getKey() ) == null ) )
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
                if ( e.getValue().size() > 1 &&
                     ( implementation.getDependencies() == null ||
                       implementation.getDependencies().getDependency( e.getKey() ) == null ) )
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
                if ( e.getValue().size() > 1 &&
                     ( implementation.getMessages() == null ||
                       ( implementation.getMessages().getMessage( e.getKey() ) == null &&
                         implementation.getMessages().getReference( e.getKey() ) == null ) ) )
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
                if ( e.getValue().size() > 1 &&
                     ( implementation.getProperties() == null ||
                       ( implementation.getProperties().getProperty( e.getKey() ) == null &&
                         implementation.getProperties().getReference( e.getKey() ) == null ) ) )
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

    private void assertSpecificationImplementationNameUniqueness( final Modules modules,
                                                                  final Specification specification,
                                                                  final ModelObjectValidationReport report )
    {
        final Implementations impls = modules.getImplementations( specification.getIdentifier() );

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
                                i.getIdentifier(), specification.getIdentifier(), i.getName()
                            }, new ObjectFactory().createImplementation( i ) ) );

                    }
                }
            }
        }
    }

    private void assertSpecificationMultiplicityConstraint( final Modules modules, final Specification specification,
                                                            final ModelObjectValidationReport report )
    {
        final Implementations impls = modules.getImplementations( specification.getIdentifier() );

        if ( specification.getMultiplicity() == Multiplicity.ONE &&
             impls != null && impls.getImplementation().size() > 1 )
        {
            for ( Implementation i : impls.getImplementation() )
            {
                report.getDetails().add( this.createDetail(
                    "SPECIFICATION_IMPLEMENTATION_MULTIPLICITY_CONSTRAINT", Level.SEVERE,
                    "specificationMultiplicityConstraint", new Object[]
                    {
                        i.getIdentifier(), specification.getIdentifier(), specification.getMultiplicity()
                    }, new ObjectFactory().createImplementation( i ) ) );

            }
        }
    }

    private void assertNoSpecificationPropertyReferenceDeclarations( final Specification specification,
                                                                     final ModelObjectValidationReport report )
    {
        if ( specification.getProperties() != null )
        {
            for ( PropertyReference r : specification.getProperties().getReference() )
            {
                report.getDetails().add( this.createDetail(
                    "SPECIFICATION_PROPERTY_REFERENCE_DECLARATION_CONSTRAINT", Level.SEVERE,
                    "specificationPropertyReferenceDeclarationConstraint", new Object[]
                    {
                        specification.getIdentifier(), r.getName()
                    }, new ObjectFactory().createSpecification( specification ) ) );

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

            if ( includeImplementation &&
                 implementations.getImplementation( implementation.getIdentifier() ) == null )
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

    private ModelObjectValidationReport.Detail createDetail( final String identifier, final Level level,
                                                             final String messageKey, final Object messageArguments,
                                                             final JAXBElement<? extends ModelObject> element )
    {
        final ModelObjectValidationReport.Detail detail = new ModelObjectValidationReport.Detail(
            identifier, level, this.getMessage( messageKey, messageArguments ) );

        detail.setElement( element );
        return detail;
    }

    private String getMessage( final String key, final Object args )
    {
        return new MessageFormat( ResourceBundle.getBundle(
            DefaultModelObjectValidator.class.getName().replace( '.', '/' ),
            Locale.getDefault() ).getString( key ) ).format( args );

    }

}

/**
 * {@code ValidationEventHandler} collecting {@code ModelObjectValidationReport} details.
 *
 * @author <a href="mailto:cs@jomc.org">Christian Schulte</a>
 * @version $Id$
 */
class ModelObjectValidationEventHandler implements ValidationEventHandler
{

    /** The report events are collected with. */
    private final ModelObjectValidationReport report;

    /**
     * Creates a new {@code ModelObjectValidationEventHandler} taking a {@code ModelObjectValidationReport} instance to
     * collect events with.
     *
     * @param report The report to use for collecting events.
     */
    ModelObjectValidationEventHandler( final ModelObjectValidationReport report )
    {
        this.report = report;
    }

    public boolean handleEvent( final ValidationEvent event )
    {
        if ( event == null )
        {
            throw new IllegalArgumentException( "event" );
        }

        switch ( event.getSeverity() )
        {
            case ValidationEvent.WARNING:
                this.report.getDetails().add( new ModelObjectValidationReport.Detail(
                    "W3C XML 1.0 Recommendation - Warning condition", Level.WARNING, event.getMessage() ) );

                return true;

            case ValidationEvent.ERROR:
                this.report.getDetails().add( new ModelObjectValidationReport.Detail(
                    "W3C XML 1.0 Recommendation - Section 1.2 - Error", Level.SEVERE, event.getMessage() ) );

                return false;

            case ValidationEvent.FATAL_ERROR:
                this.report.getDetails().add( new ModelObjectValidationReport.Detail(
                    "W3C XML 1.0 Recommendation - Section 1.2 - Fatal Error", Level.SEVERE, event.getMessage() ) );

                return false;

            default:
                throw new AssertionError( event.getSeverity() );

        }
    }

}