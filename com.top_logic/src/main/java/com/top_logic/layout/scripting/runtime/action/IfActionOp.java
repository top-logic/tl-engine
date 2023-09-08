/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.runtime.action;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.layout.scripting.action.IfAction;
import com.top_logic.layout.scripting.runtime.ActionContext;

/**
 * Executes an {@link ApplicationActionOp} depending on a condition.
 * <p>
 * As this class is deprecated, <em>there is no ScriptingGui support for this action</em>. That
 * means, neither is there a beautification xslt, nor will the ScriptingGui show the "then action"
 * or the "else action".
 * </p>
 * 
 * @deprecated Don't use this. It's only a hack. Tests have to operate on a defined application
 *             state. Therefore, a test using this "if action" should always get the same value for
 *             the condition, which makes the "if action" pointless. If this is not the case, the
 *             test (setup) has to be fixed.
 * 
 * @author <a href="mailto:Jan Stolzenburg@top-logic.com">Jan Stolzenburg</a>
 */
@Deprecated
public class IfActionOp extends AbstractApplicationActionOp<IfAction> {

	private final ApplicationActionOp<?> _then;

	private final ApplicationActionOp<?> _else;

	/**
	 * Called by the {@link TypedConfiguration} for creating a {@link IfActionOp}.
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
	public IfActionOp(InstantiationContext context, IfAction config) {
		super(context, config);
		_then = context.getInstance(config.getThen());
		_else = context.getInstance(config.getElse());
	}

	@Override
	protected Object processInternal(ActionContext context, Object argument) throws Throwable {
		Boolean condition = (Boolean) context.resolve(getConfig().getCondition());
		if (condition) {
			_then.process(context, argument);
		} else if (_else != null) {
			_else.process(context, argument);
		}
		return argument;
	}

}
