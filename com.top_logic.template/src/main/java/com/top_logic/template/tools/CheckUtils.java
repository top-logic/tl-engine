/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.template.tools;

import com.top_logic.template.parser.ParseException;
import com.top_logic.template.parser.Token;

/**
 * This class contains help methods and classes for template parsing and checking.
 * 
 * @author    <a href=mailto:tbe@top-logic.com>tbe</a>
 */
public class CheckUtils {

	/**
	 * Returns a String array with two strings, result[0] is a String with the expected tokens at
	 * the position where the given {@link ParseException} was thrown. result[1] is a String with
	 * the encountered tokens at that position.
	 * 
	 * @param aPE a caught {@link ParseException}
	 * @return a String[2] array with the expected and encountered tokens as strings.
	 */
	public static String[] getProblemTokens(ParseException aPE) {
		String theEncountered = "";
		String theExpected    = "";
		int    maxSize        = 0;
		Token  tok            = aPE.currentToken.next;
		
		for (int i = 0; i < aPE.expectedTokenSequences.length; i++) {
			int expectedLength = aPE.expectedTokenSequences[i].length;
			
			if (maxSize < expectedLength) {
				maxSize = expectedLength;
			}
			
			for (int j = 0; j < expectedLength; j++) {
				theExpected += (aPE.tokenImage[aPE.expectedTokenSequences[i][j]]);
			}
		
			if (aPE.expectedTokenSequences[i][expectedLength - 1] != 0 && i < aPE.expectedTokenSequences.length - 1) {
				theExpected += (", ");
			}
		}
		
		for (int i = 0; i < maxSize; i++) {
			if (i != 0) {
				theEncountered += " ";
			}
			if (tok.kind == 0) {
				theEncountered += aPE.tokenImage[0];
				break;
			}
			theEncountered += tok.image;
			tok = tok.next;
		}

		return new String[] {theExpected, theEncountered};
	}
}
