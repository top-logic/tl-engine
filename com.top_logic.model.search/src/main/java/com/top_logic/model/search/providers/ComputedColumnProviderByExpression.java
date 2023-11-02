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
import com.top_logic.basic.util.ResKey;
import com.top_logic.element.meta.form.AttributeFormFactory;
import com.top_logic.element.meta.form.CustomEditContext;
import com.top_logic.element.meta.form.EditContext;
import com.top_logic.element.meta.form.FieldProvider;
import com.top_logic.element.meta.form.FieldProviderAnnotation;
import com.top_logic.layout.Accessor;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.model.utility.SimplePartAnnotationOptions;
import com.top_logic.layout.form.values.ItemOptionMapping;
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
import com.top_logic.model.TLType;
import com.top_logic.model.annotate.AnnotatedConfig;
import com.top_logic.model.annotate.TLAnnotation;
import com.top_logic.model.annotate.TLAttributeAnnotation;
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
		Config.COLUMN_TYPE,
		Config.COLUMN_VISIBILITY,
		Config.ACCESSOR,
		Config.UPDATER,
		Config.ANNOTATIONS,
	})
	public interface Config<I extends ComputedColumnProviderByExpression>
			extends PolymorphicConfiguration<I>, ColumnProviderConfig, AnnotatedConfig<TLAttributeAnnotation> {

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
		 * The function expects the row object as single argument.
		 * </p>
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
		 * The operation expects the row object as first argument and the new value that should be
		 * stored as second argument.
		 * </p>
		 * 
		 * <p>
		 * The operation is executed, when an edited row is saved and the user has edited the
		 * displayed value.
		 * </p>
		 */
		@Name(UPDATER)
		Expr getUpdater();

		/**
		 * The visibility of the created column.
		 */
		@Name(COLUMN_VISIBILITY)
		DisplayMode getColumnVisibility();

		@Override
		@Options(fun = SimplePartAnnotationOptions.ForTypeRef.class, args = @Ref(COLUMN_TYPE), mapping = ItemOptionMapping.class)
		Collection<TLAttributeAnnotation> getAnnotations();
	}

	private final QueryExecutor _accessor;

	private final QueryExecutor _updater;

	private final TLType _columnType;

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
	}

	@Override
	public void adaptConfigurationTo(TableConfiguration table) {
		Config<?> config = getConfig();
		DisplayMode displayMode = config.getColumnVisibility();

		String columnName = config.getColumnId();
		ColumnConfiguration column = table.declareColumn(columnName);

		ResKey labelKey = config.getColumnLabel();
		TLTypeContext type = createTypeContext(config);

		ColumnInfo columnInfo = new ColumnInfoFactory().createColumnInfo(type, labelKey);

		columnInfo.setVisibility(displayMode);
		columnInfo.adapt(column);

		/* Default CellExistenceTester is the WrapperValueExistenceTester which assumes an existing
		 * attribute, the column info is built for. */
		column.setCellExistenceTester(AllCellsExist.INSTANCE);

		ColumnAccessor accessor = new ColumnAccessor(_accessor, _updater);
		column.setAccessor(accessor);

		if (_updater != null) {
			FieldProviderAnnotation annotation = type.getAnnotation(FieldProviderAnnotation.class);
			FieldProvider fieldProvider = TypedConfigUtil.createInstance(annotation.getImpl());
			AbstractFieldProvider columnFieldProvider = new AbstractFieldProvider() {
				@Override
				public FormMember createField(Object row, Accessor uselessAccessor, String columnName) {
					EditContext editContext = new CustomEditContext(type)
						.setLabel(labelKey)
						.setValue(accessor.getValue(row, columnName))
						.setInTableContext(true);
					FormMember field = fieldProvider.getFormField(editContext, columnName);
					if (field instanceof FormField) {
						AttributeFormFactory.initFieldValue(editContext, (FormField) field);
					}
					return field;
				}
			};
			column.setFieldProvider(columnFieldProvider);
		}
		if (displayMode.isDisplayed()) {
			table.setDefaultColumns(append(table.getDefaultColumns(), columnName));
		}
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

	@Override
	public void adaptDefaultColumn(ColumnConfiguration defaultColumn) {
		// No additions.
	}

	private static final class ColumnAccessor implements Accessor<Object> {
		private final QueryExecutor _accessor;

		private final QueryExecutor _updater;

		/**
		 * Creates a {@link ColumnAccessor}.
		 */
		public ColumnAccessor(QueryExecutor accessor, QueryExecutor updater) {
			_accessor = accessor;
			_updater = updater;
		}

		@Override
		public Object getValue(Object object, String property) {
			return _accessor.execute(object);
		}

		@Override
		public void setValue(Object object, String property, Object value) {
			if (_updater == null) {
				throw new UnsupportedOperationException(
					"Column '" + property + "' can not be updated, no setter defined.");
			}
			_updater.execute(object, value);
		}
	}
}
