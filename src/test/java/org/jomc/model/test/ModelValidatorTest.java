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
package org.jomc.model.test;

import java.util.List;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.util.JAXBSource;
import javax.xml.validation.Schema;
import javax.xml.validation.Validator;
import junit.framework.Assert;
import org.jomc.model.DefaultModelValidator;
import org.jomc.model.Dependencies;
import org.jomc.model.Dependency;
import org.jomc.model.Implementation;
import org.jomc.model.Implementations;
import org.jomc.model.Message;
import org.jomc.model.Messages;
import org.jomc.model.ModelContext;
import org.jomc.model.ModelException;
import org.jomc.model.ModelObject;
import org.jomc.model.ModelValidationReport;
import org.jomc.model.ModelValidator;
import org.jomc.model.Module;
import org.jomc.model.Modules;
import org.jomc.model.Properties;
import org.jomc.model.Property;
import org.jomc.model.SpecificationReference;
import org.jomc.model.Specifications;
import org.xml.sax.SAXException;

/**
 * Test cases for class {@code org.jomc.model.ModelObjectValidator} implementations.
 *
 * @author <a href="mailto:cs@jomc.org">Christian Schulte</a> 1.0
 * @version $Id$
 */
public class ModelValidatorTest
{

    private ModelValidator modelValidator;

    private TestSuite testSuite;

    public ModelValidatorTest()
    {
        this( null, null );
    }

    public ModelValidatorTest( final ModelValidator modelValidator, final TestSuite testSuite )
    {
        super();
        this.modelValidator = modelValidator;
        this.testSuite = testSuite;
    }

    public ModelValidator getModelValidator() throws ModelException
    {
        if ( this.modelValidator == null )
        {
            this.modelValidator = new DefaultModelValidator();
        }

        return this.modelValidator;
    }

    public void setModelValidator( final ModelValidator value )
    {
        this.modelValidator = value;
    }

    public TestSuite getTestSuite() throws ModelException
    {
        try
        {
            if ( this.testSuite == null )
            {
                final ModelContext context = ModelContext.createModelContext( this.getClass().getClassLoader() );
                final JAXBElement<TestSuite> e = (JAXBElement<TestSuite>) context.createUnmarshaller().unmarshal(
                    this.getClass().getResource( "testsuite.xml" ) );

                this.testSuite = e.getValue();
            }

            return this.testSuite;
        }
        catch ( final JAXBException e )
        {
            throw new ModelException( e );
        }
    }

    public void setTestSuite( final TestSuite value )
    {
        this.testSuite = value;
    }

    public void testIllegalArguments() throws Exception
    {
        try
        {
            this.getModelValidator().validateModel(
                ModelContext.createModelContext( this.getClass().getClassLoader() ), null );

            Assert.fail( "Expected NullPointerException not thrown." );
        }
        catch ( final NullPointerException e )
        {
            Assert.assertNotNull( e.getMessage() );
            System.out.println( e.toString() );
        }

        try
        {
            this.getModelValidator().validateModel( null, new Modules() );
            Assert.fail( "Expected NullPointerException not thrown." );
        }
        catch ( final NullPointerException e )
        {
            Assert.assertNotNull( e.getMessage() );
            System.out.println( e.toString() );
        }
    }

    public void testLegalArguments() throws Exception
    {
        final ModelContext context = ModelContext.createModelContext( this.getClass().getClassLoader() );
        Assert.assertNotNull( this.getModelValidator().validateModel( context, new Modules() ) );
    }

    public void testSchemaConstraints() throws Exception
    {
        final ModelContext context = ModelContext.createModelContext( this.getClass().getClassLoader() );
        final JAXBContext jaxbContext = context.createContext();
        final Schema schema = context.createSchema();
        final Validator validator = schema.newValidator();

        for ( SchemaConstraintsTest test : this.getTestSuite().getSchemaConstraintsTest() )
        {
            System.out.println( "SchemaConstraintsTest: " + test.getIdentifier() );

            final JAXBElement<? extends ModelObject> modelObject =
                (JAXBElement<? extends ModelObject>) test.getModelObject().getAny();

            final JAXBSource source = new JAXBSource( jaxbContext, modelObject );
            final ModelValidationReport report = context.validateModel( source );

            log( report );

            Assert.assertEquals( "[" + test.getIdentifier() + "]",
                                 test.getModelObject().isValid(), report.isModelValid() );

        }
    }

    public void testModulesConstraints() throws Exception
    {
        final ModelContext context = ModelContext.createModelContext( this.getClass().getClassLoader() );

        for ( ModulesConstraintsTest test : this.getTestSuite().getModulesConstraintsTest() )
        {
            System.out.println( "ModulesConstraintsTest: " + test.getIdentifier() );

            final JAXBElement<Modules> modules = (JAXBElement<Modules>) test.getModules().getAny();
            final ModelValidationReport report = this.getModelValidator().validateModel( context, modules.getValue() );

            log( report );

            if ( test.getModules().isValid() )
            {
                if ( !report.isModelValid() )
                {
                    Assert.fail( "[" + test.getIdentifier() + "] Unexpected invalid model object." );
                }
            }
            else
            {
                if ( report.isModelValid() )
                {
                    Assert.fail( "[" + test.getIdentifier() + "] Unexpected valid model object." );
                }

                for ( ModelValidationReportDetail expectedDetail : test.getDetail() )
                {
                    final List<ModelValidationReport.Detail> reportedDetails =
                        report.getDetails( expectedDetail.getIdentifier() );

                    Assert.assertTrue( "[" + test.getIdentifier() + "] Expected " + expectedDetail.getCount() + " " +
                                       expectedDetail.getIdentifier() + " details but got " + reportedDetails.size() +
                                       ".", expectedDetail.getCount() == reportedDetails.size() );

                    report.getDetails().removeAll( reportedDetails );
                }

                if ( !report.getDetails().isEmpty() )
                {
                    for ( ModelValidationReport.Detail d : report.getDetails() )
                    {
                        Assert.fail( "[" + test.getIdentifier() + "] Unexpected " + d.getIdentifier() + " detail." );
                    }
                }
            }
        }
    }

    public void testImplementations() throws Exception
    {
        final ModelContext context = ModelContext.createModelContext( this.getClass().getClassLoader() );
        final JAXBContext jaxbContext = context.createContext();

        for ( ImplementationTest test : this.getTestSuite().getImplementationTest() )
        {
            System.out.println( "ImplementationTest: " + test.getIdentifier() );

            final JAXBElement<Modules> modules = (JAXBElement<Modules>) test.getModules().getAny();
            final ModelValidationReport modulesReport =
                this.getModelValidator().validateModel( context, modules.getValue() );

            Assert.assertTrue( "[" + test.getIdentifier() + "] Unexpected invalid modules.",
                               modulesReport.isModelValid() );

            final JAXBElement<Implementation> expected =
                (JAXBElement<Implementation>) test.getImplementation().getAny();

            final ModelValidationReport implementationReport =
                context.validateModel( new JAXBSource( jaxbContext, expected ) );

            Assert.assertTrue( "[" + test.getIdentifier() + "] Unexpected invalid implementation.",
                               implementationReport.isModelValid() );

            final Implementation i = modules.getValue().getImplementation( expected.getValue().getIdentifier() );

            Assert.assertNotNull( i );
            assertEquals( expected.getValue(), i );
            assertEquals( expected.getValue().getDependencies(),
                          modules.getValue().getDependencies( expected.getValue().getIdentifier() ) );

            assertEquals( expected.getValue().getMessages(),
                          modules.getValue().getMessages( expected.getValue().getIdentifier() ) );

            assertEquals( expected.getValue().getProperties(),
                          modules.getValue().getProperties( expected.getValue().getIdentifier() ) );

            assertEquals( expected.getValue().getSpecifications(),
                          modules.getValue().getSpecifications( expected.getValue().getIdentifier() ) );

        }
    }

    public static void assertEquals( final ModelObject expected, final ModelObject computed ) throws Exception
    {
        if ( expected != null )
        {
            Assert.assertNotNull( computed );
            Assert.assertEquals( expected.getCreateDate(), computed.getCreateDate() );
            Assert.assertEquals( expected.getModelVersion(), computed.getModelVersion() );
            Assert.assertEquals( expected.isDeprecated(), computed.isDeprecated() );
        }
        else
        {
            Assert.assertNull( computed );
        }
    }

    public static void assertEquals( final Implementations expected, final Implementations computed ) throws Exception
    {
        if ( expected != null )
        {
            Assert.assertNotNull( computed );
            assertEquals( (ModelObject) expected, (ModelObject) computed );

            for ( Implementation i : expected.getImplementation() )
            {
                assertEquals( i, computed.getImplementation( i.getIdentifier() ) );
            }
        }
        else
        {
            Assert.assertNull( computed );
        }
    }

    public static void assertEquals( final Implementation expected, final Implementation computed ) throws Exception
    {
        if ( expected != null )
        {
            Assert.assertNotNull( computed );
            assertEquals( (ModelObject) expected, (ModelObject) computed );
            Assert.assertEquals( expected.getClazz(), computed.getClazz() );
            Assert.assertEquals( expected.getIdentifier(), computed.getIdentifier() );
            Assert.assertEquals( expected.getLocation(), computed.getLocation() );
            Assert.assertEquals( expected.getName(), computed.getName() );
            Assert.assertEquals( expected.getVendor(), computed.getVendor() );
            Assert.assertEquals( expected.getVersion(), computed.getVersion() );
            Assert.assertEquals( expected.isAbstract(), computed.isAbstract() );
            Assert.assertEquals( expected.isFinal(), computed.isFinal() );
            Assert.assertEquals( expected.isStateless(), computed.isStateless() );
        }
        else
        {
            Assert.assertNull( computed );
        }
    }

    public static void assertEquals( final Specifications expected, final Specifications computed ) throws Exception
    {
        if ( expected != null )
        {
            Assert.assertNotNull( computed );
            assertEquals( (ModelObject) expected, (ModelObject) computed );

            for ( SpecificationReference r : expected.getReference() )
            {
                assertEquals( r, computed.getReference( r.getIdentifier() ) );
            }
        }
        else
        {
            Assert.assertNull( computed );
        }
    }

    public static void assertEquals( final SpecificationReference expected, final SpecificationReference computed )
        throws Exception
    {
        if ( expected != null )
        {
            Assert.assertNotNull( computed );
            assertEquals( (ModelObject) expected, (ModelObject) computed );

            Assert.assertEquals( expected.getIdentifier(), computed.getIdentifier() );
            Assert.assertEquals( expected.getVersion(), computed.getVersion() );
            Assert.assertEquals( expected.isDeprecated(), computed.isDeprecated() );
            Assert.assertEquals( expected.isFinal(), computed.isFinal() );
            Assert.assertEquals( expected.isOverride(), computed.isOverride() );
        }
        else
        {
            Assert.assertNull( computed );
        }
    }

    public static void assertEquals( final Dependencies expected, final Dependencies computed ) throws Exception
    {
        if ( expected != null )
        {
            Assert.assertNotNull( computed );

            for ( Dependency d : expected.getDependency() )
            {
                assertEquals( d, computed.getDependency( d.getName() ) );
            }
        }
        else
        {
            Assert.assertNull( computed );
        }
    }

    public static void assertEquals( final Dependency expected, final Dependency computed ) throws Exception
    {
        if ( expected != null )
        {
            Assert.assertNotNull( computed );
            Assert.assertEquals( expected.getIdentifier(), computed.getIdentifier() );
            Assert.assertEquals( expected.getImplementationName(), computed.getImplementationName() );
            Assert.assertEquals( expected.getName(), computed.getName() );
            Assert.assertEquals( expected.isDeprecated(), computed.isDeprecated() );
            Assert.assertEquals( expected.isFinal(), computed.isFinal() );
            Assert.assertEquals( expected.isOverride(), computed.isOverride() );
            Assert.assertEquals( expected.isBound(), computed.isBound() );
            Assert.assertEquals( expected.isOptional(), computed.isOptional() );
            Assert.assertEquals( expected.getVersion(), computed.getVersion() );
            assertEquals( expected.getProperties(), computed.getProperties() );
        }
        else
        {
            Assert.assertNull( computed );
        }
    }

    public static void assertEquals( final Messages expected, final Messages computed ) throws Exception
    {
        if ( expected != null )
        {
            Assert.assertNotNull( computed );

            for ( Message m : expected.getMessage() )
            {
                assertEquals( m, computed.getMessage( m.getName() ) );
            }
        }
        else
        {
            Assert.assertNull( computed );
        }
    }

    public static void assertEquals( final Message expected, final Message computed ) throws Exception
    {
        if ( expected != null )
        {
            Assert.assertNotNull( computed );
            Assert.assertEquals( expected.getName(), computed.getName() );
            Assert.assertEquals( expected.isDeprecated(), computed.isDeprecated() );
            Assert.assertEquals( expected.isFinal(), computed.isFinal() );
            Assert.assertEquals( expected.isOverride(), computed.isOverride() );
        }
        else
        {
            Assert.assertNull( computed );
        }
    }

    public static void assertEquals( final Properties expected, final Properties computed ) throws Exception
    {
        if ( expected != null )
        {
            Assert.assertNotNull( computed );

            for ( Property p : expected.getProperty() )
            {
                assertEquals( p, computed.getProperty( p.getName() ) );
            }
        }
        else
        {
            Assert.assertNull( computed );
        }
    }

    public static void assertEquals( final Property expected, final Property computed ) throws Exception
    {
        if ( expected != null )
        {
            Assert.assertNotNull( computed );
            Assert.assertEquals( expected.getJavaValue(), computed.getJavaValue() );
            Assert.assertEquals( expected.getName(), computed.getName() );
            Assert.assertEquals( expected.getType(), computed.getType() );
            Assert.assertEquals( expected.getValue(), computed.getValue() );
            Assert.assertEquals( expected.isDeprecated(), computed.isDeprecated() );
            Assert.assertEquals( expected.isFinal(), computed.isFinal() );
            Assert.assertEquals( expected.isOverride(), computed.isOverride() );
        }
        else
        {
            Assert.assertNull( computed );
        }
    }

    public static void log( final ModelValidationReport report )
    {
        for ( ModelValidationReport.Detail d : report.getDetails() )
        {
            System.out.println( "\t" + d.toString() );
        }
    }

}
