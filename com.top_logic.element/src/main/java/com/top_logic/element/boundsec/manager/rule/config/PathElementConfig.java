/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.boundsec.manager.rule.config;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLReference;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLStructuredTypePart;

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

	/** Name of the value of {@link #isInverse()} in the configuration. */
	String XML_ATTRIBUTE_INVERSE = "inverse";

	/** Name of the value of {@link #getMetaElement()} in the configuration. */
	String XML_ATTRIBUTE_META_ELEMENT = "meta-element";

	/**
	 * Qualified name of the {@link TLStructuredType} in which {@link #getAttribute()} is defined.
	 */
	@Name(PathElementConfig.XML_ATTRIBUTE_META_ELEMENT)
	String getMetaElement();

	/**
	 * Name of the {@link TLStructuredTypePart} in {@link #getMetaElement()} over which is
	 * navigated.
	 */
	@Name(PathElementConfig.XML_ATTRIBUTE_ATTRIBUTE)
	String getAttribute();

	/**
	 * Whether {@link #getAttribute()} is navigated forwards or backwards.
	 * 
	 * <p>
	 * If <code>false</code>, then the {@link TLObject#tValue(TLStructuredTypePart) value} of the
	 * reference {@link #getAttribute()} is used; if <code>true</code>, then the
	 * {@link TLObject#tReferers(TLReference) referrers} of the reference {@link #getAttribute()} is
	 * used.
	 * </p>
	 */
	@Name(PathElementConfig.XML_ATTRIBUTE_INVERSE)
	boolean isInverse();

}

