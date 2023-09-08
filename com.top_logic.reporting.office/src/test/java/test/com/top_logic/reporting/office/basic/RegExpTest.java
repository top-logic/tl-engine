/*
 * SPDX-FileCopyrightText: 2005 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.reporting.office.basic;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import junit.framework.TestCase;


/**
 * @author    <a href=mailto:jco@top-logic.com>jco</a>
 */
public class RegExpTest extends TestCase {

    public static void main(String[] args) {
        junit.textui.TestRunner.run(RegExpTest.class);
    }

    String theText = "Ich <exp type=script>person.getName()</exp> will nach Hause";
    String theText2 = " und <exp type=script>person.getFriend()</exp> kommt mit";
    String theText3 = " Ausserdem ist noch <exp type=script style=seppl;egon;>person.getFoe()</exp> beteiligt";
    
    Pattern thePattern =Pattern.compile ("<exp type=([^>]*)>([^<]*)</exp>");
    
    String testString = "person.getName()";
    
    public void _testSimpleExpression (){
        
        Matcher theMatcher = thePattern.matcher(theText);
        
        assertNotNull(theMatcher);
        assertTrue(theMatcher.find());
        
        assertEquals(2,theMatcher.groupCount());
        assertEquals (testString,theMatcher.group(2));
        assertFalse(theMatcher.find());
    }
    public void _testMultipleMatchesSimple () {
        Matcher theMatcher = thePattern.matcher(theText + theText2);
        assertTrue(theMatcher.find());
        assertTrue(theMatcher.find());
        assertTrue (theMatcher.groupCount() == 2);
        assertEquals(theMatcher.group(2),"person.getFriend()");
        assertFalse (theMatcher.find());
    }
    
    Pattern hybridPattern = Pattern.compile ("<exp type=([^\\s]*|[^>]*)(|\\sstyle=([^>]*))>([^<]*)</exp>");
    
    public void _testHybrid() {
        Matcher m = hybridPattern.matcher(theText + theText2 + theText3);
        
        // the first one is of the short type:
        assertTrue (m.find());        
        assertEquals ("script", m.group(1));
        assertEquals ("",m.group(2));
        assertNull (m.group(3));
        assertNotNull (m.group(4));
        
        // the third one contains a style attribute
        assertTrue(m.find());
        assertTrue(m.find());
        assertEquals ("script", m.group(1));
        assertTrue (m.group(2).indexOf("style") > -1);
        assertNotNull (m.group(3));
        assertTrue (m.group(3).length() > 1);
        assertNotNull (m.group(4));
        
//        while (m.find()) {
//            System.out.println(m.group(0));
//            int count = m.groupCount();
//            for (int i = 1; i <= count; i++) {
//                System.out.println ("\t->" + m.group(i) + "<-");
//            }
//        }
        
    }
    public void testChapter1 (){
        String text = "Dies und das ist was anderes als Dies und das";
        Pattern p = Pattern.compile("Dies und das");
        Matcher m = p.matcher(text);
        assertTrue (m.find());
        assertTrue (m.find());
        
        // now the first only:
        p = Pattern.compile("^Dies und das");
        m = p.matcher(text);
        assertTrue (m.find());
        assertFalse (m.find());

        // 
        p = Pattern.compile("Dies und das$");
        m = p.matcher(text);
        assertTrue (m.find());
        assertFalse (m.find());
        
        // or pattern
        p = Pattern.compile("das|Dies");
        m = p.matcher(text);
        int hitCount = 0;
        while (m.find()) {
            hitCount++;
            //System.out.println(m.group(0)); 
        }
        assertEquals (4,hitCount);  
    }
    
    public void testChapter2() {
        Pattern p = Pattern.compile("^Re(|\\[\\d\\]):");
        Matcher m = p.matcher("");
        
        m.reset("Re: Ich bins");
        assertTrue(m.find());
        
        m.reset("Re[2]: Ich auch");
        assertTrue(m.find());
        assertEquals ("Re[2]:",m.group(0));
    }
    public void testChapter3(){
        // to do
    }
}
