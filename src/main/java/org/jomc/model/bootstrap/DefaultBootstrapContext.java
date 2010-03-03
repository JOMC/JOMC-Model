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
package org.jomc.model.bootstrap;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.validation.SchemaFactory;
import org.xml.sax.SAXException;

/**
 * Default {@code BootstrapContext} implementation.
 *
 * @author <a href="mailto:schulte2005@users.sourceforge.net">Christian Schulte</a>
 * @version $Id$
 * @see BootstrapContext#createBootstrapContext(java.lang.ClassLoader)
 */
public class DefaultBootstrapContext extends BootstrapContext
{

    /**
     * Creates a new {@code DefaultBootstrapContext} instance taking a class loader.
     *
     * @param classLoader The class loader of the context.
     */
    public DefaultBootstrapContext( final ClassLoader classLoader )
    {
        super( classLoader );
    }

    @Override
    public javax.xml.validation.Schema createSchema() throws BootstrapException
    {
        try
        {
            return SchemaFactory.newInstance( XMLConstants.W3C_XML_SCHEMA_NS_URI ).
                newSchema( this.getClass().getResource( "jomc-bootstrap-1.0.xsd" ) );

        }
        catch ( final SAXException e )
        {
            throw new BootstrapException( e.getMessage(), e );
        }
    }

    @Override
    public JAXBContext createContext() throws BootstrapException
    {
        try
        {
            return JAXBContext.newInstance( Schemas.class.getPackage().getName(), this.getClassLoader() );
        }
        catch ( final JAXBException e )
        {
            throw new BootstrapException( e.getMessage(), e );
        }
    }

    @Override
    public Marshaller createMarshaller() throws BootstrapException
    {
        try
        {
            final Marshaller m = this.createContext().createMarshaller();
            m.setProperty( Marshaller.JAXB_SCHEMA_LOCATION,
                           "http://jomc.org/model/bootstrap http://jomc.org/model/bootstrap/jomc-bootstrap-1.0.xsd" );

            return m;
        }
        catch ( final JAXBException e )
        {
            throw new BootstrapException( e.getMessage(), e );
        }
    }

    @Override
    public Unmarshaller createUnmarshaller() throws BootstrapException
    {
        try
        {
            return this.createContext().createUnmarshaller();
        }
        catch ( final JAXBException e )
        {
            throw new BootstrapException( e.getMessage(), e );
        }
    }

    // SECTION-END
}
