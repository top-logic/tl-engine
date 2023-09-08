/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.boundsec.manager.rule.config;

import java.util.List;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.EntryTag;
import com.top_logic.basic.config.annotation.Name;

/**
 * {@link ConfigurationItem} defining all definitions of role rules in the application.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface RoleRulesConfig extends ConfigurationItem {

	/**
	 * Name of the entry tags of attribute {@link #getRules()}.
	 * 
	 * @see #XML_TAG_RULES
	 */
	String XML_TAG_RULE = "rule";

	/**
	 * Name of the value of {@link #getRules()} in the configuration.
	 * 
	 * @see #XML_TAG_RULE
	 */
	String XML_TAG_RULES = "rules";

	/**
	 * Default tag name to serialise the role rules.
	 */
	String XML_TAG_ROLE_RULES = "role-rules";

	/**
	 * The list of known {@link RoleRuleConfig role rule configuration}s.
	 */
	@Name(RoleRulesConfig.XML_TAG_RULES)
	@EntryTag(RoleRulesConfig.XML_TAG_RULE)
	List<RoleRuleConfig> getRules();

}

