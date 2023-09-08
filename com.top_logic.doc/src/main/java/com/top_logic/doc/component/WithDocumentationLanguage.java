/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.doc.component;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.util.Locale;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.layout.ModelSpec;
import com.top_logic.layout.channel.linking.impl.ChannelLinking;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.util.TLContext;

/**
 * {@link ConfigurationItem} for objects that need a {@link Locale} from a
 * {@link DocumentationTreeComponent}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface WithDocumentationLanguage extends ConfigurationItem {

	/** @see com.top_logic.basic.reflect.DefaultMethodInvoker */
	Lookup LOOKUP = MethodHandles.lookup();

	/**
	 * Language to operate with.
	 * 
	 * <p>
	 * The resolved model is expected to be a {@link Locale}.
	 * </p>
	 */
	ModelSpec getLanguage();

	/**
	 * Service method to resolve the language for this {@link WithDocumentationLanguage}.
	 * 
	 * <p>
	 * If {@link #getLanguage()} is set, is is resolved in context of the given component. If it is
	 * not set and the context is itself a {@link DocumentationTreeComponent}, the
	 * {@link DocumentationTreeComponent#getDocumentationLanguage() language} is returned. Otherwise
	 * an the {@link Locale} of the current user is returned.
	 * </p>
	 */
	default Locale resolveLanguage(LayoutComponent context) {
		if (getLanguage() != null) {
			return (Locale) ChannelLinking.eval(context, getLanguage());
		} else if (context instanceof DocumentationTreeComponent) {
			return ((DocumentationTreeComponent) context).getDocumentationLanguage();
		} else {
			return TLContext.getLocale();
		}

	}

}

