/*
 *   Copyright (C) Christian Schulte, 2012-253
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * Data type of a Java identifier.
 * <p>This class provides support for parsing and normalizing text to java identifiers as specified in the Java
 * Language Specification - Java SE 7 Edition - Chapter 3.8ff.</p>
 *
 * @author <a href="mailto:cs@schulte.it">Christian Schulte</a>
 * @version $JOMC$
 * @see #normalize(java.lang.String, org.jomc.model.JavaIdentifier.NormalizationMode)
 * @see #parse(java.lang.String)
 * @see #valueOf(java.lang.String)
 * @since 1.4
 */
public final class JavaIdentifier implements CharSequence, Serializable
{

    /**
     * Normalization modes.
     *
     * @author <a href="mailto:cs@schulte.it">Christian Schulte</a>
     * @version $JOMC$
     * @since 1.4
     * @see JavaIdentifier#normalize(java.lang.String, org.jomc.model.JavaIdentifier.NormalizationMode)
     */
    public static enum NormalizationMode
    {

        /** Mode to normalize by compacting words using camel-case. */
        CAMEL_CASE,
        /** Mode to normalize by separating words using '_' and by converting all characters to lower-case. */
        LOWER_CASE,
        /** Mode to normalize by separating words using '_' and by converting all characters to upper-case. */
        UPPER_CASE,
        /**
         * Mode to normalize according to the
         * <cite>Code Conventions for the Java Programming Language - 9 - Naming Conventions - Constants</cite>.
         * <blockquote>
         * The names of variables declared class constants and of ANSI constants should be all uppercase with words
         * separated by underscores ("_"). (ANSI constants should be avoided, for ease of debugging.)
         * </blockquote>
         */
        CONSTANT_NAME_CONVENTION,
        /**
         * Mode to normalize according to the
         * <cite>Code Conventions for the Java Programming Language - 9 - Naming Conventions - Methods</cite>.
         * <blockquote>
         * Methods should be verbs, in mixed case with the first letter lowercase, with the first letter of each
         * internal word capitalized.
         * </blockquote>
         */
        METHOD_NAME_CONVENTION,
        /**
         * Mode to normalize according to the
         * <cite>Code Conventions for the Java Programming Language - 9 - Naming Conventions - Variables</cite>.
         * <blockquote>
         * Except for variables, all instance, class, and class constants are in mixed case with a lowercase first
         * letter. Internal words start with capital letters. Variable names should not start with underscore _ or
         * dollar sign $ characters, even though both are allowed. Variable names should be short yet meaningful. The
         * choice of a variable name should be mnemonic - that is - designed to indicate to the casual observer the
         * intent of its use. One-character variable names should be avoided except for temporary "throwaway" variables.
         * Common names for temporary variables are i, j, k, m, and n for integers; c, d, and e for characters.
         * </blockquote>
         */
        VARIABLE_NAME_CONVENTION

    }

    /**
     * The value of the instance.
     * @serial
     */
    private String identifier;

    /** Cached instances. */
    private static volatile Reference<Map<CacheKey, JavaIdentifier>> cache;

    /** Serial version UID for backwards compatibility with 1.4.x object streams. */
    private static final long serialVersionUID = 7600377999055800720L;

    /** Underscore character. */
    private static final int UNDERSCORE_CODEPOINT = Character.codePointAt( "_", 0 );

    /** Creates a new {@code JavaIdentifier} instance. */
    private JavaIdentifier()
    {
        super();
    }

    /**
     * Returns the length of this character sequence.
     *
     * @return The number of {@code char}s in this sequence.
     */
    public int length()
    {
        return this.identifier.length();
    }

    /**
     * Returns the {@code char} value at a given index.
     *
     * @param index The index of the {@code char} value to return.
     *
     * @return The {@code char} value at {@code index}.
     *
     * @throws IndexOutOfBoundsException if {@code index} is negative or not less than the length of the sequence.
     */
    public char charAt( final int index )
    {
        return this.identifier.charAt( index );
    }

    /**
     * Returns a new {@code CharSequence} that is a subsequence of this sequence.
     *
     * @param start The start index, inclusive.
     * @param end The end index, exclusive.
     *
     * @return The sequence of characters starting at index {@code start} up to index {@code end - 1}.
     *
     * @throws IndexOutOfBoundsException if {@code start} or {@code end} are negative, if {@code end} is greater than
     * the length of the sequence, or if {@code start} is greater than {@code end}.
     */
    public CharSequence subSequence( final int start, final int end )
    {
        return this.identifier.subSequence( start, end );
    }

    /**
     * Returns a string containing the characters in this sequence in the same order as this sequence. The length of the
     * string will be the length of this sequence.
     *
     * @return A string consisting of exactly this sequence of characters.
     */
    @Override
    public String toString()
    {
        return this.identifier;
    }

    /**
     * Returns the hash-code value of the object.
     *
     * @return The hash-code value of the object.
     */
    @Override
    public int hashCode()
    {
        return this.identifier.hashCode();
    }

    /**
     * Tests whether some other object is equal to the object.
     *
     * @param o The object to test.
     *
     * @return {@code true}, if {@code o} is an instance of the class of the object and its string value is equal to the
     * string value of the object.
     */
    @Override
    public boolean equals( final Object o )
    {
        boolean equal = o == this;

        if ( !equal && o instanceof JavaIdentifier )
        {
            equal = this.toString().equals( o.toString() );
        }

        return equal;
    }

    /**
     * Normalizes text from the beginning of the given string to produce a {@code JavaIdentifier}.
     *
     * @param text The text to normalize.
     * @param mode The normalization to apply.
     *
     * @return A {@code JavaIdentifier} instance constructed by normalizing {@code text} according to {@code mode}.
     *
     * @throws NullPointerException if {@code text} or {@code mode} is {@code null}.
     * @throws ParseException if normalization fails.
     */
    public static JavaIdentifier normalize( final String text, final NormalizationMode mode ) throws ParseException
    {
        if ( text == null )
        {
            throw new NullPointerException( "text" );
        }
        if ( mode == null )
        {
            throw new NullPointerException( "mode" );
        }

        return parse( text, mode, false );
    }

    /**
     * Parses text from the beginning of a given string to produce a {@code JavaIdentifier} instance.
     *
     * @param text The text to parse.
     *
     * @return A {@code JavaIdentifier} instance constructed by parsing {@code text}.
     *
     * @throws NullPointerException if {@code text} is {@code null}.
     * @throws ParseException if parsing fails.
     *
     * @see #valueOf(java.lang.String)
     */
    public static JavaIdentifier parse( final String text ) throws ParseException
    {
        if ( text == null )
        {
            throw new NullPointerException( "text" );
        }

        return parse( text, null, false );
    }

    /**
     * Parses text from the beginning of a given string to produce a {@code JavaIdentifier} instance.
     * <p>Unlike the {@link #parse(String)} method, this method throws an {@code IllegalArgumentException} if parsing
     * fails.</p>
     *
     * @param text The text to parse.
     *
     * @return A {@code JavaIdentifier} instance constructed by parsing {@code text}.
     *
     * @throws NullPointerException if {@code text} is {@code null}.
     * @throws IllegalArgumentException if parsing fails.
     *
     * @see #parse(java.lang.String)
     */
    public static JavaIdentifier valueOf( final String text ) throws IllegalArgumentException
    {
        if ( text == null )
        {
            throw new NullPointerException( "text" );
        }

        try
        {
            return parse( text, null, true );
        }
        catch ( final ParseException e )
        {
            throw new AssertionError( e );
        }
    }

    private static JavaIdentifier parse( final String text, final NormalizationMode mode,
                                         final boolean runtimeException )
        throws ParseException
    {
        Map<CacheKey, JavaIdentifier> map = cache == null ? null : cache.get();

        if ( map == null )
        {
            map = new HashMap<CacheKey, JavaIdentifier>( 128 );
            cache = new SoftReference<Map<CacheKey, JavaIdentifier>>( map );
        }

        synchronized ( map )
        {
            final CacheKey key = new CacheKey( text, mode );
            JavaIdentifier javaIdentifier = map.get( key );

            if ( javaIdentifier == null )
            {
                javaIdentifier = new JavaIdentifier();
                parseIdentifier( javaIdentifier, text, mode, runtimeException );

                if ( mode != null )
                {
                    final CacheKey normalizedKey = new CacheKey( javaIdentifier.toString(), mode );
                    final JavaIdentifier normalizedInstance = map.get( normalizedKey );

                    if ( normalizedInstance != null )
                    {
                        map.put( key, normalizedInstance );
                        javaIdentifier = normalizedInstance;
                    }
                    else
                    {
                        map.put( key, javaIdentifier );
                        map.put( normalizedKey, javaIdentifier );
                    }
                }
                else
                {
                    map.put( key, javaIdentifier );
                }
            }

            return javaIdentifier;
        }
    }

    private static void parseIdentifier( final JavaIdentifier t, final String text, final NormalizationMode mode,
                                         final boolean runtimeException )
        throws ParseException
    {
        if ( text.length() <= 0 )
        {
            if ( runtimeException )
            {
                throw new IllegalArgumentException( getMessage( "invalidEmptyString" ) );
            }
            else
            {
                throw new ParseException( getMessage( "invalidEmptyString" ), 0 );
            }
        }

        final StringBuilder identifierBuilder = new StringBuilder( text.length() );
        final List<Integer> retainedIndices = new ArrayList<Integer>( text.length() );
        boolean start_of_word = true;
        int words = 0;

        for ( int i = 0, j = 1, s0 = text.length(), last_codepoint = -1; i < s0; i++, j++ )
        {
            if ( !isWordSeparator( text.codePointAt( i ), mode, identifierBuilder.length() <= 0 ) )
            {
                if ( mode != null )
                {
                    switch ( mode )
                    {
                        case CAMEL_CASE:
                            if ( start_of_word )
                            {
                                identifierBuilder.append( Character.toUpperCase( text.charAt( i ) ) );
                            }
                            else if ( last_codepoint > -1 && j < s0
                                      && isCamelCase( last_codepoint, text.codePointAt( i ), text.codePointAt( j ) ) )
                            { // Retain camel-case in words.
                                identifierBuilder.append( text.charAt( i ) );
                                retainedIndices.add( identifierBuilder.length() - 1 );
                            }
                            else
                            {
                                identifierBuilder.append( Character.toLowerCase( text.charAt( i ) ) );
                            }
                            break;

                        case LOWER_CASE:
                            if ( start_of_word && last_codepoint > -1 && last_codepoint != UNDERSCORE_CODEPOINT )
                            {
                                identifierBuilder.append( Character.toChars( UNDERSCORE_CODEPOINT ) );
                            }

                            identifierBuilder.append( Character.toLowerCase( text.charAt( i ) ) );
                            break;

                        case UPPER_CASE:
                        case CONSTANT_NAME_CONVENTION:
                            if ( start_of_word && last_codepoint > -1 && last_codepoint != UNDERSCORE_CODEPOINT )
                            {
                                identifierBuilder.append( Character.toChars( UNDERSCORE_CODEPOINT ) );
                            }

                            identifierBuilder.append( Character.toUpperCase( text.charAt( i ) ) );
                            break;

                        case VARIABLE_NAME_CONVENTION:
                        case METHOD_NAME_CONVENTION:
                            if ( start_of_word )
                            {
                                identifierBuilder.append( words == 0 ? Character.toLowerCase( text.charAt( i ) )
                                                          : Character.toUpperCase( text.charAt( i ) ) );

                            }
                            else if ( last_codepoint > -1 && j < s0
                                      && isCamelCase( last_codepoint, text.codePointAt( i ), text.codePointAt( j ) ) )
                            { // Retain camel-case in words.
                                identifierBuilder.append( text.charAt( i ) );
                                retainedIndices.add( identifierBuilder.length() - 1 );
                            }
                            else
                            {
                                identifierBuilder.append( Character.toLowerCase( text.charAt( i ) ) );
                            }
                            break;

                        default:
                            throw new AssertionError( mode );

                    }
                }
                else
                {
                    identifierBuilder.append( text.charAt( i ) );
                }

                last_codepoint = identifierBuilder.codePointAt( identifierBuilder.length() - 1 );
                start_of_word = false;
            }
            else
            {
                if ( mode != null )
                {
                    if ( !start_of_word )
                    {
                        start_of_word = true;
                        words++;
                    }
                }
                else if ( runtimeException )
                {
                    throw new IllegalArgumentException( getMessage( "invalidCharacter", text, text.charAt( i ), i ) );
                }
                else
                {
                    throw new ParseException( getMessage( "invalidCharacter", text, text.charAt( i ), i ), i );
                }
            }
        }

        if ( words > 0 )
        {
            // Multiple words - no camel-case retained in any word.
            toLowerCase( identifierBuilder, retainedIndices );
        }

        t.identifier = identifierBuilder.toString();

        if ( t.identifier.length() <= 0 )
        {
            if ( runtimeException )
            {
                throw new IllegalArgumentException( getMessage( "invalidCharacters", text ) );
            }
            else
            {
                throw new ParseException( getMessage( "invalidCharacters", text ), 0 );
            }
        }

        if ( JavaLanguage.KEYWORDS.contains( t.identifier )
             || JavaLanguage.BOOLEAN_LITERALS.contains( t.identifier )
             || JavaLanguage.NULL_LITERAL.equals( t.identifier ) )
        {
            if ( mode != null )
            {
                t.identifier = "_" + t.identifier;
            }
            else if ( runtimeException )
            {
                throw new IllegalArgumentException( getMessage( "invalidWord", text, t.identifier,
                                                                text.indexOf( t.identifier ) ) );

            }
            else
            {
                throw new ParseException( getMessage( "invalidWord", text, t.identifier, text.indexOf( t.identifier ) ),
                                          text.indexOf( t.identifier ) );

            }
        }
    }

    private static boolean isWordSeparator( final int codePoint, final NormalizationMode mode, final boolean first )
    {
        return !( ( first ? Character.isJavaIdentifierStart( codePoint ) : Character.isJavaIdentifierPart( codePoint ) )
                  && ( mode != null ? Character.isLetterOrDigit( codePoint ) : true ) );

    }

    private static boolean isCamelCase( final int left, final int middle, final int right )
    {
        return Character.isLowerCase( left ) && Character.isUpperCase( middle ) && Character.isLowerCase( right );
    }

    private static void toLowerCase( final StringBuilder stringBuilder, final List<Integer> indices )
    {
        for ( int i = 0, s0 = indices.size(); i < s0; i++ )
        {
            final int index = indices.get( i );
            final int cp = Character.toLowerCase( stringBuilder.codePointAt( index ) );
            stringBuilder.replace( index, index + 1, String.valueOf( Character.toChars( cp ) ) );
        }
    }

    private static String getMessage( final String key, final Object... args )
    {
        return MessageFormat.format( ResourceBundle.getBundle(
            JavaIdentifier.class.getName().replace( '.', '/' ), Locale.getDefault() ).
            getString( key ), args );

    }

    private static final class CacheKey
    {

        private final String text;

        private final NormalizationMode mode;

        private CacheKey( final String text, final NormalizationMode mode )
        {
            super();
            this.text = text;
            this.mode = mode;
        }

        @Override
        public int hashCode()
        {
            int hc = 23;
            hc = 37 * hc + this.text.hashCode();
            hc = 37 * hc + ( this.mode == null ? 0 : this.mode.hashCode() );
            return hc;
        }

        @Override
        public boolean equals( final Object o )
        {
            boolean equal = o == this;

            if ( !equal && o instanceof CacheKey )
            {
                final CacheKey that = (CacheKey) o;
                equal = this.mode == that.mode && this.text.equals( that.text );
            }

            return equal;
        }

    }

}
