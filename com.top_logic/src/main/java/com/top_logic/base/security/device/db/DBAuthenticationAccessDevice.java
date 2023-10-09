/*
 * SPDX-FileCopyrightText: 2005 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.security.device.db;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import com.top_logic.base.accesscontrol.LoginCredentials;
import com.top_logic.base.security.device.interfaces.AuthenticationDevice;
import com.top_logic.base.security.device.interfaces.SecurityDevice;
import com.top_logic.base.security.password.I18NConstants;
import com.top_logic.base.security.password.PasswordHistoryFormat;
import com.top_logic.base.security.password.PasswordValidator;
import com.top_logic.base.security.password.PasswordValidator.ValidationResult;
import com.top_logic.base.security.password.hashing.PasswordHashingService;
import com.top_logic.base.security.password.hashing.VerificationResult;
import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.ListBinding;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.time.CalendarUtil;
import com.top_logic.dob.DataObject;
import com.top_logic.knowledge.objects.LifecycleAttributes;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.util.error.TopLogicException;

/**
 * {@link AuthenticationDevice} using password hashes stored in the database to authenticate users.
 * 
 * @author <a href="mailto:tri@top-logic.com">Thomas Richter</a>
 */
public class DBAuthenticationAccessDevice extends AbstractConfiguredInstance<SecurityDevice.Config<?>>
		implements AuthenticationDevice {

	private static final String PASSWORD_TABLE = "Password";

	private static final String ACCOUNT_REF = "account";

	private static final String PASSWORD_ATTR = "password";

	private static final String HISTORY_ATTR = "history";

	private static final String EXPIRED_ATTR = "expired";

	/**
	 * Configuration options for {@link DBAuthenticationAccessDevice}.
	 */
	public interface Config extends AuthenticationDevice.Config<DBAuthenticationAccessDevice> {

		/**
		 * Name of the resource with initial users to load.
		 */
		@ListBinding(attribute = "name")
		List<String> getInitialUserResources();

		/**
		 * The {@link PasswordValidator} to use.
		 */
		@Name("password-validator")
		PolymorphicConfiguration<? extends PasswordValidator> getPasswordValidator();
	}

	private final PasswordValidator _validator;

	private final KnowledgeBase _kb;

	/**
	 * Creates a new {@link DBAuthenticationAccessDevice} from the given configuration.
	 * 
	 * @param context
	 *        {@link InstantiationContext} to instantiate sub configurations.
	 * @param config
	 *        Configuration for this {@link DBAuthenticationAccessDevice}.
	 */
	public DBAuthenticationAccessDevice(InstantiationContext context, Config config) {
		super(context, config);
		_validator = context.getInstance(config.getPasswordValidator());
		_kb = PersistencyLayer.getKnowledgeBase();
	}

	@Override
	public PasswordValidator getPasswordValidator() {
		return _validator;
	}

	@Override
	public boolean authentify(LoginCredentials login) {
		VerificationResult verification = checkPassword(login.getPerson(), login.getPassword());
		return verification.success();
	}

	private VerificationResult checkPassword(Person account, final char[] password) {
		if (account == null) {
			Logger.info("Log-in attempt for unknown account denied.", DBAuthenticationAccessDevice.class);
			return VerificationResult.FAILED;
		}

		DataObject row = getPasswordData(account);
		if (row == null) {
			Logger.info("Log-in attempt for account without password denied: " + account.getName(),
				DBAuthenticationAccessDevice.class);
			return VerificationResult.FAILED;
		}

		String hash = (String) row.getAttributeValue(PASSWORD_ATTR);
		if (hash == null || hash.isBlank()) {
			Logger.info("Log-in attempt for account without empty password hash denied: " + account.getName(),
				DBAuthenticationAccessDevice.class);
			return VerificationResult.FAILED;
		}

		VerificationResult result = PasswordHashingService.getInstance().verify(password, hash);

		if (result.success()) {
			if (result.hasUpdatedHash()) {
				try (Transaction tx = _kb.beginTransaction()) {
					row.setAttributeValue(PASSWORD_ATTR, result.newHash());
					tx.commit();
				}
			}
		} else {
			Logger.info("Log-in attempt for account without wrong password denied: " + account.getName(),
				DBAuthenticationAccessDevice.class);
		}

		return result;
	}

	private DataObject getPasswordData(Person account) {
		return _kb.getObjectByAttribute(PASSWORD_TABLE, ACCOUNT_REF, account.tHandle());
	}

	@Override
	public boolean allowPwdChange() {
		return true;
	}

	/**
	 * Updates the password for the given account.
	 * 
	 * @param login
	 *        The {@link LoginCredentials} defining the new login.
	 * 
	 * @throws TopLogicException
	 *         If password does not match the configured criteria.
	 */
	public void setPassword(LoginCredentials login) {
		setPassword(login.getPerson(), login.getPassword());
	}

	@Override
	public void setPassword(Person account, char[] password) {
		ValidationResult validationResult = _validator.validatePassword(account, password);
		if (ValidationResult.OK != validationResult) {
			throw new TopLogicException(validationResult.messageKey());
		}

		DataObject data = getPasswordData(account);
		if (data == null) {
			data = account.tKnowledgeBase().createKnowledgeObject(PASSWORD_TABLE);
			data.setAttributeValue(ACCOUNT_REF, account.tHandle());
		} else {
			List<String> previousPwdHashes = getPWDHistory(data);
			if (alreadyUsed(previousPwdHashes, password)) {
				throw new TopLogicException(I18NConstants.PWD_USED_BEFORE);
			}

			String oldHash = (String) data.getAttributeValue(PASSWORD_ATTR);
			addPwdToHistory(data, oldHash);
		}

		data.setAttributeValue(PASSWORD_ATTR, PasswordHashingService.getInstance().createHash(password));
	}

	/**
	 * Expires the password for the given account (if password expiry is enabled for that account).
	 */
	@Override
	public void expirePassword(Person account) {
		DataObject data = getPasswordData(account);
		if (data == null) {
			return;
		}

		data.setAttributeValue(EXPIRED_ATTR, true);
	}

	@Override
	public boolean isPasswordChangeRequested(Person account, char[] password) {
		DataObject data = getPasswordData(account);
		if (isExpired(data)) {
			return true;
		}

		PasswordValidator validator = getPasswordValidator();
		if (validator.isExcluded(account)) {
			return false;
		}

		if (validator.validatePassword(account, password) != ValidationResult.OK) {
			return true;
		}

		int expiryPeriod = validator.getExpiryPeriod();
		if (expiryPeriod <= 0) {
			// Passwords never expire.
			return false;
		}

		long lastChange = getLastPasswordChange(data);
		if (lastChange == 0L) {
			// Password was not yet set.
			return true;
		}

		Calendar nextChangeTime = CalendarUtil.createCalendar(lastChange);
		nextChangeTime.add(Calendar.DAY_OF_YEAR, expiryPeriod);
		return System.currentTimeMillis() > nextChangeTime.getTimeInMillis();
	}

	private static boolean isExpired(DataObject data) {
		if (data == null) {
			return false;
		}
		Object value = data.getAttributeValue(EXPIRED_ATTR);
		return value != null && ((Boolean) value).booleanValue();
	}

	private long getLastPasswordChange(DataObject data) {
		if (data == null) {
			return 0L;
		}
		return (long) data.getAttributeValue(LifecycleAttributes.MODIFIED);
	}

	private boolean alreadyUsed(List<String> previousPwdHashes, char[] thePwd) {
		int repeatCycle = _validator.getPwdRepeatCycle();
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
	 * Adds a password hash to the persons password history.
	 */
	private void addPwdToHistory(DataObject data, String pwdHash) {
		List<String> pwdHistory = getPWDHistory(data);
		pwdHistory.add(pwdHash);
		int pwdRepeatCycle = _validator.getPwdRepeatCycle();
		while (pwdHistory.size() > pwdRepeatCycle) {
			pwdHistory.remove(0);
		}
		setPwdHistory(data, pwdHistory);
	}

	private void setPwdHistory(DataObject data, List<String> pwdHistory) {
		String pwdHashString = PasswordHistoryFormat.INSTANCE.format(pwdHistory);
		data.setAttributeValue(HISTORY_ATTR, pwdHashString);
	}

	private List<String> getPWDHistory(DataObject data) {
		String historyString = (String) data.getAttributeValue(HISTORY_ATTR);
		if (StringServices.isEmpty(historyString)) {
			return new ArrayList<>();
		}
		return parsePwdHistory(historyString);
	}

	private static List<String> parsePwdHistory(String historyString) {
		try {
			return (List<String>) PasswordHistoryFormat.INSTANCE.parseObject(historyString);
		} catch (ParseException ex) {
			Logger.error("Unable to parse password hashes '" + historyString + "'.", ex,
				DBAuthenticationAccessDevice.class);
			return new ArrayList<>();
		}
	}

}