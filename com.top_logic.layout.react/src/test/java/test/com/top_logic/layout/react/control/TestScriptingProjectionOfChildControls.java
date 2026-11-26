/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.react.control;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;

/**
 * Tests that the {@link ReactControl#scriptingScalarState() headless projection} of a control
 * leaves out the child controls its state holds - they are projected as nodes of their own - and
 * keeps everything else of the state entry holding them.
 */
public class TestScriptingProjectionOfChildControls extends TestCase {

	private static final String ROWS = "rows";

	private static final String ID = "id";

	private static final String TEXT = "text";

	private static final String TRAILING = "trailing";

	private static final String CONTROL = "control";

	private static final String LABEL = "label";

	private static final String CHILD = "child";

	private static final String CHILDREN = "children";

	private static final String TITLE = "title";

	/** The texts of rows holding a child control stay, the control is left out. */
	public void testRowsKeepTheirTexts() {
		TestControl list = new TestControl(context());
		list.put(ROWS, List.of(
			row("r1", "First", Map.of(CONTROL, new TestControl(context()), LABEL, "Open")),
			row("r2", "Second", Map.of(CONTROL, new TestControl(context())))));

		Map<String, Object> state = list.scriptingScalarState();

		assertEquals(List.of(
			Map.of(ID, "r1", TEXT, "First", TRAILING, Map.of(LABEL, "Open")),
			Map.of(ID, "r2", TEXT, "Second")),
			state.get(ROWS));
	}

	/** An entry that is a control, or holds nothing but controls, is left out as a whole. */
	public void testControlsOnlyAreLeftOut() {
		TestControl container = new TestControl(context());
		container.put(CHILD, new TestControl(context()));
		container.put(CHILDREN, List.of(new TestControl(context()), new TestControl(context())));
		container.put(TITLE, "Container");

		Map<String, Object> state = container.scriptingScalarState();

		assertFalse(state.containsKey(CHILD));
		assertFalse(state.containsKey(CHILDREN));
		assertEquals("Container", state.get(TITLE));
	}

	/** A value without any control is projected as it is. */
	public void testValueWithoutControlsIsUnchanged() {
		TestControl control = new TestControl(context());
		List<Map<String, Object>> rows = List.of(Map.of(ID, "r1", TEXT, "First"));
		control.put(ROWS, rows);

		assertSame(rows, control.scriptingScalarState().get(ROWS));
	}

	private static Map<String, Object> row(String id, String text, Map<String, Object> trailing) {
		Map<String, Object> result = new LinkedHashMap<>();
		result.put(ID, id);
		result.put(TEXT, text);
		result.put(TRAILING, new LinkedHashMap<>(trailing));
		return result;
	}

	private static ReactContext context() {
		return new DefaultReactContext("", "test", new SSEUpdateQueue(), new ReactWindowRegistry("test"));
	}

	/**
	 * {@link ReactControl} whose state the test writes.
	 */
	private static class TestControl extends ReactControl {

		TestControl(ReactContext context) {
			super(context, null, "TestControl");
		}

		void put(String key, Object value) {
			putState(key, value);
		}
	}

}
