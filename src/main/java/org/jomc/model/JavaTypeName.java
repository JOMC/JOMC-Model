/*
 *   Copyright (C) Christian Schulte <cs@schulte.it>, 2012-235
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

import java.io.Serializable;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.text.MessageFormat;
import java.text.ParseException;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * Data type of a Java type name.
 * <p>
 * This class supports parsing of Java type names as specified in the
 * Java Language Specification - Java SE 7 Edition - Chapters 3.8ff, 6.5 and 18.
 * </p>
 * <p>
 * <i>Please note that this class will move to package {@code org.jomc.jsl} in JOMC 2.0.</i>
 * </p>
 *
 * @author <a href="mailto:cs@schulte.it">Christian Schulte</a>
 * @version $JOMC$
 * @see #parse(java.lang.String)
 * @see #valueOf(java.lang.String)
 * @since 1.4
 */
public final class JavaTypeName implements Serializable
{

    /**
     * Data type of an argument of a parameterized Java type name.
     *
     * @author <a href="mailto:cs@schulte.it">Christian Schulte</a>
     * @version $JOMC$
     * @since 1.4
     */
    public static final class Argument implements Serializable
    {

        /**
         * Flag indicating the argument is a wildcard.
         *
         * @serial
         */
        private boolean wildcard;

        /**
         * The wildcard bounds of the argument.
         *
         * @serial
         */
        private String wildcardBounds;

        /**
         * The type name of the argument.
         *
         * @serial
         */
        private JavaTypeName typeName;

        /**
         * Cached string representation.
         */
        private transient String cachedString;

        /**
         * Serial version UID for backwards compatibility with 1.4.x object streams.
         */
        private static final long serialVersionUID = -6515267147665760819L;

        /**
         * Create a new {@code Argument} instance.
         */
        private Argument()
        {
            super();
        }

        /**
         * Gets a flag indicating the argument is a wildcard argument.
         *
         * @return {@code true}, if the argument is a wildcard argument; {@code false}, else.
         */
        public boolean isWildcard()
        {
            return this.wildcard;
        }

        /**
         * Gets the wildcard bounds of the argument.
         *
         * @return The wildcard bounds of the argument or {@code null}.
         */
        public String getWildcardBounds()
        {
            return this.wildcardBounds;
        }

        /**
         * Gets the type name of the argument.
         *
         * @return The type name of the argument or {@code null}, if the argument is a wildcard argument.
         */
        public JavaTypeName getTypeName()
        {
            return this.typeName;
        }

        /**
         * Creates a string representation of the instance.
         *
         * @return A string representation of the instance.
         */
        @Override
        public String toString()
        {
            if ( this.cachedString == null )
            {
                final StringBuilder builder = new StringBuilder( 128 );

                if ( this.isWildcard() )
                {
                    builder.append( "?" );

                    if ( this.getWildcardBounds() != null && this.getTypeName() != null )
                    {
                        builder.append( " " ).append( this.getWildcardBounds() ).append( " " ).
                            append( this.getTypeName() );

                    }
                }
                else
                {
                    builder.append( this.getTypeName() );
                }

                this.cachedString = builder.toString();
            }

            return this.cachedString;
        }

    }

    /**
     * Java type name of class {@code Boolean}.
     *
     * @see Boolean
     */
    public static final JavaTypeName BOOLEAN;

    /**
     * Java type name of basic type {@code boolean}.
     *
     * @see Boolean#TYPE
     */
    public static final JavaTypeName BOOLEAN_TYPE;

    /**
     * Java type name of class {@code Byte}.
     *
     * @see Byte
     */
    public static final JavaTypeName BYTE;

    /**
     * Java type name of basic type {@code byte}.
     *
     * @see Byte#TYPE
     */
    public static final JavaTypeName BYTE_TYPE;

    /**
     * Java type name of class {@code Character}.
     *
     * @see Character
     */
    public static final JavaTypeName CHARACTER;

    /**
     * Java type name of basic type {@code char}.
     *
     * @see Character#TYPE
     */
    public static final JavaTypeName CHARACTER_TYPE;

    /**
     * Java type name of class {@code Double}.
     *
     * @see Double
     */
    public static final JavaTypeName DOUBLE;

    /**
     * Java type name of basic type {@code double}.
     *
     * @see Double#TYPE
     */
    public static final JavaTypeName DOUBLE_TYPE;

    /**
     * Java type name of class {@code Float}.
     *
     * @see Float
     */
    public static final JavaTypeName FLOAT;

    /**
     * Java type name of basic type {@code float}.
     *
     * @see Float#TYPE
     */
    public static final JavaTypeName FLOAT_TYPE;

    /**
     * Java type name of class {@code Integer}.
     *
     * @see Integer
     */
    public static final JavaTypeName INTEGER;

    /**
     * Java type name of basic type {@code int}.
     *
     * @see Integer#TYPE
     */
    public static final JavaTypeName INTEGER_TYPE;

    /**
     * Java type name of class {@code Long}.
     *
     * @see Long
     */
    public static final JavaTypeName LONG;

    /**
     * Java type name of basic type {@code long}.
     *
     * @see Long#TYPE
     */
    public static final JavaTypeName LONG_TYPE;

    /**
     * Java type name of class {@code Short}.
     *
     * @see Short
     */
    public static final JavaTypeName SHORT;

    /**
     * Java type name of basic type {@code short}.
     *
     * @see Short#TYPE
     */
    public static final JavaTypeName SHORT_TYPE;

    /**
     * The array dimension of the type name.
     *
     * @serial
     */
    private int dimension;

    /**
     * The flag indicating the type name denotes a primitive type.
     *
     * @serial
     */
    private boolean primitive;

    /**
     * The class name of the type name.
     *
     * @serial
     */
    private String className;

    /**
     * The qualified package name of the type name.
     *
     * @serial
     */
    private String packageName;

    /**
     * The qualified name of the type name.
     *
     * @serial
     */
    private String qualifiedName;

    /**
     * The simple name of the type name.
     *
     * @serial
     */
    private String simpleName;

    /**
     * The arguments of the type name.
     *
     * @serial
     */
    private volatile List<Argument> arguments;

    /**
     * Cached string representation.
     */
    private transient String cachedString;

    /**
     * Cached instances.
     */
    private static volatile Reference<Map<String, JavaTypeName>> cache;

    /**
     * Mappings of basic type name to class name encoding.
     */
    private static final Map<String, String> CLASSNAME_ENCODINGS = new HashMap<String, String>( 8 );

    /**
     * Serial version UID for backwards compatibility with 1.4.x object streams.
     */
    private static final long serialVersionUID = -4258949347035910249L;

    static
    {
        CLASSNAME_ENCODINGS.put( "boolean", "Z" );
        CLASSNAME_ENCODINGS.put( "byte", "B" );
        CLASSNAME_ENCODINGS.put( "char", "C" );
        CLASSNAME_ENCODINGS.put( "double", "D" );
        CLASSNAME_ENCODINGS.put( "float", "F" );
        CLASSNAME_ENCODINGS.put( "int", "I" );
        CLASSNAME_ENCODINGS.put( "long", "J" );
        CLASSNAME_ENCODINGS.put( "short", "S" );

        BOOLEAN = JavaTypeName.valueOf( Boolean.class.getName() );
        BOOLEAN_TYPE = JavaTypeName.valueOf( Boolean.TYPE.getName() );
        BYTE = JavaTypeName.valueOf( Byte.class.getName() );
        BYTE_TYPE = JavaTypeName.valueOf( Byte.TYPE.getName() );
        CHARACTER = JavaTypeName.valueOf( Character.class.getName() );
        CHARACTER_TYPE = JavaTypeName.valueOf( Character.TYPE.getName() );
        DOUBLE = JavaTypeName.valueOf( Double.class.getName() );
        DOUBLE_TYPE = JavaTypeName.valueOf( Double.TYPE.getName() );
        FLOAT = JavaTypeName.valueOf( Float.class.getName() );
        FLOAT_TYPE = JavaTypeName.valueOf( Float.TYPE.getName() );
        INTEGER = JavaTypeName.valueOf( Integer.class.getName() );
        INTEGER_TYPE = JavaTypeName.valueOf( Integer.TYPE.getName() );
        LONG = JavaTypeName.valueOf( Long.class.getName() );
        LONG_TYPE = JavaTypeName.valueOf( Long.TYPE.getName() );
        SHORT = JavaTypeName.valueOf( Short.class.getName() );
        SHORT_TYPE = JavaTypeName.valueOf( Short.TYPE.getName() );
    }

    /**
     * Creates a new {@code JavaTypeName} instance.
     */
    private JavaTypeName()
    {
        super();
    }

    /**
     * Gets the {@code Class} object of the type using a given class loader.
     *
     * @param classLoader The class loader to use for loading the {@code Class} object to return or {@code null}, to
     * load that {@code Class} object using the platform's bootstrap class loader.
     * @param initialize Flag indicating initialization to be performed on the loaded {@code Class} object.
     *
     * @return The {@code Class} object of the type.
     *
     * @throws ClassNotFoundException if the {@code Class} object of the type is not found searching
     * {@code classLoader}.
     *
     * @see Class#forName(java.lang.String, boolean, java.lang.ClassLoader)
     */
    public Class<?> getClass( final ClassLoader classLoader, final boolean initialize ) throws ClassNotFoundException
    {
        Class<?> javaClass = null;

        if ( this.isArray() )
        {
            javaClass = Class.forName( this.getClassName(), initialize, classLoader );
        }
        else if ( this.isPrimitive() )
        {
            if ( BOOLEAN_TYPE.equals( this ) )
            {
                javaClass = Boolean.TYPE;
            }
            else if ( BYTE_TYPE.equals( this ) )
            {
                javaClass = Byte.TYPE;
            }
            else if ( CHARACTER_TYPE.equals( this ) )
            {
                javaClass = Character.TYPE;
            }
            else if ( DOUBLE_TYPE.equals( this ) )
            {
                javaClass = Double.TYPE;
            }
            else if ( FLOAT_TYPE.equals( this ) )
            {
                javaClass = Float.TYPE;
            }
            else if ( INTEGER_TYPE.equals( this ) )
            {
                javaClass = Integer.TYPE;
            }
            else if ( LONG_TYPE.equals( this ) )
            {
                javaClass = Long.TYPE;
            }
            else if ( SHORT_TYPE.equals( this ) )
            {
                javaClass = Short.TYPE;
            }
            else
            {
                throw new AssertionError( this );
            }
        }
        else
        {
            javaClass = Class.forName( this.getClassName(), initialize, classLoader );
        }

        return javaClass;
    }

    /**
     * Gets the arguments of the type name.
     *
     * @return An unmodifiable list holding the arguments of the type name.
     */
    public List<Argument> getArguments()
    {
        if ( this.arguments == null )
        {
            this.arguments = new ArrayList<Argument>();
        }

        return this.arguments;
    }

    /**
     * Gets a flag indicating the type name denotes an array type.
     *
     * @return {@code true}, if the type name denotes an array type; {@code false}, else.
     *
     * @see Class#isArray()
     */
    public boolean isArray()
    {
        return this.dimension > 0;
    }

    /**
     * Gets a flag indicating the type name denotes a primitive type.
     *
     * @return {@code true}, if the type name denotes a primitive type; {@code false}, else.
     *
     * @see Class#isPrimitive()
     */
    public boolean isPrimitive()
    {
        return this.primitive;
    }

    /**
     * Gets a flag indicating the type name denotes a wrapper type of a primitive type.
     *
     * @return {@code true}, if the type name denotes a wrapper type of a primitive type; {@code false}, else.
     */
    public boolean isUnboxable()
    {
        // The Java Language Specification - Java SE 7 Edition - 5.1.8. Unboxing Conversion
        return BOOLEAN.equals( this )
                   || BYTE.equals( this )
                   || SHORT.equals( this )
                   || CHARACTER.equals( this )
                   || INTEGER.equals( this )
                   || LONG.equals( this )
                   || FLOAT.equals( this )
                   || DOUBLE.equals( this );

    }

    /**
     * Gets the type name.
     *
     * @param qualified {@code true}, to return a qualified name; {@code false}, to return a simple name.
     *
     * @return The type name.
     */
    public String getName( final boolean qualified )
    {
        return qualified
                   ? this.toString()
                   : this.getPackageName().length() > 0
                         ? this.toString().substring( this.getPackageName().length() + 1 )
                         : this.toString();

    }

    /**
     * Gets the class name of the type name.
     *
     * @return The class name of the type name.
     *
     * @see Class#getName()
     * @see Class#forName(java.lang.String)
     */
    public String getClassName()
    {
        return this.className;
    }

    /**
     * Gets the fully qualified package name of the type name.
     *
     * @return The fully qualified package name of the type name or an empty string, if the type name denotes a type
     * located in an unnamed package.
     *
     * @see #isUnnamedPackage()
     */
    public String getPackageName()
    {
        return this.packageName;
    }

    /**
     * Gets a flag indicating the type name denotes a type located in an unnamed package.
     *
     * @return {@code true}, if the type name denotes a type located in an unnamed package; {@code false}, else.
     *
     * @see #getPackageName()
     */
    public boolean isUnnamedPackage()
    {
        return this.getPackageName().length() == 0;
    }

    /**
     * Gets the fully qualified name of the type name.
     *
     * @return The fully qualified name of the type name.
     */
    public String getQualifiedName()
    {
        return this.qualifiedName;
    }

    /**
     * Gets the simple name of the type name.
     *
     * @return The simple name of the type name.
     */
    public String getSimpleName()
    {
        return this.simpleName;
    }

    /**
     * Gets the type name applying a boxing conversion.
     *
     * @return The converted type name or {@code null}, if the instance cannot be converted.
     *
     * @see #isArray()
     * @see #isPrimitive()
     */
    public JavaTypeName getBoxedName()
    {
        JavaTypeName boxedName = null;

        // The Java Language Specification - Java SE 7 Edition - 5.1.7. Boxing Conversion
        if ( BOOLEAN_TYPE.equals( this ) )
        {
            boxedName = BOOLEAN;
        }
        else if ( BYTE_TYPE.equals( this ) )
        {
            boxedName = BYTE;
        }
        else if ( SHORT_TYPE.equals( this ) )
        {
            boxedName = SHORT;
        }
        else if ( CHARACTER_TYPE.equals( this ) )
        {
            boxedName = CHARACTER;
        }
        else if ( INTEGER_TYPE.equals( this ) )
        {
            boxedName = INTEGER;
        }
        else if ( LONG_TYPE.equals( this ) )
        {
            boxedName = LONG;
        }
        else if ( FLOAT_TYPE.equals( this ) )
        {
            boxedName = FLOAT;
        }
        else if ( DOUBLE_TYPE.equals( this ) )
        {
            boxedName = DOUBLE;
        }

        return boxedName;
    }

    /**
     * Gets the type name applying an unboxing conversion.
     *
     * @return The converted type name or {@code null}, if the instance cannot be converted.
     *
     * @see #isUnboxable()
     */
    public JavaTypeName getUnboxedName()
    {
        JavaTypeName unboxedName = null;

        // The Java Language Specification - Java SE 7 Edition - 5.1.8. Unboxing Conversion
        if ( BOOLEAN.equals( this ) )
        {
            unboxedName = BOOLEAN_TYPE;
        }
        else if ( BYTE.equals( this ) )
        {
            unboxedName = BYTE_TYPE;
        }
        else if ( SHORT.equals( this ) )
        {
            unboxedName = SHORT_TYPE;
        }
        else if ( CHARACTER.equals( this ) )
        {
            unboxedName = CHARACTER_TYPE;
        }
        else if ( INTEGER.equals( this ) )
        {
            unboxedName = INTEGER_TYPE;
        }
        else if ( LONG.equals( this ) )
        {
            unboxedName = LONG_TYPE;
        }
        else if ( FLOAT.equals( this ) )
        {
            unboxedName = FLOAT_TYPE;
        }
        else if ( DOUBLE.equals( this ) )
        {
            unboxedName = DOUBLE_TYPE;
        }

        return unboxedName;
    }

    /**
     * Creates a string representation of the instance.
     *
     * @return A string representation of the instance.
     */
    @Override
    public String toString()
    {
        if ( this.cachedString == null )
        {
            final StringBuilder builder = new StringBuilder( this.getQualifiedName() );

            if ( !this.getArguments().isEmpty() )
            {
                builder.append( "<" );

                for ( int i = 0, s0 = this.getArguments().size(); i < s0; i++ )
                {
                    builder.append( this.getArguments().get( i ) ).append( ", " );
                }

                builder.setLength( builder.length() - 2 );
                builder.append( ">" );
            }

            if ( this.isArray() )
            {
                final int idx = this.getQualifiedName().length() - this.dimension * "[]".length();
                builder.append( builder.substring( idx, this.getQualifiedName().length() ) );
                builder.delete( idx, this.getQualifiedName().length() );
            }

            this.cachedString = builder.toString();
        }

        return this.cachedString;
    }

    /**
     * Gets the hash code value of the object.
     *
     * @return The hash code value of the object.
     */
    @Override
    public int hashCode()
    {
        return this.toString().hashCode();
    }

    /**
     * Tests whether another object is compile-time equal to this object.
     *
     * @param o The object to compare.
     *
     * @return {@code true}, if {@code o} denotes the same compile-time type name than the object; {@code false}, else.
     */
    @Override
    public boolean equals( final Object o )
    {
        boolean equal = o == this;

        if ( !equal && o instanceof JavaTypeName )
        {
            equal = this.toString().equals( o.toString() );
        }

        return equal;
    }

    /**
     * Tests whether another object is runtime equal to this object.
     *
     * @param o The object to compare.
     *
     * @return {@code true}, if {@code o} denotes the same runtime type name than the object; {@code false}, else.
     */
    public boolean runtimeEquals( final Object o )
    {
        boolean equal = o == this;

        if ( !equal && o instanceof JavaTypeName )
        {
            final JavaTypeName that = (JavaTypeName) o;
            equal = this.getClassName().equals( that.getClassName() );
        }

        return equal;
    }

    /**
     * Parses text from the beginning of the given string to produce a {@code JavaTypeName} instance.
     *
     * @param text The text to parse.
     *
     * @return A {@code JavaTypeName} instance corresponding to {@code text}.
     *
     * @throws NullPointerException if {@code text} is {@code null}.
     * @throws ParseException if parsing fails.
     *
     * @see #valueOf(java.lang.String)
     */
    public static JavaTypeName parse( final String text ) throws ParseException
    {
        if ( text == null )
        {
            throw new NullPointerException( "text" );
        }

        return parse( text, false );
    }

    /**
     * Parses text from the beginning of the given string to produce a {@code JavaTypeName} instance.
     * <p>
     * Unlike the {@link #parse(String)} method, this method throws an {@code IllegalArgumentException} if parsing
     * fails.
     * </p>
     *
     * @param text The text to parse.
     *
     * @return A {@code JavaTypeName} instance corresponding to {@code text}.
     *
     * @throws NullPointerException if {@code text} is {@code null}.
     * @throws IllegalArgumentException if parsing fails.
     *
     * @see #parse(java.lang.String)
     */
    public static JavaTypeName valueOf( final String text ) throws IllegalArgumentException
    {
        if ( text == null )
        {
            throw new NullPointerException( "text" );
        }

        try
        {
            return parse( text, true );
        }
        catch ( final ParseException e )
        {
            throw new AssertionError( e );
        }
    }

    private static JavaTypeName parse( final String text, boolean runtimeException ) throws ParseException
    {
        Map<String, JavaTypeName> map = cache == null ? null : cache.get();

        if ( map == null )
        {
            map = new HashMap<String, JavaTypeName>( 128 );
            cache = new SoftReference<Map<String, JavaTypeName>>( map );
        }

        synchronized ( map )
        {
            JavaTypeName javaType = map.get( text );

            if ( javaType == null )
            {
                javaType = new JavaTypeName();
                parseType( javaType, text, runtimeException );

                javaType.arguments = javaType.arguments != null
                                         ? Collections.unmodifiableList( javaType.arguments )
                                         : Collections.<Argument>emptyList();

                final String name = javaType.getName( true );
                final JavaTypeName existingInstance = map.get( name );

                if ( existingInstance != null )
                {
                    map.put( text, existingInstance );
                    javaType = existingInstance;
                }
                else
                {
                    map.put( text, javaType );
                    map.put( name, javaType );
                }
            }

            return javaType;
        }
    }

    /**
     * JLS - Java SE 7 Edition - Chapter 18. Syntax
     * <pre>
     * Type:
     *     BasicType {[]}
     *     ReferenceType  {[]}
     * </pre>
     *
     * @see #parseReferenceType(org.jomc.model.JavaTypeName.Tokenizer, org.jomc.model.JavaTypeName, boolean, boolean)
     */
    private static void parseType( final JavaTypeName t, final String text, final boolean runtimeException )
        throws ParseException
    {
        final Tokenizer tokenizer = new Tokenizer( text, runtimeException );
        boolean basic_type_or_reference_type_seen = false;
        boolean lpar_seen = false;
        Token token;

        while ( ( token = tokenizer.next() ) != null )
        {
            switch ( token.getKind() )
            {
                case Tokenizer.TK_BASIC_TYPE:
                    if ( basic_type_or_reference_type_seen || !CLASSNAME_ENCODINGS.containsKey( token.getValue() ) )
                    {
                        if ( runtimeException )
                        {
                            throw createInvalidTokenIllegalArgumentException( tokenizer.input(), token );
                        }
                        else
                        {
                            throw createInvalidTokenParseException( tokenizer.input(), token );
                        }
                    }
                    basic_type_or_reference_type_seen = true;
                    t.className = token.getValue();
                    t.qualifiedName = token.getValue();
                    t.simpleName = token.getValue();
                    t.packageName = "";
                    t.primitive = true;
                    break;

                case Tokenizer.TK_IDENTIFIER:
                    if ( basic_type_or_reference_type_seen )
                    {
                        if ( runtimeException )
                        {
                            throw createInvalidTokenIllegalArgumentException( tokenizer.input(), token );
                        }
                        else
                        {
                            throw createInvalidTokenParseException( tokenizer.input(), token );
                        }
                    }
                    basic_type_or_reference_type_seen = true;
                    tokenizer.back();
                    parseReferenceType( tokenizer, t, false, runtimeException );
                    break;

                case Tokenizer.TK_LPAR:
                    if ( !basic_type_or_reference_type_seen || lpar_seen )
                    {
                        if ( runtimeException )
                        {
                            throw createInvalidTokenIllegalArgumentException( tokenizer.input(), token );
                        }
                        else
                        {
                            throw createInvalidTokenParseException( tokenizer.input(), token );
                        }
                    }
                    lpar_seen = true;
                    break;

                case Tokenizer.TK_RPAR:
                    if ( !( basic_type_or_reference_type_seen && lpar_seen ) )
                    {
                        if ( runtimeException )
                        {
                            throw createInvalidTokenIllegalArgumentException( tokenizer.input(), token );
                        }
                        else
                        {
                            throw createInvalidTokenParseException( tokenizer.input(), token );
                        }
                    }
                    lpar_seen = false;
                    t.dimension++;
                    t.className = "[" + t.className;
                    t.qualifiedName += "[]";
                    t.simpleName += "[]";
                    break;

                default:
                    if ( runtimeException )
                    {
                        throw createInvalidTokenIllegalArgumentException( tokenizer.input(), token );
                    }
                    else
                    {
                        throw createInvalidTokenParseException( tokenizer.input(), token );
                    }

            }
        }

        if ( !basic_type_or_reference_type_seen || lpar_seen )
        {
            if ( runtimeException )
            {
                throw createUnexpectedEndOfInputIllegalArgumentException( tokenizer.input(), tokenizer.length() );
            }
            else
            {
                throw createUnexpectedEndOfInputParseException( tokenizer.input(), tokenizer.length() );
            }
        }

        if ( t.dimension > 0 )
        {
            if ( t.primitive )
            {
                t.className = new StringBuilder( t.className.length() ).
                    append( t.className.substring( 0, t.dimension ) ).
                    append( CLASSNAME_ENCODINGS.get( t.className.substring( t.dimension ) ) ).toString();

            }
            else
            {
                t.className = new StringBuilder( t.className.length() ).
                    append( t.className.substring( 0, t.dimension ) ).
                    append( "L" ).append( t.className.substring( t.dimension ) ).append( ";" ).toString();

            }
        }

        t.arguments = Collections.unmodifiableList( t.getArguments() );
    }

    /**
     * JLS - Java SE 7 Edition - Chapter 18. Syntax
     * <pre>
     * ReferenceType:
     *      Identifier [TypeArguments] { . Identifier [TypeArguments] }
     * </pre>
     *
     * @see #parseTypeArguments(org.jomc.model.JavaTypeName.Tokenizer, org.jomc.model.JavaTypeName, boolean)
     */
    private static void parseReferenceType( final Tokenizer tokenizer, final JavaTypeName t,
                                            final boolean in_type_arguments, final boolean runtimeException )
        throws ParseException
    {
        final StringBuilder classNameBuilder = new StringBuilder( tokenizer.input().length() );
        final StringBuilder typeNameBuilder = new StringBuilder( tokenizer.input().length() );
        boolean identifier_seen = false;
        boolean type_arguments_seen = false;
        Token token;

        while ( ( token = tokenizer.next() ) != null )
        {
            switch ( token.getKind() )
            {
                case Tokenizer.TK_IDENTIFIER:
                    if ( identifier_seen || type_arguments_seen )
                    {
                        if ( runtimeException )
                        {
                            throw createInvalidTokenIllegalArgumentException( tokenizer.input(), token );
                        }
                        else
                        {
                            throw createInvalidTokenParseException( tokenizer.input(), token );
                        }
                    }
                    identifier_seen = true;
                    type_arguments_seen = false;
                    t.simpleName = token.getValue();
                    t.packageName = typeNameBuilder.length() > 0
                                        ? typeNameBuilder.substring( 0, typeNameBuilder.length() - 1 )
                                        : "";

                    classNameBuilder.append( token.getValue() );
                    typeNameBuilder.append( token.getValue() );
                    break;

                case Tokenizer.TK_DOT:
                    if ( !( identifier_seen || type_arguments_seen ) )
                    {
                        if ( runtimeException )
                        {
                            throw createInvalidTokenIllegalArgumentException( tokenizer.input(), token );
                        }
                        else
                        {
                            throw createInvalidTokenParseException( tokenizer.input(), token );
                        }
                    }
                    identifier_seen = false;
                    type_arguments_seen = false;
                    classNameBuilder.append( token.getValue() );
                    typeNameBuilder.append( token.getValue() );
                    break;

                case Tokenizer.TK_LT:
                    if ( !identifier_seen )
                    {
                        if ( runtimeException )
                        {
                            throw createInvalidTokenIllegalArgumentException( tokenizer.input(), token );
                        }
                        else
                        {
                            throw createInvalidTokenParseException( tokenizer.input(), token );
                        }
                    }
                    identifier_seen = false;
                    type_arguments_seen = true;
                    tokenizer.back();
                    parseTypeArguments( tokenizer, t, runtimeException );
                    break;

                case Tokenizer.TK_LPAR:
                    if ( !( identifier_seen || type_arguments_seen ) || in_type_arguments )
                    {
                        if ( runtimeException )
                        {
                            throw createInvalidTokenIllegalArgumentException( tokenizer.input(), token );
                        }
                        else
                        {
                            throw createInvalidTokenParseException( tokenizer.input(), token );
                        }
                    }
                    tokenizer.back();
                    t.className = classNameBuilder.toString();
                    t.qualifiedName = typeNameBuilder.toString();
                    return;

                case Tokenizer.TK_COMMA:
                case Tokenizer.TK_GT:
                    if ( !( identifier_seen || type_arguments_seen ) || !in_type_arguments )
                    {
                        if ( runtimeException )
                        {
                            throw createInvalidTokenIllegalArgumentException( tokenizer.input(), token );
                        }
                        else
                        {
                            throw createInvalidTokenParseException( tokenizer.input(), token );
                        }
                    }
                    tokenizer.back();
                    t.className = classNameBuilder.toString();
                    t.qualifiedName = typeNameBuilder.toString();
                    return;

                default:
                    if ( runtimeException )
                    {
                        throw createInvalidTokenIllegalArgumentException( tokenizer.input(), token );
                    }
                    else
                    {
                        throw createInvalidTokenParseException( tokenizer.input(), token );
                    }

            }
        }

        if ( !( identifier_seen || type_arguments_seen ) )
        {
            if ( runtimeException )
            {
                throw createUnexpectedEndOfInputIllegalArgumentException( tokenizer.input(), tokenizer.length() );
            }
            else
            {
                throw createUnexpectedEndOfInputParseException( tokenizer.input(), tokenizer.length() );
            }
        }

        t.className = classNameBuilder.toString();
        t.qualifiedName = typeNameBuilder.toString();
    }

    /**
     * JLS - Java SE 7 Edition - Chapter 18. Syntax
     * <pre>
     * TypeArguments:
     *      &lt; TypeArgument { , TypeArgument } &gt;
     * </pre>
     *
     * @see #parseTypeArgument(org.jomc.model.JavaTypeName.Tokenizer, org.jomc.model.JavaTypeName, boolean)
     */
    private static void parseTypeArguments( final Tokenizer tokenizer, final JavaTypeName t,
                                            final boolean runtimeException )
        throws ParseException
    {
        boolean lt_seen = false;
        boolean argument_seen = false;
        Token token;

        while ( ( token = tokenizer.next() ) != null )
        {
            switch ( token.getKind() )
            {
                case Tokenizer.TK_LT:
                    if ( lt_seen || argument_seen )
                    {
                        if ( runtimeException )
                        {
                            throw createInvalidTokenIllegalArgumentException( tokenizer.input(), token );
                        }
                        else
                        {
                            throw createInvalidTokenParseException( tokenizer.input(), token );
                        }
                    }
                    lt_seen = true;
                    argument_seen = false;
                    break;

                case Tokenizer.TK_GT:
                    if ( !argument_seen )
                    {
                        if ( runtimeException )
                        {
                            throw createInvalidTokenIllegalArgumentException( tokenizer.input(), token );
                        }
                        else
                        {
                            throw createInvalidTokenParseException( tokenizer.input(), token );
                        }
                    }
                    return;

                case Tokenizer.TK_COMMA:
                    if ( !argument_seen )
                    {
                        if ( runtimeException )
                        {
                            throw createInvalidTokenIllegalArgumentException( tokenizer.input(), token );
                        }
                        else
                        {
                            throw createInvalidTokenParseException( tokenizer.input(), token );
                        }
                    }
                    argument_seen = false;
                    break;

                case Tokenizer.TK_IDENTIFIER:
                    if ( !lt_seen || argument_seen )
                    {
                        if ( runtimeException )
                        {
                            throw createInvalidTokenIllegalArgumentException( tokenizer.input(), token );
                        }
                        else
                        {
                            throw createInvalidTokenParseException( tokenizer.input(), token );
                        }
                    }
                    argument_seen = true;
                    tokenizer.back();
                    parseTypeArgument( tokenizer, t, runtimeException );
                    break;

                case Tokenizer.TK_QM:
                    if ( !lt_seen || argument_seen )
                    {
                        if ( runtimeException )
                        {
                            throw createInvalidTokenIllegalArgumentException( tokenizer.input(), token );
                        }
                        else
                        {
                            throw createInvalidTokenParseException( tokenizer.input(), token );
                        }
                    }
                    argument_seen = true;
                    tokenizer.back();
                    parseTypeArgument( tokenizer, t, runtimeException );
                    break;

                default:
                    if ( runtimeException )
                    {
                        throw createInvalidTokenIllegalArgumentException( tokenizer.input(), token );
                    }
                    else
                    {
                        throw createInvalidTokenParseException( tokenizer.input(), token );
                    }

            }
        }

        if ( runtimeException )
        {
            throw createUnexpectedEndOfInputIllegalArgumentException( tokenizer.input(), tokenizer.length() );
        }
        else
        {
            throw createUnexpectedEndOfInputParseException( tokenizer.input(), tokenizer.length() );
        }
    }

    /**
     * <dl><dt>JLS - Java SE 7 Edition - Chapter 18. Syntax</dt>
     * <dd><pre>
     * TypeArgument:
     *      ReferenceType
     *      ? [ ( extends | super ) ReferenceType ]
     * </pre></dd>
     * <dt>JLS - Java SE 7 Edition - Chapter 4.5.1. Type Arguments and Wildcards</dt>
     * <dd><pre>
     * TypeArgument:
     *      ReferenceType
     *      Wildcard
     *
     * Wildcard:
     *      ? WildcardBounds<i>opt</i>
     *
     * WildcardBounds:
     *      extends ReferenceType
     *      super ReferenceType
     * </pre></dd></dl>
     */
    private static void parseTypeArgument( final Tokenizer tokenizer, final JavaTypeName t,
                                           final boolean runtimeException )
        throws ParseException
    {
        boolean qm_seen = false;
        boolean keyword_seen = false;
        Token token;

        final Argument argument = new Argument();
        t.getArguments().add( argument );

        while ( ( token = tokenizer.next() ) != null )
        {
            switch ( token.getKind() )
            {
                case Tokenizer.TK_IDENTIFIER:
                    if ( qm_seen && !keyword_seen )
                    {
                        if ( runtimeException )
                        {
                            throw createInvalidTokenIllegalArgumentException( tokenizer.input(), token );
                        }
                        else
                        {
                            throw createInvalidTokenParseException( tokenizer.input(), token );
                        }
                    }
                    tokenizer.back();
                    argument.typeName = new JavaTypeName();
                    parseReferenceType( tokenizer, argument.getTypeName(), true, runtimeException );
                    return;

                case Tokenizer.TK_QM:
                    if ( qm_seen )
                    {
                        if ( runtimeException )
                        {
                            throw createInvalidTokenIllegalArgumentException( tokenizer.input(), token );
                        }
                        else
                        {
                            throw createInvalidTokenParseException( tokenizer.input(), token );
                        }
                    }
                    qm_seen = true;
                    argument.wildcard = true;
                    break;

                case Tokenizer.TK_KEYWORD:
                    if ( !qm_seen || keyword_seen
                             || !( "extends".equals( token.getValue() ) || "super".equals( token.getValue() ) ) )
                    {
                        if ( runtimeException )
                        {
                            throw createInvalidTokenIllegalArgumentException( tokenizer.input(), token );
                        }
                        else
                        {
                            throw createInvalidTokenParseException( tokenizer.input(), token );
                        }
                    }
                    keyword_seen = true;
                    argument.wildcardBounds = token.getValue();
                    break;

                case Tokenizer.TK_COMMA:
                case Tokenizer.TK_GT:
                    if ( !qm_seen || keyword_seen )
                    {
                        if ( runtimeException )
                        {
                            throw createInvalidTokenIllegalArgumentException( tokenizer.input(), token );
                        }
                        else
                        {
                            throw createInvalidTokenParseException( tokenizer.input(), token );
                        }
                    }
                    tokenizer.back();
                    return;

                default:
                    if ( runtimeException )
                    {
                        throw createInvalidTokenIllegalArgumentException( tokenizer.input(), token );
                    }
                    else
                    {
                        throw createInvalidTokenParseException( tokenizer.input(), token );
                    }

            }
        }

        if ( runtimeException )
        {
            throw createUnexpectedEndOfInputIllegalArgumentException( tokenizer.input(), tokenizer.length() );
        }
        else
        {
            throw createUnexpectedEndOfInputParseException( tokenizer.input(), tokenizer.length() );
        }
    }

    private static ParseException createInvalidTokenParseException( final String input, final Token token )
    {
        if ( token.getValue().length() > 1 )
        {
            return new ParseException( getMessage( "invalidWord", input, token.getValue(),
                                                   token.getPosition() ), token.getPosition() );

        }
        else
        {
            return new ParseException( getMessage( "invalidCharacter", input, token.getValue(),
                                                   token.getPosition() ), token.getPosition() );

        }
    }

    private static IllegalArgumentException createInvalidTokenIllegalArgumentException( final String input,
                                                                                        final Token token )
    {
        if ( token.getValue().length() > 1 )
        {
            return new IllegalArgumentException( getMessage( "invalidWord", input, token.getValue(),
                                                             token.getPosition() ) );

        }
        else
        {
            return new IllegalArgumentException( getMessage( "invalidCharacter", input, token.getValue(),
                                                             token.getPosition() ) );

        }
    }

    private static ParseException createUnexpectedEndOfInputParseException( final String input,
                                                                            final int length )
    {
        return new ParseException( getMessage( "unexpectedEndOfInput", input, length ), length );
    }

    private static IllegalArgumentException createUnexpectedEndOfInputIllegalArgumentException( final String input,
                                                                                                final int length )
    {
        return new IllegalArgumentException( getMessage( "unexpectedEndOfInput", input, length ) );
    }

    private static String getMessage( final String key, final Object... args )
    {
        return MessageFormat.format( ResourceBundle.getBundle(
            JavaTypeName.class.getName().replace( '.', '/' ), Locale.getDefault() ).
            getString( key ), args );

    }

    private static final class Token
    {

        private int kind;

        private final int position;

        private final String value;

        private Token( final int kind, final int position, final String value )
        {
            super();
            this.kind = kind;
            this.position = position;
            this.value = value;
        }

        private int getKind()
        {
            return this.kind;
        }

        private int getPosition()
        {
            return this.position;
        }

        private String getValue()
        {
            return this.value;
        }

    }

    private static final class Tokenizer
    {

        private static final int TK_BASIC_TYPE = 1;

        private static final int TK_KEYWORD = 2;

        private static final int TK_LITERAL = 3;

        private static final int TK_IDENTIFIER = 4;

        private static final int TK_LPAR = 5;

        private static final int TK_RPAR = 6;

        private static final int TK_LT = 7;

        private static final int TK_GT = 8;

        private static final int TK_COMMA = 9;

        private static final int TK_DOT = 10;

        private static final int TK_QM = 11;

        private final String input;

        private int token;

        private final List<Token> tokens;

        private int length;

        private Tokenizer( final String input, final boolean runtimeException ) throws ParseException
        {
            super();
            this.input = input;
            this.token = 0;
            this.tokens = tokenize( input, runtimeException );

            if ( !this.tokens.isEmpty() )
            {
                final Token last = this.tokens.get( this.tokens.size() - 1 );
                this.length = last.getPosition() + last.getValue().length();
            }
        }

        private String input()
        {
            return this.input;
        }

        private Token next()
        {
            final int idx = this.token++;
            return idx < this.tokens.size() ? this.tokens.get( idx ) : null;
        }

        private void back()
        {
            this.token--;
        }

        private int length()
        {
            return this.length;
        }

        private static List<Token> tokenize( final String input, final boolean runtimeException )
            throws ParseException
        {
            final List<Token> list = new LinkedList<Token>();
            final ParsePosition pos = new ParsePosition( 0 );

            for ( Token t = nextToken( pos, input, runtimeException );
                  t != null;
                  t = nextToken( pos, input, runtimeException ) )
            {
                list.add( t );
            }

            return Collections.unmodifiableList( list );
        }

        private static Token nextToken( final ParsePosition pos, final String str, final boolean runtimeException )
            throws ParseException
        {
            for ( final int s0 = str.length(); pos.getIndex() < s0; pos.setIndex( pos.getIndex() + 1 ) )
            {
                if ( !Character.isWhitespace( str.charAt( pos.getIndex() ) ) )
                {
                    break;
                }
            }

            int idx = pos.getIndex();
            Token token = null;

            if ( idx < str.length() )
            {
                // Check separator characters.
                switch ( str.charAt( idx ) )
                {
                    case ',':
                        token = new Token( TK_COMMA, idx, "," );
                        pos.setIndex( idx + 1 );
                        break;
                    case '.':
                        token = new Token( TK_DOT, idx, "." );
                        pos.setIndex( idx + 1 );
                        break;
                    case '<':
                        token = new Token( TK_LT, idx, "<" );
                        pos.setIndex( idx + 1 );
                        break;
                    case '>':
                        token = new Token( TK_GT, idx, ">" );
                        pos.setIndex( idx + 1 );
                        break;
                    case '[':
                        token = new Token( TK_LPAR, idx, "[" );
                        pos.setIndex( idx + 1 );
                        break;
                    case ']':
                        token = new Token( TK_RPAR, idx, "]" );
                        pos.setIndex( idx + 1 );
                        break;
                    case '?':
                        token = new Token( TK_QM, idx, "?" );
                        pos.setIndex( idx + 1 );
                        break;

                    default:
                        token = null;

                }

                // Check basic type.
                if ( token == null )
                {
                    for ( final String basicType : JavaLanguage.BASIC_TYPES )
                    {
                        if ( str.substring( idx ).startsWith( basicType ) )
                        {
                            idx += basicType.length();

                            if ( idx >= str.length()
                                     || !Character.isJavaIdentifierPart( str.charAt( idx ) ) )
                            {
                                token = new Token( TK_BASIC_TYPE, pos.getIndex(), basicType );
                                pos.setIndex( idx );
                                break;
                            }

                            idx -= basicType.length();
                        }
                    }
                }

                // Check keyword.
                if ( token == null )
                {
                    for ( final String keyword : JavaLanguage.KEYWORDS )
                    {
                        if ( str.substring( idx ).startsWith( keyword ) )
                        {
                            idx += keyword.length();

                            if ( idx >= str.length()
                                     || !Character.isJavaIdentifierPart( str.charAt( idx ) ) )
                            {
                                token = new Token( TK_KEYWORD, pos.getIndex(), keyword );
                                pos.setIndex( idx );
                                break;
                            }

                            idx -= keyword.length();
                        }
                    }
                }

                // Check boolean literals.
                if ( token == null )
                {
                    for ( final String literal : JavaLanguage.BOOLEAN_LITERALS )
                    {
                        if ( str.substring( idx ).startsWith( literal ) )
                        {
                            idx += literal.length();

                            if ( idx >= str.length()
                                     || !Character.isJavaIdentifierPart( str.charAt( idx ) ) )
                            {
                                token = new Token( TK_LITERAL, pos.getIndex(), literal );
                                pos.setIndex( idx );
                                break;
                            }

                            idx -= literal.length();
                        }
                    }
                }

                // Check null literal.
                if ( token == null )
                {
                    if ( str.substring( idx ).startsWith( JavaLanguage.NULL_LITERAL ) )
                    {
                        idx += JavaLanguage.NULL_LITERAL.length();

                        if ( idx >= str.length()
                                 || !Character.isJavaIdentifierPart( str.charAt( idx ) ) )
                        {
                            token = new Token( TK_LITERAL, pos.getIndex(), JavaLanguage.NULL_LITERAL );
                            pos.setIndex( idx );
                        }
                        else
                        {
                            idx -= JavaLanguage.NULL_LITERAL.length();
                        }
                    }
                }

                // Check identifier.
                if ( token == null )
                {
                    for ( final int s0 = str.length(); idx < s0; idx++ )
                    {
                        if ( !( idx == pos.getIndex()
                                ? Character.isJavaIdentifierStart( str.charAt( idx ) )
                                : Character.isJavaIdentifierPart( str.charAt( idx ) ) ) )
                        {
                            break;
                        }
                    }

                    if ( idx != pos.getIndex() )
                    {
                        token = new Token( TK_IDENTIFIER, pos.getIndex(), str.substring( pos.getIndex(), idx ) );
                        pos.setIndex( idx );
                    }
                }

                if ( token == null )
                {
                    final Token invalidToken =
                        new Token( Integer.MIN_VALUE, idx, Character.toString( str.charAt( idx ) ) );

                    if ( runtimeException )
                    {
                        throw createInvalidTokenIllegalArgumentException( str, invalidToken );
                    }
                    else
                    {
                        throw createInvalidTokenParseException( str, invalidToken );
                    }
                }
            }

            return token;
        }

    }

}
