/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.boundsec.manager.rule.config;

import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.element.boundsec.manager.rule.PathNavigation;
import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLReference;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.util.AllReferences;
import com.top_logic.model.util.TLModelPartRef;

/**
 * Configuration of a step in the {@link RoleRuleConfig}.
 * 
 * @see RoleRuleConfig#getPathElements()
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@TagName("step")
public interface PathElementConfig extends PolymorphicConfiguration<PathNavigation> {

	/** Name of the value of {@link #getAttribute()} in the configuration. */
	String XML_ATTRIBUTE_ATTRIBUTE = "attribute";

	/** Name of the value of {@link #isInverse()} in the configuration. */
	String XML_ATTRIBUTE_INVERSE = "inverse";

	/**
	 * Qualified name of the {@link TLReference} over which is navigated.
	 */
	@Name(PathElementConfig.XML_ATTRIBUTE_ATTRIBUTE)
	@Mandatory
	@Options(fun = NavigableReferences.class, mapping = TLModelPartRef.PartMapping.class)
	TLModelPartRef getAttribute();

	/**
	 * {@link AllReferences} offering only the {@link TLReference}s that a {@link PathNavigation} can
	 * navigate.
	 *
	 * <p>
	 * A derived (computed) reference is excluded, since it does not fire change notifications and
	 * therefore cannot be tracked for role rule invalidation. Note that an abstract reference is
	 * derived, too.
	 * </p>
	 */
	class NavigableReferences extends AllReferences {

		@Override
		protected boolean acceptPart(TLStructuredTypePart part) {
			return super.acceptPart(part) && !part.isDerived();
		}

	}

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

