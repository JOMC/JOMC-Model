/*
 *   Copyright (C) Christian Schulte <cs@schulte.it>, 2005-206
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

import java.util.concurrent.Callable;
import org.jomc.model.ModelObject;
import org.jomc.model.Module;
import org.jomc.model.Modules;
import org.jomc.model.modlet.ModelHelper;
import org.jomc.modlet.Model;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Test cases for class {@code org.jomc.model.modlet.ModelHelper}.
 *
 * @author <a href="mailto:cs@schulte.it">Christian Schulte</a> 1.0
 * @version $JOMC$
 */
public class ModelHelperTest
{

    /**
     * Creates a new {@code ModelHelperTest} instance.
     */
    public ModelHelperTest()
    {
        super();
    }

    @Test
    public final void testModelHelper() throws Exception
    {
        assertNullPointerException( ()  -> ModelHelper.getModules( null ) );
        assertNullPointerException( ()  ->
        {
            ModelHelper.setModules( new Model(), null );
            return null;
        } );
        assertNullPointerException( ()  ->
        {
            ModelHelper.setModules( null, new Modules() );
            return null;
        } );
        assertNullPointerException( ()  ->
        {
            ModelHelper.addModules( new Model(), null );
            return null;
        } );
        assertNullPointerException( ()  ->
        {
            ModelHelper.addModules( null, new Modules() );
            return null;
        } );
        assertNullPointerException( ()  ->
        {
            ModelHelper.removeModules( null );
            return null;
        } );

        final Model model = new Model();
        model.setIdentifier( ModelObject.MODEL_PUBLIC_ID );

        ModelHelper.setModules( model, new Modules() );

        assertNotNull( ModelHelper.getModules( model ) );
        assertTrue( ModelHelper.getModules( model ).isPresent() );
        assertTrue( ModelHelper.getModules( model ).get().getModule().isEmpty() );

        assertIllegalStateException( ()  ->
        {
            ModelHelper.setModules( model, new Modules() );
            return null;
        } );

        final Module m = new Module();
        m.setName( "TEST" );

        final Modules add = new Modules();
        add.getModule().add( m );

        ModelHelper.addModules( model, add );
        assertNotNull( ModelHelper.getModules( model ) );
        assertTrue( ModelHelper.getModules( model ).isPresent() );
        assertEquals( 1, ModelHelper.getModules( model ).get().getModule().size() );

        ModelHelper.removeModules( model );
        assertNotNull( ModelHelper.getModules( model ) );
        assertFalse( ModelHelper.getModules( model ).isPresent() );
    }

    static void assertNullPointerException( final Callable<?> test ) throws Exception
    {
        try
        {
            test.call();
            fail( "Expected 'NullPointerException' not thrown." );
        }
        catch ( final NullPointerException e )
        {
            assertNotNull( e.getMessage() );
            System.out.println( e );
        }
    }

    static void assertIllegalStateException( final Callable<?> test ) throws Exception
    {
        try
        {
            test.call();
            fail( "Expected 'IllegalStateException' not thrown." );
        }
        catch ( final IllegalStateException e )
        {
            assertNotNull( e.getMessage() );
            System.out.println( e );
        }
    }

}
