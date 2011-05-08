/*
 *   Copyright (C) 2009 The JOMC Project
 *   Copyright (C) 2005 Christian Schulte <schulte2005@users.sourceforge.net>
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

import org.jomc.model.test.ModulesConstraintsTestType;
import org.jomc.model.test.SchemaConstraintsTestType;
import org.junit.Test;
import java.util.List;
import java.util.logging.Level;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.util.JAXBSource;
import org.jomc.model.ModelObject;
import org.jomc.model.Modules;
import org.jomc.model.modlet.DefaultModelValidator;
import org.jomc.model.modlet.ModelHelper;
import org.jomc.model.test.ModelValidationReportDetail;
import org.jomc.model.test.TestSuite;
import org.jomc.modlet.Model;
import org.jomc.modlet.ModelContext;
import org.jomc.modlet.ModelException;
import org.jomc.modlet.ModelValidationReport;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Test cases for class {@code org.jomc.model.modlet.DefaultModelValidator} implementations.
 *
 * @author <a href="mailto:schulte2005@users.sourceforge.net">Christian Schulte</a> 1.0
 * @version $Id$
 */
public class DefaultModelValidatorTest
{

    /** Constant to prefix relative resource names with. */
    private static final String ABSOLUTE_RESOURCE_NAME_PREFIX = "/org/jomc/model/modlet/test/";

    /** The {@code DefaultModelValidator} instance tests are performed with. */
    private DefaultModelValidator defaultModelValidator;

    /** The {@code TestSuite} holding module tests to run. */
    private TestSuite testSuite;

    /** The {@code ModelContext} tests are performed with. */
    private ModelContext modelContext;

    /** Creates a new {@code DefaultModelValidatorTest} instance. */
    public DefaultModelValidatorTest()
    {
        super();
    }

    /**
     * Gets the {@code DefaultModelValidator} instance tests are performed with.
     *
     * @return The {@code DefaultModelValidator} instance tests are performed with.
     *
     * @see #newModelValidator()
     */
    public DefaultModelValidator getModelValidator()
    {
        if ( this.defaultModelValidator == null )
        {
            this.defaultModelValidator = this.newModelValidator();
        }

        return this.defaultModelValidator;
    }

    /**
     * Create a new {@code DefaultModelValidator} instance to test.
     *
     * @return A new {@code DefaultModelValidator} instance to test.
     *
     * @see #getModelValidator()
     */
    protected DefaultModelValidator newModelValidator()
    {
        return new DefaultModelValidator();
    }

    /**
     * Gets the {@code ModelContext} instance tests are performed with.
     *
     * @return The {@code ModelContext} instance tests are performed with.
     *
     * @see #newModelContext()
     */
    public ModelContext getModelContext()
    {
        if ( this.modelContext == null )
        {
            this.modelContext = this.newModelContext();
            this.modelContext.getListeners().add( new ModelContext.Listener()
            {

                @Override
                public void onLog( final Level level, String message, Throwable t )
                {
                    super.onLog( level, message, t );
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

    /**
     * Creates a new {@code ModelContext} instance to perform tests with.
     *
     * @return A new {@code ModelContext} instance to perform tests with.
     */
    protected ModelContext newModelContext()
    {
        try
        {
            return ModelContext.createModelContext( this.getClass().getClassLoader() );
        }
        catch ( final ModelException e )
        {
            throw new AssertionError( e );
        }
    }

    /**
     * Gets the {@code TestSuite} instance holding module tests to run.
     *
     * @return The {@code TestSuite} instance holding module tests to run.
     *
     * @see #newTestSuite()
     */
    public TestSuite getTestSuite()
    {
        if ( this.testSuite == null )
        {
            this.testSuite = this.newTestSuite();
        }

        return this.testSuite;
    }

    /**
     * Creates a new {@code TestSuite} instance holding module tests to run.
     *
     * @return A new {@code TestSuite} instance holding module tests to run.
     */
    protected TestSuite newTestSuite()
    {
        try
        {
            return ( (JAXBElement<TestSuite>) this.getModelContext().createUnmarshaller(
                    ModelObject.MODEL_PUBLIC_ID ).unmarshal( this.getClass().getResource(
                    ABSOLUTE_RESOURCE_NAME_PREFIX + "testsuite.xml" ) ) ).getValue();

        }
        catch ( final JAXBException e )
        {
            throw new AssertionError( e );
        }
        catch ( final ModelException e )
        {
            throw new AssertionError( e );
        }
    }

    @Test
    public final void testIllegalArguments() throws Exception
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

    @Test
    public final void testLegalArguments() throws Exception
    {
        assertNotNull( this.getModelValidator().validateModel(
            this.getModelContext(), this.getModelContext().findModel( ModelObject.MODEL_PUBLIC_ID ) ) );

    }

    @Test
    public final void testSchemaConstraints() throws Exception
    {
        final ModelContext context = this.getModelContext();
        final JAXBContext jaxbContext = context.createContext( ModelObject.MODEL_PUBLIC_ID );

        for ( SchemaConstraintsTestType test : this.getTestSuite().getSchemaConstraintsTest() )
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

    @Test
    public final void testModulesConstraints() throws Exception
    {
        final ModelContext context = this.getModelContext();

        for ( ModulesConstraintsTestType test : this.getTestSuite().getModulesConstraintsTest() )
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

    private static void log( final ModelValidationReport report )
    {
        for ( ModelValidationReport.Detail d : report.getDetails() )
        {
            System.out.println( "\t" + d.toString() );
        }
    }

}
