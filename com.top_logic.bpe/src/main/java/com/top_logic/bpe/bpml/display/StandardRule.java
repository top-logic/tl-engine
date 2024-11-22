/*
 * Copyright (c) 2024 Business Operation Systems GmbH. All Rights Reserved
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
public class StandardRule extends AbstractConfiguredInstance<StandardRule.Config<?>> implements RuleCondition {

	private final QueryExecutor _condition;

	private final RuleType _ruleType;

	private final ResKey _message;

	/**
	 * 
	 * Creates a new {@link StandardRule}.
	 *
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	public StandardRule(InstantiationContext context, Config<?> config) {
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
		 * 
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
		 * 
		 * Defines the type of Rule we want to use.
		 *
		 * <p>
		 * Default - If false, we cannot access this {@link SequenceFlow} and get a Custom Error
		 * Message.
		 * </p>
		 * 
		 * 
		 * <p>
		 * Hidden - If false, we cannot see the {@link SequenceFlow}.
		 * </p>
		 * 
		 * 
		 * <p>
		 * Warning - If false, we can access this {@link SequenceFlow}, however we get a pop up with
		 * a custom Warning Message.
		 * </p>
		 */
		@Name(RULETYPE)
		RuleType getRuleType();

		/**
		 * 
		 * The {@link ResKey} to be shown, when the Condition is not met.
		 *
		 *
		 */
		@Name(MESSAGE)
		@DynamicMode(fun = IsHidden.class, args = @Ref(RULETYPE))
		ResKey getMessage();

		/**
		 * 
		 * inner Class to determine the visibility of the Message input.
		 *
		 * @author <a href=mailto:Christian.Braun@top-logic.com>Christian Braun</a>
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
	public Boolean getCondition(ProcessExecution process) {
		return (Boolean) _condition.execute(process);
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
