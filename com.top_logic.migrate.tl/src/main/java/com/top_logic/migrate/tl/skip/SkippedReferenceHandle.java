/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.migrate.tl.skip;

import java.util.Set;

import com.top_logic.dob.meta.MOReference;
import com.top_logic.knowledge.event.ItemUpdate;
import com.top_logic.knowledge.event.ObjectCreation;
import com.top_logic.knowledge.objects.identifier.ObjectBranchId;

/**
 * Handler that reacts on references to skipped objects.
 * 
 * <p>
 * This handler modifies or skips {@link ItemUpdate} or {@link ObjectCreation}. In case the event
 * contains an value for an {@link MOReference} which points to an objects whose creation was
 * formerly skipped, then the handler must decide how to change the attribute value, or whether the
 * event must be skipped completely.
 * </p>
 * 
 * @see ItemEventSkip
 * 
 * @since 5.8.0
 * 
 * @author <a href=mailto:daniel.busche@top-logic.com>Daniel Busche</a>
 */
public interface SkippedReferenceHandle {

	/**
	 * Reacts on the given {@link ItemUpdate} as the value of the given {@link MOReference} was set
	 * to an object which was not created.
	 * 
	 * @param event
	 *        The event to inspect or modify.
	 * @param attribute
	 *        The attribute which points to an object whose creation was skipped before.
	 * @param skippedObjects
	 *        The {@link ObjectBranchId ids} of all objects formerly dropped. Should not be
	 *        modified.
	 * 
	 * @return If <code>true</code> the event will be skipped.
	 */
	boolean handleSkippedReference(ItemUpdate event, MOReference attribute, Set<ObjectBranchId> skippedObjects);

	/**
	 * Reacts on the given {@link ObjectCreation} as the value of the given {@link MOReference} was
	 * set to an object which was not created.
	 * 
	 * @param event
	 *        The event to inspect or modify.
	 * @param attribute
	 *        The attribute which points to an object whose creation was skipped before.
	 * @param skippedObjects
	 *        The {@link ObjectBranchId ids} of all objects formerly dropped. Should not be
	 *        modified.
	 * 
	 * @return If <code>true</code> the event will be skipped.
	 */
	boolean handleSkippedReference(ObjectCreation event, MOReference attribute, Set<ObjectBranchId> skippedObjects);

}
