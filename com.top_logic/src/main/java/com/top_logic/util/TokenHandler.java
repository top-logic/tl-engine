/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util;

import java.util.Iterator;
import java.util.Map;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.util.ResKey;

/**
 * The TokenHandler replaces all occurences of specified tokens with values from a given map.
 * 
 * @author     <a href="mailto:tsa@top-logic.com">tsa</a>
 */
public class TokenHandler {
    
    /** The tokens to replace */
    private Map tokens;
    
	private ResKey noValueForTokenResKey;
    
    public TokenHandler(Map someTokens) {
        this(someTokens, null);
    }

	public TokenHandler(Map someTokens, ResKey noValueKey) {
        this.tokens                = someTokens;
		this.noValueForTokenResKey = noValueKey == null ? I18NConstants.TOKEN_EMPTY_VALUE_REPLACEMENT : noValueKey;
    }
    
    /** 
     * This method replaces all token in the text.
     * 
     * @param aText The text.
     * @param aToken The token to be replaced.
     * @param aValue The value for the token which be replaced.
     * @return A {@link String}. 
     */
    private String replaceToken(String aText, String aToken, String aValue){
        if(! StringServices.isEmpty(aValue)){
        	return StringServices.replace(aText, aToken, aValue);
        }
        else{
            String noValueForToken = Resources.getInstance().getString(this.noValueForTokenResKey);
            return StringServices.replace(aText, aToken, noValueForToken);
        }
    }

    /**
	 * This method replaced the tokens in the given string with the values of the given contract.
	 * 
	 * @param aString
	 *        A string with tokens.
	 * @return Returns a string without tokens.
	 */
    public String replaceTokens(String aString){
    	if (StringServices.isEmpty(aString)) {
    		return aString;
    	}
    	
        for (Iterator theIt = this.tokens.entrySet().iterator(); theIt.hasNext();) {
            Map.Entry theTokenEntry = (Map.Entry) theIt.next();
            aString = replaceToken(aString, (String) theTokenEntry.getKey(), (String) theTokenEntry.getValue());
        }
        return aString;
    }
 

}
