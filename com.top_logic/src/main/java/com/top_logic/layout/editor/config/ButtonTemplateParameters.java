/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.editor.config;

import java.util.List;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.Abstract;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.layout.form.values.edit.InAppImplementations;
import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.tool.boundsec.CommandHandler;

/**
 * Template configuration parameter to provide in app edit of buttons.
 * 
 * @author <a href="mailto:sfo@top-logic.com">sfo</a>
 */
@Abstract
public interface ButtonTemplateParameters extends ConfigurationItem {

	/**
	 * Configuration option for {@link #getButtons()}.
	 */
	public static final String BUTTONS = "buttons";

	/**
	 * All buttons on this component.
	 */
	@Options(fun = InAppImplementations.class)
	@Name(BUTTONS)
	List<CommandHandler.ConfigBase<? extends CommandHandler>> getButtons();

}
