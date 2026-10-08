/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.element;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.Test;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.basic.BufferingProtocol;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.io.binary.ClassRelativeBinaryContent;
import com.top_logic.basic.json.JSON;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.control.dnd.DropArguments;
import com.top_logic.layout.react.control.dnd.DropMode;
import com.top_logic.layout.react.control.dnd.DropSupport;
import com.top_logic.layout.react.control.dnd.DropZone;
import com.top_logic.layout.react.control.tree.ReactTreeControl;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.view.DefaultViewContext;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.ViewElement;
import com.top_logic.layout.view.ViewLoader;
import com.top_logic.layout.view.channel.DefaultViewChannel;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.dnd.DropConfig;
import com.top_logic.layout.view.element.TreeElement;
import com.top_logic.model.search.expr.config.SearchBuilder;
import com.top_logic.util.model.ModelService;

/**
 * Tests the {@code <drag>} and {@code <drop>} of a {@code <tree>} on the control the tree is built
 * as: an insertion among the nodes publishing its parent and the object it inserts before, next to
 * a drop onto a node taking what the insertion refuses.
 */
public class TestTreeElementDragDrop extends BasicTestCase {

	/** The view declaring an insertion among the nodes next to a drop onto a node. */
	private static final String VIEW = "test-tree-dnd.view.xml";

	/** A view declaring options its drops cannot use. */
	private static final String ERROR_VIEW = "test-tree-dnd-error.view.xml";

	/** Name of the channel the insertion publishes the object it inserts under on. */
	private static final String PARENT = "parent";

	/** Name of the channel the insertion publishes the object it inserts before on. */
	private static final String BEFORE = "before";

	/** Name of the channel the node drop publishes its target on. */
	private static final String TARGET = "target";

	/** A value no drop publishes, marking a channel as not written. */
	private static final String UNWRITTEN = "unwritten";

	/** The kind the nodes are dragged as, and the drops accept. */
	private static final String KIND = "item";

	/** The object the test tree is built from, displayed as no node. */
	private static final String ROOT = "root";

	/** The first top-level object, with the children {@link #A1} and {@link #A2}. */
	private static final String A = "a";

	/** The first child of {@link #A}. */
	private static final String A1 = "a1";

	/** The second top-level object, a leaf the node rules refuse to drag. */
	private static final String B = "b";

	private ViewChannel _parent;

	private ViewChannel _before;

	private ViewChannel _target;

	private ViewContext _context;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		_context = new DefaultViewContext(
			new DefaultReactContext("", "test", new SSEUpdateQueue(), new ReactWindowRegistry("test")));
		_parent = register(PARENT);
		_before = register(BEFORE);
		_target = register(TARGET);
	}

	private ViewChannel register(String name) {
		ViewChannel channel = new DefaultViewChannel(name);
		_context.registerChannel(name, channel);
		return channel;
	}

	@Override
	protected void tearDown() throws Exception {
		_context = null;
		_parent = null;
		_before = null;
		_target = null;

		super.tearDown();
	}

	/** The node rules decide which node object may be dragged. */
	public void testNodeRulesDecidePerNode() throws Exception {
		ReactTreeControl control = createControl(VIEW);

		assertTrue(control.isDragEnabled());
		assertEquals(KIND, control.dragKind());
		assertTrue(control.isDraggable(A));
		assertFalse("The node rules refuse b.", control.isDraggable(B));
	}

	/**
	 * An insertion declared before a node drop gets the parent and the object to insert before on
	 * its channels; an insertion its refusal function refuses goes to the node drop.
	 */
	public void testOrderedDropNextToNodeDrop() throws Exception {
		ReactTreeControl control = createControl(VIEW);
		control.attach();
		assertEquals("Both modes are announced in declaration order.",
			List.of(DropMode.ORDERED.wireName(), DropMode.ONTO.wireName()),
			state(control).get(DropSupport.DROP_MODES));

		assertInserted(control, A, DropZone.UPPER, ROOT, A);
		assertInserted(control, A, DropZone.MIDDLE, A, A1);
		assertInserted(control, B, DropZone.LOWER, ROOT, null);
		assertInserted(control, null, DropZone.NONE, ROOT, null);

		reset();
		assertTrue(drop(control, A, B, DropZone.MIDDLE));
		assertEquals("The insertion into b is refused, the node drop takes b.", B, _target.get());
		assertEquals(UNWRITTEN, _parent.get());
		assertEquals(UNWRITTEN, _before.get());

		reset();
		assertFalse("b cannot be dragged.", drop(control, B, A, DropZone.UPPER));
		assertEquals(UNWRITTEN, _parent.get());
	}

	private void assertInserted(ReactTreeControl control, String node, DropZone zone, Object parent,
			Object before) {
		String place = "node " + node + ", zone " + zone;
		reset();
		assertTrue(place, drop(control, A, node, zone));
		assertEquals(place, parent, _parent.get());
		assertEquals(place, before, _before.get());
		assertEquals(place + ": the node drop must not apply.", UNWRITTEN, _target.get());
	}

	private void reset() {
		_parent.set(UNWRITTEN);
		_before.set(UNWRITTEN);
		_target.set(UNWRITTEN);
	}

	/**
	 * Drops the node of the given object of the tree in the given zone of the node of the given
	 * target, beside the nodes for a {@code null} target.
	 *
	 * @return Whether the drop was applied.
	 */
	private boolean drop(ReactTreeControl control, String dragged, String target, DropZone zone) {
		Map<String, Object> arguments = new HashMap<>();
		arguments.put(DropArguments.SOURCE, control.getID());
		arguments.put(DropArguments.KEYS, nodeId(control, dragged));
		arguments.put(DropArguments.SELECTION, Boolean.FALSE);
		arguments.put(DropArguments.ZONE, zone.wireName());
		if (target != null) {
			arguments.put(DropArguments.TARGET_KEY, nodeId(control, target));
		}
		return control.executeClientCommand(DropSupport.CMD_DROP, arguments).isSuccess();
	}

	/**
	 * The client-side id of the node of the given top-level object; the top-level objects are
	 * displayed in the order {@link #A}, {@link #B}.
	 */
	private static String nodeId(ReactTreeControl control, String topLevel) {
		List<?> nodes = (List<?>) state(control).get(ReactTreeControl.NODES);
		int index = List.of(A, B).indexOf(topLevel);
		assertTrue("Not a top-level object: " + topLevel, index >= 0);
		return (String) ((Map<?, ?>) nodes.get(index)).get(ReactTreeControl.NODE_ID);
	}

	/**
	 * A parent channel on a drop on the tree or onto a node, and a target channel on an insertion,
	 * are configuration errors.
	 */
	public void testMisplacedOptionsAreReported() throws Exception {
		BufferingProtocol log = new BufferingProtocol();
		new DefaultInstantiationContext(log).getInstance(readTree(ERROR_VIEW));

		List<String> errors = log.getErrors();
		assertEquals("Expected the parent channels of the tree and the node drop to be reported: " + errors,
			2, count(errors, "declares '" + DropConfig.PARENT_CHANNEL + "'"));
		assertEquals("Expected the target channel of the insertion to be reported: " + errors,
			1, count(errors, "'" + DropMode.ORDERED.wireName() + "' declares '" + DropConfig.TARGET_CHANNEL + "'"));
		assertEquals("No other error expected: " + errors, 3, errors.size());
	}

	private static long count(List<String> errors, String part) {
		return errors.stream().filter(error -> error.contains(part)).count();
	}

	/** The control of the tree of the given view. */
	private ReactTreeControl createControl(String view) throws ConfigurationException {
		DefaultInstantiationContext instantiation = new DefaultInstantiationContext(TestTreeElementDragDrop.class);
		TreeElement element = (TreeElement) instantiation.getInstance(readTree(view));
		instantiation.checkErrors();
		return (ReactTreeControl) element.createControl(_context);
	}

	/** The {@code <tree>} configuration of the given view. */
	private static TreeElement.Config readTree(String view) throws ConfigurationException {
		ViewElement.Config config = ViewLoader.parseConfig(
			List.of(new ClassRelativeBinaryContent(TestTreeElementDragDrop.class, view)));
		return (TreeElement.Config) config.getContent();
	}

	/** The state of the given control as the client parses it. */
	private static Map<?, ?> state(ReactTreeControl control) {
		String json = control.stateAsJSON();
		try {
			return (Map<?, ?>) JSON.fromString(json);
		} catch (JSON.ParseException ex) {
			throw new AssertionError("Not the JSON state of a control: " + json, ex);
		}
	}

	/**
	 * The suite of tests.
	 *
	 * @implNote The structure of the tree and the rules are TL-Script expressions, whose compilation
	 *           and evaluation need the application model and the
	 *           {@link com.top_logic.knowledge.service.KnowledgeBase} they are executed against.
	 */
	public static Test suite() {
		return KBSetup.getSingleKBTest(TestTreeElementDragDrop.class,
			ServiceTestSetup.createStarterFactoryForModules(
				TypeIndex.Module.INSTANCE, SearchBuilder.Module.INSTANCE, ModelService.Module.INSTANCE));
	}

}
