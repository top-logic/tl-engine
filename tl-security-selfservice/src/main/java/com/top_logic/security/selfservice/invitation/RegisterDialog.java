/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.security.selfservice.invitation;

import static com.top_logic.layout.form.template.model.Templates.*;

import java.util.ArrayList;
import java.util.List;

import jakarta.servlet.http.HttpServletRequest;

import com.top_logic.base.accesscontrol.LoginFailuresModule;
import com.top_logic.html.template.HTMLTemplateFragment;
import com.top_logic.html.template.TagTemplate;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.DisplayDimension;
import com.top_logic.layout.DisplayUnit;
import com.top_logic.layout.basic.AbstractCommandModel;
import com.top_logic.layout.basic.CommandModel;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.StringField;
import com.top_logic.layout.messagebox.AbstractTemplateDialog;
import com.top_logic.layout.messagebox.MessageBox;
import com.top_logic.mig.html.HTMLConstants;
import com.top_logic.security.captcha.CaptchaService;
import com.top_logic.security.captcha.form.CaptchaField;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.util.Resources;
import com.top_logic.util.error.TopLogicException;

/**
 * Dialog in which a visitor registers an account without invitation: it asks for the e-mail
 * address and, if the {@link SelfRegistration} {@link SelfRegistration#isCaptchaRequired()
 * requires} it, shows a captcha ({@link CaptchaField}).
 *
 * <p>
 * "Send code" requests a {@link VerificationCode} for the address and replaces the dialog with the
 * code dialog {@link CheckAccountCode} of a {@link RegistrationRequest}, whose account dialog
 * creates the account. The visitor is told the same in every case, whether the address has an
 * account or not. A refusal - registration switched off, captcha not solved, invalid address, a
 * request that follows the previous one too soon - is shown and leaves the dialog open. A captcha
 * solution is accepted once only, so the captcha gets a new challenge after every request.
 * </p>
 *
 * @implNote The code is requested by {@link SelfRegistration#requestCode(String, String, String)}
 *           with the network address of the client, as the login counts failed attempts by it (see
 *           {@link LoginFailuresModule}). The captcha is checked there by the
 *           {@link CaptchaService}.
 */
public class RegisterDialog extends AbstractTemplateDialog {

	private static final String EMAIL_FIELD = "email";

	private static final String CAPTCHA_FIELD = "captcha";

	private final String _email;

	private final boolean _captcha;

	/**
	 * Creates a {@link RegisterDialog}.
	 *
	 * @param email
	 *        The address to fill in, <code>null</code> for none.
	 */
	public RegisterDialog(String email) {
		super(I18NConstants.REGISTER_DIALOG_TITLE,
			DisplayDimension.dim(450, DisplayUnit.PIXEL),
			DisplayDimension.dim(330, DisplayUnit.PIXEL));
		_email = email;
		_captcha = SelfRegistration.getInstance().isCaptchaRequired();
	}

	@Override
	protected void fillFormContext(FormContext context) {
		StringField email = FormFactory.newStringField(EMAIL_FIELD);
		email.setLabel(I18NConstants.REGISTER_EMAIL_FIELD);
		email.setMandatory(true);
		email.setTransient(true);
		email.initializeField(_email);
		context.addMember(email);

		if (_captcha) {
			context.addMember(CaptchaField.newCaptchaField(CAPTCHA_FIELD));
		}
	}

	@Override
	protected TagTemplate getTemplate() {
		List<HTMLTemplateFragment> fragments = new ArrayList<>();
		fragments.add(tag(HTMLConstants.PARAGRAPH, resource(I18NConstants.REGISTER_MESSAGE)));
		fragments.add(fieldBox(EMAIL_FIELD));
		if (_captcha) {
			fragments.add(div(member(CAPTCHA_FIELD)));
		}
		return div(fragments);
	}

	@Override
	protected void fillButtons(List<CommandModel> buttons) {
		buttons.add(MessageBox.button(I18NConstants.REGISTER_SEND_CODE,
			checkContextCommand().andThen(this::requestCode)));
		addCancel(buttons);
	}

	private HandlerResult requestCode(DisplayContext context) {
		FormContext formContext = getFormContext();
		String email = ((StringField) formContext.getField(EMAIL_FIELD)).getAsString().strip();
		FormField captcha = _captcha ? formContext.getField(CAPTCHA_FIELD) : null;

		VerificationCode code;
		try {
			code = SelfRegistration.getInstance().requestCode(email,
				captcha == null ? null : CaptchaField.getPayload(captcha), clientAddress(context));
		} catch (TopLogicException ex) {
			HandlerResult error = new HandlerResult();
			error.setException(ex);
			return error;
		} finally {
			if (captcha != null) {
				CaptchaField.reset(captcha);
			}
		}

		getDiscardClosure().executeCommand(context);
		return CheckAccountCode.forCode(new RegistrationRequest(email), code).open(context);
	}

	private static String clientAddress(DisplayContext context) {
		HttpServletRequest request = context.asRequest();
		return request == null ? null : request.getRemoteAddr();
	}

	/**
	 * Command of the login dialog opening a {@link RegisterDialog}, visible while the
	 * {@link SelfRegistration} is {@link SelfRegistration#isEnabled() enabled}.
	 */
	public static class Opener extends AbstractCommandModel {

		/**
		 * Creates a new {@link Opener}.
		 */
		public Opener() {
			setLabel(Resources.getInstance().getString(I18NConstants.REGISTER_COMMAND));
			setImage(Icons.REGISTER);
			setVisible(SelfRegistration.getInstance().isEnabled());
		}

		@Override
		protected HandlerResult internalExecuteCommand(DisplayContext context) {
			return new RegisterDialog(null).open(context);
		}

	}

}
