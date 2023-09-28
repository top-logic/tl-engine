/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.tiles;

import java.util.List;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.CommaSeparatedStrings;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Ref;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.editor.config.OptionalTypeTemplateParameters;
import com.top_logic.layout.editor.config.TypeTemplateParameters;
import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.layout.table.DefaultTableData;
import com.top_logic.layout.table.TableData;
import com.top_logic.layout.table.control.TableControl;
import com.top_logic.layout.table.model.ColumnConfiguration;
import com.top_logic.layout.table.model.ColumnCustomization;
import com.top_logic.layout.table.model.Enabled;
import com.top_logic.layout.table.model.ObjectTableModel;
import com.top_logic.layout.table.model.TableConfiguration;
import com.top_logic.layout.table.model.TableConfigurationFactory;
import com.top_logic.layout.table.model.TableConfigurationProvider;
import com.top_logic.layout.table.provider.GenericTableConfigurationProvider;
import com.top_logic.model.form.definition.NamedPartNames;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.query.QueryExecutor;
import com.top_logic.model.util.AllTypeAttributes;

/**
 * {@link TilePreviewPartProvider.Content} rendering a configured table in the preview area.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@InApp
public class TableContentProvider extends AbstractPreviewContent<TableContentProvider.Config> {

	/**
	 * Maximal number of rows displayed in the preview.
	 * 
	 * <p>
	 * The preview is not large enough to display many rows. As scrolling in the preview is not
	 * possible, it is undesirable to render more elements than can be displayed.
	 * </p>
	 */
	private static final int MAX_ROWS_NUMBER = 10;

	/**
	 * Typed configuration interface definition for {@link TableContentProvider}.
	 * 
	 * @author <a href="mailto:dbu@top-logic.com">dbu</a>
	 */
	public interface Config extends AbstractPreviewContent.Config<TableContentProvider>, TableConfig {
		// configuration interface definition
	}

	/**
	 * Configuration of the table to display in the preview area.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface TableConfig extends TypeTemplateParameters {

		/** Configuration name for the value of {@link #getTitle()}. */
		String TITLE = "title";

		/** Configuration name for the value of {@link #getRows()}. */
		String ROWS = "rows";

		/** Configuration name for the value of {@link #getAttributes()}. */
		String ATTRIBUTES = "attributes";

		/**
		 * {@link Expr} that computes the rows to be displayed in the table based on the model of
		 * the preview.
		 */
		@Mandatory
		@Name(ROWS)
		Expr getRows();

		/**
		 * The attributes of {@link #getType()} which are displayed as columns in the table.
		 */
		@Options(fun = AllTypeAttributes.class, args = @Ref(TYPE), mapping = NamedPartNames.class)
		@Format(CommaSeparatedStrings.class)
		@Name(ATTRIBUTES)
		List<String> getAttributes();

		/**
		 * {@link Expr} that computes an optional title to be displayed in the table based on the
		 * model of the preview.
		 * 
		 * <p>
		 * It is expected that the result is convertable to {@link ResKey}.
		 * </p>
		 */
		@Name(TITLE)
		Expr getTitle();
	}

	private final QueryExecutor _rows;

	private final QueryExecutor _title;

	/**
	 * Create a {@link TableContentProvider}.
	 * 
	 * @param context
	 *        the {@link InstantiationContext} to create the new object in
	 * @param config
	 *        the configuration object to be used for instantiation
	 */
	public TableContentProvider(InstantiationContext context, Config config) {
		super(context, config);
		_rows = QueryExecutor.compile(config.getRows());
		_title = QueryExecutor.compileOptional(config.getTitle());
	}

	@Override
	public HTMLFragment createPreviewPart(Object model) {
		List<?> rows = rows(model);
		List<String> columnNames = getConfig().getAttributes();
		TableConfiguration config = TableConfigurationFactory.build(
			GenericTableConfigurationProvider
				.getTableConfigurationProvider(OptionalTypeTemplateParameters.resolve(getConfig())),
			GenericTableConfigurationProvider.showColumns(columnNames),
			disableTableCustomizations(model));
		ObjectTableModel tableModel = new ObjectTableModel(columnNames, config, rows);
		TableData data = DefaultTableData.createAnonymousTableData(tableModel);
		TableControl table = new TableControl(data, config.getTableRenderer());
		table.setSelectable(false);
		return defaultPreview(table, "tablePreview");
	}

	private TableConfigurationProvider disableTableCustomizations(Object model) {
		ResKey titleKey;
		if (_title != null) {
			titleKey = SearchExpression.asResKeyNotNull(_title.execute(model), _title.getSearch());
		} else {
			titleKey = null;
		}
		return new TableConfigurationProvider() {

			@Override
			public void adaptConfigurationTo(TableConfiguration table) {
				table.setMultiSort(Enabled.never);
				table.setColumnCustomization(ColumnCustomization.NONE);
				table.getDeclaredColumns().forEach(this::adaptDefaultColumn);
				table.setShowFooter(false);
				if (titleKey != null) {
					table.setTitleKey(titleKey);
				} else {
					table.setShowTitle(false);
				}
				table.setDefaultFilterProvider(null);
			}

			@Override
			public void adaptDefaultColumn(ColumnConfiguration defaultColumn) {
				defaultColumn.setFilterProvider(null);
				defaultColumn.setSortable(false);
			}
		};
	}

	private List<?> rows(Object model) {
		List<?> rows = toList(_rows.execute(model));
		if (rows.size() <= MAX_ROWS_NUMBER) {
			return rows;
		}
		return rows.subList(0, MAX_ROWS_NUMBER);
	}

	private static List<?> toList(Object searchResult) {
		if (searchResult instanceof Iterable<?>) {
			return CollectionUtil.toList((Iterable<?>) searchResult);
		}
		return CollectionUtil.singletonOrEmptyList(searchResult);
	}

}

