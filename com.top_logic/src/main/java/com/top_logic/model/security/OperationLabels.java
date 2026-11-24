/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.security;

import java.util.Collection;
import java.util.TreeSet;

import com.top_logic.basic.util.ResKey;

/**
 * Naming of operations in messages and labels of the access parent settings.
 *
 * @see AccessParentStandsAlone
 * @see RestrictedRelation#getLabel()
 */
final class OperationLabels {

	private OperationLabels() {
		// Static utility.
	}

	/**
	 * The localized list of the given operations, named as the user knows them.
	 *
	 * @param operationNames
	 *        The names (IDs) of the operations, at least one.
	 * @return The labels of the operations sorted by name, as a comma-separated list; an operation
	 *         without a label is named by its name.
	 */
	static ResKey of(Collection<String> operationNames) {
		ResKey labels = null;
		for (String name : new TreeSet<>(operationNames)) {
			ResKey label = ResKey.fallback(
				com.top_logic.tool.boundsec.simple.I18NConstants.COMMAND_GROUP_NAMES.key(name), ResKey.text(name));
			labels = labels == null ? label : I18NConstants.OPERATION_LIST__LIST_OPERATION.fill(labels, label);
		}
		return labels;
	}

}
