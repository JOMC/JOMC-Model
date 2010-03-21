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

import junit.framework.Assert;
import org.jomc.model.DefaultModelProvider;
import org.jomc.model.ModelContext;

/**
 * Test cases for class {@code org.jomc.model.DefaultModelProcessor}.
 *
 * @author <a href="mailto:schulte2005@users.sourceforge.net">Christian Schulte</a> 1.0
 * @version $Id$
 */
public class DefaultModelProviderTest extends ModelProviderTest
{

    private DefaultModelProvider defaultModelProvider;

    public DefaultModelProviderTest()
    {
        this( null );
    }

    public DefaultModelProviderTest( final DefaultModelProvider defaultModelProvider )
    {
        super( defaultModelProvider );
        this.defaultModelProvider = defaultModelProvider;
    }

    @Override
    public DefaultModelProvider getModelProvider()
    {
        if ( this.defaultModelProvider == null )
        {
            this.defaultModelProvider = new DefaultModelProvider();
        }

        return this.defaultModelProvider;
    }

    @Override
    public void testFindModules() throws Exception
    {
        super.testFindModules();

        final ModelContext context = ModelContext.createModelContext( this.getClass().getClassLoader() );

        try
        {
            this.getModelProvider().findModules( context, null );
            Assert.fail( "Expected NullPointerException not thrown." );
        }
        catch ( final NullPointerException e )
        {
            Assert.assertNotNull( e.getMessage() );
            System.out.println( e );
        }

        try
        {
            this.getModelProvider().findModules( null, "" );
            Assert.fail( "Expected NullPointerException not thrown." );
        }
        catch ( final NullPointerException e )
        {
            Assert.assertNotNull( e.getMessage() );
            System.out.println( e );
        }

        DefaultModelProvider.setDefaultModuleLocation( null );
        this.getModelProvider().setModuleLocation( null );
        Assert.assertEquals( 1, this.getModelProvider().findModules( context ).getModule().size() );
        Assert.assertEquals( 1, this.getModelProvider().findModules(
            context, DefaultModelProvider.getDefaultModuleLocation() ).getModule().size() );

        Assert.assertEquals( 1, this.getModelProvider().findModules(
            context, this.getModelProvider().getModuleLocation() ).getModule().size() );

        DefaultModelProvider.setDefaultModuleLocation( "DOES_NOT_EXIST" );
        this.getModelProvider().setModuleLocation( "DOES_NOT_EXIST" );

        Assert.assertNull( this.getModelProvider().findModules( context ) );
        Assert.assertNull( this.getModelProvider().findModules(
            context, DefaultModelProvider.getDefaultModuleLocation() ) );

        Assert.assertNull( this.getModelProvider().findModules(
            context, this.getModelProvider().getModuleLocation() ) );

        DefaultModelProvider.setDefaultModuleLocation( null );
        this.getModelProvider().setModuleLocation( null );
    }

    public void testGetDefaultModuleLocation() throws Exception
    {
        Assert.assertNotNull( DefaultModelProvider.getDefaultModuleLocation() );
        System.setProperty( "org.jomc.model.DefaultModelProvider.defaultModuleLocation", "TEST" );
        DefaultModelProvider.setDefaultModuleLocation( null );
        Assert.assertEquals( "TEST", DefaultModelProvider.getDefaultModuleLocation() );
        System.clearProperty( "org.jomc.model.DefaultModelProvider.defaultModuleLocation" );
        DefaultModelProvider.setDefaultModuleLocation( null );
    }

    public void testGetModuleLocation() throws Exception
    {
        final DefaultModelProvider provider = this.getModelProvider();

        DefaultModelProvider.setDefaultModuleLocation( null );
        provider.setModuleLocation( null );
        Assert.assertNotNull( provider.getModuleLocation() );

        DefaultModelProvider.setDefaultModuleLocation( "TEST" );
        provider.setModuleLocation( null );
        Assert.assertEquals( "TEST", provider.getModuleLocation() );

        DefaultModelProvider.setDefaultModuleLocation( null );
        provider.setModuleLocation( null );
    }

}
