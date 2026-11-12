/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.security.captcha.form;

import static com.top_logic.mig.html.HTMLConstants.*;

import java.io.IOException;
import java.util.Map;

import com.top_logic.basic.json.JSON;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.TagUtil;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.ControlCommand;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.control.AbstractFormFieldControl;
import com.top_logic.layout.form.template.AbstractFormFieldControlProvider;
import com.top_logic.layout.form.template.ControlProvider;
import com.top_logic.mig.html.HTMLUtil;
import com.top_logic.security.captcha.CaptchaService;
import com.top_logic.security.captcha.CaptchaStart;
import com.top_logic.security.captcha.CaptchaWidget;
import com.top_logic.security.captcha.I18NConstants;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.util.Resources;

/**
 * {@link Control} showing the proof-of-work captcha of the {@link CaptchaService} (ALTCHA) for a
 * {@link CaptchaField} in the classic UI.
 *
 * <p>
 * The control renders the {@link CaptchaWidget ALTCHA widget} with a new challenge of the
 * {@link CaptchaService}; the widget fetches nothing itself. When the widget has solved the
 * challenge, the client script <code>services.captcha</code> (<code>tl-security-captcha.js</code>)
 * sends the solution as new value of the field; a solution the widget gives up again clears the
 * field. When the challenge expires, the client sends {@link #CMD_EXPIRED}: the field is cleared and
 * the control renders a new challenge.
 * </p>
 *
 * <p>
 * Outside a secure context (plain HTTP from a host other than <code>localhost</code>), the client
 * shows the text {@link CaptchaWidget#TEXT_INSECURE_CONTEXT} as error instead of the widget, since
 * the widget needs the Web Crypto API of the browser.
 * </p>
 *
 * <p>
 * Each rendering shows a new challenge, which the widget solves right away with the default
 * {@link CaptchaStart#LOAD start}. A value set on the server - e.g. clearing the field after its
 * solution has been used up - renders the control anew, which gives the widget a new challenge.
 * </p>
 */
public class CaptchaFieldControl extends AbstractFormFieldControl {

	/** Command sent when the challenge has expired, without arguments. */
	public static final String CMD_EXPIRED = "captchaExpired";

	/** CSS class of the control element. */
	private static final String CSS_CLASS = "tlCaptcha";

	/**
	 * The commands of a {@link CaptchaFieldControl}: those of a form field, the value transmitted
	 * with the standard command <code>valueChanged</code>, and {@link Expired}.
	 */
	protected static final Map<String, ControlCommand> COMMANDS =
		createCommandMap(AbstractFormFieldControl.COMMANDS, new ControlCommand[] { Expired.INSTANCE });

	/**
	 * {@link ControlProvider} creating {@link CaptchaFieldControl}s with the default
	 * {@link CaptchaStart#LOAD start}.
	 */
	public static final ControlProvider CP = new Provider(CaptchaStart.LOAD);

	private final CaptchaStart _start;

	/**
	 * Creates a {@link CaptchaFieldControl}.
	 *
	 * @param field
	 *        The field receiving the solution of the captcha.
	 * @param start
	 *        When the browser starts to solve the challenge.
	 */
	public CaptchaFieldControl(FormField field, CaptchaStart start) {
		super(field, COMMANDS);
		_start = start;
	}

	@Override
	protected String getTypeCssClass() {
		return CSS_CLASS;
	}

	@Override
	protected void writeEditable(DisplayContext context, TagWriter out) throws IOException {
		Resources resources = Resources.getInstance();
		String language = resources.getLocale().getLanguage();

		out.beginBeginTag(DIV);
		writeControlAttributes(context, out);
		out.endBeginTag();
		{
			out.beginBeginTag(CaptchaWidget.TAG);
			out.writeAttribute(CaptchaWidget.CHALLENGE_ATTR, CaptchaService.getInstance().createChallenge());
			out.writeAttribute(CaptchaWidget.LANGUAGE_ATTR, language);
			out.writeAttribute(CaptchaWidget.AUTO_ATTR, _start.getWidgetValue());
			out.writeAttribute(CaptchaWidget.CONFIGURATION_ATTR, CaptchaWidget.configuration());
			out.endBeginTag();
			out.endTag(CaptchaWidget.TAG);
		}
		out.endTag(DIV);

		HTMLUtil.beginScriptAfterRendering(out);
		out.append("services.captcha.init(");
		TagUtil.writeJsString(out, getID());
		out.append(", ");
		TagUtil.writeJsString(out, language);
		out.append(", ");
		JSON.write(out, CaptchaWidget.texts(resources));
		out.append(");");
		HTMLUtil.endScriptAfterRendering(out);
	}

	@Override
	protected void writeImmutable(DisplayContext context, TagWriter out) throws IOException {
		out.beginBeginTag(SPAN);
		writeControlAttributes(context, out);
		out.endBeginTag();
		out.endTag(SPAN);
	}

	@Override
	protected void internalHandleValueChanged(FormField field, Object oldValue, Object newValue) {
		requestRepaint();
	}

	@Override
	protected void internalHandleDisabledEvent(FormMember sender, Boolean oldValue, Boolean newValue) {
		requestRepaint();
	}

	/**
	 * Clears the field and shows a new challenge.
	 */
	void handleExpired() {
		FormField field = getFieldModel();
		if (field.getValue() != null) {
			// Renders a new challenge through internalHandleValueChanged().
			field.setValue(null);
		} else {
			requestRepaint();
		}
	}

	/**
	 * {@link ControlProvider} creating {@link CaptchaFieldControl}s.
	 */
	public static class Provider extends AbstractFormFieldControlProvider {

		private final CaptchaStart _start;

		/**
		 * Creates a {@link Provider}.
		 *
		 * @param start
		 *        When the browser starts to solve the challenge.
		 */
		public Provider(CaptchaStart start) {
			_start = start;
		}

		@Override
		protected Control createInput(FormMember member) {
			return new CaptchaFieldControl((FormField) member, _start);
		}
	}

	/**
	 * {@link ControlCommand} telling that the challenge has expired.
	 */
	static class Expired extends ControlCommand {

		/** Singleton {@link Expired} instance. */
		static final Expired INSTANCE = new Expired();

		private Expired() {
			super(CMD_EXPIRED);
		}

		@Override
		protected HandlerResult execute(DisplayContext commandContext, Control control,
				Map<String, Object> arguments) {
			((CaptchaFieldControl) control).handleExpired();
			return HandlerResult.DEFAULT_RESULT;
		}

		@Override
		public ResKey getI18NKey() {
			return I18NConstants.CAPTCHA_EXPIRED_COMMAND;
		}

		@Override
		protected boolean executeCommandIfViewDisabled() {
			return true;
		}
	}

}
