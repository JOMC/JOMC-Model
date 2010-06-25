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

import org.jomc.modlet.Model;
import org.jomc.model.Modules;
import org.jomc.model.modlet.DefaultModelProvider;
import org.jomc.modlet.ModelContext;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

/**
 * Test cases for class {@code org.jomc.model.modlet.DefaultModelProcessor}.
 *
 * @author <a href="mailto:schulte2005@users.sourceforge.net">Christian Schulte</a> 1.0
 * @version $Id$
 */
public class DefaultModelProviderTest
{

    private DefaultModelProvider defaultModelProvider;

    public DefaultModelProviderTest()
    {
        this( null );
    }

    public DefaultModelProviderTest( final DefaultModelProvider defaultModelProvider )
    {
        super();
        this.defaultModelProvider = defaultModelProvider;
    }

    public DefaultModelProvider getModelProvider()
    {
        if ( this.defaultModelProvider == null )
        {
            this.defaultModelProvider = new DefaultModelProvider();
        }

        return this.defaultModelProvider;
    }

    public void testFindModules() throws Exception
    {
        final ModelContext context = ModelContext.createModelContext( this.getClass().getClassLoader() );

        try
        {
            this.getModelProvider().findModules( null, null, null );
            fail( "Expected NullPointerException not thrown." );
        }
        catch ( final NullPointerException e )
        {
            assertNotNull( e.getMessage() );
            System.out.println( e );
        }

        try
        {
            this.getModelProvider().findModules( context, null, null );
            fail( "Expected NullPointerException not thrown." );
        }
        catch ( final NullPointerException e )
        {
            assertNotNull( e.getMessage() );
            System.out.println( e );
        }

        try
        {
            this.getModelProvider().findModules( context, "TEST", null );
            fail( "Expected NullPointerException not thrown." );
        }
        catch ( final NullPointerException e )
        {
            assertNotNull( e.getMessage() );
            System.out.println( e );
        }

        DefaultModelProvider.setDefaultModuleLocation( null );
        this.getModelProvider().setModuleLocation( null );
        assertEquals( 1, this.getModelProvider().findModules(
            context, Modules.MODEL_PUBLIC_ID, DefaultModelProvider.getDefaultModuleLocation() ).getModule().size() );

        assertEquals( 1, this.getModelProvider().findModules(
            context, Modules.MODEL_PUBLIC_ID, this.getModelProvider().getModuleLocation() ).getModule().size() );

        DefaultModelProvider.setDefaultModuleLocation( "DOES_NOT_EXIST" );
        this.getModelProvider().setModuleLocation( "DOES_NOT_EXIST" );

        assertNull( this.getModelProvider().findModules(
            context, Modules.MODEL_PUBLIC_ID, DefaultModelProvider.getDefaultModuleLocation() ) );

        assertNull( this.getModelProvider().findModules(
            context, Modules.MODEL_PUBLIC_ID, this.getModelProvider().getModuleLocation() ) );

        DefaultModelProvider.setDefaultModuleLocation( null );
        this.getModelProvider().setModuleLocation( null );
    }

    public void testFindModel() throws Exception
    {
        final ModelContext context = ModelContext.createModelContext( this.getClass().getClassLoader() );
        final Model model = new Model();
        model.setIdentifier( Modules.MODEL_PUBLIC_ID );

        try
        {
            this.getModelProvider().findModel( null, model );
            fail( "Expected NullPointerException not thrown." );
        }
        catch ( final NullPointerException e )
        {
            assertNotNull( e.getMessage() );
            System.out.println( e.toString() );
        }

        try
        {
            this.getModelProvider().findModel( context, null );
            fail( "Expected NullPointerException not thrown." );
        }
        catch ( final NullPointerException e )
        {
            assertNotNull( e.getMessage() );
            System.out.println( e.toString() );
        }

        assertNotNull( this.getModelProvider().findModel( context, model ) );
    }

    public void testDefaultEnabled() throws Exception
    {
        assertTrue( DefaultModelProvider.isDefaultEnabled() );
        System.setProperty( "org.jomc.model.DefaultModelProvider.defaultEnabled", Boolean.toString( false ) );
        DefaultModelProvider.setDefaultEnabled( null );
        assertFalse( DefaultModelProvider.isDefaultEnabled() );
        System.clearProperty( "org.jomc.model.DefaultModelProvider.defaultEnabled" );
        DefaultModelProvider.setDefaultEnabled( null );
    }

    public void testEnabled() throws Exception
    {
        final Model model = new Model();
        model.setIdentifier( Modules.MODEL_PUBLIC_ID );

        DefaultModelProvider.setDefaultEnabled( null );
        this.getModelProvider().setEnabled( null );
        assertTrue( this.getModelProvider().isEnabled() );

        this.getModelProvider().findModel( ModelContext.createModelContext( this.getClass().getClassLoader() ), model );

        DefaultModelProvider.setDefaultEnabled( false );
        this.getModelProvider().setEnabled( null );
        assertFalse( this.getModelProvider().isEnabled() );

        this.getModelProvider().findModel( ModelContext.createModelContext( this.getClass().getClassLoader() ), model );

        DefaultModelProvider.setDefaultEnabled( null );
        this.getModelProvider().setEnabled( null );
    }

    public void testDefaultModuleLocation() throws Exception
    {
        assertNotNull( DefaultModelProvider.getDefaultModuleLocation() );
        System.setProperty( "org.jomc.model.DefaultModelProvider.defaultModuleLocation", "TEST" );
        DefaultModelProvider.setDefaultModuleLocation( null );
        assertEquals( "TEST", DefaultModelProvider.getDefaultModuleLocation() );
        System.clearProperty( "org.jomc.model.DefaultModelProvider.defaultModuleLocation" );
        DefaultModelProvider.setDefaultModuleLocation( null );
        assertNotNull( DefaultModelProvider.getDefaultModuleLocation() );
    }

    public void testModuleLocation() throws Exception
    {
        DefaultModelProvider.setDefaultModuleLocation( null );
        this.getModelProvider().setModuleLocation( null );
        assertNotNull( this.getModelProvider().getModuleLocation() );

        DefaultModelProvider.setDefaultModuleLocation( "TEST" );
        this.getModelProvider().setModuleLocation( null );
        assertEquals( "TEST", this.getModelProvider().getModuleLocation() );

        DefaultModelProvider.setDefaultModuleLocation( null );
        this.getModelProvider().setModuleLocation( null );
    }

}