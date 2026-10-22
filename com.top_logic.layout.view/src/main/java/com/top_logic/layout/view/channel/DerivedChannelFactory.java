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
import com.top_logic.layout.view.model.ObservedTypes;
import com.top_logic.model.search.expr.query.QueryExecutor;
import com.top_logic.model.util.TLModelPartRef;

/**
 * {@link ChannelFactory} for derived (computed, read-only) channels.
 *
 * <p>
 * Compiles the TL-Script expression once at configuration parse time. Per-session, resolves the
 * input channel references and the observed types, and creates a {@link DerivedViewChannel} that
 * recomputes on input changes and, while displayed, on changes of the observed objects.
 * </p>
 */
public class DerivedChannelFactory implements ChannelFactory {

	private final String _name;

	private final List<InputRef> _inputRefs;

	private final QueryExecutor _executor;

	private final QueryExecutor _reverseExecutor;

	private final List<TLModelPartRef> _observedTypeRefs;

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
		_executor = InputScript.compile(config, DerivedChannelConfig.EXPR);
		_reverseExecutor = InputScript.compile(config, DerivedChannelConfig.REVERSE);
		_observedTypeRefs = config.getObservedTypes();
	}

	@Override
	public ViewChannel createChannel(ViewContext context) {
		DerivedViewChannel channel = new DerivedViewChannel(_name);
		List<ViewChannel> inputs = ChannelInputs.resolve(context, _inputRefs);
		Function<Object, Object> reverse = _reverseExecutor != null
			? value -> _reverseExecutor.execute(ChannelInputs.arguments(inputs, value))
			: null;
		channel.bind(inputs, _executor::execute, reverse, ObservedTypes.resolve(_observedTypeRefs));
		return channel;
	}
}
