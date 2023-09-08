/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2.migration.rewriters;

import java.util.function.Consumer;

import com.top_logic.knowledge.event.ItemChange;

/**
 * Rewrite algorithm to rewrite a visited {@link ItemChange}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface ItemChangeRewriter extends Consumer<ItemChange> {

	/**
	 * Rewrites the given {@link ItemChange}.
	 * 
	 * @param event
	 *        The {@link ItemChange} to visit.
	 */
	void rewriteItemChange(ItemChange event);

	@Override
	default void accept(ItemChange event) {
		rewriteItemChange(event);
	}

}

