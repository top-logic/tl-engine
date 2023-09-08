/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.annotate.ui;

import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.layout.form.model.DataField;
import com.top_logic.model.annotate.StringAnnotation;
import com.top_logic.model.annotate.TLAnnotation;
import com.top_logic.model.annotate.TLAttributeAnnotation;
import com.top_logic.model.annotate.TLTypeKind;
import com.top_logic.model.annotate.TargetType;
import com.top_logic.model.config.TLTypeAnnotation;

/**
 * {@link TLAnnotation} that defines the accepted file types for file upload dialogs.
 * 
 * @author <a href="mailto:Diana.Pankratz@top-logic.com">Diana Pankratz</a>
 */
@TagName(TLAcceptedTypes.TAG_NAME)
@TargetType(TLTypeKind.BINARY)
@InApp
public interface TLAcceptedTypes extends StringAnnotation, TLAttributeAnnotation, TLTypeAnnotation {

	/** The shortcut XML tag for this {@link ConfigurationItem} type. */
	String TAG_NAME = "accepted-types";

	/**
	 * Comma separated list of accepted file types.
	 * 
	 * @see DataField#getAcceptedTypes()
	 */
	@Override
	String getValue();

}