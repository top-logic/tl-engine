/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.shared.collection;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Reports a cyclic dependency.
 * 
 * @see CollectionUtilShared#topsort(java.util.function.Function, java.util.Collection, boolean)
 */
public class CyclicDependencyException extends IllegalArgumentException {

	private List<?> _cycle;

	/**
	 * Creates a {@link CyclicDependencyException}.
	 */
	public CyclicDependencyException(List<?> cycle) {
		super("Cyclic dependencies, cannot sort topologically, cycle: "
			+ cycle.stream().map(Object::toString).collect(Collectors.joining(" -> ")));
		_cycle = cycle;
	}

	/**
	 * The objects that form the cycle.
	 */
	public List<?> getCycle() {
		return _cycle;
	}

}
