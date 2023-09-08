/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.export;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.mig.html.layout.ComponentName;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.BoundCommandGroup;
import com.top_logic.tool.boundsec.BoundComponent;
import com.top_logic.tool.execution.ExecutabilityRule;
import com.top_logic.tool.execution.ExecutableState;
import com.top_logic.tool.export.ExportTableComponent.ExportHandlersConfig;

/**
 * Checks if the current person is allowed to perform at least one of the
 * {@link ExportHandlersConfig#getExportHandlers()} actions.
 */
public class ExportTableOpenRule extends AbstractConfiguredInstance<ExportTableOpenRule.Config>
		implements ExecutabilityRule {

	/**
	 * Typed configuration interface definition for {@link ExportTableOpenRule}.
	 * 
	 * @author <a href="mailto:dbu@top-logic.com">dbu</a>
	 */
	public interface Config extends PolymorphicConfiguration<ExportTableOpenRule>, ExportHandlersConfig {

		/** Configuration name for the value of {@link #getExportComponentName()}. */
		String EXPORT_COMPONENT_NAME = "exportComponentName";

		/**
		 * Name of the export component open.
		 */
		@Mandatory
		@Name(EXPORT_COMPONENT_NAME)
		ComponentName getExportComponentName();

	}

	LayoutComponent _exportComponent;

	/**
	 * Create a {@link ExportTableOpenRule}.
	 * 
	 * @param context
	 *        the {@link InstantiationContext} to create the new object in
	 * @param config
	 *        the configuration object to be used for instantiation
	 */
	public ExportTableOpenRule(InstantiationContext context, Config config) {
		super(context, config);
		context.resolveReference(config.getExportComponentName(), LayoutComponent.class,
			layoutComponent -> _exportComponent = layoutComponent);
	}

	@Override
	public ExecutableState isExecutable(LayoutComponent aComponent, Object model, Map<String, Object> someValues) {
		return isOpenerExecutable(_exportComponent, model);
	}

	private ExecutableState isOpenerExecutable(LayoutComponent exportComponent, Object model) {
		if (exportComponent instanceof BoundComponent) {
			BoundComponent theComp = (BoundComponent) exportComponent;
			
			// collection all command groups
			Set<BoundCommandGroup> theGroups = new HashSet<>();
			for (ExportHandler exportHandler : getConfig().getHandlers().values()) {
				if (exportHandler.canExport(model)) {
					theGroups.add(exportHandler.getReadCommandGroup());
					theGroups.add(exportHandler.getExportCommandGroup());
				}
			}

			// if no command can handle the model, then hide the button 
			if (theGroups.isEmpty()) {
				return ExecutableState.NOT_EXEC_HIDDEN;
			}
			
			// check the security of the handler
			for (BoundCommandGroup theGroup : theGroups) {
				if (theComp.allowPotentialModel(theGroup, model)) {
					// first match is enough
					return ExecutableState.EXECUTABLE;
				}
			}
			return ExecutableState.NO_EXEC_PERMISSION;
		} else {
			return ExecutableState.EXECUTABLE;
		}
	}
	
}
