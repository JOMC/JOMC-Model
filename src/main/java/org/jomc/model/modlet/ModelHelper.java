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
package org.jomc.model.modlet;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;
import javax.xml.bind.JAXBElement;
import org.jomc.model.ModelObject;
import org.jomc.model.Modules;
import org.jomc.model.ObjectFactory;
import org.jomc.modlet.Model;

/**
 * Object management and configuration {@code Model} helper.
 *
 * @author <a href="mailto:cs@schulte.it">Christian Schulte</a>
 * @version $JOMC$
 */
public abstract class ModelHelper
{

    /**
     * Creates a new {@code ModelHelper} instance.
     */
    public ModelHelper()
    {
        super();
    }

    /**
     * Gets the {@code Modules} of a {@code Model}.
     *
     * @param model The {@code Model} to get {@code Modules} of.
     *
     * @return The {@code Modules} of {@code Model} or no value, if no {@code Modules} are found.
     *
     * @throws NullPointerException if {@code model} is {@code null}.
     *
     * @see #addModules(org.jomc.modlet.Model, org.jomc.model.Modules)
     * @see #setModules(org.jomc.modlet.Model, org.jomc.model.Modules)
     */
    public static Optional<Modules> getModules( final Model model )
    {
        final Optional<JAXBElement<Modules>> e =
            Objects.requireNonNull( model, "model" ).getAnyElement( ModelObject.MODEL_PUBLIC_ID, "modules",
                                                                    Modules.class );

        return e.isPresent()
                   ? Optional.of( e.get().getValue() )
                   : Optional.empty();

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
     * @see #removeModules(org.jomc.modlet.Model)
     */
    public static void setModules( final Model model, final Modules modules )
    {
        if ( getModules( Objects.requireNonNull( model, "model" ) ).isPresent() )
        {
            throw new IllegalStateException( getMessage( "illegalState", model.getIdentifier() ) );
        }

        model.getAny().add( new ObjectFactory().createModules( Objects.requireNonNull( modules, "modules" ) ) );
    }

    /**
     * Adds {@code Modules} to a {@code Model}.
     *
     * @param model The {@code Model} to add {@code modules} to.
     * @param modules The {@code Modules} to add to {@code model}.
     *
     * @throws NullPointerException if {@code model} or {@code modules} is {@code null}.
     *
     * @see #removeModules(org.jomc.modlet.Model)
     * @see #setModules(org.jomc.modlet.Model, org.jomc.model.Modules)
     */
    public static void addModules( final Model model, final Modules modules )
    {
        Objects.requireNonNull( model, "model" );
        Objects.requireNonNull( modules, "modules" );

        final Optional<Modules> current = getModules( model );

        if ( current.isPresent() )
        {
            current.get().getModule().addAll( modules.getModule() );
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
     * @see #setModules(org.jomc.modlet.Model, org.jomc.model.Modules)
     */
    public static void removeModules( final Model model )
    {
        Objects.requireNonNull( model, "model" );
        final Optional<JAXBElement<Modules>> e =
            model.getAnyElement( ModelObject.MODEL_PUBLIC_ID, "modules", Modules.class );

        if ( e.isPresent() )
        {
            model.getAny().remove( e.get() );
        }
    }

    private static String getMessage( final String key, final Object... args )
    {
        return MessageFormat.format( ResourceBundle.getBundle(
            ModelHelper.class.getName().replace( '.', '/' ), Locale.getDefault() ).getString( key ), args );

    }

}
