/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.locking.ui;

import java.util.Map;

import com.top_logic.base.locking.token.LockInfo;
import com.top_logic.base.locking.token.TokenService;
import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.layout.DisplayContext;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.tool.boundsec.conditional.CommandStep;
import com.top_logic.tool.boundsec.conditional.Failure;
import com.top_logic.tool.boundsec.conditional.PreconditionCommandHandler;
import com.top_logic.tool.boundsec.conditional.Success;
import com.top_logic.tool.execution.I18NConstants;

/**
 * {@link CommandHandler} releasing a selected {@link LockInfo}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ReleaseLockCommand extends PreconditionCommandHandler {

	/**
	 * Creates a {@link ReleaseLockCommand} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public ReleaseLockCommand(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	protected CommandStep prepare(LayoutComponent component, Object model, Map<String, Object> arguments) {
		if (model == null) {
			return new Failure(I18NConstants.ERROR_NO_MODEL);
		}
		if (!(model instanceof LockInfo)) {
			return new Failure(I18NConstants.ERROR_MODEL_NOT_SUPPORTED);
		}
		LockInfo lock = (LockInfo) model;
		return new Success() {
			@Override
			protected void doExecute(DisplayContext context) {
				TokenService.getInstance().release(lock.getTokens());
				component.invalidate();
			}
		};
	}

}
