/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.demo.layout.demo.table;

import java.beans.IntrospectionException;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.demo.layout.demo.DummyEditComponent;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.form.FormHandler;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.layout.form.model.TableField;
import com.top_logic.layout.table.TableModel;
import com.top_logic.layout.table.model.ObjectTableModel;
import com.top_logic.layout.table.model.TableConfiguration;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.AbstractCommandHandler;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * The class {@link DemoFormulaTables} is the underlying component to show tables in formulas
 * 
 * @author    <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class DemoFormulaTables extends DummyEditComponent {

	public static final String FIELD_REPLACE_NO_SORT = "replaceNoSort";
	public static final String FIELD_SELECT_SORT_NO_REPLACE = "selectSortNoReplace";

	public static final String TABLE_HIDDEN_SELECT = "tableHiddenSelect";

	public static final String TABLE_EXCLUDED_SELECT = "tableExcludedSelect";

	public static final String FIELD_TABLE = "tableField";

	public DemoFormulaTables(InstantiationContext context, Config someAttrs) throws ConfigurationException {
		super(context, someAttrs);
	}

	@Override
	public FormContext createFormContext() {
		final FormContext formContext = new FormContext(this);
		
		List<?> allDemoPersons = (List<?>) DemoTableModellBuilder.INSTANCE.getModel(null, this);
		
		SelectField sortNoReplace = FormFactory.newSelectField(FIELD_SELECT_SORT_NO_REPLACE, allDemoPersons, true, allDemoPersons, false);
		sortNoReplace.setTableConfigurationProvider(lookupTableConfigurationBuilder(FIELD_SELECT_SORT_NO_REPLACE));
		formContext.addMember(sortNoReplace);

		SelectField replaceNoSort = FormFactory.newSelectField(FIELD_REPLACE_NO_SORT, allDemoPersons, true, allDemoPersons, false);
		replaceNoSort.setTableConfigurationProvider(lookupTableConfigurationBuilder(FIELD_REPLACE_NO_SORT));
		formContext.addMember(replaceNoSort);

		TableField formTable = createTableField(FIELD_TABLE, allDemoPersons);
		formContext.addMember(formTable);

		TableField hiddenSelectTable = createTableField(TABLE_HIDDEN_SELECT, allDemoPersons);
		formContext.addMember(hiddenSelectTable);

		TableField excludedSelectTable = createTableField(TABLE_EXCLUDED_SELECT, allDemoPersons);
		formContext.addMember(excludedSelectTable);

		return formContext;
	}

	private TableField createTableField(String tableName, List<?> allDemoPersons) {
		String[] columnNames = { "title", "surname", "givenName" };
		TableConfiguration config = TableConfiguration.table();
		config.getDefaultColumn().setAccessor(demoTableAccessor());
		adaptTableConfiguration(tableName, config);
		TableModel tableModel = new ObjectTableModel(columnNames, config, allDemoPersons);
		TableField formTable = FormFactory.newTableField(tableName, tableModel, true);
		return formTable;
	}

	private DemoTableAccessor demoTableAccessor() {
		try {
			return new DemoTableAccessor();
		} catch (IntrospectionException ex) {
			throw new RuntimeException(ex);
		}
	}

	/**
	 * {@link AbstractCommandHandler} that focused a configured table.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public static final class FocusTableCommand extends AbstractCommandHandler {

		/**
		 * Configuration for {@link FocusTableCommand}.
		 * 
		 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
		 */
		public interface Config extends AbstractCommandHandler.Config {

			/**
			 * Name of the table to focus.
			 */
			@Mandatory
			String getTable();
		}

		/**
		 * Creates a new {@link FocusTableCommand}.
		 */
		public FocusTableCommand(InstantiationContext context, Config config) {
			super(context, config);
		}

		@Override
		public HandlerResult handleCommand(DisplayContext aContext, LayoutComponent aComponent, Object model,
				Map<String, Object> someArguments) {
			((FormHandler) aComponent).getFormContext().getMember(((Config) getConfig()).getTable()).focus();
			return HandlerResult.DEFAULT_RESULT;
		}

	}

}

