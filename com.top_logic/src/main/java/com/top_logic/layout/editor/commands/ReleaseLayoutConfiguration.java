/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.editor.commands;

import java.util.Map;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.util.ResKey;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.layout.DisplayContext;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.LayoutStorage;
import com.top_logic.tool.boundsec.ConfirmCommandHandler;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.tool.execution.ExecutabilityRule;
import com.top_logic.tool.execution.InDesignModeExecutable;
import com.top_logic.util.LayoutBasedSecurity;
import com.top_logic.util.TLContext;

/**
 * Publishes all design-mode changes to the application views to all users of the application.
 * 
 * <p>
 * Changes in design-mode are only visible for the current user, until he publishes his changes
 * using this command.
 * </p>
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@Label("Release layout")
public class ReleaseLayoutConfiguration extends ConfirmCommandHandler {

	static String layoutKey(LayoutComponent component) {
		return component.getMainLayout().getLayoutKey(component);
	}

	/**
	 * Creates a new {@link ReleaseLayoutConfiguration}.
	 */
	public ReleaseLayoutConfiguration(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	protected HandlerResult internalHandleCommand(DisplayContext aContext, LayoutComponent aComponent, Object model,
			Map<String, Object> someArguments) {
		try (Transaction tx = PersistencyLayer.getKnowledgeBase().beginTransaction()) {
			LayoutStorage.getInstance().releaseLayouts(TLContext.getContext().getPerson());
			tx.commit();
		}
		LayoutBasedSecurity.reloadPersistentSecurity();
		return HandlerResult.DEFAULT_RESULT;
	}

	@Override
	protected ResKey getConfirmMessage(DisplayContext context, LayoutComponent component, Object model,
			Map<String, Object> someArguments) {
		return I18NConstants.CONFIRM_RELEASE_LAYOUT_CMD;
	}

	@Override
	protected ExecutabilityRule intrinsicExecutability() {
		return InDesignModeExecutable.INSTANCE;
	}

}

