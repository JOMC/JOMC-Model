// SECTION-START[License Header]
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
// SECTION-END
package org.jomc.model.test;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import junit.framework.Assert;
import junit.framework.TestCase;
import org.jomc.model.DefaultModelManager;
import org.jomc.model.Dependencies;
import org.jomc.model.Dependency;
import org.jomc.model.Implementation;
import org.jomc.model.Implementations;
import org.jomc.model.Message;
import org.jomc.model.Messages;
import org.jomc.model.ModelException;
import org.jomc.model.ModelManager;
import org.jomc.model.ModelObject;
import org.jomc.model.Modules;
import org.jomc.model.Properties;
import org.jomc.model.Property;
import org.jomc.model.Specification;
import org.jomc.model.SpecificationReference;
import org.jomc.model.Specifications;
import org.xml.sax.SAXException;

/**
 * Testcases for {@code org.jomc.model.ModelManager} implementations.
 *
 * @author <a href="mailto:cs@jomc.org">Christian Schulte</a> 1.0
 * @version $Id$
 */
public class ModelManagerTest extends TestCase
{

    /** The instance tests are performed with. */
    private ModelManager modelManager;

    /** The test suite to run. */
    private TestSuite testSuite;

    /**
     * Gets the {@code ModelManager} instance to test.
     *
     * @return The {@code ModelManager} instance to test.
     */
    public ModelManager getModelManager()
    {
        if ( this.modelManager == null )
        {
            final DefaultModelManager defaultModelManager = new DefaultModelManager();
            defaultModelManager.getListeners().add( new DefaultModelManager.Listener()
            {

                public void onLog( final Level level, final String message, final Throwable t )
                {
                    if ( message != null )
                    {
                        System.out.println( "[" + level.getLocalizedName() + "] " + message );
                    }
                    if ( t != null )
                    {
                        t.printStackTrace();
                    }
                }

            } );

            this.modelManager = defaultModelManager;
        }

        return this.modelManager;
    }

    /**
     * Sets the {@code ModelManager} instance to test.
     *
     * @param value The new {@code ModelManager} instance to test.
     */
    public void setModelManager( final ModelManager value )
    {
        this.modelManager = value;
    }

    /**
     * Gets the test suite of the instance.
     *
     * @return The test suite of the instance.
     */
    public TestSuite getTestSuite() throws IOException, SAXException, JAXBException
    {
        if ( this.testSuite == null )
        {
            final Unmarshaller u = this.getModelManager().getUnmarshaller( false );
            final JAXBElement<TestSuite> e =
                (JAXBElement<TestSuite>) u.unmarshal( this.getClass().getResource( "testsuite.xml" ) );

            this.testSuite = e.getValue();
        }

        return this.testSuite;
    }

    /** Tests the {@link ModelManager#getContext()} method. */
    public void testGetContext() throws Exception
    {
        Assert.assertNotNull( this.getModelManager().getContext() );
    }

    /** Tests the {@link ModelManager#getEntityResolver()} method. */
    public void testGetEntityResolver() throws Exception
    {
        Assert.assertNotNull( this.getModelManager().getEntityResolver() );
    }

    /** Tests the {@link ModelManager#getLSResourceResolver()} method. */
    public void testGetLSResourceResolver() throws Exception
    {
        Assert.assertNotNull( this.getModelManager().getLSResourceResolver() );
    }

    /** Tests the {@link ModelManager#getMarshaller(boolean, boolean)} method. */
    public void testGetMarshaller() throws Exception
    {
        Assert.assertNotNull( this.getModelManager().getMarshaller( true, true ) );
    }

    /** Tests the {@link ModelManager#getObjectFactory()} method. */
    public void testGetObjectFactory() throws Exception
    {
        Assert.assertNotNull( this.getModelManager().getObjectFactory() );
    }

    /** Tests the {@link ModelManager#getSchema()} method. */
    public void testGetSchema() throws Exception
    {
        Assert.assertNotNull( this.getModelManager().getSchema() );
    }

    /** Tests the {@link ModelManager#getUnmarshaller(boolean)} method. */
    public void testGetUnmarshaller() throws Exception
    {
        Assert.assertNotNull( this.getModelManager().getUnmarshaller( true ) );
    }

    public void testGetInstance() throws Exception
    {
        try
        {
            this.getModelManager().getInstance( null, null );
            throw new AssertionError();
        }
        catch ( NullPointerException e )
        {
            Assert.assertNotNull( e.getMessage() );
            System.out.println( e.toString() );
        }

        try
        {
            this.getModelManager().getInstance( new Modules(), null );
            throw new AssertionError();
        }
        catch ( NullPointerException e )
        {
            Assert.assertNotNull( e.getMessage() );
            System.out.println( e.toString() );
        }

        try
        {
            this.getModelManager().getInstance( null, null, null );
            throw new AssertionError();
        }
        catch ( NullPointerException e )
        {
            Assert.assertNotNull( e.getMessage() );
            System.out.println( e.toString() );
        }

        try
        {
            this.getModelManager().getInstance( new Modules(), null, null );
            throw new AssertionError();
        }
        catch ( NullPointerException e )
        {
            Assert.assertNotNull( e.getMessage() );
            System.out.println( e.toString() );
        }

        try
        {
            this.getModelManager().getInstance( new Modules(), new Implementation(), null );
            throw new AssertionError();
        }
        catch ( NullPointerException e )
        {
            Assert.assertNotNull( e.getMessage() );
            System.out.println( e.toString() );
        }

        try
        {
            this.getModelManager().getInstance( null, null, null, null );
            throw new AssertionError();
        }
        catch ( NullPointerException e )
        {
            Assert.assertNotNull( e.getMessage() );
            System.out.println( e.toString() );
        }

        try
        {
            this.getModelManager().getInstance( new Modules(), null, null, null );
            throw new AssertionError();
        }
        catch ( NullPointerException e )
        {
            Assert.assertNotNull( e.getMessage() );
            System.out.println( e.toString() );
        }

        try
        {
            this.getModelManager().getInstance( new Modules(), new Implementation(), null, null );
            throw new AssertionError();
        }
        catch ( NullPointerException e )
        {
            Assert.assertNotNull( e.getMessage() );
            System.out.println( e.toString() );
        }

        try
        {
            this.getModelManager().getInstance( new Modules(), new Implementation(), new Dependency(), null );
            throw new AssertionError();
        }
        catch ( NullPointerException e )
        {
            Assert.assertNotNull( e.getMessage() );
            System.out.println( e.toString() );
        }
    }

    public void testGetObject() throws Exception
    {
        try
        {
            this.getModelManager().getObject( null, null, null );
            throw new AssertionError();
        }
        catch ( NullPointerException e )
        {
            Assert.assertNotNull( e.getMessage() );
            System.out.println( e.toString() );
        }

        try
        {
            this.getModelManager().getObject( new Modules(), null, null );
            throw new AssertionError();
        }
        catch ( NullPointerException e )
        {
            Assert.assertNotNull( e.getMessage() );
            System.out.println( e.toString() );
        }

        try
        {
            this.getModelManager().getObject( new Modules(), new Specification(), null );
            throw new AssertionError();
        }
        catch ( NullPointerException e )
        {
            Assert.assertNotNull( e.getMessage() );
            System.out.println( e.toString() );
        }
    }

    public void testValidateModelObject() throws Exception
    {
        try
        {
            this.getModelManager().validateModelObject( null );
            throw new AssertionError();
        }
        catch ( NullPointerException e )
        {
            Assert.assertNotNull( e.getMessage() );
            System.out.println( e.toString() );
        }
    }

    public void testValidateModules() throws Exception
    {
        try
        {
            this.getModelManager().validateModules( null );
            throw new AssertionError();
        }
        catch ( NullPointerException e )
        {
            Assert.assertNotNull( e.getMessage() );
            System.out.println( e.toString() );
        }
    }

    public void testTransformModelObject() throws Exception
    {
        try
        {
            this.getModelManager().transformModelObject( null, null );
            throw new AssertionError();
        }
        catch ( NullPointerException e )
        {
            Assert.assertNotNull( e.getMessage() );
            System.out.println( e.toString() );
        }
        try
        {
            this.getModelManager().transformModelObject(
                this.getModelManager().getObjectFactory().createModules( new Modules() ), null );

            throw new AssertionError();
        }
        catch ( NullPointerException e )
        {
            Assert.assertNotNull( e.getMessage() );
            System.out.println( e.toString() );
        }
    }

    public void testSchemaConstraints() throws Exception
    {
        for ( SchemaConstraintsTest test : this.getTestSuite().getSchemaConstraintsTest() )
        {
            System.out.println( "SchemaConstraintsTest: " + test.getIdentifier() );

            final JAXBElement<? extends ModelObject> modelObject =
                (JAXBElement<? extends ModelObject>) test.getModelObject().getAny();

            if ( test.getModelObject().isValid() )
            {
                try
                {
                    this.getModelManager().validateModelObject( modelObject );
                }
                catch ( ModelException e )
                {
                    System.out.println( "FAILURE\t" + e.toString() );
                    for ( ModelException.Detail d : e.getDetails() )
                    {
                        System.out.println( "FAILURE\t" + d.toString() );
                    }
                    throw e;
                }
            }
            else
            {
                try
                {
                    this.getModelManager().validateModelObject( modelObject );
                    Assert.fail( test.getIdentifier() + " did not throw a ModelException." );
                }
                catch ( ModelException e )
                {
                    Assert.assertNotNull( e.getMessage() );
                    System.out.println( "\t" + e.toString() );
                }
            }
        }
    }

    public void testModulesConstraints() throws Exception
    {
        for ( ModulesConstraintsTest test : this.getTestSuite().getModulesConstraintsTest() )
        {
            System.out.println( "ModulesConstraintsTest: " + test.getIdentifier() );

            final JAXBElement<Modules> modules = (JAXBElement<Modules>) test.getModules().getAny();

            if ( test.getModules().isValid() )
            {
                try
                {
                    this.getModelManager().validateModules( modules.getValue() );
                }
                catch ( ModelException e )
                {
                    System.out.println( "FAILURE\t" + e.toString() );
                    for ( ModelException.Detail d : e.getDetails() )
                    {
                        System.out.println( "FAILURE\t" + d.toString() );
                    }
                    throw e;
                }
            }
            else
            {
                try
                {
                    this.getModelManager().validateModules( modules.getValue() );
                    Assert.fail( test.getIdentifier() + " did not throw a ModelException." );
                }
                catch ( ModelException e )
                {
                    Assert.assertNotNull( e.getMessage() );
                    System.out.println( "\t" + e.toString() );

                    for ( ModelExceptionDetail expectedDetail : test.getDetail() )
                    {
                        final List<ModelException.Detail> caughtDetails =
                            e.getDetails( expectedDetail.getIdentifier() );

                        for ( ModelException.Detail d : caughtDetails )
                        {
                            System.out.println( "\t" + d.toString() );
                            e.getDetails().remove( d );
                        }

                        Assert.assertEquals( expectedDetail.getCount(), caughtDetails.size() );
                    }

                    if ( !e.getDetails().isEmpty() )
                    {
                        for ( ModelException.Detail d : e.getDetails() )
                        {
                            System.out.println( "FAILURE\t" + d.toString() );
                        }

                        throw e;
                    }
                }
            }
        }
    }

    public void testImplementations() throws Exception
    {
        for ( ImplementationTest test : this.getTestSuite().getImplementationTest() )
        {
            try
            {
                System.out.println( "ImplementationTest: " + test.getIdentifier() );

                final JAXBElement<Modules> modules = (JAXBElement<Modules>) test.getModules().getAny();
                this.getModelManager().validateModules( modules.getValue() );

                final JAXBElement<Implementation> expected =
                    (JAXBElement<Implementation>) test.getImplementation().getAny();

                this.getModelManager().validateModelObject( expected );

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
            catch ( Exception e )
            {
                System.out.println( "FAILURE\t" + e.toString() );
                throw e;
            }
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
            Assert.assertEquals( expected.getJavaValue( ModelManagerTest.class.getClassLoader() ),
                                 computed.getJavaValue( ModelManagerTest.class.getClassLoader() ) );

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

}
