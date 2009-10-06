// SECTION-START[License Header]
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
// SECTION-END
package org.jomc.model.test;

import java.util.List;
import java.util.logging.Level;
import javax.xml.transform.Transformer;
import junit.framework.Assert;
import org.jomc.model.DefaultModelManager;
import org.jomc.model.Implementation;
import org.jomc.model.Implementations;
import org.jomc.model.ModelException;
import org.jomc.model.Module;
import org.jomc.model.Modules;
import org.jomc.model.SpecificationReference;
import org.jomc.model.Specifications;

/**
 * Test cases for class {@code org.jomc.model.DefaultModelManager}.
 *
 * @author <a href="mailto:cs@jomc.org">Christian Schulte</a> 1.0
 * @version $Id$
 */
public class DefaultModelManagerTest extends ModelManagerTest
{

    /** The instance tests are performed with. */
    private DefaultModelManager modelManager;

    /**
     * Gets the {@code ModelManager} instance to test.
     *
     * @return The {@code ModelManager} instance to test.
     */
    @Override
    public DefaultModelManager getModelManager()
    {
        if ( this.modelManager == null )
        {
            final DefaultModelManager defaultModelManager = new DefaultModelManager();
            defaultModelManager.getListeners().add( new DefaultModelManager.Listener()
            {

                public void onLog( final Level level, final String message, final Throwable t )
                {
                    if ( message != null )
                    {
                        System.out.println( "[" + level.getLocalizedName() + "] " + message );
                    }
                    if ( t != null )
                    {
                        t.printStackTrace();
                    }
                }

            } );

            this.modelManager = defaultModelManager;
        }

        return this.modelManager;
    }

    public void testClasspathResolution() throws Exception
    {
        try
        {
            final Modules modules = new Modules();
            final Module module = new Module();
            final Implementations implementations = new Implementations();
            final Implementation implementation = new Implementation();
            final Specifications specifications = new Specifications();
            final SpecificationReference ref = new SpecificationReference();

            modules.getModule().add( module );
            module.setImplementations( implementations );
            implementations.getImplementation().add( implementation );
            implementation.setSpecifications( specifications );
            specifications.getReference().add( ref );

            module.setName( "Test" );
            implementation.setIdentifier( "Implementation" );
            implementation.setName( "Implementation" );
            implementation.setClazz( "Implementation" );
            ref.setIdentifier( "java.util.Locale" );

            final Module classpathModule = this.getModelManager().getClasspathModule( modules );
            Assert.assertNotNull( classpathModule );
            Assert.assertNotNull( classpathModule.getSpecifications() );
            Assert.assertNotNull( classpathModule.getSpecifications().getSpecification( "java.util.Locale" ) );
            Assert.assertNotNull( classpathModule.getImplementations() );
            Assert.assertNotNull( classpathModule.getImplementations().getImplementation( "java.util.Locale" ) );

            modules.getModule().add( classpathModule );
            this.getModelManager().validateModules( modules );
        }
        catch ( ModelException e )
        {
            System.out.println( e.toString() );

            for ( ModelException.Detail d : e.getDetails() )
            {
                System.out.println( "\t" + d.toString() );
            }

            throw e;
        }
    }

    public void testClasspathTransformers() throws Exception
    {
        final List<Transformer> transformers =
            this.getModelManager().getClasspathTransformers( this.getModelManager().getDefaultStylesheetLocation() );

        Assert.assertNotNull( transformers );
        Assert.assertFalse( transformers.isEmpty() );

        Modules modules = new Modules();
        for ( Transformer t : transformers )
        {
            modules = this.getModelManager().transformModelObject(
                this.getModelManager().getObjectFactory().createModules( modules ), t );

        }
    }

}
