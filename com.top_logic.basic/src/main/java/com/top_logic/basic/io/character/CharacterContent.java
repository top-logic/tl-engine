/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.io.character;

import java.io.IOException;
import java.io.Reader;

import com.top_logic.basic.io.BinaryContent;
import com.top_logic.basic.io.Content;

/**
 * Class for accessing {@link Character} content multiple times.
 * 
 * @see BinaryContent
 * @see CharacterContents
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public interface CharacterContent extends Content {

	/**
	 * Returns a new {@link Reader} with every call.
	 * <p>
	 * Don't forget to {@link Reader#close() close} them.
	 * </p>
	 * 
	 * @return Never <code>null</code>.
	 */
	public Reader getReader() throws IOException;

}
