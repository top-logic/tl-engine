/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.boundsec.manager.rule.config;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.Name;

/**
 * Configuration of a step in the {@link RoleRuleConfig}.
 * 
 * @see RoleRuleConfig#getPathElements()
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface PathElementConfig extends ConfigurationItem {

	/** Name of the value of {@link #getAttribute()} in the configuration. */
	String XML_ATTRIBUTE_ATTRIBUTE = "attribute";

	/** Name of the value of {@link #getAssociation()} in the configuration. */
	String XML_ATTRIBUTE_ASSOCIATION = "association";

	/** Name of the value of {@link #isInverse()} in the configuration. */
	String XML_ATTRIBUTE_INVERSE = "inverse";

	/** Name of the value of {@link #getMetaElement()} in the configuration. */
	String XML_ATTRIBUTE_META_ELEMENT = "meta-element";

	@Name(PathElementConfig.XML_ATTRIBUTE_META_ELEMENT)
	String getMetaElement();

	@Name(PathElementConfig.XML_ATTRIBUTE_ATTRIBUTE)
	String getAttribute();

	@Name(PathElementConfig.XML_ATTRIBUTE_ASSOCIATION)
	String getAssociation();

	@Name(PathElementConfig.XML_ATTRIBUTE_INVERSE)
	boolean isInverse();

}

