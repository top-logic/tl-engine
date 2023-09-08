/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.io.character;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import com.top_logic.basic.StringServices;

/**
 * A {@link CharacterContent} for character arrays or Strings as data sources.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
class MemoryCharacterContent extends AbstractCharacterContent {

	/** Use Strings and not char[], as Strings are already immutable. */
	private final String _content;

	/**
	 * Creates a {@link MemoryCharacterContent} by using the given string as data source.
	 * 
	 * @param content
	 *        Is allowed to be <code>null</code>, which will result in the empty string.
	 */
	public MemoryCharacterContent(String content, String debugName) {
		super(debugName);
		_content = StringServices.nonNull(content);
	}

	@Override
	public Reader getReader() throws IOException {
		return new StringReader(_content);
	}

}
