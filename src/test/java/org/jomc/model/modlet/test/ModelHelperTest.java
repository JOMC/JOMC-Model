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

import org.junit.Test;
import org.jomc.model.Module;
import org.jomc.model.ModelObject;
import org.jomc.model.Modules;
import org.jomc.modlet.Model;
import org.jomc.model.modlet.ModelHelper;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Test cases for class {@code org.jomc.model.modlet.ModelHelper}.
 *
 * @author <a href="mailto:schulte2005@users.sourceforge.net">Christian Schulte</a> 1.0
 * @version $JOMC$
 */
public class ModelHelperTest
{

    /** Creates a new {@code ModelHelperTest} instance. */
    public ModelHelperTest()
    {
        super();
    }

    @Test
    public final void testModelHelper() throws Exception
    {
        try
        {
            ModelHelper.getModules( null );
            fail( "Expected NullPointerException not thrown." );
        }
        catch ( final NullPointerException e )
        {
            assertNotNull( e.getMessage() );
            System.out.println( e.toString() );
        }

        try
        {
            ModelHelper.setModules( new Model(), null );
            fail( "Expected NullPointerException not thrown." );
        }
        catch ( final NullPointerException e )
        {
            assertNotNull( e.getMessage() );
            System.out.println( e.toString() );
        }

        try
        {
            ModelHelper.setModules( null, new Modules() );
            fail( "Expected NullPointerException not thrown." );
        }
        catch ( final NullPointerException e )
        {
            assertNotNull( e.getMessage() );
            System.out.println( e.toString() );
        }

        try
        {
            ModelHelper.addModules( new Model(), null );
            fail( "Expected NullPointerException not thrown." );
        }
        catch ( final NullPointerException e )
        {
            assertNotNull( e.getMessage() );
            System.out.println( e.toString() );
        }

        try
        {
            ModelHelper.addModules( null, new Modules() );
            fail( "Expected NullPointerException not thrown." );
        }
        catch ( final NullPointerException e )
        {
            assertNotNull( e.getMessage() );
            System.out.println( e.toString() );
        }

        try
        {
            ModelHelper.removeModules( null );
            fail( "Expected NullPointerException not thrown." );
        }
        catch ( final NullPointerException e )
        {
            assertNotNull( e.getMessage() );
            System.out.println( e.toString() );
        }

        final Model model = new Model();
        model.setIdentifier( ModelObject.MODEL_PUBLIC_ID );

        ModelHelper.setModules( model, new Modules() );

        assertNotNull( ModelHelper.getModules( model ) );
        assertTrue( ModelHelper.getModules( model ).getModule().isEmpty() );

        try
        {
            ModelHelper.setModules( model, new Modules() );
            fail( "Expected IllegalStateException not thrown." );
        }
        catch ( final IllegalStateException e )
        {
            assertNotNull( e.getMessage() );
            System.out.println( e.toString() );
        }

        final Module m = new Module();
        m.setName( "TEST" );

        final Modules add = new Modules();
        add.getModule().add( m );

        ModelHelper.addModules( model, add );
        assertNotNull( ModelHelper.getModules( model ) );
        assertEquals( 1, ModelHelper.getModules( model ).getModule().size() );

        ModelHelper.removeModules( model );
        assertNull( ModelHelper.getModules( model ) );
    }

}
