/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.recorder.ref;

import com.top_logic.basic.config.ConfigurationItem;

/**
 * Base interface for structured model names of {@link NamedModel}s.
 * 
 * <p>
 * {@link ModelName}s are created by {@link ModelNamingScheme}s and resolved to models by the
 * {@link ModelResolver}.
 * </p>
 * 
 * @see NamedModel
 * @see ModelNamingScheme
 * @see ModelResolver
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface ModelName extends ConfigurationItem {

	// Pure root interface.

}
