/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.json.config;

import java.util.List;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.Abstract;

/**
 * Abstract interface for a json node value. A {@link JSONValue} could be a {@link String},
 * {@link Integer}, {@link Float}, {@link Boolean}, {@link List} or a {@link JSONObject}.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
@Abstract
public interface JSONValue extends ConfigurationItem {
	// Abstract interface for a json value.
}
