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
package org.jomc.model.bootstrap.test;

import junit.framework.Assert;
import org.jomc.model.bootstrap.BootstrapContext;
import org.jomc.model.bootstrap.BootstrapException;
import org.jomc.model.bootstrap.Schemas;
import org.jomc.model.bootstrap.Services;

/**
 * Test cases for {@code org.jomc.model.bootstrap.BootstrapContext} implementations.
 *
 * @author <a href="mailto:cs@jomc.org">Christian Schulte</a> 1.0
 * @version $Id$
 */
public class BootstrapContextTest
{

    private BootstrapContext bootstrapContext;

    public BootstrapContextTest()
    {
        this( null );
    }

    public BootstrapContextTest( final BootstrapContext bootstrapContext )
    {
        super();
        this.bootstrapContext = bootstrapContext;
    }

    public BootstrapContext getBootstrapContext() throws BootstrapException
    {
        if ( this.bootstrapContext == null )
        {
            this.bootstrapContext = BootstrapContext.createBootstrapContext( this.getClass().getClassLoader() );
        }

        return this.bootstrapContext;
    }

    public void testFindClass() throws Exception
    {
        try
        {
            this.getBootstrapContext().findClass( null );
            Assert.fail( "Expected NullPointerException not thrown." );
        }
        catch ( final NullPointerException e )
        {
            Assert.assertNotNull( e.getMessage() );
            System.out.println( e.toString() );
        }

        Assert.assertNotNull( this.getBootstrapContext().findClass( "java.lang.Object" ) );
    }

    public void testFindResource() throws Exception
    {
        try
        {
            this.getBootstrapContext().findResource( null );
            Assert.fail( "Expected NullPointerException not thrown." );
        }
        catch ( final NullPointerException e )
        {
            Assert.assertNotNull( e.getMessage() );
            System.out.println( e.toString() );
        }

        Assert.assertNotNull( this.getBootstrapContext().findResource( "META-INF/jomc.xsl" ) );
    }

    public void testFindResources() throws Exception
    {
        try
        {
            this.getBootstrapContext().findResources( null );
            Assert.fail( "Expected NullPointerException not thrown." );
        }
        catch ( final NullPointerException e )
        {
            Assert.assertNotNull( e.getMessage() );
            System.out.println( e.toString() );
        }

        Assert.assertTrue( this.getBootstrapContext().findResources( "META-INF/jomc.xsl" ).hasMoreElements() );
    }

    public void testFindServices() throws Exception
    {
        final Services provided = this.getBootstrapContext().findServices();
        Assert.assertNotNull( provided );
        Assert.assertNotNull( provided.getServices( "TestServiceProvider" ) );
    }

    public void testFindSchemas() throws Exception
    {
        final Schemas provided = this.getBootstrapContext().findSchemas();
        Assert.assertNotNull( provided );
        Assert.assertNotNull( provided.getSchemaByPublicId( "http://jomc.org/model/empty" ) );
    }

    public void testCreateContext() throws Exception
    {
        Assert.assertNotNull( this.getBootstrapContext().createContext() );
    }

    public void testCreateMarshaller() throws Exception
    {
        Assert.assertNotNull( this.getBootstrapContext().createMarshaller() );
    }

    public void testCreateUnmarshaller() throws Exception
    {
        Assert.assertNotNull( this.getBootstrapContext().createUnmarshaller() );
    }

    public void testCreateSchema() throws Exception
    {
        Assert.assertNotNull( this.getBootstrapContext().createSchema() );
    }

}
