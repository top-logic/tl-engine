/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2.migration;

import com.top_logic.basic.Log;
import com.top_logic.knowledge.event.ChangeSet;

/**
 * Special {@link Log} for migration.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface Migration extends Log {

	/**
	 * Returns the {@link ChangeSet} currently processed.
	 */
	ChangeSet current();

}
