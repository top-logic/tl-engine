/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.providers;

import java.util.function.BiFunction;

import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.layout.channel.BidirectionalTransformLinking;
import com.top_logic.layout.channel.BidirectionalTransformingChannel;
import com.top_logic.layout.channel.linking.impl.AbstractTransformLinking;
import com.top_logic.layout.channel.linking.impl.ChannelLinking;
import com.top_logic.model.search.expr.config.ExprFormat;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.query.QueryExecutor;

/**
 * {@link ChannelLinking} that creates a {@link BidirectionalTransformingChannel} applying the
 * forward and backward transformation implemented in TL-Script.
 * 
 * @author <a href="mailto:sfo@top-logic.com">sfo</a>
 */
@InApp
public class BidirectionalTransformLinkingByExpression
		extends BidirectionalTransformLinking<BidirectionalTransformLinkingByExpression.Config> {

	/**
	 * Configuration options for {@link BidirectionalTransformLinkingByExpression}.
	 */
	@TagName("bitransform")
	public interface Config extends AbstractTransformLinking.Config {

		@Override
		@ClassDefault(BidirectionalTransformLinkingByExpression.class)
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

		/**
		 * The transformation back to obtain the value for the input channel.
		 * 
		 * <p>
		 * The function receives the value of the transformed channel as first argument and the old
		 * channel value as second argument.
		 * </p>
		 */
		Expr getInverseFunction();
	}

	private final BiFunction<Object, Object, Object> _transform;

	private final BiFunction<Object, Object, Object> _inverseTransform;

	/**
	 * Creates a {@link BidirectionalTransformLinkingByExpression} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	public BidirectionalTransformLinkingByExpression(InstantiationContext context, Config config) {
		super(context, config);

		QueryExecutor transform = QueryExecutor.compile(config.getFunction());
		QueryExecutor inverseTransform = QueryExecutor.compile(config.getInverseFunction());

		_transform = (input, oldValue) -> transform.execute(input, oldValue);
		_inverseTransform = (input, oldValue) -> inverseTransform.execute(input, oldValue);
	}

	@Override
	public void appendTo(StringBuilder result) {
		result.append("bitransform(");
		input().appendTo(result);
		result.append(",");
		result.append(ExprFormat.INSTANCE.getSpecification(getConfig().getFunction()));
		result.append(",");
		result.append(ExprFormat.INSTANCE.getSpecification(getConfig().getInverseFunction()));
		result.append(")");
	}

	@Override
	protected BiFunction<Object, Object, ?> transformation() {
		return _transform;
	}

	@Override
	protected BiFunction<Object, Object, ?> inverseTransformation() {
		return _inverseTransform;
	}

}
