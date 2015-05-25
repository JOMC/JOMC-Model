/*
 *   Copyright (C) Christian Schulte, 2005-206
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
 *   THIS SOFTWARE IS PROVIDED "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 *   INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY
 *   AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL
 *   THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY DIRECT, INDIRECT,
 *   INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 *   NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 *   DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 *   THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *   (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 *   THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 *   $JOMC$
 *
 */
package org.jomc.model.modlet.test;

import org.jomc.model.ModelObject;
import org.jomc.model.Modules;
import org.jomc.model.modlet.DefaultModelProcessor;
import org.jomc.model.modlet.ModelHelper;
import org.jomc.modlet.Model;
import org.jomc.modlet.ModelContext;
import org.jomc.modlet.ModelContextFactory;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Test cases for class {@code org.jomc.model.modlet.DefaultModelProcessor}.
 *
 * @author <a href="mailto:cs@schulte.it">Christian Schulte</a> 1.0
 * @version $JOMC$
 */
public class DefaultModelProcessorTest
{

    /**
     * The {@code DefaultModelProcessor} instance tests are performed with.
     */
    private DefaultModelProcessor defaultModelProcessor;

    /**
     * Creates a new {@code DefaultModelProcessorTest} instance.
     */
    public DefaultModelProcessorTest()
    {
        super();
    }

    /**
     * Gets the {@code DefaultModelProcessor} instance tests are performed with.
     *
     * @return The {@code DefaultModelProcessor} instance tests are performed with.
     *
     * @see #newModelProcessor()
     */
    public DefaultModelProcessor getModelProcessor()
    {
        if ( this.defaultModelProcessor == null )
        {
            this.defaultModelProcessor = this.newModelProcessor();
        }

        return this.defaultModelProcessor;
    }

    /**
     * Creates a new {@code DefaultModelProcessor} instance to test.
     *
     * @return A new {@code DefaultModelProcessor} instance to test.
     *
     * @see #getModelProcessor()
     */
    protected DefaultModelProcessor newModelProcessor()
    {
        return new DefaultModelProcessor();
    }

    @Test
    public final void testFindTransformers() throws Exception
    {
        final ModelContext context = ModelContextFactory.newInstance().newModelContext();

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

    @Test
    public final void testProcessModel() throws Exception
    {
        final ModelContext context = ModelContextFactory.newInstance().newModelContext();
        final Model model = new Model();
        model.setIdentifier( ModelObject.MODEL_PUBLIC_ID );

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

        this.getModelProcessor().setTransformerLocation(
            this.getClass().getPackage().getName().replace( '.', '/' ) + "/system-property-test.xsl" );

        final Model processedSystemProperty = this.getModelProcessor().processModel( context, model );
        assertNotNull( processedSystemProperty );

        final Modules processedSystemPropertyModules = ModelHelper.getModules( processedSystemProperty );
        assertNotNull( processedSystemPropertyModules );
        assertNotNull( processedSystemPropertyModules.getModule( System.getProperty( "user.home" ) ) );

        this.getModelProcessor().setTransformerLocation(
            this.getClass().getPackage().getName().replace( '.', '/' ) + "/relative-uri-test.xsl" );

        final Model processedRelativeUri = this.getModelProcessor().processModel( context, model );
        assertNotNull( processedRelativeUri );

        final Modules processedRelativeUriModules = ModelHelper.getModules( processedRelativeUri );
        assertNotNull( processedRelativeUriModules );
        assertNotNull( processedRelativeUriModules.getModule( System.getProperty( "os.name" ) ) );

        this.getModelProcessor().setTransformerLocation( null );
    }

    @Test
    public final void testDefaultEnabled() throws Exception
    {
        System.clearProperty( "org.jomc.model.DefaultModelProcessor.defaultEnabled" );
        System.clearProperty( "org.jomc.model.modlet.DefaultModelProcessor.defaultEnabled" );
        DefaultModelProcessor.setDefaultEnabled( null );
        assertTrue( DefaultModelProcessor.isDefaultEnabled() );

        System.setProperty( "org.jomc.model.DefaultModelProcessor.defaultEnabled", Boolean.toString( false ) );
        DefaultModelProcessor.setDefaultEnabled( null );
        assertFalse( DefaultModelProcessor.isDefaultEnabled() );
        System.clearProperty( "org.jomc.model.DefaultModelProcessor.defaultEnabled" );
        DefaultModelProcessor.setDefaultEnabled( null );
        assertTrue( DefaultModelProcessor.isDefaultEnabled() );

        System.setProperty( "org.jomc.model.modlet.DefaultModelProcessor.defaultEnabled", Boolean.toString( false ) );
        DefaultModelProcessor.setDefaultEnabled( null );
        assertFalse( DefaultModelProcessor.isDefaultEnabled() );
        System.clearProperty( "org.jomc.model.modlet.DefaultModelProcessor.defaultEnabled" );
        DefaultModelProcessor.setDefaultEnabled( null );
        assertTrue( DefaultModelProcessor.isDefaultEnabled() );

        System.setProperty( "org.jomc.model.DefaultModelProcessor.defaultEnabled", Boolean.toString( true ) );
        System.setProperty( "org.jomc.model.modlet.DefaultModelProcessor.defaultEnabled", Boolean.toString( false ) );
        DefaultModelProcessor.setDefaultEnabled( null );
        assertFalse( DefaultModelProcessor.isDefaultEnabled() );
        System.clearProperty( "org.jomc.model.modlet.DefaultModelProcessor.defaultEnabled" );
        DefaultModelProcessor.setDefaultEnabled( null );
        assertTrue( DefaultModelProcessor.isDefaultEnabled() );
        System.clearProperty( "org.jomc.model.DefaultModelProcessor.defaultEnabled" );
        DefaultModelProcessor.setDefaultEnabled( null );
        assertTrue( DefaultModelProcessor.isDefaultEnabled() );
    }

    @Test
    public final void testEnabled() throws Exception
    {
        final Model model = new Model();
        model.setIdentifier( ModelObject.MODEL_PUBLIC_ID );

        DefaultModelProcessor.setDefaultEnabled( null );
        this.getModelProcessor().setEnabled( null );
        assertTrue( this.getModelProcessor().isEnabled() );

        this.getModelProcessor().processModel( ModelContextFactory.newInstance().newModelContext(), model );
        DefaultModelProcessor.setDefaultEnabled( false );
        this.getModelProcessor().setEnabled( null );
        assertFalse( this.getModelProcessor().isEnabled() );

        this.getModelProcessor().processModel( ModelContextFactory.newInstance().newModelContext(), model );
        DefaultModelProcessor.setDefaultEnabled( null );
        this.getModelProcessor().setEnabled( null );
    }

    @Test
    public final void testDefaultTransformerLocation() throws Exception
    {
        System.clearProperty( "org.jomc.model.DefaultModelProcessor.defaultTransformerLocation" );
        System.clearProperty( "org.jomc.model.modlet.DefaultModelProcessor.defaultTransformerLocation" );
        DefaultModelProcessor.setDefaultTransformerLocation( null );
        assertEquals( "META-INF/jomc.xsl", DefaultModelProcessor.getDefaultTransformerLocation() );

        System.setProperty( "org.jomc.model.DefaultModelProcessor.defaultTransformerLocation", "TEST" );
        DefaultModelProcessor.setDefaultTransformerLocation( null );
        assertEquals( "TEST", DefaultModelProcessor.getDefaultTransformerLocation() );
        System.clearProperty( "org.jomc.model.DefaultModelProcessor.defaultTransformerLocation" );
        DefaultModelProcessor.setDefaultTransformerLocation( null );
        assertEquals( "META-INF/jomc.xsl", DefaultModelProcessor.getDefaultTransformerLocation() );

        System.setProperty( "org.jomc.model.modlet.DefaultModelProcessor.defaultTransformerLocation", "TEST" );
        DefaultModelProcessor.setDefaultTransformerLocation( null );
        assertEquals( "TEST", DefaultModelProcessor.getDefaultTransformerLocation() );
        System.clearProperty( "org.jomc.model.modlet.DefaultModelProcessor.defaultTransformerLocation" );
        DefaultModelProcessor.setDefaultTransformerLocation( null );
        assertEquals( "META-INF/jomc.xsl", DefaultModelProcessor.getDefaultTransformerLocation() );

        System.setProperty( "org.jomc.model.DefaultModelProcessor.defaultTransformerLocation", "DEPRECATED" );
        System.setProperty( "org.jomc.model.modlet.DefaultModelProcessor.defaultTransformerLocation", "TEST" );
        DefaultModelProcessor.setDefaultTransformerLocation( null );
        assertEquals( "TEST", DefaultModelProcessor.getDefaultTransformerLocation() );
        System.clearProperty( "org.jomc.model.modlet.DefaultModelProcessor.defaultTransformerLocation" );
        DefaultModelProcessor.setDefaultTransformerLocation( null );
        assertEquals( "DEPRECATED", DefaultModelProcessor.getDefaultTransformerLocation() );
        System.clearProperty( "org.jomc.model.DefaultModelProcessor.defaultTransformerLocation" );
        DefaultModelProcessor.setDefaultTransformerLocation( null );
        assertEquals( "META-INF/jomc.xsl", DefaultModelProcessor.getDefaultTransformerLocation() );
    }

    @Test
    public final void testTransformerLocation() throws Exception
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
