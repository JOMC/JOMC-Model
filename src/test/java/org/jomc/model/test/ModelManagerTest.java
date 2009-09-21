// SECTION-START[License Header]
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
// SECTION-END
package org.jomc.model.test;

import junit.framework.Assert;
import junit.framework.TestCase;
import org.jomc.model.DefaultModelManager;
import org.jomc.model.Dependency;
import org.jomc.model.Implementation;
import org.jomc.model.ModelManager;
import org.jomc.model.Modules;
import org.jomc.model.Specification;

/**
 * Testcases for {@code org.jomc.model.ModelManager} implementations.
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
            this.modelManager = new DefaultModelManager();
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

    /** Tests the {@link ModelManager#getContext()} method. */
    public void testGetContext() throws Exception
    {
        Assert.assertNotNull( this.getModelManager().getContext() );
    }

    /** Tests the {@link ModelManager#getEntityResolver()} method. */
    public void testGetEntityResolver() throws Exception
    {
        Assert.assertNotNull( this.getModelManager().getEntityResolver() );
    }

    /** Tests the {@link ModelManager#getLSResourceResolver()} method. */
    public void testGetLSResourceResolver() throws Exception
    {
        Assert.assertNotNull( this.getModelManager().getLSResourceResolver() );
    }

    /** Tests the {@link ModelManager#getMarshaller(boolean, boolean)} method. */
    public void testGetMarshaller() throws Exception
    {
        Assert.assertNotNull( this.getModelManager().getMarshaller( true, true ) );
    }

    /** Tests the {@link ModelManager#getObjectFactory()} method. */
    public void testGetObjectFactory() throws Exception
    {
        Assert.assertNotNull( this.getModelManager().getObjectFactory() );
    }

    /** Tests the {@link ModelManager#getSchema()} method. */
    public void testGetSchema() throws Exception
    {
        Assert.assertNotNull( this.getModelManager().getSchema() );
    }

    /** Tests the {@link ModelManager#getUnmarshaller(boolean)} method. */
    public void testGetUnmarshaller() throws Exception
    {
        Assert.assertNotNull( this.getModelManager().getUnmarshaller( true ) );
    }

    public void testGetInstance() throws Exception
    {
        try
        {
            this.getModelManager().getInstance( null, null );
            throw new AssertionError();
        }
        catch ( NullPointerException e )
        {
            Assert.assertNotNull( e.getMessage() );
            System.out.println( e.toString() );
        }

        try
        {
            this.getModelManager().getInstance( new Modules(), null );
            throw new AssertionError();
        }
        catch ( NullPointerException e )
        {
            Assert.assertNotNull( e.getMessage() );
            System.out.println( e.toString() );
        }

        try
        {
            this.getModelManager().getInstance( null, null, null );
            throw new AssertionError();
        }
        catch ( NullPointerException e )
        {
            Assert.assertNotNull( e.getMessage() );
            System.out.println( e.toString() );
        }

        try
        {
            this.getModelManager().getInstance( new Modules(), null, null );
            throw new AssertionError();
        }
        catch ( NullPointerException e )
        {
            Assert.assertNotNull( e.getMessage() );
            System.out.println( e.toString() );
        }

        try
        {
            this.getModelManager().getInstance( new Modules(), new Implementation(), null );
            throw new AssertionError();
        }
        catch ( NullPointerException e )
        {
            Assert.assertNotNull( e.getMessage() );
            System.out.println( e.toString() );
        }

        try
        {
            this.getModelManager().getInstance( null, null, null, null );
            throw new AssertionError();
        }
        catch ( NullPointerException e )
        {
            Assert.assertNotNull( e.getMessage() );
            System.out.println( e.toString() );
        }

        try
        {
            this.getModelManager().getInstance( new Modules(), null, null, null );
            throw new AssertionError();
        }
        catch ( NullPointerException e )
        {
            Assert.assertNotNull( e.getMessage() );
            System.out.println( e.toString() );
        }

        try
        {
            this.getModelManager().getInstance( new Modules(), new Implementation(), null, null );
            throw new AssertionError();
        }
        catch ( NullPointerException e )
        {
            Assert.assertNotNull( e.getMessage() );
            System.out.println( e.toString() );
        }

        try
        {
            this.getModelManager().getInstance( new Modules(), new Implementation(), new Dependency(), null );
            throw new AssertionError();
        }
        catch ( NullPointerException e )
        {
            Assert.assertNotNull( e.getMessage() );
            System.out.println( e.toString() );
        }
    }

    public void testGetObject() throws Exception
    {
        try
        {
            this.getModelManager().getObject( null, null, null );
            throw new AssertionError();
        }
        catch ( NullPointerException e )
        {
            Assert.assertNotNull( e.getMessage() );
            System.out.println( e.toString() );
        }

        try
        {
            this.getModelManager().getObject( new Modules(), null, null );
            throw new AssertionError();
        }
        catch ( NullPointerException e )
        {
            Assert.assertNotNull( e.getMessage() );
            System.out.println( e.toString() );
        }

        try
        {
            this.getModelManager().getObject( new Modules(), new Specification(), null );
            throw new AssertionError();
        }
        catch ( NullPointerException e )
        {
            Assert.assertNotNull( e.getMessage() );
            System.out.println( e.toString() );
        }
    }

    public void testValidateModelObject() throws Exception
    {
        try
        {
            this.getModelManager().validateModelObject( null );
            throw new AssertionError();
        }
        catch ( NullPointerException e )
        {
            Assert.assertNotNull( e.getMessage() );
            System.out.println( e.toString() );
        }
    }

    public void testValidateModules() throws Exception
    {
        try
        {
            this.getModelManager().validateModules( null );
            throw new AssertionError();
        }
        catch ( NullPointerException e )
        {
            Assert.assertNotNull( e.getMessage() );
            System.out.println( e.toString() );
        }
    }

    public void testTransformModelObject() throws Exception
    {
        try
        {
            this.getModelManager().transformModelObject( null, null );
            throw new AssertionError();
        }
        catch ( NullPointerException e )
        {
            Assert.assertNotNull( e.getMessage() );
            System.out.println( e.toString() );
        }
        try
        {
            this.getModelManager().transformModelObject(
                this.getModelManager().getObjectFactory().createModules( new Modules() ), null );

            throw new AssertionError();
        }
        catch ( NullPointerException e )
        {
            Assert.assertNotNull( e.getMessage() );
            System.out.println( e.toString() );
        }
    }

}
