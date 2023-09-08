/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.form;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Test;

import test.com.top_logic.TLTestSetup;
import test.com.top_logic.basic.BasicTestCase;

import com.top_logic.basic.listener.EventType.Bubble;
import com.top_logic.layout.form.CollapsedListener;
import com.top_logic.layout.form.Collapsible;
import com.top_logic.layout.form.DefaultExpansionModel;

/**
 * Test of {@link DefaultExpansionModel}.
 * 
 * @since 5.7.5
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@SuppressWarnings({ "javadoc" })
public class TestDefaultExpansionModel extends BasicTestCase {

	public void testConstructor() {
		assertTrue(new DefaultExpansionModel(true).isCollapsed());
		assertFalse(new DefaultExpansionModel(false).isCollapsed());
	}

	public void testSimple() {
		Collapsible collapsible = new DefaultExpansionModel(false);

		collapsible.setCollapsed(false);
		assertFalse(collapsible.isCollapsed());

		collapsible.setCollapsed(true);
		assertTrue(collapsible.isCollapsed());
	}

	public void testListener() {
		DefaultExpansionModel expansionModel = new DefaultExpansionModel(false);
		class Listener implements CollapsedListener {

			List<PropertyEvent> _events = new ArrayList<>();

			@Override
			public Bubble handleCollapsed(Collapsible collapsible, Boolean oldValue, Boolean newValue) {
				_events.add(new PropertyEvent(collapsible, Collapsible.COLLAPSED_PROPERTY, !newValue, newValue));
				return Bubble.BUBBLE;
			}
		}

		Listener l = new Listener();
		expansionModel.addListener(Collapsible.COLLAPSED_PROPERTY, l);

		assertFalse(expansionModel.isCollapsed());
		expansionModel.setCollapsed(false);
		assertTrue("No change of collapse state must not sent events.", l._events.isEmpty());
		l._events.clear();

		expansionModel.setCollapsed(true);
		assertEquals(1, l._events.size());
		assertEquals(Boolean.TRUE, l._events.get(0).getNewValue());
		assertEquals(Boolean.FALSE, l._events.get(0).getOldValue());
		l._events.clear();

		expansionModel.setCollapsed(true);
		assertTrue("No change of collapse state must not sent events.", l._events.isEmpty());
		l._events.clear();

		expansionModel.setCollapsed(false);
		assertEquals(1, l._events.size());
		assertEquals(Boolean.FALSE, l._events.get(0).getNewValue());
		assertEquals(Boolean.TRUE, l._events.get(0).getOldValue());
		l._events.clear();

		assertFalse(expansionModel.isCollapsed());
		expansionModel.setCollapsed(false);
		assertTrue("No change of collapse state must not sent events.", l._events.isEmpty());
		l._events.clear();
	}

	/**
	 * a cumulative {@link Test} for all Tests in {@link TestDefaultExpansionModel}.
	 */
	public static Test suite() {
		return TLTestSetup.createTLTestSetup(TestDefaultExpansionModel.class);
	}

}
