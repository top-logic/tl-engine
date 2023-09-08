/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.accessors;

import junit.framework.TestCase;

import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.layout.accessors.ReflectionInstanceAccessor;
import com.top_logic.layout.accessors.ReflectionInstanceAccessor.Config;

/**
 * Test case for {@link ReflectionInstanceAccessor}
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestReflectionInstanceAccessor extends TestCase {

	public class RowType {
		public String getTheValue() {
			return "Hello";
		}
	}

	public void testAccess() {
		Config config = TypedConfiguration.newConfigItem(ReflectionInstanceAccessor.Config.class);
		config.setType(RowType.class);
		config.setMethod("getTheValue");
		ReflectionInstanceAccessor accessor =
			SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY.getInstance(config);
		assertEquals("Hello", accessor.getValue(new RowType(), "foobar"));
		assertEquals(null, accessor.getValue(null, "foobar"));
		assertEquals(null, accessor.getValue("Some other object.", "foobar"));
	}

}
