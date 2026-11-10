/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.form;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import junit.framework.Test;

import test.com.top_logic.layout.view.security.AbstractModelAccessTest;

import com.top_logic.base.locking.handler.NoTokenHandling;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationReader;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.character.CharacterContents;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.element.model.DynamicModelService;
import com.top_logic.gui.ThemeFactory;
import com.top_logic.knowledge.service.I18NConstants;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.layout.form.model.AbstractFieldModel;
import com.top_logic.layout.provider.LabelProviderService;
import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.CommandErrors;
import com.top_logic.layout.react.control.form.ReactFormFieldControl;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.view.DefaultViewContext;
import com.top_logic.layout.view.command.FormValid;
import com.top_logic.layout.view.form.AttributeFieldControl;
import com.top_logic.layout.view.form.AttributeRowSetBinding;
import com.top_logic.layout.view.form.BoundFieldModel;
import com.top_logic.layout.view.form.FieldControlService;
import com.top_logic.layout.view.form.FormControl;
import com.top_logic.layout.view.form.FormParticipant;
import com.top_logic.layout.view.form.RowEditPolicy;
import com.top_logic.layout.view.form.RowSetEditSession;
import com.top_logic.layout.view.form.RowSetTableControl;
import com.top_logic.layout.view.form.ValidationErrors;
import com.top_logic.layout.view.table.AttributeColumn;
import com.top_logic.layout.view.table.ColumnDeclaration;
import com.top_logic.layout.view.table.ColumnProviderService;
import com.top_logic.model.TLObject;
import com.top_logic.model.search.expr.config.SearchBuilder;
import com.top_logic.model.util.TLModelNamingConvention;
import com.top_logic.table.Row;
import com.top_logic.table.TableView;
import com.top_logic.tool.execution.ExecutableState;
import com.top_logic.util.Resources;
import com.top_logic.util.error.TopLogicException;

/**
 * Tests that a form names its validation errors when it refuses to be saved and in the reason of
 * its disabled save command ({@link FormValid}): each error of a field by the field's label, each
 * error of a row-editing table by its row and column, an error of the row set by its attribute.
 */
public class TestFormErrorMessage extends AbstractModelAccessTest {

	/** The tag a column declaration is read from. */
	private static final String COLUMN = "column";

	/** The name of the item a step holds initially. */
	private static final String ITEM_NAME = "item";

	private ReactContext _reactContext;

	private TLObject _step;

	private TLObject _item;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_reactContext = new DefaultReactContext("", "test", new SSEUpdateQueue(), new ReactWindowRegistry("test"));
		try (Transaction tx = kb().beginTransaction(I18NConstants.NO_COMMIT_MESSAGE)) {
			_step = DynamicModelService.getFactoryFor(MODULE).createObject(type(STEP));
			_step.tUpdateByName(NAME, "step");
			_item = DynamicModelService.getFactoryFor(MODULE).createObject(type(ITEM));
			_item.tUpdateByName(NAME, ITEM_NAME);
			_step.tUpdateByName(ITEMS, List.of(_item));
			tx.commit();
		}
	}

	@Override
	protected void tearDown() throws Exception {
		_reactContext = null;
		_step = null;
		_item = null;
		super.tearDown();
	}

	/**
	 * The error of a field is named by the field's label, in the details of a refused save and in
	 * the reason of the disabled save command.
	 */
	public void testFieldError() {
		becomeUser(_root);
		FormControl form = newForm(_item);
		AttributeFieldControl name = field(form, NAME);
		assertTrue(form.enterEditMode());
		model(name).setValue(null);
		ResKey error = model(name).getError();
		assertNotNull("A mandatory name is missing.", error);

		ResKey expected = com.top_logic.layout.view.command.I18NConstants.ERROR_FORM_FIELD__LABEL_MESSAGE
			.fill(MetaLabelProvider.INSTANCE.getLabel(part(ITEM, NAME)), error);
		assertEquals(List.of(expected), form.visibleErrorDetails());

		ExecutableState state = new FormValid(form).isExecutable(null);
		assertFalse(state.isExecutable());
		String reason = text(state.getI18NReasonKey());
		assertEquals(text(com.top_logic.layout.view.command.I18NConstants.ERROR_FORM_HAS_VALIDATION_ERRORS) + "\n"
			+ text(expected), reason);

		assertRefusedWith(form, List.of(expected));
	}

	/**
	 * An error that is not shown yet - the user has neither touched the field nor attempted to
	 * save - is not named; the attempt to save reveals it and names it in its refusal.
	 */
	public void testHiddenErrorNamedAfterSaveAttempt() {
		becomeUser(_root);
		FormControl form = newForm(_item);
		AttributeFieldControl name = field(form, NAME);
		assertTrue(form.enterEditMode());
		model(name).setValue(null);
		// As for a field the user has not touched yet.
		model(name).setRevealed(false);
		assertFalse("The error is not shown yet.", model(name).hasError());
		assertTrue(form.visibleErrorDetails().isEmpty());
		assertTrue(new FormValid(form).isExecutable(null).isExecutable());

		ResKey expectedTitle = com.top_logic.layout.view.command.I18NConstants.ERROR_FORM_HAS_VALIDATION_ERRORS;
		try {
			form.validateOrThrow();
			fail("A form with an empty mandatory field must be refused.");
		} catch (TopLogicException ex) {
			ResKey error = model(name).getError();
			assertNotNull("The save attempt reveals the error.", error);
			ResKey expected = com.top_logic.layout.view.command.I18NConstants.ERROR_FORM_FIELD__LABEL_MESSAGE
				.fill(MetaLabelProvider.INSTANCE.getLabel(part(ITEM, NAME)), error);
			assertEquals(List.of(expectedTitle, expected), CommandErrors.messages(ex));
			assertEquals(List.of(expected), form.visibleErrorDetails());
		}
	}

	/**
	 * The error of a cell of a row-editing table is named by the row and the column; a row whose
	 * label the edit empties is named by its position.
	 */
	public void testCellError() throws ConfigurationException {
		becomeUser(_root);
		FormControl form = newForm(_step);
		RowSetTableControl table = table(form, List.of(column("<column attribute='" + NAME + "'/>")));
		assertTrue(form.enterEditMode());
		TLObject row = single(table.session().currentRows());
		renderRows(table);
		BoundFieldModel name = single(List.copyOf(table.session().findRowModel(row).getColumnModels().values()));

		name.setValue("");
		ResKey error = name.getError();
		assertNotNull("A mandatory name is missing.", error);

		ResKey expected = com.top_logic.layout.view.command.I18NConstants.ERROR_FORM_CELL__ROW_COLUMN_MESSAGE.fill(
			com.top_logic.layout.view.command.I18NConstants.FORM_ROW__POSITION.fill(1),
			TLModelNamingConvention.resourceKey(part(ITEM, NAME)), error);
		assertEquals(List.of(text(expected)), texts(form.visibleErrorDetails()));
		assertRefusedWith(form, List.of(expected));
	}

	/**
	 * A row that has a label is named by it.
	 */
	public void testCellErrorNamedByRowLabel() throws ConfigurationException {
		becomeUser(_root);
		FormControl form = newForm(_step);
		RowSetTableControl table = table(form, List.of(column("<column attribute='" + NAME + "'/>")));
		assertTrue(form.enterEditMode());
		TLObject row = single(table.session().currentRows());
		renderRows(table);
		BoundFieldModel name = single(List.copyOf(table.session().findRowModel(row).getColumnModels().values()));
		ResKey error = com.top_logic.element.meta.form.constraint.I18NConstants.ERROR_MANDATORY_FIELD_EMPTY;
		name.setError(error);

		ResKey expected = com.top_logic.layout.view.command.I18NConstants.ERROR_FORM_CELL__ROW_COLUMN_MESSAGE.fill(
			ITEM_NAME, TLModelNamingConvention.resourceKey(part(ITEM, NAME)), error);
		assertEquals(List.of(text(expected)), texts(form.visibleErrorDetails()));
	}

	/**
	 * An error of the row set as a whole is named by its attribute, once it is revealed.
	 */
	public void testRowSetError() throws ConfigurationException {
		becomeUser(_root);
		FormControl form = newForm(_step);
		RowSetTableControl table = table(form, List.of(column("<column attribute='" + NAME + "'/>")));
		assertTrue(form.enterEditMode());
		RowSetEditSession session = table.session();
		ResKey error = com.top_logic.element.meta.form.constraint.I18NConstants.ERROR_MANDATORY_FIELD_EMPTY;
		session.fieldModel().setModelValidationError(error);
		assertTrue("A hidden error is not named.", form.visibleErrorDetails().isEmpty());

		ResKey expected = com.top_logic.layout.view.command.I18NConstants.ERROR_FORM_FIELD__LABEL_MESSAGE
			.fill(MetaLabelProvider.INSTANCE.getLabel(part(STEP, ITEMS)), error);
		form.revealAllValidation();
		assertEquals(List.of(expected), form.visibleErrorDetails());
	}

	/**
	 * An error of an attribute of a row that no column shows does not keep the form from being
	 * saved, so the form does not name it; the check of the rows in a dialog refuses it, and names
	 * it by the row and the attribute.
	 */
	public void testRowAttributeWithoutColumn() throws ConfigurationException {
		becomeUser(_root);
		FormControl form = newForm(_step);
		// A column that is not edited: no cell shows an error of the name.
		RowSetTableControl table = table(form, List.of(column("<column attribute='" + NAME + "' readonly='true'/>")));
		assertTrue(form.enterEditMode());
		RowSetEditSession session = table.session();
		renderRows(table);
		TLObject added = session.addRow(row -> {
			// Without a name.
		});
		assertNotNull(added);

		form.revealAllValidation();
		assertTrue("An error that does not refuse the save is not named: " + texts(form.visibleErrorDetails()),
			form.visibleErrorDetails().isEmpty());

		List<String> errors = texts(session.checkErrors());
		assertEquals(1, errors.size());
		String prefix = text(com.top_logic.layout.view.command.I18NConstants.FORM_ROW__POSITION.fill(2)) + ", "
			+ MetaLabelProvider.INSTANCE.getLabel(added.tType().getPartOrFail(NAME)) + ": ";
		assertTrue("Not named by row and attribute: " + errors.get(0), errors.get(0).startsWith(prefix));
		assertFalse(session.checkValid());
	}

	/**
	 * A long list of errors names the first ten and closes with the number of the others.
	 */
	public void testLongListCapped() {
		becomeUser(_root);
		FormControl form = newForm(_item);
		assertTrue(form.enterEditMode());
		List<ResKey> errors = new ArrayList<>();
		for (int n = 1; n <= 13; n++) {
			errors.add(com.top_logic.layout.view.command.I18NConstants.FORM_ROW__POSITION.fill(n));
		}
		form.registerParticipant(new InvalidParticipant(errors));

		assertEquals(errors, form.visibleErrorDetails());
		List<ResKey> expected = new ArrayList<>(errors.subList(0, 10));
		expected.add(com.top_logic.layout.view.command.I18NConstants.ERROR_FORM_MORE_ERRORS__COUNT.fill(3));
		assertEquals(expected, ValidationErrors.listed(errors));
		assertRefusedWith(form, expected);

		StringBuilder reason =
			new StringBuilder(text(com.top_logic.layout.view.command.I18NConstants.ERROR_FORM_HAS_VALIDATION_ERRORS));
		for (ResKey line : expected) {
			reason.append('\n').append(text(line));
		}
		assertEquals(reason.toString(), text(new FormValid(form).isExecutable(null).getI18NReasonKey()));
	}

	private static void assertRefusedWith(FormControl form, List<ResKey> details) {
		try {
			form.validateOrThrow();
			fail("A form with errors must be refused.");
		} catch (TopLogicException ex) {
			assertEquals(com.top_logic.layout.view.command.I18NConstants.ERROR_FORM_HAS_VALIDATION_ERRORS,
				ex.getErrorKey());
			List<ResKey> messages = CommandErrors.messages(ex);
			assertEquals(texts(details), texts(messages.subList(1, messages.size())));
		}
	}

	private FormControl newForm(TLObject object) {
		return new FormControl(_reactContext, object, "no model", NoTokenHandling.INSTANCE);
	}

	private static AttributeFieldControl field(FormControl form, String attribute) {
		AttributeFieldControl result =
			new AttributeFieldControl(form.getReactContext(), form, form, attribute, null, false, null);
		result.createChromeControl();
		return result;
	}

	private static AbstractFieldModel model(AttributeFieldControl field) {
		return (AbstractFieldModel) ((ReactFormFieldControl) field.getInnerControl()).getFieldModel();
	}

	private RowSetTableControl table(FormControl form, List<ColumnDeclaration> columns) {
		RowSetTableControl table = new RowSetTableControl(new DefaultViewContext(_reactContext), form,
			new AttributeRowSetBinding(ITEMS), columns, RowEditPolicy.ALL);
		table.init();
		return table;
	}

	/** Renders the cells of all rows, which creates the field models of the edited cells. */
	private static void renderRows(RowSetTableControl table) {
		TableView<TLObject> view = table.getTableControl().getView();
		for (Row<TLObject> row : view.rows(0, view.rowCount())) {
			view.cell(row, NAME);
		}
	}

	private static String text(ResKey key) {
		return Resources.getInstance().getString(key);
	}

	private static List<String> texts(List<ResKey> keys) {
		List<String> result = new ArrayList<>();
		for (ResKey key : keys) {
			result.add(text(key));
		}
		return result;
	}

	private static <T> T single(List<T> list) {
		assertEquals(1, list.size());
		return list.get(0);
	}

	/** The column declaration read from the given {@value #COLUMN} tag. */
	private static ColumnDeclaration column(String xml) throws ConfigurationException {
		DefaultInstantiationContext context = new DefaultInstantiationContext(TestFormErrorMessage.class);
		ConfigurationReader reader = new ConfigurationReader(context, Collections.singletonMap(COLUMN,
			TypedConfiguration.getConfigurationDescriptor(AttributeColumn.Config.class)));
		reader.setSource(CharacterContents.newContent(xml));
		ColumnDeclaration result =
			(ColumnDeclaration) context.getInstance((PolymorphicConfiguration<?>) reader.read());
		context.checkErrors();
		return result;
	}

	/**
	 * {@link FormParticipant} reporting a fixed list of visible errors.
	 */
	private static class InvalidParticipant implements FormParticipant {

		private final List<ResKey> _errors;

		InvalidParticipant(List<ResKey> errors) {
			_errors = errors;
		}

		@Override
		public boolean validate() {
			return false;
		}

		@Override
		public List<ResKey> visibleErrors() {
			return _errors;
		}

		@Override
		public void persist(Transaction tx) {
			// Nothing to store.
		}

		@Override
		public void cancel() {
			// Nothing to discard.
		}

		@Override
		public void revealAll() {
			// Always shown.
		}

		@Override
		public boolean isDirty() {
			return false;
		}
	}

	/**
	 * The test suite, starting the theme and resources the table needs for its icons, the services
	 * building its data columns and their cells and the form fields, and the labels of their
	 * values.
	 */
	public static Test suite() {
		return suiteWith(TestFormErrorMessage.class, ThemeFactory.Module.INSTANCE,
			ResourcesModule.Module.INSTANCE, FieldControlService.Module.INSTANCE,
			ColumnProviderService.Module.INSTANCE, LabelProviderService.Module.INSTANCE,
			SearchBuilder.Module.INSTANCE);
	}

}
