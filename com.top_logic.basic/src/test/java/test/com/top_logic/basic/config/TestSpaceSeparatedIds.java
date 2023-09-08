/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.config;

import static test.com.top_logic.basic.BasicTestCase.*;

import junit.framework.TestCase;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.SpaceSeparatedIds;

/**
 * Test case for {@link SpaceSeparatedIds}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestSpaceSeparatedIds extends TestCase {

	public void testParse() throws ConfigurationException {
		assertEquals(list("a", "b", "cde_fgh-ijk"), SpaceSeparatedIds.INSTANCE.getValue(null, "a b cde_fgh-ijk"));
	}

	public void testFormat() {
		assertEquals("a b cde_fgh-ijk", SpaceSeparatedIds.INSTANCE.getSpecification(list("a", "b", "cde_fgh-ijk")));
	}

}
