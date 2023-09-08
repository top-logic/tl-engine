/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.build.doclet;

/**
 * {@link Silly} enum with an {@link E.Inner} enum.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public enum E {

	/**
	 * The first.
	 */
	A,

	/**
	 * The second.
	 */
	B,

	/**
	 * Name that is matched by an acronym config.
	 */
	MyXYAcronymConst;

	/**
	 * An inner enum of {@link E}.
	 */
	enum Inner {
		FOO;
	}
}
