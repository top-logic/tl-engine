/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.annotate;

import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.treexf.Expr;
import com.top_logic.basic.util.InAppClassifierConstants;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.ScriptFunction3;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;

/**
 * {@link TLAnnotation} to annotate an {@link Expr} computing the label based on the displayed
 * object.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@TagName("dynamic-label")
@InApp(classifiers = { InAppClassifierConstants.FORM_RELEVANT })
public interface DynamicLabel extends TLAttributeAnnotation {

	/** Configuration name of {@link #getLabel()}. */
	String LABEL = "label";

	/**
	 * Expression computing the label based on the base object.
	 * 
	 * <p>
	 * The first argument for the label expression is the base object, the second argument is the
	 * default label, and the third object the annotated {@link TLStructuredTypePart}. The returned
	 * value can be e.g. a {@link ResKey} or a {@link String}.
	 * </p>
	 */
	@Mandatory
	@Name(LABEL)
	ScriptFunction3<Object, TLObject, ResKey, TLStructuredTypePart> getLabel();

}
