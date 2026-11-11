/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.form;

import java.util.Collections;
import java.util.HashMap;
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
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.element.model.DynamicModelService;
import com.top_logic.gui.ThemeFactory;
import com.top_logic.knowledge.service.I18NConstants;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.layout.provider.LabelProviderService;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.form.FieldValueArguments;
import com.top_logic.layout.react.control.form.ReactFormFieldControl;
import com.top_logic.layout.react.control.layout.ReactFormFieldChromeControl;
import com.top_logic.layout.react.control.table.CellControlFactory;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.state.FormFieldState;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.view.DefaultViewContext;
import com.top_logic.layout.view.form.AttributeRowSetBinding;
import com.top_logic.layout.view.form.BoundFieldModel;
import com.top_logic.layout.view.form.FieldControlService;
import com.top_logic.layout.view.form.FormControl;
import com.top_logic.layout.view.form.RowEditPolicy;
import com.top_logic.layout.view.form.RowSetTableControl;
import com.top_logic.layout.view.table.AttributeColumn;
import com.top_logic.layout.view.table.ColumnBinding;
import com.top_logic.layout.view.table.ColumnDeclaration;
import com.top_logic.layout.view.table.ColumnProviderService;
import com.top_logic.layout.view.table.ColumnSetup;
import com.top_logic.layout.view.table.ColumnType;
import com.top_logic.layout.view.table.ValueCellEditing;
import com.top_logic.model.TLObject;
import com.top_logic.model.search.expr.config.SearchBuilder;
import com.top_logic.table.CellContent;
import com.top_logic.table.Column;
import com.top_logic.table.Row;
import com.top_logic.table.TableView;

/**
 * Tests the edited cells of a row-editing {@link RowSetTableControl} and of a column edited in
 * place: each stands in a compact {@link ReactFormFieldChromeControl} that shows the error of the
 * cell's field, while a cell that is not edited is not wrapped.
 */
public class TestRowSetTableCellChrome extends AbstractModelAccessTest {

	/** The tag a column declaration is read from. */
	private static final String COLUMN = "column";

	/** The attribute of a task holding a number, the value of the column edited in place. */
	private static final String NUMBER = "number";

	/** The name of the item a step holds initially. */
	private static final String ITEM_NAME = "item";

	private ReactContext _reactContext;

	private TLObject _step;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_reactContext = new DefaultReactContext("", "test", new SSEUpdateQueue(), new ReactWindowRegistry("test"));
		try (Transaction tx = kb().beginTransaction(I18NConstants.NO_COMMIT_MESSAGE)) {
			_step = DynamicModelService.getFactoryFor(MODULE).createObject(type(STEP));
			_step.tUpdateByName(NAME, "step");
			TLObject item = DynamicModelService.getFactoryFor(MODULE).createObject(type(ITEM));
			item.tUpdateByName(NAME, ITEM_NAME);
			_step.tUpdateByName(ITEMS, List.of(item));
			tx.commit();
		}
	}

	@Override
	protected void tearDown() throws Exception {
		_reactContext = null;
		_step = null;
		super.tearDown();
	}

	/**
	 * An edited cell is a compact chrome around its input, named by the column; the error of a
	 * violated constraint - an empty mandatory name - reaches the state of the chrome and is
	 * cleared again by a valid value.
	 */
	public void testEditedCellShowsError() throws ConfigurationException {
		becomeUser(_root);
		FormControl form = new FormControl(_reactContext, _step, "no model", NoTokenHandling.INSTANCE);
		RowSetTableControl table = table(form, "<column attribute='" + NAME + "'/>");
		assertTrue(form.enterEditMode());

		TLObject row = single(table.session().currentRows());
		ReactControl control = cellControl(table, row);
		assertTrue("Not wrapped in a field chrome: " + control, control instanceof ReactFormFieldChromeControl);
		Map<?, ?> state = state(control);
		assertEquals(Boolean.TRUE, state.get(FormFieldState.COMPACT__PROP));
		assertNotNull("The column names the input.", state.get(FormFieldState.LABEL__PROP));
		assertNull(state.get(FormFieldState.ERROR__PROP));
		Map<?, ?> field = (Map<?, ?>) state.get(FormFieldState.FIELD__PROP);
		assertNotNull("The input is the field of the chrome.", field);

		BoundFieldModel name = single(List.copyOf(table.session().findRowModel(row).getColumnModels().values()));
		name.setValue("");
		assertTrue("A mandatory name is missing.", name.hasError());
		assertNotNull("The chrome shows the error of the cell.", state(control).get(FormFieldState.ERROR__PROP));

		name.setValue("valid");
		assertFalse(name.hasError());
		assertNull("A valid value clears the error.", state(control).get(FormFieldState.ERROR__PROP));
	}

	/** The input of an edited cell is the field control of its cell model. */
	public void testChromeWrapsInput() throws ConfigurationException {
		becomeUser(_root);
		FormControl form = new FormControl(_reactContext, _step, "no model", NoTokenHandling.INSTANCE);
		RowSetTableControl table = table(form, "<column attribute='" + NAME + "'/>");
		assertTrue(form.enterEditMode());

		TLObject row = single(table.session().currentRows());
		ReactFormFieldChromeControl chrome = (ReactFormFieldChromeControl) cellControl(table, row);
		ReactControl input = chrome.getField();
		assertTrue("Not the input of the cell: " + input, input instanceof ReactFormFieldControl);
		BoundFieldModel name = single(List.copyOf(table.session().findRowModel(row).getColumnModels().values()));
		assertSame(name, ((ReactFormFieldControl) input).getFieldModel());
	}

	/** A cell that is not edited is not wrapped in a chrome, also in edit mode. */
	public void testReadOnlyCellNotWrapped() throws ConfigurationException {
		becomeUser(_root);
		FormControl form = new FormControl(_reactContext, _step, "no model", NoTokenHandling.INSTANCE);
		RowSetTableControl table = table(form, "<column attribute='" + NAME + "' readonly='true'/>");
		assertTrue(form.enterEditMode());

		TLObject row = single(table.session().currentRows());
		ReactControl control = cellControl(table, row);
		assertFalse("A read-only cell is wrapped: " + control, control instanceof ReactFormFieldChromeControl);
	}

	/**
	 * A cell edited in place, outside a form, is a compact chrome around its input as well: a value
	 * the input rejects - text that is no number - reaches the state of the chrome, and a valid
	 * value clears it again and is written.
	 */
	public void testInPlaceCellShowsError() {
		becomeUser(_root);
		Map<String, Object> row = new HashMap<>();
		row.put(NUMBER, 1L);
		ColumnType type = ColumnType.of(type(TASK).getPartOrFail(NUMBER));
		ValueCellEditing editing = new ValueCellEditing(type, r -> ((Map<?, ?>) r).get(NUMBER),
			(r, value) -> row.put(NUMBER, value), r -> true);
		ColumnSetup setup = new ColumnSetup(NUMBER, ResKey.text("Number"), type, r -> ((Map<?, ?>) r).get(NUMBER),
			null, ColumnBinding.TYPE_DERIVED, 0, editing, null, null, false);
		Column<Object, ?> column = setup.buildInPlaceColumn(r -> {
			// Nothing to refresh.
		});

		CellContent cell = column.renderCell(row);
		assertTrue("Not shown through a control: " + cell, cell instanceof CellContent.Raw);
		ReactControl control = ((CellControlFactory) ((CellContent.Raw) cell).payload()).create(_reactContext);
		assertTrue("Not wrapped in a field chrome: " + control, control instanceof ReactFormFieldChromeControl);
		assertEquals(Boolean.TRUE, state(control).get(FormFieldState.COMPACT__PROP));
		assertNull(state(control).get(FormFieldState.ERROR__PROP));

		ReactControl input = ((ReactFormFieldChromeControl) control).getField();
		input.executeCommand(ReactFormFieldControl.CMD_VALUE_CHANGED, Map.of(FieldValueArguments.VALUE, "no number"));
		assertNotNull("The chrome shows the rejected input.", state(control).get(FormFieldState.ERROR__PROP));
		assertEquals("A rejected value is not written.", 1L, row.get(NUMBER));

		input.executeCommand(ReactFormFieldControl.CMD_VALUE_CHANGED, Map.of(FieldValueArguments.VALUE, "5"));
		assertNull("A valid value clears the error.", state(control).get(FormFieldState.ERROR__PROP));
		assertEquals(5L, ((Number) row.get(NUMBER)).longValue());
	}

	private RowSetTableControl table(FormControl form, String column) throws ConfigurationException {
		RowSetTableControl table = new RowSetTableControl(new DefaultViewContext(_reactContext), form,
			new AttributeRowSetBinding(ITEMS), List.of(column(column)), RowEditPolicy.ALL);
		table.init();
		return table;
	}

	/** The control of the {@link #NAME} cell of the given row, as the client receives it. */
	private ReactControl cellControl(RowSetTableControl table, TLObject rowObject) {
		TableView<TLObject> view = table.getTableControl().getView();
		for (Row<TLObject> row : view.rows(0, view.rowCount())) {
			if (row.key() == rowObject) {
				CellContent cell = view.cell(row, NAME);
				if (cell instanceof CellContent.Raw raw && raw.payload() instanceof CellControlFactory factory) {
					return factory.create(_reactContext);
				}
				return null;
			}
		}
		throw new AssertionError("No row " + rowObject);
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

	private static <T> T single(List<T> list) {
		assertEquals(1, list.size());
		return list.get(0);
	}

	/** The column declaration read from the given {@value #COLUMN} tag. */
	private static ColumnDeclaration column(String xml) throws ConfigurationException {
		DefaultInstantiationContext context = new DefaultInstantiationContext(TestRowSetTableCellChrome.class);
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
	 * building its data columns and their cells, and the labels of their values.
	 */
	public static Test suite() {
		return suiteWith(TestRowSetTableCellChrome.class, ThemeFactory.Module.INSTANCE,
			ResourcesModule.Module.INSTANCE, FieldControlService.Module.INSTANCE,
			ColumnProviderService.Module.INSTANCE, LabelProviderService.Module.INSTANCE,
			SearchBuilder.Module.INSTANCE);
	}

}
