/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.form.fieldprovider;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.element.meta.form.EditContext;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.layout.table.model.TableConfig;
import com.top_logic.layout.table.model.TableConfigurationFactory;
import com.top_logic.layout.table.model.TableConfigurationProvider;

/**
 * {@link ComplexFieldProvider} which gets an optional {@link TableConfig} for the created select
 * field.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TableSupportingComplexFieldProvider extends ComplexFieldProvider {

	/**
	 * Configuration for the {@link TableSupportingComplexFieldProvider}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config extends ComplexFieldProvider.Config {

		/**
		 * Definition of the table for the created select field.
		 * 
		 * @implNote See
		 *           {@link SelectField#setTableConfigurationProvider(TableConfigurationProvider)}
		 */
		TableConfig getTable();

		/**
		 * Definition of the table for the created select field.
		 * 
		 * @implNote See
		 *           {@link SelectField#setDialogTableConfigurationProvider(TableConfigurationProvider)}
		 */
		TableConfig getSelectTable();
	}

	/**
	 * Creates a new {@link TableSupportingComplexFieldProvider}.
	 */
	public TableSupportingComplexFieldProvider(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public Config getConfig() {
		return (Config) super.getConfig();
	}

	@Override
	public FormMember createFormField(EditContext editContext, String fieldName) {
		SelectField selectField = (SelectField) super.createFormField(editContext, fieldName);
		
		TableConfig table = getConfig().getTable();
		if (table != null) {
			selectField.setTableConfigurationProvider(toTableProvider(table));
		}
		TableConfig selectTable = getConfig().getSelectTable();
		if (selectTable != null) {
			selectField.setDialogTableConfigurationProvider(toTableProvider(selectTable));
		}
		return selectField;
	}

	private TableConfigurationProvider toTableProvider(TableConfig table) {
		return TableConfigurationFactory.toProvider(SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY, table);
	}

}

