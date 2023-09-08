/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.runtime.action;

import java.util.Map;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.layout.scripting.action.GotoAction;
import com.top_logic.layout.scripting.runtime.ActionContext;
import com.top_logic.mig.html.layout.ComponentName;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.tool.boundsec.commandhandlers.GotoHandler;

/**
 * {@link ApplicationActionOp} that executes a given command.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class GotoActionOp extends AbstractCommandActionOp<GotoAction> {

	/**
	 * Creates a {@link GotoActionOp} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public GotoActionOp(InstantiationContext context, GotoAction config) {
		super(context, config);
	}

	@Override
	protected Map<String, Object> getArguments(ActionContext context, CommandHandler command, LayoutComponent component) {
		Map<String, Object> result = super.getArguments(context, command, component);

		ComponentName targetComponent = ((GotoHandler) command).findComponentName(result);
		if (targetComponent != null) {
			LayoutComponent resolvedTargetComponent = context.getMainLayout().getComponentByName(targetComponent);
			if (resolvedTargetComponent == null) {
				StringBuilder msg = new StringBuilder();
				msg.append("There is no component named '");
				msg.append(targetComponent);
				msg.append("' defined as target for goto.");
				throw ApplicationAssertions.fail(config, msg.toString());
			}
		}
		return result;
	}

}
