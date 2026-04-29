/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.mcp;

import com.top_logic.basic.config.NamedConfigMandatory;
import com.top_logic.basic.config.annotation.Name;

/**
 * A named string value for use in config maps.
 *
 * <p>
 * Used for environment variables and HTTP headers in MCP transport configs. In XML:
 * {@code <entry name="KEY" value="VALUE"/>}
 * </p>
 */
public interface NamedValueConfig extends NamedConfigMandatory {

	/** Configuration property name for {@link #getValue()}. */
	String VALUE = "value";

	/**
	 * The value.
	 */
	@Name(VALUE)
	String getValue();

}
