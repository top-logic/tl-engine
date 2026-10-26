/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.wysiwyg.ui;

import org.jsoup.Jsoup;

import com.top_logic.layout.LabelProvider;
import com.top_logic.layout.provider.label.NullSafeLabelProvider;

/**
 * {@link LabelProvider} for a {@link StructuredText}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class StructuredTextLabelProvider extends NullSafeLabelProvider {

	@Override
	protected String getLabelNullSafe(Object model) {
		StructuredText text = (StructuredText) model;
		return Jsoup.parse(text.getSourceCode()).wholeText();
	}

}
