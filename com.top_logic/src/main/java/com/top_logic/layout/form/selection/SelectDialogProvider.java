/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.selection;

import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.misc.TypedConfigUtil;
import com.top_logic.layout.form.model.SelectField;

/**
 * Factory for {@link AbstractSelectDialog}s for {@link SelectField}s.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class SelectDialogProvider extends AbstractConfiguredInstance<SelectDialogConfig> {

	/**
	 * Create a {@link SelectDialogProvider}.
	 * 
	 * @param context
	 *        the {@link InstantiationContext} to create the new object in
	 * @param config
	 *        the configuration object to be used for instantiation
	 */
	public SelectDialogProvider(InstantiationContext context, SelectDialogConfig config) {
		super(context, config);
	}

	/**
	 * Returns a {@link AbstractSelectDialog} for dialogs which can be used to edit the selection of
	 * the given {@link SelectField}.
	 * 
	 * @param selectField
	 *        the field whose selection shall be edited
	 */
	public abstract AbstractSelectDialog createSelectDialog(SelectField selectField);

	/**
	 * New {@link SelectDialogProvider} with default configuration.
	 */
	public static SelectDialogProvider newInstance() {
		return TypedConfigUtil.createInstance(newDefaultConfig());
	}

	/**
	 * New default {@link SelectDialogConfig}.
	 */
	public static SelectDialogConfig newDefaultConfig() {
		return TypedConfiguration.newConfigItem(SelectDialogConfig.class);
	}

	/**
	 * Create a new {@link SelectDialogProvider} that displays the options as table.
	 */
	public static SelectDialogProvider newTableInstance() {
		return TypedConfigUtil.createInstance(newTableConfig());
	}

	/**
	 * New configuration for table display.
	 */
	public static SelectDialogConfig newTableConfig() {
		return TypedConfiguration.newConfigItem(TableSelectDialogConfig.class);
	}

}