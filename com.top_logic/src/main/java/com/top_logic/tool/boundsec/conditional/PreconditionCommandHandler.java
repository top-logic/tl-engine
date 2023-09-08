/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.boundsec.conditional;

import java.util.Map;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.layout.DisplayContext;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.AbstractCommandHandler;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.tool.execution.ExecutabilityRule;
import com.top_logic.tool.execution.ExecutableState;

/**
 * {@link AbstractCommandHandler} that can only be executed if some internal preconditions are
 * given.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class PreconditionCommandHandler extends AbstractCommandHandler {

	/**
	 * Creates a {@link PreconditionCommandHandler} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public PreconditionCommandHandler(InstantiationContext context, AbstractCommandHandler.Config config) {
		super(context, config);
	}

	@Override
	public final HandlerResult handleCommand(DisplayContext context, LayoutComponent component, Object model,
			Map<String, Object> arguments) {
		return prepare(component, model, arguments).execute(context);
	}

	/**
	 * Prepare for executing this command.
	 * 
	 * <p>
	 * If some precondition is violated, {@link Failure} or {@link Hide} should be returned.
	 * </p>
	 * 
	 * @param component
	 *        See {@link #handleCommand(DisplayContext, LayoutComponent, Object, Map)}.
	 * @param model
	 *        See {@link #handleCommand(DisplayContext, LayoutComponent, Object, Map)}.
	 * @param arguments
	 *        See {@link #handleCommand(DisplayContext, LayoutComponent, Object, Map)}.
	 * @return A {@link CommandStep} that can be executed, if all preconditions hold.
	 */
	protected abstract CommandStep prepare(LayoutComponent component, Object model, Map<String, Object> arguments);

	@Override
	protected ExecutabilityRule intrinsicExecutability() {
		return new ExecutabilityRule() {
			@Override
			public ExecutableState isExecutable(LayoutComponent aComponent, Object model,
					Map<String, Object> arguments) {
				return prepare(aComponent, model, arguments).getExecutability();
			}
		};
	}

}
