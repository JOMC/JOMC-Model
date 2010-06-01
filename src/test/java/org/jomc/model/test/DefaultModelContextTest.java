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

import java.util.logging.Level;
import javax.xml.XMLConstants;
import javax.xml.bind.util.JAXBSource;
import junit.framework.Assert;
import org.jomc.model.DefaultModelContext;
import org.jomc.model.ModelContext;
import org.jomc.model.ModelException;
import org.jomc.model.ModelValidationReport;
import org.jomc.model.Modules;
import org.jomc.model.ObjectFactory;
import org.jomc.model.bootstrap.DefaultBootstrapContext;
import org.jomc.model.bootstrap.DefaultSchemaProvider;
import org.jomc.model.bootstrap.DefaultServiceProvider;
import org.w3c.dom.ls.LSInput;

/**
 * Test cases for class {@code org.jomc.model.DefaultModelContext}.
 *
 * @author <a href="mailto:schulte2005@users.sourceforge.net">Christian Schulte</a> 1.0
 * @version $Id$
 */
public class DefaultModelContextTest extends ModelContextTest
{

    private DefaultModelContext defaultModelContext;

    public DefaultModelContextTest()
    {
        this( null );
    }

    public DefaultModelContextTest( final DefaultModelContext defaultModelContext )
    {
        super( defaultModelContext );
        this.defaultModelContext = defaultModelContext;
    }

    @Override
    public DefaultModelContext getModelContext() throws ModelException
    {
        if ( this.defaultModelContext == null )
        {
            this.defaultModelContext = new DefaultModelContext( this.getClass().getClassLoader() );
            this.defaultModelContext.getListeners().add( new ModelContext.Listener()
            {

                @Override
                public void onLog( final Level level, final String message, final Throwable t )
                {
                    System.out.println( "[" + level.getLocalizedName() + "] " + message );
                }

            } );

        }

        return this.defaultModelContext;
    }

    @Override
    public void testFindModules() throws Exception
    {
        super.testFindModules();

        System.setProperty( DefaultModelContext.class.getName() + ".disableCaching", Boolean.toString( true ) );

        DefaultBootstrapContext.setDefaultBootstrapSchemaSystemId( null );
        DefaultBootstrapContext.setDefaultPlatformProviderLocation( null );
        DefaultBootstrapContext.setDefaultProviderLocation( null );
        DefaultSchemaProvider.setDefaultSchemaLocation( null );
        DefaultServiceProvider.setDefaultServiceLocation( null );

        final Modules provided = this.getModelContext().findModules();
        Assert.assertNotNull( provided );
        Assert.assertNotNull( provided.getModule( "TestModelProvider" ) );

        DefaultServiceProvider.setDefaultServiceLocation( "META-INF/non-existent-services.xml" );

        try
        {
            this.getModelContext().findModules();
            Assert.fail( "Expected ModelException not thrown." );
        }
        catch ( final ModelException e )
        {
            Assert.assertNotNull( e.getMessage() );
            System.out.println( e );
        }

        DefaultServiceProvider.setDefaultServiceLocation( "META-INF/illegal-services.xml" );

        try
        {
            this.getModelContext().findModules();
            Assert.fail( "Expected ModelException not thrown." );
        }
        catch ( final ModelException e )
        {
            Assert.assertNotNull( e.getMessage() );
            System.out.println( e );
        }

        DefaultBootstrapContext.setDefaultBootstrapSchemaSystemId( null );
        DefaultBootstrapContext.setDefaultPlatformProviderLocation( null );
        DefaultBootstrapContext.setDefaultProviderLocation( null );
        DefaultSchemaProvider.setDefaultSchemaLocation( null );
        DefaultServiceProvider.setDefaultServiceLocation( null );

        System.clearProperty( DefaultModelContext.class.getName() + ".disableCaching" );
    }

    @Override
    public void testProcessModules() throws Exception
    {
        super.testProcessModules();

        System.setProperty( DefaultModelContext.class.getName() + ".disableCaching", Boolean.toString( true ) );

        DefaultBootstrapContext.setDefaultBootstrapSchemaSystemId( null );
        DefaultBootstrapContext.setDefaultPlatformProviderLocation( null );
        DefaultBootstrapContext.setDefaultProviderLocation( null );
        DefaultSchemaProvider.setDefaultSchemaLocation( null );
        DefaultServiceProvider.setDefaultServiceLocation( null );

        final Modules provided = this.getModelContext().findModules();
        Assert.assertNotNull( provided );

        final Modules processed = this.getModelContext().processModules( provided );
        Assert.assertNotNull( processed );
        Assert.assertNotNull( processed.getModule( "TestModelProcessor" ) );

        DefaultServiceProvider.setDefaultServiceLocation( "META-INF/non-existent-services.xml" );

        try
        {
            this.getModelContext().processModules( provided );
            Assert.fail( "Expected ModelException not thrown." );
        }
        catch ( final ModelException e )
        {
            Assert.assertNotNull( e.getMessage() );
            System.out.println( e );
        }

        DefaultServiceProvider.setDefaultServiceLocation( "META-INF/illegal-services.xml" );

        try
        {
            this.getModelContext().processModules( provided );
            Assert.fail( "Expected ModelException not thrown." );
        }
        catch ( final ModelException e )
        {
            Assert.assertNotNull( e.getMessage() );
            System.out.println( e );
        }

        DefaultBootstrapContext.setDefaultBootstrapSchemaSystemId( null );
        DefaultBootstrapContext.setDefaultPlatformProviderLocation( null );
        DefaultBootstrapContext.setDefaultProviderLocation( null );
        DefaultSchemaProvider.setDefaultSchemaLocation( null );
        DefaultServiceProvider.setDefaultServiceLocation( null );

        System.clearProperty( DefaultModelContext.class.getName() + ".disableCaching" );
    }

    @Override
    public void testValidateModel() throws Exception
    {
        super.testValidateModel();

        System.setProperty( DefaultModelContext.class.getName() + ".disableCaching", Boolean.toString( true ) );

        DefaultBootstrapContext.setDefaultBootstrapSchemaSystemId( null );
        DefaultBootstrapContext.setDefaultPlatformProviderLocation( null );
        DefaultBootstrapContext.setDefaultProviderLocation( null );
        DefaultSchemaProvider.setDefaultSchemaLocation( null );
        DefaultServiceProvider.setDefaultServiceLocation( null );

        ModelValidationReport report = this.getModelContext().validateModel( new Modules() );
        Assert.assertNotNull( report );
        Assert.assertEquals( 1, report.getDetails( "TestModelValidator" ).size() );

        report = this.getModelContext().validateModel( new JAXBSource(
            this.getModelContext().createContext(), new ObjectFactory().createModules( new Modules() ) ) );

        Assert.assertNotNull( report );
        Assert.assertTrue( report.isModelValid() );

        DefaultServiceProvider.setDefaultServiceLocation( "META-INF/non-existent-services.xml" );

        try
        {
            this.getModelContext().validateModel( new Modules() );
            Assert.fail( "Expected ModelException not thrown." );
        }
        catch ( final ModelException e )
        {
            Assert.assertNotNull( e.getMessage() );
            System.out.println( e );
        }

        DefaultServiceProvider.setDefaultServiceLocation( "META-INF/illegal-services.xml" );

        try
        {
            this.getModelContext().validateModel( new Modules() );
            Assert.fail( "Expected ModelException not thrown." );
        }
        catch ( final ModelException e )
        {
            Assert.assertNotNull( e.getMessage() );
            System.out.println( e );
        }

        DefaultBootstrapContext.setDefaultBootstrapSchemaSystemId( null );
        DefaultBootstrapContext.setDefaultPlatformProviderLocation( null );
        DefaultBootstrapContext.setDefaultProviderLocation( null );
        DefaultSchemaProvider.setDefaultSchemaLocation( null );
        DefaultServiceProvider.setDefaultServiceLocation( null );

        System.clearProperty( DefaultModelContext.class.getName() + ".disableCaching" );
    }

    @Override
    public void testCreateContext() throws Exception
    {
        super.testCreateContext();

        System.setProperty( DefaultModelContext.class.getName() + ".disableCaching", Boolean.toString( true ) );

        DefaultBootstrapContext.setDefaultBootstrapSchemaSystemId( null );
        DefaultBootstrapContext.setDefaultPlatformProviderLocation( "DOES_NOT_EXIST" );
        DefaultBootstrapContext.setDefaultProviderLocation( "DOES_NOT_EXIST" );
        DefaultServiceProvider.setDefaultServiceLocation( null );
        DefaultSchemaProvider.setDefaultSchemaLocation( "META-INF/no-schemas.xml" );

        try
        {
            this.getModelContext().createContext();
            Assert.fail( "Expected ModelException not thrown." );
        }
        catch ( final ModelException e )
        {
            Assert.assertNotNull( e.getMessage() );
            System.out.println( e );
        }

        DefaultBootstrapContext.setDefaultBootstrapSchemaSystemId( null );
        DefaultBootstrapContext.setDefaultPlatformProviderLocation( null );
        DefaultBootstrapContext.setDefaultProviderLocation( null );
        DefaultSchemaProvider.setDefaultSchemaLocation( null );
        DefaultServiceProvider.setDefaultServiceLocation( null );

        System.clearProperty( DefaultModelContext.class.getName() + ".disableCaching" );
    }

    @Override
    public void testCreateMarshaller() throws Exception
    {
        super.testCreateMarshaller();

        System.setProperty( DefaultModelContext.class.getName() + ".disableCaching", Boolean.toString( true ) );

        DefaultBootstrapContext.setDefaultBootstrapSchemaSystemId( null );
        DefaultBootstrapContext.setDefaultPlatformProviderLocation( "DOES_NOT_EXIST" );
        DefaultBootstrapContext.setDefaultProviderLocation( "DOES_NOT_EXIST" );
        DefaultServiceProvider.setDefaultServiceLocation( null );
        DefaultSchemaProvider.setDefaultSchemaLocation( "META-INF/no-schemas.xml" );

        try
        {
            this.getModelContext().createMarshaller();
            Assert.fail( "Expected ModelException not thrown." );
        }
        catch ( final ModelException e )
        {
            Assert.assertNotNull( e.getMessage() );
            System.out.println( e );
        }

        DefaultBootstrapContext.setDefaultBootstrapSchemaSystemId( null );
        DefaultBootstrapContext.setDefaultPlatformProviderLocation( null );
        DefaultBootstrapContext.setDefaultProviderLocation( null );
        DefaultSchemaProvider.setDefaultSchemaLocation( null );
        DefaultServiceProvider.setDefaultServiceLocation( null );

        System.clearProperty( DefaultModelContext.class.getName() + ".disableCaching" );
    }

    @Override
    public void testCreateUnmarshaller() throws Exception
    {
        super.testCreateUnmarshaller();

        System.setProperty( DefaultModelContext.class.getName() + ".disableCaching", Boolean.toString( true ) );

        DefaultBootstrapContext.setDefaultBootstrapSchemaSystemId( null );
        DefaultBootstrapContext.setDefaultPlatformProviderLocation( "DOES_NOT_EXIST" );
        DefaultBootstrapContext.setDefaultProviderLocation( "DOES_NOT_EXIST" );
        DefaultServiceProvider.setDefaultServiceLocation( null );
        DefaultSchemaProvider.setDefaultSchemaLocation( "META-INF/no-schemas.xml" );

        try
        {
            this.getModelContext().createUnmarshaller();
            Assert.fail( "Expected ModelException not thrown." );
        }
        catch ( final ModelException e )
        {
            Assert.assertNotNull( e.getMessage() );
            System.out.println( e );
        }

        DefaultBootstrapContext.setDefaultBootstrapSchemaSystemId( null );
        DefaultBootstrapContext.setDefaultPlatformProviderLocation( null );
        DefaultBootstrapContext.setDefaultProviderLocation( null );
        DefaultSchemaProvider.setDefaultSchemaLocation( null );
        DefaultServiceProvider.setDefaultServiceLocation( null );

        System.clearProperty( DefaultModelContext.class.getName() + ".disableCaching" );
    }

    @Override
    public void testCreateSchema() throws Exception
    {
        super.testCreateSchema();

        System.setProperty( DefaultModelContext.class.getName() + ".disableCaching", Boolean.toString( true ) );

        DefaultBootstrapContext.setDefaultBootstrapSchemaSystemId( null );
        DefaultBootstrapContext.setDefaultPlatformProviderLocation( "DOES_NOT_EXIST" );
        DefaultBootstrapContext.setDefaultProviderLocation( "DOES_NOT_EXIST" );
        DefaultServiceProvider.setDefaultServiceLocation( null );
        DefaultSchemaProvider.setDefaultSchemaLocation( "META-INF/no-schemas.xml" );

        try
        {
            this.getModelContext().createSchema();
            Assert.fail( "Expected ModelException not thrown." );
        }
        catch ( final ModelException e )
        {
            Assert.assertNotNull( e.getMessage() );
            System.out.println( e );
        }

        DefaultBootstrapContext.setDefaultBootstrapSchemaSystemId( null );
        DefaultBootstrapContext.setDefaultPlatformProviderLocation( null );
        DefaultBootstrapContext.setDefaultProviderLocation( null );
        DefaultSchemaProvider.setDefaultSchemaLocation( null );
        DefaultServiceProvider.setDefaultServiceLocation( null );

        System.clearProperty( DefaultModelContext.class.getName() + ".disableCaching" );
    }

    @Override
    public void testCreateEntityResolver() throws Exception
    {
        super.testCreateEntityResolver();

        System.setProperty( DefaultModelContext.class.getName() + ".disableCaching", Boolean.toString( true ) );

        DefaultBootstrapContext.setDefaultBootstrapSchemaSystemId( null );
        DefaultBootstrapContext.setDefaultPlatformProviderLocation( null );
        DefaultBootstrapContext.setDefaultProviderLocation( null );
        DefaultSchemaProvider.setDefaultSchemaLocation( null );
        DefaultServiceProvider.setDefaultServiceLocation( null );

        Assert.assertNotNull( this.getModelContext().createEntityResolver() );
        Assert.assertNull( this.getModelContext().createEntityResolver().resolveEntity( null, "UNKNOWN" ) );
        Assert.assertNotNull( this.getModelContext().createEntityResolver().
            resolveEntity( "http://jomc.org/model", "UNKNOWN" ) );

        DefaultBootstrapContext.setDefaultPlatformProviderLocation( "DOES_NOT_EXIST" );
        DefaultBootstrapContext.setDefaultProviderLocation( "DOES_NOT_EXIST" );
        DefaultSchemaProvider.setDefaultSchemaLocation( "META-INF/no-schemas.xml" );

        Assert.assertNull( this.getModelContext().createEntityResolver().
            resolveEntity( "http://jomc.org/model", "UNKNOWN" ) );

        DefaultBootstrapContext.setDefaultBootstrapSchemaSystemId( null );
        DefaultBootstrapContext.setDefaultPlatformProviderLocation( null );
        DefaultBootstrapContext.setDefaultProviderLocation( null );
        DefaultSchemaProvider.setDefaultSchemaLocation( null );
        DefaultServiceProvider.setDefaultServiceLocation( null );

        System.clearProperty( DefaultModelContext.class.getName() + ".disableCaching" );
    }

    @Override
    public void testCreateResourceResolver() throws Exception
    {
        super.testCreateResourceResolver();

        System.setProperty( DefaultModelContext.class.getName() + ".disableCaching", Boolean.toString( true ) );

        DefaultBootstrapContext.setDefaultBootstrapSchemaSystemId( null );
        DefaultBootstrapContext.setDefaultPlatformProviderLocation( null );
        DefaultBootstrapContext.setDefaultProviderLocation( null );
        DefaultSchemaProvider.setDefaultSchemaLocation( null );
        DefaultServiceProvider.setDefaultServiceLocation( null );

        Assert.assertNotNull( this.getModelContext().createResourceResolver() );

        Assert.assertNotNull( this.getModelContext().createResourceResolver().
            resolveResource( XMLConstants.W3C_XML_SCHEMA_NS_URI, "http://jomc.org/model", null, null, null ) );

        Assert.assertNotNull( this.getModelContext().createResourceResolver().
            resolveResource( XMLConstants.W3C_XML_SCHEMA_NS_URI, "http://jomc.org/model", null,
                             "http://jomc.sourceforge.net/model/jomc-1.0.xsd", null ) );

        Assert.assertNotNull( this.getModelContext().createResourceResolver().
            resolveResource( XMLConstants.W3C_XML_SCHEMA_NS_URI, null, "http://jomc.org/model",
                             "http://jomc.sourceforge.net/model/jomc-1.0.xsd", null ) );

        Assert.assertNull( this.getModelContext().createResourceResolver().
            resolveResource( "UNSUPPORTED", null, null, null, null ) );

        final LSInput input = this.getModelContext().createResourceResolver().
            resolveResource( XMLConstants.W3C_XML_SCHEMA_NS_URI, null, "http://jomc.org/model",
                             "http://jomc.sourceforge.net/model/jomc-1.0.xsd", null );

        Assert.assertNotNull( input );

        input.getBaseURI();
        input.getByteStream();
        input.getCertifiedText();
        input.getCharacterStream();
        input.getEncoding();
        input.getPublicId();
        input.getStringData();
        input.getSystemId();

        input.setBaseURI( null );
        input.setByteStream( null );
        input.setCertifiedText( false );
        input.setCharacterStream( null );
        input.setEncoding( null );
        input.setPublicId( null );
        input.setStringData( null );
        input.setSystemId( null );

        DefaultBootstrapContext.setDefaultPlatformProviderLocation( "DOES_NOT_EXIST" );
        DefaultBootstrapContext.setDefaultProviderLocation( "DOES_NOT_EXIST" );
        DefaultSchemaProvider.setDefaultSchemaLocation( "META-INF/no-schemas.xml" );

        Assert.assertNull( this.getModelContext().createResourceResolver().
            resolveResource( XMLConstants.W3C_XML_SCHEMA_NS_URI, null, "http://jomc.org/model",
                             "http://jomc.sourceforge.net/model/jomc-1.0.xsd", null ) );

        DefaultBootstrapContext.setDefaultBootstrapSchemaSystemId( null );
        DefaultBootstrapContext.setDefaultPlatformProviderLocation( null );
        DefaultBootstrapContext.setDefaultProviderLocation( null );
        DefaultSchemaProvider.setDefaultSchemaLocation( null );
        DefaultServiceProvider.setDefaultServiceLocation( null );

        System.clearProperty( DefaultModelContext.class.getName() + ".disableCaching" );
    }

}