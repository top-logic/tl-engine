/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.react.scripting;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;
import test.com.top_logic.layout.react.control.InteractionWithoutSession;

import com.top_logic.basic.json.JSON;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.basic.util.ComputationEx2;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.gui.ThemeFactory;
import com.top_logic.layout.form.model.AbstractFieldModel;
import com.top_logic.layout.form.model.SimpleSelectFieldModel;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactCommand;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.common.ReactTextControl;
import com.top_logic.layout.react.control.form.FieldValueArguments;
import com.top_logic.layout.react.control.form.ReactFormFieldControl;
import com.top_logic.layout.react.control.form.ReactTextInputControl;
import com.top_logic.layout.react.control.layout.ReactFormFieldChromeControl;
import com.top_logic.layout.react.control.layout.ReactStackControl;
import com.top_logic.layout.react.control.select.ReactDropdownSelectControl;
import com.top_logic.layout.react.control.select.SelectDisplay;
import com.top_logic.layout.react.control.sidebar.NavigationItem;
import com.top_logic.layout.react.control.sidebar.ReactSidebarControl;
import com.top_logic.layout.react.control.sidebar.SelectItemArguments;
import com.top_logic.layout.react.control.sidebar.SidebarItem;
import com.top_logic.layout.react.control.table.SelectRowArguments;
import com.top_logic.layout.react.control.table.TableViewControl;
import com.top_logic.layout.react.control.tabbar.ReactTabBarControl;
import com.top_logic.layout.react.control.tabbar.SelectTabArguments;
import com.top_logic.layout.react.control.tabbar.TabDefinition;
import com.top_logic.layout.react.control.tree.ExpandNodeArguments;
import com.top_logic.layout.react.control.tree.ReactTreeControl;
import com.top_logic.layout.react.control.tree.SelectNodeArguments;
import com.top_logic.layout.react.scripting.ScriptRecorder;
import com.top_logic.layout.react.scripting.ScriptingSession;
import com.top_logic.layout.react.scripting.ScriptingTreeProjector;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.state.DropdownSelectState;
import com.top_logic.layout.react.state.FieldState;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.scripting.recorder.ref.ModelResolver;
import com.top_logic.layout.tree.model.AbstractMutableTLTreeModel;
import com.top_logic.layout.tree.model.DefaultTreeUINodeModel;
import com.top_logic.layout.tree.model.DefaultTreeUINodeModel.DefaultTreeUINode;
import com.top_logic.layout.tree.model.TreeBuilder;
import com.top_logic.mig.html.DefaultMultiSelectionModel;
import com.top_logic.mig.html.SelectionModelOwner;
import com.top_logic.table.Column;
import com.top_logic.table.Selection;
import com.top_logic.table.SelectionMode;
import com.top_logic.table.SortSpec;
import com.top_logic.table.TableViewState;
import com.top_logic.table.impl.DefaultColumn;
import com.top_logic.table.impl.DefaultTableView;
import com.top_logic.table.impl.ListRowSource;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * Tests that a script recorded in one session replays in a fresh one.
 *
 * <p>
 * A {@link Screen} is built once per session, each in a context of its own, so the ids the client
 * addresses controls, rows, nodes and options by differ between the sessions. The gestures of a
 * user - navigation, a tab switch, a row click, a multi-selection with {@code Ctrl} and
 * {@code Shift}, expanding and selecting a tree node, choosing dropdown options, typing into a field
 * - are dispatched in the first session the way the browser dispatches them, through
 * {@link ScriptRecorder#capture(ReactControl, ReactControl, String, Map)}, followed by an
 * assertion. The recorded steps replay in the second session, which ends up with the same
 * projection as the first one.
 * </p>
 */
public class TestCrossSessionReplay extends TestCase {

	private static final List<String> ROWS = List.of("a", "b", "c", "d", "e");

	private static final List<String> COLORS = List.of("red", "green", "blue");

	private static final String ROOT = "root";

	private static final String NODE_A = "nodeA";

	private static final String NODE_A1 = "nodeA1";

	private static final String NODE_B = "nodeB";

	private static final Map<Object, List<Object>> CHILDREN =
		Map.of(ROOT, List.of(NODE_A, NODE_B), NODE_A, List.of(NODE_A1));

	private static final String ITEM_HOME = "home";

	private static final String ITEM_WORK = "work";

	private static final String TAB_TABLE = "table";

	private static final String TAB_TREE = "tree";

	private static final String TAB_FORM = "form";

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
	 * The recorded gestures replay in a fresh session with the same outcome, and after each step
	 * the fresh session shows exactly what the recording one showed after the gestures recorded as
	 * that step - the whole projection, so no part of it depends on the session.
	 */
	public void testRecordingReplaysInAFreshSession() {
		Screen recording = new Screen(context(0), ROWS);
		List<ReactCommand> steps = record(recording);
		assertEquals(steps.size(), recording._observations.size());

		Screen replay = new Screen(context(1000), ROWS);
		for (int n = 0; n < steps.size(); n++) {
			ReactCommand step = steps.get(n);
			HandlerResult result = replay.session().replay(step);
			assertTrue("Step '" + step.getName() + "' on '" + step.getAddress() + "' failed: "
				+ result.getEncodedErrors(), result.isSuccess());
			replay.render();
			assertEquals("Projection after step " + n + " '" + step.getName() + "'.",
				recording._observations.get(n), replay.session().observeJson());
		}

		assertEquals(Set.of("b", "c", "d"), replay._table.getSelectedKeys());
		assertEquals(Set.of(NODE_A1), replay.treeSelection());
		assertEquals(List.of("red", "blue"), replay._colors.getValue());
		assertEquals("hello", replay._name.getValue());
		assertEquals("Both sessions show the same.", recording.session().observeJson(),
			replay.session().observeJson());
	}

	/**
	 * A recorded step whose key no longer resolves in the replaying session fails with its
	 * message, as does a step whose address no longer resolves.
	 */
	public void testDriftFails() {
		List<ReactCommand> steps = record(new Screen(context(0), ROWS));

		Screen withoutRowB = new Screen(context(1000), List.of("a", "c", "d", "e"));
		HandlerResult failure = null;
		for (ReactCommand step : steps) {
			HandlerResult result = withoutRowB.session().replay(step);
			if (!result.isSuccess()) {
				failure = result;
				assertEquals("The row click by key fails first.", TableViewControl.CMD_SELECT_BY_KEY, step.getName());
				break;
			}
		}
		assertNotNull("A recorded row key naming a row the session does not show must fail.", failure);

		Screen other = new Screen(context(2000), ROWS);
		ReactCommand tabSwitch = steps.get(1);
		tabSwitch.setAddress(tabSwitch.getAddress() + "/missing");
		try {
			other.session().replay(tabSwitch);
			fail("A recorded address that no longer resolves must fail.");
		} catch (IllegalArgumentException expected) {
			assertTrue(expected.getMessage(), expected.getMessage().contains("missing"));
		}
	}

	/**
	 * Dispatches the gestures of the scenario in the given screen as the browser does, and returns
	 * the steps recorded meanwhile, ending with an assertion on the dropdown.
	 */
	private static List<ReactCommand> record(Screen screen) {
		ScriptRecorder recorder = new ScriptRecorder();
		recorder.start();

		screen.dispatch(recorder, screen._sidebar, ReactSidebarControl.SELECT_ITEM_COMMAND,
			Map.of(SelectItemArguments.ITEM_ID, ITEM_WORK));
		screen.dispatch(recorder, screen.tabBar(), ReactTabBarControl.SELECT_TAB_COMMAND,
			Map.of(SelectTabArguments.TAB_ID, TAB_TABLE));
		screen.dispatch(recorder, screen._table, TableViewControl.CMD_SELECT, click(1, false, false));
		screen.dispatch(recorder, screen._table, TableViewControl.CMD_SELECT, click(3, true, false));
		screen.dispatch(recorder, screen._table, TableViewControl.CMD_SELECT, click(2, false, true));

		screen.dispatch(recorder, screen.tabBar(), ReactTabBarControl.SELECT_TAB_COMMAND,
			Map.of(SelectTabArguments.TAB_ID, TAB_TREE));
		screen.dispatch(recorder, screen._tree, ReactTreeControl.EXPAND_COMMAND,
			Map.of(ExpandNodeArguments.NODE_ID, screen.nodeId(NODE_A)));
		Map<String, Object> select = new LinkedHashMap<>();
		select.put(SelectNodeArguments.NODE_ID, screen.nodeId(NODE_A1));
		select.put(SelectNodeArguments.CTRL_KEY, Boolean.FALSE);
		select.put(SelectNodeArguments.SHIFT_KEY, Boolean.FALSE);
		screen.dispatch(recorder, screen._tree, ReactTreeControl.SELECT_COMMAND, select);

		screen.dispatch(recorder, screen.tabBar(), ReactTabBarControl.SELECT_TAB_COMMAND,
			Map.of(SelectTabArguments.TAB_ID, TAB_FORM));
		screen.dispatch(recorder, screen._colorInput, ReactDropdownSelectControl.CMD_LOAD_OPTIONS, Map.of());
		screen.dispatch(recorder, screen._colorInput, ReactFormFieldControl.CMD_VALUE_CHANGED,
			Map.of(FieldValueArguments.VALUE, List.of(screen.optionId("red"), screen.optionId("blue"))));
		screen.dispatch(recorder, screen._nameInput, ReactFormFieldControl.CMD_VALUE_CHANGED,
			Map.of(FieldValueArguments.VALUE, "hel"));
		screen.dispatch(recorder, screen._nameInput, ReactFormFieldControl.CMD_VALUE_CHANGED,
			Map.of(FieldValueArguments.VALUE, "hello"));

		Map<String, Object> state = ScriptingTreeProjector.nodeState(screen._colorInput);
		recorder.recordAssertion(screen.session().addressOf(screen._colorInput),
			Map.of(FieldState.VALUE__PROP, state.get(FieldState.VALUE__PROP)));
		screen.observe(recorder);
		recorder.stop();

		List<ReactCommand> steps = recorder.steps();
		for (ReactCommand step : steps) {
			assertNotNull("Every step is addressed: " + step.getName(), step.getAddress());
		}
		return steps;
	}

	private static Map<String, Object> click(int rowIndex, boolean ctrl, boolean shift) {
		Map<String, Object> result = new LinkedHashMap<>();
		result.put(SelectRowArguments.ROW_INDEX, Integer.valueOf(rowIndex));
		result.put(SelectRowArguments.CTRL_KEY, Boolean.valueOf(ctrl));
		result.put(SelectRowArguments.SHIFT_KEY, Boolean.valueOf(shift));
		return result;
	}

	/** A context of a session that has allocated the given number of ids before. */
	private static ReactContext context(int allocated) {
		ReactContext result = new DefaultReactContext("", "test", new SSEUpdateQueue(), new ReactWindowRegistry("test"));
		for (int n = 0; n < allocated; n++) {
			result.allocateId();
		}
		return result;
	}

	/**
	 * The screen of a session: a sidebar whose {@link #ITEM_WORK} page holds a tab bar with a table,
	 * a tree and a form of a dropdown and a text field.
	 */
	private static final class Screen {

		final ReactContext _context;

		final ReactSidebarControl _sidebar;

		final TableViewControl<String> _table;

		final ReactTreeControl _tree;

		final DefaultTreeUINodeModel _treeModel;

		final DefaultMultiSelectionModel<Object> _treeSelection =
			new DefaultMultiSelectionModel<>(SelectionModelOwner.NO_OWNER);

		final SimpleSelectFieldModel _colors = new SimpleSelectFieldModel(null, COLORS, true);

		final AbstractFieldModel _name = new AbstractFieldModel("") {
			// A plain text field.
		};

		final ReactDropdownSelectControl _colorInput;

		final ReactTextInputControl _nameInput;

		final ReactTabBarControl _tabs;

		/** What the screen showed after each recorded step, see {@link #observe(ScriptRecorder)}. */
		final List<String> _observations = new ArrayList<>();

		Screen(ReactContext context, List<String> rows) {
			_context = context;
			_table = table(context, rows);
			_treeModel = new DefaultTreeUINodeModel(builder(), ROOT);
			_treeModel.setRootVisible(false);
			_tree = new ReactTreeControl(context, _treeModel, _treeSelection,
				(ctx, object) -> new ReactTextControl(ctx, String.valueOf(object)));
			_tree.setSelectionMode(SelectionMode.MULTI);
			_colorInput = new ReactDropdownSelectControl(context, _colors, String::valueOf, null, false,
				SelectDisplay.DROPDOWN);
			_nameInput = new ReactTextInputControl(context, _name);
			ReactControl form = new ReactStackControl(context, List.of(
				new ReactFormFieldChromeControl(context, "Colors", _colorInput),
				new ReactFormFieldChromeControl(context, "Name", _nameInput)));

			_tabs = new ReactTabBarControl(context, null, List.of(
				new TabDefinition(TAB_TABLE, "Table", () -> _table, null),
				new TabDefinition(TAB_TREE, "Tree", () -> _tree, null),
				new TabDefinition(TAB_FORM, "Form", () -> form, null)));
			List<SidebarItem> items = List.of(
				new NavigationItem(ITEM_HOME, "Home", null, () -> new ReactTextControl(context, "Welcome")),
				new NavigationItem(ITEM_WORK, "Work", null, () -> _tabs));
			_sidebar = new ReactSidebarControl(context, items, ITEM_HOME, false, null, null);
			_sidebar.attach();
			render();
		}

		ScriptingSession session() {
			return ScriptingSession.forRoot(_sidebar);
		}

		ReactTabBarControl tabBar() {
			return _tabs;
		}

		/** Dispatches a client command as the browser does, recording it first. */
		void dispatch(ScriptRecorder recorder, ReactControl target, String command, Map<String, Object> arguments) {
			recorder.capture(_sidebar, target, command, arguments);
			HandlerResult result = target.executeClientCommand(command, arguments);
			assertTrue("Gesture '" + command + "' failed: " + result.getEncodedErrors(), result.isSuccess());
			render();
			observe(recorder);
		}

		/**
		 * Remembers what the screen shows now as the outcome of the last recorded step, replacing
		 * what was remembered for it when further gestures were recorded as the same step.
		 */
		void observe(ScriptRecorder recorder) {
			int step = recorder.steps().size() - 1;
			String observation = session().observeJson();
			if (step < _observations.size()) {
				_observations.set(step, observation);
			} else {
				_observations.add(observation);
			}
		}

		/** Renders the screen, as the browser displays it after a command. */
		void render() {
			try {
				_sidebar.write(new TagWriter());
			} catch (IOException ex) {
				throw new AssertionError(ex);
			}
		}

		Set<Object> treeSelection() {
			Set<Object> result = new HashSet<>();
			for (Object node : _treeSelection.getSelection()) {
				result.add(((DefaultTreeUINode) node).getBusinessObject());
			}
			return result;
		}

		/** The id the client knows the node of the given object by. */
		@SuppressWarnings("unchecked")
		String nodeId(Object businessObject) {
			String json = _tree.stateAsJSON();
			Map<String, Object> state = parse(json);
			List<Map<String, Object>> nodes = (List<Map<String, Object>>) state.get(ReactTreeControl.NODES);
			List<Object> displayed = new ArrayList<>();
			collect(displayed, _treeModel.getRoot());
			return (String) nodes.get(displayed.indexOf(businessObject)).get(ReactTreeControl.NODE_ID);
		}

		private void collect(List<Object> result, DefaultTreeUINode parent) {
			for (DefaultTreeUINode child : parent.getChildren()) {
				result.add(child.getBusinessObject());
				if (_treeModel.isExpanded(child)) {
					collect(result, child);
				}
			}
		}

		/** The id the client knows the option with the given label by. */
		@SuppressWarnings("unchecked")
		String optionId(String label) {
			Map<String, Object> state = parse(_colorInput.stateAsJSON());
			for (Map<String, Object> option : (List<Map<String, Object>>) state.get(DropdownSelectState.OPTIONS__PROP)) {
				if (label.equals(option.get(DropdownSelectState.Option.LABEL__PROP))) {
					return (String) option.get(DropdownSelectState.Option.VALUE__PROP);
				}
			}
			throw new AssertionError("No option '" + label + "'.");
		}

		@SuppressWarnings("unchecked")
		private static Map<String, Object> parse(String json) {
			try {
				return (Map<String, Object>) JSON.fromString(json);
			} catch (JSON.ParseException ex) {
				throw new AssertionError(ex);
			}
		}

		private static TableViewControl<String> table(ReactContext context, List<String> rows) {
			List<Column<String, ?>> columns =
				List.<Column<String, ?>> of(DefaultColumn.<String, String> builder("name", row -> row).build());
			TableViewState state = DefaultTableView.initialState(columns, SortSpec.NONE, List.of());
			state.setSelection(Selection.none(SelectionMode.MULTI));
			return new TableViewControl<>(context,
				new DefaultTableView<>(columns, new ListRowSource<>(rows, columns), state), false);
		}

		private static TreeBuilder<DefaultTreeUINode> builder() {
			return new TreeBuilder<>() {
				@Override
				public DefaultTreeUINode createNode(AbstractMutableTLTreeModel<DefaultTreeUINode> model,
						DefaultTreeUINode parent, Object userObject) {
					return new DefaultTreeUINode(model, parent, userObject);
				}

				@Override
				public List<DefaultTreeUINode> createChildList(DefaultTreeUINode node) {
					List<DefaultTreeUINode> children = new ArrayList<>();
					for (Object child : CHILDREN.getOrDefault(node.getBusinessObject(), List.of())) {
						children.add(createNode(node.getModel(), node, child));
					}
					return children;
				}

				@Override
				public boolean isFinite() {
					return true;
				}
			};
		}
	}

	/**
	 * The test suite, run on a knowledge base - a window observes the model events of one -, with
	 * the {@link ModelResolver} naming the displayed objects.
	 */
	public static Test suite() {
		return KBSetup.getSingleKBTest(TestCrossSessionReplay.class,
			ServiceTestSetup.createStarterFactoryForModules(ThreadContextManager.Module.INSTANCE,
				ThemeFactory.Module.INSTANCE, ModelResolver.Module.INSTANCE));
	}

}
