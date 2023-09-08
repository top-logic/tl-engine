/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.annotate;

import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.util.InAppClassifierConstants;
import com.top_logic.layout.form.values.edit.annotation.RenderWholeLine;
import com.top_logic.model.config.TLTypeAnnotation;

/**
 * {@link TLAnnotation} to annotate whether an element is rendered over the whole line. So it
 * stretches over more than 1 column.
 * 
 * @see RenderWholeLine Corresponding annotation for {@link PropertyDescriptor} and
 *      {@link ConfigurationDescriptor}.
 * 
 * @author <a href="mailto:iwi@top-logic.com">Isabell Wittich</a>
 */
@TagName("render-whole-line")
@InApp(classifiers = { InAppClassifierConstants.FORM_RELEVANT })
public interface RenderWholeLineAnnotation extends TLAttributeAnnotation, TLTypeAnnotation, BooleanAnnotation {
	// Pure sum interface.
}
