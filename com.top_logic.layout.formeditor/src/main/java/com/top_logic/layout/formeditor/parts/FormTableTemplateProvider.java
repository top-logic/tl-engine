/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.formeditor.parts;

import static com.top_logic.layout.form.template.model.Templates.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.element.layout.formeditor.FormEditorUtil;
import com.top_logic.element.layout.formeditor.implementation.FieldDefinitionTemplateProvider;
import com.top_logic.element.meta.AttributeUpdateContainer;
import com.top_logic.element.meta.form.AttributeFormContext;
import com.top_logic.element.meta.form.MetaControlProvider;
import com.top_logic.element.meta.form.overlay.ObjectEditing;
import com.top_logic.element.meta.form.overlay.TLFormObject;
import com.top_logic.element.meta.gui.MetaAttributeGUIHelper;
import com.top_logic.html.template.HTMLTemplateFragment;
import com.top_logic.html.template.TagTemplate;
import com.top_logic.layout.Accessor;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.DisplayDimension;
import com.top_logic.layout.DisplayUnit;
import com.top_logic.layout.ImageProvider;
import com.top_logic.layout.ReadOnlyAccessor;
import com.top_logic.layout.basic.ErrorFragmentGenerator;
import com.top_logic.layout.editor.config.OptionalTypeTemplateParameters;
import com.top_logic.layout.form.FormContainer;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.control.Icons;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.FormGroup;
import com.top_logic.layout.form.model.TableField;
import com.top_logic.layout.formeditor.parts.FormTableDefinition.AttributeColumn;
import com.top_logic.layout.formeditor.parts.FormTableDefinition.ColumnDisplay;
import com.top_logic.layout.formeditor.parts.FormTableDefinition.ColumnDisplayVisitor;
import com.top_logic.layout.table.AbstractCellRenderer;
import com.top_logic.layout.table.CellAdapter;
import com.top_logic.layout.table.CellClassProvider;
import com.top_logic.layout.table.ConfigKey;
import com.top_logic.layout.table.TableRenderer.Cell;
import com.top_logic.layout.table.command.TableCommandConfig;
import com.top_logic.layout.table.command.TableCommandProvider;
import com.top_logic.layout.table.filter.CellExistenceTester;
import com.top_logic.layout.table.model.ColumnConfiguration;
import com.top_logic.layout.table.model.ColumnCustomization;
import com.top_logic.layout.table.model.Enabled;
import com.top_logic.layout.table.model.FieldProvider;
import com.top_logic.layout.table.model.FormTableModel;
import com.top_logic.layout.table.model.ObjectTableModel;
import com.top_logic.layout.table.model.TableConfigUtil;
import com.top_logic.layout.table.model.TableConfiguration;
import com.top_logic.layout.table.model.TableConfigurationFactory;
import com.top_logic.layout.table.model.TableConfigurationProvider;
import com.top_logic.layout.table.model.TableUtil;
import com.top_logic.layout.table.provider.GenericTableConfigurationProvider;
import com.top_logic.mig.html.DefaultMultiSelectionModel;
import com.top_logic.mig.html.HTMLConstants;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.annotate.AnnotationContainer;
import com.top_logic.model.form.ReactiveFormCSS;
import com.top_logic.model.form.definition.AttributeDefinition;
import com.top_logic.model.form.definition.FormVisibility;
import com.top_logic.model.form.implementation.AbstractFormElementProvider;
import com.top_logic.model.form.implementation.FormEditorContext;
import com.top_logic.model.form.implementation.FormMode;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.query.QueryExecutor;

/**
 * Creates a template for a {@link FormTableDefinition} and stores the necessary information.
 * 
 * @author <a href="mailto:iwi@top-logic.com">Isabell Wittich</a>
 */
public class FormTableTemplateProvider extends AbstractFormElementProvider<FormTableDefinition> {

	/**
	 * {@link FieldProvider} for the declared columns.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	private static final class FieldProviderImpl implements FieldProvider {

		private final AttributeColumn _col;

		private final AttributeFormContext _formContext;

		private final FormContainer _contentGroup;

		private final TLStructuredTypePart _part;

		private final Map<TLObject, FormContainer> _rowGroups;

		/**
		 * Creates a new {@link FieldProviderImpl}.
		 */
		FieldProviderImpl(TLStructuredTypePart part, AttributeColumn col, AttributeFormContext formContext,
				FormContainer contentGroup, Map<TLObject, FormContainer> rowGroups) {
			_col = col;
			_part = part;
			_formContext = formContext;
			_contentGroup = contentGroup;
			_rowGroups = rowGroups;
		}

		@Override
		public String getFieldName(Object aModel, Accessor anAccessor, String aProperty) {
			ObjectEditing overlay = (ObjectEditing) aModel;
			return MetaAttributeGUIHelper.getAttributeID(_part, overlay.getEditedObject());
		}

		@Override
		public FormMember createField(Object aModel, Accessor anAccessor, String aProperty) {
			TLObject rowType = (TLObject) aModel;
			boolean isDisabled = false;

			// Use the row-specific group if available, otherwise fall back to contentGroup
			FormContainer targetGroup = _rowGroups != null ? _rowGroups.get(rowType) : _contentGroup;
			if (targetGroup == null) {
				targetGroup = _contentGroup;
			}

			FormMember field =
				FormEditorUtil.createAnotherMetaAttributeForEdit(_formContext, targetGroup, _part, rowType,
					isDisabled, AnnotationContainer.EMPTY);
			if (field != null) {
				FormVisibility visibility = _col.getVisibility();
				visibility.applyTo(field);
			}
			return field;
		}
	}

	/** Width of the dialog to edit the attributes. */
	private static final DisplayDimension WIDTH = DisplayDimension.dim(300, DisplayUnit.PIXEL);

	/** Height of the dialog to edit the attributes. */
	private static final DisplayDimension HEIGHT = DisplayDimension.dim(700, DisplayUnit.PIXEL);

	private static final boolean IS_TOOL = true;

	private static final boolean OPEN_DIALOG = true;

	private static final ResKey LABEL = I18NConstants.FORM_EDITOR__TOOL_NEW_TABLE;

	/**
	 * Compiled {@link QueryExecutor} for the rows expression, or <code>null</code> if no rows
	 * expression is configured.
	 */
	private final QueryExecutor _rowsExecutor;

	/**
	 * Compiled {@link QueryExecutor} for the dynamic label expression, or <code>null</code> if no
	 * dynamic label expression is configured.
	 */
	private final QueryExecutor _dynamicLabelExecutor;

	private final boolean _selectable;

	/**
	 * Create a new {@link FormTableTemplateProvider} for a {@link FormTableDefinition} in a given
	 * {@link InstantiationContext}.
	 */
	public FormTableTemplateProvider(InstantiationContext context, FormTableDefinition config) {
		super(context, config);
		_rowsExecutor = QueryExecutor.compileOptional(config.getRows());
		_dynamicLabelExecutor = QueryExecutor.compileOptional(config.getDynamicLabel());
		_selectable = config.getSelectable();
	}

	@Override
	protected HTMLTemplateFragment createDisplayTemplate(FormEditorContext context) {
		TLObject model = context.getModel();
		String fieldName = fieldName();
		FormContainer contentGroup = context.getContentGroup();
		if (contentGroup.hasMember(fieldName)) {
			/* Return existing member */
			return templateForMember(fieldName);
		}

		boolean inDesignMode = context.getFormMode() == FormMode.DESIGN;

		Collection<ColumnDisplay> configuredCols = colums();

		ColumnDisplayVisitor<String, Void> columnNameProvider = columnNameProvider();

		List<String> visibleColumNames = new ArrayList<>();
		for (ColumnDisplay column : configuredCols) {
			visibleColumNames.add(column.visit(columnNameProvider, null));
		}

		List<?> rows = rows(model);
		AttributeFormContext formContext = (AttributeFormContext) context.getFormContext();
		AttributeUpdateContainer attributeUpdateContainer = formContext.getAttributeUpdateContainer();
		List<TLObject> overlays = new ArrayList<>();
		Map<TLObject, FormContainer> rowGroups = new HashMap<>();
		for (Object row : rows) {
			TLFormObject overlay = attributeUpdateContainer.editObject((TLObject) row);
			FormContainer rowGroup = attributeUpdateContainer.getFormContext().createFormContainerForOverlay(overlay);
			rowGroup.setStableIdSpecialCaseMarker(row);
			contentGroup.addMember(rowGroup);
			rowGroups.put((TLObject) row, rowGroup);
			overlays.add(overlay);
		}
		TableConfiguration tableConfig = TableConfigurationFactory.build(
			genericProvider(),
			adaptColumns(formContext, contentGroup, rowGroups, configuredCols, columnNameProvider),
			GenericTableConfigurationProvider.showColumns(visibleColumNames),
			tableTitleProvider(model),
			additionalCommands(),
			deactivateTableFeatures(inDesignMode));

		ObjectTableModel otm = new ObjectTableModel(visibleColumNames, tableConfig, overlays);

		FormGroup group = new FormGroup(fieldName + "_container", contentGroup.getResources());
		contentGroup.addMember(group);

		TableField tableField = newTableField(fieldName, inDesignMode);
		tableField.setTableModel(new FormTableModel(otm, group));

		contentGroup.addMember(tableField);

		return templateForMember(fieldName);
	}

	private TableConfigurationProvider additionalCommands() {
		List<TableCommandConfig> commands = getConfig().getCommands();
		if (commands.isEmpty()) {
			return TableConfigurationFactory.emptyProvider();
		}
		return new TableConfigurationProvider() {
			@Override
			public void adaptConfigurationTo(TableConfiguration table) {
				table.setCommands(
					TableConfigUtil.indexCommands(SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY,
						table.getCommands(), commands));
			}
		};
	}

	private String fieldName() {
		String fieldName = getConfig().getFormFieldName();
		if (fieldName == null) {
			fieldName = StringServices.randomUUID();
			getConfig().setFormFieldName(fieldName);
		}
		return fieldName;
	}

	private TableField newTableField(String fieldName, boolean inDesignMode) {
		TableField tableField;
		if (inDesignMode) {
			/* Deactivate loading of personal settings when the table is designed to avoid
			 * confusion. */
			tableField = FormFactory.newTableField(fieldName, ConfigKey.none());
		} else {
			tableField = FormFactory.newTableField(fieldName);
		}
		tableField.setSelectable(_selectable);
		tableField.setSelectionModel(new DefaultMultiSelectionModel(tableField));
		return tableField;
	}

	private TableConfigurationProvider adaptColumns(FormContext formContext, FormContainer contentGroup,
			Map<TLObject, FormContainer> rowGroups,
			Collection<ColumnDisplay> colums,
			ColumnDisplayVisitor<String, Void> columnNameProvider) {
		ColumnDisplayVisitor<Void, ColumnConfiguration> adaptColumn =
			adaptColumnVisitor(formContext, contentGroup, rowGroups);
		TableConfigurationProvider adaptColumns = new TableConfigurationProvider() {
			@Override
			public void adaptConfigurationTo(TableConfiguration table) {
				for (ColumnDisplay column : colums) {
					ColumnConfiguration colConfig = table.declareColumn(column.visit(columnNameProvider, null));
					column.visit(adaptColumn, colConfig);
				}
			}
		};
		return adaptColumns;
	}

	private TableConfigurationProvider genericProvider() {
		TLClass tableType = OptionalTypeTemplateParameters.resolve(getConfig());
		TableConfigurationProvider genericProvider;
		if (tableType != null) {
			genericProvider = GenericTableConfigurationProvider.getTableConfigurationProvider(tableType);
		} else {
			genericProvider = TableConfigurationFactory.emptyProvider();
		}
		return genericProvider;
	}

	private List<?> rows(TLObject model) {
		if (model == null) {
			return Collections.emptyList();
		}
		List<?> rows;
		if (_rowsExecutor != null) {
			rows = CollectionUtil.toList((Collection<?>) _rowsExecutor.execute(model));
		} else {
			rows = Collections.emptyList();
		}
		return rows;
	}

	private TableConfigurationProvider deactivateTableFeatures(boolean inDesignMode) {
		TableConfigurationProvider deactivateFeaturesForDesign;
		if (inDesignMode) {
			deactivateFeaturesForDesign = new TableConfigurationProvider() {

				@Override
				public void adaptConfigurationTo(TableConfiguration table) {
					table.setMultiSort(Enabled.never);
					table.setColumnCustomization(ColumnCustomization.NONE);
					deactivateCommands(table);
				}

				private void deactivateCommands(TableConfiguration table) {
					Map<String, Collection<TableCommandProvider>> commands = table.getCommands();
					if (commands == null || commands.isEmpty()) {
						return;
					}
					HashMap<String, Collection<TableCommandProvider>> newCommands = new HashMap<>();
					for (Entry<String, Collection<TableCommandProvider>> entry : table.getCommands().entrySet()) {
						for (TableCommandProvider command : entry.getValue()) {
							TableConfigUtil.addCommand(newCommands, entry.getKey(),
								new DeactivatedTableCommandProvider(command));
						}
					}
					table.setCommands(newCommands);
				}
			};
		} else {
			deactivateFeaturesForDesign = TableConfigurationFactory.emptyProvider();
		}
		return deactivateFeaturesForDesign;
	}

	private TableConfigurationProvider tableTitleProvider(TLObject model) {
		ResKey labelKey = getConfig().getLabel();
		TableConfigurationProvider tableTitle;
		if (_dynamicLabelExecutor != null) {
			// Calculate dynamic label based on the model and static label
			Object dynamicLabelResult = _dynamicLabelExecutor.execute(model, labelKey);
			ResKey dynamicLabelKey = SearchExpression.asResKey(dynamicLabelResult);
			tableTitle = new TableConfigurationProvider() {
				@Override
				public void adaptConfigurationTo(TableConfiguration table) {
					table.setTitleKey(dynamicLabelKey);
				}
			};
		} else if (labelKey != null) {
			// Use static label
			tableTitle = new TableConfigurationProvider() {
				@Override
				public void adaptConfigurationTo(TableConfiguration table) {
					table.setTitleKey(ResKey.message(labelKey, model));
				}
			};
		} else {
			tableTitle = TableConfigurationFactory.emptyProvider();
		}
		return tableTitle;
	}

	private ColumnDisplayVisitor<Void, ColumnConfiguration> adaptColumnVisitor(FormContext formContext,
			FormContainer contentGroup, Map<TLObject, FormContainer> rowGroups) {
		return new ColumnDisplayVisitor<Void, ColumnConfiguration>() {

			@Override
			public Void visitAttributeColumn(AttributeColumn col, ColumnConfiguration arg) {
				try {
					TLStructuredTypePart part = AttributeDefinition.resolvePart(col);
					if (part != null) {
						TLStructuredTypePart definition = part.getDefinition();
						String partName = definition.getName();

						CellExistenceTester cellExistenceTester = new CellExistenceTester() {
							@Override
							public boolean isCellExistent(Object rowObject, String columnName) {
								if (rowObject instanceof TLObject obj) {
									TLStructuredTypePart definedPart = obj.tType().getPart(partName);
									return definedPart != null && definedPart.getDefinition() == definition;
								}
								return false;
							}
						};

						arg.setCellExistenceTester(cellExistenceTester);

						AttributeFormContext attributeFormContext = (AttributeFormContext) formContext;
						arg.setFieldProvider(
							new FieldProviderImpl(part, col, attributeFormContext, contentGroup, rowGroups));
						arg.setControlProvider(MetaControlProvider.INSTANCE);
						arg.setAccessor(new ReadOnlyAccessor<>() {
							@Override
							public Object getValue(Object row, String property) {
								TLObject rowObject = (TLObject) row;
								TLFormObject overlay =
									attributeFormContext.getAttributeUpdateContainer().getOverlay(rowObject, null);
								if (overlay != null) {
									return overlay.getFieldValue(part);
								} else {
									return rowObject.tValueByName(property);
								}
							}
						});

						// Classes are displayed dynamically by the fields in the cells.
						CellClassProvider cssClassProvider = arg.getCssClassProvider();
						if (cssClassProvider != null) {
							arg.setCssClassProvider(new FormCellClassProvider(cssClassProvider));
						}
					} else {
						handleInvalidPart(col, arg);
					}
				} catch (ConfigurationException ex) {
					handleInvalidPart(col, arg);

				}
				TableUtil.setBusinessModelMapping(arg);
				return null;
			}

			private void handleInvalidPart(AttributeColumn col, ColumnConfiguration arg) {
				// part can not be resolved
				ResKey error = errorKey(col);
				arg.setCellRenderer(new AbstractCellRenderer() {

					@Override
					public void writeCell(DisplayContext context, TagWriter out, Cell cell) throws IOException {
						ErrorFragmentGenerator.writeErrorFragment(context, out, HTMLConstants.DIV, error, null);
					}
				});
				arg.setColumnLabelKey(error);
			}

			private ResKey errorKey(AttributeColumn col) {
				ResKey errorKey;
				try {
					TLStructuredType owner = AttributeDefinition.resolveOwner(col);
					errorKey = FieldDefinitionTemplateProvider.noSuchAttributeErrorKey(owner, col.getName());
				} catch (ConfigurationException ex) {
					errorKey = ex.getErrorKey();
				}
				return errorKey;
			}

		};
	}

	private ColumnDisplayVisitor<String, Void> columnNameProvider() {
		return new ColumnDisplayVisitor<String, Void>() {

			@Override
			public String visitAttributeColumn(AttributeColumn col, Void arg) {
				return col.getName();
			}

		};
	}

	private HTMLTemplateFragment templateForMember(String fieldName) {
		TagTemplate contentFragment = div(css("rf_keepInline"), member(fieldName));
		// Override grid-template-columns to prevent table overflow while maintaining full width
		// usage
		return div(css(ReactiveFormCSS.RF_LINE + " " + ReactiveFormCSS.RF_GRID_MINMAX),
			contentFragment);
	}

	private Collection<ColumnDisplay> colums() {
		List<ColumnDisplay> columns = getConfig().getColumns();
		if (columns != null) {
			return columns;
		}
		return Collections.emptyList();
	}

	@Override
	public boolean getWholeLine(TLStructuredType modelType) {
		return true;
	}

	@Override
	public boolean getIsTool() {
		return IS_TOOL;
	}

	@Override
	public ImageProvider getImageProvider() {
		return (any, flavor) -> Icons.FORM_EDITOR__TABLE;
	}

	@Override
	public ResKey getLabel(FormEditorContext context) {
		return LABEL;
	}

	@Override
	protected DisplayDimension getDialogWidth() {
		return WIDTH;
	}

	@Override
	protected DisplayDimension getDialogHeight() {
		return HEIGHT;
	}

	@Override
	public boolean openDialog() {
		return OPEN_DIALOG;
	}

	/**
	 * {@link CellClassProvider} that is wrapped around a custom {@link CellClassProvider} to shield
	 * it from grid internals.
	 */
	private class FormCellClassProvider implements CellClassProvider {

		private final CellClassProvider _wrappedTester;

		FormCellClassProvider(CellClassProvider wrappedTester) {
			_wrappedTester = wrappedTester;
		}

		@Override
		public String getCellClass(Cell cell) {
			Object value = cell.getValue();
			if (value instanceof FormField field) {
				if (field.isActive()) {
					// Class is set on the field.
					return null;
				}
			}
			return _wrappedTester.getCellClass(wrap(cell));
		}

		private Cell wrap(Cell cell) {
			return new CellAdapter() {
				@Override
				public Object getValue() {
					Object value = super.getValue();
					if (value instanceof FormField field) {
						return field.getValue();
					}
					return value;
				}

				@Override
				protected Cell impl() {
					return cell;
				}
			};
		}
	}

}