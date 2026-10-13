/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.react.control.tree;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
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
import com.top_logic.basic.util.ResKey;
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
import com.top_logic.layout.react.control.table.TableViewControl;
import com.top_logic.layout.react.control.tree.CollapseNodeArguments;
import com.top_logic.layout.react.control.tree.ExpandNodeArguments;
import com.top_logic.layout.react.control.tree.ReactTreeControl;
import com.top_logic.layout.react.control.tree.SelectNodeArguments;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.scripting.recorder.ref.ModelResolver;
import com.top_logic.layout.tree.model.AbstractMutableTLTreeModel;
import com.top_logic.layout.tree.model.DefaultTreeUINodeModel;
import com.top_logic.layout.tree.model.DefaultTreeUINodeModel.DefaultTreeUINode;
import com.top_logic.layout.tree.model.TreeBuilder;
import com.top_logic.mig.html.DefaultMultiSelectionModel;
import com.top_logic.mig.html.SelectionModel;
import com.top_logic.mig.html.SelectionModelOwner;
import com.top_logic.table.Column;
import com.top_logic.table.SelectionMode;
import com.top_logic.table.impl.DefaultColumn;
import com.top_logic.table.impl.DefaultTableView;
import com.top_logic.table.impl.ListRowSource;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * Tests dragging and dropping the nodes of a {@link ReactTreeControl} through the commands its
 * client sends.
 *
 * <p>
 * The tree resolves the node and the {@link DropZone zone} a drop is made in into the
 * {@link DropLocation} of each {@link DropMode}, whose reference objects are the business objects
 * of the nodes. The test tree holds below its hidden root {@link #ROOT} the objects {@link #A}
 * (with the children {@link #A1}, {@link #A2}, expanded), {@link #B} (with the child {@link #B1},
 * collapsed) and {@link #C} (a leaf).
 * </p>
 */
public class TestReactTreeControlDragDrop extends TestCase {

	private static final String ROOT = "root";

	private static final String A = "a";

	private static final String A1 = "a1";

	private static final String A2 = "a2";

	private static final String B = "b";

	private static final String B1 = "b1";

	private static final String C = "c";

	/** The drag kind the tests' sources and targets agree on. */
	private static final String ITEM = "item";

	private static final ResKey REFUSAL = DropOperations.REFUSAL;

	/** A {@link ReactTreeControl} whose client state the test reads. */
	private static final class Tree extends ReactTreeControl {

		Tree(ReactContext context, DefaultTreeUINodeModel model, SelectionModel<?> selection) {
			super(context, model, selection, (ctx, object) -> new ReactTextControl(ctx, String.valueOf(object)));
		}

		Object clientState(String key) {
			return getState(key);
		}

	}

	private final Map<Object, List<Object>> _children = new HashMap<>();

	private ReactContext _context;

	private DefaultTreeUINodeModel _model;

	private SelectionModel<Object> _selection;

	private Tree _tree;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_children.put(ROOT, List.of(A, B, C));
		_children.put(A, List.of(A1, A2));
		_children.put(B, List.of(B1));

		// One context, hence one control registry: a drop resolves its source control out of it.
		_context = new DefaultReactContext("", "test", new SSEUpdateQueue(), new ReactWindowRegistry("test"));
		_tree = newTree(false);
		_tree.setDragSource(ITEM, null);
		expand(A);
	}

	private Tree newTree(boolean rootVisible) {
		_model = new DefaultTreeUINodeModel(builder(), ROOT);
		_model.setRootVisible(rootVisible);
		if (rootVisible) {
			_model.setExpanded(_model.getRoot(), true);
		}
		_selection = new DefaultMultiSelectionModel<>(SelectionModelOwner.NO_OWNER);
		Tree result = new Tree(_context, _model, _selection);
		result.setSelectionMode(SelectionMode.MULTI);
		// Only a displayed control can be addressed by its ID.
		result.attach();
		return result;
	}

	/** The modes of the target's operations reach the client, in the target's order. */
	public void testDropModesAreAnnounced() {
		assertEquals("A tree accepting no drop announces no mode.", List.of(),
			_tree.clientState(DropSupport.DROP_MODES));

		_tree.setDropTarget(new DropOperations(AcceptedKinds.ANY, new Operation(DropMode.ORDERED), new Operation(DropMode.ONTO)));
		assertEquals(List.of(DropMode.ORDERED.wireName(), DropMode.ONTO.wireName()),
			_tree.clientState(DropSupport.DROP_MODES));
		assertEquals(Boolean.TRUE, _tree.clientState(DropSupport.DROP_ACCEPTS_ANY));

		_tree.setDropTarget(new DropOperations(AcceptedKinds.of(List.of(ITEM)), new Operation(DropMode.CONTROL)));
		assertEquals(List.of(DropMode.CONTROL.wireName()), _tree.clientState(DropSupport.DROP_MODES));
		assertEquals(List.of(ITEM), _tree.clientState(DropSupport.DROP_ACCEPTS));
	}

	/**
	 * The upper part of a node inserts before it among its siblings, the middle part as its first
	 * child (loading the children of a collapsed node), the lower part as the first child of an
	 * expanded node with children and after the node otherwise, and beside the nodes as the last
	 * top-level node - under the hidden root.
	 */
	public void testOrderedLocations() {
		Operation ordered = new Operation(DropMode.ORDERED);
		_tree.setDropTarget(new DropOperations(AcceptedKinds.ANY, ordered));

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
	 * A tree displaying its root as a node: the parent of that node is no object, and a drop beside
	 * the nodes appends below no object.
	 */
	public void testOrderedLocationsWithVisibleRoot() {
		_tree = newTree(true);
		_tree.setDragSource(ITEM, null);
		Operation ordered = new Operation(DropMode.ORDERED);
		_tree.setDropTarget(new DropOperations(AcceptedKinds.ANY, ordered));

		assertInserted(ordered, ROOT, DropZone.UPPER, null, ROOT);
		assertInserted(ordered, ROOT, DropZone.LOWER, ROOT, A);
		assertInserted(ordered, A, DropZone.UPPER, ROOT, A);
		assertInserted(ordered, null, DropZone.NONE, null, null);
	}

	private void assertInserted(Operation ordered, Object node, DropZone zone, Object parent, Object before) {
		assertApplied("An insertion at " + node + "/" + zone + " must be applied.",
			drop(_tree, dropAt(_tree, C, node, zone)));
		assertEquals("Insertion at " + node + "/" + zone + ".", new DropLocation.Insert(parent, before),
			ordered.lastLocation());
	}

	/**
	 * The marker of an insertion is a line before the node in its upper part, a highlight of the
	 * node in its middle, a line after it in its lower part, and a highlight of the tree as a whole
	 * beside the nodes.
	 */
	public void testOrderedMarkers() {
		_tree.setDropTarget(new DropOperations(AcceptedKinds.ANY, new Operation(DropMode.ORDERED)));

		probe("drag1", "upper", dropAt(_tree, C, B, DropZone.UPPER));
		probe("drag1", "middle", dropAt(_tree, C, B, DropZone.MIDDLE));
		probe("drag1", "lower", dropAt(_tree, C, B, DropZone.LOWER));
		probe("drag1", "beside", dropAt(_tree, C, null, DropZone.NONE));

		Map<String, Map<String, Object>> verdicts = verdicts();
		assertMarker(verdicts.get("upper"), DropMarker.BEFORE, nodeId(B));
		assertMarker(verdicts.get("middle"), DropMarker.INTO, nodeId(B));
		assertMarker(verdicts.get("lower"), DropMarker.AFTER, nodeId(B));
		assertMarker(verdicts.get("beside"), DropMarker.CONTROL, null);
	}

	/**
	 * A drop onto a node is made onto it in any of its zones and highlights it; beside the nodes
	 * there is no node to drop onto.
	 */
	public void testOnto() {
		Operation onto = new Operation(DropMode.ONTO);
		_tree.setDropTarget(new DropOperations(AcceptedKinds.of(List.of(ITEM)), onto));

		for (DropZone zone : List.of(DropZone.UPPER, DropZone.MIDDLE, DropZone.LOWER)) {
			assertApplied("A drop onto a node applies in its " + zone + " zone.",
				drop(_tree, dropAt(_tree, C, A1, zone)));
			assertEquals(new DropLocation.Onto(A1), onto.lastLocation());
		}
		assertEquals(List.of(C), onto.applied().get(0).objects());
		assertSame(_tree, onto.applied().get(0).source());

		probe("drag1", "p1", dropAt(_tree, C, A1, DropZone.MIDDLE));
		assertMarker(verdicts().get("p1"), DropMarker.INTO, nodeId(A1));

		assertRefused("Beside the nodes there is nothing to drop onto.",
			drop(_tree, dropAt(_tree, C, null, DropZone.NONE)));
	}

	/**
	 * Of an insertion declared before a drop onto nodes, the insertion takes the middle of a node -
	 * except where it refuses, there the drop onto the node takes over; declared the other way
	 * round, the drop onto the node takes the whole node.
	 */
	public void testDeclaredOrderAndFallThrough() {
		Operation ordered = new Operation(DropMode.ORDERED,
			location -> C.equals(((DropLocation.Insert) location).parent()) ? REFUSAL : null);
		Operation onto = new Operation(DropMode.ONTO);
		_tree.setDropTarget(new DropOperations(AcceptedKinds.ANY, ordered, onto));

		assertApplied("", drop(_tree, dropAt(_tree, A1, B, DropZone.MIDDLE)));
		assertEquals(new DropLocation.Insert(B, B1), ordered.lastLocation());
		assertEquals(List.of(), onto.applied());

		assertApplied("", drop(_tree, dropAt(_tree, A1, C, DropZone.MIDDLE)));
		assertEquals("The refused insertion falls through to the drop onto the node.",
			new DropLocation.Onto(C), onto.lastLocation());

		probe("drag1", "intoB", dropAt(_tree, A1, B, DropZone.MIDDLE));
		probe("drag1", "ontoC", dropAt(_tree, A1, C, DropZone.MIDDLE));
		assertMarker(verdicts().get("intoB"), DropMarker.INTO, nodeId(B));
		assertMarker(verdicts().get("ontoC"), DropMarker.INTO, nodeId(C));

		Operation ontoFirst = new Operation(DropMode.ONTO);
		Operation orderedSecond = new Operation(DropMode.ORDERED);
		_tree.setDropTarget(new DropOperations(AcceptedKinds.ANY, ontoFirst, orderedSecond));
		assertApplied("", drop(_tree, dropAt(_tree, A1, B, DropZone.UPPER)));
		assertEquals(new DropLocation.Onto(B), ontoFirst.lastLocation());
		assertEquals(List.of(), orderedSecond.applied());
		assertApplied("", drop(_tree, dropAt(_tree, A1, null, DropZone.NONE)));
		assertEquals("Beside the nodes, only the insertion has a location.",
			new DropLocation.Insert(ROOT, null), orderedSecond.lastLocation());
	}

	/**
	 * Dragging a selected node drags the whole selection, including a selected node hidden in a
	 * collapsed subtree; a node the draggable predicate refuses offers no drag.
	 */
	public void testDragSource() {
		Operation onto = new Operation(DropMode.ONTO);
		_tree.setDropTarget(new DropOperations(AcceptedKinds.ANY, onto));
		expand(B);
		select(A1, false);
		select(B1, true);
		collapse(B);

		Map<String, Object> ofSelection = dropAt(_tree, A1, C, DropZone.MIDDLE);
		ofSelection.put(DropArguments.SELECTION, Boolean.TRUE);
		assertApplied("", drop(_tree, ofSelection));
		assertEquals(List.of(A1, B1), onto.applied().get(0).objects());

		_tree.setDragSource(ITEM, object -> !A2.equals(object));
		assertEquals(Boolean.TRUE, nodeState(A1).get("draggable"));
		assertEquals(Boolean.FALSE, nodeState(A2).get("draggable"));
		assertRefused("A refused node cannot be dragged.", drop(_tree, dropAt(_tree, A2, C, DropZone.MIDDLE)));

		_tree.setDragEnabled(false);
		assertRefused("A tree with dragging switched off is no source.",
			drop(_tree, dropAt(_tree, A1, C, DropZone.MIDDLE)));
	}

	/**
	 * A node dragged out of a tree is dropped onto a table row, and a table row dragged out of a
	 * table is inserted into a tree: each drop resolves the other control as its source.
	 */
	public void testDragBetweenTreeAndTable() {
		TableViewControl<String> table = stringTable(List.of("t1", "t2"));
		Operation ontoRow = new Operation(DropMode.ONTO);
		table.setDropTarget(new DropOperations(AcceptedKinds.of(List.of(ITEM)), ontoRow));
		table.setDragSource(ITEM);

		Map<String, Object> treeToTable = new HashMap<>();
		treeToTable.put(DropArguments.SOURCE, _tree.getID());
		treeToTable.put(DropArguments.KEYS, nodeId(A2));
		treeToTable.put(DropArguments.SELECTION, Boolean.FALSE);
		treeToTable.put(DropArguments.TARGET_KEY, "row_1");
		treeToTable.put(DropArguments.ZONE, DropZone.MIDDLE.wireName());
		assertApplied("A node is dropped onto a table row.", table.executeClientCommand(DropSupport.CMD_DROP, treeToTable));
		assertEquals(List.of(A2), ontoRow.applied().get(0).objects());
		assertSame(_tree, ontoRow.applied().get(0).source());
		assertEquals(new DropLocation.Onto("t2"), ontoRow.lastLocation());

		Operation ordered = new Operation(DropMode.ORDERED);
		_tree.setDropTarget(new DropOperations(AcceptedKinds.of(List.of(ITEM)), ordered));
		Map<String, Object> tableToTree = new HashMap<>();
		tableToTree.put(DropArguments.SOURCE, table.getID());
		tableToTree.put(DropArguments.KEYS, "row_0");
		tableToTree.put(DropArguments.SELECTION, Boolean.FALSE);
		tableToTree.put(DropArguments.TARGET_KEY, nodeId(A1));
		tableToTree.put(DropArguments.ZONE, DropZone.LOWER.wireName());
		assertApplied("A table row is inserted into the tree.", drop(_tree, tableToTree));
		assertEquals(List.of("t1"), ordered.applied().get(0).objects());
		assertSame(table, ordered.applied().get(0).source());
		assertEquals(new DropLocation.Insert(A, A2), ordered.lastLocation());
	}

	/**
	 * A recorded insertion names the parent and the object inserted before by their business
	 * identity, and its replay offers exactly that location to the same operation - also for an
	 * insertion into a collapsed node and among the top-level nodes.
	 */
	public void testRecordingRoundTrip() {
		Operation ordered = new Operation(DropMode.ORDERED);
		_tree.setDropTarget(new DropOperations(AcceptedKinds.of(List.of(ITEM)), ordered));

		DropObjectsArguments lower = record(dropAt(_tree, C, A, DropZone.LOWER));
		assertEquals(DropMode.ORDERED.wireName(), lower.getMode());
		assertNotNull(lower.getParent());
		assertNotNull(lower.getBefore());
		assertEquals("Recording applies nothing.", List.of(), ordered.applied());
		assertEquals(new DropLocation.Insert(A, A1), replay(ordered, lower).location());
		assertEquals(List.of(C), ordered.applied().get(0).objects());
		assertNull("A replayed drop names no source control.", ordered.applied().get(0).source());
		assertEquals(ITEM, ordered.applied().get(0).kind());

		DropObjectsArguments intoCollapsed = record(dropAt(_tree, C, B, DropZone.MIDDLE));
		assertEquals(new DropLocation.Insert(B, B1), replay(ordered, intoCollapsed).location());

		DropObjectsArguments beside = record(dropAt(_tree, C, null, DropZone.NONE));
		assertNull(beside.getBefore());
		assertEquals(new DropLocation.Insert(ROOT, null), replay(ordered, beside).location());
	}

	/**
	 * A recorded drop whose location the tree does not display - an object inserted before that is
	 * no child of the recorded parent - fails as a drift rather than being applied somewhere else.
	 */
	public void testReplayOfAMisplacedLocationFails() {
		Operation ordered = new Operation(DropMode.ORDERED);
		_tree.setDropTarget(new DropOperations(AcceptedKinds.of(List.of(ITEM)), ordered));
		DropObjectsArguments recorded = record(dropAt(_tree, C, A, DropZone.LOWER));
		recorded.setParent(record(dropAt(_tree, C, B, DropZone.MIDDLE)).getParent());

		HandlerResult result = new InteractionWithoutSession().runWithContext(
			() -> _tree.executeClientCommand(DropSupport.CMD_DROP_OBJECTS, ReactCommands.arguments(recorded)));
		assertFalse("A drop before a child of another parent must not replay.", result.isSuccess());
		assertEquals(ErrorSeverity.ERROR, result.getErrorSeverity());
		assertEquals(List.of(), ordered.applied());
	}

	/** The probe is technical: it is never recorded. */
	public void testProbeIsNotRecorded() {
		assertFalse(_tree.isRecordable(DropSupport.CMD_DROP_PROBE));
		assertTrue(_tree.isRecordable(DropSupport.CMD_DROP));
	}

	private DropObjectsArguments record(Map<String, Object> arguments) {
		RecordedCommand recorded = _tree.recordCommand(DropSupport.CMD_DROP, arguments);
		assertNotNull("The drop must be recorded.", recorded);
		assertTrue("The drop is recorded in replay-stable form.",
			recorded.command() instanceof DropObjectsArguments);
		return (DropObjectsArguments) recorded.command();
	}

	/** Replays the given recorded drop, and returns the event the given operation applied. */
	private DropEvent replay(Operation ordered, DropObjectsArguments recorded) {
		int before = ordered.applied().size();
		HandlerResult result = new InteractionWithoutSession().runWithContext(
			() -> _tree.executeClientCommand(DropSupport.CMD_DROP_OBJECTS, ReactCommands.arguments(recorded)));
		assertApplied("The recorded drop must replay.", result);
		assertEquals(before + 1, ordered.applied().size());
		return ordered.applied().get(before);
	}

	private HandlerResult drop(ReactTreeControl tree, Map<String, Object> arguments) {
		return tree.executeClientCommand(DropSupport.CMD_DROP, arguments);
	}

	private HandlerResult probe(String drag, String probe, Map<String, Object> drop) {
		Map<String, Object> arguments = new HashMap<>(drop);
		arguments.put(DropProbeArguments.DRAG, drag);
		arguments.put(DropProbeArguments.PROBE, probe);
		HandlerResult result = _tree.executeClientCommand(DropSupport.CMD_DROP_PROBE, arguments);
		assertApplied("A probe never fails.", result);
		return result;
	}

	/**
	 * The arguments of a drop of the node of the given object of the test tree in the given zone of
	 * the node of the given target object, beside the nodes for a {@code null} target.
	 */
	private Map<String, Object> dropAt(ReactTreeControl source, Object dragged, Object target, DropZone zone) {
		Map<String, Object> arguments = new HashMap<>();
		arguments.put(DropArguments.SOURCE, source.getID());
		arguments.put(DropArguments.KEYS, nodeId(dragged));
		arguments.put(DropArguments.SELECTION, Boolean.FALSE);
		if (target != null) {
			arguments.put(DropArguments.TARGET_KEY, nodeId(target));
		}
		arguments.put(DropArguments.ZONE, zone.wireName());
		return arguments;
	}

	private void expand(Object businessObject) {
		_tree.executeCommand(ReactTreeControl.EXPAND_COMMAND, Map.of(ExpandNodeArguments.NODE_ID, nodeId(businessObject)));
	}

	private void collapse(Object businessObject) {
		_tree.executeCommand(ReactTreeControl.COLLAPSE_COMMAND,
			Map.of(CollapseNodeArguments.NODE_ID, nodeId(businessObject)));
	}

	private void select(Object businessObject, boolean ctrlKey) {
		Map<String, Object> arguments = new LinkedHashMap<>();
		arguments.put(SelectNodeArguments.NODE_ID, nodeId(businessObject));
		arguments.put(SelectNodeArguments.CTRL_KEY, Boolean.valueOf(ctrlKey));
		arguments.put(SelectNodeArguments.SHIFT_KEY, Boolean.FALSE);
		_tree.executeCommand(ReactTreeControl.SELECT_COMMAND, arguments);
	}

	/** The state the tree sends for the displayed node of the given business object. */
	@SuppressWarnings("unchecked")
	private Map<String, Object> nodeState(Object businessObject) {
		List<Map<String, Object>> states = (List<Map<String, Object>>) _tree.clientState(ReactTreeControl.NODES);
		List<DefaultTreeUINode> displayed = displayedNodes();
		assertEquals("The tree displays a node state per displayed node.", displayed.size(), states.size());
		for (int n = 0; n < displayed.size(); n++) {
			if (businessObject.equals(displayed.get(n).getBusinessObject())) {
				return states.get(n);
			}
		}
		throw new IllegalArgumentException("The tree displays no node for '" + businessObject + "'.");
	}

	/** The id of the node displaying the given business object, as the client knows it. */
	private String nodeId(Object businessObject) {
		return (String) nodeState(businessObject).get(ReactTreeControl.NODE_ID);
	}

	/** The nodes of the test tree in display order. */
	private List<DefaultTreeUINode> displayedNodes() {
		List<DefaultTreeUINode> result = new ArrayList<>();
		DefaultTreeUINode root = _model.getRoot();
		if (_model.isRootVisible()) {
			result.add(root);
		}
		if (!_model.isRootVisible() || _model.isExpanded(root)) {
			addDisplayed(result, root);
		}
		return result;
	}

	private void addDisplayed(List<DefaultTreeUINode> result, DefaultTreeUINode parent) {
		for (DefaultTreeUINode child : parent.getChildren()) {
			result.add(child);
			if (_model.isExpanded(child)) {
				addDisplayed(result, child);
			}
		}
	}

	@SuppressWarnings("unchecked")
	private Map<String, Map<String, Object>> verdicts() {
		return (Map<String, Map<String, Object>>) _tree.clientState(DropSupport.DROP_VERDICTS);
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

	private TableViewControl<String> stringTable(List<String> rows) {
		List<Column<String, ?>> columns = List.of(DefaultColumn.<String, String> builder("name", value -> value).build());
		TableViewControl<String> result = new TableViewControl<>(_context,
			DefaultTableView.create(columns, new ListRowSource<>(rows, columns)), false);
		result.attach();
		return result;
	}

	/**
	 * The builder computing the children of a node from the structure the test holds.
	 */
	private TreeBuilder<DefaultTreeUINode> builder() {
		return new TreeBuilder<>() {
			@Override
			public DefaultTreeUINode createNode(AbstractMutableTLTreeModel<DefaultTreeUINode> model,
					DefaultTreeUINode parent, Object userObject) {
				return new DefaultTreeUINode(model, parent, userObject);
			}

			@Override
			public List<DefaultTreeUINode> createChildList(DefaultTreeUINode node) {
				List<DefaultTreeUINode> children = new ArrayList<>();
				for (Object child : _children.getOrDefault(node.getBusinessObject(), List.of())) {
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

	/**
	 * The test suite, started with the resource bundles a refused drop's message needs, and the
	 * {@link ModelResolver} a recorded drop names its objects with.
	 */
	public static Test suite() {
		return ModuleTestSetup.setupModule(
			ServiceTestSetup.createSetup(TestReactTreeControlDragDrop.class, ResourcesModule.Module.INSTANCE,
				ModelResolver.Module.INSTANCE));
	}

}
