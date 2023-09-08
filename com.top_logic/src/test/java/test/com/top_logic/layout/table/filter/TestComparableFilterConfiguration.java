/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.table.filter;

import junit.framework.TestCase;

import com.top_logic.layout.table.filter.AllOperatorsProvider;
import com.top_logic.layout.table.filter.ComparableFilterConfiguration;
import com.top_logic.layout.table.filter.ComparisonOperatorsProvider;
import com.top_logic.layout.table.filter.NumberComparator;

/**
 * Tests for {@link ComparableFilterConfiguration}
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public class TestComparableFilterConfiguration extends TestCase {

	public void testStandardConfigurationAfterCreation() {
		ComparisonOperatorsProvider operatorsProvider = AllOperatorsProvider.INSTANCE;
		ComparableFilterConfiguration config =
			new ComparableFilterConfiguration(null,
				null, operatorsProvider, NumberComparator.getInstance(), false, true);

		assertEquals("Standard filter pattern must be null!", null, config.getPrimaryFilterPattern());
		assertEquals("Standard operator must be " + operatorsProvider.getDefaultOperator().name() + "!",
			operatorsProvider.getDefaultOperator(),
			config.getOperator());
	}

}
