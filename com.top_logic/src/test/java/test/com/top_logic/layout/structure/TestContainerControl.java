/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.structure;

import java.util.Arrays;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.ModuleLicenceTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.layout.basic.fragments.Fragments;
import com.top_logic.layout.structure.ContainerControl;
import com.top_logic.layout.structure.DefaultExpandable;
import com.top_logic.layout.structure.FixedFlowLayoutControl;
import com.top_logic.layout.structure.LayoutControlAdapter;
import com.top_logic.layout.structure.MaximizableControl;
import com.top_logic.layout.structure.OrientationAware.Orientation;

/**
 * Test case for {@link ContainerControl}s.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestContainerControl extends TestCase {

	public void testAddChild() {
		FixedFlowLayoutControl parent = new FixedFlowLayoutControl(Orientation.VERTICAL);
		LayoutControlAdapter child = new LayoutControlAdapter(Fragments.text("foo"));
		parent.addChild(child);

		assertEquals(parent, child.getParent());

		LayoutControlAdapter child1 = new LayoutControlAdapter(Fragments.text("bar"));
		LayoutControlAdapter child2 = new LayoutControlAdapter(Fragments.text("baz"));

		parent.setChildren(Arrays.asList(child1, child2));
		assertNull(child.getParent());
		assertEquals(parent, child1.getParent());
		assertEquals(parent, child2.getParent());
	}

	public void testSetChildControl() {
		MaximizableControl parent = new MaximizableControl(new DefaultExpandable());
		LayoutControlAdapter child = new LayoutControlAdapter(Fragments.text("foo"));
		parent.setChildControl(child);

		assertEquals(parent, child.getParent());

		LayoutControlAdapter newChild = new LayoutControlAdapter(Fragments.text("bar"));

		parent.setChildControl(newChild);
		assertNull(child.getParent());
		assertEquals(parent, newChild.getParent());
	}

	public static Test suite() {
		return ModuleLicenceTestSetup.setupModule(ServiceTestSetup.createSetup(TestContainerControl.class,
			ThreadContextManager.Module.INSTANCE));
	}

}
