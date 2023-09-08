/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.migrate.tl.skip;

import java.util.Set;

import com.top_logic.knowledge.event.ItemCreation;
import com.top_logic.knowledge.objects.identifier.ObjectBranchId;

/**
 * Filter that decides which event must be skipped.
 * 
 * @see ItemEventSkip
 * 
 * @author <a href=mailto:daniel.busche@top-logic.com>Daniel Busche</a>
 */
public interface SkipFilter {

	/**
	 * Returns <code>true</code> iff the event must be skipped.
	 * 
	 * @param event
	 *        the event to inspect or modify
	 * @param skippedObjects
	 *        the {@link ObjectBranchId ids} of all objects formerly dropped. Should not be
	 *        modified.
	 * @return if <code>true</code> the event will be skipped.
	 */
	boolean skipEvent(ItemCreation event, Set<ObjectBranchId> skippedObjects);

}
