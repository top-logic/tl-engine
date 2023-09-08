/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.security;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.element.layout.security.handler.SecurityExportHandler;
import com.top_logic.mig.html.layout.CommandRegistry;
import com.top_logic.tool.boundsec.gui.profile.EditRolesProfileComponent;

/**
 * Register security export
 * 
 * @author     <a href="mailto:kbu@top-logic.com">kbu</a>
 */
public class ExportingEditRolesProfileComponent extends
		EditRolesProfileComponent {

	/**
	 * Configuration for the {@link ExportingEditRolesProfileComponent}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config extends EditRolesProfileComponent.Config {

		/** @see com.top_logic.basic.reflect.DefaultMethodInvoker */
		Lookup LOOKUP = MethodHandles.lookup();

		@Override
		default void modifyIntrinsicCommands(CommandRegistry registry) {
			EditRolesProfileComponent.Config.super.modifyIntrinsicCommands(registry);
			registry.registerButton(SecurityExportHandler.COMMAND_ID);
		}

	}

	public ExportingEditRolesProfileComponent(InstantiationContext context, Config atts)
			throws ConfigurationException {
		super(context, atts);
	}
	
}
