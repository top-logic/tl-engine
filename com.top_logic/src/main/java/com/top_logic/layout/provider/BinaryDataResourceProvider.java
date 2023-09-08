/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.provider;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.io.binary.BinaryDataSource;
import com.top_logic.layout.AbstractResourceProvider;
import com.top_logic.layout.Flavor;
import com.top_logic.layout.ResourceProvider;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.provider.icon.BinaryDataIconProvider;

/**
 * A {@link ResourceProvider} for {@link BinaryDataSource} objects that returns the file name or
 * <code>null</code>.
 * 
 * @author <a href="mailto:msi@top-logic.com">msi</a>
 */
public class BinaryDataResourceProvider extends AbstractResourceProvider {

	@Override
	public String getLabel(Object object) {
		if (object instanceof BinaryDataSource) {
			BinaryDataSource binary = (BinaryDataSource) object;
			if (!StringServices.isEmpty(binary.getName())) {
				return binary.getName();
			}
		}
		return null;
	}

	@Override
	public ThemeImage getImage(Object anObject, Flavor aFlavor) {
		return BinaryDataIconProvider.INSTANCE.getIcon(anObject, aFlavor);
	}
}