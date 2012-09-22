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

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;
import org.jomc.model.ModelObject;
import org.junit.Test;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

/**
 * Test cases for class {@code org.jomc.model.ModelObject}.
 *
 * @author <a href="mailto:cs@schulte.it">Christian Schulte</a> 1.0
 * @version $JOMC$
 */
public class ModelObjectTest
{

    /** Creates a new {@code ModelObjectTest} instance. */
    public ModelObjectTest()
    {
        super();
    }

    /** Test {@code ModelObject}. */
    public static class TestModelObject extends ModelObject
    {

        @Override
        @SuppressWarnings( "deprecation" )
        public JAXBElement getAnyElement( final List<Object> any, final String namespaceURI, final String localPart )
        {
            return super.getAnyElement( any, namespaceURI, localPart );
        }

        @Override
        public <T> JAXBElement<T> getAnyElement( final List<Object> any, final String namespaceURI,
                                                 final String localPart, final Class<T> type )
        {
            return super.getAnyElement( any, namespaceURI, localPart, type );
        }

        @Override
        public <T> T getAnyObject( final List<Object> any, final Class<T> clazz )
        {
            return super.getAnyObject( any, clazz );
        }

    }

    @Test
    public final void testGetAnyElement() throws Exception
    {
        final TestModelObject modelObject = new TestModelObject();
        final List<Object> any = new ArrayList<Object>( 10 );
        final QName name = new QName( "http://jomc.org/model", "test" );
        final JAXBElement<Object> element = new JAXBElement<Object>( name, Object.class, null, null );
        any.add( element );
        any.add( element );

        try
        {
            modelObject.getAnyElement( any, "http://jomc.org/model", "test" );
            fail( "Expected 'IllegalStateException' not thrown." );
        }
        catch ( final IllegalStateException e )
        {
            assertNotNull( e.getMessage() );
            System.out.println( e );
        }

        try
        {
            modelObject.getAnyElement( any, "http://jomc.org/model", "test", Object.class );
            fail( "Expected 'IllegalStateException' not thrown." );
        }
        catch ( final IllegalStateException e )
        {
            assertNotNull( e.getMessage() );
            System.out.println( e );
        }
    }

    @Test
    public final void testGetAnyObject() throws Exception
    {
        final TestModelObject modelObject = new TestModelObject();
        final List<Object> any = new ArrayList<Object>( 10 );
        any.add( "TEST" );
        any.add( "TEST" );

        try
        {
            modelObject.getAnyObject( any, String.class );
            fail( "Expected 'IllegalStateException' not thrown." );
        }
        catch ( final IllegalStateException e )
        {
            assertNotNull( e.getMessage() );
            System.out.println( e );
        }
    }

}
