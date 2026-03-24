/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */

package com.top_logic.layout.component.configuration;

import static com.top_logic.layout.form.template.model.Templates.*;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import jakarta.servlet.http.HttpServletRequest;

import com.top_logic.base.accesscontrol.Login;
import com.top_logic.base.accesscontrol.Login.InMaintenanceModeException;
import com.top_logic.base.accesscontrol.Login.LoginHookFailedException;
import com.top_logic.base.accesscontrol.LoginFailure;
import com.top_logic.base.accesscontrol.LoginFailuresModule;
import com.top_logic.base.accesscontrol.SessionService;
import com.top_logic.base.security.util.Password;
import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.misc.TypedConfigUtil;
import com.top_logic.basic.util.ResKey;
import com.top_logic.html.template.TagTemplate;
import com.top_logic.knowledge.wrap.person.MfaRequirement;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.DisplayDimension;
import com.top_logic.layout.DisplayUnit;
import com.top_logic.layout.basic.Command;
import com.top_logic.layout.basic.CommandModel;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.PasswordField;
import com.top_logic.layout.form.model.StringField;
import com.top_logic.layout.messagebox.AbstractTemplateDialog;
import com.top_logic.layout.messagebox.MessageBox;
import com.top_logic.layout.structure.DialogModel;
import com.top_logic.layout.toolbar.ToolBar;
import com.top_logic.mig.html.layout.MainLayout;
import com.top_logic.tool.boundsec.CommandHandlerFactory;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * Dialog to login a user.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class LoginViewDialog extends AbstractTemplateDialog {

	private static final String USERNAME_FIELD = "username";

	private static final String PASSWORD_FIELD = "password";

	/**
	 * Static configuration for {@link LoginViewDialog}.
	 */
	@Label("Login view configuration")
	public interface Config extends ConfigurationItem {

		/**
		 * The commands to display in the toolbar of the login dialog.
		 */
		List<PolymorphicConfiguration<? extends CommandModel>> getToolbarCommands();
	}

	/**
	 * Creates a new {@link LoginViewDialog} with default values.
	 */
	public LoginViewDialog() {
		this(I18NConstants.LOGIN,
			DisplayDimension.dim(400, DisplayUnit.PIXEL),
			DisplayDimension.dim(300, DisplayUnit.PIXEL));
	}

	/**
	 * Creates a new {@link LoginViewDialog}.
	 */
	public LoginViewDialog(DialogModel dialogModel) {
		super(dialogModel);
		initToolbarCommands();
	}

	/**
	 * Creates a new {@link LoginViewDialog}.
	 */
	public LoginViewDialog(ResKey dialogTitle, DisplayDimension width, DisplayDimension height) {
		super(dialogTitle, width, height);
		initToolbarCommands();
	}

	private void initToolbarCommands() {
		Config config = ApplicationConfig.getInstance().getConfig(Config.class);
		List<CommandModel> commands = TypedConfigUtil.createInstanceList(config.getToolbarCommands());
		if (commands.isEmpty()) {
			return;
		}
		DialogModel dialogModel = getDialogModel();
		ToolBar toolbar = dialogModel.getToolbar();
		toolbar.defineGroup(CommandHandlerFactory.ADDITIONAL_GROUP).addButtons(commands);
	}

	@Override
	protected TagTemplate getTemplate() {
		return div(fieldBox(USERNAME_FIELD), fieldBox(PASSWORD_FIELD));
	}

	@Override
	protected void fillFormContext(FormContext context) {
		StringField userField = FormFactory.newStringField(USERNAME_FIELD, FormFactory.MANDATORY,
			!FormFactory.IMMUTABLE, FormFactory.NO_CONSTRAINT);
		userField.setLabel(I18NConstants.LOGIN_DIALOG_USERNAME_FIELD);
		userField.setTransient(true);
		context.addMember(userField);
		PasswordField passwordField = FormFactory.newPasswordField(PASSWORD_FIELD, FormFactory.MANDATORY,
			!FormFactory.IMMUTABLE, FormFactory.NO_CONSTRAINT);
		passwordField.setLabel(I18NConstants.LOGIN_DIALOG_PASSWORD_FIELD);
		passwordField.setTransient(true);
		context.addMember(passwordField);
	}

	@Override
	protected void fillButtons(List<CommandModel> buttons) {
		buttons.add(MessageBox.button(I18NConstants.LOGIN, getApplyClosure()));
	}

	@Override
	public Command getApplyClosure() {
		return checkContextCommand()
			.andThen(this::doLogin);
	}

	private HandlerResult doLogin(DisplayContext context) {
		FormContext fc = getFormContext();
		StringField userField = (StringField) fc.getField(USERNAME_FIELD);
		PasswordField passwordField = (PasswordField) fc.getField(PASSWORD_FIELD);
		String userName = userField.getAsString();
		char[] decryptedPassword = passwordField.getDecryptedPassword().toCharArray();
		LoginFailuresModule<?> loginFailures = LoginFailuresModule.Module.INSTANCE.getImplementationInstance();
		
		// Separate failed login counter per client address to prevent denial of service attacks
		// preventing a user from logging in (by iteratively trying to authenticate with wrong
		// credentials).
		String userKey = userName.toLowerCase() + '@' + context.asRequest().getRemoteAddr();

		LoginFailure failure = loginFailures.getFailureFor(userKey);
		if (failure != null && !failure.allowRetry()) {
			long retryTimeout = TimeUnit.MILLISECONDS.toSeconds(failure.retryTimeout());
			return loginDenied(userName, I18NConstants.ERROR_TOO_MANY_LOGIN_ATTEMPS__TIMEOUT.fill(retryTimeout));
		}

		try {
			boolean success;
			try {
				success = Login.getInstance().checkUserPassword(userName, decryptedPassword, context.asRequest(),
					context.asResponse());
			} catch (InMaintenanceModeException ex) {
				return loginDenied(userName, Login.getMaintenanceMessage(userName));
			} catch (LoginHookFailedException ex) {
				return loginDenied(userName, ex.getErrorKey());
			}

			if (!success) {
				loginFailures.notifyLoginFailed(userKey);
				return loginDenied(userName, ResKey.NONE);
			} else {
				loginFailures.notifyLoginSuccessed(userKey);
			}
			Person user = Person.byName(userName);
			boolean withPasswordChange = !Login.isPasswordValidAndNotExpired(decryptedPassword, user);

			Password mfaSecret = user.getMFASecret();
			if (mfaSecret == null) {
				if (user.getMFARequirement() == MfaRequirement.REQUIRED) {
					Command loginAndReload = ctx -> loginAndReload(ctx, user, withPasswordChange);
					return EnableMultiFactorAuthenticationDialog.informMFARequired(context, user, Command.DO_NOTHING,
						loginAndReload);
				} else {
					return loginAndReload(context, user, withPasswordChange);
				}
			}

			Command loginAndReload = ctx -> loginAndReload(ctx, user, withPasswordChange);
			return new CheckOTPDialog(loginAndReload, mfaSecret).open(context);
		} finally {
			Arrays.fill(decryptedPassword, (char) 0);
		}
	}

	private HandlerResult loginAndReload(DisplayContext context, Person user, boolean withPasswordChange) {
		if (!withPasswordChange) {
			return loginAndReload(context, user);
		}

		// Force password change
		Command loginAndReload = ctx -> loginAndReload(ctx, user);
		return new ChangePasswordDialog(user, loginAndReload).open(context);
	}

	private HandlerResult loginAndReload(DisplayContext context, Person user) {
		loginUserAndReload(context, user);
		getDiscardClosure().executeCommand(context);
		return HandlerResult.DEFAULT_RESULT;
	}

	/**
	 * Logs in the given user in a new session and reloads the application.
	 */
	public static void loginUserAndReload(DisplayContext context, Person user) {
		HttpServletRequest request = context.asRequest();
		SessionService service = SessionService.getInstance();
		// Remove session for anonymous
		service.invalidateSession(request.getSession());
		service.loginUser(request, context.asResponse(), user);
		MainLayout.addFullReload(context);
	}

	private HandlerResult loginDenied(String user, ResKey detail) {
		ResKey message = com.top_logic.base.accesscontrol.I18NConstants.ERROR_AUTHENTICATE.fill(user);
		HandlerResult error;
		if (detail == ResKey.NONE) {
			error = HandlerResult.error(message);
		} else {
			error = HandlerResult.error(detail);
			error.setErrorTitle(message);
		}
		return error;
	}

}