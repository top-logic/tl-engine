/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.col;

import junit.framework.Test;
import junit.framework.TestSuite;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.BasicTestSetup;

import com.top_logic.basic.col.AliasMapping;
import com.top_logic.basic.col.MapBuilder;
import com.top_logic.basic.col.Mapping;

/**
 * Test case for {@link AliasMapping}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TestAliasMapping extends BasicTestCase {

	public void testMapping() {
		Mapping mapping = new AliasMapping(new MapBuilder().put("a", "b").put("b", "a").put("c", "d").toMap());
		
		assertEquals("b", mapping.map("a"));
		assertEquals("a", mapping.map("b"));
		assertEquals("d", mapping.map("c"));
		assertEquals("d", mapping.map("d"));
		
	}

	public static Test suite() {
		return BasicTestSetup.createBasicTestSetup(new TestSuite(TestAliasMapping.class));
	}
	
}
