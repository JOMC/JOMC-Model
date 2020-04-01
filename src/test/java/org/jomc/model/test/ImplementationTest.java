/*
 *   Copyright (C) Christian Schulte <cs@schulte.it>, 2012-353
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

import java.net.URI;
import org.jomc.model.Implementation;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

/**
 * Test cases for class {@code org.jomc.model.Implementation}.
 *
 * @author <a href="mailto:cs@schulte.it">Christian Schulte</a>
 * @version $JOMC$
 * @since 1.4
 */
public class ImplementationTest
{

    /**
     * Creates a new {@code ImplementationTest} instance.
     */
    public ImplementationTest()
    {
        super();
    }

    @Test
    public final void JavaTypeName() throws Exception
    {
        final Implementation i = new Implementation();
        assertNotNull( i.getJavaTypeName() );
        assertFalse( i.getJavaTypeName().isPresent() );

        i.setClazz( "@" );
        ModelObjectTest.assertModelObjectException( ()  -> i.getJavaTypeName() );

        i.setClazz( "java.lang.Object<java.lang.Object<java.lang.Object<?>>>" );
        assertEquals( "java.lang.Object", i.getJavaTypeName().get().getClassName() );
    }

    @Test
    public final void LocationUri() throws Exception
    {
        final Implementation i = new Implementation();
        assertNotNull( i.getLocationUri() );
        assertFalse( i.getLocationUri().isPresent() );

        i.setLocation( "://" );
        ModelObjectTest.assertModelObjectException( ()  -> i.getLocationUri() );

        i.setLocation( "test:test" );
        assertEquals( new URI( "test:test" ), i.getLocationUri().get() );
    }

}
