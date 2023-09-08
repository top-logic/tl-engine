/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.io.character;

import static com.top_logic.basic.io.Content.*;

import com.top_logic.basic.io.Content;

/**
 * Factory class to create {@link CharacterContent}s.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class CharacterContents {

	/**
	 * Creates a {@link CharacterContent} from the given char array.
	 * 
	 * @param content
	 *        The source of the content. May be <code>null</code>.
	 * @param debugName
	 *        The {@link Content#toString()} of the returned {@link CharacterContent}.
	 */
	public static CharacterContent newContent(char[] content, String debugName) {
		return new CharArrayContent(content, debugName);
	}

	/**
	 * Creates a {@link CharacterContent} from the given char array, with {@link Content#NO_NAME}.
	 * 
	 * @param content
	 *        The source of the content. May be <code>null</code>.
	 */
	public static CharacterContent newContent(char[] content) {
		return newContent(content, NO_NAME);
	}

	/**
	 * Creates a {@link CharacterContent} from the given {@link String} source.
	 * 
	 * @param content
	 *        The source of the content. May be <code>null</code>.
	 * @param name
	 *        The name of the returned {@link CharacterContent}.
	 */
	public static CharacterContent newContent(String content, String name) {
		return new MemoryCharacterContent(content, name);
	}

	/**
	 * Creates a {@link CharacterContent} from the given {@link String}, with
	 * {@link Content#NO_NAME}.
	 * 
	 * @param content
	 *        The source of the content. May be <code>null</code>.
	 */
	public static CharacterContent newContent(String content) {
		return newContent(content, NO_NAME);
	}

}

