/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic;

import java.util.HashMap;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.top_logic.basic.NamedConstant;

/**
 * Test case for {@link NamedConstant}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TestNamedConstant extends TestCase {

	public void testEquals() {
		assertFalse("NamedConstants has object identity.", new NamedConstant("a").equals(new NamedConstant("a")));

		HashMap map = new HashMap();
		map.put(new NamedConstant("a"), "b");
		assertNull(map.get(new NamedConstant("a")));
	}
	
	public void testAsString() {
		assertEquals("a", new NamedConstant("a").asString());
	}
	
    public static Test suite() {
        return new TestSuite(TestNamedConstant.class);
    }
	
}
