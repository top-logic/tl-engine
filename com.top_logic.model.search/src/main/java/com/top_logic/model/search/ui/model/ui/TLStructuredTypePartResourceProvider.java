/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.ui.model.ui;

import com.top_logic.layout.Flavor;
import com.top_logic.layout.ResourceProvider;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.mig.html.DefaultResourceProvider;
import com.top_logic.model.TLStructuredTypePart;

/**
 * {@link ResourceProvider} for {@link TLStructuredTypePart}s.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TLStructuredTypePartResourceProvider extends TLNamedPartResourceProvider {

	/**
	 * Singleton {@link TLStructuredTypePartResourceProvider} instance.
	 */
	@SuppressWarnings("hiding")
	public static final TLStructuredTypePartResourceProvider INSTANCE = new TLStructuredTypePartResourceProvider();

	/**
	 * Creates a {@link TLStructuredTypePartResourceProvider}.
	 */
	protected TLStructuredTypePartResourceProvider() {
		super();
	}

	@Override
	public ThemeImage getImage(Object object, Flavor aFlavor) {
		return DefaultResourceProvider.getTypeImage(getType(object) + suffix(object), aFlavor);
	}

	/**
	 * Produce the suffix for the image type.
	 */
	protected String suffix(Object object) {
		TLStructuredTypePart reference = (TLStructuredTypePart) object;
		boolean mandatory = reference.isMandatory();
		return suffix(mandatory, "@mandatory");
	}

	/**
	 * Constructs a suffix based on a boolean property.
	 */
	protected static String suffix(boolean option, String suffix) {
		return option ? suffix : "";
	}

}
