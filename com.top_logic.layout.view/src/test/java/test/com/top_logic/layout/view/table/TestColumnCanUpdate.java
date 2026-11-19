/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.table;

import java.util.Collections;
import java.util.List;

import junit.framework.Test;

import test.com.top_logic.layout.view.security.AbstractModelAccessTest;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.ConfigurationReader;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.character.CharacterContents;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.layout.provider.LabelProviderService;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.view.DefaultViewContext;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.element.TableElement;
import com.top_logic.layout.view.form.FieldControlService;
import com.top_logic.layout.view.form.TLObjectOverlay;
import com.top_logic.layout.view.table.AbstractColumnDeclaration;
import com.top_logic.layout.view.table.AttributeColumn;
import com.top_logic.layout.view.table.CellEditing;
import com.top_logic.layout.view.table.ColumnDeclaration;
import com.top_logic.layout.view.table.ColumnProviderService;
import com.top_logic.layout.view.table.ColumnResolution;
import com.top_logic.layout.view.table.ColumnSetup;
import com.top_logic.layout.view.table.ComputedColumn;
import com.top_logic.layout.view.table.ValueCellEditing;
import com.top_logic.model.search.expr.config.SearchBuilder;

/**
 * Tests the {@link AbstractColumnDeclaration.Config#getCanUpdate() edit condition} of a column
 * declaration: at an {@link AttributeColumn} it restricts the edit the model and the access rights
 * allow, at a {@link ComputedColumn} it decides the rows its update is offered on.
 */
public class TestColumnCanUpdate extends AbstractModelAccessTest {

	private static final String TABLE = "table";

	/** The name of a project. */
	private static final String PROJECT_NAME = qualified(PROJECT) + "#" + NAME;

	/** A condition accepting the {@link #_project} only, by its name. */
	private static final String ONLY_PROJECT = "$row.get(`" + PROJECT_NAME + "`) == 'project'";

	private ViewContext _context;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_context = new DefaultViewContext(
			new DefaultReactContext("", "test", new SSEUpdateQueue(), new ReactWindowRegistry("test")));
	}

	/**
	 * The tests start as the super-user, so that only the edit condition and the model decide; the
	 * access rights are switched on where a test is about them.
	 */
	@Override
	protected Person initialUser() {
		return _root;
	}

	@Override
	protected void tearDown() throws Exception {
		_context = null;
		super.tearDown();
	}

	/**
	 * An attribute column with a condition edits the cells of the rows the condition accepts, on
	 * the stored row and on the overlay it is edited in, and shows the others read-only.
	 */
	public void testAttributeColumnRestricted() throws Exception {
		CellEditing editing = editing("<column attribute='" + NAME + "' can-update=\"" + ONLY_PROJECT + "\"/>");

		assertTrue(editing.canEdit(_project));
		assertTrue(editing.canEdit(new TLObjectOverlay(_project)));
		assertFalse(editing.canEdit(_otherProject));
		assertFalse(editing.canEdit(new TLObjectOverlay(_otherProject)));
	}

	/**
	 * The condition sees the row by the name the table gives it.
	 */
	public void testRenamedRow() throws Exception {
		ColumnSetup column = resolve("<table rows='[]' row-as='p'><columns>"
			+ "<column attribute='" + NAME + "' can-update=\"$p.get(`" + PROJECT_NAME + "`) == 'project'\"/>"
			+ "</columns></table>");

		assertTrue(column.editing().canEdit(_project));
		assertFalse(column.editing().canEdit(_otherProject));
	}

	/**
	 * A condition accepting every row does not edit a cell the model does not let be edited: an
	 * attribute the row type does not hold, an attribute the user may not write.
	 */
	public void testConditionOnlyRestricts() throws Exception {
		CellEditing budget = editing("<column attribute='" + BUDGET + "' can-update='true'/>");
		assertTrue(budget.canEdit(_project));
		assertFalse("A category holds no budget.", budget.canEdit(_category));

		becomeUser(_responsible);
		CellEditing secret = editing("<column attribute='" + SECRET + "' can-update='true'/>");
		assertFalse("Writing the secret is granted to no role.", secret.canEdit(_project));
		CellEditing name = editing("<column attribute='" + NAME + "' can-update='true'/>");
		assertTrue(name.canEdit(_project));
	}

	/**
	 * A read-only column is not edited, whatever its condition says.
	 */
	public void testReadonlyWins() throws Exception {
		assertNull(column("<column attribute='" + NAME + "' readonly='true' can-update='true'/>").editing());
	}

	/**
	 * A column without a condition edits every cell the model and the rights let be edited.
	 */
	public void testAttributeColumnWithoutCondition() throws Exception {
		CellEditing editing = editing("<column attribute='" + NAME + "'/>");

		assertTrue(editing.canEdit(_project));
		assertTrue(editing.canEdit(_otherProject));
	}

	/**
	 * A computed column offers its update on the rows its condition accepts, and its cells are
	 * still edited in place.
	 */
	public void testComputedColumnRestricted() throws Exception {
		CellEditing editing = editing(computed(" can-update=\"" + ONLY_PROJECT + "\""));

		assertTrue("Edited in place.", editing instanceof ValueCellEditing);
		assertTrue(editing.canEdit(_project));
		assertFalse(editing.canEdit(_otherProject));
	}

	/**
	 * A computed column without a condition offers its update on every row, one without an update
	 * on none.
	 */
	public void testComputedColumnWithoutCondition() throws Exception {
		CellEditing editing = editing(computed(""));
		assertTrue(editing.canEdit(_project));
		assertTrue(editing.canEdit(_otherProject));

		assertNull(column("<computed-column name='c' type='tl.core:String' value='$row'"
			+ " can-update='true'/>").editing());
	}

	private static String computed(String condition) {
		return "<computed-column name='c' type='tl.core:String' value=\"$row.get(`" + PROJECT_NAME + "`)\""
			+ " update=\"$row.set(`" + PROJECT_NAME + "`, $value)\"" + condition + "/>";
	}

	private CellEditing editing(String column) throws ConfigurationException {
		CellEditing result = column(column).editing();
		assertNotNull("The column is edited: " + column, result);
		return result;
	}

	private ColumnSetup column(String column) throws ConfigurationException {
		return resolve("<table rows='[]'><columns>" + column + "</columns></table>");
	}

	private ColumnSetup resolve(String tableXml) throws ConfigurationException {
		TableElement.Config table = (TableElement.Config) parseTable(tableXml);
		PolymorphicConfiguration<? extends ColumnDeclaration> declaration = table.getColumns().getColumns().get(0);
		DefaultInstantiationContext context = new DefaultInstantiationContext(TestColumnCanUpdate.class);
		ColumnDeclaration instance = context.getInstance(declaration);
		context.checkErrors();
		List<ColumnSetup> setups = instance.resolve(new ColumnResolution(type(PROJECT), _context));
		assertEquals(1, setups.size());
		return setups.get(0);
	}

	private static ConfigurationItem parseTable(String xml) throws ConfigurationException {
		DefaultInstantiationContext context = new DefaultInstantiationContext(TestColumnCanUpdate.class);
		ConfigurationReader reader = new ConfigurationReader(context,
			Collections.singletonMap(TABLE, TypedConfiguration.getConfigurationDescriptor(TableElement.Config.class)));
		reader.setSource(CharacterContents.newContent(xml));
		ConfigurationItem result = reader.read();
		context.checkErrors();
		return result;
	}

	/**
	 * The test suite, starting the scripting the conditions are computed with and the services the
	 * attribute columns are built with.
	 */
	public static Test suite() {
		return suiteWith(TestColumnCanUpdate.class, TypeIndex.Module.INSTANCE, SearchBuilder.Module.INSTANCE,
			LabelProviderService.Module.INSTANCE, FieldControlService.Module.INSTANCE,
			ColumnProviderService.Module.INSTANCE);
	}

}
