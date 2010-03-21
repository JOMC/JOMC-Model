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
import org.jomc.model.DefaultModelProcessor;
import org.jomc.model.ModelContext;

/**
 * Test cases for class {@code org.jomc.model.DefaultModelProcessor}.
 *
 * @author <a href="mailto:schulte2005@users.sourceforge.net">Christian Schulte</a> 1.0
 * @version $Id$
 */
public class DefaultModelProcessorTest extends ModelProcessorTest
{

    private DefaultModelProcessor defaultModelProcessor;

    public DefaultModelProcessorTest()
    {
        this( null );
    }

    public DefaultModelProcessorTest( final DefaultModelProcessor defaultModelProcessor )
    {
        super( defaultModelProcessor );
        this.defaultModelProcessor = defaultModelProcessor;
    }

    @Override
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
            Assert.fail( "Expected NullPointerException not thrown." );
        }
        catch ( final NullPointerException e )
        {
            Assert.assertNotNull( e.getMessage() );
            System.out.println( e );
        }

        try
        {
            this.getModelProcessor().findTransformers( null, "" );
            Assert.fail( "Expected NullPointerException not thrown." );
        }
        catch ( final NullPointerException e )
        {
            Assert.assertNotNull( e.getMessage() );
            System.out.println( e );
        }

        DefaultModelProcessor.setDefaultTransformerLocation( null );
        this.getModelProcessor().setTransformerLocation( null );
        Assert.assertEquals( 1, this.getModelProcessor().findTransformers(
            context, DefaultModelProcessor.getDefaultTransformerLocation() ).size() );

        DefaultModelProcessor.setDefaultTransformerLocation( "DOES_NOT_EXIST" );
        this.getModelProcessor().setTransformerLocation( "DOES_NOT_EXIST" );

        Assert.assertNull( this.getModelProcessor().findTransformers(
            context, DefaultModelProcessor.getDefaultTransformerLocation() ) );

        Assert.assertNull( this.getModelProcessor().findTransformers(
            context, this.getModelProcessor().getTransformerLocation() ) );

        DefaultModelProcessor.setDefaultTransformerLocation( null );
        this.getModelProcessor().setTransformerLocation( null );
    }

    public void testGetDefaultTransformerLocation() throws Exception
    {
        Assert.assertNotNull( DefaultModelProcessor.getDefaultTransformerLocation() );
        System.setProperty( "org.jomc.model.DefaultModelProcessor.defaultTransformerLocation", "TEST" );
        DefaultModelProcessor.setDefaultTransformerLocation( null );
        Assert.assertEquals( "TEST", DefaultModelProcessor.getDefaultTransformerLocation() );
        System.clearProperty( "org.jomc.model.DefaultModelProcessor.defaultTransformerLocation" );
        DefaultModelProcessor.setDefaultTransformerLocation( null );
    }

    public void testGetTransformerLocation() throws Exception
    {
        final DefaultModelProcessor processor = this.getModelProcessor();

        DefaultModelProcessor.setDefaultTransformerLocation( null );
        processor.setTransformerLocation( null );
        Assert.assertNotNull( processor.getTransformerLocation() );

        DefaultModelProcessor.setDefaultTransformerLocation( "TEST" );
        processor.setTransformerLocation( null );
        Assert.assertEquals( "TEST", processor.getTransformerLocation() );

        DefaultModelProcessor.setDefaultTransformerLocation( null );
        processor.setTransformerLocation( null );
    }

}
