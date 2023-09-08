/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.command;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.layout.table.model.TableConfig;
import com.top_logic.tool.boundsec.CommandHandler.Config.AllCliques;
import com.top_logic.tool.boundsec.CommandHandlerFactory;

/**
 * Configuration part of {@link TableConfig#getCommands()}.
 */
public interface TableCommandConfig extends ConfigurationItem {

	/**
	 * The toolbar group where to display the command.
	 * 
	 * <p>
	 * Toolbar groups are top-level
	 * {@link com.top_logic.tool.boundsec.CommandHandlerFactory.Config#getCliques()} configured
	 * in the {@link CommandHandlerFactory}.
	 * </p>
	 */
	@Name("toolbar-group")
	@StringDefault(CommandHandlerFactory.ADDITIONAL_GROUP)
	@Options(fun = AllCliques.class)
	String getToolbarGroup();

	/**
	 * {@link TableCommandProvider} creating the button.
	 * 
	 * @see TableCommandProvider#createCommand(com.top_logic.layout.table.TableData)
	 */
	@Name("button")
	@Mandatory
	PolymorphicConfiguration<? extends TableCommandProvider> getButton();
}