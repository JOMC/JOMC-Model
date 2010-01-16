/*
 *   Copyright (c) 2009 The JOMC Project
 *   Copyright (c) 2005 Christian Schulte <cs@jomc.org>
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

import java.lang.reflect.InvocationTargetException;
import junit.framework.Assert;
import org.jomc.model.ModelException;
import org.jomc.model.Property;

/**
 * Test cases for class {@code org.jomc.model.Property}.
 *
 * @author <a href="mailto:cs@jomc.org">Christian Schulte</a> 1.0
 * @version $Id$
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

        public Object getJavaValue()
        {
            throw new UnsupportedOperationException();
        }

    }

    public static class ObjectJavaValue
    {

        public Object getJavaValue()
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

        private Object getJavaValue()
        {
            return null;
        }

    }

    public PropertyTest()
    {
        super();
    }

    public void testGetJavaValue() throws Exception
    {
        final Property p = new Property();
        p.setAny( new Object() );

        try
        {
            p.getJavaValue();
            Assert.fail( "Expected ModelException not thrown for missing mandatory type." );
        }
        catch ( final ModelException e )
        {
            Assert.assertNotNull( e.getMessage() );
            System.out.println( e );
        }

        p.setType( UnsupportedJavaValue.class.getName() );
        p.setAny( new UnsupportedJavaValue() );

        try
        {
            p.getJavaValue();
            Assert.fail( "Expected ModelException not thrown for unsupported getJavaValue operation." );
        }
        catch ( final ModelException e )
        {
            Assert.assertNotNull( e.getMessage() );
            Assert.assertTrue( e.getCause() instanceof InvocationTargetException );
            System.out.println( e );
            System.out.println( e.getCause() );
        }

        p.setType( Object.class.getName() );
        p.setAny( new Object()
        {

            public Object getJavaValue()
            {
                return new Object();
            }

        } );

        try
        {
            p.getJavaValue();
            Assert.fail( "Expected ModelException not thrown for inaccessible getJavaValue method." );
        }
        catch ( final ModelException e )
        {
            Assert.assertNotNull( e.getMessage() );
            System.out.println( e );
            System.out.println( e.getCause() );
        }

        p.setType( "java.lang.String" );
        p.setAny( new ObjectJavaValue() );

        try
        {
            p.getJavaValue();
            Assert.fail( "Expected ModelException not thrown for incompatible getJavaValue method." );
        }
        catch ( final ModelException e )
        {
            Assert.assertNotNull( e.getMessage() );
            System.out.println( e );
        }

        p.setType( "int" );
        p.setAny( null );
        p.setValue( null );

        try
        {
            p.getJavaValue();
            Assert.fail( "Expected ModelException not thrown for mandatory primitive value." );
        }
        catch ( final ModelException e )
        {
            Assert.assertNotNull( e.getMessage() );
            System.out.println( e );
        }

        p.setType( "DOES_NOT_EXIST" );
        p.setAny( null );
        p.setValue( "STRING VALUE" );

        try
        {
            p.getJavaValue();
            Assert.fail( "Expected ModelException not thrown for missing class." );
        }
        catch ( final ModelException e )
        {
            Assert.assertNotNull( e.getMessage() );
            System.out.println( e );
        }

        p.setType( "char" );
        p.setValue( "NO CHAR VALUE" );

        try
        {
            p.getJavaValue();
            Assert.fail( "Expected ModelException not thrown for illegal char value." );
        }
        catch ( final ModelException e )
        {
            Assert.assertNotNull( e.getMessage() );
            System.out.println( e );
        }

        p.setType( AbstractJavaValue.class.getName() );
        p.setAny( null );
        p.setValue( "STRING VALUE" );

        try
        {
            p.getJavaValue();
            Assert.fail( "Expected ModelException not thrown for non-instantiable class." );
        }
        catch ( final ModelException e )
        {
            Assert.assertNotNull( e.getMessage() );
            System.out.println( e );
        }

        p.setType( ObjectJavaValue.class.getName() );
        p.setAny( null );
        p.setValue( "STRING VALUE" );

        try
        {
            p.getJavaValue();
            Assert.fail( "Expected ModelException not thrown for missing constructor." );
        }
        catch ( final ModelException e )
        {
            Assert.assertNotNull( e.getMessage() );
            System.out.println( e );
        }

        p.setType( UnsupportedJavaValue.class.getName() );
        p.setAny( null );
        p.setValue( "STRING VALUE" );

        try
        {
            p.getJavaValue();
            Assert.fail( "Expected ModelException not thrown for unsupported constructor." );
        }
        catch ( final ModelException e )
        {
            Assert.assertNotNull( e.getMessage() );
            System.out.println( e );
        }

        p.setType( InaccessibleJavaValue.class.getName() );
        p.setAny( null );
        p.setValue( "STRING VALUE" );

        try
        {
            p.getJavaValue();
            Assert.fail( "Expected ModelException not thrown for inaccessible constructor." );
        }
        catch ( final ModelException e )
        {
            Assert.assertNotNull( e.getMessage() );
            System.out.println( e );
        }
    }

}
