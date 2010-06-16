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
import org.jomc.model.modlet.ModletException;
import org.jomc.model.modlet.Modlets;

/**
 * Test cases for {@code org.jomc.model.modlet.ModletContext} implementations.
 *
 * @author <a href="mailto:schulte2005@users.sourceforge.net">Christian Schulte</a> 1.0
 * @version $Id$
 */
public class ModletContextTest
{

    private ModletContext modletContext;

    public ModletContextTest()
    {
        this( null );
    }

    public ModletContextTest( final ModletContext modletContext )
    {
        super();
        this.modletContext = modletContext;
    }

    public ModletContext getModletContext() throws ModletException
    {
        if ( this.modletContext == null )
        {
            this.modletContext = ModletContext.createModletContext( this.getClass().getClassLoader() );
        }

        return this.modletContext;
    }

    public void testGetAttributes() throws Exception
    {
        Assert.assertNotNull( this.getModletContext().getAttributes() );
    }

    public void testFindClass() throws Exception
    {
        try
        {
            this.getModletContext().findClass( null );
            Assert.fail( "Expected NullPointerException not thrown." );
        }
        catch ( final NullPointerException e )
        {
            Assert.assertNotNull( e.getMessage() );
            System.out.println( e.toString() );
        }

        Assert.assertEquals( Object.class, this.getModletContext().findClass( "java.lang.Object" ) );
        Assert.assertNull( this.getModletContext().findClass( "DOES_NOT_EXIST" ) );
    }

    public void testFindResource() throws Exception
    {
        try
        {
            this.getModletContext().findResource( null );
            Assert.fail( "Expected NullPointerException not thrown." );
        }
        catch ( final NullPointerException e )
        {
            Assert.assertNotNull( e.getMessage() );
            System.out.println( e.toString() );
        }

        Assert.assertNotNull( this.getModletContext().findResource( "META-INF/jomc.xsl" ) );
    }

    public void testFindResources() throws Exception
    {
        try
        {
            this.getModletContext().findResources( null );
            Assert.fail( "Expected NullPointerException not thrown." );
        }
        catch ( final NullPointerException e )
        {
            Assert.assertNotNull( e.getMessage() );
            System.out.println( e.toString() );
        }

        Assert.assertTrue( this.getModletContext().findResources( "META-INF/jomc.xsl" ).hasMoreElements() );
    }

    public void testFindModlets() throws Exception
    {
        final Modlets provided = this.getModletContext().findModlets();
        Assert.assertNotNull( provided );
        Assert.assertNotNull( provided.getServices( "http://jomc.org/model" ) );
    }

    public void testCreateModletContext() throws Exception
    {
        ModletContext.setModletContextClassName( null );
        Assert.assertNotNull( ModletContext.createModletContext( null ) );
        Assert.assertNotNull( ModletContext.createModletContext( this.getClass().getClassLoader() ) );

        ModletContext.setModletContextClassName( "DOES_NOT_EXIST" );

        try
        {
            ModletContext.createModletContext( null );
            Assert.fail( "Expected ModletException not thrown." );
        }
        catch ( final ModletException e )
        {
            Assert.assertNotNull( e.getMessage() );
            System.out.println( e );
        }

        try
        {
            ModletContext.createModletContext( this.getClass().getClassLoader() );
            Assert.fail( "Expected ModletException not thrown." );
        }
        catch ( final ModletException e )
        {
            Assert.assertNotNull( e.getMessage() );
            System.out.println( e );
        }

        ModletContext.setModletContextClassName( null );
    }

    public void testCreateContext() throws Exception
    {
        Assert.assertNotNull( this.getModletContext().createContext() );
    }

    public void testCreateMarshaller() throws Exception
    {
        Assert.assertNotNull( this.getModletContext().createMarshaller() );
    }

    public void testCreateUnmarshaller() throws Exception
    {
        Assert.assertNotNull( this.getModletContext().createUnmarshaller() );
    }

    public void testCreateSchema() throws Exception
    {
        Assert.assertNotNull( this.getModletContext().createSchema() );
    }

}
