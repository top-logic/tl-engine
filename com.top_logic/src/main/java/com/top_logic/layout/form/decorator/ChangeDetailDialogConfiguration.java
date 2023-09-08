/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.decorator;

import java.io.IOException;

import com.top_logic.basic.col.Mapping;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.IdentityAccessor;
import com.top_logic.layout.ReadOnlyAccessor;
import com.top_logic.layout.Renderer;
import com.top_logic.layout.form.decorator.DetailDecorator.Context;
import com.top_logic.layout.provider.MetaResourceProvider;
import com.top_logic.layout.table.CellRenderer;
import com.top_logic.layout.table.filter.MandatoryLabelFilterProvider;
import com.top_logic.layout.table.model.ColumnConfiguration;
import com.top_logic.layout.table.model.ColumnConfiguration.DisplayMode;
import com.top_logic.layout.table.model.NoDefaultColumnAdaption;
import com.top_logic.layout.table.model.TableConfiguration;
import com.top_logic.layout.tree.renderer.TreeCellRenderer;

/**
 * {@link TableConfiguration} for detail dialog of comparison change.
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public class ChangeDetailDialogConfiguration extends NoDefaultColumnAdaption {

	private static final String NAME_COLUMN_WIDTH = "230px";

	final DetailDecorator _decorator;

	final Context _context;

	/**
	 * Create a new {@link ChangeDetailDialogConfiguration}.
	 */
	public ChangeDetailDialogConfiguration(DetailDecorator decorator, Context context) {
		_decorator = decorator;
		_context = context;
	}

	@Override
	public void adaptConfigurationTo(TableConfiguration table) {
		table.setFixedColumnCount(1);
		table.setShowTitle(false);
		createCompareColumn(table);
		createNameColumn(table);
		createBaseValueColumn(table);
		createChangeValueColumn(table);
	}

	private void createCompareColumn(TableConfiguration tableConfiguration) {
		ColumnConfiguration decorationColumn =
			tableConfiguration.declareColumn(DecorateService.DECORATION_COLUMN);

		decorationColumn.setDefaultColumnWidth(DecorateService.DECORATION_COLUMN_WIDTH);
		decorationColumn.setAccessor(IdentityAccessor.INSTANCE);
		decorationColumn.setRenderer(new Renderer<CompareDetailRow>() {
			@Override
			public void write(DisplayContext context, TagWriter out, CompareDetailRow value) throws IOException {
				_decorator.start(context, out, value.getCompareInfo(), _context);
				_decorator.end(context, out, value.getCompareInfo(), _context);
			}
		});
		decorationColumn.setVisibility(DisplayMode.mandatory);
		decorationColumn.setShowHeader(false);
		decorationColumn.setSortable(true);
		decorationColumn.setFilterProvider(MandatoryLabelFilterProvider.INSTANCE);
		decorationColumn.setColumnLabelKey(I18NConstants.CHANGE_INFO_COLUMN_LABEL);
		decorationColumn.setSortKeyProvider(new Mapping<CompareDetailRow, String>() {

			@Override
			public String map(CompareDetailRow input) {
				return MetaResourceProvider.INSTANCE.getLabel(input.getCompareInfo());
			}
		});
		decorationColumn.setCellStyle("text-align:center;");
	}

	private void createNameColumn(TableConfiguration tableConfiguration) {
		ColumnConfiguration nameColumn = tableConfiguration.declareColumn("name");
		nameColumn
			.setColumnLabelKey(I18NConstants.COMPARE_DETAIL_PROPERTIES_COLUMN);
		nameColumn.setDefaultColumnWidth(NAME_COLUMN_WIDTH);
		nameColumn.setAccessor(new ReadOnlyAccessor<CompareDetailRow>() {

			@Override
			public Object getValue(CompareDetailRow compareDetailRow, String property) {
				return compareDetailRow.getAttributeLabel();
			}
		});
		com.top_logic.layout.tree.renderer.TreeCellRenderer.Config treeCellRendererConfig =
			TypedConfiguration.newConfigItem(TreeCellRenderer.Config.class);
		treeCellRendererConfig.setImplementationClass(TreeCellRenderer.class);

		CellRenderer treeCellRenderer =
			SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY
				.getInstance(treeCellRendererConfig);
		nameColumn.setCellRenderer(treeCellRenderer);
	}

	private void createBaseValueColumn(TableConfiguration tableConfiguration) {
		createValueColumn(tableConfiguration, "baseValue", I18NConstants.COMPARE_BASE_TABLE_PART,
			new ReadOnlyAccessor<CompareDetailRow>() {

				@Override
				public Object getValue(CompareDetailRow compareDetailRow, String property) {
					return compareDetailRow.getCompareInfo().getBaseValue();
				}
			});
	}

	private void createChangeValueColumn(TableConfiguration tableConfiguration) {
		createValueColumn(tableConfiguration, "changeValue", I18NConstants.COMPARE_CHANGE_TABLE_PART,
			new ReadOnlyAccessor<CompareDetailRow>() {

				@Override
				public Object getValue(CompareDetailRow compareDetailRow, String property) {
					return compareDetailRow.getCompareInfo().getChangeValue();
				}
			});
	}

	private void createValueColumn(TableConfiguration tableConfiguration, String columnName,
			ResKey columnLabel,
			ReadOnlyAccessor<CompareDetailRow> accessor) {
		ColumnConfiguration columnConfiguration = tableConfiguration.declareColumn(columnName);
		columnConfiguration.setColumnLabelKey(columnLabel);
		columnConfiguration.setAccessor(accessor);
	}
}