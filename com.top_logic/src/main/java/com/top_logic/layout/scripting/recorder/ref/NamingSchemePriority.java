/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.recorder.ref;

import static java.util.Objects.*;

import com.top_logic.basic.tools.NameBuilder;

/**
 * Represents the priority of a {@link ModelNamingScheme}.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class NamingSchemePriority implements Comparable<NamingSchemePriority> {

	private final NamingSchemePriorityLevel _priorityLevel;

	private final int _declarationOrder;

	/**
	 * @param priorityLevel
	 *        Is not allowed to be null.
	 * @param declarationOrder
	 *        The declaration order of the {@link ModelNamingScheme}.
	 */
	public NamingSchemePriority(NamingSchemePriorityLevel priorityLevel, int declarationOrder) {
		_priorityLevel = requireNonNull(priorityLevel);
		_declarationOrder = declarationOrder;
	}

	private NamingSchemePriorityLevel getPriorityLevel() {
		return _priorityLevel;
	}

	private int getDeclarationOrder() {
		return _declarationOrder;
	}

	@Override
	public int compareTo(NamingSchemePriority other) {
		int priorityLevelResult = getPriorityLevel().compareTo(other.getPriorityLevel());
		if (priorityLevelResult != 0) {
			return priorityLevelResult;
		}
		return Integer.compare(getDeclarationOrder(), other.getDeclarationOrder());
	}

	@Override
	public String toString() {
		return new NameBuilder(this)
			.add("priority-level", getPriorityLevel())
			.add("declaration-order", getDeclarationOrder())
			.build();
	}

}
