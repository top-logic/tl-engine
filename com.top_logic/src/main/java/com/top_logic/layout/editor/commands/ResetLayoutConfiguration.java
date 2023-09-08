/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.editor.commands;

import java.util.Map;

import com.top_logic.basic.BufferingProtocol;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.util.ResKey;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.editor.LayoutTemplateUtils;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.LayoutStorage;
import com.top_logic.tool.boundsec.AbstractCommandHandler;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.tool.boundsec.CommandHandlerFactory;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.tool.execution.CombinedExecutabilityRule;
import com.top_logic.tool.execution.ExecutabilityRule;
import com.top_logic.tool.execution.InDesignModeExecutable;
import com.top_logic.util.error.TopLogicException;

/**
 * Command that resets the layout configuration for the current user.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ResetLayoutConfiguration extends AbstractCommandHandler {

	/**
	 * {@link CommandHandler#getID() Command id} of this command in the
	 * {@link CommandHandlerFactory}.
	 */
	public static final String DEFAULT_COMMAND_ID = "resetLayoutConfig";

	/**
	 * Creates a new {@link ResetLayoutConfiguration}.
	 */
	public ResetLayoutConfiguration(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public HandlerResult handleCommand(DisplayContext aContext, LayoutComponent aComponent, Object model,
			Map<String, Object> someArguments) {
		String layoutKey = ReleaseLayoutConfiguration.layoutKey(aComponent);
		boolean deleted;
		try (Transaction tx = PersistencyLayer.getKnowledgeBase().beginTransaction()) {
			deleted = LayoutTemplateUtils.deletePersistentTemplateLayout(layoutKey);
			tx.commit();
		}
		if (deleted) {
			try {
				BufferingProtocol log = new BufferingProtocol();
				LayoutComponent.Config newLayout = LayoutStorage.getInstance().getOrCreateLayoutConfig(layoutKey);
				if (log.hasErrors()) {
					throw new TopLogicException(I18NConstants.ERROR_REINSTALL_LAYOUT)
						.initDetails(ResKey.text(log.getError()));
				}
				aComponent.getMainLayout().replaceComponent(layoutKey, newLayout);
			} catch (ConfigurationException ex) {
				return HandlerResult.error(I18NConstants.ERROR_REINSTALL_LAYOUT, ex);
			}
		}
		return HandlerResult.DEFAULT_RESULT;
	}

	@Override
	protected ExecutabilityRule intrinsicExecutability() {
		return CombinedExecutabilityRule.combine(InDesignModeExecutable.INSTANCE,
			HasLayoutStoredInDatabase.INSTANCE);
	}

}

