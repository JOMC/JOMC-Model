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
package org.jomc.model.test;

import java.lang.reflect.InvocationTargetException;
import org.jomc.model.ModelObjectException;
import org.jomc.model.Property;
import org.jomc.model.PropertyException;
import org.jomc.util.JavaIdentifier;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

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

    /** Creates a new {@code PropertyTest} instance. */
    public PropertyTest()
    {
        super();
    }

    @Test
    public final void testGetJavaValue() throws Exception
    {
        final Property p = new Property();
        assertNull( p.getJavaValue( this.getClass().getClassLoader() ) );

        p.setAny( new Object() );

        try
        {
            p.getJavaValue( this.getClass().getClassLoader() );
            fail( "Expected PropertyException not thrown for missing mandatory type." );
        }
        catch ( final PropertyException e )
        {
            assertNotNull( e.getMessage() );
            System.out.println( e );
        }

        p.setType( UnsupportedJavaValue.class.getName() );
        p.setAny( new UnsupportedJavaValue() );

        try
        {
            p.getJavaValue( this.getClass().getClassLoader() );
            fail( "Expected PropertyException not thrown for unsupported getJavaValue operation." );
        }
        catch ( final PropertyException e )
        {
            assertNotNull( e.getMessage() );
            assertTrue( e.getCause() instanceof InvocationTargetException );
            System.out.println( e );
            System.out.println( e.getCause() );
        }

        p.setType( Object.class.getName() );
        p.setAny( new Object()
        {

            public Object getJavaValue( final ClassLoader classLoader )
            {
                return new Object();
            }

        } );

        try
        {
            p.getJavaValue( this.getClass().getClassLoader() );
            fail( "Expected PropertyException not thrown for inaccessible getJavaValue method." );
        }
        catch ( final PropertyException e )
        {
            assertNotNull( e.getMessage() );
            System.out.println( e );
            System.out.println( e.getCause() );
        }

        p.setType( "java.lang.String" );
        p.setAny( new ObjectJavaValue() );

        try
        {
            p.getJavaValue( this.getClass().getClassLoader() );
            fail( "Expected PropertyException not thrown for incompatible getJavaValue method." );
        }
        catch ( final PropertyException e )
        {
            assertNotNull( e.getMessage() );
            System.out.println( e );
        }

        p.setType( "int" );
        p.setAny( null );
        p.setValue( null );

        try
        {
            p.getJavaValue( this.getClass().getClassLoader() );
            fail( "Expected PropertyException not thrown for mandatory primitive value." );
        }
        catch ( final PropertyException e )
        {
            assertNotNull( e.getMessage() );
            System.out.println( e );
        }

        p.setType( "DOES_NOT_EXIST" );
        p.setAny( null );
        p.setValue( "STRING VALUE" );

        try
        {
            p.getJavaValue( this.getClass().getClassLoader() );
            fail( "Expected PropertyException not thrown for missing class." );
        }
        catch ( final PropertyException e )
        {
            assertNotNull( e.getMessage() );
            System.out.println( e );
        }

        p.setType( "char" );
        p.setValue( "NO CHAR VALUE" );

        try
        {
            p.getJavaValue( this.getClass().getClassLoader() );
            fail( "Expected PropertyException not thrown for illegal char value." );
        }
        catch ( final PropertyException e )
        {
            assertNotNull( e.getMessage() );
            System.out.println( e );
        }

        p.setType( AbstractJavaValue.class.getName() );
        p.setAny( null );
        p.setValue( "STRING VALUE" );

        try
        {
            p.getJavaValue( this.getClass().getClassLoader() );
            fail( "Expected PropertyException not thrown for non-instantiable class." );
        }
        catch ( final PropertyException e )
        {
            assertNotNull( e.getMessage() );
            System.out.println( e );
        }

        p.setType( ObjectJavaValue.class.getName() );
        p.setAny( null );
        p.setValue( "STRING VALUE" );

        try
        {
            p.getJavaValue( this.getClass().getClassLoader() );
            fail( "Expected PropertyException not thrown for missing constructor." );
        }
        catch ( final PropertyException e )
        {
            assertNotNull( e.getMessage() );
            System.out.println( e );
        }

        p.setType( UnsupportedJavaValue.class.getName() );
        p.setAny( null );
        p.setValue( "STRING VALUE" );

        try
        {
            p.getJavaValue( this.getClass().getClassLoader() );
            fail( "Expected PropertyException not thrown for unsupported constructor." );
        }
        catch ( final PropertyException e )
        {
            assertNotNull( e.getMessage() );
            System.out.println( e );
        }

        p.setType( InaccessibleJavaValue.class.getName() );
        p.setAny( null );
        p.setValue( "STRING VALUE" );

        try
        {
            p.getJavaValue( this.getClass().getClassLoader() );
            fail( "Expected PropertyException not thrown for inaccessible constructor." );
        }
        catch ( final PropertyException e )
        {
            assertNotNull( e.getMessage() );
            System.out.println( e );
        }

        // Since 1.5
        p.setType( Thread.State.class.getName() );
        p.setAny( null );
        p.setValue( "RUNNABLE" );
        assertEquals( Thread.State.RUNNABLE, p.getJavaValue( this.getClass().getClassLoader() ) );

        p.setType( FactoryMethodTestClass.class.getName() );
        p.setAny( null );
        p.setValue( "TEST" );

        try
        {
            p.getJavaValue( this.getClass().getClassLoader() );
            fail( "Expected PropertyException not thrown for illegal factory method." );
        }
        catch ( final PropertyException e )
        {
            assertNotNull( e.getMessage() );
            System.out.println( e );
        }
    }

    @Test
    public final void JavaConstantName() throws Exception
    {
        final Property p = new Property();

        try
        {
            p.getJavaConstantName();
            fail( "Expected 'ModelObjectException' not thrown." );
        }
        catch ( final ModelObjectException e )
        {
            assertNotNull( e.getMessage() );
            System.out.println( e.toString() );
        }

        p.setName( "test test" );
        assertEquals( JavaIdentifier.valueOf( "TEST_TEST" ), p.getJavaConstantName() );
    }

    @Test
    public final void JavaGetterMethodName() throws Exception
    {
        final Property p = new Property();

        try
        {
            p.getJavaGetterMethodName();
            fail( "Expected 'ModelObjectException' not thrown." );
        }
        catch ( final ModelObjectException e )
        {
            assertNotNull( e.getMessage() );
            System.out.println( e.toString() );
        }

        p.setName( "TEST TEST" );
        assertEquals( JavaIdentifier.valueOf( "getTestTest" ), p.getJavaGetterMethodName() );
    }

    @Test
    public final void JavaSetterMethodName() throws Exception
    {
        final Property p = new Property();

        try
        {
            p.getJavaSetterMethodName();
            fail( "Expected 'ModelObjectException' not thrown." );
        }
        catch ( final ModelObjectException e )
        {
            assertNotNull( e.getMessage() );
            System.out.println( e.toString() );
        }

        p.setName( "TEST TEST" );
        assertEquals( JavaIdentifier.valueOf( "setTestTest" ), p.getJavaSetterMethodName() );
    }

    @Test
    public final void JavaVariableName() throws Exception
    {
        final Property p = new Property();

        try
        {
            p.getJavaVariableName();
            fail( "Expected 'ModelObjectException' not thrown." );
        }
        catch ( final ModelObjectException e )
        {
            assertNotNull( e.getMessage() );
            System.out.println( e.toString() );
        }

        p.setName( "TEST TEST" );
        assertEquals( JavaIdentifier.valueOf( "testTest" ), p.getJavaVariableName() );
    }

}
