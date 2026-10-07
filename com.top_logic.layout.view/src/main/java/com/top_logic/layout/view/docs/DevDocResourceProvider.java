/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.docs;

import com.top_logic.basic.docs.DevDoc;
import com.top_logic.layout.Flavor;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.mig.html.DefaultResourceProvider;

/**
 * Displays a {@link DevDoc} by its section number and title, with its description as tooltip and an
 * icon telling a chapter from an article.
 */
public class DevDocResourceProvider extends DefaultResourceProvider {

	/** Singleton {@link DevDocResourceProvider} instance. */
	@SuppressWarnings("hiding")
	public static final DevDocResourceProvider INSTANCE = new DevDocResourceProvider();

	private static final ThemeImage CHAPTER_ICON = ThemeImage.icon("css:bi bi-journal-bookmark");

	private static final ThemeImage ARTICLE_ICON = ThemeImage.icon("css:bi bi-file-earmark-text");

	/**
	 * Creates a {@link DevDocResourceProvider}.
	 */
	protected DevDocResourceProvider() {
		super();
	}

	@Override
	public String getLabel(Object object) {
		if (object instanceof DevDoc doc) {
			return doc.getNumber().isEmpty() ? doc.getTitle() : doc.getNumber() + " " + doc.getTitle();
		}
		return super.getLabel(object);
	}

	@Override
	public String getTooltip(Object object) {
		if (object instanceof DevDoc doc) {
			// A tooltip is HTML, a description may name tags such as <table>.
			return doc.getDescription().isEmpty() ? null : quote(doc.getDescription());
		}
		return super.getTooltip(object);
	}

	@Override
	public ThemeImage getImage(Object object, Flavor flavor) {
		if (object instanceof DevDoc doc) {
			return doc.isChapter() ? CHAPTER_ICON : ARTICLE_ICON;
		}
		return super.getImage(object, flavor);
	}

}
