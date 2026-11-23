/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.wysiwyg.ui;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.top_logic.layout.LabelProvider;
import com.top_logic.layout.provider.label.NullSafeLabelProvider;

/**
 * {@link LabelProvider} for a {@link StructuredText}.
 * 
 * <p>
 * The label is the text without markup. A link to an object that the current user may not discover
 * is labeled by the placeholder of a masked element, see
 * {@link TLObjectLinkUtil#maskLinks(String, java.util.function.Function)}: a table cell, its sort
 * and filter, and an export by labels do not disclose the label of such an object. The label is
 * also the string conversion of a formatted text in TL-Script, so its result depends on the user.
 * </p>
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class StructuredTextLabelProvider extends NullSafeLabelProvider {

	@Override
	protected String getLabelNullSafe(Object model) {
		StructuredText text = (StructuredText) model;
		Document document = Jsoup.parse(text.getSourceCode());
		TLObjectLinkUtil.maskLinks(document, null);
		return document.wholeText();
	}

}
