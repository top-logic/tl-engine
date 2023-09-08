/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.io;

import java.io.IOException;
import java.io.InputStream;

import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.io.character.CharacterContent;

/**
 * Class that provides an {@link InputStream} to deliver multiple times. 
 * 
 * @see CharacterContent
 * 
 * @author    <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@Label("Binary data")
public interface BinaryContent extends Content {
	
    /**
	 * Return a Stream representing the underlying data.
	 * 
	 * This function can be called multiple Times. Please {@link InputStream#close() close} the
	 * returned stream as soon as possible.
	 * 
	 * @return Is allowed to return <code>null</code>.
	 * 
	 * @throws IOException
	 *         in case retrieving the Data is not possible (any more).
	 */
	public InputStream getStream() throws IOException;

}
