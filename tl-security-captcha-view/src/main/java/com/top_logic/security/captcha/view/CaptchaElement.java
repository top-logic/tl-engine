/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.security.captcha.view;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.TypeIcon;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.layout.react.control.IReactControl;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.ChannelRef;
import com.top_logic.layout.view.channel.ChannelRefFormat;
import com.top_logic.security.captcha.CaptchaService;
import com.top_logic.security.captcha.CaptchaStart;

/**
 * {@link UIElement} showing the proof-of-work captcha of the {@link CaptchaService} (ALTCHA) in a
 * form an anonymous visitor submits.
 *
 * <p>
 * The browser solves a challenge of the service and the element writes the solution into the
 * {@link Config#getPayload() payload channel}. The action submitting the form reads the channel
 * and lets the {@link CaptchaService} check the solution. An action clearing the channel after the
 * check gets a new challenge in the element, since a solution is accepted once only.
 * </p>
 *
 * <pre>
 * &lt;channels&gt;
 *   &lt;channel name="captcha"/&gt;
 * &lt;/channels&gt;
 * ...
 * &lt;captcha payload="captcha"/&gt;
 * </pre>
 *
 * @see CaptchaControl
 * @see CaptchaService#check(String)
 */
@InApp
public class CaptchaElement implements UIElement {

	/**
	 * Configuration for {@link CaptchaElement}.
	 */
	@TagName("captcha")
	@TypeIcon("css:bi bi-shield-check")
	public interface Config extends UIElement.Config {

		/** Configuration name for {@link #getPayload()}. */
		String PAYLOAD = "payload";

		/** Configuration name for {@link #getStart()}. */
		String START = "start";

		@Override
		@ClassDefault(CaptchaElement.class)
		Class<? extends UIElement> getImplementationClass();

		/**
		 * The channel receiving the solution of the captcha.
		 *
		 * <p>
		 * The value is the solution as the ALTCHA widget submits it, the text the
		 * {@link CaptchaService} checks; <code>null</code> while the captcha is not solved.
		 * Clearing the channel issues a new challenge.
		 * </p>
		 *
		 * @see CaptchaService#check(String)
		 */
		@Name(PAYLOAD)
		@Mandatory
		@Format(ChannelRefFormat.class)
		ChannelRef getPayload();

		/**
		 * When the browser starts to solve the challenge.
		 */
		@Name(START)
		CaptchaStart getStart();
	}

	private final Config _config;

	/**
	 * Creates a new {@link CaptchaElement} from configuration.
	 */
	@CalledByReflection
	public CaptchaElement(InstantiationContext context, Config config) {
		_config = config;
	}

	@Override
	public IReactControl createControl(ViewContext context) {
		CaptchaControl control =
			new CaptchaControl(context, context.resolveChannel(_config.getPayload()), _config.getStart());
		control.setCssClass(_config.getCssClass());
		return control;
	}

}
