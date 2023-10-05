/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */ 
package com.top_logic.base.security.password;

import java.util.List;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.CommaSeparatedStrings;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Name;
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
public class PasswordValidator extends AbstractConfiguredInstance<PasswordValidator.Config>
		implements PasswordValidation {

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
		 * Number of days a password stays valid.
		 * 
		 * <p>
		 * A password will expire after the given number of days. After that period, a user has to
		 * change his password. A value of zero means that a password never expires.
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
	 * Potential results of {@link PasswordValidator#validatePassword(Person, char[]) password
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

	private final int _expiryPeriod;

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
		this._expiryPeriod = config.getExpiryPeriod();
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

	@Override
	public int getExpiryPeriod() {
		if (!enabled) {
			return 0;
		}

		return _expiryPeriod;
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

	@Override
	public boolean isExcluded(Person account) {
		if (!enabled) {
			return true;
		}

		String uid = account.getName();

		for (int idx = 0; idx < this.excludeUIDs.size(); idx++) {
			String anExclude = this.excludeUIDs.get(idx);
			if (uid.equalsIgnoreCase(anExclude)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public ValidationResult validatePassword(Person account, char[] password) throws IllegalArgumentException {
        if(!enabled){
			return ValidationResult.OK;
        }
		if (this.isExcluded(account)) {
			return ValidationResult.OK;
		}

		boolean criteria_0 = password.length >= this.minPwdLength;
		boolean criteria_2 = checkContentCriterias(password) >= numberContentCrit;

		if (criteria_0 && criteria_2) {
			return ValidationResult.OK;
        }else if(!criteria_0){
			return ValidationResult.TO_SHORT;
        }else{
			return ValidationResult.CONTENT_INVALID;
        }
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
