/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.providers;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.basic.col.TypedAnnotatable.Property;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Hidden;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.ValueInitializer;
import com.top_logic.basic.config.misc.TypedConfigUtil;
import com.top_logic.basic.config.order.DisplayOrder;
import com.top_logic.basic.util.ResKey;
import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.element.meta.form.CustomEditContext;
import com.top_logic.element.meta.form.EditContext;
import com.top_logic.element.meta.form.FieldProvider;
import com.top_logic.element.meta.form.FieldProviderAnnotation;
import com.top_logic.knowledge.service.Revision;
import com.top_logic.layout.Accessor;
import com.top_logic.layout.component.ComponentUtil;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.values.edit.initializer.UUIDInitializer;
import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.layout.table.filter.AllCellsExist;
import com.top_logic.layout.table.model.AbstractFieldProvider;
import com.top_logic.layout.table.model.ColumnConfiguration;
import com.top_logic.layout.table.model.ColumnConfiguration.DisplayMode;
import com.top_logic.layout.table.model.TableConfiguration;
import com.top_logic.layout.table.model.TableConfigurationProvider;
import com.top_logic.layout.table.provider.ColumnInfo;
import com.top_logic.layout.table.provider.generic.ColumnInfoFactory;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLType;
import com.top_logic.model.annotate.AnnotatedConfig;
import com.top_logic.model.annotate.TLAttributeAnnotation;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.query.QueryExecutor;
import com.top_logic.model.util.AnnotatedTypeContext;
import com.top_logic.model.util.ConcreteTypeContext;
import com.top_logic.model.util.TLTypeContext;

/**
 * {@link TableConfigurationProvider} that adds a dynamic number of additional columns to its table.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 * 
 * @see ComputedColumnProviderByExpression
 */
@InApp
public class DynamicColumnProviderByExpression
		extends AbstractConfiguredInstance<DynamicColumnProviderByExpression.Config<?>>
		implements TableConfigurationProvider {

	/**
	 * Configuration options for {@link DynamicColumnProviderByExpression}.
	 */
	@DisplayOrder({
		Config.COLUMNS,
		Config.COLUMN_LABEL,
		Config.COLUMN_TYPE,
		Config.COLUMN_VISIBILITY,
		Config.ACCESSOR,
		Config.UPDATER,
		Config.ANNOTATIONS,
	})
	public interface Config<I extends DynamicColumnProviderByExpression>
			extends PolymorphicConfiguration<I>, AnnotatedConfig<TLAttributeAnnotation> {

		/**
		 * @see #getIdPrefix()
		 */
		String ID_PREFIX = "idPrefix";

		/**
		 * @see #getColumns()
		 */
		String COLUMNS = "columns";

		/**
		 * @see #getColumnLabel()
		 */
		String COLUMN_LABEL = "columnLabel";

		/**
		 * @see #getColumnType()
		 */
		String COLUMN_TYPE = "columnType";

		/**
		 * @see #getColumnVisibility()
		 */
		String COLUMN_VISIBILITY = "columnVisibility";

		/**
		 * @see #getAccessor()
		 */
		String ACCESSOR = "accessor";

		/**
		 * @see #getUpdater()
		 */
		String UPDATER = "updater";

		/**
		 * Common technical prefix that is added to each technical column name of all columns
		 * created by this provider.
		 * 
		 * @implNote The value is only relevant to construct technical identifiers for columns that
		 *           serve as anchor for storing personalization information.
		 */
		@Name(ID_PREFIX)
		@Hidden
		@ValueInitializer(UUIDInitializer.class)
		@Mandatory
		String getIdPrefix();

		/**
		 * Function computing columns.
		 * 
		 * <p>
		 * A column is an arbitrary object that identifies the column. This column object is passed
		 * to the other functions for label computation, and value storage and retrieval.
		 * </p>
		 * 
		 * <p>
		 * The columns function expects the component's model as single argument. Expected as result
		 * is an arbitrary list of objects that serve as representatives for the generated table
		 * columns.
		 * </p>
		 * 
		 * <pre>
		 * <code>model -> [...]</code>
		 * </pre>
		 * 
		 * 
		 * @implNote Each of the dynamically created columns is associated with its
		 *           {@link DynamicColumnProviderByExpression#getColumnModel(ColumnConfiguration)
		 *           column model} retrieved by this function.
		 */
		@Name(COLUMNS)
		@Mandatory
		Expr getColumns();

		/**
		 * Optional function computing the column label.
		 * 
		 * <p>
		 * The function expects two arguments. The first argument is one of the column objects as
		 * returned by the {@link #getColumns()} function. The second argument is the component's
		 * model.
		 * </p>
		 * 
		 * <pre>
		 * <code>column -> model -> ...</code>
		 * </pre>
		 * 
		 * <p>
		 * The column name is expected as result. The result can either be an internationalization
		 * resource, a string, or any other object whose textual representation can be shown in the
		 * column header.
		 * </p>
		 * 
		 * <p>
		 * If no label function is specified, the textual representation of the column object as
		 * computed by the {@link #getColumns()} function is used as label.
		 * </p>
		 */
		@Name(COLUMN_LABEL)
		Expr getColumnLabel();

		/**
		 * Type of the values displayed in a certain column.
		 * 
		 * <p>
		 * If all columns have the same value type, a model type literal can be given, e.g.
		 * `tl.core:Integer`. If columns have different types, a function can be specified that
		 * computes the column type.
		 * </p>
		 * 
		 * <p>
		 * The type function expects two arguments. The first argument is one of the column objects
		 * as returned by the {@link #getColumns()} function. The second argument is the component's
		 * model.
		 * </p>
		 * 
		 * <pre>
		 * <code>column -> model -> ...</code>
		 * </pre>
		 * 
		 * <p>
		 * The result of the type function must be reference to a <i>TopLogic</i> type (a primitive
		 * type such as `tl.core:Integer`, an enumeration or any other class type.
		 * </p>
		 */
		@Name(COLUMN_TYPE)
		@Mandatory
		Expr getColumnType();

		/**
		 * Function retrieving the column's value.
		 * 
		 * <p>
		 * The function expects the row object as first argument, the column object as second
		 * argument and the component's model as optional third argument. The column object is the
		 * one retrieved by the {@link #getColumns()} function.
		 * </p>
		 * 
		 * <pre>
		 * <code>row -> column -> model -> ...</code>
		 * </pre>
		 * 
		 * <p>
		 * The result of the function is displayed in the table cell defined by the given row and
		 * column object.
		 * </p>
		 */
		@Name(ACCESSOR)
		@Mandatory
		Expr getAccessor();

		/**
		 * Optional operation storing an updated column value.
		 * 
		 * <p>
		 * If no operation is given, the column cannot be edited.
		 * </p>
		 * 
		 * <p>
		 * The operation expects the row object as first argument, the column object as second
		 * argument, the new value as third argument and the component's model as optional fourth
		 * argument. The column object is the one retrieved by the {@link #getColumns()} function.
		 * </p>
		 * 
		 * <pre>
		 * <code>row -> column -> value -> model -> ...</code>
		 * </pre>
		 * 
		 * <p>
		 * The operation is executed, when an edited row is saved and the user has edited the
		 * displayed value.
		 * </p>
		 */
		@Name(UPDATER)
		Expr getUpdater();

		/**
		 * The visibility of the created columns.
		 */
		@Name(COLUMN_VISIBILITY)
		DisplayMode getColumnVisibility();

	}

	private static final Property<Object> COLUMN_MODEL = TypedAnnotatable.property(Object.class, "columnModel");

	private LayoutComponent _component;

	private QueryExecutor _columns;

	private QueryExecutor _columnLabel;

	private QueryExecutor _columnType;

	private QueryExecutor _accessor;

	private QueryExecutor _updater;

	/**
	 * Creates a {@link DynamicColumnProviderByExpression} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public DynamicColumnProviderByExpression(InstantiationContext context, Config<?> config) {
		super(context, config);

		context.resolveReference(InstantiationContext.OUTER, LayoutComponent.class, c -> _component = c);
		_columns = QueryExecutor.compile(config.getColumns());
		_columnLabel = config.getColumnLabel() == null ? null : QueryExecutor.compile(config.getColumnLabel());
		_accessor = QueryExecutor.compile(config.getAccessor());
		_updater = config.getUpdater() == null ? null : QueryExecutor.compile(config.getUpdater());
		_columnType = QueryExecutor.compile(config.getColumnType());
	}

	@Override
	public void adaptConfigurationTo(TableConfiguration table) {
		Object model = _component.getModel();

		List<?> columns = SearchExpression.asList(_columns.execute(model));

		DisplayMode displayMode = getConfig().getColumnVisibility();

		List<String> dynamicColumnNames = new ArrayList<>();
		String idPrefix = getConfig().getIdPrefix();
		for (Object columnModel : columns) {
			if (columnModel == null) {
				continue;
			}
			String columnName = idPrefix + "-" + id(columnModel);
			ColumnConfiguration column = table.declareColumn(columnName);

			dynamicColumnNames.add(columnName);

			Object label = label(columnModel, model);
			ResKey labelKey;
			if (label instanceof ResKey) {
				labelKey = (ResKey) label;
			} else {
				labelKey = ResKey.text(MetaLabelProvider.INSTANCE.getLabel(label));
			}

			TLTypeContext baseType = new ConcreteTypeContext((TLType) _columnType.execute(columnModel, model));
			if (!getConfig().getAnnotations().isEmpty()) {
				baseType = new AnnotatedTypeContext(baseType, getConfig());
			}
			TLTypeContext type = baseType;

			ColumnInfo columnInfo = new ColumnInfoFactory().createColumnInfo(type, labelKey);
			columnInfo.setVisibility(displayMode);
			columnInfo.adapt(column);

			/* Default CellExistenceTester is the WrapperValueExistenceTester which assumes an
			 * existing attribute, the column info is built for. */
			column.setCellExistenceTester(AllCellsExist.INSTANCE);

			column.set(COLUMN_MODEL, columnModel);

			ColumnAccessor accessor = new ColumnAccessor(_component, columnModel, _accessor, _updater);
			column.setAccessor(accessor);

			if (_updater != null) {
				FieldProviderAnnotation annotation = type.getAnnotation(FieldProviderAnnotation.class);
				FieldProvider fieldProvider = TypedConfigUtil.createInstance(annotation.getImpl());
				AbstractFieldProvider columnFieldProvider = new AbstractFieldProvider() {
					@Override
					public FormMember createField(Object row, Accessor uselessAccessor, String columnName) {
						if (!ComponentUtil.isValid(columnModel)) {
							// In the case that the table configuration has not been updated after
							// an object defining a dynamic column has been deleted do not create
							// fields to prevent errors during save.
							return null;
						}

						EditContext editContext = new CustomEditContext(type)
							.setLabel(labelKey)
							.setValue(accessor.getValue(row, columnName))
							.setInTableContext(true);
						FormMember field = fieldProvider.getFormField(editContext, columnName);
						field.setStableIdSpecialCaseMarker(columnModel);
						return field;
					}
				};
				column.setFieldProvider(columnFieldProvider);
			}
		}

		if (displayMode.isDisplayed()) {
			dynamicColumnNames.removeAll(table.getDefaultColumns());
			table.setDefaultColumns(CollectionUtil.concat(table.getDefaultColumns(), dynamicColumnNames));
		}
	}

	private String id(Object columnModel) {
		if (columnModel instanceof TLObject) {
			ObjectKey id = ((TLObject) columnModel).tId();
			long rev = id.getHistoryContext();
			return id.getBranchContext() + "-" + id.getObjectName() + (rev == Revision.CURRENT_REV ? "" : "-" + rev);
		} else {
			return columnModel.toString();
		}
	}

	private Object label(Object columnModel, Object model) {
		return _columnLabel == null ? columnModel : _columnLabel.execute(columnModel, model);
	}

	/**
	 * Retrieves the column model object of the given column.
	 * 
	 * <p>
	 * The column model object was returned by the {@link Config#getColumns()} function and defines
	 * the dynamically created column.
	 * </p>
	 */
	public static Object getColumnModel(ColumnConfiguration column) {
		return column.get(COLUMN_MODEL);
	}

	private static final class ColumnAccessor implements Accessor<Object> {
		private final LayoutComponent _component;

		private final Object _column;
	
		private final QueryExecutor _accessor;
	
		private final QueryExecutor _updater;

		/**
		 * Creates a {@link ColumnAccessor}.
		 */
		public ColumnAccessor(LayoutComponent component, Object column, QueryExecutor accessor, QueryExecutor updater) {
			_component = component;
			_column = column;
			_accessor = accessor;
			_updater = updater;
		}
	
		@Override
		public Object getValue(Object object, String property) {
			return _accessor.execute(object, _column, _component.getModel());
		}
	
		@Override
		public void setValue(Object object, String property, Object value) {
			if (_updater == null) {
				throw new UnsupportedOperationException(
					"Column '" + property + "' can not be updated, no setter defined.");
			}
			_updater.execute(object, _column, value, _component.getModel());
		}
	}

}
