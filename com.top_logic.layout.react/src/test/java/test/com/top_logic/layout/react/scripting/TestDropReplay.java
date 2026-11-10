/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.react.scripting;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.layout.react.control.dnd.DropOperations;
import test.com.top_logic.layout.react.control.dnd.DropOperations.Operation;
import test.com.top_logic.layout.react.control.dnd.DropRecording;

import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.ScriptingModelKey;
import com.top_logic.layout.react.control.common.ReactTextControl;
import com.top_logic.layout.react.control.dnd.AcceptedKinds;
import com.top_logic.layout.react.control.dnd.DropArguments;
import com.top_logic.layout.react.control.dnd.DropEvent;
import com.top_logic.layout.react.control.dnd.DropLocation;
import com.top_logic.layout.react.control.dnd.DropMode;
import com.top_logic.layout.react.control.dnd.DropSupport;
import com.top_logic.layout.react.control.dnd.DropZone;
import com.top_logic.layout.react.control.kanban.ReactKanbanBoardControl;
import com.top_logic.layout.react.control.layout.ReactStackControl;
import com.top_logic.layout.react.control.table.TableViewControl;
import com.top_logic.layout.react.control.tree.ReactTreeControl;
import com.top_logic.layout.react.scripting.ScriptingAction;
import com.top_logic.layout.react.scripting.ScriptingNodeView;
import com.top_logic.layout.react.scripting.ScriptingSession;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.scripting.recorder.ref.ModelResolver;
import com.top_logic.layout.tree.model.AbstractMutableTLTreeModel;
import com.top_logic.layout.tree.model.DefaultTreeUINodeModel;
import com.top_logic.layout.tree.model.DefaultTreeUINodeModel.DefaultTreeUINode;
import com.top_logic.layout.tree.model.TreeBuilder;
import com.top_logic.mig.html.DefaultMultiSelectionModel;
import com.top_logic.mig.html.SelectionModelOwner;
import com.top_logic.table.Column;
import com.top_logic.table.impl.DefaultColumn;
import com.top_logic.table.impl.DefaultTableView;
import com.top_logic.table.impl.ListRowSource;

/**
 * Tests that a drop between two controls, recorded in one session, replays in a fresh one.
 *
 * <p>
 * The drop is recorded the way the browser dispatches it: the source control by its control id,
 * the dragged objects by their ids in the source, the target by its id in the receiving control.
 * The recorded step names the source by its address and the objects by their keys, and its replay
 * in another session - with other control, row and node ids - drops the same objects at the same
 * place.
 * </p>
 */
public class TestDropReplay extends TestCase {

	/** The drag kind all controls of the screen agree on. */
	private static final String ITEM = "item";

	private static final String ROOT = "root";

	private static final String TODO = "todo";

	private static final String DONE = "done";

	/** The number of {@link Screen}s built, to give each one other ids. */
	private static int _screens;

	/**
	 * A window holding a table to drag rows out of, a tree and a board to drop them on.
	 */
	private static final class Screen {

		final TableViewControl<String> _table;

		final ReactTreeControl _tree;

		final ReactKanbanBoardControl _board;

		final ReactControl _root;

		final Operation _ontoNode = new Operation(DropMode.ONTO);

		final Operation _ontoColumn = new Operation(DropMode.ONTO);

		Screen(List<String> rows) {
			ReactContext context =
				new DefaultReactContext("", "test", new SSEUpdateQueue(), new ReactWindowRegistry("test"));
			// Ids the other screens do not use.
			for (int n = 0, cnt = ++_screens * 10; n < cnt; n++) {
				context.allocateId();
			}

			List<Column<String, ?>> columns =
				List.of(DefaultColumn.<String, String> builder("name", value -> value).build());
			_table = new TableViewControl<>(context,
				DefaultTableView.create(columns, new ListRowSource<>(rows, columns)), false);
			_table.setDragSource(ITEM);

			DefaultTreeUINodeModel model = new DefaultTreeUINodeModel(builder(), ROOT);
			model.setRootVisible(false);
			_tree = new ReactTreeControl(context, model, new DefaultMultiSelectionModel<>(SelectionModelOwner.NO_OWNER),
				(ctx, node) -> new ReactTextControl(ctx, String.valueOf(node)));
			_tree.setDropTarget(new DropOperations(AcceptedKinds.of(List.of(ITEM)), _ontoNode));

			_board = new ReactKanbanBoardControl(context);
			_board.setColumns(List.of(
				new ReactKanbanBoardControl.Column(TODO, "To do",
					List.of(new ReactKanbanBoardControl.Card("c1", new ReactTextControl(context, "c1")))),
				new ReactKanbanBoardControl.Column(DONE, "Done", List.of())));
			_board.setDropTarget(new DropOperations(AcceptedKinds.of(List.of(ITEM)), _ontoColumn));

			_root = new ReactStackControl(context, List.of(_table, _tree, _board));
			// The window displays the screen.
			context.getSSEQueue().setRootControl(_root);
			_root.attach();
		}

		/** The arguments of a drop of the row of the given object, as the browser sends them. */
		Map<String, Object> dropOf(String row, String targetKey, DropZone zone) {
			Map<String, Object> arguments = new HashMap<>();
			arguments.put(DropArguments.SOURCE, _table.getID());
			arguments.put(DropArguments.KEYS, List.of(_table.objectIdentities().idOf(row)));
			arguments.put(DropArguments.SELECTION, Boolean.FALSE);
			arguments.put(DropArguments.TARGET_KEY, targetKey);
			arguments.put(DropArguments.ZONE, zone.wireName());
			return arguments;
		}

		String address(ReactControl control) {
			return ScriptingSession.forRoot(_root).addressOf(control);
		}

	}

	/**
	 * A row dragged out of a table onto a tree node is recorded with the address of the table, the
	 * key of the row and the key of the node, and replays in a fresh session, whose rows are in
	 * another order, onto the node of the same object.
	 */
	public void testTableToTreeReplaysInAFreshSession() {
		Screen recording = new Screen(List.of("r1", "r2", "r3"));
		String node = recording._tree.objectIdentities().idOf("n2");
		DropArguments recorded = DropRecording.record(recording._root, recording._tree,
			recording.dropOf("r2", node, DropZone.MIDDLE));

		assertEquals(recording.address(recording._tree), recorded.getAddress());
		assertEquals("The source is recorded by its address.", recording.address(recording._table),
			recorded.getSource());
		assertTrue(recorded.getSource().startsWith(ScriptingSession.ROOT));
		assertEquals(List.of(ScriptingModelKey.toJson(null, "r2")), recorded.getKeys());
		assertEquals(ScriptingModelKey.toJson(null, "n2"), recorded.getTargetKey());
		assertEquals("Recording applies nothing.", List.of(), recording._ontoNode.applied());

		Screen fresh = new Screen(List.of("r3", "r2", "r1"));
		assertFalse("The fresh session has other ids.", fresh._table.getID().equals(recording._table.getID()));
		assertNull("The replay applies the drop.", DropRecording.replay(fresh._root, recorded));

		assertEquals(1, fresh._ontoNode.applied().size());
		DropEvent event = fresh._ontoNode.applied().get(0);
		assertEquals(List.of("r2"), event.objects());
		assertEquals(new DropLocation.Onto("n2"), event.location());
		assertSame("The replayed drop names the table of the fresh session.", fresh._table, event.source());
		assertEquals(ITEM, event.kind());
	}

	/**
	 * A drop on a column of a board names the column by its key, and replays in a fresh session
	 * onto the same column.
	 */
	public void testDropOnABoardColumnReplays() {
		Screen recording = new Screen(List.of("r1", "r2"));
		String column = recording._board.objectIdentities().idOf(DONE);
		assertEquals(DONE, recording._board.columnValue(column));
		DropArguments recorded = DropRecording.record(recording._root, recording._board,
			recording.dropOf("r1", column, DropZone.NONE));
		assertEquals(ScriptingModelKey.toJson(null, DONE), recorded.getTargetKey());

		Screen fresh = new Screen(List.of("r1", "r2"));
		assertNull("The replay applies the drop.", DropRecording.replay(fresh._root, recorded));

		assertEquals(1, fresh._ontoColumn.applied().size());
		DropEvent event = fresh._ontoColumn.applied().get(0);
		assertEquals(List.of("r1"), event.objects());
		assertEquals(new DropLocation.Onto(DONE), event.location());
	}

	/**
	 * An agent performs a drop with the ids it observes, naming the source control by the address
	 * it observes the control under.
	 */
	public void testAgentDropsWithTheObservedAddressAndIds() {
		Screen screen = new Screen(List.of("r1", "r2"));
		Map<String, Object> arguments = screen.dropOf("r1", screen._tree.objectIdentities().idOf("n1"), DropZone.MIDDLE);
		arguments.put(DropArguments.SOURCE, screen.address(screen._table));

		ScriptingSession.forRoot(screen._root).act(screen.address(screen._tree), DropSupport.CMD_DROP, arguments);

		assertEquals(1, screen._ontoNode.applied().size());
		DropEvent event = screen._ontoNode.applied().get(0);
		assertEquals(List.of("r1"), event.objects());
		assertEquals(new DropLocation.Onto("n1"), event.location());
		assertSame(screen._table, event.source());
	}

	/**
	 * The drop action is advertised with a schema of its own arguments - the source, the ids of the
	 * dragged items, the target id and the zone -, without any business key type.
	 */
	public void testDropActionAdvertisesItsArguments() {
		Screen screen = new Screen(List.of("r1"));
		String tree = screen.address(screen._tree);
		ScriptingNodeView node = ScriptingSession.forRoot(screen._root).observe().actionableNodes().stream()
			.filter(candidate -> tree.equals(candidate.address())).findFirst().orElseThrow();
		ScriptingAction drop = node.actions().stream()
			.filter(action -> DropSupport.CMD_DROP.equals(action.command())).findFirst().orElseThrow();

		String schema = ScriptingNodeView.toJson(drop.argsSchema().schema());
		for (String property : List.of(DropArguments.SOURCE, DropArguments.KEYS, DropArguments.SELECTION,
			DropArguments.TARGET_KEY, DropArguments.ZONE)) {
			assertTrue("The schema describes '" + property + "': " + schema, schema.contains('"' + property + '"'));
		}
		assertFalse("The schema names no business key: " + schema, schema.contains("ModelName"));
		assertFalse("The schema is a plain object schema: " + schema, schema.contains("allOf"));
		assertFalse("The schema has no type tag: " + schema, schema.contains("$type"));

		Map<String, Object> document = ScriptingSession.forRoot(screen._root).observe().toMap();
		Map<?, ?> schemas = (Map<?, ?>) document.get(ScriptingNodeView.SCHEMAS);
		assertEquals("The observation lists the schema under its id.", drop.argsSchema().schema(),
			schemas.get(drop.argsSchema().id()));
		assertEquals("#/" + ScriptingNodeView.SCHEMAS + "/" + drop.argsSchema().id(), drop.argsSchema().reference());
		assertFalse("The probe is not offered: " + node.actions(), node.actions().stream()
			.anyMatch(action -> DropSupport.CMD_DROP_PROBE.equals(action.command())));
	}

	/**
	 * The builder of the test tree: two nodes below the invisible root.
	 */
	private static TreeBuilder<DefaultTreeUINode> builder() {
		Map<Object, List<Object>> children = Map.of(ROOT, List.of("n1", "n2"));
		return new TreeBuilder<>() {
			@Override
			public DefaultTreeUINode createNode(AbstractMutableTLTreeModel<DefaultTreeUINode> model,
					DefaultTreeUINode parent, Object userObject) {
				return new DefaultTreeUINode(model, parent, userObject);
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
		};
	}

	/**
	 * The test suite, started with the resource bundles the controls need, and the
	 * {@link ModelResolver} the recorded keys are built and resolved with.
	 */
	public static Test suite() {
		return ModuleTestSetup.setupModule(
			ServiceTestSetup.createSetup(TestDropReplay.class, ResourcesModule.Module.INSTANCE,
				ModelResolver.Module.INSTANCE));
	}

}
