/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.io.character;

import java.io.CharArrayReader;
import java.io.IOException;
import java.io.Reader;

import com.top_logic.basic.ArrayUtil;

/**
 * {@link AbstractCharacterContent} based on a char array.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
class CharArrayContent extends AbstractCharacterContent {

	private char[] _content;

	/**
	 * Creates a {@link CharArrayContent} by using the given character array as data source.
	 * 
	 * @param content
	 *        Is allowed to be <code>null</code>, which will result in the
	 *        {@link ArrayUtil#EMPTY_CHAR_ARRAY}.
	 */
	public CharArrayContent(char[] content, String debugName) {
		super(debugName);
		_content = ArrayUtil.nonNull(content);
	}

	@Override
	public Reader getReader() throws IOException {
		return new CharArrayReader(_content);
	}

}

