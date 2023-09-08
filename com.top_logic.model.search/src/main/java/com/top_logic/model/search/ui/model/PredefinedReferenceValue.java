/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.ui.model;

import com.top_logic.basic.config.annotation.Hidden;
import com.top_logic.layout.form.template.util.SpanResourceDisplay;
import com.top_logic.layout.form.values.edit.annotation.UseTemplate;

/**
 * A {@link ReferenceValue} to a predefined value with an internationalized name.
 * 
 * @author <a href=mailto:jst@top-logic.com>Jan Stolzenburg</a>
 */
@UseTemplate(SpanResourceDisplay.class)
public interface PredefinedReferenceValue extends ReferenceValue, TranslatedSearchPart {

	@Hidden
	@Override
	String getName();

}
