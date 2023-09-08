/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config;

import com.top_logic.basic.Protocol;

/**
 * Part of a {@link AbstractConfigurationDescriptor#resolve(Protocol)} process.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
interface ResolverPart {

	/**
	 * Resolves this part of the given {@link ConfigurationDescriptor}.
	 */
	void resolve(Protocol protocol, ConfigurationDescriptor descriptor);

}
