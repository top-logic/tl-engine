/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.react.scripting;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.layout.react.control.InteractionWithoutSession;

import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.basic.util.ComputationEx2;
import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.layout.form.model.SimpleSelectFieldModel;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ModelKeyArguments;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.ScriptingModelKey;
import com.top_logic.layout.react.control.common.ReactTextControl;
import com.top_logic.layout.react.control.form.FieldValueArguments;
import com.top_logic.layout.react.control.form.ReactFormFieldControl;
import com.top_logic.layout.react.control.identity.ProjectionMode;
import com.top_logic.layout.react.control.kanban.ReactKanbanBoardControl;
import com.top_logic.layout.react.control.layout.ReactGridControl;
import com.top_logic.layout.react.control.layout.ReactLayoutControl;
import com.top_logic.layout.react.control.layout.ReactLayoutControl.Item;
import com.top_logic.layout.react.control.layout.ReactStackControl.StackGap;
import com.top_logic.layout.react.control.select.ReactDropdownSelectControl;
import com.top_logic.layout.react.control.select.SelectDisplay;
import com.top_logic.layout.react.scripting.AssertCommand;
import com.top_logic.layout.react.scripting.ScriptingTreeProjector;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.state.DropdownSelectState;
import com.top_logic.layout.react.state.FieldState;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.scripting.recorder.ref.ModelResolver;
import com.top_logic.table.SelectionMode;

/**
 * Tests that the headless projection of a control identifies the objects it displays by their
 * business identity rather than by ids allocated in the session, so an assertion recorded on it
 * holds in another session.
 *
 * <p>
 * Each test builds the same control twice, each in a context of its own - two sessions allocating
 * their ids independently, the second one after allocating some ids of its own -, and compares the
 * projections the way a replayed assertion does ({@link AssertCommand#mismatchingKeys(Map, Map)}).
 * </p>
 */
public class TestSessionIndependentProjection extends TestCase {

	private static final List<String> OPTIONS = List.of("percent", "scale", "angle");

	/**
	 * Runs the test within an interaction of a session, as the naming of an object - which asks
	 * the session for script variables naming it - happens in one.
	 */
	@Override
	protected void runTest() throws Throwable {
		InteractionWithoutSession.inSession().runWithContext((ComputationEx2<Void, Throwable, RuntimeException>) () -> {
			super.runTest();
			return null;
		});
	}

	/**
	 * The value and the options of a dropdown are projected by the keys of the options, and the
	 * control offers the identities translating its option ids into these keys.
	 */
	public void testDropdownProjectsKeys() {
		Dropdown first = dropdown(firstSession(), "scale");
		Dropdown second = dropdown(secondSession(), "scale");
		first.loadOptions();
		second.loadOptions();

		Map<String, Object> state = ScriptingTreeProjector.nodeState(first);
		Map<String, Object> value = single(state.get(FieldState.VALUE__PROP));
		assertNotNull("The value is projected with the key of its option: " + value, value.get(ModelKeyArguments.KEY));
		assertFalse("A keyed option carries no session id: " + value,
			value.containsKey(DropdownSelectState.Option.VALUE__PROP));
		for (Object option : (List<?>) state.get(DropdownSelectState.OPTIONS__PROP)) {
			assertFalse("A keyed option carries no session id: " + option,
				((Map<?, ?>) option).containsKey(DropdownSelectState.Option.VALUE__PROP));
		}

		assertSameProjection(first, second, FieldState.VALUE__PROP, DropdownSelectState.OPTIONS__PROP);
		assertNotNull(first.objectIdentities());
		assertTrue(first.scriptingCommands().contains(ReactFormFieldControl.CMD_VALUE_CHANGED));
	}

	/** An agent selects an option of a dropdown by the id it observes. */
	public void testDropdownSelectsByObservedId() {
		Dropdown dropdown = dropdown(secondSession(), "percent");
		dropdown.loadOptions();
		@SuppressWarnings("unchecked")
		List<Map<String, Object>> options = (List<Map<String, Object>>) ScriptingTreeProjector
			.nodeState(dropdown, ProjectionMode.AGENT).get(DropdownSelectState.OPTIONS__PROP);
		Object observed = options.get(2).get(DropdownSelectState.Option.VALUE__PROP);
		assertNotNull("The agent observes the option by its id: " + options.get(2), observed);

		new InteractionWithoutSession().runWithContext(() -> dropdown.executeCommand(
			ReactFormFieldControl.CMD_VALUE_CHANGED, Map.of(FieldValueArguments.VALUE, List.of(observed))));
		assertEquals(List.of("angle"), dropdown.model().getValue());
	}

	/**
	 * The items and the selection of a selectable container are projected by the keys of their
	 * objects, and the container offers the identities translating its item keys into these keys.
	 */
	public void testSelectableContainerProjectsKeys() {
		ReactLayoutControl first = grid(firstSession());
		ReactLayoutControl second = grid(secondSession());

		Map<String, Object> state = ScriptingTreeProjector.nodeState(first);
		assertEquals(List.of(Map.of(ReactLayoutControl.ITEM_KEY, key("percent")),
			Map.of(ReactLayoutControl.ITEM_KEY, key("scale")), Map.of(ReactLayoutControl.ITEM_KEY, key("angle"))),
			state.get(ReactLayoutControl.ITEM_KEYS));
		assertEquals(List.of(key("scale")), state.get(ReactLayoutControl.SELECTED));
		assertSameProjection(first, second, ReactLayoutControl.ITEM_KEYS, ReactLayoutControl.SELECTED);
		assertNotNull(first.objectIdentities());
		assertTrue(first.scriptingCommands().contains(ReactLayoutControl.CMD_SELECT_ITEM));
	}

	/**
	 * The cards and the selection of a board are projected by the keys of the cards' objects, and
	 * the board offers the identities translating its card keys into these keys.
	 */
	public void testBoardProjectsKeys() {
		ReactKanbanBoardControl first = board(firstSession());
		ReactKanbanBoardControl second = board(secondSession());

		Map<String, Object> state = ScriptingTreeProjector.nodeState(first);
		assertEquals(List.of(key("angle")), state.get(ReactKanbanBoardControl.SELECTED));
		Map<String, Object> column = single(state.get(ReactKanbanBoardControl.COLUMNS));
		assertEquals(List.of(Map.of(ReactKanbanBoardControl.CARD_KEY, key("percent")),
			Map.of(ReactKanbanBoardControl.CARD_KEY, key("scale")),
			Map.of(ReactKanbanBoardControl.CARD_KEY, key("angle"))), column.get(ReactKanbanBoardControl.COLUMN_CARDS));
		assertEquals(key("todo"), column.get(ReactKanbanBoardControl.COLUMN_KEY));
		assertSameProjection(first, second, ReactKanbanBoardControl.SELECTED, ReactKanbanBoardControl.COLUMNS);
		assertNotNull(first.objectIdentities());
		assertTrue(first.scriptingCommands().contains(ReactKanbanBoardControl.CMD_SELECT_CARD));
	}

	/**
	 * Asserts that the given entries of the projections of the given controls are equal, as an
	 * assertion recorded on the first one and replayed on the second one compares them.
	 */
	private static void assertSameProjection(ReactControl recorded, ReactControl replayed, String... keys) {
		Map<String, Object> expected = ScriptingTreeProjector.nodeState(recorded);
		Map<String, Object> subset = new LinkedHashMap<>();
		for (String key : keys) {
			assertTrue("Missing '" + key + "' in " + expected, expected.containsKey(key));
			subset.put(key, expected.get(key));
		}
		assertEquals(List.of(), AssertCommand.mismatchingKeys(subset, ScriptingTreeProjector.nodeState(replayed)));
	}

	@SuppressWarnings("unchecked")
	private static Map<String, Object> single(Object list) {
		List<?> entries = (List<?>) list;
		assertEquals(1, entries.size());
		return (Map<String, Object>) entries.get(0);
	}

	private static Object key(String object) {
		return ScriptingModelKey.toKey(null, object);
	}

	private static ReactContext firstSession() {
		return new DefaultReactContext("", "test", new SSEUpdateQueue(), new ReactWindowRegistry("test"));
	}

	/** A context that has allocated ids before, so its ids differ from those of the first one. */
	private static ReactContext secondSession() {
		ReactContext result = firstSession();
		for (int n = 0; n < 17; n++) {
			result.allocateId();
		}
		return result;
	}

	private static Dropdown dropdown(ReactContext context, String value) {
		return new Dropdown(context, new SimpleSelectFieldModel(new String(value), OPTIONS, false));
	}

	private static ReactLayoutControl grid(ReactContext context) {
		ReactGridControl result = new ReactGridControl(context, "16rem", null, StackGap.DEFAULT, List.of());
		result.setItems(OPTIONS.stream().map(option -> new Item(option, new ReactTextControl(context, option))).toList());
		result.setSelection(Set.of("scale"));
		return result;
	}

	private static ReactKanbanBoardControl board(ReactContext context) {
		ReactKanbanBoardControl result = new ReactKanbanBoardControl(context);
		result.setSelectionMode(SelectionMode.MULTI);
		result.setColumns(List.of(new ReactKanbanBoardControl.Column("todo", "To do",
			OPTIONS.stream().map(option -> new ReactKanbanBoardControl.Card(option,
				new ReactTextControl(context, option))).toList())));
		result.setSelection(Set.of("angle"));
		return result;
	}

	/** A {@link ReactDropdownSelectControl} over string options. */
	private static final class Dropdown extends ReactDropdownSelectControl {

		private final SimpleSelectFieldModel _model;

		Dropdown(ReactContext context, SimpleSelectFieldModel model) {
			super(context, model, String::valueOf, null, false, SelectDisplay.DROPDOWN);
			_model = model;
		}

		SimpleSelectFieldModel model() {
			return _model;
		}

		void loadOptions() {
			executeClientCommand("loadOptions", Map.of());
		}
	}

	/**
	 * The test suite, started with the resource bundles the controls need and the
	 * {@link ModelResolver} naming the displayed objects.
	 */
	public static Test suite() {
		return ModuleTestSetup.setupModule(
			ServiceTestSetup.createSetup(TestSessionIndependentProjection.class, TypeIndex.Module.INSTANCE,
				ThreadContextManager.Module.INSTANCE, ResourcesModule.Module.INSTANCE, ModelResolver.Module.INSTANCE));
	}

}
