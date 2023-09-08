/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.col;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;

import junit.framework.Test;
import junit.framework.TestSuite;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.BasicTestSetup;

import com.top_logic.basic.col.ImmutableSet;

/**
 * Test case for {@link ImmutableSet}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TestImmutableSet extends BasicTestCase {

	private TestingImmutableSet immutableSet;

	private static class TestingImmutableSet extends ImmutableSet {

		public TestingImmutableSet() {
			// Empty.
		}

		@Override
		public Iterator iterator() {
			return Collections.EMPTY_SET.iterator();
		}

		@Override
		public int size() {
			return 0;
		}

	}
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		
		immutableSet = new TestingImmutableSet();
	}
	
	@Override
	protected void tearDown() throws Exception {
		immutableSet = null;

		super.tearDown();
	}
	
	public void testImmutableAdd() {
		try {
			immutableSet.add(new Object());
			fail("Not immutable.");
		} catch (UnsupportedOperationException ex) {
			// Expected.
		}
	}
	
	public void testImmutableAddAll() {
		try {
			immutableSet.addAll(new HashSet());
			fail("Not immutable.");
		} catch (UnsupportedOperationException ex) {
			// Expected.
		}
	}
	
	public void testImmutableRemove() {
		try {
			immutableSet.remove(null);
			fail("Not immutable.");
		} catch (UnsupportedOperationException ex) {
			// Expected.
		}
	}
	
	public void testImmutableRemoveAll() {
		try {
			immutableSet.removeAll(new HashSet());
			fail("Not immutable.");
		} catch (UnsupportedOperationException ex) {
			// Expected.
		}
	}
	
	public void testImmutableRetainAll() {
		try {
			immutableSet.retainAll(new HashSet());
			fail("Not immutable.");
		} catch (UnsupportedOperationException ex) {
			// Expected.
		}
	}
	
	public void testImmutableClear() {
		try {
			immutableSet.clear();
			fail("Not immutable.");
		} catch (UnsupportedOperationException ex) {
			// Expected.
		}
	}

	public static Test suite() {
		return BasicTestSetup.createBasicTestSetup(new TestSuite(TestImmutableSet.class));
	}

}
