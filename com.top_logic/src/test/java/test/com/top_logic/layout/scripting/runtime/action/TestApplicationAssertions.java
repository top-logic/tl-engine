/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.scripting.runtime.action;

import java.util.Collections;
import java.util.HashMap;

import junit.framework.TestCase;

import test.com.top_logic.basic.BasicTestCase;

import com.top_logic.basic.col.MapBuilder;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.layout.scripting.runtime.action.ApplicationAssertion;
import com.top_logic.layout.scripting.runtime.action.ApplicationAssertions;

/**
 * Test case for {@link ApplicationAssertions}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestApplicationAssertions extends TestCase {

	public void testMapEqualsNull() {
		try {
			ApplicationAssertions.assertMapEquals(context(), "Test equals.", Collections.emptyMap(), null);
			fail("Failure expected.");
		} catch (ApplicationAssertion ex) {
			BasicTestCase.assertContains("null", ex.getMessage());
		}
	}

	public void testMapEquals() {
		ApplicationAssertions.assertMapEquals(context(), "Test equals.", Collections.emptyMap(),
			new HashMap<>());
		ApplicationAssertions.assertMapEquals(context(), "Test equals.", Collections.singletonMap("foo", "bar"),
			new MapBuilder<>().put("foo", "bar").toMap());
	}

	public void testMapMissmatch() {
		try {
			ApplicationAssertions.assertMapEquals(context(), "Test equals.", Collections.singletonMap("foo", "bar"),
				new MapBuilder<>().put("foo", "bazz").toMap());
			fail("Failure expected.");
		} catch (ApplicationAssertion ex) {
			BasicTestCase.assertContains("Missmatch", ex.getMessage());
			BasicTestCase.assertContains("foo", ex.getMessage());
			BasicTestCase.assertContains("bar", ex.getMessage());
			BasicTestCase.assertContains("bazz", ex.getMessage());
		}
	}

	public void testMapMissing() {
		try {
			ApplicationAssertions.assertMapEquals(context(), "Test equals.", Collections.singletonMap("foo", "bar"),
				new MapBuilder<>().toMap());
			fail("Failure expected.");
		} catch (ApplicationAssertion ex) {
			BasicTestCase.assertContains("Missing", ex.getMessage());
			BasicTestCase.assertContains("foo", ex.getMessage());
			BasicTestCase.assertContains("bar", ex.getMessage());
		}
	}

	public void testMapUnexpected() {
		try {
			ApplicationAssertions.assertMapEquals(context(), "Test equals.", Collections.emptyMap(),
				new MapBuilder<>().put("foo", "bar").toMap());
			fail("Failure expected.");
		} catch (ApplicationAssertion ex) {
			BasicTestCase.assertContains("Unexpected", ex.getMessage());
			BasicTestCase.assertContains("foo", ex.getMessage());
			BasicTestCase.assertContains("bar", ex.getMessage());
		}
	}

	private ConfigurationItem context() {
		return TypedConfiguration.newConfigItem(ConfigurationItem.class);
	}

}
