/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.react.control.tree;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.layout.react.control.InteractionWithoutSession;

import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ModelKeyArguments;
import com.top_logic.layout.react.control.ReactCommand;
import com.top_logic.layout.react.control.ReactCommands;
import com.top_logic.layout.react.control.RecordedCommand;
import com.top_logic.layout.react.control.ScriptingModelKey;
import com.top_logic.layout.react.control.common.ReactTextControl;
import com.top_logic.layout.react.control.selection.SelectByKeysArguments;
import com.top_logic.layout.react.control.tree.ContextMenuArguments;
import com.top_logic.layout.react.control.tree.ContextMenuByKeyArguments;
import com.top_logic.layout.react.control.tree.ReactTreeControl;
import com.top_logic.layout.react.control.tree.SelectNodeArguments;
import com.top_logic.layout.react.control.tree.SelectNodeByKeyArguments;
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
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * Tests that a {@link ReactTreeControl} records the gestures on its nodes by the business identity
 * of the nodes' objects, so that a recording replays in another tree - whose nodes the client knows
 * by other ids, as in a fresh session.
 *
 * <p>
 * Both trees hold below their hidden root the objects {@link #A} (with the children {@link #A1},
 * {@link #A2}), {@link #B} (with the child {@link #B1}) and {@link #C}, all collapsed.
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

	private static final Map<Object, List<Object>> CHILDREN =
		Map.of(ROOT, List.of(A, B, C), A, List.of(A1, A2), B, List.of(B1));

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

		TestTree() {
			ReactContext context =
				new DefaultReactContext("", "test", new SSEUpdateQueue(), new ReactWindowRegistry("test"));
			_model = new DefaultTreeUINodeModel(builder(), ROOT);
			_model.setRootVisible(false);
			_selection = new DefaultMultiSelectionModel<>(SelectionModelOwner.NO_OWNER);
			_control = new Tree(context, _model, _selection);
			_control.attach();
			_control.setSelectionMode(SelectionMode.MULTI);
			_control.setContextMenuProvider((tree, node, x, y) -> _contextMenus.add(businessObject(node)));
			_control.setActivationHandler(node -> {
				_activated.add(businessObject(node));
				return HandlerResult.DEFAULT_RESULT;
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

		boolean isExpanded(Object businessObject) {
			return _model.isExpanded(node(businessObject));
		}

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

		/** The projected nodes of the headless state. */
		@SuppressWarnings("unchecked")
		List<Map<String, Object>> projectedNodes() {
			return (List<Map<String, Object>>) _control.scriptingScalarState().get(ReactTreeControl.NODES);
		}

		/** The id of the displayed node of the given business object, as the client knows it. */
		@SuppressWarnings("unchecked")
		String nodeId(Object businessObject) {
			List<Map<String, Object>> states =
				(List<Map<String, Object>>) _control.clientState(ReactTreeControl.NODES);
			List<Object> displayed = displayed();
			for (int n = 0; n < displayed.size(); n++) {
				if (businessObject.equals(displayed.get(n))) {
					return (String) states.get(n).get(ReactTreeControl.NODE_ID);
				}
			}
			throw new IllegalArgumentException("Not displayed: " + businessObject);
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

		/** Records the given gesture on the node of the given object and applies it. */
		RecordedCommand gesture(String command, Object businessObject, Map<String, Object> extra) {
			Map<String, Object> arguments = new LinkedHashMap<>(extra);
			arguments.put(SelectNodeArguments.NODE_ID, nodeId(businessObject));
			RecordedCommand recorded = _control.recordCommand(command, arguments);
			assertNotNull(recorded);
			_control.executeClientCommand(command, arguments);
			return recorded;
		}

		RecordedCommand click(Object businessObject, boolean ctrl, boolean shift) {
			return gesture(ReactTreeControl.SELECT_COMMAND, businessObject,
				Map.of(SelectNodeArguments.CTRL_KEY, Boolean.valueOf(ctrl),
					SelectNodeArguments.SHIFT_KEY, Boolean.valueOf(shift)));
		}

		RecordedCommand expand(Object businessObject) {
			return gesture(ReactTreeControl.EXPAND_COMMAND, businessObject, Map.of());
		}

		HandlerResult replay(ReactCommand command) {
			return new InteractionWithoutSession().runWithContext(
				() -> _control.executeClientCommand(command.getName(), ReactCommands.arguments(command)));
		}

		void assertReplayed(RecordedCommand recorded) {
			HandlerResult result = replay(recorded.command());
			assertTrue("Replay failed: " + result.getEncodedErrors(), result.isSuccess());
		}
	}

	private TestTree _recording;

	private TestTree _replay;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_recording = new TestTree();
		_replay = new TestTree();
	}

	/** The headless projection names each displayed node by the business identity of its object. */
	public void testProjectionCarriesKeys() {
		_recording.expand(A);
		List<Map<String, Object>> nodes = _recording.projectedNodes();
		assertEquals(5, nodes.size());
		Map<String, Object> a1 = nodes.get(1);
		assertEquals(ScriptingModelKey.toKey(null, A1), a1.get(ReactTreeControl.NODE_KEY));
		assertFalse("A named node carries no session-bound id.", a1.containsKey(ReactTreeControl.NODE_ID));
		assertEquals(Integer.valueOf(1), a1.get("depth"));
		assertEquals(Boolean.TRUE, nodes.get(0).get("expanded"));
	}

	/** Expanding, collapsing and a plain click are recorded by the key of the node. */
	public void testExpandCollapseAndSelectAreRecordedByKey() {
		RecordedCommand expand = _recording.expand(B);
		assertEquals(ReactTreeControl.EXPAND_BY_KEY_COMMAND, expand.command().getName());
		assertKey(B, expand);
		_replay.assertReplayed(expand);
		assertTrue(_replay.isExpanded(B));

		RecordedCommand select = _recording.click(B1, false, false);
		assertTrue(select.command() instanceof SelectNodeByKeyArguments);
		assertKey(B1, select);
		_replay.assertReplayed(select);
		assertEquals(Set.of(B1), _replay.selection());

		RecordedCommand collapse = _recording.gesture(ReactTreeControl.COLLAPSE_COMMAND, B, Map.of());
		assertEquals(ReactTreeControl.COLLAPSE_BY_KEY_COMMAND, collapse.command().getName());
		_replay.assertReplayed(collapse);
		assertFalse(_replay.isExpanded(B));
	}

	/** An activation and a context menu are recorded by the key of the node. */
	public void testActivationAndContextMenuAreRecordedByKey() {
		RecordedCommand activate = _recording.gesture(ReactTreeControl.ACTIVATE_COMMAND, C, Map.of());
		assertEquals(ReactTreeControl.ACTIVATE_BY_KEY_COMMAND, activate.command().getName());
		_replay.assertReplayed(activate);
		assertEquals(List.of(C), _replay._activated);
		assertEquals(Set.of(C), _replay.selection());

		RecordedCommand menu = _recording.gesture(ReactTreeControl.CONTEXT_MENU_COMMAND, A,
			Map.of(ContextMenuArguments.X, Integer.valueOf(10), ContextMenuArguments.Y, Integer.valueOf(20)));
		assertEquals(ReactTreeControl.CONTEXT_MENU_BY_KEY_COMMAND, menu.command().getName());
		assertEquals(20, ((ContextMenuByKeyArguments) menu.command()).getY());
		_replay.assertReplayed(menu);
		assertEquals(List.of(A), _replay._contextMenus);
	}

	/**
	 * A click with {@code Ctrl} or {@code Shift} is recorded as the selection it results in, which
	 * replays without the anchor of the recording tree.
	 */
	public void testMultiSelectionIsRecordedAsResultingSelection() {
		_replay.assertReplayed(_recording.expand(A));
		_replay.assertReplayed(_recording.click(A1, false, false));

		RecordedCommand toggle = _recording.click(C, true, false);
		assertEquals(ReactTreeControl.SELECT_BY_KEYS_COMMAND, toggle.command().getName());
		assertTrue("Consecutive multi-selections coalesce into one step.", toggle.coalescing());
		assertKeys(List.of(A1, C), toggle);
		_replay.assertReplayed(toggle);
		assertEquals(Set.of(A1, C), _replay.selection());

		RecordedCommand range = _recording.click(A2, false, true);
		assertEquals(Set.of(A1, A2, B, C), _recording.selection());
		_replay.assertReplayed(range);
		assertEquals(Set.of(A1, A2, B, C), _replay.selection());
	}

	/**
	 * A recorded gesture whose key names no displayed node - or, for a selection, no node of the
	 * tree - fails with its message.
	 */
	public void testUnresolvedKeysFail() {
		SelectNodeByKeyArguments hidden = TypedConfiguration.newConfigItem(SelectNodeByKeyArguments.class);
		hidden.setName(ReactTreeControl.SELECT_BY_KEY_COMMAND);
		hidden.setKey(name(B1));
		assertFalse("A node in a collapsed subtree is not displayed.", _replay.replay(hidden).isSuccess());

		SelectByKeysArguments unknown = TypedConfiguration.newConfigItem(SelectByKeysArguments.class);
		unknown.setName(ReactTreeControl.SELECT_BY_KEYS_COMMAND);
		unknown.getKeys().add(name(A));
		unknown.getKeys().add(name("x"));
		assertFalse(_replay.replay(unknown).isSuccess());
		assertEquals("A failed replay changes nothing.", Set.of(), _replay.selection());
	}

	private static void assertKey(Object expected, RecordedCommand recorded) {
		assertEquals(ScriptingModelKey.toJson(name(expected)),
			ScriptingModelKey.toJson(((ModelKeyArguments) recorded.command()).getKey()));
	}

	private static void assertKeys(List<Object> expected, RecordedCommand recorded) {
		List<String> actual = new ArrayList<>();
		for (ModelName key : ((SelectByKeysArguments) recorded.command()).getKeys()) {
			actual.add(ScriptingModelKey.toJson(key));
		}
		List<String> expectedJson = new ArrayList<>();
		for (Object object : expected) {
			expectedJson.add(ScriptingModelKey.toJson(name(object)));
		}
		assertEquals(expectedJson, actual);
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
	 * The test suite, started with the resource bundles a failure message needs, and the
	 * {@link ModelResolver} the recorded commands name the nodes with.
	 */
	public static Test suite() {
		return ModuleTestSetup.setupModule(
			ServiceTestSetup.createSetup(TestReactTreeControlRecording.class, ResourcesModule.Module.INSTANCE,
				ModelResolver.Module.INSTANCE));
	}

}
