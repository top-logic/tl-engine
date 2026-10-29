/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.channel;

import java.util.List;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.model.ObservedTypes;
import com.top_logic.model.search.expr.query.QueryExecutor;
import com.top_logic.model.util.TLModelPartRef;

/**
 * {@link ChannelFactory} that creates mutable value channels.
 *
 * <p>
 * Creates a {@link DefaultViewChannel} for a channel without initialization function, and an
 * {@link InitializedViewChannel} for one with an initialization function, which is compiled once at
 * configuration parse time.
 * </p>
 */
public class ValueChannelFactory implements ChannelFactory {

	private final String _name;

	private final List<InputRef> _inputRefs;

	/**
	 * The compiled initialization function, {@code null} for a channel that starts empty.
	 */
	private final QueryExecutor _init;

	private final List<TLModelPartRef> _observedTypeRefs;

	/**
	 * Creates a {@link ValueChannelFactory} from configuration.
	 */
	@CalledByReflection
	public ValueChannelFactory(InstantiationContext context, ValueChannelConfig config) {
		_name = config.getName();
		_inputRefs = config.getInputs();
		_observedTypeRefs = config.getObservedTypes();
		_init = InputScript.compile(config, ValueChannelConfig.INIT);
		if (_init == null && (!_inputRefs.isEmpty() || !_observedTypeRefs.isEmpty())) {
			context.error("Channel '" + _name + "' declares '" + Inputs.INPUTS + "' or '"
				+ ValueChannelConfig.OBSERVED_TYPES + "' without '" + ValueChannelConfig.INIT
				+ "': they only determine when the initialization function is applied.");
		}
	}

	@Override
	public ViewChannel createChannel(ViewContext context) {
		if (_init == null) {
			return new DefaultViewChannel(_name);
		}
		InitializedViewChannel channel = new InitializedViewChannel(_name);
		channel.bind(ChannelInputs.resolve(context, _inputRefs), _init::execute,
			ObservedTypes.resolve(_observedTypeRefs));
		return channel;
	}
}
