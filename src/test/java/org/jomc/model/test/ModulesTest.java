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
package org.jomc.model.test;

import java.util.Optional;
import java.util.logging.Level;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.util.JAXBSource;
import org.jomc.model.Dependencies;
import org.jomc.model.Dependency;
import org.jomc.model.Implementation;
import org.jomc.model.Implementations;
import org.jomc.model.Instance;
import org.jomc.model.Message;
import org.jomc.model.Messages;
import org.jomc.model.ModelObject;
import org.jomc.model.Modules;
import org.jomc.model.Properties;
import org.jomc.model.Property;
import org.jomc.model.Specification;
import org.jomc.model.SpecificationReference;
import org.jomc.model.Specifications;
import org.jomc.model.Text;
import org.jomc.model.Texts;
import org.jomc.model.modlet.ModelHelper;
import org.jomc.modlet.Model;
import org.jomc.modlet.ModelContext;
import org.jomc.modlet.ModelContextFactory;
import org.jomc.modlet.ModelException;
import org.jomc.modlet.ModelValidationReport;
import org.junit.Assert;

/**
 * Test cases for class {@code org.jomc.model.Modules}.
 *
 * @author <a href="mailto:cs@schulte.it">Christian Schulte</a> 1.0
 * @version $JOMC$
 */
public class ModulesTest
{

    /**
     * Constant to prefix relative resource names with.
     */
    private static final String ABSOLUTE_RESOURCE_NAME_PREFIX = "/org/jomc/model/test/";

    /**
     * The {@code TestSuite} holding the module tests to run.
     */
    private TestSuite testSuite;

    /**
     * The {@code ModelContext} instance tests are performed with.
     */
    private ModelContext modelContext;

    /**
     * Creates a new {@code ModulesTest} instance.
     */
    public ModulesTest()
    {
        super();
    }

    /**
     * Gets the {@code TestSuite} holding the module tests to run.
     *
     * @return The {@code TestSuite} holding the module tests to run.
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
     * Creates a new {@code TestSuite} holding module tests to run.
     *
     * @return A new {@code TestSuite} holding module tests to run.
     *
     * @see #getTestSuite()
     */
    protected TestSuite newTestSuite()
    {
        try
        {
            return ( (JAXBElement<TestSuite>) this.getModelContext().createUnmarshaller(
                    ModelObject.MODEL_PUBLIC_ID ).unmarshal( this.getClass().getResource(
                    ABSOLUTE_RESOURCE_NAME_PREFIX + "ModulesTestSuite.xml" ) ) ).getValue();

        }
        catch ( final JAXBException | ModelException e )
        {
            throw new AssertionError( e );
        }
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
     * Creates a new {@code ModelContext} instance tests are performed with.
     *
     * @return A new {@code ModelContext} instance tests are performed with.
     *
     * @see #getModelContext()
     */
    protected ModelContext newModelContext()
    {
        return ModelContextFactory.newInstance().newModelContext();
    }

    /**
     * Runs a {@code ImplementationTestType} test.
     *
     * @param identifier The identifier of the {@code ImplementationTestType} to run.
     *
     * @throws Exception if running the test fails.
     */
    public final void testImplementation( final String identifier ) throws Exception
    {
        Assert.assertNotNull( "identifier", identifier );

        ImplementationTestType test = null;

        for ( final ImplementationTestType candidate : this.getTestSuite().getImplementationTest() )
        {
            if ( identifier.equals( candidate.getIdentifier() ) )
            {
                test = candidate;
                break;
            }
        }

        Assert.assertNotNull( "Implementation test '" + identifier + "' not found.", test );

        final JAXBContext jaxbContext = this.getModelContext().createContext( ModelObject.MODEL_PUBLIC_ID );

        System.out.println( "ImplementationTest: " + test.getIdentifier() );

        final JAXBElement<Modules> modules = (JAXBElement<Modules>) test.getModules().getAny();
        final Model model = new Model();
        model.setIdentifier( ModelObject.MODEL_PUBLIC_ID );
        ModelHelper.setModules( model, modules.getValue() );

        final ModelValidationReport modulesReport = this.getModelContext().validateModel( model );

        if ( !modulesReport.isModelValid() )
        {
            log( modulesReport );
        }

        Assert.assertTrue( "[" + test.getIdentifier() + "] Unexpected invalid modules.",
                           modulesReport.isModelValid() );

        final JAXBElement<Implementation> expected =
            (JAXBElement<Implementation>) test.getImplementation().getAny();

        final ModelValidationReport implementationReport = this.getModelContext().validateModel(
            ModelObject.MODEL_PUBLIC_ID, new JAXBSource( jaxbContext, expected ) );

        if ( !implementationReport.isModelValid() )
        {
            log( implementationReport );
        }

        Assert.assertTrue( "[" + test.getIdentifier() + "] Unexpected invalid implementation.",
                           implementationReport.isModelValid() );

        final Optional<Implementation> i = modules.getValue().getImplementation( expected.getValue().getIdentifier() );

        Assert.assertNotNull( i );
        Assert.assertTrue( i.isPresent() );
        assertEquals( expected.getValue(), i.get() );

        final Optional<Dependencies> computedDependencies =
            modules.getValue().getDependencies( expected.getValue().getIdentifier() );

        Assert.assertNotNull( computedDependencies );
        Assert.assertTrue( computedDependencies.isPresent() );
        assertEquals( expected.getValue().getDependencies(), computedDependencies.get() );

        final Optional<Messages> computedMessages =
            modules.getValue().getMessages( expected.getValue().getIdentifier() );

        Assert.assertNotNull( computedMessages );
        Assert.assertTrue( computedMessages.isPresent() );
        assertEquals( expected.getValue().getMessages(), computedMessages.get() );

        final Optional<Properties> computedProperties =
            modules.getValue().getProperties( expected.getValue().getIdentifier() );

        Assert.assertNotNull( computedProperties );
        Assert.assertTrue( computedProperties.isPresent() );
        assertEquals( expected.getValue().getProperties(), computedProperties.get() );

        final Optional<Specifications> computedSpecifications =
            modules.getValue().getSpecifications( expected.getValue().getIdentifier() );

        Assert.assertNotNull( computedSpecifications );
        Assert.assertTrue( computedSpecifications.isPresent() );
        assertEquals( expected.getValue().getSpecifications(), computedSpecifications.get() );
    }

    /**
     * Runs a {@code InstanceTestType} test.
     *
     * @param identifier The identifier of the {@code InstanceTestType} to run.
     *
     * @throws Exception if running the test fails.
     */
    public final void testInstance( final String identifier ) throws Exception
    {
        Assert.assertNotNull( "identifier", identifier );

        InstanceTestType test = null;

        for ( final InstanceTestType candidate : this.getTestSuite().getInstanceTest() )
        {
            if ( identifier.equals( candidate.getIdentifier() ) )
            {
                test = candidate;
                break;
            }
        }

        Assert.assertNotNull( "Instance test '" + identifier + "' not found.", test );

        System.out.println( "InstanceTest: " + test.getIdentifier() );

        final JAXBElement<Modules> modules = (JAXBElement<Modules>) test.getModules().getAny();
        final Model model = new Model();
        model.setIdentifier( ModelObject.MODEL_PUBLIC_ID );
        ModelHelper.setModules( model, modules.getValue() );

        ModelValidationReport validationReport = this.getModelContext().validateModel( model );

        if ( !validationReport.isModelValid() )
        {
            log( validationReport );
        }

        Assert.assertTrue( "[" + test.getIdentifier() + "] Unexpected invalid modules.",
                           validationReport.isModelValid() );

        final JAXBElement<Instance> expected = (JAXBElement<Instance>) test.getInstance().getAny();
        validationReport = this.getModelContext().validateModel(
            ModelObject.MODEL_PUBLIC_ID,
            new JAXBSource( this.getModelContext().createContext( ModelObject.MODEL_PUBLIC_ID ), expected ) );

        if ( !validationReport.isModelValid() )
        {
            log( validationReport );
        }

        Assert.assertTrue( "[" + test.getIdentifier() + "] Unexpected invalid instance.",
                           validationReport.isModelValid() );

        Optional<Instance> instance = null;

        if ( test.getDependencyName() != null )
        {
            final Optional<Dependencies> dependencies =
                modules.getValue().getDependencies( test.getImplementationIdentifier() );

            Assert.assertNotNull( "[" + test.getIdentifier() + "] No dependencies for implementation '"
                                      + test.getImplementationIdentifier() + "' not found.", dependencies );

            Assert.assertTrue( "[" + test.getIdentifier() + "] No dependencies for implementation '"
                                   + test.getImplementationIdentifier() + "' not found.", dependencies.isPresent() );

            final Optional<Dependency> d = dependencies.get().getDependency( test.getDependencyName() );

            Assert.assertNotNull( "[" + test.getIdentifier() + "] Dependency '" + test.getDependencyName()
                                      + "' not found.", d );

            Assert.assertTrue( "[" + test.getIdentifier() + "] Dependency '" + test.getDependencyName()
                                   + "' not found.", d.isPresent() );

            Assert.assertNotNull( "[" + test.getIdentifier() + "] Expected implementation name of dependency '"
                                      + test.getDependencyName() + "' not set.", d.get().getImplementationName() );

            final Optional<Implementations> implementations =
                modules.getValue().getImplementations( d.get().getIdentifier() );

            Assert.assertNotNull( "[" + test.getIdentifier() + "] Expected implementations of dependency '"
                                      + test.getDependencyName() + "' not found.", implementations );

            Assert.assertTrue( "[" + test.getIdentifier() + "] Expected implementations of dependency '"
                                   + test.getDependencyName() + "' not found.", implementations.isPresent() );

            final Optional<Implementation> i =
                implementations.get().getImplementationByName( d.get().getImplementationName() );

            Assert.assertNotNull( "[" + test.getIdentifier() + "] Expected '" + d.get().getImplementationName()
                                      + "' implementation not found.", i );

            Assert.assertTrue( "[" + test.getIdentifier() + "] Expected '" + d.get().getImplementationName()
                                   + "' implementation not found.", i.isPresent() );

            instance = modules.getValue().getInstance( i.get().getIdentifier(), d.get() );
        }
        else
        {
            instance = modules.getValue().getInstance( test.getImplementationIdentifier() );
        }

        Assert.assertNotNull( "[" + test.getIdentifier() + "] Expected instance not found.", instance );
        Assert.assertTrue( "[" + test.getIdentifier() + "] Expected instance not found.", instance.isPresent() );
        assertEquals( expected.getValue(), instance.get() );
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

    public static void assertEquals( final Instance expected, final Instance computed ) throws Exception
    {
        if ( expected != null )
        {
            Assert.assertNotNull( computed );
            assertEquals( (ModelObject) expected, (ModelObject) computed );
            Assert.assertEquals( expected.getClazz(), computed.getClazz() );
            assertEquals( expected.getDependencies(), computed.getDependencies() );
            Assert.assertEquals( expected.getIdentifier(), computed.getIdentifier() );
            assertEquals( expected.getMessages(), computed.getMessages() );
            Assert.assertEquals( expected.getName(), computed.getName() );
            assertEquals( expected.getProperties(), computed.getProperties() );
            assertEquals( expected.getSpecifications(), computed.getSpecifications() );
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

            for ( final Implementation i : expected.getImplementation() )
            {
                final Optional<Implementation> computedImpl = computed.getImplementation( i.getIdentifier() );
                Assert.assertTrue( computedImpl.isPresent() );
                assertEquals( i, computedImpl.get() );
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

            for ( final Specification s : expected.getSpecification() )
            {
                final Optional<Specification> computedSpec = computed.getSpecification( s.getIdentifier() );
                Assert.assertTrue( computedSpec.isPresent() );
                assertEquals( s, computedSpec.get() );
            }

            for ( final SpecificationReference r : expected.getReference() )
            {
                final Optional<SpecificationReference> computedRef = computed.getReference( r.getIdentifier() );
                Assert.assertTrue( computedRef.isPresent() );
                assertEquals( r, computedRef.get() );
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

    public static void assertEquals( final Specification expected, final Specification computed ) throws Exception
    {
        if ( expected != null )
        {
            Assert.assertNotNull( computed );
            assertEquals( (ModelObject) expected, (ModelObject) computed );

            Assert.assertEquals( expected.getClazz(), computed.getClazz() );
            Assert.assertEquals( expected.getIdentifier(), computed.getIdentifier() );
            Assert.assertEquals( expected.getMultiplicity(), computed.getMultiplicity() );
            assertEquals( expected.getProperties(), computed.getProperties() );
            Assert.assertEquals( expected.getScope(), computed.getScope() );
            Assert.assertEquals( expected.getVendor(), computed.getVendor() );
            Assert.assertEquals( expected.getVersion(), computed.getVersion() );
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

            for ( final Dependency d : expected.getDependency() )
            {
                final Optional<Dependency> computedDep = computed.getDependency( d.getName() );
                Assert.assertTrue( computedDep.isPresent() );
                assertEquals( d, computedDep.get() );
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
            assertEquals( expected.getDependencies(), computed.getDependencies() );
            assertEquals( expected.getMessages(), computed.getMessages() );
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

            for ( final Message m : expected.getMessage() )
            {
                final Optional<Message> computedMsg = computed.getMessage( m.getName() );
                Assert.assertTrue( computedMsg.isPresent() );
                assertEquals( m, computedMsg.get() );
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
            assertEquals( expected.getTemplate(), computed.getTemplate() );
        }
        else
        {
            Assert.assertNull( computed );
        }
    }

    public static void assertEquals( final Texts expected, final Texts computed ) throws Exception
    {
        if ( expected != null )
        {
            Assert.assertNotNull( computed );
            Assert.assertEquals( expected.getDefaultLanguage(), computed.getDefaultLanguage() );

            for ( final Text t : expected.getText() )
            {
                Assert.assertNotNull( computed.getText( t.getLanguage() ) );
                Assert.assertEquals( t.getValue(), computed.getText( t.getLanguage() ).getValue() );
            }

            for ( final Text t : computed.getText() )
            {
                Assert.assertNotNull( expected.getText( t.getLanguage() ) );
            }
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

            for ( final Property p : expected.getProperty() )
            {
                final Optional<Property> computedProperty = computed.getProperty( p.getName() );
                Assert.assertTrue( computedProperty.isPresent() );
                assertEquals( p, computedProperty.get() );
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
            Assert.assertEquals( expected.getJavaValue( ModulesTest.class.getClassLoader() ),
                                 computed.getJavaValue( ModulesTest.class.getClassLoader() ) );

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

    private static void log( final ModelValidationReport report )
    {
        for ( final ModelValidationReport.Detail d : report.getDetails() )
        {
            System.out.println( "\t" + d.toString() );
        }
    }

}
