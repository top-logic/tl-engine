/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.react.flow.server.control;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import junit.framework.TestCase;

import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.react.flow.data.Diagram;
import com.top_logic.react.flow.data.SelectableBox;
import com.top_logic.react.flow.data.Text;
import com.top_logic.react.flow.data.VerticalLayout;
import com.top_logic.react.flow.data.Widget;
import com.top_logic.react.flow.operations.SelectionUtil;
import com.top_logic.react.flow.operations.WidgetTraversal;
import com.top_logic.react.flow.server.control.FlowDiagramControl;

import de.haumacher.msgbuf.graph.DefaultScope;
import de.haumacher.msgbuf.io.StringR;
import de.haumacher.msgbuf.io.StringW;
import de.haumacher.msgbuf.json.JsonReader;
import de.haumacher.msgbuf.json.JsonWriter;

/**
 * Tests for the way a {@link FlowDiagramControl} shares its {@link Diagram} with the client: the
 * serialization the client mounts on, and the patches exchanged afterwards.
 */
public class TestFlowDiagramControl extends TestCase {

	/** A node of the diagram under test. */
	private static final String NODE_A = "a";

	/** Another node of the diagram under test. */
	private static final String NODE_B = "b";

	private DiagramControl _control;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		ReactContext context =
			new DefaultReactContext("", "test", new SSEUpdateQueue(), new ReactWindowRegistry("test"));
		_control = new DiagramControl(context, diagram(NODE_A, NODE_B));
	}

	/**
	 * Tests that a patch of the diagram does not stay in the control's state: a full state sent
	 * later - when the client connects again - would apply it once more to the diagram the client
	 * already changed with it.
	 */
	public void testPatchIsNoPartOfTheState() {
		_control.render();

		_control.selectUserObjects(Set.of(NODE_A));
		_control.selectUserObjects(Set.of());

		assertNull("A full state must not carry a patch the client applied already.",
			_control.diagramPatch());
	}

	/**
	 * Tests that an update the client made for a diagram the control has replaced in the meantime
	 * changes nothing in the displayed one, even though both diagrams have the same structure.
	 */
	public void testUpdateForReplacedDiagramIsDropped() throws IOException {
		String patchForOldDiagram = clientSelectionPatch(_control.render(), NODE_A);

		// The same structure, the nodes in the other order: the node at the position of the one the
		// client selected carries another user object now.
		_control.setModel(diagram(NODE_B, NODE_A));
		_control.render();

		_control.executeClientCommand(FlowDiagramControl.CMD_UPDATE,
			Map.of(FlowDiagramControl.ARG_PATCH, patchForOldDiagram));

		assertEquals("The patch names elements of a diagram no longer displayed.",
			Set.of(), _control.getSelectedUserObjects());
	}

	/**
	 * Tests that an update the client made for the displayed diagram is applied after the diagram
	 * was serialized again.
	 */
	public void testUpdateForCurrentDiagramIsApplied() throws IOException {
		_control.render();
		_control.setModel(diagram(NODE_B, NODE_A));
		String patch = clientSelectionPatch(_control.render(), NODE_A);

		_control.executeClientCommand(FlowDiagramControl.CMD_UPDATE,
			Map.of(FlowDiagramControl.ARG_PATCH, patch));

		assertEquals(Set.of(NODE_A), _control.getSelectedUserObjects());
	}

	/**
	 * Tests that serializing the displayed diagram again keeps the IDs of its elements: the client
	 * holding the earlier serialization ignores an identical one, and an update it made against the
	 * earlier one still names the same elements.
	 */
	public void testSerializingAgainKeepsTheIds() throws IOException {
		String first = _control.render();
		String patch = clientSelectionPatch(first, NODE_B);

		assertEquals("The unchanged diagram is serialized the same way.", first, _control.render());

		_control.executeClientCommand(FlowDiagramControl.CMD_UPDATE,
			Map.of(FlowDiagramControl.ARG_PATCH, patch));

		assertEquals(Set.of(NODE_B), _control.getSelectedUserObjects());
	}

	/**
	 * Tests that the full state of the control describes the diagram including the changes sent
	 * only as patches: a client receiving the full state instead of the patches it missed displays
	 * the diagram the server holds.
	 */
	public void testFullStateContainsPatchedChanges() throws IOException {
		String first = _control.render();

		_control.selectUserObjects(Set.of(NODE_A));

		String full = _control.storedDiagram();
		assertEquals("A change sent as patch is part of the full state.",
			Set.of(NODE_A), markedCssClasses(full));
		assertEquals("The full state names the elements by the IDs the client knows.",
			diagramId(first), diagramId(full));

		_control.executeClientCommand(FlowDiagramControl.CMD_UPDATE,
			Map.of(FlowDiagramControl.ARG_PATCH, clientSelectionPatch(full, NODE_B)));

		assertEquals(Set.of(NODE_B), _control.getSelectedUserObjects());
		assertEquals("A change made by the client is part of the full state.",
			Set.of(NODE_B), markedCssClasses(_control.storedDiagram()));
	}

	/**
	 * The ID the given serialized diagram assigns to the diagram itself.
	 */
	private static int diagramId(String diagramJson) throws IOException {
		DefaultScope scope = new DefaultScope(2, 1);
		Diagram diagram = Diagram.readDiagram(scope, new JsonReader(new StringR(diagramJson)));
		return scope.id(diagram);
	}

	/**
	 * The CSS classes of the nodes the given serialized diagram displays as selected, read the way
	 * the client mounts on it.
	 */
	private static Set<String> markedCssClasses(String diagramJson) throws IOException {
		Diagram diagram = Diagram.readDiagram(new DefaultScope(2, 1), new JsonReader(new StringR(diagramJson)));
		Set<String> result = new HashSet<>();
		WidgetTraversal.visitAll(diagram, widget -> {
			if (SelectionUtil.isSelected(widget) && diagram.getSelection().contains(widget)) {
				result.add(widget.getCssClass());
			}
		});
		return result;
	}

	/**
	 * The patch the client sends after selecting the node with the given user object in the given
	 * serialized diagram.
	 */
	private static String clientSelectionPatch(String diagramJson, String userObject) throws IOException {
		DefaultScope clientScope = new DefaultScope(2, 1);
		Diagram clientDiagram = Diagram.readDiagram(clientScope, new JsonReader(new StringR(diagramJson)));

		// The user object never reaches the client, it knows the node by its CSS class.
		Widget[] found = { null };
		WidgetTraversal.visitAll(clientDiagram, widget -> {
			if (found[0] == null && userObject.equals(widget.getCssClass())) {
				found[0] = widget;
			}
		});
		Widget node = found[0];
		assertNotNull("No node for '" + userObject + "' in the served diagram.", node);

		clientDiagram.setIncrementalSelection(false);
		clientDiagram.setSelection(Collections.singletonList(node));
		SelectionUtil.setSelected(node, true);

		StringW patch = new StringW();
		clientScope.createPatch(new JsonWriter(patch));
		return patch.toString();
	}

	/**
	 * A diagram of one selectable node per given user object, each node also carrying it as its CSS
	 * class, so that the client can tell the nodes apart.
	 */
	private static Diagram diagram(String... userObjects) {
		VerticalLayout root = VerticalLayout.create();
		for (String userObject : userObjects) {
			root.addContent(SelectableBox.create()
				.setContent(Text.create().setValue(userObject))
				.setCssClass(userObject)
				.setUserObject(userObject));
		}
		return Diagram.create().setRoot(root);
	}

	/**
	 * A {@link FlowDiagramControl} whose state the test reads, as the client receives it.
	 */
	private static class DiagramControl extends FlowDiagramControl {

		/** State key the serialized diagram is written to. */
		private static final String DIAGRAM_STATE = "diagram";

		/** State key the client reads a patch of the diagram from. */
		private static final String DIAGRAM_PATCH_STATE = "diagramPatch";

		/**
		 * Creates a {@link DiagramControl}.
		 */
		DiagramControl(ReactContext context, Diagram diagram) {
			super(context, diagram);
		}

		/**
		 * Serializes the diagram the way rendering does and answers what the client is served.
		 */
		String render() {
			onBeforeWrite();
			return (String) getState(DIAGRAM_STATE);
		}

		/**
		 * The serialized diagram the control's state holds, as a full state of the control
		 * carries it.
		 */
		String storedDiagram() {
			return (String) getState(DIAGRAM_STATE);
		}

		/**
		 * The patch of the diagram the control's state holds.
		 */
		Object diagramPatch() {
			return getState(DIAGRAM_PATCH_STATE);
		}
	}

}
