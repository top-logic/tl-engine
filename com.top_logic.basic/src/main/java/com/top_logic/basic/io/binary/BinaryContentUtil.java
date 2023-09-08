/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.io.binary;

import static com.top_logic.basic.util.Utils.*;

import java.io.IOException;
import java.io.InputStream;

import com.top_logic.basic.io.BinaryContent;

/**
 * Utilities for working with {@link BinaryContent}s.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class BinaryContentUtil {

	/**
	 * Checks that {@link BinaryContent#getStream()} does not fail and the result is not null.
	 * <p>
	 * For better error messages than "NullPointerException" when trying to use the {@InputStream}.
	 * </p>
	 */
	public static BinaryContent checkFileExists(BinaryContent binaryContent) {
		try {
			InputStream stream = binaryContent.getStream();
			if (stream == null) {
				throw new NullPointerException("InputStream is null: " + debug(binaryContent));
			}
			stream.close();
			return binaryContent;
		} catch (IOException ex) {
			throw new RuntimeException("Failed to get the InputStream of: " + debug(binaryContent)
				+ ". Cause: " + ex.getMessage(), ex);
		}
	}

}
