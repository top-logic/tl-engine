/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.react.flow.server.control;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.layout.react.control.CommandFailures;

import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.dnd.AcceptedKinds;
import com.top_logic.layout.react.control.dnd.DropArguments;
import com.top_logic.layout.react.control.dnd.DropMarker;
import com.top_logic.layout.react.control.dnd.DropProbeArguments;
import com.top_logic.layout.react.control.dnd.DropSupport;
import com.top_logic.layout.react.control.dnd.DropZone;
import com.top_logic.layout.react.control.table.TableViewControl;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.view.command.ActionArguments;
import com.top_logic.layout.view.command.ArgumentsViewAction;
import com.top_logic.layout.view.command.Continuation;
import com.top_logic.layout.view.command.ViewExecutabilityRule;
import com.top_logic.layout.view.dnd.DropBinding;
import com.top_logic.layout.view.dnd.DropSignature;
import com.top_logic.react.flow.data.Box;
import com.top_logic.react.flow.data.Diagram;
import com.top_logic.react.flow.data.DropRegion;
import com.top_logic.react.flow.data.SelectableBox;
import com.top_logic.react.flow.data.Text;
import com.top_logic.react.flow.data.VerticalLayout;
import com.top_logic.react.flow.operations.WidgetTraversal;
import com.top_logic.react.flow.server.control.FlowDiagramControl;
import com.top_logic.table.Column;
import com.top_logic.table.impl.DefaultColumn;
import com.top_logic.table.impl.DefaultTableView;
import com.top_logic.table.impl.ListRowSource;
import com.top_logic.tool.execution.ExecutableState;
import com.top_logic.util.error.TopLogicException;

import de.haumacher.msgbuf.graph.DefaultScope;
import de.haumacher.msgbuf.io.StringR;
import de.haumacher.msgbuf.json.JsonReader;

/**
 * Tests a {@link FlowDiagramControl} as a target of the view layer's drag and drop: which elements
 * the client is told to be drop targets, and how a drop and a drop probe onto a node and on the
 * diagram as a whole are resolved and decided.
 */
public class TestFlowDiagramDrop extends TestCase {

	/** The kind the rows of the source table are dragged as. */
	private static final String ROW_KIND = "row";

	private static final List<String> SOURCE_ROWS = List.of("s1", "s2");

	/** User object (and CSS class) of a selectable node. */
	private static final String NODE = "node";

	/** User object (and CSS class) of a drop region the refusal function refuses. */
	private static final String REFUSED_REGION = "refused";

	/** CSS class of a plain box without a user object. */
	private static final String PLAIN = "plain";

	/** CSS class of a drop region without a user object. */
	private static final String OPEN_REGION = "open";

	/** The reason the refusal function of the node drop refuses with. */
	private static final String REASON = "Not here.";

	/** A {@link com.top_logic.layout.view.command.ViewAction} remembering the named arguments of its chain. */
	private static final class ArgumentsRecorder extends ArgumentsViewAction {

		ActionArguments _arguments;

		@Override
		public void execute(ReactContext context, ActionArguments arguments, Continuation continuation) {
			_arguments = arguments;
			continuation.resume(arguments.value());
		}
	}

	private ReactContext _context;

	private TableViewControl<String> _source;

	private DiagramControl _diagram;

	private ArgumentsRecorder _onNode;

	private ArgumentsRecorder _onDiagram;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		// One context, hence one control registry: the drop resolves its source control out of it.
		_context = new DefaultReactContext("", "test", new SSEUpdateQueue(), new ReactWindowRegistry("test"));
		List<Column<String, ?>> columns =
			List.of(DefaultColumn.<String, String> builder("value", value -> value).build());
		_source = new TableViewControl<>(_context,
			DefaultTableView.create(columns, new ListRowSource<>(SOURCE_ROWS, columns)), false);
		_source.setDragSource(ROW_KIND);
		_source.attach();

		_diagram = new DiagramControl(_context, diagram());
		_onNode = new ArgumentsRecorder();
		_onDiagram = new ArgumentsRecorder();
		_diagram.setDropTarget(new DropBinding(_context, List.of(
			new DropBinding.Drop(AcceptedKinds.of(List.of(ROW_KIND)), DropSignature.ONTO, Map.of(), List.of(_onNode),
				() -> ExecutableState.EXECUTABLE, ViewExecutabilityRule.ALWAYS_EXECUTABLE,
				(objects, references) -> REFUSED_REGION.equals(references.get(0)) ? REASON : null),
			new DropBinding.Drop(AcceptedKinds.of(List.of(ROW_KIND)), DropSignature.CONTROL, Map.of(),
				List.of(_onDiagram)))));
	}

	/**
	 * A diagram of a selectable node, a plain box, a drop region with a user object and one without.
	 */
	private static Diagram diagram() {
		VerticalLayout root = VerticalLayout.create();
		root.addContent(SelectableBox.create()
			.setContent(Text.create().setValue(NODE))
			.setCssClass(NODE)
			.setUserObject(NODE));
		root.addContent(Text.create().setValue(PLAIN).setCssClass(PLAIN));
		root.addContent(DropRegion.create()
			.setContent(Text.create().setValue(REFUSED_REGION))
			.setCssClass(REFUSED_REGION)
			.setUserObject(REFUSED_REGION));
		root.addContent(DropRegion.create()
			.setContent(Text.create().setValue(OPEN_REGION))
			.setCssClass(OPEN_REGION));
		return Diagram.create().setRoot(root);
	}

	/**
	 * The client learns the elements a drop is made onto: an element with a user object - a node, a
	 * drop region with one - is flagged, a plain box and a drop region without a user object are not.
	 */
	public void testDropTargetsAreFlagged() throws IOException {
		Diagram served = read(_diagram.render());

		assertTrue("A node with a user object is a drop target.", find(served, NODE).isDropTarget());
		assertTrue("A drop region with a user object is a drop target.",
			find(served, REFUSED_REGION).isDropTarget());
		assertFalse("A plain box is no drop target.", find(served, PLAIN).isDropTarget());
		assertFalse("A drop region without a user object stands for the diagram as a whole.",
			find(served, OPEN_REGION).isDropTarget());
	}

	/**
	 * A user object set on an element after the diagram was served reaches the client: in the patch
	 * of the change, and in the full state replacing the patches.
	 */
	public void testUserObjectSetLaterReachesTheClient() throws IOException {
		String served = _diagram.render();
		DefaultScope clientScope = new DefaultScope(2, 1);
		Diagram client = Diagram.readDiagram(clientScope, new JsonReader(new StringR(served)));
		assertFalse(find(client, PLAIN).isDropTarget());

		find(_diagram.getModel(), PLAIN).setUserObject(PLAIN);
		_diagram.pushDiagramChanges();

		String patch = _diagram.sentPatch();
		assertNotNull("The flag is sent as a patch.", patch);
		clientScope.applyChanges(new JsonReader(new StringR(patch)));
		assertTrue("The patch flags the element.", find(client, PLAIN).isDropTarget());
		assertTrue("The full state flags the element.", find(read(_diagram.storedDiagram()), PLAIN).isDropTarget());

		find(_diagram.getModel(), PLAIN).setUserObject(null);
		_diagram.pushDiagramChanges();
		clientScope.applyChanges(new JsonReader(new StringR(_diagram.sentPatch())));
		assertFalse("Clearing the user object takes the flag back.", find(client, PLAIN).isDropTarget());
	}

	/**
	 * A drop onto a node is made onto its user object, with the point relative to the node.
	 */
	public void testDropOntoNode() throws IOException {
		String node = idOf(_diagram.render(), NODE);

		assertNull(drop(DropSupport.CMD_DROP, node, 3.5, 4.0, null));

		assertNotNull("The node drop applies.", _onNode._arguments);
		assertNull("The diagram drop does not apply.", _onDiagram._arguments);
		assertEquals(List.of(SOURCE_ROWS.get(0)), _onNode._arguments.value());
		assertEquals(NODE, _onNode._arguments.get(com.top_logic.layout.view.dnd.DropArguments.TARGET));
		assertEquals(Double.valueOf(3.5), _onNode._arguments.get(com.top_logic.layout.view.dnd.DropArguments.X));
		assertEquals(Double.valueOf(4.0), _onNode._arguments.get(com.top_logic.layout.view.dnd.DropArguments.Y));
		assertEquals("A drop target is identified by its user object.", NODE,
			_diagram.objectIdentities().objectOf(node));
		assertEquals(node, _diagram.objectIdentities().idOf(NODE));
	}

	/**
	 * A drop on the free area of the diagram is made on the diagram as a whole, with the point in
	 * diagram coordinates.
	 */
	public void testDropOnDiagram() {
		_diagram.render();

		assertNull(drop(DropSupport.CMD_DROP, null, 120.0, 80.0, null));

		assertNull("The node drop does not apply.", _onNode._arguments);
		assertNotNull("The diagram drop applies.", _onDiagram._arguments);
		assertEquals(Arrays.asList(SOURCE_ROWS.get(0)), _onDiagram._arguments.value());
		assertNull(_onDiagram._arguments.get(com.top_logic.layout.view.dnd.DropArguments.TARGET));
		assertEquals(Double.valueOf(120), _onDiagram._arguments.get(com.top_logic.layout.view.dnd.DropArguments.X));
		assertEquals(Double.valueOf(80), _onDiagram._arguments.get(com.top_logic.layout.view.dnd.DropArguments.Y));
	}

	/**
	 * A drop the refusal function refuses is answered with its reason and applies nothing; a drop
	 * naming an element that is no drop target is refused as well.
	 */
	public void testRefusedDrop() throws IOException {
		String served = _diagram.render();

		TopLogicException refusal = drop(DropSupport.CMD_DROP, idOf(served, REFUSED_REGION), 1.0, 1.0, null);
		assertNotNull("The refused drop must fail.", refusal);
		assertNull("A refused drop applies nothing.", _onNode._arguments);
		assertNull("A refused drop onto a node is not handed to the diagram drop.", _onDiagram._arguments);

		assertNotNull("A plain box is no drop target.",
			drop(DropSupport.CMD_DROP, idOf(served, PLAIN), 1.0, 1.0, null));
		assertNull(_onNode._arguments);
		assertNull(_onDiagram._arguments);
	}

	/**
	 * A probe answers whether a drop would be accepted, with the marker of a node or of the diagram,
	 * and the reason of a refusal - without applying anything.
	 */
	public void testProbe() throws IOException {
		String served = _diagram.render();
		String node = idOf(served, NODE);

		assertNull(drop(DropSupport.CMD_DROP_PROBE, node, 1.0, 1.0, "p1"));
		Map<?, ?> onNode = verdict("p1");
		assertEquals(Boolean.TRUE, onNode.get(DropSupport.VERDICT_ACCEPTED));
		assertEquals(DropMarker.INTO.wireName(), onNode.get(DropSupport.VERDICT_MARKER));
		assertEquals(node, onNode.get(DropSupport.VERDICT_MARKER_KEY));

		assertNull(drop(DropSupport.CMD_DROP_PROBE, null, 1.0, 1.0, "p2"));
		Map<?, ?> onDiagram = verdict("p2");
		assertEquals(Boolean.TRUE, onDiagram.get(DropSupport.VERDICT_ACCEPTED));
		assertEquals(DropMarker.CONTROL.wireName(), onDiagram.get(DropSupport.VERDICT_MARKER));
		assertNull(onDiagram.get(DropSupport.VERDICT_MARKER_KEY));

		assertNull(drop(DropSupport.CMD_DROP_PROBE, idOf(served, REFUSED_REGION), 1.0, 1.0, "p3"));
		Map<?, ?> refused = verdict("p3");
		assertEquals(Boolean.FALSE, refused.get(DropSupport.VERDICT_ACCEPTED));
		assertEquals(REASON, refused.get(DropSupport.VERDICT_REASON));

		assertNull("A probe applies nothing.", _onNode._arguments);
		assertNull("A probe applies nothing.", _onDiagram._arguments);
	}

	/**
	 * Sends the given drop command for a drop of the source table's first row onto the element with
	 * the given ID, or on the diagram as a whole for {@code null}, at the given point.
	 *
	 * @param probe
	 *        The probe identifier for a {@link DropSupport#CMD_DROP_PROBE}, {@code null} for a drop.
	 * @return The failure of the command, {@code null} if it succeeded.
	 */
	private TopLogicException drop(String command, String targetKey, double x, double y, String probe) {
		Map<String, Object> arguments = new HashMap<>();
		arguments.put(DropArguments.SOURCE, _source.getID());
		arguments.put(DropArguments.KEYS, List.of(_source.rowIdAt(0)));
		arguments.put(DropArguments.SELECTION, Boolean.FALSE);
		if (targetKey != null) {
			arguments.put(DropArguments.TARGET_KEY, targetKey);
			arguments.put(DropArguments.ZONE, DropZone.MIDDLE.wireName());
		} else {
			arguments.put(DropArguments.ZONE, DropZone.NONE.wireName());
		}
		arguments.put(DropArguments.X, Double.valueOf(x));
		arguments.put(DropArguments.Y, Double.valueOf(y));
		if (probe != null) {
			arguments.put(DropProbeArguments.DRAG, "drag1");
			arguments.put(DropProbeArguments.PROBE, probe);
		}
		return CommandFailures.outcome(() -> _diagram.executeClientCommand(command, arguments));
	}

	/**
	 * The verdict answered to the probe with the given identifier.
	 */
	private Map<?, ?> verdict(String probe) {
		Map<?, ?> verdicts = (Map<?, ?>) _diagram.state(FlowDiagramControl.DROP_VERDICTS);
		assertNotNull("No verdicts answered.", verdicts);
		Map<?, ?> result = (Map<?, ?>) verdicts.get(probe);
		assertNotNull("No verdict for probe '" + probe + "'.", result);
		return result;
	}

	/**
	 * The client-side ID of the element with the given CSS class in the given serialized diagram.
	 */
	private static String idOf(String diagramJson, String cssClass) throws IOException {
		DefaultScope scope = new DefaultScope(2, 1);
		Diagram diagram = Diagram.readDiagram(scope, new JsonReader(new StringR(diagramJson)));
		return Integer.toString(scope.id(find(diagram, cssClass)));
	}

	/**
	 * The given serialized diagram, read the way the client mounts on it.
	 */
	private static Diagram read(String diagramJson) throws IOException {
		return Diagram.readDiagram(new DefaultScope(2, 1), new JsonReader(new StringR(diagramJson)));
	}

	/**
	 * The box with the given CSS class in the given diagram.
	 */
	private static Box find(Diagram diagram, String cssClass) {
		Box[] found = { null };
		WidgetTraversal.visitAll(diagram, widget -> {
			if (found[0] == null && widget instanceof Box box && cssClass.equals(widget.getCssClass())) {
				found[0] = box;
			}
		});
		assertNotNull("No element '" + cssClass + "' in the diagram.", found[0]);
		return found[0];
	}

	/**
	 * A {@link FlowDiagramControl} whose state the test reads, as the client receives it.
	 */
	private static class DiagramControl extends FlowDiagramControl {

		/** State key the serialized diagram is written to. */
		private static final String DIAGRAM_STATE = "diagram";

		/** State key the client reads a patch of the diagram from. */
		private static final String DIAGRAM_PATCH_STATE = "diagramPatch";

		private String _sentPatch;

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
		 * The serialized diagram the control's state holds.
		 */
		String storedDiagram() {
			return (String) getState(DIAGRAM_STATE);
		}

		/**
		 * The value of the given state key.
		 */
		Object state(String key) {
			return getState(key);
		}

		/**
		 * The last patch sent to the client.
		 */
		String sentPatch() {
			return _sentPatch;
		}

		@Override
		protected void sendTransientState(String key, Object value) {
			if (DIAGRAM_PATCH_STATE.equals(key)) {
				_sentPatch = (String) value;
			}
			super.sendTransientState(key, value);
		}
	}

	/**
	 * The test suite, started with the resource bundles a table's column labels and the reasons of a
	 * refusal need.
	 */
	public static Test suite() {
		return ModuleTestSetup.setupModule(
			ServiceTestSetup.createSetup(TestFlowDiagramDrop.class, ResourcesModule.Module.INSTANCE));
	}

}
