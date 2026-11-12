/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.react.scripting;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.layout.react.control.CommandFailures;
import test.com.top_logic.layout.react.control.InteractionWithoutSession;

import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.basic.util.ComputationEx2;
import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.layout.form.model.AbstractFieldModel;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactCommand;
import com.top_logic.layout.react.control.ReactCompositeControl;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.button.ReactButtonControl;
import com.top_logic.layout.react.control.common.ReactTextControl;
import com.top_logic.layout.react.control.form.FieldValueArguments;
import com.top_logic.layout.react.control.form.ReactFormFieldControl;
import com.top_logic.layout.react.control.form.ReactTextInputControl;
import com.top_logic.layout.react.control.layout.ReactFormGroupControl;
import com.top_logic.layout.react.control.layout.ReactFormGroupControl.GroupBorder;
import com.top_logic.layout.react.control.overlay.ReactMenuControl;
import com.top_logic.layout.react.control.overlay.ReactMenuControl.MenuEntry;
import com.top_logic.layout.react.control.table.CellControlFactory;
import com.top_logic.layout.react.control.table.ScrollArguments;
import com.top_logic.layout.react.control.table.TableViewControl;
import com.top_logic.layout.react.control.tree.ReactTreeControl;
import com.top_logic.layout.react.scripting.ScriptRecorder;
import com.top_logic.layout.react.scripting.ScriptingAction;
import com.top_logic.layout.react.scripting.ScriptingNodeView;
import com.top_logic.layout.react.scripting.ScriptingSession;
import com.top_logic.layout.react.scripting.ScriptingTreeProjector;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.scripting.recorder.ref.ModelResolver;
import com.top_logic.layout.tree.model.AbstractMutableTLTreeModel;
import com.top_logic.layout.tree.model.DefaultTreeUINodeModel;
import com.top_logic.layout.tree.model.DefaultTreeUINodeModel.DefaultTreeUINode;
import com.top_logic.layout.tree.model.TreeBuilder;
import com.top_logic.mig.html.DefaultSingleSelectionModel;
import com.top_logic.mig.html.SelectionModelOwner;
import com.top_logic.table.CellContent;
import com.top_logic.table.Column;
import com.top_logic.table.Selection;
import com.top_logic.table.SelectionMode;
import com.top_logic.table.SortSpec;
import com.top_logic.table.TableViewState;
import com.top_logic.table.impl.DefaultColumn;
import com.top_logic.table.impl.DefaultTableView;
import com.top_logic.table.impl.ListRowSource;

/**
 * Tests that the headless observation contains what a user sees and can operate, no more: actions
 * only where the command is executable, no invisible controls, and the controls of table rows and
 * tree nodes only where they can be operated, addressed under their row or node.
 */
public class TestObservationShowsWhatUserSees extends TestCase {

	private static final List<String> ROWS = List.of("a", "b", "c");

	private static final String NAME = "name";

	private static final String TEXT = "text";

	private static final String OPEN = "open";

	private static final String OPEN_LABEL = "Open";

	private static final String TREE_ROOT = "root";

	private ReactContext _context;

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

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_context = context();
	}

	/** A disabled button offers no click, and a headless click on it is refused. */
	public void testDisabledButtonHasNoClick() {
		List<String> clicks = new ArrayList<>();
		ReactButtonControl button = new ReactButtonControl(_context, "Go", context -> clicks.add("Go"));
		ScriptingSession session = ScriptingSession.forRoot(panel(button));

		assertEquals(List.of(ReactButtonControl.CMD_CLICK), commands(node(session, "/panel/button[Go]")));

		button.setDisabled(true);
		assertEquals(List.of(), commands(node(session, "/panel/button[Go]")));
		assertNotNull(CommandFailures.outcome(() -> session.act("/panel/button[Go]", ReactButtonControl.CMD_CLICK,
			Map.of())));
		assertEquals(List.of(), clicks);
	}

	/**
	 * A read-only field offers no input; a headless input is refused, while the input of a lagging
	 * client is dropped silently.
	 */
	public void testReadOnlyFieldHasNoInput() {
		AbstractFieldModel model = new AbstractFieldModel("x");
		ReactTextInputControl field = new ReactTextInputControl(_context, model);
		ScriptingSession session = ScriptingSession.forRoot(panel(field));
		String address = "/panel/textInput";

		assertTrue(commands(node(session, address)).contains(ReactFormFieldControl.CMD_VALUE_CHANGED));

		model.setEditable(false);
		assertEquals(List.of(), commands(node(session, address)));

		Map<String, Object> input = Map.of(FieldValueArguments.VALUE, "y");
		assertNotNull("The agent is refused.",
			CommandFailures.outcome(() -> session.act(address, ReactFormFieldControl.CMD_VALUE_CHANGED, input)));
		assertNotNull("A headless dispatch to the field itself is refused.",
			CommandFailures.outcome(() -> field.executeCommand(ReactFormFieldControl.CMD_VALUE_CHANGED, input)));
		assertNull("The input of a lagging client is dropped silently.",
			CommandFailures.outcome(() -> field.executeClientCommand(ReactFormFieldControl.CMD_VALUE_CHANGED, input)));
		assertEquals("x", model.getValue());
	}

	/** A hidden control is omitted with its subtree. */
	public void testHiddenControlIsOmittedWithSubtree() {
		ReactButtonControl inner = new ReactButtonControl(_context, "Inner", context -> {});
		ReactCompositeControl hidden = new ReactCompositeControl(_context, null, "TLGroup", List.of(inner));
		ReactButtonControl shown = new ReactButtonControl(_context, "Shown", context -> {});
		ScriptingSession session = ScriptingSession.forRoot(panel(hidden, shown));

		assertNotNull(node(session, "/panel/group/button[Inner]"));

		hidden.setHidden(true);
		ScriptingNodeView panel = node(session, "/panel");
		assertEquals(List.of("/panel/button[Shown]"), addresses(panel.children()));
		assertNull(session.addressOf(inner));
		try {
			session.resolve("/panel/group/button[Inner]");
			fail("A hidden control is not addressable.");
		} catch (IllegalArgumentException ex) {
			// Expected.
		}
	}

	/** The content of a collapsed group is omitted, the group shows its collapsed state. */
	public void testCollapsedGroupContentIsOmitted() {
		ReactTextInputControl field = new ReactTextInputControl(_context, new AbstractFieldModel("x"));
		ReactFormGroupControl group =
			new ReactFormGroupControl(_context, "Details", true, true, GroupBorder.NONE, false, List.of(), List.of(field));
		ScriptingSession session = ScriptingSession.forRoot(panel(group));

		ScriptingNodeView node = node(session, "/panel/formGroup");
		assertEquals(Boolean.TRUE, node.state().get(ReactFormGroupControl.COLLAPSED));
		assertEquals(List.of(ReactFormGroupControl.TOGGLE_COLLAPSE_COMMAND), commands(node));
		assertNull(session.addressOf(field));

		session.act("/panel/formGroup", ReactFormGroupControl.TOGGLE_COLLAPSE_COMMAND, Map.of());
		assertEquals("/panel/formGroup/textInput", session.addressOf(field));
	}

	/** A closed menu is not seen. */
	public void testClosedMenuIsOmitted() {
		ReactMenuControl menu = new ReactMenuControl(_context, null, List.of(MenuEntry.item("x", "X")), id -> {},
			() -> {});
		ScriptingSession session = ScriptingSession.forRoot(panel(menu));
		assertEquals(List.of(), node(session, "/panel").children());

		menu.open();
		assertEquals(List.of(ReactMenuControl.SELECT_ITEM_COMMAND), commands(node(session, "/panel/menu")));
	}

	/**
	 * The read-only cell controls of a table are no nodes - the rows of the table carry their
	 * text -, an operable cell control is a node under the node of its row, and acting on it hits
	 * that row.
	 */
	public void testOperableCellControlIsUnderItsRow() {
		List<String> opened = new ArrayList<>();
		TableViewControl<String> table = table(ROWS, opened);
		ScriptingSession session = ScriptingSession.forRoot(panel(table));

		ScriptingNodeView tableNode = node(session, "/panel/tableView");
		List<String> rowIds = new ArrayList<>();
		for (String row : ROWS) {
			rowIds.add(table.objectIdentities().idOf(row));
		}
		List<String> expected = new ArrayList<>();
		for (String id : rowIds) {
			expected.add("/panel/tableView/row[" + id + "]");
		}
		assertEquals("Only the rows are children, the text cells are not.", expected,
			addresses(tableNode.children()));

		ScriptingNodeView rowB = tableNode.children().get(1);
		assertEquals(TableViewControl.ITEM_ROW, rowB.role());
		assertEquals(rowIds.get(1), rowB.state().get(ScriptingTreeProjector.ITEM_ID));
		String buttonAddress = "/panel/tableView/row[" + rowIds.get(1) + "]/button[" + OPEN_LABEL + "]";
		assertEquals(List.of(buttonAddress), addresses(rowB.children()));

		session.act(buttonAddress, ReactButtonControl.CMD_CLICK, Map.of());
		assertEquals(List.of("b"), opened);
	}

	/**
	 * A click on the button of a row is recorded with the key of the row's object in its address,
	 * and replays on the button of the same object's row in a table with other row ids and another
	 * order.
	 */
	public void testRowButtonClickReplaysOnSameRow() {
		List<String> recordedOpens = new ArrayList<>();
		TableViewControl<String> recording = table(ROWS, recordedOpens);
		ReactControl recordingRoot = panel(recording);
		ScriptingSession recordingSession = ScriptingSession.forRoot(recordingRoot);
		String rowId = recording.objectIdentities().idOf("b");
		ReactControl button =
			recordingSession.resolve("/panel/tableView/row[" + rowId + "]/button[" + OPEN_LABEL + "]");

		ScriptRecorder recorder = new ScriptRecorder();
		recorder.start();
		recorder.capture(recordingRoot, button, ReactButtonControl.CMD_CLICK, Map.of());
		recorder.stop();
		ReactCommand step = recorder.steps().get(0);
		assertFalse("The recorded address names the row by its key: " + step.getAddress(),
			step.getAddress().contains("[" + rowId + "]"));

		List<String> reversed = new ArrayList<>(ROWS);
		Collections.reverse(reversed);
		List<String> replayedOpens = new ArrayList<>();
		TableViewControl<String> replay = table(reversed, replayedOpens);
		ScriptingSession replaySession = ScriptingSession.forRoot(panel(replay));
		assertFalse("The row ids differ between the tables.", rowId.equals(replay.objectIdentities().idOf("b")));

		replaySession.replay(step);
		assertEquals(List.of("b"), replayedOpens);
	}

	/**
	 * The read-only content of a tree node is no node - the nodes of the tree carry the labels -,
	 * operable content is a node under the node of its tree node, addressed by the node's id in
	 * the agent observation and by the key of its object in a recorded address.
	 */
	public void testOperableNodeContentIsUnderItsNode() {
		List<String> clicks = new ArrayList<>();
		Map<Object, List<Object>> children = Map.of(TREE_ROOT, List.of("a", "b"));
		DefaultTreeUINodeModel model = new DefaultTreeUINodeModel(new TreeBuilder<DefaultTreeUINode>() {
			@Override
			public DefaultTreeUINode createNode(AbstractMutableTLTreeModel<DefaultTreeUINode> treeModel,
					DefaultTreeUINode parent, Object userObject) {
				return new DefaultTreeUINode(treeModel, parent, userObject);
			}

			@Override
			public List<DefaultTreeUINode> createChildList(DefaultTreeUINode node) {
				List<DefaultTreeUINode> result = new ArrayList<>();
				for (Object child : children.getOrDefault(node.getBusinessObject(), List.of())) {
					result.add(createNode(node.getModel(), node, child));
				}
				return result;
			}

			@Override
			public boolean isFinite() {
				return true;
			}
		}, TREE_ROOT);
		model.setRootVisible(false);
		ReactTreeControl tree = new ReactTreeControl(_context, model,
			new DefaultSingleSelectionModel<>(SelectionModelOwner.NO_OWNER),
			(context, object) -> "a".equals(object)
				? new ReactButtonControl(context, OPEN_LABEL, buttonContext -> clicks.add(String.valueOf(object)))
				: new ReactTextControl(context, String.valueOf(object)));
		tree.updateVisibleState();
		ScriptingSession session = ScriptingSession.forRoot(panel(tree));

		String idA = tree.objectIdentities().idOf("a");
		ScriptingNodeView treeNode = node(session, "/panel/treeView");
		assertEquals("Only the node with operable content is a child, the text of 'b' is not.",
			List.of("/panel/treeView/node[" + idA + "]"), addresses(treeNode.children()));
		ScriptingNodeView nodeA = treeNode.children().get(0);
		assertEquals(ReactTreeControl.ITEM_NODE, nodeA.role());
		String buttonAddress = "/panel/treeView/node[" + idA + "]/button[" + OPEN_LABEL + "]";
		assertEquals(List.of(buttonAddress), addresses(nodeA.children()));

		ReactControl button = session.resolve(buttonAddress);
		String recorded = session.addressOf(button);
		assertFalse("The recorded address names the node by its key: " + recorded, recorded.contains(idA));
		assertSame(button, session.resolve(recorded));

		session.act(recorded, ReactButtonControl.CMD_CLICK, Map.of());
		assertEquals(List.of("a"), clicks);
	}

	private TableViewControl<String> table(List<String> rows, List<String> opened) {
		List<Column<String, ?>> columns = List.<Column<String, ?>> of(
			DefaultColumn.<String, String> builder(NAME, row -> row).build(),
			DefaultColumn.<String, String> builder(TEXT, row -> row)
				.renderer(value -> new CellContent.Raw((CellControlFactory) context -> new ReactTextControl(context, value)))
				.build(),
			DefaultColumn.<String, String> builder(OPEN, row -> row)
				.renderer(value -> new CellContent.Raw((CellControlFactory) context -> new ReactButtonControl(context,
					OPEN_LABEL, buttonContext -> opened.add(value))))
				.build());
		ListRowSource<String> source = new ListRowSource<>(rows, columns);
		TableViewState state = DefaultTableView.initialState(columns, SortSpec.NONE, List.of());
		state.setSelection(Selection.none(SelectionMode.SINGLE));
		TableViewControl<String> table =
			new TableViewControl<>(_context, new DefaultTableView<>(columns, source, state), false);
		table.executeClientCommand("scroll", Map.of(ScrollArguments.START, 0, ScrollArguments.COUNT, 10));
		return table;
	}

	private ReactControl panel(ReactControl... children) {
		return new ReactCompositeControl(_context, null, "TLPanel", List.of(children));
	}

	private static ScriptingNodeView node(ScriptingSession session, String address) {
		ScriptingNodeView result = find(session.observe(), address);
		assertNotNull("No node at " + address, result);
		return result;
	}

	private static ScriptingNodeView find(ScriptingNodeView node, String address) {
		if (address.equals(node.address())) {
			return node;
		}
		for (ScriptingNodeView child : node.children()) {
			ScriptingNodeView found = find(child, address);
			if (found != null) {
				return found;
			}
		}
		return null;
	}

	private static List<String> commands(ScriptingNodeView node) {
		List<String> result = new ArrayList<>();
		for (ScriptingAction action : node.actions()) {
			result.add(action.command());
		}
		return result;
	}

	private static List<String> addresses(List<ScriptingNodeView> nodes) {
		List<String> result = new ArrayList<>();
		for (ScriptingNodeView node : nodes) {
			result.add(node.address());
		}
		return result;
	}

	private static ReactContext context() {
		return new DefaultReactContext("", "test", new SSEUpdateQueue(), new ReactWindowRegistry("test"));
	}

	/**
	 * The test suite, started with the resource bundles the controls need, the type index the
	 * recorder binds the commands with, and the {@link ModelResolver} the rows are named with.
	 */
	public static Test suite() {
		return ModuleTestSetup.setupModule(
			ServiceTestSetup.createSetup(TestObservationShowsWhatUserSees.class, TypeIndex.Module.INSTANCE,
				ThreadContextManager.Module.INSTANCE, ResourcesModule.Module.INSTANCE,
				ModelResolver.Module.INSTANCE));
	}

}
