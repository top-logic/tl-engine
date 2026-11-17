/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.react.control.tree;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.I18NConstants;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactCommand;
import com.top_logic.layout.react.control.ScriptingModelKey;
import com.top_logic.layout.react.control.common.ReactTextControl;
import com.top_logic.layout.react.control.identity.ObjectIdTranslation;
import com.top_logic.layout.react.control.identity.ProjectionMode;
import com.top_logic.layout.react.control.tree.ActivateNodeArguments;
import com.top_logic.layout.react.control.tree.CollapseNodeArguments;
import com.top_logic.layout.react.control.tree.ContextMenuArguments;
import com.top_logic.layout.react.control.tree.ExpandNodeArguments;
import com.top_logic.layout.react.control.tree.ReactTreeControl;
import com.top_logic.layout.react.control.tree.SelectNodeArguments;
import com.top_logic.layout.react.scripting.ScriptRecorder;
import com.top_logic.layout.react.scripting.ScriptingSession;
import com.top_logic.layout.react.scripting.ScriptingTreeProjector;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.scripting.recorder.ref.ModelName;
import com.top_logic.layout.scripting.recorder.ref.ModelResolver;
import com.top_logic.layout.tree.model.AbstractMutableTLTreeModel;
import com.top_logic.layout.tree.model.DefaultTreeUINodeModel;
import com.top_logic.layout.tree.model.DefaultTreeUINodeModel.DefaultTreeUINode;
import com.top_logic.layout.tree.model.TreeBuilder;
import com.top_logic.mig.html.DefaultMultiSelectionModel;
import com.top_logic.mig.html.SelectionModel;
import com.top_logic.mig.html.SelectionModelOwner;
import com.top_logic.table.SelectionMode;
import com.top_logic.util.error.TopLogicException;

/**
 * Tests the node ids of a {@link ReactTreeControl} as object ids: a gesture on a node is recorded
 * with the business key of the node's object in place of the node id, and replays in another tree
 * - whose nodes have other ids, as in a fresh session - on the node displaying that object; a key
 * of an object the tree does not display fails.
 *
 * <p>
 * Both trees hold below their hidden root the objects {@link #A} (with the children {@link #A1},
 * {@link #A2}), {@link #B} (with the children {@link #B1} and, a second time, {@link #A1}) and
 * {@link #C} (with the children {@link #AA} and {@link #BB}, whose hash codes are equal), all
 * collapsed.
 * </p>
 */
public class TestReactTreeControlRecording extends TestCase {

	private static final String ROOT = "root";

	private static final String A = "a";

	private static final String A1 = "a1";

	private static final String A2 = "a2";

	private static final String B = "b";

	private static final String B1 = "b1";

	private static final String C = "c";

	/** An object whose hash code equals the one of {@link #BB}. */
	private static final String AA = "Aa";

	/** An object whose hash code equals the one of {@link #AA}. */
	private static final String BB = "BB";

	private static final Map<Object, List<Object>> CHILDREN =
		Map.of(ROOT, List.of(A, B, C), A, List.of(A1, A2), B, List.of(B1, A1), C, List.of(AA, BB));

	/** A {@link ReactTreeControl} whose client state the test reads. */
	private static final class Tree extends ReactTreeControl {

		Tree(ReactContext context, DefaultTreeUINodeModel model, SelectionModel<?> selection) {
			super(context, model, selection, (ctx, object) -> new ReactTextControl(ctx, String.valueOf(object)));
		}

		Object clientState(String key) {
			return getState(key);
		}

	}

	/** A tree with its model and selection. */
	private static final class TestTree {

		final DefaultTreeUINodeModel _model;

		final SelectionModel<Object> _selection;

		final Tree _control;

		final List<Object> _contextMenus = new ArrayList<>();

		final List<Object> _activated = new ArrayList<>();

		/**
		 * Creates a tree in a session that has allocated the given number of ids before, so that
		 * its node ids differ from those of a tree created with another number.
		 */
		TestTree(int allocated) {
			ReactContext context =
				new DefaultReactContext("", "test", new SSEUpdateQueue(), new ReactWindowRegistry("test"));
			for (int n = 0; n < allocated; n++) {
				context.allocateId();
			}
			_model = new DefaultTreeUINodeModel(builder(), ROOT);
			_model.setRootVisible(false);
			_selection = new DefaultMultiSelectionModel<>(SelectionModelOwner.NO_OWNER);
			_control = new Tree(context, _model, _selection);
			_control.attach();
			_control.setSelectionMode(SelectionMode.MULTI);
			_control.setContextMenuProvider((tree, node, x, y) -> _contextMenus.add(businessObject(node)));
			_control.setActivationHandler(node -> {
				_activated.add(businessObject(node));
			});
		}

		/** The business objects of the selected nodes. */
		Set<Object> selection() {
			Set<Object> result = new LinkedHashSet<>();
			for (Object node : _selection.getSelection()) {
				result.add(businessObject(node));
			}
			return result;
		}

		/** The selected nodes. */
		Set<Object> selectedNodes() {
			return new LinkedHashSet<>(_selection.getSelection());
		}

		boolean isExpanded(Object businessObject) {
			return _model.isExpanded(node(businessObject));
		}

		/** The first node of the given business object, in depth-first order. */
		DefaultTreeUINode node(Object businessObject) {
			return find(_model.getRoot(), businessObject);
		}

		private DefaultTreeUINode find(DefaultTreeUINode node, Object businessObject) {
			if (businessObject.equals(node.getBusinessObject())) {
				return node;
			}
			for (DefaultTreeUINode child : node.getChildren()) {
				DefaultTreeUINode result = find(child, businessObject);
				if (result != null) {
					return result;
				}
			}
			return null;
		}

		/** The projected nodes of the headless state in the given mode. */
		@SuppressWarnings("unchecked")
		List<Map<String, Object>> projectedNodes(ProjectionMode mode) {
			return (List<Map<String, Object>>) ScriptingTreeProjector.nodeState(_control, mode)
				.get(ReactTreeControl.NODES);
		}

		/** The ids of the displayed nodes, in display order, as the client knows them. */
		@SuppressWarnings("unchecked")
		List<String> nodeIds() {
			List<String> result = new ArrayList<>();
			for (Map<String, Object> state : (List<Map<String, Object>>) _control.clientState(ReactTreeControl.NODES)) {
				result.add((String) state.get(ReactTreeControl.NODE_ID));
			}
			return result;
		}

		/** The id of the first displayed node of the given business object, as the client knows it. */
		String nodeId(Object businessObject) {
			return nodeId(businessObject, 0);
		}

		/**
		 * The id of the displayed node of the given business object, as the client knows it.
		 *
		 * @param occurrence
		 *        Which of the displayed nodes of the object, counted from 0 in display order.
		 */
		String nodeId(Object businessObject, int occurrence) {
			List<String> ids = nodeIds();
			List<Object> displayed = displayed();
			int found = 0;
			for (int n = 0; n < displayed.size(); n++) {
				if (businessObject.equals(displayed.get(n)) && found++ == occurrence) {
					return ids.get(n);
				}
			}
			throw new IllegalArgumentException("Not displayed: " + businessObject + "#" + occurrence);
		}

		/** The business objects of the displayed nodes, in display order. */
		List<Object> displayed() {
			List<Object> result = new ArrayList<>();
			addDisplayed(result, _model.getRoot());
			return result;
		}

		private void addDisplayed(List<Object> result, DefaultTreeUINode parent) {
			for (DefaultTreeUINode child : parent.getChildren()) {
				result.add(child.getBusinessObject());
				if (_model.isExpanded(child)) {
					addDisplayed(result, child);
				}
			}
		}

		private static Object businessObject(Object node) {
			return ((DefaultTreeUINode) node).getBusinessObject();
		}

		/** Records the given gesture on the given node id as the browser sends it, and applies it. */
		ReactCommand gestureOnId(String command, String nodeId, Map<String, Object> extra) {
			Map<String, Object> arguments = new LinkedHashMap<>(extra);
			arguments.put(SelectNodeArguments.NODE_ID, nodeId);
			ScriptRecorder recorder = new ScriptRecorder();
			recorder.start();
			recorder.capture(_control, _control, command, arguments);
			_control.executeClientCommand(command, arguments);
			recorder.stop();

			List<ReactCommand> steps = recorder.steps();
			assertEquals(1, steps.size());
			return steps.get(0);
		}

		/** Records the given gesture on the first node of the given object and applies it. */
		ReactCommand gesture(String command, Object businessObject, Map<String, Object> extra) {
			return gestureOnId(command, nodeId(businessObject), extra);
		}

		ReactCommand click(Object businessObject, boolean ctrl, boolean shift) {
			return gesture(ReactTreeControl.SELECT_COMMAND, businessObject,
				Map.of(SelectNodeArguments.CTRL_KEY, Boolean.valueOf(ctrl),
					SelectNodeArguments.SHIFT_KEY, Boolean.valueOf(shift)));
		}

		ReactCommand expand(Object businessObject) {
			return gesture(ReactTreeControl.EXPAND_COMMAND, businessObject, Map.of());
		}

		/** Replays the given step, and answers with its failure, if any. */
		TopLogicException replay(ReactCommand step) {
			return CommandFailures.outcome(() -> ScriptingSession.forRoot(_control).replay(step));
		}

		void assertReplayed(ReactCommand step) {
			TopLogicException failure = replay(step);
			assertNull("Replay failed: " + failure, failure);
		}
	}

	private TestTree _recording;

	private TestTree _replay;

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
		_recording = new TestTree(0);
		_replay = new TestTree(1000);
	}

	/** The key projection names each displayed node by the business identity of its object. */
	public void testKeyProjectionCarriesKeys() {
		_recording.expand(A);
		List<Map<String, Object>> nodes = _recording.projectedNodes(ProjectionMode.KEY);
		assertEquals(5, nodes.size());
		Map<String, Object> a1 = nodes.get(1);
		assertEquals(ScriptingModelKey.toKey(null, A1), a1.get(ReactTreeControl.NODE_KEY));
		assertFalse("A named node carries no session-bound id.", a1.containsKey(ReactTreeControl.NODE_ID));
		assertEquals(Integer.valueOf(1), a1.get("depth"));
		assertEquals(Boolean.TRUE, nodes.get(0).get("expanded"));
	}

	/**
	 * The agent projection names each displayed node by the id the commands take it by, with the
	 * label of its object, and a command with such an id acts on the node.
	 */
	public void testAgentProjectionShowsIds() {
		_recording.expand(A);
		List<Map<String, Object>> nodes = _recording.projectedNodes(ProjectionMode.AGENT);
		assertEquals(5, nodes.size());
		assertEquals(_recording.nodeIds(), ids(nodes));
		Map<String, Object> a1 = nodes.get(1);
		assertEquals(A1, a1.get(ScriptingModelKey.LABEL));
		assertFalse("An agent node carries no key.", a1.containsKey(ReactTreeControl.NODE_KEY));

		new InteractionWithoutSession().runWithContext(() -> _recording._control.executeCommand(
			ReactTreeControl.SELECT_COMMAND, Map.of(SelectNodeArguments.NODE_ID, a1.get(ReactTreeControl.NODE_ID))));
		assertEquals(Set.of(A1), _recording.selection());
	}

	/**
	 * Every displayed node has an id of its own - also two nodes of one object and two objects of
	 * equal hash code -, and a node keeps its id while siblings are expanded and collapsed.
	 */
	public void testIdsAreUniqueAndStable() {
		_recording.expand(A);
		_recording.expand(B);
		_recording.expand(C);
		List<String> ids = _recording.nodeIds();
		assertEquals(9, ids.size());
		assertEquals("Ids are unique: " + ids, ids.size(), new HashSet<>(ids).size());
		assertEquals(AA.hashCode(), BB.hashCode());
		assertFalse(_recording.nodeId(AA).equals(_recording.nodeId(BB)));
		assertFalse(_recording.nodeId(A1, 0).equals(_recording.nodeId(A1, 1)));

		String c = _recording.nodeId(C);
		String bb = _recording.nodeId(BB);
		_recording.gesture(ReactTreeControl.COLLAPSE_COMMAND, B, Map.of());
		_recording.gesture(ReactTreeControl.COLLAPSE_COMMAND, A, Map.of());
		_recording.expand(A);
		assertEquals(c, _recording.nodeId(C));
		assertEquals(bb, _recording.nodeId(BB));
	}

	/**
	 * Expanding, collapsing and a plain click are recorded with the key of the node's object, and
	 * replay in a tree whose nodes have other ids.
	 */
	public void testExpandCollapseAndSelectAreRecordedByKey() {
		ReactCommand expand = _recording.expand(B);
		assertEquals(ReactTreeControl.EXPAND_COMMAND, expand.getName());
		assertKey(B, ((ExpandNodeArguments) expand).getNodeId());
		assertFalse("The node ids differ between the trees.",
			_replay.nodeIds().contains(_recording.nodeId(B)));
		_replay.assertReplayed(expand);
		assertTrue(_replay.isExpanded(B));

		ReactCommand select = _recording.click(B1, false, false);
		assertEquals(ReactTreeControl.SELECT_COMMAND, select.getName());
		assertKey(B1, ((SelectNodeArguments) select).getNodeId());
		_replay.assertReplayed(select);
		assertEquals(Set.of(B1), _replay.selection());

		ReactCommand collapse = _recording.gesture(ReactTreeControl.COLLAPSE_COMMAND, B, Map.of());
		assertEquals(ReactTreeControl.COLLAPSE_COMMAND, collapse.getName());
		assertKey(B, ((CollapseNodeArguments) collapse).getNodeId());
		_replay.assertReplayed(collapse);
		assertFalse(_replay.isExpanded(B));
	}

	/** An activation and a context menu are recorded with the key of the node's object. */
	public void testActivationAndContextMenuAreRecordedByKey() {
		ReactCommand activate = _recording.gesture(ReactTreeControl.ACTIVATE_COMMAND, C, Map.of());
		assertEquals(ReactTreeControl.ACTIVATE_COMMAND, activate.getName());
		assertKey(C, ((ActivateNodeArguments) activate).getNodeId());
		_replay.assertReplayed(activate);
		assertEquals(List.of(C), _replay._activated);
		assertEquals(Set.of(C), _replay.selection());

		ReactCommand menu = _recording.gesture(ReactTreeControl.CONTEXT_MENU_COMMAND, A,
			Map.of(ContextMenuArguments.X, Integer.valueOf(10), ContextMenuArguments.Y, Integer.valueOf(20)));
		assertEquals(ReactTreeControl.CONTEXT_MENU_COMMAND, menu.getName());
		assertKey(A, ((ContextMenuArguments) menu).getNodeId());
		assertEquals(20, ((ContextMenuArguments) menu).getY());
		_replay.assertReplayed(menu);
		assertEquals(List.of(A), _replay._contextMenus);
	}

	/**
	 * A click with {@code Ctrl} or {@code Shift} is recorded with its modifiers; replaying the
	 * clicks in order reproduces the selection, range anchor included.
	 */
	public void testMultiSelectionReplaysClickByClick() {
		_replay.assertReplayed(_recording.expand(A));
		_replay.assertReplayed(_recording.click(A1, false, false));

		ReactCommand toggle = _recording.click(C, true, false);
		assertTrue(((SelectNodeArguments) toggle).isCtrlKey());
		assertKey(C, ((SelectNodeArguments) toggle).getNodeId());
		_replay.assertReplayed(toggle);
		assertEquals(Set.of(A1, C), _replay.selection());

		ReactCommand range = _recording.click(A2, false, true);
		assertTrue(((SelectNodeArguments) range).isShiftKey());
		assertEquals(Set.of(A1, A2, B, C), _recording.selection());
		_replay.assertReplayed(range);
		assertEquals(Set.of(A1, A2, B, C), _replay.selection());
	}

	/**
	 * A recorded key of a node below a collapsed parent fails with the message that the object is
	 * not displayed; the replay expands nothing and changes no selection.
	 */
	public void testReplayOfNodeUnderCollapsedParentFails() {
		_recording.expand(B);
		ReactCommand select = _recording.click(B1, false, false);

		TopLogicException failure = _replay.replay(select);
		assertNotNull("A node in a collapsed subtree is not displayed.", failure);
		assertEquals(I18NConstants.ERROR_OBJECT_NOT_DISPLAYED__KEY.getKey(), failure.getErrorKey().plain().getKey());
		assertFalse("The replay expands nothing.", _replay.isExpanded(B));
		assertEquals(Set.of(), _replay.selection());
	}

	/**
	 * A key names an object, not a node: a gesture recorded on the second node of an object
	 * replays on the first displayed node of that object.
	 */
	public void testKeyOfObjectShownTwiceResolvesToFirstDisplayedNode() {
		ReactCommand expandA = _recording.expand(A);
		ReactCommand expandB = _recording.expand(B);
		ReactCommand select = _recording.gestureOnId(ReactTreeControl.SELECT_COMMAND, _recording.nodeId(A1, 1),
			Map.of());
		assertKey(A1, ((SelectNodeArguments) select).getNodeId());
		DefaultTreeUINode selected = (DefaultTreeUINode) _recording.selectedNodes().iterator().next();
		assertEquals("The second node of the object is selected.", B, selected.getParent().getBusinessObject());

		_replay.assertReplayed(expandA);
		_replay.assertReplayed(expandB);
		_replay.assertReplayed(select);
		assertEquals(Set.of(_replay.node(A1)), _replay.selectedNodes());
	}

	private static List<Object> ids(List<Map<String, Object>> nodes) {
		List<Object> result = new ArrayList<>();
		for (Map<String, Object> node : nodes) {
			result.add(node.get(ReactTreeControl.NODE_ID));
		}
		return result;
	}

	private static void assertKey(Object expected, String recorded) {
		assertNotNull("A key in place of the node id: " + recorded, ObjectIdTranslation.parseKey(recorded));
		assertEquals(ScriptingModelKey.toJson(name(expected)), recorded);
	}

	private static ModelName name(Object object) {
		return ScriptingModelKey.name(null, object);
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

	/**
	 * The test suite, started with the resource bundles a failure message needs, the type index
	 * the recorder binds the commands with, and the {@link ModelResolver} the recorded commands name
	 * the nodes with.
	 */
	public static Test suite() {
		return ModuleTestSetup.setupModule(
			ServiceTestSetup.createSetup(TestReactTreeControlRecording.class, TypeIndex.Module.INSTANCE,
				ThreadContextManager.Module.INSTANCE, ResourcesModule.Module.INSTANCE, ModelResolver.Module.INSTANCE));
	}

}
