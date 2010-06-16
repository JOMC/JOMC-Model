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
package org.jomc.model.modlet.test;

import org.jomc.model.modlet.Modlet;
import org.jomc.model.modlet.ModletContext;
import org.jomc.model.modlet.ModletException;
import org.jomc.model.modlet.ModletProvider;
import org.jomc.model.modlet.Modlets;
import org.jomc.model.modlet.Schema;
import org.jomc.model.modlet.Schemas;
import org.jomc.model.modlet.Service;
import org.jomc.model.modlet.Services;

/**
 * {@code ModletProvider} test implementation.
 *
 * @author <a href="mailto:schulte2005@users.sourceforge.net">Christian Schulte</a> 1.0
 * @version $Id$
 */
public class TestModletProvider implements ModletProvider
{

    public TestModletProvider()
    {
        super();
    }

    public Modlets findModlets( final ModletContext context ) throws ModletException
    {
        final Modlets modlets = new Modlets();
        final Modlet modlet = new Modlet();
        final Schemas schemas = new Schemas();
        final Schema schema = new Schema();
        final Services services = new Services();
        final Service service = new Service();
        schemas.getSchema().add( schema );
        services.getService().add( service );
        modlets.getModlet().add( modlet );
        modlet.setSchemas( schemas );
        modlet.setServices( services );

        schema.setPublicId( "http://jomc.org/model/empty" );
        schema.setSystemId( "http://jomc.org/model/empty/empty.xsd" );
        schema.setClasspathId( "org/jomc/model/modlet/test/empty.xsd" );

        service.setOrdinal( Integer.MAX_VALUE );
        service.setClazz( "java.lang.Object" );
        service.setIdentifier( "java.lang.Object" );

        modlet.setIdentifier( "TestModletProvider" );
        modlet.setName( "TestModletProvider" );
        modlet.setModel( "http://jomc.org/model/empty" );

        return modlets;
    }

}
