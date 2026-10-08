/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.element;

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
import com.top_logic.layout.react.control.dnd.DropMode;
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
import com.top_logic.layout.view.element.TableElement;
import com.top_logic.layout.view.form.FieldControlService;
import com.top_logic.layout.view.table.ColumnProviderService;
import com.top_logic.model.search.expr.config.SearchBuilder;
import com.top_logic.util.model.ModelService;

/**
 * Tests the rules of a {@code <table>}'s {@code <drag>} and {@code <drop>} on the control the table
 * is built as: the table-wide rules followed live over their input channel, the row rules deciding
 * per row.
 */
public class TestTableElementDragDrop extends BasicTestCase {

	/** The view declaring a restricted drag and a restricted drop. */
	private static final String VIEW = "test-table-dnd.view.xml";

	/** A view declaring target rules on a drop on the table as a whole. */
	private static final String ERROR_VIEW = "test-table-dnd-error.view.xml";

	/** Name of the channel the table-wide drag rules decide over. */
	private static final String DRAG_ALLOWED = "dragAllowed";

	/** Name of the channel the table-wide drop rules decide over. */
	private static final String DROP_ALLOWED = "dropAllowed";

	/** The kind the rows are dragged as, and the drop accepts. */
	private static final String ROW_KIND = "value";

	/** The row the row rules of the test view let be dragged. */
	private static final String FREE = "free";

	/** The row the row rules of the test view refuse. */
	private static final String LOCKED = "locked";

	/**
	 * State key holding the kinds a table accepts a drop of.
	 *
	 * @implNote Restated here because {@link TableViewControl} keeps it private.
	 */
	private static final String DROP_ACCEPTS = "dropAccepts";

	/**
	 * State key holding the modes of the drop operations a table offers.
	 *
	 * @implNote Restated here because {@link TableViewControl} keeps it private.
	 */
	private static final String DROP_MODES = "dropModes";

	private ViewChannel _dragAllowed;

	private ViewChannel _dropAllowed;

	private ViewContext _context;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		_dragAllowed = new DefaultViewChannel(DRAG_ALLOWED);
		_dropAllowed = new DefaultViewChannel(DROP_ALLOWED);
		_context = new DefaultViewContext(
			new DefaultReactContext("", "test", new SSEUpdateQueue(), new ReactWindowRegistry("test")));
		_context.registerChannel(DRAG_ALLOWED, _dragAllowed);
		_context.registerChannel(DROP_ALLOWED, _dropAllowed);
	}

	@Override
	protected void tearDown() throws Exception {
		_context = null;
		_dropAllowed = null;
		_dragAllowed = null;

		super.tearDown();
	}

	/**
	 * While the table-wide drag rules refuse, the table is no drag source; a new value of their
	 * input makes it one again, and back.
	 */
	public void testTableWideDragRulesAreFollowedLive() throws Exception {
		TableViewControl<?> control = createControl();
		control.attach();

		assertFalse("The rules refuse without input, so the rows are not draggable.", control.isDragEnabled());
		assertEquals("The kind is declared whether or not dragging is enabled.", ROW_KIND, control.dragKind());

		_dragAllowed.set("yes");
		assertTrue("The input allows dragging now.", control.isDragEnabled());

		_dragAllowed.set(null);
		assertFalse("Without input, dragging is refused again.", control.isDragEnabled());

		control.cleanupTree();
		_dragAllowed.set("yes");
		assertFalse("A disposed table follows its rules no more.", control.isDragEnabled());
	}

	/**
	 * A row the row rules refuse is not draggable, while every other row is.
	 */
	public void testRowRulesDecidePerRow() throws Exception {
		_dragAllowed.set("yes");
		TableViewControl<?> control = createControl();

		assertTrue(control.isDragEnabled());
		assertEquals(ROW_KIND, control.dragKind());
		assertTrue("The free row may be dragged.", control.isDraggable(FREE));
		assertFalse("The locked row is refused by the row rules.", control.isDraggable(LOCKED));
	}

	/**
	 * The table-wide drop rules decide what the table announces to accept, followed live.
	 */
	public void testTableWideDropRulesAreFollowedLive() throws Exception {
		TableViewControl<?> control = createControl();
		control.attach();

		assertEquals("A refused drop is not announced.", List.of(), state(control).get(DROP_ACCEPTS));
		assertEquals("A refused drop announces no mode.", List.of(), state(control).get(DROP_MODES));

		_dropAllowed.set("yes");
		assertEquals("The drop is announced as soon as its rules allow it.",
			List.of(ROW_KIND), state(control).get(DROP_ACCEPTS));
		assertEquals(List.of(DropMode.ONTO.wireName()), state(control).get(DROP_MODES));

		_dropAllowed.set(null);
		assertEquals(List.of(), state(control).get(DROP_ACCEPTS));
	}

	/**
	 * Target rules on a drop on the table as a whole are a configuration error: there is no target
	 * row they could decide over.
	 */
	public void testTargetRulesOnATableDropAreReported() throws Exception {
		BufferingProtocol log = new BufferingProtocol();
		new DefaultInstantiationContext(log).getInstance(readTable(ERROR_VIEW));

		List<String> errors = log.getErrors();
		assertTrue("Expected the target rules to be reported: " + errors,
			errors.stream().anyMatch(error -> error.contains(DropConfig.TARGET_EXECUTABILITY)));
	}

	/** The control of the table of the {@link #VIEW test view}. */
	private TableViewControl<?> createControl() throws ConfigurationException {
		DefaultInstantiationContext instantiation = new DefaultInstantiationContext(TestTableElementDragDrop.class);
		TableElement element = (TableElement) instantiation.getInstance(readTable(VIEW));
		instantiation.checkErrors();
		return (TableViewControl<?>) element.createControl(_context);
	}

	/** The {@code <table>} configuration of the given view. */
	private static TableElement.Config readTable(String view) throws ConfigurationException {
		ViewElement.Config config = ViewLoader.parseConfig(
			List.of(new ClassRelativeBinaryContent(TestTableElementDragDrop.class, view)));
		return (TableElement.Config) config.getContent();
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
	 * @implNote The rows, the columns and the rules are TL-Script expressions, whose compilation and
	 *           evaluation need the application model and the
	 *           {@link com.top_logic.knowledge.service.KnowledgeBase} they are executed against.
	 */
	public static Test suite() {
		return KBSetup.getSingleKBTest(TestTableElementDragDrop.class,
			ServiceTestSetup.createStarterFactoryForModules(
				TypeIndex.Module.INSTANCE, SearchBuilder.Module.INSTANCE, ModelService.Module.INSTANCE,
				ColumnProviderService.Module.INSTANCE, FieldControlService.Module.INSTANCE));
	}

}
