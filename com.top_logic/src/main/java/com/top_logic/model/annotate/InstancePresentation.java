/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.annotate;

import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.order.DisplayOrder;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.model.annotate.AnnotationInheritance.Policy;
import com.top_logic.model.config.TLTypeAnnotation;

/**
 * Configuration options for specifying the presentation of instances.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@TagName(InstancePresentation.TAG_NAME)
@TargetType(value = { TLTypeKind.REF, TLTypeKind.COMPOSITION, TLTypeKind.ENUMERATION })
// Generalizations are considered explicitly by code using this annotation.
@AnnotationInheritance(Policy.REDEFINE)
@InApp
@DisplayOrder({
	InstancePresentation.ICON,
	InstancePresentation.LARGE_ICON,
	InstancePresentation.EXPANDED_ICON,
})
public interface InstancePresentation extends TLTypeAnnotation, TLClassifierAnnotation {

	/**
	 * Name of the tag in parent annotations element to define a {@link InstancePresentation}.
	 */
	String TAG_NAME = "instance-presentation";

	/** Property name of {@link InstancePresentation#getIcon()}. */
	String ICON = "icon";

	/** Property name of {@link InstancePresentation#getLargeIcon()}. */
	String LARGE_ICON = "large-icon";

	/** Property name of {@link InstancePresentation#getExpandedIconOrEmpty()}. */
	String EXPANDED_ICON = "expanded-icon";

	/**
	 * Returns the icon when element is closed in tree.
	 */
	@Name(ICON)
	ThemeImage getIcon();

	/**
	 * Set the icon to be used in UI when the element is in a closed state.
	 * 
	 * @param anIcon
	 *        The icon to set.
	 */
	void setIcon(ThemeImage anIcon);

	/**
	 * Returns the large icon to use in title bars.
	 */
	@Name(LARGE_ICON)
	ThemeImage getLargeIcon();

	/**
	 * @see #getLargeIcon()
	 */
	void setLargeIcon(ThemeImage anIcon);

	/**
	 * Returns the icon when element is opened in tree.
	 */
	@Name(EXPANDED_ICON)
	ThemeImage getExpandedIconOrEmpty();

	/**
	 * Set the icon to be used in UI when the element is in a closed state.
	 * 
	 * @param anIcon
	 *        The icon to set.
	 */
	void setExpandedIconOrEmpty(ThemeImage anIcon);

}
