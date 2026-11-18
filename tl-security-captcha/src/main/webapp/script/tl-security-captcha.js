/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */

/**
 * Client side of the classic captcha field (CaptchaFieldControl): connects the ALTCHA web component
 * <altcha-widget> (script /script/altcha.js) with the form field on the server.
 */
services.captcha = {

	/** The command transmitting the value of a form field (AbstractFormFieldControlBase.ValueChanged). */
	CMD_VALUE_CHANGED: "valueChanged",

	/** The argument of CMD_VALUE_CHANGED carrying the value. */
	ARG_VALUE: "value",

	/** The command telling that the challenge has expired (CaptchaFieldControl.CMD_EXPIRED). */
	CMD_EXPIRED: "captchaExpired",

	/** The tag of the ALTCHA web component (CaptchaWidget.TAG). */
	WIDGET_TAG: "altcha-widget",

	/** The widget state of a solved challenge. */
	STATE_VERIFIED: "verified",

	/** The text shown instead of the widget outside a secure context (CaptchaWidget.TEXT_INSECURE_CONTEXT). */
	TEXT_INSECURE_CONTEXT: "insecureContext",

	/** CSS class of the hint shown instead of the widget, the class of error texts (FormConstants.ERROR_CSS_CLASS). */
	ERROR_CSS_CLASS: "error",

	/**
	 * Connects the widget of a captcha field with the server.
	 *
	 * When the widget has solved its challenge, the solution becomes the value of the field. When
	 * the widget leaves the solved state again (reset, error), the field is cleared. When the
	 * challenge expires, the server is told and renders the field anew with a new challenge.
	 *
	 * Outside a secure context (plain HTTP from a host other than localhost), the browser offers no
	 * Web Crypto API, so the widget cannot solve anything: it is replaced by a hint.
	 *
	 * @param {String} controlID The ID of the control element containing the widget.
	 * @param {String} language The language the texts are registered for.
	 * @param {Object} strings The texts of the widget by their ALTCHA names.
	 */
	init: function(controlID, language, strings) {
		var control = document.getElementById(controlID);
		if (control == null) {
			return;
		}
		var widget = control.querySelector(this.WIDGET_TAG);
		if (widget == null) {
			return;
		}

		if (!window.isSecureContext) {
			var hint = document.createElement("span");
			hint.className = this.ERROR_CSS_CLASS;
			hint.textContent = strings[this.TEXT_INSECURE_CONTEXT];
			widget.replaceWith(hint);
			return;
		}

		var self = this;
		customElements.whenDefined(this.WIDGET_TAG).then(function() {
			self.registerStrings(language, strings);
		});

		var sent = false;
		widget.addEventListener("statechange", function(event) {
			var detail = event.detail || {};
			if (detail.state === self.STATE_VERIFIED && detail.payload) {
				sent = true;
				self.sendValue(controlID, detail.payload);
			} else if (sent) {
				sent = false;
				self.sendValue(controlID, "");
			}
		});
		widget.addEventListener("expired", function() {
			sent = false;
			services.ajax.execute("dispatchControlCommand", {
				controlCommand: self.CMD_EXPIRED,
				controlID: controlID
			}, false);
		});
	},

	/**
	 * Registers the texts of the widget for the given language, on top of the English texts the
	 * widget ships.
	 */
	registerStrings: function(language, strings) {
		var altcha = globalThis.$altcha;
		if (altcha == null || strings == null) {
			return;
		}
		altcha.i18n.set(language, Object.assign({}, altcha.i18n.get("en") || {}, strings));
	},

	sendValue: function(controlID, value) {
		var args = {
			controlCommand: this.CMD_VALUE_CHANGED,
			controlID: controlID
		};
		args[this.ARG_VALUE] = value;
		services.ajax.execute("dispatchControlCommand", args, false);
	}
};
