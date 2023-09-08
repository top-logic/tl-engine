/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.col;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.BasicTestSetup;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.col.ImmutableSetView;

/**
 * Test case for {@link ImmutableSetView}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TestImmutableSetView extends BasicTestCase {

	protected HashSet<String>      originals;
	protected HashSet<String>      views;
	protected Collection<String>   view;

	private static class ReversedStringCollection extends ImmutableSetView<String, String>  {

		public ReversedStringCollection(Set<String> original) {
			super(original);
		}

		@Override
		protected String getOriginalMember(Object o) {
			return new StringBuilder((String)o).reverse().toString();
		}

		@Override
		protected String getViewMember(Object originalMember) {
			return new StringBuilder((String)originalMember).reverse().toString();
		}

		@Override
		protected boolean isCompatible(Object o) {
			return o instanceof String;
		}
		
	}
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		
		originals = new HashSet<>(Arrays.asList(new String[] {
			"Hello", "World", "!", "123456789"
		}));
		
		views = new HashSet<>(Arrays.asList(new String[] {
			"olleH", "dlroW", "!", "987654321"
		}));
		
		view = new ReversedStringCollection(originals);
		
	}
	
	@Override
	protected void tearDown() throws Exception {
		this.originals = null;
		this.views    = null;
		this.view     = null;
		
		super.tearDown();
	}
	
	public void testEquals() {
		assertEquals(views, view);
	}

	public void testSize() {
		assertEquals(view.size(), views.size());
	}
	
	public void testIsEmpty() {
		assertFalse(view.isEmpty());
		assertTrue(new ReversedStringCollection(new HashSet<>()).isEmpty());
	}
	
	public void testContains() {
		assertTrue(view.contains("olleH"));
		assertTrue(view.contains("dlroW"));
		assertTrue(view.contains("!"));
	}

	public void testNotContainsIncompatible() {
		assertFalse(view.contains(null));
		assertFalse(view.contains(new Object()));
	}

	public void testContainsAll() {
		assertTrue(view.containsAll(views));
	}
	
	public void testIterator() {
		HashSet<String> viewCopy = CollectionUtil.toSet(view.iterator());
		assertEquals(views, viewCopy);
	}

	public void testIteratorImmutable() {
		Iterator<String> it = view.iterator();
		assertTrue(it.hasNext());
		it.next();
		
		try {
			it.remove();
			fail("Not immutable.");
		} catch (UnsupportedOperationException ex) {
			// Expected.
		}
	}

	public static Test suite() {
		return BasicTestSetup.createBasicTestSetup(new TestSuite(TestImmutableSetView.class));
	}

}
