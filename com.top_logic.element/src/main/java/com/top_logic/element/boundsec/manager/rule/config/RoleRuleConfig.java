/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.boundsec.manager.rule.config;

import java.util.List;

import com.top_logic.basic.config.CommaSeparatedStrings;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.util.ResKey;
import com.top_logic.element.boundsec.manager.rule.RoleProvider.Type;
import com.top_logic.model.TLClass;

/**
 * Configuration of a role rule.
 * 
 * @see RoleRulesConfig#getRules()
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface RoleRuleConfig extends NavigationRuleConfig {

	/** Name of the value of {@link #getSourceMetaElement()} in the configuration. */
	String XML_ATTRIBUTE_SOURCE_META_ELEMENT = "source-meta-element";

	/** Name of the value of {@link #getRole()} in the configuration. */
	String XML_ATTRIBUTE_ROLE = "role";

	/** Name of the value of {@link #getSourceRole()} in the configuration. */
	String XML_ATTRIBUTE_SOURCE_ROLE = "source-role";

	/** Name of the value of {@link #getResKey()} in the configuration. */
	String XML_ATTRIBUTE_RESOURCE_KEY = "resource-key";

	/** Name of the value of {@link #getType()} in the configuration. */
	String XML_ATTRIBUTE_TYPE = "type";

	/** Name of the value of {@link #getBase()} in the configuration. */
	String XML_ATTRIBUTE_BASE = "base";

	/**
	 * The user gets the {@link #getRole()} on the target elements.
	 */
	@Override
	List<PathElementConfig> getPathElements();

	/**
	 * Full qualified name of the {@link TLClass} to that an object must have to get the given
	 * {@link #getRole()}.
	 */
	@Override
	String getMetaElement();

	/**
	 * Optional name of the {@link TLClass} which the source object must have to get the given
	 * {@link #getRole()}.
	 */
	@Name(RoleRuleConfig.XML_ATTRIBUTE_SOURCE_META_ELEMENT)
	@Nullable
	String getSourceMetaElement();

	/**
	 * Target roles which a user gets on the target objects.
	 */
	@Format(CommaSeparatedStrings.class)
	@Name(RoleRuleConfig.XML_ATTRIBUTE_ROLE)
	List<String> getRole();

	/**
	 * If the user has one of the role on the source object, it gets the {@link #getRole() target
	 * role} on the target object.
	 */
	@Format(CommaSeparatedStrings.class)
	@Name(RoleRuleConfig.XML_ATTRIBUTE_SOURCE_ROLE)
	List<String> getSourceRole();

	/**
	 * Which kind of rule is this?
	 */
	@Name(RoleRuleConfig.XML_ATTRIBUTE_TYPE)
	Type getType();

	/**
	 * Definition of the "base" object to copy roles from.
	 */
	@Name(RoleRuleConfig.XML_ATTRIBUTE_BASE)
	String getBase();

	/**
	 * {@link ResKey} defining the internationalisation for the rule.
	 */
	@Name(RoleRuleConfig.XML_ATTRIBUTE_RESOURCE_KEY)
	ResKey getResKey();
}

