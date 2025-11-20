/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.bpe.bpml.display;

import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Ref;
import com.top_logic.basic.config.order.DisplayOrder;
import com.top_logic.basic.func.Function1;
import com.top_logic.basic.util.ResKey;
import com.top_logic.bpe.bpml.model.SequenceFlow;
import com.top_logic.bpe.execution.model.ProcessExecution;
import com.top_logic.bpe.layout.execution.SelectTransitionDialog;
import com.top_logic.layout.form.component.WarningsDialog;
import com.top_logic.layout.form.model.FieldMode;
import com.top_logic.layout.form.values.edit.annotation.DynamicMode;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.query.QueryExecutor;

/**
 * {@link RuleCondition} that determines either a {@link SequenceFlow} can be accessed or not and
 * under which circumstances.
 *
 * @author <a href="mailto:Jonathan.Hüsing@top-logic.com">Jonathan Hüsing</a>
 */
public class ConfigurableCondition extends AbstractConfiguredInstance<ConfigurableCondition.Config<?>> implements RuleCondition {

	private final QueryExecutor _condition;

	private final RuleType _ruleType;

	private final ResKey _message;

	/**
	 * Creates a {@link ConfigurableCondition}.
	 *
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	public ConfigurableCondition(InstantiationContext context, Config<?> config) {
		super(context, config);

		_ruleType = config.getRuleType();
		_condition = QueryExecutor.compile(config.getCondition());
		_message = config.getMessage();
	}

	/**
	 * Configuration options for {@link RuleCondition}.
	 */
	@DisplayOrder({
		Config.RULETYPE,
		Config.CONDITION,
		Config.MESSAGE,
	})
	public interface Config<I extends RuleCondition> extends PolymorphicConfiguration<I> {
		/**
		 * @see #getCondition()
		 */
		String CONDITION = "condition";

		/**
		 * @see #getRuleType()
		 */
		String RULETYPE = "rule-type";

		/**
		 * @see #getMessage()
		 */
		String MESSAGE = "message";

		/**
		 * The {@link Expr} used to calculate if the {@link SequenceFlow} is accessible.
		 * 
		 * <p>
		 * The {@link Expr} uses the {@link ProcessExecution} as first argument and
		 * {@link #getMessage()} as second.
		 * </p>
		 *
		 * <p>
		 * <code>true</code> as the result means that the condition is met. If a {@link ResKey} or
		 * false is returned, then the {@link SequenceFlow} is not accessible.
		 * </p>
		 */
		@Name(CONDITION)
		@Mandatory
		Expr getCondition();

		/**
		 * Defines rule enforcement for {@link SequenceFlow} transitions when a
		 * {@link ConfigurableCondition} evaluates to false.
		 *
		 * <p>
		 * Default - Block {@link SequenceFlow} and show error message.
		 * </p>
		 * 
		 * <p>
		 * Hidden - Hide {@link SequenceFlow} from {@link SelectTransitionDialog}
		 * </p>
		 * 
		 * <p>
		 * Warning - Allow {@link SequenceFlow} but show a {@link WarningsDialog}
		 * </p>
		 */
		@Name(RULETYPE)
		RuleType getRuleType();

		/**
		 * The {@link ResKey} to be shown, when the condition is not met.
		 */
		@Name(MESSAGE)
		@DynamicMode(fun = IsHidden.class, args = @Ref(RULETYPE))
		ResKey getMessage();

		/**
		 * Function determining the visibility of {@link Config#getMessage()}.
		 */
		class IsHidden extends Function1<FieldMode, RuleType> {
			@Override
			public FieldMode apply(RuleType type) {
				return type == RuleType.HIDDEN ? FieldMode.INVISIBLE : FieldMode.ACTIVE;
			}
		}
	}

	@Override
	public ResKey getMessage(ProcessExecution process) {
		Object resultMessage = _condition.execute(process, _message);
		if (resultMessage instanceof ResKey key) {
			return key;
		}
		if (resultMessage instanceof String keyString) {
			return ResKey.text(keyString);
		}
		if (resultMessage instanceof Boolean booleanResult) {
			if (booleanResult.booleanValue()) {
				return null;
			}
		}
		return _message;
	}

	/**
	 * Returns the chosen {@link RuleType}
	 */
	@Override
	public RuleType getRuleType() {
		return _ruleType;
	}

}
