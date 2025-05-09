/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.bpe.bpml.display;

import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
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
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.query.QueryExecutor;

/**
 * {@link RuleCondition} that determines either a {@link SequenceFlow} can be accessed or not and
 * under which circumstances.
 *
 * @author <a href="mailto:Jonathan.H�sing@top-logic.com">Jonathan H�sing</a>
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
		 * The {@link Expr} uses the {@link ProcessExecution} as the only argument.
		 * </p>
		 *
		 * <p>
		 * A {@link Boolean} is expected as the return.
		 * </p>
		 */
		@Name(CONDITION)
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
		 * The {@link ResKey} to be shown, when the Condition is not met.
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

	/**
	 * Returns the {@link Boolean} calculated by the given {@link Expr}.
	 */
	@Override
	public boolean getTestCondition(ProcessExecution process) {
		return SearchExpression.isTrue(_condition.execute(process));
	}

	/**
	 * returns the {@link ResKey} Message.
	 */
	@Override
	public ResKey getMessage() {
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
