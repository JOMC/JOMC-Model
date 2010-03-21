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
package org.jomc.model.bootstrap.test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Properties;
import junit.framework.Assert;
import org.jomc.model.bootstrap.BootstrapException;
import org.jomc.model.bootstrap.DefaultBootstrapContext;

/**
 * Test cases for class {@code org.jomc.model.bootstrap.DefaultBootstrapContext}.
 *
 * @author <a href="mailto:schulte2005@users.sourceforge.net">Christian Schulte</a> 1.0
 * @version $Id$
 */
public class DefaultBootstrapContextTest extends BootstrapContextTest
{

    private DefaultBootstrapContext defaultBootstrapContext;

    public DefaultBootstrapContextTest()
    {
        this( null );
    }

    public DefaultBootstrapContextTest( final DefaultBootstrapContext defaultBootstrapContext )
    {
        super( defaultBootstrapContext );
        this.defaultBootstrapContext = defaultBootstrapContext;
    }

    @Override
    public DefaultBootstrapContext getBootstrapContext()
    {
        if ( this.defaultBootstrapContext == null )
        {
            this.defaultBootstrapContext = new DefaultBootstrapContext( this.getClass().getClassLoader() );
        }

        return this.defaultBootstrapContext;
    }

    @Override
    public void testFindServices() throws Exception
    {
        super.testFindServices();

        final File tmpFile = File.createTempFile( this.getClass().getName(), ".properties" );
        final Properties properties = new Properties();
        properties.setProperty( "org.jomc.model.bootstrap.ServiceProvider.0", "DOES_NOT_EXIST" );

        OutputStream out = new FileOutputStream( tmpFile );
        properties.store( out, this.getClass().getName() );
        out.close();

        try
        {
            DefaultBootstrapContext.setDefaultPlatformProviderLocation( tmpFile.getAbsolutePath() );
            DefaultBootstrapContext.setDefaultProviderLocation( "DOES_NOT_EXIST" );
            this.getBootstrapContext().setPlatformProviderLocation( null );
            this.getBootstrapContext().setProviderLocation( null );

            this.getBootstrapContext().findServices();
            Assert.fail( "Expected BootstrapException not thrown." );
        }
        catch ( final BootstrapException e )
        {
            Assert.assertNotNull( e.getMessage() );
            System.out.println( e );
        }

        properties.setProperty( "org.jomc.model.bootstrap.ServiceProvider.0", "java.lang.Object" );
        out = new FileOutputStream( tmpFile );
        properties.store( out, this.getClass().getName() );
        out.close();

        try
        {
            DefaultBootstrapContext.setDefaultPlatformProviderLocation( tmpFile.getAbsolutePath() );
            DefaultBootstrapContext.setDefaultProviderLocation( "DOES_NOT_EXIST" );
            this.getBootstrapContext().setPlatformProviderLocation( null );
            this.getBootstrapContext().setProviderLocation( null );

            this.getBootstrapContext().findServices();
            Assert.fail( "Expected BootstrapException not thrown." );
        }
        catch ( final BootstrapException e )
        {
            Assert.assertNotNull( e.getMessage() );
            System.out.println( e );
        }

        properties.setProperty( "org.jomc.model.bootstrap.ServiceProvider.0", TestServiceProvider.class.getName() );
        out = new FileOutputStream( tmpFile );
        properties.store( out, this.getClass().getName() );
        out.close();

        DefaultBootstrapContext.setDefaultPlatformProviderLocation( tmpFile.getAbsolutePath() );
        DefaultBootstrapContext.setDefaultProviderLocation( "DOES_NOT_EXIST" );
        this.getBootstrapContext().setPlatformProviderLocation( null );
        this.getBootstrapContext().setProviderLocation( null );

        Assert.assertEquals( 1, this.getBootstrapContext().findServices().getService().size() );

        tmpFile.delete();

        try
        {
            DefaultBootstrapContext.setDefaultPlatformProviderLocation( null );
            DefaultBootstrapContext.setDefaultProviderLocation( "META-INF/non-existent-services" );
            this.getBootstrapContext().setPlatformProviderLocation( null );
            this.getBootstrapContext().setProviderLocation( null );

            this.getBootstrapContext().findServices();
            Assert.fail( "Expected BootstrapException not thrown." );
        }
        catch ( final BootstrapException e )
        {
            Assert.assertNotNull( e.getMessage() );
            System.out.println( e );
        }

        try
        {
            DefaultBootstrapContext.setDefaultPlatformProviderLocation( null );
            DefaultBootstrapContext.setDefaultProviderLocation( "META-INF/illegal-services" );
            this.getBootstrapContext().setPlatformProviderLocation( null );
            this.getBootstrapContext().setProviderLocation( null );

            this.getBootstrapContext().findServices();
            Assert.fail( "Expected BootstrapException not thrown." );
        }
        catch ( final BootstrapException e )
        {
            Assert.assertNotNull( e.getMessage() );
            System.out.println( e );
        }

        DefaultBootstrapContext.setDefaultPlatformProviderLocation( null );
        DefaultBootstrapContext.setDefaultProviderLocation( null );
        this.getBootstrapContext().setPlatformProviderLocation( null );
        this.getBootstrapContext().setProviderLocation( null );
    }

    @Override
    public void testFindSchemas() throws Exception
    {
        super.testFindSchemas();

        final File tmpFile = File.createTempFile( this.getClass().getName(), ".properties" );
        final Properties properties = new Properties();
        properties.setProperty( "org.jomc.model.bootstrap.SchemaProvider.0", "DOES_NOT_EXIST" );

        OutputStream out = new FileOutputStream( tmpFile );
        properties.store( out, this.getClass().getName() );
        out.close();

        try
        {
            DefaultBootstrapContext.setDefaultPlatformProviderLocation( tmpFile.getAbsolutePath() );
            DefaultBootstrapContext.setDefaultProviderLocation( "DOES_NOT_EXIST" );
            this.getBootstrapContext().setPlatformProviderLocation( null );
            this.getBootstrapContext().setProviderLocation( null );

            this.getBootstrapContext().findSchemas();
            Assert.fail( "Expected BootstrapException not thrown." );
        }
        catch ( final BootstrapException e )
        {
            Assert.assertNotNull( e.getMessage() );
            System.out.println( e );
        }

        properties.setProperty( "org.jomc.model.bootstrap.SchemaProvider.0", "java.lang.Object" );
        out = new FileOutputStream( tmpFile );
        properties.store( out, this.getClass().getName() );
        out.close();

        try
        {
            DefaultBootstrapContext.setDefaultPlatformProviderLocation( tmpFile.getAbsolutePath() );
            DefaultBootstrapContext.setDefaultProviderLocation( "DOES_NOT_EXIST" );
            this.getBootstrapContext().setPlatformProviderLocation( null );
            this.getBootstrapContext().setProviderLocation( null );

            this.getBootstrapContext().findSchemas();
            Assert.fail( "Expected BootstrapException not thrown." );
        }
        catch ( final BootstrapException e )
        {
            Assert.assertNotNull( e.getMessage() );
            System.out.println( e );
        }

        properties.setProperty( "org.jomc.model.bootstrap.SchemaProvider.0", TestSchemaProvider.class.getName() );
        out = new FileOutputStream( tmpFile );
        properties.store( out, this.getClass().getName() );
        out.close();

        DefaultBootstrapContext.setDefaultPlatformProviderLocation( tmpFile.getAbsolutePath() );
        DefaultBootstrapContext.setDefaultProviderLocation( "DOES_NOT_EXIST" );
        this.getBootstrapContext().setPlatformProviderLocation( null );
        this.getBootstrapContext().setProviderLocation( null );

        Assert.assertEquals( 1, this.getBootstrapContext().findSchemas().getSchema().size() );

        tmpFile.delete();

        try
        {
            DefaultBootstrapContext.setDefaultPlatformProviderLocation( null );
            DefaultBootstrapContext.setDefaultProviderLocation( "META-INF/non-existent-services" );
            this.getBootstrapContext().setPlatformProviderLocation( null );
            this.getBootstrapContext().setProviderLocation( null );

            this.getBootstrapContext().findSchemas();
            Assert.fail( "Expected BootstrapException not thrown." );
        }
        catch ( final BootstrapException e )
        {
            Assert.assertNotNull( e.getMessage() );
            System.out.println( e );
        }

        try
        {
            DefaultBootstrapContext.setDefaultPlatformProviderLocation( null );
            DefaultBootstrapContext.setDefaultProviderLocation( "META-INF/illegal-services" );
            this.getBootstrapContext().setPlatformProviderLocation( null );
            this.getBootstrapContext().setProviderLocation( null );

            this.getBootstrapContext().findSchemas();
            Assert.fail( "Expected BootstrapException not thrown." );
        }
        catch ( final BootstrapException e )
        {
            Assert.assertNotNull( e.getMessage() );
            System.out.println( e );
        }

        DefaultBootstrapContext.setDefaultPlatformProviderLocation( null );
        DefaultBootstrapContext.setDefaultProviderLocation( null );
        this.getBootstrapContext().setPlatformProviderLocation( null );
        this.getBootstrapContext().setProviderLocation( null );
    }

    public void testGetDefaultProviderLocation() throws Exception
    {
        Assert.assertNotNull( DefaultBootstrapContext.getDefaultProviderLocation() );
        DefaultBootstrapContext.setDefaultProviderLocation( null );
        System.setProperty( "org.jomc.model.bootstrap.DefaultBootstrapContext.defaultProviderLocation", "TEST" );
        Assert.assertEquals( "TEST", DefaultBootstrapContext.getDefaultProviderLocation() );
        System.clearProperty( "org.jomc.model.bootstrap.DefaultBootstrapContext.defaultProviderLocation" );
        DefaultBootstrapContext.setDefaultProviderLocation( null );
    }

    public void testGetDefaultPlatformProviderLocation() throws Exception
    {
        Assert.assertNotNull( DefaultBootstrapContext.getDefaultPlatformProviderLocation() );
        DefaultBootstrapContext.setDefaultPlatformProviderLocation( null );
        System.setProperty( "org.jomc.model.bootstrap.DefaultBootstrapContext.defaultPlatformProviderLocation", "TEST" );
        Assert.assertEquals( "TEST", DefaultBootstrapContext.getDefaultPlatformProviderLocation() );
        System.clearProperty( "org.jomc.model.bootstrap.DefaultBootstrapContext.defaultPlatformProviderLocation" );
        DefaultBootstrapContext.setDefaultPlatformProviderLocation( null );
    }

    public void testGetDefaultBootstrapSchemaSystemIdLocation() throws Exception
    {
        Assert.assertNotNull( DefaultBootstrapContext.getDefaultBootstrapSchemaSystemId() );
        DefaultBootstrapContext.setDefaultBootstrapSchemaSystemId( null );
        System.setProperty( "org.jomc.model.bootstrap.DefaultBootstrapContext.defaultBootstrapSchemaSystemId", "TEST" );
        Assert.assertEquals( "TEST", DefaultBootstrapContext.getDefaultBootstrapSchemaSystemId() );
        System.clearProperty( "org.jomc.model.bootstrap.DefaultBootstrapContext.defaultBootstrapSchemaSystemId" );
        DefaultBootstrapContext.setDefaultBootstrapSchemaSystemId( null );
    }

    public void testGetProviderLocation() throws Exception
    {
        final DefaultBootstrapContext ctx = this.getBootstrapContext();

        DefaultBootstrapContext.setDefaultProviderLocation( null );
        ctx.setProviderLocation( null );
        Assert.assertNotNull( ctx.getProviderLocation() );

        DefaultBootstrapContext.setDefaultProviderLocation( "TEST" );
        ctx.setProviderLocation( null );
        Assert.assertEquals( "TEST", ctx.getProviderLocation() );

        DefaultBootstrapContext.setDefaultProviderLocation( null );
        ctx.setProviderLocation( null );
    }

    public void testGetPlatformProviderLocation() throws Exception
    {
        final DefaultBootstrapContext ctx = this.getBootstrapContext();

        DefaultBootstrapContext.setDefaultPlatformProviderLocation( null );
        ctx.setPlatformProviderLocation( null );
        Assert.assertNotNull( ctx.getPlatformProviderLocation() );

        DefaultBootstrapContext.setDefaultPlatformProviderLocation( "TEST" );
        ctx.setPlatformProviderLocation( null );
        Assert.assertEquals( "TEST", ctx.getPlatformProviderLocation() );

        DefaultBootstrapContext.setDefaultPlatformProviderLocation( null );
        ctx.setPlatformProviderLocation( null );
    }

    public void testGetBootstrapSchemaSystemId() throws Exception
    {
        final DefaultBootstrapContext ctx = this.getBootstrapContext();

        DefaultBootstrapContext.setDefaultBootstrapSchemaSystemId( null );
        ctx.setBootstrapSchemaSystemId( null );
        Assert.assertNotNull( ctx.getBootstrapSchemaSystemId() );

        DefaultBootstrapContext.setDefaultBootstrapSchemaSystemId( "TEST" );
        ctx.setBootstrapSchemaSystemId( null );
        Assert.assertEquals( "TEST", ctx.getBootstrapSchemaSystemId() );

        DefaultBootstrapContext.setDefaultBootstrapSchemaSystemId( null );
        ctx.setBootstrapSchemaSystemId( null );
    }

}
