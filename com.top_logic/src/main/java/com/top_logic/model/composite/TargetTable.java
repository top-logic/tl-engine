/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.composite;

import com.top_logic.model.TLObject;
import com.top_logic.model.TLReference;

/**
 * Container is stored in the table in which the part is stored.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class TargetTable extends ContainerStorage {

	private final String _container;

	/**
	 * Creates a new {@link TargetTable}.
	 */
	public TargetTable(String container) {
		_container = container;
	}

	/**
	 * Name of the column in which the container is stored.
	 */
	public String getContainer() {
		return _container;
	}

	/**
	 * Derived the {@link TLReference} in which the given part is stored.
	 */
	public abstract TLReference getReference(TLObject part);

}

