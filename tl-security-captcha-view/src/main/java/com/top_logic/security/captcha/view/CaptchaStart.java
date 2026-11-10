/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.security.captcha.view;

import com.top_logic.basic.config.annotation.Name;

/**
 * When the browser starts to solve the challenge of a {@link CaptchaElement}.
 */
public enum CaptchaStart {

	/**
	 * As soon as the captcha is displayed, without any action of the visitor.
	 */
	@Name("load")
	LOAD("onload"),

	/**
	 * When the visitor focuses a field of the form the captcha belongs to.
	 */
	@Name("focus")
	FOCUS("onfocus"),

	/**
	 * When the visitor checks the box of the captcha.
	 */
	@Name("click")
	CLICK("off"),

	;

	private final String _widgetValue;

	private CaptchaStart(String widgetValue) {
		_widgetValue = widgetValue;
	}

	/**
	 * The value of the option <code>auto</code> of the ALTCHA widget.
	 */
	public String getWidgetValue() {
		return _widgetValue;
	}

}
