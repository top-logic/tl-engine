/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.annotate;

import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.util.InAppClassifierConstants;
import com.top_logic.model.config.TLTypeAnnotation;

/**
 * {@link TLAnnotation} to annotate if the input is rendered before the label.
 * 
 * @author <a href="mailto:iwi@top-logic.com">Isabell Wittich</a>
 */
@TagName("input-before-label")
@InApp(classifiers = { InAppClassifierConstants.FORM_RELEVANT })
public interface RenderInputBeforeLabelAnnotation extends TLAttributeAnnotation, TLTypeAnnotation, BooleanAnnotation {
	// Pure sum interface.
}
