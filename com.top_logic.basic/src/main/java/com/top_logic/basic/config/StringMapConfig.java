/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config;

import java.util.Map;

/**
 * Configuration wrapper for a {@link String} to String {@link Map}.
 * 
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public interface StringMapConfig extends NamedConfigMandatory {

	/**
	 * Value for the specific key.
	 */
	String getValue();
}
