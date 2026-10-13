/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.element;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import com.top_logic.layout.react.control.table.ExpandArguments;
import com.top_logic.layout.react.control.table.TableViewControl;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.view.DefaultViewContext;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.ViewElement;
import com.top_logic.layout.view.ViewLoader;
import com.top_logic.layout.view.channel.DefaultViewChannel;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.dnd.DropConfig;
import com.top_logic.layout.view.element.TreeTableElement;
import com.top_logic.layout.view.form.FieldControlService;
import com.top_logic.layout.view.table.ColumnProviderService;
import com.top_logic.model.search.expr.config.SearchBuilder;
import com.top_logic.table.Row;
import com.top_logic.util.model.ModelService;

/**
 * Tests the {@code <tree-table>}: the rows it computes from the root and the children function, its
 * selection channel, and its {@code <drag>} and {@code <drop>} - an insertion among the rows
 * publishing the row it inserts under and the row it inserts before, next to a drop onto a row
 * taking what the insertion refuses.
 */
public class TestTreeTableElement extends BasicTestCase {

	/** The view declaring the tree table. */
	private static final String VIEW = "test-tree-table.view.xml";

	/** A view declaring options its drops cannot use. */
	private static final String ERROR_VIEW = "test-tree-table-dnd-error.view.xml";

	/** Name of the selection channel. */
	private static final String SELECTED = "selected";

	/** Name of the channel the insertion publishes the object it inserts under on. */
	private static final String PARENT = "parent";

	/** Name of the channel the insertion publishes the object it inserts before on. */
	private static final String BEFORE = "before";

	/** Name of the channel the row drop publishes its target on. */
	private static final String TARGET = "target";

	/** A value no drop publishes, marking a channel as not written. */
	private static final String UNWRITTEN = "unwritten";

	/** The kind the rows are dragged as, and the drops accept. */
	private static final String KIND = "item";

	/** The object the test tree is built from, displayed as no row. */
	private static final String ROOT = "root";

	/** The first top-level object, with the children {@link #A1} and {@link #A2}. */
	private static final String A = "a";

	/** The first child of {@link #A}. */
	private static final String A1 = "a1";

	/** The second child of {@link #A}. */
	private static final String A2 = "a2";

	/** The second top-level object, a leaf the row rules refuse to drag. */
	private static final String B = "b";

	/**
	 * The prefix of the client-side key of a table row, followed by the row's index.
	 *
	 * @implNote Restated here because {@link TableViewControl} keeps it private.
	 */
	private static final String ROW_KEY_PREFIX = "row_";

	/**
	 * State key telling whether the rows are displayed as a tree.
	 *
	 * @implNote Restated here because {@link TableViewControl} keeps it private.
	 */
	private static final String TREE_MODE = "treeMode";

	private ViewChannel _selected;

	private ViewChannel _parent;

	private ViewChannel _before;

	private ViewChannel _target;

	private ViewContext _context;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		_context = new DefaultViewContext(
			new DefaultReactContext("", "test", new SSEUpdateQueue(), new ReactWindowRegistry("test")));
		_selected = register(SELECTED);
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
		_selected = null;
		_parent = null;
		_before = null;
		_target = null;

		super.tearDown();
	}

	/**
	 * The rows are the children of the root object, the object itself is no row; opening a row
	 * shows its children below it, one level deeper.
	 */
	public void testRowsFollowTheTree() throws Exception {
		TableViewControl<?> control = createControl(VIEW);

		assertEquals(Boolean.TRUE, state(control).get(TREE_MODE));
		assertEquals(Boolean.TRUE, state(control).get(TableViewControl.DROP_TREE_ZONES));
		assertEquals(List.of(A, B), rows(control));

		expand(control, A);
		assertEquals(List.of(A, A1, A2, B), rows(control));
		Row<?> a1 = control.getView().rows(1, 2).get(0);
		assertEquals(1, a1.depth());
		assertFalse("A leaf cannot be opened.", a1.expandable());
		assertTrue(control.getView().rows(0, 1).get(0).expanded());
	}

	/**
	 * The selection channel holds the object of the selected row, and an object written to it is
	 * selected - also one in a subtree that is not open.
	 */
	public void testSelection() throws Exception {
		TableViewControl<?> control = createControl(VIEW);

		control.selectRows(List.of(B));
		assertEquals(B, _selected.get());

		_selected.set(A1);
		assertEquals(Set.of(A1), control.getSelectedKeys());
	}

	/** The row rules decide which row object may be dragged. */
	public void testRowRulesDecidePerRow() throws Exception {
		TableViewControl<?> control = createControl(VIEW);

		assertTrue(control.isDragEnabled());
		assertEquals(KIND, control.dragKind());
		assertTrue(control.isDraggable(A));
		assertFalse("The row rules refuse b.", control.isDraggable(B));
	}

	/**
	 * An insertion declared before a row drop gets the parent and the row to insert before on its
	 * channels; an insertion its refusal function refuses goes to the row drop.
	 */
	public void testOrderedDropNextToRowDrop() throws Exception {
		TableViewControl<?> control = createControl(VIEW);
		control.attach();
		assertEquals("Both modes are announced in declaration order.",
			List.of(DropMode.ORDERED.wireName(), DropMode.ONTO.wireName()),
			state(control).get(DropSupport.DROP_MODES));
		expand(control, A);

		assertInserted(control, A, DropZone.UPPER, ROOT, A);
		assertInserted(control, A, DropZone.MIDDLE, A, A1);
		assertInserted(control, A, DropZone.LOWER, A, A1);
		assertInserted(control, A2, DropZone.LOWER, A, null);
		assertInserted(control, B, DropZone.LOWER, ROOT, null);
		assertInserted(control, null, DropZone.NONE, ROOT, null);

		reset();
		assertTrue(drop(control, A, B, DropZone.MIDDLE));
		assertEquals("The insertion into b is refused, the row drop takes b.", B, _target.get());
		assertEquals(UNWRITTEN, _parent.get());
		assertEquals(UNWRITTEN, _before.get());

		reset();
		assertFalse("b cannot be dragged.", drop(control, B, A, DropZone.UPPER));
		assertEquals(UNWRITTEN, _parent.get());
	}

	private void assertInserted(TableViewControl<?> control, String row, DropZone zone, Object parent,
			Object before) {
		String place = "row " + row + ", zone " + zone;
		reset();
		assertTrue(place, drop(control, A1, row, zone));
		assertEquals(place, parent, _parent.get());
		assertEquals(place, before, _before.get());
		assertEquals(place + ": the row drop must not apply.", UNWRITTEN, _target.get());
	}

	private void reset() {
		_parent.set(UNWRITTEN);
		_before.set(UNWRITTEN);
		_target.set(UNWRITTEN);
	}

	/**
	 * A parent channel on a drop on the table or onto a row, and a target channel on an insertion,
	 * are configuration errors.
	 */
	public void testMisplacedOptionsAreReported() throws Exception {
		BufferingProtocol log = new BufferingProtocol();
		new DefaultInstantiationContext(log).getInstance(readTreeTable(ERROR_VIEW));

		List<String> errors = log.getErrors();
		assertEquals("Expected the parent channels of the table and the row drop to be reported: " + errors,
			2, count(errors, "declares '" + DropConfig.PARENT_CHANNEL + "'"));
		assertEquals("Expected the target channel of the insertion to be reported: " + errors,
			1, count(errors, "'" + DropMode.ORDERED.wireName() + "' declares '" + DropConfig.TARGET_CHANNEL + "'"));
		assertEquals("No other error expected: " + errors, 3, errors.size());
	}

	private static long count(List<String> errors, String part) {
		return errors.stream().filter(error -> error.contains(part)).count();
	}

	/**
	 * Drops the row of the given object in the given zone of the row of the given target, beside
	 * the rows for a {@code null} target.
	 *
	 * @return Whether the drop was applied.
	 */
	private static boolean drop(TableViewControl<?> control, String dragged, String target, DropZone zone) {
		Map<String, Object> arguments = new HashMap<>();
		arguments.put(DropArguments.SOURCE, control.getID());
		arguments.put(DropArguments.KEYS, rowKey(control, dragged));
		arguments.put(DropArguments.SELECTION, Boolean.FALSE);
		arguments.put(DropArguments.ZONE, zone.wireName());
		if (target != null) {
			arguments.put(DropArguments.TARGET_KEY, rowKey(control, target));
		}
		return control.executeClientCommand(DropSupport.CMD_DROP, arguments).isSuccess();
	}

	private static void expand(TableViewControl<?> control, String object) {
		control.executeCommand(TableViewControl.CMD_EXPAND,
			Map.of(ExpandArguments.ROW_INDEX, Integer.valueOf(rows(control).indexOf(object)),
				ExpandArguments.EXPANDED, Boolean.TRUE));
	}

	/** The client-side key of the displayed row of the given object. */
	private static String rowKey(TableViewControl<?> control, String object) {
		int index = rows(control).indexOf(object);
		assertTrue("No row displayed for " + object, index >= 0);
		return ROW_KEY_PREFIX + index;
	}

	/** The objects of the displayed rows, in display order. */
	private static List<Object> rows(TableViewControl<?> control) {
		List<Object> result = new ArrayList<>();
		for (Row<?> row : control.getView().rows(0, control.getView().rowCount())) {
			result.add(row.data());
		}
		return result;
	}

	/** The control of the tree table of the given view. */
	private TableViewControl<?> createControl(String view) throws ConfigurationException {
		DefaultInstantiationContext instantiation = new DefaultInstantiationContext(TestTreeTableElement.class);
		TreeTableElement element = (TreeTableElement) instantiation.getInstance(readTreeTable(view));
		instantiation.checkErrors();
		return (TableViewControl<?>) element.createControl(_context);
	}

	/** The {@code <tree-table>} configuration of the given view. */
	private static TreeTableElement.Config readTreeTable(String view) throws ConfigurationException {
		ViewElement.Config config = ViewLoader.parseConfig(
			List.of(new ClassRelativeBinaryContent(TestTreeTableElement.class, view)));
		return (TreeTableElement.Config) config.getContent();
	}

	/** The state of the given control as the client parses it. */
	private static Map<?, ?> state(TableViewControl<?> control) {
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
	 * @implNote The tree, the columns and the rules are TL-Script expressions, whose compilation and
	 *           evaluation need the application model and the
	 *           {@link com.top_logic.knowledge.service.KnowledgeBase} they are executed against.
	 */
	public static Test suite() {
		return KBSetup.getSingleKBTest(TestTreeTableElement.class,
			ServiceTestSetup.createStarterFactoryForModules(
				TypeIndex.Module.INSTANCE, SearchBuilder.Module.INSTANCE, ModelService.Module.INSTANCE,
				ColumnProviderService.Module.INSTANCE, FieldControlService.Module.INSTANCE));
	}

}
