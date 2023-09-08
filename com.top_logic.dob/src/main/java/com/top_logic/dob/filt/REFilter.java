/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.dob.filt;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * AttributeFilter using RegularExpression.
 * 
 * @author    <a href=mailto:klaus.halfmann@top-logic.com>Klaus Halfmann</a>
 */
public class REFilter extends DOAttributeFilter {

    /** The parsed regular expression */
    Pattern expr;

    /** Create a Filter for the given String by applying the
     *  simple '*' and '?' Semantics.
     *<p>
     *  To Do so I replace any '*' by ".*" and '?' by ".?".
     *</p>
     * We just use the JDK1.4 Pattern
     */
    public REFilter(String attrName, String re) 
        throws PatternSyntaxException
    {
        super(attrName);
        int          len    = re.length();
        StringBuffer grepRE = new StringBuffer(len + 16);
        for (int i=0; i < len; i++) {
            char c = re.charAt(i);
            switch (c) {
                case '?': grepRE.append('.');
                          break;
                case '*': 
                          grepRE.append('.');
                            // no break intended !
                default : grepRE.append(c);
            }
        }
        expr = Pattern.compile(grepRE.toString());
    }

    /** Create a Filter for the given {@link Pattern}.
     */
    public REFilter(String attrName, Pattern pattern) {
        super(attrName);
        expr = pattern;
    }

    /** 
     * Filter the value as String by the expression.
     *
     * @param  theValue of the Attribute to check.
     * @return true in case the Value matches the your desires.
     */
    @Override
	public boolean test(Object theValue) {
        return theValue != null && expr.matcher(theValue.toString()).matches();
    }
}