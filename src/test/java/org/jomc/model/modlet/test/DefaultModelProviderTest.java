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
import org.jomc.model.modlet.DefaultModelProvider;
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
public class DefaultModelProviderTest
{

    /**
     * The {@code DefaultModelProvider} instance tests are performed with.
     */
    private DefaultModelProvider defaultModelProvider;

    /**
     * Creates a new {@code DefaultModelProviderTest} instance.
     */
    public DefaultModelProviderTest()
    {
        super();
    }

    /**
     * Gets the {@code DefaultModelProvider} instance tests are performed with.
     *
     * @return The {@code DefaultModelProvider} instance tests are performed with.
     *
     * @see #newModelProvider()
     */
    public DefaultModelProvider getModelProvider()
    {
        if ( this.defaultModelProvider == null )
        {
            this.defaultModelProvider = this.newModelProvider();
        }

        return this.defaultModelProvider;
    }

    /**
     * Creates a new {@code DefaultModelProvider} instance to test.
     *
     * @return A new {@code DefaultModelProvider} instance to test.
     *
     * @see #getModelProvider()
     */
    protected DefaultModelProvider newModelProvider()
    {
        return new DefaultModelProvider();
    }

    @Test
    public final void testFindModules() throws Exception
    {
        final ModelContext context = ModelContextFactory.newInstance().newModelContext();

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
                      context, ModelObject.MODEL_PUBLIC_ID, DefaultModelProvider.getDefaultModuleLocation() ).
                      getModule().size() );

        assertEquals( 1, this.getModelProvider().findModules(
                      context, ModelObject.MODEL_PUBLIC_ID, this.getModelProvider().getModuleLocation() ).getModule().
                      size() );

        DefaultModelProvider.setDefaultModuleLocation( "DOES_NOT_EXIST" );
        this.getModelProvider().setModuleLocation( "DOES_NOT_EXIST" );

        assertNull( this.getModelProvider().findModules(
            context, ModelObject.MODEL_PUBLIC_ID, DefaultModelProvider.getDefaultModuleLocation() ) );

        assertNull( this.getModelProvider().findModules(
            context, ModelObject.MODEL_PUBLIC_ID, this.getModelProvider().getModuleLocation() ) );

        DefaultModelProvider.setDefaultModuleLocation( null );
        this.getModelProvider().setModuleLocation( null );
    }

    @Test
    public final void testFindModel() throws Exception
    {
        final ModelContext context = ModelContextFactory.newInstance().newModelContext();
        final Model model = new Model();
        model.setIdentifier( ModelObject.MODEL_PUBLIC_ID );

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

    @Test
    public final void testDefaultEnabled() throws Exception
    {
        System.clearProperty( "org.jomc.model.DefaultModelProvider.defaultEnabled" );
        System.clearProperty( "org.jomc.model.modlet.DefaultModelProvider.defaultEnabled" );
        DefaultModelProvider.setDefaultEnabled( null );
        assertTrue( DefaultModelProvider.isDefaultEnabled() );

        System.setProperty( "org.jomc.model.DefaultModelProvider.defaultEnabled", Boolean.toString( false ) );
        DefaultModelProvider.setDefaultEnabled( null );
        assertFalse( DefaultModelProvider.isDefaultEnabled() );
        System.clearProperty( "org.jomc.model.DefaultModelProvider.defaultEnabled" );
        DefaultModelProvider.setDefaultEnabled( null );
        assertTrue( DefaultModelProvider.isDefaultEnabled() );

        System.setProperty( "org.jomc.model.modlet.DefaultModelProvider.defaultEnabled", Boolean.toString( false ) );
        DefaultModelProvider.setDefaultEnabled( null );
        assertFalse( DefaultModelProvider.isDefaultEnabled() );
        System.clearProperty( "org.jomc.model.modlet.DefaultModelProvider.defaultEnabled" );
        DefaultModelProvider.setDefaultEnabled( null );
        assertTrue( DefaultModelProvider.isDefaultEnabled() );

        System.setProperty( "org.jomc.model.DefaultModelProvider.defaultEnabled", Boolean.toString( true ) );
        System.setProperty( "org.jomc.model.modlet.DefaultModelProvider.defaultEnabled", Boolean.toString( false ) );
        DefaultModelProvider.setDefaultEnabled( null );
        assertFalse( DefaultModelProvider.isDefaultEnabled() );
        System.clearProperty( "org.jomc.model.modlet.DefaultModelProvider.defaultEnabled" );
        DefaultModelProvider.setDefaultEnabled( null );
        assertTrue( DefaultModelProvider.isDefaultEnabled() );
        System.clearProperty( "org.jomc.model.DefaultModelProvider.defaultEnabled" );
        DefaultModelProvider.setDefaultEnabled( null );
        assertTrue( DefaultModelProvider.isDefaultEnabled() );
    }

    @Test
    public final void testEnabled() throws Exception
    {
        final Model model = new Model();
        model.setIdentifier( ModelObject.MODEL_PUBLIC_ID );

        DefaultModelProvider.setDefaultEnabled( null );
        this.getModelProvider().setEnabled( null );
        assertTrue( this.getModelProvider().isEnabled() );

        this.getModelProvider().findModel( ModelContextFactory.newInstance().newModelContext(), model );
        DefaultModelProvider.setDefaultEnabled( false );
        this.getModelProvider().setEnabled( null );
        assertFalse( this.getModelProvider().isEnabled() );

        this.getModelProvider().findModel( ModelContextFactory.newInstance().newModelContext(), model );
        DefaultModelProvider.setDefaultEnabled( null );
        this.getModelProvider().setEnabled( null );
    }

    @Test
    public final void testDefaultModuleLocation() throws Exception
    {
        System.clearProperty( "org.jomc.model.DefaultModelProvider.defaultModuleLocation" );
        System.clearProperty( "org.jomc.model.modlet.DefaultModelProvider.defaultModuleLocation" );
        DefaultModelProvider.setDefaultModuleLocation( null );
        assertEquals( "META-INF/jomc.xml", DefaultModelProvider.getDefaultModuleLocation() );

        System.setProperty( "org.jomc.model.DefaultModelProvider.defaultModuleLocation", "TEST" );
        DefaultModelProvider.setDefaultModuleLocation( null );
        assertEquals( "TEST", DefaultModelProvider.getDefaultModuleLocation() );
        System.clearProperty( "org.jomc.model.DefaultModelProvider.defaultModuleLocation" );
        DefaultModelProvider.setDefaultModuleLocation( null );
        assertEquals( "META-INF/jomc.xml", DefaultModelProvider.getDefaultModuleLocation() );

        System.setProperty( "org.jomc.model.modlet.DefaultModelProvider.defaultModuleLocation", "TEST" );
        DefaultModelProvider.setDefaultModuleLocation( null );
        assertEquals( "TEST", DefaultModelProvider.getDefaultModuleLocation() );
        System.clearProperty( "org.jomc.model.modlet.DefaultModelProvider.defaultModuleLocation" );
        DefaultModelProvider.setDefaultModuleLocation( null );
        assertEquals( "META-INF/jomc.xml", DefaultModelProvider.getDefaultModuleLocation() );

        System.setProperty( "org.jomc.model.DefaultModelProvider.defaultModuleLocation", "DEPRECATED" );
        System.setProperty( "org.jomc.model.modlet.DefaultModelProvider.defaultModuleLocation", "TEST" );
        DefaultModelProvider.setDefaultModuleLocation( null );
        assertEquals( "TEST", DefaultModelProvider.getDefaultModuleLocation() );
        System.clearProperty( "org.jomc.model.modlet.DefaultModelProvider.defaultModuleLocation" );
        DefaultModelProvider.setDefaultModuleLocation( null );
        assertEquals( "DEPRECATED", DefaultModelProvider.getDefaultModuleLocation() );
        System.clearProperty( "org.jomc.model.DefaultModelProvider.defaultModuleLocation" );
        DefaultModelProvider.setDefaultModuleLocation( null );
        assertEquals( "META-INF/jomc.xml", DefaultModelProvider.getDefaultModuleLocation() );
    }

    @Test
    public final void testModuleLocation() throws Exception
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

    @Test
    public final void testDefaultValidating() throws Exception
    {
        System.clearProperty( "org.jomc.model.modlet.DefaultModelProvider.defaultValidating" );
        DefaultModelProvider.setDefaultValidating( null );
        assertTrue( DefaultModelProvider.isDefaultValidating() );
        DefaultModelProvider.setDefaultValidating( null );
        System.setProperty( "org.jomc.model.modlet.DefaultModelProvider.defaultValidating", "false" );
        assertFalse( DefaultModelProvider.isDefaultValidating() );
        System.clearProperty( "org.jomc.model.modlet.DefaultModelProvider.defaultValidating" );
        DefaultModelProvider.setDefaultValidating( null );
        assertTrue( DefaultModelProvider.isDefaultValidating() );
    }

    @Test
    public final void testValidating() throws Exception
    {
        DefaultModelProvider.setDefaultValidating( null );
        this.getModelProvider().setValidating( null );
        assertTrue( this.getModelProvider().isValidating() );

        DefaultModelProvider.setDefaultValidating( false );
        this.getModelProvider().setValidating( null );
        assertFalse( this.getModelProvider().isValidating() );

        DefaultModelProvider.setDefaultValidating( null );
        this.getModelProvider().setValidating( null );
    }

}
