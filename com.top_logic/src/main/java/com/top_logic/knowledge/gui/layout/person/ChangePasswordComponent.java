/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.gui.layout.person;

import java.util.Map;

import com.top_logic.base.accesscontrol.LoginCredentials;
import com.top_logic.base.security.device.interfaces.AuthenticationDevice;
import com.top_logic.base.security.password.PasswordValidator;
import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.util.ResKey;
import com.top_logic.knowledge.service.KnowledgeBaseException;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.form.component.FormComponent;
import com.top_logic.layout.form.constraints.StringLengthConstraint;
import com.top_logic.layout.form.control.PasswordInputControlProvider;
import com.top_logic.layout.form.model.BooleanField;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.StringField;
import com.top_logic.layout.form.template.ControlProvider;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.AbstractCommandHandler;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.tool.execution.ExecutabilityRule;
import com.top_logic.tool.execution.ExecutableState;
import com.top_logic.util.TLContext;
import com.top_logic.util.Utils;

/**
 * Dialog allowing to change the password for a {@link Person}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ChangePasswordComponent extends FormComponent {

	private static final StringLengthConstraint CONSTRAINT_LENGTH_0_64 = new StringLengthConstraint(0, 64);

	/**
	 * Default width of password field, to fit dialog width
	 */
	public static final int DEFAULT_COLUMNS = 20;

	/** Name of field for new password. */
	public static final String OLD_PASSWORD = "oldPassword";

	/** Name of field for new password. */
	public static final String NEW_PASSWORD_1 = "newPassword1";

	/** Name of field for repeated new password. */
	public static final String NEW_PASSWORD_2 = "newPassword2";

	/** Name of field requesting a password change upon next login. */
	public static final String REQUIRE_CHANGE = "requireChange";

	/**
	 * Creates a {@link ChangePasswordComponent} from configuration.
	 */
	public ChangePasswordComponent(InstantiationContext context, Config atts) throws ConfigurationException {
		super(context, atts);
	}

	@Override
	protected boolean supportsInternalModel(Object anObject) {
		return anObject instanceof Person;
	}

	private Person getCurrentPerson() {
		return (Person) getModel();
	}

	@Override
	protected void becomingInvisible() {
		super.becomingInvisible();

		// Make sure to drop sensitive information.
		removeFormContext();
	}

	@Override
	public FormContext createFormContext() {
		FormContext formContext = new FormContext(this);

		Person currentPerson = getCurrentPerson();
		boolean changingOwnPassword = currentPerson == TLContext.getContext().getCurrentPersonWrapper();
		addChangePasswordFields(formContext, currentPerson, changingOwnPassword);

		return formContext;
	}

	/**
	 * Adds fields to change the password for the given person.
	 * 
	 * @param changingOwnPassword
	 *        Whether the person adds its own password. In this case the "old password" field is
	 *        displayed.
	 * 
	 * @see ApplyPasswordCommand#applyPasswordChange(FormContext, Person)
	 */
	public static void addChangePasswordFields(FormContext formContext, Person person, boolean changingOwnPassword) {
		if (changingOwnPassword) {
			// Require authentication for changing the own password.
			formContext.addMember(createPasswordField(OLD_PASSWORD, person, false));
		}
		formContext.addMember(createNewPasswordField(person));
		formContext.addMember(createPasswordField(NEW_PASSWORD_2, person, false));

		BooleanField requireChange = FormFactory.newBooleanField(REQUIRE_CHANGE);
		formContext.addMember(requireChange);

		PasswordValidator validator = getPasswordValidator(person);
		if (changingOwnPassword || validator.isExcluded(person)) {
			requireChange.setVisible(false);
		}
	}

	private static StringField createNewPasswordField(Person person) {
		StringField newPasswordField = createPasswordField(NEW_PASSWORD_1, person, isPasswordValidationEnabled(person));
		if (isPasswordValidationEnabled(person)) {
			newPasswordField.setTooltip(getPasswordFieldTooltip(person));
		}
		return newPasswordField;
	}

	private static StringField createPasswordField(String name, Person person, boolean withValidation) {
		StringField result =
			FormFactory.newStringField(name, true, false, CONSTRAINT_LENGTH_0_64);
		result.setControlProvider(getPasswordFieldControlProvider(person, withValidation));
		return result;
	}

	private static ControlProvider getPasswordFieldControlProvider(Person person, boolean validation) {
		if (validation) {
			PasswordValidator passwordValidator = getPasswordValidator(person);
			return new PasswordInputControlProvider(DEFAULT_COLUMNS, passwordValidator.getMinPwdLength(),
				passwordValidator.getNumberContentCrit());
		} else {
			return new PasswordInputControlProvider(DEFAULT_COLUMNS, 0, 0);
		}
	}

	private static ResKey getPasswordFieldTooltip(Person person) {
		PasswordValidator validator = getPasswordValidator(person);
		return I18NConstants.PASSWORD_FIELD_TOOLTIP.fill(validator.getMinPwdLength(), validator.getNumberContentCrit());
	}

	private static boolean isPasswordValidationEnabled(Person person) {
		PasswordValidator validator = getPasswordValidator(person);
		return !validator.isExcluded(person);
	}

	private static PasswordValidator getPasswordValidator(Person person) {
		return person.getAuthenticationDevice().getPasswordValidator();
	}

	/**
	 * {@link CommandHandler} applying a password change.
	 * 
	 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
	 */
	public static class ApplyPasswordCommand extends AbstractCommandHandler {

		/**
		 * Creates a {@link ApplyPasswordCommand} from configuration.
		 */
		public ApplyPasswordCommand(InstantiationContext context, Config config) {
			super(context, config);
		}

		@Override
		public HandlerResult handleCommand(DisplayContext aContext, LayoutComponent aComponent,
				Object model, Map<String, Object> someArguments) {

			FormComponent form = (FormComponent) aComponent;
			Person account = (Person) model;

			HandlerResult changeResult = applyPasswordChange(form.getFormContext(), account);
			if (!changeResult.isSuccess()) {
				return changeResult;
			}

			// Make sure to drop sensitive data.
			form.removeFormContext();

			HandlerResult result = new HandlerResult();
			result.setCloseDialog(true);
			return result;
		}

		/**
		 * Applies the password change made in the given {@link FormContext} for the given
		 * {@link Person}.
		 * 
		 * @see ChangePasswordComponent#addChangePasswordFields(FormContext, Person, boolean)
		 */
		public static HandlerResult applyPasswordChange(FormContext formContext, Person account) {
			AuthenticationDevice device = account.getAuthenticationDevice();
			if (formContext.hasMember(OLD_PASSWORD)) {
				char[] oldPassword = ((String) formContext.getField(OLD_PASSWORD).getValue()).toCharArray();

				LoginCredentials login = LoginCredentials.fromUserAndPassword(account, oldPassword);
				try {
					boolean oldPasswordValid = device.authentify(login);
					if (!oldPasswordValid) {
						return error(I18NConstants.WRONG_OLD_PASSWORD);
					}
				} finally {
					login.clearPassword();
				}
			}

			String newPassword1 = (String) formContext.getField(NEW_PASSWORD_1).getValue();
			String newPassword2 = (String) formContext.getField(NEW_PASSWORD_2).getValue();

			if (!Utils.equals(newPassword1, newPassword2)) {
				return error(I18NConstants.PASSWORD_MISSMATCH);
			}

			if (StringServices.isEmpty(newPassword1)) {
				return error(I18NConstants.EMPTY_PASSWORD_DISALLOWED);
			}
			LoginCredentials newLogin = LoginCredentials.fromUserAndPassword(account, newPassword1.toCharArray());
			try {
				Transaction tx =
					PersistencyLayer.getKnowledgeBase()
						.beginTransaction(I18NConstants.CHANGED_PASSWORD__USER.fill(account.getName()));
				try {
					device.setPassword(account, newLogin.getPassword());

					Boolean requireChange = (Boolean) formContext.getField(REQUIRE_CHANGE).getValue();
					if (Utils.isTrue(requireChange)) {
						device.expirePassword(account);
					}

					tx.commit();
				} catch (KnowledgeBaseException ex) {
					Logger.error("Transaction failed.", ex, ChangePasswordComponent.class);
					return error(I18NConstants.PASSWORD_CHANGE_FAILED);
				} finally {
					tx.rollback();
				}
			} finally {
				newLogin.clearPassword();
			}
			return HandlerResult.DEFAULT_RESULT;
		}

		private static HandlerResult error(ResKey key) {
			return HandlerResult.error(key);
		}
	}

	/**
	 * {@link ExecutabilityRule} defining whether the person has a locally defined password.
	 * 
	 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
	 */
	public static class PasswordChangePossible implements ExecutabilityRule {

		/**
		 * Singleton {@link ChangePasswordComponent.PasswordChangePossible} instance.
		 */
		public static final PasswordChangePossible INSTANCE = new PasswordChangePossible();

		private static final ExecutableState NO_EXEC_NO_DEVICE =
			ExecutableState.createDisabledState(I18NConstants.NO_PASSWORD_CHANGE_NO_DEVICE);

		private static final ExecutableState NO_EXEC_EXTERNALY_DEFINED =
			ExecutableState.createDisabledState(I18NConstants.NO_PASSWORD_CHANGE_EXTERNALLY_DEFINED);

		private PasswordChangePossible() {
			// Singleton constructor.
		}

		@Override
		public ExecutableState isExecutable(LayoutComponent aComponent, Object model, Map<String, Object> someValues) {
			Person person = (Person) model;
			if (person == null) {
				return ExecutableState.NOT_EXEC_HIDDEN;
			}
			AuthenticationDevice device = person.getAuthenticationDevice();
			if (device == null) {
				return NO_EXEC_NO_DEVICE;
			}
			else if (!device.allowPwdChange()) {
				return NO_EXEC_EXTERNALY_DEFINED;
			}
			return ExecutableState.EXECUTABLE;
		}
	}
}
