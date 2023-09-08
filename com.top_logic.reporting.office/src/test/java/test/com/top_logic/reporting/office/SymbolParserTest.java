/*
 * SPDX-FileCopyrightText: 2005 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.reporting.office;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.top_logic.reporting.office.ExpansionObject;
import com.top_logic.reporting.office.SymbolParser;
import com.top_logic.reporting.office.basic.BasicSymbolParser;


/** 
 * @author    <a href=mailto:jco@top-logic.com>jco</a>
 */
public class SymbolParserTest extends TestCase {


    public void testParseValid () {
        SymbolParser parser = new BasicSymbolParser();
        ExpansionObject expObject = parser.parse ("seppl",getTextValid());
        assertNotNull(expObject);
        assertEquals ("static",expObject.getSymbolType());
        assertEquals ("CURRENT_PERSON",expObject.getSymbolContent());
        
        expObject = parser.parse ("seppl2",getTextValid2());
        assertNotNull(expObject);
        assertEquals ("script",expObject.getSymbolType());
        
        assertEquals ("<exp type=script>out.println('Seppl');</exp>",expObject.getSymbol());
    }
    public void testParseInvalid() {
        SymbolParser parser = new BasicSymbolParser();
        ExpansionObject expObject = null;
        
        expObject = parser.parse ("hugo",getTextInValid1());
        assertNull (expObject);
        
        expObject = parser.parse ("hugo2",getTextInValid2());
        assertNull (expObject);
    }
    
    protected String getTextValid () {
        StringBuffer buffy= new StringBuffer ();
        buffy.append("<exp type=static>CURRENT_PERSON</exp>");
        return buffy.toString();
    }
    protected String getTextValid2 () {
        StringBuffer buffy= new StringBuffer ();
        buffy.append("Mitten im Text <exp type=script>out.println('Seppl');</exp> steht das hier");
        return buffy.toString();
    }
    protected String getTextInValid1 () {
        StringBuffer buffy= new StringBuffer ();
        buffy.append("<exp typo=static>CURRENT_PERSON</exp>");
        return buffy.toString();
    }
    protected String getTextInValid2 () {
        StringBuffer buffy= new StringBuffer ();
        buffy.append("<ex typo=static>CURRENT_PERSON</exp>");
        return buffy.toString();
    }

    /** Return the suite of Tests to perform */
    public static Test suite () {
        return OfficeTestSetup.createOfficeTestSetup(new TestSuite (SymbolParserTest.class));
    }
 
    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

}
