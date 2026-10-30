/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.security.passkey.view;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.listener.Registration;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.react.control.IReactControl;
import com.top_logic.layout.react.control.button.ButtonAppearance;
import com.top_logic.layout.react.control.webauthn.ReactWebAuthnControl;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.ChannelRef;
import com.top_logic.layout.view.channel.ChannelRefFormat;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.util.Resources;

/**
 * Base class of a {@link UIElement} showing a button that runs a {@link PasskeyCeremony} in the
 * browser, for the account on an optional channel.
 *
 * @param <C>
 *        The configuration type.
 * @param <K>
 *        The type of ceremony.
 */
public abstract class PasskeyCeremonyElement<C extends PasskeyCeremonyElement.Config, K extends PasskeyCeremony<?, ?>>
		implements UIElement {

	/**
	 * Configuration of a {@link PasskeyCeremonyElement}.
	 */
	public interface Config extends UIElement.Config {

		/** Configuration name for {@link #getLabel()}. */
		String LABEL = "label";

		/** Configuration name for {@link #getImage()}. */
		String IMAGE = "image";

		/** Configuration name for {@link #getAppearance()}. */
		String APPEARANCE = "appearance";

		/** Configuration name for {@link #isAutoStart()}. */
		String AUTO_START = "auto-start";

		/** Configuration name for {@link #getAccount()}. */
		String ACCOUNT = "account";

		/**
		 * The label of the button.
		 */
		@Name(LABEL)
		@Mandatory
		ResKey getLabel();

		/**
		 * The icon of the button.
		 */
		@Name(IMAGE)
		@Nullable
		ThemeImage getImage();

		/**
		 * The appearance of the button, e.g. {@link ButtonAppearance#LINK} for an inline link.
		 */
		@Name(APPEARANCE)
		ButtonAppearance getAppearance();

		/**
		 * Whether the browser asks for the passkey as soon as the element is displayed, without a
		 * click on the button.
		 *
		 * <p>
		 * A browser that only accepts the request after a click of the user refuses the automatic
		 * start silently; the button stays available.
		 * </p>
		 */
		@Name(AUTO_START)
		boolean isAutoStart();

		/**
		 * Channel holding the account the passkey ceremony is run for.
		 *
		 * <p>
		 * The channel must be filled by the server, e.g. the login, not by an input of the user.
		 * While it holds no account, the button is disabled.
		 * </p>
		 */
		@Name(ACCOUNT)
		@Nullable
		@Format(ChannelRefFormat.class)
		ChannelRef getAccount();
	}

	private final C _config;

	/**
	 * Creates a {@link PasskeyCeremonyElement} from configuration.
	 *
	 * @param context
	 *        The context for reporting configuration errors.
	 * @param config
	 *        The configuration.
	 */
	public PasskeyCeremonyElement(InstantiationContext context, C config) {
		_config = config;
	}

	/**
	 * The configuration of this element.
	 */
	public C getConfig() {
		return _config;
	}

	@Override
	public IReactControl createControl(ViewContext context) {
		K ceremony = createCeremony(context);
		ReactWebAuthnControl control =
			ceremony.createControl(context, Resources.getInstance().getString(_config.getLabel()),
				_config.isAutoStart());
		control.setImage(_config.getImage());
		control.setAppearance(_config.getAppearance());
		control.setCssClass(_config.getCssClass());

		ChannelRef accountRef = _config.getAccount();
		if (accountRef == null) {
			offer(ceremony, null);
		} else {
			ViewChannel channel = context.resolveChannel(accountRef);
			offer(ceremony, channel.get());
			Registration registration =
				channel.addListener((sender, oldValue, newValue) -> offer(ceremony, newValue));
			control.addCleanupAction(registration::dispose);
		}
		return control;
	}

	/**
	 * Creates the ceremony of a control of this element.
	 *
	 * @param context
	 *        The context of the control.
	 */
	protected abstract K createCeremony(ViewContext context);

	/**
	 * {@link PasskeyCeremony#offer(com.top_logic.knowledge.wrap.person.Person) Offers} the ceremony
	 * for the given value of the {@link Config#getAccount() account channel}, or
	 * {@link PasskeyCeremony#withdraw(ResKey) withdraws} it.
	 *
	 * @param ceremony
	 *        The ceremony of the control.
	 * @param accountValue
	 *        The value of the account channel; <code>null</code> also if this element has none.
	 */
	protected abstract void offer(K ceremony, Object accountValue);

}
