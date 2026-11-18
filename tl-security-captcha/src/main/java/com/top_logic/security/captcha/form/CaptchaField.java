/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.security.captcha.form;

import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.StringField;
import com.top_logic.security.captcha.CaptchaService;
import com.top_logic.security.captcha.CaptchaStart;

/**
 * Form field of the classic UI holding the solution of the proof-of-work captcha of the
 * {@link CaptchaService} (ALTCHA).
 *
 * <p>
 * The field is a {@link StringField} displayed by a {@link CaptchaFieldControl}: the browser solves
 * a challenge of the service and the solution becomes the value of the field. The command
 * submitting the form reads it with {@link #getPayload(FormField)}, lets the service
 * {@link CaptchaService#check(String) check} it, and {@link #reset(FormField) resets} the field
 * afterwards, since a solution is accepted once only. The reset gives the widget a new challenge.
 * </p>
 *
 * <pre>
 * context.addMember(CaptchaField.newCaptchaField("captcha"));
 * ...
 * try {
 *     CaptchaService.getInstance().check(CaptchaField.getPayload(field));
 *     ...
 * } finally {
 *     CaptchaField.reset(field);
 * }
 * </pre>
 */
public final class CaptchaField {

	private CaptchaField() {
		// Static utilities only.
	}

	/**
	 * Creates a captcha field that starts solving as soon as it is displayed.
	 *
	 * @param name
	 *        The name of the field.
	 * @return The new field, not mandatory, so that a missing solution is reported by
	 *         {@link CaptchaService#check(String)}.
	 */
	public static StringField newCaptchaField(String name) {
		return newCaptchaField(name, CaptchaStart.LOAD);
	}

	/**
	 * Creates a captcha field.
	 *
	 * @param name
	 *        The name of the field.
	 * @param start
	 *        When the browser starts to solve the challenge.
	 * @return The new field, not mandatory, so that a missing solution is reported by
	 *         {@link CaptchaService#check(String)}.
	 */
	public static StringField newCaptchaField(String name, CaptchaStart start) {
		StringField field = FormFactory.newStringField(name);
		field.setTransient(true);
		field.setControlProvider(
			start == CaptchaStart.LOAD ? CaptchaFieldControl.CP : new CaptchaFieldControl.Provider(start));
		return field;
	}

	/**
	 * The solution of the captcha in the given field.
	 *
	 * @return The solution as the ALTCHA widget submits it, the text
	 *         {@link CaptchaService#check(String)} takes; <code>null</code> while the captcha is not
	 *         solved.
	 */
	public static String getPayload(FormField field) {
		Object value = field.getValue();
		return value == null ? null : value.toString();
	}

	/**
	 * Clears the solution of the given field, which gives the widget a new challenge.
	 *
	 * <p>
	 * Called after the solution has been checked, whether the check succeeded or not: a solution is
	 * accepted once only.
	 * </p>
	 */
	public static void reset(FormField field) {
		field.setValue(null);
	}

}
