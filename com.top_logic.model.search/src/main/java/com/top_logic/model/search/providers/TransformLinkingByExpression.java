/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.providers;

import java.util.function.BiFunction;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.layout.channel.TransformingChannel;
import com.top_logic.layout.channel.linking.impl.AbstractTransformLinking;
import com.top_logic.layout.channel.linking.impl.ChannelLinking;
import com.top_logic.model.search.expr.config.ExprFormat;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.query.QueryExecutor;

/**
 * {@link ChannelLinking} that creates a {@link TransformingChannel} applying a function implemented
 * in TL-Script.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@InApp
public class TransformLinkingByExpression extends AbstractTransformLinking<TransformLinkingByExpression.Config> {

	/**
	 * Configuration options for {@link TransformLinkingByExpression}.
	 */
	@TagName("transform")
	public interface Config extends AbstractTransformLinking.Config {

		@Override
		@ClassDefault(TransformLinkingByExpression.class)
		Class<? extends ChannelLinking> getImplementationClass();

		/**
		 * The transformation to apply to the value of the input channel.
		 * 
		 * <p>
		 * The function receives the value of the input channel as first argument and the old
		 * channel value as second argument.
		 * </p>
		 */
		Expr getFunction();
	}

	private final BiFunction<Object, Object, Object> _transform;

	/**
	 * Creates a {@link TransformLinkingByExpression} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public TransformLinkingByExpression(InstantiationContext context, Config config) {
		super(context, config);

		QueryExecutor transform = QueryExecutor.compile(config.getFunction());
		_transform = (input, oldValue) -> transform.execute(input, oldValue);
	}

	@Override
	protected BiFunction<Object, Object, ?> transformation() {
		return _transform;
	}

	@Override
	public void appendTo(StringBuilder result) {
		result.append("transform(");
		input().appendTo(result);
		result.append(",");
		result.append(ExprFormat.INSTANCE.getSpecification(getConfig().getFunction()));
		result.append(")");
	}

}
