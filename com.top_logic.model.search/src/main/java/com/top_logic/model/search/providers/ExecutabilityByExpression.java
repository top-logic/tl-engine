/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.providers;

import java.util.Map;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.util.ResKey;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.query.QueryExecutor;
import com.top_logic.tool.execution.ExecutabilityRule;
import com.top_logic.tool.execution.ExecutableState;
import com.top_logic.tool.execution.I18NConstants;
import com.top_logic.util.TLResKeyUtil;

/**
 * {@link ExecutabilityRule} that can be parameterized with a TL-Script expression.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@InApp
public class ExecutabilityByExpression extends AbstractConfiguredInstance<ExecutabilityByExpression.Config>
		implements ExecutabilityRule {

	/**
	 * Configuration options for {@link ExecutabilityByExpression}.
	 */
	@TagName("rule-by-expression")
	public interface Config extends PolymorphicConfiguration<ExecutabilityByExpression> {

		/**
		 * Whether a non-executable command is displayed in disabled state.
		 * 
		 * <p>
		 * A value of <code>false</code> means to hide a non-executable command. If a disabled
		 * command is shown, {@link #getDecision()} should provide a reason for the
		 * non-executability.
		 * </p>
		 */
		boolean getShowDisabled();

		/**
		 * Function deciding whether a command can currently be executed.
		 * 
		 * <p>
		 * The function takes the target model of the command as single argument. To allow
		 * execution, <code>true</code> or <code>null</code> must be returned. Any other value
		 * denies execution. If {@link #getShowDisabled()} is configured to be <code>true</code>, a
		 * non-<code>null</code> function result is used as reason for the disabled state show to
		 * the user. It's best to return a {@link ResKey} with an internationalized reason. A string
		 * result is used as not internationalized text. Any other value produces no reason.
		 * </p>
		 */
		Expr getDecision();

	}

	private QueryExecutor _decision;

	/**
	 * Creates a {@link ExecutabilityByExpression} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public ExecutabilityByExpression(InstantiationContext context, Config config) {
		super(context, config);

		_decision = QueryExecutor.compile(config.getDecision());
	}

	@Override
	public ExecutableState isExecutable(LayoutComponent aComponent, Object model, Map<String, Object> someValues) {
		return toExecutabilityState(getConfig().getShowDisabled(), _decision.execute(model));
	}

	/**
	 * Converts the given function result to an {@link ExecutableState}
	 * 
	 * @param showDisabled
	 *        Whether to produce an {@link ExecutableState#isDisabled()} result (instead of a
	 *        {@link ExecutableState#isHidden()} one.
	 */
	public static ExecutableState toExecutabilityState(boolean showDisabled, Object result) {
		if (result == null || Boolean.TRUE.equals(result)) {
			return ExecutableState.EXECUTABLE;
		}
		if (showDisabled) {
			return toState(result);
		} else {
			return ExecutableState.NOT_EXEC_HIDDEN;
		}
	}

	private static ExecutableState toState(Object result) {
		return ExecutableState.createDisabledState(TLResKeyUtil.toResKey(result, I18NConstants.ERROR_DISABLED));
	}

}
