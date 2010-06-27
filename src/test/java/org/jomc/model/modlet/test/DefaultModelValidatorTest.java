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
package org.jomc.model.modlet.test;

import java.util.List;
import java.util.logging.Level;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.util.JAXBSource;
import org.jomc.model.ModelObject;
import org.jomc.model.Modules;
import org.jomc.model.PropertyException;
import org.jomc.model.modlet.DefaultModelValidator;
import org.jomc.model.modlet.ModelHelper;
import org.jomc.model.test.ModelValidationReportDetail;
import org.jomc.model.test.ModulesConstraintsTest;
import org.jomc.model.test.SchemaConstraintsTest;
import org.jomc.model.test.TestSuite;
import org.jomc.modlet.Model;
import org.jomc.modlet.ModelContext;
import org.jomc.modlet.ModelException;
import org.jomc.modlet.ModelValidationReport;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

/**
 * Test cases for class {@code org.jomc.model.modlet.DefaultModelValidator} implementations.
 *
 * @author <a href="mailto:schulte2005@users.sourceforge.net">Christian Schulte</a> 1.0
 * @version $Id$
 */
public class DefaultModelValidatorTest
{

    private DefaultModelValidator defaultModelValidator;

    private TestSuite testSuite;

    private ModelContext modelContext;

    public DefaultModelValidatorTest()
    {
        this( null, null );
    }

    public DefaultModelValidatorTest( final DefaultModelValidator defaultModelValidator, final TestSuite testSuite )
    {
        super();
        this.defaultModelValidator = defaultModelValidator;
        this.testSuite = testSuite;
    }

    public DefaultModelValidator getModelValidator() throws PropertyException
    {
        if ( this.defaultModelValidator == null )
        {
            this.defaultModelValidator = new DefaultModelValidator();
        }

        return this.defaultModelValidator;
    }

    public ModelContext getModelContext() throws ModelException
    {
        if ( this.modelContext == null )
        {
            this.modelContext = ModelContext.createModelContext( this.getClass().getClassLoader() );
            this.modelContext.getListeners().add( new ModelContext.Listener()
            {

                @Override
                public void onLog( final Level level, String message, Throwable t )
                {
                    System.out.println( "[" + level.getLocalizedName() + "] " + message );
                    if ( t != null )
                    {
                        t.printStackTrace( System.out );
                    }
                }

            } );
        }

        return this.modelContext;
    }

    public TestSuite getTestSuite() throws ModelException
    {
        try
        {
            if ( this.testSuite == null )
            {
                final JAXBElement<TestSuite> e = (JAXBElement<TestSuite>) this.getModelContext().createUnmarshaller(
                    ModelObject.MODEL_PUBLIC_ID ).unmarshal( this.getClass().getResource( "testsuite.xml" ) );

                this.testSuite = e.getValue();
            }

            return this.testSuite;
        }
        catch ( final JAXBException e )
        {
            String message = e.getMessage();
            if ( message == null && e.getLinkedException() != null )
            {
                message = e.getLinkedException().getMessage();
            }

            throw new ModelException( message, e );
        }
    }

    public void testIllegalArguments() throws Exception
    {
        try
        {
            this.getModelValidator().validateModel( this.getModelContext(), null );
            fail( "Expected NullPointerException not thrown." );
        }
        catch ( final NullPointerException e )
        {
            assertNotNull( e.getMessage() );
            System.out.println( e.toString() );
        }

        try
        {
            this.getModelValidator().validateModel( null, new Model() );
            fail( "Expected NullPointerException not thrown." );
        }
        catch ( final NullPointerException e )
        {
            assertNotNull( e.getMessage() );
            System.out.println( e.toString() );
        }
    }

    public void testLegalArguments() throws Exception
    {
        assertNotNull( this.getModelValidator().validateModel(
            this.getModelContext(), this.getModelContext().findModel( ModelObject.MODEL_PUBLIC_ID ) ) );

    }

    public void testSchemaConstraints() throws Exception
    {
        final ModelContext context = this.getModelContext();
        final JAXBContext jaxbContext = context.createContext( ModelObject.MODEL_PUBLIC_ID );

        for ( SchemaConstraintsTest test : this.getTestSuite().getSchemaConstraintsTest() )
        {
            System.out.println( "SchemaConstraintsTest: " + test.getIdentifier() );

            final JAXBElement<? extends ModelObject> modelObject =
                (JAXBElement<? extends ModelObject>) test.getModelObject().getAny();

            final JAXBSource source = new JAXBSource( jaxbContext, modelObject );
            final ModelValidationReport report = context.validateModel( ModelObject.MODEL_PUBLIC_ID, source );

            log( report );

            assertEquals( "[" + test.getIdentifier() + "]", test.getModelObject().isValid(), report.isModelValid() );
        }
    }

    public void testModulesConstraints() throws Exception
    {
        final ModelContext context = this.getModelContext();

        for ( ModulesConstraintsTest test : this.getTestSuite().getModulesConstraintsTest() )
        {
            System.out.println( "ModulesConstraintsTest: " + test.getIdentifier() );

            final JAXBElement<Modules> modules = (JAXBElement<Modules>) test.getModules().getAny();
            final Model model = new Model();
            model.setIdentifier( ModelObject.MODEL_PUBLIC_ID );
            ModelHelper.setModules( model, modules.getValue() );

            final ModelValidationReport report = this.getModelValidator().validateModel( context, model );

            log( report );

            if ( test.getModules().isValid() )
            {
                if ( !report.isModelValid() )
                {
                    fail( "[" + test.getIdentifier() + "] Unexpected invalid model object." );
                }
            }
            else
            {
                if ( report.isModelValid() )
                {
                    fail( "[" + test.getIdentifier() + "] Unexpected valid model object." );
                }

                for ( ModelValidationReportDetail expectedDetail : test.getDetail() )
                {
                    final List<ModelValidationReport.Detail> reportedDetails =
                        report.getDetails( expectedDetail.getIdentifier() );

                    assertTrue( "[" + test.getIdentifier() + "] Expected " + expectedDetail.getCount() + " "
                                + expectedDetail.getIdentifier() + " details but got " + reportedDetails.size()
                                + ".", expectedDetail.getCount() == reportedDetails.size() );

                    report.getDetails().removeAll( reportedDetails );
                }

                if ( !report.getDetails().isEmpty() )
                {
                    for ( ModelValidationReport.Detail d : report.getDetails() )
                    {
                        fail( "[" + test.getIdentifier() + "] Unexpected " + d.getIdentifier() + " detail." );
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
