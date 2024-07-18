/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.form.fieldprovider;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.annotation.FrameworkInternal;
import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.basic.col.TypedAnnotatable.Property;
import com.top_logic.basic.shared.collection.CollectionUtilShared;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.element.config.annotation.TLReadOnlyColumns;
import com.top_logic.element.meta.AttributeUpdate;
import com.top_logic.element.meta.AttributeUpdate.StoreAlgorithm;
import com.top_logic.element.meta.AttributeUpdateContainer;
import com.top_logic.element.meta.form.AttributeFormContext;
import com.top_logic.element.meta.form.EditContext;
import com.top_logic.element.meta.form.FieldProvider;
import com.top_logic.element.meta.form.MetaControlProvider;
import com.top_logic.element.meta.form.controlprovider.CompositionControlProvider;
import com.top_logic.element.meta.form.overlay.TLFormObject;
import com.top_logic.knowledge.service.KBUtils;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.layout.Accessor;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.DisplayDimension;
import com.top_logic.layout.ReadOnlyAccessor;
import com.top_logic.layout.Renderer;
import com.top_logic.layout.ResPrefix;
import com.top_logic.layout.ResourceView;
import com.top_logic.layout.VetoException;
import com.top_logic.layout.basic.CommandModel;
import com.top_logic.layout.basic.DefaultDisplayContext;
import com.top_logic.layout.form.FormContainer;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.control.ErrorControl;
import com.top_logic.layout.form.model.CompositeField;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.FormGroup;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.layout.form.model.TableField;
import com.top_logic.layout.form.tag.TableTag;
import com.top_logic.layout.form.template.AbstractFormFieldControlProvider;
import com.top_logic.layout.form.template.ControlProvider;
import com.top_logic.layout.messagebox.MessageBox;
import com.top_logic.layout.messagebox.MessageBox.ButtonType;
import com.top_logic.layout.messagebox.SimpleFormDialog;
import com.top_logic.layout.table.AbstractCellRenderer;
import com.top_logic.layout.table.RowObjectCreator;
import com.top_logic.layout.table.RowObjectRemover;
import com.top_logic.layout.table.TableData;
import com.top_logic.layout.table.TableRenderer.Cell;
import com.top_logic.layout.table.control.TableControl;
import com.top_logic.layout.table.control.TableListControl;
import com.top_logic.layout.table.filter.AllCellsExist;
import com.top_logic.layout.table.filter.CellExistenceTester;
import com.top_logic.layout.table.model.ColumnConfiguration;
import com.top_logic.layout.table.model.ObjectTableModel;
import com.top_logic.layout.table.model.TableConfiguration;
import com.top_logic.layout.table.model.TableConfigurationFactory;
import com.top_logic.layout.table.model.TableConfigurationProvider;
import com.top_logic.layout.table.model.TableModelEvent;
import com.top_logic.layout.table.model.TableModelListener;
import com.top_logic.layout.table.renderer.DefaultTableRenderer;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.annotate.DisplayAnnotations;
import com.top_logic.model.annotate.ui.TLRowObject;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.util.Resources;

/**
 * {@link FieldProvider} for composition reference attributes.
 * 
 * <p>
 * In a composition attribute, containment objects can be created and deleted and their properties
 * can be edited.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class CompositionFieldProvider extends AbstractWrapperFieldProvider {

	/**
	 * Name of the {@link TableField} in the created {@link #getFormField(EditContext, String)
	 * field}.
	 * 
	 * @see Composite
	 */
	@FrameworkInternal
	public static final String TABLE_FIELD_NAME = "table";

	/**
	 * The deleted objects of the table field.
	 */
	static final Property<Set<TLObject>> DELETED = TypedAnnotatable.propertySet("deleted");

	private static final StoreAlgorithm DELETE_AND_STORE = new AttributeUpdate.DefaultStorageAlgorithm() {
		@Override
		public void store(AttributeUpdate update) {
			Set<TLObject> deleted = ((Composite) update.getField()).getProxy().get(DELETED);
			KBUtils.deleteAll(deleted);
			super.store(update);
		}
	};

	@Override
	public FormMember createFormField(EditContext update, String fieldName) {
		final TLObject obj = update.getOverlay();

		boolean searchUpdate = update.isSearchUpdate();
		if (searchUpdate) {
			/* Searching for a composition attribute is senseless, because the values for such an
			 * attribute belong to exactly one object. */
			return null;
		}

		boolean isOrdered = update.isOrdered();

		AttributeUpdateContainer updateContainer = update.getOverlay().getScope();

		List<TableConfigurationProvider> tableConfigProviders = new ArrayList<>();
		tableConfigProviders.add(AbstractWrapperFieldProvider.getTableConfigurationProvider(update));
		tableConfigProviders.add(new FieldAccessProvider(updateContainer));

		TableConfiguration config = TableConfigurationFactory.build(tableConfigProviders);

		ResPrefix resources = ResPrefix.NONE;
		ObjectTableModel tableModel =
			new ObjectTableModel(config.getDefaultColumns(), config, Collections.emptyList(), isOrdered);

		TableField tableField =
			FormFactory.newTableField(TABLE_FIELD_NAME, update::getConfigKey);
		tableField.setTableModel(tableModel);

		/* Set label explicit, because generic just sets the label for the wrapping group. */
		ResKey labelKey = update.getLabelKey();
		tableField.setLabel(Resources.getInstance()
			.getString(I18NConstants.COMPOSITE_FIELD_LABEL__ATTRIBUTE.fill(labelKey)));
		Composite result =
			new Composite(update, fieldName, resources, tableModel, tableField);
		result.addMember(tableField);
		result.setMandatory(update.isMandatory());

		result.setControlProvider(new AbstractFormFieldControlProvider() {
			@Override
			protected Control createInput(FormMember member) {
				TableField table = (TableField) ((FormGroup) member).getMember(TABLE_FIELD_NAME);

				if (table.isImmutable()) {
					for (Object row : table.getTableModel().getAllRows()) {
						for (AttributeUpdate rowUpdate : updateContainer.getAllUpdates((TLObject) row)) {
							rowUpdate.getField().setImmutable(true);
						}
					}
					return TableTag.createTableControl(table);
				} else {
					TLRowObject rowObjectAnnotation = update.getAnnotation(TLRowObject.class);
					RowObjectCreator creator = null;
					RowObjectRemover remover = null;
					boolean isSortable = isOrdered;
					if (rowObjectAnnotation == null || rowObjectAnnotation.isCreatable()) {
						creator = new RowCreator(update, result.getContentGroup(), obj,
							(TLClass) update.getValueType(),
							update.isMultiple(), labelKey);
					}
					if (rowObjectAnnotation == null || rowObjectAnnotation.isDeletable()) {
						remover = new RowRemover(updateContainer, update.isMultiple());
					}
					if (rowObjectAnnotation != null) {
						isSortable = isSortable && rowObjectAnnotation.isSortable();
					}
					TableListControl tableControl = TableListControl.createTableListControl(tableField,
						DefaultTableRenderer.newInstance(), creator, remover, true, true, !isSortable);
					tableControl.addFocusListener(tableField);
					return tableControl;
				}

			}
		});

		update.setStoreAlgorithm(DELETE_AND_STORE);

		return result;
	}

	static Collection<?> collection(Object value) {
		if (value instanceof Collection<?>) {
			return (Collection<?>) value;
		} else {
			return CollectionUtilShared.singletonOrEmptySet(value);
		}
	}

	private static final class FieldAccessProvider implements TableConfigurationProvider {
		final AttributeUpdateContainer _updateContainer;

		final Accessor<TLFormObject> ACCESSOR = new ReadOnlyAccessor<>() {
			@Override
			public Object getValue(TLFormObject row, String property) {
				TLStructuredTypePart part = resolvePart(row, property);
				if (part == null) {
					// e.g. select column.
					return null;
				}
				FormMember field = getField(row, part);
				if (field instanceof FormField) {
					return ((FormField) field).getValue();
				}
				if (field == null) {
					return row.tValueByName(property);
				}
				return null;
			}
		};

		public class CompositionFieldCellRenderer extends AbstractCellRenderer {

			private Renderer<Object> _renderer;

			private ControlProvider _editControlProvider;

			public CompositionFieldCellRenderer(Renderer<Object> renderer, ControlProvider editControlProvider) {
				_renderer = renderer;
				_editControlProvider = editControlProvider;
			}

			@Override
			public void writeCell(DisplayContext context, TagWriter out, Cell cell) throws IOException {
				TLFormObject row = (TLFormObject) cell.getRowObject();
				String columnName = cell.getColumnName();
				TLStructuredTypePart part = resolvePart(row, columnName);
				if (part == null) {
					// Nothing to render.
					return;
				}
				FormMember field = getField(row, part);
				if (field instanceof Composite) {
					CompositionControlProvider.INSTANCE.createControl(field).write(context, out);
				} else if (field != null) {
					if (_editControlProvider != null) {
						_editControlProvider.createControl(field).write(context, out);
					} else {
						MetaControlProvider.INSTANCE.createControl(field).write(context, out);
					}
				} else {
					Object attrValue = row.tValueByName(columnName);
					if (attrValue != null) {
						_renderer.write(context, out, attrValue);
					}
				}

				if (field != null) {
					new ErrorControl(field, true).write(context, out);
				}
			}
		}

		FormMember getField(TLObject row, TLStructuredTypePart part) {
			AttributeUpdate update = _updateContainer.getAttributeUpdate(part, row);
			if (update == null) {
				return null;
			}
			return update.getField();
		}

		TLStructuredTypePart resolvePart(TLObject row, String column) {
			TLStructuredType type = row.tType();
			TLStructuredTypePart part = type.getPart(column);
			if (part == null) {
				// E.g. for select column
				return null;
			}
			return part;
		}

		/**
		 * Creates a {@link FieldAccessProvider}.
		 */
		public FieldAccessProvider(AttributeUpdateContainer updateContainer) {
			_updateContainer = updateContainer;
		}

		private void adaptColumn(ColumnConfiguration col) {
			col.setAccessor(ACCESSOR);
			if (!TableControl.SELECT_COLUMN_NAME.equals(col.getName())) {
				col.setCellRenderer(
					new CompositionFieldCellRenderer(col.finalRenderer(), col.getEditControlProvider()));
			}
			col.setPreloadContribution(null);
			CellExistenceTester tester = col.getCellExistenceTester();
			if (tester != null && tester != AllCellsExist.INSTANCE && !(tester instanceof UnwrapFormTester)) {
				col.setCellExistenceTester(new UnwrapFormTester(tester));
			}
		}

		@Override
		public void adaptConfigurationTo(TableConfiguration table) {
			for (ColumnConfiguration column : table.getElementaryColumns()) {
				adaptColumn(column);
			}
		}
	}

	/**
	 * {@link CompositeField} holding the actual displayed {@link TableField} as proxy.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public class Composite extends CompositeField implements TableModelListener {

		private final EditContext _context;

		private final ObjectTableModel _tableModel;

		private final FormGroup _contentGroup;

		private final TableField _tableField;

		Composite(EditContext context, String name, ResourceView aLabelRessource,
				ObjectTableModel tableModel, TableField tableField) {
			super(name, aLabelRessource);
			_context = context;
			_tableModel = tableModel;
			_contentGroup = new FormGroup("rows", ResPrefix.NONE);
			_tableField = tableField;

			addMember(_contentGroup);

			_tableModel.addTableModelListener(this);
		}

		@Override
		public void handleTableModelEvent(TableModelEvent event) {
			switch (event.getType()) {
				case TableModelEvent.INSERT:
				case TableModelEvent.DELETE:
				case TableModelEvent.UPDATE:
					Collection<?> rows = _tableModel.getAllRows();
					Object value;
					if (isMultiple()) {
						value = new ArrayList<>(rows);
					} else {
						value = CollectionUtil.getSingleValueFromCollection(rows);
					}
					try {
						getProxy().update(value);
					} catch (VetoException ex) {
						// Cannot process veto.
					}
			}
		}

		/**
		 * The container for fields of containments.
		 */
		public FormGroup getContentGroup() {
			return _contentGroup;
		}

		@Override
		public void initializeField(Object defaultValue) {
			List<TLObject> overlays = new ArrayList<>();
			Collection<String> readOnlyColumns = getReadOnlyColumns(_context);
			for (Object element : collection(defaultValue)) {
				TLObject overlay =
					mkEditContext(_context.getOverlay().getScope(), _contentGroup, (Wrapper) element, readOnlyColumns);
				overlays.add(overlay);
			}
			_tableModel.setRowObjects(overlays);
			// Use the rows as value to be able to use constraints.
			if (isMultiple()) {
				getProxy().initializeField(new ArrayList<>(overlays));
			} else {
				getProxy().initializeField(CollectionUtil.getSingleValueFromCollection(overlays));
			}
		}

		/**
		 * Creates the fields for one row in the result table for a persistent row object.
		 * 
		 * @param contentGroup
		 *        {@link FormContainer} for created fields.
		 * @param editedObject
		 *        The persistent row object.
		 * @param readOnlyColumns
		 *        Name of columns that are read only.
		 */
		private TLObject mkEditContext(AttributeUpdateContainer updateContainer, FormContainer contentGroup,
				Wrapper editedObject, Collection<String> readOnlyColumns) {
			TLFormObject overlay = updateContainer.editObject(editedObject);
			FormContainer rowGroup = updateContainer.getFormContext().createFormContainerForOverlay(overlay);
			rowGroup.setStableIdSpecialCaseMarker(editedObject);
			contentGroup.addMember(rowGroup);
			for (TLStructuredTypePart attribute : editedObject.tType().getAllParts()) {
				if (DisplayAnnotations.isHidden(attribute)) {
					continue;
				}
				addFieldForEditContext(updateContainer, rowGroup, overlay, attribute, readOnlyColumns);
			}
			return overlay;
		}

		/**
		 * Adds a field for the given attribute to the form.
		 * 
		 * <p>
		 * This is used for persistent row objects.
		 * </p>
		 * 
		 * @param contentGroup
		 *        {@link FormContainer} for created fields.
		 * @param editedObject
		 *        The transient edited object.
		 * @param attribute
		 *        The {@link TLStructuredTypePart} to create member for.
		 * @param readOnlyColumns
		 *        Name of columns that are read only.
		 * @return The created {@link FormMember}. May be <code>null</code>.
		 */
		private FormMember addFieldForEditContext(AttributeUpdateContainer updateContainer, FormContainer contentGroup,
				TLFormObject editedObject, TLStructuredTypePart attribute, Collection<String> readOnlyColumns) {
			AttributeFormContext formContext = updateContainer.getFormContext();
			AttributeUpdate update = editedObject.newEditUpdateDefault(attribute, false);
			update.setInTableContext(true);
			return createFieldForUpdate(contentGroup, attribute, readOnlyColumns, formContext, update);
		}

		@Override
		public String getError() {
			FormField proxy = getProxy();
			if (proxy.hasError()) {
				return proxy.getError();
			} else {
				return Resources.getInstance().getString(I18NConstants.COMPOSITE_FIELD_INNER_FIELD_ERROR);
			}
		}

		@Override
		public List<String> getWarnings() {
			FormField proxy = getProxy();
			if (proxy.hasWarnings()) {
				return proxy.getWarnings();
			} else {
				return Collections.singletonList(
					Resources.getInstance().getString(I18NConstants.COMPOSITE_FIELD_INNER_FIELD_WARNING));
			}
		}

		@Override
		public void checkDependency() {
			getProxy().checkDependency();
		}

		@Override
		public boolean addDependant(FormField dependant) {
			return getProxy().addDependant(dependant);
		}

		@Override
		public boolean removeDependant(FormField dependant) {
			return getProxy().removeDependant(dependant);
		}

		@Override
		protected FormField getProxy() {
			return _tableField;
		}

		/**
		 * The business objects for which the rows was created. When there is currently no object
		 * for a rows (e.g. because the row is new), then nothing is contained for such a row is
		 * contained in the result.
		 */
		public List<TLObject> getRowBusinessObjects() {
			Object currentValue = getProxy().getValue();
			List<TLObject> result;
			if (isMultiple()) {
				@SuppressWarnings("unchecked")
				Collection<TLObject> rows = (Collection<TLObject>) currentValue;

				result = new ArrayList<>();
				for (TLObject row : rows) {
					if (row != null) {
						result.add(row);
					}
				}
			} else {
				result = CollectionUtil.singletonOrEmptyList((TLObject) currentValue);
			}
			return result;
		}

		boolean isMultiple() {
			return _context.isMultiple();
		}

	}

	/**
	 * {@link RowObjectRemover} to remove existing rows.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	private final class RowRemover implements RowObjectRemover {

		private final AttributeUpdateContainer _updateContainer;

		private final boolean _multiple;

		/**
		 * Creates a new {@link RowRemover}.
		 * 
		 * @param updateContainer
		 *        The {@link AttributeUpdateContainer} of the form.
		 * @param multiple
		 *        Whether table contain multiple values.
		 */
		RowRemover(AttributeUpdateContainer updateContainer, boolean multiple) {
			_updateContainer = updateContainer;
			_multiple = multiple;
		}

		@Override
		public void removeRow(Object rowObject, Control aControl) {
			TableField table = (TableField) ((TableControl) aControl).getModel();
			TLFormObject row = (TLFormObject) rowObject;

			// Remove updates from update container.
			_updateContainer.removeOverlay(row);

			// Remember deletion of persistent object.
			TLObject editedObject = row.getEditedObject();
			if (editedObject != null) {
				Set<TLObject> deleted = table.get(DELETED);
				if (deleted.isEmpty()) {
					// The default is unmodifiable.
					deleted = new HashSet<>();
					table.set(DELETED, deleted);
				}
				deleted.add(editedObject);
			}

			// Remove from table field value.
			removeValue(table, row);
		}

		private void removeValue(TableField table, TLObject row) {
			if (_multiple) {
				Collection<Object> value = new ArrayList<>((Collection<?>) table.getValue());
				value.remove(row);
				table.setValue(value);
			} else {
				table.setValue(null);
			}
		}
	}

	/**
	 * {@link RowObjectCreator} to create row for new objects.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	private final class RowCreator implements RowObjectCreator {

		private final EditContext _context;

		private final FormContainer _contentGroup;

		private final TLObject _owner;

		private final boolean _multiple;

		private final ResKey _labelKey;

		private Set<TLClass> _rowTypes;

		/**
		 * Creates a new {@link RowCreator}.
		 * @param owner
		 *        The owner of the newly created object.
		 * @param valueType
		 *        The type of objects being created.
		 * @param multiple
		 *        Whether multiple objects can be created.
		 * @param labelKey
		 *        The label of the edit context.
		 */
		RowCreator(EditContext context, FormContainer contentGroup, TLObject owner,
				TLClass valueType, boolean multiple, ResKey labelKey) {
			_context = context;
			_contentGroup = contentGroup;
			_owner = owner;
			_rowTypes = TLModelUtil.getConcreteReflexiveTransitiveSpecializations(valueType);
			_multiple = multiple;
			_labelKey = labelKey;
		}

		@Override
		public Object createNewRow(Control aControl) {
			TLClass valueType;
			Set<TLClass> rowTypes = _rowTypes;
			if (rowTypes.size() == 1) {
				valueType = rowTypes.iterator().next();

				return createRow(aControl, valueType);
			} else {
				SimpleFormDialog dialog =
					new SimpleFormDialog(I18NConstants.CREATE_COMPOSITION_ROW, DisplayDimension.px(350),
						DisplayDimension.px(180)) {
						private SelectField _selectField;

						@Override
						protected void fillFormContext(FormContext context) {
							_selectField = FormFactory.newSelectField(INPUT_FIELD, rowTypes);
							_selectField.setMandatory(true);
							context.addMember(_selectField);
						}

						@Override
						protected void fillButtons(List<CommandModel> buttons) {
							addCancel(buttons);
							buttons.add(MessageBox.button(ButtonType.OK, context -> {
								TLClass selection = (TLClass) _selectField.getSingleSelection();
								TLObject newRow = createRow(aControl, selection);
								((TableListControl) aControl).addNewRow(newRow);
								getDialogModel().getCloseAction().executeCommand(context);
								return HandlerResult.DEFAULT_RESULT;
							}));
						}
					};
				dialog.open(DefaultDisplayContext.getDisplayContext());
				return null;
			}
		}

		final TLObject createRow(Control aControl, TLClass valueType) {
			TableField tableField = (TableField) ((TableControl) aControl).getModel();
			TLObject newRow = mkCreateContext(_context.getOverlay().getScope(), _contentGroup, _owner, valueType);
			addValue(tableField, newRow);
			return newRow;
		}

		/**
		 * Creates the fields for one row in the result table for a new row object.
		 * 
		 * @param owner
		 *        The owner for the new object.
		 * @param type
		 *        The type for the new object.
		 */
		private TLObject mkCreateContext(AttributeUpdateContainer updateContainer, FormContainer contentGroup,
				TLObject owner, TLClass type) {
			AttributeFormContext formContext = updateContainer.getFormContext();

			TLFormObject newObject = updateContainer.newObject(type, owner);
			FormContainer rowGroup = formContext.createFormContainerForOverlay(newObject);
			contentGroup.addMember(rowGroup);
			Collection<String> readOnlyColumns = getReadOnlyColumns(_context);
			for (TLStructuredTypePart attribute : type.getAllParts()) {
				addFieldForCreateContext(formContext, rowGroup, newObject, attribute, readOnlyColumns);
			}
			return newObject;
		}

		/**
		 * Adds a field for the given attribute to the form context.
		 * 
		 * <p>
		 * This is used for new row objects.
		 * </p>
		 * 
		 * @param contentGroup
		 *        {@link FormContainer} for created fields.
		 * @param newObject
		 *        The new object being created.
		 * @param attribute
		 *        The {@link TLStructuredTypePart} to create member for.
		 * @param readOnlyColumns
		 *        Name of columns that are read only.
		 * 
		 * @return The created {@link FormMember}. May be <code>null</code>.
		 */
		private FormMember addFieldForCreateContext(AttributeFormContext formContext, FormContainer contentGroup,
				TLFormObject newObject, TLStructuredTypePart attribute, Collection<String> readOnlyColumns) {
			if (DisplayAnnotations.isHiddenInCreate(attribute)) {
				return null;
			}
			AttributeUpdate update = newObject.newCreateUpdate(attribute);
			update.setInTableContext(true);
			return createFieldForUpdate(contentGroup, attribute, readOnlyColumns, formContext, update);
		}

		private void addValue(TableField table, TLObject row) {
			Collection<?> currentValue = (Collection<?>) table.getValue();
			ArrayList<Object> value;
			if (currentValue == null) {
				value = new ArrayList<>();
			} else {
				value = new ArrayList<>(currentValue);
			}
			value.add(row);
			table.setValue(value);
		}

		@Override
		public ResKey allowCreateNewRow(int row, TableData data, Control control) {
			if (!_multiple && !data.getViewModel().getAllRows().isEmpty()) {
				return I18NConstants.NEW_ROW_DISABLED_NOT_MULTIPLE__ATTRIBUTE.fill(_labelKey);
			}
			return RowObjectCreator.super.allowCreateNewRow(row, data, control);
		}
	}

	private static final class UnwrapFormTester implements CellExistenceTester {
		private final CellExistenceTester _tester;

		UnwrapFormTester(CellExistenceTester tester) {
			_tester = tester;
		}

		@Override
		public boolean isCellExistent(Object rowObject, String columnName) {
			TLObject row = (TLObject) rowObject;
			if (row != null) {
				return _tester.isCellExistent(row, columnName);
			}
			return false;
		}
	}

	@Override
	public boolean renderWholeLine(EditContext editContext) {
		return true;
	}

	Collection<String> getReadOnlyColumns(EditContext context) {
		TLReadOnlyColumns annotation = context.getAnnotation(TLReadOnlyColumns.class);

		if (annotation != null) {
			return annotation.getReadOnlyColumns();
		}

		return Collections.emptySet();
	}

	FormMember createFieldForUpdate(FormContainer contentGroup, TLStructuredTypePart attribute,
			Collection<String> readOnlyColumns, AttributeFormContext formContext, AttributeUpdate update) {
		FormMember field = formContext.createFormMemberForUpdate(update);
		if (field != null) {
			if (readOnlyColumns.contains(attribute.getName())) {
				field.setImmutable(true);
			}
			contentGroup.addMember(field);
		}
		return field;
	}
}
