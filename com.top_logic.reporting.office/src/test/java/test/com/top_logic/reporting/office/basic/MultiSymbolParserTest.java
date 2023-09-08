/*
 * SPDX-FileCopyrightText: 2005 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.reporting.office.basic;

import java.util.Iterator;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import test.com.top_logic.reporting.office.OfficeTestSetup;

import com.top_logic.reporting.office.ExpansionContext;
import com.top_logic.reporting.office.ExpansionEngine;
import com.top_logic.reporting.office.ExpansionObject;
import com.top_logic.reporting.office.StaticSymbolResolver;
import com.top_logic.reporting.office.basic.MultiSymbolParser;
import com.top_logic.reporting.office.basic.StackedExpansionObject;


/**
 * @author    <a href=mailto:jco@top-logic.com>jco</a>
 */
public class MultiSymbolParserTest extends TestCase {


    String theText = "Ich <exp type=script>person.getName()</exp> will nach Hause";
    String theText2 = " und <exp type=script>person.getFriend()</exp> kommt mit";
    String theText3 = " Ausserdem ist noch <exp type=script style=seppl:01;hugo:02>person.getFoe()</exp> beteiligt";
    
    public void testMultiSymbolParsing() {
        MultiSymbolParser theParser = new MultiSymbolParser();
        
        ExpansionObject theSymbol = theParser.parse("seppl",theText3 + theText + theText2);
        
        assertTrue (theSymbol instanceof StackedExpansionObject);
        StackedExpansionObject stackedObject = (StackedExpansionObject) theSymbol;
        assertTrue (stackedObject.hasInnerExpansionObjects());
        List innerObjects = stackedObject.getInnerExpansionObjects();
        assertEquals (2,innerObjects.size());
        assertTrue (theSymbol.getContentStyle().indexOf("hugo:02") > -1);
        
    }
    
    public void testMultiSymbolReplacing (){
        MultiSymbolParser theParser = new MultiSymbolParser();     
        String testText = theText3 + theText + theText2;
        ExpansionObject theSymbol = theParser.parse("seppl",testText);
        theSymbol.expand(new TestingExpansionEngine(),null);
        
        assertTrue (theSymbol.isExpanded());
        testText = replaceSymbolInText(testText,theSymbol.getSymbol(),theSymbol.getExpandedObject().toString());
        Iterator iter = ((StackedExpansionObject) theSymbol).getInnerExpansionObjects().iterator();
        while (iter.hasNext()) {
            ExpansionObject element = (ExpansionObject) iter.next();
            assertTrue (element.isExpanded());
            testText = replaceSymbolInText(testText,element.getSymbol(),element.getExpandedObject().toString());
        }
        assertTrue (testText.indexOf("Kilroy") > -1);
    }
    
    protected String replaceSymbolInText(String aText, String aSymbol, String aValue) {
        StringBuffer result = new StringBuffer ();
        int symbolStart = aText.indexOf(aSymbol);
        result.append (aText.substring(0,symbolStart));
        result.append (aValue);
        result.append (aText.substring(symbolStart+aSymbol.length()));
        return result.toString();
    }
    class TestingExpansionEngine implements ExpansionEngine {
        /**
         * expand by comparing to static strings :)
         * @see com.top_logic.reporting.office.ExpansionEngine#expandSymbol(com.top_logic.reporting.office.ExpansionContext, com.top_logic.reporting.office.ExpansionObject)
         */
        @Override
		public Object expandSymbol(ExpansionContext aContext, ExpansionObject aSymbol) {
            String sym = aSymbol.getSymbolContent();
            if (sym.indexOf("getFoe") > -1) {
                return "DER FEIND!";
            }
            if (sym.indexOf("getFriend") > -1) {
                return "Mein Freund";
            }
            if (sym.indexOf("getName") > -1) {
                return "Kilroy";
            }
            return null;
        }

        /** 
         * @see com.top_logic.reporting.office.ExpansionEngine#setStaticSymbolResolver(com.top_logic.reporting.office.StaticSymbolResolver)
         */
        @Override
		public void setStaticSymbolResolver(StaticSymbolResolver aResolver) {
        }
        
    }
    /** Return the suite of Tests to perform */
    public static Test suite () {
        return OfficeTestSetup.createOfficeTestSetup(new TestSuite (MultiSymbolParserTest.class));
    }
 
    /** Main function for direct execution */
    public static void main (String[] args) {
        junit.textui.TestRunner.run (suite ());
    }

    
}
