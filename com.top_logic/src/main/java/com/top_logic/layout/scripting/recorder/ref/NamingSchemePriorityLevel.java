/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.recorder.ref;

import static java.util.Objects.*;

import com.top_logic.basic.tools.NameBuilder;

/**
 * A priority level for {@link ModelNamingScheme}s.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class NamingSchemePriorityLevel implements Comparable<NamingSchemePriorityLevel> {

	private final String _name;

	private final int _priority;

	/**
	 * Creates a {@link NamingSchemePriorityLevel}.
	 * 
	 * @param name
	 *        Is not allowed to be null.
	 */
	public NamingSchemePriorityLevel(String name, int priority) {
		_name = requireNonNull(name);
		_priority = priority;
	}

	/**
	 * The name of this priority level.
	 * 
	 * @return Never null.
	 */
	private String getName() {
		return _name;
	}

	/**
	 * The priority of this priority level.
	 */
	private int getPriority() {
		return _priority;
	}

	@Override
	public int compareTo(NamingSchemePriorityLevel other) {
		return Integer.compare(getPriority(), other.getPriority());
	}

	@Override
	public String toString() {
		return new NameBuilder(this)
			.add("name", getName())
			.add("priority", getPriority())
			.build();
	}

}
