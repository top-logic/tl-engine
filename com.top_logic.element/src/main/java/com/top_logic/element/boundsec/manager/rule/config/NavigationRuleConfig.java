/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */

package com.top_logic.element.boundsec.manager.rule.config;

import java.util.List;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.EntryTag;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.model.TLClass;

/**
 * Configuration to navigate from an object of a configured type to some target objects.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface NavigationRuleConfig extends ConfigurationItem {

	/** Name of the value of {@link #getMetaElement()} in the configuration. */
	String XML_ATTRIBUTE_META_ELEMENT = "meta-element";

	/** Name of the value of {@link #isInherit()} in the configuration. */
	String XML_ATTRIBUTE_INHERIT = "inherit";

	/**
	 * Name of the value of {@link #getPathElements()} in the configuration.
	 * 
	 * @see #XML_TAG_STEP_ELEMENT
	 */
	String XML_TAG_PATH_ELEMENT = "path";

	/**
	 * Name of the entry tags of attribute {@link #getPathElements()}.
	 * 
	 * @see #XML_TAG_PATH_ELEMENT
	 */
	String XML_TAG_STEP_ELEMENT = "step";

	/**
	 * Full qualified name of the {@link TLClass} that an object must have such that
	 * {@link NavigationRuleConfig} is applied.
	 */
	@Name(RoleRuleConfig.XML_ATTRIBUTE_META_ELEMENT)
	@Nullable
	String getMetaElement();

	/**
	 * Whether this {@link NavigationRuleConfig} should also be applied to all sub types of
	 * {@link #getMetaElement()}.
	 */
	@Name(RoleRuleConfig.XML_ATTRIBUTE_INHERIT)
	boolean isInherit();

	/**
	 * The configuration of the steps to get from the source object to the target object.
	 */
	@Name(RoleRuleConfig.XML_TAG_PATH_ELEMENT)
	@EntryTag(RoleRuleConfig.XML_TAG_STEP_ELEMENT)
	List<PathElementConfig> getPathElements();

}

