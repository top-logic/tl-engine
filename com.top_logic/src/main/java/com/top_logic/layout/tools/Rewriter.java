/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.tools;

import com.top_logic.basic.Main;

/**
 * Base class for file rewriting tools.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class Rewriter extends Main implements FileHandler {

	@Override
	protected boolean argumentsRequired() {
		return true;
	}

	@Override
	protected int parameter(String[] args, int i) throws Exception {
		FileUtil.handleFile(args[i++], this);
		return i;
	}

	@Override
	protected void doActualPerformance() throws Exception {
		// Nothing more.
	}

}
