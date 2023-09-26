/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.annotate;

import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.util.InAppClassifierConstants;
import com.top_logic.model.config.TLTypeAnnotation;

/**
 * Determination of the position of the label in relation to the value.
 * 
 * @author <a href="mailto:iwi@top-logic.com">Isabell Wittich</a>
 */
@TagName("label-position")
@InApp(classifiers = { InAppClassifierConstants.FORM_RELEVANT })
@Label("Label position")
public interface LabelPositionAnnotation extends TLAttributeAnnotation, TLTypeAnnotation {

	/**
	 * The position of the label in relation to the value.
	 */
	LabelPosition getValue();

}
