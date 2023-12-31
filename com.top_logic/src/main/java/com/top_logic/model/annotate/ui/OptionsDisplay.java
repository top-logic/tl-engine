/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.annotate.ui;

import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.util.InAppClassifierConstants;
import com.top_logic.model.annotate.TLAnnotation;
import com.top_logic.model.annotate.TLAttributeAnnotation;
import com.top_logic.model.annotate.TLTypeKind;
import com.top_logic.model.annotate.TargetType;
import com.top_logic.model.config.TLTypeAnnotation;

/**
 * {@link TLAnnotation} that defines how options are presented in a pop-up dialog for selecting
 * values.
 * 
 * <p>
 * This annotation is only relevant, if the reference is edited by a
 * {@link ReferencePresentation#POP_UP pop-up dialog}. To achieve this, the reference may need to be
 * annotated with {@link ReferenceDisplay} selecting {@link ReferencePresentation#POP_UP} as
 * presentation.
 * </p>
 * 
 * @see ReferencePresentation#POP_UP
 * @see ReferencePresentation#TABLE
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@Label("Presentation in select dialog")
@TagName("options-display")
@InApp(classifiers = { InAppClassifierConstants.FORM_RELEVANT })
@TargetType(value = { TLTypeKind.REF, TLTypeKind.COMPOSITION })
public interface OptionsDisplay extends TLAttributeAnnotation, TLTypeAnnotation {

	/**
	 * Property name of {@link #getValue()}.
	 */
	String VALUE = "value";

	/**
	 * Chooses the way to display options in choose dialog.
	 */
	@Label("Presentation")
	@Name(VALUE)
	OptionsPresentation getValue();

	/**
	 * @see #getValue()
	 */
	void setValue(OptionsPresentation value);

}

