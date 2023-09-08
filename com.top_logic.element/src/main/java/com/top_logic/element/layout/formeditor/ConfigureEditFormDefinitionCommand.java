/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.formeditor;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.element.layout.formeditor.builder.ConfiguredDynamicFormBuilder;
import com.top_logic.layout.form.component.FormComponent;
import com.top_logic.model.form.definition.FormDefinition;
import com.top_logic.tool.boundsec.CommandHandlerFactory;
import com.top_logic.tool.execution.CombinedExecutabilityRule;
import com.top_logic.tool.execution.ExecutabilityRule;
import com.top_logic.tool.execution.NullModelDisabled;

/**
 * Command to change the view of a {@link FormComponent} by editing the underlying
 * {@link FormDefinition}.
 * 
 * <p>
 * This command is only usable by {@link FormComponent}s instantiated from typed layout templates
 * using the {@link ConfiguredDynamicFormBuilder}.
 * </p>
 * 
 * @see ConfiguredDynamicFormBuilder
 * @see FormComponent
 * 
 * @author <a href="mailto:sfo@top-logic.com">sfo</a>
 */
public class ConfigureEditFormDefinitionCommand extends AbstractConfigureFormDefinitionCommand {

	/**
	 * Identifier under which an instance of this class is configured in the
	 * {@link CommandHandlerFactory}.
	 */
	public static final String DEFAULT_COMMAND_ID = "configureEditFormDefinitionCommand";

	/**
	 * Creates an instance of {@link ConfigureEditFormDefinitionCommand}.
	 */
	public ConfigureEditFormDefinitionCommand(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	protected ExecutabilityRule intrinsicExecutability() {
		return CombinedExecutabilityRule.combine(NullModelDisabled.INSTANCE, super.intrinsicExecutability());
	}
}
