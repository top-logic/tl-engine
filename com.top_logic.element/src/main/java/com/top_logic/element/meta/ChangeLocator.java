/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta;

import java.util.function.Consumer;

import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.knowledge.service.event.PendingChanges;
import com.top_logic.model.TLStructuredTypePart;

/**
 * Way to find the objects whose value of an attribute may have changed in a commit, from the
 * changed rows of one table.
 *
 * <p>
 * A {@link StorageImplementation} contributes its locators in
 * {@link StorageImplementation#addChangeLocators(TLStructuredTypePart, java.util.function.BiConsumer)}.
 * </p>
 */
@FunctionalInterface
public interface ChangeLocator {

	/**
	 * Reports the objects whose value may have changed by the changes of the table for which this
	 * locator is registered.
	 *
	 * <p>
	 * An object may be reported several times. It may be reported although its value did not
	 * change, although it does not have the attribute, or although it is created or removed in the
	 * transaction.
	 * </p>
	 *
	 * @param changes
	 *        The changes of the transaction being committed.
	 * @param objects
	 *        Receives the IDs of the objects.
	 */
	void findChangedObjects(PendingChanges changes, Consumer<? super ObjectKey> objects);

}
