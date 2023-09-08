/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.form.constraints;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import junit.framework.Test;

import test.com.top_logic.TLTestSetup;
import test.com.top_logic.basic.BasicTestCase;

import com.top_logic.layout.form.CheckException;
import com.top_logic.layout.form.constraints.NotEmptyConstraint;

/**
 * Tests the class {@link NotEmptyConstraint}.
 * 
 * @author <a href="mailto:msi@top-logic.com">msi</a>
 */
public class TestNotEmptyConstraint extends BasicTestCase {

	private static final NotEmptyConstraint NOT_EMPTY_CONSTRAINT = NotEmptyConstraint.INSTANCE;

	@SuppressWarnings("javadoc")
	public void testCollection() throws CheckException {
		Collection<String> testCollection = new ArrayList<>();
		testCollection.add("value1");
		testCollection.add("value2");
		assertTrue(NOT_EMPTY_CONSTRAINT.check(testCollection));
	}

	@SuppressWarnings("javadoc")
	public void testMap() throws CheckException {
		Map<String, String> testMap = new HashMap<>();
		testMap.put("key1", "value1");
		testMap.put("key2", "value2");
		assertTrue(NOT_EMPTY_CONSTRAINT.check(testMap));
	}

	@SuppressWarnings("javadoc")
	public void testObject() throws CheckException {
		assertTrue(NOT_EMPTY_CONSTRAINT.check("value"));
	}

	@SuppressWarnings("javadoc")
	public void testCollectionEmpty() {
		Collection<String> testCollection = new ArrayList<>();
		try {
			NOT_EMPTY_CONSTRAINT.check(testCollection);
			fail();
		} catch (CheckException ex) {
		}
	}

	@SuppressWarnings("javadoc")
	public void testMapEmpty() {
		Map<String, String> testMap = new HashMap<>();
		try {
			NOT_EMPTY_CONSTRAINT.check(testMap);
			fail();
		} catch (CheckException ex) {
		}
	}

	@SuppressWarnings("javadoc")
	public void testObjectEmpty() {
		try {
			NOT_EMPTY_CONSTRAINT.check("");
			fail();
		} catch (CheckException ex) {
		}
	}

	@SuppressWarnings("javadoc")
	public void testNull() {
		try {
			NOT_EMPTY_CONSTRAINT.check(null);
			fail();
		} catch (CheckException ex) {
		}
	}

	/**
	 * Returns the test suite
	 * 
	 * @return the test suite
	 */
	public static Test suite() {
		return TLTestSetup.createTLTestSetup(TestNotEmptyConstraint.class);
	}
}
