/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.format;

import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;

import com.top_logic.basic.StringServices;

/**
 * Format used to fill a String up to a given length using a character. You also need to define a start-position.
 * This might either be {@link com.top_logic.basic.StringServices#START_POSITION_HEAD} or 
 * {@link com.top_logic.basic.StringServices#START_POSITION_TAIL}. 
 * START_POSITION_HEAD means to fill up the string from the head and START_POSITION_TAIL from the 
 * tail. If the string is longer than the given length an {@link java.lang.IllegalArgumentException} is thrown.
 * 
 * <p>
 * This is best explained in an example:
 * 
 * <table border="1">
 * 	<tr>
 * 	  <th>input</th>
 *    <th>length</th> 
 *    <th>character</th> 
 *    <th>start-position</th> 
 *    <th>output</th> 
 *  </tr>
 *  
 * 	<tr>
 * 	  <td>xxx</td>
 *    <td>5</td> 
 *    <td>#</td> 
 *    <td>HEAD</td> 
 *    <td>##xxx</td> 
 *  </tr>
 *  
 * 	<tr>
 * 	  <td>123</td>
 *    <td>6</td> 
 *    <td>.</td> 
 *    <td>TAIL</td> 
 *    <td>123...</td> 
 *  </tr>
 * 
 * 	<tr>
 * 	  <td>A string longer than 6</td>
 *    <td>6</td> 
 *    <td>.</td> 
 *    <td>TAIL</td> 
 *    <td>IllegalArgumentException</td> 
 *  </tr>
 *  
 * </table>
 * </p> 
 * 
 * @author     <a href="mailto:tma@top-logic.com">tma</a>
 */
public class FillFormat extends Format {
	
	private final int length;
	private final int startPosition;
	private final char fillChar;
	
	/**
	 * Creates a new FillFormat.
	 * 
	 * @param length the size to fill up the string
	 * @param startPosition either {@link com.top_logic.basic.StringServices#START_POSITION_HEAD} or 
	 *                             {@link com.top_logic.basic.StringServices#START_POSITION_TAIL}
	 * @param fillChar the character used to fill up the string
	 */
	public FillFormat(int length,int startPosition,char fillChar) {
		this.length = length;
		this.startPosition = startPosition;
		this.fillChar = fillChar;
	}

	/** 
	 * Returns the input if it is lower than the given length, otherwise
	 * the input string is truncated to the given length.
	 * 
	 * @see java.text.Format#parseObject(java.lang.String, java.text.ParsePosition)
	 */
	@Override
	public Object parseObject(String source, ParsePosition pos) {
		
		if (source.length() > length){
			source = source.substring(0,length);
		}
		
		pos.setIndex(pos.getIndex() + source.length());
		
		return source;
	}

	/** 
	 * @see java.text.Format#format(java.lang.Object, java.lang.StringBuffer, java.text.FieldPosition)
	 */
	@Override
	public StringBuffer format(Object obj, StringBuffer toAppendTo,
			FieldPosition pos) {
		
		String str = (String) obj;
		
		if (str.length() > length){
			throw new IllegalArgumentException("The input string '" +  str +  "' may not be longer than the given length '" + length + "'");
		}
		
		return toAppendTo.append(StringServices.fillString(str,length,fillChar,startPosition));
	}
	
}
