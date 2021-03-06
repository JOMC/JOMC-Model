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
package org.jomc.model.test;

import org.jomc.jls.JavaIdentifier;
import org.jomc.model.Property;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

/**
 * Test cases for class {@code org.jomc.model.Property}.
 *
 * @author <a href="mailto:cs@schulte.it">Christian Schulte</a> 1.0
 * @version $JOMC$
 */
public class PropertyTest
{

    public abstract static class AbstractJavaValue
    {

        public AbstractJavaValue( final String value )
        {
            super();
        }

    }

    public static class UnsupportedJavaValue
    {

        public UnsupportedJavaValue()
        {
            super();
        }

        public UnsupportedJavaValue( final String value )
        {
            super();
            throw new UnsupportedOperationException();
        }

        public Object getJavaValue( final ClassLoader classLoader )
        {
            throw new UnsupportedOperationException();
        }

    }

    public static class ObjectJavaValue
    {

        public Object getJavaValue( final ClassLoader classLoader )
        {
            return new Object();
        }

    }

    private static class InaccessibleJavaValue
    {

        private InaccessibleJavaValue( final String value )
        {
            super();
        }

        private Object getJavaValue( final ClassLoader classLoader )
        {
            return null;
        }

    }

    private static class FactoryMethodTestClass
    {

        public static Object valueOf( final String string )
        {
            return null;
        }

    }

    /**
     * Creates a new {@code PropertyTest} instance.
     */
    public PropertyTest()
    {
        super();
    }

    @Test
    public final void testGetJavaValue() throws Exception
    {
        final Property p = new Property();
        assertNotNull( p.getJavaValue( this.getClass().getClassLoader() ) );
        assertFalse( p.getJavaValue( this.getClass().getClassLoader() ).isPresent() );

        p.setAny( new Object() );
        ModelObjectTest.assertPropertyException( ()  -> p.getJavaValue( this.getClass().getClassLoader() ) );

        p.setType( UnsupportedJavaValue.class.getName() );
        p.setAny( new UnsupportedJavaValue() );
        ModelObjectTest.assertPropertyException( ()  -> p.getJavaValue( this.getClass().getClassLoader() ) );

        p.setType( Object.class.getName() );
        p.setAny( new Object()
        {

            public Object getJavaValue( final ClassLoader classLoader )
            {
                return new Object();
            }

        } );
        ModelObjectTest.assertPropertyException( ()  -> p.getJavaValue( this.getClass().getClassLoader() ) );

        p.setType( "java.lang.String" );
        p.setAny( new ObjectJavaValue() );
        ModelObjectTest.assertPropertyException( ()  -> p.getJavaValue( this.getClass().getClassLoader() ) );

        p.setType( "int" );
        p.setAny( null );
        p.setValue( null );
        ModelObjectTest.assertPropertyException( ()  -> p.getJavaValue( this.getClass().getClassLoader() ) );

        p.setType( "DOES_NOT_EXIST" );
        p.setAny( null );
        p.setValue( "STRING VALUE" );
        ModelObjectTest.assertPropertyException( ()  -> p.getJavaValue( this.getClass().getClassLoader() ) );

        p.setType( "char" );
        p.setValue( "NO CHAR VALUE" );
        ModelObjectTest.assertPropertyException( ()  -> p.getJavaValue( this.getClass().getClassLoader() ) );

        p.setType( AbstractJavaValue.class.getName() );
        p.setAny( null );
        p.setValue( "STRING VALUE" );
        ModelObjectTest.assertPropertyException( ()  -> p.getJavaValue( this.getClass().getClassLoader() ) );

        p.setType( ObjectJavaValue.class.getName() );
        p.setAny( null );
        p.setValue( "STRING VALUE" );
        ModelObjectTest.assertPropertyException( ()  -> p.getJavaValue( this.getClass().getClassLoader() ) );

        p.setType( UnsupportedJavaValue.class.getName() );
        p.setAny( null );
        p.setValue( "STRING VALUE" );
        ModelObjectTest.assertPropertyException( ()  -> p.getJavaValue( this.getClass().getClassLoader() ) );

        p.setType( InaccessibleJavaValue.class.getName() );
        p.setAny( null );
        p.setValue( "STRING VALUE" );
        ModelObjectTest.assertPropertyException( ()  -> p.getJavaValue( this.getClass().getClassLoader() ) );

        // Since 1.5
        p.setType( Thread.State.class.getName() );
        p.setAny( null );
        p.setValue( "RUNNABLE" );
        assertEquals( Thread.State.RUNNABLE, p.getJavaValue( this.getClass().getClassLoader() ).get() );

        p.setType( FactoryMethodTestClass.class.getName() );
        p.setAny( null );
        p.setValue( "TEST" );
        ModelObjectTest.assertPropertyException( ()  -> p.getJavaValue( this.getClass().getClassLoader() ) );
    }

    @Test
    public final void JavaConstantName() throws Exception
    {
        final Property p = new Property();
        ModelObjectTest.assertModelObjectException( ()  -> p.getJavaConstantName() );

        p.setName( "test test" );
        assertEquals( JavaIdentifier.valueOf( "TEST_TEST" ), p.getJavaConstantName() );
    }

    @Test
    public final void JavaGetterMethodName() throws Exception
    {
        final Property p = new Property();
        ModelObjectTest.assertModelObjectException( ()  -> p.getJavaGetterMethodName() );

        p.setName( "TEST TEST" );
        assertEquals( JavaIdentifier.valueOf( "getTestTest" ), p.getJavaGetterMethodName() );
    }

    @Test
    public final void JavaSetterMethodName() throws Exception
    {
        final Property p = new Property();
        ModelObjectTest.assertModelObjectException( ()  -> p.getJavaSetterMethodName() );

        p.setName( "TEST TEST" );
        assertEquals( JavaIdentifier.valueOf( "setTestTest" ), p.getJavaSetterMethodName() );
    }

    @Test
    public final void JavaVariableName() throws Exception
    {
        final Property p = new Property();
        ModelObjectTest.assertModelObjectException( ()  -> p.getJavaVariableName() );

        p.setName( "TEST TEST" );
        assertEquals( JavaIdentifier.valueOf( "testTest" ), p.getJavaVariableName() );
    }

}
