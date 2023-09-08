/*
 * SPDX-FileCopyrightText: 2002 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.mig.html.layout;

import static test.com.top_logic.ComponentTestUtils.*;

import java.util.List;

import junit.framework.Test;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.base.services.simpleajax.RequestLockFactory;
import com.top_logic.basic.listener.EventType.Bubble;
import com.top_logic.mig.html.layout.ChildrenChangedListener;
import com.top_logic.mig.html.layout.ComponentName;
import com.top_logic.mig.html.layout.Layout;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.LayoutConstants;
import com.top_logic.mig.html.layout.LayoutContainer;
import com.top_logic.mig.html.layout.MainLayout;
import com.top_logic.mig.html.layout.SimpleComponent;
import com.top_logic.tool.boundsec.CommandHandlerFactory;
import com.top_logic.tool.boundsec.wrap.SecurityComponentCache;

/**
 * Testcase to test classes in {@link test.com.top_logic.mig.html.layout}.
 *
 * @author  <a href="mailto:kha@top-logic.com">kha</a>
 */
public class TestLayout extends BasicTestCase {

	private static class TestingChildListener implements ChildrenChangedListener {

		List<LayoutComponent> _oldChildren;

		List<LayoutComponent> _newChildren;

		TestingChildListener() {
			super();
		}

		@Override
		public Bubble notifyChildrenChanged(LayoutContainer sender, List<LayoutComponent> oldChildren,
				List<LayoutComponent> newValue) {
			_oldChildren = oldChildren;
			_newChildren = newValue;
			return Bubble.BUBBLE;
		}

	}

	/**
	 * Tests sending events on children changed.
	 */
	public void testChildrenListener() {
		Layout main = newMainLayout();
		TestingChildListener l = new TestingChildListener();
		main.addListener(LayoutContainer.CHILDREN_PROPERTY, l);

		Layout la = newLayout(Layout.HORIZONTAL);
		Layout lb = newLayout(Layout.HORIZONTAL);
		Layout lc = newLayout(Layout.HORIZONTAL);

		main.addComponent(la);
		assertChildrenEventSent(l, list(), list(la));
		main.addComponent(0, lb);
		assertChildrenEventSent(l, list(la), list(lb, la));
		main.addComponent(lc);
		assertChildrenEventSent(l, list(lb, la), list(lb, la, lc));
		main.removeComponent(la);
		assertChildrenEventSent(l, list(lb, la, lc), list(lb, lc));
	}


	private void assertChildrenEventSent(TestingChildListener l, List<Object> expectedOld, List<Layout> expectedNew) {
		assertEquals(expectedOld, l._oldChildren);
		assertEquals(expectedNew, l._newChildren);
	}

	/** Test various collection features. */
	public void testCollection() {

		// creation
		Layout la = newLayout(Layout.HORIZONTAL);
		Layout lb = newLayout(Layout.HORIZONTAL);
		Layout lc = newLayout(Layout.HORIZONTAL);
		ComponentName componentName = ComponentName.newName("PaName");
		SimpleComponent pa = newSimpleComponent(componentName, "", "");

		// start
		assertEquals(0, la.getChildCount());
		la.addComponent(lb);
		assertEquals(1, la.getChildCount());

		try {
			lb.addComponent(1, lc);
			fail("No IndexOutOfBoundsException"
					+ ", when adding in a list at an unused place after the end.");
		} catch (IndexOutOfBoundsException expected) {
			/* expected */
		}

		lb.addComponent(0, lc);
		try {
			la.addComponent(lc);
			fail("No IllegalArgumentException when adding contained component.");
		} catch (IllegalArgumentException expected) {
			/* expected */
		}

		MainLayout ml = newMainLayout();
		ml.addComponent(la);
		ml.setVisible(true);

		assertNotNull(lc.getName());
		assertTrue(LayoutConstants.hasSynthesizedName(lc));

		lc.addComponent(pa);
		assertEquals(lc, pa.getParent());

		assertTrue(lb.removeComponent(lc));
		assertNull(lc.getParent());
		assertEquals(0, lb.getChildCount());
		assertEquals(la, ml.getFirstVisibleChild(null));
		assertNull(ml.getFirstVisibleChild(la));

		la.addComponent(pa);
		assertEquals(la, pa.getParent());
		assertEquals(componentName, pa.getName());
	}


    /**
     * the suite of Tests to execute 
     */
    static public Test suite() {
		return KBSetup.getSingleKBTest(
			ServiceTestSetup.createSetup(TestLayout.class,
				RequestLockFactory.Module.INSTANCE,
				SecurityComponentCache.Module.INSTANCE,
				CommandHandlerFactory.Module.INSTANCE));
    }

}
