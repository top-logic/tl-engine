/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.toolbar;

import junit.framework.Test;

import test.com.top_logic.layout.AbstractLayoutTest;

import com.top_logic.layout.structure.DefaultExpandable;
import com.top_logic.layout.toolbar.DefaultToolBar;

/**
 * Test for {@link DefaultToolBar}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@SuppressWarnings("javadoc")
public class TestDefaultToolbar extends AbstractLayoutTest {

	private DefaultToolBar _toolBar;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_toolBar = new DefaultToolBar(this, new DefaultExpandable());
	}

	public void testDefineGroup() {
		// Insert at end
		_toolBar.defineGroup(0, "TestDefaultToolbar_Group1");
		assertEquals(0, _toolBar.getGroupIndex("TestDefaultToolbar_Group1"));
		_toolBar.defineGroup(1, "TestDefaultToolbar_Group2");
		assertEquals(1, _toolBar.getGroupIndex("TestDefaultToolbar_Group2"));
		_toolBar.defineGroup(2, "TestDefaultToolbar_Group3");
		assertEquals(2, _toolBar.getGroupIndex("TestDefaultToolbar_Group3"));

		// Insert at first
		_toolBar.defineGroup(0, "TestDefaultToolbar_Group0");
		assertEquals(0, _toolBar.getGroupIndex("TestDefaultToolbar_Group0"));
		assertEquals(1, _toolBar.getGroupIndex("TestDefaultToolbar_Group1"));
		assertEquals(2, _toolBar.getGroupIndex("TestDefaultToolbar_Group2"));
		assertEquals(3, _toolBar.getGroupIndex("TestDefaultToolbar_Group3"));
		// Insert in the middle
		_toolBar.defineGroup(2, "TestDefaultToolbar_Group4");
		assertEquals(2, _toolBar.getGroupIndex("TestDefaultToolbar_Group4"));
		assertEquals(3, _toolBar.getGroupIndex("TestDefaultToolbar_Group2"));
		assertEquals(4, _toolBar.getGroupIndex("TestDefaultToolbar_Group3"));

		try {
			_toolBar.defineGroup(20, "IndexOutOfBounds");
			fail("Illegal index. There are less existing groups.");
		} catch (Exception ex) {
			// expected
		}
	}

	/**
	 * a cumulative {@link Test} for all Tests in {@link TestDefaultToolbar}.
	 */
	public static Test suite() {
		return suite(TestDefaultToolbar.class);
	}

}
