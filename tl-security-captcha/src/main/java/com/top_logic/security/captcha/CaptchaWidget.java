/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.security.captcha;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import com.top_logic.basic.json.JSON;
import com.top_logic.basic.util.ResKey;
import com.top_logic.util.Resources;

/**
 * The ALTCHA widget in the browser, the web component that solves the challenges of the
 * {@link CaptchaService}: its names and the settings every captcha of the application gives it.
 *
 * <p>
 * The web component is the script <code>/script/altcha.js</code> of this module, the file
 * <code>dist/main/altcha.min.js</code> of the npm package <code>altcha</code>, copied by the
 * build. It defines the element {@link #TAG} and is shared by all user interfaces showing a
 * captcha.
 * </p>
 */
public final class CaptchaWidget {

	/** The tag of the ALTCHA web component. */
	public static final String TAG = "altcha-widget";

	/** Attribute of {@link #TAG} taking the challenge, the JSON of {@link CaptchaService#createChallenge()}. */
	public static final String CHALLENGE_ATTR = "challenge";

	/** Attribute of {@link #TAG} taking the language code the widget shows its texts in. */
	public static final String LANGUAGE_ATTR = "language";

	/** Attribute of {@link #TAG} taking {@link CaptchaStart#getWidgetValue()}. */
	public static final String AUTO_ATTR = "auto";

	/** Attribute of {@link #TAG} taking the {@link #configuration() configuration} as JSON. */
	public static final String CONFIGURATION_ATTR = "configuration";

	/** Option of the widget hiding the attribution link to the ALTCHA website. */
	public static final String OPTION_HIDE_FOOTER = "hideFooter";

	/** Option of the widget hiding the ALTCHA logo. */
	public static final String OPTION_HIDE_LOGO = "hideLogo";

	/**
	 * Option of the widget collecting pointer and scroll events, which it sends only to a server
	 * fetching challenges by URL.
	 */
	public static final String OPTION_HUMAN_INTERACTION_SIGNATURE = "humanInteractionSignature";

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

	/**
	 * Name of the text shown instead of the widget when the page is not a secure context (served
	 * over plain HTTP from a host other than <code>localhost</code>).
	 *
	 * <p>
	 * The widget computes its proof of work with the Web Crypto API, which the browser offers in a
	 * secure context only (<code>window.isSecureContext</code>). This text is not one of the ALTCHA
	 * texts; the user interfaces show it in place of the widget.
	 * </p>
	 */
	public static final String TEXT_INSECURE_CONTEXT = "insecureContext";

	private static final Map<String, Object> CONFIGURATION;

	static {
		Map<String, Object> configuration = new LinkedHashMap<>();
		configuration.put(OPTION_HIDE_FOOTER, Boolean.TRUE);
		configuration.put(OPTION_HIDE_LOGO, Boolean.TRUE);
		configuration.put(OPTION_HUMAN_INTERACTION_SIGNATURE, Boolean.FALSE);
		CONFIGURATION = Collections.unmodifiableMap(configuration);
	}

	private CaptchaWidget() {
		// Constants only.
	}

	/**
	 * The configuration of the widget, the value of {@link #CONFIGURATION_ATTR}.
	 *
	 * <p>
	 * It keeps the widget from reaching anything outside the application and shows it without the
	 * ALTCHA branding: no attribution link ({@link #OPTION_HIDE_FOOTER}), no logo
	 * ({@link #OPTION_HIDE_LOGO}), no collection of pointer events
	 * ({@link #OPTION_HUMAN_INTERACTION_SIGNATURE}).
	 * </p>
	 *
	 * @return The configuration as JSON object.
	 */
	public static String configuration() {
		return JSON.toString(CONFIGURATION);
	}

	/**
	 * The texts of the widget in the language of the given resources.
	 *
	 * @param resources
	 *        The resources of the user.
	 * @return The texts by their ALTCHA names, registered with the widget for the language
	 *         ({@link Resources#getLocale()}), and the text {@link #TEXT_INSECURE_CONTEXT}.
	 */
	public static Map<String, String> texts(Resources resources) {
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
		put(result, resources, TEXT_INSECURE_CONTEXT, I18NConstants.WIDGET_INSECURE_CONTEXT);
		return result;
	}

	private static void put(Map<String, String> texts, Resources resources, String name, ResKey key) {
		texts.put(name, resources.getString(key));
	}

}
