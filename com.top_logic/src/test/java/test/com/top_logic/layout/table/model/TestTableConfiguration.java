/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.table.model;

import junit.framework.Test;

import com.top_logic.layout.table.model.TableConfiguration;

/**
 * Test case for {@link TableConfiguration}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TestTableConfiguration extends AbstractTestTableConfigurationTest<TableConfiguration> {

	@Override
	protected TableConfiguration createTableConfiguration() {
		return TableConfiguration.table();
	}

	/**
	 * Return the suite of tests to perform.
	 */
	public static Test suite() {
		return generateSuite(TestTableConfiguration.class);
	}
}
