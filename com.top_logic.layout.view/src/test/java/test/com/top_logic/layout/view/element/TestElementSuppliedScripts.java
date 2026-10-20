/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.element;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import junit.framework.Test;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.basic.BufferingProtocol;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.ConfigurationReader;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.character.CharacterContents;
import com.top_logic.basic.json.JSON;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.layout.form.values.edit.DefaultValueModel;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.control.calendar.CalendarEvent;
import com.top_logic.layout.react.control.kanban.ReactKanbanBoardControl;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.tree.model.DefaultTreeUINodeModel;
import com.top_logic.layout.tree.model.DefaultTreeUINodeModel.DefaultTreeUINode;
import com.top_logic.layout.view.DefaultViewContext;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.DefaultViewChannel;
import com.top_logic.layout.view.channel.InputScript;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.element.CalendarElement;
import com.top_logic.layout.view.element.ExpressionCalendarModel;
import com.top_logic.layout.view.element.TreeElement;
import com.top_logic.layout.view.element.TreeFunctions;
import com.top_logic.layout.view.element.TreeStructureConfig;
import com.top_logic.layout.view.element.TreeTableElement;
import com.top_logic.layout.view.list.KanbanBoardElement;
import com.top_logic.layout.view.list.ObjectListElement;
import com.top_logic.layout.view.list.ObjectListScope;
import com.top_logic.layout.view.table.ComputedColumn;
import com.top_logic.layout.view.table.RowAsConfig;
import com.top_logic.model.search.expr.config.SearchBuilder;
import com.top_logic.model.search.ui.ScriptContextVariablesResolver;
import com.top_logic.util.error.TopLogicException;
import com.top_logic.util.model.ModelService;

/**
 * Tests the scripts of trees, object lists, kanban boards and calendars, which see the inputs of
 * their element as variables and the values the element supplies - the node, the element, the
 * column, the calendar entry and the values of an edit - under their default names or the names
 * the element gives them.
 */
public class TestElementSuppliedScripts extends BasicTestCase {

	private static final String TREE = "tree";

	private static final String TREE_TABLE = "tree-table";

	private static final String OBJECT_LIST = "object-list";

	private static final String KANBAN_BOARD = "kanban-board";

	private static final String CALENDAR = "calendar";

	private ViewContext _context;

	private ViewChannel _factor;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_context = new DefaultViewContext(
			new DefaultReactContext("", "test", new SSEUpdateQueue(), new ReactWindowRegistry("test")));
		_factor = new DefaultViewChannel("factor");
		_context.registerChannel("factor", _factor);
		_factor.set("f");
	}

	@Override
	protected void tearDown() throws Exception {
		_context = null;
		_factor = null;
		super.tearDown();
	}

	/**
	 * Tests that the functions of a tree see its inputs and the node by the name the tree gives it,
	 * in the editor and at runtime.
	 */
	public void testTree() throws Exception {
		TreeElement.Config tree = (TreeElement.Config) parse(TREE,
			"<tree inputs='factor as f' node-as='folder' root=\"$f + 'root'\""
				+ " children=\"if($folder == $f + 'root', [$folder + '/a', $folder + '/b'], [])\""
				+ " parents=\"$f + 'root'\"/>");
		assertScope(List.of("f"), tree, TreeStructureConfig.ROOT);
		assertScope(List.of("f", "folder"), tree, TreeStructureConfig.CHILDREN);
		assertScope(List.of("f", "folder"), tree, TreeStructureConfig.PARENTS);

		TreeFunctions functions = new TreeFunctions(tree);
		List<ViewChannel> inputs = List.of(_factor);
		DefaultTreeUINodeModel model = functions.treeModel(functions.builder(inputs), inputs);
		assertEquals("froot", model.getRoot().getBusinessObject());
		List<Object> children = new ArrayList<>();
		for (DefaultTreeUINode child : model.getRoot().getChildren()) {
			children.add(child.getBusinessObject());
		}
		assertEquals(List.of("froot/a", "froot/b"), children);
		assertEquals("froot", functions.parentFunction(inputs).apply("froot/a"));
	}

	/**
	 * Tests that the tree functions of a tree table see the node, while the scripts of its rows see
	 * the row.
	 */
	public void testTreeTable() throws Exception {
		TreeTableElement.Config treeTable = (TreeTableElement.Config) parse(TREE_TABLE,
			"<tree-table root='1' children='[$node]'><columns>"
				+ "<computed-column name='c' type='tl.core:String'><value>$row</value></computed-column>"
				+ "</columns></tree-table>");
		assertScope(List.of(), treeTable, TreeStructureConfig.ROOT);
		assertScope(List.of(TreeStructureConfig.DEFAULT_NODE_AS), treeTable, TreeStructureConfig.CHILDREN);
		assertScope(List.of(RowAsConfig.DEFAULT_ROW_AS), treeTable.getColumns().getColumns().get(0),
			ComputedColumn.Config.VALUE);
	}

	/**
	 * Tests that a tree reports an input named like its node.
	 */
	public void testNodeClashesWithInput() throws Exception {
		assertInstantiationFails(TREE, "<tree inputs='factor as node' root='$node' children='[]'/>",
			TreeStructureConfig.NODE_AS);
	}

	/**
	 * Tests that a tree script with a lambda header is an error naming the variables.
	 */
	public void testTreeLambdaHeaderIsAnError() throws Exception {
		assertInstantiationFails(TREE, "<tree root='1' children='n -> []'/>", "$node");
	}

	/**
	 * Tests that the link and remove functions of an object list see its inputs and the element by
	 * the name the list gives it.
	 */
	public void testObjectList() throws Exception {
		ObjectListElement.Config list = (ObjectListElement.Config) parse(OBJECT_LIST,
			"<object-list inputs='factor as f' element-as='comment' items='[]'"
				+ " link=\"$f + ':link:' + $comment\" remove=\"$f + ':remove:' + $comment\">"
				+ "<item><text input='element'/></item>"
				+ "</object-list>");
		assertScope(List.of("f", "comment"), list, ObjectListElement.Config.LINK);
		assertScope(List.of("f", "comment"), list, ObjectListElement.Config.REMOVE);

		ObjectListScope scope = new ObjectListScope(List.of(_factor),
			InputScript.compile(list, ObjectListElement.Config.LINK),
			InputScript.compile(list, ObjectListElement.Config.REMOVE));
		assertEquals("f:link:x", scope.linkElement("x"));
		assertEquals("f:remove:x", scope.removeElement("x"));

		ObjectListElement.Config defaultName = (ObjectListElement.Config) parse(OBJECT_LIST,
			"<object-list items='[]' link='$element'><item><text input='element'/></item></object-list>");
		assertScope(List.of("element"), defaultName, ObjectListElement.Config.LINK);
	}

	/**
	 * Tests that the scripts of a kanban board see its inputs, the object of a card and the column
	 * by the names the board gives them, and the reordered objects.
	 */
	public void testKanbanBoard() throws Exception {
		KanbanBoardElement.Config board = (KanbanBoardElement.Config) parse(KANBAN_BOARD,
			"<kanban-board inputs='factor as f' element-as='card' column-as='lane'"
				+ " columns=\"[$f + 'x', $f + 'y']\" items=\"['a', 'y', 'b']\""
				+ " column=\"$f + if($card == 'y', 'y', 'x')\" column-label=\"$lane + '!' + $f\""
				+ " on-reorder=\"throw($lane + ':' + $objects.size() + ':' + $f)\">"
				+ "<card><text input='element'/></card>"
				+ "</kanban-board>");
		assertScope(List.of("f", "card"), board, KanbanBoardElement.Config.COLUMN);
		assertScope(List.of("f", "lane"), board, KanbanBoardElement.Config.COLUMN_LABEL);
		assertScope(List.of("f", "lane", KanbanBoardElement.Config.OBJECTS_VARIABLE), board,
			KanbanBoardElement.Config.ON_REORDER);

		ReactKanbanBoardControl control =
			(ReactKanbanBoardControl) instantiate(board).createControl(_context);
		List<Map<String, Object>> columns = columns(control);
		assertEquals(2, columns.size());
		assertEquals("fx!f", columns.get(0).get(ReactKanbanBoardControl.COLUMN_LABEL));
		assertEquals(2, ((Number) columns.get(0).get(ReactKanbanBoardControl.COLUMN_COUNT)).intValue());
		assertEquals("fy!f", columns.get(1).get(ReactKanbanBoardControl.COLUMN_LABEL));
		assertEquals(1, ((Number) columns.get(1).get(ReactKanbanBoardControl.COLUMN_COUNT)).intValue());

		try {
			InputScript.compile(board, KanbanBoardElement.Config.ON_REORDER)
				.execute("f", "fx", List.of("x", "x"));
			fail("The reorder function throws.");
		} catch (TopLogicException ex) {
			assertTrue(ex.getMessage(), String.valueOf(ex.getMessage()).contains("fx:2:f"));
		}
	}

	/**
	 * Tests that the scripts of a calendar see its inputs, the entry by the name the calendar gives
	 * it, and the values of an edit.
	 */
	public void testCalendar() throws Exception {
		CalendarElement.Config calendar = (CalendarElement.Config) parse(CALENDAR,
			"<calendar inputs='factor as f' entry-as='appointment' objects='[]'>"
				+ "<event><start>null</start><title>$appointment + $f</title></event>"
				+ "<on-move>throw($appointment + ':' + $f + ':' + ($start == $end))</on-move>"
				+ "<on-resize>throw($appointment + ':' + $f + ':' + ($end != null))</on-resize>"
				+ "<on-create>$title + ':' + $f + ':' + $allDay + ':' + ($start == $end)</on-create>"
				+ "</calendar>");
		CalendarElement.EventConfig event = calendar.getEvent();
		assertScope(List.of("f", "appointment"), event, CalendarElement.EventConfig.START);
		assertScope(List.of("f", "appointment"), event, CalendarElement.EventConfig.TITLE);
		assertScope(List.of("f", "appointment", "start", "end"), calendar, CalendarElement.Config.ON_MOVE);
		assertScope(List.of("f", "appointment", "end"), calendar, CalendarElement.Config.ON_RESIZE);
		assertScope(List.of("f", "start", "end", "allDay", "title"), calendar, CalendarElement.Config.ON_CREATE);

		ExpressionCalendarModel model = new ExpressionCalendarModel(List.of(_factor), List.of(),
			new ExpressionCalendarModel.EventExprs(
				InputScript.compile(event, CalendarElement.EventConfig.START),
				null, null,
				InputScript.compile(event, CalendarElement.EventConfig.TITLE),
				null, null, null, null, null),
			InputScript.compile(calendar, CalendarElement.Config.ON_MOVE),
			InputScript.compile(calendar, CalendarElement.Config.ON_RESIZE),
			InputScript.compile(calendar, CalendarElement.Config.ON_CREATE));

		Date date = new Date();
		CalendarEvent created = model.createEvent(date, date, true, "t");
		assertEquals("t:f:true:true", created.getBusinessObject());
		assertEquals("t:f:true:truef", created.getTitle());

		try {
			model.moveEvent(created, date, date);
			fail("The move function throws.");
		} catch (TopLogicException ex) {
			assertTrue(ex.getMessage(), String.valueOf(ex.getMessage()).contains("t:f:true:true:f:true"));
		}
		try {
			model.resizeEvent(created, date);
			fail("The resize function throws.");
		} catch (TopLogicException ex) {
			assertTrue(ex.getMessage(), String.valueOf(ex.getMessage()).contains("t:f:true:true:f:true"));
		}
	}

	/**
	 * Tests that a calendar reports an input named like the value of an edit, which cannot be
	 * renamed.
	 */
	public void testCalendarFixedNameClashesWithInput() throws Exception {
		assertInstantiationFails(CALENDAR,
			"<calendar inputs='factor as start' objects='[]'><event><start>null</start></event>"
				+ "<on-create>$start</on-create></calendar>",
			"factor as start");
	}

	@SuppressWarnings("unchecked")
	private static List<Map<String, Object>> columns(ReactKanbanBoardControl control) {
		try {
			Map<String, Object> state = (Map<String, Object>) JSON.fromString(control.stateAsJSON());
			return (List<Map<String, Object>>) state.get(ReactKanbanBoardControl.COLUMNS);
		} catch (JSON.ParseException ex) {
			throw new AssertionError(ex);
		}
	}

	private static UIElement instantiate(PolymorphicConfiguration<? extends UIElement> config)
			throws ConfigurationException {
		DefaultInstantiationContext context = new DefaultInstantiationContext(TestElementSuppliedScripts.class);
		UIElement result = context.getInstance(config);
		context.checkErrors();
		return result;
	}

	private static void assertScope(List<String> expected, ConfigurationItem config, String property) {
		assertEquals("Editor scope of '" + property + "'.", expected, ScriptContextVariablesResolver
			.resolve(new DefaultValueModel(config, config.descriptor().getProperty(property))));
		assertEquals("Runtime scope of '" + property + "'.", expected,
			InputScript.variables(config, config.descriptor().getProperty(property)));
	}

	/**
	 * Asserts that instantiating the given configuration reports an error whose text contains the
	 * given fragment.
	 */
	private static void assertInstantiationFails(String tag, String xml, String expected)
			throws ConfigurationException {
		BufferingProtocol log = new BufferingProtocol();
		DefaultInstantiationContext instantiation = new DefaultInstantiationContext(log);
		PolymorphicConfiguration<?> config = (PolymorphicConfiguration<?>) parse(tag, xml);
		instantiation.getInstance(config);
		assertTrue("The configuration must be rejected: " + xml, log.hasErrors());
		assertTrue("The error names '" + expected + "': " + log.getError(), log.getError().contains(expected));
	}

	private static ConfigurationItem parse(String tag, String xml) throws ConfigurationException {
		DefaultInstantiationContext context = new DefaultInstantiationContext(TestElementSuppliedScripts.class);
		Map<String, ConfigurationDescriptor> descriptors = Map.of(
			TREE, TypedConfiguration.getConfigurationDescriptor(TreeElement.Config.class),
			TREE_TABLE, TypedConfiguration.getConfigurationDescriptor(TreeTableElement.Config.class),
			OBJECT_LIST, TypedConfiguration.getConfigurationDescriptor(ObjectListElement.Config.class),
			KANBAN_BOARD, TypedConfiguration.getConfigurationDescriptor(KanbanBoardElement.Config.class),
			CALENDAR, TypedConfiguration.getConfigurationDescriptor(CalendarElement.Config.class));
		ConfigurationReader reader = new ConfigurationReader(context, Collections.singletonMap(tag, descriptors.get(tag)));
		reader.setSource(CharacterContents.newContent(xml));
		ConfigurationItem result = reader.read();
		context.checkErrors();
		return result;
	}

	/**
	 * The suite of tests.
	 *
	 * @implNote The scripts are compiled and evaluated against the application model and the
	 *           knowledge base.
	 */
	public static Test suite() {
		return KBSetup.getSingleKBTest(TestElementSuppliedScripts.class,
			ServiceTestSetup.createStarterFactoryForModules(
				TypeIndex.Module.INSTANCE, SearchBuilder.Module.INSTANCE, ModelService.Module.INSTANCE));
	}
}
