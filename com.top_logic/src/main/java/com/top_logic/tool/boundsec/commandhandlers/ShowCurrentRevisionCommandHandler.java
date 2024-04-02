/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.boundsec.commandhandlers;

import java.util.Map;

import com.top_logic.base.services.simpleajax.AJAXCommandHandler;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.defaults.FormattedDefault;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.knowledge.service.Revision;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.knowledge.wrap.WrapperHistoryUtils;
import com.top_logic.layout.DisplayContext;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.CommandGroupReference;
import com.top_logic.tool.boundsec.CommandHandlerFactory;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.tool.boundsec.simple.SimpleBoundCommandGroup;
import com.top_logic.tool.execution.CombinedExecutabilityRule;
import com.top_logic.tool.execution.ExecutabilityRule;
import com.top_logic.tool.execution.HasCurrentRevisionRule;
import com.top_logic.tool.execution.HistoricDataHideCurrentRule;
import com.top_logic.tool.execution.InViewModeExecutable;

/**
 * Display the current revision of the components model.
 * 
 * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class ShowCurrentRevisionCommandHandler extends AJAXCommandHandler {

    /** ID this command can be called by application. */
    public static final String COMMAND_ID = "showCurrentRevision";

	/**
	 * Configuration options for {@link ShowCurrentRevisionCommandHandler}.
	 */
	public interface Config extends AJAXCommandHandler.Config {
		@Override
		@FormattedDefault(SimpleBoundCommandGroup.SYSTEM_NAME)
		CommandGroupReference getGroup();

		@Override
		@StringDefault(CommandHandlerFactory.ADDITIONAL_GROUP)
		String getClique();
	}

    /** 
     * Creates a {@link ShowCurrentRevisionCommandHandler}.
     */
    public ShowCurrentRevisionCommandHandler(InstantiationContext context, Config config) {
		super(context, config);
    }

    @Override
	public HandlerResult handleCommand(DisplayContext aContext, LayoutComponent aComponent, Object model, Map<String, Object> someArguments) {
		if (model instanceof Wrapper) {
			model = WrapperHistoryUtils.getWrapper(Revision.CURRENT, (Wrapper) model);

			if (aComponent.supportsModel(model)) {
				aComponent.setModel(model);
                this.updateComponent(aComponent);
            }
        }

        return HandlerResult.DEFAULT_RESULT;
    }

    protected void updateComponent(LayoutComponent aComponent) {
        aComponent.invalidate();
        aComponent.invalidateButtons();
    }

	@Override
	protected ExecutabilityRule intrinsicExecutability() {
		return CombinedExecutabilityRule.combine(
			super.intrinsicExecutability(),
			HistoricDataHideCurrentRule.INSTANCE,
			InViewModeExecutable.INSTANCE,
			HasCurrentRevisionRule.INSTANCE);
	}

}

