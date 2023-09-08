/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.migration;

import java.util.Map;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.Indexed;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Name;

/**
 * Descriptor for an application data version.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface VersionDescriptor extends ConfigurationItem {

	/**
	 * @see #getModuleVersions()
	 */
	String MODULE_VERSIONS = "module-versions";

	/**
	 * {@link Version} descriptors for each module.
	 */
	@Name(MODULE_VERSIONS)
	@DefaultContainer
	@Key(Version.MODULE_ATTRIBUTE)
	Map<String, Version> getModuleVersions();

	/**
	 * @see #getModuleVersions()
	 */
	void setModuleVersions(Map<String, Version> versions);

	/**
	 * Access to the {@link Version} of a given module.
	 * 
	 * @see #getModuleVersions()
	 */
	@Indexed(collection = MODULE_VERSIONS)
	Version getModuleVersion(String module);

}
