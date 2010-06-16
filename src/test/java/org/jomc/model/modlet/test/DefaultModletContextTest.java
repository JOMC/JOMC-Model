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

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Properties;
import junit.framework.Assert;
import org.jomc.model.modlet.ModletException;
import org.jomc.model.modlet.DefaultModletContext;

/**
 * Test cases for class {@code org.jomc.model.modlet.DefaultModletContext}.
 *
 * @author <a href="mailto:schulte2005@users.sourceforge.net">Christian Schulte</a> 1.0
 * @version $Id$
 */
public class DefaultModletContextTest extends ModletContextTest
{

    private DefaultModletContext defaultModletContext;

    public DefaultModletContextTest()
    {
        this( null );
    }

    public DefaultModletContextTest( final DefaultModletContext defaultModletContext )
    {
        super( defaultModletContext );
        this.defaultModletContext = defaultModletContext;
    }

    @Override
    public DefaultModletContext getModletContext()
    {
        if ( this.defaultModletContext == null )
        {
            this.defaultModletContext = new DefaultModletContext( this.getClass().getClassLoader() );
        }

        return this.defaultModletContext;
    }

    @Override
    public void testFindModlets() throws Exception
    {
        super.testFindModlets();

        final File tmpFile = File.createTempFile( this.getClass().getName(), ".properties" );
        final Properties properties = new Properties();
        properties.setProperty( "org.jomc.model.modlet.ModletProvider.0", "DOES_NOT_EXIST" );

        OutputStream out = new FileOutputStream( tmpFile );
        properties.store( out, this.getClass().getName() );
        out.close();

        try
        {
            DefaultModletContext.setDefaultPlatformProviderLocation( tmpFile.getAbsolutePath() );
            DefaultModletContext.setDefaultProviderLocation( "DOES_NOT_EXIST" );
            this.getModletContext().setPlatformProviderLocation( null );
            this.getModletContext().setProviderLocation( null );

            this.getModletContext().findModlets();
            Assert.fail( "Expected ModletException not thrown." );
        }
        catch ( final ModletException e )
        {
            Assert.assertNotNull( e.getMessage() );
            System.out.println( e );
        }

        properties.setProperty( "org.jomc.model.modlet.ModletProvider.0", "java.lang.Object" );
        out = new FileOutputStream( tmpFile );
        properties.store( out, this.getClass().getName() );
        out.close();

        try
        {
            DefaultModletContext.setDefaultPlatformProviderLocation( tmpFile.getAbsolutePath() );
            DefaultModletContext.setDefaultProviderLocation( "DOES_NOT_EXIST" );
            this.getModletContext().setPlatformProviderLocation( null );
            this.getModletContext().setProviderLocation( null );

            this.getModletContext().findModlets();
            Assert.fail( "Expected ModletException not thrown." );
        }
        catch ( final ModletException e )
        {
            Assert.assertNotNull( e.getMessage() );
            System.out.println( e );
        }

        properties.setProperty( "org.jomc.model.modlet.ModletProvider.0", TestModletProvider.class.getName() );
        out = new FileOutputStream( tmpFile );
        properties.store( out, this.getClass().getName() );
        out.close();

        DefaultModletContext.setDefaultPlatformProviderLocation( tmpFile.getAbsolutePath() );
        DefaultModletContext.setDefaultProviderLocation( "DOES_NOT_EXIST" );
        this.getModletContext().setPlatformProviderLocation( null );
        this.getModletContext().setProviderLocation( null );

        Assert.assertEquals( 1, this.getModletContext().findModlets().getServices( "http://jomc.org/model/empty" ).
            getService().size() );

        Assert.assertEquals( 1, this.getModletContext().findModlets().getSchemas( "http://jomc.org/model/empty" ).
            getSchema().size() );

        tmpFile.delete();

        try
        {
            DefaultModletContext.setDefaultPlatformProviderLocation( null );
            DefaultModletContext.setDefaultProviderLocation( "META-INF/non-existent-services" );
            this.getModletContext().setPlatformProviderLocation( null );
            this.getModletContext().setProviderLocation( null );

            this.getModletContext().findModlets();
            Assert.fail( "Expected ModletException not thrown." );
        }
        catch ( final ModletException e )
        {
            Assert.assertNotNull( e.getMessage() );
            System.out.println( e );
        }

        try
        {
            DefaultModletContext.setDefaultPlatformProviderLocation( null );
            DefaultModletContext.setDefaultProviderLocation( "META-INF/illegal-services" );
            this.getModletContext().setPlatformProviderLocation( null );
            this.getModletContext().setProviderLocation( null );

            this.getModletContext().findModlets();
            Assert.fail( "Expected ModletException not thrown." );
        }
        catch ( final ModletException e )
        {
            Assert.assertNotNull( e.getMessage() );
            System.out.println( e );
        }

        DefaultModletContext.setDefaultPlatformProviderLocation( null );
        DefaultModletContext.setDefaultProviderLocation( null );
        this.getModletContext().setPlatformProviderLocation( null );
        this.getModletContext().setProviderLocation( null );
    }

    public void testGetDefaultProviderLocation() throws Exception
    {
        Assert.assertNotNull( DefaultModletContext.getDefaultProviderLocation() );
        DefaultModletContext.setDefaultProviderLocation( null );
        System.setProperty( "org.jomc.model.modlet.DefaultModletContext.defaultProviderLocation", "TEST" );
        Assert.assertEquals( "TEST", DefaultModletContext.getDefaultProviderLocation() );
        System.clearProperty( "org.jomc.model.modlet.DefaultModletContext.defaultProviderLocation" );
        DefaultModletContext.setDefaultProviderLocation( null );
    }

    public void testGetDefaultPlatformProviderLocation() throws Exception
    {
        Assert.assertNotNull( DefaultModletContext.getDefaultPlatformProviderLocation() );
        DefaultModletContext.setDefaultPlatformProviderLocation( null );
        System.setProperty( "org.jomc.model.modlet.DefaultModletContext.defaultPlatformProviderLocation", "TEST" );
        Assert.assertEquals( "TEST", DefaultModletContext.getDefaultPlatformProviderLocation() );
        System.clearProperty( "org.jomc.model.modlet.DefaultModletContext.defaultPlatformProviderLocation" );
        DefaultModletContext.setDefaultPlatformProviderLocation( null );
    }

    public void testGetDefaultModletSchemaSystemIdLocation() throws Exception
    {
        Assert.assertNotNull( DefaultModletContext.getDefaultModletSchemaSystemId() );
        DefaultModletContext.setDefaultModletSchemaSystemId( null );
        System.setProperty( "org.jomc.model.modlet.DefaultModletContext.defaultModletSchemaSystemId", "TEST" );
        Assert.assertEquals( "TEST", DefaultModletContext.getDefaultModletSchemaSystemId() );
        System.clearProperty( "org.jomc.model.modlet.DefaultModletContext.defaulModletSchemaSystemId" );
        DefaultModletContext.setDefaultModletSchemaSystemId( null );
    }

    public void testGetProviderLocation() throws Exception
    {
        final DefaultModletContext ctx = this.getModletContext();

        DefaultModletContext.setDefaultProviderLocation( null );
        ctx.setProviderLocation( null );
        Assert.assertNotNull( ctx.getProviderLocation() );

        DefaultModletContext.setDefaultProviderLocation( "TEST" );
        ctx.setProviderLocation( null );
        Assert.assertEquals( "TEST", ctx.getProviderLocation() );

        DefaultModletContext.setDefaultProviderLocation( null );
        ctx.setProviderLocation( null );
    }

    public void testGetPlatformProviderLocation() throws Exception
    {
        final DefaultModletContext ctx = this.getModletContext();

        DefaultModletContext.setDefaultPlatformProviderLocation( null );
        ctx.setPlatformProviderLocation( null );
        Assert.assertNotNull( ctx.getPlatformProviderLocation() );

        DefaultModletContext.setDefaultPlatformProviderLocation( "TEST" );
        ctx.setPlatformProviderLocation( null );
        Assert.assertEquals( "TEST", ctx.getPlatformProviderLocation() );

        DefaultModletContext.setDefaultPlatformProviderLocation( null );
        ctx.setPlatformProviderLocation( null );
    }

    public void testGetModletSchemaSystemId() throws Exception
    {
        final DefaultModletContext ctx = this.getModletContext();

        DefaultModletContext.setDefaultModletSchemaSystemId( null );
        ctx.setModletSchemaSystemId( null );
        Assert.assertNotNull( ctx.getModletSchemaSystemId() );

        DefaultModletContext.setDefaultModletSchemaSystemId( "TEST" );
        ctx.setModletSchemaSystemId( null );
        Assert.assertEquals( "TEST", ctx.getModletSchemaSystemId() );

        DefaultModletContext.setDefaultModletSchemaSystemId( null );
        ctx.setModletSchemaSystemId( null );
    }

}
