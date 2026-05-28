/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */

package com.top_logic.layout.component.configuration;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.defaults.IntDefault;

/**
 * Configuration for time-based one-time password (TOTP) multi-factor authentication.
 *
 * @author    <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface MFAConfig extends ConfigurationItem {

	/**
	 * The number of digits in the generated one-time password.
	 */
	@IntDefault(6)
	int getDigits();

	/**
	 * The time period in seconds for which a generated one-time password is valid.
	 */
	@IntDefault(30)
	int getPeriod();

	/**
	 * The number of adjacent time periods (before and after the current one) that are also
	 * accepted during validation, to compensate for clock drift between client and server.
	 */
	@IntDefault(1)
	int getAllowedTimePeriodDiscrepancy();

}

