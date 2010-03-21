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

import junit.framework.Assert;
import org.jomc.model.bootstrap.BootstrapContext;
import org.jomc.model.bootstrap.DefaultSchemaProvider;

/**
 * Test cases for class {@code org.jomc.model.bootstrap.DefaultSchemaProvider}.
 *
 * @author <a href="mailto:schulte2005@users.sourceforge.net">Christian Schulte</a> 1.0
 * @version $Id$
 */
public class DefaultSchemaProviderTest extends SchemaProviderTest
{

    private DefaultSchemaProvider defaultSchemaProvider;

    public DefaultSchemaProviderTest()
    {
        this( null );
    }

    public DefaultSchemaProviderTest( final DefaultSchemaProvider defaultSchemaProvider )
    {
        super( defaultSchemaProvider );
        this.defaultSchemaProvider = defaultSchemaProvider;
    }

    @Override
    public DefaultSchemaProvider getSchemaProvider()
    {
        if ( this.defaultSchemaProvider == null )
        {
            this.defaultSchemaProvider = new DefaultSchemaProvider();
        }

        return this.defaultSchemaProvider;
    }

    @Override
    public void testFindSchemas() throws Exception
    {
        super.testFindSchemas();

        final BootstrapContext context = BootstrapContext.createBootstrapContext( this.getClass().getClassLoader() );

        try
        {
            this.getSchemaProvider().findSchemas( context, null );
            Assert.fail( "Expected NullPointerException not thrown." );
        }
        catch ( final NullPointerException e )
        {
            Assert.assertNotNull( e.getMessage() );
            System.out.println( e );
        }

        try
        {
            this.getSchemaProvider().findSchemas( null, "" );
            Assert.fail( "Expected NullPointerException not thrown." );
        }
        catch ( final NullPointerException e )
        {
            Assert.assertNotNull( e.getMessage() );
            System.out.println( e );
        }

        DefaultSchemaProvider.setDefaultSchemaLocation( null );
        this.getSchemaProvider().setSchemaLocation( null );
        Assert.assertEquals( 2, this.getSchemaProvider().findSchemas( context ).getSchema().size() );
        Assert.assertEquals( 2, this.getSchemaProvider().findSchemas(
            context, DefaultSchemaProvider.getDefaultSchemaLocation() ).getSchema().size() );

        Assert.assertEquals( 2, this.getSchemaProvider().findSchemas(
            context, this.getSchemaProvider().getSchemaLocation() ).getSchema().size() );

        DefaultSchemaProvider.setDefaultSchemaLocation( "DOES_NOT_EXIST" );
        this.getSchemaProvider().setSchemaLocation( "DOES_NOT_EXIST" );

        Assert.assertNull( this.getSchemaProvider().findSchemas( context ) );
        Assert.assertNull( this.getSchemaProvider().findSchemas(
            context, DefaultSchemaProvider.getDefaultSchemaLocation() ) );

        Assert.assertNull( this.getSchemaProvider().findSchemas(
            context, this.getSchemaProvider().getSchemaLocation() ) );

        DefaultSchemaProvider.setDefaultSchemaLocation( null );
        this.getSchemaProvider().setSchemaLocation( null );
    }

    public void testGetDefaultSchemaLocation() throws Exception
    {
        Assert.assertNotNull( DefaultSchemaProvider.getDefaultSchemaLocation() );
        DefaultSchemaProvider.setDefaultSchemaLocation( null );
        System.setProperty( "org.jomc.model.bootstrap.DefaultSchemaProvider.defaultSchemaLocation", "TEST" );
        Assert.assertEquals( "TEST", DefaultSchemaProvider.getDefaultSchemaLocation() );
        System.clearProperty( "org.jomc.model.bootstrap.DefaultSchemaProvider.defaultSchemaLocation" );
        DefaultSchemaProvider.setDefaultSchemaLocation( null );
    }

    public void testGetSchemaLocation() throws Exception
    {
        final DefaultSchemaProvider provider = this.getSchemaProvider();

        DefaultSchemaProvider.setDefaultSchemaLocation( null );
        provider.setSchemaLocation( null );
        Assert.assertNotNull( provider.getSchemaLocation() );

        DefaultSchemaProvider.setDefaultSchemaLocation( "TEST" );
        provider.setSchemaLocation( null );
        Assert.assertEquals( "TEST", provider.getSchemaLocation() );

        DefaultSchemaProvider.setDefaultSchemaLocation( null );
        provider.setSchemaLocation( null );
    }

}
