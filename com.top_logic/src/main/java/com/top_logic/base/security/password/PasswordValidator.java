/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */ 
package com.top_logic.base.security.password;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.top_logic.base.accesscontrol.LoginCredentials;
import com.top_logic.base.security.password.hashing.PasswordHashingService;
import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.CommaSeparatedStrings;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.time.CalendarUtil;
import com.top_logic.basic.util.ResKey;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.util.Resources;


/**
 * This class implements the a password policy.
 * 
 * The individual parameters can be configured.
 * This is the class used for validating the passwords
 * 
 * @author <a href="mailto:tri@top-logic.com">tri@top-logic.com</a>
 */
public class PasswordValidator extends AbstractConfiguredInstance<PasswordValidator.Config> {

	/**
	 * Configuration options for {@link PasswordValidator}
	 */
	public interface Config extends PolymorphicConfiguration<PasswordValidator> {

		/**
		 * Whether validation is enabled.
		 * 
		 * <p>
		 * All passwords are OK, if validation is disabled.
		 * </p>
		 */
		@Name("enabled")
		boolean isEnabled();

		/**
		 * Password expiry period in days.
		 * 
		 * <p>
		 * A password will expire after the given number of days. A value of zero means no expiry at
		 * all.
		 * </p>
		 */
		@Name("expiry-period")
		int getExpiryPeriod();

		/**
		 * Restricts the reuse of passwords.
		 * 
		 * <p>
		 * A password must not be reused for the given number of password changes. A value of zero
		 * means not to to restrict password reuse.
		 * </p>
		 */
		@Name("repeat-cycle")
		int getRepeatCycle();

		/**
		 * The minimum number of characters in a password.
		 */
		@Name("min-password-length")
		int getMinPasswordLength();

		/**
		 * The number of password criteria a user password must match.
		 * 
		 * <p>
		 * There are 4 content criteria defined.
		 * </p>
		 */
		@Name("criteria-count")
		int getContentCriteriaCnt();

		/**
		 * Comma separated list of login IDs that are excluded from password validation.
		 * 
		 * <p>
		 * Note: Using this options breaks password security.
		 * </p>
		 */
		@Name("exclude-uids")
		@Label("Exclude user IDs")
		@Format(CommaSeparatedStrings.class)
		List<String> getExcludeUIDs();

	}

	/**
	 * Potential results of {@link PasswordValidator#validatePwd(LoginCredentials) password
	 * validation}.
	 */
	public enum ValidationResult {
		/**
		 * The password matches all requirements.
		 */
		OK(I18NConstants.PWD_OK),

		/**
		 * The password was used before and must not be reused.
		 * 
		 * @see Config#getRepeatCycle()
		 */
		USED_BEFORE(I18NConstants.PWD_USED_BEFORE), 
		
		/**
		 * The password is to short.
		 * 
		 * @see Config#getMinPasswordLength()
		 */
		TO_SHORT(I18NConstants.PWD_TO_SHORT),

		/**
		 * The password does not comply with all requirements.
		 * 
		 * @see Config#getContentCriteriaCnt()
		 */
		CONTENT_INVALID(I18NConstants.PWD_CONTENT_INVALID);

		private ResKey _messageKey;

		/**
		 * Creates a {@link ValidationResult}.
		 */
		ValidationResult(ResKey messageKey) {
			_messageKey = messageKey;
		}

		/**
		 * The message to show to a user, if the validation fails.
		 */
		public ResKey messageKey() {
			return _messageKey;
		}
	}
    
	private static final int MAX_CONTENT_CRITERIA = 4;
    
	private final boolean enabled;

	private final int expiryPeriod;

	private final int repeatCycle;

	private final int minPwdLength;

	private final int numberContentCrit;

	private final List<String> excludeUIDs;
    
	/**
	 * Creates a {@link PasswordValidator} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public PasswordValidator(InstantiationContext context, Config config) {
		super(context, config);

		this.enabled = config.isEnabled();
		this.expiryPeriod = config.getExpiryPeriod();
		this.repeatCycle = config.getRepeatCycle();
		this.minPwdLength = config.getMinPasswordLength();
		this.numberContentCrit = Math.min(MAX_CONTENT_CRITERIA, config.getContentCriteriaCnt());
		this.excludeUIDs = config.getExcludeUIDs();
    }
    
	/**
	 * @see Config#getRepeatCycle()
	 */
    public int getPwdRepeatCycle(){
        return this.repeatCycle;
    }

	/**
	 * @see Config#getExpiryPeriod()
	 */
    public int getExpiryPeriod(){
        return this.expiryPeriod;
    }

	/**
	 * @see Config#getMinPasswordLength()
	 */
    public int getMinPwdLength(){
        return this.minPwdLength;
    }

	/**
	 * @see Config#getContentCriteriaCnt()
	 */
    public int getNumberContentCrit(){
        return this.numberContentCrit;
	}

	/**
	 * Checks whether the given account should be excluded from password policy requirements.
	 *
	 * @param account
	 *        the account to check.
	 * @return true if userID is in exclude list
	 */
	public boolean isExcluded(Person account) {
		String uid = account.getName();

		for (int idx = 0; idx < this.excludeUIDs.size(); idx++) {
			String anExclude = this.excludeUIDs.get(idx);
			if (uid.equalsIgnoreCase(anExclude)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * if password validation is enabled, false otherwise
	 */
    public boolean isEnabled() {
		return enabled;
	}

	/**
	 * Validates a password for a person.
	 * 
	 * @param login
	 *        {@link LoginCredentials} to validate.
	 * @return returns an int indicating the state of the pwd (refer constants of this class)
	 */
	public ValidationResult validatePwd(LoginCredentials login) throws IllegalArgumentException {
        if(!enabled){
			return ValidationResult.OK;
        }
		Person account = login.getPerson();
		if (this.isExcluded(account)) {
			return ValidationResult.OK;
		}

		List<String> previousPwdHashes = PasswordManager.getInstance().getPWDHistory(account);
		char[] password = login.getPassword();
		boolean criteria_0 = password.length >= this.minPwdLength;
		boolean criteria_1 = !alreadyUsed(previousPwdHashes, password);
		boolean criteria_2 = checkContentCriterias(password) >= numberContentCrit;

        if(criteria_0 && criteria_1 && criteria_2){
			return ValidationResult.OK;
        }else if(!criteria_0){
			return ValidationResult.TO_SHORT;
        }else if(!criteria_1){
			return ValidationResult.USED_BEFORE;
        }else{
			return ValidationResult.CONTENT_INVALID;
        }
    }
    
	private boolean alreadyUsed(List<String> previousPwdHashes, char[] thePwd) {
		if (repeatCycle <= 0) {
			return false;
		}
		PasswordHashingService phs = PasswordHashingService.getInstance();
		int hashCnt = previousPwdHashes.size();
		List<String> relevantHashes = previousPwdHashes.subList(Math.max(0, hashCnt - repeatCycle), hashCnt);
		for (String aPwdHash : relevantHashes) {
			if (phs.verify(thePwd, aPwdHash).success()) {
                return true;
            }
        }
        return false;
    }
    
    /**
	 * Checks pwd content by four criterias: -contains uppercase letters -contains lowercase letters
	 * -contains digits -contains special chars
	 * 
	 * @param pwd
	 *        The password characters.
	 * @return how many of these criterias are matched (0<=returnValue<=4)
	 */
    private int checkContentCriterias(char[] pwd){
        int containsDigit = 0; 
        int containsLowerCaseLetter = 0; 
        int containsUpperCaseLetter = 0; 
        int containsSomethingElse = 0;
		for (int idx = 0; idx < pwd.length; idx++) {
			char c = pwd[idx];
            if(Character.getType(c)==Character.DECIMAL_DIGIT_NUMBER){
                containsDigit = 1;
            }else if(Character.getType(c)==Character.UPPERCASE_LETTER){
                containsUpperCaseLetter = 1;
            }else if(Character.getType(c)==Character.LOWERCASE_LETTER){
                containsLowerCaseLetter = 1;
            }else{
                containsSomethingElse=1;
            }
        }
        return containsDigit+containsLowerCaseLetter+containsUpperCaseLetter+containsSomethingElse;
    }
    
	/**
	 * Whether the password for the given account can expire.
	 */
	public boolean canExpire(Person account) {
		return enabled && !isExcluded(account);
	}

	/**
	 * Whether the password for the given account has expired and must be updated during next login.
	 */
	public boolean isPasswordExpired(Person account) {
		if (!canExpire(account)) {
			return false;
		}

		Date changeDate = account.getLastPasswordChange();
		if (changeDate.getTime() == 0L) {
			// Password was explicitly reset.
			return true;
		}

		if (expiryPeriod <= 0) {
			// No expire by default.
			return false;
		}

		Calendar nextPasswordChange = CalendarUtil.createCalendar(changeDate);
		nextPasswordChange.add(Calendar.DAY_OF_YEAR, expiryPeriod);
		return System.currentTimeMillis() > nextPasswordChange.getTimeInMillis();
	}

    /**
     * a human readable translation for the given pwd state
     */
	public String getStateMessage(ValidationResult aPwdState) {
		return Resources.getInstance().getString(getStateMessageKey(aPwdState));
    }
    
    /**
     * an i18Nkey which can be used to retrieve a human readable translation for the given pwd state
     */
	public ResKey getStateMessageKey(ValidationResult aPwdState) {
		return aPwdState.messageKey();
    }

}
