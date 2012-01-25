<?xml version="1.0" encoding="UTF-8"?>
<!--

  Copyright (C) Christian Schulte, 2011-332
  All rights reserved.

  Redistribution and use in source and binary forms, with or without
  modification, are permitted provided that the following conditions
  are met:

    o Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.

    o Redistributions in binary form must reproduce the above copyright
      notice, this list of conditions and the following disclaimer in
      the documentation and/or other materials provided with the
      distribution.

  THIS SOFTWARE IS PROVIDED "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
  INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY
  AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL
  THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY DIRECT, INDIRECT,
  INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
  NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
  DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
  THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
  THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

  $JOMC$

-->
<xsl:stylesheet xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:test="http://jomc.org/model/test"
                version="1.0">

  <xsl:output method="text" encoding="UTF-8"/>

  <xsl:template match="/">/*
 *  Copyright (C) Christian Schulte, 2011-332
 *  All rights reserved.
 *
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions
 *  are met:
 *
 *    o Redistributions of source code must retain the above copyright
 *      notice, this list of conditions and the following disclaimer.
 *
 *    o Redistributions in binary form must reproduce the above copyright
 *      notice, this list of conditions and the following disclaimer in
 *      the documentation and/or other materials provided with the
 *      distribution.
 *
 *  THIS SOFTWARE IS PROVIDED "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 *  INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY
 *  AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL
 *  THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY DIRECT, INDIRECT,
 *  INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 *  NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 *  DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 *  THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 *  THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 *  $JOMC$
 *
 */
package org.jomc.model.modlet.test;

/**
 * Test cases for class {@code org.jomc.model.modlet.DefaultModelValidator} implementations.
 *
 * @author &lt;a href="mailto:schulte2005@users.sourceforge.net">Christian Schulte&lt;/a>
 * @version $JOMC$
 */
public class DefaultModelValidatorTestSuite extends DefaultModelValidatorTest
{

    /** Creates a new {@code DefaultModelValidatorTestSuite} instance. */
    public DefaultModelValidatorTestSuite()
    {
        super();
    }<xsl:apply-templates/>
}</xsl:template><xsl:template match="test:schema-constraints-test">  /**
     * Runs the {@code <xsl:value-of select="@identifier"/>} schema constraints test.
     *
     * @throws Exception if running the test fails.
     *
     * @see #testSchemaConstraints(java.lang.String)
     */
    @org.junit.Test public final void <xsl:value-of select="@identifier"/>() throws Exception
    {
        this.testSchemaConstraints( "<xsl:value-of select="@identifier"/>" );
    }</xsl:template><xsl:template match="test:modules-constraints-test">  /**
     * Runs the {@code <xsl:value-of select="@identifier"/>} modules constraints test.
     *
     * @throws Exception if running the test fails.
     *
     * @see #testModulesConstraints(java.lang.String)
     */
    @org.junit.Test public final void <xsl:value-of select="@identifier"/>() throws Exception
    {
        this.testModulesConstraints( "<xsl:value-of select="@identifier"/>" );
    }</xsl:template>
</xsl:stylesheet>