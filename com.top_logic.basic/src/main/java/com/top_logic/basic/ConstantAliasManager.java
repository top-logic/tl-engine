/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic;

import java.util.Map;

/**
 * {@link AliasManager} that is statically initialized with given alias definitions.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ConstantAliasManager extends AliasManager {

	private Map<String, String> _aliasDefinitions;

	/**
	 * Creates a {@link ConstantAliasManager}.
	 */
	public ConstantAliasManager(Map<String, String> aliasDefinitions) {
		_aliasDefinitions = aliasDefinitions;
	}

	@Override
	public boolean usesXMLProperties() {
		return false;
	}

	@Override
	protected Map<String, String> resolveConfiguredAliases() {
		return _aliasDefinitions;
	}

}
