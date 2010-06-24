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

import org.jomc.model.Modules;
import org.jomc.modlet.Model;
import org.jomc.model.modlet.DefaultModelProcessor;
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
public class DefaultModelProcessorTest
{

    private DefaultModelProcessor defaultModelProcessor;

    public DefaultModelProcessorTest()
    {
        this( null );
    }

    public DefaultModelProcessorTest( final DefaultModelProcessor defaultModelProcessor )
    {
        super();
        this.defaultModelProcessor = defaultModelProcessor;
    }

    public DefaultModelProcessor getModelProcessor()
    {
        if ( this.defaultModelProcessor == null )
        {
            this.defaultModelProcessor = new DefaultModelProcessor();
        }

        return this.defaultModelProcessor;
    }

    public void testFindTransformers() throws Exception
    {
        final ModelContext context = ModelContext.createModelContext( this.getClass().getClassLoader() );

        try
        {
            this.getModelProcessor().findTransformers( context, null );
            fail( "Expected NullPointerException not thrown." );
        }
        catch ( final NullPointerException e )
        {
            assertNotNull( e.getMessage() );
            System.out.println( e );
        }

        try
        {
            this.getModelProcessor().findTransformers( null, "" );
            fail( "Expected NullPointerException not thrown." );
        }
        catch ( final NullPointerException e )
        {
            assertNotNull( e.getMessage() );
            System.out.println( e );
        }

        DefaultModelProcessor.setDefaultTransformerLocation( null );
        this.getModelProcessor().setTransformerLocation( null );
        assertEquals( 1, this.getModelProcessor().findTransformers(
            context, DefaultModelProcessor.getDefaultTransformerLocation() ).size() );

        DefaultModelProcessor.setDefaultTransformerLocation( "DOES_NOT_EXIST" );
        this.getModelProcessor().setTransformerLocation( "DOES_NOT_EXIST" );

        assertNull( this.getModelProcessor().findTransformers(
            context, DefaultModelProcessor.getDefaultTransformerLocation() ) );

        assertNull( this.getModelProcessor().findTransformers(
            context, this.getModelProcessor().getTransformerLocation() ) );

        DefaultModelProcessor.setDefaultTransformerLocation( null );
        this.getModelProcessor().setTransformerLocation( null );
    }

    public void testProcessModel() throws Exception
    {
        final ModelContext context = ModelContext.createModelContext( this.getClass().getClassLoader() );
        final Model model = new Model();
        model.setIdentifier( Modules.MODEL_PUBLIC_ID );

        try
        {
            this.getModelProcessor().processModel( null, model );
            fail( "Expected NullPointerException not thrown." );
        }
        catch ( final NullPointerException e )
        {
            assertNotNull( e.getMessage() );
            System.out.println( e.toString() );
        }

        try
        {
            this.getModelProcessor().processModel( context, null );
            fail( "Expected NullPointerException not thrown." );
        }
        catch ( final NullPointerException e )
        {
            assertNotNull( e.getMessage() );
            System.out.println( e.toString() );
        }

        assertNotNull( this.getModelProcessor().processModel( context, model ) );
    }

    public void testDefaultEnabled() throws Exception
    {
        assertTrue( DefaultModelProcessor.isDefaultEnabled() );
        System.setProperty( "org.jomc.model.DefaultModelProcessor.defaultEnabled", Boolean.toString( false ) );
        DefaultModelProcessor.setDefaultEnabled( null );
        assertFalse( DefaultModelProcessor.isDefaultEnabled() );
        System.clearProperty( "org.jomc.model.DefaultModelProcessor.defaultEnabled" );
        DefaultModelProcessor.setDefaultEnabled( null );
    }

    public void testEnabled() throws Exception
    {
        final Model model = new Model();
        model.setIdentifier( Modules.MODEL_PUBLIC_ID );

        DefaultModelProcessor.setDefaultEnabled( null );
        this.getModelProcessor().setEnabled( null );
        assertTrue( this.getModelProcessor().isEnabled() );

        this.getModelProcessor().processModel(
            ModelContext.createModelContext( this.getClass().getClassLoader() ), model );

        DefaultModelProcessor.setDefaultEnabled( false );
        this.getModelProcessor().setEnabled( null );
        assertFalse( this.getModelProcessor().isEnabled() );

        this.getModelProcessor().processModel(
            ModelContext.createModelContext( this.getClass().getClassLoader() ), model );

        DefaultModelProcessor.setDefaultEnabled( null );
        this.getModelProcessor().setEnabled( null );
    }

    public void testDefaultTransformerLocation() throws Exception
    {
        assertNotNull( DefaultModelProcessor.getDefaultTransformerLocation() );
        System.setProperty( "org.jomc.model.DefaultModelProcessor.defaultTransformerLocation", "TEST" );
        DefaultModelProcessor.setDefaultTransformerLocation( null );
        assertEquals( "TEST", DefaultModelProcessor.getDefaultTransformerLocation() );
        System.clearProperty( "org.jomc.model.DefaultModelProcessor.defaultTransformerLocation" );
        DefaultModelProcessor.setDefaultTransformerLocation( null );
        assertNotNull( DefaultModelProcessor.getDefaultTransformerLocation() );
    }

    public void testTransformerLocation() throws Exception
    {
        DefaultModelProcessor.setDefaultTransformerLocation( null );
        this.getModelProcessor().setTransformerLocation( null );
        assertNotNull( this.getModelProcessor().getTransformerLocation() );

        DefaultModelProcessor.setDefaultTransformerLocation( "TEST" );
        this.getModelProcessor().setTransformerLocation( null );
        assertEquals( "TEST", this.getModelProcessor().getTransformerLocation() );

        DefaultModelProcessor.setDefaultTransformerLocation( null );
        this.getModelProcessor().setTransformerLocation( null );
    }

}
