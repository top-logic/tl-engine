/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.runtime.action;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.layout.scripting.action.CommandActionBase;
import com.top_logic.layout.scripting.recorder.ref.ModelName;
import com.top_logic.layout.scripting.recorder.ref.ModelResolver;
import com.top_logic.layout.scripting.runtime.ActionContext;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.CommandHandler;

/**
 * {@link ApplicationActionOp} executing a {@link CommandHandler} referenced by a {@link ModelName}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class GenericCommnadActionOp extends CommandActionOpBase<GenericCommnadActionOp.Config> {

	/**
	 * Configuration options for {@link GenericCommnadActionOp}.
	 */
	public interface Config extends CommandActionBase {
		/**
		 * A reference to the {@link CommandHandler} to execute.
		 */
		ModelName getCommand();

		/**
		 * @see #getCommand()
		 */
		void setCommand(ModelName value);

		@Override
		@ClassDefault(GenericCommnadActionOp.class)
		Class<? extends AbstractApplicationActionOp<?>> getImplementationClass();
	}

	/**
	 * Creates a {@link GenericCommnadActionOp}.
	 */
	public GenericCommnadActionOp(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	protected CommandHandler getCommandHandler(ActionContext context, LayoutComponent component) {
		return (CommandHandler) ModelResolver.getInstance().locateModelImpl(context, component,
			getConfig().getCommand());
	}

}
