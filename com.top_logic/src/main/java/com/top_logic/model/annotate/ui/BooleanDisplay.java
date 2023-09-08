/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.annotate.ui;

import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.model.annotate.TLAnnotation;
import com.top_logic.model.annotate.TLAttributeAnnotation;
import com.top_logic.model.annotate.TLTypeKind;
import com.top_logic.model.annotate.TargetType;
import com.top_logic.model.config.TLTypeAnnotation;

/**
 * {@link TLAnnotation} that defines display properties for {@link Boolean}-valued attributes.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@TagName("boolean-display")
@TargetType(TLTypeKind.BOOLEAN)
@InApp
public interface BooleanDisplay extends TLAttributeAnnotation, TLTypeAnnotation {

	/**
	 * Configuration name for the {@link #getPresentation()} property.
	 */
	String PRESENTATION_PROPERTY = "presentation";

	/**
	 * How to display this attribute by default.
	 * 
	 * @see BooleanPresentation
	 */
	@Name(PRESENTATION_PROPERTY)
	BooleanPresentation getPresentation();

	/**
	 * @see #getPresentation()
	 */
	void setPresentation(BooleanPresentation value);

	/**
	 * Creates a {@link BooleanDisplay} annotation with the given presentation value.
	 */
	static BooleanDisplay display(BooleanPresentation value) {
		BooleanDisplay result = TypedConfiguration.newConfigItem(BooleanDisplay.class);
		result.setPresentation(value);
		return result;
	}

}
