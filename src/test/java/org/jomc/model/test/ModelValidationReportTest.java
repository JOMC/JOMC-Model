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
package org.jomc.model.test;

import java.io.ObjectInputStream;
import java.util.logging.Level;
import junit.framework.Assert;
import org.jomc.model.ModelValidationReport;

/**
 * Test cases for class {@code org.jomc.model.ModelValidationReport}.
 *
 * @author <a href="mailto:schulte2005@users.sourceforge.net">Christian Schulte</a> 1.0
 * @version $Id$
 */
public class ModelValidationReportTest
{

    public ModelValidationReportTest()
    {
        super();
    }

    public void testSerializabe() throws Exception
    {
        final ObjectInputStream reportStream =
            new ObjectInputStream( this.getClass().getResourceAsStream( "ModelValidationReport.ser" ) );

        final ObjectInputStream detailStream =
            new ObjectInputStream( this.getClass().getResourceAsStream( "ModelValidationReportDetail.ser" ) );

        final ModelValidationReport report = (ModelValidationReport) reportStream.readObject();
        final ModelValidationReport.Detail detail = (ModelValidationReport.Detail) detailStream.readObject();

        reportStream.close();
        detailStream.close();

        System.out.println( report );
        System.out.println( detail );

        Assert.assertEquals( 1, report.getDetails( "Identifier 1" ).size() );
        Assert.assertEquals( 1, report.getDetails( "Identifier 2" ).size() );
        Assert.assertEquals( 1, report.getDetails( "Identifier 3" ).size() );
        Assert.assertEquals( 1, report.getDetails( "Identifier 4" ).size() );
        Assert.assertEquals( 1, report.getDetails( "Identifier 5" ).size() );
        Assert.assertEquals( 1, report.getDetails( "Identifier 6" ).size() );
        Assert.assertEquals( 1, report.getDetails( "Identifier 7" ).size() );
        Assert.assertEquals( 2, report.getDetails( "Identifier 8" ).size() );
        Assert.assertEquals( 0, report.getDetails( "Identifier 9" ).size() );
        Assert.assertEquals( 1, report.getDetails( "Identifier 10" ).size() );

        Assert.assertEquals( "Identifier 1", detail.getIdentifier() );
        Assert.assertEquals( Level.OFF, detail.getLevel() );
        Assert.assertEquals( "Message 1", detail.getMessage() );
        Assert.assertNull( detail.getElement() );
    }

}
