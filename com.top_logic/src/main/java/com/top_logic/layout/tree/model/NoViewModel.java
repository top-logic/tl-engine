/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.tree.model;

import com.top_logic.basic.col.Provider;
import com.top_logic.layout.table.TableViewModel;

/**
 * {@link Provider} of {@link TableViewModel} that always return null.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class NoViewModel implements Provider<TableViewModel> {

	/** Singleton {@link NoViewModel} instance. */
	public static final NoViewModel INSTANCE = new NoViewModel();

	private NoViewModel() {
		// singleton instance
	}

	@Override
	public TableViewModel get() {
		return null;
	}

}

