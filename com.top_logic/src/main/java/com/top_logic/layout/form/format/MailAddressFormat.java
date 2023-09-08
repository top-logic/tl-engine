/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.format;

import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;
import java.util.regex.Pattern;

import com.top_logic.basic.StringServices;
import com.top_logic.layout.form.I18NConstants;
import com.top_logic.layout.form.model.DescriptiveParsePosition;
import com.top_logic.util.Resources;

/**
 * The MailAddressFormat is a concrete class for parsing e-mail addresses.
 * 
 * @author  <a href="mailto:tdi@top-logic.com">Thomas Dickhut</a>
 */
public class MailAddressFormat extends Format {

	/** Email address as regualr expression. */
    public static final String MAIL_ADDRESS_REGEX = "([a-zA-Z0-9_\\-\\.]+)"  +   // User
	                                   				"@" +                        // @
	                                   				"([a-zA-Z0-9_\\-\\.]{2,})" + // Domain (Subdomain)
	                                   				"\\." +                      // dot
	                                   				"([a-zA-Z]{2,5})";           // top level domain
    
	/** The e-mail address pattern. */
    public final static Pattern MAIL_ADDRESS_PATTERN = Pattern.compile(MAIL_ADDRESS_REGEX); 
    
    @Override
	public Object parseObject(String aSource, ParsePosition aPos) {
        if (aPos == null) {
            throw new NullPointerException();
        }
        if (StringServices.isEmpty(aSource)) {
            return returnError(aPos, 0);
        }
        
        int    start = aPos.getIndex();
        String str   = aSource.substring(start).trim();
        if (checkEMailAddress(str)) {
            aPos.setIndex(aSource.length());
        } else {
            return returnError(aPos, start);
        }
        return str;
    }

    @Override
	public StringBuffer format(Object aMailAddress, StringBuffer aToAppendTo, FieldPosition aPos) {
        if (aToAppendTo == null || aPos == null) {
            throw new NullPointerException();
        }
        if (!(aMailAddress instanceof String)) {
            throw new IllegalArgumentException();
        }
        return aToAppendTo.append(aMailAddress);        
    }

    /**
     * This method returns whether the given e-mail address is valid.
     * 
     * @param  aAddress A e-mail address.
     * @return Returns <code>true</code> if the e-mail address is valid,
     *         <code>false</code> otherwise.
     */
    public static boolean checkEMailAddress(String aAddress){
        return MAIL_ADDRESS_PATTERN.matcher(aAddress).matches();
    }
    
    private Object returnError(ParsePosition aPos, int aIndex) {
        aPos.setErrorIndex(aIndex);
        if (aPos instanceof DescriptiveParsePosition) {
            DescriptiveParsePosition desPos = (DescriptiveParsePosition)aPos;
			desPos.setErrorDescription(Resources.getInstance().getString(I18NConstants.INVALID_EMAIL_ADDRESSES));
        }
        return null;
    }
    
}

