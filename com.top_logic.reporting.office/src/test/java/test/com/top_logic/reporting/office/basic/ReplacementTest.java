/*
 * SPDX-FileCopyrightText: 2005 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.reporting.office.basic;

import junit.framework.TestCase;


/**
 * @author    <a href=mailto:jco@top-logic.com>jco</a>
 */
public class ReplacementTest extends TestCase {

    public static void main(String[] args) {
        junit.textui.TestRunner.run(ReplacementTest.class);
    }

    /**
     * Constructor for ReplacementTest.
     */
    public ReplacementTest(String arg0) {
        super(arg0);
    }
    
    public void testReplace () throws Exception {
        String pattern1 = "<exp type=static>SEPPL</exp>";
        String pattern2 = "<exp type=script>person.getName\\(\\)</exp>";
        
        String text = "Ich bin " + pattern1 + " und mein Name ist <exp type=script>person.getName()</exp> vom Hofe.";
        
        String replaced1 = text.replaceAll(pattern1,"GOD");
        System.out.println(replaced1);
        
        System.out.println (text.replaceAll(pattern2,"Falk"));
    }

}
