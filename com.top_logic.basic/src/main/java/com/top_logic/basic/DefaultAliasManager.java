/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic;

import java.io.File;
import java.util.Map;

/**
 * {@link AliasManager} retrieving alias definitions from {@link XMLProperties}.
 *
 * <p>
 * Values set in the application are stored in the file {@link AliasManager#STORED_VALUES_FILE} of
 * the {@link XMLProperties.Setting#resolveAutoconfFolder() autoconf folder}.
 * </p>
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DefaultAliasManager extends AliasManager {

	@Override
	public boolean usesXMLProperties() {
		return true;
	}

	@Override
	protected Map<String, String> resolveConfiguredAliases() {
		XMLProperties xProp = XMLProperties.getInstance();
		@SuppressWarnings({ "unchecked", "rawtypes" })
		Map<String, String> definitions = (Map) xProp.getAliases();
		return resolveDefinitions(definitions);
	}

	/**
	 * The file {@link AliasManager#STORED_VALUES_FILE} in the
	 * {@link XMLProperties.Setting#resolveAutoconfFolder() autoconf folder}, <code>null</code> if
	 * no external autoconf folder is configured and the application has no webapp folder in the
	 * file system.
	 */
	@Override
	public File getStoredValuesFile() {
		if (XMLProperties.Setting.getAutoconf() == null && FileManager.getInstance().getIDEPaths().isEmpty()) {
			return null;
		}
		return new File(XMLProperties.Setting.resolveAutoconfFolder(), STORED_VALUES_FILE);
	}

}
