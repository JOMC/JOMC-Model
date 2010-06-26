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
package org.jomc.model.modlet;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;
import javax.xml.bind.JAXBElement;
import org.jomc.model.ModelObject;
import org.jomc.model.Modules;
import org.jomc.model.ObjectFactory;
import org.jomc.modlet.Model;

/**
 * Object management and configuration {@code Model} helper.
 *
 * @author <a href="mailto:schulte2005@users.sourceforge.net">Christian Schulte</a>
 * @version $Id$
 */
public abstract class ModelHelper
{

    /** Creates a new {@code ModelHelper} instance. */
    public ModelHelper()
    {
        super();
    }

    /**
     * Gets the {@code Modules} of a {@code Model}.
     *
     * @param model The {@code Model} to get {@code Modules} of.
     *
     * @return The {@code Modules} of {@code Model} or {@code null}.
     *
     * @throws NullPointerException if {@code model} is {@code null}.
     */
    public static Modules getModules( final Model model )
    {
        if ( model == null )
        {
            throw new NullPointerException( "model" );
        }

        final JAXBElement<Modules> e = model.getAnyElement( ModelObject.MODEL_PUBLIC_ID, "modules" );
        return e != null ? e.getValue() : null;
    }

    /**
     * Sets the {@code Modules} of a {@code Model}.
     *
     * @param model The {@code Model} to set {@code modules} of.
     * @param modules The {@code Modules} to set.
     *
     * @throws NullPointerException if {@code model} or {@code modules} is {@code null}.
     * @throws IllegalStateException if {@code model} already holds {@code Modules}.
     *
     * @see #addModules(org.jomc.modlet.Model, org.jomc.model.Modules)
     */
    public static void setModules( final Model model, final Modules modules )
    {
        if ( model == null )
        {
            throw new NullPointerException( "model" );
        }
        if ( modules == null )
        {
            throw new NullPointerException( "modules" );
        }
        if ( getModules( model ) != null )
        {
            throw new IllegalStateException( getMessage( "illegalState", model.getIdentifier() ) );
        }

        model.getAny().add( new ObjectFactory().createModules( modules ) );
    }

    /**
     * Adds {@code Modules} to a {@code Model}.
     *
     * @param model The {@code Model} to a {@code modules} to.
     * @param modules The {@code Modules} to add to {@code model}.
     *
     * @throws NullPointerException if {@code model} or {@code modules} is {@code null}.
     *
     * @see #removeModules(org.jomc.modlet.Model)
     */
    public static void addModules( final Model model, final Modules modules )
    {
        if ( model == null )
        {
            throw new NullPointerException( "model" );
        }
        if ( modules == null )
        {
            throw new NullPointerException( "modules" );
        }

        final Modules current = getModules( model );

        if ( current != null )
        {
            current.getModule().addAll( modules.getModule() );
        }
        else
        {
            setModules( model, modules );
        }
    }

    /**
     * Removes {@code Modules} from a {@code Model}.
     *
     * @param model The {@code Model} to remove {@code modules} from.
     *
     * @throws NullPointerException if {@code model} is {@code null}.
     *
     * @see #addModules(org.jomc.modlet.Model, org.jomc.model.Modules)
     */
    public static void removeModules( final Model model )
    {
        if ( model == null )
        {
            throw new NullPointerException( "model" );
        }

        final JAXBElement<Modules> modules = model.getAnyElement( ModelObject.MODEL_PUBLIC_ID, "modules" );

        if ( modules != null )
        {
            model.getAny().remove( modules );
        }
    }

    private static String getMessage( final String key, final Object... args )
    {
        return MessageFormat.format( ResourceBundle.getBundle(
            ModelHelper.class.getName().replace( '.', '/' ), Locale.getDefault() ).getString( key ), args );

    }

}
