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
 * The label is the text without markup, as it is stored. Only in a computation on behalf of the
 * current user (see {@link DiscoverRight#isComputedForUser()}), a link to an object that the user
 * may not discover is labeled by the placeholder of a masked element, see
 * {@link TLObjectLinkUtil#maskLinks(String, java.util.function.Function)}. Such a computation is a
 * secured TL-Script evaluation, whose string conversion of a formatted text thereby depends on the
 * user, and the text of a table cell of the React view layer, which the cell is sorted, filtered
 * and exported by.
 * </p>
 *
 * <p>
 * The label provider does not mask in every computation: the label is also the string conversion
 * of an unsecured script, for example a derived attribute or a scheduled task, whose result can be
 * stored and must not depend on the user who started the computation.
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
