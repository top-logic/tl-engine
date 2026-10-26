/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.form;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import junit.framework.Test;

import test.com.top_logic.layout.view.security.AbstractModelAccessTest;

import com.top_logic.base.locking.handler.NoTokenHandling;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationReader;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.character.CharacterContents;
import com.top_logic.basic.json.JSON;
import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.gui.ThemeFactory;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.layout.provider.LabelProviderService;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.table.CellControlFactory;
import com.top_logic.layout.react.control.table.ReactResourceCellControl;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.view.DefaultViewContext;
import com.top_logic.layout.view.element.ResourceDisplayByExpression;
import com.top_logic.layout.view.form.AttributeRowSetBinding;
import com.top_logic.layout.view.form.FieldControlService;
import com.top_logic.layout.view.form.FormControl;
import com.top_logic.layout.view.form.RowEditPolicy;
import com.top_logic.layout.view.form.RowSetTableControl;
import com.top_logic.layout.view.table.AttributeColumn;
import com.top_logic.layout.view.table.ColumnDeclaration;
import com.top_logic.layout.view.table.ColumnProviderService;
import com.top_logic.model.search.expr.config.SearchBuilder;
import com.top_logic.table.CellContent;
import com.top_logic.table.Row;
import com.top_logic.table.TableView;
import com.top_logic.util.TLContextManager;

/**
 * Tests the read-only cells of a {@link RowSetTableControl}: they carry the display, the tooltip
 * and the CSS class their column declares, in view mode and, for a column that is not edited, in
 * edit mode alike.
 */
public class TestRowSetTableCells extends AbstractModelAccessTest {

	/** The tag a column declaration is read from. */
	private static final String COLUMN = "column";

	/** State key of the label of a {@link ReactResourceCellControl}. */
	private static final String LABEL_STATE = "label";

	private ReactContext _reactContext;

	private FormControl _form;

	private RowSetTableControl _table;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_reactContext = new DefaultReactContext("", "test", new SSEUpdateQueue(), new ReactWindowRegistry("test"));
		ColumnDeclaration column = column("<column attribute='" + NAME + "' readonly='true'"
			+ " tooltip=\"'tip ' + $value\" cell-class=\"'marked'\">"
			+ "<display class='" + ResourceDisplayByExpression.class.getName() + "' label=\"'D:' + $value\"/>"
			+ "</column>");
		TLContextManager.inPersonContext(_responsible, () -> {
			_form = new FormControl(_reactContext, _project, "no model", NoTokenHandling.INSTANCE);
			_table = new RowSetTableControl(new DefaultViewContext(_reactContext), _form,
				new AttributeRowSetBinding(TASKS), List.of(column), RowEditPolicy.ALL);
			_table.init();
		});
	}

	@Override
	protected void tearDown() throws Exception {
		_table = null;
		_form = null;
		_reactContext = null;
		super.tearDown();
	}

	@Override
	protected Person initialUser() {
		return _responsible;
	}

	/** In view mode a cell shows the display, the tooltip and the class of its column. */
	public void testViewModeCell() {
		assertDecoratedCell();
	}

	/** In edit mode a cell of a column that is not edited is the same as in view mode. */
	public void testReadOnlyCellInEditMode() {
		assertTrue(_form.enterEditMode());
		assertDecoratedCell();
	}

	private void assertDecoratedCell() {
		TableView<?> view = _table.getTableControl().getView();
		assertCell(view);
	}

	private <R> void assertCell(TableView<R> view) {
		Row<R> row = view.rows(0, 1).get(0);
		CellContent cell = view.cell(row, NAME);
		assertEquals("tip task", cell.tooltip());
		assertTrue("Not shown through a control: " + cell, cell instanceof CellContent.Raw);
		ReactControl control =
			((CellControlFactory) ((CellContent.Raw) cell).payload()).create(_reactContext);
		assertTrue("Not the display of the column: " + control, control instanceof ReactResourceCellControl);
		assertEquals("D:task", state(control).get(LABEL_STATE));
		String cssClass = view.cellCssClass(row, NAME);
		assertNotNull(cssClass);
		assertTrue(cssClass, List.of(cssClass.split(" ")).contains("marked"));
	}

	/** The state of the given control as the client receives it. */
	private static Map<?, ?> state(ReactControl control) {
		String json = control.stateAsJSON();
		try {
			return (Map<?, ?>) JSON.fromString(json);
		} catch (JSON.ParseException ex) {
			throw new AssertionError("Not the JSON state of a control: " + json, ex);
		}
	}

	/** The column declaration read from the given {@value #COLUMN} tag. */
	private static ColumnDeclaration column(String xml) throws ConfigurationException {
		DefaultInstantiationContext context = new DefaultInstantiationContext(TestRowSetTableCells.class);
		ConfigurationReader reader = new ConfigurationReader(context, Collections.singletonMap(COLUMN,
			TypedConfiguration.getConfigurationDescriptor(AttributeColumn.Config.class)));
		reader.setSource(CharacterContents.newContent(xml));
		ColumnDeclaration result =
			(ColumnDeclaration) context.getInstance((PolymorphicConfiguration<?>) reader.read());
		context.checkErrors();
		return result;
	}

	/**
	 * The test suite, starting the theme and resources the table needs for its icons, the services
	 * building its data columns and their cells, and the scripting the column computes with.
	 */
	public static Test suite() {
		return suiteWith(TestRowSetTableCells.class, ThemeFactory.Module.INSTANCE,
			ResourcesModule.Module.INSTANCE, FieldControlService.Module.INSTANCE,
			ColumnProviderService.Module.INSTANCE, LabelProviderService.Module.INSTANCE,
			SearchBuilder.Module.INSTANCE);
	}

}
