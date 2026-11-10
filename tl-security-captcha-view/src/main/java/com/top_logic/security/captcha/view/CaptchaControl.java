/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.security.captcha.view;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import com.top_logic.basic.listener.Registration;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactCommandHandler;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.security.captcha.CaptchaService;
import com.top_logic.util.Resources;

/**
 * The ALTCHA proof-of-work captcha in the browser, writing its solution into a {@link ViewChannel}.
 *
 * <p>
 * On the client side, this control renders a <code>TLCaptcha</code> React component, which shows
 * the ALTCHA widget with a challenge of the {@link CaptchaService} ({@link #CHALLENGE}). The widget
 * fetches nothing itself. When it has solved the challenge, the solution arrives with the
 * {@link #CMD_SOLVED} command and is written into the payload channel, where an action reads it and
 * passes it to {@link CaptchaService#check(String)}. A solution the widget gives up again clears the
 * channel.
 * </p>
 *
 * <p>
 * A new challenge is issued when the control is created, when the client reports with
 * {@link #CMD_EXPIRED} that the challenge has expired, and when someone else writes the payload
 * channel, e.g. an action clearing it after it has used up the solution. The new challenge resets
 * the widget.
 * </p>
 *
 * <p>
 * State keys: {@link #CHALLENGE}, {@link #LANGUAGE}, {@link #STRINGS}, {@link #AUTO}.
 * </p>
 */
public class CaptchaControl extends ReactControl {

	/** The React component rendering this control. */
	private static final String REACT_MODULE = "TLCaptcha";

	/** State key for the challenge, the JSON text of {@link CaptchaService#createChallenge()}. */
	public static final String CHALLENGE = "challenge";

	/** State key for the language code the {@link #STRINGS} are registered for in the widget. */
	public static final String LANGUAGE = "language";

	/** State key for the texts of the widget, by the names of the ALTCHA texts. */
	public static final String STRINGS = "strings";

	/** State key for the {@link CaptchaStart#getWidgetValue() start} of the solving. */
	public static final String AUTO = "auto";

	/** Command sent with the solution of the challenge, see {@link CaptchaSolvedArguments}. */
	public static final String CMD_SOLVED = "solved";

	/** Command sent when the challenge has expired, without arguments. */
	public static final String CMD_EXPIRED = "expired";

	/** Name of the ALTCHA text of the check box. */
	public static final String TEXT_LABEL = "label";

	/** Name of the ALTCHA text shown while a challenge is fetched. */
	public static final String TEXT_LOADING = "loading";

	/** Name of the ALTCHA text shown while the challenge is solved. */
	public static final String TEXT_VERIFYING = "verifying";

	/** Name of the ALTCHA text shown when the challenge is solved. */
	public static final String TEXT_VERIFIED = "verified";

	/** Name of the ALTCHA text shown when a form is submitted without solution. */
	public static final String TEXT_VERIFICATION_REQUIRED = "verificationRequired";

	/** Name of the ALTCHA text shown when a form is submitted while the challenge is solved. */
	public static final String TEXT_WAIT_ALERT = "waitAlert";

	/** Name of the ALTCHA text shown when solving failed. */
	public static final String TEXT_ERROR = "error";

	/** Name of the ALTCHA text shown when the challenge has expired. */
	public static final String TEXT_EXPIRED = "expired";

	/** Name of the ALTCHA text of the button reloading a challenge. */
	public static final String TEXT_RELOAD = "reload";

	/** Name of the ALTCHA text of the button starting the solving. */
	public static final String TEXT_VERIFY = "verify";

	private final ViewChannel _payload;

	/** The value this control wrote into {@link #_payload} last. */
	private Object _written;

	/**
	 * Creates a {@link CaptchaControl}.
	 *
	 * @param context
	 *        The context of the control.
	 * @param payload
	 *        The channel receiving the solution, <code>null</code> while there is none.
	 * @param start
	 *        When the browser starts to solve the challenge.
	 */
	public CaptchaControl(ReactContext context, ViewChannel payload, CaptchaStart start) {
		super(context, null, REACT_MODULE);
		_payload = payload;
		_written = payload.get();

		Resources resources = Resources.getInstance();
		putState(LANGUAGE, resources.getLocale().getLanguage());
		putState(STRINGS, texts(resources));
		putState(AUTO, start.getWidgetValue());
		newChallenge();

		Registration registration = payload.addListener(this::onPayloadChanged);
		addCleanupAction(registration::dispose);
	}

	private static Map<String, String> texts(Resources resources) {
		Map<String, String> result = new LinkedHashMap<>();
		put(result, resources, TEXT_LABEL, I18NConstants.WIDGET_LABEL);
		put(result, resources, TEXT_LOADING, I18NConstants.WIDGET_LOADING);
		put(result, resources, TEXT_VERIFYING, I18NConstants.WIDGET_VERIFYING);
		put(result, resources, TEXT_VERIFIED, I18NConstants.WIDGET_VERIFIED);
		put(result, resources, TEXT_VERIFICATION_REQUIRED, I18NConstants.WIDGET_VERIFICATION_REQUIRED);
		put(result, resources, TEXT_WAIT_ALERT, I18NConstants.WIDGET_WAIT_ALERT);
		put(result, resources, TEXT_ERROR, I18NConstants.WIDGET_ERROR);
		put(result, resources, TEXT_EXPIRED, I18NConstants.WIDGET_EXPIRED);
		put(result, resources, TEXT_RELOAD, I18NConstants.WIDGET_RELOAD);
		put(result, resources, TEXT_VERIFY, I18NConstants.WIDGET_VERIFY);
		return result;
	}

	private static void put(Map<String, String> texts, Resources resources, String name, ResKey key) {
		texts.put(name, resources.getString(key));
	}

	/**
	 * Gives the widget a new challenge, which resets it.
	 */
	private void newChallenge() {
		putState(CHALLENGE, CaptchaService.getInstance().createChallenge());
	}

	/**
	 * A value written by someone else - e.g. the payload cleared after it was used - asks for a new
	 * challenge.
	 */
	private void onPayloadChanged(ViewChannel sender, Object oldValue, Object newValue) {
		if (Objects.equals(newValue, _written)) {
			return;
		}
		_written = newValue;
		newChallenge();
	}

	private void writePayload(String payload) {
		_written = payload;
		_payload.set(payload);
	}

	/**
	 * Handles the solution of the challenge, or the loss of the solution.
	 */
	@ReactCommandHandler(value = CMD_SOLVED, technical = true)
	void handleSolved(ReactContext context, CaptchaSolvedArguments args) {
		writePayload(args.getPayload());
	}

	/**
	 * Handles the expiry of the challenge: clears the solution and issues a new challenge.
	 */
	@ReactCommandHandler(value = CMD_EXPIRED, technical = true)
	void handleExpired(ReactContext context) {
		writePayload(null);
		newChallenge();
	}

}
