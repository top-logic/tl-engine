/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.instances;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.knowledge.wrap.WrapperAccessor;
import com.top_logic.layout.ReadOnlyAccessor;
import com.top_logic.layout.table.control.TableControl;
import com.top_logic.layout.table.model.ColumnConfiguration;
import com.top_logic.layout.table.model.ColumnConfiguration.DisplayMode;
import com.top_logic.layout.table.model.TableConfiguration;
import com.top_logic.layout.table.model.TableConfigurationProvider;
import com.top_logic.layout.table.provider.GenericTableConfigurationProvider;
import com.top_logic.layout.table.provider.generic.TableConfigModelInfo;
import com.top_logic.layout.table.provider.generic.TableConfigModelService;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.annotate.DisplayAnnotations;
import com.top_logic.model.util.TLModelUtil;

/**
 * {@link TableConfigurationProvider} that configures a table with it's surrounding component's
 * model (expecting it to be a {@link TLClass}).
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DirectInstancesTable<C extends DirectInstancesTable.Config<?>> extends AbstractConfiguredInstance<C>
		implements TableConfigurationProvider {

	/** Name of the column displaying the default view for the row object. */
	private static final String DEFAULT_VIEW_COLUMN_NAME = "__default_view";

	/**
	 * Configuration options for {@link DirectInstancesTable}.
	 */
	public interface Config<I extends DirectInstancesTable<?>> extends PolymorphicConfiguration<I> {
		// Pure marker.
	}

	private LayoutComponent _component;

	/**
	 * Creates a {@link DirectInstancesTable} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public DirectInstancesTable(InstantiationContext context, C config) {
		super(context, config);

		context.resolveReference(InstantiationContext.OUTER, LayoutComponent.class, c -> _component = c);
	}

	@Override
	public void adaptConfigurationTo(TableConfiguration table) {
		Object model = _component.getModel();
		if (!(model instanceof TLClass)) {
			return;
		}
		TLClass type = (TLClass) model;
		GenericTableConfigurationProvider.getTableConfigurationProvider(type).adaptConfigurationTo(table);

		TableConfigModelInfo modelInfo = modelInfo(type);

		// Note: The order of the default columns is the order in which the columns are displayed,
		// see GridComponent#getAvailableColumns(TableConfiguration).
		Set<String> defaultColumns = new LinkedHashSet<>(modelInfo.getMainColumns());
		defaultColumns.addAll(adaptColumnVisibility(table, type, modelInfo.getColumnInfos().keySet()));
		table.setDefaultColumns(new ArrayList<>(defaultColumns));

		addDefaultViewColumn(table);
	}

	/**
	 * Assigns the {@link DisplayMode} to all columns already declared for the given type and
	 * collects those columns that must be part of the default columns.
	 * 
	 * @param table
	 *        The table being configured.
	 * @param type
	 *        The type whose instances are displayed.
	 * @param attributeColumns
	 *        Names of the columns representing an attribute of the given type, of one of its
	 *        generalizations, or of one of its specializations.
	 * @return The technical columns declared so far, which must stay visible. Never
	 *         <code>null</code>, may be modified by the caller.
	 */
	private List<String> adaptColumnVisibility(TableConfiguration table, TLClass type,
			Set<String> attributeColumns) {
		List<String> technicalColumns = new ArrayList<>();
		for (ColumnConfiguration column : table.getElementaryColumns()) {
			String columnName = column.getName();
			if (TableControl.SELECT_COLUMN_NAME.equals(columnName)) {
				continue;
			}
			if (!attributeColumns.contains(columnName)) {
				// A technical column declared by another provider, e.g. a button column. Such a
				// column has no representation in the model and must therefore be kept visible
				// explicitly.
				technicalColumns.add(columnName);
				continue;
			}

			// Note: The column may represent an attribute of a specialization of the displayed
			// type, which is not found here. Such an attribute cannot be filled during a creation
			// anyway, since instances of the displayed type are created.
			TLStructuredTypePart attribute = type.getPart(columnName);
			if (attribute != null && isMandatoryInCreate(attribute)) {
				// The column must be displayed, since the grid creates input fields for the
				// displayed columns only. Without a field, an instance could be created without
				// its mandatory values being set.
				column.setVisibility(DisplayMode.mandatory);
			} else if (column.getVisibility() == DisplayMode.excluded) {
				// The instance browser is an administrative view: Every attribute must stay
				// selectable, see InstanceBrowserGrid#isHidden(TLStructuredTypePart). An excluded
				// column could not be displayed at all, not even on demand.
				column.setVisibility(DisplayMode.hidden);
			}
		}
		return technicalColumns;
	}

	/**
	 * Whether the grid displays a mandatory input field for the given attribute while creating an
	 * instance.
	 */
	private static boolean isMandatoryInCreate(TLStructuredTypePart attribute) {
		return DisplayAnnotations.isMandatoryInCreate(attribute) && !TLModelUtil.isDerived(attribute);
	}

	/**
	 * The model information describing the columns available for the given type.
	 * 
	 * @implNote Provides the same {@link com.top_logic.model.config.annotation.MainProperties}
	 *           lookup as for all other generic tables, including its fall-back to the main
	 *           properties of the specializations and finally to all visible attributes.
	 */
	private static TableConfigModelInfo modelInfo(TLClass type) {
		return TableConfigModelService.getInstance().getModelInfoProvider()
			.getModelInfo(Collections.singleton(type));
	}

	private void addDefaultViewColumn(TableConfiguration table) {
		ColumnConfiguration viewColumn = table.declareColumn(DEFAULT_VIEW_COLUMN_NAME);
		viewColumn.setAccessor(new ReadOnlyAccessor<>() {

			@Override
			public Object getValue(Object object, String property) {
				return object;
			}
		});
		viewColumn.setVisibility(DisplayMode.hidden);
		viewColumn.setColumnLabelKey(I18NConstants.DEFAULT_VIEW_COLUMN_LABEL);
	}

	@Override
	public void adaptDefaultColumn(ColumnConfiguration defaultColumn) {
		defaultColumn.setAccessor(WrapperAccessor.INSTANCE);
	}

}
