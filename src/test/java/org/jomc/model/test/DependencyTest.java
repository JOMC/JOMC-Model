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

import org.jomc.jls.JavaIdentifier;
import org.jomc.model.Dependency;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 * Test cases for class {@code org.jomc.model.Dependency}.
 *
 * @author <a href="mailto:cs@schulte.it">Christian Schulte</a>
 * @version $JOMC$
 * @since 1.4
 */
public class DependencyTest
{

    /**
     * Creates a new {@code DependencyTest} instance.
     */
    public DependencyTest()
    {
        super();
    }

    @Test
    public final void JavaConstantName() throws Exception
    {
        final Dependency d = new Dependency();
        ModelObjectTest.assertModelObjectException( ()  -> d.getJavaConstantName() );

        d.setName( "test test" );
        assertEquals( JavaIdentifier.valueOf( "TEST_TEST" ), d.getJavaConstantName() );
    }

    @Test
    public final void JavaGetterMethodName() throws Exception
    {
        final Dependency d = new Dependency();
        ModelObjectTest.assertModelObjectException( ()  -> d.getJavaGetterMethodName() );

        d.setName( "TEST TEST" );
        assertEquals( JavaIdentifier.valueOf( "getTestTest" ), d.getJavaGetterMethodName() );
    }

    @Test
    public final void JavaSetterMethodName() throws Exception
    {
        final Dependency d = new Dependency();
        ModelObjectTest.assertModelObjectException( ()  -> d.getJavaSetterMethodName() );

        d.setName( "TEST TEST" );
        assertEquals( JavaIdentifier.valueOf( "setTestTest" ), d.getJavaSetterMethodName() );
    }

    @Test
    public final void JavaVariableName() throws Exception
    {
        final Dependency d = new Dependency();
        ModelObjectTest.assertModelObjectException( ()  -> d.getJavaVariableName() );

        d.setName( "TEST TEST" );
        assertEquals( JavaIdentifier.valueOf( "testTest" ), d.getJavaVariableName() );
    }

}
