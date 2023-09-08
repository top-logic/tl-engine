/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.col;

import junit.framework.TestCase;

import com.top_logic.basic.col.ConstantMapping;

/**
 * Test case for {@link ConstantMapping}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TestConstantMapping extends TestCase {

	public void testMapping() {
		ConstantMapping<String> mapping = new ConstantMapping<>("A");
		assertEquals("A", mapping.map("B"));
		assertEquals("A", mapping.map(null));
		assertEquals("A", mapping.map(new Object()));
	}
}
