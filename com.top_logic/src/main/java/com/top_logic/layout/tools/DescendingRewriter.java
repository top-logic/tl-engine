/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.tools;

import java.io.File;
import java.io.IOException;

/**
 * {@link Rewriter} that descends a directory for files to rewrite.
 */
public abstract class DescendingRewriter extends Rewriter {

	@Override
	public void handleFile(String fileName) throws Exception {
		descend(new File(fileName));
	}

	private void descend(File file) throws Exception {
		if (file.getName().startsWith(".")) {
			return;
		}
		if (file.isDirectory()) {
			File[] contents = file.listFiles();
			if (contents != null) {
				for (File content : contents) {
					descend(content);
				}
			}
		} else {
			if (matches(file)) {
				handleFile(file);
			}
		}
	}

	/**
	 * Performs the actual rewrite operation on the given file.
	 */
	protected abstract void handleFile(File file) throws IOException;

	/**
	 * Whether the given file should be handled.
	 */
	protected abstract boolean matches(File file);

}
