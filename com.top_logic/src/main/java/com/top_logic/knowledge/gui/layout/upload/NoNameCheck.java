/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.gui.layout.upload;

import com.top_logic.basic.util.ResKey;

/**
 * {@link FileNameStrategy} that imposes no constraints on a file name.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class NoNameCheck implements FileNameStrategy {

	/**
	 * Singleton {@link NoNameCheck} instance.
	 */
	public static final NoNameCheck INSTANCE = new NoNameCheck();

	private NoNameCheck() {
		// Singleton constructor.
	}

	@Override
	public ResKey checkFileName(String fileName) {
		return null;
	}

}
