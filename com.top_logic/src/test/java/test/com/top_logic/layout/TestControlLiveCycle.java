/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import junit.framework.Test;

import com.top_logic.basic.listener.EventType;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.UpdateQueue;
import com.top_logic.layout.basic.AbstractControlBase;
import com.top_logic.layout.basic.AttachedPropertyListener;
import com.top_logic.layout.basic.UpToDateListener;

/**
 * Test case for observing the {@link Control} live-cycle.
 * 
 * @see AbstractControlBase#ATTACHED_PROPERTY
 * @see AbstractControlBase#UP_TO_DATE_PROPERTY
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestControlLiveCycle extends TestControl {

	class DummyControl extends AbstractControlBase {
		@Override
		public boolean isVisible() {
			return true;
		}

		@Override
		public Object getModel() {
			return null;
		}

		@Override
		protected void internalWrite(DisplayContext context, TagWriter out) throws IOException {
			// No contents.
		}

		@Override
		protected void internalRevalidate(DisplayContext context, UpdateQueue actions) {
			// Noting to update.
		}

		@Override
		protected boolean hasUpdates() {
			return false;
		}
	}

	class PropertiesTester implements AttachedPropertyListener, UpToDateListener {
		private Map<EventType<?, ?, ?>, Object> _values = new HashMap<>();

		public Object getValue(EventType<?, ?, ?> property) {
			return _values.get(property);
		}

		@Override
		public void isUpToDate(AbstractControlBase sender, Boolean oldValue, Boolean newValue) {
			_values.put(AbstractControlBase.UP_TO_DATE_PROPERTY, newValue);
		}

		@Override
		public void handleAttachEvent(AbstractControlBase sender, Boolean oldValue, Boolean newValue) {
			_values.put(AbstractControlBase.ATTACHED_PROPERTY, newValue);
		}
	}

	public void testObserve() {
		AbstractControlBase control = new DummyControl();

		PropertiesTester properties = new PropertiesTester();
		control.addListener(AbstractControlBase.ATTACHED_PROPERTY, properties);
		control.addListener(AbstractControlBase.UP_TO_DATE_PROPERTY, properties);

		assertNull(properties.getValue(AbstractControlBase.ATTACHED_PROPERTY));
		assertNull(properties.getValue(AbstractControlBase.UP_TO_DATE_PROPERTY));

		writeControl(control);

		assertAttached(properties);
		assertUpToDate(properties);

		control.requestRepaint();

		assertAttached(properties);
		assertNotUpToDate(properties);

		writeControl(control);

		assertAttached(properties);
		assertUpToDate(properties);

		control.detach();

		assertNotAttached(properties);
		assertNotUpToDate(properties);

		writeControl(control);

		assertAttached(properties);
		assertUpToDate(properties);

		control.requestRepaint();

		assertAttached(properties);
		assertNotUpToDate(properties);

		control.detach();

		assertNotAttached(properties);
		assertNotUpToDate(properties);
	}

	private void assertAttached(PropertiesTester properties) {
		assertAttached(properties, Boolean.TRUE);
	}

	private void assertNotAttached(PropertiesTester properties) {
		assertAttached(properties, Boolean.FALSE);
	}

	private void assertAttached(PropertiesTester properties, Boolean expected) {
		assertEquals(expected, properties.getValue(AbstractControlBase.ATTACHED_PROPERTY));
	}

	private void assertUpToDate(PropertiesTester properties) {
		assertUpToDate(properties, Boolean.TRUE);
	}

	private void assertNotUpToDate(PropertiesTester properties) {
		assertUpToDate(properties, Boolean.FALSE);
	}

	private void assertUpToDate(PropertiesTester properties, Boolean expected) {
		assertEquals(expected, properties.getValue(AbstractControlBase.UP_TO_DATE_PROPERTY));
	}

	public static Test suite() {
		return suite(TestControlLiveCycle.class);
	}
}
