/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config.format;

import java.text.Format;

import com.top_logic.basic.config.FormatValueProvider;
import com.top_logic.basic.json.JSONFormat;

/**
 * {@link FormatValueProvider} formatting JSON objects.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class JSONValueProvider extends FormatValueProvider {

	/**
	 * Creates a new {@link JSONValueProvider}.
	 */
	public JSONValueProvider() {
		super(Object.class);
	}

	@Override
	protected Format format() {
		return new JSONFormat().setPretty(false);
	}

}

