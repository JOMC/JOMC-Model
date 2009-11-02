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

import java.util.logging.Level;
import junit.framework.Assert;
import junit.framework.TestCase;
import org.jomc.model.DefaultModelManager;
import org.jomc.model.ModelManager;

/**
 * Test cases for {@code org.jomc.model.ModelManager} implementations.
 *
 * @author <a href="mailto:cs@jomc.org">Christian Schulte</a> 1.0
 * @version $Id$
 */
public class ModelManagerTest extends TestCase
{

    /** The instance tests are performed with. */
    private ModelManager modelManager;

    /**
     * Gets the {@code ModelManager} instance to test.
     *
     * @return The {@code ModelManager} instance to test.
     */
    public ModelManager getModelManager()
    {
        if ( this.modelManager == null )
        {
            final DefaultModelManager defaultManager = new DefaultModelManager();
            defaultManager.setLogLevel( Level.ALL );
            defaultManager.getListeners().add( new DefaultModelManager.Listener()
            {

                public void onLog( final Level level, final String message, final Throwable t )
                {
                    System.out.print( "[" + level.getLocalizedName() + "] " );
                    if ( message != null )
                    {
                        System.out.print( message );
                    }
                    if ( t != null )
                    {
                        System.out.print( ( message == null ? "" : " - " ) + t.toString() );
                    }
                    System.out.println();
                }

            } );

            this.modelManager = defaultManager;
        }

        return this.modelManager;
    }

    /**
     * Sets the {@code ModelManager} instance to test.
     *
     * @param value The new {@code ModelManager} instance to test.
     */
    public void setModelManager( final ModelManager value )
    {
        this.modelManager = value;
    }

    /** Tests the {@link ModelManager#getContext(java.lang.ClassLoader) } method. */
    public void testGetContext() throws Exception
    {
        try
        {
            this.getModelManager().getContext( null );
            Assert.fail( "Expected NullPointerException not thrown." );
        }
        catch ( final NullPointerException e )
        {
            Assert.assertNotNull( e.getMessage() );
            System.out.println( e.toString() );
        }

        Assert.assertNotNull( this.getModelManager().getContext( this.getClass().getClassLoader() ) );
    }

    /** Tests the {@link ModelManager#getMarshaller(java.lang.ClassLoader) } method. */
    public void testGetMarshaller() throws Exception
    {
        try
        {
            this.getModelManager().getMarshaller( null );
            Assert.fail( "Expected NullPointerException not thrown." );
        }
        catch ( final NullPointerException e )
        {
            Assert.assertNotNull( e.getMessage() );
            System.out.println( e.toString() );
        }

        Assert.assertNotNull( this.getModelManager().getMarshaller( this.getClass().getClassLoader() ) );
    }

    /** Tests the {@link ModelManager#getUnmarshaller(java.lang.ClassLoader) } method. */
    public void testGetUnmarshaller() throws Exception
    {
        try
        {
            this.getModelManager().getUnmarshaller( null );
            Assert.fail( "Expected NullPointerException not thrown." );
        }
        catch ( final NullPointerException e )
        {
            Assert.assertNotNull( e.getMessage() );
            System.out.println( e.toString() );
        }

        Assert.assertNotNull( this.getModelManager().getUnmarshaller( this.getClass().getClassLoader() ) );
    }

    /** Tests the {@link ModelManager#getEntityResolver(java.lang.ClassLoader)} method. */
    public void testGetEntityResolver() throws Exception
    {
        try
        {
            this.getModelManager().getEntityResolver( null );
            Assert.fail( "Expected NullPointerException not thrown." );
        }
        catch ( final NullPointerException e )
        {
            Assert.assertNotNull( e.getMessage() );
            System.out.println( e.toString() );
        }

        Assert.assertNotNull( this.getModelManager().getEntityResolver( this.getClass().getClassLoader() ) );
    }

    /** Tests the {@link ModelManager#getResourceResolver(java.lang.ClassLoader)} method. */
    public void testGetResourceResolver() throws Exception
    {
        try
        {
            this.getModelManager().getResourceResolver( null );
            Assert.fail( "Expected NullPointerException not thrown." );
        }
        catch ( final NullPointerException e )
        {
            Assert.assertNotNull( e.getMessage() );
            System.out.println( e.toString() );
        }

        Assert.assertNotNull( this.getModelManager().getResourceResolver( this.getClass().getClassLoader() ) );
    }

    /** Tests the {@link ModelManager#getSchema(java.lang.ClassLoader)} method. */
    public void testGetSchema() throws Exception
    {
        try
        {
            this.getModelManager().getSchema( null );
            Assert.fail( "Expected NullPointerException not thrown." );
        }
        catch ( final NullPointerException e )
        {
            Assert.assertNotNull( e.getMessage() );
            System.out.println( e.toString() );
        }

        Assert.assertNotNull( this.getModelManager().getSchema( this.getClass().getClassLoader() ) );
    }

}
