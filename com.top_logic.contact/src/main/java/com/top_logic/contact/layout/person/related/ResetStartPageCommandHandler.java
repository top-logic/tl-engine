/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.contact.layout.person.related;

import java.util.List;
import java.util.Map;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.defaults.FormattedDefault;
import com.top_logic.basic.config.annotation.defaults.ListDefault;
import com.top_logic.knowledge.wrap.person.NoStartPageAutomatismExecutability;
import com.top_logic.knowledge.wrap.person.PersonalConfiguration;
import com.top_logic.layout.DisplayContext;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.AbstractCommandHandler;
import com.top_logic.tool.boundsec.CommandGroupReference;
import com.top_logic.tool.boundsec.DefaultHandlerResult;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.tool.boundsec.simple.SimpleBoundCommandGroup;
import com.top_logic.tool.execution.ExecutabilityRule;
import com.top_logic.tool.execution.InViewModeExecutable;

/** Resets the start page of the current user. */
@Label("Reset start page")
public class ResetStartPageCommandHandler extends AbstractCommandHandler {

	/** {@link ConfigurationItem} of the {@link ResetStartPageCommandHandler}. */
	public interface Config extends AbstractCommandHandler.Config {

		@Override
		@ListDefault({ InViewModeExecutable.class, NoStartPageAutomatismExecutability.class })
		List<PolymorphicConfiguration<? extends ExecutabilityRule>> getExecutability();

		@Override
		@FormattedDefault(SimpleBoundCommandGroup.SYSTEM_NAME)
		CommandGroupReference getGroup();

	}

	/** The default {@link #getID()} of the command. */
	public static final String COMMAND_ID = "resetStartPage";

	/**
	 * Called by the {@link TypedConfiguration} for creating a
	 * {@link ResetStartPageCommandHandler}.
	 * <p>
	 * <b>Don't call directly.</b> Use
	 * {@link InstantiationContext#getInstance(PolymorphicConfiguration)} instead.
	 * </p>
	 * 
	 * @param context
	 *        For error reporting and instantiation of dependent configured objects.
	 * @param config
	 *        The configuration for the new instance.
	 */
	@CalledByReflection
	public ResetStartPageCommandHandler(InstantiationContext context, ResetStartPageCommandHandler.Config config) {
		super(context, config);
	}

	@Override
	public HandlerResult handleCommand(DisplayContext aContext, LayoutComponent aComponent, Object model, Map<String, Object> someArguments) {
		PersonalConfiguration thePConf = PersonalConfiguration.getPersonalConfiguration();
		thePConf.removeHomepage(aComponent.getMainLayout());
		PersonalConfiguration.storePersonalConfiguration();
		aComponent.invalidate();
		return DefaultHandlerResult.DEFAULT_RESULT;
	}

}