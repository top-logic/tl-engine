/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.react.control.table;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.layout.react.control.InteractionWithoutSession;
import test.com.top_logic.layout.react.control.dnd.DropOperations;
import test.com.top_logic.layout.react.control.dnd.DropOperations.Operation;

import com.top_logic.basic.exception.ErrorSeverity;
import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactCommands;
import com.top_logic.layout.react.control.RecordedCommand;
import com.top_logic.layout.react.control.common.ReactTextControl;
import com.top_logic.layout.react.control.dnd.AcceptedKinds;
import com.top_logic.layout.react.control.dnd.DropArguments;
import com.top_logic.layout.react.control.dnd.DropEvent;
import com.top_logic.layout.react.control.dnd.DropLocation;
import com.top_logic.layout.react.control.dnd.DropMarker;
import com.top_logic.layout.react.control.dnd.DropMode;
import com.top_logic.layout.react.control.dnd.DropObjectsArguments;
import com.top_logic.layout.react.control.dnd.DropProbeArguments;
import com.top_logic.layout.react.control.dnd.DropSupport;
import com.top_logic.layout.react.control.dnd.DropZone;
import com.top_logic.layout.react.control.table.ExpandArguments;
import com.top_logic.layout.react.control.table.TableViewControl;
import com.top_logic.layout.react.control.tree.ReactTreeControl;
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
import com.top_logic.table.Row;
import com.top_logic.table.SortColumn;
import com.top_logic.table.SortSpec;
import com.top_logic.table.TableView;
import com.top_logic.table.TreeStructure;
import com.top_logic.table.impl.DefaultColumn;
import com.top_logic.table.impl.DefaultTableView;
import com.top_logic.table.impl.ListRowSource;
import com.top_logic.table.impl.TreeRowSource;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * Tests dragging and dropping the rows of a {@link TableViewControl} whose rows form a tree.
 *
 * <p>
 * Such a table resolves a drop by the rules a tree resolves it by, over its rows. The test table
 * holds below the object {@link #ROOT} the rows {@link #A} (with the children {@link #A1},
 * {@link #A2}, expanded), {@link #B} (with the child {@link #B1}, collapsed) and {@link #C} (a
 * leaf).
 * </p>
 */
public class TestTableViewTreeDragDrop extends TestCase {

	private static final String ROOT = "root";

	private static final String A = "a";

	private static final String A1 = "a1";

	private static final String A2 = "a2";

	private static final String B = "b";

	private static final String B1 = "b1";

	private static final String C = "c";

	/** The drag kind the tests' sources and targets agree on. */
	private static final String ITEM = "item";

	/** The name of the single column of the test table. */
	private static final String NAME = "name";

	private final Map<String, List<String>> _children = new HashMap<>();

	private ReactContext _context;

	private TreeTable _table;

	/** A {@link TableViewControl} whose client state the test reads. */
	private static final class TreeTable extends TableViewControl<String> {

		TreeTable(ReactContext context, TableView<String> view) {
			super(context, view, true);
		}

		Object clientState(String key) {
			return getState(key);
		}

	}

	/** A {@link ReactTreeControl} whose client state the test reads. */
	private static final class Tree extends ReactTreeControl {

		Tree(ReactContext context, DefaultTreeUINodeModel model) {
			super(context, model, new DefaultMultiSelectionModel<>(SelectionModelOwner.NO_OWNER),
				(ctx, object) -> new ReactTextControl(ctx, String.valueOf(object)));
		}

		/** The id of the single node of the tree, as the client knows it. */
		@SuppressWarnings("unchecked")
		String singleNodeId() {
			List<Map<String, Object>> nodes = (List<Map<String, Object>>) getState(NODES);
			assertEquals("The test tree displays a single node.", 1, nodes.size());
			return (String) nodes.get(0).get(NODE_ID);
		}

	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_children.put(ROOT, List.of(A, B, C));
		_children.put(A, List.of(A1, A2));
		_children.put(B, List.of(B1));

		// One context, hence one control registry: a drop resolves its source control out of it.
		_context = new DefaultReactContext("", "test", new SSEUpdateQueue(), new ReactWindowRegistry("test"));
		_table = treeTable();
		_table.setDragSource(ITEM);
		expand(A);
	}

	private TreeTable treeTable() {
		TreeStructure<String, String> structure = new TreeStructure<>() {
			@Override
			public List<String> roots() {
				return _children.get(ROOT);
			}

			@Override
			public List<String> children(String node) {
				return _children.getOrDefault(node, List.of());
			}

			@Override
			public boolean isLeaf(String node) {
				return children(node).isEmpty();
			}

			@Override
			public String businessObject(String node) {
				return node;
			}

			@Override
			public Object rootParent() {
				return ROOT;
			}
		};
		List<Column<String, ?>> columns = columns();
		TreeTable result = new TreeTable(_context, DefaultTableView.create(columns, new TreeRowSource<>(structure, columns)));
		// Only a displayed control can be addressed by its ID.
		result.attach();
		return result;
	}

	private static List<Column<String, ?>> columns() {
		return List.of(DefaultColumn.<String, String> builder(NAME, value -> value).sort(() -> Comparator.naturalOrder()).build());
	}

	/** A table whose rows form a tree tells the client to split them as tree nodes. */
	public void testTreeZonesAreAnnounced() {
		assertEquals(Boolean.TRUE, _table.clientState(TableViewControl.DROP_TREE_ZONES));

		List<Column<String, ?>> columns = columns();
		TreeTable flat = new TreeTable(_context, DefaultTableView.create(columns, new ListRowSource<>(List.of(A, B), columns)));
		assertEquals(Boolean.FALSE, flat.clientState(TableViewControl.DROP_TREE_ZONES));
	}

	/**
	 * The upper part of a row inserts before it among its siblings, the middle part as its first
	 * child (computing the children of a collapsed row), the lower part as the first child of an
	 * expanded row with children and after the row otherwise, and beside the rows as the last
	 * top-level row - under the object the top-level rows are the children of.
	 */
	public void testOrderedLocations() {
		Operation ordered = new Operation(DropMode.ORDERED);
		_table.setDropTarget(new DropOperations(AcceptedKinds.ANY, ordered));

		assertInserted(ordered, A, DropZone.UPPER, ROOT, A);
		assertInserted(ordered, A2, DropZone.UPPER, A, A2);
		assertInserted(ordered, B, DropZone.MIDDLE, B, B1);
		assertInserted(ordered, C, DropZone.MIDDLE, C, null);
		assertInserted(ordered, A, DropZone.LOWER, A, A1);
		assertInserted(ordered, B, DropZone.LOWER, ROOT, C);
		assertInserted(ordered, A2, DropZone.LOWER, A, null);
		assertInserted(ordered, C, DropZone.LOWER, ROOT, null);
		assertInserted(ordered, null, DropZone.NONE, ROOT, null);
	}

	/**
	 * Siblings are taken in the order they are displayed in: sorted in descending order, the first
	 * child of an expanded row is the one displayed first, and the sibling a row is followed by the
	 * one displayed after it.
	 */
	public void testOrderFollowsTheDisplayedSort() {
		Operation ordered = new Operation(DropMode.ORDERED);
		_table.setDropTarget(new DropOperations(AcceptedKinds.ANY, ordered));
		_table.getView().sort(new SortSpec(List.of(new SortColumn(NAME, false))));
		_table.refreshData();

		assertInserted(ordered, A, DropZone.LOWER, A, A2);
		assertInserted(ordered, C, DropZone.LOWER, ROOT, B);
		assertInserted(ordered, A1, DropZone.LOWER, A, null);
	}

	private void assertInserted(Operation ordered, String row, DropZone zone, Object parent, Object before) {
		assertApplied("An insertion at " + row + "/" + zone + " must be applied.", drop(dropAt(C, row, zone)));
		assertEquals("Insertion at " + row + "/" + zone + ".", new DropLocation.Insert(parent, before),
			ordered.lastLocation());
	}

	/**
	 * The marker of an insertion is a line before the row in its upper part, a highlight of the row
	 * in its middle, a line after it in its lower part, and a highlight of the table as a whole
	 * beside the rows.
	 */
	public void testOrderedMarkers() {
		_table.setDropTarget(new DropOperations(AcceptedKinds.ANY, new Operation(DropMode.ORDERED)));

		probe("drag1", "upper", dropAt(C, B, DropZone.UPPER));
		probe("drag1", "middle", dropAt(C, B, DropZone.MIDDLE));
		probe("drag1", "lower", dropAt(C, B, DropZone.LOWER));
		probe("drag1", "beside", dropAt(C, null, DropZone.NONE));

		Map<String, Map<String, Object>> verdicts = verdicts();
		assertMarker(verdicts.get("upper"), DropMarker.BEFORE, rowKey(B));
		assertMarker(verdicts.get("middle"), DropMarker.INTO, rowKey(B));
		assertMarker(verdicts.get("lower"), DropMarker.AFTER, rowKey(B));
		assertMarker(verdicts.get("beside"), DropMarker.CONTROL, null);
	}

	/**
	 * A drop onto a row is made onto it in any of its zones and highlights it; beside the rows there
	 * is no row to drop onto.
	 */
	public void testOnto() {
		Operation onto = new Operation(DropMode.ONTO);
		_table.setDropTarget(new DropOperations(AcceptedKinds.of(List.of(ITEM)), onto));

		for (DropZone zone : List.of(DropZone.UPPER, DropZone.MIDDLE, DropZone.LOWER)) {
			assertApplied("A drop onto a row applies in its " + zone + " zone.", drop(dropAt(C, A1, zone)));
			assertEquals(new DropLocation.Onto(A1), onto.lastLocation());
		}
		assertEquals(List.of(C), onto.applied().get(0).objects());

		probe("drag1", "p1", dropAt(C, A1, DropZone.MIDDLE));
		assertMarker(verdicts().get("p1"), DropMarker.INTO, rowKey(A1));

		assertRefused("Beside the rows there is nothing to drop onto.", drop(dropAt(C, null, DropZone.NONE)));
	}

	/**
	 * Of an insertion declared before a drop onto rows, the insertion takes the middle of a row -
	 * except where it refuses, there the drop onto the row takes over.
	 */
	public void testDeclaredOrderAndFallThrough() {
		Operation ordered = new Operation(DropMode.ORDERED,
			location -> C.equals(((DropLocation.Insert) location).parent()) ? DropOperations.REFUSAL : null);
		Operation onto = new Operation(DropMode.ONTO);
		_table.setDropTarget(new DropOperations(AcceptedKinds.ANY, ordered, onto));

		assertApplied("", drop(dropAt(A1, B, DropZone.MIDDLE)));
		assertEquals(new DropLocation.Insert(B, B1), ordered.lastLocation());
		assertEquals(List.of(), onto.applied());

		assertApplied("", drop(dropAt(A1, C, DropZone.MIDDLE)));
		assertEquals("The refused insertion falls through to the drop onto the row.",
			new DropLocation.Onto(C), onto.lastLocation());
	}

	/** Dragging a selected row drags the whole selection, in display order. */
	public void testDragOfTheSelection() {
		Operation onto = new Operation(DropMode.ONTO);
		_table.setDropTarget(new DropOperations(AcceptedKinds.ANY, onto));
		_table.selectRows(List.of(C, A1));

		Map<String, Object> ofSelection = dropAt(A1, B, DropZone.MIDDLE);
		ofSelection.put(DropArguments.SELECTION, Boolean.TRUE);
		assertApplied("", drop(ofSelection));
		assertEquals(List.of(A1, C), onto.applied().get(0).objects());
	}

	/**
	 * A row dragged out of a tree table is dropped onto a node of a tree, and a node dragged out of
	 * a tree is inserted among the rows of a tree table: each drop resolves the other control as its
	 * source.
	 */
	public void testDragBetweenTreeTableAndTree() {
		Tree tree = tree();
		tree.setDragSource(ITEM, null);
		Operation ontoNode = new Operation(DropMode.ONTO);
		tree.setDropTarget(new DropOperations(AcceptedKinds.of(List.of(ITEM)), ontoNode));
		String nodeX = tree.singleNodeId();

		Map<String, Object> tableToTree = new HashMap<>();
		tableToTree.put(DropArguments.SOURCE, _table.getID());
		tableToTree.put(DropArguments.KEYS, rowKey(A2));
		tableToTree.put(DropArguments.SELECTION, Boolean.FALSE);
		tableToTree.put(DropArguments.TARGET_KEY, nodeX);
		tableToTree.put(DropArguments.ZONE, DropZone.MIDDLE.wireName());
		assertApplied("A row is dropped onto a tree node.",
			tree.executeClientCommand(DropSupport.CMD_DROP, tableToTree));
		assertEquals(List.of(A2), ontoNode.applied().get(0).objects());
		assertSame(_table, ontoNode.applied().get(0).source());
		assertEquals(new DropLocation.Onto("x"), ontoNode.lastLocation());

		Operation ordered = new Operation(DropMode.ORDERED);
		_table.setDropTarget(new DropOperations(AcceptedKinds.of(List.of(ITEM)), ordered));
		Map<String, Object> treeToTable = new HashMap<>();
		treeToTable.put(DropArguments.SOURCE, tree.getID());
		treeToTable.put(DropArguments.KEYS, nodeX);
		treeToTable.put(DropArguments.SELECTION, Boolean.FALSE);
		treeToTable.put(DropArguments.TARGET_KEY, rowKey(A1));
		treeToTable.put(DropArguments.ZONE, DropZone.LOWER.wireName());
		assertApplied("A tree node is inserted among the rows.", drop(treeToTable));
		assertEquals(List.of("x"), ordered.applied().get(0).objects());
		assertSame(tree, ordered.applied().get(0).source());
		assertEquals(new DropLocation.Insert(A, A2), ordered.lastLocation());
	}

	/**
	 * A recorded insertion names the parent and the object inserted before by their business
	 * identity, and its replay offers exactly that location to the same operation - also for an
	 * insertion into a collapsed row and among the top-level rows.
	 */
	public void testRecordingRoundTrip() {
		Operation ordered = new Operation(DropMode.ORDERED);
		_table.setDropTarget(new DropOperations(AcceptedKinds.of(List.of(ITEM)), ordered));

		DropObjectsArguments lower = record(dropAt(C, A, DropZone.LOWER));
		assertEquals(DropMode.ORDERED.wireName(), lower.getMode());
		assertNotNull(lower.getParent());
		assertNotNull(lower.getBefore());
		assertEquals("Recording applies nothing.", List.of(), ordered.applied());
		assertEquals(new DropLocation.Insert(A, A1), replay(ordered, lower).location());
		assertEquals(List.of(C), ordered.applied().get(0).objects());
		assertNull("A replayed drop names no source control.", ordered.applied().get(0).source());
		assertEquals(ITEM, ordered.applied().get(0).kind());

		DropObjectsArguments intoCollapsed = record(dropAt(C, B, DropZone.MIDDLE));
		assertEquals(new DropLocation.Insert(B, B1), replay(ordered, intoCollapsed).location());

		DropObjectsArguments beside = record(dropAt(C, null, DropZone.NONE));
		assertNull(beside.getBefore());
		assertEquals(new DropLocation.Insert(ROOT, null), replay(ordered, beside).location());
	}

	/**
	 * A recorded drop whose location the table does not display - an object inserted before that is
	 * no child of the recorded parent - fails as a drift rather than being applied somewhere else.
	 */
	public void testReplayOfAMisplacedLocationFails() {
		Operation ordered = new Operation(DropMode.ORDERED);
		_table.setDropTarget(new DropOperations(AcceptedKinds.of(List.of(ITEM)), ordered));
		DropObjectsArguments recorded = record(dropAt(C, A, DropZone.LOWER));
		recorded.setParent(record(dropAt(C, B, DropZone.MIDDLE)).getParent());

		HandlerResult result = new InteractionWithoutSession().runWithContext(
			() -> _table.executeClientCommand(DropSupport.CMD_DROP_OBJECTS, ReactCommands.arguments(recorded)));
		assertFalse("A drop before a child of another parent must not replay.", result.isSuccess());
		assertEquals(ErrorSeverity.ERROR, result.getErrorSeverity());
		assertEquals(List.of(), ordered.applied());
	}

	private DropObjectsArguments record(Map<String, Object> arguments) {
		RecordedCommand recorded = _table.recordCommand(DropSupport.CMD_DROP, arguments);
		assertNotNull("The drop must be recorded.", recorded);
		assertTrue("The drop is recorded in replay-stable form.",
			recorded.command() instanceof DropObjectsArguments);
		return (DropObjectsArguments) recorded.command();
	}

	/** Replays the given recorded drop, and returns the event the given operation applied. */
	private DropEvent replay(Operation operation, DropObjectsArguments recorded) {
		int before = operation.applied().size();
		HandlerResult result = new InteractionWithoutSession().runWithContext(
			() -> _table.executeClientCommand(DropSupport.CMD_DROP_OBJECTS, ReactCommands.arguments(recorded)));
		assertApplied("The recorded drop must replay.", result);
		assertEquals(before + 1, operation.applied().size());
		return operation.applied().get(before);
	}

	private HandlerResult drop(Map<String, Object> arguments) {
		return _table.executeClientCommand(DropSupport.CMD_DROP, arguments);
	}

	private void probe(String drag, String probe, Map<String, Object> drop) {
		Map<String, Object> arguments = new HashMap<>(drop);
		arguments.put(DropProbeArguments.DRAG, drag);
		arguments.put(DropProbeArguments.PROBE, probe);
		assertApplied("A probe never fails.", _table.executeClientCommand(DropSupport.CMD_DROP_PROBE, arguments));
	}

	/**
	 * The arguments of a drop of the row of the given object in the given zone of the row of the
	 * given target object, beside the rows for a {@code null} target.
	 */
	private Map<String, Object> dropAt(String dragged, String target, DropZone zone) {
		Map<String, Object> arguments = new HashMap<>();
		arguments.put(DropArguments.SOURCE, _table.getID());
		arguments.put(DropArguments.KEYS, rowKey(dragged));
		arguments.put(DropArguments.SELECTION, Boolean.FALSE);
		if (target != null) {
			arguments.put(DropArguments.TARGET_KEY, rowKey(target));
		}
		arguments.put(DropArguments.ZONE, zone.wireName());
		return arguments;
	}

	private void expand(String object) {
		_table.executeCommand(TableViewControl.CMD_EXPAND,
			Map.of(ExpandArguments.ROW_INDEX, Integer.valueOf(rowIndex(object)),
				ExpandArguments.EXPANDED, Boolean.TRUE));
	}

	/** The client-side key of the displayed row of the given object. */
	private String rowKey(String object) {
		return "row_" + rowIndex(object);
	}

	private int rowIndex(String object) {
		List<Row<String>> rows = _table.getView().rows(0, _table.getView().rowCount());
		for (int n = 0; n < rows.size(); n++) {
			if (object.equals(rows.get(n).data())) {
				return n;
			}
		}
		throw new IllegalArgumentException("The table displays no row for '" + object + "'.");
	}

	@SuppressWarnings("unchecked")
	private Map<String, Map<String, Object>> verdicts() {
		return (Map<String, Map<String, Object>>) _table.clientState(DropSupport.DROP_VERDICTS);
	}

	/** A tree of the single node {@code x} below a hidden root. */
	private Tree tree() {
		DefaultTreeUINodeModel model = new DefaultTreeUINodeModel(new TreeBuilder<>() {
			@Override
			public DefaultTreeUINode createNode(AbstractMutableTLTreeModel<DefaultTreeUINode> treeModel,
					DefaultTreeUINode parent, Object userObject) {
				return new DefaultTreeUINode(treeModel, parent, userObject);
			}

			@Override
			public List<DefaultTreeUINode> createChildList(DefaultTreeUINode node) {
				List<DefaultTreeUINode> children = new ArrayList<>();
				if (node.getParent() == null) {
					children.add(createNode(node.getModel(), node, "x"));
				}
				return children;
			}

			@Override
			public boolean isFinite() {
				return true;
			}
		}, "treeRoot");
		Tree result = new Tree(_context, model);
		result.attach();
		return result;
	}

	private static void assertMarker(Map<String, Object> verdict, DropMarker marker, String markerKey) {
		assertEquals("The drop must be accepted: " + verdict, Boolean.TRUE, verdict.get(DropSupport.VERDICT_ACCEPTED));
		assertEquals(marker.wireName(), verdict.get(DropSupport.VERDICT_MARKER));
		assertEquals(markerKey, verdict.get(DropSupport.VERDICT_MARKER_KEY));
	}

	private static void assertRefused(String message, HandlerResult result) {
		assertFalse(message, result.isSuccess());
		assertEquals(message + " A refused drop is a warning.", ErrorSeverity.WARNING, result.getErrorSeverity());
	}

	private static void assertApplied(String message, HandlerResult result) {
		assertTrue(message + " Refused with: " + result.getEncodedErrors(), result.isSuccess());
	}

	/**
	 * The test suite, started with the resource bundles a refused drop's message needs, and the
	 * {@link ModelResolver} a recorded drop names its objects with.
	 */
	public static Test suite() {
		return ModuleTestSetup.setupModule(
			ServiceTestSetup.createSetup(TestTableViewTreeDragDrop.class, ResourcesModule.Module.INSTANCE,
				ModelResolver.Module.INSTANCE));
	}

}
