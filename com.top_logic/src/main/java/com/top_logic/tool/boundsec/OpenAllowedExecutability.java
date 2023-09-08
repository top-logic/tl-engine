/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.boundsec;

import java.util.Map;

import com.top_logic.basic.Configuration;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.util.ResKey;
import com.top_logic.mig.html.layout.ComponentName;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.execution.ExecutabilityRule;
import com.top_logic.tool.execution.ExecutableState;

/**
 * {@link ExecutabilityRule} that checks that the current user is allowed to view a configured
 * dialog of the calling {@link LayoutComponent}.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class OpenAllowedExecutability extends AbstractConfiguredInstance<OpenAllowedExecutability.Config>
		implements ExecutabilityRule {

	/**
	 * {@link Configuration} of the {@link OpenAllowedExecutability}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config extends PolymorphicConfiguration<OpenAllowedExecutability> {
		
		/**
		 * Name of the {@link #getDialogName()} property.
		 */
		String DIALOG_NAME_PROPERTY = "dialog-name";

		/**
		 * The name of the dialog whose allow must be checked.
		 */
		@Name(DIALOG_NAME_PROPERTY)
		@Mandatory
		ComponentName getDialogName();
		
		/**
		 * Setter for {@link #getDialogName()}.
		 */
		void setDialogName(ComponentName dialogName);
	}
	
	/**
	 * Creates a new {@link OpenAllowedExecutability} from the given configuration.
	 * 
	 * @param context
	 *        {@link InstantiationContext} to instantiate sub configurations.
	 * @param config
	 *        Configuration for this {@link OpenAllowedExecutability}.
	 */
	public OpenAllowedExecutability(InstantiationContext context, Config config)  {
		super(context, config);
	}

	@Override
	public ExecutableState isExecutable(LayoutComponent component, Object model, Map<String, Object> someValues) {
		ComponentName dialogName = getConfig().getDialogName();
		LayoutComponent dialog = component.getDialog(dialogName);
		if (dialog instanceof BoundCheckerComponent) {
			// Note: The model, with the opening command is called must not be used for computing
			// the current visibility of the dialog, because the OpenAllowedExecutability is only
			// used, if the opening command has no target component. Therefore, the command target
			// model is not used.
			ResKey hideReason = ((BoundCheckerComponent) dialog).hideReason();
			if (hideReason != null) {
				return ExecutableState.createDisabledState(hideReason);
			}
		}
		return ExecutableState.EXECUTABLE;
	}

}

