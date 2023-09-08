/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout;

import java.io.IOException;

/**
 * Dynamically produce text content.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface DynamicText {

	/**
	 * Writes some dynamic content depending on the given {@link DisplayContext} to the given
	 * {@link Appendable}.
	 */
	void append(DisplayContext context, Appendable out) throws IOException;

}
