/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.ui.model.ui;

import com.top_logic.layout.Flavor;
import com.top_logic.layout.LabelProvider;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.mig.html.DefaultResourceProvider;
import com.top_logic.model.TLNamedPart;
import com.top_logic.model.annotate.TLI18NKey;
import com.top_logic.util.Resources;

/**
 * {@link LabelProvider} for {@link TLNamedPart}s based on their {@link TLI18NKey} annotation.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TLNamedPartResourceProvider extends DefaultResourceProvider {

	/**
	 * Singleton {@link TLNamedPartResourceProvider} instance.
	 */
	@SuppressWarnings("hiding")
	public static final TLNamedPartResourceProvider INSTANCE = new TLNamedPartResourceProvider();

	/**
	 * Creates a {@link TLNamedPartResourceProvider}.
	 */
	protected TLNamedPartResourceProvider() {
		super();
	}

	@Override
	public String getLabel(Object object) {
		return getInternationalizedName((TLNamedPart) object);
	}

	/**
	 * The internationalized name for a {@link TLNamedPart}.
	 */
	public static String getInternationalizedName(TLNamedPart part) {
		TLI18NKey i18nAnnotation = part.getAnnotation(TLI18NKey.class);
		if (i18nAnnotation != null) {
			return Resources.getInstance().getString(i18nAnnotation.getValue());
		} else {
			String internalName = part.getName();
			if (internalName == null) {
				return "<anonymous>";
			} else {
				return internalName;
			}
		}
	}

	@Override
	public ThemeImage getImage(Object anObject, Flavor aFlavor) {
		return DefaultResourceProvider.getTypeImage(this.getType(anObject), aFlavor);
	}

}