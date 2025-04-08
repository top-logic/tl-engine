/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.providers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Ref;
import com.top_logic.basic.config.misc.TypedConfigUtil;
import com.top_logic.basic.config.order.DisplayOrder;
import com.top_logic.basic.func.Function1;
import com.top_logic.basic.util.ResKey;
import com.top_logic.element.meta.form.CustomEditContext;
import com.top_logic.element.meta.form.EditContext;
import com.top_logic.element.meta.form.FieldProvider;
import com.top_logic.element.meta.form.FieldProviderAnnotation;
import com.top_logic.layout.Accessor;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.model.FieldMode;
import com.top_logic.layout.form.model.utility.SimplePartAnnotationOptions;
import com.top_logic.layout.form.values.ItemOptionMapping;
import com.top_logic.layout.form.values.edit.annotation.DynamicMode;
import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.layout.table.filter.AllCellsExist;
import com.top_logic.layout.table.model.AbstractFieldProvider;
import com.top_logic.layout.table.model.ColumnConfiguration;
import com.top_logic.layout.table.model.ColumnConfiguration.DisplayMode;
import com.top_logic.layout.table.model.TableConfiguration;
import com.top_logic.layout.table.model.TableConfigurationProvider;
import com.top_logic.layout.table.provider.ColumnInfo;
import com.top_logic.layout.table.provider.ColumnProviderConfig;
import com.top_logic.layout.table.provider.generic.ColumnInfoFactory;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.TLType;
import com.top_logic.model.annotate.AnnotatedConfig;
import com.top_logic.model.annotate.TLAnnotation;
import com.top_logic.model.annotate.TLAttributeAnnotation;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.ToString;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.query.QueryExecutor;
import com.top_logic.model.util.AllTypes;
import com.top_logic.model.util.AnnotatedTypeContext;
import com.top_logic.model.util.ConcreteTypeContext;
import com.top_logic.model.util.TLModelPartRef;
import com.top_logic.model.util.TLTypeContext;

/**
 * {@link TableConfigurationProvider} that adds a single computed column to a table or grid.
 * 
 * @see DynamicColumnProviderByExpression
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@InApp
public class ComputedColumnProviderByExpression
		extends AbstractConfiguredInstance<ComputedColumnProviderByExpression.Config<?>>
		implements TableConfigurationProvider {

	/**
	 * Configuration options for {@link ComputedColumnProviderByExpression}.
	 */
	@DisplayOrder({
		Config.COLUMN_LABEL,
		Config.DYNAMIC_COLUMN_LABEL,
		Config.COLUMN_TYPE,
		Config.COLUMN_VISIBILITY,
		Config.ACCESSOR,
		Config.UPDATER,
		Config.CAN_UPDATE,
		Config.ANNOTATIONS,
	})
	public interface Config<I extends ComputedColumnProviderByExpression>
			extends PolymorphicConfiguration<I>, ColumnProviderConfig, AnnotatedConfig<TLAttributeAnnotation> {

		/**
		 * @see #getColumnType()
		 */
		String COLUMN_TYPE = "columnType";

		/**
		 * @see #getDynamicColumnLabel()
		 */
		String DYNAMIC_COLUMN_LABEL = "dynamicColumnLabel";

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
		 * @see #getCanUpdate()
		 */
		String CAN_UPDATE = "canUpdate";

		/**
		 * Type of the values displayed in this column.
		 */
		@Name(COLUMN_TYPE)
		@Mandatory
		@Options(fun = AllTypes.class, mapping = TLModelPartRef.PartMapping.class)
		TLModelPartRef getColumnType();

		/**
		 * Function retrieving the column's value.
		 * 
		 * <p>
		 * The function expects the row object as first argument and the component's model as
		 * optional second argument.
		 * </p>
		 * 
		 * <pre>
		 * <code>row -> model -> ...</code>
		 * </pre>
		 * 
		 * <p>
		 * The result of the function is displayed in the table cell defined by this column provider
		 * and the given row object.
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
		 * The operation expects the row object as first argument, the new value that should be
		 * stored as second argument and the component's model as optional third arguments.
		 * </p>
		 * 
		 * <pre>
		 * <code>row -> value -> model -> ...</code>
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
		 * Optional function to control field creation for editing in specific rows.
		 * 
		 * <p>
		 * The function takes the row object and optionally the component's model as arguments:
		 * <code>row -> model -> ...</code>
		 * </p>
		 * 
		 * <p>
		 * The function must return a boolean value indicating whether a field should be created for
		 * the given row.
		 * </p>
		 * 
		 * <p>
		 * Only relevant if {@link #getUpdater()} is specified. In that case:
		 * <ul>
		 * <li>If no function is provided, fields are created for all rows</li>
		 * <li>If a function is provided, it determines per row whether a field should be
		 * created</li>
		 * </ul>
		 * If no updater is specified, this function is ignored and no fields are created.
		 * </p>
		 */
		@Name(CAN_UPDATE)
		@DynamicMode(fun = ShowIfUpdater.class, args = @Ref(UPDATER))
		Expr getCanUpdate();

		/**
		 * The visibility of the created column.
		 */
		@Name(COLUMN_VISIBILITY)
		DisplayMode getColumnVisibility();

		/**
		 * Optional function computing the column label.
		 * 
		 * <p>
		 * The function expects the component's model as single argument.
		 * </p>
		 * 
		 * <pre>
		 * <code>model -> ...</code>
		 * </pre>
		 * 
		 * <p>
		 * The column name is expected as result. The result can either be an internationalization
		 * resource, a string, or any other object whose textual representation can be shown in the
		 * column header.
		 * </p>
		 * 
		 * <p>
		 * If no label function is specified, the static label from
		 * {@link ColumnProviderConfig#getColumnLabel()} is used.
		 * </p>
		 */
		@Name(DYNAMIC_COLUMN_LABEL)
		Expr getDynamicColumnLabel();

		@Override
		@Options(fun = SimplePartAnnotationOptions.ForTypeRef.class, args = @Ref(COLUMN_TYPE), mapping = ItemOptionMapping.class)
		Collection<TLAttributeAnnotation> getAnnotations();

		/**
		 * Function that shows the field only if an updater is specified.
		 */
		class ShowIfUpdater extends Function1<FieldMode, Expr> {
			@Override
			public FieldMode apply(Expr updater) {
				return updater != null ? FieldMode.ACTIVE : FieldMode.INVISIBLE;
			}
		}
	}

	private final QueryExecutor _accessor;

	private final QueryExecutor _updater;

	private final TLType _columnType;

	private LayoutComponent _component;

	private final QueryExecutor _canUpdate;

	private final QueryExecutor _dynamicColumnLabel;

	/**
	 * Creates a {@link ComputedColumnProviderByExpression} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public ComputedColumnProviderByExpression(InstantiationContext context, Config<?> config)
			throws ConfigurationException {
		super(context, config);

		_accessor = QueryExecutor.compile(config.getAccessor());
		_updater = QueryExecutor.compileOptional(config.getUpdater());
		_columnType = config.getColumnType().resolveType();
		_canUpdate = QueryExecutor.compileOptional(config.getCanUpdate());
		_dynamicColumnLabel = QueryExecutor.compileOptional(config.getDynamicColumnLabel());
		context.resolveReference(InstantiationContext.OUTER, LayoutComponent.class, c -> _component = c);
	}

	@Override
	public void adaptConfigurationTo(TableConfiguration table) {
		Config<?> config = getConfig();
		DisplayMode displayMode = config.getColumnVisibility();

		String columnName = config.getColumnId();
		ColumnConfiguration column = table.declareColumn(columnName);

		ResKey labelKey = getLabelKey(config);
		TLTypeContext type = createTypeContext(config);

		ColumnInfo columnInfo = new ColumnInfoFactory().createColumnInfo(type, labelKey);

		columnInfo.setVisibility(displayMode);
		columnInfo.adapt(column);

		/* Default CellExistenceTester is the WrapperValueExistenceTester which assumes an existing
		 * attribute, the column info is built for. */
		column.setCellExistenceTester(AllCellsExist.INSTANCE);

		ColumnAccessor accessor = new ColumnAccessor(_component, _accessor, _updater);
		column.setAccessor(accessor);

		if (_updater != null) {
			FieldProviderAnnotation annotation = type.getAnnotation(FieldProviderAnnotation.class);
			FieldProvider fieldProvider = TypedConfigUtil.createInstance(annotation.getImpl());
			AbstractFieldProvider columnFieldProvider = new AbstractFieldProvider() {
				@Override
				public FormMember createField(Object row, Accessor uselessAccessor, String columnName) {
					if (!canUpdate(row)) {
						return null;
					}
					EditContext editContext = new CustomEditContext(type)
						.setLabel(labelKey)
						.setValue(accessor.getValue(row, columnName))
						.setInTableContext(true);
					return fieldProvider.getFormField(editContext, columnName);
				}
			};
			column.setFieldProvider(columnFieldProvider);
		}
		if (displayMode.isDisplayed() && !table.getDefaultColumns().contains(columnName)) {
			table.setDefaultColumns(append(table.getDefaultColumns(), columnName));
		}
	}

	private ResKey getLabelKey(Config<?> config) {
		if (_dynamicColumnLabel != null) {
			Object label = _dynamicColumnLabel.execute(_component.getModel());
			if (label instanceof ResKey) {
				return (ResKey) label;
			}
			return ResKey.text(ToString.toString(label));
		}
		return config.getColumnLabel();
	}

	private boolean canUpdate(Object row) {
		if (_canUpdate == null) {
			return true;
		}
		Object result = _canUpdate.execute(row, _component.getModel());
		return SearchExpression.asBoolean(result);
	}

	private TLTypeContext createTypeContext(Config<?> config) {
		TLTypeContext type = new ConcreteTypeContext(_columnType);

		Collection<? extends TLAnnotation> annotations = config.getAnnotations();
		if (annotations.isEmpty()) {
			return type;
		}

		return new AnnotatedTypeContext(type, config);
	}

	private static <T> List<T> append(List<T> list, T value) {
		if (list.isEmpty()) {
			return Collections.singletonList(value);
		} else {
			ArrayList<T> result = new ArrayList<>(list);
			result.add(value);
			return result;
		}
	}

	private static final class ColumnAccessor implements Accessor<Object> {
		private final LayoutComponent _component;

		private final QueryExecutor _accessor;

		private final QueryExecutor _updater;

		/**
		 * Creates a {@link ColumnAccessor}.
		 */
		public ColumnAccessor(LayoutComponent component, QueryExecutor accessor, QueryExecutor updater) {
			_component = component;
			_accessor = accessor;
			_updater = updater;
		}

		@Override
		public Object getValue(Object object, String property) {
			return _accessor.execute(object, _component.getModel());
		}

		@Override
		public void setValue(Object object, String property, Object value) {
			if (_updater == null) {
				throw new UnsupportedOperationException(
					"Column '" + property + "' can not be updated, no setter defined.");
			}
			_updater.execute(object, value, _component.getModel());
		}
	}
}
