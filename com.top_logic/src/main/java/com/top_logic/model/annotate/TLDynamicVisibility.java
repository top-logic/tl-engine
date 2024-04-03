/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.annotate;

import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.TagName;

/**
 * Annotation to dynamically select a field visibility and mode for displaying an attribute in a
 * form based on the values of other attribute values.
 */
@TagName("dynamic-visibility")
@InApp
public interface TLDynamicVisibility extends TLAttributeAnnotation {

	/**
	 * Algorithm to compute the visibility of the form member.
	 */
	@Mandatory
	PolymorphicConfiguration<ModeSelector> getModeSelector();

}
