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
import org.jomc.model.ModelProvider;

/**
 * Test cases for {@code org.jomc.model.ModelProvider} implementations.
 *
 * @author <a href="mailto:schulte2005@users.sourceforge.net">Christian Schulte</a> 1.0
 * @version $Id$
 */
public class ModelProviderTest
{

    private ModelProvider modelProvider;

    public ModelProviderTest()
    {
        this( null );
    }

    public ModelProviderTest( final ModelProvider modelProvider )
    {
        this.modelProvider = modelProvider;
    }

    public ModelProvider getModelProvider()
    {
        if ( this.modelProvider == null )
        {
            this.modelProvider = new DefaultModelProvider();
        }

        return this.modelProvider;
    }

    public void testFindModules() throws Exception
    {
        try
        {
            this.getModelProvider().findModules( null );
            Assert.fail( "Expected NullPointerException not thrown." );
        }
        catch ( final NullPointerException e )
        {
            Assert.assertNotNull( e.getMessage() );
            System.out.println( e );
        }
    }

    public void testDefaultModelProvider() throws Exception
    {
        if ( this.getModelProvider() instanceof DefaultModelProvider )
        {
            final ModelContext context = ModelContext.createModelContext( this.getClass().getClassLoader() );
            final DefaultModelProvider defaultModelProvider = (DefaultModelProvider) this.getModelProvider();

            try
            {
                defaultModelProvider.findModules( context, null );
                Assert.fail( "Expected NullPointerException not thrown." );
            }
            catch ( final NullPointerException e )
            {
                Assert.assertNotNull( e.getMessage() );
                System.out.println( e );
            }

            try
            {
                defaultModelProvider.findModules( null, "" );
                Assert.fail( "Expected NullPointerException not thrown." );
            }
            catch ( final NullPointerException e )
            {
                Assert.assertNotNull( e.getMessage() );
                System.out.println( e );
            }

            DefaultModelProvider.setDefaultModuleLocation( "DEFAULT_LOCATION" );
            defaultModelProvider.setModuleLocation( null );
            Assert.assertEquals( "DEFAULT_LOCATION", defaultModelProvider.getModuleLocation() );

            DefaultModelProvider.setDefaultModuleLocation( null );
            defaultModelProvider.setModuleLocation( null );
            Assert.assertEquals( 1, defaultModelProvider.findModules( context ).getModule().size() );
            Assert.assertEquals( 1, defaultModelProvider.findModules(
                context, DefaultModelProvider.getDefaultModuleLocation() ).getModule().size() );

            Assert.assertEquals( 1, defaultModelProvider.findModules(
                context, defaultModelProvider.getModuleLocation() ).getModule().size() );

            DefaultModelProvider.setDefaultModuleLocation( "DOES_NOT_EXIST" );
            defaultModelProvider.setModuleLocation( "DOES_NOT_EXIST" );

            Assert.assertNull( defaultModelProvider.findModules( context ) );
            Assert.assertNull( defaultModelProvider.findModules(
                context, DefaultModelProvider.getDefaultModuleLocation() ) );

            Assert.assertNull( defaultModelProvider.findModules( context, defaultModelProvider.getModuleLocation() ) );
            DefaultModelProvider.setDefaultModuleLocation( null );
            defaultModelProvider.setModuleLocation( null );
        }
    }

}
