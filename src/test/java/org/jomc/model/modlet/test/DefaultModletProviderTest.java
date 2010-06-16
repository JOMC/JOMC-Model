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

import junit.framework.Assert;
import org.jomc.model.modlet.ModletContext;
import org.jomc.model.modlet.DefaultModletProvider;

/**
 * Test cases for class {@code org.jomc.model.modlet.DefaultModletProvider}.
 *
 * @author <a href="mailto:schulte2005@users.sourceforge.net">Christian Schulte</a> 1.0
 * @version $Id$
 */
public class DefaultModletProviderTest extends ModletProviderTest
{

    private DefaultModletProvider defaultModletProvider;

    public DefaultModletProviderTest()
    {
        this( null );
    }

    public DefaultModletProviderTest( final DefaultModletProvider defaultModletProvider )
    {
        super( defaultModletProvider );
        this.defaultModletProvider = defaultModletProvider;
    }

    @Override
    public DefaultModletProvider getModletProvider()
    {
        if ( this.defaultModletProvider == null )
        {
            this.defaultModletProvider = new DefaultModletProvider();
        }

        return this.defaultModletProvider;
    }

    @Override
    public void testFindModlets() throws Exception
    {
        super.testFindModlets();

        final ModletContext context = ModletContext.createModletContext( this.getClass().getClassLoader() );

        try
        {
            this.getModletProvider().findModlets( context, null );
            Assert.fail( "Expected NullPointerException not thrown." );
        }
        catch ( final NullPointerException e )
        {
            Assert.assertNotNull( e.getMessage() );
            System.out.println( e );
        }

        DefaultModletProvider.setDefaultModletLocation( null );
        this.getModletProvider().setModletLocation( null );
        Assert.assertEquals( 2, this.getModletProvider().findModlets( context ).getSchemas( "http://jomc.org/model" ).
            getSchema().size() );

        Assert.assertEquals( 2, this.getModletProvider().findModlets(
            context, DefaultModletProvider.getDefaultModletLocation() ).getSchemas( "http://jomc.org/model" ).
            getSchema().size() );

        Assert.assertEquals( 2, this.getModletProvider().findModlets(
            context, this.getModletProvider().getModletLocation() ).getSchemas( "http://jomc.org/model" ).
            getSchema().size() );

        DefaultModletProvider.setDefaultModletLocation( "DOES_NOT_EXIST" );
        this.getModletProvider().setModletLocation( "DOES_NOT_EXIST" );

        Assert.assertNull( this.getModletProvider().findModlets( context ) );
        Assert.assertNull( this.getModletProvider().findModlets(
            context, DefaultModletProvider.getDefaultModletLocation() ) );

        Assert.assertNull( this.getModletProvider().findModlets(
            context, this.getModletProvider().getModletLocation() ) );

        DefaultModletProvider.setDefaultModletLocation( null );
        this.getModletProvider().setModletLocation( null );
    }

    public void testGetDefaultModletLocation() throws Exception
    {
        Assert.assertNotNull( DefaultModletProvider.getDefaultModletLocation() );
        DefaultModletProvider.setDefaultModletLocation( null );
        System.setProperty( "org.jomc.model.modlet.DefaultModletProvider.defaultModletLocation", "TEST" );
        Assert.assertEquals( "TEST", DefaultModletProvider.getDefaultModletLocation() );
        System.clearProperty( "org.jomc.model.modlet.DefaultModletProvider.defaultModletLocation" );
        DefaultModletProvider.setDefaultModletLocation( null );
    }

    public void testGetModletLocation() throws Exception
    {
        final DefaultModletProvider provider = this.getModletProvider();

        DefaultModletProvider.setDefaultModletLocation( null );
        provider.setModletLocation( null );
        Assert.assertNotNull( provider.getModletLocation() );

        DefaultModletProvider.setDefaultModletLocation( "TEST" );
        provider.setModletLocation( null );
        Assert.assertEquals( "TEST", provider.getModletLocation() );

        DefaultModletProvider.setDefaultModletLocation( null );
        provider.setModletLocation( null );
    }

}
