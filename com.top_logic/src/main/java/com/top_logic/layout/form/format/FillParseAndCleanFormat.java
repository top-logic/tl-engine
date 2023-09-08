/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.format;

import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;

import com.top_logic.basic.StringServices;

/**
 * Fill a String when parsing and strip it when formatting
 * 
 * @author    <a href=mailto:kbu@top-logic.com>kbu</a>
 */
public class FillParseAndCleanFormat extends Format {

	private final int  length;
	private final int  startPosition;
	private final char fillChar;
	
	/**
	 * Creates a new FillFormat.
	 * 
	 * @param aLenght the size to fill up the string
	 * @param aStartPosition either {@link com.top_logic.basic.StringServices#START_POSITION_HEAD} or 
	 *                             {@link com.top_logic.basic.StringServices#START_POSITION_TAIL}
	 * @param aFillChar the character used to fill up the string
	 */
	public FillParseAndCleanFormat(int aLenght,int aStartPosition,char aFillChar) {
		this.length        = aLenght;
		this.startPosition = aStartPosition;
		this.fillChar      = aFillChar;
	}

	/** 
	 * @see java.text.Format#parseObject(java.lang.String, java.text.ParsePosition)
	 */
	@Override
	public Object parseObject(String str, ParsePosition pos) {
		pos.setIndex(pos.getIndex() + str.length());
		
		return StringServices.fillString(str,length,fillChar,startPosition);
	}

	/** 
	 * @see java.text.Format#format(java.lang.Object, java.lang.StringBuffer, java.text.FieldPosition)
	 */
	@Override
	public StringBuffer format(Object obj, StringBuffer toAppendTo,
			FieldPosition pos) {
		return toAppendTo.append(StringServices.stripString((String) obj,fillChar,startPosition,""));
	}

}
