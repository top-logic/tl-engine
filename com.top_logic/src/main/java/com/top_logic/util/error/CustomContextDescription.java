/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util.error;

import com.top_logic.basic.util.ResKey;
import com.top_logic.util.Resources;

/**
 * Custom {@link Resources} based {@link ContextDescription}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class CustomContextDescription implements ContextDescription {

	private ResKey _title;

	private ResKey _message;

	/**
	 * Creates a {@link CustomContextDescription}.
	 */
	public CustomContextDescription(ResKey title, ResKey message) {
		_title = title;
		_message = message;
	}

	@Override
	public ResKey getErrorTitle() {
		return _title;
	}

	@Override
	public ResKey getErrorMessage() {
		return _message;
	}

	@Override
	public void logError() {
		// Ignore.
	}

}
