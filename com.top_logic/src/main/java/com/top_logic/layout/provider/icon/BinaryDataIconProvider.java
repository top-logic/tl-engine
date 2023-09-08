/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.provider.icon;

import com.top_logic.basic.io.binary.BinaryDataSource;
import com.top_logic.dsa.util.MimeTypes;
import com.top_logic.layout.Flavor;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.mig.html.DefaultResourceProvider;
import com.top_logic.util.TLMimeTypes;

/**
 * {@link IconProvider} determining the Icon for a given {@link BinaryDataSource}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class BinaryDataIconProvider implements IconProvider {

	/** Singleton {@link BinaryDataIconProvider} instance. */
	public static final BinaryDataIconProvider INSTANCE = new BinaryDataIconProvider();

	/**
	 * Creates a new {@link BinaryDataIconProvider}.
	 */
	protected BinaryDataIconProvider() {
		// singleton instance
	}

	@Override
	public ThemeImage getIcon(Object object, Flavor flavor) {
		if (!(object instanceof BinaryDataSource)) {
			return null;
		}
		BinaryDataSource data = (BinaryDataSource) object;
		String mimeType = MimeTypes.getInstance().getMimeType(data.getName());
		return DefaultResourceProvider.getTypeImage(mimeType, flavor, TLMimeTypes.unknownIcon());
	}

}

