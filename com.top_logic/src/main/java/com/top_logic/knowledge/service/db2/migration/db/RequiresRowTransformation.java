/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2.migration.db;

import com.top_logic.knowledge.event.ChangeSet;
import com.top_logic.knowledge.event.convert.EventRewriter;

/**
 * Interface extended by {@link EventRewriter} to indicate that not only {@link ChangeSet} must be
 * rewritten but also row values from non item tables.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface RequiresRowTransformation extends EventRewriter {

	/**
	 * {@link RowTransformer} that is additionally required by this {@link EventRewriter}.
	 */
	RowTransformer getRequiredTransformations();

}

