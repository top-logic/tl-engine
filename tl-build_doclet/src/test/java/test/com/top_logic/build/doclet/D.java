/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.build.doclet;

/**
 * {@link Number} with silly additional interface {@link AutoCloseable}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class D extends Number implements AutoCloseable {

	@Override
	public void close() throws Exception {
		// Ignore.
	}

	@Override
	public int intValue() {
		return 0;
	}

	@Override
	public long longValue() {
		return 0;
	}

	@Override
	public float floatValue() {
		return 0;
	}

	@Override
	public double doubleValue() {
		return 0;
	}

}
