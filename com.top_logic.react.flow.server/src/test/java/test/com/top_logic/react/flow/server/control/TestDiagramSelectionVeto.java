/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.react.flow.server.control;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.TestCase;

import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.dirty.ChannelVetoException;
import com.top_logic.layout.react.protocol.PatchEvent;
import com.top_logic.layout.react.protocol.SSEEvent;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.view.channel.DefaultViewChannel;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.form.StateHandler;
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
 * Tests that a selection made in a {@link FlowDiagramControl} that the selection channel refuses
 * leaves the diagram selecting - on the server and in the client - what it selected before.
 *
 * <p>
 * The refusal is the unsaved changes of a form bound to the channel, reaching the diagram as a
 * {@link ChannelVetoException} from writing the channel through its
 * {@link DiagramSelectionBinding}.
 * </p>
 */
public class TestDiagramSelectionVeto extends TestCase {

	/** A node of the diagram under test, the one selected before the refused change. */
	private static final String NODE_A = "a";

	/** Another node of the diagram under test, the one the refused change selects. */
	private static final String NODE_B = "b";

	private CapturingQueue _queue;

	private ReactContext _context;

	private ViewChannel _channel;

	/** The unsaved changes the channel refuses a change with while they are dirty. */
	private UnsavedChanges _unsavedChanges;

	/** How often the channel was asked whether it accepts a change. */
	private int _vetoChecks;

	private DiagramControl _control;

	private DiagramSelectionBinding _binding;

	/** The client's copy of the displayed diagram. */
	private Client _client;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		_queue = new CapturingQueue();
		_context = new DefaultReactContext("", "test", _queue, new ReactWindowRegistry("test"));
		_channel = new DefaultViewChannel("selection");
		_unsavedChanges = new UnsavedChanges();
		_channel.addVetoListener((sender, oldValue, newValue) -> {
			_vetoChecks++;
			return _unsavedChanges.isDirty() ? List.<StateHandler> of(_unsavedChanges) : List.<StateHandler> of();
		});
	}

	/**
	 * Tests that a click on another element the channel refuses keeps the element selected before,
	 * passes the veto on, and makes the client mark that element again.
	 */
	public void testRefusedSelectionIsRestored() throws IOException {
		display(diagram(false, NODE_A, NODE_B), NODE_A);

		_client.selectOnly(NODE_B);
		assertVetoed(_client.sendUpdate());

		assertEquals(Set.of(NODE_A), _control.getSelectedUserObjects());
		assertEquals("The channel keeps its value.", NODE_A, _channel.get());

		_client.applyServerPatches();
		assertEquals("The client marks the element selected before again.",
			Set.of(NODE_A), _client.marked());
		assertEquals(Set.of(NODE_A), _client.selection());
	}

	/**
	 * Tests that a click the channel accepts selects the clicked element and writes it.
	 */
	public void testAcceptedSelectionIsWritten() throws IOException {
		display(diagram(false, NODE_A, NODE_B), NODE_A);
		_unsavedChanges.resolve();

		_client.selectOnly(NODE_B);
		_client.sendUpdate().run();

		assertEquals(Set.of(NODE_B), _control.getSelectedUserObjects());
		assertEquals(NODE_B, _channel.get());
		assertEquals("The client already marks what it selected.", List.of(), _client.serverPatches());
	}

	/**
	 * Tests that a click with a modifier key the channel refuses is taken back as well.
	 */
	public void testRefusedIncrementalSelectionIsRestored() throws IOException {
		display(diagram(true, NODE_A, NODE_B), NODE_A);

		_client.addToSelection(NODE_B);
		assertVetoed(_client.sendUpdate());

		assertEquals(Set.of(NODE_A), _control.getSelectedUserObjects());
		assertEquals(NODE_A, _channel.get());

		_client.applyServerPatches();
		assertEquals(Set.of(NODE_A), _client.marked());
		assertEquals(Set.of(NODE_A), _client.selection());
	}

	/**
	 * Tests that clicking the refused element again asks again, and selects it once the unsaved
	 * changes are resolved.
	 */
	public void testClickingTheRefusedElementAgainAsksAgain() throws IOException {
		display(diagram(false, NODE_A, NODE_B), NODE_A);

		_client.selectOnly(NODE_B);
		assertVetoed(_client.sendUpdate());
		_client.applyServerPatches();
		int checks = _vetoChecks;

		_client.selectOnly(NODE_B);
		assertVetoed(_client.sendUpdate());
		assertEquals("The channel is asked again.", checks + 1, _vetoChecks);
		assertEquals(Set.of(NODE_A), _control.getSelectedUserObjects());
		_client.applyServerPatches();
		assertEquals(Set.of(NODE_A), _client.marked());

		_unsavedChanges.resolve();
		_client.selectOnly(NODE_B);
		_client.sendUpdate().run();

		assertEquals(Set.of(NODE_B), _control.getSelectedUserObjects());
		assertEquals(NODE_B, _channel.get());
	}

	@Override
	protected void tearDown() throws Exception {
		if (_binding != null) {
			_binding.dispose();
		}

		super.tearDown();
	}

	/**
	 * Displays the given diagram bound to the channel, with the node carrying the given user object
	 * selected, and gives the client its copy of it.
	 */
	private void display(Diagram diagram, String selected) throws IOException {
		_control = new DiagramControl(_context, diagram);
		_binding = new DiagramSelectionBinding(_control, _channel);
		_channel.set(selected);
		_unsavedChanges.makeDirty();

		_control.write(new TagWriter());
		_client = new Client(_control.storedDiagram());
		_queue.clear();
	}

	/** Asserts that the given update is refused with a {@link ChannelVetoException}. */
	private static void assertVetoed(Update update) throws IOException {
		try {
			update.run();
			fail("The channel refuses the selection.");
		} catch (ChannelVetoException ex) {
			// Expected: the command invoker asks the user about the unsaved changes.
		}
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

	/** An update the client sends to the control. */
	private interface Update {

		/** Sends the update. */
		void run() throws IOException;
	}

	/**
	 * The client's side of the shared diagram: it changes its own copy, sends the changes as a
	 * msgbuf patch, and applies the patches the control sends back.
	 */
	private class Client {

		private final DefaultScope _scope = new DefaultScope(2, 1);

		private final Diagram _diagram;

		/**
		 * Creates a {@link Client} mounted on the given serialized diagram.
		 */
		Client(String diagramJson) throws IOException {
			_diagram = Diagram.readDiagram(_scope, new JsonReader(new StringR(diagramJson)));
		}

		/** Selects only the node drawn with the given CSS class, as a plain click does. */
		void selectOnly(String cssClass) {
			_diagram.setIncrementalSelection(false);
			for (Widget selected : _diagram.getSelection()) {
				SelectionUtil.setSelected(selected, false);
			}
			Widget node = node(cssClass);
			_diagram.setSelection(Collections.singletonList(node));
			SelectionUtil.setSelected(node, true);
		}

		/** Adds the node drawn with the given CSS class to the selection, as a modifier click does. */
		void addToSelection(String cssClass) {
			_diagram.setIncrementalSelection(true);
			Widget node = node(cssClass);
			_diagram.getSelection().add(node);
			SelectionUtil.setSelected(node, true);
		}

		/** The update carrying the changes made so far. */
		Update sendUpdate() throws IOException {
			StringW patch = new StringW();
			_scope.createPatch(new JsonWriter(patch));
			Map<String, Object> args = Map.of(FlowDiagramControl.ARG_PATCH, patch.toString());
			return () -> _control.handleUpdate(_context, args);
		}

		/** The diagram patches the control sent since the client last received them. */
		List<String> serverPatches() throws IOException {
			List<String> result = new ArrayList<>();
			for (SSEEvent event : _queue.events()) {
				if (event instanceof PatchEvent patch && _control.getID().equals(patch.getControlId())) {
					JsonReader json = new JsonReader(new StringR(patch.getPatch()));
					json.beginObject();
					while (json.hasNext()) {
						if (DiagramControl.DIAGRAM_PATCH_STATE.equals(json.nextName())) {
							result.add(json.nextString());
						} else {
							json.skipValue();
						}
					}
					json.endObject();
				}
			}
			return result;
		}

		/** Applies the diagram patches the control sent to the client's copy. */
		void applyServerPatches() throws IOException {
			for (String patch : serverPatches()) {
				_scope.applyChanges(new JsonReader(new StringR(patch)));
			}
			_queue.clear();
		}

		/** The CSS classes of the nodes the client marks as selected. */
		Set<String> marked() {
			Set<String> result = new LinkedHashSet<>();
			WidgetTraversal.visitAll(_diagram, widget -> {
				if (SelectionUtil.isSelected(widget)) {
					result.add(widget.getCssClass());
				}
			});
			return result;
		}

		/** The CSS classes of the nodes in the client's selection. */
		Set<String> selection() {
			Set<String> result = new LinkedHashSet<>();
			for (Widget widget : _diagram.getSelection()) {
				result.add(widget.getCssClass());
			}
			return result;
		}

		private Widget node(String cssClass) {
			List<Widget> found = new ArrayList<>();
			WidgetTraversal.visitAll(_diagram, widget -> {
				if (found.isEmpty() && cssClass.equals(widget.getCssClass())) {
					found.add(widget);
				}
			});
			assertFalse("No node for '" + cssClass + "' in the served diagram.", found.isEmpty());
			return found.get(0);
		}
	}

	/**
	 * Unsaved changes of a form bound to the channel.
	 */
	private static class UnsavedChanges implements StateHandler {

		private boolean _dirty;

		/** Makes the changes unsaved, so that they block a change of the channel. */
		void makeDirty() {
			_dirty = true;
		}

		/** Resolves the changes, so that they block nothing any more. */
		void resolve() {
			_dirty = false;
		}

		@Override
		public boolean isDirty() {
			return _dirty;
		}

		@Override
		public boolean hasErrors() {
			return false;
		}

		@Override
		public void executeSave() {
			resolve();
		}

		@Override
		public void executeDiscard() {
			resolve();
		}

		@Override
		public String getDescription() {
			return "unsaved changes";
		}
	}

	/**
	 * An {@link SSEUpdateQueue} keeping the events it is handed, as the client receives them.
	 */
	private static class CapturingQueue extends SSEUpdateQueue {

		private final List<SSEEvent> _events = new ArrayList<>();

		@Override
		public void enqueue(SSEEvent event) {
			_events.add(event);
		}

		/** The events handed over since the last {@link #clear()}. */
		List<SSEEvent> events() {
			return _events;
		}

		/** Forgets the events handed over so far. */
		void clear() {
			_events.clear();
		}
	}

	/**
	 * A {@link FlowDiagramControl} whose serialization the test reads, as the client receives it.
	 */
	private static class DiagramControl extends FlowDiagramControl {

		/** State key the serialized diagram is written to. */
		static final String DIAGRAM_STATE = "diagram";

		/** State key the client reads a patch of the diagram from. */
		static final String DIAGRAM_PATCH_STATE = "diagramPatch";

		/**
		 * Creates a {@link DiagramControl}.
		 */
		DiagramControl(ReactContext context, Diagram diagram) {
			super(context, diagram);
		}

		/** The serialized diagram the control's state holds. */
		String storedDiagram() {
			return (String) getState(DIAGRAM_STATE);
		}
	}

}
