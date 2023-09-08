/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.formeditor;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.layout.basic.Command;
import com.top_logic.layout.form.component.AbstractCreateComponent;
import com.top_logic.model.form.definition.FormDefinition;
import com.top_logic.tool.boundsec.CommandHandlerFactory;

/**
 * {@link Command} to edit a {@link FormDefinition} of a {@link AbstractCreateComponent}.
 * 
 * @author <a href="mailto:iwi@top-logic.com">Isabell Wittich</a>
 */
public class ConfigureCreateFormDefinitionCommand extends AbstractConfigureFormDefinitionCommand {
	/**
	 * Identifier under which an instance of this class is configured in the
	 * {@link CommandHandlerFactory}.
	 */
	public static final String DEFAULT_COMMAND_ID = "configureCreateFormDefinitionCommand";

	/**
	 * Creates an instance of {@link ConfigureCreateFormDefinitionCommand}.
	 */
	public ConfigureCreateFormDefinitionCommand(InstantiationContext context, Config config) {
		super(context, config);
	}
}
