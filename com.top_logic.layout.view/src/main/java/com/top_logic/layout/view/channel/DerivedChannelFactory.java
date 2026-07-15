/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.channel;

import java.util.List;
import java.util.function.Function;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.query.QueryExecutor;

/**
 * {@link ChannelFactory} for derived (computed, read-only) channels.
 *
 * <p>
 * Compiles the TL-Script expression once at configuration parse time. Per-session, resolves input
 * channel references and creates a {@link DerivedViewChannel} that recomputes on input changes.
 * </p>
 */
public class DerivedChannelFactory implements ChannelFactory {

	private final String _name;

	private final List<ChannelRef> _inputRefs;

	private final QueryExecutor _executor;

	private final QueryExecutor _reverseExecutor;

	/**
	 * Creates a {@link DerivedChannelFactory} from configuration.
	 *
	 * <p>
	 * The TL-Script expressions are compiled here (config time) and shared across all sessions.
	 * </p>
	 */
	@CalledByReflection
	public DerivedChannelFactory(InstantiationContext context, DerivedChannelConfig config) {
		_name = config.getName();
		_inputRefs = config.getInputs();
		_executor = QueryExecutor.compile(config.getExpr());
		Expr reverseExpr = config.getReverse();
		_reverseExecutor = reverseExpr != null ? QueryExecutor.compile(reverseExpr) : null;
	}

	@Override
	public ViewChannel createChannel(ViewContext context) {
		DerivedViewChannel channel = new DerivedViewChannel(_name);
		List<ViewChannel> inputs = _inputRefs.stream()
			.map(context::resolveChannel)
			.toList();
		Function<Object, Object> reverse =
			_reverseExecutor != null ? value -> _reverseExecutor.execute(value) : null;
		channel.bind(inputs, _executor::execute, reverse);
		return channel;
	}
}
