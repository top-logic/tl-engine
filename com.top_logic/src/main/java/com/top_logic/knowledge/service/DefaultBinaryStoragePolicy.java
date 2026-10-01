/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service;

import com.top_logic.knowledge.objects.KnowledgeItem;

/**
 * {@link DynamicBinaryStoragePolicy} using the configured defaults for all values.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DefaultBinaryStoragePolicy implements DynamicBinaryStoragePolicy {

	/**
	 * Singleton {@link DefaultBinaryStoragePolicy} instance.
	 */
	public static final DefaultBinaryStoragePolicy INSTANCE = new DefaultBinaryStoragePolicy();

	/**
	 * Creates a {@link DefaultBinaryStoragePolicy}.
	 */
	protected DefaultBinaryStoragePolicy() {
		// Singleton constructor.
	}

	@Override
	public BinaryStorageSettings forValue(KnowledgeItem item, String attribute, BinaryStorageSettings defaults) {
		return defaults;
	}

}
