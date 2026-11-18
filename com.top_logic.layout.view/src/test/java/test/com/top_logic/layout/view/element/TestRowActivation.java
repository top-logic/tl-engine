/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.element;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.Test;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;
import test.com.top_logic.layout.react.control.CommandFailures;
import test.com.top_logic.layout.view.command.ArgumentsProbe;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.io.character.CharacterContents;
import com.top_logic.basic.json.JSON;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.layout.provider.LabelProviderService;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.control.table.ActivateRowArguments;
import com.top_logic.layout.react.control.table.TableViewControl;
import com.top_logic.layout.react.control.tree.ActivateNodeArguments;
import com.top_logic.layout.react.control.tree.ReactTreeControl;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.view.DefaultViewContext;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.ViewElement;
import com.top_logic.layout.view.ViewLoader;
import com.top_logic.layout.view.channel.DefaultViewChannel;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.command.DisabledIf;
import com.top_logic.layout.view.command.GenericViewCommand;
import com.top_logic.layout.view.command.ViewCommandModel;
import com.top_logic.layout.view.command.VisibleIf;
import com.top_logic.layout.view.element.AbstractTableElement;
import com.top_logic.layout.view.element.TreeElement;
import com.top_logic.layout.view.element.TreeTableElement;
import com.top_logic.layout.view.form.FieldControlService;
import com.top_logic.layout.view.table.ColumnProviderService;
import com.top_logic.model.search.expr.config.SearchBuilder;
import com.top_logic.table.Row;
import com.top_logic.util.model.ModelService;

/**
 * Tests the activation of a row of a {@link TreeTableElement table} and of a node of a
 * {@link TreeElement tree} whose activation command decides by executability rules over the
 * activated object: an object the rules hide or disable is selected and nothing else happens, an
 * object the rules accept runs the command with it.
 *
 * @see ViewCommandModel#activate(Object)
 */
public class TestRowActivation extends BasicTestCase {

	/** Name of the views given inline. */
	private static final String INLINE_VIEW = "inline-activation.view.xml";

	/** Name of the channel holding the object the rows are the children of. */
	private static final String INPUT = "input";

	/** Name of the selection channel. */
	private static final String SELECTED = "selected";

	/** The object the rows are the children of, displayed as no row. */
	private static final String ROOT = "root";

	/** A row the activation command accepts. */
	private static final String OPEN = "open";

	/** A row the {@link VisibleIf} rule of the activation command hides the command for. */
	private static final String HIDDEN = "hidden";

	/** A row the {@link DisabledIf} rule of the activation command disables the command for. */
	private static final String DISABLED = "disabled";

	/**
	 * The activation command, deciding by rules over the activated object named by the given
	 * variable, and recording its runs in the {@link ArgumentsProbe}.
	 */
	private static String onActivate(String variable) {
		return """
			<on-activate class="%s">
				<executability>
					<%s expr='$%s != "%s"'/>
					<%s expr='if($%s == "%s", "The row is locked.")'/>
				</executability>
				<action class="%s"/>
			</on-activate>
			""".formatted(GenericViewCommand.class.getName(),
			VisibleIf.Config.TAG_NAME, variable, HIDDEN,
			DisabledIf.Config.TAG_NAME, variable, DISABLED,
			ArgumentsProbe.class.getName());
	}

	/** The children of the {@link #ROOT}, in display order. */
	private static final List<String> CHILD_OBJECTS = List.of(OPEN, HIDDEN, DISABLED);

	/** The children of the {@link #ROOT}, as TL-Script. */
	private static final String CHILDREN =
		"if($node == \"" + ROOT + "\", [\"" + String.join("\", \"", CHILD_OBJECTS) + "\"], [])";

	/** A tree table over the children of the {@link #ROOT} with an activation command. */
	private static final String TREE_TABLE_VIEW = """
		<view>
			<tree-table selection="%s">
				<inputs>
					<input channel="%s"/>
				</inputs>
				<root>$%s</root>
				<children>%s</children>
				<columns>
					<computed-column name="value" type="tl.core:String">
						<value>$row</value>
					</computed-column>
				</columns>
				%s
			</tree-table>
		</view>
		""".formatted(SELECTED, INPUT, INPUT, CHILDREN,
		onActivate(AbstractTableElement.Config.DEFAULT_ACTIVATE_AS));

	/** A tree over the children of the {@link #ROOT} with an activation command. */
	private static final String TREE_VIEW = """
		<view>
			<tree selection="%s">
				<inputs>
					<input channel="%s"/>
				</inputs>
				<root>$%s</root>
				<children>%s</children>
				%s
			</tree>
		</view>
		""".formatted(SELECTED, INPUT, INPUT, CHILDREN,
		onActivate(TreeElement.Config.DEFAULT_ACTIVATE_AS));

	private ViewContext _context;

	private ViewChannel _selected;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		ArgumentsProbe.reset();
		_context = new DefaultViewContext(
			new DefaultReactContext("", "test", new SSEUpdateQueue(), new ReactWindowRegistry("test")));
		_selected = register(SELECTED);
		register(INPUT).set(ROOT);
	}

	@Override
	protected void tearDown() throws Exception {
		ArgumentsProbe.reset();
		_context = null;
		_selected = null;

		super.tearDown();
	}

	private ViewChannel register(String name) {
		ViewChannel channel = new DefaultViewChannel(name);
		_context.registerChannel(name, channel);
		return channel;
	}

	/**
	 * Activating a row the rules hide the activation command for selects the row and does nothing
	 * else.
	 */
	public void testTableHiddenRowOnlySelects() throws Exception {
		assertTableRowOnlySelects(HIDDEN);
	}

	/**
	 * Activating a row the rules disable the activation command for selects the row and does
	 * nothing else.
	 */
	public void testTableDisabledRowOnlySelects() throws Exception {
		assertTableRowOnlySelects(DISABLED);
	}

	private void assertTableRowOnlySelects(String row) throws Exception {
		TableViewControl<?> control = createTableControl();
		control.attach();
		try {
			assertNull("Activating a row the rules reject reports nothing.",
				CommandFailures.outcome(() -> activateRow(control, row)));
			assertEquals("The activated row is selected.", Set.of(row), control.getSelectedKeys());
			assertEquals("The command does not run.", List.of(), ArgumentsProbe.SEEN);
		} finally {
			control.detach();
		}
	}

	/**
	 * Activating a row the rules accept selects it and runs the command with it.
	 */
	public void testTableExecutableRowRunsCommand() throws Exception {
		TableViewControl<?> control = createTableControl();
		control.attach();
		try {
			activateRow(control, OPEN);
			assertEquals(Set.of(OPEN), control.getSelectedKeys());
			assertEquals("The command runs once.", 1, ArgumentsProbe.SEEN.size());
			assertEquals("The command runs with the activated row.", OPEN, ArgumentsProbe.SEEN.get(0).value());
		} finally {
			control.detach();
		}
	}

	/**
	 * Activating a node the rules hide or disable the activation command for selects the node and
	 * does nothing else; activating a node the rules accept runs the command with it.
	 */
	public void testTree() throws Exception {
		ReactTreeControl control = createTreeControl();
		control.attach();
		try {
			for (String node : List.of(HIDDEN, DISABLED)) {
				assertNull("Activating a node the rules reject reports nothing: " + node,
					CommandFailures.outcome(() -> activateNode(control, node)));
				assertEquals("The activated node is selected.", node, _selected.get());
				assertEquals("The command does not run for " + node, List.of(), ArgumentsProbe.SEEN);
			}

			activateNode(control, OPEN);
			assertEquals(OPEN, _selected.get());
			assertEquals("The command runs once.", 1, ArgumentsProbe.SEEN.size());
			assertEquals("The command runs with the activated node's object.", OPEN,
				ArgumentsProbe.SEEN.get(0).value());
		} finally {
			control.detach();
		}
	}

	private static void activateRow(TableViewControl<?> control, String object) {
		control.executeClientCommand(TableViewControl.CMD_ACTIVATE,
			Map.of(ActivateRowArguments.ROW_ID, control.rowIdAt(rows(control).indexOf(object))));
	}

	private static void activateNode(ReactTreeControl control, String object) {
		control.executeClientCommand(ReactTreeControl.ACTIVATE_COMMAND,
			Map.of(ActivateNodeArguments.NODE_ID, nodeId(control, object)));
	}

	/** The objects of the displayed rows, in display order. */
	private static List<Object> rows(TableViewControl<?> control) {
		List<Object> result = new ArrayList<>();
		for (Row<?> row : control.getView().rows(0, control.getView().rowCount())) {
			result.add(row.data());
		}
		return result;
	}

	/**
	 * The id of the displayed node showing the given object.
	 *
	 * @implNote The children of the {@link #ROOT} are leaves displayed in the order of
	 *           {@link #CHILD_OBJECTS}.
	 */
	private static String nodeId(ReactTreeControl control, String object) {
		List<?> nodes = (List<?>) state(control).get(ReactTreeControl.NODES);
		return (String) ((Map<?, ?>) nodes.get(CHILD_OBJECTS.indexOf(object))).get(ReactTreeControl.NODE_ID);
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

	private TableViewControl<?> createTableControl() throws ConfigurationException {
		TreeTableElement element = (TreeTableElement) instantiate(TREE_TABLE_VIEW);
		return (TableViewControl<?>) element.createControl(_context);
	}

	private ReactTreeControl createTreeControl() throws ConfigurationException {
		TreeElement element = (TreeElement) instantiate(TREE_VIEW);
		return (ReactTreeControl) element.createControl(_context);
	}

	private static Object instantiate(String view) throws ConfigurationException {
		ViewElement.Config config =
			ViewLoader.parseConfig(List.of(CharacterContents.newContent(view, INLINE_VIEW)));
		DefaultInstantiationContext instantiation = new DefaultInstantiationContext(TestRowActivation.class);
		Object element = instantiation.getInstance(config.getContent());
		instantiation.checkErrors();
		return element;
	}

	/**
	 * The suite of tests.
	 *
	 * @implNote The tree, the columns and the rules are TL-Script expressions, whose compilation and
	 *           evaluation need the application model and the
	 *           {@link com.top_logic.knowledge.service.KnowledgeBase} they are executed against.
	 */
	public static Test suite() {
		return KBSetup.getSingleKBTest(TestRowActivation.class,
			ServiceTestSetup.createStarterFactoryForModules(
				TypeIndex.Module.INSTANCE, SearchBuilder.Module.INSTANCE, ModelService.Module.INSTANCE,
				ColumnProviderService.Module.INSTANCE, FieldControlService.Module.INSTANCE,
				LabelProviderService.Module.INSTANCE));
	}

}
