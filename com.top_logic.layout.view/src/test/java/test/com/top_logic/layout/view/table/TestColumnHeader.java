/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.table;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import junit.framework.Test;

import test.com.top_logic.layout.view.security.AbstractModelAccessTest;

import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.ConfigurationReader;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.character.CharacterContents;
import com.top_logic.basic.json.JSON;
import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.gui.ThemeFactory;
import com.top_logic.layout.provider.LabelProviderService;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.control.table.TableViewControl;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.view.DefaultViewContext;
import com.top_logic.layout.view.channel.DefaultViewChannel;
import com.top_logic.layout.view.element.TableElement;
import com.top_logic.layout.view.form.FieldControlService;
import com.top_logic.layout.view.table.AbstractColumnDeclaration;
import com.top_logic.layout.view.table.ColumnProviderService;
import com.top_logic.layout.view.table.DynamicColumns;
import com.top_logic.model.TLClassifier;
import com.top_logic.model.TLEnumeration;
import com.top_logic.model.TLModule;
import com.top_logic.model.annotate.ui.TLColor;
import com.top_logic.model.annotate.ui.ValueColor;
import com.top_logic.model.impl.TLModelImpl;
import com.top_logic.model.search.expr.config.SearchBuilder;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.table.Column;
import com.top_logic.util.error.TopLogicException;

/**
 * Tests the heading of a column of a {@code <table>}:
 * {@value AbstractColumnDeclaration.Config#HEADER_ROLE} and
 * {@value AbstractColumnDeclaration.Config#HEADER_CLASS} of a column and of a set of
 * {@link DynamicColumns}, and a classifier taken as a color role.
 *
 * <p>
 * The table is built from its view configuration and its input channels are set as a view sets
 * them; what is checked is what the control sends to the client.
 * </p>
 */
public class TestColumnHeader extends AbstractModelAccessTest {

	/** Tag of the table configurations under test. */
	private static final String TABLE = "table";

	/** Name of the channel the headings read. */
	private static final String SHOW = "show";

	/** Name of the channel holding the project the table shows. */
	private static final String PROJECT_CHANNEL = "project";

	/** State key of the columns the client displays. */
	private static final String COLUMNS = "columns";

	/** Key of the name of a column in the state the client receives. */
	private static final String COLUMN_NAME = "name";

	/** Key of the role of the heading of a column in the state the client receives. */
	private static final String HEADER_ROLE = "headerRole";

	/** Key of the class of the heading of a column in the state the client receives. */
	private static final String HEADER_CLASS = "headerClass";

	/** Key of the class of all cells of a column in the state the client receives. */
	private static final String CSS_CLASS = "cssClass";

	/** State key of the rows the client displays. */
	private static final String ROWS = "rows";

	/** Row property holding the classes of its cells, keyed by column name. */
	private static final String CELL_CLASSES = "cellClasses";

	/** Row property holding the color roles of its cells, keyed by column name. */
	private static final String CELL_ROLES = "cellRoles";

	/** Name of the dynamic columns under test. */
	private static final String DYNAMIC = "letters";

	/** Name of the computed column under test. */
	private static final String COMPUTED = "computed";

	/** Class put on the heading under test. */
	private static final String HEAD_CLASS = "my-head";

	private DefaultViewContext _context;

	private DefaultViewChannel _show;

	private TLClassifier _warning;

	private TLClassifier _uncolored;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_context = new DefaultViewContext(
			new DefaultReactContext("", "test", new SSEUpdateQueue(), new ReactWindowRegistry("test")));
		_show = channel(SHOW);
		channel(PROJECT_CHANNEL).set(_project);

		TLModelImpl model = new TLModelImpl();
		model.addCoreModule();
		TLModule module = TLModelUtil.addModule(model, "test");
		TLEnumeration phase = TLModelUtil.addEnumeration(module, "Phase");
		_warning = TLModelUtil.addClassifier(phase, "late");
		_warning.setAnnotation(roleColor(ValueColor.WARNING));
		_uncolored = TLModelUtil.addClassifier(phase, "plain");
	}

	@Override
	protected void tearDown() throws Exception {
		_uncolored = null;
		_warning = null;
		_show = null;
		_context = null;
		super.tearDown();
	}

	private DefaultViewChannel channel(String name) {
		DefaultViewChannel result = new DefaultViewChannel(name);
		_context.registerChannel(name, result);
		return result;
	}

	/**
	 * Role and class of the heading reach the state of the column, apart from the class of all its
	 * cells, and no data cell carries them.
	 */
	public void testHeaderOnlyOnHeading() throws Exception {
		becomeUser(_root);
		TableViewControl<?> control = readOnly("<table inputs='project' types='" + qualified(PROJECT)
			+ "' rows='[$project]'><columns>"
			+ "<column attribute='" + NAME + "' header-role=\"'warning'\" header-class=\"'" + HEAD_CLASS + "'\"/>"
			+ "</columns></table>");
		control.attach();

		Map<?, ?> column = sentColumn(control, NAME);
		assertEquals(ValueColor.WARNING.getExternalName(), column.get(HEADER_ROLE));
		assertEquals(HEAD_CLASS, column.get(HEADER_CLASS));
		assertNull("The class of the heading is not the class of all cells.", column.get(CSS_CLASS));

		Map<?, ?> row = firstRow(control);
		assertFalse(rowMap(row, CELL_CLASSES).containsKey(NAME));
		assertFalse(rowMap(row, CELL_ROLES).containsKey(NAME));
	}

	/**
	 * A new value of an input computes the heading anew, without the column being built again.
	 */
	public void testHeaderFollowsInput() throws Exception {
		becomeUser(_root);
		_show.set("info");
		TableViewControl<?> control = readOnly("<table inputs='show' types='" + qualified(PROJECT) + "' rows='[]'>"
			+ "<columns><column attribute='" + NAME + "' header-role='$show' header-class='$show'/></columns>"
			+ "</table>");
		Column<?, ?> before = control.getView().column(NAME);
		assertEquals("info", sentColumn(control, NAME).get(HEADER_ROLE));

		_show.set("error");

		assertEquals("error", sentColumn(control, NAME).get(HEADER_ROLE));
		assertEquals("error", sentColumn(control, NAME).get(HEADER_CLASS));
		assertSame("The column is not built again.", before, control.getView().column(NAME));

		control.cleanupTree();
	}

	/**
	 * A set of dynamic columns computes the heading of each column from the object it stands for.
	 */
	public void testDynamicColumnsPerColumn() throws Exception {
		becomeUser(_root);
		TableViewControl<?> control = readOnly("<table types='" + qualified(PROJECT) + "' rows='[]'>"
			+ "<columns><dynamic-columns name='" + DYNAMIC + "' columns=\"['a', 'b', 'c']\" value='$column'"
			+ " header-role=\"$column == 'a' ? 'info' : ($column == 'b' ? 'category-2' : null)\""
			+ " header-class=\"'col-' + $column\"/>"
			+ "</columns></table>");

		assertEquals("info", sentColumn(control, dynamic("a")).get(HEADER_ROLE));
		assertEquals("category-2", sentColumn(control, dynamic("b")).get(HEADER_ROLE));
		assertNull(sentColumn(control, dynamic("c")).get(HEADER_ROLE));
		assertEquals("col-a", sentColumn(control, dynamic("a")).get(HEADER_CLASS));
		assertEquals("col-c", sentColumn(control, dynamic("c")).get(HEADER_CLASS));
	}

	/**
	 * A classifier names the role of its color annotation, a classifier without one names no
	 * role.
	 */
	public void testClassifierAsRole() throws Exception {
		becomeUser(_root);
		_show.set(_warning);
		TableViewControl<?> control = readOnly("<table inputs='show' types='" + qualified(PROJECT) + "' rows='[]'>"
			+ "<columns><column attribute='" + NAME + "' header-role='$show'/></columns></table>");

		assertEquals(ValueColor.WARNING.getExternalName(), sentColumn(control, NAME).get(HEADER_ROLE));

		_show.set(_uncolored);

		assertNull(sentColumn(control, NAME).get(HEADER_ROLE));
	}

	/**
	 * A text naming no role is an error.
	 */
	public void testUnknownRoleFails() throws Exception {
		becomeUser(_root);
		try {
			TableViewControl<?> control = readOnly("<table types='" + qualified(PROJECT) + "' rows='[]'>"
				+ "<columns><column attribute='" + NAME + "' header-role=\"'WARNING'\"/></columns></table>");
			sentState(control);
			fail("A text naming no role is an error.");
		} catch (RuntimeException ex) {
			assertTrue("Not the error of an unknown role: " + ex, causedByTopLogicException(ex));
		}
	}

	/**
	 * Nothing and an empty text leave the heading without role and class.
	 */
	public void testEmptyResultLeavesHeadingPlain() throws Exception {
		becomeUser(_root);
		TableViewControl<?> control = readOnly("<table types='" + qualified(PROJECT) + "' rows='[]'>"
			+ "<columns><column attribute='" + NAME + "' header-role=\"''\" header-class='null'/></columns></table>");

		Map<?, ?> column = sentColumn(control, NAME);
		assertFalse(column.containsKey(HEADER_ROLE));
		assertFalse(column.containsKey(HEADER_CLASS));
	}

	/**
	 * The role of a cell may be given as a classifier as well.
	 */
	public void testCellRoleClassifier() throws Exception {
		becomeUser(_root);
		_show.set(_warning);
		TableViewControl<?> control = readOnly("<table inputs='project' types='" + qualified(PROJECT)
			+ "' rows='[$project]'><columns>"
			+ "<computed-column name='" + COMPUTED + "' inputs='show' value='$row' cell-role='$show'/>"
			+ "</columns></table>");
		control.attach();

		assertEquals(ValueColor.WARNING.getExternalName(), rowMap(firstRow(control), CELL_ROLES).get(COMPUTED));
		assertNull("The role of a cell is not the role of the heading.",
			sentColumn(control, COMPUTED).get(HEADER_ROLE));
	}

	private static boolean causedByTopLogicException(Throwable ex) {
		for (Throwable cause = ex; cause != null; cause = cause.getCause()) {
			if (cause instanceof TopLogicException) {
				return true;
			}
		}
		return false;
	}

	private static String dynamic(String column) {
		return DYNAMIC + "." + column;
	}

	private TableViewControl<?> readOnly(String xml) throws ConfigurationException {
		return (TableViewControl<?>) table(xml).createControl(_context);
	}

	/** The state the given control sends for the column of the given name. */
	private static Map<?, ?> sentColumn(TableViewControl<?> control, String name) {
		for (Object column : (List<?>) sentState(control).get(COLUMNS)) {
			Map<?, ?> state = (Map<?, ?>) column;
			if (name.equals(state.get(COLUMN_NAME))) {
				return state;
			}
		}
		throw new AssertionError("No column '" + name + "' sent.");
	}

	/** The state the given control sends for its first row. */
	private static Map<?, ?> firstRow(TableViewControl<?> control) {
		return (Map<?, ?>) ((List<?>) sentState(control).get(ROWS)).get(0);
	}

	/** The map in the given property of a row, empty where the row sends none. */
	private static Map<?, ?> rowMap(Map<?, ?> row, String property) {
		Map<?, ?> result = (Map<?, ?>) row.get(property);
		return result == null ? Map.of() : result;
	}

	/** The state of the given control as the client receives it. */
	private static Map<?, ?> sentState(TableViewControl<?> control) {
		try {
			return (Map<?, ?>) JSON.fromString(control.stateAsJSON());
		} catch (JSON.ParseException ex) {
			throw new AssertionError("Not the JSON state of a control.", ex);
		}
	}

	private static TLColor roleColor(ValueColor role) {
		TLColor result = TypedConfiguration.newConfigItem(TLColor.class);
		result.setRole(role);
		return result;
	}

	private static TableElement table(String xml) throws ConfigurationException {
		DefaultInstantiationContext context = new DefaultInstantiationContext(TestColumnHeader.class);
		TableElement result = (TableElement) context.getInstance((PolymorphicConfiguration<?>) parseTable(xml));
		context.checkErrors();
		return result;
	}

	private static ConfigurationItem parseTable(String xml) throws ConfigurationException {
		DefaultInstantiationContext context = new DefaultInstantiationContext(TestColumnHeader.class);
		ConfigurationDescriptor descriptor = TypedConfiguration.getConfigurationDescriptor(TableElement.Config.class);
		ConfigurationReader reader = new ConfigurationReader(context, Collections.singletonMap(TABLE, descriptor));
		reader.setSource(CharacterContents.newContent(xml));
		ConfigurationItem result = reader.read();
		context.checkErrors();
		return result;
	}

	/**
	 * The test suite, starting the theme and resources the table needs for its icons, the services
	 * building its columns and their cells, and the labels of its values.
	 */
	public static Test suite() {
		return suiteWith(TestColumnHeader.class, ThemeFactory.Module.INSTANCE,
			ResourcesModule.Module.INSTANCE, FieldControlService.Module.INSTANCE,
			ColumnProviderService.Module.INSTANCE, LabelProviderService.Module.INSTANCE,
			SearchBuilder.Module.INSTANCE);
	}

}
