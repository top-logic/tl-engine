/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.doc.misc;

import static com.top_logic.basic.shared.string.StringServicesShared.*;

import java.util.Locale;

import com.top_logic.base.context.TLSubSessionContext;
import com.top_logic.doc.model.Page;
import com.top_logic.knowledge.gui.WrapperResourceProvider;
import com.top_logic.layout.ResourceProvider;
import com.top_logic.layout.wysiwyg.ui.StructuredTextConfigService;
import com.top_logic.util.Resources;
import com.top_logic.util.TLContextManager;

/**
 * {@link ResourceProvider} for {@link Page}s that uses {@link Page#getTitle()} as label. It uses
 * the {@link Locale} of the {@link TLSubSessionContext} to determine the language of the title.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class PageResourceProvider extends WrapperResourceProvider {

	private Locale _language;

	/**
	 * Creates a {@link PageResourceProvider} without a language. The language will be defined by
	 * the subsession {@link Locale}.
	 */
	public PageResourceProvider() {
		this(null);
	}

	/**
	 * Creates a {@link PageResourceProvider} with a defined {@link Locale} for the language.
	 */
	public PageResourceProvider(Locale language) {
		_language = language;
	}

	@Override
	public String getLabel(Object model) {
		if (model == null) {
			return null;
		}
		Page page = (Page) model;
		Resources resources = getResources(page);
		String title = resources.getString(page.getTitle());
		if (isEmpty(title)) {
			return resources.getString(I18NConstants.PAGE_WITHOUT_TITLE);
		}
		return title;
	}

	/**
	 * Determines the {@link Resources} which are used to translate {@link Page#getTitle() title}.
	 * 
	 * <p>
	 * If no {@link #_language} is defined the language of the Provider will be determined by the
	 * {@link StructuredTextConfigService#LOCALE} or the subsession {@link Locale}.
	 * </p>
	 * 
	 * @param page
	 *        The page to get label for.
	 */
	private Resources getResources(Page page) {
		Locale language;
		if(_language == null) {
			language = TLContextManager.getSubSession().get(StructuredTextConfigService.LOCALE);
		} else {
			language = _language;
		}
		return (language == null) ? Resources.getInstance() : Resources.getInstance(language);
	}

}

