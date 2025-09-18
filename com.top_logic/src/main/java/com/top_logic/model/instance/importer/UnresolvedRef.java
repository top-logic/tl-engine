/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.instance.importer;

import com.top_logic.model.instance.importer.schema.InstanceRefConf;

/**
 * Internal exception that signals a unresolvable (forwards) reference.
 */
public class UnresolvedRef extends Exception {

	private InstanceRefConf _ref;

	/**
	 * Creates a {@link UnresolvedRef}.
	 */
	public UnresolvedRef(InstanceRefConf ref) {
		super("Unresolved reference");
		_ref = ref;
	}

	/**
	 * The reference that cannot be resolved.
	 */
	public InstanceRefConf getRef() {
		return _ref;
	}

}
