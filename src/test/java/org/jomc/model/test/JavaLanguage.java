/*
 *   Copyright (C) Christian Schulte, 2012-262
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

import java.util.HashSet;
import java.util.Set;

/**
 * Java language support.
 *
 * @author <a href="mailto:cs@schulte.it">Christian Schulte</a>
 * @version $JOMC$
 * @since 1.4
 */
class JavaLanguage
{

    /** Creates a new {@code JavaLanguage} instance. */
    JavaLanguage()
    {
        super();
    }

    /**
     * The Java Language Specification - Java SE 7 Edition - Chapter 18. Syntax
     * <pre>
     * BasicType:
     *      byte
     *      short
     *      char
     *      int
     *      long
     *      float
     *      double
     * </pre>
     */
    static final Set<String> BASIC_TYPES = new HashSet<String>( 8 );

    /**
     * The Java Language Specification - Java SE 7 Edition - Chapter 3.10.3. Boolean Literals
     * <pre>
     * BooleanLiteral: one of
     *      true false
     * </pre>
     */
    static final Set<String> BOOLEAN_LITERALS = new HashSet<String>( 2 );

    /**
     * The Java Language Specification - Java SE 7 Edition - Chapter 3.9. Keywords
     * <pre>
     * Keyword: one of
     *      abstract   continue   for          new         switch
     *      assert     default    if           package     synchronized
     *      boolean    do         goto         private     this
     *      break      double     implements   protected   throw
     *      byte       else       import       public      throws
     *      case       enum       instanceof   return      transient
     *      catch      extends    int          short       try
     *      char       final      interface    static      void
     *      class      finally    long         strictfp    volatile
     *      const      float      native       super       while
     * </pre>
     */
    static final Set<String> KEYWORDS = new HashSet<String>( 50 );

    /**
     * The Java Language Specification - Java SE 7 Edition - Chapter 3.10.7. The Null Literal
     * <pre>
     * NullLiteral:
     *      null
     * </pre>
     */
    static final String NULL_LITERAL = "null";

    static
    {
        // JLS - Java SE 7 Edition - Chapter 18. Syntax - BasicType
        BASIC_TYPES.add( "boolean" );
        BASIC_TYPES.add( "byte" );
        BASIC_TYPES.add( "char" );
        BASIC_TYPES.add( "double" );
        BASIC_TYPES.add( "float" );
        BASIC_TYPES.add( "short" );
        BASIC_TYPES.add( "int" );
        BASIC_TYPES.add( "long" );

        // JLS - Java SE 7 Edition - 3.10.3. Boolean Literals
        BOOLEAN_LITERALS.add( "true" );
        BOOLEAN_LITERALS.add( "false" );

        // JLS - Java SE 7 Edition - Chapter 3. Lexical Structure - 3.9. Keywords
        KEYWORDS.add( "abstract" );
        KEYWORDS.add( "assert" );
        KEYWORDS.add( "boolean" );
        KEYWORDS.add( "break" );
        KEYWORDS.add( "byte" );
        KEYWORDS.add( "case" );
        KEYWORDS.add( "catch" );
        KEYWORDS.add( "char" );
        KEYWORDS.add( "class" );
        KEYWORDS.add( "const" );
        KEYWORDS.add( "continue" );
        KEYWORDS.add( "default" );
        KEYWORDS.add( "do" );
        KEYWORDS.add( "double" );
        KEYWORDS.add( "else" );
        KEYWORDS.add( "enum" );
        KEYWORDS.add( "extends" );
        KEYWORDS.add( "final" );
        KEYWORDS.add( "finally" );
        KEYWORDS.add( "float" );
        KEYWORDS.add( "for" );
        KEYWORDS.add( "if" );
        KEYWORDS.add( "goto" );
        KEYWORDS.add( "implements" );
        KEYWORDS.add( "import" );
        KEYWORDS.add( "instanceof" );
        KEYWORDS.add( "int" );
        KEYWORDS.add( "interface" );
        KEYWORDS.add( "long" );
        KEYWORDS.add( "native" );
        KEYWORDS.add( "new" );
        KEYWORDS.add( "package" );
        KEYWORDS.add( "private" );
        KEYWORDS.add( "protected" );
        KEYWORDS.add( "public" );
        KEYWORDS.add( "return" );
        KEYWORDS.add( "short" );
        KEYWORDS.add( "static" );
        KEYWORDS.add( "strictfp" );
        KEYWORDS.add( "super" );
        KEYWORDS.add( "switch" );
        KEYWORDS.add( "synchronized" );
        KEYWORDS.add( "this" );
        KEYWORDS.add( "throw" );
        KEYWORDS.add( "throws" );
        KEYWORDS.add( "transient" );
        KEYWORDS.add( "try" );
        KEYWORDS.add( "void" );
        KEYWORDS.add( "volatile" );
        KEYWORDS.add( "while" );
    }

}
