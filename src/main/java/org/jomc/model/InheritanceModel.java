/*
 *   Copyright (C) Christian Schulte <cs@schulte.it>, 2011-325
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
package org.jomc.model;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;
import org.w3c.dom.Element;

/**
 * Inheritance model.
 *
 * @author <a href="mailto:cs@schulte.it">Christian Schulte</a>
 * @version $JOMC$
 * @since 1.2
 */
public class InheritanceModel
{

    /**
     * Inheritance model node.
     *
     * @param <T> The type of the model object of the node.
     *
     * @author <a href="mailto:cs@schulte.it">Christian Schulte</a>
     * @version $JOMC$
     * @since 1.2
     */
    public static class Node<T>
    {

        /**
         * The implementation the node originates from.
         */
        private final Implementation implementation;

        /**
         * The specification the node originates from.
         */
        private final Specification specification;

        /**
         * The class declaration the node originates from.
         */
        private final Implementation classDeclaration;

        /**
         * The direct descendant node.
         */
        private final Node<Implementation> descendant;

        /**
         * The model object of the node.
         */
        private final T modelObject;

        /**
         * Flag indicating the node is the final node in an inheritance hierarchy.
         */
        private final boolean _final;

        /**
         * Flag indicating the node is intended to override an ancestor node.
         */
        private final boolean override;

        /**
         * The path to the node.
         */
        private final LinkedList<Node<Implementation>> path = new LinkedList<Node<Implementation>>();

        /**
         * The nodes overridden by the node.
         */
        private final Set<Node<T>> overriddenNodes = new HashSet<Node<T>>();

        /**
         * Creates a new {@code Node} instance.
         *
         * @param implementation The implementation the node originates from.
         * @param specification The specification the node originates from or {@code null}.
         * @param classDeclaration The class declaration the node originates from or {@code null}.
         * @param descendant The direct descendant node of the node or {@code null}.
         * @param modelObject The model object of the node.
         * @param finalNode {@code true}, if the node is the final node in an inheritance hierarchy; {@code false},
         * else.
         * @param overrideNode {@code true}, if the node is intended to override an ancestor node; {@code false}, else.
         */
        public Node( final Implementation implementation, final Specification specification,
                     final Implementation classDeclaration, final Node<Implementation> descendant, final T modelObject,
                     final boolean finalNode, final boolean overrideNode )
        {
            super();
            this.implementation = implementation;
            this.specification = specification;
            this.classDeclaration = classDeclaration;
            this.descendant = descendant;
            this.modelObject = modelObject;
            this._final = finalNode;
            this.override = overrideNode;
        }

        /**
         * Gets the implementation the node originates from.
         *
         * @return The implementation the node originates from.
         */
        public final Implementation getImplementation()
        {
            return this.implementation;
        }

        /**
         * Gets the specification the node originates from.
         *
         * @return The specification the node originates from or {@code null}, if the node does not originate from a
         * specification.
         */
        public final Specification getSpecification()
        {
            return this.specification;
        }

        /**
         * Gets the class declaration the node originates from.
         *
         * @return The class declaration the node originates from or {@code null}, if the node does not originate from a
         * class declaration.
         */
        public final Implementation getClassDeclaration()
        {
            return this.classDeclaration;
        }

        /**
         * Gets the direct descendant node of the node.
         *
         * @return The direct descendant node of the node or {@code null}.
         *
         * @see InheritanceModel#getSourceNodes(java.lang.String)
         */
        public final Node<Implementation> getDescendant()
        {
            return this.descendant;
        }

        /**
         * Gets the model object of the node.
         *
         * @return The model object of the node.
         */
        public final T getModelObject()
        {
            return this.modelObject;
        }

        /**
         * Gets a flag indicating the node is the final node in an inheritance hierarchy.
         *
         * @return {@code true}, if the node is the final node in an inheritance hierarchy; {@code false}, else.
         */
        public final boolean isFinal()
        {
            return this._final;
        }

        /**
         * Gets a flag indicating the node is intended to override an ancestor node.
         *
         * @return {@code true}, if the node is intended to override an ancestor; {@code false} else.
         */
        public final boolean isOverride()
        {
            return this.override;
        }

        /**
         * Gets a set of nodes overridden by the node.
         *
         * @return An unmodifiable set holding nodes overridden by the node.
         */
        public final Set<Node<T>> getOverriddenNodes()
        {
            return Collections.unmodifiableSet( this.overriddenNodes );
        }

        /**
         * Gets the path to the node.
         *
         * @return An unmodifiable list holding path elements.
         */
        public final List<Node<Implementation>> getPath()
        {
            return Collections.unmodifiableList( this.path );
        }

        /**
         * Gets a set of nodes overridden by the node.
         *
         * @return A modifiable set holding nodes overridden by the node.
         *
         * @see #getOverriddenNodes()
         */
        private Set<Node<T>> getModifiableOverriddenNodes()
        {
            return this.overriddenNodes;
        }

        /**
         * Gets the path to the node.
         *
         * @return A modifiable list holding path nodes of the node.
         *
         * @see #getPath()
         */
        private LinkedList<Node<Implementation>> getModifiablePath()
        {
            return this.path;
        }

    }

    /**
     * Enumeration of context states.
     */
    private enum ContextState
    {

        PREPARING,
        PREPARED

    }

    /**
     * The modules backing the model.
     */
    private final Modules modules;

    /**
     * {@code Dependency} nodes by context and dependency name.
     */
    private final Map<String, Map<String, Set<Node<Dependency>>>> dependencies = newMap();

    /**
     * {@code Dependency} nodes by context and implementation identifier.
     */
    private final Map<String, Map<String, Map<String, Set<Node<Dependency>>>>> effDependencies = newMap();

    /**
     * {@code Message} nodes by context and message name.
     */
    private final Map<String, Map<String, Set<Node<Message>>>> messages = newMap();

    /**
     * {@code Message} nodes by context and implementation identifier.
     */
    private final Map<String, Map<String, Map<String, Set<Node<Message>>>>> effMessages = newMap();

    /**
     * {@code Property} nodes by context and property name.
     */
    private final Map<String, Map<String, Set<Node<Property>>>> properties = newMap();

    /**
     * {@code Property} nodes by context and implementation identifier.
     */
    private final Map<String, Map<String, Map<String, Set<Node<Property>>>>> effProperties = newMap();

    /**
     * {@code SpecificationReference} nodes by context and specification identifier.
     */
    private final Map<String, Map<String, Set<Node<SpecificationReference>>>> specReferences = newMap();

    /**
     * {@code SpecificationReference} nodes by context and implementation identifier.
     */
    private final Map<String, Map<String, Map<String, Set<Node<SpecificationReference>>>>> effSpecReferences =
        newMap();

    /**
     * {@code ImplementationReference} nodes by context and implementation reference identifier.
     */
    private final Map<String, Map<String, Set<Node<ImplementationReference>>>> implReferences = newMap();

    /**
     * {@code ImplementationReference} nodes by context and implementation reference identifier.
     */
    private final Map<String, Set<Node<ImplementationReference>>> cyclicImplReferences = newMap();

    /**
     * {@code ImplementationReference} nodes by context and implementation identifier.
     */
    private final Map<String, Map<String, Map<String, Set<Node<ImplementationReference>>>>> effImplReferences =
        newMap();

    /**
     * {@code Element} nodes by context and qualified name.
     */
    private final Map<String, Map<QName, Set<Node<Element>>>> xmlElements = newMap();

    /**
     * {@code Element} nodes by context and implementation identifier.
     */
    private final Map<String, Map<String, Map<QName, Set<Node<Element>>>>> effXmlElements = newMap();

    /**
     * {@code JAXBElement} nodes by context and qualified name.
     */
    private final Map<String, Map<QName, Set<Node<JAXBElement<?>>>>> jaxbElements = newMap();

    /**
     * {@code JAXBElement} nodes by context and implementation identifier.
     */
    private final Map<String, Map<String, Map<QName, Set<Node<JAXBElement<?>>>>>> effJaxbElements =
        newMap();

    /**
     * {@code Implementation} nodes by context and implementation identifier.
     */
    private final Map<String, Map<String, Node<Implementation>>> implementations = newMap();

    /**
     * Source nodes of a hierarchy by context and implementation identifier.
     */
    private final Map<String, Map<String, Node<Implementation>>> sourceNodes = newMap();

    /**
     * Context states by context identifier.
     */
    private final Map<String, ContextState> contextStates = newMap();

    /**
     * Creates a new {@code InheritanceModel} instance.
     *
     * @param modules The modules backing the model.
     *
     * @throws NullPointerException if {@code modules} is {@code null}.
     *
     * @see Modules#clone()
     */
    public InheritanceModel( final Modules modules )
    {
        super();

        if ( modules == null )
        {
            throw new NullPointerException( "modules" );
        }

        this.modules = modules.clone();
    }

    /**
     * Gets a set holding source nodes of an implementation.
     *
     * @param implementation The identifier of the implementation to get source nodes of.
     *
     * @return An unmodifiable set holding source nodes of the implementation identified by {@code implementation}.
     *
     * @throws NullPointerException if {@code implementation} is {@code null}.
     *
     * @see Node#getDescendant()
     */
    public Set<Node<Implementation>> getSourceNodes( final String implementation )
    {
        if ( implementation == null )
        {
            throw new NullPointerException( "implementation" );
        }

        this.prepareContext( implementation );
        final Collection<Node<Implementation>> col = map( this.sourceNodes, implementation ).values();
        return unmodifiableSet( newSet( col ) );
    }

    /**
     * Gets a set holding implementation reference nodes of an implementation causing a cycle.
     *
     * @param implementation The identifier of the implementation to get implementation reference nodes causing a cycle
     * of.
     *
     * @return An unmodifiable set holding implementation reference nodes of the implementation identified by
     * {@code implementation} causing a cycle.
     *
     * @throws NullPointerException if {@code implementation} is {@code null}.
     *
     * @since 1.5
     *
     * @see Node#getPath()
     */
    public Set<Node<ImplementationReference>> getCycleNodes( final String implementation )
    {
        if ( implementation == null )
        {
            throw new NullPointerException( "implementation" );
        }

        this.prepareContext( implementation );
        return unmodifiableSet( nodes( this.cyclicImplReferences, implementation ) );
    }

    /**
     * Gets a set holding the names of all dependencies of an implementation.
     *
     * @param implementation The identifier of the implementation to get the names of all dependencies of.
     *
     * @return An unmodifiable set holding the names of all dependencies of the implementation identified by
     * {@code implementation}.
     *
     * @throws NullPointerException if {@code implementation} is {@code null}.
     */
    public Set<String> getDependencyNames( final String implementation )
    {
        if ( implementation == null )
        {
            throw new NullPointerException( "implementation" );
        }

        this.prepareContext( implementation );
        return Collections.unmodifiableSet( map( this.dependencies, implementation ).keySet() );
    }

    /**
     * Gets a set holding effective dependency nodes of an implementation.
     *
     * @param implementation The identifier of the implementation to get effective dependency nodes of.
     * @param name The dependency name to get effective nodes for.
     *
     * @return An unmodifiable set holding effective dependency nodes matching {@code name} of the implementation
     * identified by {@code implementation}.
     *
     * @throws NullPointerException if {@code implementation} or {@code name} is {@code null}.
     *
     * @see #getDependencyNames(java.lang.String)
     */
    public Set<Node<Dependency>> getDependencyNodes( final String implementation, final String name )
    {
        if ( implementation == null )
        {
            throw new NullPointerException( "implementation" );
        }
        if ( name == null )
        {
            throw new NullPointerException( "name" );
        }

        this.prepareContext( implementation );
        Set<Node<Dependency>> set = null;

        final Map<String, Set<Node<Dependency>>> map =
            getEffectiveNodes( this.effDependencies, implementation, implementation );

        if ( map != null )
        {
            set = map.get( name );
        }

        return unmodifiableSet( set );
    }

    /**
     * Gets a set holding the identifiers of all implementation references of an implementation.
     *
     * @param implementation The identifier of the implementation to get the identifiers of all implementation
     * references of.
     *
     * @return An unmodifiable set holding the identifiers of all implementation references of the implementation
     * identified by {@code implementation}.
     *
     * @throws NullPointerException if {@code implementation} is {@code null}.
     */
    public Set<String> getImplementationReferenceIdentifiers( final String implementation )
    {
        if ( implementation == null )
        {
            throw new NullPointerException( "implementation" );
        }

        this.prepareContext( implementation );
        return Collections.unmodifiableSet( map( this.implReferences, implementation ).keySet() );
    }

    /**
     * Gets a set holding effective implementation reference nodes of an implementation.
     *
     * @param implementation The identifier of the implementation to get effective implementation reference nodes of.
     * @param identifier The implementation reference identifier to get effective nodes for.
     *
     * @return An unmodifiable set holding effective implementation reference nodes matching {@code identifier} of the
     * implementation identified by {@code implementation}.
     *
     * @throws NullPointerException if {@code implementation} or {@code identifier} is {@code null}.
     *
     * @see #getImplementationReferenceIdentifiers(java.lang.String)
     */
    public Set<Node<ImplementationReference>> getImplementationReferenceNodes( final String implementation,
                                                                               final String identifier )
    {
        if ( implementation == null )
        {
            throw new NullPointerException( "implementation" );
        }
        if ( identifier == null )
        {
            throw new NullPointerException( "identifier" );
        }

        this.prepareContext( implementation );
        Set<Node<ImplementationReference>> set = null;
        final Map<String, Set<Node<ImplementationReference>>> map =
            getEffectiveNodes( this.effImplReferences, implementation, implementation );

        if ( map != null )
        {
            set = map.get( identifier );
        }

        return unmodifiableSet( set );
    }

    /**
     * Gets a set holding the qualified names of all XML elements of an implementation.
     *
     * @param implementation The identifier of the implementation to get the qualified names of all XML elements of.
     *
     * @return An unmodifiable set holding the qualified names of all XML elements of the implementation identified by
     * {@code implementation}.
     *
     * @throws NullPointerException if {@code implementation} is {@code null}.
     */
    public Set<QName> getJaxbElementNames( final String implementation )
    {
        if ( implementation == null )
        {
            throw new NullPointerException( "implementation" );
        }

        this.prepareContext( implementation );
        return Collections.unmodifiableSet( map( this.jaxbElements, implementation ).keySet() );
    }

    /**
     * Gets a set holding effective JAXB element nodes of an implementation.
     *
     * @param implementation The identifier of the implementation to get effective JAXB element nodes of.
     * @param name The qualified JAXB element name to get effective nodes for.
     *
     * @return An unmodifiable set holding effective JAXB element nodes matching {@code name} of the implementation
     * identified by {@code implementation}.
     *
     * @throws NullPointerException if {@code implementation} or {@code name} is {@code null}.
     *
     * @see #getJaxbElementNames(java.lang.String)
     */
    public Set<Node<JAXBElement<?>>> getJaxbElementNodes( final String implementation, final QName name )
    {
        if ( implementation == null )
        {
            throw new NullPointerException( "implementation" );
        }
        if ( name == null )
        {
            throw new NullPointerException( "name" );
        }

        this.prepareContext( implementation );
        Set<Node<JAXBElement<?>>> set = null;
        final Map<QName, Set<Node<JAXBElement<?>>>> map =
            getEffectiveNodes( this.effJaxbElements, implementation, implementation );

        if ( map != null )
        {
            set = map.get( name );
        }

        return unmodifiableSet( set );
    }

    /**
     * Gets a set holding the names of all messages of an implementation.
     *
     * @param implementation The identifier of the implementation to get the names of all messages of.
     *
     * @return An unmodifiable set holding the names of all messages of the implementation identified by
     * {@code implementation}.
     *
     * @throws NullPointerException if {@code implementation} is {@code null}.
     */
    public Set<String> getMessageNames( final String implementation )
    {
        if ( implementation == null )
        {
            throw new NullPointerException( "implementation" );
        }

        this.prepareContext( implementation );
        return Collections.unmodifiableSet( map( this.messages, implementation ).keySet() );
    }

    /**
     * Gets a set holding effective message nodes of an implementation.
     *
     * @param implementation The identifier of the implementation to get effective message nodes of.
     * @param name The message name to get effective nodes for.
     *
     * @return An unmodifiable set holding effective message nodes matching {@code name} of the implementation
     * identified by {@code implementation}.
     *
     * @throws NullPointerException if {@code implementation} or {@code name} is {@code null}.
     *
     * @see #getMessageNames(java.lang.String)
     */
    public Set<Node<Message>> getMessageNodes( final String implementation, final String name )
    {
        if ( implementation == null )
        {
            throw new NullPointerException( "implementation" );
        }
        if ( name == null )
        {
            throw new NullPointerException( "name" );
        }

        this.prepareContext( implementation );
        Set<Node<Message>> set = null;
        final Map<String, Set<Node<Message>>> map =
            getEffectiveNodes( this.effMessages, implementation, implementation );

        if ( map != null )
        {
            set = map.get( name );
        }

        return unmodifiableSet( set );
    }

    /**
     * Gets a set holding the names of all properties of an implementation.
     *
     * @param implementation The identifier of the implementation to get the names of all properties of.
     *
     * @return An unmodifiable set holding the names of all properties of the implementation identified by
     * {@code implementation}.
     *
     * @throws NullPointerException if {@code implementation} is {@code null}.
     */
    public Set<String> getPropertyNames( final String implementation )
    {
        if ( implementation == null )
        {
            throw new NullPointerException( "implementation" );
        }

        this.prepareContext( implementation );
        return Collections.unmodifiableSet( map( this.properties, implementation ).keySet() );
    }

    /**
     * Gets a set holding effective property nodes of an implementation.
     *
     * @param implementation The identifier of the implementation to get effective property nodes of.
     * @param name The property name to get effective nodes for.
     *
     * @return An unmodifiable set holding effective property nodes matching {@code name} of the implementation
     * identified by {@code implementation}.
     *
     * @throws NullPointerException if {@code implementation} or {@code name} is {@code null}.
     *
     * @see #getPropertyNames(java.lang.String)
     */
    public Set<Node<Property>> getPropertyNodes( final String implementation, final String name )
    {
        if ( implementation == null )
        {
            throw new NullPointerException( "implementation" );
        }
        if ( name == null )
        {
            throw new NullPointerException( "name" );
        }

        this.prepareContext( implementation );
        Set<Node<Property>> set = null;
        final Map<String, Set<Node<Property>>> map =
            getEffectiveNodes( this.effProperties, implementation, implementation );

        if ( map != null )
        {
            set = map.get( name );
        }

        return unmodifiableSet( set );
    }

    /**
     * Gets a set holding the identifiers of all specification references of an implementation.
     *
     * @param implementation The identifier of the implementation to get the identifiers of all specification references
     * of.
     *
     * @return An unmodifiable set holding the identifiers of all specification references of the implementation
     * identified by {@code implementation}.
     *
     * @throws NullPointerException if {@code implementation} is {@code null}.
     */
    public Set<String> getSpecificationReferenceIdentifiers( final String implementation )
    {
        if ( implementation == null )
        {
            throw new NullPointerException( "implementation" );
        }

        this.prepareContext( implementation );
        return Collections.unmodifiableSet( map( this.specReferences, implementation ).keySet() );
    }

    /**
     * Gets a set holding effective specification reference nodes of an implementation.
     *
     * @param implementation The identifier of the implementation to get effective specification reference nodes of.
     * @param identifier The specification reference identifier to get effective nodes for.
     *
     * @return An unmodifiable set holding effective specification reference nodes matching {@code identifier} of the
     * implementation identified by {@code implementation}.
     *
     * @throws NullPointerException if {@code implementation} or {@code identifier} is {@code null}.
     *
     * @see #getSpecificationReferenceIdentifiers(java.lang.String)
     */
    public Set<Node<SpecificationReference>> getSpecificationReferenceNodes( final String implementation,
                                                                             final String identifier )
    {
        if ( implementation == null )
        {
            throw new NullPointerException( "implementation" );
        }
        if ( identifier == null )
        {
            throw new NullPointerException( "identifier" );
        }

        this.prepareContext( implementation );
        Set<Node<SpecificationReference>> set = null;
        final Map<String, Set<Node<SpecificationReference>>> map =
            getEffectiveNodes( this.effSpecReferences, implementation, implementation );

        if ( map != null )
        {
            set = map.get( identifier );
        }

        return unmodifiableSet( set );
    }

    /**
     * Gets a set holding the qualified names of all XML elements of an implementation.
     *
     * @param implementation The identifier of the implementation to get the qualified names of all XML elements of.
     *
     * @return An unmodifiable set holding the qualified names of all XML elements of the implementation identified by
     * {@code implementation}.
     *
     * @throws NullPointerException if {@code implementation} is {@code null}.
     */
    public Set<QName> getXmlElementNames( final String implementation )
    {
        if ( implementation == null )
        {
            throw new NullPointerException( "implementation" );
        }

        this.prepareContext( implementation );
        return Collections.unmodifiableSet( map( this.xmlElements, implementation ).keySet() );
    }

    /**
     * Gets a set holding effective XML element nodes of an implementation.
     *
     * @param implementation The identifier of the implementation to get effective XML element nodes of.
     * @param name The qualified XML element name to get effective nodes for.
     *
     * @return An unmodifiable set holding effective XML element nodes matching {@code name} of the implementation
     * identified by {@code implementation}.
     *
     * @throws NullPointerException if {@code implementation} or {@code name} is {@code null}.
     *
     * @see #getXmlElementNames(java.lang.String)
     */
    public Set<Node<Element>> getXmlElementNodes( final String implementation, final QName name )
    {
        if ( implementation == null )
        {
            throw new NullPointerException( "implementation" );
        }
        if ( name == null )
        {
            throw new NullPointerException( "name" );
        }

        this.prepareContext( implementation );
        Set<Node<Element>> set = null;
        final Map<QName, Set<Node<Element>>> map =
            getEffectiveNodes( this.effXmlElements, implementation, implementation );

        if ( map != null )
        {
            set = map.get( name );
        }

        return unmodifiableSet( set );
    }

    private void prepareContext( final String context )
    {
        ContextState state = this.contextStates.get( context );

        if ( state == null )
        {
            state = ContextState.PREPARING;
            this.contextStates.put( context, state );

            final Implementation i = this.modules.getImplementation( context );

            if ( i != null )
            {
                this.collectNodes( context, i, null, null );

                for ( final Node<Implementation> source : map( this.sourceNodes, context ).values() )
                {
                    this.collectEffectiveNodes( context, source );
                }
            }

            state = ContextState.PREPARED;
            this.contextStates.put( context, state );
        }

        assert state == ContextState.PREPARED :
            "Unexpected context state '" + state + "' for context '" + context + "'.";

    }

    private void collectNodes( final String context, final Implementation declaration,
                               final Node<Implementation> descendant, LinkedList<Node<Implementation>> path )
    {
        if ( path == null )
        {
            path = new LinkedList<Node<Implementation>>();
        }

        final Map<String, Node<Implementation>> contextImplementations = map( this.implementations, context );

        if ( declaration != null && !contextImplementations.containsKey( declaration.getIdentifier() ) )
        {
            final Node<Implementation> declarationNode = new Node<Implementation>(
                declaration, null, null, descendant, declaration, declaration.isFinal(), false );

            declarationNode.getModifiablePath().addAll( path );

            contextImplementations.put( declaration.getIdentifier(), declarationNode );

            path.addLast( declarationNode );

            if ( declaration.getDependencies() != null )
            {
                for ( int i = 0, s0 = declaration.getDependencies().getDependency().size(); i < s0; i++ )
                {
                    final Dependency d = declaration.getDependencies().getDependency().get( i );
                    final Node<Dependency> node =
                        new Node<Dependency>( declaration, null, null, descendant, d, d.isFinal(), d.isOverride() );

                    node.getModifiablePath().addAll( path );

                    addNode( map( this.dependencies, context ), node, node.getModelObject().getName() );
                }
            }

            if ( declaration.getMessages() != null )
            {
                for ( int i = 0, s0 = declaration.getMessages().getMessage().size(); i < s0; i++ )
                {
                    final Message m = declaration.getMessages().getMessage().get( i );
                    final Node<Message> node =
                        new Node<Message>( declaration, null, null, descendant, m, m.isFinal(), m.isOverride() );

                    node.getModifiablePath().addAll( path );

                    addNode( map( this.messages, context ), node, node.getModelObject().getName() );
                }

                if ( !declaration.getMessages().getReference().isEmpty() )
                {
                    final Module m = this.modules.getModuleOfImplementation( declaration.getIdentifier() );

                    if ( m != null && m.getMessages() != null )
                    {
                        for ( int i = 0, s0 = declaration.getMessages().getReference().size(); i < s0; i++ )
                        {
                            final MessageReference r = declaration.getMessages().getReference().get( i );
                            Message msg = m.getMessages().getMessage( r.getName() );

                            if ( msg != null )
                            {
                                msg = msg.clone();
                                msg.setFinal( r.isFinal() );
                                msg.setOverride( r.isOverride() );

                                final Node<Message> node = new Node<Message>(
                                    declaration, null, null, descendant, msg, msg.isFinal(), msg.isOverride() );

                                node.getModifiablePath().addAll( path );

                                addNode( map( this.messages, context ), node, node.getModelObject().getName() );
                            }
                        }
                    }
                }
            }

            if ( declaration.getProperties() != null )
            {
                for ( int i = 0, s0 = declaration.getProperties().getProperty().size(); i < s0; i++ )
                {
                    final Property p = declaration.getProperties().getProperty().get( i );
                    final Node<Property> node =
                        new Node<Property>( declaration, null, null, descendant, p, p.isFinal(), p.isOverride() );

                    node.getModifiablePath().addAll( path );

                    addNode( map( this.properties, context ), node, node.getModelObject().getName() );
                }

                if ( !declaration.getProperties().getReference().isEmpty() )
                {
                    final Module m = this.modules.getModuleOfImplementation( declaration.getIdentifier() );

                    if ( m != null && m.getProperties() != null )
                    {
                        for ( int i = 0, s0 = declaration.getProperties().getReference().size(); i < s0; i++ )
                        {
                            final PropertyReference r = declaration.getProperties().getReference().get( i );
                            Property p = m.getProperties().getProperty( r.getName() );

                            if ( p != null )
                            {
                                p = p.clone();
                                p.setFinal( r.isFinal() );
                                p.setOverride( r.isOverride() );

                                final Node<Property> node = new Node<Property>(
                                    declaration, null, null, descendant, p, p.isFinal(), p.isOverride() );

                                node.getModifiablePath().addAll( path );

                                addNode( map( this.properties, context ), node, node.getModelObject().getName() );
                            }
                        }
                    }
                }
            }

            if ( declaration.getSpecifications() != null )
            {
                for ( int i = 0, s0 = declaration.getSpecifications().getReference().size(); i < s0; i++ )
                {
                    final SpecificationReference r = declaration.getSpecifications().getReference().get( i );
                    final Node<SpecificationReference> node = new Node<SpecificationReference>(
                        declaration, null, null, descendant, r, r.isFinal(), r.isOverride() );

                    node.getModifiablePath().addAll( path );

                    addNode( map( this.specReferences, context ), node, node.getModelObject().getIdentifier() );

                    final Specification s = this.modules.getSpecification( r.getIdentifier() );

                    if ( s != null && s.getProperties() != null )
                    {
                        for ( int j = 0, s1 = s.getProperties().getProperty().size(); j < s1; j++ )
                        {
                            final Property p = s.getProperties().getProperty().get( j );
                            final Node<Property> n =
                                new Node<Property>( declaration, s, null, descendant, p, p.isFinal(), p.isOverride() );

                            n.getModifiablePath().addAll( path );

                            addNode( map( this.properties, context ), n, n.getModelObject().getName() );
                        }
                    }
                }
            }

            if ( !declaration.getAny().isEmpty() )
            {
                for ( int i = 0, s0 = declaration.getAny().size(); i < s0; i++ )
                {
                    final Object any = declaration.getAny().get( i );

                    if ( any instanceof Element )
                    {
                        final Element e = (Element) any;
                        final Node<Element> node =
                            new Node<Element>( declaration, null, null, descendant, e, false, false );

                        node.getModifiablePath().addAll( path );

                        addNode( map( this.xmlElements, context ), node, getXmlElementName( e ) );
                        continue;
                    }

                    if ( any instanceof JAXBElement<?> )
                    {
                        final JAXBElement<?> e = (JAXBElement<?>) any;
                        boolean _final = false;
                        boolean override = false;

                        if ( e.getValue() instanceof Inheritable )
                        {
                            _final = ( (Inheritable) e.getValue() ).isFinal();
                            override = ( (Inheritable) e.getValue() ).isOverride();
                        }

                        final Node<JAXBElement<?>> node =
                            new Node<JAXBElement<?>>( declaration, null, null, descendant, e, _final, override );

                        node.getModifiablePath().addAll( path );

                        addNode( map( this.jaxbElements, context ), node, e.getName() );
                        continue;
                    }
                }
            }

            if ( declaration.getImplementations() != null
                     && !declaration.getImplementations().getReference().isEmpty() )
            {
                boolean all_cyclic = true;

                for ( int i = 0, s0 = declaration.getImplementations().getReference().size(); i < s0; i++ )
                {
                    final ImplementationReference r = declaration.getImplementations().getReference().get( i );
                    final Node<ImplementationReference> node = new Node<ImplementationReference>(
                        declaration, null, null, descendant, r, r.isFinal(), r.isOverride() );

                    node.getModifiablePath().addAll( path );

                    final Implementation ancestor = this.modules.getImplementation( r.getIdentifier() );

                    boolean cycle = false;
                    if ( ancestor != null && contextImplementations.containsKey( ancestor.getIdentifier() ) )
                    {
                        for ( int j = 0, s1 = path.size(); j < s1; j++ )
                        {
                            final Node<Implementation> n = path.get( j );

                            if ( n.getModelObject().getIdentifier().equals( ancestor.getIdentifier() ) )
                            {
                                cycle = true;
                                node.getModifiablePath().add( n );
                                break;
                            }
                        }
                    }

                    if ( cycle )
                    {
                        addNode( this.cyclicImplReferences, node, context );
                    }
                    else
                    {
                        all_cyclic = false;
                        addNode( map( this.implReferences, context ), node, node.getModelObject().getIdentifier() );
                        this.collectNodes( context, ancestor, declarationNode, path );
                    }
                }

                if ( all_cyclic )
                {
                    map( this.sourceNodes, context ).
                        put( declarationNode.getModelObject().getIdentifier(), declarationNode );

                }
            }
            else
            {
                map( this.sourceNodes, context ).
                    put( declarationNode.getModelObject().getIdentifier(), declarationNode );

            }

            path.removeLast();
        }
    }

    private void collectEffectiveNodes( final String context, final Node<Implementation> node )
    {
        final Map<String, Set<Node<SpecificationReference>>> directSpecificationReferences =
            getDirectEffectiveNodes( map( this.specReferences, context ), node.getModelObject().getIdentifier() );

        final Map<String, Set<Node<Dependency>>> directDependencies =
            getDirectEffectiveNodes( map( this.dependencies, context ), node.getModelObject().getIdentifier() );

        final Map<String, Set<Node<Message>>> directMessages =
            getDirectEffectiveNodes( map( this.messages, context ), node.getModelObject().getIdentifier() );

        final Map<String, Set<Node<Property>>> directProperties =
            getDirectEffectiveNodes( map( this.properties, context ), node.getModelObject().getIdentifier() );

        final Map<String, Set<Node<ImplementationReference>>> directImplementationReferences =
            getDirectEffectiveNodes( map( this.implReferences, context ), node.getModelObject().getIdentifier() );

        final Map<QName, Set<Node<Element>>> directXmlElements =
            getDirectEffectiveNodes( map( this.xmlElements, context ), node.getModelObject().getIdentifier() );

        final Map<QName, Set<Node<JAXBElement<?>>>> directJaxbElements =
            getDirectEffectiveNodes( map( this.jaxbElements, context ), node.getModelObject().getIdentifier() );

        overrideNodes( map( this.effSpecReferences, context ), node, directSpecificationReferences );
        overrideNodes( map( this.effImplReferences, context ), node, directImplementationReferences );
        overrideNodes( map( this.effDependencies, context ), node, directDependencies );
        overrideNodes( map( this.effMessages, context ), node, directMessages );
        overrideNodes( map( this.effProperties, context ), node, directProperties );
        overrideNodes( map( this.effJaxbElements, context ), node, directJaxbElements );
        overrideNodes( map( this.effXmlElements, context ), node, directXmlElements );

        this.addClassDeclarationNodes( context, node );

        final Map<String, Set<Node<SpecificationReference>>> ancestorSpecificationReferences =
            getEffectiveNodes( this.effSpecReferences, context, node.getModelObject().getIdentifier() );

        final Map<String, Set<Node<Dependency>>> ancestorDependencies =
            getEffectiveNodes( this.effDependencies, context, node.getModelObject().getIdentifier() );

        final Map<String, Set<Node<Message>>> ancestorMessages =
            getEffectiveNodes( this.effMessages, context, node.getModelObject().getIdentifier() );

        final Map<String, Set<Node<Property>>> ancestorProperties =
            getEffectiveNodes( this.effProperties, context, node.getModelObject().getIdentifier() );

        final Map<String, Set<Node<ImplementationReference>>> ancestorImplementationReferences =
            getEffectiveNodes( this.effImplReferences, context, node.getModelObject().getIdentifier() );

        final Map<QName, Set<Node<Element>>> ancestorXmlElements =
            getEffectiveNodes( this.effXmlElements, context, node.getModelObject().getIdentifier() );

        final Map<QName, Set<Node<JAXBElement<?>>>> ancestorJaxbElements =
            getEffectiveNodes( this.effJaxbElements, context, node.getModelObject().getIdentifier() );

        if ( node.getDescendant() != null )
        {
            if ( ancestorSpecificationReferences != null )
            {
                inheritNodes( map( this.effSpecReferences, context ), ancestorSpecificationReferences,
                              node.getDescendant() );

            }

            if ( ancestorDependencies != null )
            {
                inheritNodes( map( this.effDependencies, context ), ancestorDependencies,
                              node.getDescendant() );

            }

            if ( ancestorProperties != null )
            {
                inheritNodes( map( this.effProperties, context ), ancestorProperties, node.getDescendant() );
            }

            if ( ancestorMessages != null )
            {
                inheritNodes( map( this.effMessages, context ), ancestorMessages, node.getDescendant() );
            }

            if ( ancestorImplementationReferences != null )
            {
                inheritNodes( map( this.effImplReferences, context ), ancestorImplementationReferences,
                              node.getDescendant() );

            }

            if ( ancestorXmlElements != null )
            {
                inheritNodes( map( this.effXmlElements, context ), ancestorXmlElements,
                              node.getDescendant() );

            }

            if ( ancestorJaxbElements != null )
            {
                inheritNodes( map( this.effJaxbElements, context ), ancestorJaxbElements,
                              node.getDescendant() );

            }

            collectEffectiveNodes( context, node.getDescendant() );
        }
    }

    private void addClassDeclarationNodes( final String context, final Node<Implementation> node )
    {
        final Implementation classDeclaration = this.getClassDeclaration( node.getModelObject() );

        if ( classDeclaration != null )
        {
            this.prepareContext( classDeclaration.getIdentifier() );

            Map<String, Set<Node<Dependency>>> effectiveDependencies =
                getEffectiveNodes( this.effDependencies, context, node.getModelObject().getIdentifier() );

            Map<String, Set<Node<Message>>> effectiveMessages =
                getEffectiveNodes( this.effMessages, context, node.getModelObject().getIdentifier() );

            Map<String, Set<Node<Property>>> effectiveProperties =
                getEffectiveNodes( this.effProperties, context, node.getModelObject().getIdentifier() );

            Map<String, Set<Node<SpecificationReference>>> effectiveSpecificationReferences =
                getEffectiveNodes( this.effSpecReferences, context, node.getModelObject().getIdentifier() );

            Map<QName, Set<Node<Element>>> effectiveXmlElements =
                getEffectiveNodes( this.effXmlElements, context, node.getModelObject().getIdentifier() );

            Map<QName, Set<Node<JAXBElement<?>>>> effectiveJaxbElements =
                getEffectiveNodes( this.effJaxbElements, context, node.getModelObject().getIdentifier() );

            final Map<String, Set<Node<Dependency>>> declDependencies =
                getEffectiveNodes( this.effDependencies, classDeclaration.getIdentifier(),
                                   classDeclaration.getIdentifier() );

            final Map<String, Set<Node<Message>>> declMessages =
                getEffectiveNodes( this.effMessages, classDeclaration.getIdentifier(),
                                   classDeclaration.getIdentifier() );

            final Map<String, Set<Node<Property>>> declProperties =
                getEffectiveNodes( this.effProperties, classDeclaration.getIdentifier(),
                                   classDeclaration.getIdentifier() );

            final Map<String, Set<Node<SpecificationReference>>> declSpecReferences =
                getEffectiveNodes( this.effSpecReferences, classDeclaration.getIdentifier(),
                                   classDeclaration.getIdentifier() );

            final Map<QName, Set<Node<Element>>> declXmlElements =
                getEffectiveNodes( this.effXmlElements, classDeclaration.getIdentifier(),
                                   classDeclaration.getIdentifier() );

            final Map<QName, Set<Node<JAXBElement<?>>>> declJaxbElements =
                getEffectiveNodes( this.effJaxbElements, classDeclaration.getIdentifier(),
                                   classDeclaration.getIdentifier() );

            if ( declDependencies != null )
            {
                if ( effectiveDependencies == null )
                {
                    effectiveDependencies = newMap();
                    map( this.effDependencies, context ).
                        put( node.getModelObject().getIdentifier(), effectiveDependencies );

                }

                for ( final Map.Entry<String, Set<Node<Dependency>>> e : declDependencies.entrySet() )
                {
                    final Set<Node<Dependency>> set = newSet( e.getValue().size() );

                    for ( final Node<Dependency> n : e.getValue() )
                    {
                        final Node<Dependency> effNode = new Node<Dependency>(
                            node.getModelObject(), n.getSpecification(), classDeclaration, null, n.getModelObject(),
                            n.isFinal(), n.isOverride() );

                        effNode.getModifiablePath().addAll( n.getPath() );
                        set.add( effNode );

                        addNode( map( this.dependencies, context ), effNode, e.getKey() );
                    }

                    if ( effectiveDependencies.containsKey( e.getKey() ) )
                    {
                        for ( final Node<Dependency> effNode : effectiveDependencies.get( e.getKey() ) )
                        {
                            effNode.getModifiableOverriddenNodes().addAll( set );
                        }
                    }
                    else
                    {
                        effectiveDependencies.put( e.getKey(), set );
                    }
                }
            }

            if ( declSpecReferences != null )
            {
                if ( effectiveSpecificationReferences == null )
                {
                    effectiveSpecificationReferences = newMap();
                    map( this.effSpecReferences, context ).
                        put( node.getModelObject().getIdentifier(), effectiveSpecificationReferences );

                }

                for ( final Map.Entry<String, Set<Node<SpecificationReference>>> e : declSpecReferences.entrySet() )
                {
                    final Set<Node<SpecificationReference>> set = newSet( e.getValue().size() );

                    for ( final Node<SpecificationReference> n : e.getValue() )
                    {
                        final Node<SpecificationReference> effNode = new Node<SpecificationReference>(
                            node.getModelObject(), n.getSpecification(), classDeclaration, null, n.getModelObject(),
                            n.isFinal(), n.isOverride() );

                        effNode.getModifiablePath().addAll( n.getPath() );
                        set.add( effNode );

                        addNode( map( this.specReferences, context ), effNode, e.getKey() );
                    }

                    if ( effectiveSpecificationReferences.containsKey( e.getKey() ) )
                    {
                        for ( final Node<SpecificationReference> effNode
                                  : effectiveSpecificationReferences.get( e.getKey() ) )
                        {
                            effNode.getModifiableOverriddenNodes().addAll( set );
                        }
                    }
                    else
                    {
                        effectiveSpecificationReferences.put( e.getKey(), set );
                    }
                }
            }

            if ( declMessages != null )
            {
                if ( effectiveMessages == null )
                {
                    effectiveMessages = newMap();
                    map( this.effMessages, context ).
                        put( node.getModelObject().getIdentifier(), effectiveMessages );

                }

                for ( final Map.Entry<String, Set<Node<Message>>> e : declMessages.entrySet() )
                {
                    final Set<Node<Message>> set = newSet( e.getValue().size() );

                    for ( final Node<Message> n : e.getValue() )
                    {
                        final Node<Message> effNode = new Node<Message>(
                            node.getModelObject(), n.getSpecification(), classDeclaration, null, n.getModelObject(),
                            n.isFinal(), n.isOverride() );

                        effNode.getModifiablePath().addAll( n.getPath() );
                        set.add( effNode );

                        addNode( map( this.messages, context ), effNode, e.getKey() );
                    }

                    if ( effectiveMessages.containsKey( e.getKey() ) )
                    {
                        for ( final Node<Message> effNode : effectiveMessages.get( e.getKey() ) )
                        {
                            effNode.getModifiableOverriddenNodes().addAll( set );
                        }
                    }
                    else
                    {
                        effectiveMessages.put( e.getKey(), set );
                    }
                }
            }

            if ( declProperties != null )
            {
                if ( effectiveProperties == null )
                {
                    effectiveProperties = newMap();
                    map( this.effProperties, context ).
                        put( node.getModelObject().getIdentifier(), effectiveProperties );

                }

                for ( final Map.Entry<String, Set<Node<Property>>> e : declProperties.entrySet() )
                {
                    final Set<Node<Property>> set = newSet( e.getValue().size() );

                    for ( final Node<Property> n : e.getValue() )
                    {
                        final Node<Property> effNode = new Node<Property>(
                            node.getModelObject(), n.getSpecification(), classDeclaration, null, n.getModelObject(),
                            n.isFinal(), n.isOverride() );

                        effNode.getModifiablePath().addAll( n.getPath() );
                        set.add( effNode );

                        addNode( map( this.properties, context ), effNode, e.getKey() );
                    }

                    if ( effectiveProperties.containsKey( e.getKey() ) )
                    {
                        for ( final Node<Property> effNode : effectiveProperties.get( e.getKey() ) )
                        {
                            effNode.getModifiableOverriddenNodes().addAll( set );
                        }
                    }
                    else
                    {
                        effectiveProperties.put( e.getKey(), set );
                    }
                }
            }

            if ( declXmlElements != null )
            {
                if ( effectiveXmlElements == null )
                {
                    effectiveXmlElements = newMap();
                    map( this.effXmlElements, context ).
                        put( node.getModelObject().getIdentifier(), effectiveXmlElements );

                }

                for ( final Map.Entry<QName, Set<Node<Element>>> e : declXmlElements.entrySet() )
                {
                    final Set<Node<Element>> set = newSet( e.getValue().size() );

                    for ( final Node<Element> n : e.getValue() )
                    {
                        final Node<Element> effNode = new Node<Element>(
                            node.getModelObject(), n.getSpecification(), classDeclaration, null, n.getModelObject(),
                            n.isFinal(), n.isOverride() );

                        effNode.getModifiablePath().addAll( n.getPath() );
                        set.add( effNode );

                        addNode( map( this.xmlElements, context ), effNode, e.getKey() );
                    }

                    if ( effectiveXmlElements.containsKey( e.getKey() ) )
                    {
                        for ( final Node<Element> effNode : effectiveXmlElements.get( e.getKey() ) )
                        {
                            effNode.getModifiableOverriddenNodes().addAll( set );
                        }
                    }
                    else
                    {
                        effectiveXmlElements.put( e.getKey(), set );
                    }
                }
            }

            if ( declJaxbElements != null )
            {
                if ( effectiveJaxbElements == null )
                {
                    effectiveJaxbElements = newMap();
                    map( this.effJaxbElements, context ).
                        put( node.getModelObject().getIdentifier(), effectiveJaxbElements );

                }

                for ( final Map.Entry<QName, Set<Node<JAXBElement<?>>>> e : declJaxbElements.entrySet() )
                {
                    final Set<Node<JAXBElement<?>>> set = newSet( e.getValue().size() );

                    for ( final Node<JAXBElement<?>> n : e.getValue() )
                    {
                        final Node<JAXBElement<?>> effNode = new Node<JAXBElement<?>>(
                            node.getModelObject(), n.getSpecification(), classDeclaration, null, n.getModelObject(),
                            n.isFinal(), n.isOverride() );

                        effNode.getModifiablePath().addAll( n.getPath() );
                        set.add( effNode );

                        addNode( map( this.jaxbElements, context ), effNode, e.getKey() );
                    }

                    if ( effectiveJaxbElements.containsKey( e.getKey() ) )
                    {
                        for ( final Node<JAXBElement<?>> effNode : effectiveJaxbElements.get( e.getKey() ) )
                        {
                            effNode.getModifiableOverriddenNodes().addAll( set );
                        }
                    }
                    else
                    {
                        effectiveJaxbElements.put( e.getKey(), set );
                    }
                }
            }
        }
    }

    private Implementation getClassDeclaration( final Implementation implementation )
    {
        Implementation declaration = null;

        if ( implementation.getClazz() != null && !implementation.isClassDeclaration() )
        {
            find:
            for ( int i = 0, s0 = this.modules.getModule().size(); i < s0; i++ )
            {
                final Module candidateModule = this.modules.getModule().get( i );

                if ( candidateModule.getImplementations() != null )
                {
                    for ( int j = 0, s1 = candidateModule.getImplementations().getImplementation().size(); j < s1; j++ )
                    {
                        final Implementation candidate =
                            candidateModule.getImplementations().getImplementation().get( j );

                        if ( candidate.isClassDeclaration()
                                 && candidate.getClazz().equals( implementation.getClazz() ) )
                        {
                            declaration = candidate;
                            break find;
                        }
                    }
                }
            }
        }

        return declaration;
    }

    private static <T, K> void addNode( final Map<K, Set<Node<T>>> map, final Node<T> node, final K key )
    {
        Set<Node<T>> set = map.get( key );

        if ( set == null )
        {
            set = newSet();
            map.put( key, set );
        }

        set.add( node );
    }

    private static <T, K> void overrideNodes( final Map<String, Map<K, Set<Node<T>>>> effective,
                                              final Node<Implementation> implementation,
                                              final Map<K, Set<Node<T>>> directNodes )
    {
        for ( final Map.Entry<K, Set<Node<T>>> e : directNodes.entrySet() )
        {
            final Set<Node<T>> effectiveNodes =
                effectiveNodes( effective, implementation.getModelObject().getIdentifier(), e.getKey() );

            final Set<Node<T>> overridingNodes = newSet();

            for ( final Node<T> directNode : e.getValue() )
            {
                for ( final Iterator<Node<T>> it = effectiveNodes.iterator(); it.hasNext(); )
                {
                    final Node<T> effectiveNode = it.next();

                    if ( isOverriding( effectiveNode, directNode ) )
                    {
                        it.remove();

                        if ( directNode != effectiveNode )
                        {
                            directNode.getModifiableOverriddenNodes().add( effectiveNode );
                        }
                    }
                }

                boolean overriddenByAncestor = false;

                if ( directNode.getSpecification() != null )
                {
                    for ( final Node<T> effectiveNode : effectiveNodes )
                    {
                        if ( effectiveNode.getSpecification() == null )
                        {
                            overriddenByAncestor = true;
                            effectiveNode.getModifiableOverriddenNodes().add( directNode );
                        }
                    }
                }

                if ( !overriddenByAncestor )
                {
                    overridingNodes.add( directNode );
                }
            }

            effectiveNodes.addAll( overridingNodes );
        }
    }

    private static <K, V, T> Map<K, V> map( final Map<T, Map<K, V>> map, final T context )
    {
        Map<K, V> contextMap = map.get( context );

        if ( contextMap == null )
        {
            contextMap = newMap();
            map.put( context, contextMap );
        }

        return contextMap;
    }

    private static <K, V> Set<Node<V>> nodes( final Map<K, Set<Node<V>>> map, final K key )
    {
        Set<Node<V>> nodes = map.get( key );

        if ( nodes == null )
        {
            nodes = newSet();
            map.put( key, nodes );
        }

        return nodes;
    }

    private static <K, V> Set<Node<V>> effectiveNodes( final Map<String, Map<K, Set<Node<V>>>> map,
                                                       final String context, final K key )
    {
        return nodes( map( map, context ), key );
    }

    private static <T, K> void inheritNodes(
        final Map<String, Map<K, Set<Node<T>>>> effective, final Map<K, Set<Node<T>>> ancestor,
        final Node<Implementation> descendant )
    {
        for ( Map.Entry<K, Set<Node<T>>> e : ancestor.entrySet() )
        {
            for ( final Node<T> inherit : e.getValue() )
            {
                if ( isInheritableNode( inherit ) )
                {
                    effectiveNodes( effective, descendant.getModelObject().getIdentifier(), e.getKey() ).add( inherit );
                }
            }
        }
    }

    private static <T, K> Map<K, Set<Node<T>>> getDirectEffectiveNodes( final Map<K, Set<Node<T>>> map,
                                                                        final String origin )
    {
        final Map<K, Set<Node<T>>> declarationMap = newMap( map.size() );

        for ( final Map.Entry<K, Set<Node<T>>> e : map.entrySet() )
        {
            final Set<Node<T>> set = nodes( declarationMap, e.getKey() );

            for ( final Node<T> n : e.getValue() )
            {
                if ( isDirectEffectiveNode( n, origin ) )
                {
                    set.add( n );
                }
            }

            for ( final Node<T> n : e.getValue() )
            {
                if ( isDirectSpecifiedNode( n, origin ) )
                {
                    boolean add = true;

                    for ( final Node<T> override : set )
                    {
                        if ( override.getSpecification() == null )
                        {
                            override.getModifiableOverriddenNodes().add( n );
                            add = false;
                        }
                    }

                    if ( add )
                    {
                        set.add( n );
                    }
                }
            }
        }

        return declarationMap;
    }

    private static <T, K> Map<K, Set<Node<T>>> getEffectiveNodes(
        final Map<String, Map<String, Map<K, Set<Node<T>>>>> effective, final String context,
        final String implementation )
    {
        return map( effective, context ).get( implementation );
    }

    private static boolean isDirectNode( final Node<?> node, final String implementation )
    {
        return implementation.equals( node.getImplementation().getIdentifier() );
    }

    private static boolean isDirectEffectiveNode( final Node<?> node, final String implementation )
    {
        return isDirectNode( node, implementation ) && node.getClassDeclaration() == null
                   && node.getSpecification() == null;

    }

    private static boolean isDirectSpecifiedNode( final Node<?> node, final String implementation )
    {
        return isDirectNode( node, implementation ) && node.getClassDeclaration() == null
                   && node.getSpecification() != null;

    }

    private static boolean isOverriding( final Node<?> node, final Node<?> override )
    {
        if ( override.getSpecification() != null )
        {
            if ( node.getSpecification() == null )
            {
                return false;
            }
            else if ( !override.getSpecification().getIdentifier().equals( node.getSpecification().getIdentifier() ) )
            {
                return false;
            }
        }

        return true;
    }

    private static boolean isInheritableNode( final Node<?> node )
    {
        return node.getClassDeclaration() == null;
    }

    private static <K, V> Map<K, V> newMap()
    {
        return new HashMap<K, V>();
    }

    private static <K, V> Map<K, V> newMap( final int initialCapacity )
    {
        return new HashMap<K, V>( initialCapacity );
    }

    private static <T> Set<T> newSet()
    {
        return new HashSet<T>();
    }

    private static <T> Set<T> newSet( final int initialCapacity )
    {
        return new HashSet<T>( initialCapacity );
    }

    private static <T> Set<T> newSet( final Collection<? extends T> col )
    {
        return new HashSet<T>( col );
    }

    private static <T> Set<T> unmodifiableSet( final Set<T> set )
    {
        return set != null ? Collections.unmodifiableSet( set ) : Collections.<T>emptySet();
    }

    private static QName getXmlElementName( final Element element )
    {
        if ( element.getNamespaceURI() != null )
        {
            return new QName( element.getNamespaceURI(), element.getLocalName() );
        }
        else
        {
            return new QName( element.getLocalName() );
        }
    }

}
