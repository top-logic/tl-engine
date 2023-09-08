/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.check;

import java.io.IOException;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.io.binary.BinaryData;

/**
 * {@link AbstractFileEqualityCheck} which does not check the content.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class NoContentEqualityCheck extends AbstractFileEqualityCheck {

	/**
	 * Creates a new {@link NoContentEqualityCheck}.
	 * 
	 */
	public NoContentEqualityCheck(InstantiationContext context, FileMatchConfig config) {
		super(context, config);
	}

	@Override
	protected void checkContents(BinaryData actual, BinaryData expected) throws IOException {
		// ignore content.
	}

}

