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
import com.top_logic.basic.util.InAppClassifierConstants;
import com.top_logic.model.TLReference;
import com.top_logic.model.annotate.TLAnnotation;
import com.top_logic.model.annotate.TLAttributeAnnotation;
import com.top_logic.model.annotate.TLTypeKind;
import com.top_logic.model.annotate.TargetType;
import com.top_logic.model.config.TLTypeAnnotation;

/**
 * {@link TLAnnotation} that defines display properties of {@link TLReference references}.
 * 
 * @see OptionsPresentation
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@TagName("reference-display")
@InApp(classifiers = { InAppClassifierConstants.FORM_RELEVANT })
@TargetType(value = { TLTypeKind.REF, TLTypeKind.COMPOSITION })
public interface ReferenceDisplay extends TLAttributeAnnotation, TLTypeAnnotation {

	/**
	 * Property name of {@link #getValue()}.
	 */
	String VALUE = "value";

	/**
	 * Chooses the way to display values.
	 */
	@Name(VALUE)
	ReferencePresentation getValue();

	/**
	 * @see #getValue()
	 */
	void setValue(ReferencePresentation value);

	/**
	 * Creates a {@link ReferenceDisplay} annotation with the given presentation value set.
	 */
	static ReferenceDisplay display(ReferencePresentation presentation) {
		ReferenceDisplay result = TypedConfiguration.newConfigItem(ReferenceDisplay.class);
		result.setValue(presentation);
		return result;
	}

}
