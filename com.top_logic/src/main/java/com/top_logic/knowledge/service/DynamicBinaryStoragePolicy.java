/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service;

import com.top_logic.knowledge.objects.KnowledgeItem;

/**
 * Decides where the content of a binary value of a dynamic attribute is stored.
 *
 * <p>
 * Dynamic attributes are attributes of a {@link KnowledgeItem} without a column of their own in
 * the table of the item. Their binary values are stored in a table shared by all dynamic binary
 * attributes. The policy chooses blob store and threshold per value, when the value is assigned to
 * the attribute.
 * </p>
 *
 * @see FlexDataManagerFactory.Config#getBinaryStoragePolicy()
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface DynamicBinaryStoragePolicy {

	/**
	 * The storage settings for a binary value of the given dynamic attribute.
	 *
	 * @param item
	 *        The item the value is assigned to.
	 * @param attribute
	 *        The name of the dynamic attribute.
	 * @param defaults
	 *        The settings configured for all dynamic binary attributes.
	 * @return The settings to use for the value, not <code>null</code>.
	 */
	BinaryStorageSettings forValue(KnowledgeItem item, String attribute, BinaryStorageSettings defaults);

}
