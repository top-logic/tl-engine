/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */ 
package com.top_logic.base.security.password;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.top_logic.base.accesscontrol.Login;
import com.top_logic.base.accesscontrol.LoginCredentials;
import com.top_logic.base.security.device.db.DBUserRepository;
import com.top_logic.base.security.password.PasswordValidator.ValidationResult;
import com.top_logic.base.security.password.hashing.PasswordHashingService;
import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.knowledge.wrap.person.Person;


/**
 * This manager is responsible for password changing / setting.
 * 
 * It takes care of all operations neccessary for password changing, including encoding, the addition of the old password to the password history.
 * This manager also provides access to the Passwordvalidator used to validate the new password
 * 
 * @author <a href="mailto:tri@top-logic.com">tri@top-logic.com</a>
 */
public class PasswordManager extends AbstractConfiguredInstance<PasswordManager.Config> {

	/**
	 * Configuration options for {@link PasswordManager}.
	 */
	public interface Config extends PolymorphicConfiguration<PasswordManager> {

		/**
		 * The {@link PasswordValidator} to use.
		 */
		@Name("password-validator")
		PolymorphicConfiguration<? extends PasswordValidator> getPasswordValidator();

	}

    /**
     * Attributename for '!' sepearted List of olde, hashed passwords.
     */
    private static final String PERSON_KO_ATTRIB_PWD_HISTORY     = "pwdhistory";

    private final PasswordValidator _validator;

	/**
	 * Placeholder that is used upon user creation as initial password-hash as long as no explicit
	 * password has been set.
	 */
	public static final String INITIAL_PWD_HASH_PLACEHOLDER = "*";

    /**
	 * Creates a {@link PasswordManager} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public PasswordManager(InstantiationContext context, Config config) {
		super(context, config);

		_validator = context.getInstance(config.getPasswordValidator());
	}

    /**
     * the passwordvalidator used to validate passwords
     */
    public PasswordValidator getPwdValidator(){
		return _validator;
    }
    
    /**
	 * Updates the password for the given account.
	 * 
	 * @param login
	 *        The {@link LoginCredentials} defining the new login.
	 * @return <code>true</code> upon success, <code>false</code> otherwise. Note, this method
	 *         validates the given password and returns false if it was not valid. However, if you
	 *         want to bring some detailed message back to the user, you should use the validator
	 *         and validate the password before calling this method.
	 */
	public boolean setPassword(LoginCredentials login) {
		Person account = login.getPerson();
		char[] password = login.getPassword();

		PasswordValidator validator = getPwdValidator();
		ValidationResult validationResult = validator.validatePwd(login);
		if (ValidationResult.OK != validationResult) {
			Logger.debug("Attempt to set invalid password: " + validator.getStateMessage(validationResult), this);
            return false;
        }
		try {
			/* returns pass signed with leading '#', or the initial password hash */
			String theOldPwd = getPasswordHash(account);

			setPasswordHash(account, PasswordHashingService.getInstance().createHash(password));

			if (!DBUserRepository.NO_PASSWORD.equals(theOldPwd)) {
				addPwdToHistory(account, theOldPwd);
			}
			setLastPWDChange(account, new Date());
			account.updateUserData();
            return true;
		} catch (Exception e) {
			Logger.error("Failed to set new password.", e, PasswordManager.class);
		}
        return false;
	}

	/**
	 * The currently stored password hash for the given account.
	 *
	 * @param account
	 *        The account whose password should be checked.
	 * @return The password hash for the given account, or <code>null</code>, if the account cannot
	 *         log in with a password.
	 */
	public String getPasswordHash(Person account) {
		// TODO: Automatically created
		throw new UnsupportedOperationException();
	}

	/**
	 * Updates the password hash of the given account.
	 * 
	 * <p>
	 * Note: Must be called in a transaction context.
	 * </p>
	 *
	 * @param account
	 *        The account to assign a new password to.
	 * @param hash
	 *        The new password hash for the given account.
	 * 
	 * @see #getPasswordHash(Person)
	 */
	public void setPasswordHash(Person account, String hash) {
		// TODO: Automatically created
		throw new UnsupportedOperationException();
	}

	/**
	 * Expires the password for the given account (if password expiry is enabled for that account).
	 */
	public void expirePassword(Person account) {
		PasswordValidator validator = getPwdValidator();
		if (!validator.canExpire(account)) {
			return;
		}

		account.setLastPasswordChange(new Date(0));
	}
    
    /**
	 * Adds a password hash to the persons password history.
	 */
	private void addPwdToHistory(Person aPerson, String pwdHash) {
		if (PasswordManager.INITIAL_PWD_HASH_PLACEHOLDER.equals(pwdHash)) {
			/* do not add the initial placeholder to pwd history as it is no valid pwd hash */
			return;
		}
		List<String> pwdHistory = getPWDHistory(aPerson);
		pwdHistory.add(pwdHash);
        int pwdRepeatCycle = getPwdValidator().getPwdRepeatCycle();
        while(pwdHistory.size() > pwdRepeatCycle){
            pwdHistory.remove(0);
        }
        setPwdHistory(aPerson,pwdHistory);
    }

    private void setPwdHistory(Person thePerson, List<String> pwdHistory){
		String pwdHashString = PasswordHistoryFormat.INSTANCE.format(pwdHistory);
		thePerson.setValue(PERSON_KO_ATTRIB_PWD_HISTORY, pwdHashString);
    }
    
    /**
     * Return List of old password from attribute {@link #PERSON_KO_ATTRIB_PWD_HISTORY}.
     * 
     * Stores as many of the previously pwdhashes as being configured with pwdrepeatcycle
     * 
     * @return a modifieable List of Strings.
     */
    List<String> getPWDHistory(Person thePerson) {
        String pwdHashString = (String) thePerson.getValue(PERSON_KO_ATTRIB_PWD_HISTORY);
		if (!StringServices.isEmpty(pwdHashString)) {
			return parsePwdHistory(thePerson, pwdHashString);
        }
		return new ArrayList<>();
    }

	@SuppressWarnings("unchecked")
	private static List<String> parsePwdHistory(Person person, String pwdHashString) {
		List<String> result;
		try {
			result = (List<String>) PasswordHistoryFormat.INSTANCE.parseObject(pwdHashString);
		} catch (ParseException ex) {
			Logger.error("Unable to parse password hashes '" + pwdHashString + "' for person " + person, ex,
				PasswordManager.class);
			result = new ArrayList<>();
		}
		return result;
	}

    /**
     * same a aDate
     */
	private Date setLastPWDChange(Person account, Date changeDate) {
		account.setLastPasswordChange(changeDate);
		return changeDate;
    }

    /**
	 * @see Login#getPasswordManager()
	 */
    public static synchronized PasswordManager getInstance(){
		return Login.getInstance().getPasswordManager();
    }
    

}
