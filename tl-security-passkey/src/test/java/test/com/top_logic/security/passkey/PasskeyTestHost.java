/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.security.passkey;

import java.util.Objects;

import com.yubico.webauthn.data.UserVerificationRequirement;

import com.top_logic.basic.AliasManager;
import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.module.ModuleUtil;
import com.top_logic.basic.module.RestartException;
import com.top_logic.security.passkey.PasskeyService;
import com.top_logic.security.passkey.PasskeyUsage;

/**
 * Sets the public address of the application ({@link AliasManager#HOST}) the
 * {@link PasskeyService} of a test takes its relying party from.
 *
 * <p>
 * The alias is defined from the variable {@link PasskeyService#HOST_VARIABLE}, which is read from
 * a system property of that name as well. Setting the property, reloading the
 * {@link AliasManager} and restarting the {@link PasskeyService} gives the service the address.
 * </p>
 *
 * <p>
 * Further options of the service, the {@link PasskeyUsage usage} of the passkeys and the
 * configured user verification, are set in the configuration of the service, which is restarted
 * with them.
 * </p>
 */
public class PasskeyTestHost {

	/** The host of the public address of the tests. */
	public static final String HOST = "localhost";

	/** The port of the public address of the tests. */
	public static final int PORT = 8080;

	/** The origin of the public address of the tests, the page origin of their ceremonies. */
	public static final String ORIGIN = "http://" + HOST + ":" + PORT;

	/** The address the service was last restarted with by {@link #use(String)}. */
	private static String _current;

	/**
	 * Sets the variable {@link PasskeyService#HOST_VARIABLE} for the start of the services of a
	 * test suite, to {@link #ORIGIN}.
	 */
	public static void init() {
		System.setProperty(PasskeyService.HOST_VARIABLE, ORIGIN);
	}

	/**
	 * Makes the {@link PasskeyService} use the given public address, restarting it if it does not
	 * already.
	 *
	 * @param host
	 *        The value of {@link AliasManager#HOST}; empty for no public address.
	 */
	public static void use(String host) throws RestartException {
		if (host.equals(_current)) {
			return;
		}
		System.setProperty(PasskeyService.HOST_VARIABLE, host);
		AliasManager.getInstance().reload();
		ModuleUtil.INSTANCE.restart(PasskeyService.Module.INSTANCE, null);
		_current = host;
	}

	/**
	 * Makes the {@link PasskeyService} use the given usage of the passkeys, restarting it if it
	 * does not already.
	 */
	public static void use(PasskeyUsage usage) throws RestartException, ConfigurationException {
		configure(PasskeyService.Config.USAGE, usage);
	}

	/**
	 * Makes the {@link PasskeyService} use the given configured user verification, restarting it
	 * if it does not already.
	 */
	public static void use(UserVerificationRequirement userVerification)
			throws RestartException, ConfigurationException {
		configure(PasskeyService.Config.USER_VERIFICATION, userVerification);
	}

	private static void configure(String propertyName, Object value)
			throws RestartException, ConfigurationException {
		PasskeyService.Config<?> config = (PasskeyService.Config<?>) ApplicationConfig.getInstance()
			.getServiceConfiguration(PasskeyService.class);
		PropertyDescriptor property = config.descriptor().getProperty(propertyName);
		if (Objects.equals(value, config.value(property))) {
			return;
		}
		config.update(property, value);
		ModuleUtil.INSTANCE.restart(PasskeyService.Module.INSTANCE, null);
	}

	/**
	 * Makes the {@link PasskeyService} use {@link #ORIGIN} as public address, the usage
	 * {@link PasskeyUsage#LOGIN} and the configured user verification
	 * {@link UserVerificationRequirement#REQUIRED}.
	 */
	public static void useDefault() throws RestartException, ConfigurationException {
		use(ORIGIN);
		use(PasskeyUsage.LOGIN);
		use(UserVerificationRequirement.REQUIRED);
	}

}
