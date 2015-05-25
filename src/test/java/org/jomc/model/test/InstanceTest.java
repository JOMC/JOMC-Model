/*
 *   Copyright (C) Christian Schulte, 2012-353
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
package org.jomc.model.test;

import org.jomc.model.Instance;
import org.jomc.model.ModelObjectException;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

/**
 * Test cases for class {@code org.jomc.model.Instance}.
 *
 * @author <a href="mailto:cs@schulte.it">Christian Schulte</a>
 * @version $JOMC$
 * @since 1.4
 */
public class InstanceTest
{

    /**
     * Creates a new {@code InstanceTest} instance.
     */
    public InstanceTest()
    {
        super();
    }

    @Test
    public final void JavaTypeName() throws Exception
    {
        final Instance i = new Instance();
        assertNull( i.getJavaTypeName() );

        i.setClazz( "@" );

        try
        {
            i.getJavaTypeName();
            fail( "Expected 'ModelObjectException' not thrown." );
        }
        catch ( final ModelObjectException e )
        {
            assertNotNull( e.getMessage() );
            System.out.println( e.toString() );
        }

        i.setClazz( "java.lang.Object<java.lang.Object<java.lang.Object<?>>>" );
        assertEquals( "java.lang.Object", i.getJavaTypeName().getClassName() );
    }

    @Test
    public final void JavaFactoryMethodName() throws Exception
    {
        final Instance i = new Instance();
        assertNull( i.getJavaFactoryMethodName() );

        i.setName( "" );

        try
        {
            i.getJavaFactoryMethodName();
            fail( "Expected 'ModelObjectException' not thrown." );
        }
        catch ( final ModelObjectException e )
        {
            assertNotNull( e.getMessage() );
            System.out.println( e.toString() );
        }

        i.setName( "TEST TEST" );
        assertEquals( "getTestTest", i.getJavaFactoryMethodName() );
    }

}
