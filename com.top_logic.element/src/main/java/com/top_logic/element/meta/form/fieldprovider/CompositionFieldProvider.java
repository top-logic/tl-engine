/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.form.fieldprovider;

import static com.top_logic.mig.html.HTMLConstants.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.annotation.FrameworkInternal;
import com.top_logic.basic.col.Maybe;
import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.basic.col.TypedAnnotatable.Property;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.shared.collection.CollectionUtilShared;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey1;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.element.config.annotation.TLReadOnlyColumns;
import com.top_logic.element.layout.formeditor.FormEditorUtil;
import com.top_logic.element.layout.formeditor.builder.TypedForm;
import com.top_logic.element.meta.AttributeUpdate;
import com.top_logic.element.meta.AttributeUpdate.StoreAlgorithm;
import com.top_logic.element.meta.AttributeUpdateContainer;
import com.top_logic.element.meta.form.AttributeFormContext;
import com.top_logic.element.meta.form.EditContext;
import com.top_logic.element.meta.form.FieldProvider;
import com.top_logic.element.meta.form.MetaControlProvider;
import com.top_logic.element.meta.form.controlprovider.CompositionControlProvider;
import com.top_logic.element.meta.form.overlay.DefaultObjectConstructor;
import com.top_logic.element.meta.form.overlay.ObjectConstructor;
import com.top_logic.element.meta.form.overlay.ObjectCreation;
import com.top_logic.element.meta.form.overlay.TLFormObject;
import com.top_logic.element.model.copy.CopyConstructor;
import com.top_logic.element.model.copy.CopyOperation;
import com.top_logic.knowledge.gui.layout.SizeInfo;
import com.top_logic.knowledge.service.KBUtils;
import com.top_logic.knowledge.service.event.Modification;
import com.top_logic.layout.Accessor;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.DisplayDimension;
import com.top_logic.layout.ReadOnlyAccessor;
import com.top_logic.layout.Renderer;
import com.top_logic.layout.ResPrefix;
import com.top_logic.layout.ResourceView;
import com.top_logic.layout.VetoException;
import com.top_logic.layout.basic.AbstractCommandModel;
import com.top_logic.layout.basic.CommandModel;
import com.top_logic.layout.basic.DefaultDisplayContext;
import com.top_logic.layout.basic.fragments.Fragments;
import com.top_logic.layout.form.FormConstants;
import com.top_logic.layout.form.FormContainer;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.component.AbstractApplyCommandHandler;
import com.top_logic.layout.form.control.ButtonControl;
import com.top_logic.layout.form.control.ErrorControl;
import com.top_logic.layout.form.control.ImageButtonRenderer;
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
import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.layout.scripting.recorder.ref.GlobalModelNamingScheme;
import com.top_logic.layout.scripting.recorder.ref.ModelName;
import com.top_logic.layout.scripting.recorder.ref.ModelNamingScheme;
import com.top_logic.layout.scripting.recorder.ref.ModelResolver;
import com.top_logic.layout.scripting.runtime.ActionContext;
import com.top_logic.layout.structure.DefaultDialogModel;
import com.top_logic.layout.structure.DefaultLayoutData;
import com.top_logic.layout.structure.MediaQueryControl;
import com.top_logic.layout.structure.Scrolling;
import com.top_logic.layout.table.AbstractCellRenderer;
import com.top_logic.layout.table.CellRenderer;
import com.top_logic.layout.table.RowObjectCreator;
import com.top_logic.layout.table.RowObjectRemover;
import com.top_logic.layout.table.TableData;
import com.top_logic.layout.table.TableRenderer.Cell;
import com.top_logic.layout.table.control.ColumnDescription;
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
import com.top_logic.model.TLReference;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.annotate.DisplayAnnotations;
import com.top_logic.model.annotate.ui.TLRowObject;
import com.top_logic.model.form.implementation.FormEditorContext;
import com.top_logic.model.form.implementation.FormMode;
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
		public Modification store(AttributeUpdate update) {
			Set<TLObject> deleted = ((Composite) update.getField()).getProxy().get(DELETED);
			super.store(update);

			// Note: Deletion must be delayed until all other updates have been processed.
			// Otherwise, the update may fails because it still accesses deleted objects that have
			// been implicitly deleted due to deletion policies on references.
			return () -> KBUtils.deleteAll(deleted);
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

		TableField tableField =
			FormFactory.newTableField(TABLE_FIELD_NAME, update::getConfigKey);
		boolean isOrdered = update.isOrdered();

		AttributeUpdateContainer updateContainer = update.getOverlay().getScope();

		List<TableConfigurationProvider> tableConfigProviders = new ArrayList<>();
		tableConfigProviders.add(AbstractWrapperFieldProvider.getTableConfigurationProvider(update));
		tableConfigProviders.add(new FieldAccessProvider(updateContainer));

		TableConfiguration config = TableConfigurationFactory.build(tableConfigProviders);
		
		TLDetailDialog annotation = update.getAnnotation(TLDetailDialog.class);
		if (annotation != null) {
			ColumnConfiguration detailDialogColumn = new ColumnDescription("_details");
			detailDialogColumn.setDefaultColumnWidth("30px");
			detailDialogColumn.setCssClass("tblCenter");
			detailDialogColumn.setFilterProvider(null);
			detailDialogColumn.setSortable(false);
			CellRenderer detailsRenderer = new CellRenderer() {
				@Override
				public void writeCell(DisplayContext context, TagWriter out, Cell cell) throws IOException {
					Object rowObj = cell.getRowObject();
					if (rowObj instanceof TLFormObject formObj) {

						/* Compute "immutable" as late as possible,because it is changed late by the
						 * layout component. */
						boolean immutable = tableField.isImmutable();
						CommandModel openCommand =
							new OpenDetailsCommand(update, formObj, cell.getView(), annotation, immutable);

						openCommand.setLabel(I18NConstants.EDIT_DETAILS);
						openCommand.setImage(com.top_logic.layout.table.control.Icons.OPEN_SELECTOR);

						new ButtonControl(openCommand, ImageButtonRenderer.INSTANCE).write(context, out);
					}
				}
			};
			detailDialogColumn.setCellRenderer(detailsRenderer);
			detailDialogColumn.setColumnLabelKey(I18NConstants.DETAIL_OPENER);
			detailDialogColumn.setShowHeader(false);
			config.addColumn(detailDialogColumn);

			List<String> newDefaultColumns = new ArrayList<>(config.getDefaultColumns());
			newDefaultColumns.add(0, "_details");
				config.setDefaultColumns(newDefaultColumns);
		}

		ResPrefix resources = ResPrefix.NONE;
		ObjectTableModel tableModel =
			new ObjectTableModel(config.getDefaultColumns(), config, Collections.emptyList(), isOrdered);

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

	/**
	 * Creates the fields for one row in the result table for a new row object.
	 * @param owner
	 *        The owner for the new object.
	 * @param type
	 *        The type for the new object.
	 */
	public static TLObject mkCreateContext(EditContext editContext, FormContainer contentGroup,
			TLObject owner, TLStructuredType type, ObjectConstructor constructor) {
		AttributeUpdateContainer updateContainer = editContext.getOverlay().getScope();
		AttributeFormContext formContext = updateContainer.getFormContext();

		TLFormObject newObject = updateContainer.newObject(type, owner, constructor);
		FormContainer rowGroup = formContext.createFormContainerForOverlay(newObject);
		contentGroup.addMember(rowGroup);
		Collection<String> readOnlyColumns = getReadOnlyColumns(editContext);
		for (TLStructuredTypePart attribute : type.getAllParts()) {
			addFieldForCreateContext(formContext, rowGroup, newObject, attribute, readOnlyColumns);
		}
		return newObject;
	}

	/**
	 * {@link AbstractCommandModel} that opens the detail dialog for a {@link TLFormObject row}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public static final class OpenDetailsCommand extends AbstractCommandModel {

		private final EditContext _update;

		private final SizeInfo _dialogSize;

		private final TLFormObject _formObj;

		private final TableControl _view;

		private boolean _immutable;

		/**
		 * This constructor creates a new {@link OpenDetailsCommand}.
		 */
		private OpenDetailsCommand(EditContext update, TLFormObject row, TableControl view,
				SizeInfo dialogSize, boolean immutable) {
			_update = update;
			_formObj = row;
			_view = view;
			_dialogSize = dialogSize;
			_immutable = immutable;
			Naming.annotate(this);
		}

		@Override
		protected HandlerResult internalExecuteCommand(DisplayContext openerContext) {
			CopyOperation operation = CopyOperation.initial();
			operation.setContext(_formObj.tContainer(), null);
			operation.setTransient(true);
			Object copy = operation.copyReference(_formObj);
			operation.finish();

			TLObject editObject = (TLObject) copy;

			ResKey title = _dialogSize.getDefaultI18N();
			if (title == null) {
				title = I18NConstants.EDIT_DETAILS_TITLE__TYPE
					.fill(MetaLabelProvider.INSTANCE.getLabel(editObject.tType()));
			} else {
				@SuppressWarnings("deprecation")
				ResKey1 titleTemplate = title.asResKey1();
				title = titleTemplate.fill(MetaLabelProvider.INSTANCE.getLabel(editObject.tType()));
			}
			DefaultDialogModel dialogModel = new DefaultDialogModel(
				new DefaultLayoutData(
					_dialogSize.getWidth(), 100,
					_dialogSize.getHeight(), 100, Scrolling.NO),
				Fragments.message(title),
				true, true, null);

			// See
			// com.top_logic.element.layout.formeditor.builder.EditFormBuilder
			AttributeFormContext formContext;
			{
				formContext = new AttributeFormContext(ResPrefix.NONE);
				TypedForm typedForm = TypedForm.lookup(Collections.emptyMap(), editObject);

				FormEditorContext editContext = new FormEditorContext.Builder()
					.formMode(FormMode.EDIT)
					.formType(typedForm.getFormType())
					.concreteType(typedForm.getDisplayedType())
					.model(editObject)
					.formContext(formContext)
					.contentGroup(formContext)
					.editMode(!_immutable)
					.build();

				FormEditorUtil.createAttributes(editContext, typedForm.getFormDefinition());
				formContext.setImmutable(_immutable);
			}

			// See com.top_logic.layout.form.declarative.DirectFormDisplay.
			MediaQueryControl content = new MediaQueryControl(
				Fragments.div(FormConstants.FORM_BODY_CSS_CLASS,
					MetaControlProvider.INSTANCE.createControl(formContext)));

			content.setConstraint(DefaultLayoutData.DEFAULT_CONSTRAINT);

			if (_immutable) {
				return MessageBox.open(openerContext, dialogModel, content,
					Arrays.asList(MessageBox.button(ButtonType.CLOSE, dialogModel.getCloseAction())));
			}

			CommandModel okButton = MessageBox.button(ButtonType.OK, applyContext -> {
				if (!formContext.checkAll()) {
					return AbstractApplyCommandHandler.createErrorResult(formContext);
				}
				formContext.store();

				Map<TLObject, TLObject> origByCopy = new HashMap<>();
				for (var entry : operation.getCorrespondence().entrySet()) {
					origByCopy.put(entry.getValue(), entry.getKey());
				}

				HashSet<TLObject> didCopy = new HashSet<>();

				// Copy back.
				CopyConstructor constructor = new CopyConstructor() {
					@Override
					public TLObject allocate(TLObject orig, TLReference reference,
							TLObject copyContext) {
						// The orig here is an object from the copied graph
						// created above!

						// Note, newly allocated objects return null and are
						// allocated by the default constructor.
						TLObject existing = origByCopy.get(orig);
						if (existing != null) {
							didCopy.add(existing);
							return existing;
						}

						TLObject copiedContainer = origByCopy.get(copyContext);

						TLObject result =
							mkCreateContext(_update, _formObj.getFormContainer(), copiedContainer,
								orig.tType(), lookupConstructor(_formObj));

						// Keep new object, in case an inner allocation
						// has also happened.
						origByCopy.put(orig, result);
						didCopy.add(result);

						return result;
					}
				};
				CopyOperation copyBack =
					CopyOperation.initial().setTransient(true).setConstructor(constructor);
				copyBack.copyReference(editObject);
				copyBack.finish();

				// All objects that have not used during the copy-back
				// operation are inner compositions that must be deleted,
				// since they were deleted transiently during the edit
				// operation in the dialog.
				Collection<TLObject> toDelete = new HashSet<>(origByCopy.values());
				toDelete.removeAll(didCopy);

				_view.getModel().mkSet(DELETED).addAll(toDelete);

				return dialogModel.getCloseAction().executeCommand(applyContext);
			});

			dialogModel.setDefaultCommand(okButton);
			return MessageBox.open(openerContext, dialogModel, content,
				Arrays.asList(okButton,
					MessageBox.button(ButtonType.CANCEL, dialogModel.getCloseAction())));
		}

		/**
		 * {@link ModelNamingScheme} for am {@link OpenDetailsCommand}.
		 * 
		 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
		 */
		public static class Naming extends GlobalModelNamingScheme<OpenDetailsCommand, Naming.Name> {

			private static final Property<Map<Object, OpenDetailsCommand>> DETAIL_OPENERS =
				TypedAnnotatable.propertyMap("Composition detail openers");

			/**
			 * {@link ModelName} identifying an {@link OpenDetailsCommand}.
			 * 
			 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
			 */
			@Label("Detail of '{row}'")
			public interface Name extends ModelName {

				/**
				 * Name of the displayed table.
				 */
				@Mandatory
				ModelName getTable();

				/**
				 * Setter for {@link #getTable()}.
				 */
				void setTable(ModelName value);

				/**
				 * Name of the row for which the detail dialog is opened.
				 */
				@Mandatory
				ModelName getRow();

				/**
				 * Setter for {@link #getRow()}.
				 */
				void setRow(ModelName value);

			}

			/**
			 * Creates a new {@link Naming}.
			 */
			public Naming() {
				super(OpenDetailsCommand.class, Name.class);
			}

			@Override
			public Maybe<Name> buildName(OpenDetailsCommand model) {
				TableData owner = findOwner(model);
				Maybe<? extends ModelName> ownerName = ModelResolver.buildModelNameIfAvailable(owner);
				Maybe<? extends ModelName> rowName = ModelResolver.buildModelNameIfAvailable(owner, findRow(model));
				if (ownerName.hasValue() && rowName.hasValue()) {
					Name name = TypedConfiguration.newConfigItem(Name.class);
					name.setTable(ownerName.get());
					name.setRow(rowName.get());
					return Maybe.some(name);
				}
				return Maybe.none();
			}


			private static TLFormObject findRow(OpenDetailsCommand model) {
				return model._formObj;
			}

			private static TableData findOwner(OpenDetailsCommand model) {
				return model._view.getTableData();
			}

			@Override
			public OpenDetailsCommand locateModel(ActionContext context, Name name) {
				TypedAnnotatable owner = (TypedAnnotatable) ModelResolver.locateModel(context, name.getTable());
				Object row = ModelResolver.locateModel(context, owner, name.getRow());
				return owner.get(DETAIL_OPENERS).get(row);
			}
			
			static void annotate(OpenDetailsCommand command) {
				findOwner(command).mkMap(DETAIL_OPENERS).put(findRow(command), command);
			}


		}
	}

	private static final class FieldAccessProvider implements TableConfigurationProvider {
		final AttributeUpdateContainer _updateContainer;

		final Accessor<TLFormObject> _formContextAccessor = new ReadOnlyAccessor<>() {
			@Override
			public Object getValue(TLFormObject row, String property) {
				TLStructuredTypePart part = resolvePart(row, property);
				if (part == null) {
					// e.g. select column.
					return null;
				}

				TLFormObject overlay = _updateContainer.getOverlay(row, null);
				if (overlay != null) {
					return overlay.getFieldValue(part);
				} else {
					return row.tValueByName(property);
				}
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
				if (field != null) {
					out.beginBeginTag(DIV);
					out.writeAttribute(CLASS_ATTR, FormConstants.DECORATED_CELL2_CSS_CLASS);
					out.endBeginTag();

					out.beginBeginTag(DIV);
					out.writeAttribute(CLASS_ATTR, FormConstants.FLEXIBLE2_CSS_CLASS);
					out.endBeginTag();
				}

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
					out.endTag(DIV);

					out.beginBeginTag(DIV);
					out.writeAttribute(CLASS_ATTR, FormConstants.FIXED_RIGHT2_CSS_CLASS);
					out.endBeginTag();
					{
						ErrorControl errorControl = new ErrorControl(field, true);
						errorControl.write(context, out);
					}
					out.endTag(DIV);

					out.endTag(DIV);
				}
			}
		}

		FormMember getField(TLObject row, TLStructuredTypePart part) {
			TLFormObject overlay = _updateContainer.getOverlay(row, null);
			if (overlay == null) {
				return null;
			}
			return overlay.getField(part);
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
			if (TableControl.SELECT_COLUMN_NAME.equals(col.getName())) {
				return;
			}

			col.setCellRenderer(
				new CompositionFieldCellRenderer(col.finalRenderer(), col.getEditControlProvider()));
			col.setAccessor(_formContextAccessor);
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
					mkEditContext(_context.getOverlay().getScope(), _contentGroup, (TLObject) element, readOnlyColumns);
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
		public static TLObject mkEditContext(AttributeUpdateContainer updateContainer, FormContainer contentGroup,
				TLObject editedObject, Collection<String> readOnlyColumns) {
			TLFormObject existingOverlay = updateContainer.getExistingOverlay(editedObject);
			if (existingOverlay != null) {
				return existingOverlay;
			}

			TLFormObject newOverlay = updateContainer.editObject(editedObject);
			FormContainer rowGroup = updateContainer.getFormContext().createFormContainerForOverlay(newOverlay);
			rowGroup.setStableIdSpecialCaseMarker(editedObject);
			contentGroup.addMember(rowGroup);
			for (TLStructuredTypePart attribute : editedObject.tType().getAllParts()) {
				if (DisplayAnnotations.isHidden(attribute)) {
					continue;
				}
				addFieldForEditContext(updateContainer, rowGroup, newOverlay, attribute, readOnlyColumns);
			}
			return newOverlay;
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
		private static FormMember addFieldForEditContext(AttributeUpdateContainer updateContainer,
				FormContainer contentGroup,
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

		/**
		 * The table showing composition entries as rows.
		 */
		public TableField getTableField() {
			return _tableField;
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
				table.mkSet(DELETED).add(editedObject);
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
						DisplayDimension.px(280)) {
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
			TLObject newRow = mkCreateContext(_context, _contentGroup, _owner, valueType, lookupConstructor(_owner));
			addValue(tableField, newRow);
			return newRow;
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

	static ObjectConstructor lookupConstructor(TLObject obj) {
		ObjectConstructor constuctor;
		if (obj instanceof ObjectCreation creation) {
			constuctor = creation.getConstructor();
		} else {
			constuctor = obj.tTransient()
				? DefaultObjectConstructor.getTransientInstance()
				: DefaultObjectConstructor.getPersistentInstance();
		}
		return constuctor;
	}

	@Override
	public boolean renderWholeLine(EditContext editContext) {
		return true;
	}

	static Collection<String> getReadOnlyColumns(EditContext context) {
		TLReadOnlyColumns annotation = context.getAnnotation(TLReadOnlyColumns.class);

		if (annotation != null) {
			return annotation.getReadOnlyColumns();
		}

		return Collections.emptySet();
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
	private static FormMember addFieldForCreateContext(AttributeFormContext formContext, FormContainer contentGroup,
			TLFormObject newObject, TLStructuredTypePart attribute, Collection<String> readOnlyColumns) {
		if (DisplayAnnotations.isHiddenInCreate(attribute)) {
			return null;
		}
		AttributeUpdate update = newObject.newCreateUpdate(attribute);
		update.setInTableContext(true);
		return createFieldForUpdate(contentGroup, attribute, readOnlyColumns, formContext, update);
	}

	static FormMember createFieldForUpdate(FormContainer contentGroup, TLStructuredTypePart attribute,
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
