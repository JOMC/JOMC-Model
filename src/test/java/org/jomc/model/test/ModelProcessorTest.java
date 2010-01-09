/*
 *   Copyright (c) 2009 The JOMC Project
 *   Copyright (c) 2005 Christian Schulte <cs@jomc.org>
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
import org.jomc.model.ModelProcessor;
import org.jomc.model.Modules;

/**
 * Test cases for {@code org.jomc.model.ModelProcessor} implementations.
 *
 * @author <a href="mailto:cs@jomc.org">Christian Schulte</a> 1.0
 * @version $Id$
 */
public class ModelProcessorTest
{

    private ModelProcessor modelProcessor;

    public ModelProcessorTest()
    {
        this( null );
    }

    public ModelProcessorTest( final ModelProcessor modelProcessor )
    {
        this.modelProcessor = modelProcessor;
    }

    public ModelProcessor getModelProcessor()
    {
        if ( this.modelProcessor == null )
        {
            this.modelProcessor = new DefaultModelProcessor();
        }

        return this.modelProcessor;
    }

    public void testProcessModules() throws Exception
    {
        final ModelContext context = ModelContext.createModelContext( this.getClass().getClassLoader() );

        try
        {
            this.getModelProcessor().processModules( null, null );
            Assert.fail( "Expected NullPointerException not thrown." );
        }
        catch ( final NullPointerException e )
        {
            Assert.assertNotNull( e.getMessage() );
            System.out.println( e );
        }

        try
        {
            this.getModelProcessor().processModules( context, null );
            Assert.fail( "Expected NullPointerException not thrown." );
        }
        catch ( final NullPointerException e )
        {
            Assert.assertNotNull( e.getMessage() );
            System.out.println( e );
        }

        try
        {
            this.getModelProcessor().processModules( null, new Modules() );
            Assert.fail( "Expected NullPointerException not thrown." );
        }
        catch ( final NullPointerException e )
        {
            Assert.assertNotNull( e.getMessage() );
            System.out.println( e );
        }
    }

    public void testDefaultModelProcessor() throws Exception
    {
        if ( this.getModelProcessor() instanceof DefaultModelProcessor )
        {
            final ModelContext context = ModelContext.createModelContext( this.getClass().getClassLoader() );
            final DefaultModelProcessor defaultModelProcessor = (DefaultModelProcessor) this.getModelProcessor();

            try
            {
                defaultModelProcessor.findTransformers( context, null );
                Assert.fail( "Expected NullPointerException not thrown." );
            }
            catch ( final NullPointerException e )
            {
                Assert.assertNotNull( e.getMessage() );
                System.out.println( e );
            }

            try
            {
                defaultModelProcessor.findTransformers( null, "" );
                Assert.fail( "Expected NullPointerException not thrown." );
            }
            catch ( final NullPointerException e )
            {
                Assert.assertNotNull( e.getMessage() );
                System.out.println( e );
            }

            DefaultModelProcessor.setDefaultTransformerLocation( null );
            Assert.assertEquals( 1, defaultModelProcessor.findTransformers(
                context, DefaultModelProcessor.getDefaultTransformerLocation() ).size() );

            DefaultModelProcessor.setDefaultTransformerLocation( "DOES_NOT_EXIST" );
            Assert.assertNull( defaultModelProcessor.findTransformers(
                context, DefaultModelProcessor.getDefaultTransformerLocation() ) );

            DefaultModelProcessor.setDefaultTransformerLocation( null );
        }
    }

}
