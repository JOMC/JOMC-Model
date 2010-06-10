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
package org.jomc.model.test;

import java.util.List;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.util.JAXBSource;
import junit.framework.Assert;
import org.jomc.model.DefaultModelValidator;
import org.jomc.model.ModelContext;
import org.jomc.model.ModelException;
import org.jomc.model.ModelObject;
import org.jomc.model.ModelValidationReport;
import org.jomc.model.ModelValidator;
import org.jomc.model.Modules;

/**
 * Test cases for class {@code org.jomc.model.ModelValidator} implementations.
 *
 * @author <a href="mailto:schulte2005@users.sourceforge.net">Christian Schulte</a> 1.0
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
            throw new ModelException( e.getMessage(), e );
        }
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

                    Assert.assertTrue( "[" + test.getIdentifier() + "] Expected " + expectedDetail.getCount() + " "
                                       + expectedDetail.getIdentifier() + " details but got " + reportedDetails.size()
                                       + ".", expectedDetail.getCount() == reportedDetails.size() );

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

    public static void log( final ModelValidationReport report )
    {
        for ( ModelValidationReport.Detail d : report.getDetails() )
        {
            System.out.println( "\t" + d.toString() );
        }
    }

}
