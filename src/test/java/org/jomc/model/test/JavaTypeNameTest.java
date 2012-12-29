/*
 *   Copyright (C) Christian Schulte, 2012-235
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

import java.io.ObjectInputStream;
import java.text.ParseException;
import org.jomc.model.JavaTypeName;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Test cases for class {@code org.jomc.model.JavaTypeName}.
 *
 * @author <a href="mailto:cs@schulte.it">Christian Schulte</a>
 * @version $JOMC$
 * @since 1.4
 */
public class JavaTypeNameTest
{

    /** Constant to prefix relative resource names with. */
    private static final String ABSOLUTE_RESOURCE_NAME_PREFIX = "/org/jomc/model/test/";

    /** Creates a new {@code JavaTypeNameTest} instance. */
    public JavaTypeNameTest()
    {
        super();
    }

    @Test
    public final void ThrowsNullPointerExceptionOnNullArgument() throws Exception
    {
        try
        {
            JavaTypeName.parse( null );
            fail( "Expected 'NullPointerException' not thrown." );
        }
        catch ( final NullPointerException e )
        {
            assertNotNull( e.getMessage() );
            System.out.println( e.getMessage() );
        }

        try
        {
            JavaTypeName.valueOf( null );
            fail( "Expected 'NullPointerException' not thrown." );
        }
        catch ( final NullPointerException e )
        {
            assertNotNull( e.getMessage() );
            System.out.println( e.getMessage() );
        }
    }

    @Test
    public final void ParsesBasicTypeNames() throws Exception
    {
        for ( final String basicType : JavaLanguage.BASIC_TYPES )
        {
            assertBasicTypeName( basicType );
        }
    }

    @Test
    public final void ParsesReferenceTypeNames() throws Exception
    {
        JavaTypeName javaTypeName = JavaTypeName.parse( "ReferenceType" );
        assertEquals( "ReferenceType", javaTypeName.getClassName() );
        assertEquals( "", javaTypeName.getPackageName() );
        assertEquals( "ReferenceType", javaTypeName.getQualifiedName() );
        assertEquals( "ReferenceType", javaTypeName.getSimpleName() );
        assertEquals( "ReferenceType", javaTypeName.toString() );
        assertFalse( javaTypeName.isArray() );
        assertFalse( javaTypeName.isPrimitive() );

        javaTypeName = JavaTypeName.parse( "validpackagename.ReferenceType" );
        assertEquals( "validpackagename.ReferenceType", javaTypeName.getClassName() );
        assertEquals( "validpackagename", javaTypeName.getPackageName() );
        assertEquals( "validpackagename.ReferenceType", javaTypeName.getQualifiedName() );
        assertEquals( "ReferenceType", javaTypeName.getSimpleName() );
        assertEquals( "validpackagename.ReferenceType", javaTypeName.toString() );
        assertFalse( javaTypeName.isArray() );
        assertFalse( javaTypeName.isPrimitive() );

        javaTypeName = JavaTypeName.parse( "ReferenceType<?>" );
        assertEquals( "ReferenceType", javaTypeName.getClassName() );
        assertEquals( "", javaTypeName.getPackageName() );
        assertEquals( "ReferenceType", javaTypeName.getQualifiedName() );
        assertEquals( "ReferenceType", javaTypeName.getSimpleName() );
        assertEquals( "ReferenceType<?>", javaTypeName.toString() );
        assertFalse( javaTypeName.isArray() );
        assertFalse( javaTypeName.isPrimitive() );

        javaTypeName = JavaTypeName.parse( "validpackagename.ReferenceType<?>" );
        assertEquals( "validpackagename.ReferenceType", javaTypeName.getClassName() );
        assertEquals( "validpackagename", javaTypeName.getPackageName() );
        assertEquals( "validpackagename.ReferenceType", javaTypeName.getQualifiedName() );
        assertEquals( "ReferenceType", javaTypeName.getSimpleName() );
        assertEquals( "validpackagename.ReferenceType<?>", javaTypeName.toString() );
        assertFalse( javaTypeName.isArray() );
        assertFalse( javaTypeName.isPrimitive() );

        javaTypeName = JavaTypeName.parse( "ReferenceType<?   extends   ReferenceType>" );
        assertEquals( "ReferenceType", javaTypeName.getClassName() );
        assertEquals( "", javaTypeName.getPackageName() );
        assertEquals( "ReferenceType", javaTypeName.getQualifiedName() );
        assertEquals( "ReferenceType", javaTypeName.getSimpleName() );
        assertEquals( "ReferenceType<? extends ReferenceType>", javaTypeName.toString() );
        assertFalse( javaTypeName.isArray() );
        assertFalse( javaTypeName.isPrimitive() );

        javaTypeName = JavaTypeName.parse( "validpackagename.ReferenceType<?   extends   ReferenceType>" );
        assertEquals( "validpackagename.ReferenceType", javaTypeName.getClassName() );
        assertEquals( "validpackagename", javaTypeName.getPackageName() );
        assertEquals( "validpackagename.ReferenceType", javaTypeName.getQualifiedName() );
        assertEquals( "ReferenceType", javaTypeName.getSimpleName() );
        assertEquals( "validpackagename.ReferenceType<? extends ReferenceType>", javaTypeName.toString() );
        assertFalse( javaTypeName.isArray() );
        assertFalse( javaTypeName.isPrimitive() );

        javaTypeName = JavaTypeName.parse( "ReferenceType<?   super   ReferenceType>" );
        assertEquals( "ReferenceType", javaTypeName.getClassName() );
        assertEquals( "", javaTypeName.getPackageName() );
        assertEquals( "ReferenceType", javaTypeName.getQualifiedName() );
        assertEquals( "ReferenceType", javaTypeName.getSimpleName() );
        assertEquals( "ReferenceType<? super ReferenceType>", javaTypeName.toString() );
        assertFalse( javaTypeName.isArray() );
        assertFalse( javaTypeName.isPrimitive() );

        javaTypeName = JavaTypeName.parse( "validpackagename.ReferenceType<?   super   ReferenceType>" );
        assertEquals( "validpackagename.ReferenceType", javaTypeName.getClassName() );
        assertEquals( "validpackagename", javaTypeName.getPackageName() );
        assertEquals( "validpackagename.ReferenceType", javaTypeName.getQualifiedName() );
        assertEquals( "ReferenceType", javaTypeName.getSimpleName() );
        assertEquals( "validpackagename.ReferenceType<? super ReferenceType>", javaTypeName.toString() );
        assertFalse( javaTypeName.isArray() );
        assertFalse( javaTypeName.isPrimitive() );

        javaTypeName = JavaTypeName.parse( "ReferenceType<ReferenceType>" );
        assertEquals( "ReferenceType", javaTypeName.getClassName() );
        assertEquals( "", javaTypeName.getPackageName() );
        assertEquals( "ReferenceType", javaTypeName.getQualifiedName() );
        assertEquals( "ReferenceType", javaTypeName.getSimpleName() );
        assertEquals( "ReferenceType<ReferenceType>", javaTypeName.toString() );
        assertFalse( javaTypeName.isArray() );
        assertFalse( javaTypeName.isPrimitive() );

        javaTypeName = JavaTypeName.parse( "validpackagename.ReferenceType<ReferenceType>" );
        assertEquals( "validpackagename.ReferenceType", javaTypeName.getClassName() );
        assertEquals( "validpackagename", javaTypeName.getPackageName() );
        assertEquals( "validpackagename.ReferenceType", javaTypeName.getQualifiedName() );
        assertEquals( "ReferenceType", javaTypeName.getSimpleName() );
        assertEquals( "validpackagename.ReferenceType<ReferenceType>", javaTypeName.toString() );
        assertFalse( javaTypeName.isArray() );
        assertFalse( javaTypeName.isPrimitive() );

        javaTypeName = JavaTypeName.parse( "ReferenceType<ReferenceType,?>" );
        assertEquals( "ReferenceType", javaTypeName.getClassName() );
        assertEquals( "", javaTypeName.getPackageName() );
        assertEquals( "ReferenceType", javaTypeName.getQualifiedName() );
        assertEquals( "ReferenceType", javaTypeName.getSimpleName() );
        assertEquals( "ReferenceType<ReferenceType, ?>", javaTypeName.toString() );
        assertFalse( javaTypeName.isArray() );
        assertFalse( javaTypeName.isPrimitive() );

        javaTypeName = JavaTypeName.parse( "validpackagename.ReferenceType<ReferenceType,?>" );
        assertEquals( "validpackagename.ReferenceType", javaTypeName.getClassName() );
        assertEquals( "validpackagename", javaTypeName.getPackageName() );
        assertEquals( "validpackagename.ReferenceType", javaTypeName.getQualifiedName() );
        assertEquals( "ReferenceType", javaTypeName.getSimpleName() );
        assertEquals( "validpackagename.ReferenceType<ReferenceType, ?>", javaTypeName.toString() );
        assertFalse( javaTypeName.isArray() );
        assertFalse( javaTypeName.isPrimitive() );

        javaTypeName = JavaTypeName.parse( "ReferenceType<ReferenceType,?   extends ReferenceType>" );
        assertEquals( "ReferenceType", javaTypeName.getClassName() );
        assertEquals( "", javaTypeName.getPackageName() );
        assertEquals( "ReferenceType", javaTypeName.getQualifiedName() );
        assertEquals( "ReferenceType", javaTypeName.getSimpleName() );
        assertEquals( "ReferenceType<ReferenceType, ? extends ReferenceType>", javaTypeName.toString() );
        assertFalse( javaTypeName.isArray() );
        assertFalse( javaTypeName.isPrimitive() );

        javaTypeName = JavaTypeName.parse( "validpackagename.ReferenceType<ReferenceType,?   extends ReferenceType>" );
        assertEquals( "validpackagename.ReferenceType", javaTypeName.getClassName() );
        assertEquals( "validpackagename", javaTypeName.getPackageName() );
        assertEquals( "validpackagename.ReferenceType", javaTypeName.getQualifiedName() );
        assertEquals( "ReferenceType", javaTypeName.getSimpleName() );
        assertEquals( "validpackagename.ReferenceType<ReferenceType, ? extends ReferenceType>",
                      javaTypeName.toString() );

        assertFalse( javaTypeName.isArray() );
        assertFalse( javaTypeName.isPrimitive() );

        javaTypeName = JavaTypeName.parse( "ReferenceType<ReferenceType,?   super ReferenceType>" );
        assertEquals( "ReferenceType", javaTypeName.getClassName() );
        assertEquals( "", javaTypeName.getPackageName() );
        assertEquals( "ReferenceType", javaTypeName.getQualifiedName() );
        assertEquals( "ReferenceType", javaTypeName.getSimpleName() );
        assertEquals( "ReferenceType<ReferenceType, ? super ReferenceType>", javaTypeName.toString() );
        assertFalse( javaTypeName.isArray() );
        assertFalse( javaTypeName.isPrimitive() );

        javaTypeName = JavaTypeName.parse( "validpackagename.ReferenceType<ReferenceType,?   super ReferenceType>" );
        assertEquals( "validpackagename.ReferenceType", javaTypeName.getClassName() );
        assertEquals( "validpackagename", javaTypeName.getPackageName() );
        assertEquals( "validpackagename.ReferenceType", javaTypeName.getQualifiedName() );
        assertEquals( "ReferenceType", javaTypeName.getSimpleName() );
        assertEquals( "validpackagename.ReferenceType<ReferenceType, ? super ReferenceType>", javaTypeName.toString() );
        assertFalse( javaTypeName.isArray() );
        assertFalse( javaTypeName.isPrimitive() );

        javaTypeName = JavaTypeName.parse( "ReferenceType<ReferenceType,ReferenceType>" );
        assertEquals( "ReferenceType", javaTypeName.getClassName() );
        assertEquals( "", javaTypeName.getPackageName() );
        assertEquals( "ReferenceType", javaTypeName.getQualifiedName() );
        assertEquals( "ReferenceType", javaTypeName.getSimpleName() );
        assertEquals( "ReferenceType<ReferenceType, ReferenceType>", javaTypeName.toString() );
        assertFalse( javaTypeName.isArray() );
        assertFalse( javaTypeName.isPrimitive() );

        javaTypeName = JavaTypeName.parse( "validpackagename.ReferenceType<ReferenceType,ReferenceType>" );
        assertEquals( "validpackagename.ReferenceType", javaTypeName.getClassName() );
        assertEquals( "validpackagename", javaTypeName.getPackageName() );
        assertEquals( "validpackagename.ReferenceType", javaTypeName.getQualifiedName() );
        assertEquals( "ReferenceType", javaTypeName.getSimpleName() );
        assertEquals( "validpackagename.ReferenceType<ReferenceType, ReferenceType>",
                      javaTypeName.toString() );

        assertFalse( javaTypeName.isArray() );
        assertFalse( javaTypeName.isPrimitive() );

        javaTypeName = JavaTypeName.parse(
            "validpackagename.ReferenceType<validpackagename.ReferenceType<validpackagename.ReferenceType<"
            + "validpackagename.ReferenceType>>,?   super validpackagename.ReferenceType<"
            + "validpackagename.ReferenceType<validpackagename.ReferenceType>>>" );

        assertEquals( "validpackagename.ReferenceType", javaTypeName.getClassName() );
        assertEquals( "validpackagename", javaTypeName.getPackageName() );
        assertEquals( "validpackagename.ReferenceType", javaTypeName.getQualifiedName() );
        assertEquals( "ReferenceType", javaTypeName.getSimpleName() );
        assertEquals( "validpackagename.ReferenceType<validpackagename.ReferenceType<validpackagename.ReferenceType<"
                      + "validpackagename.ReferenceType>>, ? super validpackagename.ReferenceType<"
                      + "validpackagename.ReferenceType<validpackagename.ReferenceType>>>",
                      javaTypeName.toString() );

        assertFalse( javaTypeName.isArray() );
        assertFalse( javaTypeName.isPrimitive() );
    }

    @Test
    public final void ParsesBasicArrayTypeNames() throws Exception
    {
        for ( final String basicType : JavaLanguage.BASIC_TYPES )
        {
            assertBasicArrayTypeName( basicType + "[]" );
            assertBasicArrayTypeName( basicType + "[][]" );
            assertBasicArrayTypeName( basicType + "[][][]" );
        }
    }

    @Test
    public final void DetectsInvalidQualifiedBasicTypeNames() throws Exception
    {
        for ( final String basicType : JavaLanguage.BASIC_TYPES )
        {
            assertInvalidTypeName( "validpackagename." + basicType );
        }
    }

    @Test
    public final void DetectsInvalidQualifiedBasicArrayTypeNames() throws Exception
    {
        for ( final String basicType : JavaLanguage.BASIC_TYPES )
        {
            assertInvalidTypeName( "validpackagename." + basicType + "[]" );
            assertInvalidTypeName( "validpackagename." + basicType + "[][]" );
            assertInvalidTypeName( "validpackagename." + basicType + "[][][]" );
        }
    }

    @Test
    public final void DetectsInvalidBasicTypeNamesWithReferenceTypeArgument() throws Exception
    {
        for ( final String basicType : JavaLanguage.BASIC_TYPES )
        {
            assertInvalidTypeName( basicType + "<Test>" );
        }
    }

    @Test
    public final void DetectsInvalidBasicTypeNamesWithWildcardTypeArgument() throws Exception
    {
        for ( final String basicType : JavaLanguage.BASIC_TYPES )
        {
            assertInvalidTypeName( basicType + "<?>" );
        }
    }

    @Test
    public final void DetectsInvalidBasicTypeNamesWithBoundedWildcardTypeArgument() throws Exception
    {
        for ( final String basicType : JavaLanguage.BASIC_TYPES )
        {
            assertInvalidTypeName( basicType + "<? extends Test>" );
            assertInvalidTypeName( basicType + "<? super Test>" );
        }
    }

    @Test
    public final void DetectsInvalidKeywordOrBooleanLiteralOrNullLiteralIdentifiers() throws Exception
    {
        for ( final String keyword : JavaLanguage.KEYWORDS )
        {
            if ( !JavaLanguage.BASIC_TYPES.contains( keyword ) )
            {
                assertInvalidTypeName( keyword );
            }

            assertInvalidTypeName( "validpackagename." + keyword );
            assertInvalidTypeName( "validpackagename.Test<" + keyword + ">" );
            assertInvalidTypeName( "validpackagename.Test<Test," + keyword + ">" );
            assertInvalidTypeName( "validpackagename.Test<?,Test," + keyword + ">" );
            assertInvalidTypeName( "validpackagename.Test<? extends " + keyword + ">" );
            assertInvalidTypeName( "validpackagename.Test<? super " + keyword + ">" );
            assertInvalidTypeName( "validpackagename.Test<Test, ? extends " + keyword + ">" );
            assertInvalidTypeName( "validpackagename.Test<Test, ? super " + keyword + ">" );
            assertInvalidTypeName( "validpackagename.Test<?, Test, ? extends " + keyword + ">" );
            assertInvalidTypeName( "validpackagename.Test<?, Test, ? super " + keyword + ">" );
        }

        for ( final String literal : JavaLanguage.BOOLEAN_LITERALS )
        {
            assertInvalidTypeName( literal );
            assertInvalidTypeName( "validpackagename." + literal );
            assertInvalidTypeName( "validpackagename.Test<" + literal + ">" );
            assertInvalidTypeName( "validpackagename.Test<Test," + literal + ">" );
            assertInvalidTypeName( "validpackagename.Test<?,Test," + literal + ">" );
            assertInvalidTypeName( "validpackagename.Test<? extends " + literal + ">" );
            assertInvalidTypeName( "validpackagename.Test<? super " + literal + ">" );
            assertInvalidTypeName( "validpackagename.Test<Test, ? extends " + literal + ">" );
            assertInvalidTypeName( "validpackagename.Test<Test, ? super " + literal + ">" );
            assertInvalidTypeName( "validpackagename.Test<?, Test, ? extends " + literal + ">" );
            assertInvalidTypeName( "validpackagename.Test<?, Test, ? super " + literal + ">" );
        }

        assertInvalidTypeName( JavaLanguage.NULL_LITERAL );
        assertInvalidTypeName( "validpackagename." + JavaLanguage.NULL_LITERAL );
        assertInvalidTypeName( "validpackagename.Test<" + JavaLanguage.NULL_LITERAL + ">" );
        assertInvalidTypeName( "validpackagename.Test<Test," + JavaLanguage.NULL_LITERAL + ">" );
        assertInvalidTypeName( "validpackagename.Test<?,Test," + JavaLanguage.NULL_LITERAL + ">" );
        assertInvalidTypeName( "validpackagename.Test<? extends " + JavaLanguage.NULL_LITERAL + ">" );
        assertInvalidTypeName( "validpackagename.Test<? super " + JavaLanguage.NULL_LITERAL + ">" );
        assertInvalidTypeName( "validpackagename.Test<Test, ? extends " + JavaLanguage.NULL_LITERAL + ">" );
        assertInvalidTypeName( "validpackagename.Test<Test, ? super " + JavaLanguage.NULL_LITERAL + ">" );
        assertInvalidTypeName( "validpackagename.Test<?, Test, ? extends " + JavaLanguage.NULL_LITERAL + ">" );
        assertInvalidTypeName( "validpackagename.Test<?, Test, ? super " + JavaLanguage.NULL_LITERAL + ">" );
    }

    @Test
    public final void DetectsMisplacedSeparatorTokens() throws Exception
    {
        assertInvalidTypeName( "[" );
        assertInvalidTypeName( "]" );
        assertInvalidTypeName( "<" );
        assertInvalidTypeName( ">" );
        assertInvalidTypeName( "," );
        assertInvalidTypeName( "." );
        assertInvalidTypeName( "?" );

        assertInvalidTypeName( "[TEST" );
        assertInvalidTypeName( "]TEST" );
        assertInvalidTypeName( "<TEST" );
        assertInvalidTypeName( ">TEST" );
        assertInvalidTypeName( ",TEST" );
        assertInvalidTypeName( ".TEST" );
        assertInvalidTypeName( "?TEST" );

        assertInvalidTypeName( "TEST[TEST" );
        assertInvalidTypeName( "TEST]TEST" );
        assertInvalidTypeName( "TEST>TEST" );
        assertInvalidTypeName( "TEST,TEST" );
        assertInvalidTypeName( "TEST?TEST" );

        assertInvalidTypeName( "TEST]" );
        assertInvalidTypeName( "TEST>" );
        assertInvalidTypeName( "TEST," );
        assertInvalidTypeName( "TEST?" );

        assertInvalidTypeName( "TEST.TEST]" );
        assertInvalidTypeName( "TEST.TEST>" );
        assertInvalidTypeName( "TEST.TEST," );
        assertInvalidTypeName( "TEST.TEST?" );

        assertInvalidTypeName( "TEST<TEST[" );
        assertInvalidTypeName( "TEST<TEST]" );
        assertInvalidTypeName( "TEST<TEST," );
        assertInvalidTypeName( "TEST<TEST." );
        assertInvalidTypeName( "TEST<TEST?" );

        assertInvalidTypeName( "TEST<TEST<[" );
        assertInvalidTypeName( "TEST<TEST<]" );
        assertInvalidTypeName( "TEST<TEST<," );
        assertInvalidTypeName( "TEST<TEST<." );
        assertInvalidTypeName( "TEST<TEST<?" );

        for ( final String basicType : JavaLanguage.BASIC_TYPES )
        {
            assertInvalidTypeName( "[" + basicType );
            assertInvalidTypeName( "]" + basicType );
            assertInvalidTypeName( "<" + basicType );
            assertInvalidTypeName( ">" + basicType );
            assertInvalidTypeName( "," + basicType );
            assertInvalidTypeName( "." + basicType );
            assertInvalidTypeName( "?" + basicType );
        }

        for ( final String basicType : JavaLanguage.BASIC_TYPES )
        {
            assertInvalidTypeName( basicType + "]" );
            assertInvalidTypeName( basicType + ">" );
            assertInvalidTypeName( basicType + "," );
            assertInvalidTypeName( basicType + "?" );
        }
    }

    @Test
    public final void DetectsUnexpectedEndOfInput() throws Exception
    {
        assertInvalidTypeName( "" );
        assertInvalidTypeName( "boolean[" );
        assertInvalidTypeName( "TEST[" );

        assertInvalidTypeName( "TEST[" );
        assertInvalidTypeName( "TEST<" );
        assertInvalidTypeName( "TEST." );

        assertInvalidTypeName( "TEST.TEST[" );
        assertInvalidTypeName( "TEST.TEST<" );

        assertInvalidTypeName( "TEST<TEST" );
        assertInvalidTypeName( "TEST<TEST." );
        assertInvalidTypeName( "TEST<?" );
        assertInvalidTypeName( "TEST<?extends" );
        assertInvalidTypeName( "TEST<?super" );

        assertInvalidTypeName( "TEST.TEST<TEST" );
        assertInvalidTypeName( "TEST.TEST<?" );
        assertInvalidTypeName( "TEST.TEST<?extends" );
        assertInvalidTypeName( "TEST.TEST<?super" );

        for ( final String basicType : JavaLanguage.BASIC_TYPES )
        {
            assertInvalidTypeName( basicType + "[" );
        }
    }

    @Test
    public final void DetectsInvalidTokens() throws Exception
    {
        assertInvalidTypeName( "@" );

        for ( final String basicType : JavaLanguage.BASIC_TYPES )
        {
            assertInvalidTypeName( basicType + "@" );
        }

        assertInvalidTypeName( "ReferenceType@" );
        assertInvalidTypeName( "validpackagename.ReferenceType@" );
        assertInvalidTypeName( "validpackagename@ReferenceType" );
        assertInvalidTypeName( "validpackagename.ReferenceType<@>" );
        assertInvalidTypeName( "validpackagename.ReferenceType<? extends @>" );
        assertInvalidTypeName( "validpackagename.ReferenceType<? super @>" );
        assertInvalidTypeName( "ReferenceType<ReferenceType<@>>" );
        assertInvalidTypeName( "ReferenceType<ReferenceType<? extends @>>" );
        assertInvalidTypeName( "ReferenceType<ReferenceType<? super @>>" );
        assertInvalidTypeName( "validpackagename.ReferenceType<validpackagename.ReferenceType<@>>" );
        assertInvalidTypeName( "validpackagename.ReferenceType<validpackagename.ReferenceType<? extends @>>" );
        assertInvalidTypeName( "validpackagename.ReferenceType<validpackagename.ReferenceType<? super @>>" );
    }

    @Test
    public final void DetectsDuplicateTokens() throws Exception
    {
        for ( final String basicType : JavaLanguage.BASIC_TYPES )
        {
            assertInvalidTypeName( basicType + " " + basicType );
        }

        assertInvalidTypeName( "ReferenceType ReferenceType" );
        assertInvalidTypeName( "ReferenceType.." );
        assertInvalidTypeName( "ReferenceType<<" );
        assertInvalidTypeName( "ReferenceType[[" );
        assertInvalidTypeName( "ReferenceType<ReferenceType,," );
    }

    @Test
    public final void ClassNameEncodings() throws Exception
    {
        assertEquals( "boolean", JavaTypeName.parse( "boolean" ).getClassName() );
        assertEquals( "byte", JavaTypeName.parse( "byte" ).getClassName() );
        assertEquals( "char", JavaTypeName.parse( "char" ).getClassName() );
        assertEquals( "double", JavaTypeName.parse( "double" ).getClassName() );
        assertEquals( "float", JavaTypeName.parse( "float" ).getClassName() );
        assertEquals( "int", JavaTypeName.parse( "int" ).getClassName() );
        assertEquals( "long", JavaTypeName.parse( "long" ).getClassName() );
        assertEquals( "short", JavaTypeName.parse( "short" ).getClassName() );

        assertEquals( "[Z", JavaTypeName.parse( "boolean[]" ).getClassName() );
        assertEquals( "[B", JavaTypeName.parse( "byte[]" ).getClassName() );
        assertEquals( "[C", JavaTypeName.parse( "char[]" ).getClassName() );
        assertEquals( "[D", JavaTypeName.parse( "double[]" ).getClassName() );
        assertEquals( "[F", JavaTypeName.parse( "float[]" ).getClassName() );
        assertEquals( "[I", JavaTypeName.parse( "int[]" ).getClassName() );
        assertEquals( "[J", JavaTypeName.parse( "long[]" ).getClassName() );
        assertEquals( "[S", JavaTypeName.parse( "short[]" ).getClassName() );

        assertEquals( "[[Z", JavaTypeName.parse( "boolean[][]" ).getClassName() );
        assertEquals( "[[B", JavaTypeName.parse( "byte[][]" ).getClassName() );
        assertEquals( "[[C", JavaTypeName.parse( "char[][]" ).getClassName() );
        assertEquals( "[[D", JavaTypeName.parse( "double[][]" ).getClassName() );
        assertEquals( "[[F", JavaTypeName.parse( "float[][]" ).getClassName() );
        assertEquals( "[[I", JavaTypeName.parse( "int[][]" ).getClassName() );
        assertEquals( "[[J", JavaTypeName.parse( "long[][]" ).getClassName() );
        assertEquals( "[[S", JavaTypeName.parse( "short[][]" ).getClassName() );

        assertEquals( "[[[Z", JavaTypeName.parse( "boolean[][][]" ).getClassName() );
        assertEquals( "[[[B", JavaTypeName.parse( "byte[][][]" ).getClassName() );
        assertEquals( "[[[C", JavaTypeName.parse( "char[][][]" ).getClassName() );
        assertEquals( "[[[D", JavaTypeName.parse( "double[][][]" ).getClassName() );
        assertEquals( "[[[F", JavaTypeName.parse( "float[][][]" ).getClassName() );
        assertEquals( "[[[I", JavaTypeName.parse( "int[][][]" ).getClassName() );
        assertEquals( "[[[J", JavaTypeName.parse( "long[][][]" ).getClassName() );
        assertEquals( "[[[S", JavaTypeName.parse( "short[][][]" ).getClassName() );

        assertEquals( "ReferenceType", JavaTypeName.parse( "ReferenceType" ).getClassName() );
        assertEquals( "validpackagename.ReferenceType",
                      JavaTypeName.parse( "validpackagename.ReferenceType" ).getClassName() );

        assertEquals( "[LReferenceType;", JavaTypeName.parse( "ReferenceType[]" ).getClassName() );
        assertEquals( "[Lvalidpackagename.ReferenceType;",
                      JavaTypeName.parse( "validpackagename.ReferenceType[]" ).getClassName() );

        assertEquals( "[[LReferenceType;", JavaTypeName.parse( "ReferenceType[][]" ).getClassName() );
        assertEquals( "[[Lvalidpackagename.ReferenceType;",
                      JavaTypeName.parse( "validpackagename.ReferenceType[][]" ).getClassName() );

        assertEquals( "[[[LReferenceType;", JavaTypeName.parse( "ReferenceType[][][]" ).getClassName() );
        assertEquals( "[[[Lvalidpackagename.ReferenceType;",
                      JavaTypeName.parse( "validpackagename.ReferenceType[][][]" ).getClassName() );

    }

    @Test
    public final void Deserializable() throws Exception
    {
        ObjectInputStream in = null;

        try
        {
            in = new ObjectInputStream( this.getClass().getResourceAsStream(
                ABSOLUTE_RESOURCE_NAME_PREFIX + "JavaTypeName.ser" ) );

            final JavaTypeName javaTypeName = (JavaTypeName) in.readObject();
            assertEquals( "Java<Java>", javaTypeName.getName( true ) );
            assertEquals( 1, javaTypeName.getArguments().size() );
            assertEquals( "Java", javaTypeName.getArguments().get( 0 ).getTypeName().getName( true ) );
            System.out.println( javaTypeName );
        }
        finally
        {
            in.close();
        }

        try
        {
            in = new ObjectInputStream( this.getClass().getResourceAsStream(
                ABSOLUTE_RESOURCE_NAME_PREFIX + "JavaTypeNameArgument.ser" ) );

            final JavaTypeName.Argument javaTypeNameArgument = (JavaTypeName.Argument) in.readObject();
            assertEquals( "Java", javaTypeNameArgument.getTypeName().getName( true ) );
            System.out.println( javaTypeNameArgument );
        }
        finally
        {
            in.close();
        }
    }

    private static void assertBasicTypeName( final String typeName )
    {
        final JavaTypeName t = JavaTypeName.valueOf( typeName );
        assertEquals( "", t.getPackageName() );
        assertEquals( typeName, t.getClassName() );
        assertEquals( typeName, t.getQualifiedName() );
        assertEquals( typeName, t.getSimpleName() );
        assertFalse( t.isArray() );
        assertTrue( t.isPrimitive() );
    }

    private static void assertBasicArrayTypeName( final String typeName )
    {
        final JavaTypeName t = JavaTypeName.valueOf( typeName );
        assertEquals( "", t.getPackageName() );
        assertEquals( typeName, t.getQualifiedName() );
        assertEquals( typeName, t.getSimpleName() );
        assertTrue( t.isArray() );
        assertTrue( t.isPrimitive() );
    }

    private static void assertInvalidTypeName( final String typeName )
    {
        try
        {
            JavaTypeName.parse( typeName );
            fail( "Expected 'ParseException' not thrown parsing Java type name '" + typeName + "'." );
        }
        catch ( final ParseException e )
        {
            assertNotNull( e.getMessage() );
            System.out.println( e.getMessage() );
        }
    }

}
