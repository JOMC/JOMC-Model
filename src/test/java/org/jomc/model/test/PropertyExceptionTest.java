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

import org.junit.Test;
import java.io.ObjectInputStream;
import org.jomc.model.PropertyException;
import static org.junit.Assert.assertEquals;

/**
 * Test cases for class {@code org.jomc.model.PropertyException}.
 *
 * @author <a href="mailto:schulte2005@users.sourceforge.net">Christian Schulte</a> 1.0
 * @version $Id$
 */
public class PropertyExceptionTest
{

    /** Creates a new {@code PropertyExceptionTest} instance. */
    public PropertyExceptionTest()
    {
        super();
    }

    @Test
    public final void testSerializabe() throws Exception
    {
        final ObjectInputStream objectStream =
            new ObjectInputStream( this.getClass().getResourceAsStream( "PropertyException.ser" ) );

        final PropertyException e = (PropertyException) objectStream.readObject();
        objectStream.close();

        assertEquals( "PropertyException", e.getMessage() );
        System.out.println( e );
    }

}
