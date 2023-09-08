/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.io.character;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

/**
 * {@link CharacterContent} wrapper for a literal {@link String}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class StringContent extends AbstractCharacterContent {

	private String _content;

	/**
	 * Creates a {@link StringContent} with the content as {@link #getName()}.
	 */
	public StringContent(String content) {
		this(content, content);
	}

	/**
	 * Creates a {@link StringContent}.
	 */
	public StringContent(String content, String debugName) {
		super(debugName);
		_content = content;
	}

	@Override
	public Reader getReader() throws IOException {
		return new StringReader(_content);
	}
}
