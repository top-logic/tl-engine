/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.tagext.BodyContent;
import jakarta.servlet.jsp.tagext.BodyTagSupport;

import com.top_logic.basic.util.ResKey;

/**
 * This tag handler converts variables in the bodyText to I18N resource values.
 * 
 * The variables have to be marked with sum braces (e.g. {cancel}) and have to
 * contain only the last keword in the point separated key (e.g. &quot; base.edit.cancel &quot;). 
 * The prefix is determined by the prefix attribute of the tag.
 *
 * @author    <a href="mailto:asc@top-logic.com">asc</a>
 */
public class VariableReplaceTag extends BodyTagSupport {
    // Tag "replaceVar" constained in util.tld
    // Testsite in /jsp/test/taglibs/BasicReplacement
    
    /**
     * The pattern to match the body of the tag against.
     */
    protected Pattern pattern;
    
    /*
     * Variable to store the size of the prefix. Impoves
     * performance.
     */
    private int prefixLength;
    
    /**
     * Constructor that initializes the pattern to
     * find variables in sum braces.
     *
     */
    public VariableReplaceTag() {
        // find a string after an opening sum brace
        // and give back all until you reach a closing
        // sum brace. Attention : All characters without
        // substracting the sum braces gets all characters
        // until the last sum brce in one line!!!
       pattern = Pattern.compile("\\{(.[^{}]*)\\}", Pattern.MULTILINE);
    }

    /**
     * The prefix of the key (e.g. &quot; base.edit. &quot; for
     * variables like &quot; base.edit.cancel &quot;).
     */
    private String prefix;
    
    /**
     * Returns the prefix of the key (e.g. &quot; base.edit. &quot; for
     * variables like &quot; base.edit.cancel &quot;).
     * @return the prefix of the key
     */
    public String getPrefix() {
        return prefix;
    }

    /**
     * Sets the prefix of the key (e.g. &quot; base.edit. &quot; for
     * variables like &quot; base.edit.cancel &quot;). 
     * <b>Attention:</b> The prefix has to end with a point unles it is
     * empty.
     * @param aString the prefix to attach the keys.
     */
    public void setPrefix(String aString) {
        prefixLength = aString.length();
        prefix = aString;
    }

    /**
     * Replaces variables in the body of the tag with
     * the corresponding values in the server language (via I18n
     * resources).  
     * 
     * @see jakarta.servlet.jsp.tagext.IterationTag#doAfterBody()
     */
    @Override
	public int doAfterBody() throws JspException {
        // fetch the bodyObject
        BodyContent theBc = getBodyContent();
        
        // get the body as String. Is this OK?
        String theBody = theBc.getString();
        
        // dynamically ask for the size of the String.
        // are there other ways?
        StringBuffer theNewBody = new StringBuffer(theBody.length());
        
        // remove the content with variables from the body
        theBc.clearBody();
        
        // initialize the matcher with the body of the Tag
        Matcher theMatcher = pattern.matcher(theBody);
        
        // go through the String and find groups
        while (theMatcher.find()) {
            String theVariable = theMatcher.group(1);
            String theValue = findValue(theVariable);
            theMatcher.appendReplacement(theNewBody,theValue);
        }
        // important: Append the substring after the last pattern occurance.
        theMatcher.appendTail(theNewBody);
        
        try {
            // overwrite
           getPreviousOut().print(theNewBody.toString());
        } catch (IOException e) {
           throw new JspException( e.getMessage());
        }
        
        // otherwise we are iterating
        return SKIP_BODY;
    }
    
    /**
	 * Finds the value of a key consisting of <code>prefix</code> and <code>aKey</code> in the I18n
	 * resources.
	 * 
	 * <p>
	 * The prefix has to contain a terminal point or the key has to start with a point, because in
	 * the current implementation no point is added.
	 * </p>
	 * 
	 * @param aKey
	 *        a key without prefix
	 * @return the value in the server language or the <code>aKey</code> if no value can be found.
	 */
    protected String findValue(String aKey) {
        // calculate the size
        StringBuffer theKey = new StringBuffer(prefixLength+aKey.length() +1);
        
        theKey.append(prefix);
        // Sorry, but we don't have the time to ask friendly questions
        //if (!prefix.endsWith(".") && !aKey.startsWith(".")) {
        //theKey.append('.'); 

        theKey.append(aKey);
        
        // get the value from the default (server) resource
		String theValue = Resources.getInstance().getString(ResKey.legacy(theKey.toString()));
        
        return theValue != null ? theValue : aKey;
    }

}
