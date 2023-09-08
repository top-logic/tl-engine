/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import junit.framework.Test;
import junit.framework.TestCase;

import com.top_logic.basic.col.Filter;
import com.top_logic.basic.listener.EventType;
import com.top_logic.basic.listener.EventType.Bubble;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.control.AbstractSelectControl;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.SelectField;

/**
 * {@link TestCase} for {@link AbstractSelectControl}
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
@SuppressWarnings("javadoc")
public class TestAbstractSelectControl extends TestControl {

	public void testHandleOptionsEvent() {
		SelectField selectField = FormFactory.newSelectField("foo", Collections.singleton("foo"));
		TestSelectControl selectControl = new TestSelectControl(selectField, Collections.emptyMap());
		writeControl(selectControl);
		selectField.setOptions(Collections.singletonList("bar"));
		assertEventHandling(selectControl, SelectField.OPTIONS_PROPERTY);
	}

	public void testHandleFixedOptionsEvent() {
		String fixedOption = "bar";
		SelectField selectField = FormFactory.newSelectField("foo", Arrays.asList("foo", fixedOption));
		TestSelectControl selectControl = new TestSelectControl(selectField, Collections.emptyMap());
		writeControl(selectControl);
		selectField.setFixedOptions(Collections.singleton(fixedOption));
		assertEventHandling(selectControl, SelectField.FIXED_FILTER_PROPERTY);
	}

	private void assertEventHandling(TestSelectControl selectControl,
			EventType<?, ?, ?> eventType) {
		assertTrue("No event was processed!", selectControl.getEventTypeProcessed().isPresent());
		assertEquals(eventType, selectControl.getEventTypeProcessed().get());
	}

	public static Test suite() {
		return TestControl.suite(TestAbstractSelectControl.class);
	}

	private static class TestSelectControl extends AbstractSelectControl {

		private Optional<EventType<?, ?, ?>> eventTypeProcessed = Optional.empty();

		protected TestSelectControl(FormField model, Map commandsByName) {
			super(model, commandsByName);
		}

		@Override
		protected void writeEditable(DisplayContext context, TagWriter out) throws IOException {
			// Do nothing
		}

		@Override
		protected void writeImmutable(DisplayContext context, TagWriter out) throws IOException {
			// Do nothing
		}

		@Override
		protected void internalHandleDisabledEvent(FormMember sender, Boolean oldValue, Boolean newValue) {
			// Do nothing
		}

		@Override
		protected void internalHandleValueChanged(FormField field, Object oldValue, Object newValue) {
			// Do nothing
		}

		@Override
		public Bubble handleFixedOptionsChanged(SelectField sender, Filter oldValue, Filter newValue) {
			eventTypeProcessed = Optional.of(SelectField.FIXED_FILTER_PROPERTY);
			return super.handleFixedOptionsChanged(sender, oldValue, newValue);
		}

		@Override
		public Bubble handleOptionsChanged(SelectField sender) {
			eventTypeProcessed = Optional.of(SelectField.OPTIONS_PROPERTY);
			return super.handleOptionsChanged(sender);
		}

		Optional<EventType<?, ?, ?>> getEventTypeProcessed() {
			return eventTypeProcessed;
		}

	}

}
