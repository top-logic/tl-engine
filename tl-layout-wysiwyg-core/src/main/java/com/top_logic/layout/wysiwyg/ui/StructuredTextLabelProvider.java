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
import com.top_logic.model.security.DiscoverRight;

/**
 * {@link LabelProvider} for a {@link StructuredText}.
 * 
 * <p>
 * The label is the text without markup. A link to an object that the current user may not discover
 * is labeled by the placeholder of a masked element, see
 * {@link TLObjectLinkUtil#maskLinks(String, java.util.function.Function)}: a table cell, its sort
 * and filter, an export by labels and every other display of the label do not disclose the label
 * of such an object. The label is also the string conversion of a formatted text in a secured
 * TL-Script, whose result therefore depends on the user.
 * </p>
 *
 * <p>
 * Only a computation on behalf of the system (see {@link DiscoverRight#isComputedForUser()}) gets
 * the text as it is stored: an unsecured script, for example a derived attribute or a scheduled
 * task, whose result can be stored and must not depend on the user who started it.
 * </p>
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class StructuredTextLabelProvider extends NullSafeLabelProvider {

	@Override
	protected String getLabelNullSafe(Object model) {
		StructuredText text = (StructuredText) model;
		Document document = Jsoup.parse(text.getSourceCode());
		if (DiscoverRight.isComputedForUser()) {
			TLObjectLinkUtil.maskLinks(document, null);
		}
		return document.wholeText();
	}

}
