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
import org.jomc.model.bootstrap.DefaultServiceProvider;

/**
 * Test cases for class {@code org.jomc.model.bootstrap.DefaultServiceProvider}.
 *
 * @author <a href="mailto:schulte2005@users.sourceforge.net">Christian Schulte</a> 1.0
 * @version $Id$
 */
public class DefaultServiceProviderTest extends ServiceProviderTest
{

    private DefaultServiceProvider defaultServiceProvider;

    public DefaultServiceProviderTest()
    {
        this( null );
    }

    public DefaultServiceProviderTest( final DefaultServiceProvider defaultServiceProvider )
    {
        super( defaultServiceProvider );
        this.defaultServiceProvider = defaultServiceProvider;
    }

    @Override
    public DefaultServiceProvider getServiceProvider()
    {
        if ( this.defaultServiceProvider == null )
        {
            this.defaultServiceProvider = new DefaultServiceProvider();
        }

        return this.defaultServiceProvider;
    }

    @Override
    public void testFindServices() throws Exception
    {
        super.testFindServices();

        final BootstrapContext context = BootstrapContext.createBootstrapContext( this.getClass().getClassLoader() );

        try
        {
            this.getServiceProvider().findServices( context, null );
            Assert.fail( "Expected NullPointerException not thrown." );
        }
        catch ( final NullPointerException e )
        {
            Assert.assertNotNull( e.getMessage() );
            System.out.println( e );
        }

        try
        {
            this.getServiceProvider().findServices( null, "" );
            Assert.fail( "Expected NullPointerException not thrown." );
        }
        catch ( final NullPointerException e )
        {
            Assert.assertNotNull( e.getMessage() );
            System.out.println( e );
        }

        DefaultServiceProvider.setDefaultServiceLocation( null );
        this.getServiceProvider().setServiceLocation( null );
        Assert.assertEquals( 6, this.getServiceProvider().findServices( context ).getService().size() );
        Assert.assertEquals( 6, this.getServiceProvider().findServices(
            context, DefaultServiceProvider.getDefaultServiceLocation() ).getService().size() );

        Assert.assertEquals( 6, this.getServiceProvider().findServices(
            context, this.getServiceProvider().getServiceLocation() ).getService().size() );

        DefaultServiceProvider.setDefaultServiceLocation( "DOES_NOT_EXIST" );
        this.getServiceProvider().setServiceLocation( "DOES_NOT_EXIST" );

        Assert.assertNull( this.getServiceProvider().findServices( context ) );
        Assert.assertNull( this.getServiceProvider().findServices(
            context, DefaultServiceProvider.getDefaultServiceLocation() ) );

        Assert.assertNull( this.getServiceProvider().findServices(
            context, this.getServiceProvider().getServiceLocation() ) );

        DefaultServiceProvider.setDefaultServiceLocation( null );
        this.getServiceProvider().setServiceLocation( null );
    }

    public void testGetDefaultServiceLocation() throws Exception
    {
        Assert.assertNotNull( DefaultServiceProvider.getDefaultServiceLocation() );
        DefaultServiceProvider.setDefaultServiceLocation( null );
        System.setProperty( "org.jomc.model.bootstrap.DefaultServiceProvider.defaultServiceLocation", "TEST" );
        Assert.assertEquals( "TEST", DefaultServiceProvider.getDefaultServiceLocation() );
        System.clearProperty( "org.jomc.model.bootstrap.DefaultServiceProvider.defaultServiceLocation" );
        DefaultServiceProvider.setDefaultServiceLocation( null );
    }

    public void testGetServiceLocation() throws Exception
    {
        final DefaultServiceProvider provider = this.getServiceProvider();

        DefaultServiceProvider.setDefaultServiceLocation( null );
        provider.setServiceLocation( null );
        Assert.assertNotNull( provider.getServiceLocation() );

        DefaultServiceProvider.setDefaultServiceLocation( "TEST" );
        provider.setServiceLocation( null );
        Assert.assertEquals( "TEST", provider.getServiceLocation() );

        DefaultServiceProvider.setDefaultServiceLocation( null );
        provider.setServiceLocation( null );
    }

}
