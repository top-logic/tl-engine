/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.component;

import com.top_logic.basic.Logger;
import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.IntDefault;

/**
 * Configuration of the lock time for {@link EditComponent}s.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class LockTimeConfiguration {

	private static final int NOT_INITIALIZED = Integer.MIN_VALUE;

	public interface Config extends ConfigurationItem {

		int DEFAULT_MILLIS_DEFAULT = 30 * 60 * 1000; // 30 minutes

		int MAX_MILLIS_DEFAULT = 60 * 60 * 1000; // 60 minutes

		int MIN_MILLIS_DEFAULT = 1000; // 1 second;

		String MIN_MILLIS_NAME = "min-millis";

		String MAX_MILLIS_NAME = "max-millis";

		String DEFAULT_MILLIS_NAME = "default-millis";

		@IntDefault(DEFAULT_MILLIS_DEFAULT)
		@Name(DEFAULT_MILLIS_NAME)
		int getDefaultMillis();

		@IntDefault(MAX_MILLIS_DEFAULT)
		@Name(MAX_MILLIS_NAME)
		int getMaxMillis();

		@IntDefault(MIN_MILLIS_DEFAULT)
		@Name(MIN_MILLIS_NAME)
		int getMinMillis();

	}

	private static volatile int MIN_LOCK_MILLIS;

	private static volatile int MAX_LOCK_MILLIS;

	private static volatile int DEFAULT_LOCK_MILLIS = NOT_INITIALIZED;

	public static int getMinLockMillis() {
		init();
		return MIN_LOCK_MILLIS;
	}

	public static int getMaxLockMillis() {
		init();
		return MAX_LOCK_MILLIS;

	}

	public static int getDefaultLockMillis() {
		init();
		return DEFAULT_LOCK_MILLIS;
	}

	private static void init() {
		if (DEFAULT_LOCK_MILLIS != NOT_INITIALIZED) {
			return;
		}
		Config config = ApplicationConfig.getInstance().getConfig(Config.class);
		int minMillis = config.getMinMillis();
		int maxMillis = config.getMaxMillis();
		int defaultMillis = config.getDefaultMillis();
		if (!configurationValid(minMillis, maxMillis, defaultMillis)) {
			minMillis = Config.MIN_MILLIS_DEFAULT;
			maxMillis = Config.MAX_MILLIS_DEFAULT;
			defaultMillis = Config.DEFAULT_MILLIS_DEFAULT;
		}
		MIN_LOCK_MILLIS = minMillis;
		MAX_LOCK_MILLIS = maxMillis;
		/* Attention: Set DEFAULT_LOCK_MILLIS as last, because it is used as check for "values are
		 * initialised." */
		DEFAULT_LOCK_MILLIS = defaultMillis;
	}

	private static boolean configurationValid(int minMillis, int maxMillis, int defaultMillis) {
		boolean valid = true;
		if (NOT_INITIALIZED == defaultMillis ) {
			Logger.error(Config.DEFAULT_MILLIS_NAME + " must not be " + defaultMillis + " as this is a reserved value.",
				LockTimeConfiguration.class);
			valid = false;
		}
		if (minMillis > defaultMillis ) {
			Logger.error(Config.MIN_MILLIS_NAME + "(" + minMillis + ") must not be greater than "
				+ Config.DEFAULT_MILLIS_NAME + "(" + defaultMillis + ")", LockTimeConfiguration.class);
			valid = false;
		}
		if (maxMillis < defaultMillis) {
			Logger.error(Config.MAX_MILLIS_NAME + "(" + maxMillis + ") must not be less than "
				+ Config.DEFAULT_MILLIS_NAME + "(" + defaultMillis + ")", LockTimeConfiguration.class);
			valid = false;
		}
		if (minMillis < Config.MIN_MILLIS_DEFAULT) {
			Logger.error(
				Config.MIN_MILLIS_NAME + "(" + minMillis + ") must not be less than " + Config.MIN_MILLIS_DEFAULT,
				LockTimeConfiguration.class);
			valid = false;
		}
		return valid; 
	}
}
