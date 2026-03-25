/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */

package com.top_logic.layout.component.configuration;

import static com.top_logic.layout.form.template.model.Templates.*;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import com.top_logic.base.accesscontrol.LoginFailure;
import com.top_logic.base.accesscontrol.LoginFailuresModule;
import com.top_logic.base.security.util.Password;
import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.misc.TypedConfigUtil;
import com.top_logic.basic.util.ResKey;
import com.top_logic.event.infoservice.InfoService;
import com.top_logic.html.template.TagTemplate;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.DisplayDimension;
import com.top_logic.layout.DisplayUnit;
import com.top_logic.layout.basic.Command;
import com.top_logic.layout.basic.CommandModel;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.StringField;
import com.top_logic.layout.messagebox.AbstractTemplateDialog;
import com.top_logic.layout.messagebox.MessageBox;
import com.top_logic.layout.messagebox.MessageBox.ButtonType;
import com.top_logic.layout.structure.DialogModel;
import com.top_logic.layout.toolbar.ToolBar;
import com.top_logic.layout.toolbar.ToolBarGroup;
import com.top_logic.tool.boundsec.CommandHandlerFactory;
import com.top_logic.tool.boundsec.HandlerResult;

import dev.samstevens.totp.code.CodeGenerator;
import dev.samstevens.totp.code.CodeVerifier;
import dev.samstevens.totp.code.DefaultCodeGenerator;
import dev.samstevens.totp.code.DefaultCodeVerifier;
import dev.samstevens.totp.time.SystemTimeProvider;
import dev.samstevens.totp.time.TimeProvider;

/**
 * A dialog to check a one time password.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class CheckOTPDialog extends AbstractTemplateDialog {

	private static final String OTP_FIELD = "otp";

	/**
	 * Static configuration for {@link CheckOTPDialog}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public static interface Config extends ConfigurationItem {

		/**
		 * The commands to display in the toolbar of the dialog.
		 */
		List<PolymorphicConfiguration<? extends Function<? super Person, ? extends CommandModel>>> getToolbarCommands();

	}

	private final Command _continuation;

	private final Person _account;

	private final Password _secret;

	private final LoginFailuresModule<?> _loginFailures =
		LoginFailuresModule.Module.INSTANCE.getImplementationInstance();


	/**
	 * Creates a new {@link CheckOTPDialog}.
	 */
	public CheckOTPDialog(Command continuation, Person account, Password secret) {
		this(continuation, account, secret, I18NConstants.CHECK_OTP_DIALOG_TITLE,
			DisplayDimension.dim(500, DisplayUnit.PIXEL),
			DisplayDimension.dim(300, DisplayUnit.PIXEL));
	}

	/**
	 * Creates a new {@link CheckOTPDialog}.
	 */
	public CheckOTPDialog(Command continuation, Person account, Password secret, ResKey dialogTitle,
			DisplayDimension width,
			DisplayDimension height) {
		super(dialogTitle, width, height);
		_continuation = continuation;
		_account = account;
		_secret = secret;
		initToolbarCommands();
	}

	/**
	 * Creates a new {@link CheckOTPDialog}.
	 */
	public CheckOTPDialog(Command continuation, Person account, Password secret, DialogModel dialogModel) {
		super(dialogModel);
		_continuation = continuation;
		_account = account;
		_secret = secret;
		initToolbarCommands();
	}

	private void initToolbarCommands() {
		Config config = ApplicationConfig.getInstance().getConfig(Config.class);
		List<Function<? super Person, ? extends CommandModel>> commands =
			TypedConfigUtil.createInstanceList(config.getToolbarCommands());
		if (commands.isEmpty()) {
			return;
		}
		DialogModel dialogModel = getDialogModel();
		ToolBar toolbar = dialogModel.getToolbar();
		ToolBarGroup toolbarGroup = toolbar.defineGroup(CommandHandlerFactory.ADDITIONAL_GROUP);
		commands.stream().map(fun -> fun.apply(_account)).forEach(toolbarGroup::addButton);
	}

	@Override
	protected TagTemplate getTemplate() {
		return div(resource(I18NConstants.CHECK_OTP_FIELD_MESSAGE), fieldBox(OTP_FIELD));
	}

	@Override
	protected void fillFormContext(FormContext context) {
		StringField otpField =
			FormFactory.newStringField(OTP_FIELD, null, FormFactory.MANDATORY, !FormFactory.IMMUTABLE,
			FormFactory.NO_CONSTRAINT);
		otpField.setLabel(I18NConstants.CHECK_OTP_FIELD_LABEL);
		context.addMember(otpField);
	}

	@Override
	public Command getApplyClosure() {
		return checkContextCommand()
			.andThen(this::checkOTP);
	}

	private HandlerResult checkOTP(DisplayContext context) {
		String failureKey = failureKey();
		LoginFailure failure = _loginFailures.getFailureFor(failureKey);
		if (failure != null && !failure.allowRetry()) {
			long retryTimeout = TimeUnit.MILLISECONDS.toSeconds(failure.retryTimeout());
			InfoService.showError(I18NConstants.CHECK_OTP_TOO_MANY_ATTEMPTS__TIMEOUT.fill(retryTimeout));
			return HandlerResult.DEFAULT_RESULT;
		}

		StringField field = (StringField) getFormContext().getField(OTP_FIELD);
		String code = field.getAsString();
		TimeProvider timeProvider = new SystemTimeProvider();
		CodeGenerator codeGenerator = new DefaultCodeGenerator();
		CodeVerifier verifier = new DefaultCodeVerifier(codeGenerator, timeProvider);
		
		boolean successful = verifier.isValidCode(_secret.decrypt(), code);
		if (successful) {
			_loginFailures.notifyLoginSuccessed(failureKey);
			return getDiscardClosure().andThen(_continuation).executeCommand(context);
		} else {
			_loginFailures.notifyLoginFailed(failureKey);
		}
		InfoService.showError(I18NConstants.INVALID_OTP);
		return HandlerResult.DEFAULT_RESULT;
	}

	@Override
	protected void fillButtons(List<CommandModel> buttons) {
		buttons.add(MessageBox.button(ButtonType.OK, getApplyClosure()));
	}

	private String failureKey() {
		return _secret.getCryptedValue() + "@" + "OTP";
	}

}

