/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.common.folder.model;

import com.top_logic.basic.col.Filter;

/**
 * Filter matching {@link FolderNode} that are {@link FolderNode#isFolder() folder}s.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class FolderFilter implements Filter<FolderNode> {

	/** Singleton {@link FolderFilter} instance. */
	public static final FolderFilter INSTANCE = new FolderFilter();

	private FolderFilter() {
		// singleton instance
	}

	@Override
	public boolean accept(FolderNode anObject) {
		return anObject.isFolder();
	}

}

