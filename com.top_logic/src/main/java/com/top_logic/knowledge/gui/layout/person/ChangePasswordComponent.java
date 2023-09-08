/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.gui.layout.person;

import java.util.Date;
import java.util.Map;

import com.top_logic.base.accesscontrol.LoginCredentials;
import com.top_logic.base.security.device.SecurityDeviceFactory;
import com.top_logic.base.security.device.interfaces.AuthenticationDevice;
import com.top_logic.base.security.device.interfaces.PersonDataAccessDevice;
import com.top_logic.base.security.password.PasswordManager;
import com.top_logic.base.security.password.PasswordValidator;
import com.top_logic.base.security.password.PasswordValidator.ValidationResult;
import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.UnreachableAssertion;
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
import com.top_logic.util.Resources;
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

	@Override
	protected void becomingInvisible() {
		super.becomingInvisible();

		// Make sure to drop sensitive information.
		removeFormContext();
	}

	@Override
	public FormContext createFormContext() {
		FormContext formContext = new FormContext(this);

		boolean changingOwnPassword = changingOwnPassword();
		if (changingOwnPassword) {
			// Require authentication for changing the own password.
			formContext.addMember(createPasswordField(OLD_PASSWORD, false));
		}
		formContext.addMember(createNewPasswordField());
		formContext.addMember(createPasswordField(NEW_PASSWORD_2, false));

		BooleanField requireChange = FormFactory.newBooleanField(REQUIRE_CHANGE);
		formContext.addMember(requireChange);

		PasswordValidator validator = getPasswordValidator();
		if (changingOwnPassword || !validator.canExpire(getCurrentPerson())) {
			requireChange.setVisible(false);
		}

		return formContext;
	}

	/**
	 * Whether a user is currently changing his own password.
	 */
	private boolean changingOwnPassword() {
		return getModel() == TLContext.getContext().getCurrentPersonWrapper();
	}

	private StringField createNewPasswordField() {
		StringField newPasswordField = createPasswordField(NEW_PASSWORD_1, isPasswordValidationEnabled());
		if (isPasswordValidationEnabled()) {
			newPasswordField.setTooltip(getPasswordFieldTooltip());
		}
		return newPasswordField;
	}

	private StringField createPasswordField(String name, boolean withValidation) {
		StringField result =
			FormFactory.newStringField(name, true, false, CONSTRAINT_LENGTH_0_64);
		result.setControlProvider(getPasswordFieldControlProvider(withValidation));
		return result;
	}

	private ControlProvider getPasswordFieldControlProvider(boolean validation) {
		if (validation) {
			PasswordValidator passwordValidator = getPasswordValidator();
			return new PasswordInputControlProvider(DEFAULT_COLUMNS, passwordValidator.getMinPwdLength(),
				passwordValidator.getNumberContentCrit());
		} else {
			return new PasswordInputControlProvider(DEFAULT_COLUMNS, 0, 0);
		}
	}

	private String getPasswordFieldTooltip() {
		PasswordValidator validator = getPasswordValidator();
		return Resources.getInstance().getMessage(I18NConstants.PASSWORD_FIELD_TOOLTIP, validator.getMinPwdLength(),
			validator.getNumberContentCrit());
	}

	private boolean isPasswordValidationEnabled() {
		PasswordValidator validator = getPasswordValidator();
		Person currentPerson = getCurrentPerson();
		return validator.isEnabled() && !validator.isExcluded(currentPerson);
	}

	private static PasswordValidator getPasswordValidator() {
		return PasswordManager.getInstance().getPwdValidator();
	}

	private Person getCurrentPerson() {
		return (Person) getModel();
	}

	/**
	 * Check, if the changing of password is allowed for the given user.
	 * 
	 * @param aPerson
	 *        The person to change the password for, must not be <code>null</code>
	 * @return <code>true</code>, if password change is allowed.
	 */
	protected boolean allowPwdChange(Person aPerson) {
		{
			String theID = aPerson.getAuthenticationDeviceID();
			AuthenticationDevice theDevice = SecurityDeviceFactory.getAuthenticationDevice(theID);
			boolean hasChangeAccess = ((EditPersonComponent) getDialogParent()).hasDeleteAccess(aPerson);

			return (theDevice != null)
				&& theDevice.allowPwdChange()
				&& (TLContext.getContext().getCurrentUserName().equals(aPerson.getName())
				|| hasChangeAccess);
		}
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

			Person account = (Person) model;

			FormComponent form = (FormComponent) aComponent;
			FormContext formContext = form.getFormContext();

			if (formContext.hasMember(OLD_PASSWORD)) {
				char[] oldPassword = ((String) formContext.getField(OLD_PASSWORD).getValue()).toCharArray();

				AuthenticationDevice device = getAuthenticationDevice(account);
				try (LoginCredentials login = LoginCredentials.fromUserAndPassword(account, oldPassword)) {
					boolean oldPasswordValid = device.authentify(login);
					if (!oldPasswordValid) {
						return error(I18NConstants.WRONG_OLD_PASSWORD);
					}
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
			try (LoginCredentials newLogin = LoginCredentials.fromUserAndPassword(account, newPassword1.toCharArray())) {
				PasswordValidator pwdValidator = getPasswordValidator();
				ValidationResult validatorResult = pwdValidator.validatePwd(newLogin);
				switch (validatorResult) {
					case OK: {
						break;
					}
					case CONTENT_INVALID:
						return error(
							I18NConstants.INVALID_PASSWORD_CONTENT__SPECIALS.fill(pwdValidator.getNumberContentCrit()));
					case TO_SHORT:
						return error(I18NConstants.PASSWORD_TOO_SHORT__CHARS.fill(pwdValidator.getMinPwdLength()));
					case USED_BEFORE:
						return error(I18NConstants.PASSWORD_USED_BEFORE);
					default: {
						throw new UnreachableAssertion("Unexpected validator result: " + validatorResult);
					}
				}

				Transaction tx = PersistencyLayer.getKnowledgeBase().beginTransaction();
				try {
					PasswordManager passwordManager = PasswordManager.getInstance();
					boolean updateOK = passwordManager.setPassword(newLogin);
					if (!updateOK) {
						return error(I18NConstants.PASSWORD_CHANGE_FAILED);
					}

					Boolean requireChange = (Boolean) formContext.getField(REQUIRE_CHANGE).getValue();
					if (Utils.isTrue(requireChange)) {
						passwordManager.expirePassword(account);
					} else {
						account.setLastPasswordChange(new Date());
					}

					tx.commit();
				} catch (KnowledgeBaseException ex) {
					Logger.error("Transaction failed.", ex, ChangePasswordComponent.class);
					error(I18NConstants.PASSWORD_CHANGE_FAILED);
				} finally {
					tx.rollback();
				}
			}

			// Make sure to drop sensitive data.
			form.removeFormContext();

			HandlerResult result = new HandlerResult();
			result.setCloseDialog(true);
			return result;
		}

		private HandlerResult error(ResKey key) {
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

		private static final ExecutableState NO_EXEC_READ_ONLY =
			ExecutableState.createDisabledState(I18NConstants.NO_PASSWORD_CHANGE_READ_ONLY);

		private PasswordChangePossible() {
			// Singleton constructor.
		}

		@Override
		public ExecutableState isExecutable(LayoutComponent aComponent, Object model, Map<String, Object> someValues) {
			Person person = (Person) model;
			if (person == null) {
				return ExecutableState.NOT_EXEC_HIDDEN;
			}
			AuthenticationDevice device = getAuthenticationDevice(person);
			if (device == null) {
				return NO_EXEC_NO_DEVICE;
			}
			else if (!device.allowPwdChange()) {
				return NO_EXEC_EXTERNALY_DEFINED;
			}
			PersonDataAccessDevice dataDevice = getPersonDataAccessDevice(person);
			if (dataDevice == null || dataDevice.isReadOnly()) {
				return NO_EXEC_READ_ONLY;
			}
			return ExecutableState.EXECUTABLE;
		}

	}

	static AuthenticationDevice getAuthenticationDevice(Person person) {
		String authDevice;
		authDevice = person.getAuthenticationDeviceID();
		AuthenticationDevice device = SecurityDeviceFactory.getAuthenticationDevice(authDevice);
		return device;
	}

	static PersonDataAccessDevice getPersonDataAccessDevice(Person person) {
		String dataDevice = person.getDataAccessDeviceID();
		PersonDataAccessDevice device = SecurityDeviceFactory.getPersonAccessDevice(dataDevice);
		return device;
	}

}
