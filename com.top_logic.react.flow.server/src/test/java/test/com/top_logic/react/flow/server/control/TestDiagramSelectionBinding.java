/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.react.flow.server.control;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

import junit.framework.TestCase;

import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.view.channel.DefaultViewChannel;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.react.flow.data.Diagram;
import com.top_logic.react.flow.data.SelectableBox;
import com.top_logic.react.flow.data.Text;
import com.top_logic.react.flow.data.VerticalLayout;
import com.top_logic.react.flow.data.Widget;
import com.top_logic.react.flow.operations.SelectionUtil;
import com.top_logic.react.flow.operations.WidgetTraversal;
import com.top_logic.react.flow.server.control.DiagramSelectionBinding;
import com.top_logic.react.flow.server.control.FlowDiagramControl;

import de.haumacher.msgbuf.graph.DefaultScope;
import de.haumacher.msgbuf.io.StringR;
import de.haumacher.msgbuf.io.StringW;
import de.haumacher.msgbuf.json.JsonReader;
import de.haumacher.msgbuf.json.JsonWriter;

/**
 * Tests for {@link DiagramSelectionBinding}.
 *
 * <p>
 * The diagram is one selector among several on a shared selection channel: it marks the nodes
 * carrying the value as user object, it writes what the user picks in it, and it leaves a value it
 * has no node for to whoever can display it.
 * </p>
 */
public class TestDiagramSelectionBinding extends TestCase {

	/** A node of the diagram under test. */
	private static final String NODE_A = "a";

	/** Another node of the diagram under test. */
	private static final String NODE_B = "b";

	/** An object no node of the diagram carries. */
	private static final String ELSEWHERE = "elsewhere";

	private ReactContext _context;

	private ViewChannel _channel;

	/** The values the channel was notified about, in order. */
	private List<Object> _notifiedValues;

	private DiagramControl _control;

	private DiagramSelectionBinding _binding;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		_context = new DefaultReactContext("", "test", new SSEUpdateQueue(), new ReactWindowRegistry("test"));
		_channel = new DefaultViewChannel("selection");
		_notifiedValues = new ArrayList<>();
		_channel.addListener((sender, oldValue, newValue) -> _notifiedValues.add(newValue));

		bind(diagram(false, NODE_A, NODE_B));
	}

	/**
	 * Tests that a value written to the channel from outside marks the node carrying it, and that
	 * the diagram does not write the value back.
	 */
	public void testChannelValueMarksTheNodeCarryingIt() {
		_channel.set(NODE_A);

		assertEquals(Set.of(NODE_A), selectedUserObjects());
		assertTrue("The node itself carries the marking the client draws.", isMarked(NODE_A));
		assertFalse(isMarked(NODE_B));
		assertEquals("The value is the selection, and the diagram only displays it.",
			NODE_A, _channel.get());
		assertEquals("Displaying a value is not a selection made in the diagram.",
			List.of(NODE_A), _notifiedValues);
	}

	/**
	 * Tests that a value no node carries marks nothing and stays on the channel - it is the
	 * selection of whoever wrote it.
	 */
	public void testForeignValueMarksNothingAndIsKept() {
		_channel.set(ELSEWHERE);

		assertEquals(Set.of(), selectedUserObjects());
		assertFalse(isMarked(NODE_A));
		assertFalse(isMarked(NODE_B));
		assertEquals(ELSEWHERE, _channel.get());
		assertEquals(List.of(ELSEWHERE), _notifiedValues);
	}

	/**
	 * Tests that a diagram showing one selected node at a time cannot display a set - not even one
	 * naming its own nodes - and does not take it from whoever can.
	 */
	public void testSingleSelectDiagramIgnoresASet() {
		Set<String> value = Set.of(NODE_A, NODE_B);
		_channel.set(value);

		assertEquals(Set.of(), selectedUserObjects());
		assertFalse(isMarked(NODE_A));
		assertFalse(isMarked(NODE_B));
		assertEquals("The value belongs to whoever wrote it.", value, _channel.get());
		assertEquals(List.of(value), _notifiedValues);
	}

	/**
	 * Tests that a diagram showing several selected nodes marks every node of a set.
	 */
	public void testMultiSelectDiagramMarksEveryNodeOfASet() {
		bind(diagram(true, NODE_A, NODE_B));

		Set<String> value = Set.of(NODE_A, NODE_B);
		_channel.set(value);

		assertEquals(Set.of(NODE_A, NODE_B), selectedUserObjects());
		assertTrue(isMarked(NODE_A));
		assertTrue(isMarked(NODE_B));
		assertEquals(value, _channel.get());
	}

	/**
	 * Tests that a set naming a node the diagram does not have marks the node it has, and leaves
	 * the value alone - the missing one is somebody else's to display.
	 */
	public void testSetWithForeignKeyMarksTheNodePresent() {
		bind(diagram(true, NODE_A, NODE_B));

		Set<String> value = Set.of(NODE_A, ELSEWHERE);
		_channel.set(value);

		assertEquals(Set.of(NODE_A), selectedUserObjects());
		assertEquals("A key no diagram has a node for is nobody's to drop.", value, _channel.get());
	}

	/**
	 * Tests that a selection the user makes in the diagram becomes the channel value: the client
	 * sends what it selected as a patch of the shared diagram, and the user object of the node it
	 * marked is written.
	 */
	public void testSelectionMadeInTheDiagramIsWritten() throws IOException {
		clientSelects(NODE_B);

		assertEquals(NODE_B, _channel.get());
		assertEquals(Set.of(NODE_B), selectedUserObjects());
		assertEquals(List.of(NODE_B), _notifiedValues);
	}

	/**
	 * Tests that the selection the user gives up in the diagram clears the channel.
	 */
	public void testDeselectionInTheDiagramClearsTheValue() throws IOException {
		_channel.set(NODE_A);

		clientSelects(null);

		assertNull(_channel.get());
		assertEquals(Set.of(), selectedUserObjects());
	}

	/**
	 * Tests that a diagram built anew keeps the selection on the node still carrying it, and leaves
	 * the value alone.
	 */
	public void testReplacedDiagramKeepsTheNodeItStillHas() {
		_channel.set(NODE_A);

		_control.setModel(diagram(false, NODE_A, ELSEWHERE));

		assertEquals(Set.of(NODE_A), selectedUserObjects());
		assertTrue("The node of the diagram now displayed carries the marking.", isMarked(NODE_A));
		assertEquals(NODE_A, _channel.get());
		assertEquals("The carried-over selection is not a selection made in the diagram.",
			List.of(NODE_A), _notifiedValues);
	}

	/**
	 * Tests that the value is given up when the diagram built anew has no node for it any more:
	 * nobody displays it, and no click could correct it.
	 */
	public void testReplacedDiagramWithoutTheNodeClearsTheValue() {
		_channel.set(NODE_A);

		_control.setModel(diagram(false, NODE_B));

		assertEquals(Set.of(), selectedUserObjects());
		assertNull("The node displaying the value is gone.", _channel.get());
		assertEquals("The value was written once for the node and once for its loss.",
			Arrays.asList(NODE_A, null), _notifiedValues);
	}

	/**
	 * Tests that a node appearing only with the diagram built anew is marked as soon as it does -
	 * the object a create command wrote to the channel before the diagram caught up with it.
	 */
	public void testReplacedDiagramMarksTheNodeAppearingForTheValue() {
		_channel.set(ELSEWHERE);
		assertEquals("No node carries it yet.", Set.of(), selectedUserObjects());

		_control.setModel(diagram(false, NODE_A, ELSEWHERE));

		assertEquals(Set.of(ELSEWHERE), selectedUserObjects());
		assertTrue(isMarked(ELSEWHERE));
		assertEquals(ELSEWHERE, _channel.get());
		assertEquals("The value was there all along.", List.of(ELSEWHERE), _notifiedValues);
	}

	/**
	 * Tests that the marking a replaced diagram carries over is part of the diagram the client
	 * mounts on, rather than a patch racing that mount.
	 */
	public void testCarriedOverMarkingIsPartOfTheServedDiagram() {
		_channel.set(NODE_A);

		_control.setModel(diagram(false, NODE_A, NODE_B));

		Diagram served = readAsClient(_control.render());
		assertEquals("The client draws the marking from the diagram it mounts on.",
			Set.of(NODE_A), markedCssClasses(served));
	}

	/**
	 * Tests that a disposed binding leaves the diagram alone.
	 */
	public void testDisposedBindingDoesNotMarkAnything() {
		_binding.dispose();

		_channel.set(NODE_A);

		assertEquals(Set.of(), selectedUserObjects());
		assertFalse(isMarked(NODE_A));
		assertEquals("Nothing of the diagram writes the channel any more.", NODE_A, _channel.get());
	}

	/**
	 * Tests that a selection the user makes in a disposed diagram is not written either.
	 */
	public void testDisposedBindingDoesNotWriteTheValue() throws IOException {
		String diagramJson = _control.render();
		_binding.dispose();

		_control.executeClientCommand(FlowDiagramControl.CMD_UPDATE,
			Map.of(FlowDiagramControl.ARG_PATCH, clientSelectionPatch(diagramJson, NODE_B)));

		assertEquals("The diagram displays what the client marked.", Set.of(NODE_B), selectedUserObjects());
		assertNull("Nothing of the diagram writes the channel any more.", _channel.get());
	}

	@Override
	protected void tearDown() throws Exception {
		_binding.dispose();

		super.tearDown();
	}

	/**
	 * Creates the control for the given diagram and binds it to the channel, replacing what was
	 * bound before.
	 */
	private void bind(Diagram diagram) {
		if (_binding != null) {
			_binding.dispose();
		}
		_control = new DiagramControl(_context, diagram);
		_binding = new DiagramSelectionBinding(_control, _channel);
	}

	/** The user objects of the nodes the diagram displays as selected. */
	private Set<Object> selectedUserObjects() {
		return _control.getSelectedUserObjects();
	}

	/** Whether the node carrying the given user object is marked as selected. */
	private boolean isMarked(String userObject) {
		Widget node = node(_control.getModel(), widget -> userObject.equals(widget.getUserObject()));
		assertNotNull("No node for '" + userObject + "'.", node);
		return SelectionUtil.isSelected(node)
			&& _control.getModel().getSelection().contains(node);
	}

	/**
	 * Selects the node carrying the given user object the way the client does: on its own copy of
	 * the shared diagram, sending the change back as a msgbuf patch.
	 *
	 * @param userObject
	 *        The user object of the node to select, or <code>null</code> to give the selection up.
	 */
	private void clientSelects(String userObject) throws IOException {
		_control.executeClientCommand(FlowDiagramControl.CMD_UPDATE,
			Map.of(FlowDiagramControl.ARG_PATCH, clientSelectionPatch(_control.render(), userObject)));
	}

	/**
	 * The patch the client sends after selecting the node with the given user object in the given
	 * serialized diagram.
	 */
	private static String clientSelectionPatch(String diagramJson, String userObject) throws IOException {
		DefaultScope clientScope = new DefaultScope(2, 1);
		Diagram clientDiagram = Diagram.readDiagram(clientScope, new JsonReader(new StringR(diagramJson)));

		// The user object never reaches the client (it is a transient property), so the client
		// knows the node by what it draws: its CSS class.
		Widget node = userObject == null ? null : node(clientDiagram, widget -> userObject.equals(widget.getCssClass()));
		assertTrue("No node for '" + userObject + "' in the served diagram.", userObject == null || node != null);

		// As SelectableBoxOperations.onClick makes a selection unique.
		for (Widget selected : clientDiagram.getSelection()) {
			SelectionUtil.setSelected(selected, false);
		}
		if (node == null) {
			clientDiagram.setSelection(Collections.emptyList());
		} else {
			clientDiagram.setSelection(Collections.singletonList(node));
			SelectionUtil.setSelected(node, true);
		}

		StringW patch = new StringW();
		clientScope.createPatch(new JsonWriter(patch));
		return patch.toString();
	}

	/** Reads a serialized diagram the way the client mounts on it. */
	private static Diagram readAsClient(String diagramJson) {
		try {
			return Diagram.readDiagram(new DefaultScope(2, 1), new JsonReader(new StringR(diagramJson)));
		} catch (IOException ex) {
			throw new AssertionError("Cannot read the served diagram.", ex);
		}
	}

	/** The CSS classes of the nodes the given diagram displays as selected. */
	private static Set<String> markedCssClasses(Diagram diagram) {
		Set<String> result = new LinkedHashSet<>();
		WidgetTraversal.visitAll(diagram, widget -> {
			if (SelectionUtil.isSelected(widget)) {
				result.add(widget.getCssClass());
			}
		});
		return result;
	}

	/** The first widget of the given diagram matching the given criterion, or <code>null</code>. */
	private static Widget node(Diagram diagram, Predicate<Widget> criterion) {
		List<Widget> found = new ArrayList<>();
		WidgetTraversal.visitAll(diagram, widget -> {
			if (found.isEmpty() && criterion.test(widget)) {
				found.add(widget);
			}
		});
		return found.isEmpty() ? null : found.get(0);
	}

	/**
	 * A diagram of one selectable node per given user object, each node also carrying it as its CSS
	 * class, so that the client can tell the nodes apart.
	 */
	private static Diagram diagram(boolean multiSelect, String... userObjects) {
		VerticalLayout root = VerticalLayout.create();
		for (String userObject : userObjects) {
			root.addContent(SelectableBox.create()
				.setContent(Text.create().setValue(userObject))
				.setCssClass(userObject)
				.setUserObject(userObject));
		}
		return Diagram.create().setRoot(root).setMultiSelect(multiSelect);
	}

	/**
	 * A {@link FlowDiagramControl} whose serialization the test reads, as the client receives it.
	 */
	private static class DiagramControl extends FlowDiagramControl {

		/** State key the serialized diagram is written to. */
		private static final String DIAGRAM_STATE = "diagram";

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
	}

}
