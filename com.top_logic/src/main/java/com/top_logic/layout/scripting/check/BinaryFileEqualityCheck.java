/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.check;

import static com.top_logic.layout.scripting.runtime.action.ApplicationAssertions.*;

import java.io.IOException;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.io.binary.BinaryData;

/**
 * Checks whether the actual file is equal to the expected file based on its name, content type and
 * binary data.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public final class BinaryFileEqualityCheck extends AbstractFileEqualityCheck {

	public BinaryFileEqualityCheck(InstantiationContext context, FileMatchConfig config) {
		super(context, config);
	}

	@Override
	protected void checkContents(BinaryData actual, BinaryData expected) throws IOException {
		assertEquals(_config, "Unexpected content!", expected, actual);
	}

}
