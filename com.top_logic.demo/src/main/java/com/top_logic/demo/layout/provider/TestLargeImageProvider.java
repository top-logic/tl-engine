/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.demo.layout.provider;

import com.top_logic.layout.Flavor;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.provider.MetaResourceProvider;

/**
 * A {@link MetaResourceProvider} with a large image.
 * 
 * @author <a href="mailto:iwi@top-logic.com">Isabell Wittich</a>
 */
public class TestLargeImageProvider extends MetaResourceProvider {

	@Override
	public ThemeImage getImage(Object anObject, Flavor aFlavor) {
		return ThemeImage.forTest("/demo/as-large.png");
	}
}
