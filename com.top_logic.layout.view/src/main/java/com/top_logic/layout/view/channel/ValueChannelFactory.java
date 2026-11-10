/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.channel;

import java.util.List;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.knowledge.wrap.person.PersonalConfiguration;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.model.ObservedTypes;
import com.top_logic.model.search.expr.query.QueryExecutor;
import com.top_logic.model.util.JsonValueCodec;
import com.top_logic.model.util.TLModelPartRef;

/**
 * {@link ChannelFactory} that creates mutable value channels.
 *
 * <p>
 * Creates a {@link DefaultViewChannel} for a channel without initialization function, and an
 * {@link InitializedViewChannel} for one with an initialization function, which is compiled once at
 * configuration parse time.
 * </p>
 *
 * <p>
 * A {@link ValueChannelConfig#isPersonalize() personalized} channel is filled with the value
 * remembered in the {@link PersonalConfiguration} after its initialization, and remembers every
 * value written into it afterwards.
 * </p>
 *
 * @implNote The value is stored in the JSON value model form of {@link JsonValueCodec} under the
 *           configured {@link ValueChannelConfig#getPersonalizationKey() personalization key} or,
 *           if none is configured, under the personalization key of the view context extended by
 *           {@link #CHANNEL_SEGMENT} and the channel name. An empty value removes the entry.
 */
public class ValueChannelFactory implements ChannelFactory {

	/**
	 * Personalization key segment of a personalized channel without configured personalization
	 * key, followed by the channel name.
	 */
	public static final String CHANNEL_SEGMENT = "channel";

	private final String _name;

	private final List<InputRef> _inputRefs;

	/**
	 * The compiled initialization function, {@code null} for a channel that starts empty.
	 */
	private final QueryExecutor _init;

	private final List<TLModelPartRef> _observedTypeRefs;

	private final boolean _personalize;

	private final String _personalizationKey;

	/**
	 * Creates a {@link ValueChannelFactory} from configuration.
	 */
	@CalledByReflection
	public ValueChannelFactory(InstantiationContext context, ValueChannelConfig config) {
		_name = config.getName();
		_inputRefs = config.getInputs();
		_observedTypeRefs = config.getObservedTypes();
		_init = InputScript.compile(config, ValueChannelConfig.INIT);
		_personalize = config.isPersonalize();
		_personalizationKey = config.getPersonalizationKey();
		if (_init == null && (!_inputRefs.isEmpty() || !_observedTypeRefs.isEmpty())) {
			context.error("Channel '" + _name + "' declares '" + Inputs.INPUTS + "' or '"
				+ ValueChannelConfig.OBSERVED_TYPES + "' without '" + ValueChannelConfig.INIT
				+ "': they only determine when the initialization function is applied.");
		}
		if (!_personalize && !StringServices.isEmpty(_personalizationKey)) {
			context.error("Channel '" + _name + "' declares '" + ValueChannelConfig.PERSONALIZATION_KEY
				+ "' without '" + ValueChannelConfig.PERSONALIZE + "': the key is used for a personalized channel only.");
		}
	}

	@Override
	public ViewChannel createChannel(ViewContext context) {
		DefaultViewChannel channel;
		if (_init == null) {
			channel = new DefaultViewChannel(_name);
		} else {
			InitializedViewChannel initialized = new InitializedViewChannel(_name);
			initialized.bind(ChannelInputs.resolve(context, _inputRefs), _init::execute,
				ObservedTypes.resolve(_observedTypeRefs));
			channel = initialized;
		}
		if (_personalize) {
			personalize(channel, personalizationKey(context));
		}
		return channel;
	}

	/**
	 * The key the value of the channel is remembered under.
	 */
	private String personalizationKey(ViewContext context) {
		if (!StringServices.isEmpty(_personalizationKey)) {
			return _personalizationKey;
		}
		return context.getPersonalizationKey() + "." + CHANNEL_SEGMENT + "." + _name;
	}

	/**
	 * Fills the given channel with the value remembered under the given key, and remembers every
	 * value written into it afterwards.
	 */
	private void personalize(ViewChannel channel, String key) {
		Object stored = PersonalConfiguration.getPersonalConfiguration().getJSONValue(key);
		if (stored != null) {
			JsonValueCodec.Result remembered = JsonValueCodec.decode(stored);
			if (remembered.isPresent()) {
				// A channel just created has no veto listeners.
				channel.set(remembered.get());
			} else {
				PersonalConfiguration.getPersonalConfiguration().setJSONValue(key, null);
			}
		}
		channel.addListener(new Remember(key));
	}

	/**
	 * {@link ViewChannel.ChannelListener} remembering every value of a personalized channel.
	 */
	private final class Remember implements ViewChannel.ChannelListener {

		private final String _key;

		/**
		 * Whether a value that cannot be remembered has been reported for this channel.
		 */
		private boolean _reported;

		Remember(String key) {
			_key = key;
		}

		@Override
		public void handleNewValue(ViewChannel sender, Object oldValue, Object newValue) {
			JsonValueCodec.Result encoded = JsonValueCodec.encode(newValue);
			if (!encoded.isPresent() && !_reported) {
				_reported = true;
				Logger.warn("Channel '" + _name + "' cannot remember a value of type '"
					+ newValue.getClass().getName() + "' per user under '" + _key + "'.", ValueChannelFactory.class);
			}
			// An empty value and a value that cannot be remembered remove the entry, so that the
			// channel starts with the value of the initialization function in the next session.
			PersonalConfiguration.getPersonalConfiguration().setJSONValue(_key, encoded.orElse(null));
		}
	}
}
