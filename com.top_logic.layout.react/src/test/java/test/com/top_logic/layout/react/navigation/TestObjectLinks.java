/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.react.navigation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.layout.AbstractResourceProvider;
import com.top_logic.layout.ResourceProvider;
import com.top_logic.layout.form.model.SimpleSelectFieldModel;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.ForwardingReactContext;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.select.ReactDropdownSelectControl;
import com.top_logic.layout.react.control.table.ReactResourceCellControl;
import com.top_logic.layout.react.navigation.ObjectNavigator;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * Tests that a control offers a value it displays as a link exactly when the
 * {@link ObjectNavigator} of its {@link ReactContext} can display that value, and leads there when
 * the user follows the link.
 */
public class TestObjectLinks extends TestCase {

	/** The value the application displays somewhere. */
	private static final String SHOWN = "shown";

	/** The value it displays nowhere. */
	private static final String UNSHOWN = "unshown";

	private static final String HAS_LINK = "\"hasLink\":true";

	private static final String OPTION_LINK = "\"link\":true";

	private static final String CMD_GOTO = "goto";

	private static final String ARG_OPTION = "option";

	/** The labels the resource cells display, so that nothing else can produce them. */
	private static final ResourceProvider LABELS = new AbstractResourceProvider() {
		@Override
		public String getLabel(Object object) {
			return String.valueOf(object);
		}
	};

	/**
	 * The navigator under test: it displays {@link #SHOWN} and nothing else, and records what it
	 * was asked to display.
	 */
	private static final class Targets implements ObjectNavigator {

		private final List<Object> _shown = new ArrayList<>();

		@Override
		public boolean canShow(Object value) {
			return SHOWN.equals(value);
		}

		@Override
		public void show(ReactContext context, Object value) {
			_shown.add(value);
		}

		List<Object> shown() {
			return _shown;
		}
	}

	private Targets _navigator;

	private ReactContext _context;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		_navigator = new Targets();
		_context = new ForwardingReactContext(new DefaultReactContext("", "test", new SSEUpdateQueue())) {
			@Override
			public ObjectNavigator getObjectNavigator() {
				return _navigator;
			}
		};
	}

	/** A context with nothing to display objects offers no links. */
	public void testNothingLinksWithoutANavigator() {
		ReactContext plain = new DefaultReactContext("", "test", new SSEUpdateQueue());

		assertNull(plain.getObjectNavigator());
		assertFalse(cell(plain, SHOWN, true).stateAsJSON().contains(HAS_LINK));
	}

	/** A value the application displays is a link. */
	public void testADisplayedValueIsALink() {
		assertTrue(cell(_context, SHOWN, true).stateAsJSON().contains(HAS_LINK));
	}

	/** A value it displays nowhere is not. */
	public void testAnUndisplayedValueIsNoLink() {
		assertFalse(cell(_context, UNSHOWN, true).stateAsJSON().contains(HAS_LINK));
	}

	/** A cell that may not link stays plain, however well the value could be displayed. */
	public void testACellThatMayNotLinkStaysPlain() {
		assertFalse(cell(_context, SHOWN, false).stateAsJSON().contains(HAS_LINK));
	}

	/** A cell with a listener of its own links whatever the navigator says. */
	public void testAListenerMakesEvenAnUndisplayedValueALink() {
		ReactResourceCellControl cell = cell(_context, UNSHOWN, false);
		cell.setGotoListener((context, target) -> HandlerResult.DEFAULT_RESULT);

		assertTrue(cell.stateAsJSON().contains(HAS_LINK));
	}

	/** Following the link hands the value to the navigator. */
	public void testFollowingTheLinkDisplaysTheValue() {
		cell(_context, SHOWN, true).executeCommand(CMD_GOTO, Map.of());

		assertEquals(List.of(SHOWN), _navigator.shown());
	}

	/** A listener of the display takes precedence over the navigator. */
	public void testAListenerIsAskedInsteadOfTheNavigator() {
		List<Object> handled = new ArrayList<>();
		ReactResourceCellControl cell = cell(_context, SHOWN, true);
		cell.setGotoListener((context, target) -> {
			handled.add(target);
			return HandlerResult.DEFAULT_RESULT;
		});

		cell.executeCommand(CMD_GOTO, Map.of());

		assertEquals(List.of(SHOWN), handled);
		assertEquals("The listener decides where the value leads.", List.of(), _navigator.shown());
	}

	/** A field that only displays its value offers each displayed object as a link. */
	public void testAReadOnlySelectLinksItsValue() {
		assertTrue(readOnlySelect(SHOWN).stateAsJSON().contains(OPTION_LINK));
	}

	/** A value it displays nowhere stays plain. */
	public void testAReadOnlySelectDoesNotLinkAnUndisplayedValue() {
		assertFalse(readOnlySelect(UNSHOWN).stateAsJSON().contains(OPTION_LINK));
	}

	/** While the field is edited, its chips are the handle for changing the value, not links. */
	public void testAnEditableSelectLinksNothing() {
		SimpleSelectFieldModel model = new SimpleSelectFieldModel(SHOWN, List.of(SHOWN, UNSHOWN), false);

		assertFalse(select(model).stateAsJSON().contains(OPTION_LINK));
	}

	/** Turning the field read-only turns its value into a link. */
	public void testTheValueBecomesALinkWhenEditingEnds() {
		SimpleSelectFieldModel model = new SimpleSelectFieldModel(SHOWN, List.of(SHOWN, UNSHOWN), false);
		ReactDropdownSelectControl field = select(model);

		model.setEditable(false);

		assertTrue(field.stateAsJSON().contains(OPTION_LINK));
	}

	/** Following the link of a displayed option hands its object to the navigator. */
	public void testFollowingAnOptionLinkDisplaysTheOption() {
		ReactDropdownSelectControl field = readOnlySelect(SHOWN);

		field.executeCommand(CMD_GOTO, Map.of(ARG_OPTION, optionId(field)));

		assertEquals(List.of(SHOWN), _navigator.shown());
	}

	/** An option id nothing answers to leads nowhere rather than failing. */
	public void testAnUnknownOptionLeadsNowhere() {
		readOnlySelect(SHOWN).executeCommand(CMD_GOTO, Map.of(ARG_OPTION, "no-such-option"));

		assertEquals(List.of(), _navigator.shown());
	}

	private ReactResourceCellControl cell(ReactContext context, Object value, boolean useLink) {
		return new ReactResourceCellControl(context, value, LABELS, false, true, useLink);
	}

	private ReactDropdownSelectControl readOnlySelect(Object value) {
		SimpleSelectFieldModel model = new SimpleSelectFieldModel(value, List.of(SHOWN, UNSHOWN), false);
		model.setEditable(false);
		return select(model);
	}

	private ReactDropdownSelectControl select(SimpleSelectFieldModel model) {
		return new ReactDropdownSelectControl(_context, model, String::valueOf, null, false);
	}

	/**
	 * The id the given field addresses its single displayed option by, as the client reads it from
	 * the field's state.
	 */
	private String optionId(ReactDropdownSelectControl field) {
		String state = field.stateAsJSON();
		String marker = "\"value\":\"";
		int start = state.indexOf(marker, state.indexOf("\"value\":[")) + marker.length();
		return state.substring(start, state.indexOf('"', start));
	}

	/**
	 * Test suite requiring the session resources a select field labels its empty selection with.
	 */
	public static Test suite() {
		return ModuleTestSetup.setupModule(
			ServiceTestSetup.createSetup(TestObjectLinks.class, ThreadContextManager.Module.INSTANCE));
	}

}
