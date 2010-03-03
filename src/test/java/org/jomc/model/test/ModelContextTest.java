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

import java.util.logging.Level;
import javax.xml.XMLConstants;
import javax.xml.bind.util.JAXBSource;
import javax.xml.transform.Source;
import junit.framework.Assert;
import junit.framework.TestCase;
import org.jomc.model.ModelContext;
import org.jomc.model.ModelException;
import org.jomc.model.ModelValidationReport;
import org.jomc.model.Modules;
import org.jomc.model.ObjectFactory;
import org.w3c.dom.ls.LSInput;

/**
 * Test cases for {@code org.jomc.model.ModelContext} implementations.
 *
 * @author <a href="mailto:schulte2005@users.sourceforge.net">Christian Schulte</a> 1.0
 * @version $Id$
 */
public class ModelContextTest extends TestCase
{

    private ModelContext modelContext;

    public ModelContextTest()
    {
        this( null );
    }

    public ModelContextTest( final ModelContext modelContext )
    {
        super();
        this.modelContext = modelContext;
    }

    public ModelContext getModelContext() throws ModelException
    {
        if ( this.modelContext == null )
        {
            this.modelContext = ModelContext.createModelContext( this.getClass().getClassLoader() );
            this.modelContext.getListeners().add( new ModelContext.Listener()
            {

                @Override
                public void onLog( final Level level, final String message, final Throwable t )
                {
                    System.out.println( "[" + level.getLocalizedName() + "] " + message );
                }

            } );

        }

        return this.modelContext;
    }

    public void testFindClass() throws Exception
    {
        try
        {
            this.getModelContext().findClass( null );
            Assert.fail( "Expected NullPointerException not thrown." );
        }
        catch ( final NullPointerException e )
        {
            Assert.assertNotNull( e.getMessage() );
            System.out.println( e.toString() );
        }

        Assert.assertNotNull( this.getModelContext().findClass( "java.lang.Object" ) );
    }

    public void testFindResource() throws Exception
    {
        try
        {
            this.getModelContext().findResource( null );
            Assert.fail( "Expected NullPointerException not thrown." );
        }
        catch ( final NullPointerException e )
        {
            Assert.assertNotNull( e.getMessage() );
            System.out.println( e.toString() );
        }

        Assert.assertNotNull( this.getModelContext().findResource( "META-INF/jomc.xsl" ) );
    }

    public void testFindResources() throws Exception
    {
        try
        {
            this.getModelContext().findResources( null );
            Assert.fail( "Expected NullPointerException not thrown." );
        }
        catch ( final NullPointerException e )
        {
            Assert.assertNotNull( e.getMessage() );
            System.out.println( e.toString() );
        }

        Assert.assertTrue( this.getModelContext().findResources( "META-INF/jomc.xsl" ).hasMoreElements() );
    }

    public void testFindModules() throws Exception
    {
        final Modules provided = this.getModelContext().findModules();
        Assert.assertNotNull( provided );
        Assert.assertNotNull( provided.getModule( "TestModelProvider" ) );
    }

    public void testProcessModules() throws Exception
    {
        final Modules provided = this.getModelContext().findModules();
        Assert.assertNotNull( provided );
        final Modules processed = this.getModelContext().processModules( provided );
        Assert.assertNotNull( processed );
        Assert.assertNotNull( processed.getModule( "TestModelProcessor" ) );
    }

    public void testValidateModel() throws Exception
    {
        try
        {
            this.getModelContext().validateModel( (Modules) null );
            Assert.fail( "Expected NullPointerException not thrown." );
        }
        catch ( final NullPointerException e )
        {
            Assert.assertNotNull( e.getMessage() );
            System.out.println( e.toString() );
        }

        try
        {
            this.getModelContext().validateModel( (Source) null );
            Assert.fail( "Expected NullPointerException not thrown." );
        }
        catch ( final NullPointerException e )
        {
            Assert.assertNotNull( e.getMessage() );
            System.out.println( e.toString() );
        }

        ModelValidationReport report = this.getModelContext().validateModel( new Modules() );
        Assert.assertNotNull( report );
        Assert.assertEquals( 1, report.getDetails( "TestModelValidator" ).size() );

        report = this.getModelContext().validateModel( new JAXBSource(
            this.getModelContext().createContext(), new ObjectFactory().createModules( new Modules() ) ) );

        Assert.assertNotNull( report );
        Assert.assertTrue( report.isModelValid() );
    }

    public void testCreateContext() throws Exception
    {
        Assert.assertNotNull( this.getModelContext().createContext() );
    }

    public void testCreateMarshaller() throws Exception
    {
        Assert.assertNotNull( this.getModelContext().createMarshaller() );
    }

    public void testCreateUnmarshaller() throws Exception
    {
        Assert.assertNotNull( this.getModelContext().createUnmarshaller() );
    }

    public void testCreateSchema() throws Exception
    {
        Assert.assertNotNull( this.getModelContext().createSchema() );
    }

    public void testCreateEntityResolver() throws Exception
    {
        Assert.assertNotNull( this.getModelContext().createEntityResolver() );
        Assert.assertNull( this.getModelContext().createEntityResolver().resolveEntity( null, "UNKNOWN" ) );
        Assert.assertNotNull( this.getModelContext().createEntityResolver().
            resolveEntity( "http://jomc.org/model", "UNKNOWN" ) );

    }

    public void testCreateResourceResolver() throws Exception
    {
        Assert.assertNotNull( this.getModelContext().createResourceResolver() );

        Assert.assertNotNull( this.getModelContext().createResourceResolver().
            resolveResource( XMLConstants.W3C_XML_SCHEMA_NS_URI, "http://jomc.org/model", null, null, null ) );

        Assert.assertNotNull( this.getModelContext().createResourceResolver().
            resolveResource( XMLConstants.W3C_XML_SCHEMA_NS_URI, "http://jomc.org/model", null,
                             "http://jomc.org/model/jomc-1.0.xsd", null ) );

        Assert.assertNotNull( this.getModelContext().createResourceResolver().
            resolveResource( XMLConstants.W3C_XML_SCHEMA_NS_URI, null, "http://jomc.org/model",
                             "http://jomc.org/model/jomc-1.0.xsd", null ) );

        Assert.assertNull( this.getModelContext().createResourceResolver().
            resolveResource( "UNSUPPORTED", null, null, null, null ) );

        final LSInput input = this.getModelContext().createResourceResolver().
            resolveResource( XMLConstants.W3C_XML_SCHEMA_NS_URI, null, "http://jomc.org/model",
                             "http://jomc.org/model/jomc-1.0.xsd", null );

        Assert.assertNotNull( input );

        input.getBaseURI();
        input.getByteStream();
        input.getCertifiedText();
        input.getCharacterStream();
        input.getEncoding();
        input.getPublicId();
        input.getStringData();
        input.getSystemId();

        input.setBaseURI( null );
        input.setByteStream( null );
        input.setCertifiedText( false );
        input.setCharacterStream( null );
        input.setEncoding( null );
        input.setPublicId( null );
        input.setStringData( null );
        input.setSystemId( null );
    }

}
