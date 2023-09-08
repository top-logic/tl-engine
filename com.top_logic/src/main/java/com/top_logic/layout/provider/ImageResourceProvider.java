/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.provider;

import com.top_logic.layout.Flavor;
import com.top_logic.layout.ImageProvider;
import com.top_logic.layout.ResourceProvider;
import com.top_logic.layout.basic.ThemeImage;

/**
 * {@link ResourceProvider}, that returns label, tooltips, etc. from a (given)
 * {@link ResourceProvider} and the image from a given {@link ImageProvider}.
 * 
 * @author <a href="mailto:iwi@top-logic.com">Isabell Wittich</a>
 */
public class ImageResourceProvider extends ProxyResourceProvider {

	private ImageProvider _imageProvider;

	/**
	 * Create a new {@link ImageResourceProvider} with a given {@link ResourceProvider} and an
	 * {@link ImageProvider}.
	 */
	public ImageResourceProvider(ResourceProvider resourceProvider, ImageProvider imageProvider) {
		super(resourceProvider);
		_imageProvider = imageProvider;
	}

	/**
	 * Create a new {@link ImageResourceProvider} with a {@link ResourceProvider} given by the super
	 * class and a given {@link ImageProvider}.
	 */
	public ImageResourceProvider(ImageProvider imageProvider) {
		super();
		_imageProvider = imageProvider;
	}

	@Override
	public ThemeImage getImage(Object anObject, Flavor aFlavor) {
		return _imageProvider.getImage(anObject, aFlavor);
	}
}