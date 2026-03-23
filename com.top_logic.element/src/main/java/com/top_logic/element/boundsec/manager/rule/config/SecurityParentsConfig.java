/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.boundsec.manager.rule.config;

import java.util.List;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.EntryTag;
import com.top_logic.basic.config.annotation.Name;

/**
 * {@link ConfigurationItem} defining the security parents in the application.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface SecurityParentsConfig extends ConfigurationItem {

	/**
	 * Name of the entry tags of attribute {@link #getRules()}.
	 */
	String RULE = "rule";

	/**
	 * Configuration name for {@link #getRules()} in the configuration.
	 */
	String RULES = "rules";

	/**
	 * The list of known {@link NavigationRuleConfig security rule} configurations.
	 */
	@Name(RULES)
	@EntryTag(RULE)
	@DefaultContainer
	List<RoleRuleConfig> getRules();

}

