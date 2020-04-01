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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Stream;
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
        private final List<Node<Implementation>> path = new CopyOnWriteArrayList<>();

        /**
         * The nodes overridden by the node.
         */
        private final Set<Node<T>> overriddenNodes = newSet();

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
        private List<Node<Implementation>> getModifiablePath()
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
        this.modules = Objects.requireNonNull( modules, "modules" ).clone();
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
        this.prepareContext( Objects.requireNonNull( implementation, "implementation" ) );
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
        this.prepareContext( Objects.requireNonNull( implementation, "implementation" ) );
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
        this.prepareContext( Objects.requireNonNull( implementation, "implementation" ) );
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
        Objects.requireNonNull( name, "name" );
        this.prepareContext( Objects.requireNonNull( implementation, "implementation" ) );
        Set<Node<Dependency>> set = null;

        final Map<String, Set<Node<Dependency>>> map =
            effectiveNodes( this.effDependencies, implementation, implementation );

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
        this.prepareContext( Objects.requireNonNull( implementation, "implementation" ) );
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
        Objects.requireNonNull( identifier, "identifier" );
        this.prepareContext( Objects.requireNonNull( implementation, "implementation" ) );
        Set<Node<ImplementationReference>> set = null;
        final Map<String, Set<Node<ImplementationReference>>> map =
            effectiveNodes( this.effImplReferences, implementation, implementation );

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
        this.prepareContext( Objects.requireNonNull( implementation, "implementation" ) );
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
        Objects.requireNonNull( name, "name" );
        this.prepareContext( Objects.requireNonNull( implementation, "implementation" ) );
        Set<Node<JAXBElement<?>>> set = null;
        final Map<QName, Set<Node<JAXBElement<?>>>> map =
            effectiveNodes( this.effJaxbElements, implementation, implementation );

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
        this.prepareContext( Objects.requireNonNull( implementation, "implementation" ) );
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
        Objects.requireNonNull( name, "name" );
        this.prepareContext( Objects.requireNonNull( implementation, "implementation" ) );
        Set<Node<Message>> set = null;
        final Map<String, Set<Node<Message>>> map = effectiveNodes( this.effMessages, implementation, implementation );

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
        this.prepareContext( Objects.requireNonNull( implementation, "implementation" ) );
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
        Objects.requireNonNull( name, "name" );
        this.prepareContext( Objects.requireNonNull( implementation, "implementation" ) );
        Set<Node<Property>> set = null;
        final Map<String, Set<Node<Property>>> map =
            effectiveNodes( this.effProperties, implementation, implementation );

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
        this.prepareContext( Objects.requireNonNull( implementation, "implementation" ) );
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
        Objects.requireNonNull( identifier, "identifier" );
        this.prepareContext( Objects.requireNonNull( implementation, "implementation" ) );
        Set<Node<SpecificationReference>> set = null;
        final Map<String, Set<Node<SpecificationReference>>> map =
            effectiveNodes( this.effSpecReferences, implementation, implementation );

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
        this.prepareContext( Objects.requireNonNull( implementation, "implementation" ) );
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
        Objects.requireNonNull( name, "name" );
        this.prepareContext( Objects.requireNonNull( implementation, "implementation" ) );
        Set<Node<Element>> set = null;
        final Map<QName, Set<Node<Element>>> map =
            effectiveNodes( this.effXmlElements, implementation, implementation );

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

            final Optional<Implementation> i = this.modules.getImplementation( context );

            if ( i.isPresent() )
            {
                this.collectNodes( context, i.get(), null, null );
                map( this.sourceNodes, context ).values().forEach( n  -> this.collectEffectiveNodes( context, n ) );
            }

            state = ContextState.PREPARED;
            this.contextStates.put( context, state );
        }

        assert state == ContextState.PREPARED :
            "Unexpected context state '" + state + "' for context '" + context + "'.";

    }

    private void collectNodes( final String context, final Implementation declaration,
                               final Node<Implementation> descendant, final LinkedList<Node<Implementation>> path )
    {
        final LinkedList<Node<Implementation>> currentPath = path == null ? new LinkedList<>() : path;
        final Map<String, Node<Implementation>> contextImplementations = map( this.implementations, context );

        if ( declaration != null && !contextImplementations.containsKey( declaration.getIdentifier() ) )
        {
            final Optional<Module> moduleOfDeclaration =
                this.modules.getModuleOfImplementation( declaration.getIdentifier() );

            final Node<Implementation> declarationNode =
                new Node<>( declaration, null, null, descendant, declaration, declaration.isFinal(), false );

            declarationNode.getModifiablePath().addAll( currentPath );

            contextImplementations.put( declaration.getIdentifier(), declarationNode );

            currentPath.addLast( declarationNode );

            if ( declaration.getDependencies() != null )
            {
                collectNodes( map( this.dependencies, context ), declaration.getDependencies().getDependency(),
                              currentPath, null, declaration, descendant, d  -> d.getName() );

            }

            if ( declaration.getMessages() != null )
            {
                collectNodes( map( this.messages, context ), declaration.getMessages().getMessage(),
                              currentPath, null, declaration, descendant, m  -> m.getName() );

                if ( !declaration.getMessages().getReference().isEmpty() && moduleOfDeclaration.isPresent()
                         && moduleOfDeclaration.get().getMessages() != null )
                {
                    collectNodes( map( this.messages, context ), declaration.getMessages().getReference(), currentPath,
                                  declaration, descendant,
                                  r  -> moduleOfDeclaration.get().getMessages().getMessage( r.getName() ),
                                  ( o, r )  ->
                              {
                                  final Message msg = o.clone();
                                  msg.setFinal( r.isFinal() );
                                  msg.setOverride( r.isOverride() );
                                  return msg;
                              }, m  -> m.getName() );

                }
            }

            if ( declaration.getProperties() != null )
            {
                collectNodes( map( this.properties, context ), declaration.getProperties().getProperty(),
                              currentPath, null, declaration, descendant, p  -> p.getName() );

                if ( !declaration.getProperties().getReference().isEmpty() && moduleOfDeclaration.isPresent()
                         && moduleOfDeclaration.get().getProperties() != null )
                {
                    collectNodes( map( this.properties, context ), declaration.getProperties().getReference(),
                                  currentPath, declaration, descendant,
                                  r  -> moduleOfDeclaration.get().getProperties().getProperty( r.getName() ),
                                  ( o, r )  ->
                              {
                                  final Property p = o.clone();
                                  p.setFinal( r.isFinal() );
                                  p.setOverride( r.isOverride() );
                                  return p;
                              }, p  -> p.getName() );

                }
            }

            if ( declaration.getSpecifications() != null )
            {
                collectNodes( map( this.specReferences, context ), declaration.getSpecifications().getReference(),
                              currentPath, null, declaration, descendant, r  -> r.getIdentifier() );

                declaration.getSpecifications().getReference().forEach( ref  ->
                {
                    final Optional<Specification> s = this.modules.getSpecification( ref.getIdentifier() );

                    if ( s.isPresent() && s.get().getProperties() != null )
                    {
                        collectNodes( map( this.properties, context ), s.get().getProperties().getProperty(),
                                      currentPath, s.get(), declaration, descendant, p  -> p.getName() );

                    }
                } );
            }

            if ( !declaration.getAny().isEmpty() )
            {
                collectNodes( map( this.xmlElements, context ), declaration.getAny(),
                              any  ->
                          {
                              Node<Element> node = null;

                              if ( any instanceof Element )
                              {
                                  node = new Node<>( declaration, null, null, descendant, (Element) any, false, false );
                                  node.getModifiablePath().addAll( currentPath );
                              }

                              return node;
                          }, o  -> getXmlElementName( o ) );

                collectNodes( map( this.jaxbElements, context ), declaration.getAny(),
                              any  ->
                          {
                              Node<JAXBElement<?>> node = null;

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

                                  node = new Node<>( declaration, null, null, descendant, e, _final, override );
                                  node.getModifiablePath().addAll( currentPath );
                              }

                              return node;
                          }, o  -> o.getName() );

            }

            if ( declaration.getImplementations() != null
                     && !declaration.getImplementations().getReference().isEmpty() )
            {
                boolean all_cyclic = true;

                for ( int i = 0, s0 = declaration.getImplementations().getReference().size(); i < s0; i++ )
                {
                    final ImplementationReference r = declaration.getImplementations().getReference().get( i );
                    final Node<ImplementationReference> node =
                        new Node<>( declaration, null, null, descendant, r, r.isFinal(), r.isOverride() );

                    node.getModifiablePath().addAll( currentPath );

                    final Optional<Implementation> ancestor = this.modules.getImplementation( r.getIdentifier() );

                    boolean cycle = false;
                    if ( ancestor.isPresent() && contextImplementations.containsKey( ancestor.get().getIdentifier() ) )
                    {
                        for ( int j = 0, s1 = currentPath.size(); j < s1; j++ )
                        {
                            final Node<Implementation> n = currentPath.get( j );

                            if ( n.getModelObject().getIdentifier().equals( ancestor.get().getIdentifier() ) )
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
                        final Map<String, Set<Node<ImplementationReference>>> implementationReferenceNodes =
                            map( this.implReferences, context );

                        addNode( implementationReferenceNodes, node, node.getModelObject().getIdentifier() );

                        if ( ancestor.isPresent() )
                        {
                            this.collectNodes( context, ancestor.get(), declarationNode, currentPath );
                        }
                    }
                }

                if ( all_cyclic )
                {
                    final Map<String, Node<Implementation>> srcNodes = map( this.sourceNodes, context );
                    srcNodes.put( declarationNode.getModelObject().getIdentifier(), declarationNode );
                }
            }
            else
            {
                final Map<String, Node<Implementation>> srcNodes = map( this.sourceNodes, context );
                srcNodes.put( declarationNode.getModelObject().getIdentifier(), declarationNode );
            }

            currentPath.removeLast();
        }
    }

    private static <T extends Inheritable, K> void collectNodes( final Map<K, Set<Node<T>>> collectedNodes,
                                                                 final Collection<T> declaredModelObjects,
                                                                 final Collection<Node<Implementation>> path,
                                                                 final Specification specification,
                                                                 final Implementation declaration,
                                                                 final Node<Implementation> descendant,
                                                                 final Function<T, K> modelObjectKeyFunction )
    {
        try ( final Stream<T> st0 = declaredModelObjects.parallelStream().unordered() )
        {
            st0.map( o  ->
            {
                final Node<T> node = new Node<>( declaration, specification, null, descendant, o, o.isFinal(),
                                                 o.isOverride() );

                node.getModifiablePath().addAll( path );
                return node;
            } ).forEach( n  ->
            {
                final K modelObjectKey = modelObjectKeyFunction.apply( n.getModelObject() );
                addNode( collectedNodes, n, modelObjectKey );
            } );
        }
    }

    private static <R extends Inheritable, T, K> void collectNodes(
        final Map<K, Set<Node<T>>> collectedNodes, final Collection<R> declaredReferenceModelObjects,
        final Collection<Node<Implementation>> path, final Implementation declaration,
        final Node<Implementation> descendant, final Function<R, Optional<T>> findModelObjectFunction,
        final BiFunction<T, R, T> inheritanceAttributesFunction, final Function<T, K> modelObjectKeyFunction )
    {
        try ( final Stream<R> st0 = declaredReferenceModelObjects.parallelStream().unordered() )
        {
            st0.map( r  ->
            {
                Node<T> node = null;
                final Optional<T> modelObject = findModelObjectFunction.apply( r );

                if ( modelObject.isPresent() )
                {
                    node = new Node<>( declaration, null, null, descendant,
                                       inheritanceAttributesFunction.apply( modelObject.get(), r ),
                                       r.isFinal(), r.isOverride() );

                    node.getModifiablePath().addAll( path );
                }

                return node;
            } ).filter( n  -> n != null ).forEach( n  ->
            {
                final K modelObjectKey = modelObjectKeyFunction.apply( n.getModelObject() );
                addNode( collectedNodes, n, modelObjectKey );
            } );
        }
    }

    private static <T, K, D> void collectNodes(
        final Map<K, Set<Node<T>>> collectedNodes, final Collection<D> declaredModelObjects,
        final Function<D, Node<T>> mapFunction, final Function<T, K> modelObjectKeyFunction )
    {
        try ( final Stream<D> st0 = declaredModelObjects.parallelStream().unordered() )
        {
            st0.map( mapFunction ).filter( n  -> n != null ).forEach( n  ->
            {
                final K modelObjectKey = modelObjectKeyFunction.apply( n.getModelObject() );
                addNode( collectedNodes, n, modelObjectKey );
            } );
        }
    }

    private void collectEffectiveNodes( final String context, final Node<Implementation> node )
    {
        final Map<String, Set<Node<SpecificationReference>>> directSpecificationReferences =
            directEffectiveNodes( map( this.specReferences, context ), node.getModelObject().getIdentifier() );

        final Map<String, Set<Node<Dependency>>> directDependencies =
            directEffectiveNodes( map( this.dependencies, context ), node.getModelObject().getIdentifier() );

        final Map<String, Set<Node<Message>>> directMessages =
            directEffectiveNodes( map( this.messages, context ), node.getModelObject().getIdentifier() );

        final Map<String, Set<Node<Property>>> directProperties =
            directEffectiveNodes( map( this.properties, context ), node.getModelObject().getIdentifier() );

        final Map<String, Set<Node<ImplementationReference>>> directImplementationReferences =
            directEffectiveNodes( map( this.implReferences, context ), node.getModelObject().getIdentifier() );

        final Map<QName, Set<Node<Element>>> directXmlElements =
            directEffectiveNodes( map( this.xmlElements, context ), node.getModelObject().getIdentifier() );

        final Map<QName, Set<Node<JAXBElement<?>>>> directJaxbElements =
            directEffectiveNodes( map( this.jaxbElements, context ), node.getModelObject().getIdentifier() );

        overrideNodes( map( this.effSpecReferences, context ), node, directSpecificationReferences );
        overrideNodes( map( this.effImplReferences, context ), node, directImplementationReferences );
        overrideNodes( map( this.effDependencies, context ), node, directDependencies );
        overrideNodes( map( this.effMessages, context ), node, directMessages );
        overrideNodes( map( this.effProperties, context ), node, directProperties );
        overrideNodes( map( this.effJaxbElements, context ), node, directJaxbElements );
        overrideNodes( map( this.effXmlElements, context ), node, directXmlElements );

        this.addClassDeclarationNodes( context, node );

        final Map<String, Set<Node<SpecificationReference>>> ancestorSpecificationReferences =
            effectiveNodes( this.effSpecReferences, context, node.getModelObject().getIdentifier() );

        final Map<String, Set<Node<Dependency>>> ancestorDependencies =
            effectiveNodes( this.effDependencies, context, node.getModelObject().getIdentifier() );

        final Map<String, Set<Node<Message>>> ancestorMessages =
            effectiveNodes( this.effMessages, context, node.getModelObject().getIdentifier() );

        final Map<String, Set<Node<Property>>> ancestorProperties =
            effectiveNodes( this.effProperties, context, node.getModelObject().getIdentifier() );

        final Map<String, Set<Node<ImplementationReference>>> ancestorImplementationReferences =
            effectiveNodes( this.effImplReferences, context, node.getModelObject().getIdentifier() );

        final Map<QName, Set<Node<Element>>> ancestorXmlElements =
            effectiveNodes( this.effXmlElements, context, node.getModelObject().getIdentifier() );

        final Map<QName, Set<Node<JAXBElement<?>>>> ancestorJaxbElements =
            effectiveNodes( this.effJaxbElements, context, node.getModelObject().getIdentifier() );

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

            addClassDeclarationNodes( context, node.getModelObject(), classDeclaration, this.dependencies,
                                      this.effDependencies );

            addClassDeclarationNodes( context, node.getModelObject(), classDeclaration, this.specReferences,
                                      this.effSpecReferences );

            addClassDeclarationNodes( context, node.getModelObject(), classDeclaration, this.messages,
                                      this.effMessages );

            addClassDeclarationNodes( context, node.getModelObject(), classDeclaration, this.properties,
                                      this.effProperties );

            addClassDeclarationNodes( context, node.getModelObject(), classDeclaration, this.xmlElements,
                                      this.effXmlElements );

            addClassDeclarationNodes( context, node.getModelObject(), classDeclaration, this.jaxbElements,
                                      this.effJaxbElements );

        }
    }

    private static <T, K> void addClassDeclarationNodes(
        final String context, final Implementation implementation, final Implementation classDeclaration,
        final Map<String, Map<K, Set<Node<T>>>> collectedNodes,
        final Map<String, Map<String, Map<K, Set<Node<T>>>>> effectiveNodes )
    {
        final Map<K, Set<Node<T>>> declNodes =
            effectiveNodes( effectiveNodes, classDeclaration.getIdentifier(), classDeclaration.getIdentifier() );

        final Map<K, Set<Node<T>>> effNodes =
            effectiveNodes( effectiveNodes, context, implementation.getIdentifier() );

        addClassDeclarationNodes( map( collectedNodes, context ), effNodes, declNodes, implementation,
                                  classDeclaration );

    }

    private static <T, K> void addClassDeclarationNodes( final Map<K, Set<Node<T>>> collectedNodes,
                                                         final Map<K, Set<Node<T>>> effectiveNodes,
                                                         final Map<K, Set<Node<T>>> declaredNodes,
                                                         final Implementation implementation,
                                                         final Implementation classDeclaration )
    {
        try ( final Stream<Map.Entry<K, Set<Node<T>>>> st0 = declaredNodes.entrySet().parallelStream().unordered() )
        {
            st0.forEach( e  ->
            {
                try ( final Stream<Node<T>> st1 = e.getValue().parallelStream().unordered() )
                {
                    final Set<Node<T>> nodes = st1.map( n  ->
                    {
                        final Node<T> effNode =
                            new Node<>( implementation, n.getSpecification(), classDeclaration,
                                        null, n.getModelObject(), n.isFinal(), n.isOverride() );

                        effNode.getModifiablePath().addAll( n.getPath() );
                        return effNode;
                    } ).collect( Collector.of( CopyOnWriteArraySet::new, Set::add, ( s1, s2 )  ->
                                           {
                                               s1.addAll( s2 );
                                               return s1;
                                           }, Collector.Characteristics.CONCURRENT,
                                               Collector.Characteristics.UNORDERED ) );

                    nodes.forEach( n  -> addNode( collectedNodes, n, e.getKey() ) );

                    final Set<Node<T>> effNodes = effectiveNodes.putIfAbsent( e.getKey(), nodes );

                    if ( effNodes != null )
                    {
                        effNodes.forEach( n  ->
                        {
                            n.getModifiableOverriddenNodes().addAll( nodes );
                        } );
                    }
                }
            } );
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
        map.computeIfAbsent( key, k  -> newSet() ).add( node );
    }

    private static <T, K> void overrideNodes( final Map<String, Map<K, Set<Node<T>>>> effective,
                                              final Node<Implementation> implementation,
                                              final Map<K, Set<Node<T>>> directNodes )
    {
        directNodes.entrySet().forEach( e  ->
        {
            final Set<Node<T>> effectiveNodes =
                effectiveNodes( effective, implementation.getModelObject().getIdentifier(), e.getKey() );

            final Set<Node<T>> overridingNodes = newSet();

            try ( final Stream<Node<T>> st0 = e.getValue().parallelStream().unordered() )
            {
                st0.forEach( directNode  ->
                {
                    final Collection<Node<T>> overriddenNodes = newSet();

                    try ( final Stream<Node<T>> st1 = effectiveNodes.parallelStream().unordered() )
                    {
                        st1.filter( n  -> isOverriding( n, directNode ) ).
                            map( n  ->
                            {
                                overriddenNodes.add( n );
                                return n;
                            } ).
                            filter( n  -> n != directNode ).
                            forEach( n  -> directNode.getModifiableOverriddenNodes().add( n ) );

                    }

                    effectiveNodes.removeAll( overriddenNodes );

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
                } );
            }

            effectiveNodes.addAll( overridingNodes );
        } );
    }

    private static <K, V, T> Map<K, V> map( final Map<T, Map<K, V>> map, final T context )
    {
        return map.computeIfAbsent( context, k  -> newMap() );
    }

    private static <K, V> Set<Node<V>> nodes( final Map<K, Set<Node<V>>> map, final K key )
    {
        return map.computeIfAbsent( key, k  -> newSet() );
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
        try ( final Stream<Map.Entry<K, Set<Node<T>>>> st0 = ancestor.entrySet().parallelStream().unordered() )
        {
            st0.forEach( e  ->
            {
                try ( final Stream<Node<T>> st1 = e.getValue().parallelStream().unordered() )
                {
                    final Set<Node<T>> nodes = effectiveNodes( effective, descendant.getModelObject().getIdentifier(),
                                                               e.getKey() );

                    nodes.addAll( st1.filter( n  -> isInheritableNode( n ) ).collect( Collector.of(
                        CopyOnWriteArrayList::new, List::add, ( l1, l2 )  ->
                    {
                        l1.addAll( l2 );
                        return l1;
                    }, Collector.Characteristics.CONCURRENT, Collector.Characteristics.UNORDERED ) ) );
                }
            } );
        }
    }

    private static <T, K> Map<K, Set<Node<T>>> directEffectiveNodes( final Map<K, Set<Node<T>>> map,
                                                                     final String origin )
    {
        final Map<K, Set<Node<T>>> declarationMap = newMap( map.size() );

        try ( final Stream<Map.Entry<K, Set<Node<T>>>> st0 = map.entrySet().parallelStream().unordered() )
        {
            st0.forEach( e  ->
            {
                final Set<Node<T>> set = nodes( declarationMap, e.getKey() );

                try ( final Stream<Node<T>> st1 = e.getValue().parallelStream().unordered() )
                {
                    set.addAll( st1.filter( n  -> isDirectEffectiveNode( n, origin ) ).
                        collect( Collector.of( CopyOnWriteArrayList::new, List::add, ( l1, l2 )  ->
                                           {
                                               l1.addAll( l2 );
                                               return l1;
                                           }, Collector.Characteristics.CONCURRENT,
                                               Collector.Characteristics.UNORDERED ) ) );

                }

                try ( final Stream<Node<T>> st1 = e.getValue().parallelStream().unordered() )
                {
                    set.addAll( st1.filter( n  -> isDirectSpecifiedNode( n, origin ) ).
                        filter( n  ->
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

                            return add;
                        } ).collect( Collector.of( CopyOnWriteArrayList::new, List::add, ( l1, l2 )  ->
                                               {
                                                   l1.addAll( l2 );
                                                   return l1;
                                               }, Collector.Characteristics.CONCURRENT,
                                                   Collector.Characteristics.UNORDERED ) ) );

                }
            } );
        }

        return declarationMap;
    }

    private static <T, K> Map<K, Set<Node<T>>> effectiveNodes(
        final Map<String, Map<String, Map<K, Set<Node<T>>>>> effective, final String context,
        final String implementation )
    {
        return map( effective, context ).computeIfAbsent( implementation, k  -> newMap() );
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
        return new ConcurrentHashMap<>( 1024 );
    }

    private static <K, V> Map<K, V> newMap( final int initialCapacity )
    {
        return new ConcurrentHashMap<>( initialCapacity );
    }

    private static <T> Set<T> newSet()
    {
        return new CopyOnWriteArraySet<>();
    }

    private static <T> Set<T> newSet( final Collection<? extends T> col )
    {
        final Set<T> set = new CopyOnWriteArraySet<>();
        set.addAll( col );
        return set;
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
